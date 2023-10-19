package com.kod.assurancecontracthandler.views.customerdetails

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.databinding.ContractListItemBinding
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Contract

class CustomerContractsAdapter(
    private val listContracts: List<BaseContract>,
    private val itemClicked: (Contract) -> Unit,
    private val activeStateTouched: (Boolean) -> Unit,
    private val copyBtnTouched: (Contract?) -> Unit,
    private val actualContract: Contract?,
    private val colorForChosenContract: Int
) :
    RecyclerView.Adapter<CustomerContractsAdapter.ContractViewHolder>() {

    class ContractViewHolder(
        view: View,
        private val actualContract: Contract?,
        private val colorForChosenContract: Int,
        private val context: Context,
    ) : RecyclerView.ViewHolder(view) {
        val binding = ContractListItemBinding.bind(view)

        fun bindView(baseContract: BaseContract) {
            val attestation = baseContract.contract?.attestation?.replace('*', '-', false)
            val contractNumberStr = "$attestation-${baseContract.contract?.carteRose}"
            if (baseContract.contract == actualContract) {
                binding.contractListItem.setBackgroundResource(colorForChosenContract)
                animateActualContractItem()
            }
            binding.tvContractNumber.text = contractNumberStr
            binding.tvPoliceNumber.text = baseContract.contract?.numeroPolice
            binding.tvStartDate.text = baseContract.contract?.effet
            binding.tvDueDate.text = baseContract.contract?.echeance
            val myContract = baseContract.contract
            if (myContract?.isContractActive() == true)
                binding.ivContractActiveState.setColorFilter(Color.GREEN)
            else
                binding.ivContractActiveState.setColorFilter(Color.RED)
        }

        private fun animateActualContractItem() {
            val animationImageView = AnimationUtils.loadAnimation(context, R.anim.zoom_in_and_out)
            binding.contractListItem.startAnimation(animationImageView)
        }

    }

    override fun onBindViewHolder(holder: ContractViewHolder, position: Int) {
        val currentContract = listContracts[position]

        holder.bindView(currentContract)
        holder.binding.ivContractActiveState.setOnClickListener {
            activeStateTouched.invoke(currentContract.contract?.isContractActive() == true)

        }
        holder.binding.ivCopyContractDetails.setOnClickListener {
            copyBtnTouched.invoke(currentContract.contract)
        }
        holder.itemView.setOnClickListener {
            currentContract.contract?.let { it1 -> itemClicked.invoke(it1) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContractViewHolder =
        ContractViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.contract_list_item, parent, false),
            actualContract,
            colorForChosenContract,
            parent.context
        )

    override fun getItemCount(): Int = listContracts.size
}