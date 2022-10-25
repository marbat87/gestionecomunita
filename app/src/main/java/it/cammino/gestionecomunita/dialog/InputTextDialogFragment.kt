package it.cammino.gestionecomunita.dialog


import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.KeyEvent
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.capitalize
import it.cammino.gestionecomunita.util.getSerializableWrapper
import java.io.Serializable

class InputTextDialogFragment : DialogFragment() {

    private val viewModel: DialogViewModel by viewModels({ requireActivity() })

    private val builder: Builder?
        get() = if (arguments?.containsKey(BUILDER_TAG) != true) null else arguments?.getSerializableWrapper(
            BUILDER_TAG,
            Builder::class.java
        ) as? Builder

    @SuppressLint("CheckResult")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val mBuilder = builder
            ?: throw IllegalStateException("SimpleDialogFragment should be created using its Builder interface.")

        val dialog = MaterialAlertDialogBuilder(requireContext())

        val mView = layoutInflater.inflate(R.layout.input_search, null, false)
        dialog.setView(mView)
        val input = mView.findViewById<TextInputEditText>(R.id.input_text)
        input.setText(mBuilder.mPrefill ?: StringUtils.EMPTY_STRING)
        input.selectAll()

        if (mBuilder.mTitle != 0)
            dialog.setTitle(mBuilder.mTitle)

        mBuilder.mPositiveButton?.let {
            dialog.setPositiveButton(it) { _, _ ->
                viewModel.mTag = mBuilder.mTag
                viewModel.outputText = input.text.toString()
                viewModel.handled = false
                viewModel.state.value = DialogState.Positive(this)
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
        var mPrefill: CharSequence? = null

        fun title(@StringRes text: Int): Builder {
            mTitle = text
            return this
        }

        fun prefill(@StringRes text: Int): Builder {
            mPrefill = this.mContext.resources.getText(text)
            return this
        }

        fun prefill(text: String): Builder {
            mPrefill = text
            return this
        }

        fun positiveButton(@StringRes text: Int): Builder {
            mPositiveButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
            return this
        }

        fun negativeButton(@StringRes text: Int): Builder {
            mNegativeButton = this.mContext.resources.getText(text).capitalize(mContext.resources)
            return this
        }

        fun setCanceable(canceable: Boolean): Builder {
            mCanceable = canceable
            return this
        }

    }

    companion object {

        private const val BUILDER_TAG = "bundle_builder"

        private fun newInstance() = InputTextDialogFragment()

        private fun newInstance(builder: Builder): InputTextDialogFragment {
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

    class DialogViewModel : ViewModel() {
        var mTag: String = StringUtils.EMPTY_STRING
        var outputText: String = StringUtils.EMPTY_STRING
        var handled = true
        val state = MutableLiveData<DialogState<InputTextDialogFragment>>()
    }

}
