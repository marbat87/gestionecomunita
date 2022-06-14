package it.cammino.gestionecomunita.ui.vocazione.list

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
import it.cammino.gestionecomunita.item.*
import it.cammino.gestionecomunita.ui.vocazione.CentroVocazionaleViewModel
import it.cammino.gestionecomunita.util.Utility
import java.util.*

class VocazioneSectionedListFragment : Fragment() {

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
                if (item is VocazioneSubItem) {
                    if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                        mLastClickTime = SystemClock.elapsedRealtime()
                        indexViewModel.clickedId = item.id
                        indexViewModel.itemCLickedState.value =
                            CentroVocazionaleViewModel.ItemClickState.CLICKED
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
        viewModel.vocazioniLiveList?.observe(viewLifecycleOwner) { vocazioni ->
            val vocazioniList: ArrayList<IItem<*>> = ArrayList()
            val orderedVocazioni = vocazioni.sortedBy { it.idTappa }
            var mSubItems = LinkedList<ISubItem<*>>()
            var totVocazioni = 0
            var totListe = 0

            val tappe = resources.getStringArray(R.array.passaggi_entries)

            for (i in orderedVocazioni.indices) {
                mSubItems.add(
                    vocazioneSubItem {
                        setNome = orderedVocazioni[i].nome
                        setSesso = orderedVocazioni[i].sesso
                        id = orderedVocazioni[i].idVocazione
                    }
                )
                totVocazioni++

                if ((i == (orderedVocazioni.size - 1)
                            || orderedVocazioni[i].idTappa != orderedVocazioni[i + 1].idTappa)
                ) {
                    vocazioniList.add(
                        expandableItem {
                            setTitle =
                                if (orderedVocazioni[i].idTappa == -1) getString(R.string.nessun_passaggio) else tappe[orderedVocazioni[i].idTappa]
                            totItems = totVocazioni
                            position = totListe++
                            identifier = ((orderedVocazioni[i].idTappa + 1) * 100000000).toLong()
                            subItems = mSubItems
                            subItems.sortBy {
                                (it as? VocazioneSubItem)?.nome
                            }
                        })
                    mSubItems = LinkedList()
                    totVocazioni = 0
                }
            }
            mAdapter.set(vocazioniList)
        }
    }

    companion object {
        private val TAG = VocazioneSectionedListFragment::class.java.canonicalName
    }

}