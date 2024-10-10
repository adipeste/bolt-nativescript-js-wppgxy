package org.nativescript.samplejs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val startLat: Double,
    val startLon: Double,
    val endLat: Double,
    val endLon: Double,
    val route: List<RoutePoint>
)

data class RoutePoint(
    val latitude: Double,
    val longitude: Double
)