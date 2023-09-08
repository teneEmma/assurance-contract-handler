package com.kod.assurancecontracthandler.common.utilities

import android.util.Log
import androidx.room.TypeConverter
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.collections.HashMap

class TimeConverters {
    @TypeConverter
    fun fromTimestampToDate(timeStamp: Long?): Date?{
        return timeStamp?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimeStamp(date: Date?): Long?{
        return date?.time
    }

    fun dateFromExcelHeader(str: String): HashMap<String, Date?>{
        val strParameter = str.uppercase()
        val delimiter = "-"
        val returnMap = hashMapOf<String, Date?> (Pair("START_DATE", null), Pair("END_DATE", null))

        if(strParameter.contains("SEMAINE DU")){
            if (!strParameter.contains("AU"))
                return returnMap
            val splitStr = strParameter.split("AU")
            val startDateString = splitStr[0].substringAfter("DU").trim()
            returnMap["START_DATE"] = dateFromFormattedString(startDateString, delimiter)
            if (splitStr.size != 2){
                return returnMap
            }
            val endDateString = splitStr[1]
            returnMap["END_DATE"] = dateFromFormattedString(endDateString, delimiter)
            return returnMap
        }else if (strParameter.contains("MOIS DE")){
            val splitStr = strParameter.split("MOIS DE")
            ConstantsVariables.months.forEachIndexed { index, month ->
                if (splitStr[1].substringBefore("20").trim() == month){
                    val year: Int? = splitStr[1].trim().split(" ").getOrNull(1)?.toIntOrNull()
                    var yearStr = "2022"
                    if (year != null )
                        yearStr = year.toString()
                    val endDateString = "30-${index+1}-${yearStr}"
                    val startDateString = "01-${index+1}-${yearStr}"
                    returnMap["START_DATE"] = dateFromFormattedString(startDateString, delimiter)
                    returnMap["END_DATE"] = dateFromFormattedString(endDateString, delimiter)
                }
            }
            return returnMap
        }
        else{
            return returnMap
        }
    }

    private fun dateFromFormattedString(str: String, delimiter: String): Date?{
        val splitStr = str.trim().split(delimiter).toMutableList()

        if (splitStr.size != 3){
            return null
        }
        if (splitStr[2].length != 4){
            splitStr[2] = "20" + splitStr[2]
        }
        val dateString: String = "${splitStr[0]}-${splitStr[1]}-${splitStr[2]}"
        val localDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).parse(dateString)
        return Date(localDate.time)
    }

}