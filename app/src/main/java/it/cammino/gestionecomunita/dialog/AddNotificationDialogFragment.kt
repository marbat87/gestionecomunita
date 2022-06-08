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
import it.cammino.gestionecomunita.util.capitalize
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.Serializable
import java.sql.Date


@Suppress("unused")
open class AddNotificationDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })


    @SuppressLint("ClickableViewAccessibility")
    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {
        val mView = layoutInflater.inflate(R.layout.add_notification_dialog, container, false)

        lifecycleScope.launch { fillComunitaList(mView, mBuilder) }

        if (!mBuilder.mFreeMode)
            mView.findViewById<TextInputEditText>(R.id.comunita_text_field)
                .setText(mBuilder.mIdComunitaPrefill.toString())

        val inputData =
            mView.findViewById<TextInputLayout>(R.id.data_text_field).editText

        inputData?.inputType = InputType.TYPE_NULL
        inputData?.setOnKeyListener(null)
        inputData?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (inputData.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    mView.context,
                                    inputData.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "inputDataPicker")
                picker.addOnPositiveButtonClickListener {
                    inputData.setText(
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
        viewModel.idComunita =
            mView.findViewById<TextInputEditText>(R.id.comunita_text_field).text.toString()
                .toLong()

        mView.findViewById<TextInputLayout>(R.id.data_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.data =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        viewModel.descrizioneText =
            mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.text.toString()
                .trim()

    }

    protected fun validateForm(mView: View, showError: Boolean = true): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(
                mView.findViewById(R.id.comunita_auto_text),
                showError
            )
        )
            valid = false

        val dataInizio = mView.findViewById<TextInputLayout>(R.id.data_text_field)
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

        if (!requireContext().validateMandatoryField(
                mView.findViewById(R.id.note_text_field),
                showError
            )
        )
            valid = false

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
        var mIdComunitaPrefill: Long = 0
        var mFreeMode: Boolean = false

        fun idComunitaPrefill(id: Long): Builder {
            mIdComunitaPrefill = id
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

        fun setFreeMode(freeMode: Boolean): Builder {
            mFreeMode = freeMode
            return this
        }

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

    }

    private suspend fun fillComunitaList(view: View, mBuilder: Builder) {
        viewModel.dataFilled.value = false
        lateinit var comunitaList: List<Comunita>
        lateinit var comunita: Comunita
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            comunitaList =
                ComunitaDatabase.getInstance(requireContext()).comunitaDao().allByName
            if (!mBuilder.mFreeMode)
                comunita = ComunitaDatabase.getInstance(requireContext()).comunitaDao()
                    .getById(mBuilder.mIdComunitaPrefill) ?: Comunita()
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

        if (!mBuilder.mFreeMode) {
            textView.setText(
                resources.getString(
                    R.string.comunita_item_name,
                    comunita.numero,
                    comunita.parrocchia
                ), false
            )
        }
        view.findViewById<TextInputLayout>(R.id.comunita_auto_text).isEnabled = mBuilder.mFreeMode

        textView
            .setOnItemClickListener { _, _, i, _ ->
                view.findViewById<TextInputEditText>(R.id.comunita_text_field)
                    .setText(comunitaList[i].id.toString())
            }
        viewModel.dataFilled.value = true
    }

    class DialogViewModel : ViewModel() {
        var mTag: String = ""

        var idComunita: Long = 0
        var data: Date? = null
        var descrizioneText: String = ""

        var handled = true
        val state = MutableLiveData<DialogState<AddNotificationDialogFragment>>()
        val dataFilled = MutableLiveData<Boolean>()
    }

}
