package it.cammino.gestionecomunita.ui.comunita

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import it.cammino.gestionecomunita.ItemClickState
import it.cammino.gestionecomunita.MainActivityViewModel
import it.cammino.gestionecomunita.databinding.FragmentCommunityHostBinding
import it.cammino.gestionecomunita.ui.GestioneComunitaFragment
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListFragment
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListViewModel

class CommunityDetailHostFragment : GestioneComunitaFragment() {

    private val activityViewModel: MainActivityViewModel by activityViewModels()
    private val mViewModel: CommunityListViewModel by viewModels()

    private var _binding: FragmentCommunityHostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mViewModel.onlyNotVisitedForOneYear = arguments?.getBoolean(CommunityListFragment.SOLO_NON_VISITATE_UNO_ANNO, false) ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityHostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        activity?.supportFragmentManager?.commit {
//            replace(
//                R.id.nav_host_fragment_community_host,
//                CommunityListFragment(),
//                R.id.navigation_home.toString()
//            )
//        }

//        val navHostFragment =
//            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_community_detail) as NavHostFragment
//        val navController = navHostFragment.navController
//        appBarConfiguration = AppBarConfiguration(navController.graph)

        activityViewModel.itemCLickedState.observe(viewLifecycleOwner) {
            if (it == ItemClickState.CLICKED) {
                activityViewModel.itemCLickedState.value = ItemClickState.UNCLICKED
//                val fragment: Fragment = CommunityDetailFragment()
                val args = Bundle()
                args.putInt(CommunityDetailFragment.ARG_ITEM_ID, activityViewModel.clickedId)
                args.putBoolean(CommunityDetailFragment.EDIT_MODE, false)
                args.putBoolean(CommunityDetailFragment.CREATE_MODE, false)
//                fragment.arguments = args

//                activity?.supportFragmentManager?.commit {
//                    replace(
//                        R.id.nav_host_fragment_community_detail,
//                        fragment,
//                        R.id.navigation_home.toString()
//                    )
//                }
                val intent = Intent(requireContext(), CommunityDetailHostActivity::class.java)
                intent.putExtras(args)
                startActivity(intent)
            }
        }

    }

    companion object {
        fun newInstance(onlyNotVisitedOneYear: Boolean): CommunityDetailHostFragment {
            val f = CommunityDetailHostFragment()
            f.arguments = bundleOf(CommunityListFragment.SOLO_NON_VISITATE_UNO_ANNO to onlyNotVisitedOneYear)
            return f
        }
    }

//    override fun onSupportNavigateUp(): Boolean {
//        val navHostFragment =
//        requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_community_detail) as NavHostFragment
//        val navController = navHostFragment.navController
//        return navController.navigateUp(appBarConfiguration)
//                || super.onSupportNavigateUp()
//    }
}