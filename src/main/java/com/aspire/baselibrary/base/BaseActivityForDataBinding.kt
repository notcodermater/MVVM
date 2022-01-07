package com.aspire.baselibrary.base

import android.content.Context
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import com.aspire.baselibrary.R
import com.aspire.baselibrary.expand.*
import com.aspire.baselibrary.skeleton.SkeletonScreen
import com.aspire.baselibrary.util.BaseToastUtil
import com.gyf.immersionbar.ImmersionBar
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.reflect.ParameterizedType
import java.util.*

/**
 * 带databinding的基类activity
 * @author NotCoder
 * @since 2020/6/4 14:23
 */

abstract class BaseActivityForDataBinding<T : BaseActivityModel<V>, V : ViewDataBinding> :
    AppCompatActivity() {

    lateinit var binding: V
    lateinit var viewModel: T

    /***
     * 没有颜色
     */
    val MARK_NOCOLOR = -1


    /***
     * 是否开启双击关闭窗体
     */
    private var doubleClose = false

    /***
     * 上次点击时间
     */
    private var lastClickTime = 0L;

    /***
     * 点击距离
     */
    private val clickInterval = 1000L;

    /***
     * 标题设置
     */
    var tvTitle: AppCompatTextView? = null

    /***
     * 标题右边文字
     */
    private var tvTitleRight: AppCompatTextView? = null

    /***
     * 标题返回按钮
     */
    private var ivClose: View? = null

    /***
     * 上下拉初始化
     */
    public var smRefresh: SmartRefreshLayout? = null

    /***
     * 是否已经加载过数据 加载数据在onResume 防止loading无法弹出
     */
    private var hasLoad = false

    /***
     * 协程job队列
     */
    private var jobStack: Stack<Job> = Stack()

    /***
     * 骨架屏队列
     */
    private var skeletonStack: Stack<SkeletonScreen> = Stack()


    /***
     * RXJava订阅队列 两种不同类型...
     */
    private var rxStack: Stack<Disposable> = Stack()
    private var rxStack2: Stack<io.reactivex.disposables.Disposable> = Stack()

    /***
     * 本对象持有
     */
    lateinit var mContext: Context
    lateinit var mActivity: AppCompatActivity


    /***
     * 设置标题控件ID，用于自定义标题控件
     */
    open fun renameTitleIds() {
        tvTitle = binding.root.findViewById(getTitleTvTitleID())
        tvTitleRight = binding.root.findViewById(getTitleTvRightID())
        ivClose = binding.root.findViewById(getTitleIvCloseID())
        ivClose?.baseClick {
            backClick()
        }
    }

    /***
     * 设置返回按钮ID，用于自定义标题控件
     */
    open fun renameBackViewID() {
        tvTitle = binding.root.findViewById(getTitleTvTitleID())
        tvTitleRight = binding.root.findViewById(getTitleTvRightID())
        ivClose = binding.root.findViewById(getTitleIvCloseID())
        ivClose?.baseClick {
            backClick()
        }
    }

    open fun backClick() {
        finish()
    }


    /***
     * 获取标题ID
     * @return ID
     */
    open fun getTitleTvTitleID(): Int {
        return R.id.base_tvTitle
    }

    /***
     * 获取关闭按钮ID
     * @return ID
     */
    open fun getTitleIvCloseID(): Int {
        return R.id.base_ivClose
    }

    /***
     * 获取标题右侧按钮ID
     * @return ID
     */
    open fun getTitleTvRightID(): Int {
        return R.id.base_tvRight
    }

    /***
     * 屏幕状态
     * @return 默认强制竖屏
     */
    open fun getScreenStatus(): Int {
        return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
    }

    open fun needSetStatusBar(): Boolean {
        return true;
    }

    open fun getStatusBarColor(): Int {
        return R.color.base_white
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        BaseActivityStackManager.nowActivity = this
        mContext = this
        mActivity = this
        //嘿嘿 不让旋转
        requestedOrientation = getScreenStatus()
        if (needSetStatusBar()) {
            if (getStatusBarColor() != MARK_NOCOLOR) {
                ImmersionBar.with(this).transparentStatusBar().barColor(getStatusBarColor())
                    .statusBarDarkFont(true).init()
            } else {
                ImmersionBar.with(this).transparentStatusBar().init()
            }

        }

        //找寻标题注解
        val resAniMation = javaClass.getAnnotation(BaseLayoutRes::class.java)
        if (resAniMation != null) {
            binding = DataBindingUtil.setContentView<V>(this, resAniMation.redID)
            if (getFitsSystemWindows() && needSetStatusBar()) {
                ImmersionBar.setFitsSystemWindows(this)
            }
            initEventBus()

            val moduleClass: Class<T>
            val type = javaClass.genericSuperclass
            moduleClass = if (type is ParameterizedType) {
                type.actualTypeArguments[0] as Class<T>
            } else {
                BaseActivityModel::class.java as Class<T>
            }

            renameTitleIds()
            renameRefreshId()
            renameBackViewID()

            viewModel = ViewModelProvider(this).get<T>(moduleClass)
            viewModel.init(this, binding, smRefresh)
            binding.setVariable(getBRmoduleId(), viewModel)
        }


    }


    /***
     * 获取BR的ID
     */
    abstract fun getBRmoduleId(): Int

    /***
     * 初始化布局
     */
    open fun initView() {
        smRefresh?.setOnRefreshListener {
            onpageDown()
        }

        smRefresh?.setOnLoadMoreListener {
            jobStack.push(GlobalScope.launch() {
                onpageUp()
            })
        }

        //找寻标题注解
        val titleAniMation = javaClass.getAnnotation(BaseActivityTitle::class.java)
        titleAniMation?.apply {
            setMTitle(title)
        }

    }

    /***
     * 初始化数据
     */
    open fun initData() {

    }

    /***
     * 是否需要注册eventBus
     */
    open fun registerEventBus(): Boolean {
        return false
    }

    /***
     * 接收到的event消息
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    open fun receiveMessage(message: BaseBeanMessage) {
    }

    /***
     * 设置标题栏内容
     */
    fun setMTitleRight(resId: Int, func: () -> Unit) {
        baseCloseSoftKeyboard(this)
        tvTitleRight?.setText(resId)
        tvTitleRight?.visibility = View.VISIBLE
        tvTitleRight?.setOnClickListener {
            func()
        }
    }


    override fun onResume() {
        super.onResume()
        if (!hasLoad) {
            initView()
            initData()
            hasLoad = true
        }
    }

    /***
     * 设置标题栏内容
     */
    fun setMTitle(res: String?) {
        if (res != null) {
            tvTitle?.text = res
            tvTitle?.baseBlod()
        }
    }

    /***
     * 设置标题栏内容
     */
    fun setMTitle(resId: Int) {
        tvTitle?.setText(resId)
        tvTitle?.baseBlod()
    }

    override fun onDestroy() {
        super.onDestroy()
        baseLoadingDismiss()
        if (registerEventBus()) {
            EventBus.getDefault().unregister(this)
        }

        rxStack.indices.map {
            rxStack.pop().dispose()
        }

        rxStack2.indices.map {
            rxStack2.pop().dispose()
        }

        jobStack.indices.map {
            val it = jobStack.pop()
            if (it != null && it.isActive) {
                it.cancel()
            }
        }
        skeletonStack.indices.map {
            skeletonStack.pop()?.hide()
        }
    }

    /***
     * s
     */
    fun addRxStack(dispos: Disposable) {
        rxStack.push(dispos)
    }

    fun addRxStack(dispos: io.reactivex.disposables.Disposable) {
        rxStack2.push(dispos)
    }


    fun addSkeletonStack(dispos: SkeletonScreen) {
        skeletonStack.push(dispos)
    }

    fun doubleClose() {
        doubleClose = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (doubleClose) {
                var time = System.currentTimeMillis()
                Log.e(
                    "123123123",
                    (time - lastClickTime).toString() + "" + ((time - lastClickTime) >= clickInterval)
                )
                return if ((time - lastClickTime) >= clickInterval) {
                    BaseToastUtil.showToastShort(res = R.string.base_again_exit)
                    lastClickTime = time
                    true
                } else {
                    finish()
                    Log.e("123123123", "结束")

                    true
                }
            }
        }

        return super.onKeyDown(keyCode, event)
    }

    open fun getFitsSystemWindows(): Boolean {
        return true
    }


    /***
     * 设置下拉刷新列表id
     */
    open fun renameRefreshId(id: Int = R.id.base_smRefresh) {
        smRefresh = binding.root.findViewById(id)
        smRefresh?.setEnableLoadMore(canLoadMore())
    }

    open fun canLoadMore(): Boolean {
        return false
    }

    /***
     * 下拉刷新
     */
    open fun onpageDown() {


    }

    /***
     * 上拉加载
     */
    open fun onpageUp() {}


    fun disMissSkeleton() {
        if (skeletonStack.size > 0) {
            var data = skeletonStack.pop()
            if (data != null) {
                data.hide()
            }
        }
    }

    /***
     * 添加协程job队列 用来统一cancel
     */
    fun addJobStack(job: Job) {
        jobStack.push(job)
    }

    fun getFoucs() {

    }

    /***
     * 注册eventBus
     */
    private fun initEventBus() {
        if (registerEventBus()) {
            EventBus.getDefault().register(this)
        }
    }
}