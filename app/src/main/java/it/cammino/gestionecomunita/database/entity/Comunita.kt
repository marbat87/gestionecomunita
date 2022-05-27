package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Comunita {

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

    var diocesi: String = ""

    var numero: String = ""

    var parrocchia: String = ""

    var parroco: String = ""

    var responsabile: String = ""

    var catechisti: String = ""

    var numAnni: Int = 0

    var email: String = ""

    var telefono: String = ""

    var idTappa: Int = -1

    var dataUltimaModifica= Date(System.currentTimeMillis())

    var dataUltimaVisita: Date? = null

    var dataConvivenza: Date? = null

    var note: String = ""

}