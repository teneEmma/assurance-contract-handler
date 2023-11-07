package com.kod.assurancecontracthandler.views.expiringactivity

import android.Manifest
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.BuildConfig
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.common.utilities.SimpleItemTouchCallback
import com.kod.assurancecontracthandler.databinding.ActivityExpiringContractsBinding
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.databinding.PermissionDialogBinding
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.expiringviewmodel.ExpiringContractsViewModel
import com.kod.assurancecontracthandler.viewmodels.expiringviewmodel.ExpiringContractsViewModelFactory
import com.kod.assurancecontracthandler.views.customerdetails.CustomerDetailsActivity
import com.kod.assurancecontracthandler.views.main.fragmentListContracts.ContractListAdapter
import java.io.File

class ExpiringContractsActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var binding: ActivityExpiringContractsBinding
    private var adapter: ContractListAdapter? = null
    private lateinit var sharedPrefs: SharedPreferences
    private val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private var shouldGoToSettingsPage = false
    private val expiringContractViewModel by viewModels<ExpiringContractsViewModel> {
        val contractDao = ContractDatabase.getDatabase(this).contractDao()
        val contractRepository = ContractRepository(contractDao)
        ExpiringContractsViewModelFactory(contractRepository)
    }
    private val requestPermissionLauncher: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { hasPermission ->
            if (!hasPermission) {
                if (!shouldGoToSettingsPage) {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.setData(uri)
                    startActivity(intent)
                }
                return@registerForActivityResult
            }
            exportContractToFile()
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
        expiringContractViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.activityContent.progressBar.show()
            } else {
                binding.activityContent.progressBar.hide()
            }
        }
        expiringContractViewModel.messageResourceId.observe(this) { resourceId ->
            when (resourceId) {
                R.string.file_creation_successful -> showSnackWithAction(R.string.file_creation_successful) { openDocument() }
                else -> {
                    if (resourceId == null) {
                        return@observe
                    }
                    shortSnack(resources.getString(resourceId))
                }
            }
        }
        val maxNumber =
            sharedPrefs.getString(resources.getString(R.string.expiring_notifications_periodicity_key), "1")
        expiringContractViewModel.getExpiringContracts(maxNumber?.toLong() ?: 1L)
        setUpSearchBar()
        setupRecyclerView()
    }

    private fun openDocument() {
        val fileName = expiringContractViewModel.createdFileName ?: return
        val file = File(fileName)
        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
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
        ItemTouchHelper(
            SimpleItemTouchCallback(
                this,
                adapter!!,
                onSwipeCallback = { idItemSlided ->
                    expiringContractViewModel.idItemSlided = idItemSlided
                    if (expiringContractViewModel.isLoading.value == true) {
                        shortSnack(resources.getString(R.string.wait_file_creation_to_complete))
                        return@SimpleItemTouchCallback
                    }
                    checkPermissionsStatus()
                }
            )
        ).attachToRecyclerView(binding.activityContent.rvExpiringContracts)
    }

    private fun checkPermissionsStatus() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                requiredPermission,
            ) == PackageManager.PERMISSION_GRANTED -> {
                checkManageExternalStoragePermission()
            }

            shouldShowRequestPermissionRationale(requiredPermission) -> {
                shouldGoToSettingsPage = true
                showPermissionDialog()
            }

            else -> {
                shouldGoToSettingsPage = false
                checkManageExternalStoragePermission()
            }
        }
    }

    private fun exportContractToFile() {
        expiringContractViewModel.exportContractToFile(
            assets,
        )
    }

    private fun showPermissionDialog() {
        val dialogBinding = PermissionDialogBinding.inflate(layoutInflater)
        val dialog = Dialog(this)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(dialogBinding.root)
        dialog.show()

        dialogBinding.btnDialogPositive.setOnClickListener {
            checkManageExternalStoragePermission()
            dialog.dismiss()
        }
    }

    private fun checkManageExternalStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                val uri = Uri.fromParts("package", packageName, null)
                intent.setData(uri)
                startActivity(intent)
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                return
            }
        }
        requestPermissionLauncher.launch(
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
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

    private fun showSnackWithAction(message: Int, action: () -> Unit) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.view_text) {
                action()
            }.show()
    }

    private fun shortSnack(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
}