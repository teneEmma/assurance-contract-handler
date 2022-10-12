package com.kod.assurancecontracthandler.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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

    @Query("SELECT * FROM contract WHERE assure LIKE :searchKey " +
            "OR attestation LIKE :searchKey " +
            "OR mark LIKE :searchKey " +
            "OR immatriculation LIKE :searchKey " +
            "OR APPORTEUR LIKE :searchKey")
    fun searchForAClient(searchKey: String): List<ContractDbDto>
}