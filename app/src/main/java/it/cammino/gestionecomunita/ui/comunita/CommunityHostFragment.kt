package it.cammino.gestionecomunita.ui.comunita

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import it.cammino.gestionecomunita.databinding.FragmentCommunityHostBinding
import it.cammino.gestionecomunita.ui.GestioneComunitaFragment
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListViewModel
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListViewModel.Companion.INDEX_TYPE

class CommunityDetailHostFragment : GestioneComunitaFragment() {

    private var _binding: FragmentCommunityHostBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

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

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
////        activity?.supportFragmentManager?.commit {
////            replace(
////                R.id.nav_host_fragment_community_host,
////                CommunityListFragment(),
////                R.id.navigation_home.toString()
////            )
////        }
//
////        val navHostFragment =
////            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment_community_detail) as NavHostFragment
////        val navController = navHostFragment.navController
////        appBarConfiguration = AppBarConfiguration(navController.graph)
//
//    }

    companion object {
        fun newInstance(indexType: CommunityListViewModel.IndexType): CommunityDetailHostFragment {
            val f = CommunityDetailHostFragment()
            f.arguments = Bundle()
            f.arguments?.putSerializable(INDEX_TYPE, indexType)
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