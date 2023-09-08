package com.kod.assurancecontracthandler.viewmodels.customerviewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CustomerViewModelFactory(private val application: Application): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CustomerViewModel::class.java)) return CustomerViewModel(application) as T
        throw java.lang.IllegalArgumentException("Unknown ViewModel Class")
    }
}