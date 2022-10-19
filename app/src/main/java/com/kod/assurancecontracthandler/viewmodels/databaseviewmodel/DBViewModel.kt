package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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

    private val _allContracts = MutableLiveData<List<ContractDbDto>?>()
    val allContracts: LiveData<List<ContractDbDto>?> = _allContracts

    fun fetchAllContracts() {
        viewModelScope.launch(Dispatchers.IO) {
            _allContracts.postValue(repository.readAllContracts())
        }
    }

    fun searchClient(str: String, id: Int){
        viewModelScope.launch(Dispatchers.IO) {
            _allContracts.postValue(filterList(repository.searchForClient(generateQuery(str, id))))
        }
    }

    private fun generateQuery(str: String, id: Int): SimpleSQLiteQuery{
        var query = "SELECT * FROM contract WHERE"
        when(id){
            1-> query += """ assure LIKE "%$str%" """
            2-> query += """ numeroPolice LIKE "%$str%" """
            3-> query += """ attestation LIKE "%$str%" """
            4-> query += """ compagnie LIKE "%$str%" """
            5-> query += """ immatriculation LIKE "%$str%" """
            6-> query += """ mark LIKE "%$str%" """
            7-> query += """ APPORTEUR LIKE "%$str%""""
            else->return SimpleSQLiteQuery("")
        }

        return SimpleSQLiteQuery( query)
    }

    private fun filterList(list: List<ContractDbDto>?): List<ContractDbDto>?{
        if(list == null){
            return null
        }
        list.forEachIndexed { index, contractDbDto ->
            contractDbDto.id = index+1
        }

        return list
    }
}