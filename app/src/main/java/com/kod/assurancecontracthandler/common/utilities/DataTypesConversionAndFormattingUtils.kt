package com.kod.assurancecontracthandler.common.utilities

import com.kod.assurancecontracthandler.common.constants.ConstantsVariables

object DataTypesConversionAndFormattingUtils {

    fun convertPowerFieldStringToFloat(powerFieldString: String?): Float? {
        if(powerFieldString == null){
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
        if(customerName == null){
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
            initials += name.first()
        }
        if (initials.length > 3)
            initials = initials.substring(0, 2)

        return initials
    }

    fun addPowerOrPriceUnitToAttribute(value: String, groupPosition: Int): String{
        return if(groupPosition == 0){
            "${value}${ConstantsVariables.priceUnit}"
        }else{
            "${value}${ConstantsVariables.powerUnit}"
        }
    }
}