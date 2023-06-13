package it.cammino.gestionecomunita.ui.vocazione.incontri

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.IncontroVocazionale

class IncontriVocazioneViewModel(application: Application) : AndroidViewModel(application) {

    var removedIncontro: IncontroVocazionale? = null
    var selectedIncontroId: Long = 0

    var itemsResult: LiveData<List<IncontroVocazionale>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.incontroVocazionaleDao().liveAll
    }

}