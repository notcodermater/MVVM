package com.aspire.baselibrary.view

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.recyclerview.widget.RecyclerView

/***
 ** create by: NotCoder
 ** time: 2020/4/27
 ** des:
 **/

class BaseRecyclerViewNoTouch :RecyclerView {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean {
        return true
    }
}