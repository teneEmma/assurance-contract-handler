package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExcelDocumentsViewModelFactory(): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExcelDocumentsViewModel::class.java)) return ExcelDocumentsViewModel() as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}