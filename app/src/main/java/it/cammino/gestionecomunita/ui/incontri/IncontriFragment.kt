package it.cammino.gestionecomunita.ui.incontri

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.databinding.FragmentMeetingsBinding
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditMeetingDialogFragment
import it.cammino.gestionecomunita.item.ExpandableMeetingItem
import it.cammino.gestionecomunita.item.expandableMeetingItem
import it.cammino.gestionecomunita.util.systemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class IncontriFragment : Fragment() {

    private val viewModel: IncontriViewModel by viewModels()
    private val addNotificationViewModel: EditMeetingDialogFragment.DialogViewModel by viewModels({ requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentMeetingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    private var mAdapter: FastItemAdapter<ExpandableMeetingItem> = FastItemAdapter()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? AppCompatActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMeetingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.incontroRecycler.adapter = mAdapter
        subscribeUiChanges()

        binding.extendedFabIncontro?.let { fab ->
            mMainActivity?.let { mActivity ->
                fab.setOnClickListener {
                    val builder = EditMeetingDialogFragment.Builder(
                        mActivity, ADD_INCONTRO
                    )
                    if (resources.getBoolean(R.bool.large_layout)) {
                        builder.positiveButton(R.string.save)
                            .negativeButton(android.R.string.cancel)
                        LargeEditMeetingDialogFragment.show(
                            builder,
                            mActivity.supportFragmentManager
                        )
                    } else {
                        SmallEditMeetingDialogFragment.show(
                            builder,
                            mActivity.supportFragmentManager
                        )
                    }
                }
            }
        }

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { incontroList ->
            var lastPosition = 0
            mAdapter.set(incontroList.map {
                expandableMeetingItem {
                    id = it.idIncontro
                    nome = it.nome
                    cognome = it.cognome
                    dataIncontro = it.data
                    luogoIncontro = it.luogo
                    idComunita = it.idComunita
                    numeroComunita = it.numero
                    parrocchiaComunita = it.parrocchia
                    note = it.note
                    position = lastPosition++
                    editClickClickListener = mEditClickClickListener
                    deleteClickClickListener = mDeleteClickClickListener
                    expandClickClickListener = mExpandClickClickListener
                }
            })

            binding.noIncontri.isVisible = mAdapter.adapterItemCount == 0
        }

        addNotificationViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "inputDialogViewModel state $it")
            if (!addNotificationViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (addNotificationViewModel.mTag) {
                            ADD_INCONTRO, EDIT_INCONTRO -> {
                                addNotificationViewModel.handled = true
                                lifecycleScope.launch {
                                    addOrUpdateIncontro(
                                        addNotificationViewModel.nomeText,
                                        addNotificationViewModel.cognomeText,
                                        addNotificationViewModel.dataIncontro,
                                        addNotificationViewModel.luogoText,
                                        addNotificationViewModel.idComunita,
                                        addNotificationViewModel.noteText,
                                        if (addNotificationViewModel.mTag == EDIT_INCONTRO) viewModel.selectedIncontroId else 0,
                                        addNotificationViewModel.mTag == EDIT_INCONTRO
                                    )
                                }
                            }
                        }
                    }
                    is DialogState.Negative -> {
                        addNotificationViewModel.handled = true
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
                            DELETE_INCONTRO -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { removeIncontro(viewModel.selectedIncontroId) }
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

    private val mDeleteClickClickListener = object : ExpandableMeetingItem.OnClickListener {
        override fun onClick(it: ExpandableMeetingItem) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedIncontroId = it.id
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_INCONTRO
                    )
                        .title(R.string.delete_incontro)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_incontro_dialog)
                        .positiveButton(R.string.delete_confirm)
                        .negativeButton(android.R.string.cancel),
                    mActivity.supportFragmentManager
                )
            }
        }
    }

    private val mEditClickClickListener = object : ExpandableMeetingItem.OnClickListener {
        override fun onClick(it: ExpandableMeetingItem) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedIncontroId = it.id
                val builder = EditMeetingDialogFragment.Builder(
                    mActivity, EDIT_INCONTRO
                )
                    .nomePrefill(it.nome)
                    .cognomePrefill(it.cognome)
                    .dataIncontroPrefill(it.dataIncontro)
                    .luogoPrefill(it.luogoIncontro)
                    .comunitaPrefill(it.idComunita)
                    .notePrefill(it.note)
                    .setEditMode(true)
                if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeEditMeetingDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                } else {
                    SmallEditMeetingDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                }
            }
        }
    }

    private val mExpandClickClickListener = object : ExpandableMeetingItem.OnClickListener {
        override fun onClick(it: ExpandableMeetingItem) {
            mAdapter.notifyItemChanged(it.position)
        }
    }

    private suspend fun addOrUpdateIncontro(
        nome: String,
        cognome: String,
        dataIncontro: Date?,
        luogo: String,
        comunita: Long,
        note: String,
        idIncontro: Long,
        update: Boolean = false
    ) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            val incontro = Incontro()
            incontro.nome = nome
            incontro.cognome = cognome
            incontro.data = dataIncontro
            incontro.luogo = luogo
            incontro.idComunita = comunita
            incontro.note = note
            if (update) {
                incontro.idIncontro = idIncontro
                db.incontroDao().updateIncontro(incontro)
            } else
                db.incontroDao().insertIncontro(incontro)
        }
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(if (update) R.string.incontro_modificato else R.string.incontro_aggiunto),
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    private suspend fun removeIncontro(idIncontro: Long) {
        var rimosso = false
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())

            viewModel.removedIncontro = db.incontroDao().getIncontroById(idIncontro)
            viewModel.removedIncontro?.let {
                db.incontroDao().deleteIncontro(it)
                rimosso = true
            }
        }
        if (rimosso)
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                getString(R.string.incontro_cancellato),
                Snackbar.LENGTH_SHORT
            ).setAction(getString(android.R.string.cancel).uppercase(resources.systemLocale)) {
                viewModel.removedIncontro?.let {
                    lifecycleScope.launch {
                        restoreIncontro(it)
                    }
                }
            }
                .show()
    }

    private suspend fun restoreIncontro(incontro: Incontro) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).incontroDao()
                .insertIncontro(incontro)
        }
    }

    companion object {
        private val TAG = IncontriFragment::class.java.canonicalName
        const val ADD_INCONTRO = "add_incontro"
        const val EDIT_INCONTRO = "edit_incontro"
        const val DELETE_INCONTRO = "delete_incontro"
    }

}