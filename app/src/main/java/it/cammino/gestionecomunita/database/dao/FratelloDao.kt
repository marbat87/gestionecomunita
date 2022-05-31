package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.ComunitaFratello
import it.cammino.gestionecomunita.database.entity.Fratello

@Dao
interface FratelloDao {

    @Query("DELETE FROM fratello where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Insert
    fun insertFratelli(fratello: List<Fratello>)

    @Update
    fun updateFratello(fratello: Fratello)

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithFratelli(idComunita: Long): ComunitaFratello?

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveComunitaWithFratelli(idComunita: Long): LiveData<ComunitaFratello>?

}
