package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.PromemoriaRowItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun promemoriaItem(block: PromemoriaItem.() -> Unit): PromemoriaItem = PromemoriaItem().apply(block)

class PromemoriaItem : AbstractBindingItem<PromemoriaRowItemBinding>() {

    var numeroComunita: String = ""
    var parrocchiaComunita: String = ""
    var data: Date? = null
    var descrizione: String = ""
    var idComunita: Long = -1

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_promemoria_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): PromemoriaRowItemBinding {
        return PromemoriaRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: PromemoriaRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.promemoriaComunita.text =
            ctx.resources.getString(R.string.comunita_item_name, numeroComunita, parrocchiaComunita)

        data?.let {
            binding.promemoriaData.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.promemoriaData.text = StringUtils.DASH
        }

        binding.promemoriaDescrizione.text = descrizione

    }

    override fun unbindView(binding: PromemoriaRowItemBinding) {
        binding.promemoriaComunita.text = null
        binding.promemoriaData.text = null
        binding.promemoriaDescrizione.text = null
    }

}