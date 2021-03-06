package it.cammino.gestionecomunita.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.cammino.gestionecomunita.database.converter.Converters
import it.cammino.gestionecomunita.database.dao.*
import it.cammino.gestionecomunita.database.entity.*

@Database(
    entities = [(Comunita::class), (Fratello::class), (Promemoria::class), (Passaggio::class), (Vocazione::class)],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ComunitaDatabase : RoomDatabase() {

    abstract fun comunitaDao(): ComunitaDao
    abstract fun fratelloDao(): FratelloDao
    abstract fun passaggioDao(): PassaggioDao
    abstract fun promemoriaDao(): PromemoriaDao
    abstract fun vocazioneDao(): VocazioneDao

    companion object {

        private const val TAG = "ComunitaDatabase"

        private const val dbName = "ComunitaDB"

        // For Singleton instantiation
        private val LOCK = Any()

        private var sInstance: ComunitaDatabase? = null

        /**
         * Gets the singleton instance of RisuscitoDatabase.
         *
         * @param context The context.
         * @return The singleton instance of RisuscitoDatabase.
         */
        @Synchronized
        fun getInstance(context: Context): ComunitaDatabase {
            Log.d(TAG, "getInstance()")
            if (sInstance == null) {
                Log.d(TAG, "getInstance: NULL")
                synchronized(LOCK) {
                    sInstance = Room.databaseBuilder(
                        context.applicationContext,
                        ComunitaDatabase::class.java,
                        dbName
                    )
                        .build()
                }
            } else
                Log.d(TAG, "getInstance: EXISTS")
            return sInstance as ComunitaDatabase
        }

    }
}
