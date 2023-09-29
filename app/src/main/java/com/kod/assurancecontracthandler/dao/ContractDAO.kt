package com.kod.assurancecontracthandler.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.BaseContract

@Dao
sealed interface ContractDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContract(contract: BaseContract)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContracts(contract: List<BaseContract>)

    @Update
    suspend fun updateContract(contract: BaseContract)

    @Query("UPDATE contract set assure = :customerName, telephone = :phoneNumber WHERE assure = :oldName")
    suspend fun updateContract(oldName: String, customerName: String, phoneNumber: String)

    @Query("SELECT * FROM contract WHERE assure = :customerName")
    suspend fun fetchCustomerContract(customerName: String): List<BaseContract>

    @Query("SELECT * FROM contract")
    fun readDatabase(): List<BaseContract>?

    @RawQuery
    fun searchForAClient(query: SimpleSQLiteQuery): List<BaseContract>

    @Query("SELECT * FROM contract WHERE echeance BETWEEN  :today AND :maxTime  ORDER BY echeance")
    fun getExpiringContractsIn(today: Long,  maxTime: Long): List<BaseContract>?

    @Query("SELECT  count(*) FROM contract WHERE echeance BETWEEN  :today AND :maxTime  ORDER BY echeance")
    fun numberOfContractsExpiring(today: Long,  maxTime: Long): Int
}