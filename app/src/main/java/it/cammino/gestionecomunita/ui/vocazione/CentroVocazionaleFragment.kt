package it.cammino.gestionecomunita.ui.vocazione

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.transition.platform.MaterialSharedAxis
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.TabsLayoutBinding
import it.cammino.gestionecomunita.dialog.EditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.large.LargeEditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.small.SmallEditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.ui.AccountMenuFragment
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailFragment
import it.cammino.gestionecomunita.ui.vocazione.detail.VocazioneDetailHostActivity
import it.cammino.gestionecomunita.ui.vocazione.incontri.IncontriVocazioneFragment
import it.cammino.gestionecomunita.ui.vocazione.list.VocazioneListFragment
import it.cammino.gestionecomunita.ui.vocazione.list.VocazioneSectionedListFragment
import it.cammino.gestionecomunita.util.startActivityWithTransition

class CentroVocazionaleFragment : AccountMenuFragment() {

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
            if (viewModel.selectedTab == 2) {
                mMainActivity?.let { mActivity ->
                    fab.setOnClickListener {
                        val builder = EditVocazioneMeetingDialogFragment.Builder(
                            mActivity, IncontriVocazioneFragment.ADD_INCONTRO
                        )
                        if (resources.getBoolean(R.bool.large_layout)) {
                            builder.positiveButton(R.string.save)
                                .negativeButton(android.R.string.cancel)
                            LargeEditVocazioneMeetingDialogFragment.show(
                                builder,
                                mActivity.supportFragmentManager
                            )
                        } else {
                            SmallEditVocazioneMeetingDialogFragment.show(
                                builder,
                                mActivity.supportFragmentManager
                            )
                        }
                    }
                }
            } else {
                fab.setOnClickListener {
                    goToDetails(editMode = true, createMode = true)
                }
            }
        }

        binding.extendedFabVocazione?.setText(if (viewModel.selectedTab == 2) R.string.nuovo_incontro else R.string.vocazione_new_title)
        binding.extendedFabVocazione?.contentDescription =
            getString(if (viewModel.selectedTab == 2) R.string.nuovo_incontro else R.string.vocazione_new_title)

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
                    goToDetails(editMode = false, createMode = false)
                }
            }
        }

        binding.viewPager.adapter = IndexTabsAdapter(this)
        TabLayoutMediator(binding.materialTabs, binding.viewPager) { tab, position ->
            tab.setText(
                when (position) {
                    0 -> R.string.tutte_vocazioni
                    1 -> R.string.tappa_vocazioni
                    2 -> R.string.incontri_tab
                    else -> R.string.tutte_vocazioni
                }
            )
        }.attach()

        binding.materialTabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    viewModel.selectedTab = it.position
                    mMainActivity?.updateVocazioneSelectedIndex(viewModel.selectedTab)
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Handle tab reselect
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Handle tab unselect
            }
        })

    }

    private fun goToDetails(editMode: Boolean, createMode: Boolean) {
        val args = Bundle()
        args.putLong(VocazioneDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
        args.putBoolean(VocazioneDetailFragment.EDIT_MODE, editMode)
        args.putBoolean(VocazioneDetailFragment.CREATE_MODE, createMode)
        activity?.let { act ->
            val intent = Intent(act, VocazioneDetailHostActivity::class.java)
            intent.putExtras(args)
            act.startActivityWithTransition(intent, MaterialSharedAxis.X)
        }
    }


    private class IndexTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment =
            when (position) {
                0 -> VocazioneListFragment()
                1 -> VocazioneSectionedListFragment()
                2 -> IncontriVocazioneFragment()
                else -> VocazioneListFragment()
            }
    }

    companion object {
        internal val TAG = CentroVocazionaleFragment::class.java.canonicalName
    }

}
