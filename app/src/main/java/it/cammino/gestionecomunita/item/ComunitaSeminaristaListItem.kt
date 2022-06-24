package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentActivity
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ComunitaSeminaristaRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import it.cammino.gestionecomunita.util.setupDatePicker
import java.sql.Date

fun comunitaSeminaristaListItem(block: ComunitaSeminaristaListItem.() -> Unit): ComunitaSeminaristaListItem =
    ComunitaSeminaristaListItem().apply(block)

class ComunitaSeminaristaListItem : AbstractBindingItem<ComunitaSeminaristaRowItemBinding>() {

    var comunitaAssegnazione: String = ""
    var dataAssegnazione: Date? = null
    var idTappaCammino: Int = 0

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_comunita_seminarista_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ComunitaSeminaristaRowItemBinding {
        return ComunitaSeminaristaRowItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindView(binding: ComunitaSeminaristaRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.nomeComunitaTextField.editText?.setText(comunitaAssegnazione)

        dataAssegnazione?.let {
            binding.dataComunitaTextField.editText?.setText(Utility.getStringFromDate(ctx, it))
        }

        if (idTappaCammino != -1)
            binding.tappaAutcomplete.setText(
                ctx.resources.getTextArray(R.array.passaggi_entries)[idTappaCammino],
                false
            )
        else {
            binding.tappaAutcomplete.text = null
        }

        binding.tappaAutcomplete.setOnItemClickListener { _, _, i, _ ->
            idTappaCammino = i
        }
        binding.dataComunitaTextField.editText.setupDatePicker(
            (ctx as FragmentActivity),
            "dataComunitaTextField",
            R.string.data_convivenza
        )
        binding.dataComunitaTextField.editText?.addTextChangedListener {
            dataAssegnazione = Utility.getDateFromString(
                ctx,
                it.toString()
            )
        }

        binding.nomeComunitaTextField.editText?.doOnTextChanged { text, _, _, _ ->
            comunitaAssegnazione = text.toString()
        }

    }

    override fun unbindView(binding: ComunitaSeminaristaRowItemBinding) {
        binding.nomeComunitaTextField.editText?.text = null
        binding.dataComunitaTextField.editText?.text = null
        binding.tappaAutcomplete.text = null
    }

}
