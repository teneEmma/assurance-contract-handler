package com.kod.assurancecontracthandler.views.fragments.home.listcustomers

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.Customer

class ListCustomersAdapter(private val customers: List<Customer>, private val listener: (Customer)-> Unit) : RecyclerView.Adapter<ListCustomersAdapter.MyViewHolder>() {

    class MyViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val customerName: TextView = view.findViewById(R.id.tv_customer_name)

        fun bindItems(customer: Customer, ){
            itemView.setBackgroundColor(itemView.context.getColor(R.color.dialog_background))
            customerName.text = customer.customerName
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(LayoutInflater.from(parent.context)
            .inflate(R.layout.rv_item_customer, parent, false))

    override fun getItemCount(): Int = customers.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentCustomer = customers[position]
        holder.bindItems(currentCustomer)
        holder.itemView.setOnClickListener { listener(currentCustomer) }
    }
}