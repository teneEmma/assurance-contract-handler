package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {
    val months = listOf(
        "JANVIER", "FEVRIER", "MARS", "AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
        "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DECEMBRE"
    )
    val possibleBeginningSheetValues = listOf("SEMAINE", "MOIS DE")
    val possibleHeaderValues = listOf("ATTESTATION", "CARTE ROSE", "ASSURE", "N°")
    val carDetailsTitle = listOf(
        "ATTESTATION",
        "N° POLICE",
        "COMPAGNIE",
        "MARK",
        "IMMATRICULATION",
        "PUISS / ENERGIE",
        "CARTE ROSE",
        "CATEGORIE",
        "ZONE"
    )
    val pricesTitle = listOf(
        "DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "COM PN",
        "COM ACC", "TOTAL COM", "NET A REVERSER", "ENCAIS", "C LIMBE", "C APPORT"
    )
    val searchBarChipsTitles = listOf(
        "APPORTEUR", "assure", "attestation", "compagnie", "telephone",
        "immatriculation", "mark", "numeroPolice"
    ).sorted()
    val filterDialogChips: List<String> = mutableListOf(
        "Apporteur", "Assure", "Attestation", "Carte rose",
        "Compagnie", "Immatriculation", "Mark", "Numero police"
    ).sorted()
    const val isoDatePattern = "yyyy-MM-dd'T'HH:mm"
    const val desiredDatePattern = "dd/MM/yyyy"
    const val datePickerTag = "DATE_RANGE_PICKER"
    const val priceUnit = "XAF"
    const val powerUnit = "CV-ESS"
    const val minPriceText = "1000 $priceUnit"
    const val maxPriceText = "500000 $priceUnit"
    const val minPriceValue = 1000.0F
    const val maxPriceValue = 500000.0F
    const val minPowerText = "1.0 $powerUnit"
    const val maxPowerText = "30.0 $powerUnit"
    const val minPowerValue = 1.0F
    const val maxPowerValue = 30.0F

    enum class ExcelDateStringHeaderSuffix(val value: String) {
        WeekString("SEMAINE DU"),
        MonthString("MOIS DE")
    }

    enum class ExcelDateStringHeaderDelimiter(val value: String) {
        From("DU"),
        To("AU")
    }

    enum class StartEndDatesTexts(val value: String) {
        Start("START_DATE"),
        End("END_DATE")
    }
}