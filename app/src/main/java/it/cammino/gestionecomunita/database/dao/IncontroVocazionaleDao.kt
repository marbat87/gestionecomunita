package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.IncontroVocazionale

@Dao
interface IncontroVocazionaleDao {

    @get:Query("SELECT * FROM incontrovocazionale")
    val all: List<IncontroVocazionale>

    @get:Query("SELECT * FROM incontrovocazionale order by data desc")
    val liveAll: LiveData<List<IncontroVocazionale>>

    @Query("SELECT * FROM incontrovocazionale where idIncontro = :idIncontro")
    fun getById(idIncontro: Long): IncontroVocazionale?

    @Insert
    fun insertIncontroVocazionale(incontroVocazionale: IncontroVocazionale): Long

    @Insert
    fun insertIncontroVocazionale(incontroVocazionale: List<IncontroVocazionale>)

    @Update
    fun updateIncontroVocazionale(incontroVocazionale: IncontroVocazionale)

    @Delete
    fun deleteIncontroVocazionale(incontroVocazionale: IncontroVocazionale)

    @Query("DELETE FROM incontrovocazionale")
    fun truncateTable()

}
