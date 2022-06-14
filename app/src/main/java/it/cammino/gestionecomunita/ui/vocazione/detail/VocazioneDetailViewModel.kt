package it.cammino.gestionecomunita.ui.vocazione.detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Vocazione
import it.cammino.gestionecomunita.database.item.ComunitaFratello
import it.cammino.gestionecomunita.item.ExpandableBrotherItem

class VocazioneDetailViewModel: ViewModel(){

    var listId: Long = -1
    val editMode = MutableLiveData(true)
    var createMode = true
    var vocazione: Vocazione = Vocazione()
    lateinit var comunitaList: List<Comunita>

}
