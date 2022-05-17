package it.cammino.catechisti.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Comunita {

    @PrimaryKey
    var id: Int = 0

    var diocesi: String = ""

    var comunita: String = ""

    var parrocchia: String = ""

    var responsabile: String = ""

    var numAnni: Int = 0

    var email: String = ""

    var telefono: String = ""

    var idTappa: Int = 0

    var dataUltimaModifica= Date(System.currentTimeMillis())

    var dataUltimaVisita: Date? = null

    var dataConvivenza: Date? = null

    var note: String = ""

}