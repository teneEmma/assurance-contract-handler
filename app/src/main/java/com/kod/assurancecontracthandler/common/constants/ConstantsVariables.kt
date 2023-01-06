package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {

    val priceUnit = "XAF"
    val powerUnit = "CV-ESS"
    val customerKey = "customer"
    val pricesTitle = listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "COM PN",
        "COM ACC", "TOTAL COM", "NET A REVERSER", "ENCAIS", "C LIMBE", "C APPORT")
    val carDetailsTitle = listOf("MARK", "IMMATRICULATION", "PUISS / ENERGIE", "CARTE ROSE", "CATEGORIE", "ZONE")
    const val datePickerTag = "DATE_RANGE_PICKER"
    val possibleBeginningSheetValues = listOf("SEMAINE", "MOIS DE")
    val possibleHeaderValues = listOf("ATTESTATION", "CARTE ROSE", "ASSURE")
    val headerNames = listOf(
        "N°", "ATTESTATION", "CARTE ROSE", "N°POLICE",
        "COMPAGNIE", "TEL", "ASSURE", "EFFET", "ECHEANCE",
        "PUISS / ENERGIE", "MARK", "IMMATRICULATION", "CATEGORIE", "ZONE",
        "DUREE (Jours)", "DTA", "PN", "ACC", "FC",
        "TVA", "CR", "PTTC", "COM PN", "COM ACC",
        "TOTAL COM", "NET A REVERSER", "ENCAIS", "COMM LIMBE", "COMM APPORT",
        "APPORTEUR"
    )
    val months = listOf("JANVIER","FEVRIER","MARS","AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
    "SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE")

    val allChips: MutableList<String> = mutableListOf("Apporteur", "Assure", "Attestation", "Carte Rose",
        "Compagnie", "Immatriculation", "Mark", "N° Police")
    val expandableListsTitles = listOf("FILTRER LES PRIX", "FILTRER LA PUISSANCE")
    val expandableChildListTitles = listOf(listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "ENCAIS", "REVER", "SOLDE"),
        listOf("PUISSANCE"))
    const val textType = "text/plain"
    const val whatsappPackage = "com.whatsapp"
    const val whatsappURIPrefix = "https://api.whatsapp.com/send?phone="
    const val smsURIPrefix = "smsto:"
    const val calURIPrefix = "tel:"
    const val phoneIndex = "+237"
    const val sharedPreferenceMsg = "ASSURANCE_PREDEFINED_MSG"

    const val expiryChannelID_str = "ASSURANCE_CONTRACT_CHANNEL_ID"
    const val expiryChannelID_int = 1234
    const val EXPIRY_NOTIFICATION_TITLE = "Expiration De Contrat"
    const val CHECKING_EXPIRY_CONTRACTS_WORKNAME = "CHECKING_EXPIRY_CONTRACTS"
    const val INTENT_LIST_WORKER = "ListOfExpiryContracts"
    const val INTENT_WORKER_EFFET = "ListOfExpiryContracts"
    const val INTENT_WORKER_ECHEANCE = "ListOfExpiryContracts"
    const val INTENT_WORKER_ASSURE = "ListOfExpiryContracts"
    const val INTENT_WORKER_ATTESTION = "ListOfExpiryContracts"
    const val INTENT_WORKER_ = "ListOfExpiryContracts"

}