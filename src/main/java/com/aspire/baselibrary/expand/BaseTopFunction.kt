package com.aspire.baselibrary.expand

import android.app.Activity
import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.edit
import androidx.core.view.children
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.afollestad.materialdialogs.DialogCallback
import com.afollestad.materialdialogs.LayoutMode
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.bottomsheets.BottomSheet
import com.afollestad.materialdialogs.lifecycle.lifecycleOwner
import com.afollestad.materialdialogs.list.ItemListener
import com.afollestad.materialdialogs.list.listItems
import com.alibaba.android.arouter.launcher.ARouter
import com.aspire.baselibrary.R
import com.aspire.baselibrary.base.BaseActivityForDataBinding
import com.aspire.baselibrary.base.BaseActivityStackManager
import com.aspire.baselibrary.base.BaseApplication
import com.aspire.baselibrary.base.BaseActivityModel
import com.aspire.baselibrary.expand.SomeAttr.delayLong
import com.aspire.baselibrary.expand.SomeAttr.loadingFragment
import com.aspire.baselibrary.util.BaseToastUtil
import com.aspire.baselibrary.view.BaseLoadingDialogFragment
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.coorchice.library.SuperTextView
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding2.view.RxView
import com.lzy.okgo.https.HttpsUtils
import io.reactivex.functions.Consumer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okio.Buffer
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import rxhttp.wrapper.param.*
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession


/***
 * ?????????????????????
 * @param activity ??????????????????
 * @param list ???????????? ????????????
 * @param title dialog ?????? ????????????
 * @param message ???????????? ????????????
 * @param positiveText ?????????????????? ????????????
 * @param negativeText ?????????????????? ????????????
 * @param waitPositiveClick ???????????????????????????????????????????????????????????? ??????false
 * @param listItemClick lambda ????????????????????????
 * @param positiveListener lambda ????????????????????????
 * @param negativeListener ????????????????????????
 */
fun baseShowBottomSheetDialog(
    activity: AppCompatActivity,
    list: List<String>? = null,
    title: String? = null,
    message: String? = null,
    positiveText: String? = null,
    negativeText: String? = null,
    waitPositiveClick: Boolean = false,
    positiveListener: ((dialog: MaterialDialog) -> Unit)? = null,
    negativeListener: ((dialog: MaterialDialog) -> Unit)? = null,
    listItemClick: ((index: Int, txt: String) -> Unit)? = null
) {
    MaterialDialog(activity, BottomSheet(LayoutMode.WRAP_CONTENT)).show {
        title?.let {
            title(text = it)
        }
        message?.let {
            message(text = it)
        }
        positiveText?.let {
            positiveButton(text = positiveText, click = object : DialogCallback {
                override fun invoke(p1: MaterialDialog) {
                    positiveListener?.invoke(p1)
                }
            })
        }
        negativeText?.let {
            negativeButton(text = negativeText, click = object : DialogCallback {
                override fun invoke(p1: MaterialDialog) {
                    negativeListener?.invoke(p1)
                }
            })
        }
        list?.let {
            listItems(
                items = list,
                waitForPositiveButton = waitPositiveClick,
                selection = object : ItemListener {
                    override fun invoke(dialog: MaterialDialog, index: Int, text: CharSequence) {
                        listItemClick?.invoke(index, text.toString())
                    }
                })
        }
        lifecycleOwner(activity)
    }

}


/***?????????????????????
 * ??????????????????????????????
 */
object SomeAttr {
    var isDebug = false

    /***
     * loading??????????????? delaylong??????????????? ??????loading??????
     */
    var loadingFragment = BaseLoadingDialogFragment()
    var delayLong = 600L
}

/***
 * ??????????????????
 */
open class BaseListClickListener {
    /***
     * ??????????????????
     */
    open fun click(pos: Int, binding: ViewDataBinding) {}

    /***
     *  ??????view??????????????????
     */
    open fun inject(pos: Int, view: View) {}
}


/***
 * ??????debug??????
 */
fun Application.baseSetDebug(debug: Boolean) {
    SomeAttr.isDebug = debug
}

/***
 * ??????debug
 */
fun baseIsDebug(): Boolean {
    return true
}


fun AppCompatActivity.baseLog(str: String) {
    Log.e(this::class.java.name, str)
}


/***
 * ????????????????????????????????????
 */
fun baseNetworkException(
    activity: AppCompatActivity,
    callback: ((throwable: Throwable) -> Unit)? = null
): CoroutineExceptionHandler {
    return CoroutineExceptionHandler { _, throwable ->
        baseLoadingDismiss()
        activity.runOnUiThread {
            if (callback != null) {
                callback(throwable)
            } else {
                when (throwable.javaClass.name) {
                    "java.net.ConnectException" -> {
                        BaseToastUtil.showToastShort("????????????,???????????????")
                    }
                    else -> {
                        if (throwable.message.toString().baseNotEmpty()) {
                            BaseToastUtil.showToastShort(throwable.message.toString())
                        }
                    }
                }
            }
        }
    }
}

/***
 * rxJava????????????
 */
fun baseRxTimer(
    delay: Long,
    doSome: (disponse: Disposable) -> Unit,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS
): Disposable {
    var able: Disposable? = null

    able = io.reactivex.rxjava3.core.Observable.interval(delay, timeUnit)
        .observeOn(AndroidSchedulers.mainThread()).subscribe {
            doSome(able!!)
        }
    doSome(able!!)
    return able
}

