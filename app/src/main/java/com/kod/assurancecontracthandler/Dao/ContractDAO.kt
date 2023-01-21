package com.kod.assurancecontracthandler.Dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
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

    @Query("SELECT * FROM contract WHERE echeance<:maxTime AND echeance>:today ORDER BY echeance")
    fun getExpiringContractsIn(today: Long,  maxTime: Long): List<ContractDbDto>
}