package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.CommunityRowItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun communityListItem(block: CommunityListItem.() -> Unit): CommunityListItem =
    CommunityListItem().apply(block)

class CommunityListItem : AbstractBindingItem<CommunityRowItemBinding>(),
    Comparable<CommunityListItem> {

    var numeroComunita: String = ""
    var setNumeroComunita: String = ""
        set(value) {
            numeroComunita = value
            field = value
        }

    var parrocchia: String = ""
        private set
    var setParrocchia: String = ""
        set(value) {
            parrocchia = value
            field = value
        }

    private var responsabile: StringHolder? = null
    var setResponsabile: String? = null
        set(value) {
            responsabile = StringHolder(value)
        }

    private var dataUltimaVisita: Date? = null
    var setDataUltimaVisita: Date? = null
        set(value) {
            dataUltimaVisita = value
            field = value
        }

    private var dateMode: Boolean = false
    var setDateMode: Boolean = false
        set(value) {
            dateMode = value
            field = value
        }

    private var catechisti: String = ""
    var setCatechisti: String = ""
        set(value) {
            catechisti = value
            field = value
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
            R.string.comunita_item_name, numeroComunita, parrocchia
        )
        if (dateMode)
            dataUltimaVisita?.let {
                val date = Utility.getStringFromDate(ctx, it)
                binding.textResponsabile.text =
                    ctx.getString(
                        R.string.ultima_visita_dots,
                        if (!date.isNullOrBlank()) date else "N.D."
                    )
            } ?: run {
                binding.textResponsabile.text =
                    ctx.getString(
                        R.string.ultima_visita_dots,
                        StringUtils.ND
                    )
            }
        else
            responsabile?.let {
                val resp = it.getText(ctx)
                binding.textResponsabile.text =
                    ctx.getString(
                        R.string.responsabile_dots,
                        if (!resp.isNullOrBlank()) resp else StringUtils.ND
                    )
            } ?: run {
                binding.textResponsabile.text =
                    ctx.getString(
                        R.string.responsabile_dots,
                        StringUtils.ND
                    )
            }
    }

    override fun unbindView(binding: CommunityRowItemBinding) {
        binding.textComunita.text = null
        binding.textResponsabile.text = null
    }

    override fun compareTo(other: CommunityListItem): Int {
        if (catechisti.lowercase().trim() == StringUtils.ITINERANTI
            && other.catechisti.lowercase().trim() != StringUtils.ITINERANTI
        )
            return -1
        if (catechisti.lowercase().trim() != StringUtils.ITINERANTI
            && other.catechisti.lowercase().trim() == StringUtils.ITINERANTI
        )
            return 1
        if (parrocchia < other.parrocchia)
            return -1
        if (parrocchia > other.parrocchia)
            return 1
        if (numeroComunita < other.numeroComunita)
            return -1
        if (parrocchia > other.parrocchia)
            return 1

        return 0
    }

}
