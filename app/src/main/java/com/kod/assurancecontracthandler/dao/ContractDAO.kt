package com.kod.assurancecontracthandler.dao

import androidx.room.*
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.BaseContract

@Dao
sealed interface ContractDAO{

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addContract(contract: BaseContract)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addContracts(contract: List<BaseContract>)

    @Update
    suspend fun updateContract(contract: BaseContract)

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
    @Query("SELECT * FROM contract WHERE id == :id")
    fun getContractWithId(id: Int): BaseContract?
}