package it.cammino.gestionecomunita

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.database.entity.Promemoria

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var livePromemoria: LiveData<List<Promemoria>>? = null
        private set
    var liveIncontri: LiveData<List<Incontro>>? = null
        private set

    var backupCode: String = ""
    var showSnackbar = true
    var selectedVocazioniIndex = 0

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        livePromemoria = mDb.promemoriaDao().liveAll
        liveIncontri = mDb.incontroDao().liveAll
    }

}
