package it.cammino.gestionecomunita.dialog


import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista
import it.cammino.gestionecomunita.item.ComunitaSeminaristaViewListItem
import it.cammino.gestionecomunita.item.comunitaSeminaristaViewListItem
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.capitalize
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class ViewSeminaristaDialogFragment : DialogFragment() {

    private val mAdapterComunita: FastItemAdapter<ComunitaSeminaristaViewListItem> =
        FastItemAdapter()

    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {
        val mView = layoutInflater.inflate(R.layout.seminarista_view_dialog, container, false)

        mView.findViewById<RecyclerView>(R.id.comunita_recycler).adapter = mAdapterComunita

        mView.findViewById<TextView>(R.id.nome_text).text =
            mBuilder.nomePrefill.ifEmpty { StringUtils.DASH }
        mBuilder.dataNascitaPrefill?.let {
            mView.findViewById<TextView>(R.id.data_nascita_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.data_nascita_text).text = StringUtils.DASH
        }
        mView.findViewById<TextView>(R.id.nazione_text).text =
            mBuilder.nazionePrefill.ifEmpty { StringUtils.DASH }

        mView.findViewById<TextView>(R.id.comunita_provenienza_text).text =
            mBuilder.comunitaProvenienzaPrefill.ifEmpty { StringUtils.DASH }
        mView.findViewById<TextView>(R.id.catechisti_text).text =
            mBuilder.catechistiProvenienzaPrefill.ifEmpty { StringUtils.DASH }
        if (mBuilder.idTappaPrefill != -1)
            mView.findViewById<TextView>(R.id.tappa_text).text =
                resources.getTextArray(R.array.passaggi_entries)[mBuilder.idTappaPrefill]
        else
            mView.findViewById<TextView>(R.id.tappa_text).text = StringUtils.DASH

        mBuilder.dataEntrataPrefill?.let {
            mView.findViewById<TextView>(R.id.data_ingresso_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.data_ingresso_text).text = StringUtils.DASH
        }
        mBuilder.dataUscitaPrefill?.let {
            mView.findViewById<TextView>(R.id.data_uscita_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.data_uscita_text).text = StringUtils.DASH
        }
        mView.findViewById<TextView>(R.id.motivo_text).text =
            mBuilder.motivoPrefill.ifEmpty { StringUtils.DASH }

        mAdapterComunita.set(mBuilder.comunitaAssegnazione
            .map {
                comunitaSeminaristaViewListItem {
                    comunitaAssegnazione = it.comunitaAssegnazione
                    dataAssegnazione = it.dataAssegnazione
                    idTappaCammino = it.idTappaAssegnazione
                }
            })

        mBuilder.dataAdmissioPrefill?.let {
            mView.findViewById<TextView>(R.id.admissio_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.admissio_text).text = StringUtils.DASH
        }
        mBuilder.dataAccolitatoPrefill?.let {
            mView.findViewById<TextView>(R.id.accolitato_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.accolitato_text).text = StringUtils.DASH
        }
        mBuilder.dataLettoratoPrefill?.let {
            mView.findViewById<TextView>(R.id.lettorato_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.lettorato_text).text = StringUtils.DASH
        }
        mBuilder.dataDiaconatoPrefill?.let {
            mView.findViewById<TextView>(R.id.diaconato_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.diaconato_text).text = StringUtils.DASH
        }
        mBuilder.dataPresbiteratoPrefill?.let {
            mView.findViewById<TextView>(R.id.presbiterato_text).text =
                Utility.getStringFromDate(mView.context, it)
        } ?: run {
            mView.findViewById<TextView>(R.id.presbiterato_text).text = StringUtils.DASH
        }
        mView.findViewById<TextView>(R.id.note_text).text =
            mBuilder.notePrefill.toString().ifEmpty { StringUtils.DASH }

        return mView
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

        var nomePrefill: CharSequence = StringUtils.EMPTY_STRING
        var dataNascitaPrefill: Date? = null
        var nazionePrefill: CharSequence = StringUtils.EMPTY_STRING

        var comunitaProvenienzaPrefill: CharSequence = StringUtils.EMPTY_STRING
        var catechistiProvenienzaPrefill: CharSequence = StringUtils.EMPTY_STRING
        var idTappaPrefill: Int = 0

        var dataEntrataPrefill: Date? = null
        var dataUscitaPrefill: Date? = null
        var motivoPrefill: CharSequence = StringUtils.EMPTY_STRING

        var comunitaAssegnazione: List<ComunitaSeminarista> = ArrayList()

        var dataAdmissioPrefill: Date? = null
        var dataAccolitatoPrefill: Date? = null
        var dataLettoratoPrefill: Date? = null
        var dataDiaconatoPrefill: Date? = null
        var dataPresbiteratoPrefill: Date? = null

        var notePrefill: CharSequence = StringUtils.EMPTY_STRING

        var editMode: Boolean = false

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
            return this
        }

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

    }

}
