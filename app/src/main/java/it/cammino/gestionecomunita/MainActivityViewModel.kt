package it.cammino.gestionecomunita

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Promemoria

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var itemsResult: LiveData<List<Promemoria>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.promemoriaDao().liveAll
    }

}
