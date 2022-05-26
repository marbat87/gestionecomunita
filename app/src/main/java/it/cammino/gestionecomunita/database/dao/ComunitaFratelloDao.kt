package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.ComunitaFratello

@Dao
interface ComunitaFratelloDao {

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithFratelli(idComunita: Long): ComunitaFratello?

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveComunitaWithFratelli(idComunita: Long): LiveData<ComunitaFratello>?

}
