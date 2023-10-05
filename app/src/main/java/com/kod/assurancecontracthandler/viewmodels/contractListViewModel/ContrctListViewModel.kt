package com.kod.assurancecontracthandler.viewmodels.contractListViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionUtils
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class ContractListViewModel(private val repository: ContractRepository) : BaseViewModel() {


    //    var searchChip: Int? = 1
//    var apporteur: String? = null
//    var immatriculation: String? = null
//    var attestation: String? = null
//    var compagnie: String? = null
//    var assure: String? = null
//    var mark: String? = null
//    var nPolice: String? = null
//    var carteRose: String? = null
//    private val  _listContracts = MutableLiveData<List<BaseContract>?>()
//    val listContracts: LiveData<List<BaseContract>?>
//        get() = _listContracts
//    var group1SliderValues = hashMapOf<Int, Pair<Int, Int>>()
//    var group2SliderValues = hashMapOf<Int, Pair<Int, Int>>()
//    var childrenTouched: Int? = null
//
//    private val _isSearching = MutableLiveData<Boolean>()
//    val isSearching : LiveData<Boolean>
//        get() = _isSearching
//    private val _success = MutableLiveData<Boolean>(false)
//    val success : LiveData<Boolean>
//        get() = _success

//
//    fun filterFields(listContracts: List<BaseContract>){
//        _isSearching.value = true
//        var filteredValues: List<BaseContract> = listContracts
//        if(checkField(apporteur)) {
//            filteredValues = filteredValues.filter { this.apporteur?.let { value ->
//                it.contract?.APPORTEUR?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        /*TODO: Replace it it.contract?.APPORTEUR?.uppercase()?.contains(value.uppercase()) to
//        TODO: it.contract?.APPORTEUR?.contains(value, ignoreCase=true)*/
//        if(checkField(immatriculation)) {
//            filteredValues = filteredValues.filter { this.immatriculation?.let { value ->
//                it.contract?.immatriculation?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(attestation)) {
//            filteredValues = filteredValues.filter { this.attestation?.let { value ->
//                it.contract?.attestation?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(carteRose)) {
//            filteredValues = filteredValues.filter { this.carteRose?.let { value ->
//                it.contract?.carteRose?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(compagnie)) {
//            filteredValues = filteredValues.filter { this.compagnie?.let { value ->
//                it.contract?.compagnie?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(assure)) {
//            filteredValues = filteredValues.filter { this.assure?.let { value ->
//                it.contract?.assure?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(mark)) {
//            filteredValues = filteredValues.filter { this.mark?.let { value ->
//                it.contract?.mark?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(nPolice)) {
//            filteredValues = filteredValues.filter { this.nPolice?.let { value ->
//                it.contract?.numeroPolice?.uppercase()?.contains(value.uppercase()) } == true }
//        }
//        if(checkField(this.minDate) && checkField(this.maxDate)) {
//            filteredValues = filteredValues.filter {
//                it.contract?.echeance?.let {date-> date.time >= this.minDate!!}  == true &&
//                        it.contract.echeance?.let {date-> date.time  <= this.maxDate!!} == true
//            }
//        }
//
//        group1SliderValues.forEach { price->
//            when(price.key){
//                0-> filteredValues = filteredValues.filter { c -> (c.contract?.DTA?.let { it >= price.value.first && it <= price.value.second }) == true }
//                1-> filteredValues = filteredValues.filter {c-> c.contract?.PN?.let { it >= price.value.first && it <= price.value.second} == true }
//                2-> filteredValues = filteredValues.filter {c-> c.contract?.ACC?.let { it >= price.value.first && it <= price.value.second} == true }
//                3-> filteredValues = filteredValues.filter {c-> c.contract?.FC?.let { it >= price.value.first && it <= price.value.second} == true }
//                4-> filteredValues = filteredValues.filter {c-> c.contract?.TVA?.let { it >= price.value.first && it <= price.value.second} == true }
//                5-> filteredValues = filteredValues.filter {c-> c.contract?.CR?.let { it >= price.value.first && it <= price.value.second} == true }
//                6-> filteredValues = filteredValues.filter {c-> c.contract?.PTTC?.let { it >= price.value.first && it <= price.value.second} == true }
//                7-> filteredValues = filteredValues.filter {c-> c.contract?.ENCAIS?.let { it >= price.value.first && it <= price.value.second} == true }
//                8-> filteredValues = filteredValues.filter {c-> c.contract?.NET_A_REVERSER?.let { it >= price.value.first && it <= price.value.second} == true }
//                9-> filteredValues = filteredValues.filter {c-> c.contract?.COMM_APPORT?.let { it >= price.value.first && it <= price.value.second} == true }
//            }
//        }
//
//        if (group2SliderValues.containsKey(0)){
//            val price = this.group2SliderValues.getValue(0)
//            filteredValues = filteredValues.filter {c->
//                c.contract?.puissanceVehicule?.split("CV-")?.get(0)?.toIntOrNull()?.
//                let { price.first <= it && price.second >= it} == true }
//        }
//
//        filteredValues = filteredValues.filter { c -> c.contract?.numeroPolice.isNullOrEmpty().not() ||
//                c.contract?.attestation.isNullOrEmpty().not()
//        }
//        _success.postValue(filteredValues.isNotEmpty())
//        _isSearching.value = false
//
//        _listContracts.postValue(filteredValues)
//    }

    //TODO: put these in xml files.
    val expandableGroupTitlesList = listOf("FILTRER LES PRIX", "FILTRER LA PUISSANCE")
    val expandableChildrenTitlesList = listOf(
        listOf(
            "ACC",
            "COMMISION APPORTEUR",
            "CR",
            "DTA",
            "ENCAISSEMENT",
            "FC",
            "NET_A_REVERSER",
            "PN",
            "PTTC",
            "TVA"
        ).sorted(),
        listOf("PUISSANCE")
    )

    private var _selectedSearchChip: Int? = null
    private var _searchText: String = ""
    private val _shouldDisplayFabFilter = MutableLiveData<Boolean>(false)
    private val _messageResourceId = MutableLiveData<Int>()
    private var _minDate: Long? = null
    private var _maxDate: Long? = null
    private var _slidersValues: MutableMap<String, MutableMap<String, Pair<Float, Float>?>> = initializeSlidersMap()
    private var _selectedFilteredChips: List<Int> = emptyList()
    private var providerTextViewContent: String = ""
        private set
    private var matriculationTextViewContent: String = ""
        private set
    private var registrationTextViewContent: String = ""
        private set
    private var pinkCardTextViewContent: String = ""
        private set
    private var companyTextViewContent: String = ""
        private set
    private var assurerTextViewContent: String = ""
        private set
    private var markTextViewContent: String = ""
        private set
    private var policeNumberTextViewContent: String = ""
        private set

    private fun initializeSlidersMap(): MutableMap<String, MutableMap<String, Pair<Float, Float>?>> {
        val nullPair: Pair<Float, Float>? = null
        return expandableGroupTitlesList.zip(expandableChildrenTitlesList).associate { (key, value) ->
            key to value.associateWith {
                when (key) {
                    expandableGroupTitlesList[0] -> nullPair
                    else -> nullPair
                }
            } as MutableMap
        } as MutableMap
    }

    val selectedSearchChip: Int?
        get() = _selectedSearchChip
    val searchText: String
        get() = _searchText
    val shouldDisplayFabFilter: LiveData<Boolean>
        get() = _shouldDisplayFabFilter
    val messageResourceId: LiveData<Int>
        get() = _messageResourceId

    val isLoading: LiveData<Boolean>
        get() = _isLoading

    val filterChipsToShow: List<String>
        get() = ConstantsVariables.filterDialogChips
    val slidersValues: Map<String, Map<String, Pair<Float, Float>?>>
        get() = _slidersValues

    val startDate: Long?
        get() = _minDate
    val endDate: Long?
        get() = _maxDate

    val selectedFilteredChips: List<Int>
        get() = _selectedFilteredChips

    @Throws(IndexOutOfBoundsException::class)
    fun onSearchChipCheckChanged(searchChipId: Int?) {
        if (searchChipId == null) {
            _selectedSearchChip = null
            _shouldDisplayFabFilter.postValue(false)
            return
        }
        if (searchChipId > ConstantsVariables.searchBarChipsTitles.size) {
            throw IndexOutOfBoundsException("chip index is greater than title's indices")
        }
        _selectedSearchChip = searchChipId
        onSearchText(_searchText)
    }

    fun onSearchText(newText: String?) {
        if (newText == null) {
            return
        }
        _searchText = newText
        if (_selectedSearchChip == null) {
            _messageResourceId.postValue(R.string.no_selected_chips)
            return
        }
        _shouldDisplayFabFilter.postValue(newText.isNotEmpty())

        executeFunctionWithAnimation {
            makeQueryToDB(newText)
        }
    }

    private fun makeQueryToDB(str: String) {
        _allContracts.postValue(formatContractIds(repository.searchForClient(generateQuery(str))))
    }

    @Throws(IndexOutOfBoundsException::class)
    private fun generateQuery(str: String): SimpleSQLiteQuery {
        val indexQueryTitle = selectedSearchChip ?: return SimpleSQLiteQuery("")
        if (indexQueryTitle > ConstantsVariables.searchBarChipsTitles.size) {
            throw IndexOutOfBoundsException("chip index is greater than title's indices")
        }
        val initialQuery = "SELECT * FROM contract WHERE" +
                """ ${ConstantsVariables.searchBarChipsTitles[indexQueryTitle]} LIKE "%$str%" """

        return SimpleSQLiteQuery(initialQuery)
    }

    private fun formatContractIds(list: List<BaseContract>?): List<BaseContract>? {
        if (list == null) {
            return null
        }
        list.forEachIndexed { index, baseContract ->
            baseContract.id = index + 1
        }

        return list
    }

    fun deactivateAllSearchChips() {
        _selectedSearchChip = null
        _shouldDisplayFabFilter.postValue(false)
    }

    suspend fun fetchAllContracts() = _allContracts.postValue(repository.readAllContracts())
    fun onDateChanged(maxDate: Long?, minDate: Long?) {
        _minDate = minDate
        _maxDate = maxDate
    }

    fun onSliderTouched(groupAndChildPositions: Pair<Int, Int>, minAndMaxValues: Pair<Float, Float>) {
        val groupKey = expandableGroupTitlesList[groupAndChildPositions.first]
        val childKey = expandableChildrenTitlesList[groupAndChildPositions.first][groupAndChildPositions.second]
        _slidersValues[groupKey]?.put(childKey, minAndMaxValues)
    }

    fun onFilterChipCheckChanged(checkedIds: List<Int>) {
        _selectedFilteredChips = checkedIds
    }

    fun clearData() {
        _selectedFilteredChips = emptyList()
        _minDate = null
        _maxDate = null
        _slidersValues = initializeSlidersMap()
    }

    fun setEditTextValues(
        provider: String,
        matriculation: String,
        attestation: String,
        pinkCard: String,
        company: String,
        assurer: String,
        mark: String,
        policeNumber: String
    ) {
        providerTextViewContent = provider
        matriculationTextViewContent = matriculation
        registrationTextViewContent = attestation
        pinkCardTextViewContent = pinkCard
        companyTextViewContent = company
        assurerTextViewContent = assurer
        markTextViewContent = mark
        policeNumberTextViewContent = policeNumber
    }

    fun filterContracts() {
        if (_allContracts.value.isNullOrEmpty()) {
            return
        }
        var filteredValues: List<BaseContract> = _allContracts.value ?: emptyList()

        filteredValues = filterContractsUsingTextInputFields(filteredValues)
        filteredValues = filterContractsUsingDates(filteredValues)
        filteredValues = filterContractsUsingSliders(filteredValues)

        if (filteredValues.isEmpty()) {
            _messageResourceId.postValue(R.string.no_result_found)
        }
        _allContracts.postValue(filteredValues)
    }

    private fun filterContractsUsingDates(contracts: List<BaseContract>): List<BaseContract> {
        var filteredValues: List<BaseContract> = contracts
        if (checkField(this._minDate) && checkField(this._maxDate)) {
            val timeConverterUtil = TimeConverters
            val minDate = timeConverterUtil.formatLongToLocaleDate(_minDate) ?: ""
            val maxDate = timeConverterUtil.formatLongToLocaleDate(_maxDate) ?: ""
            filteredValues = filteredValues.filter {
                it.contract?.effet?.let { startDate -> startDate >= minDate } == true &&
                        it.contract.echeance?.let { endDate -> endDate <= maxDate } == true
            }
        }
        return filteredValues
    }

    private fun filterContractsUsingTextInputFields(contracts: List<BaseContract>): List<BaseContract>{
        var filteredValues: List<BaseContract> = contracts
        if (checkField(providerTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.APPORTEUR?.contains(providerTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(matriculationTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.immatriculation?.contains(matriculationTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(registrationTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.attestation?.contains(registrationTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(pinkCardTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.carteRose?.contains(pinkCardTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(companyTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.compagnie?.contains(companyTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(assurerTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.assure?.contains(assurerTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(markTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.mark?.contains(markTextViewContent, ignoreCase = true) == true
            }
        }
        if (checkField(policeNumberTextViewContent)) {
            filteredValues = filteredValues.filter {
                it.contract?.numeroPolice?.contains(policeNumberTextViewContent, ignoreCase = true) == true
            }
        }
        return filteredValues
    }

    private fun filterContractsUsingSliders(contracts: List<BaseContract>):  List<BaseContract> {
        var filteredValues: List<BaseContract> = contracts
        slidersValues.entries.forEach { groups ->
            groups.value.entries.forEach { children ->
                val childValue = children.value
                val childKey = children.key

                when (childKey to childValue.isNotNull()) {
                    expandableChildrenTitlesList[0][0] to true -> filteredValues =
                        filteredValues.filter { c -> (c.contract?.ACC?.let { it >= childValue!!.first && it <= childValue.second }) == true }

                    expandableChildrenTitlesList[0][1] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.COMM_APPORT?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][2] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.CR?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][3] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.DTA?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][4] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.ENCAIS?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][5] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.FC?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][6] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.NET_A_REVERSER?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][7] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.PN?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][8] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.PTTC?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[0][9] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.TVA?.let { it >= childValue!!.first && it <= childValue.second } == true }

                    expandableChildrenTitlesList[1][0] to true -> filteredValues =
                        filteredValues.filter { c ->

                            val vehiclePower =
                                DataTypesConversionUtils.convertPowerFieldStringToFloat(c.contract?.puissanceVehicule)
                                    ?: return@filter false

                            vehiclePower >= childValue!!.first && vehiclePower <= childValue.second

                        }
                }
            }
        }
        return filteredValues
    }

    private fun checkField(field: String?): Boolean = !field.isNullOrEmpty()
    private fun checkField(field: Long?): Boolean = field != null && field != 0L
}

private fun <A, B> Pair<A, B>?.isNotNull(): Boolean {
    return this != null
}
