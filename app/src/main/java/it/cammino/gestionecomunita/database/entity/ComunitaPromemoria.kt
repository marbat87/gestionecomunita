package it.cammino.gestionecomunita.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ComunitaPromemoria(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val promemoria: List<Promemoria>
)