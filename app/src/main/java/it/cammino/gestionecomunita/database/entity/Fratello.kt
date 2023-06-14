package it.cammino.gestionecomunita.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import it.cammino.gestionecomunita.util.StringUtils
import java.sql.Date

@Entity
open class Fratello : Comparable<Fratello> {

    @PrimaryKey(autoGenerate = true)
    var idFratello: Long = 0

    var idComunita: Long = 0

    var nome: String = ""

    var cognome: String = ""

    var statoCivile: String = ""

    var coniuge: String = ""

    var tribu: String = ""

    var annoNascita: Date? = null

    var carisma: String = ""

    var numFigli: Int = 0

    var dataInizioCammino: Date? = null

    var comunitaOrigine: String = ""

    var dataArrivo: Date? = null

    var note: String = ""

    var statoAttuale: Int = 0

    override fun compareTo(other: Fratello): Int {
        if (statoAttuale == 0 && other.statoAttuale > 0) return -1

        if (statoAttuale > 0 && other.statoAttuale == 0) return 1

        if (statoAttuale == 1 && other.statoAttuale == 2) return -1

        if (statoAttuale == 2 && other.statoAttuale == 1) return 1

        if (StringUtils.RESPONSABILE.contains(
                carisma.lowercase().trim()
            ) && !StringUtils.RESPONSABILE.contains(other.carisma.lowercase().trim())
        ) return -1

        if (!StringUtils.RESPONSABILE.contains(
                carisma.lowercase().trim()
            ) && StringUtils.RESPONSABILE.contains(other.carisma.lowercase().trim())
        ) return 1

        if (StringUtils.VICE_RESPONSABILE.contains(
                carisma.lowercase().trim()
            ) && !StringUtils.VICE_RESPONSABILE.contains(other.carisma.lowercase().trim())
        ) return -1

        if (!StringUtils.VICE_RESPONSABILE.contains(
                carisma.lowercase().trim()
            ) && StringUtils.VICE_RESPONSABILE.contains(other.carisma.lowercase().trim())
        ) return 1

        val thisName = "$nome $cognome".lowercase()
        val otherName = "${other.nome} ${other.cognome}".lowercase()

        return thisName.compareTo(otherName)

    }

}