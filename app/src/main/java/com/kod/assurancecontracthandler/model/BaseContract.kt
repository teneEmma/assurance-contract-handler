package com.kod.assurancecontracthandler.model

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Model representing a 'contract' table in our database.
 *
 *
 * @param id Represents the primary key of each entry in our DB.
 *  @param contract This is the contract that is going to be saved in the DB.
 */
@Entity(tableName = "contract")
@Parcelize
data class BaseContract(
    @PrimaryKey
    var id: Int,
    @Embedded
    val contract: Contract?
) : Parcelable {
}