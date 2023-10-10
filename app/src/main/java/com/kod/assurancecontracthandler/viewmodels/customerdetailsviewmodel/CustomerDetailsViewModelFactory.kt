package com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.CustomerRepository

class CustomerDetailsViewModelFactory(private val customerRepository: CustomerRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerListViewModel::class.java)) return CustomerListViewModel(
            customerRepository
        ) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}