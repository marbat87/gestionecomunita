package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.SeminarioRowItemBinding

fun seminarioListItem(block: SeminarioListItem.() -> Unit): SeminarioListItem =
    SeminarioListItem().apply(block)

class SeminarioListItem : AbstractBindingItem<SeminarioRowItemBinding>() {

    private var nomeSeminario: String = ""
    var setNomeSeminario: String = ""
        set(value) {
            nomeSeminario = value
            field = value
        }

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_seminario_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): SeminarioRowItemBinding {
        return SeminarioRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: SeminarioRowItemBinding, payloads: List<Any>) {
        binding.seminarioNameText.text = nomeSeminario
    }

    override fun unbindView(binding: SeminarioRowItemBinding) {
        binding.seminarioNameText.text = null
    }

}
