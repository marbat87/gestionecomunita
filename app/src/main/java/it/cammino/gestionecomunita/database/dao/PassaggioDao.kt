package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.item.ComunitaPassaggi
import it.cammino.gestionecomunita.database.entity.Passaggio

@Dao
interface PassaggioDao {

    @Query("DELETE FROM passaggio where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Insert
    fun insertPassaggio(passaggio: Passaggio)

    @Insert
    fun insertPassaggi(passaggi: List<Passaggio>)

    @Update
    fun updatePassaggio(passaggio: Passaggio)

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithPassaggi(idComunita: Long): ComunitaPassaggi?

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun liveComunitaWithPassaggi(idComunita: Long): LiveData<ComunitaPassaggi>?

}
