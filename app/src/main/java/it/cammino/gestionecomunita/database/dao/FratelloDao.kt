package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.cammino.gestionecomunita.database.entity.Fratello
import it.cammino.gestionecomunita.database.item.ComunitaFratello

@Dao
interface FratelloDao {

    @Query("DELETE FROM fratello where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Insert
    fun insertFratelli(fratello: List<Fratello>)

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithFratelli(idComunita: Long): ComunitaFratello?

}
