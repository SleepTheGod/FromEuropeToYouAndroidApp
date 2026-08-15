package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String = "Arrivals", // "Arrivals", "Salvage", "Security", "Promotions"
    val targetUrl: String = "https://www.fromeuropetoyou.com/",
    val isRead: Boolean = false
)
