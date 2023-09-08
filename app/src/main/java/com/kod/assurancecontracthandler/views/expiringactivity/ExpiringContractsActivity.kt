package com.kod.assurancecontracthandler.views.expiringactivity

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.databinding.ActivityExpiringContractsBinding
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.ContractListAdapter
import java.time.LocalDateTime
import java.time.ZoneOffset

class ExpiringContractsActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
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
        setupSearchView()
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
        BottomDialogView().manageContractDetailViews(contractItemBinding, c, this)

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

    private fun setupSearchView(){
        binding!!.searchView.queryHint = resources.getString(R.string.search_bar_query_hint_exp)
        binding!!.searchView.isSubmitButtonEnabled = false
        binding!!.searchView.background = AppCompatResources.getDrawable(this, R.drawable.elevated_zone)
        binding!!.searchView.setIconifiedByDefault(false)
        binding!!.searchView.setOnQueryTextListener(this)

        binding!!.searchView.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn).apply {
            setOnClickListener {
                binding!!.searchView.
                findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
            }
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        expListContracts.postValue(expListContracts.value?.filter {
            query?.uppercase()?.let { queryTxt->
                it.contract?.assure?.uppercase()?.contains(queryTxt) } == true
        })

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        expListContracts.postValue(expListContracts.value?.filter {
            newText?.uppercase()?.let { newTxt ->
                it.contract?.assure?.uppercase()?.contains(newTxt) } == true
        })

        return true
    }
}