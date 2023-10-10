package com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel

import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.repository.CustomerRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class CustomerDetailsViewModel(
    private val customerRepository: CustomerRepository,
    private val contractRepository: ContractRepository
) : BaseViewModel() {
    private val _customerContracts = MutableLiveData<List<BaseContract>>()
    private val _actualCustomer = MutableLiveData<Customer>()
    private var _relatedContract: BaseContract? = null
    private var _relatedContractId = -1
    private var _newCustomerName: String? = null
    private var _newCustomerPhoneNumber: String? = null
    private var _btnModifyState: Boolean? = false
    private var _messageToSend: String? = null
    private var _shouldShowRelatedContractBtn: MutableLiveData<Boolean> = MutableLiveData(false)

    val customerContracts: LiveData<List<BaseContract>>
        get() = _customerContracts
    val actualCustomer: LiveData<Customer>
        get() = _actualCustomer
    val relatedContract: BaseContract?
        get() = _relatedContract
    val btnModifyState: Boolean?
        get() = _btnModifyState
    val messageToSend: String?
        get() = _messageToSend
    val shouldShowRelatedContractBtn: LiveData<Boolean>
        get() = _shouldShowRelatedContractBtn
    val messageResourceId: LiveData<Int?>
        get() = Companion._messageResourceId

    fun getActualCustomerDetails(intent: Intent) {
        val customerName = intent.getStringExtra(ConstantsVariables.customerNameKey)
        _relatedContractId = intent.getIntExtra(ConstantsVariables.relatedContractIdKey, -1)
        executeFunctionWithoutAnimation { getContractRelatedToCustomer() }
        Log.e("RECEIVING...", "-> $customerName | $_relatedContractId")
        if (customerName.isNullOrEmpty()) {
            _messageResourceId.postValue(R.string.user_exist_not)
            return
        }
        executeFunctionWithoutAnimation {
            val customer = getActualContract(customerName)
            _newCustomerName = customer?.customerName
            _newCustomerPhoneNumber = customer?.phoneNumber
        }
    }

    private fun getActualContract(customerName: String): Customer? {
        val concatenatedString = "%$customerName%"
        val customer = customerRepository.getCustomerWithName(concatenatedString)
        if (customer == null) {
            _messageResourceId.postValue(R.string.user_exist_not)
            return null
        }
        _actualCustomer.postValue(customer!!)
        return customer
    }


    fun fetchCustomerContracts() {
        executeFunctionWithoutAnimation {
            _actualCustomer.value?.customerName?.let {
                _customerContracts.postValue(
                    contractRepository.fetchCustomerContract(
                        it
                    )
                )
            }
        }
    }

    private fun getContractRelatedToCustomer() {
        if (_relatedContractId == -1) {
            _relatedContract = null
            _shouldShowRelatedContractBtn.postValue(false)
            return
        }
        executeFunctionWithoutAnimation {
            val result = contractRepository.getContractWithId(_relatedContractId)
            if(result == null){
                _shouldShowRelatedContractBtn.postValue(false)
                return@executeFunctionWithoutAnimation
            }
            _shouldShowRelatedContractBtn.postValue(true)
            _relatedContract = result
        }
    }

    fun onCustomerNameChanging(newText: String): Int? {
        if (newText.isNotEmpty()) {
            _newCustomerName = newText
            _btnModifyState = true
            return null
        }

        _newCustomerName = null
        _btnModifyState = false
        return R.string.invalid_customer_name
    }

    fun onCustomerPhoneNumberChanging(newPhoneNumber: String): Int? {
        if (newPhoneNumber.contains(".*\\D.*".toRegex())) {
            _btnModifyState = false
            return R.string.invalid_phone_number
        }
        _newCustomerPhoneNumber = newPhoneNumber
        _btnModifyState = true
        return null
    }

    fun onDismissModifyCustomerDetailsDialog() {
        _btnModifyState = null
        _newCustomerName = null
        _newCustomerPhoneNumber = null
        _messageToSend = null
    }

    fun updateCustomerDetails() {
        executeFunctionWithoutAnimation {
            val oldName = _actualCustomer.value?.customerName
            Log.e("NAMES_XXXXXXX", "old: $oldName new: $_newCustomerName")
            Log.e("NAMES_XXXXXXX", "old: ${_actualCustomer.value?.phoneNumber} new: $_newCustomerPhoneNumber")
            if (_newCustomerName == null || oldName == null) {
                return@executeFunctionWithoutAnimation
            }
            customerRepository.updateCustomer(
                oldName = oldName,
                customerName = _newCustomerName!!.uppercase(),
                phoneNumber = _newCustomerPhoneNumber
            )
            _actualCustomer.postValue(Customer(_newCustomerName, _newCustomerPhoneNumber))
        }
    }

    fun setMessageToSend(message: String?) {
        _messageToSend = message
    }
}