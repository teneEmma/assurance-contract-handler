package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DBViewModel(application: Application): AndroidViewModel(application) {

    private val repository: ContractRepository
    init{
        val contractDAO = ContractDatabase.getDatabase(application.applicationContext).contractDao()
        repository = ContractRepository(contractDAO)
    }

    fun addContracts(contracts: List<ContractDbDto>){
        viewModelScope.launch {
            repository.addContracts(contracts)
        }
    }

    private var _allContracts = MutableLiveData<List<ContractDbDto>>()
    var allContracts: LiveData<List<ContractDbDto>> = _allContracts

    fun fetchAllContracts() {
        viewModelScope.launch(Dispatchers.IO) {
            allContracts = repository.readAllContracts()
            Log.e("allContracts", repository.readAllContracts().value.toString())
        }
    }
}