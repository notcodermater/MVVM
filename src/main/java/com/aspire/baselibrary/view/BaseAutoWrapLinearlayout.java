package com.aspire.baselibrary.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * create by NotCoder
 * date 2020/3/3
 * Describe:
 */
public class BaseAutoWrapLinearlayout extends LinearLayout {

    public BaseAutoWrapLinearlayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseAutoWrapLinearlayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public BaseAutoWrapLinearlayout(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        if (getChildCount() == 0){
            super.onMeasure(widthMeasureSpec,heightMeasureSpec);
            return;
        }

//如果有子view，行数肯定至少1行
        int lineCount = 1;
        //此布局高度一般是wrap_content,所以需要对AT_MOST模式做处理
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED){
            //子View的宽度之和
            int childrenTotalWidth = 0;
            View childView = null;
            LayoutParams params = null;
            //循环子View，分别测量子View的宽高
            for (int i = 0 ; i < getChildCount() ; i ++){
                childView = getChildAt(i);
                if (childView.getVisibility() != GONE) {
                    params = ((LayoutParams) childView.getLayoutParams());
                    //测量子View
                    measureChild(childView, widthMeasureSpec, heightMeasureSpec);
                    //把子View的宽度和margin属性做加和
                    childrenTotalWidth += childView.getMeasuredWidth() + 10;
                    //比较此layout的width和子View的总宽度
                    if (childrenTotalWidth > width) {//条件成立，即折行
                        //行数加1
                        lineCount++;
                        //把子view的总宽度置为当前子view的宽度，以便后续的子view宽度的继续加和操作
                        childrenTotalWidth = childView.getMeasuredWidth() + 10;
                    }
                }
            }
            //循环结束，即可得到lineCount的值
            LayoutParams layoutParams = (LayoutParams) getChildAt(0).getLayoutParams();
            //注意这里设置的所有的子View的topMargin和bottomMargin分别一样。
            //由于子View的高度一致，所以取第一个子View的高度和其上下margin属性，乘以行数，即可得到layout在AT_MOST模式下总高度
            height = (getChildAt(0).getMeasuredHeight() + 8) * lineCount;
        }

        setMeasuredDimension(width,height);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        //子view的总宽度，用来做折行判断，并计算出所有位于每一行行首的子View的索引
        int currentLineTotalWidth = 0;
        //存储每一行行首子View的索引
        List<Integer> list = new ArrayList<>();
        //第一个子View肯定位于行首
        list.add(0);
        View child = null;
        LayoutParams p = null;
        //当前行
        int currentLine = 0;
        for (int i = 0 ; i < getChildCount() ; i ++){
            child = getChildAt(i);
            p = ((LayoutParams) child.getLayoutParams());
            currentLineTotalWidth += child.getMeasuredWidth() + 10;
            //同onMeasure方法中的判断，判断折行的位置
            if (currentLineTotalWidth > getMeasuredWidth()){//条件满足则折行
                //并把当前View的索引存储list中
                list.add(i);
                //重置currentLineTotalWidth
                currentLineTotalWidth = child.getMeasuredWidth() + 10;
            }


            int left = 0;
            int top = 0;
            //设置当前行
            currentLine = list.size() - 1;
            //循环每一行的textView计算当前view的left
            for (int m = list.get(currentLine); m < i; m++) {
                left += getChildAt(m).getMeasuredWidth() + 10;
            }
            //计算出的left需要加上当前子View的leftMargin属性
            left += 4;
            //注意这里设置的所有的子View的topMargin和bottomMargin分别一样。
            //top属性是由行数乘以行高，并加上当前View的top属性
            top = (getChildAt(0).getMeasuredHeight() +8) * currentLine + 4;
            //调用子view的layout方法去完成布局
            child.layout(left, top, left + child.getMeasuredWidth(), top + child.getMeasuredHeight());

        }
    }
}
