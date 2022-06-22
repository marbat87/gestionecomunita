package it.cammino.gestionecomunita.ui.seminario

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.FragmentSeminariBinding
import it.cammino.gestionecomunita.ui.seminario.detail.SeminarioDetailFragment
import it.cammino.gestionecomunita.ui.seminario.detail.SeminarioDetailHostActivity
import it.cammino.gestionecomunita.util.OSUtils

class SeminariFragment : Fragment() {

    private val viewModel: SeminariViewModel by viewModels()

    private var _binding: FragmentSeminariBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeminariBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.extendedFabSeminari?.let { fab ->
            fab.setOnClickListener {
                if (OSUtils.isObySamsung()) {
                    startActivity(
                        Intent(
                            requireActivity(),
                            SeminarioDetailHostActivity::class.java
                        )
                    )
                } else {
                    it.transitionName = "shared_element_seminario"
                    val options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        requireActivity(),
                        it,
                        "shared_element_seminario" // The transition name to be matched in Activity B.
                    )
                    val intent = Intent(requireActivity(), SeminarioDetailHostActivity::class.java)
                    startActivity(intent, options.toBundle())
                }
            }
        }

        viewModel.itemCLickedState.observe(viewLifecycleOwner) {
            if (it == SeminariViewModel.ItemClickState.CLICKED) {
                viewModel.itemCLickedState.value =
                    SeminariViewModel.ItemClickState.UNCLICKED
                if (resources.getBoolean(R.bool.tablet_layout)) {
                    val fragment: Fragment = SeminarioDetailFragment()
                    val args = Bundle()
                    args.putLong(SeminarioDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(SeminarioDetailFragment.EDIT_MODE, false)
                    args.putBoolean(SeminarioDetailFragment.CREATE_MODE, false)
                    fragment.arguments = args
                    activity?.supportFragmentManager?.commit {
                        replace(
                            R.id.detail_fragment,
                            fragment,
                            R.id.seminario_detail_fragment.toString()
                        )
                    }
                } else {
                    val args = Bundle()
                    args.putLong(SeminarioDetailFragment.ARG_ITEM_ID, viewModel.clickedId)
                    args.putBoolean(SeminarioDetailFragment.EDIT_MODE, false)
                    args.putBoolean(SeminarioDetailFragment.CREATE_MODE, false)
                    val intent = Intent(requireContext(), SeminarioDetailHostActivity::class.java)
                    intent.putExtras(args)
                    startActivity(intent)
                }
            }
        }

    }

    companion object {
        internal val TAG = SeminariFragment::class.java.canonicalName
    }

}
