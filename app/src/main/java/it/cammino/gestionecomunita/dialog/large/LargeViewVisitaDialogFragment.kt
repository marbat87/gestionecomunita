package it.cammino.gestionecomunita.dialog.large


import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.dialog.ViewVisitaDialogFragment

@Suppress("unused")
class LargeViewVisitaDialogFragment : ViewVisitaDialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("LargeEditMeetingDialogFragment should be created using its Builder interface.")

        val mView = prefill(mBuilder, null)

        val dialog = MaterialAlertDialogBuilder(requireContext())
        dialog.setView(mView)

        dialog.setTitle(R.string.visita)

        mBuilder.mPositiveButton?.let { it ->
            dialog.setPositiveButton(it) { _, _ ->
                dismiss()
            }
        }

        mBuilder.mNegativeButton?.let {
            dialog.setNegativeButton(it) { _, _ ->
                dismiss()
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

        return dialog.show()
    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = LargeViewVisitaDialogFragment()

        private fun newInstance(builder: Builder): LargeViewVisitaDialogFragment {
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