/***
 * rxJava??????timeout
 */
fun baseRxTimeOut(
    delay: Long,
    doSome: () -> Unit,
    timeUnit: TimeUnit = TimeUnit.MILLISECONDS
): Disposable {
    var able = io.reactivex.rxjava3.core.Observable.timer(delay, timeUnit)
        .observeOn(AndroidSchedulers.mainThread()).subscribe {
            doSome()
        }
    return able
}


/***
 * ????????????????????????
 */
fun String?.baseEmpty(): Boolean {
    if (this == null || this.isEmpty()) {
        return true
    }
    return false
}

/***
 * ???????????????????????????
 */
fun String?.baseNotEmpty(): Boolean {
    if (this == null || this.isEmpty()) {
        return false
    }
    return true
}

/***
 * ????????????
 */
fun baseGetColor(resId: Int): Int {
    return BaseApplication.INSTANCE.applicationContext.resources.getColor(resId)
}

/***
 * ????????????
 */
fun baseGetDrawable(resId: Int): Drawable {
    return BaseApplication.INSTANCE.applicationContext.resources.getDrawable(resId)
}

fun BaseGetYMDFromat(): SimpleDateFormat {
    return SimpleDateFormat("yyyy-MM-dd")
}

/***
 * ????????????
 */
fun baseGetString(resId: Int): String {
    return BaseApplication.INSTANCE.applicationContext.resources.getString(resId)
}

/***
 * ??????root??????
 */
fun baseSuExusecmd(command: String): Boolean {
    var process: Process? = null
    var os: DataOutputStream? = null
    try {
        process = Runtime.getRuntime().exec("su")
        os = DataOutputStream(process.outputStream)
//        mount -o rw,remount /system
        os.writeBytes(
            """
                $command
                
                """.trimIndent()
        )
        os.writeBytes("exit\n")
        os.flush()
        Log.e("updateFile", "======000==writeSuccess======")
        process.waitFor()
    } catch (e: java.lang.Exception) {
        Log.e("updateFile", "======111=writeError======$e")
        return false
    } finally {
        try {
            os?.close()
            process?.destroy()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }
    return true
}

/***
 * ??????????????????
 */
fun baseGetShakeAnimation(counts: Int): Animation? {
    val translateAnimation: Animation = TranslateAnimation(0f, 3f, 0f, 0f)
    translateAnimation.interpolator = CycleInterpolator(counts.toFloat())
    translateAnimation.repeatCount = 1
    translateAnimation.duration = 400
    return translateAnimation
}


/***
 * ????????????????????? ???????????????
 */
fun String?.baseIsMobile(): Boolean {
    if (this == null) return false
    return this?.length == 11
}

/***
 * ?????????????????????
 */
abstract class BaseItemClickListener {
    open fun onItemClick(item: Any) {}
    open fun onItemClick(position: Int) {}
    open fun onItemClick(position: Int, type: Int) {}
    open fun onItemClick(position: Int, type: Int, item: Any) {}
}

/***
 * dp??????px
 */
fun baseDp2px(dp: Float): Float {
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        BaseApplication.INSTANCE!!.resources.displayMetrics
    )
}


/***
 * ????????????????????????
 * @param viewList ?????????view??????
 * @param clickCommand ????????????????????? ??????lambda {}
 * @param isThrottleFirst ????????????????????????
 */
fun baseClick(viewList: List<View?>, clickCommand: () -> Unit, isThrottleFirst: Boolean = true) {
    viewList.map {
        var view = it
        if (view != null) {
            if (isThrottleFirst) {
                RxView.clicks(view)
                    .subscribe(object : Consumer<Any> {
                        @Throws(Exception::class)
                        override fun accept(`object`: Any) {
                            clickCommand()
                        }
                    })
            } else {
                RxView.clicks(view)
                    .throttleFirst(600, TimeUnit.MILLISECONDS)//1????????????????????????1???
                    .subscribe {
                        clickCommand()
                    }
            }
        }
    }

}

/***
 * ????????????
 * @param view ?????????view
 * @param clickCommand ????????????????????? ??????lambda {}
 * @param isThrottleFirst ????????????????????????
 */
fun View?.baseClick(
    clickCommand: () -> Unit,
    isThrottleFirst: Boolean = true
): io.reactivex.disposables.Disposable? {
    if (this == null) {
        return null
    }
    var obs = RxView.clicks(this)
    if (isThrottleFirst) {
        return obs.subscribe {
            clickCommand()
        }
    } else {
        return obs.throttleFirst(600, TimeUnit.MILLISECONDS)//1????????????????????????1???
            .subscribe {
                clickCommand()
            }
    }
}


/***
 * ???????????? ???????????????????????? ?????????lambda {}
 * @param view ?????????view
 * @param clickCommand ????????????????????? ??????lambda {}
 */
fun View?.baseClick(
    clickCommand: (() -> Unit)?
): io.reactivex.disposables.Disposable? {
    if (this == null) {
        return null
    }
    var obs = RxView.clicks(this!!)
    return obs.subscribe {
        if (clickCommand != null) {
            clickCommand()
        }
    }
//    this?.setOnClickListener {
//        clickCommand?.invoke()
//    }

}

