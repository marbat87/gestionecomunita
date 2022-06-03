package it.cammino.gestionecomunita.ui.comunita

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.TabsLayoutBinding
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailHostActivity
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListFragment
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListViewModel
import it.cammino.gestionecomunita.ui.comunita.list.CommunitySectionedListFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ComunitaIndexFragment : Fragment() {

    private val viewModel: ComunitaIndexViewModel by viewModels()

    private val mPageChange: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                viewModel.pageViewed = position
            }
        }

    private var _binding: TabsLayoutBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = TabsLayoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        binding.viewPager.unregisterOnPageChangeCallback(mPageChange)
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mMainActivity?.setTabVisible(true)
//        mMainActivity?.enableFab(false)
//        mMainActivity?.enableBottombar(false)

        binding.extendedFab?.let { fab ->
            fab.setOnClickListener {
                it.transitionName = "shared_element_comunita"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    it,
                    "shared_element_comunita" // The transition name to be matched in Activity B.
                )
                val intent = Intent(requireActivity(), CommunityDetailHostActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }

        viewModel.itemCLickedState.observe(viewLifecycleOwner) {
            if (it == ComunitaIndexViewModel.ItemClickState.CLICKED) {
                viewModel.itemCLickedState.value =
                    ComunitaIndexViewModel.ItemClickState.UNCLICKED
                if (resources.getBoolean(R.bool.tablet_layout)) {
                    val fragment: Fragment = CommunityDetailFragment()
                    val args = Bundle()
                    args.putLong(CommunityDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(CommunityDetailFragment.EDIT_MODE, false)
                    args.putBoolean(CommunityDetailFragment.CREATE_MODE, false)
                    fragment.arguments = args
                    activity?.supportFragmentManager?.commit {
                        replace(
                            R.id.detail_fragment,
                            fragment,
                            R.id.community_detail_fragment.toString()
                        )
                    }
                } else {
                    val args = Bundle()
                    args.putLong(CommunityDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(CommunityDetailFragment.EDIT_MODE, false)
                    args.putBoolean(CommunityDetailFragment.CREATE_MODE, false)
                    val intent = Intent(requireContext(), CommunityDetailHostActivity::class.java)
                    intent.putExtras(args)
                    startActivity(intent)
                }
            }
        }

        binding.viewPager.adapter = IndexTabsAdapter(this)
        TabLayoutMediator(binding.materialTabs, binding.viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.tutte_comunita
                    1 -> R.string.oltre_un_anno_comunita
                    2 -> R.string.tappa_comunita
                    3 -> R.string.diocesi_comunita
                    else -> R.string.tutte_comunita
                }
            )
        }.attach()
        binding.viewPager.registerOnPageChangeCallback(mPageChange)

        lifecycleScope.launch {
            delay(500)
            binding.viewPager.currentItem = viewModel.pageViewed
        }

    }

    private class IndexTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> CommunityListFragment.newInstance(CommunityListViewModel.IndexType.TUTTE)
                1 -> CommunityListFragment.newInstance(CommunityListViewModel.IndexType.VISITATE_OLTRE_ANNO)
                2 -> CommunitySectionedListFragment.newInstance(CommunityListViewModel.IndexType.TAPPA)
                3 -> CommunitySectionedListFragment.newInstance(CommunityListViewModel.IndexType.DIOCESI)
                else -> CommunityListFragment()
            }
    }

    companion object {
        internal val TAG = ComunitaIndexFragment::class.java.canonicalName
    }

}
