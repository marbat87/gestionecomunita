package it.cammino.gestionecomunita.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import it.cammino.gestionecomunita.database.entity.Promemoria
import it.cammino.gestionecomunita.database.item.PromemoriaComunita

@Dao
interface PromemoriaDao {

    @get:Query("SELECT a.*, b.numero, b.parrocchia FROM promemoria a, comunita b WHERE a.idComunita = b.id ORDER BY case when data is null then 1 else 0 end, data")
    val liveAllWithComunita: LiveData<List<PromemoriaComunita>>

    @get:Query("SELECT * FROM promemoria ORDER BY data asc")
    val liveAll: LiveData<List<Promemoria>>

    @get:Query("SELECT * FROM promemoria")
    val all: List<Promemoria>

    @Query("SELECT * FROM promemoria where idPromemoria = :idPromemoria")
    fun getById(idPromemoria: Long): Promemoria?

    @Delete
    fun deletePromemoria(promemoria: Promemoria)

    @Query("DELETE FROM promemoria where idComunita = :idComunita")
    fun truncateTableByComunita(idComunita: Long)

    @Query("DELETE FROM promemoria")
    fun truncateTable()

    @Insert
    fun insertPromemoria(promemoria: Promemoria)

    @Insert
    fun insertPromemoria(promemoria: List<Promemoria>)

    @Update
    fun updatePromemoria(promemoria: Promemoria)

}
