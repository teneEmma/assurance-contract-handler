package com.kod.assurancecontracthandler.viewmodels.baseviewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  The base ViewModel for all our viewModels. It contains the base values which each viewModel can contain.
 *  @param hasQueried A live value which indicates the state of the execution of a function through the
 *  [executeFunWithAnimation].
 *  @property executeFunWithAnimation Executes concurrently a given function while updating a live value.
 *  @property executeFunWithoutAnimation Just concurrently execute a function.
 */
abstract class BaseViewModel : ViewModel() {
    private val _hasQueried = MutableLiveData<Boolean>()
    val hasQueried: LiveData<Boolean>
        get() = _hasQueried

    open val messageResourceId: LiveData<Int>? = null
    open fun executeFunWithAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _hasQueried.postValue(false)
            execute()
            _hasQueried.postValue(true)
        }
    }


    fun executeFunWithoutAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            execute()
        }
    }
}