package org.nativescript.samplejs

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MapDao {
    @Query("SELECT * FROM maps")
    suspend fun getAllMaps(): List<MapEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(map: MapEntity)

    @Query("DELETE FROM maps WHERE region = :region")
    suspend fun deleteMap(region: String)
}