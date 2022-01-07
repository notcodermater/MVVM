package com.aspire.baselibrary.base

import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModel
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import io.reactivex.rxjava3.disposables.Disposable

open class BaseActivityModel<T : ViewDataBinding> : ViewModel() {
    lateinit var binding: T
    var refresh: SmartRefreshLayout? = null
    lateinit var mActivity: BaseActivityForDataBinding<*,*>


    /***
     * 初始化
     */
    open fun init(activity: BaseActivityForDataBinding<*,*>, binding: T, refresh: SmartRefreshLayout?) {
        this.binding = binding
        this.refresh = refresh
        this.mActivity = activity
        initData()
    }

    open fun initData() {

    }

    fun addStack(disposable: Disposable) {
        mActivity.addRxStack(disposable)
    }

    fun endRefresh() {
        refresh?.finishRefresh()
        refresh?.finishLoadMore()
    }


}