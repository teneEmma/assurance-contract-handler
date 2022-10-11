package com.kod.assurancecontracthandler.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "contract")
data class ContractDbDto(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @Embedded
    val contract: Contract?,
    val date: Date?
)
