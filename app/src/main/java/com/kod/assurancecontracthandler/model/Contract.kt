package com.kod.assurancecontracthandler.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Contract(
    var attestation: String? = null,
    var carteRose: String? = null,
    var numeroPolice: String? = null,
    var compagnie: String? = null,
    var telephone: Long? = null,
    var assure: String? = null,
    var effet: String? = null,
    var echeance: String?= null,
    var puissanceVehicule: String? = null,
    var mark: String? = null,
    var immatriculation: String? = null,
    var categorie: Int? = null,
    var zone: String? = null,
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
): Parcelable