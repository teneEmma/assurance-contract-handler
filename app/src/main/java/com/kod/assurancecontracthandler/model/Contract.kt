package com.kod.assurancecontracthandler.model

import java.util.Date

data class Contract(
    var index: Int,
    var attestation: String? ="",
    var carteRose: String? = "0",
    var numeroPolice: String? = "",
    var compagnie: String? = "",
    var telephone: Long? = null,
    var assure: String? = "",
    var effet: Date? = null,
    var echeance: Date?= null,
    var puissanceVehicule: String? = "",
    var mark: String? = "",
    var immatriculation: String? = "",
    var categorie: Int? = 1,
    var zone: String? = "C",
    var duree: Int? = null,
    var DTA: Int? = null,
    var PN: Int? = null,
    var ACC: Int? = null,
    var FC: Int? = null,
    var TVA: Int? = null,
    var CR: Int? = null,
    var PTTC: Int? = null,
    var COM_PN: Int? = null,
    var COM_ACC: Int? = null,
    var TOTAL_COM: Int? = null,
    var NET_A_REVERSER: Int? = null,
    var ENCAIS: Int? = null,
    var COMM_LIMBE: Int? = null,
    var COMM_APPORT: Int? = null,
    var APPORTEUR: String? = null
)