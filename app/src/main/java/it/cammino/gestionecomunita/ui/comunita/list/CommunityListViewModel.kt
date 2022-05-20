package it.cammino.gestionecomunita.ui.comunita.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.item.CommunityListItem
import it.cammino.gestionecomunita.item.communityListItem

class CommunityListViewModel(application: Application) :
    AndroidViewModel(application) {

    var itemsResult: LiveData<List<CommunityListItem>>? = null
        private set

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveAll.map { comunita ->
            val newList = ArrayList<CommunityListItem>()
            comunita.forEach {
                newList.add(
                    communityListItem {
                        setComunita = "${it.numero} - ${it.parrocchia}"
                        setResponsabile = it.responsabile
                        id = it.id
                    }
                )
            }
            newList
        }
    }

}
