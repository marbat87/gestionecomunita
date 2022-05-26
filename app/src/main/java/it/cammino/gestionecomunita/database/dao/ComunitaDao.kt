package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Comunita

@Dao
interface ComunitaDao {

    @get:Query("SELECT * FROM comunita ORDER BY parrocchia, numero ASC")
    val allByName: List<Comunita>

    @get:Query("SELECT * FROM comunita ORDER BY parrocchia, numero ASC")
    val liveAll: LiveData<List<Comunita>>

    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getById(idComunita: Long): Comunita?

    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveById(idComunita: Long): LiveData<Comunita>

    @Insert
    fun insertComunita(comunita: Comunita): Long

    @Update
    fun updateComnuita(comunita: Comunita)

    @Delete
    fun deleteComunita(comunita: Comunita)

}
