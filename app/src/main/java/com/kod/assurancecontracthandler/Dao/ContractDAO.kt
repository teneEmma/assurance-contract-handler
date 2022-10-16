package com.kod.assurancecontracthandler.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.model.ContractDbDto
import kotlinx.coroutines.flow.Flow

@Dao
sealed interface ContractDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContract(contract: ContractDbDto)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContracts(contract: List<ContractDbDto>)

    @Update
    suspend fun updateContract(contract: ContractDbDto)

    @Query("SELECT * FROM contract ORDER BY id ASC")
    fun readDatabase(): List<ContractDbDto>?

    @RawQuery
    fun searchForAClient(query: SimpleSQLiteQuery): List<ContractDbDto>
}