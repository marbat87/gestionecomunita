package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.swipe.ISwipeable
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.SwipeableItemBinding
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun swipeableItem(block: SwipeableItem.() -> Unit): SwipeableItem = SwipeableItem().apply(block)

class SwipeableItem : AbstractBindingItem<SwipeableItemBinding>(), ISwipeable {

    var numeroComunita: String = ""
    var parrocchiaComunita: String = ""
    var data: Date? = null
    var descrizione: String = ""

    var id: Long = 0

    var swipedDirection: Int = 0

    override val type: Int
        get() = R.id.fastadapter_swipable_item_id

    override var isSwipeable = true

    override fun createBinding(inflater: LayoutInflater, parent: ViewGroup?): SwipeableItemBinding {
        return SwipeableItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: SwipeableItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.promemoriaComunita.text =
            ctx.resources.getString(R.string.comunita_item_name, numeroComunita, parrocchiaComunita)

        data?.let {
            binding.promemoriaData.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.promemoriaData.text = Utility.DASH
        }

        binding.promemoriaDescrizione.text = descrizione


    }

    override fun unbindView(binding: SwipeableItemBinding) {
        binding.promemoriaComunita.text = null
        binding.promemoriaData.text = null
        binding.promemoriaDescrizione.text = null
    }

}