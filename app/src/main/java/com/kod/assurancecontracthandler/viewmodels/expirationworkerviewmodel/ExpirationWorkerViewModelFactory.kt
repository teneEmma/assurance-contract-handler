package com.kod.assurancecontracthandler.viewmodels.selectfileviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository

class SelectFileViewModelFactory(private val contractRepository: ContractRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(SelectFileViewModel::class.java)) return SelectFileViewModel(contractRepository) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}