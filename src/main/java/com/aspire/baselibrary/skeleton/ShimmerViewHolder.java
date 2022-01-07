package com.aspire.baselibrary.skeleton;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.aspire.baselibrary.R;

/**
 * Created by ethanhua on 2017/7/29.
 */

public class ShimmerViewHolder extends RecyclerView.ViewHolder {

    public ShimmerViewHolder(LayoutInflater inflater, ViewGroup parent, int innerViewResId) {
        super(inflater.inflate(R.layout.base_layout_shimmer, parent, false));
//        super(inflater.inflate(innerViewResId, parent, false));
        ViewGroup layout = (ViewGroup) itemView;
        View view = inflater.inflate(innerViewResId, layout, false);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp != null) {
            layout.setLayoutParams(lp);
        }
        layout.addView(view);
    }
}
