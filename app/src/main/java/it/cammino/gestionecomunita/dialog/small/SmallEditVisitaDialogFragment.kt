package it.cammino.gestionecomunita.dialog.small


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.transition.MaterialSharedAxis
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditVisitaDialogFragment
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.util.OSUtils

@Suppress("unused")
class SmallEditVisitaDialogFragment : EditVisitaDialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!OSUtils.isObySamsung()) {
            enterTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward= */ true)
            returnTransition = MaterialSharedAxis(MaterialSharedAxis.Y, /* forward= */ false)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        val mBuilder = builder
            ?: throw IllegalStateException("SmallEditVisitaDialogFragment should be created using its Builder interface.")

        val view = prefill(mBuilder, container)

        view.findViewById<MaterialToolbar>(R.id.visita_toolbar)?.title = getString(
            if (mBuilder.mEditMode) R.string.modifica_visita else R.string.nuova_visita
        )

        view.findViewById<MaterialToolbar>(R.id.visita_toolbar).setNavigationOnClickListener {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value = DialogState.Negative(this)
            dismiss()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value = DialogState.Negative(this@SmallEditVisitaDialogFragment)
            dismiss()
        }

        view.findViewById<Button>(R.id.salva_visita).setOnClickListener {
            if (validateForm(view)) {
                viewModel.mTag = mBuilder.mTag
                fillreturnText(view)
                viewModel.handled = false
                viewModel.state.value = DialogState.Positive(this)
                dismiss()
            } else {
                (activity as? AppCompatActivity)?.let { mActivity ->
                    SimpleDialogFragment.show(
                        SimpleDialogFragment.Builder(
                            mActivity,
                            ERROR_DIALOG
                        )
                            .title(R.string.error)
                            .icon(R.drawable.error_24px)
                            .content(R.string.campi_non_compilati)
                            .positiveButton(android.R.string.ok),
                        mActivity.supportFragmentManager
                    )
                }
            }
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

        private fun newInstance() = SmallEditVisitaDialogFragment()

        private fun newInstance(builder: Builder): SmallEditVisitaDialogFragment {
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