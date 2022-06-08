package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.HistoryRowItemBinding
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun communityHistoryListItem(block: CommunityHistoryListItem.() -> Unit): CommunityHistoryListItem =
    CommunityHistoryListItem().apply(block)

class CommunityHistoryListItem : AbstractBindingItem<HistoryRowItemBinding>() {

    var indicePassaggio: Int = 0
    var dataPassaggio: Date? = null

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    override val type: Int
        get() = R.id.community_history_fragment

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): HistoryRowItemBinding {
        return HistoryRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: HistoryRowItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.textNomePassaggio.text = ctx.getString(R.string.passaggio_dots, ctx.resources.getStringArray(R.array.passaggi_entries)[indicePassaggio])
        dataPassaggio?.let {
            binding.textDataPassaggio.text = ctx.getString(
                R.string.data_passaggio_dots,
                Utility.getStringFromDate(ctx, it)
            )
        } ?: run {
            binding.textDataPassaggio.text = ctx.getString(
                R.string.data_passaggio_dots,
                Utility.ND
            )
        }

    }

    override fun unbindView(binding: HistoryRowItemBinding) {
        binding.textNomePassaggio.text = null
        binding.textDataPassaggio.text = null
    }

}
