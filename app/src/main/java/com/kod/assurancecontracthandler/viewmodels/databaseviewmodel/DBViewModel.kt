package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.ContractDbDto
import com.kod.assurancecontracthandler.model.database.ContractDatabase
import com.kod.assurancecontracthandler.repository.ContractRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DBViewModel(application: Application): AndroidViewModel(application) {

    private val repository: ContractRepository
    init{
        val contractDAO = ContractDatabase.getDatabase(application.applicationContext).contractDao()
        repository = ContractRepository(contractDAO)
    }

    private val _hasQueried = MutableLiveData<Boolean>()
    val hasQueried: LiveData<Boolean>
    get() = _hasQueried

    fun executeFunWithAnimation(execute: suspend () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            _hasQueried.postValue(false)
            execute()
            _hasQueried.postValue(true)
        }
    }

    fun executeFunWithoutAnimation(execute: suspend () -> Unit){
        viewModelScope.launch(Dispatchers.IO) {
            execute()
        }
    }

    suspend fun addContracts(contracts: List<ContractDbDto>) = repository.addContracts(contracts)

    suspend fun addContract(contract: ContractDbDto) = repository.addContract(contract)

    private val _allContracts = MutableLiveData<List<ContractDbDto>?>()
    val allContracts: LiveData<List<ContractDbDto>?> = _allContracts

    fun setContracts(contracts: List<ContractDbDto>?) {
        contracts?.forEachIndexed { index, contractDbDto ->
            contractDbDto.id = index+1
        }
        _allContracts.postValue(contracts)
    }
    suspend fun fetchAllContracts() = _allContracts.postValue(repository.readAllContracts())

    private val _listContracts = MutableLiveData<List<ContractDbDto>>()
    val listContracts: LiveData<List<ContractDbDto>>
    get() = _listContracts
    suspend fun fetchCustomerContracts(customerName: String) =
        _listContracts.postValue(repository.fetchCustomerContract(customerName))

    suspend fun updateCustomer(oldName: String, customerName: String, phoneNumber: String) =
        repository.updateCustomer(oldName = oldName, customerName, phoneNumber)

    suspend fun searchClient(str: String, id: Int) = _allContracts.postValue(filterList(repository.searchForClient(generateQuery(str, id))))

    private fun generateQuery(str: String, id: Int): SimpleSQLiteQuery{
        var initialQuery = "SELECT * FROM contract WHERE"
        initialQuery += when(id){
            1-> """ APPORTEUR LIKE "%$str%" """
            2-> """ assure LIKE "%$str%" """
            3-> """ attestation LIKE "%$str%" """
            4-> """ carteRose LIKE "%$str%" """
            5-> """ compagnie LIKE "%$str%" """
            6-> """ immatriculation LIKE "%$str%" """
            7-> """ mark LIKE "%$str%" """
            8-> """ numeroPolice LIKE "%$str%""""
            else->return SimpleSQLiteQuery("")
        }

        return SimpleSQLiteQuery( initialQuery)
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

    fun fetchExpiringContractsIn(today: Long, expDate: Long): List<ContractDbDto>? {
        return repository.getExpiringContractsIn(today, expDate)
    }

    fun isContractsExpiring(today: Long, expDate: Long): Boolean {
        return repository.numberOfContractsExpiring(today, expDate) != 0
    }

}