package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Incontro

@Dao
interface IncontroDao {

    @Insert
    fun insertIncontro(incontro: Incontro)

    @Update
    fun updateIncontro(incontro: Incontro)

    @Delete
    fun deleteIncontro(incontro: Incontro)

    @Query("SELECT * FROM incontro WHERE idIncontro = :idIncontro")
    fun getIncontroById(idIncontro: Long): Incontro?

    @get:Query("SELECT * FROM incontro order by data")
    val allByDate: List<Incontro>?

    @get:Query("SELECT * FROM incontro order by data")
    val liveByDate: LiveData<Incontro>?

}
