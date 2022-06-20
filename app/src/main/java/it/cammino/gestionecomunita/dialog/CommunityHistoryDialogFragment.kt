package it.cammino.gestionecomunita.dialog


import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.item.CommunityHistoryListItem
import it.cammino.gestionecomunita.item.communityHistoryListItem
import it.cammino.gestionecomunita.util.capitalize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable

@Suppress("unused")
open class CommunityHistoryDialogFragment : DialogFragment() {

    private val mAdapter: FastItemAdapter<CommunityHistoryListItem> = FastItemAdapter()


    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {
        val mView = layoutInflater.inflate(R.layout.community_history_dialog, container, false)

        val recycler = mView.findViewById<RecyclerView>(R.id.history_recycler)

        recycler.adapter = mAdapter

        lifecycleScope.launch { getData(mView.context, mBuilder.idComunita) }

        return mView
    }

    private suspend fun getData(ctx: Context, idComunita: Long) {
        val passaggiList = ArrayList<CommunityHistoryListItem>()
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(ctx).passaggioDao().getComunitaWithPassaggi(idComunita)
                ?.let {
                    it.passaggi.forEach { passaggio ->
                        passaggiList.add(
                            communityHistoryListItem {
                                indicePassaggio = passaggio.passaggio
                                dataPassaggio = passaggio.data
                            })
                    }
                }
        }
        mAdapter.set(passaggiList)
    }

    fun cancel() {
        dialog?.cancel()
    }

    class Builder(context: AppCompatActivity, val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context

        var mPositiveButton: CharSequence? = null
        var mNegativeButton: CharSequence? = null
        var mCanceable = false
        var idComunita: Long = 0

        fun idComunita(id: Long): Builder {
            idComunita = id
            return this
        }

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
            return this
        }

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

    }

}
