package it.cammino.gestionecomunita.ui.vocazione.incontri

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.listeners.ClickEventHook
import com.mikepenz.itemanimators.SlideRightAlphaAnimator
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.IncontroVocazionale
import it.cammino.gestionecomunita.databinding.FragmentVocazioniMeetingsBinding
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.item.ExpandableVocazioneMeetingItem
import it.cammino.gestionecomunita.item.expandableVocazioneMeetingItem
import it.cammino.gestionecomunita.ui.AccountMenuFragment
import it.cammino.gestionecomunita.util.setEnterTransition
import it.cammino.gestionecomunita.util.systemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class IncontriVocazioneFragment : AccountMenuFragment() {

    private val viewModel: IncontriVocazioneViewModel by viewModels()
    private val addNotificationViewModel: EditVocazioneMeetingDialogFragment.DialogViewModel by viewModels(
        { requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: FragmentVocazioniMeetingsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mAdapter: FastItemAdapter<ExpandableVocazioneMeetingItem> = FastItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEnterTransition()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVocazioniMeetingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.incontroRecycler.adapter = mAdapter
        binding.incontroRecycler.itemAnimator = SlideRightAlphaAnimator()

        subscribeUiChanges()

        mAdapter.addEventHooks(
            listOf(
                cancellaIncontroHook,
                modificaIncontroHook,
                expandCollapeHook
            )
        )

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { incontroList ->
            var lastPosition = 0
            mAdapter.set(incontroList.map {
                expandableVocazioneMeetingItem {
                    id = it.idIncontro
                    tipo = it.tipo
                    dataIncontro = it.data
                    luogoIncontro = it.luogo
                    note = it.note
                    position = lastPosition++
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
                                        addNotificationViewModel.tipo,
                                        addNotificationViewModel.dataIncontro,
                                        addNotificationViewModel.luogoText,
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

    private var cancellaIncontroHook = object : ClickEventHook<ExpandableVocazioneMeetingItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.cancella_incontro)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<ExpandableVocazioneMeetingItem>,
            item: ExpandableVocazioneMeetingItem
        ) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedIncontroId = item.id
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

    private var modificaIncontroHook = object : ClickEventHook<ExpandableVocazioneMeetingItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.modifica_incontro)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<ExpandableVocazioneMeetingItem>,
            item: ExpandableVocazioneMeetingItem
        ) {
            mMainActivity?.let { mActivity ->
                viewModel.selectedIncontroId = item.id
                val builder = EditVocazioneMeetingDialogFragment.Builder(
                    mActivity, EDIT_INCONTRO
                )
                    .tipoPrefill(item.tipo)
                    .dataIncontroPrefill(item.dataIncontro)
                    .luogoPrefill(item.luogoIncontro)
                    .notePrefill(item.note)
                    .setEditMode(true)
                if (mActivity.resources.getBoolean(R.bool.large_layout)) {
                    builder.positiveButton(R.string.save)
                        .negativeButton(android.R.string.cancel)
                    LargeEditVocazioneMeetingDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                } else {
                    SmallEditVocazioneMeetingDialogFragment.show(
                        builder,
                        mActivity.supportFragmentManager
                    )
                }
            }
        }
    }

    private val expandCollapeHook = object : ClickEventHook<ExpandableVocazioneMeetingItem>() {
        override fun onBind(viewHolder: RecyclerView.ViewHolder): View? {
            //return the views on which you want to bind this event
            return viewHolder.itemView.findViewById(R.id.title_section)
        }

        override fun onClick(
            v: View,
            position: Int,
            fastAdapter: FastAdapter<ExpandableVocazioneMeetingItem>,
            item: ExpandableVocazioneMeetingItem
        ) {
            ViewCompat.animate(v.findViewById(R.id.group_indicator))
                .rotation(if (item.isExpanded) 180f else 0f)
                .start()
            item.isExpanded = !item.isExpanded
            fastAdapter.notifyItemChanged(item.position)
        }
    }

    private suspend fun addOrUpdateIncontro(
        tipo: IncontroVocazionale.Tipo,
        dataIncontro: Date?,
        luogo: String,
        note: String,
        idIncontro: Long,
        update: Boolean = false
    ) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            val incontro = IncontroVocazionale()
            incontro.tipo = tipo
            incontro.data = dataIncontro
            incontro.luogo = luogo
            incontro.note = note
            if (update) {
                incontro.idIncontro = idIncontro
                db.incontroVocazionaleDao().updateIncontroVocazionale(incontro)
            } else
                db.incontroVocazionaleDao().insertIncontroVocazionale(incontro)
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

            viewModel.removedIncontro = db.incontroVocazionaleDao().getById(idIncontro)
            viewModel.removedIncontro?.let {
                db.incontroVocazionaleDao().deleteIncontroVocazionale(it)
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

    private suspend fun restoreIncontro(incontro: IncontroVocazionale) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).incontroVocazionaleDao()
                .insertIncontroVocazionale(incontro)
        }
    }

    companion object {
        private val TAG = IncontriVocazioneFragment::class.java.canonicalName
        const val ADD_INCONTRO = "add_incontro_vocazione"
        const val EDIT_INCONTRO = "edit_incontro_vocazione"
        const val DELETE_INCONTRO = "delete_incontro_vocazione"
    }

}