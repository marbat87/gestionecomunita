package it.cammino.gestionecomunita.ui

import android.content.Context
import androidx.fragment.app.Fragment
import it.cammino.gestionecomunita.MainActivity

open class GestioneComunitaFragment : Fragment() {

    protected var mMainActivity: MainActivity? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mMainActivity = activity as? MainActivity
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
