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
    const val minPriceText = "1000.0 $priceUnit"
    const val maxPriceText = "500000.0 $priceUnit"
    const val minPriceValue = 1000.0F
    const val maxPriceValue = 500000.0F
    const val minPowerText = "1.0 $powerUnit"
    const val maxPowerText = "30.0 $powerUnit"
    const val minPowerValue = 1.0F
    const val maxPowerValue = 30.0F
    const val customerNameKey = "customer_name"
    const val relatedContractIdKey = "related_contract_id_key"
    const val smsURIPrefix = "smsto:"
    const val calURIPrefix = "tel:"
    const val phoneIndex = "+237"
    const val predefinedMsgVehicleId = "VEHICLE_ID"
    const val predefinedMsgDateId = "DATE_ID"
    const val predefinedImmatricualtionId = "PLATE_NUMBER_ID"
    const val sharedPreferenceMsg = "ASSURANCE_PREDEFINED_MSG"
    const val futureTenseExpire = "will expire"
    const val pastTenseExpire = "expired"

    val whatsappPackages = listOf("com.whatsapp", "com.gbwhatsapp", "com.whatsapp.w4b")

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