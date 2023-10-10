package com.kod.assurancecontracthandler.views.main.fragmentListCustomers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.utilities.CustomerCallback
import com.kod.assurancecontracthandler.databinding.RvCustomerItemBinding
import com.kod.assurancecontracthandler.model.Customer

class ListCustomersAdapter(private val listener: (Customer) -> Unit) :
    RecyclerView.Adapter<ListCustomersAdapter.MyViewHolder>() {

    private var listCustomers: List<Customer> = emptyList()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = RvCustomerItemBinding.bind(view)

        fun bindItems(position: Int, customer: Customer) {
            val actualPosition = position + 1
            binding.tvItemCustomerName.text = customer.customerName
            binding.columnId.text = (actualPosition).toString()
        }
    }

    fun setCustomerList(newCustomerList: List<Customer>) {
        if (listCustomers.isEmpty()) {
            listCustomers = newCustomerList
            notifyDataSetChanged()
        } else {
            val diffCallback = CustomerCallback(listCustomers, newCustomerList)
            val diffOfCustomers = DiffUtil.calculateDiff(diffCallback, true)
            listCustomers = newCustomerList
            diffOfCustomers.dispatchUpdatesTo(this)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_customer_item, parent, false)
        )

    override fun getItemCount(): Int = listCustomers.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentCustomer = listCustomers[position]
        holder.bindItems(position, currentCustomer)
        holder.itemView.setOnClickListener { listener(currentCustomer) }
    }
}