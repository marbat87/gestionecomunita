package it.cammino.gestionecomunita.ui.comunita

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ComunitaIndexViewModel : ViewModel() {

    var pageViewed = 0

    val itemCLickedState = MutableLiveData(ItemClickState.UNCLICKED)
    var clickedId: Long = 0

    enum class ItemClickState {
        CLICKED,
        UNCLICKED
    }

}
