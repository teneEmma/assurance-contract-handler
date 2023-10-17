package com.kod.assurancecontracthandler.viewmodels.selectfileviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository

class ExpirationWorkerViewModelFactory(private val contractRepository: ContractRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpirationWorkerViewModel::class.java)) return ExpirationWorkerViewModel(
            contractRepository
        ) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}