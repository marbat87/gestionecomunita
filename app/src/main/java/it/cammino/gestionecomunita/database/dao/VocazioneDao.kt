package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Vocazione

@Dao
interface VocazioneDao {

    @get:Query("SELECT * FROM vocazione")
    val all: List<Vocazione>

    @get:Query("SELECT * FROM vocazione order by nome")
    val liveAll: LiveData<List<Vocazione>>

    @Query("SELECT * FROM vocazione where idVocazione = :idVocazione")
    fun getById(idVocazione: Long): Vocazione?

    @Insert
    fun insertVocazione(vocazione: Vocazione): Long

    @Insert
    fun insertVocazioni(vocazione: List<Vocazione>)

    @Update
    fun updateVocazione(vocazione: Vocazione)

    @Delete
    fun deleteVocazione(vocazione: Vocazione)

    @Query("DELETE FROM vocazione where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Query("DELETE FROM vocazione")
    fun truncateTable()

}
