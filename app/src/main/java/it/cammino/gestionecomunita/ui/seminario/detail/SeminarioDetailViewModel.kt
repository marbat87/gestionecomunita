package it.cammino.gestionecomunita.ui.seminario.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.gestionecomunita.database.entity.Seminario
import it.cammino.gestionecomunita.database.item.SeminarioWithDetails
import it.cammino.gestionecomunita.database.item.SeminaristaWithComunita
import it.cammino.gestionecomunita.item.ResponsabileListItem
import it.cammino.gestionecomunita.item.SeminaristaItem
import it.cammino.gestionecomunita.item.VisitaSeminarioItem

class SeminarioDetailViewModel : ViewModel() {

    var listId: Long = -1
    val editMode = MutableLiveData(true)
    var createMode = true
    var seminario = SeminarioWithDetails(Seminario(), ArrayList(), ArrayList(), ArrayList())
    var selectedTabIndex = 0
    var selectedSeminarista = 0
    var selectedVisita = 0
    var seminaristiItems: List<SeminaristaItem> = ArrayList()
    var visiteItems: List<VisitaSeminarioItem> = ArrayList()
    var rettori: List<ResponsabileListItem> = ArrayList()
    var viceRettori: List<ResponsabileListItem> = ArrayList()
    var direttoriSpirituali: List<ResponsabileListItem> = ArrayList()

}
