package com.kod.assurancecontracthandler.views.customerdetails

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.usecases.ContactAction
import com.kod.assurancecontracthandler.databinding.ContactListItemBinding

class CustomerContactAdapter(
    private val listContacts: List<String>,
    private val iconContactActionClicked: (Int, String) -> Unit
) : RecyclerView.Adapter<CustomerContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = ContactListItemBinding.bind(view)

        fun bindView(phoneNumber: String) {
            binding.tvPhoneNumber.text = phoneNumber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder =
        ContactViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contact_list_item, parent, false))

    override fun getItemCount(): Int = listContacts.size

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentNumber = listContacts[position]

        holder.bindView(currentNumber)
        holder.binding.ivPhoneNumber.setOnClickListener {
            iconContactActionClicked.invoke(ContactAction.CALL.action, currentNumber)
        }
        holder.binding.ivMessage.setOnClickListener {
            iconContactActionClicked.invoke(ContactAction.SMS.action, currentNumber)
        }
        holder.binding.ivWhatsapp.setOnClickListener {
            iconContactActionClicked.invoke(ContactAction.WHATSAPP_TEXT.action, currentNumber)
        }
    }
}