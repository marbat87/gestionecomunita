package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
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
import it.cammino.gestionecomunita.util.Utility.EMPTY_STRING
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

        mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.setText(mBuilder.mNomePrefill)
        mView.findViewById<TextInputLayout>(R.id.cognome_text_field).editText?.setText(mBuilder.mCognomePrefill)

        mView.findViewById<TextInputLayout>(R.id.stato_civile_text_field).editText?.setText(mBuilder.mStatoCivilePrefill)
        mView.findViewById<TextInputLayout>(R.id.coniuge_text_field).editText?.setText(mBuilder.mConiugePrefill)
        mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field).editText?.setText(mBuilder.mNumeroFigliPrefill.toString())

        val inputDataNascita =
            mView.findViewById<TextInputLayout>(R.id.data_nascita_text_field).editText
        mBuilder.mDataNascitaPrefill?.let {
            inputDataNascita?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataNascita?.inputType = InputType.TYPE_NULL
        inputDataNascita?.setOnKeyListener(null)
        inputDataNascita?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (inputDataNascita.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    mView.context,
                                    inputDataNascita.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_ultima_visita)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "datanascitaTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    inputDataNascita.setText(
                        Utility.getStringFromDate(
                            mView.context,
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        mView.findViewById<TextInputLayout>(R.id.carisma_text_field).editText?.setText(mBuilder.mCarismaPrefill.toString())
        mView.findViewById<TextInputLayout>(R.id.tribu_text_field).editText?.setText(mBuilder.mTribuPrefill.toString())

        mView.findViewById<TextInputLayout>(R.id.comunita_origine_text_field).editText?.setText(
            mBuilder.mComunitaOriginePrefill.toString()
        )
        val inputDataArrivo =
            mView.findViewById<TextInputLayout>(R.id.data_arrivo_text_field).editText
        mBuilder.mDataArrivoPrefill?.let {
            inputDataArrivo?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataArrivo?.inputType = InputType.TYPE_NULL
        inputDataArrivo?.setOnKeyListener(null)
        inputDataArrivo?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (inputDataArrivo.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    mView.context,
                                    inputDataArrivo.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_ultima_visita)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "datanascitaTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    inputDataArrivo.setText(
                        Utility.getStringFromDate(
                            mView.context,
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        val inputStato = mView.findViewById<AutoCompleteTextView>(R.id.stato_autcomplete)
        inputStato.setText(
            requireContext().resources.getStringArray(R.array.stati)[mBuilder.mStatoPrefill],
            false
        )
        viewModel.statoInt = mBuilder.mStatoPrefill
        mView.findViewById<AutoCompleteTextView>(R.id.stato_autcomplete)
            .setOnItemClickListener { _, _, i, _ ->
                viewModel.statoInt = i
            }

        mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.setText(mBuilder.mNotePrefill.toString())

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
                .trim()
        viewModel.cognomeText =
            mView.findViewById<TextInputLayout>(R.id.cognome_text_field).editText?.text.toString()
                .trim()

        viewModel.statoCivileText =
            mView.findViewById<TextInputLayout>(R.id.stato_civile_text_field).editText?.text.toString()
                .trim()
        viewModel.coniugeText =
            mView.findViewById<TextInputLayout>(R.id.coniuge_text_field).editText?.text.toString()
                .trim()
        mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank() && txt.toString().trim().isDigitsOnly())
                viewModel.numFigli = txt.toString().trim().toInt()
        }

        viewModel.tribuText =
            mView.findViewById<TextInputLayout>(R.id.tribu_text_field).editText?.text.toString()
                .trim()

        mView.findViewById<TextInputLayout>(R.id.data_nascita_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.annoNascita =
                    Utility.getDateFromString(mView.context, txt.toString())
        }
        viewModel.carismaText =
            mView.findViewById<TextInputLayout>(R.id.carisma_text_field).editText?.text.toString()
                .trim()

        viewModel.comunitaOrigineText =
            mView.findViewById<TextInputLayout>(R.id.comunita_origine_text_field).editText?.text.toString()
                .trim()
        mView.findViewById<TextInputLayout>(R.id.data_arrivo_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataArrivo =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        viewModel.statoText =
            mView.findViewById<TextInputLayout>(R.id.stato_text_field).editText?.text.toString()

        viewModel.noteText =
            mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.text.toString()
                .trim()

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
        var mNomePrefill: CharSequence = EMPTY_STRING
        var mCognomePrefill: CharSequence = EMPTY_STRING
        var mStatoCivilePrefill: CharSequence = EMPTY_STRING
        var mConiugePrefill: CharSequence = EMPTY_STRING
        var mNumeroFigliPrefill: Int = 0
        var mDataNascitaPrefill: Date? = null
        var mCarismaPrefill: CharSequence = EMPTY_STRING
        var mTribuPrefill: CharSequence = EMPTY_STRING
        var mComunitaOriginePrefill: CharSequence = EMPTY_STRING
        var mDataArrivoPrefill: Date? = null
        var mStatoPrefill: Int = 0
        var mNotePrefill: CharSequence = EMPTY_STRING
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

        fun setConiugePrefill(text: String): Builder {
            mConiugePrefill = text
            return this
        }

        fun numeroFigliPrefill(text: Int): Builder {
            mNumeroFigliPrefill = text
            return this
        }

        fun setDataNascitaPrefill(text: Date?): Builder {
            mDataNascitaPrefill = text
            return this
        }

        fun setCarismaPrefill(text: String): Builder {
            mCarismaPrefill = text
            return this
        }

        fun setTribuPrefill(text: String): Builder {
            mTribuPrefill = text
            return this
        }

        fun setComunitaOriginePrefill(text: String): Builder {
            mComunitaOriginePrefill = text
            return this
        }

        fun setDataArrivoPrefill(text: Date?): Builder {
            mDataArrivoPrefill = text
            return this
        }

        fun setStatoPrefill(text: Int): Builder {
            mStatoPrefill = text
            return this
        }

        fun setNotePrefill(text: String): Builder {
            mNotePrefill = text
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
        var coniugeText: String = ""
        var tribuText: String = ""
        var annoNascita: Date? = null
        var carismaText: String = ""
        var comunitaOrigineText: String = ""
        var dataArrivo: Date? = null
        var statoInt: Int = 0
        var statoText: String = ""
        var noteText: String = ""
        var numFigli: Int = 0
        var dataInizioCammino: Date? = null
        var handled = true
        val state = MutableLiveData<DialogState<EditBrotherDialogFragment>>()
    }

}
