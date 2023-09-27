package com.kod.assurancecontracthandler.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.dao.ContractDAO
import com.kod.assurancecontracthandler.model.BaseContract

class ContractRepository(private val contractDao: ContractDAO) {

    suspend fun addContract(contract: BaseContract) = contractDao.addContract(contract)

    suspend fun addContracts(contracts: List<BaseContract>) = contractDao.addContracts(contracts)

    suspend fun updateContract(contract: BaseContract) = contractDao.updateContract(contract)

    suspend fun fetchCustomerContract(customerName: String): List<BaseContract> = contractDao.fetchCustomerContract(customerName)

    suspend fun updateCustomer(oldName: String, customerName: String, phoneNumber: String) =
        contractDao.updateContract(oldName = oldName,
            customerName = customerName,
            phoneNumber = phoneNumber)

    fun readAllContracts() = contractDao.readDatabase()

    fun searchForClient(query: SimpleSQLiteQuery) = contractDao.searchForAClient(query)

    fun getExpiringContractsIn(today: Long, maxTime: Long): List<BaseContract>? = contractDao.getExpiringContractsIn(today,  maxTime)
    fun numberOfContractsExpiring(today: Long, maxTime: Long): Int = contractDao.numberOfContractsExpiring(today,  maxTime)
}