package it.cammino.gestionecomunita.dialog.small


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.CommunityHistoryDialogFragment
import it.cammino.gestionecomunita.dialog.DialogState

@Suppress("unused")
class SmallCommunityHistoryDialogFragment : CommunityHistoryDialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val mView = prefill(mBuilder, container)

        mView.findViewById<MaterialToolbar>(R.id.history_toolbar).setNavigationOnClickListener {
            dismiss()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            dismiss()
        }

        return mView
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = SmallCommunityHistoryDialogFragment()

        private fun newInstance(builder: Builder): SmallCommunityHistoryDialogFragment {
            return newInstance().apply {
                arguments = bundleOf(
                    Pair(BUILDER_TAG, builder)
                )
            }
        }

        fun show(builder: Builder, fragmentManager: FragmentManager) {
            fragmentManager.commit {
                replace(
                    android.R.id.content,
                    newInstance(builder)
                ).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            }
        }
    }

}
