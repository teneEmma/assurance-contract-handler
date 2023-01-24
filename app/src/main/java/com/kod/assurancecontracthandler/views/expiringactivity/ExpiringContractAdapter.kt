package com.kod.assurancecontracthandler.views.expiringactivity

import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.Customer
import java.time.Instant
import java.time.LocalDateTime
import java.util.*

class ExpiringContractAdapter(
    private val listContracts: List<Customer>,
    private val onClickBtn: (Customer) -> Unit ): RecyclerView.Adapter<ExpiringContractAdapter.ViewHolder>() {

    class ViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val index = itemView.findViewById<TextView>(R.id.tv_contract_number_index_worker)
        private val contractNumb = itemView.findViewById<TextView>(R.id.tv_contract_number_worker)
        private val descriptionTv = itemView.findViewById<TextView>(R.id.tv_some_information)

        fun bindItem(position: Int, customer: Customer){
            val attestation = customer.attestation?.replace('*', '-', false)
            val contractNumberStr = "$attestation-${customer.carteRose}"
            val contractDateMilli = customer.echeance
            val itemPosition = position+1
            val contractDate = LocalDateTime.ofInstant(contractDateMilli?.let {
                Instant.ofEpochMilli(it) }, TimeZone.getDefault().toZoneId())

            val differenceDay = contractDate?.dayOfYear?.minus(LocalDateTime.now().dayOfYear)
            val description = "Le contrat de Mr/Mme <font color=#08FF00>${customer.customerName}</font> expire le " +
                    "<font color=#FF0000>${contractDate.toLocalDate()}</font>." +
                    " Qui est dans <font color=#4F34EE>${differenceDay}jours</font>. Appelez ou écrivez lui un" +
                    " message pour le rappeler de le renouveler"
            contractNumb.text = contractNumberStr
            index.text = (itemPosition).toString()
            descriptionTv.text = Html.fromHtml(description, Html.FROM_HTML_OPTION_USE_CSS_COLORS)
        }
    }

    private var expandedPosition: Int = -1

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

        val isExpanded = position == expandedPosition
        holder.itemView.findViewById<LinearLayout>(R.id.ll_contract_clicked)?.visibility =
            if (isExpanded) View.VISIBLE else View.GONE
        holder.itemView.isActivated = isExpanded
        holder.itemView.setOnClickListener {
            expandedPosition = if (isExpanded) -1 else position
            notifyItemChanged(position)
        }
    }
}