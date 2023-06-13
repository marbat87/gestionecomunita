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
import it.cammino.gestionecomunita.databinding.ServizioSeminarioRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.setupDatePicker
import java.sql.Date

fun servizioSeminarioListItem(block: ServizioSeminarioListItem.() -> Unit): ServizioSeminarioListItem =
    ServizioSeminarioListItem().apply(block)

class ServizioSeminarioListItem : AbstractBindingItem<ServizioSeminarioRowItemBinding>() {

    var nomeServizio: String = ""
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
        get() = R.id.fastadapter_servizio_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ServizioSeminarioRowItemBinding {
        return ServizioSeminarioRowItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindView(binding: ServizioSeminarioRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.servNomeTextField.editText?.setText(nomeServizio)

        dataDal?.let {
            binding.servDataDalTextField.editText?.setText(Utility.getStringFromDate(ctx, it))
        }
        dataAl?.let {
            binding.servDataAlTextField.editText?.setText(Utility.getStringFromDate(ctx, it))
        }

        binding.servNomeTextField.isEnabled = editable
        binding.servDataDalTextField.isEnabled = editable
        binding.servDataAlTextField.isEnabled = editable
        binding.removeServizio.isVisible = editable

        binding.servDataDalTextField.editText?.addTextChangedListener {
            val data = Utility.getDateFromString(ctx, it.toString())
            dataDal = data
            if (data != null && dataAl != null && data > dataAl)
                binding.servDataDalTextField.error = ctx.getString(R.string.data_dal_error)
            else {
                binding.servDataAlTextField.error = null
                binding.servDataDalTextField.error = null
            }
            hasError = data != null && dataAl != null && data > dataAl
        }
        binding.servDataDalTextField.editText.setupDatePicker(
            (ctx as FragmentActivity),
            "respDataDalTextField",
            R.string.servizio_data_dal
        )

        binding.servDataAlTextField.editText?.addTextChangedListener {
            val data = Utility.getDateFromString(ctx, it.toString())
            dataAl = data
            if (data != null && dataDal != null && data < dataDal)
                binding.servDataAlTextField.error = ctx.getString(R.string.data_al_error)
            else {
                binding.servDataAlTextField.error = null
                binding.servDataDalTextField.error = null
            }
            hasError = data != null && dataDal != null && data < dataDal
        }
        binding.servDataAlTextField.editText.setupDatePicker(
            ctx,
            "servDataAlTextField",
            R.string.servizio_data_al
        )

        binding.servNomeTextField.editText?.doOnTextChanged { text, _, _, _ ->
            nomeServizio = text.toString()
        }

    }

    override fun unbindView(binding: ServizioSeminarioRowItemBinding) {
        binding.servNomeTextField.editText?.text = null
        binding.servDataDalTextField.editText?.text = null
        binding.servDataAlTextField.editText?.text = null
    }

}
