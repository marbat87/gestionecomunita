package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.IncontroExpandableItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun expandableMeetingItem(block: ExpandableMeetingItem.() -> Unit): ExpandableMeetingItem =
    ExpandableMeetingItem().apply(block)

class ExpandableMeetingItem : AbstractBindingItem<IncontroExpandableItemBinding>() {

    var nome: String = StringUtils.EMPTY_STRING
    var cognome: String = StringUtils.EMPTY_STRING

    var idComunita: Long = 0

    var numeroComunita: String = StringUtils.EMPTY_STRING
    var parrocchiaComunita: String = StringUtils.EMPTY_STRING

    var dataIncontro: Date? = null
    var luogoIncontro: String = StringUtils.EMPTY_STRING

    var note: String = StringUtils.EMPTY_STRING

    var done: Boolean = false

    var deleteClickClickListener: OnClickListener? = null
    var editClickClickListener: OnClickListener? = null
    var todoClickListener: OnClickListener? = null
    var doneClickListener: OnClickListener? = null
    var expandClickClickListener: OnClickListener? = null

    private var isExpanded: Boolean = false

    var id: Long = 0

    var position: Int = 0

    override val type: Int
        get() = R.id.fastadapter_incontro_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): IncontroExpandableItemBinding {
        return IncontroExpandableItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bindView(binding: IncontroExpandableItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.incontroTitle.text = "$nome $cognome"
        binding.incontroLuogo.text =
            ctx.getString(R.string.luogo_dots, luogoIncontro.ifBlank { StringUtils.ND })
        binding.textNome.text = nome
        binding.textCognome.text = cognome.ifBlank { StringUtils.DASH }
        binding.textLuogo.text = luogoIncontro.ifBlank { StringUtils.DASH }
        binding.textComunita.text = if (idComunita != (-1).toLong()) ctx.getString(
            R.string.comunita_item_name,
            numeroComunita,
            parrocchiaComunita
        ) else StringUtils.DASH
        binding.textNote.text = note.ifBlank { StringUtils.DASH }
        dataIncontro?.let {
            binding.textData.text = Utility.getStringFromDate(ctx, it)
            binding.incontroData.text = ctx.getString(
                R.string.data_passaggio_dots,
                Utility.getStringFromDate(ctx, it)
            )
        } ?: run {
            binding.textData.text = StringUtils.DASH
            binding.incontroData.text = ctx.getString(
                R.string.data_passaggio_dots,
                StringUtils.ND
            )
        }

        ViewCompat.animate(binding.incontroIndicator).rotation(if (isExpanded) 0f else 180f).start()
        binding.expansion.isVisible = isExpanded
        binding.todoIncontro.isVisible = isExpanded && done
        binding.doneIncontro.isVisible = isExpanded && !done

        binding.cancellaIncontro.setOnClickListener { deleteClickClickListener?.onClick(this) }
        binding.modificaIncontro.setOnClickListener { editClickClickListener?.onClick(this) }
        binding.todoIncontro.setOnClickListener { todoClickListener?.onClick(this) }
        binding.doneIncontro.setOnClickListener { doneClickListener?.onClick(this) }
        binding.titleSection.setOnClickListener {
            ViewCompat.animate(binding.incontroIndicator).rotation(if (isExpanded) 180f else 0f)
                .start()
            isExpanded = !isExpanded
            expandClickClickListener?.onClick(this)
        }

    }

    override fun unbindView(binding: IncontroExpandableItemBinding) {
        binding.incontroTitle.text = null
        binding.textNome.text = null
        binding.textCognome.text = null
        binding.textComunita.text = null
        binding.textNote.text = null
        binding.textData.text = null
        binding.textLuogo.text = null
    }

    interface OnClickListener {
        fun onClick(it: ExpandableMeetingItem)
    }

}
