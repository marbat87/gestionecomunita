package it.cammino.gestionecomunita.ui.comunita.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita

class CommunityListViewModel(application: Application) : AndroidViewModel(application) {

    val itemCLickedState = MutableLiveData(ItemClickState.UNCLICKED)
    var clickedId: Long = 0

    var itemsResult: LiveData<List<Comunita>>? = null
        private set
    var indexType: IndexType = IndexType.TUTTE

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveAll
    }

    enum class IndexType {

        TUTTE,
        VISITATE_OLTRE_ANNO,
        TAPPA,
        DIOCESI

    }

    enum class ItemClickState {

        CLICKED,
        UNCLICKED

    }

    companion object {
        const val INDEX_TYPE = "index_type"
    }

}
