package it.cammino.gestionecomunita.dialog


import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.Button
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista
import it.cammino.gestionecomunita.item.ComunitaSeminaristaListItem
import it.cammino.gestionecomunita.item.comunitaSeminaristaListItem
import it.cammino.gestionecomunita.util.*
import java.io.Serializable
import java.sql.Date

@Suppress("unused")
open class EditSeminaristaDialogFragment : DialogFragment() {

    protected val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val mAdapterComunita: FastItemAdapter<ComunitaSeminaristaListItem> = FastItemAdapter()

    protected fun prefill(mBuilder: Builder, container: ViewGroup?): View {
        val mView = layoutInflater.inflate(R.layout.seminarista_edit_dialog, container, false)

        mView.findViewById<RecyclerView>(R.id.comunita_recycler).adapter = mAdapterComunita
        mView.findViewById<Button>(R.id.add_comunita).setOnClickListener {
            mAdapterComunita.add(0, ComunitaSeminaristaListItem())
        }
        mAdapterComunita.addEventHook(cancellaComunitaHook)

        mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.setText(mBuilder.nomePrefill)
        val inputDataNascita =
            mView.findViewById<TextInputLayout>(R.id.data_nascita_text_field).editText
        mBuilder.dataNascitaPrefill?.let {
            inputDataNascita?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataNascita.setupDatePicker(
            requireActivity(),
            "inputDataNascita",
            R.string.data_nascita
        )
        mView.findViewById<TextInputLayout>(R.id.nazione_text_field).editText?.setText(mBuilder.nazionePrefill)

        mView.findViewById<TextInputLayout>(R.id.comunita_provenienza_text).editText?.setText(
            mBuilder.comunitaProvenienzaPrefill
        )
        mView.findViewById<TextInputLayout>(R.id.catechisti_text_field).editText?.setText(mBuilder.catechistiProvenienzaPrefill)
        val tappaAutocomplete = mView.findViewById<AutoCompleteTextView>(R.id.tappa_autcomplete)
        if (mBuilder.idTappaPrefill != -1)
            tappaAutocomplete.setText(
                resources.getTextArray(R.array.passaggi_entries)[mBuilder.idTappaPrefill],
                false
            )
        else {
            tappaAutocomplete.text = null
        }
        viewModel.idTappaText = mBuilder.idTappaPrefill
        tappaAutocomplete.setOnItemClickListener { _, _, i, _ ->
            viewModel.idTappaText = i
        }

        val inputDataEntrata =
            mView.findViewById<TextInputLayout>(R.id.data_ingresso_text_field).editText
        mBuilder.dataEntrataPrefill?.let {
            inputDataEntrata?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataEntrata.setupDatePicker(
            requireActivity(),
            "inputDataEntrata",
            R.string.data_entrata_seminario
        )
        val inputDataUscita =
            mView.findViewById<TextInputLayout>(R.id.data_uscita_text_field).editText
        mBuilder.dataUscitaPrefill?.let {
            inputDataUscita?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataUscita?.setupDatePicker(
            requireActivity(),
            "inputDataUscita",
            R.string.data_uscita_seminario
        )
        mView.findViewById<TextInputLayout>(R.id.motivo_text_field).editText?.setText(mBuilder.motivoPrefill.toString())

        mAdapterComunita.set(mBuilder.comunitaAssegnazione
            .map {
                comunitaSeminaristaListItem {
                    comunitaAssegnazione = it.comunitaAssegnazione
                    dataAssegnazione = it.dataAssegnazione
                    idTappaCammino = it.idTappaAssegnazione
                }
            })

        val inputDataAdmissio =
            mView.findViewById<TextInputLayout>(R.id.data_admissio_text_field).editText
        mBuilder.dataAdmissioPrefill?.let {
            inputDataAdmissio?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataAdmissio.setupDatePicker(
            requireActivity(),
            "inputDataAdmissio",
            R.string.admissio_label
        )

        val inputDataAccolitato =
            mView.findViewById<TextInputLayout>(R.id.data_accolitato_text_field).editText
        mBuilder.dataAccolitatoPrefill?.let {
            inputDataAccolitato?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataAccolitato.setupDatePicker(
            requireActivity(),
            "inputDataAccolitato",
            R.string.accolitato_label
        )

        val inputDataLettorato =
            mView.findViewById<TextInputLayout>(R.id.data_lettorato_text_field).editText
        mBuilder.dataLettoratoPrefill?.let {
            inputDataLettorato?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataLettorato.setupDatePicker(
            requireActivity(),
            "inputDataLettorato",
            R.string.lettorato_label
        )

        val inputDataDiaconato =
            mView.findViewById<TextInputLayout>(R.id.data_diaconato_text_field).editText
        mBuilder.dataDiaconatoPrefill?.let {
            inputDataDiaconato?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataDiaconato.setupDatePicker(
            requireActivity(),
            "inputDataDiaconato",
            R.string.diaconato_label
        )

        val inputDataPresbiterato =
            mView.findViewById<TextInputLayout>(R.id.data_presbiterato_text_field).editText
        mBuilder.dataPresbiteratoPrefill?.let {
            inputDataPresbiterato?.setText(Utility.getStringFromDate(mView.context, it))
        }
        inputDataPresbiterato.setupDatePicker(
            requireActivity(),
            "inputDataPresbiterato",
            R.string.presbiterato_label
        )

        mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.setText(mBuilder.notePrefill.toString())

        return mView
    }

    private var cancellaComunitaHook = object : ClickEventHook<ComunitaSeminaristaListItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.remove_comunita)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<ComunitaSeminaristaListItem>,
            item: ComunitaSeminaristaListItem
        ) {
            (fastAdapter as? FastItemAdapter)?.remove(position)
        }
    }

    protected fun fillreturnText(mView: View) {
        viewModel.nomeText =
            mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.text.toString()
                .trim()
        mView.findViewById<TextInputLayout>(R.id.data_nascita_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataNascitaText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }
        viewModel.nazioneText =
            mView.findViewById<TextInputLayout>(R.id.nazione_text_field).editText?.text.toString()
                .trim()

        viewModel.comunitaProvenienzaText =
            mView.findViewById<TextInputLayout>(R.id.comunita_provenienza_text).editText?.text.toString()
                .trim()
        viewModel.catechistiProvenienzaText =
            mView.findViewById<TextInputLayout>(R.id.catechisti_text_field).editText?.text.toString()
                .trim()

        mView.findViewById<TextInputLayout>(R.id.data_ingresso_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataEntrataText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }
        mView.findViewById<TextInputLayout>(R.id.data_uscita_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataUscitaText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }
        viewModel.motivoText =
            mView.findViewById<TextInputLayout>(R.id.motivo_text_field).editText?.text.toString()
                .trim()

        viewModel.comunitaAssegnazioneText =
            mAdapterComunita.adapterItems.filter { it.comunitaAssegnazione.isNotEmpty() }.map {
                ComunitaSeminarista().apply {
                    comunitaAssegnazione = it.comunitaAssegnazione
                    dataAssegnazione = it.dataAssegnazione
                    idTappaAssegnazione = it.idTappaCammino
                }
            }

        mView.findViewById<TextInputLayout>(R.id.data_admissio_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataAdmissioText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        mView.findViewById<TextInputLayout>(R.id.data_accolitato_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataAccolitatoText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        mView.findViewById<TextInputLayout>(R.id.data_lettorato_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataLettoratoText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        mView.findViewById<TextInputLayout>(R.id.data_diaconato_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataDiaconatoText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        mView.findViewById<TextInputLayout>(R.id.data_presbiterato_text_field).editText?.text?.let { txt ->
            if (txt.isNotBlank())
                viewModel.dataPresbiteratoText =
                    Utility.getDateFromString(mView.context, txt.toString())
        }

        viewModel.noteText =
            mView.findViewById<TextInputLayout>(R.id.note_text_field).editText?.text.toString()
                .trim()

    }

    protected fun validateForm(mView: View, showError: Boolean = true): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(
                mView.findViewById(R.id.nome_text_field),
                showError
            )
        )
            valid = false

        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_ingresso_text_field)
            .validateDate()
        valid =
            valid && mView.findViewById<TextInputLayout>(R.id.data_uscita_text_field).validateDate()

        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_admissio_text_field)
            .validateDate()
        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_accolitato_text_field)
            .validateDate()
        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_lettorato_text_field)
            .validateDate()
        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_diaconato_text_field)
            .validateDate()
        valid = valid && mView.findViewById<TextInputLayout>(R.id.data_presbiterato_text_field)
            .validateDate()

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

    class DialogViewModel : ViewModel() {
        var mTag: String = ""

        var nomeText: String = StringUtils.EMPTY_STRING
        var dataNascitaText: Date? = null
        var nazioneText: String = StringUtils.EMPTY_STRING

        var comunitaProvenienzaText: String = StringUtils.EMPTY_STRING
        var catechistiProvenienzaText: String = StringUtils.EMPTY_STRING
        var idTappaText: Int = 0

        var dataEntrataText: Date? = null
        var dataUscitaText: Date? = null
        var motivoText: String = StringUtils.EMPTY_STRING

        var comunitaAssegnazioneText: List<ComunitaSeminarista> = ArrayList()

        var dataAdmissioText: Date? = null
        var dataAccolitatoText: Date? = null
        var dataLettoratoText: Date? = null
        var dataDiaconatoText: Date? = null
        var dataPresbiteratoText: Date? = null

        var noteText: String = StringUtils.EMPTY_STRING

        var handled = true
        val state = MutableLiveData<DialogState<EditSeminaristaDialogFragment>>()
    }

}
