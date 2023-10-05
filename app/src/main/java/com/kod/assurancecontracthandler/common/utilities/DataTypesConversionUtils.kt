package com.kod.assurancecontracthandler.common.utilities

object DataTypesConversionUtils {

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
}