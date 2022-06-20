package it.cammino.gestionecomunita.ui.comunita.list

import android.content.Context
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.IItem
import com.mikepenz.fastadapter.ISubItem
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import com.mikepenz.fastadapter.adapters.GenericFastItemAdapter
import com.mikepenz.fastadapter.expandable.getExpandableExtension
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.CommunitySubItem
import it.cammino.gestionecomunita.item.ExpandableItem
import it.cammino.gestionecomunita.item.communitySubItem
import it.cammino.gestionecomunita.item.expandableItem
import it.cammino.gestionecomunita.ui.comunita.ComunitaIndexViewModel
import it.cammino.gestionecomunita.util.Utility
import java.util.*
import kotlin.math.floor

class CommunitySectionedListFragment : Fragment() {

    private val viewModel: CommunityListViewModel by viewModels()
    private val indexViewModel: ComunitaIndexViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentCommunityListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel.indexType =
            arguments?.getSerializable(CommunityListViewModel.INDEX_TYPE) as? CommunityListViewModel.IndexType
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

    private val mAdapter: GenericFastItemAdapter = FastItemAdapter()
    private var llm: LinearLayoutManager? = null
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemExpandableExtension = mAdapter.getExpandableExtension()
        itemExpandableExtension.isOnlyOneExpandedItem = true

        mAdapter.onClickListener =
            { _, _, item, _ ->
                var consume = false
                if (item is CommunitySubItem) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        indexViewModel.clickedId = item.id
                        indexViewModel.itemCLickedState.value =
                            ComunitaIndexViewModel.ItemClickState.CLICKED
                        consume = true
                    }
                }
                consume
            }

        mAdapter.onPreClickListener =
            { _: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
                if (item is ExpandableItem) {
                    Log.i(TAG, "item.position ${item.position}")
                    if (!item.isExpanded) {
                        llm?.scrollToPositionWithOffset(
                            item.position, 0
                        )
                    }
                }
                false
            }

        llm = binding.communityList.layoutManager as? LinearLayoutManager
        binding.communityList.setHasFixedSize(true)
        binding.communityList.adapter = mAdapter

        subscribeUiChanges()

    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { comunita ->
            val titoliList: ArrayList<IItem<*>> = ArrayList()
            val orderedComunita =
                if (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA) comunita.sortedBy { it.idTappa } else comunita.sortedBy {
                    it.diocesi.trim().lowercase()
                }
            var mSubItems = LinkedList<ISubItem<*>>()
            var totCanti = 0
            var totListe = 0

            val tappe = resources.getStringArray(R.array.passaggi_entries)

            for (i in orderedComunita.indices) {
                mSubItems.add(
                    communitySubItem {
                        setNumeroComunita = orderedComunita[i].numero
                        setParrocchia = orderedComunita[i].parrocchia
                        setResponsabile = orderedComunita[i].responsabile
                        id = orderedComunita[i].id
                        identifier = ((i + 1) * 100).toLong()
                    }
                )
                totCanti++

                if ((i == (orderedComunita.size - 1)
                            || (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA && orderedComunita[i].idTappa != orderedComunita[i + 1].idTappa))
                    || (viewModel.indexType == CommunityListViewModel.IndexType.DIOCESI && orderedComunita[i].diocesi.trim()
                        .lowercase() != orderedComunita[i + 1].diocesi.trim().lowercase())
                ) {
                    // serve a non mettere il divisore sull'ultimo elemento della lista
                    titoliList.add(
                        expandableItem {
                            setTitle =
                                if (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA) tappe[orderedComunita[i].idTappa] else orderedComunita[i].diocesi
                            totItems = totCanti
                            position = totListe++
                            identifier =
                                if (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA) orderedComunita[i].idTappa.toLong() else floor(
                                    Math.random() * 10000
                                ).toLong()
                            subItems = mSubItems
                            subItems.sortBy {
                                (it as? CommunitySubItem)?.parrocchia?.getText(
                                    requireContext()
                                ) + (it as? CommunitySubItem)?.numeroComunita?.getText(
                                    requireContext()
                                )
                            }
                        })
                    mSubItems = LinkedList()
                    totCanti = 0
                }
            }
            mAdapter.set(titoliList)
        }
    }

    companion object {
        private val TAG = CommunitySectionedListFragment::class.java.canonicalName

        fun newInstance(indexType: CommunityListViewModel.IndexType): CommunitySectionedListFragment {
            val f = CommunitySectionedListFragment()
            f.arguments = Bundle()
            f.arguments?.putSerializable(CommunityListViewModel.INDEX_TYPE, indexType)
            return f
        }
    }

}