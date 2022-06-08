package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Incontro {

    @PrimaryKey(autoGenerate = true)
    var idIncontro: Long = 0

    var nome: String = ""

    var cognome: String = ""

    var idComunita: Long = -1

    var data: Date? = null

    var luogo: String = ""

    var note: String = ""

}