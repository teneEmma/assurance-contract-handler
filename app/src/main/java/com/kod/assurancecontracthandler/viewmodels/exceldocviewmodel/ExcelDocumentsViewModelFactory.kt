package com.kod.assurancecontracthandler.viewmodels.exceldocviewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ExcelDocumentsViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(ExcelDocumentsViewModel::class.java)) return ExcelDocumentsViewModel(application) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}