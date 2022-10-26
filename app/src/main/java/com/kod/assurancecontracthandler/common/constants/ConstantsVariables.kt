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
    val contractIntTypes = listOf(0,2,12,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28)
    val contractDateTypes = listOf(7,8)
    val months = listOf("JANVIER","FEVRIER","MARS","AVRIL", "MAI", "JUIN", "JUILLET", "AOUT",
    "SEPTEMBRE","OCTOBRE","NOVEMBRE","DECEMBRE")
    val searchChipNames = listOf("Assure", "N° Police", "Attestaion", "Compagnie",
        "Immatriculation", "Mark", "Apporteur")

    val filterChipNames = listOf("Apporteur", "Immatriculation", "Attestation", "Carte Rose",
        "Compagnie", "Assure", "Mark", "N° Police")


    enum class ChipIds(val index: Int, val nameStr: String){
        ATTESTATION(1, headerNames[1]),
        CARTEROSE(2, headerNames[2]),
        N_POLICE(3, headerNames[3]),
        COMPAGNIE(4, headerNames[4]),
        TELEPHONE(5, headerNames[5]),
        ASSURE(6, headerNames[6]),
        EFFET(7, headerNames[7]),
        ECHEANCE(8, headerNames[8]),
        PUISSANCE(9, headerNames[9]),
        MARK(10, headerNames[10]),
        IMMATRICULATION(11, headerNames[11]),
        CATEGORIE(12, headerNames[12]),
        ZONE(13, headerNames[13]),
        DUREE(14, headerNames[14]),
        APPORTEUR(30, headerNames[30]),
    }
}