package com.kod.assurancecontracthandler.viewmodels.expiringviewmodel

import android.content.res.AssetManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.R
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

class ExpiringContractsViewModel(private val contractRepository: ContractRepository) : BaseViewModel() {
    private val _contractsToExpire = MutableLiveData<List<BaseContract>?>()
    private var _thereIsNoContractToExpire = false
    private var _createdFileName: String? = null
    private var _expiringContractsMaxNumberOfDays = 1L
    var idItemSlided: Int = -1

    val contractsToExpire: LiveData<List<BaseContract>?>
        get() = _contractsToExpire
    val thereIsNoContractToExpire: Boolean
        get() = _thereIsNoContractToExpire
    val isLoading: LiveData<Boolean>
        get() = _isLoading
    val createdFileName: String?
        get() = _createdFileName
    val messageResourceId: LiveData<Int?>
        get() = _messageResourceId

    fun searchExpiringContractForAssurer(newText: String?) {
        if (newText == null) {
            return
        }
        val todayInLong = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000
        val todayString = TimeConverters.formatLongToLocaleDate(todayInLong)
        val maxDateInLong = LocalDateTime.now().plusDays(_expiringContractsMaxNumberOfDays)
            .toEpochSecond(ZoneOffset.UTC) * 1000
        val maxDateString = TimeConverters.formatLongToLocaleDate(maxDateInLong)
        val concatenatedName = "%$newText%"
        executeFunctionWithoutAnimation {
            val filteredContracts =
                contractRepository.fetchExpiringContractForCustomerWithName(concatenatedName, todayString, maxDateString)
            _thereIsNoContractToExpire = filteredContracts?.isEmpty() == true
            _contractsToExpire.postValue(filteredContracts)
        }
    }

    fun getExpiringContracts(numberOfDays: Long) {
        _expiringContractsMaxNumberOfDays = numberOfDays
        val todayInLong = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) * 1000
        val todayString = TimeConverters.formatLongToLocaleDate(todayInLong)
        val maxDateInLong = LocalDateTime.now().plusDays(_expiringContractsMaxNumberOfDays)
            .toEpochSecond(ZoneOffset.UTC) * 1000
        val maxDateString = TimeConverters.formatLongToLocaleDate(maxDateInLong)

        if (todayString == null || maxDateString == null) {
            return
        }
        executeFunctionWithoutAnimation {
            val contracts = contractRepository.getExpiringContractsForGivenDate(
                today = todayString, maxTime = maxDateString
            )
            _thereIsNoContractToExpire = contracts?.isEmpty() == true
            _contractsToExpire.postValue(contracts)
        }
    }

    fun exportContractToFile(assetManager: AssetManager) {
        val baseContract = contractsToExpire.value?.get(idItemSlided)
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
}