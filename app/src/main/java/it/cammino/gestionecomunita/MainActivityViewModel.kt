package it.cammino.gestionecomunita

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class MainActivityViewModel(application: Application) :
    AndroidViewModel(application) {

    val itemCLickedState = MutableLiveData(ItemClickState.UNCLICKED)
    var clickedId = 0

}
