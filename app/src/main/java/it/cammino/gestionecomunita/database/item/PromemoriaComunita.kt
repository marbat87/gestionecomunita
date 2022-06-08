package it.cammino.gestionecomunita.database.item

import java.sql.Date

open class PromemoriaComunita {

    var idPromemoria: Long = 0

    var idComunita: Long = 0

    var data: Date? = null

    var note: String = ""

    var numero: String = ""

    var parrocchia: String = ""

}