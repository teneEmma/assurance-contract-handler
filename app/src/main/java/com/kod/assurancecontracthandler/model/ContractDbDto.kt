package com.kod.assurancecontracthandler.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.LocalDate

/**
 * Model representing a 'contract' table in our database.
 *
 *
 * @param id Represents the primary key of each entry in our DB.
 * @param sheetCreationDateStart This is the first year that appears in the header of our Excel sheet.
 *  * e.g. sheetCreationDateStart in [2021-2022] is 2021
 * @param sheetCreationDateEnd This is the second year that appears in the header of our Excel sheet.
 *  * e.g. sheetCreationDateEnd in [2021-2022] is 2022
 *  @param contract This is the contract that is going to be saved in the DB.
 */
@Entity(tableName = "contract")
@Parcelize
data class ContractDbDto(
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    val sheetCreationDateStart: String = LocalDate.now().toString(),
    val sheetCreationDateEnd: String = LocalDate.now().toString(),
    @Embedded
    val contract: Contract?
): Parcelable