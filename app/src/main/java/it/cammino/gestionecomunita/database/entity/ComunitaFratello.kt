package it.cammino.gestionecomunita.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ComunitaFratello(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val fratelli: List<Fratello>
)