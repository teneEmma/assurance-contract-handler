package com.kod.assurancecontracthandler.views.fragments.contractsvisualizer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.ContractDbDto
import java.text.SimpleDateFormat
import java.util.*

class ContractListAdapter(private val listContracts: List<ContractDbDto>): RecyclerView.Adapter<ContractListAdapter.ContractViewHolder>() {

    class ContractViewHolder(view: View): RecyclerView.ViewHolder(view){
        val id: TextView = view.findViewById(R.id.column_id)
        private val assurerName: TextView = itemView.findViewById(R.id.column_assure)
        private val dateContracted: TextView = itemView.findViewById(R.id.column_effet)
        private val expiryDate: TextView = itemView.findViewById(R.id.column_echeance)
        private val attestation: TextView = itemView.findViewById(R.id.column_attestion)
        private val carteGrise: TextView = itemView.findViewById(R.id.column_carteRose)

        fun bindItems(contract: ContractDbDto){
            id.text = contract.id.toString()
            assurerName.text = contract.contract!!.assure.toString()
            dateContracted.text = contract.contract.effet?.let {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
            expiryDate.text = contract.contract.echeance?.let {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it)
            }
            attestation.text = contract.contract.attestation.toString()
            carteGrise.text = contract.contract.carteRose.toString()
            setBackgroundColor(Integer.parseInt(id.text.toString()))
        }

        private fun setBackgroundColor(id: Int){
            if(id%2 == 0){
                return itemView.setBackgroundColor(Color.parseColor("#FFC5C5C5"))
            }
            itemView.setBackgroundColor(Color.WHITE)
        }
    }
    override fun getItemCount(): Int = listContracts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder
            = ContractViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent,false))

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val currentHabit = listContracts[position]

        holder.bindItems(currentHabit)
    }
}