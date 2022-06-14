package it.cammino.gestionecomunita.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Vocazione {

    @PrimaryKey(autoGenerate = true)
    var idVocazione: Long = 0

    var nome: String = ""

    var telefono: String = ""

    var citta: String = ""

    var studi: String = ""

    var dataNascita: Date? = null

    var idTappa: Int = -1

    var dataIngresso: Date? = null

    var dataUltimaModifica = Date(System.currentTimeMillis())

    var osservazioni: String = ""

    var sesso: Sesso? = null

    @ColumnInfo(defaultValue = "-1")
    var idComunita: Long = -1

    enum class Sesso {
        MASCHIO, FEMMINA
    }

}