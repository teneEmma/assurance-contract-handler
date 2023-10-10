package com.kod.assurancecontracthandler.views.customerdetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.style.UnderlineSpan
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.usecases.ContactAction
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.databinding.ActivityCustomerDetailsBinding
import com.kod.assurancecontracthandler.databinding.ContractDetailsBinding
import com.kod.assurancecontracthandler.databinding.EditCustomerDialogBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.repository.CustomerRepository
import com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel.CustomerDetailsViewModel
import com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel.CustomerDetailsViewModelFactory
import java.net.URLEncoder


class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailsBinding
    private lateinit var dataStore: DataStoreRepository
    private val customerDetailsViewModel by viewModels<CustomerDetailsViewModel> {
        val customerDao = ContractDatabase.getDatabase(this).customerDao()
        val contractDao = ContractDatabase.getDatabase(this).contractDao()
        val customerRepository = CustomerRepository(customerDao)
        val contractRepository = ContractRepository(contractDao)
        CustomerDetailsViewModelFactory(customerRepository, contractRepository)
    }
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(ConstantsVariables.sharedPreferenceMsg, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        dataStore = DataStoreRepository(sharedPreferences)

        customerDetailsViewModel.customerContracts.observe(this) { contracts ->
            val contractRecyclerAdapter = CustomerContractsAdapter(
                listContracts = contracts,
                colorForChosenContract = R.color.highlighted_actual_contract,
                actualContract = customerDetailsViewModel.relatedContract?.contract,
                itemClicked = { contract ->
                    contractItemSelected(contract)
                }, copyBtnTouched = { contract ->
                    val message = formatMessageToSend(
                        vehicleMark = contract?.mark ?: "",
                        plateNumber = contract?.immatriculation ?: "",
                        dueDate = contract?.echeance ?: "",
                        isActive = contract?.isContractActive() == true
                    )
                    customerDetailsViewModel.setMessageToSend(message)
                    binding.etMessageToSend.setText(customerDetailsViewModel.messageToSend)
                }, activeStateTouched = { isActive ->
                    if (isActive) shortToast(resources.getString(R.string.active_contract_text))
                    else shortToast(resources.getString(R.string.contract_state_inactive))
                })
            binding.recyclerActiveContracts.adapter = contractRecyclerAdapter
            binding.recyclerActiveContracts.layoutManager = LinearLayoutManager(applicationContext)
            binding.recyclerActiveContracts.setHasFixedSize(true)
        }

        customerDetailsViewModel.actualCustomer.observe(this) {
            customerDetailsViewModel.fetchCustomerContracts()
            setViews()
        }

        binding.ivModify.setOnClickListener {
            showModificationDialog()
        }

//        customerDetailsViewModel.shouldShowRelatedContractBtn.observe(this) { isBeenShown ->
//            if (isBeenShown == false) {
//                binding.ivCurrentContractInfo.visibility = View.GONE
//                return@observe
//            }
//            binding.ivCurrentContractInfo.visibility = View.VISIBLE
//        }
//        binding.ivCurrentContractInfo.setOnClickListener {
//            customerDetailsViewModel.relatedContract?.contract?.let { it1 -> contractItemSelected(it1) }
//        }

        customerDetailsViewModel.getActualCustomerDetails(intent)
    }

    private fun showModificationDialog() {
        val editCustomerBinding = EditCustomerDialogBinding.inflate(layoutInflater)
        val editCustomerDialog = Dialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(editCustomerBinding.root)
        }

        val customer = customerDetailsViewModel.actualCustomer.value
        customer?.apply {
            editCustomerBinding.tvAssureName.text = customerName
            editCustomerBinding.ilCustomerName.hint = customerName
            editCustomerBinding.ilPhoneNumber.hint = phoneNumber
        }

        editCustomerBinding.etUsername.doOnTextChanged { text, _, _, _ ->
            val stringResourceId = customerDetailsViewModel.onCustomerNameChanging(text.toString())
            editCustomerBinding.btnModify.isEnabled = customerDetailsViewModel.btnModifyState == true
            if (stringResourceId != null) {
                shortToast(resources.getString(stringResourceId))
            }
        }

        editCustomerBinding.etPhoneNumber.doOnTextChanged { text, _, _, _ ->
            val stringResourceId = customerDetailsViewModel.onCustomerPhoneNumberChanging(text.toString())
            editCustomerBinding.btnModify.isEnabled = customerDetailsViewModel.btnModifyState == true
            if (stringResourceId != null) {
                shortToast(resources.getString(stringResourceId))
            }
        }

        editCustomerBinding.btnCancel.setOnClickListener {
            editCustomerDialog.dismiss()
            customerDetailsViewModel.onDismissModifyCustomerDetailsDialog()
        }

        customerDetailsViewModel.messageResourceId.observe(this) {
            if (it != null) {
                shortToast(resources.getString(it))
            }
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

    private fun setViews() {
        // Underlining text.
        val customer = customerDetailsViewModel.actualCustomer.value
        val content = SpannableString(customer?.customerName)
        content.setSpan(UnderlineSpan(), 0, content.length, 0)
        binding.tvCustomerName.text = content
        binding.ivCustomerProfile.text = DataTypesConversionAndFormattingUtils.setNameInitials(customer?.customerName)
        if (customer?.phoneNumber.isNullOrEmpty() || customer?.phoneNumber == null) {
            binding.tvContactCustomerText.text = resources.getString(R.string.no_contact_text)
            binding.tvContactCustomerText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.tvContactCustomerText.setTextColor(Color.RED)
        } else {
            customer.phoneNumber?.let {
                val recyclerView = CustomerContactAdapter(listOf(it)) { action, phoneNumber ->
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
            val message = formatMessageToSend(
                vehicleMark = contract?.mark ?: "",
                plateNumber = contract?.immatriculation ?: "",
                dueDate = contract?.echeance ?: "",
                isActive = contract?.isContractActive() == true
            )
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

    private fun formatMessageToSend(
        vehicleMark: String,
        dueDate: String,
        plateNumber: String,
        isActive: Boolean
    ): String {
        var message = dataStore.readPredefinedMessage()
        if (message.isNullOrEmpty()) {
            message = resources.getString(R.string.predefined_message)
        }
        if (!isActive) {
            message = message.replace(ConstantsVariables.futureTenseExpire, ConstantsVariables.pastTenseExpire)
        }
        return message.replace(ConstantsVariables.predefinedMsgVehicleId, vehicleMark)
            .replace(ConstantsVariables.predefinedMsgDateId, dueDate)
            .replace(ConstantsVariables.predefinedImmatricualtionId, plateNumber)
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
                            msg,
                            "UTF-8"
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

        BottomDialogView().manageContractDetailViews(contractItemBinding, contract, this)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract.window?.setLayout(width, height)
        dialogTouchContract.show()
    }

    private fun shortToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}