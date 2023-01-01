package com.kod.assurancecontracthandler.views.customerdetails

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.ContractDbDto
import java.util.Calendar

class CustomerContractsAdapter(private val listContracts: List<ContractDbDto>,private val itemClicked: (Contract) -> Unit, private val activeStateTouched: (Boolean) -> Unit):
    RecyclerView.Adapter<CustomerContractsAdapter.ContractViewHolder>() {


    class ContractViewHolder(view: View): RecyclerView.ViewHolder(view){
        private val contractNumber = itemView.findViewById<TextView>(R.id.tv_contract_number)
        private val contractState = itemView.findViewById<ImageView>(R.id.iv_contract_active_state)

        fun bindView(contract: ContractDbDto){
            val attestation = contract.contract?.attestation?.replace('*', '-', false)
            val contractNumberStr = "$attestation-${contract.contract?.carteRose}"
            contractNumber.text = contractNumberStr
            val myContract = contract.contract
            if(myContract?.let { isContractActive(it) } == true)
                contractState.setColorFilter(Color.GREEN)
            else
                contractState.setColorFilter(Color.RED)
        }

        fun isContractActive(contract: Contract): Boolean{
            val today = Calendar.getInstance().time
            return contract.effet?.let { value ->
                today >= value  } == true && contract.echeance?.let { value ->
                today  <= value  } == true
        }
    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val currentContract = listContracts[position]

        holder.bindView(currentContract)
        holder.itemView.findViewById<ImageView>(R.id.iv_contract_active_state).setOnClickListener{
            currentContract.contract?.let { it1 ->
                activeStateTouched.invoke(holder.isContractActive(it1)) }
        }
        holder.itemView.setOnClickListener {
            currentContract.contract?.let { it1 -> itemClicked.invoke(it1) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder =
        ContractViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.contract_list_item, parent, false))

    override fun getItemCount(): Int  = listContracts.size
}