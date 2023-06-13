package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.IncontroVocazionale
import it.cammino.gestionecomunita.util.*
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class EditVocazioneMeetingDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })


    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {

        val mView =
            layoutInflater.inflate(R.layout.incontro_vocazione_edit_dialog, container, false)

        mView.findViewById<AutoCompleteTextView>(R.id.tipo_autcomplete).setText(
            requireContext().resources.getTextArray(R.array.tipo_incontro_entries)[if (mBuilder.mTipoPrefill == IncontroVocazionale.Tipo.INCONTRO) 0 else 1],
            false
        )

        mView.findViewById<AutoCompleteTextView>(R.id.tipo_autcomplete)
            .setOnItemClickListener { _, _, i, _ ->
                when (i) {
                    0 -> viewModel.tipo = IncontroVocazionale.Tipo.INCONTRO
                    1 -> viewModel.tipo = IncontroVocazionale.Tipo.CONVIVENZA
                    else -> viewModel.tipo = IncontroVocazionale.Tipo.INCONTRO
                }
            }

        mView.findViewById<TextInputLayout>(R.id.luogo_text_field).editText?.setText(mBuilder.mLuogoPrefill)
        mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.setText(mBuilder.mNotePrefill.toString())

        val inputDataIncontro =
            mView.findViewById<TextInputLayout>(R.id.data_incontro_text_field).editText
        mBuilder.mDataIncontroPrefill?.let {
            inputDataIncontro?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataIncontro.setupDatePicker(
            requireActivity(),
            "inputDataIncontro",
            R.string.data
        )

        return mView
    }

    protected fun fillreturnText(mView: View) {
        viewModel.luogoText =
            mView.findViewById<TextInputLayout>(R.id.luogo_text_field).editText?.text.toString()
                .trim()

        viewModel.noteText =
            mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.text.toString()
                .trim()

        mView.findViewById<TextInputLayout>(R.id.data_incontro_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataIncontro =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

    }

    protected fun validateForm(mView: View, showError: Boolean = true): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(
                mView.findViewById(R.id.tipo_text_field),
                showError
            )
        )
            valid = false

        val dataIncontro = mView.findViewById<TextInputLayout>(R.id.data_incontro_text_field)
        dataIncontro.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                dataIncontro.error = getString(R.string.invalid_date)
                valid = false
            } else
                if (!requireContext().validateMandatoryField(
                        dataIncontro,
                        showError
                    )
                )
                    valid = false
        }

        return valid
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
        var mTipoPrefill: IncontroVocazionale.Tipo = IncontroVocazionale.Tipo.INCONTRO
        var mDataIncontroPrefill: Date? = null
        var mLuogoPrefill: CharSequence = StringUtils.EMPTY_STRING
        var mNotePrefill: CharSequence = StringUtils.EMPTY_STRING
        var mEditMode: Boolean = false

        fun tipoPrefill(text: IncontroVocazionale.Tipo): Builder {
            mTipoPrefill = text
            return this
        }

        fun notePrefill(text: String): Builder {
            mNotePrefill = text
            return this
        }

        fun luogoPrefill(text: String): Builder {
            mLuogoPrefill = text
            return this
        }

        fun dataIncontroPrefill(text: Date?): Builder {
            mDataIncontroPrefill = text
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
        var tipo: IncontroVocazionale.Tipo = IncontroVocazionale.Tipo.INCONTRO
        var dataIncontro: Date? = null
        var luogoText: String = ""
        var noteText: String = ""
        var handled = true
        val state = MutableLiveData<DialogState<EditVocazioneMeetingDialogFragment>>()
        val dataFilled = MutableLiveData<Boolean>()
    }

}
