package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.*
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class EditVisitaDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })

    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {

        val mView = layoutInflater.inflate(R.layout.visita_edit_dialog, container, false)

        mView.findViewById<TextInputLayout>(R.id.formatori_text_field).editText?.setText(mBuilder.mFormatoriPrefill.toString())
        mView.findViewById<TextInputLayout>(R.id.seminaristi_text_field).editText?.setText(mBuilder.mSeminaristiPrefill.toString())
        mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.setText(mBuilder.mNotePrefill.toString())

        val inputDataVisita =
            mView.findViewById<TextInputLayout>(R.id.data_visita_text_field).editText
        mBuilder.mDataIncontroPrefill?.let {
            inputDataVisita?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataVisita.setupDatePicker(
            requireActivity(),
            "inputDataVisita",
            R.string.data
        )

        return mView
    }

    protected fun fillreturnText(mView: View) {
        viewModel.formatoriText =
            mView.findViewById<TextInputLayout>(R.id.formatori_text_field).editText?.text.toString()
                .trim()
        viewModel.seminaristiText =
            mView.findViewById<TextInputLayout>(R.id.seminaristi_text_field).editText?.text.toString()
                .trim()

        viewModel.noteText =
            mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.text.toString()
                .trim()

        mView.findViewById<TextInputLayout>(R.id.data_visita_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataIncontro =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

    }

    protected fun validateForm(mView: View, showError: Boolean = true): Boolean {
        val dataIncontro = mView.findViewById<TextInputLayout>(R.id.data_visita_text_field)
        dataIncontro.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                dataIncontro.error = getString(R.string.invalid_date)
                return false
            } else
                if (!requireContext().validateMandatoryField(
                        dataIncontro,
                        showError
                    )
                )
                    return false
        }

        return true
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
        var mEditMode: Boolean = false

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

        fun setEditMode(editMode: Boolean): Builder {
            mEditMode = editMode
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
        val state = MutableLiveData<DialogState<EditVisitaDialogFragment>>()
    }

}
