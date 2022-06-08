package it.cammino.gestionecomunita.ui.incontri

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.database.item.IncontroComunita

class IncontriViewModel(application: Application) : AndroidViewModel(application) {

    var removedIncontro: Incontro? = null
    var selectedIncontroId: Long = 0

    var itemsResult: LiveData<List<IncontroComunita>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.incontroDao().liveByDate
    }

}