package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import it.cammino.gestionecomunita.database.entity.ComunitaSeminarista

@Dao
interface ComunitaSeminaristaDao {

    @Query("DELETE FROM comunitaseminarista where idSeminarista = :idSeminarista")
    fun truncateTableBySeminarista(idSeminarista: Long)

    @Insert
    fun insertComunita(comunita: List<ComunitaSeminarista>)

}
