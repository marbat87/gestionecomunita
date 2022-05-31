package it.cammino.gestionecomunita.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ComunitaPassaggi(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val passaggi: List<Passaggio>
)