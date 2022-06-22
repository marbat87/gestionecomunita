package it.cammino.gestionecomunita.ui.vocazione.detail

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Vocazione
import it.cammino.gestionecomunita.databinding.FragmentVocazioneDetailBinding
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.systemLocale
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


open class VocazioneDetailFragment : Fragment() {

    private val viewModel: VocazioneDetailViewModel by viewModels()
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentVocazioneDetailBinding? =
        null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? AppCompatActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            arguments?.let {
                // Load the placeholder content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                viewModel.listId = it.getLong(ARG_ITEM_ID)
                viewModel.editMode.value = it.getBoolean(EDIT_MODE, true)
                viewModel.createMode = it.getBoolean(CREATE_MODE, true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocazioneDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.tablet_layout)

        if (isTablet && !viewModel.createMode)
            binding.appBar.isVisible = false

        binding.appBar.setNavigationOnClickListener {
            activity?.finishAfterTransition()
        }

        binding.appBar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            if (viewModel.createMode) R.drawable.close_24px else R.drawable.arrow_back_24px
        )

        binding.appBar.title =
            getString(if (viewModel.createMode) R.string.vocazione_new_title else R.string.vocazione_title)

        binding.salvaVocazione.isVisible = viewModel.createMode
        binding.bottomAppBar.isVisible = !viewModel.createMode

        binding.dataNascitaTextField.editText?.doOnTextChanged { text, _, _, _ ->
            Utility.getDateFromString(
                requireContext(),
                text?.toString().orEmpty()
            )?.let { binding.textEta.text = Utility.calculateAge(text.toString()).toString() }
        }

        lifecycleScope.launch { retrieveData(savedInstanceState == null) }

        editMode(viewModel.editMode.value == true || viewModel.createMode)
        viewModel.editMode.observe(viewLifecycleOwner) {
            editMode(it || viewModel.createMode)
        }

        binding.sessoAutcomplete.setOnItemClickListener { _, _, i, _ ->
            when (i) {
                0 -> viewModel.vocazione.sesso = Vocazione.Sesso.MASCHIO
                1 -> viewModel.vocazione.sesso = Vocazione.Sesso.FEMMINA
                else -> viewModel.vocazione.sesso = Vocazione.Sesso.MASCHIO
            }
            binding.vocazioneMaleFemaleImage.setImageResource(if (viewModel.vocazione.sesso == Vocazione.Sesso.MASCHIO) R.drawable.man_24px else R.drawable.woman_24px)
        }

        binding.tappaAutcomplete.setOnItemClickListener { _, _, i, _ ->
            viewModel.vocazione.idTappa = i
        }

        binding.comunitaAutcomplete.setOnItemClickListener { _, _, i, _ ->
            viewModel.vocazione.idComunita = viewModel.comunitaList[i].id
        }

