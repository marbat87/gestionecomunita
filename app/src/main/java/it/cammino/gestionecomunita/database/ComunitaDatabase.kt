package it.cammino.gestionecomunita.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import it.cammino.catechisti.database.converter.Converters
import it.cammino.gestionecomunita.database.entity.Comunita
import it.cammino.gestionecomunita.database.entity.ComunitaFratello
import it.cammino.gestionecomunita.database.entity.Fratello
import it.cammino.gestionecomunita.database.dao.ComunitaDao

@Database(
    entities = [(Comunita::class), (ComunitaFratello::class), (Fratello::class)],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ComunitaDatabase : RoomDatabase() {

    abstract fun comunitaDao(): ComunitaDao

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
//                        .addCallback(object : Callback() {
//                            /**
//                             * Called when the database is created for the first time. This is called after all the
//                             * tables are created.
//                             *
//                             * @param db The database.
//                             */
//                            override fun onCreate(db: SupportSQLiteDatabase) {
//                                super.onCreate(db)
//                                Log.d(TAG, "Callback onCreate")
////                                GlobalScope.launch(Dispatchers.IO) { insertDefaultData(sInstance as ComunitaDatabase) }
//                            }
//                        })
                        .build()
                }
            } else
                Log.d(TAG, "getInstance: EXISTS")
            return sInstance as ComunitaDatabase
        }

    }
}
