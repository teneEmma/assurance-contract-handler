package com.kod.assurancecontracthandler.views.expiringactivity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.databinding.ActivityExpiringContractsBinding
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.expiringviewmodel.ExpiringContractsViewModel
import com.kod.assurancecontracthandler.viewmodels.expiringviewmodel.ExpiringContractsViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.main.fragmentListContracts.ContractListAdapter

class ExpiringContractsActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityExpiringContractsBinding
    private var adapter: ContractListAdapter? = null
    private lateinit var sharedPrefs: SharedPreferences
    private val expiringContractViewModel by viewModels<ExpiringContractsViewModel> {
        val contractDao = ContractDatabase.getDatabase(this).contractDao()
        val contractRepository = ContractRepository(contractDao)
        ExpiringContractsViewModelFactory(contractRepository)
    }

    override fun onDestroy() {
        adapter = null
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityExpiringContractsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this)
        expiringContractViewModel.contractsToExpire.observe(this) { contracts2Expire ->
            if (contracts2Expire.isNullOrEmpty() && expiringContractViewModel.thereIsNoContractToExpire) {
                binding.activityContent.ivNoExpiringContract.visibility = View.VISIBLE
                binding.activityContent.tvNoExpiringContract.visibility = View.VISIBLE
                binding.activityContent.rvExpiringContracts.visibility = View.GONE
                shortSnack(resources.getString(R.string.no_result_found))
                return@observe
            }
            binding.activityContent.ivNoExpiringContract.visibility = View.GONE
            binding.activityContent.tvNoExpiringContract.visibility = View.GONE
            binding.activityContent.rvExpiringContracts.visibility = View.VISIBLE
            adapter?.setContractList(contracts2Expire!!)
        }

        val maxNumber =
            sharedPrefs.getString(resources.getString(R.string.expiring_notifications_periodicity_key), "1")
        expiringContractViewModel.getExpiringContracts(maxNumber?.toLong() ?: 1L)
        setUpSearchBar()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        adapter = ContractListAdapter(clickFunction = { baseContract ->
            showBottomDialog(baseContract)
        }, touchFunction = {})

        binding.activityContent.apply {
            rvExpiringContracts.adapter = adapter
            rvExpiringContracts.layoutManager = LinearLayoutManager(applicationContext)
            rvExpiringContracts.setHasFixedSize(true)
        }
    }

    private fun showBottomDialog(baseContract: BaseContract) {
        val contractItemBinding = ContractDetailsBinding.inflate(layoutInflater)
        val dialogTouchContract = BottomSheetDialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        val c = baseContract.contract
        val carDetailsListTitles = resources.getStringArray(R.array.car_details_title).toList()
        val priceDetailsListTitles = resources.getStringArray(R.array.price_details_title).toList()
        BottomDialogView(
            carDetailsTitle = carDetailsListTitles,
            pricesTitle = priceDetailsListTitles,
            providerTitle = resources.getString(R.string.provider_text),
        ).manageContractDetailViews(contractItemBinding, c, this)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract.window?.setLayout(width, height)
        dialogTouchContract.show()

        contractItemBinding.assurerName.setOnClickListener {
            val customer = Customer(c?.assure, c?.telephone.toString())
            customer.actualContractNumber = baseContract.id
            val intent = Intent(this, CustomerDetailsActivity::class.java)
            intent.putExtra(ConstantsVariables.relatedContractIdKey, customer.actualContractNumber)
            intent.putExtra(ConstantsVariables.customerNameKey, customer.customerName)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            dialogTouchContract.dismiss()
        }
    }

    private fun setUpSearchBar() {
        binding.activityContent.apply {
            searchView.queryHint = resources.getString(R.string.search_bar_query_hint)
            searchView.isSubmitButtonEnabled = false
            searchView.background =
                AppCompatResources.getDrawable(this@ExpiringContractsActivity, R.drawable.elevated_zone)
            searchView.setIconifiedByDefault(false)
            searchView.setOnQueryTextListener(this@ExpiringContractsActivity)

            searchView.findViewById<ImageView>(com.google.android.material.R.id.search_close_btn).setOnClickListener {
                searchView.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
            }
        }

        binding.activityContent.btnDismissSearch.setOnClickListener {
            deactivateSearchBar()
            this.onQueryTextSubmit("")
        }
    }

    private fun deactivateSearchBar() {
        binding.activityContent.apply {
            searchView.findViewById<TextView>(com.google.android.material.R.id.search_src_text).text = ""
            searchView.isActivated = false
        }
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        expiringContractViewModel.searchExpiringContractForAssurer(query)

        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        expiringContractViewModel.searchExpiringContractForAssurer(newText)

        return true
    }

    private fun shortSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}