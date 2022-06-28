package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario

@Dao
interface ResponsabileSeminarioDao {

    @get:Query("SELECT * FROM responsabileseminario")
    val all: List<ResponsabileSeminario>

    @Query("DELETE FROM responsabileseminario where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Query("DELETE FROM responsabileseminario")
    fun truncateTable()

    @Insert
    fun insertResponsabili(responsabili: List<ResponsabileSeminario>)

}
