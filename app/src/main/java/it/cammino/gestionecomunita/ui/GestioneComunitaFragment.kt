package it.cammino.gestionecomunita.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import it.cammino.gestionecomunita.MainActivity
import it.cammino.gestionecomunita.ui.comunita.CommunityDetailHostActivity
import it.cammino.gestionecomunita.ui.comunita.detail.CommunityDetailFragment
import it.cammino.gestionecomunita.ui.comunita.list.CommunityListViewModel

open class GestioneComunitaFragment : Fragment() {

    private val viewModel: CommunityListViewModel by viewModels()

    protected var mMainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? MainActivity
        viewModel.indexType =
            arguments?.getSerializable(CommunityListViewModel.INDEX_TYPE) as? CommunityListViewModel.IndexType
                ?: CommunityListViewModel.IndexType.TUTTE
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

        viewModel.itemCLickedState.observe(viewLifecycleOwner) {
            if (it == CommunityListViewModel.ItemClickState.CLICKED) {
                viewModel.itemCLickedState.value = CommunityListViewModel.ItemClickState.UNCLICKED
//                val fragment: Fragment = CommunityDetailFragment()
                val args = Bundle()
                args.putLong(CommunityDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
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

//    override fun onDestroy() {
//        super.onDestroy()
//        mMainActivity?.actionMode?.finish()
//        mMainActivity?.activitySearchView?.closeSearch()
//    }
//
//    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
//        super.onCreateOptionsMenu(menu, inflater)
//        mMainActivity?.updateProfileImage()
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        setHasOptionsMenu(true)
//    }

}
