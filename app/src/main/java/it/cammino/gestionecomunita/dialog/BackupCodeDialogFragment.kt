package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.capitalize
import java.io.Serializable


@Suppress("unused")
class BackupCodeDialogFragment : DialogFragment() {

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializable(
            BUILDER_TAG
        ) as? Builder

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("BackupCodeDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val mView = layoutInflater.inflate(R.layout.backup_code_dialog_layout, null, false)
        dialog.setView(mView)
        mView.findViewById<TextView>(R.id.backup_code).text =
            mBuilder.mBackupCode ?: StringUtils.EMPTY_STRING

        mView.findViewById<Button>(R.id.copy_code).setOnClickListener {
            val clip = ClipData.newPlainText("backup code", mBuilder.mBackupCode)
            ContextCompat.getSystemService(requireContext(), ClipboardManager::class.java)
                ?.setPrimaryClip(clip)
            Toast.makeText(
                requireActivity(),
                R.string.code_copied,
                Toast.LENGTH_SHORT
            ).show()
        }

        if (mBuilder.mTitle != 0)
            dialog.setTitle(mBuilder.mTitle)

        mBuilder.mPositiveButton?.let {
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

    fun cancel() {
        dialog?.cancel()
    }

    class Builder(context: AppCompatActivity, val mTag: String) : Serializable {

        @Transient
        private val mContext: AppCompatActivity = context

        @StringRes
        var mTitle = 0
        var mPositiveButton: CharSequence? = null
        var mNegativeButton: CharSequence? = null
        var mCanceable = false
        var mBackupCode: CharSequence? = null

        fun positiveButton(@StringRes text: Int) {
            mPositiveButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
        }

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = BackupCodeDialogFragment()

        private fun newInstance(builder: Builder): BackupCodeDialogFragment {
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
