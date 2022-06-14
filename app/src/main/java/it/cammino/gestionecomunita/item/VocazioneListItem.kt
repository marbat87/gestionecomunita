package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.Vocazione
import it.cammino.gestionecomunita.databinding.VocazioneRowItemBinding

fun vocazioneListItem(block: VocazioneListItem.() -> Unit): VocazioneListItem =
    VocazioneListItem().apply(block)

class VocazioneListItem : AbstractBindingItem<VocazioneRowItemBinding>() {

    var nome: String = ""
    var setNome: String = ""
        set(value) {
            nome = value
            field = value
        }

    var sesso: Vocazione.Sesso? = Vocazione.Sesso.MASCHIO
        private set
    var setSesso: Vocazione.Sesso? = Vocazione.Sesso.MASCHIO
        set(value) {
            sesso = value
            field = value
        }

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_vocation_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): VocazioneRowItemBinding {
        return VocazioneRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: VocazioneRowItemBinding, payloads: List<Any>) {
        binding.vocazioneNome.text = nome
        binding.vocazioneMaleFemaleImage.setImageResource(if (sesso == Vocazione.Sesso.MASCHIO) R.drawable.man_24px else R.drawable.woman_24px)
    }

    override fun unbindView(binding: VocazioneRowItemBinding) {
        binding.vocazioneNome.text = null
    }

}
