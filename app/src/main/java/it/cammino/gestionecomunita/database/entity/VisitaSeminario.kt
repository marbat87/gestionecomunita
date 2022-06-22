package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class VisitaSeminario {

    @PrimaryKey(autoGenerate = true)
    var idVisita: Long = 0

    var idSeminario: Long = 0

    var formatoriPresenti: String = ""

    var seminaristiPresenti: String = ""

    var note: String = ""

}