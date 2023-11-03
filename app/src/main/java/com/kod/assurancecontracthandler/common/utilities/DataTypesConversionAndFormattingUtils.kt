package com.kod.assurancecontracthandler.common.utilities

import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import java.text.NumberFormat
import java.util.*

object DataTypesConversionAndFormattingUtils {

    fun notStringifyingNull(value: Any?): String? {
        return value?.toString()
    }

    fun convertPowerFieldStringToFloat(powerFieldString: String?): Float? {
        if (powerFieldString == null) {
            return null
        }
        return try {
            powerFieldString.toFloat()
        } catch (_: Exception) {
            if (!powerFieldString.contains("CV")) {
                return null
            }
            powerFieldString.split("CV").first().toFloat()
        }
    }

    fun setNameInitials(customerName: String?): String {
        if (customerName == null) {
            return "??"
        }
        val initialsList = try {
            if (customerName.isNotEmpty()) {
                customerName.split(" ").onEach { it.first() }
            } else {
                listOf("???")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            listOf("???")
        }

        var initials = ""
        for (name in initialsList) {
            initials += name.first().uppercase()
        }
        if (initials.length > 3)
            initials = initials.substring(0, 2).uppercase()

        return initials
    }

    fun formatCurrencyPrices(price: Int): String {
        val value2: NumberFormat = NumberFormat.getCurrencyInstance(ConstantsVariables.appLocal).apply {
            maximumFractionDigits = 0
            currency = Currency.getInstance("XAF")
        }
        return value2.format(price)
    }

    fun formatIntegerPrice(price: Int): String {
        val value2: NumberFormat = NumberFormat.getIntegerInstance(ConstantsVariables.appLocal).apply {
            maximumFractionDigits = 0
            currency = Currency.getInstance("XAF")
        }
        return value2.format(price)
    }

    fun addPowerOrPriceUnitToAttribute(price: Int, groupPosition: Int): String {
        return when (groupPosition) {
            0 -> formatCurrencyPrices(price)
            1 -> "${price}${ConstantsVariables.powerUnit}"
            else -> "$price"
        }
    }

    fun removePowerToString(power: String?): String? {
        return power?.split(ConstantsVariables.powerUnit)?.first()
    }

    fun formatPhoneNumberForExporting(phoneNumber: String?): String?{
        if(phoneNumber == null){
            return null
        }
        if(phoneNumber.length != 9){
            return null
        }

        return phoneNumber.substring(0, 3) + "-" + phoneNumber.substring(3, 5) + "-" +
                phoneNumber.substring(5, 7) + "-" + phoneNumber.substring(7, 9)
    }
    fun concatenateStringForDBQuery(stringValue: String): String = "%$stringValue%"
}