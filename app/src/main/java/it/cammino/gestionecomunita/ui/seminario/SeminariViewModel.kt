package it.cammino.gestionecomunita.ui.seminario

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SeminariViewModel : ViewModel() {

    val itemCLickedState = MutableLiveData(ItemClickState.UNCLICKED)
    var clickedId: Long = -1

    enum class ItemClickState {
        CLICKED,
        UNCLICKED
    }

}
