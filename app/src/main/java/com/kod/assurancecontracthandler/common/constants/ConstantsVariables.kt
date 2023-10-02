package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {
    val months = listOf("JANVIER","FEVRIER","MARS","AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
    "SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE")
    val possibleBeginningSheetValues = listOf("SEMAINE", "MOIS DE")
    val possibleHeaderValues = listOf("ATTESTATION", "CARTE ROSE", "ASSURE", "N°")
    val carDetailsTitle = listOf("ATTESTATION", "N° POLICE", "COMPAGNIE", "MARK", "IMMATRICULATION", "PUISS / ENERGIE", "CARTE ROSE", "CATEGORIE", "ZONE")
    val pricesTitle = listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "COM PN",
        "COM ACC", "TOTAL COM", "NET A REVERSER", "ENCAIS", "C LIMBE", "C APPORT")
    const val isoDatePattern = "yyyy-MM-dd'T'HH:mm"
    const val desiredDatePattern = "dd/MM/yyyy"

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