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
    @Ignore var carteRose: String? = null
    @Ignore var immatriculation: String? = null
    @Ignore var numeroPolice: String? = null
    @Ignore var mark: String? = null
    @Ignore var effet: Long? = null
    @Ignore var echeance: Long?= null

    companion object : Parceler<Customer> {
        override fun Customer.write(parcel: Parcel, flags: Int) {
            parcel.writeString(customerName)
            parcel.writeString(phoneNumber)
            parcel.writeString(carteRose)
            parcel.writeString(immatriculation)
            parcel.writeString(numeroPolice)
            parcel.writeString(mark)
            effet?.let { parcel.writeLong(it) }
            echeance?.let { parcel.writeLong(it) }
        }

        override fun create(parcel: Parcel): Customer {
            val name = parcel.readString()
            val phoneNumber = parcel.readString()
            val carteRose = parcel.readString()
            val attestation = parcel.readString()
            val numeroPolice = parcel.readString()
            val mark = parcel.readString()
            val echeance = parcel.readLong()
            val effet = parcel.readLong()


            val customer = Customer(name, phoneNumber)
            customer.carteRose = carteRose
            customer.immatriculation = attestation
            customer.numeroPolice = numeroPolice
            customer.mark = mark
            customer.effet = effet
            customer.echeance = echeance
            return customer
        }

        override fun newArray(size: Int): Array<Customer> {
            val custo = Customer("TESTE NAME", phoneNumber = "133456")
            return arrayOf(custo)
        }
    }
}
