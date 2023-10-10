package com.kod.assurancecontracthandler.viewmodels.baseviewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kod.assurancecontracthandler.model.BaseContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 *  The base ViewModel for all our viewModels. It contains the base values which each viewModel can contain.
 *  @param hasQueried A live value which indicates the state of the execution of a function through the
 *  [executeFunctionWithAnimation].
 *  @property executeFunctionWithAnimation Executes concurrently a given function while updating a live value.
 *  @property executeFunctionWithoutAnimation Just concurrently execute a function.
 */
abstract class BaseViewModel : ViewModel() {
    companion object {
        @JvmStatic
        protected val _allContracts: MutableLiveData<List<BaseContract>?> = MutableLiveData(null)

        @JvmStatic
        protected val _isLoading = MutableLiveData<Boolean>()

        @JvmStatic
        protected val _messageResourceId = MutableLiveData<Int?>(null)
    }

    open fun executeFunctionWithAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)
            execute()
            _isLoading.postValue(false)
        }
    }


    fun executeFunctionWithoutAnimation(execute: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            execute()
        }
    }
}