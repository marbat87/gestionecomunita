package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import it.cammino.gestionecomunita.database.entity.Fratello

@Dao
interface FratelloDao {

    @Query("DELETE FROM fratello where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Insert
    fun insertFratelli(fratello: List<Fratello>)

    @Update
    fun updateFratello(fratello: Fratello)

}
