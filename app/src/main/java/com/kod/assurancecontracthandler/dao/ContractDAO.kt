package com.kod.assurancecontracthandler.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.ContractDbDto

@Dao
sealed interface ContractDAO{

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addContract(contract: ContractDbDto)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun addContracts(contract: List<ContractDbDto>)

    @Update
    suspend fun updateContract(contract: ContractDbDto)

    @Query("UPDATE contract set assure = :customerName, telephone = :phoneNumber WHERE assure = :oldName")
    suspend fun updateContract(oldName: String, customerName: String, phoneNumber: String)

    @Query("SELECT * FROM contract WHERE assure = :customerName")
    suspend fun fetchCustomerContract(customerName: String): List<ContractDbDto>

    @Query("SELECT * FROM contract")
    fun readDatabase(): List<ContractDbDto>?

    @RawQuery
    fun searchForAClient(query: SimpleSQLiteQuery): List<ContractDbDto>

    @Query("SELECT * FROM contract WHERE echeance BETWEEN  :today AND :maxTime  ORDER BY echeance")
    fun getExpiringContractsIn(today: Long,  maxTime: Long): List<ContractDbDto>?

    @Query("SELECT  count(*) FROM contract WHERE echeance BETWEEN  :today AND :maxTime  ORDER BY echeance")
    fun numberOfContractsExpiring(today: Long,  maxTime: Long): Int
}