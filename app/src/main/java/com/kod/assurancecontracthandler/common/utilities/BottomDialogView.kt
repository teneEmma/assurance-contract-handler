package com.kod.assurancecontracthandler.common.utilities

import android.content.Context
import android.view.View
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.views.main.fragmentListContracts.GridViewItemAdapter

class BottomDialogView(
    private val carDetailsTitle: List<String>,
    private val pricesTitle: List<String>,
    private val providerTitle: String,
) {

    fun manageContractDetailViews(contractItemBinding: ContractDetailsBinding, c: Contract?, context: Context) {
        contractItemBinding.assurerName.text = c?.assure
        if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()) {
            val price = c?.DTA?.let { DataTypesConversionAndFormattingUtils.formatCurrencyPrices(it) }
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
            c?.DTA?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.PN?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.ACC?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.FC?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.TVA?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.CR?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.PTTC?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.COM_PN?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.COM_ACC?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.TOTAL_COM?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.NET_A_REVERSER?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.ENCAIS?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.COMM_LIMBE?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) },
            c?.COMM_APPORT?.let { DataTypesConversionAndFormattingUtils.formatIntegerPrice(it) }
        )
        val carValues: List<String?> = listOf(
            c?.attestation,
            c?.numeroPolice,
            c?.compagnie,
            c?.mark,
            c?.immatriculation,
            c?.chassis,
            c?.puissanceVehicule,
            c?.carteRose,
            c?.categorie?.toString(),
            c?.zone,
            DataTypesConversionAndFormattingUtils.notStringifyingNull(c?.duree)
        )
        val provider = "$providerTitle ${c?.APPORTEUR}"


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