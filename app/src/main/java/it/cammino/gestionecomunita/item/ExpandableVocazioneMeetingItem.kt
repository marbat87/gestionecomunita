package it.cammino.gestionecomunita.item

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.IncontroVocazionale
import it.cammino.gestionecomunita.databinding.IncontroVocazioneExpandableItemBinding
import it.cammino.gestionecomunita.util.StringUtils
import it.cammino.gestionecomunita.util.Utility
import java.sql.Date

fun expandableVocazioneMeetingItem(block: ExpandableVocazioneMeetingItem.() -> Unit): ExpandableVocazioneMeetingItem =
    ExpandableVocazioneMeetingItem().apply(block)

class ExpandableVocazioneMeetingItem :
    AbstractBindingItem<IncontroVocazioneExpandableItemBinding>() {

    var dataIncontro: Date? = null
    var luogoIncontro: String = StringUtils.EMPTY_STRING

    var note: String = StringUtils.EMPTY_STRING

    var tipo: IncontroVocazionale.Tipo = IncontroVocazionale.Tipo.INCONTRO

    var isExpanded: Boolean = false

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    var position: Int = 0

    override val type: Int
        get() = R.id.fastadapter_incontro_vocazione_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): IncontroVocazioneExpandableItemBinding {
        return IncontroVocazioneExpandableItemBinding.inflate(inflater, parent, false)
    }

    @SuppressLint("SetTextI18n")
    override fun bindView(binding: IncontroVocazioneExpandableItemBinding, payloads: List<Any>) {
        val ctx = binding.root.context

        binding.incontroTitle.text =
            ctx.resources.getTextArray(R.array.tipo_incontro_entries)[if (tipo == IncontroVocazionale.Tipo.INCONTRO) 0 else 1]
        binding.incontroLuogo.text =
            ctx.getString(R.string.luogo_dots, luogoIncontro.ifBlank { StringUtils.ND })
        binding.textLuogo.text = luogoIncontro.ifBlank { StringUtils.DASH }
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

    }

    override fun unbindView(binding: IncontroVocazioneExpandableItemBinding) {
        binding.incontroTitle.text = null
        binding.textNote.text = null
        binding.textData.text = null
        binding.textLuogo.text = null
    }

}
