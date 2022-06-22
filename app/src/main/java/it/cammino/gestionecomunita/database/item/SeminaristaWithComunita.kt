package it.cammino.gestionecomunita.database.item

import androidx.room.Embedded
import androidx.room.Relation
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista
import it.cammino.gestionecomunita.database.entity.Seminarista

data class SeminaristaWithComunita(
    @Embedded
    val seminarista: Seminarista,

    @Relation(
        parentColumn = "idSeminarista",
        entityColumn = "idSeminarista",
    )
    val comunita: List<ComunitaSeminarista>
)