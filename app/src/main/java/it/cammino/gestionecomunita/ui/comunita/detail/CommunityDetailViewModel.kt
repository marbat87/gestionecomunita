package it.cammino.gestionecomunita.ui.comunita.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.ComunitaFratello
import it.cammino.gestionecomunita.item.ExpandableBrotherItem

class CommunityDetailViewModel(application: Application) :
    AndroidViewModel(application) {

    var listId: Int = -1
    val editMode = MutableLiveData(true)
    var createMode = true
    var comunita: Comunita = Comunita()
    var comunitaFratello: ComunitaFratello? = null
    var selectedTabIndex = 0
    var selectedFratello = 0
    var elementi: ArrayList<ExpandableBrotherItem>? = null

    var itemsResult: LiveData<Comunita>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveById(listId)
    }

}
