package com.peer_messanger.ui.base


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    fun work(work: suspend () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) { work.invoke() }
    }
}