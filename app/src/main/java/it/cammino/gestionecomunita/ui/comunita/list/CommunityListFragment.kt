package it.cammino.gestionecomunita.ui.comunita.list

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.*
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.CommunityListItem
import it.cammino.gestionecomunita.item.communityListItem
import it.cammino.gestionecomunita.ui.comunita.ComunitaIndexViewModel
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.getSerializableWrapper

open class CommunityListFragment : Fragment() {

    private val viewModel: CommunityListViewModel by viewModels()
    private val indexViewModel: ComunitaIndexViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentCommunityListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.indexType =
            arguments?.getSerializableWrapper(
                CommunityListViewModel.INDEX_TYPE,
                CommunityListViewModel.IndexType::class.java
            ) as? CommunityListViewModel.IndexType
                ?: CommunityListViewModel.IndexType.TUTTE
    }

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

    private val mAdapter: FastItemAdapter<CommunityListItem> = FastItemAdapter()
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.communityList.setHasFixedSize(true)
        binding.communityList.adapter = mAdapter

        mAdapter.onClickListener =
            { _: View?, _: IAdapter<CommunityListItem>, item: CommunityListItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    indexViewModel.clickedId = item.id
                    indexViewModel.itemCLickedState.value =
                        ComunitaIndexViewModel.ItemClickState.CLICKED
                    consume = true
                }
                consume
            }

        if (viewModel.indexType == CommunityListViewModel.IndexType.TUTTE) {
            activity?.addMenuProvider(object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    // Add menu items here
                    menuInflater.inflate(R.menu.sort_community_menu, menu)
                    menu.findItem(viewModel.selectedSort).isChecked = true
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    // Handle the menu selection
                    return when (menuItem.itemId) {
                        R.id.sort_alphabet,
                        R.id.sort_itineranti -> {
                            menuItem.isChecked = true
                            viewModel.selectedSort = menuItem.itemId
                            sortList()
                            true
                        }
                        else -> false
                    }
                }
            }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        }

        subscribeUiChanges()

    }

    private fun sortList() {
        when (viewModel.selectedSort) {
            R.id.sort_alphabet -> mAdapter.set(mAdapter.adapterItems.sortedBy { it.parrocchia + it.numeroComunita })
            R.id.sort_itineranti -> mAdapter.set(mAdapter.adapterItems.sorted())
        }
    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { comunita ->
            val orderedComunita =
                if (viewModel.indexType == CommunityListViewModel.IndexType.VISITATE_OLTRE_ANNO) comunita.sortedBy { it.dataUltimaVisita } else comunita
            mAdapter.set(orderedComunita
                .map {
                    Log.d(TAG, "get id: ${it.id}")
                    communityListItem {
                        setNumeroComunita = it.numero
                        setParrocchia = it.parrocchia
                        setResponsabile = it.responsabile
                        setCatechisti = it.catechisti
                        id = it.id
                        setDataUltimaVisita = it.dataUltimaVisita
                        setDateMode =
                            viewModel.indexType == CommunityListViewModel.IndexType.VISITATE_OLTRE_ANNO
                    }
                })
            if (viewModel.indexType == CommunityListViewModel.IndexType.TUTTE)
                sortList()
        }
    }


    companion object {
        internal val TAG = CommunityListFragment::class.java.canonicalName

        fun newInstance(indexType: CommunityListViewModel.IndexType): CommunityListFragment {
            val f = CommunityListFragment()
            f.arguments = Bundle()
            f.arguments?.putSerializable(CommunityListViewModel.INDEX_TYPE, indexType)
            return f
        }
    }

}