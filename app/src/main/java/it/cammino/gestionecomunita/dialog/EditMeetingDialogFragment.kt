package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.Utility.EMPTY_STRING
import it.cammino.gestionecomunita.util.capitalize
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class EditMeetingDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })


    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {

        val mView = layoutInflater.inflate(R.layout.incontro_edit_dialog, container, false)

        lifecycleScope.launch { fillComunitaList(mView, mBuilder) }

        mView.findViewById<TextInputEditText>(R.id.comunita_text_field)
            .setText(mBuilder.mComunitaPrefill.toString())

        mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.setText(mBuilder.mNomePrefill)
        mView.findViewById<TextInputLayout>(R.id.cognome_text_field).editText?.setText(mBuilder.mCognomePrefill)

        mView.findViewById<TextInputLayout>(R.id.luogo_text_field).editText?.setText(mBuilder.mLuogoPrefill)
        mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.setText(mBuilder.mNotePrefill.toString())

        val inputDataIncontro =
            mView.findViewById<TextInputLayout>(R.id.data_incontro_text_field).editText
        mBuilder.mDataIncontroPrefill?.let {
            inputDataIncontro?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataIncontro?.inputType = InputType.TYPE_NULL
        inputDataIncontro?.setOnKeyListener(null)
        inputDataIncontro?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (inputDataIncontro.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    mView.context,
                                    inputDataIncontro.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "inputDataIncontroPicker")
                picker.addOnPositiveButtonClickListener {
                    inputDataIncontro.setText(
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

        viewModel.idComunita =
            mView.findViewById<TextInputEditText>(R.id.comunita_text_field).text.toString()
                .toLong()

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

    private suspend fun fillComunitaList(view: View, mBuilder: Builder) {
        viewModel.dataFilled.value = false
        lateinit var comunitaList: List<Comunita>
        lateinit var comunita: Comunita
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            comunitaList =
                ComunitaDatabase.getInstance(requireContext()).comunitaDao().allByName
            if (mBuilder.mComunitaPrefill != (-1).toLong())
                comunita = ComunitaDatabase.getInstance(requireContext()).comunitaDao()
                    .getById(mBuilder.mComunitaPrefill) ?: Comunita()
        }
        val comunitaStrings = comunitaList
            .map {
                resources.getString(R.string.comunita_item_name, it.numero, it.parrocchia)
            }
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, comunitaStrings
        )
        val textView = view.findViewById<AutoCompleteTextView>(R.id.comunita_autcomplete)
        textView.setAdapter(adapter)

        if (mBuilder.mComunitaPrefill != (-1).toLong()) {
            textView.setText(
                resources.getString(
                    R.string.comunita_item_name,
                    comunita.numero,
                    comunita.parrocchia
                ), false
            )
        }

        textView
            .setOnItemClickListener { _, _, i, _ ->
                view.findViewById<TextInputEditText>(R.id.comunita_text_field)
                    .setText(comunitaList[i].id.toString())
            }
        viewModel.dataFilled.value = true
    }

    protected fun validateForm(mView: View, showError: Boolean = true): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(
                mView.findViewById(R.id.nome_text_field),
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
        var mNomePrefill: CharSequence = EMPTY_STRING
        var mCognomePrefill: CharSequence = EMPTY_STRING
        var mComunitaPrefill: Long = -1
        var mDataIncontroPrefill: Date? = null
        var mLuogoPrefill: CharSequence = EMPTY_STRING
        var mNotePrefill: CharSequence = EMPTY_STRING
        var mEditMode: Boolean = false

        fun nomePrefill(text: String): Builder {
            mNomePrefill = text
            return this
        }

        fun cognomePrefill(text: String): Builder {
            mCognomePrefill = text
            return this
        }

        fun notePrefill(text: String): Builder {
            mNotePrefill = text
            return this
        }

        fun comunitaPrefill(text: Long): Builder {
            mComunitaPrefill = text
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
        var nomeText: String = ""
        var cognomeText: String = ""
        var idComunita: Long = 0
        var dataIncontro: Date? = null
        var luogoText: String = ""
        var noteText: String = ""
        var handled = true
        val state = MutableLiveData<DialogState<EditMeetingDialogFragment>>()
        val dataFilled = MutableLiveData<Boolean>()
    }

}
