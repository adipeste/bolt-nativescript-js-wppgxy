package org.nativescript.samplejs

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.util.GeoPoint
import java.util.concurrent.TimeUnit

@Database(entities = [CachedRoute::class], version = 1)
@TypeConverters(Converters::class)
abstract class RouteDatabase : RoomDatabase() {
    abstract fun routeDao(): RouteDao

    companion object {
        @Volatile
        private var INSTANCE: RouteDatabase? = null

        fun getDatabase(context: Context): RouteDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RouteDatabase::class.java,
                    "route_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

@Entity(tableName = "cached_routes")
data class CachedRoute(
    @PrimaryKey val id: String,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double,
    val road: Road,
    val timestamp: Long
)

@Dao
interface RouteDao {
    @Query("SELECT * FROM cached_routes WHERE id = :id")
    suspend fun getRoute(id: String): CachedRoute?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: CachedRoute)

    @Query("DELETE FROM cached_routes WHERE timestamp < :timestamp")
    suspend fun deleteOldRoutes(timestamp: Long)
}

class Converters {
    @TypeConverter
    fun fromRoad(road: Road): String {
        return Gson().toJson(road)
    }

    @TypeConverter
    fun toRoad(roadString: String): Road {
        val type = object : TypeToken<Road>() {}.type
        return Gson().fromJson(roadString, type)
    }
}

class RouteCache(context: Context) {
    private val routeDao = RouteDatabase.getDatabase(context).routeDao()
    private val gson = Gson()

    suspend fun getRoute(start: GeoPoint, end: GeoPoint): Road? {
        val id = generateRouteId(start, end)
        val cachedRoute = routeDao.getRoute(id)
        return if (cachedRoute != null && !isExpired(cachedRoute.timestamp)) {
            cachedRoute.road
        } else {
            null
        }
    }

    suspend fun saveRoute(start: GeoPoint, end: GeoPoint, road: Road) {
        val id = generateRouteId(start, end)
        val cachedRoute = CachedRoute(
            id = id,
            startLat = start.latitude,
            startLon = start.longitude,
            endLat = end.latitude,
            endLon = end.longitude,
            road = road,
            timestamp = System.currentTimeMillis()
        )
        routeDao.insertRoute(cachedRoute)
        cleanOldCache()
    }

    private suspend fun cleanOldCache() {
        val expirationTime = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7)
        routeDao.deleteOldRoutes(expirationTime)
    }

    private fun generateRouteId(start: GeoPoint, end: GeoPoint): String {
        return "${start.latitude},${start.longitude}-${end.latitude},${end.longitude}"
    }

    private fun isExpired(timestamp: Long): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheDuration = TimeUnit.HOURS.toMillis(24)
        return currentTime - timestamp > cacheDirection
    }
}