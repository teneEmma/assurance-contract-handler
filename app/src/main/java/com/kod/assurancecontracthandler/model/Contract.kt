package com.kod.assurancecontracthandler.model

import java.util.Date

data class Contract(
    val index: Int,
    val attestation: String? ="",
    val carteRose: String? = "0",
    val numeroPolice: String? = "",
    val compagnie: String? = "",
    val telephone: String? = null,
    val assure: String? = "",
    val effet: Date? = null,
    val echeance: Date?= null,
    val puissanceVehicule: String? = "",
    val mark: String? = "",
    val immatriculation: String? = "",
    val categorie: Int? = 1,
    val zone: String? = "C",
    val duree: Int? = null,
    val DTA: Int? = null,
    val PN: Int? = null,
    val ACC: Int? = null,
    val FC: Int? = null,
    val TVA: Int? = null,
    val CR: Int? = null,
    val PTTC: Int? = null,
    val COM_PN: Int? = null,
    val COM_ACC: Int? = null,
    val TOTAL_COM: Int? = null,
    val NET_A_REVERSER: Int? = null,
    val ENCAIS: Int? = null,
    val COMM_LIMBE: Int? = null,
    val COMM_APPORT: Int? = null,
    val APPORTEUR: String? = null
)