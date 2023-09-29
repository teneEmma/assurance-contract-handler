package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class DatabaseViewModel(private val repository: ContractRepository) : BaseViewModel() {

    suspend fun addContracts(contracts: List<BaseContract>) = repository.addContracts(contracts)

    suspend fun addContract(contract: BaseContract) = repository.addContract(contract)

    private val _allContracts = MutableLiveData<List<BaseContract>?>()
    val allContracts: LiveData<List<BaseContract>?> = _allContracts

    fun setContracts(contracts: List<BaseContract>?) {
        contracts?.forEachIndexed { index, contractDbDto ->
            contractDbDto.id = index + 1
        }
        _allContracts.postValue(contracts)
    }

    suspend fun fetchAllContracts() = _allContracts.postValue(repository.readAllContracts())

    private val _listContracts = MutableLiveData<List<BaseContract>>()
    val listContracts: LiveData<List<BaseContract>>
        get() = _listContracts

    suspend fun fetchCustomerContracts(customerName: String) =
        _listContracts.postValue(repository.fetchCustomerContract(customerName))

    suspend fun updateCustomer(oldName: String, customerName: String, phoneNumber: String) =
        repository.updateCustomer(oldName = oldName, customerName, phoneNumber)

    suspend fun searchClient(str: String, id: Int) =
        _allContracts.postValue(filterList(repository.searchForClient(generateQuery(str, id))))

    private fun generateQuery(str: String, id: Int): SimpleSQLiteQuery {
        var initialQuery = "SELECT * FROM contract WHERE"
        initialQuery += when (id) {
            1 -> """ APPORTEUR LIKE "%$str%" """
            2 -> """ assure LIKE "%$str%" """
            3 -> """ attestation LIKE "%$str%" """
            4 -> """ carteRose LIKE "%$str%" """
            5 -> """ compagnie LIKE "%$str%" """
            6 -> """ immatriculation LIKE "%$str%" """
            7 -> """ mark LIKE "%$str%" """
            8 -> """ numeroPolice LIKE "%$str%""""
            else -> return SimpleSQLiteQuery("")
        }

        return SimpleSQLiteQuery(initialQuery)
    }

    private fun filterList(list: List<BaseContract>?): List<BaseContract>? {
        if (list == null) {
            return null
        }
        list.forEachIndexed { index, contractDbDto ->
            contractDbDto.id = index + 1
        }

        return list
    }

    fun fetchExpiringContractsIn(today: Long, expDate: Long): List<BaseContract>? {
        return repository.getExpiringContractsIn(today, expDate)
    }

    fun isContractsExpiring(today: Long, expDate: Long): Boolean {
        return repository.numberOfContractsExpiring(today, expDate) != 0
    }

}