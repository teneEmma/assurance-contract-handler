package com.kod.assurancecontracthandler.repository

import com.kod.assurancecontracthandler.Dao.ContractDAO
import com.kod.assurancecontracthandler.model.ContractDbDto

class ContractRepository(private val contractDao: ContractDAO) {

    suspend fun addContract(contract: ContractDbDto) = contractDao.addContract(contract)

    suspend fun addContracts(contracts: List<ContractDbDto>) = contractDao.addContracts(contracts)

    suspend fun updateContract(contract: ContractDbDto) = contractDao.updateContract(contract)

    fun readAllContracts() = contractDao.readDatabase()

}