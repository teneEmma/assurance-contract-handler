package com.kod.assurancecontracthandler.views.expiringactivity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.databinding.ActivityExpiringContractsBinding
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.ContractListAdapter
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.GridViewItemAdapter
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class ExpiringContractsActivity : AppCompatActivity() {
    var binding: ActivityExpiringContractsBinding? = null
    private lateinit var dbViewModel: DBViewModel
    private var expListContracts = MutableLiveData<List<ContractDbDto>?>()
    private var adapter: ContractListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiringContractsBinding.inflate(layoutInflater)
        dbViewModel = ViewModelProvider(this,
            DBViewModelFactory(this.application))[DBViewModel::class.java]
        setContentView(binding!!.root)

        getExpiringContracts()
        setRecyclerView()

        expListContracts.observe(this) { listContracts->
            listContracts?.let { adapter?.setContractList(it) }
        }
    }

    override fun onDestroy() {
        binding = null
        adapter = null
        super.onDestroy()
    }

    private fun setRecyclerView(){
        adapter = ContractListAdapter({ contractDbDto ->
            showDialog(contractDbDto)
        }, {})

        adapter!!.isExpiringActivity = true
        binding?.rvSlidersWorker?.adapter = adapter
        binding?.rvSlidersWorker?.addItemDecoration(
            DividerItemDecoration(
                this,
                DividerItemDecoration.VERTICAL
            )
        )
        binding?.rvSlidersWorker?.layoutManager = LinearLayoutManager(applicationContext)
        binding?.rvSlidersWorker?.setHasFixedSize(true)
    }

    private fun showDialog(contractDbDto: ContractDbDto) {
        val contractItemBinding = ContractDeetailsBinding.inflate(layoutInflater)
        val dialogTouchContract = BottomSheetDialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        val c = contractDbDto.contract
        manageContractDetailViews(contractItemBinding, c)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract.window?.setLayout(width, height)
        dialogTouchContract.show()

        contractItemBinding.assureName.setOnClickListener {
            val customer = Customer(c?.assure, c?.telephone.toString())
            customer.echeance = c?.echeance?.time
            customer.mark = c?.mark
            customer.immatriculation = c?.immatriculation
            customer.numeroPolice = c?.numeroPolice
            customer.carteRose = c?.carteRose
            customer.effet = c?.effet?.time
            val intent = Intent(this, CustomerDetailsActivity::class.java)
            intent.putExtra(ConstantsVariables.customerKey, customer)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialogTouchContract.dismiss()
        }
    }

    private fun getExpiringContracts() {
        val today = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)*1000
        val maxDate = LocalDateTime.now().plusMonths(1).toEpochSecond(ZoneOffset.UTC)*1000

        dbViewModel.apply {
            executeFunWithoutAnimation {
                expListContracts.postValue(fetchExpiringContractsIn(today, maxDate))
            }
        }
    }

    private fun manageContractDetailViews(contractItemBinding: ContractDeetailsBinding, c: Contract?){
        contractItemBinding.assureName.text = c?.assure
        if (c?.numeroPolice.isNullOrEmpty() || c?.attestation.isNullOrEmpty()){
            val price = "${c?.DTA} XAF"
            contractItemBinding.tvGrandTotal.visibility = View.VISIBLE
            contractItemBinding.llCarStuff.visibility = View.GONE
            contractItemBinding.tvApporteur.visibility = View.GONE
            contractItemBinding.effetEcheance.visibility = View.GONE
            contractItemBinding.dividerBottom.visibility = View.GONE
            contractItemBinding.dividerEffetEcheance.visibility = View.GONE
            contractItemBinding.tvGrandTotal.text = price
            return
        }

        contractItemBinding.tvGrandTotal.visibility = View.GONE
        contractItemBinding.llCarStuff.visibility = View.VISIBLE
        contractItemBinding.tvApporteur.visibility = View.VISIBLE
        contractItemBinding.effetEcheance.visibility = View.VISIBLE
        contractItemBinding.dividerBottom.visibility = View.VISIBLE
        contractItemBinding.dividerEffetEcheance.visibility = View.VISIBLE

        val carTitles = ConstantsVariables.carDetailsTitle
        val pricesTitles = ConstantsVariables.pricesTitle
        val pricesValues = listOf(
            c?.DTA.toString(), c?.PN.toString(), c?.ACC.toString(), c?.FC.toString(),
            c?.TVA?.toString(),c?.CR.toString(), c?.PTTC?.toString(), c?.COM_PN.toString(),
            c?.COM_ACC.toString(), c?.TOTAL_COM?.toString(), c?.NET_A_REVERSER.toString(),
            c?.ENCAIS.toString(), c?.COMM_LIMBE?.toString(), c?.COMM_APPORT.toString() )
        val carValues: List<String?> = listOf(
            c?.mark, c?.immatriculation, c?.puissanceVehicule, c?.carteRose, c?.categorie?.toString(), c?.zone )
        val apporteur = "APPORTEUR: ${c?.APPORTEUR}"


        contractItemBinding.tvApporteur.text = apporteur
        contractItemBinding.dateEffet.text = c?.effet?.let {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
        contractItemBinding.dateEcheance.text = c?.echeance?.let {
            SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(it) }
        contractItemBinding.gvPrices.adapter = GridViewItemAdapter(this,
            pricesTitles, pricesValues)
        contractItemBinding.gvCarStuff.adapter = GridViewItemAdapter(this,
            carTitles, carValues)

    }
}