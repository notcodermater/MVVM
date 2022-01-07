package com.aspire.baselibrary.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableList
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.aspire.baselibrary.expand.BaseListClickListener
import com.aspire.baselibrary.expand.baseAddChangeListener

/***
 ** create by: NotCoder
 ** time: 2020/4/26
 ** des:
 **/

class BaseBindingAdapterObserverAble<T> : RecyclerView.Adapter<BaseBindingAdapterObserverAble.HolderBinding> {
     var dataList: ObservableList<T>?=null
    var layoutId = 0
    var moduleId = 0

    var itemClick: BaseListClickListener? = null




    var variableMap: Map<Int, Any>? = null

    constructor(
        moduleId: Int,
        layoutId: Int,
        dataList: ObservableList<T>?,
        variableMap: Map<Int, Any>?
    ) : super() {
        this.dataList = dataList
        this.layoutId = layoutId
        this.moduleId = moduleId
        this.variableMap = variableMap
    }

    constructor(
        moduleId: Int,
        layoutId: Int,
        dataList: ObservableArrayList<T>,
        variableMap: Map<Int, Any>?,
        itemClick: BaseListClickListener?=null
    ) : super() {
        this.dataList = dataList
        this.layoutId = layoutId
        this.moduleId = moduleId
        this.variableMap = variableMap
        this.itemClick=itemClick
        dataList.baseAddChangeListener(this)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBinding {
        var binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(BaseApplication.INSTANCE),
            layoutId,
            parent,
            false
        )
        var holder = HolderBinding(binding!!.root, binding)
        return holder
    }

    override fun getItemCount(): Int {
        if(dataList==null){
            return 0
        }else{
            return dataList!!.size
        }
    }


    class HolderBinding : RecyclerView.ViewHolder {
        var binding: ViewDataBinding

        constructor(view: View, binding: ViewDataBinding) : super(view) {
            this.binding = binding
        }
    }

    override fun onBindViewHolder(holder: HolderBinding, position: Int) {
        var binding = holder.binding
        binding.setVariable(moduleId, dataList!![position])
        variableMap?.map {
            binding.setVariable(it.key, it.value)
        }
        if(itemClick!=null){
            binding.root.setOnClickListener {
                itemClick!!.click(position,binding)
            }
            itemClick!!.inject(position,holder.binding.root)
        }
        binding.executePendingBindings()
    }


}