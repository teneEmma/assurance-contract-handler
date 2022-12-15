package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kod.assurancecontracthandler.model.ContractDbDto
import kotlin.math.max

class FilterViewModel: ViewModel() {
    var searchChip: Int? = 1
    var filterChip: List<Int>? = null
    var apporteur: String? = null
    var immatriculation: String? = null
    var attestation: String? = null
    var compagnie: String? = null
    var assure: String? = null
    var mark: String? = null
    var nPolice: String? = null
    var carteRose: String? = null
    var prixSlidersValue:  HashMap<Int, Pair<Int, Int>>? = null
    var puissanceSliderValue:  HashMap<Int, Pair<Int, Int>>? = null
    var minDate: Long? = null
    var maxDate: Long? = null

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching :LiveData<Boolean>
        get() = _isSearching
    private val _success = MutableLiveData<Boolean>(false)
    val success :LiveData<Boolean>
        get() = _success
    private fun checkField(field: String?) : Boolean = !field.isNullOrEmpty()

    fun filterFields(listContracts: List<ContractDbDto>): List<ContractDbDto>{
        _isSearching.value = true
        var filteredValues: List<ContractDbDto> = listContracts
        if(checkField(apporteur)) {
            filteredValues = filteredValues.filter { this.apporteur?.let { value ->
                it.contract?.APPORTEUR?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(immatriculation)) {
            filteredValues = filteredValues.filter { this.immatriculation?.let { value ->
                it.contract?.immatriculation?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(attestation)) {
            filteredValues = filteredValues.filter { this.attestation?.let { value ->
                it.contract?.attestation?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(carteRose)) {
            filteredValues = filteredValues.filter { this.carteRose?.let { value ->
                it.contract?.carteRose?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(compagnie)) {
            filteredValues = filteredValues.filter { this.compagnie?.let { value ->
                it.contract?.compagnie?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(assure)) {
            filteredValues = filteredValues.filter { this.assure?.let { value ->
                it.contract?.assure?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(mark)) {
            filteredValues = filteredValues.filter { this.mark?.let { value ->
                it.contract?.mark?.uppercase()?.contains(value.uppercase()) } == true }
        }
        if(checkField(nPolice)) {
            filteredValues = filteredValues.filter { this.nPolice?.let { value ->
                it.contract?.numeroPolice?.uppercase()?.contains(value.uppercase()) } == true }
        }
        _success.value = filteredValues.isNotEmpty()
        _isSearching.value = false
        return filteredValues
    }

    fun clearData(){
        apporteur = null
        immatriculation = null
        apporteur = null
        attestation = null
        carteRose = null
        compagnie = null
        mark = null
        nPolice = null
        minDate = null
        maxDate = null
        searchChip = null
        filterChip = null
        //TODO: Equally set all the slider values to zero
    }
}