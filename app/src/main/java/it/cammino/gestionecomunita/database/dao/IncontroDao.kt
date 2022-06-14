package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.database.item.IncontroComunita

@Dao
interface IncontroDao {

    @Insert
    fun insertIncontro(incontro: Incontro)

    @Update
    fun updateIncontro(incontro: Incontro)

    @Delete
    fun deleteIncontro(incontro: Incontro)

    @Query("DELETE FROM incontro where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Query("SELECT * FROM incontro WHERE idIncontro = :idIncontro")
    fun getIncontroById(idIncontro: Long): Incontro?

    @get:Query("SELECT * from incontro")
    val liveAll: LiveData<List<Incontro>>

    @get:Query("SELECT a.*, COALESCE(b.numero,'') numero, COALESCE(b.parrocchia,'') parrocchia FROM incontro a LEFT JOIN comunita b ON a.idComunita = b.id order by a.data")
    val liveByDate: LiveData<List<IncontroComunita>>

    @Query("SELECT a.*, COALESCE(b.numero,'') numero, COALESCE(b.parrocchia,'') parrocchia FROM incontro a LEFT JOIN comunita b ON a.idComunita = b.id WHERE a.done= :completati order by a.data")
    fun liveByDate(completati: Boolean = false): LiveData<List<IncontroComunita>>

}
