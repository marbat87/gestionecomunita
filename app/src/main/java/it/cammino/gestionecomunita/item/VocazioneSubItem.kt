package it.cammino.gestionecomunita.item

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.mikepenz.fastadapter.FastAdapter
import com.mikepenz.fastadapter.IExpandable
import com.mikepenz.fastadapter.expandable.items.AbstractExpandableItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.Vocazione

fun vocazioneSubItem(block: VocazioneSubItem.() -> Unit): VocazioneSubItem =
    VocazioneSubItem().apply(block)

class VocazioneSubItem : AbstractExpandableItem<VocazioneSubItem.ViewHolder>(),
    IExpandable<VocazioneSubItem.ViewHolder> {

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

    override val layoutRes: Int
        get() = R.layout.vocazione_row_item

    override fun getViewHolder(v: View): ViewHolder {
        return ViewHolder(v)
    }

    override val type: Int
        get() = R.id.fastadapter_vocation_sub_item_id

    class ViewHolder(view: View) : FastAdapter.ViewHolder<VocazioneSubItem>(view) {

        private var vocazioneNome: TextView? = null
        private var vocazioneMaleFemaleImage: ImageView? = null

        override fun bindView(item: VocazioneSubItem, payloads: List<Any>) {
            vocazioneNome?.text = item.nome
            vocazioneMaleFemaleImage?.setImageResource(if (item.sesso == Vocazione.Sesso.MASCHIO) R.drawable.man_24px else R.drawable.woman_24px)

        }

        override fun unbindView(item: VocazioneSubItem) {
            vocazioneNome?.text = null
        }

        init {
            vocazioneNome = view.findViewById(R.id.vocazione_nome)
            vocazioneMaleFemaleImage = view.findViewById(R.id.vocazione_male_female_image)
        }
    }

}
