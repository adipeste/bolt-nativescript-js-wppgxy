package org.nativescript.samplejs

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "maps")
data class MapEntity(
    @PrimaryKey val region: String,
    val downloadDate: Long
)