package com.kod.assurancecontracthandler.viewmodels.databaseviewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DBViewModelFactory(val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(DBViewModel::class.java)) return DBViewModel(application) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}