/***
 * ??????Activity
 * @param cls ????????????activity???
 * @param params ?????????????????? key to value
 */
fun baseStartActivity(cls: Class<out AppCompatActivity>?, params: Map<String, Any>? = null) {
    if (cls == null) {
        return
    }
    var intent = Intent(BaseApplication.INSTANCE.applicationContext, cls)
    var bundle = Bundle()
    params?.entries?.map {
        if (it.value is String) {
            bundle.putString(it.key, it.value.toString())
        } else if (it.value is Int) {
            bundle.putInt(it.key, it.value as Int)
        } else if (it.value is Boolean) {
            bundle.putBoolean(it.key, it.value as Boolean)
        } else if (it.value is Float) {
            bundle.putFloat(it.key, it.value as Float)
        } else if (it.value is Double) {
            bundle.putDouble(it.key, it.value as Double)
        } else if (it.value is Serializable) {
            bundle.putSerializable(it.key, it.value as Serializable)
        }
    }
    intent.putExtras(bundle)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
    BaseApplication.INSTANCE.applicationContext!!.startActivity(intent)
}


/***
 * ??????Activity
 * @param params ?????????????????? key to value
 */
fun Activity.baseSetResult(params: Map<String, Any>) {
    var intent = Intent()
    var bundle = Bundle()
    params.entries.map {
        when (it.value) {
            is String -> {
                bundle.putString(it.key, it.value.toString())
            }
            is Int -> {
                bundle.putInt(it.key, it.value as Int)
            }
            is Boolean -> {
                bundle.putBoolean(it.key, it.value as Boolean)
            }
            is Float -> {
                bundle.putFloat(it.key, it.value as Float)
            }
            is Double -> {
                bundle.putDouble(it.key, it.value as Double)
            }
            is Serializable -> {
                bundle.putSerializable(it.key, it.value as Serializable)
            }
        }
    }
    intent.putExtras(bundle)
    this.setResult(Activity.RESULT_OK, intent)
    finish()
}

/***
 * ??????tablayout?????????
 * @param selectColor ??????????????????
 * @param normalColor ??????????????????
 * @param list ?????????????????????
 */
fun TabLayout.baseBlodTitle(
    selectColor: Int = Color.parseColor("#59a1ff"),
    normalColor: Int = Color.BLACK,
    list: List<String>,
    isBlod: Boolean = true,
    textSizeSelect: Float = 16f,
    textSizeUnSelect: Float = 14f
) {
    for (i in 0 until this.tabCount) {
        val view = AppCompatTextView(BaseApplication.INSTANCE.applicationContext)
        view.textSize = 16f
        view.text = list[i]
        view.gravity = Gravity.CENTER
        view.setPadding(30, 0, 30, 0)
        var params = LinearLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )
        params.setMargins(0, 0, 0, 0)
        this.isTabIndicatorFullWidth = false
        if (this.getTabAt(i)!!.isSelected) {
            view.setTextColor(selectColor)
            if (textSizeSelect != -1f) {
                view.setTextSize(textSizeSelect)
            }
        } else {
            view.setTextColor(normalColor)
            if (textSizeUnSelect != -1f) {
                view.setTextSize(textSizeUnSelect)
            }
        }
        if (isBlod) {
            view.baseBlod()
        }
        this.getTabAt(i)?.setCustomView(view)
        view.layoutParams = params

    }

    this.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
        override fun onTabReselected(p0: TabLayout.Tab?) {

        }

        override fun onTabUnselected(p0: TabLayout.Tab?) {
            var tv = p0!!.customView as TextView
            tv.setTextColor(normalColor)
            if (textSizeUnSelect != -1f) {
                tv.setTextSize(textSizeUnSelect)
            }
        }

        override fun onTabSelected(p0: TabLayout.Tab?) {
            var tv = p0!!.customView as TextView
            tv.setTextColor(selectColor)
            if (textSizeSelect != -1f) {
                tv.setTextSize(textSizeSelect)
            }
        }

    })

}

/***
 * ???????????????????????????
 * @param fragmentManager ???????????????
 * @param listFragments fragment??????
 */
fun ViewPager.basesetAdaper(
    fragmentManager: FragmentManager,
    listFragments: List<Fragment>,
    params: Map<String, Any>? = null
) {
    this.adapter =
        object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                var fragment = listFragments[position]


                var bundle = Bundle()
                params?.entries?.map {
                    if (it.value is String) {
                        bundle.putString(it.key, it.value.toString())
                    } else if (it.value is Int) {
                        bundle.putInt(it.key, it.value as Int)
                    } else if (it.value is Boolean) {
                        bundle.putBoolean(it.key, it.value as Boolean)
                    } else if (it.value is Float) {
                        bundle.putFloat(it.key, it.value as Float)
                    } else if (it.value is Double) {
                        bundle.putDouble(it.key, it.value as Double)
                    } else if (it.value is Serializable) {
                        bundle.putSerializable(it.key, it.value as Serializable)
                    }
                }
                if (params != null) {
                    fragment.arguments = bundle
                }
                return fragment
            }

            override fun getCount(): Int {
                return listFragments.size
            }
        }
}


