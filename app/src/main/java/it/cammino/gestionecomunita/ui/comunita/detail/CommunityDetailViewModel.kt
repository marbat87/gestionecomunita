package it.cammino.gestionecomunita.ui.comunita.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.ComunitaDatabase

class CommunityDetailViewModel(application: Application) :
    AndroidViewModel(application) {

    var listId: Int = -1
    val editMode = MutableLiveData(true)
    var createMode = true
    var comunita: Comunita = Comunita()
    var selectedTabIndex = 0

    var itemsResult: LiveData<Comunita>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveById(listId)
    }

}
