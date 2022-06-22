package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.sql.Date

@Entity
open class ResponsabileSeminario {

    @PrimaryKey(autoGenerate = true)
    var idResponsabile: Long = 0

    var idSeminario: Long = 0

    var nome: String = ""

    var dataInizioIncarico: Date? = null

    var dataFineIncarico: Date? = null

    var incarico: Incarico = Incarico.RETTORE

    enum class Incarico {
        RETTORE, VICE_RETTORE, DIRETTORE_SPIRITUALE
    }

}