package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Seminarista {

    @PrimaryKey(autoGenerate = true)
    var idSeminarista: Long = 0

    var idSeminario: Long = 0

    var nome: String = ""
    var dataNascita: Date? = null
    var nazione: String = ""

    var comuntiaProvenienza: String = ""
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

}