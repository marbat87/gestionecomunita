package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista
import it.cammino.gestionecomunita.database.entity.Seminario

@Dao
interface ComunitaSeminaristaDao {

    @get:Query("SELECT * FROM comunitaseminarista")
    val all: List<ComunitaSeminarista>

    @Query("DELETE FROM comunitaseminarista where idSeminarista = :idSeminarista")
    fun truncateTableBySeminarista(idSeminarista: Long)

    @Query("DELETE FROM comunitaseminarista")
    fun truncateTable()

    @Insert
    fun insertComunita(comunita: List<ComunitaSeminarista>)

}