        binding.dataNascitaTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataNascitaTextField.editText?.setOnKeyListener(null)
        binding.dataNascitaTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataNascitaTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataNascitaTextField.editText?.text?.toString()
                                        .orEmpty()
                                )?.time
                        )
                        .setTitleText(R.string.data_nascita)
                        .build()
                picker.show(
                    requireActivity().supportFragmentManager,
                    "dataNascitaTextFieldPicker"
                )
                picker.addOnPositiveButtonClickListener {
                    binding.dataNascitaTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        binding.dataIngressoTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataIngressoTextField.editText?.setOnKeyListener(null)
        binding.dataIngressoTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataIngressoTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataIngressoTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_ultima_visita)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "dataIngressoTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    binding.dataIngressoTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        binding.editVocazione.setOnClickListener {
            viewModel.editMode.value = true
        }

        binding.deleteVocazione.setOnClickListener {
            mMainActivity?.let { mActivity ->
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_VOCAZIONE
                    )
                        .title(R.string.delete_vocazione)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_vocazione_dialog)
                        .positiveButton(R.string.remove)
                        .negativeButton(android.R.string.cancel),
                    mActivity.supportFragmentManager
                )
            }
        }

        binding.cancelChange.setOnClickListener {
            if (viewModel.createMode) {
                activity?.finishAfterTransition()
            } else {
                viewModel.editMode.value = false
                lifecycleScope.launch { retrieveData(false) }
            }
        }

        binding.confirmChanges.setOnClickListener {
            confirmChanges()
        }

        binding.salvaVocazione.setOnClickListener {
            confirmChanges()
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            DELETE_VOCAZIONE -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { deleteVocazione() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence("nomeTextField", binding.nomeTextField.editText?.text)
        outState.putCharSequence("comunitaAutcomplete", binding.comunitaAutcomplete.text)
        outState.putCharSequence("telefonoTextField", binding.telefonoTextField.editText?.text)
        outState.putCharSequence("cittaTextField", binding.cittaTextField.editText?.text)
        outState.putCharSequence(
            "dataNascitaTextField",
            binding.dataNascitaTextField.editText?.text
        )
        outState.putCharSequence("studiTextField", binding.studiTextField.editText?.text)
        outState.putCharSequence("tappaAutcomplete", binding.tappaAutcomplete.text)
        outState.putCharSequence("sessoAutcomplete", binding.sessoAutcomplete.text)
        outState.putCharSequence(
            "dataIngressoTextField",
            binding.dataIngressoTextField.editText?.text
        )
        outState.putCharSequence("noteTextField", binding.noteTextField.editText?.text)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored")
        savedInstanceState?.let { instance ->
            if (viewModel.createMode)
                binding.lastEditDate.isVisible = false
            else {
                binding.lastEditDate.text = getString(
                    R.string.data_ultima_modifica,
                    getTimestampFormatted(viewModel.vocazione.dataUltimaModifica)
                )
            }
            binding.nomeTextField.editText?.setText(instance.getCharSequence("nomeTextField"))
            binding.comunitaAutcomplete.setText(
                instance.getCharSequence("comunitaAutcomplete"),
                false
            )
            binding.telefonoTextField.editText?.setText(instance.getCharSequence("telefonoTextField"))
            binding.cittaTextField.editText?.setText(instance.getCharSequence("cittaTextField"))
            binding.dataNascitaTextField.editText?.setText(instance.getCharSequence("dataNascitaTextField"))
            binding.studiTextField.editText?.setText(instance.getCharSequence("studiTextField"))
            binding.tappaTextField.editText?.setText(instance.getCharSequence("responsabileTextField"))
            binding.telefonoTextField.editText?.setText(instance.getCharSequence("telefonoTextField"))
            binding.sessoAutcomplete.setAdapter(
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_dropdown_item_1line,
                    resources.getStringArray(R.array.sesso_entries)
                )
            )
            binding.tappaAutcomplete.setText(instance.getCharSequence("tappaAutcomplete"), false)
            binding.sessoAutcomplete.setText(instance.getCharSequence("sessoAutcomplete"), false)
            binding.dataIngressoTextField.editText?.setText(instance.getCharSequence("dataIngressoTextField"))
            binding.noteTextField.editText?.setText(instance.getCharSequence("noteTextField"))
            binding.vocazioneMaleFemaleImage.setImageResource(if (viewModel.vocazione.sesso == Vocazione.Sesso.MASCHIO) R.drawable.man_24px else R.drawable.woman_24px)
        }
    }

    private fun confirmChanges() {
        if (validateForm()) {
            viewModel.vocazione.nome =
                binding.nomeTextField.editText?.text?.toString().orEmpty().trim()
            viewModel.vocazione.telefono =
                binding.telefonoTextField.editText?.text?.toString().orEmpty().trim()
            viewModel.vocazione.citta =
                binding.cittaTextField.editText?.text?.toString().orEmpty().trim()
            viewModel.vocazione.dataNascita = Utility.getDateFromString(
                requireContext(),
                binding.dataNascitaTextField.editText?.text?.toString().orEmpty().trim()
            )
            viewModel.vocazione.studi =
                binding.studiTextField.editText?.text?.toString().orEmpty().trim()
            viewModel.vocazione.dataIngresso = Utility.getDateFromString(
                requireContext(),
                binding.dataIngressoTextField.editText?.text?.toString().orEmpty().trim()
            )
            viewModel.vocazione.osservazioni =
                binding.noteTextField.editText?.text?.toString().orEmpty().trim()

            viewModel.vocazione.dataUltimaModifica =
                Date(Calendar.getInstance().time.time)

            if (viewModel.createMode)
                lifecycleScope.launch { saveVocazione() }
            else
                lifecycleScope.launch {
                    updateVocazione()
                    retrieveData(false)
                }
        } else {
            mMainActivity?.let { mActivity ->
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        ERROR_DIALOG
                    )
                        .title(R.string.error)
                        .icon(R.drawable.error_24px)
                        .content(R.string.campi_non_compilati)
                        .positiveButton(android.R.string.ok),
                    mActivity.supportFragmentManager
                )
            }
        }
    }

    private fun editMode(editMode: Boolean) {
        binding.nomeTextField.isEnabled = editMode
        binding.comunitaAutoText.isEnabled = editMode
        binding.telefonoTextField.isEnabled = editMode
        binding.cittaTextField.isEnabled = editMode
        binding.dataNascitaTextField.isEnabled = editMode
        binding.studiTextField.isEnabled = editMode
        binding.tappaTextField.isEnabled = editMode
        binding.telefonoTextField.isEnabled = editMode
        binding.tappaTextField.isEnabled = editMode
        binding.dataIngressoTextField.isEnabled = editMode
        binding.noteTextField.isEnabled = editMode
        binding.editMenu.isVisible = editMode
        binding.vocazioneMenu.isVisible = !editMode
        binding.sessoTextField.isEnabled = editMode
        if (!editMode) {
            binding.nomeTextField.error = null
            binding.dataNascitaTextField.error = null
            binding.dataIngressoTextField.error = null
        }
    }

    private fun getTimestampFormatted(dateTimestamp: Date?): String {
        if (dateTimestamp == null)
            return ""
        val df = DateFormat.getDateTimeInstance(
            DateFormat.SHORT, DateFormat.MEDIUM, requireContext().resources.systemLocale
        )

        return if (df is SimpleDateFormat) {
            val pattern = df.toPattern().replace("y+".toRegex(), "yyyy")
            df.applyPattern(pattern)
            df.format(dateTimestamp)
        } else
            df.format(dateTimestamp)
    }

    private fun validateForm(): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(binding.nomeTextField))
            valid = false

        binding.dataNascitaTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataNascitaTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataNascitaTextField.error = null
        }

        binding.dataIngressoTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataIngressoTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataIngressoTextField.error = null
        }

        return valid
    }

    private suspend fun saveVocazione() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).vocazioneDao()
                .insertVocazione(viewModel.vocazione)
        }
        activity?.finishAfterTransition()
    }

    private suspend fun updateVocazione() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).vocazioneDao()
                .updateVocazione(viewModel.vocazione)
        }
        viewModel.editMode.value = false
    }

    private suspend fun deleteVocazione() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).vocazioneDao()
                .deleteVocazione(Vocazione().apply { idVocazione = viewModel.listId })
        }

        if (resources.getBoolean(R.bool.tablet_layout)) {
            val fragment =
                mMainActivity?.supportFragmentManager?.findFragmentByTag(R.id.vocazione_detail_fragment.toString())
            fragment?.let {
                mMainActivity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commit()
            }
        } else
            activity?.finishAfterTransition()
    }

    private suspend fun retrieveData(loadAll: Boolean) {
        Log.d(TAG, "createMode ${viewModel.createMode}")
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            viewModel.comunitaList =
                ComunitaDatabase.getInstance(requireContext()).comunitaDao().allByName
        }

        val comunitaStrings = viewModel.comunitaList
            .map {
                resources.getString(R.string.comunita_item_name, it.numero, it.parrocchia)
            }
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line, comunitaStrings
        )
        val textView = binding.comunitaAutcomplete
        textView.setAdapter(adapter)

        if (!loadAll) return

        if (!viewModel.createMode) {
            lateinit var comunita: Comunita
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                viewModel.vocazione = ComunitaDatabase.getInstance(requireContext()).vocazioneDao()
                    .getById(viewModel.listId) ?: Vocazione()
                if (viewModel.vocazione.idComunita != (-1).toLong())
                    comunita = ComunitaDatabase.getInstance(requireContext()).comunitaDao()
                        .getById(viewModel.vocazione.idComunita) ?: Comunita()
            }

            if (viewModel.vocazione.idComunita != (-1).toLong()) {
                textView.setText(
                    resources.getString(
                        R.string.comunita_item_name,
                        comunita.numero,
                        comunita.parrocchia
                    ), false
                )
            }

            binding.lastEditDate.text = getString(
                R.string.data_ultima_modifica,
                getTimestampFormatted(viewModel.vocazione.dataUltimaModifica)
            )
            binding.nomeTextField.editText?.setText(viewModel.vocazione.nome)
            binding.noteTextField.editText?.setText(viewModel.vocazione.osservazioni)
            binding.telefonoTextField.editText?.setText(viewModel.vocazione.telefono)
            binding.cittaTextField.editText?.setText(viewModel.vocazione.citta)
            binding.studiTextField.editText?.setText(viewModel.vocazione.studi)

            binding.sessoAutcomplete.setText(
                requireContext().resources.getTextArray(R.array.sesso_entries)[if (viewModel.vocazione.sesso == Vocazione.Sesso.MASCHIO) 0 else 1],
                false
            )
            binding.vocazioneMaleFemaleImage.setImageResource(if (viewModel.vocazione.sesso == Vocazione.Sesso.MASCHIO) R.drawable.man_24px else R.drawable.woman_24px)

            if (viewModel.vocazione.idTappa != -1)
                binding.tappaAutcomplete.setText(
                    requireContext().resources.getTextArray(R.array.passaggi_entries)[viewModel.vocazione.idTappa],
                    false
                )
            else {
                binding.tappaAutcomplete.text = null
            }

            viewModel.vocazione.dataNascita?.let {
                binding.dataNascitaTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }

            viewModel.vocazione.dataIngresso?.let {
                binding.dataIngressoTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }

        } else {
            binding.lastEditDate.isVisible = false
            binding.sessoAutcomplete.setText(
                requireContext().resources.getTextArray(R.array.sesso_entries)[0],
                false
            )
            viewModel.vocazione.sesso = Vocazione.Sesso.MASCHIO
        }
    }

    companion object {
        internal val TAG = VocazioneDetailFragment::class.java.canonicalName
        const val ARG_ITEM_ID = "item_id"
        const val EDIT_MODE = "edit_mode"
        const val CREATE_MODE = "create_mode"
        const val DELETE_VOCAZIONE = "delete_vocazione"
        const val ERROR_DIALOG = "community_detail_error_dialog"

    }

}