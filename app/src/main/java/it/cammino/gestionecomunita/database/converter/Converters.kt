package it.cammino.gestionecomunita.database.converter

import androidx.room.TypeConverter
import it.cammino.gestionecomunita.database.entity.ResponsabileSeminario
import it.cammino.gestionecomunita.database.entity.Vocazione
import java.sql.Date

@Suppress("unused")
class Converters {

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return if (value == null) null else Date(value)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toSesso(value: String) = enumValueOf<Vocazione.Sesso>(value)

    @TypeConverter
    fun fromSesso(value: Vocazione.Sesso) = value.name

    @TypeConverter
    fun toIncarico(value: String) = enumValueOf<ResponsabileSeminario.Incarico>(value)

    @TypeConverter
    fun fromIncarico(value: ResponsabileSeminario.Incarico) = value.name

}
