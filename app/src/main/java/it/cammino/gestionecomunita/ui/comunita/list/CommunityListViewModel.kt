package it.cammino.gestionecomunita.ui.comunita.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita

class CommunityListViewModel(application: Application) : AndroidViewModel(application) {

    var itemsResult: LiveData<List<Comunita>>? = null
        private set
    var onlyNotVisitedForOneYear: Boolean = false

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveAll
    }

}
