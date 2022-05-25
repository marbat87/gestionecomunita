package it.cammino.gestionecomunita.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class DashboardViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Centro Vocazionale è in sviluppo"
    }
    val text: LiveData<String> = _text
}