package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Incontro
import it.cammino.gestionecomunita.database.item.IncontroComunita

@Dao
interface IncontroDao {

    @Insert
    fun insertIncontro(incontro: Incontro)

    @Insert
    fun insertIncontri(incontro: List<Incontro>)

    @Update
    fun updateIncontro(incontro: Incontro)

    @Delete
    fun deleteIncontro(incontro: Incontro)

    @Query("DELETE FROM incontro where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Query("DELETE FROM incontro")
    fun truncateTable()

    @Query("SELECT * FROM incontro WHERE idIncontro = :idIncontro")
    fun getIncontroById(idIncontro: Long): Incontro?

    @get:Query("SELECT * from incontro")
    val all: List<Incontro>

    @get:Query("SELECT * from incontro")
    val liveAll: LiveData<List<Incontro>>

//    @get:Query("SELECT a.*, COALESCE(b.numero,'') AS numero, COALESCE(b.parrocchia,'') AS parrocchia FROM incontro AS a LEFT JOIN comunita AS b ON a.idComunita = b.id ORDER BY a.data")
//    val liveByDate: LiveData<List<IncontroComunita>>

    @Query("SELECT a.*, COALESCE(b.numero,'') AS numero, COALESCE(b.parrocchia,'') AS parrocchia FROM incontro AS a LEFT JOIN comunita AS b ON a.idComunita = b.id WHERE a.done= :completati ORDER BY a.data")
    fun liveByDateFiltered(completati: Boolean = false): LiveData<List<IncontroComunita>>

}
