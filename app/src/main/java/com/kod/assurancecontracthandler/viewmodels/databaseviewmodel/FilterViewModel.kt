package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.kod.assurancecontracthandler.model.ContractDbDto

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
    private val  _listContracts = MutableLiveData<List<ContractDbDto>?>()
    val listContracts: LiveData<List<ContractDbDto>?>
    get() = _listContracts
    var minDate: Long? = null
    var maxDate: Long? = null
    var group1SliderValues = hashMapOf<Int, Pair<Int, Int>>()
    var group2SliderValues = hashMapOf<Int, Pair<Int, Int>>()
    var childrenTouched: Int? = null
    var searchText: String = ""

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching :LiveData<Boolean>
        get() = _isSearching
    private val _success = MutableLiveData<Boolean>(false)
    val success :LiveData<Boolean>
        get() = _success
    private fun checkField(field: String?) : Boolean = !field.isNullOrEmpty()
    private fun checkField(field: Long?) : Boolean = field != null && field != 0L

    fun filterFields(listContracts: List<ContractDbDto>){
        _isSearching.value = true
        var filteredValues: List<ContractDbDto> = listContracts
        if(checkField(apporteur)) {
            filteredValues = filteredValues.filter { this.apporteur?.let { value ->
                it.contract?.APPORTEUR?.uppercase()?.contains(value.uppercase()) } == true }
        }
        /*TODO: Replace it it.contract?.APPORTEUR?.uppercase()?.contains(value.uppercase()) to
        TODO: it.contract?.APPORTEUR?.contains(value, ignoreCase=true)*/
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
        if(checkField(this.minDate) && checkField(this.maxDate)) {
            filteredValues = filteredValues.filter {
                it.contract?.echeance?.let {date-> date.time >= this.minDate!!}  == true &&
                it.contract.echeance?.let {date-> date.time  <= this.maxDate!!} == true
            }
        }

        group1SliderValues.forEach { price->
            when(price.key){
                0-> filteredValues = filteredValues.filter { c -> (c.contract?.DTA?.let { it >= price.value.first && it <= price.value.second }) == true }
                1-> filteredValues = filteredValues.filter {c-> c.contract?.PN?.let { it >= price.value.first && it <= price.value.second} == true }
                2-> filteredValues = filteredValues.filter {c-> c.contract?.ACC?.let { it >= price.value.first && it <= price.value.second} == true }
                3-> filteredValues = filteredValues.filter {c-> c.contract?.FC?.let { it >= price.value.first && it <= price.value.second} == true }
                4-> filteredValues = filteredValues.filter {c-> c.contract?.TVA?.let { it >= price.value.first && it <= price.value.second} == true }
                5-> filteredValues = filteredValues.filter {c-> c.contract?.CR?.let { it >= price.value.first && it <= price.value.second} == true }
                6-> filteredValues = filteredValues.filter {c-> c.contract?.PTTC?.let { it >= price.value.first && it <= price.value.second} == true }
                7-> filteredValues = filteredValues.filter {c-> c.contract?.ENCAIS?.let { it >= price.value.first && it <= price.value.second} == true }
                8-> filteredValues = filteredValues.filter {c-> c.contract?.NET_A_REVERSER?.let { it >= price.value.first && it <= price.value.second} == true }
                9-> filteredValues = filteredValues.filter {c-> c.contract?.COMM_APPORT?.let { it >= price.value.first && it <= price.value.second} == true }
            }
        }

        if (group2SliderValues.containsKey(0)){
            val price = this.group2SliderValues.getValue(0)
            filteredValues = filteredValues.filter {c->
                c.contract?.puissanceVehicule?.split("CV-")?.get(0)?.toIntOrNull()?.
                let { price.first <= it && price.second >= it} == true }
        }

        filteredValues = filteredValues.filter { c -> c.contract?.numeroPolice.isNullOrEmpty().not() ||
                    c.contract?.attestation.isNullOrEmpty().not()
        }
        _success.postValue(filteredValues.isNotEmpty())
        _isSearching.value = false

        _listContracts.postValue(filteredValues)
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
        filterChip = null
        group1SliderValues.clear()
        group2SliderValues.clear()
        minDate = 0
        maxDate = 0
    }
}