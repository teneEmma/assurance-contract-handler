package com.kod.assurancecontracthandler.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.dao.ContractDAO
import com.kod.assurancecontracthandler.model.BaseContract

class ContractRepository(private val contractDao: ContractDAO) {

    suspend fun addContract(contract: BaseContract) = contractDao.addContract(contract)

    suspend fun addContracts(contracts: List<BaseContract>) = contractDao.addContracts(contracts)

    suspend fun updateContract(contract: BaseContract) = contractDao.updateContract(contract)

    suspend fun fetchCustomerContract(customerName: String): List<BaseContract> =
        contractDao.fetchCustomerContract(customerName)

    suspend fun getContractWithId(id: Int): BaseContract? = contractDao.getContractWithId(id)

    fun readAllContracts() = contractDao.readDatabase()

    fun searchForClient(query: SimpleSQLiteQuery): List<BaseContract> {
        return contractDao.searchForAClient(query)
    }

    fun filterContracts(query: SimpleSQLiteQuery) = contractDao.filterContracts(query)

    fun getExpiringContractsForGivenDate(today: String, maxTime: String): List<BaseContract>? =
        contractDao.getExpiringContractsForGivenDate(today, maxTime)

    fun numberOfContractsExpiring(today: String, maxTime: String): Int =
        contractDao.numberOfContractsExpiring(today, maxTime)

    fun fetchExpiringContractForCustomerWithName(
        concatenatedName: String,
        todayString: String?,
        maxDateString: String?
    ): List<BaseContract>? = contractDao.fetchExpiringContractForCustomerWithName(concatenatedName, todayString, maxDateString)
}