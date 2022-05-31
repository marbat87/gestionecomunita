package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.ComunitaPromemoria
import it.cammino.gestionecomunita.database.entity.Promemoria

@Dao
interface PromemoriaDao {

    @Query("DELETE FROM promemoria where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Insert
    fun insertPromemoria(promemoria: Promemoria)

    @Insert
    fun insertPromemoria(promemoria: List<Promemoria>)

    @Update
    fun updatePromemoria(promemoria: Promemoria)

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithPromemoria(idComunita: Long): ComunitaPromemoria?

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveComunitaWithPromemoria(idComunita: Long): LiveData<ComunitaPromemoria>?

}
