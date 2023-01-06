package com.kod.assurancecontracthandler.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.util.*

@Entity(tableName = "contract")
@Parcelize
data class ContractDbDto(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val sheetCreationDateStart: Date = Date(),
    val sheetCreationDateEnd: Date = Date(),
    @Embedded
    val contract: Contract?
): Parcelable