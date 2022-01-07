package com.aspire.baselibrary.view

import android.R
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class DividerLinearItemDecoration : RecyclerView.ItemDecoration {

    val HORIZONTAL: Int = android.widget.LinearLayout.HORIZONTAL
    val VERTICAL: Int = android.widget.LinearLayout.VERTICAL
    private val TAG = "itemDivider"
    private lateinit var mContext: Context
    public var mDivider: Drawable? = null
    private val mBounds = Rect()

    /**
     * 在AppTheme里配置 android:listDivider
     */
    private val ATTRS = intArrayOf(R.attr.listDivider)

    /**
     * 头部 不显示分割线的item个数 这里应该包含刷新头，
     * 比如有一个headerView和有下拉刷新，则这里传 2
     */
    private var mHeaderNoShowSize = 0

    /**
     * 尾部 不显示分割线的item个数 默认不显示最后一个item的分割线
     */
    private var mFooterNoShowSize = 1

    /**
     * Current orientation. Either [.HORIZONTAL] or [.VERTICAL].
     */
    private var mOrientation = 0
    private var mPaint: Paint? = null

    /**
     * 如果是横向 - 宽度
     * 如果是纵向 - 高度
     */
    private var mDividerSpacing = 0

    /**
     * 如果是横向 - 左边距
     * 如果是纵向 - 上边距
     */
    private var mLeftTopPadding = 0

    /**
     * 如果是横向 - 右边距
     * 如果是纵向 - 下边距
     */
    private var mRightBottomPadding = 0
    private var byRecyclerView: RecyclerView? = null

    constructor(context: Context): this(context, LinearLayoutManager.VERTICAL, 0, 1) {

    }

    constructor(
        context: Context,
        orientation: Int
    ): this(context, orientation, 0, 1) {

    }

    constructor(
        context: Context,
        orientation: Int,
        headerNoShowSize: Int
    ):this(context, orientation, headerNoShowSize, 1) {

    }

    /**
     * Creates a divider [RecyclerView.ItemDecoration]
     *
     * @param context          Current context, it will be used to access resources.
     * @param orientation      Divider orientation. Should be [.HORIZONTAL] or [.VERTICAL].
     * @param headerNoShowSize headerViewSize + RefreshViewSize
     * @param footerNoShowSize footerViewSize
     */
    constructor(
        context: Context,
        orientation: Int,
        headerNoShowSize: Int,
        footerNoShowSize: Int
    ) {
        mContext = context
        mHeaderNoShowSize = headerNoShowSize
        mFooterNoShowSize = footerNoShowSize
        setOrientation(orientation)
        val a = context.obtainStyledAttributes(ATTRS)
        mDivider = a.getDrawable(0)
        a.recycle()
    }

    /**
     * Sets the orientation for this divider. This should be called if
     * [RecyclerView.LayoutManager] changes orientation.
     *
     * @param orientation [.HORIZONTAL] or [.VERTICAL]
     */
    fun setOrientation(orientation: Int): DividerLinearItemDecoration? {
        require(!(orientation != HORIZONTAL && orientation != VERTICAL)) { "Invalid orientation. It should be either HORIZONTAL or VERTICAL" }
        mOrientation = orientation
        return this
    }

    /**
     * Sets the [Drawable] for this divider.
     *
     * @param drawable Drawable that should be used as a divider.
     */
    fun setDrawable(drawable: Drawable?): DividerLinearItemDecoration? {
        requireNotNull(drawable) { "drawable cannot be null." }
        mDivider = drawable
        return this
    }

    fun setDrawable(@DrawableRes id: Int): DividerLinearItemDecoration? {
        setDrawable(ContextCompat.getDrawable(mContext, id))
        return this
    }

    override fun onDraw(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (parent.layoutManager == null || mDivider == null && mPaint == null) {
            return
        }
        if (mOrientation == VERTICAL) {
            drawVertical(canvas, parent, state)
        } else {
            drawHorizontal(canvas, parent, state)
        }
    }

    private fun drawVertical(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        canvas.save()
        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(
                left,
                parent.paddingTop,
                right,
                parent.height - parent.paddingBottom
            )
        } else {
            left = 0
            right = parent.width
        }
        val childCount = parent.childCount
        val lastPosition = state.itemCount - 1
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val childRealPosition = parent.getChildAdapterPosition(child)

            // 过滤到头部不显示的分割线
            if (childRealPosition < mHeaderNoShowSize) {
                continue
            }
            // 过滤到尾部不显示的分割线
            if (childRealPosition <= lastPosition - mFooterNoShowSize) {
                if (mDivider != null) {
                    parent.getDecoratedBoundsWithMargins(child, mBounds)
                    val bottom = mBounds.bottom + Math.round(child.translationY)
                    val top = bottom - mDivider!!.intrinsicHeight
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
                }
                if (mPaint != null) {
                    val params =
                        child.layoutParams as RecyclerView.LayoutParams
                    val left1 = left + mLeftTopPadding
                    val right1 = right - mRightBottomPadding
                    val top1 = child.bottom + params.bottomMargin
                    val bottom1 = top1 + mDividerSpacing
                    canvas.drawRect(
                        left1.toFloat(),
                        top1.toFloat(),
                        right1.toFloat(),
                        bottom1.toFloat(),
                        mPaint!!
                    )
                }
            }
        }
        canvas.restore()
    }

    private fun drawHorizontal(
        canvas: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        canvas.save()
        val top: Int
        val bottom: Int
        if (parent.clipToPadding) {
            top = parent.paddingTop
            bottom = parent.height - parent.paddingBottom
            canvas.clipRect(
                parent.paddingLeft, top,
                parent.width - parent.paddingRight, bottom
            )
        } else {
            top = 0
            bottom = parent.height
        }
        val childCount = parent.childCount
        val lastPosition = state.itemCount - 1
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val childRealPosition = parent.getChildAdapterPosition(child)

            // 过滤到头部不显示的分割线
            if (childRealPosition < mHeaderNoShowSize) {
                continue
            }
            // 过滤到尾部不显示的分割线
            if (childRealPosition <= lastPosition - mFooterNoShowSize) {
                if (mDivider != null) {
                    parent.getDecoratedBoundsWithMargins(child, mBounds)
                    val right = mBounds.right + Math.round(child.translationX)
                    val left = right - mDivider!!.intrinsicWidth
                    mDivider!!.setBounds(left, top, right, bottom)
                    mDivider!!.draw(canvas)
                }
                if (mPaint != null) {
                    val params =
                        child.layoutParams as RecyclerView.LayoutParams
                    val left1 = child.right + params.rightMargin
                    val right1 = left1 + mDividerSpacing
                    val top1 = top + mLeftTopPadding
                    val bottom1 = bottom - mRightBottomPadding
                    canvas.drawRect(
                        left1.toFloat(),
                        top1.toFloat(),
                        right1.toFloat(),
                        bottom1.toFloat(),
                        mPaint!!
                    )
                }
            }
        }
        canvas.restore()
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (mDivider == null && mPaint == null) {
            outRect[0, 0, 0] = 0
            return
        }
        //parent.getChildCount() 不能拿到item的总数
        val lastPosition = state.itemCount - 1
        val position = parent.getChildAdapterPosition(view!!)
        var mScrollTopFix = false
        if (byRecyclerView == null && parent is RecyclerView) {
            byRecyclerView = parent as RecyclerView
        }
        if (byRecyclerView != null ) {
            mScrollTopFix = true
        }

        // 滚动条置顶
        val isFixScrollTop = mScrollTopFix && position == 0
        val isShowDivider =
            mHeaderNoShowSize <= position && position <= lastPosition - mFooterNoShowSize
        if (mOrientation == VERTICAL) {
            if (isFixScrollTop) {
                outRect[0, 0, 0] = 1
            } else if (isShowDivider) {
                outRect[0, 0, 0] =
                    if (mDivider != null) mDivider!!.intrinsicHeight else mDividerSpacing
            } else {
                outRect[0, 0, 0] = 0
            }
        } else {
            if (isFixScrollTop) {
                outRect[0, 0, 1] = 0
            } else if (isShowDivider) {
                outRect[0, 0, if (mDivider != null) mDivider!!.intrinsicWidth else mDividerSpacing] =
                    0
            } else {
                outRect[0, 0, 0] = 0
            }
        }
    }

    /**
     * 设置不显示分割线的item位置与个数
     *
     * @param headerNoShowSize 头部 不显示分割线的item个数
     * @param footerNoShowSize 尾部 不显示分割线的item个数，默认1，不显示最后一个,最后一个一般为加载更多view
     */
    fun setNoShowDivider(headerNoShowSize: Int, footerNoShowSize: Int): DividerLinearItemDecoration? {
        mHeaderNoShowSize = headerNoShowSize
        mFooterNoShowSize = footerNoShowSize
        return this
    }

    /**
     * 设置不显示头部分割线的item个数
     *
     * @param headerNoShowSize 头部 不显示分割线的item个数
     */
    fun setHeaderNoShowDivider(headerNoShowSize: Int): DividerLinearItemDecoration? {
        mHeaderNoShowSize = headerNoShowSize
        return this
    }

    fun setParam(dividerColor: Int, dividerSpacing: Int): DividerLinearItemDecoration? {
        return setParam(dividerColor, dividerSpacing, 0f, 0f)
    }

    /**
     * 直接设置分割线颜色等，不设置drawable
     *
     * @param dividerColor         分割线颜色
     * @param dividerSpacing       分割线间距
     * @param leftTopPaddingDp     如果是横向 - 左边距
     * 如果是纵向 - 上边距
     * @param rightBottomPaddingDp 如果是横向 - 右边距
     * 如果是纵向 - 下边距
     */
    fun setParam(
        dividerColor: Int,
        dividerSpacing: Int,
        leftTopPaddingDp: Float,
        rightBottomPaddingDp: Float
    ): DividerLinearItemDecoration? {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mPaint!!.setStyle(Paint.Style.FILL)
        mPaint!!.setColor(ContextCompat.getColor(mContext, dividerColor))
        mDividerSpacing = dividerSpacing
        mLeftTopPadding = dip2px(leftTopPaddingDp)
        mRightBottomPadding = dip2px(rightBottomPaddingDp)
        mDivider = null
        return this
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    fun dip2px(dpValue: Float): Int {
        val scale = mContext!!.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }


}