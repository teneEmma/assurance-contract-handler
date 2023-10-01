package com.kod.assurancecontracthandler.views.fragments.home.contractlist

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontrac.ContractsCallback
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.RvItemBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import java.text.SimpleDateFormat
import java.util.*

class ContractListAdapter(private val clickFunction: (ContractDbDto) -> Unit,
                          private val touchFunction: ()-> Unit):
    RecyclerView.Adapter<ContractListAdapter.ContractViewHolder>() {
    private var contractList: List<ContractDbDto> = emptyList()
    var isExpiringActivity = false

    inner class ContractViewHolder(view: View): RecyclerView.ViewHolder(view){
        private var itemBinding = RvItemBinding.bind(itemView)

        fun bindItems(contract: ContractDbDto, position: Int) {
            val isFooter = (contract.contract?.numeroPolice.isNullOrEmpty() ||
                    contract.contract?.attestation.isNullOrEmpty())
            itemBinding.apply {
                columnId.text = (position+1).toString()

                val grandTotal = "${contract.contract?.DTA.toString()}XAF"

                columnAssure.text = contract.contract!!.assure.toString()
                columnEffet.text = contract.contract.effet?.let {
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it)
                }
                columnEcheance.text = contract.contract.echeance?.let {
                    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it)
                }
                columnImmatriculation.text = contract.contract.immatriculation.toString()
                columnCarteRose.text = contract.contract.carteRose.toString()
                tvTotal.text = grandTotal
                if (isExpiringActivity) {
                    columnEffet.visibility = View.GONE
                    columnCarteRose.visibility = View.GONE
                    titleEffet.visibility = View.GONE
                    titleCarteRose.visibility = View.GONE
                }
            }
            setBackgroundColor(isFooter)
        }

        private fun setBackgroundColor(isFooter: Boolean){
            if(!isFooter) {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.dialog_background))
                showView(false)
            } else {
                itemView.setBackgroundColor(itemView.context.getColor(R.color.footer_color))
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
            itemBinding.columnId.visibility = otherVisibility
            val dates = itemBinding.dates
            val attestationCarte = itemBinding.attestationCarte
            dates.visibility = otherVisibility
            attestationCarte.visibility = otherVisibility
            itemBinding.tvTotal.visibility = footerVisibility
        }
    }
    override fun getItemCount(): Int = contractList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder
            = ContractViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.rv_item, parent,false))

    fun setContractList(contractList: List<ContractDbDto>){
        if (this.contractList.isEmpty()){
            this.contractList = contractList
            notifyDataSetChanged()
        }else{
            val diffCallback = ContractsCallback(this.contractList, contractList)
            val diffContracts = DiffUtil.calculateDiff(diffCallback)

            this.contractList = contractList
            diffContracts.dispatchUpdatesTo(this)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val currentHabit = contractList[position]

        holder.bindItems(currentHabit, position)
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