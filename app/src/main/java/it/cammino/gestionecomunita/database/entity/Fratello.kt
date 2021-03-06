package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Fratello {

    @PrimaryKey(autoGenerate = true)
    var idFratello: Long = 0

    var idComunita: Long = 0

    var nome: String = ""

    var cognome: String = ""

    var statoCivile: String = ""

    var coniuge: String = ""

    var tribu: String = ""

    var annoNascita: Date? = null

    var carisma: String = ""

    var numFigli: Int = 0

    var dataInizioCammino: Date? = null

    var comunitaOrigine: String = ""

    var dataArrivo: Date? = null

    var note: String = ""

    var statoAttuale: Int = 0

}