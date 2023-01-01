package com.kod.assurancecontracthandler.views.customerdetails

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
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
import com.kod.assurancecontracthandler.databinding.ActivityCustomerDetailsBinding
import com.kod.assurancecontracthandler.databinding.ContractDeetailsBinding
import com.kod.assurancecontracthandler.databinding.EditCustomerBinding
import com.kod.assurancecontracthandler.model.Contract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.viewmodels.databaseviewmodel.DBViewModel
import com.kod.assurancecontracthandler.views.fragments.home.contractlist.GridViewItemAdapter
import java.text.SimpleDateFormat
import java.util.*

class CustomerDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomerDetailsBinding
    private var customer =  MutableLiveData<Customer?>()
    private lateinit var viewModel: DBViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomerDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[DBViewModel::class.java]

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
        val dialogTouchContract = BottomSheetDialog(this, R.style.AppBottomSheetDialogTheme).apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setContentView(contractItemBinding.root)
        }

        manageContractDetailViews(contractItemBinding, contract)

        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.80).toInt()

        dialogTouchContract.window?.setLayout(width, height)
        dialogTouchContract.show()
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
                    ContactAction.CALL.action -> toast("calling $phoneNumber")
                    ContactAction.SMS.action -> toast("sending an SMS to $phoneNumber")
                    ContactAction.WHATSAPP_TEXT.action -> toast("opening whatsapp for $phoneNumber")
                }

            }
                binding.recyclerContact.adapter = recyclerView
                binding.recyclerContact.layoutManager = LinearLayoutManager(applicationContext)
                binding.recyclerContact.setHasFixedSize(true)
            }
        }

    }

    private fun setNameInitials():String {
        val initialsList = if(customer.value?.customerName?.isNotEmpty() == true){
            customer.value?.customerName?.split(" ")?.onEach { it.first() }
        }else{
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
        return if (Build.VERSION.SDK_INT >= 33)
            intent.getParcelableExtra("customer", Customer::class.java)
        else
            intent.getParcelableExtra("customer") as? Customer
    }

    private fun getCustomerContracts(customerName: String){
        viewModel.apply {
            executeFunWithoutAnimation {
                fetchCustomerContracts(customerName)
            }
        }
    }
}