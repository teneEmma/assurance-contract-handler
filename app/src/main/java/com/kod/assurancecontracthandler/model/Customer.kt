package com.kod.assurancecontracthandler.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    @ColumnInfo(name = "assure") var customerName: String?,
    @ColumnInfo(name = "telephone") var phoneNumber: String?
) : Parcelable
