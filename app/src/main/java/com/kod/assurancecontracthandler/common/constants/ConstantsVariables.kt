package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {

    val customerKey = "customer"
    val pricesTitle = listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "COM PN",
        "COM ACC", "TOTAL COM", "NET A REVERSER", "ENCAIS", "C LIMBE", "C APPORT")
    val carDetailsTitle = listOf("MARK", "IMMATRICULATION", "PUISS / ENERGIE", "CARTE ROSE", "CATEGORIE", "ZONE")
    val datePickerTag = "DATE_RANGE_PICKER"
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
    //Sorted == [Apporteur, Assure, Attestation, Carte Rose, Compagnie, Immatriculation, Mark, N° Police]
    val allChips: MutableList<String> = mutableListOf("Apporteur", "Assure", "Attestation", "Carte Rose",
        "Compagnie", "Immatriculation", "Mark", "N° Police")
    val expandableListsTitles = listOf("FILTRER LES PRIX", "FILTRER LA PUISSANCE")
    val expandableChildListTitles = listOf(listOf("DTA", "PN", "ACC", "FC", "TVA", "CR", "PTTC", "ENCAIS", "REVER", "SOLDE"),
        listOf("PUISSANCE"))
    val filterChipNames = listOf("Apporteur", "Immatriculation", "Attestation", "Carte Rose",
        "Compagnie", "Assure", "Mark", "N° Police")
}