package com.aspire.baselibrary.base

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.aspire.baselibrary.R
import com.aspire.baselibrary.expand.baseBlod
import com.aspire.baselibrary.expand.baseClick
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.Job
import org.greenrobot.eventbus.EventBus
import java.lang.reflect.ParameterizedType
import java.util.*

abstract class BaseFragmentForDataBinding<T : BaseActivityModel<V>, V : ViewDataBinding> :
    Fragment() {

    lateinit var binding: V
    lateinit var viewModel: T

    /***
     * 标题设置
     */
    var tvTitle: AppCompatTextView? = null
    var ivClose: AppCompatImageView? = null

    /***
     * 上下拉初始化
     */
    var smRefresh: SmartRefreshLayout? = null

    /***
     * RXJava订阅队列
     */
    var rxStack: Stack<Disposable> = Stack()

    /***
     * 协程job队列
     */
    var jobStack: Stack<Job> = Stack()

    /***
     * fragment的根布局
     */
    lateinit var rootView:View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, getLayoutId(), container, false);
        val moduleClass: Class<T>
        val type = javaClass.genericSuperclass
        moduleClass = if (type is ParameterizedType) {
            type.actualTypeArguments[0] as Class<T>
        } else {
            BaseActivityModel::class.java as Class<T>
        }


        renameRefreshId()
        renameTitleId()
        renameBackImgId()


        viewModel = ViewModelProvider(this).get<T>(moduleClass)
        viewModel.init( requireActivity() as BaseActivityForDataBinding<*, *>,binding, smRefresh)
        binding.setVariable(getBRmoduleId(), viewModel)

        ivClose.baseClick {
            requireActivity().finish()
        }
        rootView=binding.root


        smRefresh?.setOnRefreshListener {
            onpageDown()
        }
        smRefresh?.setOnLoadMoreListener {
            onpageUp()
        }
        if (autoRefresh()) {
            smRefresh?.autoRefresh()
        }

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initView()
        initData()
    }

    abstract fun getLayoutId(): Int
    abstract fun getBRmoduleId(): Int

    open fun initView() {

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (registerEventBus()) {
            EventBus.getDefault().register(this)
        }
    }


    open fun initData() {

    }

    /***
     * 设置返回按钮id
     */
    open fun renameBackImgId(id: Int = R.id.base_ivClose) {
        ivClose = binding.root.findViewById(id)
    }


    override fun onDestroy() {
        super.onDestroy()
        if (rxStack.size > 0) {
            for (ind in 0 until rxStack.size) {
                val it = rxStack.pop()
                if (!it.isDisposed) {
                    it.dispose()
                }

            }
        }
        if (jobStack.size > 0) {
            for (ind in 0 until jobStack.size) {
                val it = jobStack.pop()
                if (it != null && it.isActive) {
                    it.cancel()
                }
            }
        }

        if (registerEventBus()) {
            EventBus.getDefault().unregister(this)
        }
    }

    /***
     * 添加rxjava订阅队列 用于没有生命周期的订阅
     */
    fun addRxStack(dispos: Disposable) {
        rxStack.push(dispos)
    }

    /***
     * 设置标题栏内容
     */
    fun setMTitle(resId: Int) {
        tvTitle?.setText(resId)
        tvTitle?.baseBlod()
    }

    /***
     * 设置titleid
     */
    open fun renameTitleId(id: Int = R.id.base_tvTitle) {
        tvTitle = binding.root.findViewById(id)
    }

    /***
     * 设置下拉刷新列表id
     */
    open fun renameRefreshId(id: Int = R.id.base_smRefresh) {
        smRefresh = binding.root.findViewById(id)
        if (canLoadMore()) {
            smRefresh?.setEnableLoadMore(true)
        } else {
            smRefresh?.setEnableLoadMore(false)
        }
    }

    /***
     * 下拉刷新
     */
    open fun onpageDown() {
    }


    /***
     * 上拉加载
     */
    open fun onpageUp() {
    }


    /***
     * 是否注册eventbus
     */
    open fun registerEventBus(): Boolean {
        return false
    }

    /***
     * 是否可以加载更多
     */
    open fun canLoadMore(): Boolean {
        return false
    }

    /***
     * 是否自动下拉
     */
    open fun autoRefresh(): Boolean {
        return true
    }

    override fun onResume() {
        super.onResume()
        Log.e("123", "当前激活的fragment" + this::class.java.name)
    }
}