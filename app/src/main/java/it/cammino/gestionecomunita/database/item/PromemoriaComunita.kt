package it.cammino.gestionecomunita.database.item

import androidx.room.Entity
import java.sql.Date

@Entity
open class PromemoriaComunita {

    var idPromemoria: Long = 0

    var idComunita: Long = 0

    var data: Date? = null

    var note: String = ""

    var numero: String = ""

    var parrocchia: String = ""

}