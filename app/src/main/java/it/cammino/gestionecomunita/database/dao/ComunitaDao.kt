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

    @Query("DELETE FROM comunita")
    fun truncateTable()

    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getById(idComunita: Long): Comunita?

    @Insert
    fun insertComunita(comunita: Comunita): Long

    @Insert
    fun insertComunita(comunita: List<Comunita>)

    @Update
    fun updateComnuita(comunita: Comunita)

    @Delete
    fun deleteComunita(comunita: Comunita)

}
