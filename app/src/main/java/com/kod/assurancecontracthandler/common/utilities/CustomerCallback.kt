package com.kod.assurancecontracthandler.common.utilities

import androidx.recyclerview.widget.DiffUtil
import com.kod.assurancecontracthandler.model.Customer

class CustomerCallback(private val oldList: List<Customer>,private val newList: List<Customer>) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition] == newList[newItemPosition]

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
        oldList[oldItemPosition].customerName == newList[newItemPosition].customerName &&
                oldList[oldItemPosition].phoneNumber == newList[newItemPosition].phoneNumber
}