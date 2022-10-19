package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import androidx.lifecycle.ViewModel

class FilterViewModel: ViewModel() {
    var searchChip: Int? = 1
    var filterChip: List<Int>? = null
    var apporteur: String? = null
    var immatriculation: String? = null
    var attestation: String? = null
    var carteRose: String? = null
    var minPrix: Int? = null
    var maxPrix: Int? = null
    var minPuissance: Int? = null
    var maxPuissance: Int? = null
    var minDate: Long? = null
    var maxDate: Long? = null
}