/***
 * ??????????????????????????????
 * @param fragmentManager ???????????????
 * @param fragment ??????
 * @param listTitle ????????????
 */
fun ViewPager.basesetAdaper(
    fragmentManager: FragmentManager,
    listFragments: List<Fragment>,
    listTitle: List<String>
) {
    this.adapter =
        object : FragmentPagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
            override fun getItem(position: Int): Fragment {
                return listFragments[position]
            }

            override fun getCount(): Int {
                return listFragments.size
            }

            override fun getPageTitle(position: Int): CharSequence? {
                return listTitle[position]
            }
        }
}


/***
 * ??????string???long ??????????????????
 */
fun String?.baseLong(): Long {
    if (this == null) {
        return 0
    }
    return this.toLong()
}

/***
 * ??????string???int ??????????????????
 */
fun String?.baseInt(): Int {
    if (this == null || this == "") {
        return 0
    }
    return try {
        this.toInt()
    } catch (e: java.lang.NumberFormatException) {
        0
    }

}


/***
 * textview??????
 */
fun AppCompatTextView.baseBlod() {
    var textPaint = this.paint
    textPaint.isFakeBoldText = true
}

/***
 * textview??????
 */
fun TextView.baseBlod() {
    var textPaint = this.paint
    textPaint.isFakeBoldText = true
}

/***
 * edittextview??????
 */
fun AppCompatEditText.baseBlod() {
    var textPaint = this.paint
    textPaint.isFakeBoldText = true
}


/***
 * ??????intent???????????????
 */
fun <T> AppCompatActivity.baseGetIntentParam(paramName: String): T {
    var extras = intent.extras
    return (this.intent.extras!![paramName] as T)
}

/***
 * ??????intent???????????????
 */
fun <T> AppCompatActivity.baseGetIntentParamNull(paramName: String): T? {
    var extras = intent.extras
    return (this.intent.extras!![paramName] as T)
}

/***
 * ??????intent???????????????
 */
fun <T> Activity.baseGetIntentParamNull(paramName: String): T? {
    var extras = intent.extras
    return (this.intent.extras!![paramName] as T)
}

/***
 * ???????????? ??????arouter????????????
 * @param path ??????????????????
 * @param param ?????????????????????
 * @param clean ?????????????????????activity??? ???????????????
 * @param requstCode ?????????
 * @param activity ??????requstCode???????????????
 */
fun go(
    path: String,
    param: Map<String, Any?>? = null,
    clean: Boolean = false,
    requstCode: Int? = null,
    activity: AppCompatActivity? = null
) {

    var postCard = ARouter.getInstance().build(path)
//    if (clean) {
////        postCard.withFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//    }else{
////        postCard.withFlags( Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
//    }

    param?.map {
        if (it.value is String) {
            postCard.withString(it.key, it.value as String)
        } else if (it.value is Int) {
            postCard.withInt(it.key, it.value as Int)
        } else if (it.value is Boolean) {
            postCard.withBoolean(it.key, it.value as Boolean)
        } else if (it.value is ByteArray) {
            postCard.withByteArray(it.key, it.value as ByteArray)
        } else if (it.value is CharArray) {
            postCard.withCharArray(it.key, it.value as CharArray)
        } else if (it.value is IntArray) {
            postCard.withIntegerArrayList(it.key, it.value as ArrayList<Int>)
        } else if (it.value is ArrayList<*>) {
            postCard.withSerializable(it.key, it.value as ArrayList<*>)
        } else {
            postCard.withSerializable(it.key, it.value as Serializable)
        }
    }


    if (clean) {
        BaseActivityStackManager.finishAll()
        activity?.finish()
    }

    if (requstCode != null) {
        postCard.navigation(activity, requstCode!!)
    } else {
        postCard.navigation()
    }

}

/***
 * ????????????
 * @param url ????????????
 * @param view ?????????imageview
 * @param placeHolder ???????????????????????????
 */
fun baseLoadHead(url: String, view: ImageView, placeHolder: Int = R.drawable.base_defhead) {

    var drawable: Drawable =
        BaseApplication.INSTANCE.resources.getDrawable(placeHolder, null)
    var transforms: RequestBuilder<Drawable> =
        Glide.with(BaseApplication.INSTANCE).load(drawable).circleCrop()
    Glide.with(BaseApplication.INSTANCE)
        .load(url)
        .circleCrop()
        .thumbnail(transforms)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)

}

/***
 * ?????????????????? ?????????
 * @param url ????????????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquare(url: String, view: ImageView, placeHolder: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(url)
        .into(view)
}

/***
 * ?????????????????? ?????????
 * @param url ????????????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquare(url: Int, view: ImageView, placeHolder: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(url)
        .into(view)
}

/***
 * ?????????????????? ?????????
 * @param url ????????????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquareNoCache(url: Int, view: ImageView, placeHolder: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(url)
        .diskCacheStrategy(
            DiskCacheStrategy.NONE
        ).skipMemoryCache(true)
        .into(view)
}

/***
 * ?????????????????? ?????????
 * @param url ????????????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquare(url: Uri, view: ImageView, placeHolder: Int = 0, corner: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    options.transform(RoundedCorners(baseDp2px(corner.toFloat()).toInt()))

    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(url)
        .into(view)
}

/***
 * ?????????????????? ?????????
 * @param url ????????????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquare(url: String, view: ImageView, placeHolder: Int = 0, corner: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    options.transform(RoundedCorners(baseDp2px(corner.toFloat()).toInt()))

    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(url)
        .into(view)
}


/***
 * ?????????????????? ?????????
 * @param file ??????
 * @param view ImageView
 * @param placeHolder ?????????????????????
 */
