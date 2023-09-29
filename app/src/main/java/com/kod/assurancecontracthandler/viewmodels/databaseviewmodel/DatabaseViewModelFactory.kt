package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository

class DatabaseViewModelFactory(val repository: ContractRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DatabaseViewModel::class.java)) return DatabaseViewModel(repository) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}