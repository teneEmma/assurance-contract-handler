package com.kod.assurancecontracthandler.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "contract")
data class ContractDbDto(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val contract: Contract?,
    val date: Long
)
