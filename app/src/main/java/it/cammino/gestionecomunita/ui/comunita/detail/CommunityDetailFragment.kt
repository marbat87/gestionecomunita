package it.cammino.gestionecomunita.ui.comunita.detail

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.tabs.TabLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.databinding.FragmentCommunityDetailBinding
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


class CommunityDetailFragment : Fragment() {

    private val mViewModel: CommunityDetailViewModel by viewModels()

    private var _binding: FragmentCommunityDetailBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            arguments?.let {
                // Load the placeholder content specified by the fragment
                // arguments. In a real-world scenario, use a Loader
                // to load content from a content provider.
                mViewModel.listId = it.getInt(ARG_ITEM_ID)
                mViewModel.editMode.value = it.getBoolean(EDIT_MODE, true)
                mViewModel.createMode = it.getBoolean(CREATE_MODE, true)
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

        if (savedInstanceState == null)
            lifecycleScope.launch { retrieveData() }

        editMode(mViewModel.editMode.value == true || mViewModel.createMode)
        mViewModel.editMode.observe(viewLifecycleOwner) {
            editMode(it || mViewModel.createMode)
        }

        binding.fabAddBrother.setOnClickListener {
//            mViewModel.editMode.value = true
        }

        binding.tappaAutcomplete.setOnItemClickListener { _, _, i, _ ->
            mViewModel.comunita.idTappa = i
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
                tab?.let { mViewModel.selectedTabIndex = it.position }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

        binding.bottomAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.edit_community -> {
                    mViewModel.editMode.value = true
                    true
                }
                R.id.cancel_change -> {
                    mViewModel.editMode.value = false
                    lifecycleScope.launch { retrieveData() }
                    true
                }
                R.id.confirm_changes -> {
                    if (validateForm()) {
                        mViewModel.comunita.diocesi =
                            binding.diocesiTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.numero =
                            binding.numeroTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.parrocchia =
                            binding.parrocchiaTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.email =
                            binding.emailTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.responsabile =
                            binding.responsabileTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.telefono =
                            binding.telefonoTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.dataConvivenza = Utility.getDateFromString(
                            requireContext(),
                            binding.dataConvivenzaTextField.editText?.text?.toString() ?: ""
                        )
                        mViewModel.comunita.dataUltimaVisita = Utility.getDateFromString(
                            requireContext(),
                            binding.dataVisitaTextField.editText?.text?.toString() ?: ""
                        )
                        mViewModel.comunita.note =
                            binding.noteTextField.editText?.text?.toString() ?: ""
                        mViewModel.comunita.dataUltimaModifica =
                            Date(Calendar.getInstance().time.time)

                        if (mViewModel.createMode)
                            lifecycleScope.launch { saveComunita(mViewModel.comunita) }
                        else
                            lifecycleScope.launch {
                                updateComunita(mViewModel.comunita)
                                retrieveData()
                            }
                    }
                    true
                }
                else -> false
            }
        }

        lifecycleScope.launch {
            delay(500)
            binding.materialTabs.getTabAt(mViewModel.selectedTabIndex)?.select()
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

    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        binding.lastEditDate.text = getString(
            R.string.data_ultima_modifica,
            getTimestampFormatted(mViewModel.comunita.dataUltimaModifica)
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
    }

    private fun showGeneralOrBrothers(generalDetails: Boolean) {
        binding.brothersList.isVisible = !generalDetails
        binding.fabAddBrother.isVisible = !generalDetails && mViewModel.editMode.value == true
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
        binding.fabAddBrother.isVisible = editMode && mViewModel.selectedTabIndex == 1
        binding.bottomAppBar.menu.clear()
        binding.bottomAppBar.inflateMenu(if (editMode) R.menu.bottom_app_bar_edit_menu else R.menu.bottom_app_bar_menu)
        if (!editMode) {
            binding.numeroTextField.error = null
            binding.parrocchiaTextField.error = null
            binding.tappaTextField.error = null
            binding.emailTextField.error = null
            binding.dataConvivenzaTextField.error = null
            binding.dataVisitaTextField.error = null
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

    private suspend fun saveComunita(comunita: Comunita) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).comunitaDao().insertComunita(comunita)
        }
//        activity?.supportFragmentManager?.commit {
//            replace(
//                R.id.nav_host_fragment_community_detail,
//                CommunityListFragment(),
//                R.id.navigation_home.toString()
//            )
        activity?.finish()
    }

    private suspend fun updateComunita(comunita: Comunita) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).comunitaDao().updateComnuita(comunita)
        }
        mViewModel.editMode.value = false
    }

    private suspend fun retrieveData() {
        if (!mViewModel.createMode) {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                val comunita = ComunitaDatabase.getInstance(requireContext()).comunitaDao()
                    .getById(mViewModel.listId)
                if (comunita != null)
                    mViewModel.comunita = comunita
            }
            binding.lastEditDate.text = getString(
                R.string.data_ultima_modifica,
                getTimestampFormatted(mViewModel.comunita.dataUltimaModifica)
            )
            binding.diocesiTextField.editText?.setText(mViewModel.comunita.diocesi)
            binding.numeroTextField.editText?.setText(mViewModel.comunita.numero)
            binding.parrocchiaTextField.editText?.setText(mViewModel.comunita.parrocchia)
            binding.emailTextField.editText?.setText(mViewModel.comunita.email)
            binding.responsabileTextField.editText?.setText(mViewModel.comunita.responsabile)
            binding.telefonoTextField.editText?.setText(mViewModel.comunita.telefono)
            if (mViewModel.comunita.idTappa != -1)
                binding.tappaAutcomplete.setText(
                    requireContext().resources.getTextArray(R.array.passaggi_entries)[mViewModel.comunita.idTappa],
                    false
                )
            else {
                binding.tappaAutcomplete.text = null
            }
            mViewModel.comunita.dataConvivenza?.let {
                binding.dataConvivenzaTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }
            mViewModel.comunita.dataUltimaVisita?.let {
                binding.dataVisitaTextField.editText?.setText(
                    Utility.getStringFromDate(
                        requireContext(),
                        it
                    )
                )
            }
            binding.noteTextField.editText?.setText(mViewModel.comunita.note)
        }
    }

    companion object {
        //        internal val TAG = CommunityDetailFragment::class.java.canonicalName
        const val ARG_ITEM_ID = "item_id"
        const val EDIT_MODE = "edit_mode"
        const val CREATE_MODE = "create_mode"
    }

}