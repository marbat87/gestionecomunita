package it.cammino.gestionecomunita.item

import android.view.View
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.gestionecomunita.R

fun communitySubItem(block: CommunitySubItem.() -> Unit): CommunitySubItem =
    CommunitySubItem().apply(block)

class CommunitySubItem : AbstractExpandableItem<CommunitySubItem.ViewHolder>(),
    IExpandable<CommunitySubItem.ViewHolder> {

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

    override val type: Int
        get() = R.id.fastadapter_sub_item_id

    override val layoutRes: Int
        get() = R.layout.community_row_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    class ViewHolder(view: View) : FastAdapter.ViewHolder<CommunitySubItem>(view) {

        private var textComunita: TextView? = null
        private var textResponsabile: TextView? = null
//        private var mId: TextView? = null

        override fun bindView(item: CommunitySubItem, payloads: List<Any>) {
            val ctx = itemView.context

            textComunita?.text = ctx.getString(
                R.string.comunita_item_name,
                item.numeroComunita.getText(ctx),
                item.parrocchia.getText(ctx)
            )
            item.responsabile?.let {
                val resp = it.getText(ctx)
                textResponsabile?.text =
                    ctx.getString(
                        R.string.responsabile_dots,
                        if (!resp.isNullOrBlank()) resp else "N.D."
                    )
            }

        }

        override fun unbindView(item: CommunitySubItem) {
            textComunita?.text = null
            textResponsabile?.text = null
//            mId?.text = null
        }

        init {
            textComunita = view.findViewById(R.id.text_comunita)
            textResponsabile = view.findViewById(R.id.text_responsabile)
//            mId = view.findViewById(R.id.text_id_canto)
        }
    }

}
