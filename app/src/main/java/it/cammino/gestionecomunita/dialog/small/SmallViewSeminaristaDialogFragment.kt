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
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialSharedAxis
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.ViewSeminaristaDialogFragment
import it.cammino.gestionecomunita.util.OSUtils
import it.cammino.gestionecomunita.util.getSerializableWrapper

@Suppress("unused")
class SmallViewSeminaristaDialogFragment : ViewSeminaristaDialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializableWrapper(
            BUILDER_TAG,
            Builder::class.java
        ) as? Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!OSUtils.isObySamsung()) {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward = */ true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward = */ false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        val mBuilder = builder
            ?: throw IllegalStateException("SmallViewSeminaristaDialogFragment should be created using its Builder interface.")

        val view = prefill(mBuilder, container)


        view.findViewById<MaterialToolbar>(R.id.seminarista_toolbar).setNavigationOnClickListener {
            dismiss()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            dismiss()
        }

        return view
    }

    /** The system calls this only when creating the layout in a dialog. */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"
        private const val ERROR_DIALOG = "error_dialog"

        private fun newInstance() = SmallViewSeminaristaDialogFragment()

        private fun newInstance(builder: Builder): SmallViewSeminaristaDialogFragment {
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
                )
            }
        }
    }

}
