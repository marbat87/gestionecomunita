package it.cammino.gestionecomunita.ui.comunita.list

import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.ItemClickState
import it.cammino.gestionecomunita.MainActivityViewModel
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.CommunityListItem
import it.cammino.gestionecomunita.util.Utility

class CommunityListFragment : Fragment() {

    private val mViewModel: CommunityListViewModel by viewModels()
    private val activityViewModel: MainActivityViewModel by activityViewModels()

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
                    activityViewModel.clickedId = item.id
                    activityViewModel.itemCLickedState.value = ItemClickState.CLICKED
                    consume = true
                }
                consume
            }

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
        mViewModel.itemsResult?.observe(viewLifecycleOwner) { comunita ->
            Log.d("ciao", "comunita size" + comunita.size)
            mAdapter.set(comunita)
        }
    }

}