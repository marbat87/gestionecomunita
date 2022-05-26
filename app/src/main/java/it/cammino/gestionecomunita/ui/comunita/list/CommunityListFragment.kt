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
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.CommunityListItem
import it.cammino.gestionecomunita.item.communityListItem
import it.cammino.gestionecomunita.ui.comunita.ComunitaIndexViewModel
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date
import java.util.*

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

    private val mAdapter: FastItemAdapter<CommunityListItem> = FastItemAdapter()
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.communityList.setHasFixedSize(true)
        binding.communityList.adapter = mAdapter

        subscribeUiChanges()

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

    }

    private fun subscribeUiChanges() {
        val oneYearAgo = Calendar.getInstance().apply { add(Calendar.YEAR, -1) }
        viewModel.itemsResult?.observe(viewLifecycleOwner) { comunita ->
            mAdapter.set(comunita
                .filter {
                    viewModel.indexType == CommunityListViewModel.IndexType.TUTTE || it.dataUltimaVisita == null || it.dataUltimaVisita!! <= Date(
                        oneYearAgo.time.time
                    )
                }.map {
                    Log.d(TAG, "get id: ${it.id}")
                    communityListItem {
                        setNumeroComunita = it.numero
                        setParrocchia = it.parrocchia
                        setResponsabile = it.responsabile
                        id = it.id
                        setDataUltimaVisita = it.dataUltimaVisita
                        setDateMode = viewModel.indexType == CommunityListViewModel.IndexType.VISITATE_OLTRE_ANNO
                    }
                })
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