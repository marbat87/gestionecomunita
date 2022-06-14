package it.cammino.gestionecomunita.ui.vocazione

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.TabsLayoutBinding
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailFragment
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailHostActivity
import it.cammino.gestionecomunita.ui.vocazione.list.VocazioneListFragment
import it.cammino.gestionecomunita.ui.vocazione.list.VocazioneSectionedListFragment

class CentroVocazionaleFragment : Fragment() {

    private val viewModel: CentroVocazionaleViewModel by viewModels()

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
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.extendedFab?.isVisible = false
        binding.extendedFabVocazione?.isVisible = true
        binding.extendedFabVocazione?.let { fab ->
            fab.setOnClickListener {
                it.transitionName = "shared_element_vocazione"
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    requireActivity(),
                    it,
                    "shared_element_vocazione" // The transition name to be matched in Activity B.
                )
                val intent = Intent(requireActivity(), VocazioneDetailHostActivity::class.java)
                startActivity(intent, options.toBundle())
            }
        }

        viewModel.itemCLickedState.observe(viewLifecycleOwner) {
            if (it == CentroVocazionaleViewModel.ItemClickState.CLICKED) {
                viewModel.itemCLickedState.value =
                    CentroVocazionaleViewModel.ItemClickState.UNCLICKED
                if (resources.getBoolean(R.bool.tablet_layout)) {
                    val fragment: Fragment = VocazioneDetailFragment()
                    val args = Bundle()
                    args.putLong(VocazioneDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(VocazioneDetailFragment.EDIT_MODE, false)
                    args.putBoolean(VocazioneDetailFragment.CREATE_MODE, false)
                    fragment.arguments = args
                    activity?.supportFragmentManager?.commit {
                        replace(
                            R.id.detail_fragment,
                            fragment,
                            R.id.vocazione_detail_fragment.toString()
                        )
                    }
                } else {
                    val args = Bundle()
                    args.putLong(VocazioneDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(VocazioneDetailFragment.EDIT_MODE, false)
                    args.putBoolean(VocazioneDetailFragment.CREATE_MODE, false)
                    val intent = Intent(requireContext(), VocazioneDetailHostActivity::class.java)
                    intent.putExtras(args)
                    startActivity(intent)
                }
            }
        }

        binding.viewPager.adapter = IndexTabsAdapter(this)
        TabLayoutMediator(binding.materialTabs, binding.viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.tutte_vocazioni
                    1 -> R.string.tappa_vocazioni
                    else -> R.string.tutte_vocazioni
                }
            )
        }.attach()

    }

    private class IndexTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> VocazioneListFragment()
                1 -> VocazioneSectionedListFragment()
                else -> VocazioneListFragment()
            }
    }

    companion object {
        internal val TAG = CentroVocazionaleFragment::class.java.canonicalName
    }

}
