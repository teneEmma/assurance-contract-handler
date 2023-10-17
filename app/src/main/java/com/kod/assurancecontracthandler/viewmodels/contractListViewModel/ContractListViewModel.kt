package com.kod.assurancecontracthandler.viewmodels.contractListViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.android.material.textfield.TextInputEditText
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class ContractListViewModel(
    private val repository: ContractRepository,
    private val expandableGroupTitlesList: List<String>,
    private val expandableChildrenTitlesList: List<List<String>>
) : BaseViewModel() {
    private var _selectedSearchChip: Int? = null
    private var _searchText: String = ""
    private val _shouldDisplayFabFilter = MutableLiveData<Boolean>(false)
    private val _messageResourceId = MutableLiveData<Int>()
    private var _minDate: Long? = null
    private var _maxDate: Long? = null
    private var _slidersValues: MutableMap<String, MutableMap<String, Pair<Float, Float>?>> = initializeSlidersMap()
    private var _selectedFilteredChips: List<Int> = emptyList()

    /**
     * Here is the structure of this map and what each index represent
     * {
     *   0=Apporteur,
     *   1=Assure,
     *   2=Attestation,
     *   3=Carte rose,
     *   4=Categorie,
     *   5=Compagnie,
     *   6=Immatriculation,
     *   7=Mark,
     *   8=Numero police,
     *  }
     */
    private var _filterChipsAndTextFieldsValues: MutableMap<Int, String?> = initializeChipsAndTextFieldsValues()

    private fun initializeChipsAndTextFieldsValues(): MutableMap<Int, String?> {
        var key = 0
        return ConstantsVariables.filterDialogChips.associate {
            key++ to null as String?
        }.toMutableMap()
    }

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

    val allContracts: LiveData<List<BaseContract>?>
        get() = _allContracts
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
        val result = repository.searchForClient(generateQuery(str))
        if(result.isEmpty()){
            _messageResourceId.postValue(R.string.no_result_found)
        }
        _allContracts.postValue(result)
    }

    @Throws(IndexOutOfBoundsException::class)
    private fun generateQuery(str: String): SimpleSQLiteQuery {
        val indexQueryTitle = selectedSearchChip ?: return SimpleSQLiteQuery("")
        if (indexQueryTitle > ConstantsVariables.searchBarChipsTitles.size) {
            throw IndexOutOfBoundsException("chip index is greater than title's indices")
        }
        val initialQuery =
            "SELECT * FROM contract WHERE" + """ ${ConstantsVariables.searchBarChipsTitles[indexQueryTitle]} LIKE "%$str%" """

        return SimpleSQLiteQuery(initialQuery)
    }

    fun deactivateAllSearchChips() {
        _selectedSearchChip = null
        _shouldDisplayFabFilter.postValue(false)
    }

    fun fetchAllContracts() = _allContracts.postValue(repository.readAllContracts())
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
        _filterChipsAndTextFieldsValues = initializeChipsAndTextFieldsValues()
    }

    fun setEditTextValues(listEditTexts: List<TextInputEditText>) {
        _selectedFilteredChips.forEach { index ->
            _filterChipsAndTextFieldsValues[index] = listEditTexts[index].text?.toString()?.trim()
        }
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
        if (shouldFilterField(this._minDate) && shouldFilterField(this._maxDate)) {
            val minDate = TimeConverters.formatLongToLocaleDate(_minDate) ?: ""
            val maxDate = TimeConverters.formatLongToLocaleDate(_maxDate) ?: ""
            filteredValues = filteredValues.filter {
                it.contract?.effet?.let { startDate -> startDate >= minDate } == true &&
                        it.contract.echeance?.let { endDate -> endDate <= maxDate } == true
            }
        }
        return filteredValues
    }

    private fun filterContractsUsingTextInputFields(contracts: List<BaseContract>): List<BaseContract> {
        var filteredValues: List<BaseContract> = contracts
        if (shouldFilterField(_filterChipsAndTextFieldsValues[0])) {
            filteredValues = filteredValues.filter {
                it.contract?.APPORTEUR?.contains(_filterChipsAndTextFieldsValues[0] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[1])) {
            filteredValues = filteredValues.filter {
                it.contract?.assure?.contains(_filterChipsAndTextFieldsValues[1] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[2])) {
            filteredValues = filteredValues.filter {
                it.contract?.attestation?.contains(_filterChipsAndTextFieldsValues[2] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[3])) {
            filteredValues = filteredValues.filter {
                it.contract?.carteRose?.contains(_filterChipsAndTextFieldsValues[3] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[4])) {
            filteredValues = filteredValues.filter {
                it.contract?.categorie == _filterChipsAndTextFieldsValues[4]?.toInt()
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[5])) {
            filteredValues = filteredValues.filter {
                it.contract?.compagnie?.contains(_filterChipsAndTextFieldsValues[4] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[6])) {
            filteredValues = filteredValues.filter {
                it.contract?.immatriculation?.contains(
                    _filterChipsAndTextFieldsValues[6] ?: "", ignoreCase = true
                ) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[7])) {
            filteredValues = filteredValues.filter {
                it.contract?.mark?.contains(_filterChipsAndTextFieldsValues[7] ?: "", ignoreCase = true) == true
            }
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[8])) {
            filteredValues = filteredValues.filter {
                it.contract?.numeroPolice?.contains(_filterChipsAndTextFieldsValues[8] ?: "", ignoreCase = true) == true
            }
        }
        return filteredValues
    }

    private fun filterContractsUsingSliders(contracts: List<BaseContract>): List<BaseContract> {
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

                    expandableChildrenTitlesList[1][0] to true -> filteredValues = filteredValues.filter { c ->

                        val vehiclePower =
                            DataTypesConversionAndFormattingUtils.convertPowerFieldStringToFloat(c.contract?.puissanceVehicule)
                                ?: return@filter false

                        vehiclePower >= childValue!!.first && vehiclePower <= childValue.second

                    }

                    expandableChildrenTitlesList[2][0] to true -> filteredValues =
                        filteredValues.filter { c -> c.contract?.duree?.let { it >= childValue!!.first && it <= childValue.second } == true }
                }
            }
        }
        return filteredValues
    }

    private fun shouldFilterField(field: String?): Boolean = !field.isNullOrEmpty()

    private fun shouldFilterField(field: Long?): Boolean = field != null && field != 0L
}

private fun <A, B> Pair<A, B>?.isNotNull(): Boolean {
    return this != null
}
