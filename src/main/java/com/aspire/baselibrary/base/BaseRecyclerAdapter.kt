package com.aspire.baselibrary.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.aspire.baselibrary.R
import com.aspire.baselibrary.expand.BaseListClickListener

/***
 ** create by: NotCoder
 ** time: 2020/4/26
 ** des:  ≈‰∆˜
 **/

 class BaseRecyclerAdapter<T>(
    var layoutId: Int,
    dataList: ArrayList<T>,
    var variableMap: Map<Int, Any>? = null,
    var itemClick: BaseListClickListener? = null
) : RecyclerView.Adapter<BaseRecyclerAdapter.HolderBinding>() {

    companion object {
        var defaultVariableValue = 1
        fun setVariableValue(value: Int) {
            defaultVariableValue = value
        }
    }

    var dataList: ArrayList<T>? = dataList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBinding {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(BaseActivityStackManager.nowActivity),
            layoutId,
            parent,
            false
        )
        return HolderBinding(binding!!.root, binding)
    }

    override fun getItemCount(): Int {
        return if (dataList == null) 0 else dataList!!.size
    }


    class HolderBinding(view: View, var binding: ViewDataBinding) : RecyclerView.ViewHolder(view)

    override fun onBindViewHolder(holder: HolderBinding, position: Int) {
        val binding = holder.binding
        binding.setVariable(defaultVariableValue, dataList!![position])
        variableMap?.map {
            binding.setVariable(it.key, it.value)
        }

        if (binding.root.findViewById<View>(R.id.rootView) != null) {
            binding.root.findViewById<View>(R.id.rootView).setOnClickListener {
                itemClick?.click(position, binding)
            }
        } else {
            binding.root.setOnClickListener {
                itemClick?.click(position, binding)
            }
        }

        itemClick?.inject(position, binding.root)
        binding.executePendingBindings()
    }

}