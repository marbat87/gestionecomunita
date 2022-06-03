package it.cammino.gestionecomunita.ui.notifications

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.snackbar.Snackbar
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.drag.ItemTouchCallback
import com.mikepenz.fastadapter.swipe.SimpleSwipeCallback
import com.mikepenz.fastadapter.swipe_drag.SimpleSwipeDragCallback
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.ComunitaDatabase
import it.cammino.gestionecomunita.database.entity.Promemoria
import it.cammino.gestionecomunita.databinding.FragmentNotificationsBinding
import it.cammino.gestionecomunita.dialog.AddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.large.LargeAddNotificationDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallAddNotificationDialogFragment
import it.cammino.gestionecomunita.item.SwipeableItem
import it.cammino.gestionecomunita.item.swipeableItem
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.util.systemLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.Date

class NotificationsFragment : Fragment(), ItemTouchCallback, SimpleSwipeCallback.ItemSwipeCallback {

    private val viewModel: NotificationsViewModel by viewModels()
    private val addNotificationViewMode: AddNotificationDialogFragment.DialogViewModel by viewModels(
        { requireActivity() })

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var mMainActivity: AppCompatActivity? = null

    private var mAdapter: FastItemAdapter<SwipeableItem> = FastItemAdapter()

    // drag & drop
    private var mTouchHelper: ItemTouchHelper? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? AppCompatActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.promemoriaRecycler.adapter = mAdapter
        subscribeUiChanges()

        AppCompatResources.getDrawable(requireContext(), R.drawable.delete_sweep_24px)
            ?.let {
                it.setTint(
                    MaterialColors.getColor(
                        view,
                        com.google.android.material.R.attr.colorOnPrimary
                    )
                )
                val touchCallback = SimpleSwipeDragCallback(
                    this,
                    this,
                    it,
                    ItemTouchHelper.LEFT,
                    MaterialColors.getColor(
                        requireContext(),
                        com.google.android.material.R.attr.colorPrimary,
                        TAG
                    )
                )
                    .withBackgroundSwipeRight(
                        MaterialColors.getColor(
                            requireContext(),
                            com.google.android.material.R.attr.colorPrimary,
                            TAG
                        )
                    )
                    .withLeaveBehindSwipeRight(it)

                touchCallback.setIsDragEnabled(false)
                touchCallback.notifyAllDrops = true

                mTouchHelper =
                    ItemTouchHelper(touchCallback) // Create ItemTouchHelper and pass with parameter the SimpleDragCallback

            }
        mTouchHelper?.attachToRecyclerView(binding.promemoriaRecycler) // Attach ItemTouchHelper to RecyclerView

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
                        }
                    }
                    is DialogState.Negative -> {
                        addNotificationViewMode.handled = true
                    }
                }
            }
        }

        binding.extendedFabPromemoria?.let { fab ->
            mMainActivity?.let { mActivity ->
                fab.setOnClickListener {
                    val builder = AddNotificationDialogFragment.Builder(
                        mActivity, CommunityDetailFragment.ADD_NOTIFICATION
                    )
                        .setFreeMode(true)
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

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { promemoriaList ->

            mAdapter.set(promemoriaList.map {
                swipeableItem {
                    id = it.idPromemoria
                    numeroComunita = it.numero
                    parrocchiaComunita = it.parrocchia
                    data = it.data
                    descrizione = it.note
                }
            })

            binding.noPromemoria.isVisible = mAdapter.adapterItemCount == 0
        }
    }

    override fun itemSwiped(position: Int, direction: Int) {
        val item = mAdapter.getItem(position) ?: return
        lifecycleScope.launch { rimuoviPromemoria(item) }
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

    private suspend fun rimuoviPromemoria(item: SwipeableItem) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            val db = ComunitaDatabase.getInstance(requireContext())
            viewModel.removedPromemoria = db.promemoriaDao().getById(item.id)
            viewModel.removedPromemoria?.let {
                db.promemoriaDao().deletePromemoria(it)
            }
        }

        Snackbar.make(
            requireActivity().findViewById(android.R.id.content),
            getString(R.string.promemoria_rimosso),
            Snackbar.LENGTH_SHORT
        )
            .setAction(getString(android.R.string.cancel).uppercase(resources.systemLocale)) {
                viewModel.removedPromemoria?.let {
                    lifecycleScope.launch {
                        restorePromemoria(it)
                    }
                }
            }.show()
    }

    private suspend fun restorePromemoria(promemoria: Promemoria) {
        withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            ComunitaDatabase.getInstance(requireContext()).promemoriaDao()
                .insertPromemoria(promemoria)
        }
    }

    override fun itemTouchOnMove(oldPosition: Int, newPosition: Int): Boolean {
        return false
    }

    companion object {
        private val TAG = NotificationsFragment::class.java.canonicalName
    }

}