package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class IncontroVocazionale {

    @PrimaryKey(autoGenerate = true)
    var idIncontro: Long = 0

    var tipo: Tipo = Tipo.INCONTRO

    var data: Date? = null

    var luogo: String = ""

    var note: String = ""

    enum class Tipo {
        CONVIVENZA, INCONTRO
    }

}