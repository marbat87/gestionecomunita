package it.cammino.gestionecomunita.ui.seminario.list

import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.databinding.FragmentSeminariListBinding
import it.cammino.gestionecomunita.item.SeminarioListItem
import it.cammino.gestionecomunita.item.seminarioListItem
import it.cammino.gestionecomunita.ui.seminario.SeminariViewModel
import it.cammino.gestionecomunita.util.Utility

open class SeminarioListFragment : Fragment() {

    private val viewModel: SeminarioListViewModel by viewModels()
    private val indexViewModel: SeminariViewModel by viewModels({ requireParentFragment() })

    private var _binding: FragmentSeminariListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeminariListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val mAdapter: FastItemAdapter<SeminarioListItem> = FastItemAdapter()
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.seminariList.setHasFixedSize(true)
        binding.seminariList.adapter = mAdapter

        mAdapter.onClickListener =
            { _, _, item, _ ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
                    indexViewModel.clickedId = item.id
                    indexViewModel.itemCLickedState.value =
                        SeminariViewModel.ItemClickState.CLICKED
                    consume = true
                }
                consume
            }

        subscribeUiChanges()

    }

    private fun subscribeUiChanges() {
        viewModel.seminarioLiveList?.observe(viewLifecycleOwner) { seminari ->
            mAdapter.set(seminari.map {
                seminarioListItem {
                    setNomeSeminario = it.nome
                    id = it.id
                }
            })
            binding.noSeminariView.isVisible = mAdapter.itemAdapter.adapterItemCount == 0
        }
    }

    companion object {
        internal val TAG = SeminarioListFragment::class.java.canonicalName
    }

}