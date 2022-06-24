package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ResponsabileRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.setupDatePicker
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
    var hasError: Boolean = false

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

        binding.respDataDalTextField.editText?.addTextChangedListener {
            val data = Utility.getDateFromString(ctx, it.toString())
            dataDal = data
            if (data != null && dataAl != null && data > dataAl)
                binding.respDataDalTextField.error = ctx.getString(R.string.data_dal_error)
            else {
                binding.respDataAlTextField.error = null
                binding.respDataDalTextField.error = null
            }
            hasError = data != null && dataAl != null && data > dataAl
        }
        binding.respDataDalTextField.editText.setupDatePicker(
            (ctx as FragmentActivity),
            "respDataDalTextField",
            R.string.data_dal
        )

        binding.respDataAlTextField.editText?.addTextChangedListener {
            val data = Utility.getDateFromString(ctx, it.toString())
            dataAl = data
            if (data != null && dataDal != null && data < dataDal)
                binding.respDataAlTextField.error = ctx.getString(R.string.data_al_error)
            else {
                binding.respDataAlTextField.error = null
                binding.respDataDalTextField.error = null
            }
            hasError = data != null && dataDal != null && data < dataDal
        }
        binding.respDataAlTextField.editText.setupDatePicker(
            ctx,
            "respDataAlTextField",
            R.string.data_al
        )

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
