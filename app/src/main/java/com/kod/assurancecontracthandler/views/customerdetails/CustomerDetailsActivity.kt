package com.kod.assurancecontracthandler.views.customerdetails

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.usecases.ContactAction
import com.kod.assurancecontracthandler.common.utilities.BottomDialogView
import com.kod.assurancecontracthandler.databinding.ActivityCustomerDetailsBinding
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.databinding.EditCustomerBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.common.utilities.DataStoreRepository
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.GridViewItemAdapter
import java.net.URLEncoder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailsBinding
    private var customer =  MutableLiveData<Customer?>()
    private lateinit var viewModel: DBViewModel
    private lateinit var dataStore: DataStoreRepository
    private val sharedPreferences: SharedPreferences by lazy {
        getSharedPreferences(ConstantsVariables.sharedPreferenceMsg, Context.MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[DBViewModel::class.java]

        dataStore = DataStoreRepository(sharedPreferences)
        viewModel.listContracts.observe(this){listContracts->
            val recyclerView = CustomerContractsAdapter(listContracts, itemClicked = {contract ->
                itemLongClick(contract)
            }, activeStateTouched = {
                if(it)
                    toast(resources.getString(R.string.contract_state_active))
                else
                    toast(resources.getString(R.string.contract_state_inactive))
            })
            binding.recyclerActiveContracts.adapter = recyclerView
            binding.recyclerActiveContracts.layoutManager = LinearLayoutManager(applicationContext)
            binding.recyclerActiveContracts.setHasFixedSize(true)
        }

        customer.observe(this){customer->
            customer?.customerName?.let { getCustomerContracts(it) }
            setViews()
        }

        binding.ivModify.setOnClickListener {
            showModificationDialog()
        }

        customer.value = retrieveCustomer()
    }

    private fun toast(message: String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    private fun showModificationDialog(){
        val editCustomerBinding = EditCustomerBinding.inflate(layoutInflater)
        val editCustomerDialog = Dialog(this).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(editCustomerBinding.root)
        }

        customer.value?.apply {
            editCustomerBinding.tvAssureName.text = customerName
            editCustomerBinding.ilCustomerName.hint = customerName
            editCustomerBinding.ilPhoneNumber.hint = phoneNumber
        }

        var customerName = ""
        var number = ""
        customer.value?.customerName?.let { customerName = it }
        customer.value?.phoneNumber?.let { number = it }

        editCustomerBinding.etUsername.doOnTextChanged { text, _, _, _ ->
            if(isCustomerNameValid(text.toString())) {
                customerName = text.toString()
                editCustomerBinding.btnModify.isEnabled = true
            }
            else {
                editCustomerBinding.ilCustomerName.helperText =
                    resources.getString(R.string.invalid_customer_name)
                editCustomerBinding.btnModify.isEnabled = false
            }
        }

        editCustomerBinding.etPhoneNumber.doOnTextChanged { text, _, _, _ ->
            if(text.toString().contains(".*\\D.*".toRegex())) {
                editCustomerBinding.ilPhoneNumber.helperText =
                    resources.getString(R.string.invalid_phone_number)
                editCustomerBinding.btnModify.isEnabled = false
            }
            else {
                number = text.toString()
                editCustomerBinding.btnModify.isEnabled = true
            }
        }

        editCustomerBinding.btnCancel.setOnClickListener {
            editCustomerDialog.dismiss()
        }

        editCustomerBinding.btnModify.setOnClickListener {
            viewModel.executeFunWithoutAnimation {
                customer.value?.customerName?.let { name->
                    viewModel.updateCustomer(oldName = name,
                        customerName = customerName, phoneNumber = number)
                }
                customer.postValue(Customer(customerName, number))
            }

            editCustomerDialog.dismiss()

            toast(resources.getString(R.string.modified_success))
        }

        val width = (resources.displayMetrics.widthPixels * 0.95).toInt()

        editCustomerDialog.window?.setLayout(width, WindowManager.LayoutParams.WRAP_CONTENT)
        editCustomerDialog.show()
    }

    private fun isCustomerNameValid(name: String?): Boolean = name.isNullOrEmpty().not()

    private fun itemLongClick(contract: Contract) {
        val contractItemBinding = ContractDeetailsBinding.inflate(layoutInflater)
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


    private fun setViews(){
        binding.tvCustomerName.text = customer.value?.customerName
        binding.ivCustomerProfile.text = setNameInitials()
        if(customer.value?.phoneNumber.isNullOrEmpty() || customer.value?.phoneNumber == "null"){
            binding.tvContactCustomerText.text = resources.getString(R.string.no_contact_text)
            binding.tvContactCustomerText.textAlignment = View.TEXT_ALIGNMENT_CENTER
            binding.tvContactCustomerText.setTextColor(Color.RED)
        }else{
            customer.value?.phoneNumber?.let { val recyclerView = CustomerContactAdapter(listOf(it)){action, phoneNumber ->
                when(action){
                    ContactAction.CALL.action -> makePhoneCall()
                    ContactAction.SMS.action -> showMessageLayout {msg-> sendSMS(msg) }
                    ContactAction.WHATSAPP_TEXT.action -> showMessageLayout{msg-> sendWhatsappMsg(msg) }
                }
            }
                binding.recyclerContact.adapter = recyclerView
                binding.recyclerContact.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerContact.setHasFixedSize(true)
            }
        }
    }
    private fun isWhatsappInstalled(packageName: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            else
                packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        }catch (e: PackageManager.NameNotFoundException){
            e.printStackTrace()
            false
        }
    }

    private fun showMessageLayout(function: (String) -> Unit){
        var message = dataStore.readPredefinedMessage()
        if (message.isNullOrEmpty()) message = resources.getString(R.string.predefined_message)

        if(customer.value?.echeance == null || customer.value?.echeance == 0L && customer.value?.mark.isNullOrEmpty()){
            message = ""
            binding.btnSendMessage.isEnabled = false
        } else {
            customer.value?.mark?.let {
                message = message!!.replace(ConstantsVariables.predefinedMsgVehicleId, it)
            }

            val date = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(customer.value?.echeance!!))
            message = message!!.replace(ConstantsVariables.predefinedMsgDateId, date)
            customer.value?.immatriculation?.let {
                message = message!!.replace(ConstantsVariables.predefinedImmatricualtionId, it)
            }
            binding.etMessageToSend.setText(message)
            binding.btnSendMessage.isEnabled = true
        }

        binding.apply {
            llEnterMessage.visibility = View.VISIBLE
            etMessageToSend.doOnTextChanged { text, _, _, _ ->
                if(text?.length!! > 0) binding.btnSendMessage.isEnabled = true
            }
            btnCancelMessage.setOnClickListener {
                etMessageToSend.text?.clear()
                llEnterMessage.visibility = View.GONE
            }
            btnSendMessage.setOnClickListener{
                function.invoke(etMessageToSend.text.toString())
                etMessageToSend.text?.clear()
            }
        }
    }

    private fun sendWhatsappMsg(msg: String) {
        for (packageName in ConstantsVariables.whatsappPackages){
            if (isWhatsappInstalled(packageName)) {
                val whatsappURI = Uri.parse(
                    "whatsapp://send?phone=" +
                            "${ConstantsVariables.phoneIndex}${customer.value?.phoneNumber}" +
                            "&text=${URLEncoder.encode(msg, "UTF-8")}"
                )
                val intent = Intent(Intent.ACTION_VIEW, whatsappURI)
                startActivity(intent)
                return
            }
        }
        toast(resources.getString(R.string.whatsapp_not_installed))
    }

    private fun sendSMS(msg: String) {
        val smsURI = Uri.parse(ConstantsVariables.smsURIPrefix+customer.value?.phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW, smsURI)
        intent.putExtra(Intent.EXTRA_TEXT, msg)
        startActivity(intent)
    }

    private fun makePhoneCall() {
        val callURI = Uri.parse(ConstantsVariables.calURIPrefix+ConstantsVariables.phoneIndex+
                customer.value?.phoneNumber)
        val intent = Intent(Intent.ACTION_VIEW, callURI)
        startActivity(intent)
    }

    private fun setNameInitials():String {
        val initialsList = try {
            if(customer.value?.customerName?.isNotEmpty() == true){
                customer.value?.customerName?.split(" ")?.onEach { it.first() }
            }else{
                listOf("???")
            }
        }catch (e: java.lang.Exception){
            e.printStackTrace()
            listOf("???")
        }

        var initials = ""
        if (initialsList != null) {
            for(name in initialsList){
                initials += name.first()
            }
            if(initials.length > 3)
                initials = initials.substring(0,2)
        }

        return initials
    }

    private fun retrieveCustomer(): Customer?{
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            intent.getParcelableExtra(ConstantsVariables.customerKey, Customer::class.java)
        else
            intent.getParcelableExtra(ConstantsVariables.customerKey) as? Customer
    }

    private fun getCustomerContracts(customerName: String){
        viewModel.apply {
            executeFunWithoutAnimation {
                fetchCustomerContracts(customerName)
            }
        }
    }
}