package com.kod.assurancecontrac

import androidx.recyclerview.widget.DiffUtil
import com.kod.assurancecontracthandler.model.BaseContract


class ContractsCallback(private val oldList: List<BaseContract>?, private val newList: List<BaseContract>)
    : DiffUtil.Callback(){
    override fun getOldListSize(): Int {
        return if (oldList.isNullOrEmpty()) 0
        else oldList.size
    }

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList?.get(oldItemPosition) == newList[newItemPosition]


    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList?.get(oldItemPosition)?.contract?.assure ==
                newList[newItemPosition].contract?.assure &&
                oldList?.get(oldItemPosition)?.contract?.telephone ==
                newList[newItemPosition].contract?.telephone
    }
}