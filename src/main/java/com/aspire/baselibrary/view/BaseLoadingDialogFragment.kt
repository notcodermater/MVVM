package com.aspire.baselibrary.view

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.aspire.baselibrary.R
import com.aspire.baselibrary.base.BaseApplication
import com.bumptech.glide.Glide


class BaseLoadingDialogFragment: DialogFragment() {
    var rootView:View?=null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        rootView=inflater.inflate(R.layout.base_loading,container,false)
        Glide.with(BaseApplication.INSTANCE).load(R.drawable.base_loading_new)
            .into(rootView!!.findViewById(R.id.ivPic))
        return rootView
    }

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        requireActivity().windowManager.defaultDisplay.getMetrics(dm)
        dialog!!.window!!.setLayout(dm.widthPixels, dialog!!.window!!.attributes.height)
        dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun show(manager: FragmentManager, tag: String?) {
        setCancelable(false)
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            if (manager.isDestroyed()) return
        }
        try {
            //在每个add事务前增加一个remove事务，防止连续的add
            manager.beginTransaction().remove(this).commit()
            super.show(manager, tag)
        } catch (e: Exception) {
            //同一实例使用不同的tag会异常，这里捕获一下
            e.printStackTrace()
        }
    }
}