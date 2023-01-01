package com.kod.assurancecontracthandler.views.customerdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.usecases.ContactAction

class CustomerContactAdapter(private val listContacts: List<String>, private val iconContactActionClicked: (Int, String) -> Unit): RecyclerView.Adapter<CustomerContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val number = itemView.findViewById<TextView>(R.id.tv_phone_number)

        fun bindView(phoneNumber: String){
            number.text = phoneNumber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent,false))

    override fun getItemCount(): Int  = listContacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentNumber = listContacts[position]

        holder.bindView(currentNumber)
        holder.itemView.findViewById<ImageView>(R.id.iv_phone_number).setOnClickListener{
            iconContactActionClicked.invoke(ContactAction.CALL.action, currentNumber)
        }
        holder.itemView.findViewById<ImageView>(R.id.iv_message).setOnClickListener{
            iconContactActionClicked.invoke(ContactAction.SMS.action, currentNumber)
        }
        holder.itemView.findViewById<ImageView>(R.id.iv_whatsapp).setOnClickListener{
            iconContactActionClicked.invoke(ContactAction.WHATSAPP_TEXT.action, currentNumber)
        }
    }
}