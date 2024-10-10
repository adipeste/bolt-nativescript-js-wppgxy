package org.nativescript.samplejs

import android.content.Context
import androidx.room.*

@Database(entities = [Favorite::class], version = 1, exportSchema = false)
abstract class FavoritesDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao

    companion object {
        @Volatile
        private var INSTANCE: FavoritesDatabase? = null

        fun getDatabase(context: Context): FavoritesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FavoritesDatabase::class.java,
                    "favorites_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String
)

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    suspend fun getAllFavorites(): List<Favorite>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: Favorite)

    @Delete
    suspend fun deleteFavorite(favorite: Favorite)
}

class FavoritesRepository(private val favoritesDao: FavoritesDao) {
    suspend fun getAllFavorites(): List<Favorite> = favoritesDao.getAllFavorites()
    suspend fun insertFavorite(favorite: Favorite) = favoritesDao.insertFavorite(favorite)
    suspend fun deleteFavorite(favorite: Favorite) = favoritesDao.deleteFavorite(favorite)
}