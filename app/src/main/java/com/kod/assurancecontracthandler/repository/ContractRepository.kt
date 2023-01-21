package com.kod.assurancecontracthandler.repository

import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.Dao.ContractDAO
import com.kod.assurancecontracthandler.model.ContractDbDto

class ContractRepository(private val contractDao: ContractDAO) {

    suspend fun addContract(contract: ContractDbDto) = contractDao.addContract(contract)

    suspend fun addContracts(contracts: List<ContractDbDto>) = contractDao.addContracts(contracts)

    suspend fun updateContract(contract: ContractDbDto) = contractDao.updateContract(contract)

    suspend fun fetchCustomerContract(customerName: String): List<ContractDbDto> = contractDao.fetchCustomerContract(customerName)

    suspend fun updateCustomer(oldName: String, customerName: String, phoneNumber: String) =
        contractDao.updateContract(oldName = oldName,
            customerName = customerName,
            phoneNumber = phoneNumber)

    fun readAllContracts() = contractDao.readDatabase()

    fun searchForClient(query: SimpleSQLiteQuery) = contractDao.searchForAClient(query)

    fun getExpiringContractsIn(today: Long, maxTime: Long): List<ContractDbDto> = contractDao.getExpiringContractsIn(today,  maxTime)
}