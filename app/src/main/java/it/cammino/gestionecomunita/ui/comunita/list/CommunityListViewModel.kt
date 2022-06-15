package it.cammino.gestionecomunita.ui.comunita.list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita

class CommunityListViewModel(application: Application) : AndroidViewModel(application) {

    var itemsResult: LiveData<List<Comunita>>? = null
        private set

    var selectedSort: Int = R.id.sort_alphabet

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        itemsResult = mDb.comunitaDao().liveAll
    }


    var indexType: IndexType = IndexType.TUTTE

    enum class IndexType {
        TUTTE,
        VISITATE_OLTRE_ANNO,
        TAPPA,
        DIOCESI
    }

    companion object {
        const val INDEX_TYPE = "index_type"
    }

}
