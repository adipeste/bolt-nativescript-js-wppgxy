package org.nativescript.samplejs

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MapEntity::class], version = 1, exportSchema = false)
abstract class MapDatabase : RoomDatabase() {
    abstract fun mapDao(): MapDao

    companion object {
        @Volatile
        private var INSTANCE: MapDatabase? = null

        fun getDatabase(context: Context): MapDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapDatabase::class.java,
                    "map_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}