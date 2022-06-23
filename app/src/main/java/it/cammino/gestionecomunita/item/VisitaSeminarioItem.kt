package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.VisitaSeminarioRowItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun visitaSeminarioItem(block: VisitaSeminarioItem.() -> Unit): VisitaSeminarioItem =
    VisitaSeminarioItem().apply(block)

class VisitaSeminarioItem : AbstractBindingItem<VisitaSeminarioRowItemBinding>() {

    var formatoriPresenti: String = StringUtils.EMPTY_STRING
    var seminaristiPresenti: String = StringUtils.EMPTY_STRING
    var data: Date? = null
    var note: String = StringUtils.EMPTY_STRING

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    var editable: Boolean = false

    override val type: Int
        get() = R.id.fastadapter_visita_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): VisitaSeminarioRowItemBinding {
        return VisitaSeminarioRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: VisitaSeminarioRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.editButtons.isVisible = editable
        binding.vediVisita.isVisible = !editable

        data?.let {
            binding.dataVisita.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.dataVisita.text = StringUtils.DASH
        }

    }

    override fun unbindView(binding: VisitaSeminarioRowItemBinding) {
        binding.dataVisita.text = null
    }

}