package com.kod.assurancecontracthandler.viewmodels.customerdetailsviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.repository.CustomerRepository

class CustomerDetailsViewModelFactory(
    private val customerRepository: CustomerRepository,
    private val contractRepository: ContractRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerDetailsViewModel::class.java)) return CustomerDetailsViewModel(
            customerRepository,
            contractRepository
        ) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}