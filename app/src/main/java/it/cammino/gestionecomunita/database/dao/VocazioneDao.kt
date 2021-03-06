package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Vocazione

@Dao
interface VocazioneDao {

    @get:Query("SELECT * FROM vocazione")
    val all: List<Vocazione>

    @get:Query("SELECT * FROM vocazione")
    val liveAll: LiveData<List<Vocazione>>

    @Query("SELECT * FROM vocazione where idVocazione = :idVocazione")
    fun getById(idVocazione: Long): Vocazione?

    @Insert
    fun insertVocazione(vocazione: Vocazione): Long

    @Update
    fun updateVocazione(vocazione: Vocazione)

    @Delete
    fun deleteVocazione(vocazione: Vocazione)

}
