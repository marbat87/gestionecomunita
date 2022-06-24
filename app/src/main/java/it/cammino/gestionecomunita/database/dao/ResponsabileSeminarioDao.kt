package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario

@Dao
interface ResponsabileSeminarioDao {

    @Query("DELETE FROM responsabileseminario where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Insert
    fun insertResponsabili(responsabili: List<ResponsabileSeminario>)

}
