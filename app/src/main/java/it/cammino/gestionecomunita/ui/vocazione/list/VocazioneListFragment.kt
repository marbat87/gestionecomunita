package it.cammino.gestionecomunita.ui.vocazione.list

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.Vocazione
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.VocazioneListItem
import it.cammino.gestionecomunita.item.vocazioneListItem
import it.cammino.gestionecomunita.ui.vocazione.CentroVocazionaleViewModel
import it.cammino.gestionecomunita.util.Utility

open class VocazioneListFragment : Fragment() {

    private val viewModel: VocazioneListViewModel by viewModels()
    private val indexViewModel: CentroVocazionaleViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentCommunityListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val mAdapter: FastItemAdapter<VocazioneListItem> = FastItemAdapter()
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.communityList.setHasFixedSize(true)
        binding.communityList.adapter = mAdapter

        mAdapter.onClickListener =
            { _: View?, _: IAdapter<VocazioneListItem>, item: VocazioneListItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    indexViewModel.clickedId = item.id
                    indexViewModel.itemCLickedState.value =
                        CentroVocazionaleViewModel.ItemClickState.CLICKED
                    consume = true
                }
                consume
            }

        activity?.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.filter_vocation_menu, menu)
                menu.findItem(viewModel.selectedFilter).isChecked = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.filter_male,
                    R.id.filter_female,
                    R.id.filter_all -> {
                        menuItem.isChecked = true
                        viewModel.selectedFilter = menuItem.itemId
                        filterList()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        subscribeUiChanges()

    }

    private fun filterList() {
        when (viewModel.selectedFilter) {
            R.id.filter_male -> mAdapter.set(viewModel.vocazioniList.filter { it.sesso == Vocazione.Sesso.MASCHIO })
            R.id.filter_female -> mAdapter.set(viewModel.vocazioniList.filter { it.sesso == Vocazione.Sesso.FEMMINA })
            R.id.filter_all -> mAdapter.set(viewModel.vocazioniList)
        }
    }

    private fun subscribeUiChanges() {
        viewModel.vocazioniLiveList?.observe(viewLifecycleOwner) { vocazioni ->
            viewModel.vocazioniList = vocazioni
                .map {
                    Log.d(TAG, "get id: ${it.idVocazione}")
                    vocazioneListItem {
                        setNome = it.nome
                        setSesso = it.sesso
                        id = it.idVocazione
                    }
                }
            binding.noVocazioniView.isVisible = viewModel.vocazioniList.isEmpty()
            filterList()
        }
    }

    companion object {
        internal val TAG = VocazioneListFragment::class.java.canonicalName
    }

}