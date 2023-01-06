package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.model.ContractDbDto
import java.text.SimpleDateFormat
import java.util.*

class ContractListAdapter(private val listContracts: List<ContractDbDto>, private val clickFunction: (ContractDbDto) -> Unit, private val touchFunction: ()-> Unit): RecyclerView.Adapter<ContractListAdapter.ContractViewHolder>() {

    class ContractViewHolder(view: View): RecyclerView.ViewHolder(view){
        val id: TextView = view.findViewById(R.id.column_id)
        private val assurerName: TextView = itemView.findViewById(R.id.column_assure)
        private val dateContracted: TextView = itemView.findViewById(R.id.column_effet)
        private val expiryDate: TextView = itemView.findViewById(R.id.column_echeance)
        private val attestation: TextView = itemView.findViewById(R.id.column_attestion)
        private val carteGrise: TextView = itemView.findViewById(R.id.column_carteRose)
        private val total: TextView = itemView.findViewById(R.id.tv_total)

        fun bindItems(contract: ContractDbDto){
            val isFooter = (contract.contract?.numeroPolice.isNullOrEmpty() ||
                    contract.contract?.attestation.isNullOrEmpty())
            id.text = contract.id.toString()

            val grandTotal = "${contract.contract?.DTA.toString()}XAF"

            assurerName.text = contract.contract!!.assure.toString()
            dateContracted.text = contract.contract.effet?.let {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
            expiryDate.text = contract.contract.echeance?.let {
                SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it)
            }
            attestation.text = contract.contract.attestation.toString()
            carteGrise.text = contract.contract.carteRose.toString()
            total.text = grandTotal
            setBackgroundColor(Integer.parseInt(id.text.toString()), isFooter)
        }

        private fun setBackgroundColor(id: Int, isFooter: Boolean){
            if(id%2 == 0 && !isFooter) {
                showView(false)
            }
            else if(id%2 == 1 && !isFooter) {
                itemView.setBackgroundColor(Color.DKGRAY)
                showView(false)
            }
            else {
                itemView.setBackgroundColor(Color.parseColor("#FFBE9DED"))
                showView(true)
            }
        }

        private fun showView(isFooter: Boolean){
            var otherVisibility = View.VISIBLE
            var footerVisibility = View.GONE
            if (isFooter){
                otherVisibility = View.GONE
                footerVisibility = View.VISIBLE
            }

            id.visibility = otherVisibility
            val dates = itemView.findViewById<LinearLayout>(R.id.dates)
            val attestationCarte = itemView.findViewById<LinearLayout>(R.id.attestationCarte)
            dates.visibility = otherVisibility
            attestationCarte.visibility = otherVisibility
            total.visibility = footerVisibility
        }
    }
    override fun getItemCount(): Int = listContracts.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder
            = ContractViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent,false))

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val currentHabit = listContracts[position]

        holder.bindItems(currentHabit)
        holder.itemView.setOnLongClickListener{
            clickFunction(currentHabit)
            true
        }

        holder.itemView.setOnClickListener{
            clickFunction(currentHabit)
        }

        holder.itemView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP)
                touchFunction()
            false
        }


    }
}