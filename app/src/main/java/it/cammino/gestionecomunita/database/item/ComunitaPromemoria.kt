package it.cammino.gestionecomunita.database.item

import androidx.room.Embedded
import androidx.room.Relation
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Promemoria

data class ComunitaPromemoria(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val promemoria: List<Promemoria>
)