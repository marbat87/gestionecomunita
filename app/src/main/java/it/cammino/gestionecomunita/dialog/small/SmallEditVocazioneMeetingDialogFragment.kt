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
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.appbar.MaterialToolbar
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditVocazioneMeetingDialogFragment
import it.cammino.gestionecomunita.dialog.SimpleDialogFragment
import it.cammino.gestionecomunita.util.getSerializableWrapper
import it.cammino.gestionecomunita.util.setEnterTransition

@Suppress("unused")
class SmallEditVocazioneMeetingDialogFragment : EditVocazioneMeetingDialogFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setEnterTransition()
    }

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializableWrapper(
            BUILDER_TAG,
            Builder::class.java
        ) as? Builder

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout to use as dialog or embedded fragment
        val mBuilder = builder
            ?: throw IllegalStateException("SmallEditMeetingDialogFragment should be created using its Builder interface.")

        val view = prefill(mBuilder, container)

        view.findViewById<MaterialToolbar>(R.id.incontro_toolbar)?.title = getString(
            if (mBuilder.mEditMode) R.string.modifica_incontro else R.string.nuovo_incontro
        )

        view.findViewById<MaterialToolbar>(R.id.incontro_toolbar).setNavigationOnClickListener {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value = DialogState.Negative(this)
            dismiss()
        }

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            viewModel.mTag = mBuilder.mTag
            viewModel.handled = false
            viewModel.state.value =
                DialogState.Negative(this@SmallEditVocazioneMeetingDialogFragment)
            dismiss()
        }

        view.findViewById<Button>(R.id.salva_incontro).setOnClickListener {
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

        private fun newInstance() = SmallEditVocazioneMeetingDialogFragment()

        private fun newInstance(builder: Builder): SmallEditVocazioneMeetingDialogFragment {
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