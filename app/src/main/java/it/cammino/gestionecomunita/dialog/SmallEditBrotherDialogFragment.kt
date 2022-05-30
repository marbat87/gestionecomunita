package it.cammino.gestionecomunita.dialog


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.google.android.material.appbar.MaterialToolbar
import it.cammino.gestionecomunita.R

@Suppress("unused")
class SmallEditBrotherDialogFragment : EditBrotherDialogFragment() {

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

        val view = prefill(mBuilder, container)

        view.findViewById<MaterialToolbar>(R.id.fratello_toolbar)?.title = getString(
            if (mBuilder.mEditMode) R.string.modifica_fratello else R.string.nuovo_fratello
        )

        view.findViewById<MaterialToolbar>(R.id.fratello_toolbar).setNavigationOnClickListener {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value = DialogState.Negative(this)
            dismiss()
        }

        view.findViewById<Button>(R.id.salva_fratello).setOnClickListener {
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

        private fun newInstance() = SmallEditBrotherDialogFragment()

        private fun newInstance(builder: Builder): SmallEditBrotherDialogFragment {
            return newInstance().apply {
                arguments = bundleOf(
                    Pair(BUILDER_TAG, builder)
                )
            }
        }

        fun show(builder: Builder, fragmentManager: FragmentManager) {
            val transaction = fragmentManager.beginTransaction()
            // For a little polish, specify a transition animation
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            // To make it fullscreen, use the 'content' root view as the container
            // for the fragment, which is always the root view for the activity
            transaction
                .add(android.R.id.content, newInstance(builder))
                .addToBackStack(null)
                .commit()
        }
    }


}
