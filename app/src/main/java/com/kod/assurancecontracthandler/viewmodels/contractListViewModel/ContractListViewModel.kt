package com.kod.assurancecontracthandler.viewmodels.contractListViewModel

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.sqlite.db.SimpleSQLiteQuery
import com.google.android.material.textfield.TextInputEditText
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.constants.ConstantsVariables
import com.kod.assurancecontracthandler.common.utilities.DataTypesConversionAndFormattingUtils.concatenateStringForDBQuery
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
    private var initialSearchString: String? = null
    private val _shouldDisplayFabFilter = MutableLiveData<Boolean>(false)
    private val _messageResourceId = MutableLiveData<Int>()
    private var _minDate: Long? = null
    private var _maxDate: Long? = null
    private var _slidersValues: MutableMap<String, MutableMap<String, Pair<Float, Float>?>> = initializeSlidersMap()
    private var _selectedFilteredChips: List<Int> = emptyList()
    private var _createdFileName: String? = null

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
                nullPair
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
    var idItemSlided: Int = -1
    val createdFileName: String?
        get() = _createdFileName

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
        _shouldDisplayFabFilter.postValue(true)

        executeFunctionWithAnimation {
            makeQueryToDB(newText)
        }
    }

    private fun makeQueryToDB(str: String) {
        val result = repository.searchForClient(generateQuery(str))
        if (result.isEmpty()) {
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
        initialSearchString =
            "SELECT * FROM contract WHERE" + """ ${ConstantsVariables.searchBarChipsTitles[indexQueryTitle]} LIKE "%$str%" """

        return SimpleSQLiteQuery(initialSearchString)
    }

    fun deactivateAllSearchChips() {
        _selectedSearchChip = null
        initialSearchString = null
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
        val textInputsSuffixFilterQuery = filterContractsUsingTextInputFields()
        val datesSuffixFilterQuery = filterContractsUsingDates()
        val slidersSuffixFilterQuery = filterContractsUsingSliders()
        var suffixFilterQuery = datesSuffixFilterQuery + slidersSuffixFilterQuery + textInputsSuffixFilterQuery

        executeFunctionWithAnimation {
            // Removing the first AND in query
            suffixFilterQuery = suffixFilterQuery.removeRange(0, 3)
            val filterQuery = "SELECT * FROM ($initialSearchString) WHERE $suffixFilterQuery"
            val query = SimpleSQLiteQuery(filterQuery)
            val filteredValues111 = repository.filterContracts(query)
            if (filteredValues111.isEmpty()) {
                _messageResourceId.postValue(R.string.no_result_found)
            }
            _allContracts.postValue(filteredValues111)
        }

    }

    private fun filterContractsUsingDates(): String {
        var suffixFilterQuery = ""
        if (shouldFilterField(this._minDate)) {
            val minDate = TimeConverters.formatLongToLocaleDate(_minDate)
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND effet >= "$minDate" """
            )
        }
        if (shouldFilterField(this._maxDate)) {
            val maxDate = TimeConverters.formatLongToLocaleDate(_maxDate) ?: ""
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND echeance <= "$maxDate" """
            )
        }
        return suffixFilterQuery
    }

    private fun filterContractsUsingTextInputFields(): String {
        var suffixFilterQuery = ""
        if (shouldFilterField(_filterChipsAndTextFieldsValues[0])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND APPORTEUR LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[0]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[1])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND assure LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[1]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[2])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND attestation LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[2]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[3])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND carteRose LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[3]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[4])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND categorie LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[4]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[5])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND compagnie LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[5]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[6])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND immatriculation LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[6]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[7])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND mark LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[7]!!)}" """
            )
        }
        if (shouldFilterField(_filterChipsAndTextFieldsValues[8])) {
            suffixFilterQuery = suffixFilterQuery.plus(
                """AND numeroPolice LIKE "${concatenateStringForDBQuery(_filterChipsAndTextFieldsValues[8]!!)}" """
            )
        }

        return suffixFilterQuery
    }

    private fun filterContractsUsingSliders(): String {
        var suffixFilterQuery = ""
        slidersValues.entries.forEach { groups ->
            groups.value.entries.forEach { children ->
                val childValue = children.value
                val childKey = children.key

                when (childKey to childValue.isNotNull()) {
                    expandableChildrenTitlesList[0][0] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND ACC BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][1] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND COMM_APPORT BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][2] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND CR BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][3] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND DTA BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][4] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND ENCAIS BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][5] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND FC BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][6] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND NET_A_REVERSER BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][7] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND PN BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][8] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND PTTC BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[0][9] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND TVA BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )

                    expandableChildrenTitlesList[1][0] to true -> {
                        suffixFilterQuery = suffixFilterQuery.plus(
                            """AND puissanceVehicule >= "${childValue?.first?.toInt()}%" AND """ + """puissanceVehicule <= "${childValue?.second?.toInt()}%" """
                        )
                    }

                    expandableChildrenTitlesList[2][0] to true -> suffixFilterQuery = suffixFilterQuery.plus(
                        """AND duree BETWEEN "${concatenateStringForDBQuery(childValue!!.first.toString())}" and 
                            "${concatenateStringForDBQuery(childValue.second.toString())}" """
                    )
                }
            }
        }


        return suffixFilterQuery
    }

    fun exportContractToFile(assetManager: AssetManager) {
        val baseContract = _allContracts.value?.get(idItemSlided)
        val contractToExport = baseContract?.contract
        if (contractToExport == null) {
            _messageResourceId.postValue(R.string.error_on_file_reading)
            return
        }

        executeFunctionWithAnimation {
            val result = super.exportContractToFile(contractToExport, assetManager)
            if (result.first) {
                _createdFileName = result.second
                _messageResourceId.postValue(R.string.file_creation_successful)
            } else {
                _messageResourceId.postValue(R.string.file_creation_failed)
            }
        }
    }

    private fun shouldFilterField(field: String?): Boolean = !field.isNullOrEmpty()

    private fun shouldFilterField(field: Long?): Boolean = field != null && field != 0L
}

private fun <A, B> Pair<A, B>?.isNotNull(): Boolean {
    return this != null
}
