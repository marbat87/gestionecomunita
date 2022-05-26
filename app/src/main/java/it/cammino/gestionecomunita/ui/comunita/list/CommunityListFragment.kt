package it.cammino.gestionecomunita.ui.comunita.list

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mikepenz.fastadapter.IAdapter
import com.mikepenz.fastadapter.adapters.FastItemAdapter
import it.cammino.gestionecomunita.databinding.FragmentCommunityListBinding
import it.cammino.gestionecomunita.item.CommunityListItem
import it.cammino.gestionecomunita.item.communityListItem
import it.cammino.gestionecomunita.ui.comunita.CommunityDetailHostActivity
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date
import java.util.*

class CommunityListFragment : Fragment() {

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

    private val mAdapter: FastItemAdapter<CommunityListItem> = FastItemAdapter()
    private var mLastClickTime: Long = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.communityList.setHasFixedSize(true)
        binding.communityList.adapter = mAdapter

        subscribeUiChanges()

        mAdapter.onClickListener =
            { mView: View?, _: IAdapter<CommunityListItem>, item: CommunityListItem, _: Int ->
                var consume = false
                if (SystemClock.elapsedRealtime() - mLastClickTime >= Utility.CLICK_DELAY) {
                    mLastClickTime = SystemClock.elapsedRealtime()
//                    viewModel.clickedId = item.id
//                    viewModel.itemCLickedState.value = CommunityListViewModel.ItemClickState.CLICKED
                    val args = Bundle()
                    args.putLong(CommunityDetailFragment.ARG_ITEM_ID, item.id)
                    args.putBoolean(CommunityDetailFragment.EDIT_MODE, false)
                    args.putBoolean(CommunityDetailFragment.CREATE_MODE, false)
                    var options: ActivityOptionsCompat? = null
                    mView?.let {
                        it.transitionName = "shared_element_comunita"
                        options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                            requireActivity(),
                            mView,
                            "shared_element_comunita" // The transition name to be matched in Activity B.
                        )
                    }
                    val intent = Intent(requireContext(), CommunityDetailHostActivity::class.java)
                    intent.putExtras(args)
                    startActivity(intent, options?.toBundle())
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
                    }
                })
        }
    }

    companion object {
        internal val TAG = CommunityListFragment::class.java.canonicalName

    }

}