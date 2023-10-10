package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class DatabaseViewModel(private val repository: ContractRepository) : BaseViewModel() {
    suspend fun addContracts(contracts: List<BaseContract>) = repository.addContracts(contracts)

    suspend fun addContract(contract: BaseContract) = repository.addContract(contract)

    fun setContracts(contracts: List<BaseContract>?) {
        contracts?.forEachIndexed { index, contractDbDto ->
            contractDbDto.id = index + 1
        }
        _allContracts.postValue(contracts)
    }

    private val _listContracts = MutableLiveData<List<BaseContract>>()
    val listContracts: LiveData<List<BaseContract>>
        get() = _listContracts

    suspend fun fetchCustomerContracts(customerName: String) =
        _listContracts.postValue(repository.fetchCustomerContract(customerName))

    fun fetchExpiringContractsIn(today: Long, expDate: Long): List<BaseContract>? {
        return repository.getExpiringContractsIn(today, expDate)
    }

    fun isContractsExpiring(today: Long, expDate: Long): Boolean {
        return repository.numberOfContractsExpiring(today, expDate) != 0
    }

}