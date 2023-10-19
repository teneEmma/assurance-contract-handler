package com.kod.assurancecontracthandler.viewmodels.expiringviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.kod.assurancecontracthandler.common.utilities.TimeConverters
import com.kod.assurancecontracthandler.model.BaseContract
import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel
import java.time.LocalDateTime
import java.time.ZoneOffset

class ExpiringContractsViewModel(private val contractRepository: ContractRepository) : BaseViewModel() {
    private val _contractsToExpire = MutableLiveData<List<BaseContract>?>()
    private var _thereIsNoContractToExpire = false
    private var _expiringContractsMaxNumberOfDays = 1L

    val contractsToExpire: LiveData<List<BaseContract>?>
        get() = _contractsToExpire
    val thereIsNoContractToExpire: Boolean
        get() = _thereIsNoContractToExpire

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
}