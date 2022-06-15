package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.FratelloDetailItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun expandableBrotherItem(block: ExpandableBrotherItem.() -> Unit): ExpandableBrotherItem =
    ExpandableBrotherItem().apply(block)

class ExpandableBrotherItem : AbstractBindingItem<FratelloDetailItemBinding>() {

    var nome: String = StringUtils.EMPTY_STRING
    var cognome: String = StringUtils.EMPTY_STRING

    var statoCivile: String = StringUtils.EMPTY_STRING
    var coniuge: String = StringUtils.EMPTY_STRING

    var tribu: String = StringUtils.EMPTY_STRING

    var annoNascita: Date? = null

    var carisma: String = StringUtils.EMPTY_STRING

    var numFigli: Int = 0

    var dataInizioCammino: Date? = null

    var comunitaOrigine: String = StringUtils.EMPTY_STRING
    var dataArrivo: Date? = null

    var stato: Int = 0

    var note: String = StringUtils.EMPTY_STRING

    var editable: Boolean = false

    var deleteClickClickListener: OnClickListener? = null
    var editClickClickListener: OnClickListener? = null
    var expandClickClickListener: OnClickListener? = null

    var isExpanded: Boolean = false

    var position: Int = 0

    override val type: Int
        get() = R.id.fastadapter_expandable_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): FratelloDetailItemBinding {
        return FratelloDetailItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bindView(binding: FratelloDetailItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.groupTitle.text = "$nome $cognome"
        binding.textNome.text = nome
        binding.textCognome.text = cognome.ifBlank { StringUtils.DASH }
        binding.textStatoCivile.text = statoCivile.ifBlank { StringUtils.DASH }
        binding.textConiuge.text = coniuge.ifBlank { StringUtils.DASH }
        binding.textNumFigli.text = numFigli.toString()
        binding.textTribu.text = tribu.ifBlank { StringUtils.DASH }
        annoNascita?.let {
            binding.textAnnoNascita.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.textAnnoNascita.text = StringUtils.DASH
        }
        binding.textCarisma.text = carisma.ifBlank { StringUtils.DASH }
        binding.textComunitaProvenienza.text = comunitaOrigine.ifBlank { StringUtils.DASH }
        dataArrivo?.let {
            binding.textDataArrivo.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.textDataArrivo.text = StringUtils.DASH
        }

        binding.textStato.text = ctx.resources.getStringArray(R.array.stati)[stato]
        binding.textStatoInt.text = stato.toString()

        binding.textNote.text = note.ifBlank { StringUtils.DASH }
        dataInizioCammino?.let {
            binding.textDataInizioCammino.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.textDataInizioCammino.text = StringUtils.DASH
        }

        ViewCompat.animate(binding.groupIndicator).rotation(if (isExpanded) 0f else 180f).start()
        binding.texts.isVisible = isExpanded
        binding.buttons.isVisible = editable && isExpanded

        binding.cancellaFratello.setOnClickListener { deleteClickClickListener?.onClick(this) }
        binding.modificaFratello.setOnClickListener { editClickClickListener?.onClick(this) }
        binding.titleSection.setOnClickListener {
            ViewCompat.animate(binding.groupIndicator).rotation(if (isExpanded) 180f else 0f)
                .start()
            isExpanded = !isExpanded
            expandClickClickListener?.onClick(this)
        }

    }

    override fun unbindView(binding: FratelloDetailItemBinding) {
        binding.groupTitle.text = null
        binding.textNome.text = null
        binding.textCognome.text = null
        binding.textStatoCivile.text = null
        binding.textConiuge.text = null
        binding.textNumFigli.text = null
        binding.textTribu.text = null
        binding.textAnnoNascita.text = null
        binding.textCarisma.text = null
        binding.textComunitaProvenienza.text = null
        binding.textDataArrivo.text = null
        binding.textStato.text = null
        binding.textNote.text = null
        binding.textDataInizioCammino.text = null
    }

    interface OnClickListener {
        fun onClick(it: ExpandableBrotherItem)
    }

}
