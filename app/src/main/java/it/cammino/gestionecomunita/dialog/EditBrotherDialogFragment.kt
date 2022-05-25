package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.capitalize
import it.cammino.gestionecomunita.util.validateMandatoryField
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class EditBrotherDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })


    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {
        val mView = layoutInflater.inflate(R.layout.fratello_edit_dialog, container, false)

        val inputNome = mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText
        inputNome?.setText(mBuilder.mNomePrefill ?: "")
        val inputCognome = mView.findViewById<TextInputLayout>(R.id.cognome_text_field).editText
        inputCognome?.setText(mBuilder.mCognomePrefill ?: "")
        val inputStatoCivile =
            mView.findViewById<TextInputLayout>(R.id.stato_civile_text_field).editText
        inputStatoCivile?.setText(mBuilder.mStatoCivilePrefill ?: "")
        val inputNumFigli =
            mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field).editText
        inputNumFigli?.setText(mBuilder.mNumeroFigliPrefill.toString())
        val inputDataInizio =
            mView.findViewById<TextInputLayout>(R.id.data_inizio_cammino_text_field).editText
        mBuilder.mDataInizioCamminoPrefill?.let {
            inputDataInizio?.setText(Utility.getStringFromDate(mView.context, it))
        }

        inputDataInizio?.inputType = InputType.TYPE_NULL
        inputDataInizio?.setOnKeyListener(null)
        inputDataInizio?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (inputDataInizio.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    mView.context,
                                    inputDataInizio.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_ultima_visita)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "dataVisitaTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    inputDataInizio.setText(
                        Utility.getStringFromDate(
                            mView.context,
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        return mView
    }

    protected fun fillreturnText(mView: View) {
        viewModel.nomeText =
            mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.text.toString()
        viewModel.cognomeText =
            mView.findViewById<TextInputLayout>(R.id.cognome_text_field).editText?.text.toString()
        viewModel.statoCivileText =
            mView.findViewById<TextInputLayout>(R.id.stato_civile_text_field).editText?.text.toString()
        mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank() && txt.toString().isDigitsOnly())
                viewModel.numFigli = txt.toString().toInt()
        }
        mView.findViewById<TextInputLayout>(R.id.data_inizio_cammino_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataInizioCammino =
                    Utility.getDateFromString(mView.context, txt.toString())
        }
    }

    protected fun validateForm(mView: View): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(mView.findViewById(R.id.nome_text_field)))
            valid = false

        val inputNome = mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field)
        inputNome.editText?.let {
            if (!it.text.isNullOrEmpty() && !it.text.isDigitsOnly()) {
                inputNome.error = getString(R.string.invalid_number)
                valid = false
            } else
                inputNome.error = null
        }

        val dataInizio = mView.findViewById<TextInputLayout>(R.id.data_inizio_cammino_text_field)
        dataInizio.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                dataInizio.error = getString(R.string.invalid_date)
                valid = false
            } else
                dataInizio.error = null
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
        var mNomePrefill: CharSequence? = null
        var mCognomePrefill: CharSequence? = null
        var mStatoCivilePrefill: CharSequence? = null
        var mNumeroFigliPrefill: Int = 0
        var mDataInizioCamminoPrefill: Date? = null
        var mEditMode: Boolean = false

        fun nomePrefill(text: String): Builder {
            mNomePrefill = text
            return this
        }

        fun cognomePrefill(text: String): Builder {
            mCognomePrefill = text
            return this
        }

        fun statoCivilePrefill(text: String): Builder {
            mStatoCivilePrefill = text
            return this
        }

        fun numeroFigliPrefill(text: Int): Builder {
            mNumeroFigliPrefill = text
            return this
        }

        fun dataInizioCamminoPrefill(text: Date?): Builder {
            mDataInizioCamminoPrefill = text
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
        var nomeText: String = ""
        var cognomeText: String = ""
        var statoCivileText: String = ""
        var numFigli: Int = 0
        var dataInizioCammino: Date? = null
        var handled = true
        val state = MutableLiveData<DialogState<EditBrotherDialogFragment>>()
    }

}
