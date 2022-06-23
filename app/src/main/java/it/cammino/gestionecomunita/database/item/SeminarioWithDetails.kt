package it.cammino.gestionecomunita.database.item

import androidx.room.Embedded
import androidx.room.Relation
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario
import it.cammino.gestionecomunita.database.entity.Seminario
import it.cammino.gestionecomunita.database.entity.Seminarista
import it.cammino.gestionecomunita.database.entity.VisitaSeminario

data class SeminarioWithDetails(
    @Embedded
    val seminario: Seminario,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSeminario",
    )
    val responsabili: List<ResponsabileSeminario>,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSeminario",
    )
    val visite: List<VisitaSeminario>,

    @Relation(
        parentColumn = "id",
        entityColumn = "idSeminario",
    )
    val seminaristi: List<Seminarista>,

)