fun baseLoadSquare(file: File, view: ImageView, placeHolder: Int = 0, corner: Int = 0) {
    var options = RequestOptions()
    if (placeHolder != 0) {
        options.placeholder(placeHolder)
        options.error(placeHolder)
    }
    options.transform(RoundedCorners(baseDp2px(corner.toFloat()).toInt()))

    Glide.with(BaseApplication.INSTANCE).applyDefaultRequestOptions(options).load(file)
        .into(view)
}

/***
 * ????????????
 * @param resId ??????Id
 * @param parent ????????? ?????????
 */
fun baseInflate(resId: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(BaseApplication.INSTANCE).inflate(resId, parent, false)
}

/***
 * ????????????
 * @param resId ??????Id
 * @param parent ????????? ?????????
 */
fun baseInflate(activity: AppCompatActivity, resId: Int, parent: ViewGroup? = null): View {
    return LayoutInflater.from(activity).inflate(resId, parent, false)
}

/***
 * ????????? ???????????????????????????????????????
 */
fun <T> T?.baseGetValue(): T {
    if (this is String?) {
        if (this == null || this.equals("null")) {
            return "" as T
        }
    } else if (this is Number?) {
        if (this == null) {
            return 0 as T
        }
    }
    return this!!
}

/***
 * ??????loading
 * @param ?????????activity
 *  ????????? ????????????Activity????????????????????????id??? base_root ???????????????????????????????????????show????????? ??????activity token??????
 */
suspend fun baseLoadingShowBase(activity: AppCompatActivity, text: String = "?????????") {
    if (!loadingFragment.isAdded && !loadingFragment.isVisible && !loadingFragment.isRemoving) {
        loadingFragment.show(activity.supportFragmentManager, "")
    }
}


/***
 * ??????loading
 * @param ?????????activity
 *  ????????? ????????????Activity????????????????????????id??? base_root ???????????????????????????????????????show????????? ??????activity token??????
 */
suspend fun baseLoadingShowSingle(
    activity: AppCompatActivity,
    text: String = "?????????"
): BaseLoadingDialogFragment {
    var fragment = BaseLoadingDialogFragment()
    fragment.show(activity.supportFragmentManager, "")
    return fragment
}


/***
 *  ??????loading
 */
fun baseLoadingDismiss() {
    GlobalScope.launch(Dispatchers.Main) {
        delay(500)
        if (loadingFragment.dialog != null) {
            loadingFragment.dismiss()
        }
    }
}


/***
 * ???databinding???activity??????showloading??????
 */
fun baseLoadingShow(activity: AppCompatActivity) {
    GlobalScope.launch(Dispatchers.Main) {
        baseLoadingShowBase(activity)
    }
}

/***
 * ???databinding???activity??????showloading??????
 */
suspend fun BaseActivityForDataBinding<*, *>.baseLoadingShow() {
    baseLoadingShowBase(this)
    delay(delayLong)
}

/***
 * ???databinding???activity??????showloading??????
 */
fun BaseActivityForDataBinding<*, *>.baseLoadingShowNornal() {
    GlobalScope.launch {
        baseLoadingShow()
        delay(delayLong)
    }
}

/***
 * ???databinding???activity??????showloading??????
 */
suspend fun BaseActivityModel<*>.baseLoadingShow() {
    baseLoadingShowBase(this.mActivity)
}

/**
 * ??????????????????Base64??????????????????
 */
