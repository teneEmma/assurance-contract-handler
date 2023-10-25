package com.kod.assurancecontracthandler.views.main.fragmentListContracts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontrac.ContractsCallback
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.databinding.RvContractItemBinding
import com.kod.assurancecontracthandler.model.BaseContract
import java.util.*

class ContractListAdapter(
    private val clickFunction: (BaseContract) -> Unit,
    private val touchFunction: () -> Unit
) :
    RecyclerView.Adapter<ContractListAdapter.ContractViewHolder>() {
    private var contractList: List<BaseContract> = emptyList()

    inner class ContractViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private var itemBinding = RvContractItemBinding.bind(itemView)

        fun bindItems(contract: BaseContract, position: Int) {
            itemBinding.apply {
                val actualPosition = position + 1
                columnId.text = (actualPosition).toString()

                val grandTotal = contract.contract?.DTA?.let {
                    DataTypesConversionAndFormattingUtils.formatCurrencyPrices(
                        it
                    )
                }

                columnAssure.text = contract.contract!!.assure
                columnStartDate.text = contract.contract.effet
                columnDueDate.text = contract.contract.echeance
                columnPlateNumber.text = contract.contract.immatriculation
                columnAttestation.text = contract.contract.attestation
                tvTotal.text = grandTotal
            }
        }
    }

    override fun getItemCount(): Int = contractList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder =
        ContractViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.rv_contract_item, parent, false)
        )

    fun setContractList(contractList: List<BaseContract>) {
        if (this.contractList.isEmpty()) {
            this.contractList = contractList
            notifyDataSetChanged()
        } else {
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
        holder.itemView.setOnLongClickListener {
            clickFunction(currentHabit)
            true
        }

        holder.itemView.setOnClickListener {
            clickFunction(currentHabit)
        }

        holder.itemView.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP)
                touchFunction()
            false
        }
    }
}