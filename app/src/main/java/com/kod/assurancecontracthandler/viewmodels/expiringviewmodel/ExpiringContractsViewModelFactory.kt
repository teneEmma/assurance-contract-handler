package com.kod.assurancecontracthandler.viewmodels.expiringviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository

class ExpiringContractsViewModelFactory(private val contractRepository: ContractRepository) :
    ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpiringContractsViewModel::class.java)) return ExpiringContractsViewModel(
            contractRepository
        ) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}