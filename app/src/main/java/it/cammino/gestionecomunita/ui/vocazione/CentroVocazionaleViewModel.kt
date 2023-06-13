package it.cammino.gestionecomunita.ui.vocazione

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CentroVocazionaleViewModel : ViewModel() {

    val itemCLickedState = MutableLiveData(ItemClickState.UNCLICKED)
    var clickedId: Long = 0
    var selectedTab: Int = 0

    enum class ItemClickState {
        CLICKED,
        UNCLICKED
    }

}
