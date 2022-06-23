package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.capitalize
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class ViewVisitaDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })

    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {

        val mView = layoutInflater.inflate(R.layout.visita_view_dialog, container, false)

        mBuilder.mDataIncontroPrefill?.let {
            mView.findViewById<TextView>(R.id.data_visita_text).text =
                Utility.getStringFromDate(mView.context, it)
        }

        mView.findViewById<TextView>(R.id.formatori_text).text =
            mBuilder.mFormatoriPrefill.toString()
        mView.findViewById<TextView>(R.id.seminaristi_text).text =
            mBuilder.mSeminaristiPrefill.toString()
        mView.findViewById<TextView>(R.id.note_text).text = mBuilder.mNotePrefill.toString()

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
        var mFormatoriPrefill: CharSequence = StringUtils.EMPTY_STRING
        var mSeminaristiPrefill: CharSequence = StringUtils.EMPTY_STRING
        var mNotePrefill: CharSequence = StringUtils.EMPTY_STRING
        var mDataIncontroPrefill: Date? = null

        fun dataIncontroPrefill(text: Date?): Builder {
            mDataIncontroPrefill = text
            return this
        }

        fun formatoriPrefill(text: String): Builder {
            mFormatoriPrefill = text
            return this
        }

        fun seminaristiPrefill(text: String): Builder {
            mSeminaristiPrefill = text
            return this
        }

        fun notePrefill(text: String): Builder {
            mNotePrefill = text
            return this
        }

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

    class DialogViewModel : ViewModel() {
        var mTag: String = ""
        var formatoriText: String = ""
        var seminaristiText: String = ""
        var dataIncontro: Date? = null
        var noteText: String = ""
        var handled = true
        val state = MutableLiveData<DialogState<ViewVisitaDialogFragment>>()
    }

}
