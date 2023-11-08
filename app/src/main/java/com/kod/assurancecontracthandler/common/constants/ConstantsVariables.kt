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
        "immatriculation", "chassis", "mark", "numeroPolice"
    ).sorted()
    val filterDialogChips: List<String> = mutableListOf(
        "Apporteur", "Assure", "Attestation", "Carte rose", "Categorie",
        "Chassis", "Compagnie", "Immatriculation", "Mark", "Numero police"
    ).sorted()
    const val isoDatePattern = "yyyy-MM-dd'T'HH:mm"
    const val desiredDatePattern = "yyyy-MM-dd"
    const val desiredDateTimePattern = "yyyy-MM-dd_HH_mm_ss"
    const val datePickerTag = "DATE_RANGE_PICKER"
    const val priceUnit = "XAF"
    const val powerUnit = "CV"
    const val timeUnit = "Jours"
    const val customerNameKey = "customer_name"
    const val relatedContractIdKey = "related_contract_id_key"
    const val smsURIPrefix = "smsto:"
    const val calURIPrefix = "tel:"
    const val phoneIndex = "+237"
    const val identifierPrefix = "Mr/Mrs"
    const val predefinedMsgAssurerId = "ASSURER_ID"
    const val predefinedMsgPinkCardId = "PINK_CARD_ID"
    const val predefinedMsgCategoryId = "CATEGORY_ID"
    const val predefinedMsgAttestationId = "REGISTRATION_ID"
    const val predefinedMsgVehicleId = "VEHICLE_ID"
    const val predefinedMsgEndDateId = "END_DATE_ID"
    const val predefinedMsgStartDateId = "START_DATE_ID"
    const val predefinedImmatricualtionId = "PLATE_NUMBER_ID"
    const val predefinedChassisId = "CHASSIS_ID"
    const val EXPIRY_CHANNEL_ID_STRING = "ASSURANCE_CONTRACT_CHANNEL_ID"
    const val FIRST_USAGE_CHANNEL_ID_STRING = "FIRST_USAGE_CHANNEL_ID"
    const val WELCOME_WORK_NAME = "SHOWING_WELCOME_MESSAGE"
    const val CHECKING_EXPIRY_CONTRACTS_WORK_NAME = "CHECKING_EXPIRING_CONTRACTS"
    const val EXPORT_SHEET_NAME = "CONTRATS"
    const val expiryChannelID_int = 1234
    const val firstTimeChannelID_int = 4321
    const val futureTenseExpire = "will expire"
    const val pastTenseExpire = "expired"
    const val FOLDER_DIR = "/ORION ASSURANCE"

    const val minPriceValue = 0
    const val maxPriceValue = 10000000
    const val minPowerValue = 0
    const val maxPowerValue = 100
    const val minTimeValue = 0
    const val maxTimeValue = 365

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