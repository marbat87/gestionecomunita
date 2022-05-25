package it.cammino.gestionecomunita.ui.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotificationsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Notifiche è in sviluppo"
    }
    val text: LiveData<String> = _text
}