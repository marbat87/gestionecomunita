package it.cammino.gestionecomunita.ui.comunita.detail

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Fratello
import it.cammino.gestionecomunita.databinding.FragmentCommunityDetailBinding
import it.cammino.gestionecomunita.dialog.*
import it.cammino.gestionecomunita.item.ExpandableBrotherItem
import it.cammino.gestionecomunita.item.expandableBrotherItem
import it.cammino.gestionecomunita.ui.comunita.CommunityDetailHostActivity
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.systemLocale
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


open class CommunityDetailFragment : Fragment() {

    private val viewModel: CommunityDetailViewModel by viewModels()
    private val inputdialogViewModel: EditBrotherDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentCommunityDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: CommunityDetailHostActivity? = null

    private val mAdapter: FastItemAdapter<ExpandableBrotherItem> = FastItemAdapter()
    private var llm: LinearLayoutManager? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? CommunityDetailHostActivity
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            arguments?.let {
                // Load the placeholder content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                viewModel.listId = it.getInt(ARG_ITEM_ID)
                viewModel.editMode.value = it.getBoolean(EDIT_MODE, true)
                viewModel.createMode = it.getBoolean(CREATE_MODE, true)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.appBar?.setNavigationOnClickListener {
            activity?.finishAfterTransition()
        }

        if (savedInstanceState == null)
            lifecycleScope.launch { retrieveData() }

        editMode(viewModel.editMode.value == true || viewModel.createMode)
        viewModel.editMode.observe(viewLifecycleOwner) {
            editMode(it || viewModel.createMode)
        }

        binding.fabAddBrother.setOnClickListener {
            mMainActivity?.let { mActivity ->
                val builder = EditBrotherDialogFragment.Builder(
                    mActivity, ADD_BROTHER
                )
                    .setEditMode(false)
                if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeEditBrotherDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                } else {
                    SmallEditBrotherDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                }
            }
        }

        binding.tappaAutcomplete.setOnItemClickListener { _, _, i, _ ->
            viewModel.comunita.idTappa = i
        }

