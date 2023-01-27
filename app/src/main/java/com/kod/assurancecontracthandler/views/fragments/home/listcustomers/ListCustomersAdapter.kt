package com.kod.assurancecontracthandler.views.fragments.home.listcustomers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.utilities.CustomerCallback
import com.kod.assurancecontracthandler.databinding.RvItemCustomerBinding
import com.kod.assurancecontracthandler.model.Customer
import org.apache.xmlbeans.impl.tool.Diff

class ListCustomersAdapter(private val listener: (Customer)-> Unit) :
    RecyclerView.Adapter<ListCustomersAdapter.MyViewHolder>() {

    var listCustomers: List<Customer> = emptyList()

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val binding = RvItemCustomerBinding.bind(view)

        fun bindItems(customer: Customer, ){
            itemView.setBackgroundColor(itemView.context.getColor(R.color.dialog_background))
            binding.tvItemCustomerName.text = customer.customerName
        }
    }

    fun setCustomerList(newCustomerList: List<Customer>){
        if (listCustomers.isEmpty()){
            listCustomers = newCustomerList
            notifyDataSetChanged()
        }else{
            val diffCallback = CustomerCallback(listCustomers, newCustomerList)
            val diffOfCustomers = DiffUtil.calculateDiff(diffCallback, true)
            listCustomers = newCustomerList
            diffOfCustomers.dispatchUpdatesTo(this)
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_customer, parent, false))

    override fun getItemCount(): Int = listCustomers.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentCustomer = listCustomers[position]
        holder.bindItems(currentCustomer)
        holder.itemView.setOnClickListener { listener(currentCustomer) }
    }
}