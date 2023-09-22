package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {
    val months = listOf("JANVIER","FEVRIER","MARS","AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
    "SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE")

    enum class ExcelDateStringHeaderSuffix(val value: String){
        WeekString("SEMAINE DU"),
        MonthString("MOIS DE")
    }

    enum class ExcelDateStringHeaderDelimiter(val value: String){
        From("DU"),
        To("AU")
    }

    enum class StartEndDatesTexts(val value: String){
        Start("START_DATE"),
        End("END_DATE")
    }
}