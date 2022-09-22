package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.cammino.gestionecomunita.database.entity.Passaggio
import it.cammino.gestionecomunita.database.item.ComunitaPassaggi

@Dao
interface PassaggioDao {

    @Insert
    fun insertPassaggio(passaggio: Passaggio)

    @Insert
    fun insertPassaggi(passaggio: List<Passaggio>)

    @Transaction
    @Query("SELECT * FROM comunita where id = :idComunita")
    fun getComunitaWithPassaggi(idComunita: Long): ComunitaPassaggi?

    @Query("DELETE FROM passaggio where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Query("DELETE FROM passaggio")
    fun truncateTable()

    @get:Query("SELECT * FROM passaggio")
    val all: List<Passaggio>

}
