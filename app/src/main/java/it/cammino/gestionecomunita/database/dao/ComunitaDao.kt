package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.cammino.gestionecomunita.database.entity.Comunita

@Dao
interface ComunitaDao {

    @get:Query("SELECT * FROM comunita ORDER BY parrocchia, numero ASC")
    val allByName: List<Comunita>

    @get:Query("SELECT * FROM comunita ORDER BY parrocchia, numero ASC")
    val liveAll: LiveData<List<Comunita>>

    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getById(idComunita: Int): Comunita?

    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveById(idComunita: Int): LiveData<Comunita>

    @Insert
    fun insertComunita(comunita: Comunita)

    @Update
    fun updateComnuita(comunita: Comunita)

}
