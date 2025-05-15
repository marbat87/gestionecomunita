package it.cammino.gestionecomunita

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.database.entity.Promemoria

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    var backupRestoreState = MutableLiveData(BakupRestoreState.NONE)
    var httpRequestState = MutableLiveData(ClientState.STARTED)
    var loginState = MutableLiveData(LOGIN_STATE_STARTED)
    var profileAction = ProfileAction.NONE

    var livePromemoria: LiveData<List<Promemoria>>? = null
        private set
    var liveIncontri: LiveData<List<Incontro>>? = null
        private set

    var backupCode: String = ""
    var selectedVocazioniIndex = 0

    init {
        val mDb = ComunitaDatabase.getInstance(getApplication())
        livePromemoria = mDb.promemoriaDao().liveAll
        liveIncontri = mDb.incontroDao().liveAll
    }

    enum class ProfileAction {
        BACKUP_OLD_CODE,
        BACKUP_NEW_CODE,
        RESTORE_OLD_CODE,
        RESTORE_NEW_CODE,
        NONE
    }

    enum class ClientState {
        STARTED,
        COMPLETED
    }

    enum class BakupRestoreState {
        NONE,
        BACKUP_STARTED,
        BACKUP_COMPLETED,
        RESTORE_STARTED,
        RESTORE_COMPLETED

    }

    var sub: String = ""

    companion object {
        const val LOGIN_STATE_STARTED = "LoginStateStarted"
        const val LOGIN_STATE_OK = "LoginStateOk"
        const val LOGIN_STATE_OK_SILENT = "LoginStateOkSilent"
    }

}
