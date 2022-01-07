package com.aspire.baselibrary.expand

import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.appcompat.widget.AppCompatImageView
import androidx.cardview.widget.CardView
import androidx.databinding.BindingAdapter
import com.coorchice.library.SuperTextView
import com.jakewharton.rxbinding2.view.RxView
import io.reactivex.disposables.Disposable
import org.jetbrains.anko.*
import java.io.File
import java.util.concurrent.TimeUnit

/***
 *  DataBinding的扩展方法
 *  @author NotCoder
 *  date: 20200604
 *
 *
 */

@BindingAdapter(value = ["cmd", "single"], requireAll = false)
fun mBClick(view: View, cmd: () -> Unit, single: Boolean = false): Disposable {
    return if (single) {
        RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            cmd()
        }
    } else {
        RxView.clicks(view).subscribe {
            cmd()
        }
    }
}

/***
 * 点击事件绑定 防抖动默认一秒
 */
@BindingAdapter(value = ["cmdarg", "arg", "single"], requireAll = false)
fun mBClick(
    view: View,
    cmdarg: ((any: Any) -> Unit)?,
    arg: Any,
    single: Boolean = false
): Disposable {
    return if (single != null && single || single == null) {
        RxView.clicks(view).throttleFirst(1, TimeUnit.SECONDS).subscribe {
            cmdarg?.invoke(arg)
        }
    } else {
        RxView.clicks(view).subscribe {
            cmdarg?.invoke(arg)
        }
    }
}

@BindingAdapter("baseBlod")
fun baseBlod(view: TextView, baseBlod: Boolean) {
    view.baseBlod()
}
@BindingAdapter("basePadding")
fun basePadding(view: View, padding: Float){
    view.padding= baseDp2px(padding).toInt()
}

@BindingAdapter("baseShow")
fun baseShow(view: View, visibity: Boolean) {
    if (visibity) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.GONE
    }
}

@BindingAdapter("baseShowInVis")
fun baseShowInVis(view: View, visibity: Boolean) {
    if (visibity) {
        view.visibility = View.VISIBLE
    } else {
        view.visibility = View.INVISIBLE
    }
}

@BindingAdapter("baseEnable")
fun baseEnable(view: View, enable: Boolean) {
    view.isEnabled=enable
}

@BindingAdapter("drawable1")
fun superDrawable1(view: SuperTextView, ref: Int) {
    view.drawable = ColorDrawable(ref)
}

@BindingAdapter("basebg")
fun viewBg(view: View, ref: Any) {
    if (ref is Int) {
        view.setBackgroundResource(ref)
    } else if (ref is String) {
        view.background = ColorDrawable(Color.parseColor(ref))
    } else if (ref is Drawable) {
        view.background = ref
    }
}

@BindingAdapter("baseCardBgStr")
fun viewBg(view: CardView, str: String) {
    view.setCardBackgroundColor(Color.parseColor(str))
}

@BindingAdapter("baseCardBg")
fun viewBg(view: CardView, res: Int) {
    view.setCardBackgroundColor(baseGetColor(res))
}
@BindingAdapter("baseIsCheck")
fun baseIsCheck(view: AppCompatCheckBox, checked: Boolean) {
    view.isChecked=checked
}

@BindingAdapter("drawable1Color")
fun superDrawable1(view: SuperTextView, drawable1Color: String) {
    view.drawable = ColorDrawable(Color.parseColor(drawable1Color))
}

@BindingAdapter("baseTextColor")
fun superTextColor(view: TextView, textColor: Any) {
    if (textColor is String) {
        view.setTextColor(Color.parseColor(textColor))
    } else if (textColor is Int) {
        view.textColorResource = textColor
    }
}


@BindingAdapter("baseTextSize")
fun baseTextSize(view: TextView, textSize: Float) {
    view.textSize=textSize
}

@BindingAdapter("baseStokeColor")
fun superStokeColor(view: SuperTextView, textColor: String) {
    view.setStrokeColor(Color.parseColor(textColor))
}

@BindingAdapter("baseStatusDrawable1")
fun baseStatusDrawable1(view: SuperTextView, obj: Any?) {
    obj?.let {
        if (obj is GradientDrawable) {
            view.drawable = obj
        } else if (obj is String) {
            view.drawable = ColorDrawable(Color.parseColor(obj))
        } else if (obj is BitmapDrawable) {
            view.drawable = obj
        }
    }
}

@BindingAdapter("baseStatusDrawable1Tint")
fun baseStatusDrawable1Tint(view: SuperTextView, obj: String) {
    obj?.let {
        view.drawable = ColorDrawable(Color.parseColor(obj))
    }
}

@BindingAdapter("baseSuperTextDrawable2")
fun baseSuperTextDrawable2(view: SuperTextView, res: Drawable) {
    view.setDrawable2(res)
}

@BindingAdapter("baseTextColorRes")
fun superTextColor(view: SuperTextView, textColor: Int) {
    view.setTextColor(textColor)
}


@BindingAdapter("borderColor")
fun superBorderColor(view: SuperTextView, textColor: String) {
    view.strokeColor = Color.parseColor(textColor)
}


@BindingAdapter("mStokeColor")
fun mStokeColor(view: SuperTextView, ref: Int) {
    view.strokeColor = ref
}


@BindingAdapter("baseSrc")
fun baseSrc(view: ImageView, src: Any?) {
    if (src is Int) {
        baseLoadSquare(src, view)
    } else if(src is Drawable){
        view.setImageDrawable(src)
    }else if(src is String){
        baseLoadSquare(src.baseGetValue(), view)
    }
}


@BindingAdapter("baseSrc", "baseCorner")
fun baseSrcCorner(view: ImageView, src: String?, corner: Int = 0) {
    baseLoadSquare(src.baseGetValue()!!, view, corner = corner)
}

@BindingAdapter("baseVideo", "baseCorner")
fun baseVideoCorner(view: ImageView, baseVideo: String?, corner: Int = 0) {
    baseLoadSquare(File(baseVideo), view, corner = corner)
}


@BindingAdapter("srcOval")
fun srcOval(view: ImageView, src: String?) {
    baseLoadHead(src.baseGetValue()!!, view)
}




