package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.VisitaSeminario

@Dao
interface VisitaSeminarioDao {

    @Query("DELETE FROM visitaseminario where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Insert
    fun insertVisite(visite: List<VisitaSeminario>)

}
