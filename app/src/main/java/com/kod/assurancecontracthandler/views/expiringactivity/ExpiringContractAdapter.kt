package com.kod.assurancecontracthandler.views.expiringactivity

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.Customer
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone

class ExpiringContractAdapter(
    private val listContracts: List<Customer>,
    private val onClickBtn: (Customer) -> Unit,
    private val onClickItem: (View)-> Unit ): RecyclerView.Adapter<ExpiringContractAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val index = itemView.findViewById<TextView>(R.id.tv_contract_number_index_worker)
        private val contractNumb = itemView.findViewById<TextView>(R.id.tv_contract_number_worker)
        private val descriptionTv = itemView.findViewById<TextView>(R.id.tv_some_information)

        fun bindItem(position: Int, customer: Customer){
            val attestation = customer.attestation?.replace('*', '-', false)
            val contractNumberStr = "$attestation-${customer.carteRose}"
            val contractDateMilli = customer.echeance
            val contractDate = LocalDateTime.ofInstant(contractDateMilli?.let {
                Instant.ofEpochMilli(it) }, TimeZone.getDefault().toZoneId())

            val differenceDay = contractDate?.dayOfMonth?.minus(LocalDateTime.now().dayOfMonth)
            val differenceMonth = contractDate?.month?.minus(LocalDateTime.now().monthValue.toLong())
            val description = "Le contrat de Mr/Mme <font color=#08FF00>${customer.customerName}</font> expire le " +
                    "<font color=#FF0000>${contractDate.toLocalDate()}</font>." +
                    " Qui est dans <font color=#4F34EE>${differenceDay}jours</font>. Appelez ou écrivez lui un" +
                    " message pour le rappeler de le renouveler"
            contractNumb.text = contractNumberStr
            index.text = position.toString()
            descriptionTv.text = Html.fromHtml(description, Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.worker_data_group_item, parent, false))

    override fun getItemCount(): Int = listContracts.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentCustomer = listContracts[position]

        holder.bindItem(position, currentCustomer)
        holder.itemView.findViewById<Button>(R.id.btn_customer_redirection_link)
            .setOnClickListener {
            onClickBtn.invoke(currentCustomer)
        }

        holder.itemView.setOnClickListener {
            onClickItem.invoke(it)
        }
    }
}