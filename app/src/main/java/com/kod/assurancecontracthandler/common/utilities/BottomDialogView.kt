package com.kod.assurancecontracthandler.common.utilities

import android.content.Context
import android.view.View
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.views.main.fragmentListContracts.GridViewItemAdapter

class BottomDialogView(private val carDetailsTitle: List<String>, private val pricesTitle: List<String>) {

    fun manageContractDetailViews(contractItemBinding: ContractDetailsBinding, c: Contract?, context: Context) {
        contractItemBinding.assurerName.text = c?.assure
        if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()) {
            val price = "${c?.DTA} XAF"
            contractItemBinding.tvGrandTotal.visibility = View.VISIBLE
            contractItemBinding.llCarStuff.visibility = View.GONE
            contractItemBinding.tvProvider.visibility = View.GONE
            contractItemBinding.startEndDates.visibility = View.GONE
            contractItemBinding.dividerBottom.visibility = View.GONE
            contractItemBinding.dividerEffetEcheance.visibility = View.GONE
            contractItemBinding.tvGrandTotal.text = price
            return
        }

        contractItemBinding.tvGrandTotal.visibility = View.GONE
        contractItemBinding.llCarStuff.visibility = View.VISIBLE
        contractItemBinding.tvProvider.visibility = View.VISIBLE
        contractItemBinding.startEndDates.visibility = View.VISIBLE
        contractItemBinding.dividerBottom.visibility = View.VISIBLE
        contractItemBinding.dividerEffetEcheance.visibility = View.VISIBLE

        val carTitles = carDetailsTitle
        val pricesTitles = pricesTitle
        val pricesValues = listOf(
            c?.DTA.toString(),
            c?.PN.toString(),
            c?.ACC.toString(),
            c?.FC.toString(),
            c?.TVA?.toString(),
            c?.CR.toString(),
            c?.PTTC?.toString(),
            c?.COM_PN.toString(),
            c?.COM_ACC.toString(),
            c?.TOTAL_COM?.toString(),
            c?.NET_A_REVERSER.toString(),
            c?.ENCAIS.toString(),
            c?.COMM_LIMBE?.toString(),
            c?.COMM_APPORT.toString()
        )
        val carValues: List<String?> = listOf(
            c?.attestation,
            c?.numeroPolice,
            c?.compagnie,
            c?.mark,
            c?.immatriculation,
            c?.puissanceVehicule,
            c?.carteRose,
            c?.categorie?.toString(),
            c?.zone,
            DataTypesConversionAndFormattingUtils.notStringifyingNull(c?.duree)
        )
        val providerText = ConstantsVariables.searchBarChipsTitles[0]
        val provider = "${providerText}: ${c?.APPORTEUR}"


        contractItemBinding.tvProvider.text = provider
        contractItemBinding.startDate.text = c?.effet
        contractItemBinding.dueDate.text = c?.echeance
        contractItemBinding.gvPrices.adapter = GridViewItemAdapter(
            context, pricesTitles, pricesValues
        )
        contractItemBinding.gvCarStuff.adapter = GridViewItemAdapter(
            context, carTitles, carValues
        )
    }
}