        binding.dataConvivenzaTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataConvivenzaTextField.editText?.setOnKeyListener(null)
        binding.dataConvivenzaTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataConvivenzaTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataConvivenzaTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_convivenza)
                        .build()
                picker.show(
                    requireActivity().supportFragmentManager,
                    "dataConvivenzaTextFieldPicker"
                )
                picker.addOnPositiveButtonClickListener {
                    binding.dataConvivenzaTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        binding.dataVisitaTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataVisitaTextField.editText?.setOnKeyListener(null)
        binding.dataVisitaTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataVisitaTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataVisitaTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_ultima_visita)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "dataVisitaTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    binding.dataVisitaTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        llm = binding.brothersList.layoutManager as? LinearLayoutManager
        binding.brothersList.adapter = mAdapter

        binding.materialTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        showGeneralOrBrothers(true)
                    }
                    1 -> {
                        showGeneralOrBrothers(false)
                    }
                }
                tab?.let { viewModel.selectedTabIndex = it.position }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        binding.editCommunity.setOnClickListener {
            viewModel.editMode.value = true
        }

        binding.cancelChange.setOnClickListener {
            viewModel.editMode.value = false
            lifecycleScope.launch { retrieveData() }
        }

        binding.confirmChanges.setOnClickListener {
            if (validateForm()) {
                viewModel.comunita.diocesi =
                    binding.diocesiTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.numero =
                    binding.numeroTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.parrocchia =
                    binding.parrocchiaTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.email =
                    binding.emailTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.responsabile =
                    binding.responsabileTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.telefono =
                    binding.telefonoTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.dataConvivenza = Utility.getDateFromString(
                    requireContext(),
                    binding.dataConvivenzaTextField.editText?.text?.toString() ?: ""
                )
                viewModel.comunita.dataUltimaVisita = Utility.getDateFromString(
                    requireContext(),
                    binding.dataVisitaTextField.editText?.text?.toString() ?: ""
                )
                viewModel.comunita.note =
                    binding.noteTextField.editText?.text?.toString() ?: ""
                viewModel.comunita.dataUltimaModifica =
                    Date(Calendar.getInstance().time.time)

                if (viewModel.createMode)
                    lifecycleScope.launch { saveComunita() }
                else
                    lifecycleScope.launch {
                        updateComunita()
                        retrieveData()
                    }
            }
        }

        inputdialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "inputdialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        inputdialogViewModel.handled = true
                        val fratello = createBrotherItem(
                            inputdialogViewModel.nomeText,
                            inputdialogViewModel.cognomeText,
                            inputdialogViewModel.statoCivileText,
                            inputdialogViewModel.numFigli,
                            inputdialogViewModel.dataInizioCammino,
                            viewModel.selectedFratello
                        )
                        fratello.setEditable = true
                        fratello.isExpanded = true
                        when (inputdialogViewModel.mTag) {
                            ADD_BROTHER -> {
                                mAdapter.add(fratello)
                            }
                            EDIT_BROTHER -> {
                                mAdapter[viewModel.selectedFratello] = fratello
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        inputdialogViewModel.handled = true
                    }
                }
            }
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            DELETE_BROTHER -> {
                                simpleDialogViewModel.handled = true
                                mAdapter.remove(viewModel.selectedFratello)
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

        lifecycleScope.launch {
            delay(500)
            binding.materialTabs.getTabAt(viewModel.selectedTabIndex)?.select()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence("diocesiTextField", binding.diocesiTextField.editText?.text)
        outState.putCharSequence("numeroTextField", binding.numeroTextField.editText?.text)
        outState.putCharSequence("parrocchiaTextField", binding.parrocchiaTextField.editText?.text)
        outState.putCharSequence("emailTextField", binding.emailTextField.editText?.text)
        outState.putCharSequence(
            "responsabileTextField",
            binding.responsabileTextField.editText?.text
        )
        outState.putCharSequence("telefonoTextField", binding.telefonoTextField.editText?.text)
        outState.putCharSequence("tappaAutcomplete", binding.tappaAutcomplete.text)
        outState.putCharSequence(
            "dataConvivenzaTextField",
            binding.dataConvivenzaTextField.editText?.text
        )
        outState.putCharSequence("dataVisitaTextField", binding.dataVisitaTextField.editText?.text)
        outState.putCharSequence("noteTextField", binding.noteTextField.editText?.text)

        viewModel.elementi =
            mAdapter.itemAdapter.adapterItems as? ArrayList<ExpandableBrotherItem>

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.lastEditDate.text = getString(
            R.string.data_ultima_modifica,
            getTimestampFormatted(viewModel.comunita.dataUltimaModifica)
        )
        binding.diocesiTextField.editText?.setText(savedInstanceState?.getCharSequence("diocesiTextField"))
        binding.numeroTextField.editText?.setText(savedInstanceState?.getCharSequence("numeroTextField"))
        binding.parrocchiaTextField.editText?.setText(savedInstanceState?.getCharSequence("parrocchiaTextField"))
        binding.emailTextField.editText?.setText(savedInstanceState?.getCharSequence("emailTextField"))
        binding.responsabileTextField.editText?.setText(savedInstanceState?.getCharSequence("responsabileTextField"))
        binding.telefonoTextField.editText?.setText(savedInstanceState?.getCharSequence("telefonoTextField"))
        binding.tappaAutcomplete.setText(savedInstanceState?.getCharSequence("tappaAutcomplete"))
        binding.dataConvivenzaTextField.editText?.setText(savedInstanceState?.getCharSequence("dataConvivenzaTextField"))
        binding.dataVisitaTextField.editText?.setText(savedInstanceState?.getCharSequence("dataVisitaTextField"))
        binding.noteTextField.editText?.setText(savedInstanceState?.getCharSequence("noteTextField"))
        viewModel.elementi?.forEach {
            it.deleteClickClickListener = mDeleteClickClickListener
            it.expandClickClickListener = mExpandClickClickListener
            it.editClickClickListener = mEditClickClickListener
        }
        viewModel.elementi?.let { mAdapter.itemAdapter.set(it) }
    }

    private fun createBrotherItem(
        nome: String,
        cognome: String,
        statoCivile: String,
        numFigli: Int,
        dataInizio: Date?,
        position: Int = 0
    ): ExpandableBrotherItem {
        return expandableBrotherItem {
            setNome = nome
            setCognome = cognome
            setStatoCivile = statoCivile
            setNumFigli = numFigli
            setDataInizioCammino = dataInizio
            setPosition = position
            deleteClickClickListener = mDeleteClickClickListener
            expandClickClickListener = mExpandClickClickListener
            editClickClickListener = mEditClickClickListener
        }
    }

    private fun showGeneralOrBrothers(generalDetails: Boolean) {
        binding.brothersList.isVisible = !generalDetails
        binding.fabAddBrother.isVisible = !generalDetails && viewModel.editMode.value == true
        binding.communityDetailScrollView.isVisible = generalDetails
    }

    private fun editMode(editMode: Boolean) {
        binding.diocesiTextField.isEnabled = editMode
        binding.numeroTextField.isEnabled = editMode
        binding.parrocchiaTextField.isEnabled = editMode
        binding.emailTextField.isEnabled = editMode
        binding.responsabileTextField.isEnabled = editMode
        binding.telefonoTextField.isEnabled = editMode
        binding.tappaTextField.isEnabled = editMode
        binding.dataConvivenzaTextField.isEnabled = editMode
        binding.dataVisitaTextField.isEnabled = editMode
        binding.noteTextField.isEnabled = editMode
        binding.fabAddBrother.isVisible = editMode && viewModel.selectedTabIndex == 1
        binding.editMenu.isVisible = editMode
        binding.editCommunity.isVisible = !editMode
        if (!editMode) {
            binding.numeroTextField.error = null
            binding.parrocchiaTextField.error = null
            binding.tappaTextField.error = null
            binding.emailTextField.error = null
            binding.dataConvivenzaTextField.error = null
            binding.dataVisitaTextField.error = null
        }
        mAdapter.itemAdapter.adapterItems.forEach { it.setEditable = editMode }
        mAdapter.notifyAdapterDataSetChanged()
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

        if (!requireContext().validateMandatoryField(binding.numeroTextField))
            valid = false

        if (!requireContext().validateMandatoryField(binding.parrocchiaTextField))
            valid = false

        if (!requireContext().validateMandatoryField(binding.tappaTextField))
            valid = false

        binding.emailTextField.editText?.let {
            if (!it.text.isNullOrEmpty() && !Patterns.EMAIL_ADDRESS.matcher(it.text).matches()) {
                binding.emailTextField.error = getString(R.string.invalid_email)
                valid = false
            } else
                binding.emailTextField.error = null
        }

        binding.dataConvivenzaTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataConvivenzaTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataConvivenzaTextField.error = null
        }

        binding.dataVisitaTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataVisitaTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataVisitaTextField.error = null
        }


        return valid
    }

    private suspend fun saveComunita() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val id = ComunitaDatabase.getInstance(requireContext()).comunitaDao()
                .insertComunita(viewModel.comunita)
            val fratelli = ArrayList<Fratello>()
            mAdapter.itemAdapter.adapterItems.forEach {
                val fratello = Fratello()
                fratello.nome = it.nome?.getText(requireContext()) ?: ""
                fratello.cognome = it.cognome?.getText(requireContext()) ?: ""
                fratello.statoCivile = it.statoCivile?.getText(requireContext()) ?: ""
                fratello.numFigli = it.numFigli
                fratello.dataInizioCammino = it.dataInizioCammino
                fratello.idComunita = id.toInt()
                fratelli.add(fratello)
            }
        }
        activity?.finish()
    }

    private suspend fun updateComunita() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            db.comunitaDao()
                .updateComnuita(viewModel.comunita)
            db.fratelloDao().truncateTableByComunita(viewModel.listId)
            val fratelli = ArrayList<Fratello>()
            viewModel.elementi =
                mAdapter.itemAdapter.adapterItems as? ArrayList<ExpandableBrotherItem>
            viewModel.elementi?.forEach {
                val fratello = Fratello()
                fratello.nome = it.nome?.getText(requireContext()) ?: ""
                fratello.cognome = it.cognome?.getText(requireContext()) ?: ""
                fratello.statoCivile = it.statoCivile?.getText(requireContext()) ?: ""
                fratello.numFigli = it.numFigli
                fratello.dataInizioCammino = it.dataInizioCammino
                fratello.idComunita = viewModel.listId
                fratelli.add(fratello)
            }
            db.fratelloDao().insertFratelli(fratelli)

        }
        viewModel.editMode.value = false
    }

    private suspend fun retrieveData() {
        if (!viewModel.createMode) {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                val comunitaFratello =
                    ComunitaDatabase.getInstance(requireContext()).comunitaFratelloDao()
                        .getComunitaWithFratelli(viewModel.listId)
                if (comunitaFratello != null)
                    viewModel.comunita = comunitaFratello.comunita
                viewModel.comunitaFratello = comunitaFratello
            }
            binding.lastEditDate.text = getString(
                R.string.data_ultima_modifica,
                getTimestampFormatted(viewModel.comunita.dataUltimaModifica)
            )
            binding.diocesiTextField.editText?.setText(viewModel.comunita.diocesi)
            binding.numeroTextField.editText?.setText(viewModel.comunita.numero)
            binding.parrocchiaTextField.editText?.setText(viewModel.comunita.parrocchia)
            binding.emailTextField.editText?.setText(viewModel.comunita.email)
            binding.responsabileTextField.editText?.setText(viewModel.comunita.responsabile)
            binding.telefonoTextField.editText?.setText(viewModel.comunita.telefono)
            if (viewModel.comunita.idTappa != -1)
                binding.tappaAutcomplete.setText(
                    requireContext().resources.getTextArray(R.array.passaggi_entries)[viewModel.comunita.idTappa],
                    false
                )
            else {
                binding.tappaAutcomplete.text = null
            }
            viewModel.comunita.dataConvivenza?.let {
                binding.dataConvivenzaTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }
            viewModel.comunita.dataUltimaVisita?.let {
                binding.dataVisitaTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }
            binding.noteTextField.editText?.setText(viewModel.comunita.note)

            viewModel.elementi?.let {
                Log.d(TAG, "Lista già valorizzata")
            } ?: run {
                Log.d(TAG, "Lista nulla")
                viewModel.elementi = ArrayList()
                viewModel.comunitaFratello?.fratelli?.let {
                    var position = 0
                    it.forEach { fratello ->
                        viewModel.elementi?.add(
                            createBrotherItem(
                                fratello.nome,
                                fratello.cognome,
                                fratello.statoCivile,
                                fratello.numFigli,
                                fratello.dataInizioCammino,
                                position++
                            )
                        )
                    }
                }
            }
            viewModel.elementi?.forEach { it.setEditable = false }
            viewModel.elementi?.let { mAdapter.set(it) }
        } else {
            if (viewModel.elementi == null)
                viewModel.elementi = ArrayList()
            viewModel.elementi?.let { mAdapter.set(it) }
        }
    }

    val mDeleteClickClickListener = View.OnClickListener {
        mMainActivity?.let { mActivity ->
            viewModel.selectedFratello =
                (it.parent.parent as? View)?.findViewById<TextView>(R.id.positon)?.text.toString()
                    .toInt()
            SimpleDialogFragment.show(
                SimpleDialogFragment.Builder(
                    mActivity,
                    DELETE_BROTHER
                )
                    .title(R.string.delete_fratello)
                    .icon(R.drawable.delete_24px)
                    .content(R.string.delete_fratello_dialog)
                    .positiveButton(R.string.delete_confirm)
                    .negativeButton(android.R.string.cancel),
                mActivity.supportFragmentManager
            )
        }
    }

    val mExpandClickClickListener = View.OnClickListener {
        val parent = it.parent.parent as? View
        mAdapter.notifyItemChanged(
            parent?.findViewById<TextView>(R.id.positon)?.text.toString()
                .toInt()
        )
    }

    val mEditClickClickListener = View.OnClickListener {
        mMainActivity?.let { mActivity ->

            val parent = it.parent.parent as? View
            viewModel.selectedFratello =
                parent?.findViewById<TextView>(R.id.positon)?.text.toString()
                    .toInt()
            val builder = EditBrotherDialogFragment.Builder(
                mActivity, EDIT_BROTHER
            )
                .nomePrefill(parent?.findViewById<TextView>(R.id.text_nome)?.text.toString())
                .cognomePrefill(parent?.findViewById<TextView>(R.id.text_cognome)?.text.toString())
                .statoCivilePrefill(parent?.findViewById<TextView>(R.id.text_stato_civile)?.text.toString())
                .numeroFigliPrefill(
                    parent?.findViewById<TextView>(R.id.text_num_figli)?.text.toString()
                        .toInt()
                )
                .dataInizioCamminoPrefill(
                    Utility.getDateFromString(
                        mActivity,
                        parent?.findViewById<TextView>(R.id.text_data_inizio_cammino)?.text.toString()
                    )
                )
                .setEditMode(true)
            if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                builder.positiveButton(R.string.save)
                    .negativeButton(android.R.string.cancel)
                LargeEditBrotherDialogFragment.show(
                    builder,
                    mActivity.supportFragmentManager
                )
            } else {
                SmallEditBrotherDialogFragment.show(
                    builder,
                    mActivity.supportFragmentManager
                )
            }
        }
    }

    companion object {
        internal val TAG = CommunityDetailFragment::class.java.canonicalName
        const val ARG_ITEM_ID = "item_id"
        const val EDIT_MODE = "edit_mode"
        const val CREATE_MODE = "create_mode"
        const val ADD_BROTHER = "add_brother"
        const val EDIT_BROTHER = "edit_brother"
        const val DELETE_BROTHER = "delete_brother"

    }

}