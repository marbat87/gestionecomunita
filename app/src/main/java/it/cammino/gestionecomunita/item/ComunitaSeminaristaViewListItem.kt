package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.ComunitaSeminaristaViewRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun comunitaSeminaristaViewListItem(block: ComunitaSeminaristaViewListItem.() -> Unit): ComunitaSeminaristaViewListItem =
    ComunitaSeminaristaViewListItem().apply(block)

class ComunitaSeminaristaViewListItem :
    AbstractBindingItem<ComunitaSeminaristaViewRowItemBinding>() {

    var comunitaAssegnazione: String = ""
    var dataAssegnazione: Date? = null
    var idTappaCammino: Int = 0

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_comunita_seminarista_view_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): ComunitaSeminaristaViewRowItemBinding {
        return ComunitaSeminaristaViewRowItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun bindView(binding: ComunitaSeminaristaViewRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.nomeComunitaText.text = comunitaAssegnazione
        dataAssegnazione?.let {
            binding.dataComunitaText.text = Utility.getStringFromDate(ctx, it)
        }

        if (idTappaCammino != -1)
            binding.tappaComunitaText.text =
                ctx.resources.getTextArray(R.array.passaggi_entries)[idTappaCammino]

    }

    override fun unbindView(binding: ComunitaSeminaristaViewRowItemBinding) {
        binding.nomeComunitaText.text = null
        binding.dataComunitaText.text = null
        binding.tappaComunitaText.text = null
    }

}
