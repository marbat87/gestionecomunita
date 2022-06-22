package it.cammino.gestionecomunita.ui.seminario.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Seminario

class SeminarioListViewModel(application: Application) : AndroidViewModel(application) {

    var seminarioLiveList: LiveData<List<Seminario>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        seminarioLiveList = mDb.seminarioDao().liveAll
    }

}
