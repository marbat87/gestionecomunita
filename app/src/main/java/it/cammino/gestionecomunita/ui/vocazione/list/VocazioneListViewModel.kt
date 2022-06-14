package it.cammino.gestionecomunita.ui.vocazione.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Vocazione
import it.cammino.gestionecomunita.item.VocazioneListItem

class VocazioneListViewModel(application: Application) : AndroidViewModel(application) {

    var vocazioniLiveList: LiveData<List<Vocazione>>? = null
        private set
    var vocazioniList: List<VocazioneListItem> = ArrayList()


    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        vocazioniLiveList = mDb.vocazioneDao().liveAll
    }

}
