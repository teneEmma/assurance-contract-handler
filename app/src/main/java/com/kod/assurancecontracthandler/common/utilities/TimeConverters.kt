package com.kod.assurancecontracthandler.common.utilities

import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import java.text.SimpleDateFormat
import java.util.*

// TODO: Document this function.
object TimeConverters {
    fun dateFromExcelHeader(str: String): HashMap<String, String?> {
        val startDateStr = ConstantsVariables.StartEndDatesTexts.Start.value
        val endDateStr = ConstantsVariables.StartEndDatesTexts.End.value
        val strParameter = str.uppercase()
        val returnMap = hashMapOf<String, String?>(Pair(startDateStr, null), Pair(endDateStr, null))

        if (strParameter.contains(ConstantsVariables.ExcelDateStringHeaderSuffix.WeekString.value)) {
            if (!strParameter.contains(ConstantsVariables.ExcelDateStringHeaderDelimiter.To.value)) {
                return returnMap
            }
            val splitStr = strParameter.split(ConstantsVariables.ExcelDateStringHeaderDelimiter.To.value)
            val startDateString = splitStr[0].substringAfter(
                ConstantsVariables.ExcelDateStringHeaderDelimiter.From.value
            ).trim()
            returnMap[startDateStr] = dateFromFormattedString(startDateString)
            if (splitStr.size != 2) {
                return returnMap
            }
            val endDateString = splitStr[1]
            returnMap[endDateStr] = dateFromFormattedString(endDateString)
            return returnMap
        } else if (strParameter.contains(ConstantsVariables.ExcelDateStringHeaderSuffix.MonthString.value)) {
            val splitStr = strParameter.split(ConstantsVariables.ExcelDateStringHeaderSuffix.MonthString.value)
            ConstantsVariables.months.forEachIndexed { index, month ->
                if (splitStr[1].substringBefore("20").trim() == month) {
                    val year: Int? = splitStr[1].trim().split(" ").getOrNull(1)?.toIntOrNull()
                    var yearStr = "2022"
                    if (year != null) {
                        yearStr = year.toString()
                    }
                    val endDateString = "30-${index + 1}-${yearStr}"
                    val startDateString = "01-${index + 1}-${yearStr}"
                    returnMap[startDateStr] = dateFromFormattedString(startDateString)
                    returnMap[endDateStr] = dateFromFormattedString(endDateString)
                }
            }
            return returnMap
        } else {
            return returnMap
        }
    }

    private fun dateFromFormattedString(str: String): String? {
        val dateRegex = Regex(
            "^(?:(?:31(\\/|-|\\.)(?:0?[13578]|1[02]|(?:Jan|Mar|May|Jul|Aug|Oct|Dec)))\\1|" +
                    "(?:(?:29|30)(\\/|-|\\.)(?:0?[1,3-9]|1[0-2]|(?:Jan|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))\\2))" +
                    "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$|^(?:29(\\/|-|\\.)(?:0?2|(?:Feb))\\3(?:(?:(?:1[6-9]|[2-9]\\d)" +
                    "?(?:0[48]|[2468][048]|[13579][26])|(?:(?:16|[2468][048]|[3579][26])00))))\$|^(?:0?[1-9]|1\\d|2[0-8])" +
                    "(\\/|-|\\.)(?:(?:0?[1-9]|(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep))|(?:1[0-2]|(?:Oct|Nov|Dec)))\\4" +
                    "(?:(?:1[6-9]|[2-9]\\d)?\\d{2})\$"
        )
        if (!dateRegex.matches(str.trim())) {
            return null
        }
        return str
    }

    fun formatISODateToLocaleDate(dateToFormat: String): String? {
        val isoFormat = SimpleDateFormat(ConstantsVariables.isoDatePattern, ConstantsVariables.appLocal)
        val isoDate = isoFormat.parse(dateToFormat)
        val formattedDate =
            SimpleDateFormat(ConstantsVariables.desiredDatePattern, ConstantsVariables.appLocal)

        return isoDate?.let { formattedDate.format(it) }
    }

    fun formatLongToLocaleDate(
        dateInLong: Long?,
        desiredDatePattern: String = ConstantsVariables.desiredDatePattern
    ): String? {
        if (dateInLong == null) {
            return null
        }
        val date = Date(dateInLong)
        val formattedDate =
            SimpleDateFormat(desiredDatePattern, ConstantsVariables.appLocal)
        return formattedDate.format(date)
    }

    fun formatLocaleDateTimeForFileName(dateInLong: Long?): String? {
        if (dateInLong == null) {
            return null
        }
        val date = Date(dateInLong)
        val formattedDate =
            SimpleDateFormat(ConstantsVariables.desiredDateTimePattern, ConstantsVariables.appLocal)
        return formattedDate.format(date)
    }
}