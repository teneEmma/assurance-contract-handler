package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import kotlinx.coroutines.launch

class DBViewModel(application: Application): AndroidViewModel(application) {

    private val repository: ContractRepository
    init{
        val database = ContractDatabase.getDatabase(application)
        val contractDAO = database.contractDao()
        repository = ContractRepository(contractDAO)
    }

    fun addContracts(contracts: List<ContractDbDto>){
        viewModelScope.launch {
            repository.addContracts(contracts)
        }
    }
}