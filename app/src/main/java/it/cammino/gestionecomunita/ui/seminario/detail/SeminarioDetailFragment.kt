package it.cammino.gestionecomunita.ui.seminario.detail

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.InputType
import android.util.Log
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario
import it.cammino.gestionecomunita.database.entity.Seminario
import it.cammino.gestionecomunita.database.entity.Seminarista
import it.cammino.gestionecomunita.databinding.FragmentSeminarioDetailBinding
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditBrotherDialogFragment
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditBrotherDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditBrotherDialogFragment
import it.cammino.gestionecomunita.item.ResponsabileListItem
import it.cammino.gestionecomunita.item.SeminaristaItem
import it.cammino.gestionecomunita.item.responsabileListItem
import it.cammino.gestionecomunita.item.seminaristaItem
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.validateMandatoryField
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date


open class SeminarioDetailFragment : Fragment() {

    private val viewModel: SeminarioDetailViewModel by viewModels()
    private val inputdialogViewModel: EditBrotherDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentSeminarioDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    private val mAdapterSeminaristi: FastItemAdapter<SeminaristaItem> = FastItemAdapter()
    private val mAdapterVisite: FastItemAdapter<SeminaristaItem> = FastItemAdapter()
    private val mAdapterRettori: FastItemAdapter<ResponsabileListItem> = FastItemAdapter()
    private val mAdapterVice: FastItemAdapter<ResponsabileListItem> = FastItemAdapter()
    private val mAdapterSpirituali: FastItemAdapter<ResponsabileListItem> = FastItemAdapter()

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
        _binding = FragmentSeminarioDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.getBoolean(R.bool.tablet_layout)

        if (!isTablet)
            binding.appBarLayout.statusBarForeground = ColorDrawable(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )

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
            getString(if (viewModel.createMode) R.string.nuovo_seminario else R.string.seminario)

        binding.salvaSeminario.isVisible = viewModel.createMode
        binding.bottomAppBar.isVisible = !viewModel.createMode

        if (savedInstanceState == null && !viewModel.createMode)
            lifecycleScope.launch { retrieveData() }

        editMode(viewModel.editMode.value == true || viewModel.createMode)
        viewModel.editMode.observe(viewLifecycleOwner) {
            editMode(it || viewModel.createMode)
        }

        binding.fabAddSeminarista.setOnClickListener {
            mMainActivity?.let { mActivity ->
                val builder = EditBrotherDialogFragment.Builder(
                    mActivity, ADD_SEMINARISTA
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

        binding.dataInizioTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataInizioTextField.editText?.setOnKeyListener(null)
        binding.dataInizioTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataInizioTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataInizioTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_inizio)
                        .build()
                picker.show(
                    requireActivity().supportFragmentManager,
                    "dataInizioTextFieldPicker"
                )
                picker.addOnPositiveButtonClickListener {
                    binding.dataInizioTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        binding.dataDecretoTextField.editText?.inputType = InputType.TYPE_NULL
        binding.dataDecretoTextField.editText?.setOnKeyListener(null)
        binding.dataDecretoTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.dataDecretoTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    requireContext(),
                                    binding.dataDecretoTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_decreto)
                        .build()
                picker.show(requireActivity().supportFragmentManager, "dataDecretoTextFieldPicker")
                picker.addOnPositiveButtonClickListener {
                    binding.dataDecretoTextField.editText?.setText(
                        Utility.getStringFromDate(
                            requireContext(),
                            Date(it)
                        )
                    )
                }
            }
            false
        }

        binding.seminaristiRecyclew.adapter = mAdapterSeminaristi
        binding.visiteRecyclew.adapter = mAdapterVisite
        binding.rettoriRecycler.adapter = mAdapterRettori
        binding.vicerettoriRecycler.adapter = mAdapterVice
        binding.spiritualiRecycler.adapter = mAdapterSpirituali

        binding.addRettore.setOnClickListener {
            mAdapterRettori.add(0, ResponsabileListItem())
        }
        binding.addVicerettore.setOnClickListener {
            mAdapterVice.add(0, ResponsabileListItem())
        }
        binding.addSpirituale.setOnClickListener {
            mAdapterSpirituali.add(0, ResponsabileListItem())
        }

