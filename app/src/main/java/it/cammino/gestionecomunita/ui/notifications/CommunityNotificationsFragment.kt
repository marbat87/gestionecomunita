package it.cammino.gestionecomunita.ui.notifications

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
import com.google.android.material.transition.MaterialContainerTransform
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Promemoria
import it.cammino.gestionecomunita.databinding.CommunityFragmentNotificationsBinding
import it.cammino.gestionecomunita.dialog.AddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.item.PromemoriaItem
import it.cammino.gestionecomunita.item.promemoriaItem
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.util.systemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class CommunityNotificationsFragment : Fragment() {

    private val viewModel: NotificationsViewModel by viewModels()
    private val addNotificationViewMode: AddNotificationDialogFragment.DialogViewModel by viewModels(
        { requireActivity() })
    private val simpleDialogViewModel: SimpleDialogFragment.DialogViewModel by viewModels({ requireActivity() })

    private var _binding: CommunityFragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    private var mAdapter: FastItemAdapter<PromemoriaItem> = FastItemAdapter()

    override fun onCreate(savedInstanceState: Bundle?)  {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = MaterialContainerTransform()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? AppCompatActivity
        viewModel.idComunita = arguments?.getLong(ID_COMUNITA) ?: 0
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = CommunityFragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.promemoriaRecycler.adapter = mAdapter

        binding.promemoriaToolbar.setNavigationOnClickListener {
            parentFragmentManager.popBackStack()
        }

        addNotificationViewMode.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!addNotificationViewMode.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (addNotificationViewMode.mTag) {
                            CommunityDetailFragment.ADD_NOTIFICATION -> {
                                addNotificationViewMode.handled = true
                                lifecycleScope.launch {
                                    addPromemoria(
                                        addNotificationViewMode.idComunita,
                                        addNotificationViewMode.data,
                                        addNotificationViewMode.descrizioneText
                                    )
                                }
                            }
                            CommunityDetailFragment.EDIT_NOTIFICATION -> {
                                addNotificationViewMode.handled = true
                                lifecycleScope.launch {
                                    updatePromemoria(
                                        addNotificationViewMode.idPromemoria,
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

        binding.extendedFabPromemoria.let { fab ->
            mMainActivity?.let { mActivity ->
                fab.setOnClickListener {
                    val builder = AddNotificationDialogFragment.Builder(
                        mActivity, CommunityDetailFragment.ADD_NOTIFICATION
                    )
                        .idComunitaPrefill(viewModel.idComunita)
                    if (resources.getBoolean(R.bool.large_layout)) {
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
        }

        subscribeUiChanges()

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { promemoriaList ->

            mAdapter.set(promemoriaList.filter { it.idComunita == viewModel.idComunita }.map {
                promemoriaItem {
                    id = it.idPromemoria
                    numeroComunita = it.numero
                    parrocchiaComunita = it.parrocchia
                    data = it.data
                    descrizione = it.note
                    idComunita = it.idComunita
                    editClickClickListener = mEditClickClickListener
                    deleteClickClickListener = mDeleteClickClickListener
                }
            })

            binding.noPromemoria.isVisible = mAdapter.adapterItemCount == 0
        }

        simpleDialogViewModel.state.observe(viewLifecycleOwner) {
            Log.d(TAG, "simpleDialogViewModel state $it")
            if (!simpleDialogViewModel.handled) {
                when (it) {
                    is DialogState.Positive -> {
                        when (simpleDialogViewModel.mTag) {
                            DELETE_PROMEMORIA -> {
                                simpleDialogViewModel.handled = true
                                lifecycleScope.launch { rimuoviPromemoria(0.toLong(), true) }
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

    private val mDeleteClickClickListener = object : PromemoriaItem.OnClickListener {
        override fun onClick(it: PromemoriaItem) {
            lifecycleScope.launch { rimuoviPromemoria(it.id) }
        }
    }

    private val mEditClickClickListener = object : PromemoriaItem.OnClickListener {
        override fun onClick(it: PromemoriaItem) {
            mMainActivity?.let { mActivity ->
                val builder = AddNotificationDialogFragment.Builder(
                    mActivity, CommunityDetailFragment.EDIT_NOTIFICATION
                )
                    .setEditMode(true)
                    .idComunitaPrefill(it.idComunita)
                    .idPromemoria(it.id)
                    .datePrefill(it.data)
                    .notaPrefill(it.descrizione)
                if (resources.getBoolean(R.bool.large_layout)) {
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

    private suspend fun updatePromemoria(
        idPromemoria: Long,
        idComunita: Long,
        data: Date?,
        descrizione: String
    ) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            val promemoria = Promemoria()
            promemoria.idPromemoria = idPromemoria
            promemoria.note = descrizione
            promemoria.idComunita = idComunita
            promemoria.data = data
            db.promemoriaDao().updatePromemoria(promemoria)
        }
        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.promemoria_modificato),
            Snackbar.LENGTH_SHORT
        )
            .show()
    }

    private suspend fun rimuoviPromemoria(idPromemoria: Long, confirmed: Boolean = false) {
        if (confirmed) {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                viewModel.removedPromemoria?.let {
                    ComunitaDatabase.getInstance(requireContext()).promemoriaDao()
                        .deletePromemoria(it)
                }
            }
            Snackbar.make(
                requireActivity().findViewById(android.R.id.content),
                getString(R.string.promemoria_rimosso),
                Snackbar.LENGTH_SHORT
            )
                .setAction(
                    getString(android.R.string.cancel).uppercase(
                        resources.systemLocale
                    )
                ) {
                    viewModel.removedPromemoria?.let {
                        lifecycleScope.launch {
                            restorePromemoria(it)
                        }
                    }
                }.show()
        } else {
            withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
                viewModel.removedPromemoria =
                    ComunitaDatabase.getInstance(requireContext()).promemoriaDao()
                        .getById(idPromemoria)
            }
            mMainActivity?.let { mActivity ->
                SimpleDialogFragment.show(
                    SimpleDialogFragment.Builder(
                        mActivity,
                        DELETE_PROMEMORIA
                    )
                        .title(R.string.delete_promemoria)
                        .icon(R.drawable.delete_24px)
                        .content(R.string.delete_promemoria_dialog)
                        .positiveButton(R.string.delete_confirm)
                        .negativeButton(android.R.string.cancel),
                    mActivity.supportFragmentManager
                )
            }
        }
    }

    private suspend fun restorePromemoria(promemoria: Promemoria) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).promemoriaDao()
                .insertPromemoria(promemoria)
        }
    }

    companion object {
        private val TAG = CommunityNotificationsFragment::class.java.canonicalName
        private const val ID_COMUNITA = "id_comunita"
        private const val DELETE_PROMEMORIA = "delete_promemoria"

        fun newInstance(idComunita: Long): CommunityNotificationsFragment {
            val f = CommunityNotificationsFragment()
            f.arguments = Bundle()
            f.arguments?.putLong(ID_COMUNITA, idComunita)
            return f
        }
    }

}