package com.kod.assurancecontracthandler.Dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.kod.assurancecontracthandler.model.ContractDbDto

@Dao
sealed interface contractDAO{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContract(contract: ContractDbDto)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addContracts(contract: List<ContractDbDto>)

    @Update
    suspend fun updateContract(contract: ContractDbDto)

    @Query("SELECT * FROM contract ")
    suspend fun readDatabase(): LiveData<List<ContractDbDto>>
}