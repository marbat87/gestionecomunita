package it.cammino.gestionecomunita.ui.comunita

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.TabsLayoutBinding
import it.cammino.gestionecomunita.ui.GestioneComunitaFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ComunitaIndexFragment : GestioneComunitaFragment() {

    private val mViewModel: ComunitaIndexViewModel by viewModels()

    private val mPageChange: ViewPager2.OnPageChangeCallback =
        object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                Log.d(TAG, "onPageSelected: $position")
                mViewModel.pageViewed = position
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

        mMainActivity?.setTabVisible(true)
//        mMainActivity?.enableFab(false)
//        mMainActivity?.enableBottombar(false)

        binding.viewPager.adapter = IndexTabsAdapter(this)
        mMainActivity?.getMaterialTabs()?.let {
            TabLayoutMediator(it, binding.viewPager) { tab, position ->
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
        }
        binding.viewPager.registerOnPageChangeCallback(mPageChange)

        lifecycleScope.launch {
            delay(500)
//            if (savedInstanceState == null) {
//                val pref = PreferenceManager.getDefaultSharedPreferences(requireContext())
//                binding.viewPager.currentItem = Integer.parseInt(
//                    pref.getString(Utility.DEFAULT_INDEX, "0")
//                        ?: "0"
//                )
//            } else
                binding.viewPager.currentItem = mViewModel.pageViewed
        }

    }

    private class IndexTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 4

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> CommunityDetailHostFragment()
                1 -> CommunityDetailHostFragment()
                2 -> CommunityDetailHostFragment()
                3 -> CommunityDetailHostFragment()
                else -> CommunityDetailHostFragment()
            }
    }

    companion object {
        internal val TAG = ComunitaIndexFragment::class.java.canonicalName
    }

}
