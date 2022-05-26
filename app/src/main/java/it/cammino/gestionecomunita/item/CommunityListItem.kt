package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.CommunityRowItemBinding

fun communityListItem(block: CommunityListItem.() -> Unit): CommunityListItem =
    CommunityListItem().apply(block)

class CommunityListItem : AbstractBindingItem<CommunityRowItemBinding>() {

    lateinit var numeroComunita: StringHolder
        private set
    var setNumeroComunita: String? = null
        set(value) {
            numeroComunita = StringHolder(value)
        }

    lateinit var parrocchia: StringHolder
        private set
    var setParrocchia: String? = null
        set(value) {
            parrocchia = StringHolder(value)
        }

    var responsabile: StringHolder? = null
        private set
    var setResponsabile: String? = null
        set(value) {
            responsabile = StringHolder(value)
        }

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.fastadapter_community_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): CommunityRowItemBinding {
        return CommunityRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: CommunityRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.textComunita.text = ctx.getString(
            R.string.comunita_item_name,
            numeroComunita.getText(ctx),
            parrocchia.getText(ctx)
        )
        responsabile?.let {
            val resp = it.getText(ctx)
            binding.textResponsabile.text =
                ctx.getString(
                    R.string.responsabile_dots,
                    if (!resp.isNullOrBlank()) resp else "N.D."
                )
        }
    }

    override fun unbindView(binding: CommunityRowItemBinding) {
        binding.textComunita.text = null
        binding.textResponsabile.text = null
    }

//    companion object {
//        private val TAG = CommunityListItem::class.java.canonicalName
//    }

}
