package com.kod.assurancecontracthandler.viewmodels.contractListViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.kod.assurancecontracthandler.repository.ContractRepository

class ContractListViewModelFactory(
    private val repository: ContractRepository,
    private val expandableGroupTitlesList: List<String>,
    private val expandableChildrenTitlesList: List<List<String>>
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContractListViewModel::class.java)) return ContractListViewModel(
            repository,
            expandableGroupTitlesList,
            expandableChildrenTitlesList
        ) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}