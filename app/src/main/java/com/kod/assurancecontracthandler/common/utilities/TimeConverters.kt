package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import androidx.room.TypeConverter
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date

class TimeConverters {
    @TypeConverter
    fun fromTimestampToDate(timeStamp: Long?): Date?{
        return timeStamp?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long?{
        return date?.time
    }

    //SEMAINE DU 01-01-22 AU 04-01-22
    //PRODUCTION ORION-LIMBE MOIS DE JANVIER 2022
    fun dateFromExcelHeader(str: String): HashMap<String, Date?>{
        val hashMap = hashMapOf<String, Date?> ()
        if(str.contains("SEMAINE DU")){
            if (!str.contains("AU")){
                return hashMap
            }
            val splitStr = str.split("AU")
            val startDateString = splitStr[0].substringAfter("DU").trim()
            hashMap["startDate"] = dateFromFormattedString(startDateString, "-")
            if (splitStr.size != 2){
                return hashMap
            }
            val endDateString = splitStr[1]
            hashMap["endDate"] = dateFromFormattedString(endDateString, "-")
            return hashMap
        }else if (str.contains("MOIS DE")){
            val splitStr = str.split("MOIS DE")
            ConstantsVariables.months.forEachIndexed { index, month ->
                if (splitStr[1].substringBefore('2').trim() == month){
                    val endDateString = "30-${index}-${splitStr[1].substringAfter(month)}"
                    val startDateString = "01-${index}-${splitStr[1].substringAfter(month)}"
                    hashMap["startDate"] = dateFromFormattedString(startDateString, "-")
                    hashMap["endDate"] = dateFromFormattedString(endDateString, "-")
                }
            }
            return hashMap
        }
        else{
            return hashMap
        }
    }

    //SEMAINE DU 01-01-22 AU 04-01-22
    fun dateFromFormattedString(str: String, delimiter: String): Date?{
        val splitStr = str.trim().split(delimiter).toMutableList()
        val dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        var dateString = ""
        if (splitStr.size != 3){
            return null
        }
        if (splitStr[2].length != 4){
            splitStr[2] = "20" + splitStr[2]
        }
        dateString = "${splitStr[0]}-${splitStr[1]}-${splitStr[2]}"
        val localDate = LocalDate.parse(dateString, dateFormatter)
        return Date(localDate.year-1900, localDate.monthValue-1, localDate.dayOfMonth)
    }

}