fun baseImageToBase64(path: String?): String? {
    if (path.baseEmpty()) {
        return null
    }
    var `is`: InputStream? = null
    var data: ByteArray? = null
    var result: String? = null
    try {
        `is` = FileInputStream(path)
        //???????????????????????????????????????
        data = ByteArray(`is`.available())
        //????????????
        `is`.read(data)
        //????????????????????????????????????
        result = Base64.encodeToString(data, Base64.NO_CLOSE)
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    } finally {
        if (null != `is`) {
            try {
                `is`.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
    return result
}


/***
 * ??????lifecleowner
 * @param owner owner
 */
fun MaterialDialog.lifecycleOwner(owner: LifecycleOwner? = null): MaterialDialog {
    val observer =
        DialogLifecycleObserver(::dismiss)
    val lifecycleOwner = owner ?: (windowContext as? LifecycleOwner
        ?: throw IllegalStateException(
            "$windowContext is not a LifecycleOwner."
        ))
    lifecycleOwner.lifecycle.addObserver(observer)
    return this
}

/***
 * ??????lifecleowner
 * @param dismiss ????????????
 */
internal class DialogLifecycleObserver(private val dismiss: () -> Unit) : LifecycleObserver {
    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() = dismiss()

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() = dismiss()
}

/***
 * ????????????sharepreference
 * @param preferenceName ????????????
 * @param key  ???????????????
 * @param clazz ???????????????
 */
fun <T> baseGetLocal(preferenceName: String, key: String, clazz: Class<T>): T? {
    var tins: T? = null
    try {
        tins = clazz.newInstance()
    } catch (e: java.lang.Exception) {
    }
    if (tins != null) {
        if (tins is String) {
            return BaseApplication.INSTANCE.getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
                .getString(key, "") as T
        } else if (tins is Int) {
            return BaseApplication.INSTANCE.getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
                .getInt(key, 0) as T
        } else if (tins is Boolean) {
            return BaseApplication.INSTANCE.getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
                .getBoolean(key, false) as T
        } else if (tins is Float) {
            return BaseApplication.INSTANCE.getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
                .getFloat(key, 0.0f) as T
        } else if (tins is Long) {
            return BaseApplication.INSTANCE.getSharedPreferences(
                preferenceName,
                Context.MODE_PRIVATE
            )
                .getLong(key, 0L) as T
        }
    }
    try {
        var obj = Base64.decode(
            BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                .getString(key, ""), android.util.Base64.DEFAULT
        )
        val bais = ByteArrayInputStream(obj)
        val ois = ObjectInputStream(bais)
        return ois.readObject() as T
    } catch (e: java.lang.Exception) {
    }
    return null as T
}

/***
 * ???????????????sharepreference
 * @param preferenceName ????????????
 * @param key  ???????????????
 * @param value ???????????????
 */
fun baseSaveLcoal(preferenceName: String, key: String, value: Any?) {
    if (value is String) {
        BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putString(key, value)
            }
    } else if (value is Int) {
        BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putInt(key, value)
            }
    } else if (value is Boolean) {
        BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putBoolean(key, value)
            }
    } else if (value is Float) {
        BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putFloat(key, value)
            }
    } else if (value is Long) {
        BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putLong(key, value)
            }
    } else {
        //?????????????????????
        val baos = ByteArrayOutputStream()
        //???????????????????????????
        var out: ObjectOutputStream? = null
        try {
            out = ObjectOutputStream(baos)
            out.writeObject(value)
            val objectVal =
                String(
                    android.util.Base64.encode(
                        baos.toByteArray(),
                        android.util.Base64.DEFAULT
                    )
                )
            BaseApplication.INSTANCE.getSharedPreferences(preferenceName, Context.MODE_PRIVATE)
                .edit(commit = true) {
                    putString(key, objectVal)
                }
        } catch (e: java.lang.Exception) {
        } finally {
            baos?.close()
            out?.close()
        }

    }
}

/***
 * ???viewgroup???????????????
 */
fun ViewGroup.baseAddEmptyView(
    bgColor: Int = -1,
    bgColorResId: Int = -1,
    emptyViewID: Int = R.layout.base_empty
) {
    this.children.forEach {
        if (it.tag == "empty") {
            return
        }
    }
    this.children.forEach {
        if (it.visibility == View.VISIBLE) {
            it.visibility = View.GONE
            it.tag = "change"
        }
    }
    var emptyView = baseInflate(emptyViewID, this)
    when {
        bgColor != -1 -> {
            emptyView.setBackgroundColor(bgColor)
        }
        bgColorResId != -1 -> {
            emptyView.setBackgroundResource(bgColorResId)
        }
        else -> {
            emptyView.setBackgroundColor(
                baseGetColor(
                    R.color.base_gray_bg
                )
            )
        }
    }
    emptyView.tag = "empty"
    this.addView(emptyView, 0)
}


/***
 * ???viewgroup????????????????????????
 */
fun ViewGroup.baseAddNetworkErrorView(
    bgColor: Int = -1,
    bgColorResId: Int = -1,
    networkErrorViewID: Int = R.layout.base_network_error,
    doSome: (() -> Unit)? = null
) {
    this.children.forEach {
        if (it.tag == "empty") {
            return
        }
    }
    this.children.forEach {
        if (it.visibility == View.VISIBLE) {
            it.visibility = View.GONE
            it.tag = "change"
        }
    }
    var view = baseInflate(networkErrorViewID, this)
    when {
        bgColor != -1 -> {
            view.setBackgroundColor(bgColor)
        }
        bgColorResId != -1 -> {
            view.setBackgroundResource(bgColorResId)
        }
        else -> {
            view.setBackgroundColor(
                baseGetColor(
                    R.color.base_gray_bg
                )
            )
        }
    }
    view.findViewById<SuperTextView>(R.id.tvTryAgain).baseClick {
        baseRemoveEmptyAndNetWorkErrorView()
        doSome?.invoke()
    }
    view.tag = "empty"
    this.addView(view, 0)
}


/***
 * ???viewgroup???????????????
 */
fun ViewGroup.baseRemoveEmptyAndNetWorkErrorView() {
    if (this.children.count() > 0) {

        var diff = 0
        this.children.toList().indices.forEach {
            val view = this.children.toList()[it - diff]
            if (view.tag == "empty") {
                this.removeViewAt(it)
                diff++
            }

            if (view.tag != null && view.tag.toString() == "change") {
                view.visibility = View.VISIBLE
                view.tag = ""
            }
        }
    }
}

fun AppCompatEditText.baseCloseKeyBoard() {
    setOnFocusChangeListener({ v, t ->
        if (!v.hasFocus())
            baseCloseKeyBoard()
    })
}

