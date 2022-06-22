package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.text.InputType
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ResponsabileRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun responsabileListItem(block: ResponsabileListItem.() -> Unit): ResponsabileListItem =
    ResponsabileListItem().apply(block)

class ResponsabileListItem : AbstractBindingItem<ResponsabileRowItemBinding>() {

    var nomeResponsabile: String = ""
    var dataDal: Date? = null
    var dataAl: Date? = null

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    var editable: Boolean = true

    override val type: Int
        get() = R.id.fastadapter_responsabile_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ResponsabileRowItemBinding {
        return ResponsabileRowItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindView(binding: ResponsabileRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.respNomeTextField.editText?.setText(nomeResponsabile)

        dataDal?.let {
            binding.respDataDalTextField.editText?.setText(Utility.getStringFromDate(ctx, it))
        }
        dataAl?.let {
            binding.respDataAlTextField.editText?.setText(Utility.getStringFromDate(ctx, it))
        }

        binding.respNomeTextField.isEnabled = editable
        binding.respDataDalTextField.isEnabled = editable
        binding.respDataAlTextField.isEnabled = editable
        binding.removeResponsabile.isVisible = editable

        binding.respDataDalTextField.editText?.inputType = InputType.TYPE_NULL
        binding.respDataDalTextField.editText?.setOnKeyListener(null)
        binding.respDataDalTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.respDataDalTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    ctx,
                                    binding.respDataDalTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_convivenza)
                        .build()
                picker.show(
                    (ctx as FragmentActivity).supportFragmentManager,
                    "respDataDalTextFieldPicker"
                )
                picker.addOnPositiveButtonClickListener {
                    val data = Date(it)
                    binding.respDataDalTextField.editText?.setText(
                        Utility.getStringFromDate(
                            ctx,
                            data
                        )
                    )
                    dataDal = data
                }
            }
            false
        }

        binding.respDataAlTextField.editText?.inputType = InputType.TYPE_NULL
        binding.respDataAlTextField.editText?.setOnKeyListener(null)
        binding.respDataAlTextField.editText?.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                val picker =
                    MaterialDatePicker.Builder.datePicker()
                        .setSelection(
                            if (binding.respDataAlTextField.editText?.text.isNullOrBlank()) MaterialDatePicker.todayInUtcMilliseconds() else
                                Utility.getDateFromString(
                                    ctx,
                                    binding.respDataAlTextField.editText?.text?.toString() ?: ""
                                )?.time
                        )
                        .setTitleText(R.string.data_convivenza)
                        .build()
                picker.show(
                    (ctx as FragmentActivity).supportFragmentManager,
                    "respDataAlTextFieldPicker"
                )
                picker.addOnPositiveButtonClickListener {
                    val data = Date(it)
                    binding.respDataAlTextField.editText?.setText(
                        Utility.getStringFromDate(
                            ctx,
                            data
                        )
                    )
                    dataAl = data
                }
            }
            false
        }

        binding.respNomeTextField.editText?.doOnTextChanged { text, _, _, _ ->
            nomeResponsabile = text.toString()
        }

    }

    override fun unbindView(binding: ResponsabileRowItemBinding) {
        binding.respNomeTextField.editText?.text = null
        binding.respDataDalTextField.editText?.text = null
        binding.respDataAlTextField.editText?.text = null
    }

}
