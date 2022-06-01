package it.cammino.gestionecomunita.database.item

import androidx.room.Embedded
import androidx.room.Relation
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Fratello

data class ComunitaFratello(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val fratelli: List<Fratello>
)