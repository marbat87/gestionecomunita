package it.cammino.gestionecomunita.item

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.mikepenz.fastadapter.binding.AbstractBindingItem
import it.cammino.gestionecomunita.R
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista
import it.cammino.gestionecomunita.databinding.SeminaristaRowItemBinding
import java.sql.Date

fun seminaristaItem(block: SeminaristaItem.() -> Unit): SeminaristaItem =
    SeminaristaItem().apply(block)

class SeminaristaItem : AbstractBindingItem<SeminaristaRowItemBinding>() {

    var id: Long = 0
        set(value) {
            identifier = value
            field = value
        }

    var idSeminario: Long = 0

    var nome: String = ""
    var dataNascita: Date? = null
    var nazione: String = ""

    var comunitaProvenienza: String = ""
    var catechistiProvenienza: String = ""
    var idTappaProvenienza: Int = -1

    var dataEntrata: Date? = null
    var dataUscita: Date? = null
    var motivoUscita: String = ""

    var dataAdmissio: Date? = null
    var dataAccolitato: Date? = null
    var dataLettorato: Date? = null
    var dataDiaconato: Date? = null
    var dataPresbiterato: Date? = null

    var note: String = ""

    var comunitaList: List<ComunitaSeminarista> = ArrayList()

    var editable: Boolean = true

    override val type: Int
        get() = R.id.fastadapter_seminarista_item_id

    override fun createBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?
    ): SeminaristaRowItemBinding {
        return SeminaristaRowItemBinding.inflate(inflater, parent, false)
    }

    override fun bindView(binding: SeminaristaRowItemBinding, payloads: List<Any>) {

        binding.nomeSeminarista.text = nome
        binding.cancellaSeminarista.isVisible = editable

    }

    override fun unbindView(binding: SeminaristaRowItemBinding) {
        binding.nomeSeminarista.text = null
    }

}