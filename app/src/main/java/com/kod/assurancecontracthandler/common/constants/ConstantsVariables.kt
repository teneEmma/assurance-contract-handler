package com.kod.assurancecontracthandler.common.constants

object ConstantsVariables {
    const val dataBaseName = "CONTRACT_DATABASE"
    val headerNames = listOf(
        "N°", "ATTESTATION", "CARTE ROSE", "N°POLICE",
        "COMPAGNIE", "TEL", "ASSURE", "EFFET", "ECHEANCE",
        "PUISS / ENERGIE", "MARK", "IMMATRICULATION", "CATEGORIE", "ZONE",
        "DUREE (Jours)", "DTA", "PN", "ACC", "FC",
        "TVA", "CR", "PTTC", "COM PN", "COM ACC",
        "TOTAL COM", "NET A REVERSER", "ENCAIS", "COMM LIMBE", "COMM APPORT",
        "APPORTEUR"
    )
    val sizeOfRow = headerNames.size

    val contractIntTypes = listOf(0,2,7,8,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28)
}