fun baseCloseSoftKeyboard(activity: AppCompatActivity) {
    (BaseApplication.INSTANCE.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
        activity.window.decorView.windowToken, 0
    )
}

fun ObservableArrayList<*>.baseAddChangeListener(adapter: RecyclerView.Adapter<*>) {
    addOnListChangedCallback(object :
        ObservableList.OnListChangedCallback<ObservableList<*>>() {
        override fun onChanged(sender: ObservableList<*>?) {
            adapter.notifyDataSetChanged()
        }

        override fun onItemRangeRemoved(
            sender: ObservableList<*>?,
            positionStart: Int,
            itemCount: Int
        ) {
            adapter.notifyItemRangeRemoved(positionStart, itemCount)
        }


        override fun onItemRangeMoved(
            sender: ObservableList<*>?,
            fromPosition: Int,
            toPosition: Int,
            itemCount: Int
        ) {
            if (itemCount == 1) {
                adapter.notifyItemMoved(fromPosition, toPosition)
            } else {
                adapter.notifyDataSetChanged()
            }
        }

        override fun onItemRangeInserted(
            sender: ObservableList<*>?,
            positionStart: Int,
            itemCount: Int
        ) {
            adapter.notifyItemRangeInserted(positionStart, itemCount)
        }

        override fun onItemRangeChanged(
            sender: ObservableList<*>?,
            positionStart: Int,
            itemCount: Int
        ) {
            adapter.notifyItemRangeChanged(positionStart, itemCount)
        }
    })
}


/**
 * findView??????
 * @return ??????
 */
fun View.baseFindView(id: Int): View {
    return findViewById<View>(id)
}

/**
 * findTextView??????
 * @return ??????
 */
fun View.baseFindTextView(id: Int): TextView {
    return findViewById<TextView>(id)
}

/**
 * findEditText??????
 * @return ??????
 */
fun View.baseFindEdittext(id: Int): EditText {
    return findViewById<EditText>(id)
}

/**
 * findEditText??????
 * @return ??????
 */
fun View.baseShow() {
    this.visibility = View.VISIBLE
}

/**
 * findEditText??????
 * @return ??????
 */
fun View.baseHide(type: Int = 0) {
    if (type == 0) {
        this.visibility = View.GONE
    } else {
        this.visibility = View.INVISIBLE
    }
}

/**
 * findAppCompatEditText??????
 * @return ??????
 */
fun View.baseFindAppcompatEdittext(id: Int): AppCompatEditText {
    return findViewById<AppCompatEditText>(id)
}

/**
 * findAppCompatTextView??????
 * @return ??????
 */
fun View.baseFindAppcompatTextView(id: Int): AppCompatTextView {
    return findViewById<AppCompatTextView>(id)
}

/**
 * findImageView??????
 * @return ??????
 */
fun View.baseFindImageView(id: Int): ImageView {
    return findViewById<ImageView>(id)
}


/**
 * findAppcompatImageView??????
 * @return ??????
 */
fun View.baseFindAppCompatImageView(id: Int): AppCompatImageView {
    return findViewById<AppCompatImageView>(id)
}

fun <T> Fragment.baseGetIntentParam(key: String): T {
    return arguments?.get(key) as T
}

fun <T> Fragment.baseGetIntentParamNull(key: String): T? {
    return arguments?.get(key) as T?
}

fun Fragment.addParam(params: Map<String, Any>): Fragment {
    var param = arguments
    if (param == null) {
        param = Bundle()
    }
    params.forEach { it ->
        when (it.value) {
            is String -> {
                param.putString(it.key, it.value.toString())
            }
            is Int -> {
                param.putInt(it.key, it.value as Int)
            }
            is Boolean -> {
                param.putBoolean(it.key, it.value as Boolean)
            }
            is Float -> {
                param.putFloat(it.key, it.value as Float)
            }
            is Double -> {
                param.putDouble(it.key, it.value as Double)
            }
            is Serializable -> {
                param.putSerializable(it.key, it.value as Serializable)
            }
        }
    }
    arguments = param
    return this
}

/***
 * ?????????????????????????????????
 */
fun baseAddLog(str: String, tag: String = "LOG") {
    //???????????? ??????????????????
//    if (baseIsDebug()) {
    printJson(tag, str, "NETWORK")
//    }

}


val LINE_SEPARATOR = System.getProperty("line.separator")

fun printLine(tag: String?, isTop: Boolean) {
    if (isTop) {
        Log.e(
            tag,
            "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"
        )
    } else {
        Log.e(
            tag,
            "????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????"
        )
    }
}

