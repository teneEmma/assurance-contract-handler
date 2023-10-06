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

    fun addPowerOrPriceUnitToAttribute(value: String, groupPosition: Int): String{
        return if(groupPosition == 0){
            "${value}${ConstantsVariables.priceUnit}"
        }else{
            "${value}${ConstantsVariables.powerUnit}"
        }
    }
}