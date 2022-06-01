package it.cammino.gestionecomunita.database.item

import androidx.room.Embedded
import androidx.room.Relation
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.Passaggio

data class ComunitaPassaggi(
    @Embedded
    val comunita: Comunita,
    @Relation(
        parentColumn = "id",
        entityColumn = "idComunita",
    )
    val passaggi: List<Passaggio>
)