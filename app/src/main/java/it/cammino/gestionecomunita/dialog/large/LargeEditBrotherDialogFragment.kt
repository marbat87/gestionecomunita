package it.cammino.gestionecomunita.dialog.large


import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.DialogState
import it.cammino.gestionecomunita.dialog.EditBrotherDialogFragment

@Suppress("unused")
class LargeEditBrotherDialogFragment : EditBrotherDialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val mView = prefill(mBuilder, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setView(mView)

        dialog.setTitle(getString(if (mBuilder.mEditMode) R.string.modifica_fratello else R.string.nuovo_fratello))

        mBuilder.mPositiveButton?.let { it ->
            dialog.setPositiveButton(it) { _, _ ->
                if (validateForm(mView)) {
                    viewModel.mTag = mBuilder.mTag
                    fillreturnText(mView)
                    viewModel.handled = false
                    viewModel.state.value = DialogState.Positive(this)
                }
            }
        }

        mBuilder.mNegativeButton?.let {
            dialog.setNegativeButton(it) { _, _ ->
                viewModel.mTag = mBuilder.mTag
                viewModel.handled = false
                viewModel.state.value = DialogState.Negative(this)
            }
        }

        dialog.setCancelable(mBuilder.mCanceable)

        dialog.setOnKeyListener { arg0, keyCode, event ->
            var returnValue = false
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                arg0.cancel()
                returnValue = true
            }
            returnValue
        }

        val alertDialog = dialog.create()

        mView.findViewById<TextInputLayout>(R.id.nome_text_field).editText?.doOnTextChanged { _, _, _, _ ->
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = validateForm(mView)
        }

        mView.findViewById<TextInputLayout>(R.id.numero_figli_text_field).editText?.doOnTextChanged { _, _, _, _ ->
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = validateForm(mView)
        }

        mView.findViewById<TextInputLayout>(R.id.data_inizio_cammino_text_field).editText?.doOnTextChanged { _, _, _, _ ->
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = validateForm(mView)
        }

        mView.findViewById<TextInputLayout>(R.id.data_nascita_text_field).editText?.doOnTextChanged { _, _, _, _ ->
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = validateForm(mView)
        }

        mView.findViewById<TextInputLayout>(R.id.data_arrivo_text_field).editText?.doOnTextChanged { _, _, _, _ ->
            alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled = validateForm(mView)
        }

        alertDialog.show()
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).isEnabled =
            validateForm(mView, false)

        return alertDialog
    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = LargeEditBrotherDialogFragment()

        private fun newInstance(builder: Builder): LargeEditBrotherDialogFragment {
            return newInstance().apply {
                arguments = bundleOf(
                    Pair(BUILDER_TAG, builder)
                )
            }
        }

        fun show(builder: Builder, fragmentManger: FragmentManager) {
            newInstance(builder).run {
                show(fragmentManger, builder.mTag)
            }
        }
    }

}
