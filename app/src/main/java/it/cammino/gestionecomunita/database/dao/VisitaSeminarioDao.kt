package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.VisitaSeminario

@Dao
interface VisitaSeminarioDao {

    @get:Query("SELECT * FROM visitaseminario")
    val all: List<VisitaSeminario>

    @Query("DELETE FROM visitaseminario where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Query("DELETE FROM visitaseminario")
    fun truncateTable()

    @Insert
    fun insertVisite(visite: List<VisitaSeminario>)

}
