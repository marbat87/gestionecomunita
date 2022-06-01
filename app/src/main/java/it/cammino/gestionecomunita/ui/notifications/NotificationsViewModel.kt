package it.cammino.gestionecomunita.ui.notifications

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Promemoria
import it.cammino.gestionecomunita.database.item.PromemoriaComunita

class NotificationsViewModel(application: Application) : AndroidViewModel(application) {

    var removedPromemoria: Promemoria? = null

    var itemsResult: LiveData<List<PromemoriaComunita>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.promemoriaDao().liveAll
    }

}