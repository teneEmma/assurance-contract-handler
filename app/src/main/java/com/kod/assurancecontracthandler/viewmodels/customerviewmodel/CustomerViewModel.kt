package com.kod.assurancecontracthandler.viewmodels.customerviewmodel

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class CustomerViewModel(application: Application): ViewModel() {
    private val  repository: CustomerRepository
    init {
        repository = CustomerRepository(ContractDatabase.getDatabase(application).customerDao())
    }

    private val _customerList = MutableLiveData<List<Customer>>()
    val customerList: LiveData<List<Customer>>
        get() = _customerList

    fun fetchCustomers(){
        viewModelScope.launch(Dispatchers.IO) {
            _customerList.postValue(repository.getAllCustomers())
        }
    }

    fun fetchCustomers(name: String){
        viewModelScope.launch(Dispatchers.IO) {
            _customerList.postValue(repository.getAllCustomers("%$name%"))
        }
    }

    private val _numbCustomers = MutableLiveData<Int>()
    val numbCustomers: LiveData<Int>
        get() = _numbCustomers
    fun validNumbCustomers(){
        viewModelScope.launch(Dispatchers.IO) {
            _numbCustomers.postValue(repository.validNumberOfCustomers())
        }
    }

    suspend fun sortWithNumbers(){
        viewModelScope.launch(Dispatchers.IO) {
            _customerList.postValue(_customerList.value?.sortedByDescending {
                it.phoneNumber
            })
        }
    }

    suspend fun getAllPhoneNumbers(name: String): List<Long?>{
        return withContext(Dispatchers.IO) {
            repository.getCustomerPhoneNumbers(name)
        }
    }

    private val _numbCustomersWithPhones = MutableLiveData<Int>()
    val numbCustomersWithPhones: LiveData<Int>
        get() = _numbCustomersWithPhones
    fun validNumbCustomersWithPhones(){
        viewModelScope.launch(Dispatchers.IO) {
            _numbCustomersWithPhones.postValue(repository.numberCustomersWithTelephone())
        }
    }

    private val _activeContracts = MutableLiveData<Int>()
    val activeContracts: LiveData<Int>
        get() = _activeContracts
    fun getActiveContracts(){
        viewModelScope.launch(Dispatchers.IO){
            _activeContracts.postValue(repository.getActiveContracts(Date().time))
        }
    }
}