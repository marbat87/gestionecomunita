package it.cammino.catechisti.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [ForeignKey(
        entity = Comunita::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("idComunita"),
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Fratello::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("idFratello"),
        onDelete = ForeignKey.CASCADE
    )]
)
open class ComunitaFratello {

    @PrimaryKey
    var id: Int = 0

    var idComunita: Int = 0

    var idFratello: Int = 0

}