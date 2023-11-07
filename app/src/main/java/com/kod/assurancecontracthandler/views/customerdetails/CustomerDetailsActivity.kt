package com.kod.assurancecontracthandler.views.customerdetails

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
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doOnTextChanged
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.kod.assurancecontracthandler.BuildConfig
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.usecases.ContactAction
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.common.utilities.SimpleItemTouchCallback
import com.kod.assurancecontracthandler.databinding.ActivityCustomerDetailsBinding
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.databinding.EditCustomerDialogBinding
import com.kod.assurancecontracthandler.databinding.PermissionDialogBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.repository.CustomerRepository
import com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel.CustomerDetailsViewModel
import com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel.CustomerDetailsViewModelFactory
import java.io.File
import java.net.URLEncoder


class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailsBinding
    private lateinit var dataStore: DataStoreRepository
    private val requiredPermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private var shouldGoToSettingsPage = false
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
    private val customerDetailsViewModel by viewModels<CustomerDetailsViewModel> {
        val customerDao = ContractDatabase.getDatabase(this).customerDao()
        val contractDao = ContractDatabase.getDatabase(this).contractDao()
        val customerRepository = CustomerRepository(customerDao)
        val contractRepository = ContractRepository(contractDao)
        CustomerDetailsViewModelFactory(customerRepository, contractRepository)
    }
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        setContentView(binding.root)
        dataStore = DataStoreRepository(
            sharedPreferences,
            resources.getString(R.string.predefined_message_key),
            resources.getString(R.string.first_install_key)
        )

        customerDetailsViewModel.customerContracts.observe(this) { contracts ->
            val contractRecyclerAdapter = CustomerContractsAdapter(listContracts = contracts,
                colorForChosenContract = R.color.highlighted_actual_contract,
                actualContract = customerDetailsViewModel.relatedContract?.contract,
                itemClicked = { contract ->
                    contractItemSelected(contract)
                },
                copyBtnTouched = { contract ->
                    val message = formatMessageToSend(contract)
                    customerDetailsViewModel.setMessageToSend(message)
                    binding.etMessageToSend.setText(customerDetailsViewModel.messageToSend)
                },
                activeStateTouched = { isActive ->
                    if (isActive) shortToast(resources.getString(R.string.active_contract_text))
                    else shortToast(resources.getString(R.string.contract_state_inactive))
                })
            binding.recyclerActiveContracts.adapter = contractRecyclerAdapter
            binding.recyclerActiveContracts.layoutManager = LinearLayoutManager(applicationContext)
            binding.recyclerActiveContracts.setHasFixedSize(true)
            ItemTouchHelper(
                SimpleItemTouchCallback<CustomerContractsAdapter.ContractViewHolder>(this,
                    contractRecyclerAdapter,
                    onSwipeCallback = { idItemSlided ->
                        customerDetailsViewModel.idItemSlided = idItemSlided
                        if (customerDetailsViewModel.isLoading.value == true) {
                            shortSnack(resources.getString(R.string.wait_file_creation_to_complete))
                            return@SimpleItemTouchCallback
                        }
                        checkPermissionsStatus()
                    })
            ).attachToRecyclerView(binding.recyclerActiveContracts)
        }

        customerDetailsViewModel.isLoading.observe(this) { isLoading ->
            if (isLoading) {
                binding.progressBar.show()
            } else {
                binding.progressBar.hide()
            }
        }

        customerDetailsViewModel.messageResourceId.observe(this) { resourceId ->
            Log.d("XXX MESSAGE XXX", "${resourceId?.let { resources.getString(it) }}")
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

        customerDetailsViewModel.actualCustomer.observe(this) {
            customerDetailsViewModel.fetchCustomerContracts()
            setViews()
        }

        binding.ivModify.setOnClickListener {
            showModificationDialog()
        }

        customerDetailsViewModel.getActualCustomerDetails(intent)
    }

    private fun checkPermissionsStatus() {
        when {
            ContextCompat.checkSelfPermission(
                this, requiredPermission
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

    private fun shortSnack(message: String) {
        Snackbar.make(this.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT).show()
    }

    private fun showSnackWithAction(message: Int, action: () -> Unit) {
        Snackbar.make(this.findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
            .setAction(R.string.view_text) {
                action()
            }.show()
    }

    private fun exportContractToFile() {
        customerDetailsViewModel.exportContractToFile(
            assets,
        )
    }


    private fun showModificationDialog() {
        val editCustomerBinding = EditCustomerDialogBinding.inflate(layoutInflater)
        val editCustomerDialog = Dialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(editCustomerBinding.root)
        }

        val customer = customerDetailsViewModel.actualCustomer.value
        customer?.apply {
            editCustomerBinding.tvAssureName.text = customerName?.uppercase()
            editCustomerBinding.ilCustomerName.hint = customerName?.uppercase()
            editCustomerBinding.ilPhoneNumber.hint = phoneNumber
        }

        editCustomerBinding.etUsername.doOnTextChanged { text, _, _, _ ->
            val stringResourceId = customerDetailsViewModel.onCustomerNameChanging(text.toString().trim().uppercase())
            editCustomerBinding.btnModify.isEnabled = customerDetailsViewModel.btnModifyState == true
            if (stringResourceId != null) {
                shortToast(resources.getString(stringResourceId))
            }
        }

        editCustomerBinding.etPhoneNumber.doOnTextChanged { text, _, _, _ ->
            val stringResourceId =
                customerDetailsViewModel.onCustomerPhoneNumberChanging(text.toString().trim().uppercase())
            editCustomerBinding.btnModify.isEnabled = customerDetailsViewModel.btnModifyState == true
            if (stringResourceId != null) {
                shortToast(resources.getString(stringResourceId))
            }
        }

        editCustomerBinding.btnCancel.setOnClickListener {
            editCustomerDialog.dismiss()
            customerDetailsViewModel.onDismissModifyCustomerDetailsDialog()
        }

        editCustomerBinding.btnModify.setOnClickListener {
            customerDetailsViewModel.updateCustomerDetails()
            editCustomerDialog.dismiss()
            customerDetailsViewModel.onDismissModifyCustomerDetailsDialog()

            R.string.modification_successful
        }

        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()

        editCustomerDialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        editCustomerDialog.show()
    }

    private fun openDocument() {
        val fileName = customerDetailsViewModel.createdFileName ?: return
        val file = File(fileName)
        val uri = FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.provider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/vnd.ms-excel")
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        startActivity(intent)
    }

    private fun setViews() {
        val customer = customerDetailsViewModel.actualCustomer.value

        // Underlining text.
        val content = SpannableString(customer?.customerName)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.tvCustomerName.text = content
        binding.ivCustomerProfile.text =
            DataTypesConversionAndFormattingUtils.setNameInitials(customer?.customerName).uppercase()
        if (customer?.phoneNumber.isNullOrEmpty() || customer?.phoneNumber == null) {
            binding.tvContactCustomerText.text = resources.getString(R.string.no_contact_text)
            binding.tvContactCustomerText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.tvContactCustomerText.setTextColor(Color.RED)
        } else {
            customer.phoneNumber?.let {
                val recyclerView = CustomerContactAdapter(listOf(it)) { action, _ ->
                    when (action) {
                        ContactAction.CALL.action -> makePhoneCall()
                        ContactAction.SMS.action -> showMessageLayout(customerDetailsViewModel.relatedContract?.contract) { msg ->
                            sendSMS(
                                msg
                            )
                        }

                        ContactAction.WHATSAPP_TEXT.action -> showMessageLayout(customerDetailsViewModel.relatedContract?.contract) { msg ->
                            sendWhatsappMsg(
                                msg
                            )
                        }
                    }
                }
                binding.tvContactCustomerText.text = resources.getString(R.string.contact_customer)
                binding.recyclerContact.adapter = recyclerView
                binding.recyclerContact.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerContact.setHasFixedSize(true)
            }
        }
    }

    private fun showMessageLayout(contract: Contract?, function: (String) -> Unit) {

        if (contract?.echeance.isNullOrEmpty() && contract?.mark.isNullOrEmpty()) {
            customerDetailsViewModel.setMessageToSend(null)
            binding.btnSendMessage.isEnabled = false
        } else {
            val message = formatMessageToSend(contract)
            customerDetailsViewModel.setMessageToSend(message)
            binding.etMessageToSend.setText(customerDetailsViewModel.messageToSend)
            binding.btnSendMessage.isEnabled = true
        }

        binding.apply {
            llEnterMessage.visibility = View.VISIBLE
            etMessageToSend.doOnTextChanged { text, _, _, _ ->
                if (text?.length!! > 0) binding.btnSendMessage.isEnabled = true
            }
            btnCancelMessage.setOnClickListener {
                etMessageToSend.text?.clear()
                llEnterMessage.visibility = View.GONE
            }
            btnSendMessage.setOnClickListener {
                function.invoke(etMessageToSend.text.toString())
                etMessageToSend.text?.clear()
            }
        }
    }

    private fun formatMessageToSend(contract: Contract?): String {
        var message = dataStore.readPredefinedMessage()
        if (message.isNullOrEmpty()) {
            message = resources.getString(R.string.predefined_message)
        }
        if (contract?.isContractActive() != true) {
            message = message.replace(ConstantsVariables.futureTenseExpire, ConstantsVariables.pastTenseExpire)
        }
        return message.replace(ConstantsVariables.predefinedMsgVehicleId, contract?.mark ?: "")
            .replace(ConstantsVariables.predefinedMsgEndDateId, contract?.echeance ?: "")
            .replace(ConstantsVariables.predefinedMsgStartDateId, contract?.effet ?: "")
            .replace(ConstantsVariables.predefinedMsgAssurerId, contract?.assure ?: "")
            .replace(ConstantsVariables.predefinedMsgPinkCardId, contract?.carteRose ?: "")
            .replace(ConstantsVariables.predefinedMsgCategoryId, contract?.categorie.toString() ?: "")
            .replace(ConstantsVariables.predefinedMsgAttestationId, contract?.attestation ?: "")
            .replace(ConstantsVariables.predefinedImmatricualtionId, contract?.immatriculation ?: "")
            .replace(ConstantsVariables.predefinedChassisId, contract?.chassis ?: "")
    }

    private fun sendSMS(msg: String) {
        val customer = customerDetailsViewModel.actualCustomer.value
        val smsURI = Uri.parse(ConstantsVariables.smsURIPrefix + customer?.phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW, smsURI)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(intent)
    }

    private fun sendWhatsappMsg(msg: String) {
        val customer = customerDetailsViewModel.actualCustomer.value
        for (packageName in ConstantsVariables.whatsappPackages) {
            if (isWhatsappInstalled(packageName)) {
                val whatsappURI = Uri.parse(
                    "whatsapp://send?phone=" + "${ConstantsVariables.phoneIndex}${customer?.phoneNumber}" + "&text=${
                        URLEncoder.encode(
                            msg, "UTF-8"
                        )
                    }"
                )
                val intent = Intent(Intent.ACTION_VIEW, whatsappURI)
                startActivity(intent)
                return
            }
        }
        shortToast(resources.getString(R.string.whatsapp_not_installed))
    }

    private fun makePhoneCall() {
        val callURI = Uri.parse(
            ConstantsVariables.calURIPrefix + ConstantsVariables.phoneIndex + customerDetailsViewModel.actualCustomer.value?.phoneNumber
        )
        val intent = Intent(Intent.ACTION_VIEW, callURI)
        startActivity(intent)
    }

    private fun isWhatsappInstalled(packageName: String): Boolean {
        return try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        }
    }

    private fun contractItemSelected(contract: Contract) {
        val contractItemBinding = ContractDetailsBinding.inflate(layoutInflater)
        val dialogTouchContract = BottomSheetDialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        val carDetailsListTitles = resources.getStringArray(R.array.car_details_title).toList()
        val priceDetailsListTitles = resources.getStringArray(R.array.price_details_title).toList()
        BottomDialogView(
            carDetailsListTitles,
            priceDetailsListTitles,
            resources.getString(R.string.provider_text),
        ).manageContractDetailViews(
            contractItemBinding, contract, this
        )

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract.window?.setLayout(width, height)
        dialogTouchContract.show()
    }

    private fun shortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}