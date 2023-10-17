package com.kod.assurancecontracthandler.common.constants

import java.util.*

object ConstantsVariables {
    val appLocal = Locale("fr", "CM")
    val months = listOf(
        "JANVIER", "FEVRIER", "MARS", "AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
        "SEPTEMBRE", "OCTOBRE", "NOVEMBRE", "DECEMBRE"
    )
    val possibleHeaderValues = listOf("ATTESTATION", "CARTE ROSE", "ASSURE", "N°")
    val searchBarChipsTitles = listOf(
        "APPORTEUR", "assure", "attestation", "compagnie", "telephone",
        "immatriculation", "mark", "numeroPolice"
    ).sorted()
    val filterDialogChips: List<String> = mutableListOf(
        "Apporteur", "Assure", "Attestation", "Carte rose", "Categorie",
        "Compagnie", "Immatriculation", "Mark", "Numero police"
    ).sorted()
    const val isoDatePattern = "yyyy-MM-dd'T'HH:mm"
    const val desiredDatePattern = "yyyy-MM-dd"
    const val datePickerTag = "DATE_RANGE_PICKER"
    const val priceUnit = "XAF"
    const val powerUnit = "CV-ESS"
    const val timeUnit = "Jours"
    const val customerNameKey = "customer_name"
    const val relatedContractIdKey = "related_contract_id_key"
    const val smsURIPrefix = "smsto:"
    const val calURIPrefix = "tel:"
    const val phoneIndex = "+237"
    const val predefinedMsgVehicleId = "VEHICLE_ID"
    const val predefinedMsgDateId = "DATE_ID"
    const val predefinedImmatricualtionId = "PLATE_NUMBER_ID"
    const val sharedPreferenceMsg = "ASSURANCE_PREDEFINED_MSG"
    const val APP_USAGE_STATE = "APP_USAGE_STATE"
    const val EXPIRY_CHANNEL_ID_STRING = "ASSURANCE_CONTRACT_CHANNEL_ID"
    const val FIRST_USAGE_CHANNEL_ID_STRING = "FIRST_USAGE_CHANNEL_ID"
    const val WELCOME_WORK_NAME = "SHOWING_WELCOME_MESSAGE"
    const val CHECKING_EXPIRY_CONTRACTS_WORK_NAME = "CHECKING_EXPIRING_CONTRACTS"
    const val expiryChannelID_int = 1234
    const val firstTimeChannelID_int = 4321
    const val futureTenseExpire = "will expire"
    const val pastTenseExpire = "expired"

    const val minPriceValue = 0
    const val maxPriceValue = 10000000
    const val minPowerValue = 0
    const val maxPowerValue = 100
    const val minTimeValue = 0
    const val maxTimeValue = 365
    const val maxMonthForExpiringContracts = 1L

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