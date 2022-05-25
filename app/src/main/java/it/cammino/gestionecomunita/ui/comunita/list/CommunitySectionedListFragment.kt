package it.cammino.gestionecomunita.ui.comunita.list

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
import it.cammino.gestionecomunita.util.Utility
import java.util.*

class CommunitySectionedListFragment : Fragment() {

    private val viewModel: CommunityListViewModel by viewModels({ requireParentFragment() })

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

    private val mAdapter: GenericFastItemAdapter = FastItemAdapter()
    private var llm: LinearLayoutManager? = null
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemExpandableExtension = mAdapter.getExpandableExtension()
        itemExpandableExtension.isOnlyOneExpandedItem = true

        mAdapter.onClickListener =
            { _: View?, _: IAdapter<IItem<out RecyclerView.ViewHolder>>, item: IItem<out RecyclerView.ViewHolder>, _: Int ->
                var consume = false
                if (item is CommunitySubItem) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        viewModel.clickedId = item.id
                        viewModel.itemCLickedState.value =
                            CommunityListViewModel.ItemClickState.CLICKED
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

//        binding.extendedFab.setOnClickListener {
//            val fragment: Fragment = CommunityDetailFragment()
//            val args = Bundle()
//            args.putBoolean(CommunityDetailFragment.EDIT_MODE, true)
//            fragment.arguments = args
//            activity?.supportFragmentManager?.commit {
//                replace(
//                    R.id.nav_host_fragment_community_detail,
//                    fragment,
//                    R.id.navigation_home.toString()
//                )
//            }
//        }
    }

    private fun subscribeUiChanges() {
        viewModel.itemsResult?.observe(viewLifecycleOwner) { comunita ->
            val titoliList: ArrayList<IItem<*>> = ArrayList()
            val orderedComunita =
                if (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA) comunita.sortedBy { it.idTappa } else comunita.sortedBy { it.diocesi }
            var mSubItems = LinkedList<ISubItem<*>>()
            var totCanti = 0
            var totListe = 0

            val tappe = resources.getStringArray(R.array.passaggi_entries)

            for (i in orderedComunita.indices) {
                mSubItems.add(
                    communitySubItem {
                        setNumeroComunita = comunita[i].numero
                        setParrocchia = comunita[i].parrocchia
                        setResponsabile = comunita[i].responsabile
                        id = comunita[i].id
                        identifier = (i * 1000).toLong()
                    }
                )
                totCanti++

                if ((i == (comunita.size - 1)
                            || (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA && comunita[i].idTappa != comunita[i + 1].idTappa))
                    || (viewModel.indexType == CommunityListViewModel.IndexType.DIOCESI && comunita[i].diocesi != comunita[i + 1].diocesi)
                ) {
                    // serve a non mettere il divisore sull'ultimo elemento della lista
                    titoliList.add(
                        expandableItem {
                            setTitle =
                                if (viewModel.indexType == CommunityListViewModel.IndexType.TAPPA) tappe[comunita[i].idTappa] else comunita[i].diocesi
                            totItems = totCanti
                            position = totListe++
//                            onPreItemClickListener = { _: View?, _: IAdapter<ExpandableItem>, item: ExpandableItem, _: Int ->
//                                if (!item.isExpanded) {
//                                    if (activityViewModel.isGridLayout)
//                                        glm?.scrollToPositionWithOffset(
//                                            item.position, 0)
//                                    else
//                                        llm?.scrollToPositionWithOffset(
//                                            item.position, 0)
//                                }
//                                false
//                            }
                            identifier = comunita[i].idTappa.toLong()
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
    }

}