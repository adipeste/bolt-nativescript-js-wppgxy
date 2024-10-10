package org.nativescript.samplejs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RouteDao {
    @Query("SELECT * FROM routes WHERE startLat = :startLat AND startLon = :startLon AND endLat = :endLat AND endLon = :endLon LIMIT 1")
    suspend fun getRoute(startLat: Double, startLon: Double, endLat: Double, endLon: Double): RouteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(route: RouteEntity)
}