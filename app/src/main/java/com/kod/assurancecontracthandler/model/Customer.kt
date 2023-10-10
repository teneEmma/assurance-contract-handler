package com.kod.assurancecontracthandler.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Ignore
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize

@Parcelize
data class Customer(
    @ColumnInfo(name = "assure") var customerName: String?,
    @ColumnInfo(name = "telephone") var phoneNumber: String?
) : Parcelable{
    // The contract number sent when trying to open a customer page to view the details of a contract
    @Ignore var actualContractNumber: Int? = null

    companion object : Parceler<Customer> {
        override fun Customer.write(parcel: Parcel, flags: Int) {
            parcel.writeString(customerName)
            parcel.writeString(phoneNumber)
            actualContractNumber?.let { parcel.writeInt(it) }
        }

        override fun create(parcel: Parcel): Customer {
            val name = parcel.readString()
            val phoneNumber = parcel.readString()
            val actualContractNumber = parcel.readInt()


            val customer = Customer(name, phoneNumber)
            customer.actualContractNumber = actualContractNumber
            return customer
        }
    }
}