fun printJson(
    tag: String?,
    msg: String,
    headString: String
) {
    var message: String = msg
    var regex = Regex("""(?s)(?<=\*\*JSON).*?(?=JSON\*\*)""")
    var result = regex.findAll(msg)
    result.forEach {
        var value = it.value
        var valueFormat = try {
            if (value.startsWith("{")) {
                val jsonObject = JSONObject(value)
                jsonObject.toString(4) //???????????????????????????????????????????????????json???????????????????????????4??????????????????
            } else if (value.startsWith("[")) {
                val jsonArray = JSONArray(value)
                jsonArray.toString(4)
            } else {
                value
            }
        } catch (e: JSONException) {
            value
        }
        message = message.replace("""**JSON""" + value + """JSON**""", valueFormat)
        var dd = 0
        dd += 0
    }

//    message = try {
//        if (msg.startsWith("{")) {
//            val jsonObject = JSONObject(msg)
//            jsonObject.toString(4) //???????????????????????????????????????????????????json???????????????????????????4??????????????????
//        } else if (msg.startsWith("[")) {
//            val jsonArray = JSONArray(msg)
//            jsonArray.toString(4)
//        } else {
//            msg
//        }
//    } catch (e: JSONException) {
//        msg
//    }
    printLine(tag, true)
    message = headString + LINE_SEPARATOR + message
    val lines = message.split(LINE_SEPARATOR!!.toRegex()).toTypedArray()
    for (line in lines) {
        Log.e(tag, "??? $line")
    }
    printLine(tag, false)
}


/***
 * ?????????????????????
 */
fun baseShowBasicListDialog(
    activity: AppCompatActivity,
    title: String,
    list: List<String>,
    doSome: (ind: Int, txt: String) -> Unit
) {
    MaterialDialog(activity).show {
        title(null, title)
        listItems(null, list) { _, index, text ->
            doSome(index, text.toString())
        }
        lifecycleOwner(activity)
    }

}


/**
 * ????????????????????????
 *
 * @param copyStr
 * @return
 */
fun baseCopyToClip(copyStr: String) {
    return try {
        //????????????????????????
        val cm: ClipboardManager =
            BaseApplication.INSTANCE.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        // ?????????????????????ClipData
        val mClipData: ClipData = ClipData.newPlainText("Label", copyStr)
        // ???ClipData?????????????????????????????????
        cm.setPrimaryClip(mClipData)
    } catch (e: java.lang.Exception) {
    }
}


fun RxHttpJsonParam.addNotCloseLoading(): RxHttpJsonParam {
    add("notclose", true)
    return this
}

fun RxHttpNoBodyParam.addNotCloseLoading(): RxHttpNoBodyParam {
    add("notclose", true)
    return this
}

/***
 * ??????time???????????????
 * @return ??????0-6?????????????????????
 * @param date ??????
 */
fun baseGetWeekOfDay(date: Date): Int {
    val cal: Calendar = Calendar.getInstance()
    cal.setTime(date)
    var w: Int = cal.get(Calendar.DAY_OF_WEEK) - 1
    if (w < 0) w = 0
    if (w == 0) {
        w = 6
    } else {
        w = w - 1
    }
    return w
}

/***
 * ????????????????????????
 * @return ??????0-6?????????????????????
 */
fun baseGetWeekOfToday(): Int {
    return baseGetWeekOfDay(Calendar.getInstance().time)
}

/***
 * ??????????????????????????????
 */
fun baseDateFormatYMD(date: Date = Calendar.getInstance().time): String {
    var format = SimpleDateFormat("yyyy-MM-dd")
    return format.format(date)
}

fun baseRequestBodyToString(requestBody: RequestBody?): String? {
    if (requestBody != null) {
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        return buffer.readUtf8()
    }
    return ""
}

/***
 * ???????????????okhttp??????????????????ssl????????????
 */
fun baseGetDefaultOkHttpClient(): OkHttpClient? {
    val sslParams: HttpsUtils.SSLParams = HttpsUtils.getSslSocketFactory()
    return OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .sslSocketFactory(
            sslParams.sSLSocketFactory,
            sslParams.trustManager
        ) //??????????????????
        .hostnameVerifier(HostnameVerifier { hostname: String?, session: SSLSession? -> true }) //??????host??????
        .build()
}

/***
 * ???????????????????????????V
 */
fun RecyclerView.baseSetLinearLayoutManagerV(): RecyclerView {
    this.layoutManager = LinearLayoutManager(
        BaseActivityStackManager.nowActivity,
        LinearLayoutManager.VERTICAL,
        false
    )
    return this
}

/***
 * ???????????????????????????H
 */
fun RecyclerView.setLinearLayoutManagerH(): RecyclerView {
    this.layoutManager = LinearLayoutManager(
        BaseActivityStackManager.nowActivity,
        LinearLayoutManager.HORIZONTAL,
        false
    )
    return this
}

/***
 * ??????grid??????
 */
fun RecyclerView.baseSetGridLayoutManager(span: Int): RecyclerView {
    this.layoutManager = GridLayoutManager(
        BaseActivityStackManager.nowActivity, span
    )
    return this
}

/***
 * ??????????????????
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseActivityTitle(
    val title: String
)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class BaseLayoutRes(
    val redID: Int
)


//viewmodel???lunch??????
suspend fun BaseActivityModel<*>.baseLaunch(
    doSome: (suspend () -> Unit)?,
    error: ((throwable: Throwable) -> Unit)?
) {
    viewModelScope.launch(baseNetworkException(mActivity) {
        error?.invoke(it)
    }) {
        doSome?.invoke()
    }
}

//viewmodel???lunch??????
fun BaseActivityModel<*>.baseLaunch(
    doSome: (suspend () -> Unit)?,
) {
    viewModelScope.launch(baseNetworkException(mActivity)) {
        doSome?.invoke()
    }
}




fun baseTextWatcher(after: ((str: String) -> Unit)): TextWatcher {
    return object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            after.invoke(s.toString())
        }

    }
}