package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.cammino.gestionecomunita.database.entity.Fratello
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario
import it.cammino.gestionecomunita.database.entity.VisitaSeminario
import it.cammino.gestionecomunita.database.item.ComunitaFratello

@Dao
interface VisitaSeminarioDao {

    @Query("DELETE FROM visitaseminario where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Insert
    fun insertVisite(visite: List<VisitaSeminario>)

}
