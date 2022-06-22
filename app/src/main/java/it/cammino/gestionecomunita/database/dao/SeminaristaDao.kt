package it.cammino.gestionecomunita.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import it.cammino.gestionecomunita.database.entity.Seminarista
import it.cammino.gestionecomunita.database.item.SeminaristaWithComunita

@Dao
interface SeminaristaDao {

    @Query("DELETE FROM seminarista where idSeminario = :idSeminario")
    fun truncateTableBySeminario(idSeminario: Long)

    @Insert
    fun insertSeminaristi(seminaristi: List<Seminarista>)

    @Insert
    fun insertSeminarista(seminaristi: Seminarista): Long

    @Transaction
    @Query("SELECT * FROM seminarista WHERE idSeminario = :idSeminario")
    fun getBySeminarioWithDetails(idSeminario: Long): List<SeminaristaWithComunita>

}
