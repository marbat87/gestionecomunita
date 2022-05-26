package it.cammino.gestionecomunita.database.converter

import androidx.room.TypeConverter
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

}
