package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class Passaggio {

    @PrimaryKey(autoGenerate = true)
    var idPassaggio: Long = 0

    var idComunita: Long = 0

    var data: Date? = null

    var passaggio: Int = 0

}