        mAdapterRettori.addEventHook(cancellaResponsabileHook)
        mAdapterVice.addEventHook(cancellaResponsabileHook)
        mAdapterSpirituali.addEventHook(cancellaResponsabileHook)


        mAdapterSeminaristi.addEventHooks(
            listOf(
                cancellaSeminaristaHook,
                modificaSeminaristaHook
            )
        )

        binding.materialTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                renderPageView(tab?.position ?: 0)
                tab?.let { viewModel.selectedTabIndex = it.position }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        binding.editSeminario.setOnClickListener {
            viewModel.editMode.value = true
        }

        binding.deleteSeminario.setOnClickListener {
            mMainActivity?.let { mActivity ->
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_SEMINARIO
                    )
                        .title(R.string.delete_seminario)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_seminario_dialog)
                        .positiveButton(R.string.delete_confirm)
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
                lifecycleScope.launch { retrieveData() }
            }
        }

        binding.confirmChanges.setOnClickListener {
            confirmChanges()
        }

        binding.salvaSeminario.setOnClickListener {
            confirmChanges()
        }

        inputdialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "inputDialogViewModel state $it")
            if (!inputdialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        inputdialogViewModel.handled = true
                        val seminaristaItem = seminaristaItem {
                            nome = "AFFIO"
                        }
                        when (inputdialogViewModel.mTag) {
                            ADD_SEMINARISTA -> {
                                mAdapterSeminaristi.add(seminaristaItem)
                                mAdapterSeminaristi.notifyAdapterItemInserted(mAdapterSeminaristi.adapterItemCount - 1)
                                binding.noSeminaristiView.isVisible = false
                            }
                            EDIT_SEMINARISTA -> {
                                mAdapterSeminaristi[viewModel.selectedSeminarista] = seminaristaItem
                                mAdapterSeminaristi.notifyAdapterItemChanged(viewModel.selectedSeminarista)
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
                            DELETE_SEMINARISTA -> {
                                simpleDialogViewModel.handled = true
                                mAdapterSeminaristi.remove(viewModel.selectedSeminarista)
                                binding.noSeminaristiView.isVisible =
                                    mAdapterSeminaristi.itemAdapter.adapterItemCount == 0
                            }
                            DELETE_SEMINARIO -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { deleteSeminario() }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        simpleDialogViewModel.handled = true
                    }
                }
            }
        }

        binding.materialTabs.getTabAt(viewModel.selectedTabIndex)?.select()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putCharSequence("nomeTextField", binding.nomeTextField.editText?.text)
        outState.putCharSequence("dataInizioTextField", binding.dataInizioTextField.editText?.text)
        outState.putCharSequence(
            "dataDecretoTextField",
            binding.dataDecretoTextField.editText?.text
        )
        viewModel.rettori = mAdapterRettori.adapterItems
        viewModel.viceRettori = mAdapterVice.adapterItems
        viewModel.direttoriSpirituali = mAdapterSpirituali.adapterItems
        viewModel.seminaristiItems = mAdapterSeminaristi.adapterItems
        viewModel.visiteItems = mAdapterVisite.adapterItems
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        Log.d(TAG, "onViewStateRestored")
        savedInstanceState?.let { instance ->
            binding.nomeTextField.editText?.setText(instance.getCharSequence("nomeTextField"))
            binding.dataInizioTextField.editText?.setText(instance.getCharSequence("dataInizioTextField"))
            binding.dataDecretoTextField.editText?.setText(instance.getCharSequence("dataDecretoTextField"))
            mAdapterSeminaristi.set(viewModel.seminaristiItems)
            mAdapterVisite.set(viewModel.visiteItems)
            mAdapterRettori.set(viewModel.rettori)
            mAdapterVice.set(viewModel.viceRettori)
            mAdapterSpirituali.set(viewModel.direttoriSpirituali)
        }
    }

    private var cancellaResponsabileHook = object : ClickEventHook<ResponsabileListItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.remove_responsabile)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<ResponsabileListItem>,
            item: ResponsabileListItem
        ) {
            (fastAdapter as? FastItemAdapter)?.remove(position)
            fastAdapter.notifyItemRemoved(position)
        }
    }

    private val cancellaSeminaristaHook = object : ClickEventHook<SeminaristaItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.cancella_seminarista)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SeminaristaItem>,
            item: SeminaristaItem
        ) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedSeminarista = position
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_SEMINARISTA
                    )
                        .title(R.string.delete_seminarista)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_seminarista_dialog)
                        .positiveButton(R.string.delete_confirm)
                        .negativeButton(android.R.string.cancel),
                    mActivity.supportFragmentManager
                )
            }
        }
    }

    private val modificaSeminaristaHook = object : ClickEventHook<SeminaristaItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.modifica_seminarista)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<SeminaristaItem>,
            item: SeminaristaItem
        ) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedSeminarista = position
                val builder = EditBrotherDialogFragment.Builder(
                    mActivity, EDIT_SEMINARISTA
                )
                    .nomePrefill(item.nome)
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

    private fun confirmChanges() {
        if (validateForm()) {
            viewModel.seminario.seminario.nome =
                binding.nomeTextField.editText?.text?.toString().orEmpty().trim()
            viewModel.seminario.seminario.dataInizio = Utility.getDateFromString(
                requireContext(),
                binding.dataInizioTextField.editText?.text?.toString().orEmpty().trim()
            )
            viewModel.seminario.seminario.dataDecreto = Utility.getDateFromString(
                requireContext(),
                binding.dataDecretoTextField.editText?.text?.toString().orEmpty().trim()
            )
            if (viewModel.createMode)
                lifecycleScope.launch { saveSeminario() }
            else
                lifecycleScope.launch {
                    updateSeminario()
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

    private fun renderPageView(tabIndex: Int) {
        binding.seminaristiDetailScrollView.isVisible = tabIndex == 0
        binding.seminaristiRecyclew.isVisible = tabIndex == 2
        binding.noSeminaristiView.isVisible =
            (tabIndex == 2 && mAdapterSeminaristi.itemCount == 0)
        binding.noVisiteView.isVisible =
            (tabIndex == 1 && mAdapterVisite.itemCount == 0)
        binding.fabAddSeminarista.isVisible = (tabIndex == 2 && viewModel.editMode.value == true)
        binding.fabAddVisita.isVisible = (tabIndex == 1 && viewModel.editMode.value == true)
        binding.bottomAppBar.performShow()
    }

    private fun editMode(editMode: Boolean) {
        binding.nomeTextField.isEnabled = editMode
        binding.dataInizioTextField.isEnabled = editMode
        binding.dataDecretoTextField.isEnabled = editMode
        binding.fabAddSeminarista.isVisible = editMode && viewModel.selectedTabIndex == 2
        binding.fabAddVisita.isVisible =
            (viewModel.selectedTabIndex == 1 && viewModel.editMode.value == true)
        binding.editMenu.isVisible = editMode
        binding.seminarioMenu.isVisible = !editMode
        binding.addRettore.isVisible = editMode
        binding.addVicerettore.isVisible = editMode
        binding.addSpirituale.isVisible = editMode
        if (!editMode) {
            binding.nomeTextField.error = null
            binding.dataInizioTextField.error = null
            binding.dataDecretoTextField.error = null
        }
        mAdapterVisite.itemAdapter.adapterItems.forEach { it.editable = editMode }
        mAdapterVisite.notifyAdapterDataSetChanged()
        mAdapterSeminaristi.itemAdapter.adapterItems.forEach { it.editable = editMode }
        mAdapterSeminaristi.notifyAdapterDataSetChanged()
        mAdapterRettori.itemAdapter.adapterItems.forEach { it.editable = editMode }
        mAdapterRettori.notifyAdapterDataSetChanged()
        mAdapterVice.itemAdapter.adapterItems.forEach { it.editable = editMode }
        mAdapterVice.notifyAdapterDataSetChanged()
        mAdapterSpirituali.itemAdapter.adapterItems.forEach { it.editable = editMode }
        mAdapterSpirituali.notifyAdapterDataSetChanged()
    }

    private fun validateForm(): Boolean {
        var valid = true

        if (!requireContext().validateMandatoryField(binding.nomeTextField))
            valid = false

        binding.dataInizioTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataInizioTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataInizioTextField.error = null
        }

        binding.dataDecretoTextField.editText?.let {
            if (!it.text.isNullOrEmpty() &&
                Utility.getDateFromString(
                    requireContext(),
                    it.text.toString()
                ) == null
            ) {
                binding.dataDecretoTextField.error = getString(R.string.invalid_date)
                valid = false
            } else
                binding.dataDecretoTextField.error = null
        }

        return valid
    }

    private suspend fun saveSeminario() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            val insertedId = db.seminarioDao().insertSeminario(viewModel.seminario.seminario)
            Log.d(TAG, "save insertedId : $insertedId")
            insertRelatedTable(insertedId)
        }
        activity?.finishAfterTransition()
    }

    private suspend fun updateSeminario() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())

            //update seminario
            db.seminarioDao()
                .updateSeminario(viewModel.seminario.seminario)

            //truncate related table
            truncateRelatedTables(viewModel.listId)

            //reinsert
            insertRelatedTable(viewModel.listId)

        }
        viewModel.editMode.value = false
    }

    private suspend fun deleteSeminario() {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            truncateRelatedTables(viewModel.listId)
            db.seminarioDao().deleteSeminario(Seminario().apply { id = viewModel.listId })
        }

        if (resources.getBoolean(R.bool.tablet_layout)) {
            val fragment =
                mMainActivity?.supportFragmentManager?.findFragmentByTag(R.id.seminario_detail_fragment.toString())
            fragment?.let {
                mMainActivity?.supportFragmentManager?.beginTransaction()?.remove(it)?.commit()
            }
        } else
            activity?.finishAfterTransition()
    }

    private fun truncateRelatedTables(id: Long) {
        val db = ComunitaDatabase.getInstance(requireContext())

        viewModel.seminaristi.forEach {
            db.comunitaSeminaristaDao().truncateTableBySeminarista(it.seminarista.idSeminarista)
        }
        db.seminaristaDao().truncateTableBySeminario(id)
        db.responsabileSeminarioDao().truncateTableBySeminario(id)
        db.visitaSeminarioDao().truncateTableBySeminario(id)
    }

    private fun insertRelatedTable(id: Long) {
        val db = ComunitaDatabase.getInstance(requireContext())

        mAdapterSeminaristi.adapterItems.forEach {
            val seminarista = Seminarista().apply {
                nome = it.note
                dataNascita = it.dataNascita
                nazione = it.nazione
                comuntiaProvenienza = it.comunitaProvenienza
                catechistiProvenienza = it.catechistiProvenienza
                idTappaProvenienza = it.idTappaProvenienza
                dataEntrata = it.dataEntrata
                dataUscita = it.dataUscita
                motivoUscita = it.motivoUscita
                dataAdmissio = it.dataAdmissio
                dataAccolitato = it.dataAccolitato
                dataLettorato = it.dataLettorato
                dataDiaconato = it.dataDiaconato
                dataPresbiterato = it.dataPresbiterato
                note = it.note
            }
            val insertedSeminaristaId = db.seminaristaDao().insertSeminarista(seminarista)
            it.comunitaList.forEach { com -> com.idSeminarista = insertedSeminaristaId }
            db.comunitaSeminaristaDao().insertComunita(it.comunitaList)
        }

        db.responsabileSeminarioDao()
            .insertResponsabili(mAdapterRettori.adapterItems.filter { it.nomeResponsabile.isNotEmpty() }
                .map {
                    ResponsabileSeminario().apply {
                        idSeminario = id
                        nome = it.nomeResponsabile
                        dataInizioIncarico = it.dataDal
                        dataFineIncarico = it.dataAl
                        incarico = ResponsabileSeminario.Incarico.RETTORE
                    }
                })
        db.responsabileSeminarioDao()
            .insertResponsabili(mAdapterVice.adapterItems.filter { it.nomeResponsabile.isNotEmpty() }
                .map {
                    ResponsabileSeminario().apply {
                        idSeminario = id
                        nome = it.nomeResponsabile
                        dataInizioIncarico = it.dataDal
                        dataFineIncarico = it.dataAl
                        incarico = ResponsabileSeminario.Incarico.VICE_RETTORE
                    }
                })
        db.responsabileSeminarioDao()
            .insertResponsabili(mAdapterSpirituali.adapterItems.filter { it.nomeResponsabile.isNotEmpty() }
                .map {
                    ResponsabileSeminario().apply {
                        idSeminario = id
                        nome = it.nomeResponsabile
                        dataInizioIncarico = it.dataDal
                        dataFineIncarico = it.dataAl
                        incarico = ResponsabileSeminario.Incarico.DIRETTORE_SPIRITUALE
                    }
                })
    }

    private suspend fun retrieveData() {
        Log.d(TAG, "createMode ${viewModel.createMode}")
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            viewModel.seminario = db.seminarioDao()
                .getByIdWithDetails(viewModel.listId)
            viewModel.seminaristi = db.seminaristaDao().getBySeminarioWithDetails(viewModel.listId)
        }
        binding.nomeTextField.editText?.setText(viewModel.seminario.seminario.nome)
        viewModel.seminario.seminario.dataInizio?.let {
            binding.dataInizioTextField.editText?.setText(
                Utility.getStringFromDate(
                    requireContext(),
                    it
                )
            )
        }
        viewModel.seminario.seminario.dataDecreto?.let {
            binding.dataDecretoTextField.editText?.setText(
                Utility.getStringFromDate(
                    requireContext(),
                    it
                )
            )
        }

        mAdapterRettori.set(viewModel.seminario.responsabili.filter { it.incarico == ResponsabileSeminario.Incarico.RETTORE }
            .sortedByDescending { it.dataInizioIncarico }
            .map {
                responsabileListItem {
                    nomeResponsabile = it.nome
                    dataDal = it.dataInizioIncarico
                    dataAl = it.dataFineIncarico
                    editable = false
                }
            })

        mAdapterVice.set(viewModel.seminario.responsabili.filter { it.incarico == ResponsabileSeminario.Incarico.VICE_RETTORE }
            .sortedByDescending { it.dataInizioIncarico }
            .map {
                responsabileListItem {
                    nomeResponsabile = it.nome
                    dataDal = it.dataInizioIncarico
                    dataAl = it.dataFineIncarico
                    editable = false
                }
            })

        mAdapterSpirituali.set(viewModel.seminario.responsabili.filter { it.incarico == ResponsabileSeminario.Incarico.DIRETTORE_SPIRITUALE }
            .sortedByDescending { it.dataInizioIncarico }
            .map {
                responsabileListItem {
                    nomeResponsabile = it.nome
                    dataDal = it.dataInizioIncarico
                    dataAl = it.dataFineIncarico
                    editable = false
                }
            })

        mAdapterSeminaristi.set(viewModel.seminaristi.map {
            seminaristaItem {
                nome = it.seminarista.note
                dataNascita = it.seminarista.dataNascita
                nazione = it.seminarista.nazione
                comunitaProvenienza = it.seminarista.comuntiaProvenienza
                catechistiProvenienza = it.seminarista.catechistiProvenienza
                idTappaProvenienza = it.seminarista.idTappaProvenienza
                dataEntrata = it.seminarista.dataEntrata
                dataUscita = it.seminarista.dataUscita
                motivoUscita = it.seminarista.motivoUscita
                dataAdmissio = it.seminarista.dataAdmissio
                dataAccolitato = it.seminarista.dataAccolitato
                dataLettorato = it.seminarista.dataLettorato
                dataDiaconato = it.seminarista.dataDiaconato
                dataPresbiterato = it.seminarista.dataPresbiterato
                note = it.seminarista.note
                comunitaList = it.comunita
            }
        })

        mAdapterSeminaristi.set(viewModel.seminaristi.map { seminaristaItem { nome = it.seminarista.nome } })

    }

    companion object {
        internal val TAG = SeminarioDetailFragment::class.java.canonicalName
        const val ARG_ITEM_ID = "item_id"
        const val EDIT_MODE = "edit_mode"
        const val CREATE_MODE = "create_mode"
        const val ADD_SEMINARISTA = "add_seminarista"
        const val EDIT_SEMINARISTA = "edit_seminarista"
        const val DELETE_SEMINARISTA = "delete_seminarista"
        const val DELETE_SEMINARIO = "delete_seminario"
        const val ERROR_DIALOG = "community_detail_error_dialog"

    }

}