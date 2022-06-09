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
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Fratello
import it.cammino.gestionecomunita.database.entity.Passaggio
import it.cammino.gestionecomunita.database.entity.Promemoria
import it.cammino.gestionecomunita.databinding.FragmentCommunityDetailBinding
import it.cammino.gestionecomunita.dialog.*
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeCommunityHistoryDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditBrotherDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallCommunityHistoryDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditBrotherDialogFragment
import it.cammino.gestionecomunita.item.ExpandableBrotherItem
import it.cammino.gestionecomunita.item.expandableBrotherItem
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.Utility.EMPTY_STRING
import it.cammino.gestionecomunita.util.systemLocale
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
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
    private val addNotificationViewMode: AddNotificationDialogFragment.DialogViewModel by viewModels(
        { requireActivity() })


    private var _binding: FragmentCommunityDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    private val mAdapter: FastItemAdapter<ExpandableBrotherItem> = FastItemAdapter()
    private var llm: LinearLayoutManager? = null

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
        _binding = FragmentCommunityDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.tablet_layout)

        if (isTablet && !viewModel.createMode)
            binding.appBar.isVisible = false

        if (isTablet && viewModel.createMode)
            binding.materialTabs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )

        binding.appBar.setNavigationOnClickListener {
            activity?.finishAfterTransition()
        }

        binding.appBar.navigationIcon = AppCompatResources.getDrawable(
            requireContext(),
            if (viewModel.createMode) R.drawable.close_24px else R.drawable.arrow_back_24px
        )

        binding.appBar.title =
            getString(if (viewModel.createMode) R.string.nuova_comunita else R.string.comunita)

        binding.salvaComunita.isVisible = viewModel.createMode
        binding.bottomAppBar.isVisible = !viewModel.createMode

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

        binding.deleteCommunity.setOnClickListener {
            mMainActivity?.let { mActivity ->
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_COMMUNITY
                    )
                        .title(R.string.delete_community)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_community_dialog)
                        .positiveButton(R.string.delete_confirm)
                        .negativeButton(android.R.string.cancel),
                    mActivity.supportFragmentManager
                )
            }
        }

        binding.historyCommunity.setOnClickListener {
            mMainActivity?.let { mActivity ->
                val builder = CommunityHistoryDialogFragment.Builder(
                    mActivity, HISTORY
                )
                builder.idComunita = viewModel.listId
                if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(android.R.string.ok)
                    LargeCommunityHistoryDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                } else {
                    SmallCommunityHistoryDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                }
            }
        }

        binding.addNotification.setOnClickListener {
            mMainActivity?.let { mActivity ->
                val builder = AddNotificationDialogFragment.Builder(
                    mActivity, ADD_NOTIFICATION
                )
                    .idComunitaPrefill(viewModel.listId)
                if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeAddNotificationDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                } else {
                    SmallAddNotificationDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                }
            }
        }

        binding.cancelChange.setOnClickListener {
            if (viewModel.createMode) {
                activity?.finishAfterTransition()
            } else {
                viewModel.editMode.value = false
                lifecycleScope.launch { retrieveData() }
            }
        }

        binding.confirmChanges.setOnClickListener {
            confirmChanges()
        }

        binding.salvaComunita.setOnClickListener {
            confirmChanges()
        }

        inputdialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "inputDialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        inputdialogViewModel.handled = true
                        val fratello = createBrotherItem(
                            inputdialogViewModel.nomeText,
                            inputdialogViewModel.cognomeText,
                            inputdialogViewModel.statoCivileText,
                            inputdialogViewModel.coniugeText,
                            inputdialogViewModel.numFigli,
                            inputdialogViewModel.tribuText,
                            inputdialogViewModel.annoNascita,
                            inputdialogViewModel.carismaText,
                            inputdialogViewModel.comunitaOrigineText,
                            inputdialogViewModel.dataArrivo,
                            inputdialogViewModel.statoInt,
                            inputdialogViewModel.noteText,
                            inputdialogViewModel.dataInizioCammino,
                            viewModel.selectedFratello
                        )
                        fratello.editable = true
                        fratello.isExpanded = true
                        when (inputdialogViewModel.mTag) {
                            ADD_BROTHER -> {
                                fratello.position = mAdapter.itemAdapter.adapterItemCount
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
                            DELETE_COMMUNITY -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { deleteComunita() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

        addNotificationViewMode.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!addNotificationViewMode.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (addNotificationViewMode.mTag) {
                            ADD_NOTIFICATION -> {
                                addNotificationViewMode.handled = true
                                lifecycleScope.launch {
                                    addPromemoria(
                                        addNotificationViewMode.idComunita,
                                        addNotificationViewMode.data,
                                        addNotificationViewMode.descrizioneText
                                    )
                                }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        addNotificationViewMode.handled = true
                    }
                }
            }
        }
        binding.materialTabs.getTabAt(viewModel.selectedTabIndex)?.select()
//        lifecycleScope.launch {
//            delay(500)
//            binding.materialTabs.getTabAt(viewModel.selectedTabIndex)?.select()
//        }
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
        outState.putCharSequence("parroccoTextField", binding.parroccoTextField.editText?.text)
        outState.putCharSequence("catechistiTextField", binding.catechistiTextField.editText?.text)
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
        Log.d(TAG, "onViewStateRestored")
        savedInstanceState?.let { instance ->
            if (viewModel.createMode)
                binding.lastEditDate.isVisible = false
            else {
                binding.lastEditDate.text = getString(
                    R.string.data_ultima_modifica,
                    getTimestampFormatted(viewModel.comunita.dataUltimaModifica)
                )
            }
            binding.diocesiTextField.editText?.setText(instance.getCharSequence("diocesiTextField"))
            binding.numeroTextField.editText?.setText(instance.getCharSequence("numeroTextField"))
            binding.parrocchiaTextField.editText?.setText(instance.getCharSequence("parrocchiaTextField"))
            binding.parroccoTextField.editText?.setText(instance.getCharSequence("parroccoTextField"))
            binding.catechistiTextField.editText?.setText(instance.getCharSequence("catechistiTextField"))
            binding.emailTextField.editText?.setText(instance.getCharSequence("emailTextField"))
            binding.responsabileTextField.editText?.setText(instance.getCharSequence("responsabileTextField"))
            binding.telefonoTextField.editText?.setText(instance.getCharSequence("telefonoTextField"))
            binding.tappaAutcomplete.setText(instance.getCharSequence("tappaAutcomplete"))
            binding.dataConvivenzaTextField.editText?.setText(instance.getCharSequence("dataConvivenzaTextField"))
            binding.dataVisitaTextField.editText?.setText(instance.getCharSequence("dataVisitaTextField"))
            binding.noteTextField.editText?.setText(instance.getCharSequence("noteTextField"))
            viewModel.elementi?.forEach {
                it.deleteClickClickListener = mDeleteClickClickListener
                it.expandClickClickListener = mExpandClickClickListener
                it.editClickClickListener = mEditClickClickListener
            }
            viewModel.elementi?.let { mAdapter.itemAdapter.set(it) }
        }
    }

    private fun confirmChanges() {
        if (validateForm()) {
            viewModel.comunita.diocesi =
                binding.diocesiTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.numero =
                binding.numeroTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.parrocchia =
                binding.parrocchiaTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.parroco =
                binding.parroccoTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.catechisti =
                binding.catechistiTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.email =
                binding.emailTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.responsabile =
                binding.responsabileTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.telefono =
                binding.telefonoTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.dataConvivenza = Utility.getDateFromString(
                requireContext(),
                binding.dataConvivenzaTextField.editText?.text?.toString()?.trim()
                    ?: EMPTY_STRING
            )
            viewModel.comunita.dataUltimaVisita = Utility.getDateFromString(
                requireContext(),
                binding.dataVisitaTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            )
            viewModel.comunita.note =
                binding.noteTextField.editText?.text?.toString()?.trim() ?: EMPTY_STRING
            viewModel.comunita.dataUltimaModifica =
                Date(Calendar.getInstance().time.time)

            if (viewModel.createMode)
                lifecycleScope.launch { saveComunita() }
            else
                lifecycleScope.launch {
                    updateComunita()
                    retrieveData()
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

    private fun createBrotherItem(
        nome: String,
        cognome: String,
        statoCivile: String,
        coniuge: String,
        numFigli: Int,
        tribu: String,
        annoNascita: Date?,
        carisma: String,
        comunitaOrigine: String,
        dataArrivo: Date?,
        stato: Int,
        note: String,
        dataInizio: Date?,
        position: Int = 0
    ): ExpandableBrotherItem {
        return expandableBrotherItem {
            this.nome = nome
            this.cognome = cognome
            this.statoCivile = statoCivile
            this.coniuge = coniuge
            this.tribu = tribu
            this.annoNascita = annoNascita
            this.carisma = carisma
            this.comunitaOrigine = comunitaOrigine
            this.dataArrivo = dataArrivo
            this.stato = stato
            this.note = note
            this.numFigli = numFigli
            this.dataInizioCammino = dataInizio
            this.position = position
            deleteClickClickListener = mDeleteClickClickListener
            expandClickClickListener = mExpandClickClickListener
            editClickClickListener = mEditClickClickListener
        }
    }

    private fun showGeneralOrBrothers(generalDetails: Boolean) {
        binding.brothersList.isVisible = !generalDetails
        binding.fabAddBrother.isVisible = !generalDetails && viewModel.editMode.value == true
        binding.communityDetailScrollView.isVisible = generalDetails
        binding.bottomAppBar.performShow()
    }

    private fun editMode(editMode: Boolean) {
        binding.diocesiTextField.isEnabled = editMode
        binding.numeroTextField.isEnabled = editMode
        binding.parrocchiaTextField.isEnabled = editMode
        binding.parroccoTextField.isEnabled = editMode
        binding.catechistiTextField.isEnabled = editMode
        binding.emailTextField.isEnabled = editMode
        binding.responsabileTextField.isEnabled = editMode
        binding.telefonoTextField.isEnabled = editMode
        binding.tappaTextField.isEnabled = editMode
        binding.dataConvivenzaTextField.isEnabled = editMode
        binding.dataVisitaTextField.isEnabled = editMode
        binding.noteTextField.isEnabled = editMode
        binding.fabAddBrother.isVisible = editMode && viewModel.selectedTabIndex == 1
        binding.editMenu.isVisible = editMode
        binding.communityMenu.isVisible = !editMode
        if (!editMode) {
            binding.numeroTextField.error = null
            binding.parrocchiaTextField.error = null
            binding.tappaTextField.error = null
            binding.emailTextField.error = null
            binding.dataConvivenzaTextField.error = null
            binding.dataVisitaTextField.error = null
        }
        mAdapter.itemAdapter.adapterItems.forEach { it.editable = editMode }
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
            val db = ComunitaDatabase.getInstance(requireContext())
            val insertedId = db.comunitaDao().insertComunita(viewModel.comunita)
            Log.d(TAG, "save insertedId : $insertedId")
            val fratelli = ArrayList<Fratello>()
            mAdapter.itemAdapter.adapterItems.forEach {
                val fratello = Fratello()
                fratello.nome = it.nome.trim()
                fratello.cognome = it.cognome.trim()
                fratello.statoCivile = it.statoCivile.trim()
                fratello.coniuge = it.coniuge.trim()
                fratello.tribu = it.tribu.trim()
                fratello.annoNascita = it.annoNascita
                fratello.carisma = it.carisma.trim()
                fratello.comunitaOrigine = it.comunitaOrigine.trim()
                fratello.dataArrivo = it.dataArrivo
                fratello.statoAttuale = it.stato
                fratello.note = it.note.trim()
                fratello.numFigli = it.numFigli
                fratello.dataInizioCammino = it.dataInizioCammino
                fratello.idComunita = insertedId
                fratelli.add(fratello)
            }
            db.fratelloDao().insertFratelli(fratelli)
            updateHistory(insertedId)
        }
        activity?.finishAfterTransition()
    }

    private suspend fun updateComunita() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            updateHistory(viewModel.listId)
            db.comunitaDao()
                .updateComnuita(viewModel.comunita)
            db.fratelloDao().truncateTableByComunita(viewModel.listId)
            val fratelli = ArrayList<Fratello>()
            viewModel.elementi =
                mAdapter.itemAdapter.adapterItems as? ArrayList<ExpandableBrotherItem>
            viewModel.elementi?.forEach {
                val fratello = Fratello()
                fratello.nome = it.nome.trim()
                fratello.cognome = it.cognome.trim()
                fratello.statoCivile = it.statoCivile.trim()
                fratello.coniuge = it.coniuge.trim()
                fratello.tribu = it.tribu.trim()
                fratello.annoNascita = it.annoNascita
                fratello.carisma = it.carisma.trim()
                fratello.comunitaOrigine = it.comunitaOrigine.trim()
                fratello.dataArrivo = it.dataArrivo
                fratello.statoAttuale = it.stato
                fratello.note = it.note.trim()
                fratello.numFigli = it.numFigli
                fratello.dataInizioCammino = it.dataInizioCammino
                fratello.idComunita = viewModel.listId
                fratelli.add(fratello)
            }
            db.fratelloDao().insertFratelli(fratelli)
        }
        viewModel.editMode.value = false
    }

    private fun updateHistory(idComunita: Long) {
        val db = ComunitaDatabase.getInstance(requireContext())
        val comunitaLatestTappa = db.comunitaDao().getById(idComunita)?.idTappa ?: 0
        if (viewModel.createMode || viewModel.comunita.idTappa > comunitaLatestTappa) {
            val passaggio = Passaggio()
            passaggio.data = viewModel.comunita.dataConvivenza
            passaggio.idComunita = idComunita
            passaggio.passaggio = viewModel.comunita.idTappa
            db.passaggioDao().insertPassaggio(passaggio)
        }
    }

    private suspend fun deleteComunita() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            db.fratelloDao().truncateTableByComunita(viewModel.listId)
            db.comunitaDao().deleteComunita(Comunita().apply { id = viewModel.listId })
        }

        if (resources.getBoolean(R.bool.tablet_layout)) {
            val fragment =
                mMainActivity?.supportFragmentManager?.findFragmentByTag(R.id.community_detail_fragment.toString())
            fragment?.let {
                mMainActivity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commit()
            }
        } else
            activity?.finishAfterTransition()
    }

    private suspend fun addPromemoria(idComunita: Long, data: Date?, descrizione: String) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            val promemoria = Promemoria()
            promemoria.note = descrizione
            promemoria.idComunita = idComunita
            promemoria.data = data
            db.promemoriaDao().insertPromemoria(promemoria)
        }
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.promemoria_aggiunto),
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    private suspend fun retrieveData() {
        Log.d(TAG, "createMode ${viewModel.createMode}")
        if (!viewModel.createMode) {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                val comunitaFratello =
                    ComunitaDatabase.getInstance(requireContext()).fratelloDao()
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
            binding.catechistiTextField.editText?.setText(viewModel.comunita.catechisti)
            binding.parroccoTextField.editText?.setText(viewModel.comunita.parroco)
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
                                fratello.coniuge,
                                fratello.numFigli,
                                fratello.tribu,
                                fratello.annoNascita,
                                fratello.carisma,
                                fratello.comunitaOrigine,
                                fratello.dataArrivo,
                                fratello.statoAttuale,
                                fratello.note,
                                fratello.dataInizioCammino,
                                position++
                            )
                        )
                    }
                }
            }
            viewModel.elementi?.forEach { it.editable = false }
            viewModel.elementi?.let { mAdapter.set(it) }
        } else {
            if (viewModel.elementi == null)
                viewModel.elementi = ArrayList()
            viewModel.elementi?.let { mAdapter.set(it) }
            binding.lastEditDate.isVisible = false
            val passaggio = requireContext().resources.getTextArray(R.array.passaggi_entries)[0]
            binding.tappaAutcomplete.setText(
                passaggio,
                false
            )
        }
    }

    private val mDeleteClickClickListener = object : ExpandableBrotherItem.OnClickListener {
        override fun onClick(it: ExpandableBrotherItem) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedFratello = it.position
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
    }

    private val mExpandClickClickListener = object : ExpandableBrotherItem.OnClickListener {
        override fun onClick(it: ExpandableBrotherItem) {
            mAdapter.notifyItemChanged(it.position)
        }
    }

    private val mEditClickClickListener = object : ExpandableBrotherItem.OnClickListener {
        override fun onClick(it: ExpandableBrotherItem) {
            mMainActivity?.let { mActivity ->

                viewModel.selectedFratello = it.position
                val builder = EditBrotherDialogFragment.Builder(
                    mActivity, EDIT_BROTHER
                )
                    .nomePrefill(it.nome)
                    .cognomePrefill(it.cognome)
                    .statoCivilePrefill(it.statoCivile)
                    .setConiugePrefill(it.coniuge)
                    .numeroFigliPrefill(it.numFigli)
                    .setDataNascitaPrefill(it.annoNascita)
                    .setCarismaPrefill(it.carisma)
                    .setTribuPrefill(it.tribu)
                    .setComunitaOriginePrefill(it.comunitaOrigine)
                    .setDataArrivoPrefill(it.dataArrivo)
                    .setStatoPrefill(it.stato)
                    .setNotePrefill(it.note)
                    .dataInizioCamminoPrefill(it.dataInizioCammino)
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
    }

    companion object {
        internal val TAG = CommunityDetailFragment::class.java.canonicalName
        const val ARG_ITEM_ID = "item_id"
        const val EDIT_MODE = "edit_mode"
        const val CREATE_MODE = "create_mode"
        const val ADD_BROTHER = "add_brother"
        const val EDIT_BROTHER = "edit_brother"
        const val ADD_NOTIFICATION = "add_notification"
        const val HISTORY = "history"
        const val DELETE_BROTHER = "delete_brother"
        const val DELETE_COMMUNITY = "delete_community"
        const val ERROR_DIALOG = "community_detail_error_dialog"

    }

}