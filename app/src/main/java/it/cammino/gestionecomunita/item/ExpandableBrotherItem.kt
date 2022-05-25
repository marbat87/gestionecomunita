package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import com.mikepenz.fastadapter.ui.utils.StringHolder
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.databinding.FratelloDetailItemBinding
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun expandableBrotherItem(block: ExpandableBrotherItem.() -> Unit): ExpandableBrotherItem =
    ExpandableBrotherItem().apply(block)

class ExpandableBrotherItem : AbstractBindingItem<FratelloDetailItemBinding>() {

    var nome: StringHolder? = null
        private set
    var setNome: String? = null
        set(value) {
            nome = StringHolder(value)
        }
    var cognome: StringHolder? = null
        private set
    var setCognome: String? = null
        set(value) {
            cognome = StringHolder(value)
        }
    var statoCivile: StringHolder? = null
        private set
    var setStatoCivile: String? = null
        set(value) {
            statoCivile = StringHolder(value)
        }
    var numFigli: Int = 0
        private set
    var setNumFigli: Int = 0
        set(value) {
            numFigli = value
            field = value
        }
    var dataInizioCammino: Date? = null
        private set
    var setDataInizioCammino: Date? = null
        set(value) {
            dataInizioCammino = value
            field = value
        }

    var editable: Boolean = false
    var setEditable: Boolean = false
        set(value) {
            editable = value
            field = value
        }

    var deleteClickClickListener: View.OnClickListener? = null
    var editClickClickListener: View.OnClickListener? = null
    var expandClickClickListener: View.OnClickListener? = null

    var isExpanded: Boolean = false

    var position: Int = 0
    var setPosition: Int = 0
        set(value) {
            position = value
            field = value
        }

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

        binding.groupTitle.text = "${nome?.getText(ctx)} ${cognome?.getText(ctx)}"
        binding.textNome.text = nome?.getText(ctx)
        val cognomeText = cognome?.getText(ctx)
        binding.textCognome.text = if (cognomeText.isNullOrBlank()) DASH else cognomeText
        val statoCivileText = statoCivile?.getText(ctx)
        binding.textStatoCivile.text =
            if (statoCivileText.isNullOrBlank()) DASH else statoCivileText
        binding.textNumFigli.text = numFigli.toString()
        binding.positon.text = position.toString()
        dataInizioCammino?.let {
            binding.textDataInizioCammino.text = Utility.getStringFromDate(ctx, it)
        } ?: run {
            binding.textDataInizioCammino.text = DASH
        }

        if (!isExpanded) {
            ViewCompat.animate(binding.groupIndicator).rotation(180f).start()
            binding.texts.isVisible = false
            binding.buttons.isVisible = false
        } else {
            ViewCompat.animate(binding.groupIndicator).rotation(0f).start()
            binding.texts.isVisible = true
            binding.buttons.isVisible = editable
        }

        deleteClickClickListener?.let { binding.cancellaFratello.setOnClickListener(it) }
        editClickClickListener?.let { binding.modificaFratello.setOnClickListener(it) }
        binding.titleSection.setOnClickListener {
            isExpanded = !isExpanded
            expandClickClickListener?.onClick(it)
        }

    }

    override fun unbindView(binding: FratelloDetailItemBinding) {
        binding.groupTitle.text = null
        binding.textNome.text = null
        binding.textCognome.text = null
        binding.textStatoCivile.text = null
        binding.textNumFigli.text = null
        binding.textDataInizioCammino.text = null
        binding.positon.text = null
    }

    companion object {
        const val DASH = "-"
    }

}
