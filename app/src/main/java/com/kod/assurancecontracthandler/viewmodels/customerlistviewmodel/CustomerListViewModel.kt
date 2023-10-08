package com.kod.assurancecontracthandler.viewmodels.customerlistviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.Customer
import com.kod.assurancecontracthandler.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class CustomerListViewModel(private val customerRepository: CustomerRepository) : ViewModel() {

    private val _customerList = MutableLiveData<List<Customer>>()
    private val _numbCustomers = MutableLiveData<Int>()
    private val _activeContracts = MutableLiveData<Int>()
    private val _numbCustomersWithPhones = MutableLiveData<Int>()
    private var _sortIconState = false
    private var _searchBarState = false
    private var _noResultFound = true

    val numbCustomersWithPhones: LiveData<Int>
        get() = _numbCustomersWithPhones
    val activeContracts: LiveData<Int>
        get() = _activeContracts
    val customerList: LiveData<List<Customer>>
        get() = _customerList
    val numbCustomers: LiveData<Int>
        get() = _numbCustomers
    val isSortIconSelected: Boolean
        get() = _sortIconState
    val searchViewState: Boolean
        get() = _searchBarState
    val noResultFound: Boolean
        get() = _noResultFound

    fun fetchCustomersWithName(customerName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val concatenatedName = "%$customerName%"
            val result = customerRepository.getAllCustomers(concatenatedName)
            if (result.isEmpty()) {
                _noResultFound = true
            }
            _noResultFound = false
            _customerList.postValue(result)
        }
    }

    fun fetchCustomers() {
        viewModelScope.launch(Dispatchers.IO) {
            _customerList.postValue(customerRepository.getAllCustomers())
        }
    }

    fun sortWithNumbers() {
        viewModelScope.launch(Dispatchers.IO) {
            val result = _customerList.value?.sortedByDescending {
                it.phoneNumber
            } ?: return@launch

            _customerList.postValue(result)
        }
    }

    fun validNumbCustomers() {
        viewModelScope.launch(Dispatchers.IO) {
            _numbCustomers.postValue(customerRepository.validNumberOfCustomers())
        }
    }

    fun validNumbCustomersWithPhones() {
        viewModelScope.launch(Dispatchers.IO) {
            _numbCustomersWithPhones.postValue(customerRepository.numberCustomersWithTelephone())
        }
    }

    fun getActiveContracts() {
        viewModelScope.launch(Dispatchers.IO) {
            val dateString = TimeConverters.formatLongToLocaleDate(Date().time)
            _activeContracts.postValue(dateString?.let { customerRepository.getActiveContracts(it) })
        }
    }

    fun toggleSortIconState() {
        _sortIconState = !_sortIconState
    }

    fun setSearchBarState(searchBarState: Boolean) {
        _searchBarState = searchBarState
    }
}
