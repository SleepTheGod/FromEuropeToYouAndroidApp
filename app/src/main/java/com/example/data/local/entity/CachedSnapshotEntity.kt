package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_snapshots")
data class CachedSnapshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val url: String,
    val title: String,
    val summary: String = "",
    val htmlContent: String = "",
    val cachedTimestamp: Long = System.currentTimeMillis(),
    val sizeBytes: Long = 0,
    val category: String = "Catalog"
)
