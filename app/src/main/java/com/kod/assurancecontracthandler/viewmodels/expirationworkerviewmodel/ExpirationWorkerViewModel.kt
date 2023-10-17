package com.kod.assurancecontracthandler.viewmodels.selectfileviewmodel

import com.kod.assurancecontracthandler.repository.ContractRepository
import com.kod.assurancecontracthandler.viewmodels.baseviewmodel.BaseViewModel

class ExpirationWorkerViewModel(private val contractRepository: ContractRepository) : BaseViewModel() {

    fun isContractsExpiring(today: String, expDate: String): Boolean {
        return contractRepository.numberOfContractsExpiring(today, expDate) != 0
    }
}