package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class ComunitaSeminarista {

    @PrimaryKey(autoGenerate = true)
    var idComunitaSeminarista: Long = 0

    var idSeminarista: Long = 0

    var comunitaAssegnazione: String = ""

    var dataAssegnazione: Date? = null

    var idTappaAssegnazione: Int = 0

}