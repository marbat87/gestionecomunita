package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Seminario
import it.cammino.gestionecomunita.database.item.SeminarioWithDetails

@Dao
interface SeminarioDao {

    @get:Query("SELECT * FROM seminario ORDER BY nome ASC")
    val allByName: List<Seminario>

    @get:Query("SELECT * FROM seminario ORDER BY nome ASC")
    val liveAll: LiveData<List<Seminario>>

    @Query("SELECT * FROM seminario where id = :idSeminario")
    fun getById(idSeminario: Long): Seminario?

    @Transaction
    @Query("SELECT * FROM seminario where id = :idSeminario")
    fun getByIdWithDetails(idSeminario: Long): SeminarioWithDetails

    @Insert
    fun insertSeminario(seminario: Seminario): Long

    @Update
    fun updateSeminario(seminario: Seminario)

    @Delete
    fun deleteSeminario(seminario: Seminario)

}
