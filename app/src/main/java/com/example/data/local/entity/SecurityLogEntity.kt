package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "security_logs")
data class SecurityLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val eventType: String, // "SSL_HANDSHAKE_SUCCESS", "PIN_VERIFIED", "SSL_ERROR_BLOCKED", "CACHE_CLEARED"
    val domain: String,
    val status: String, // "SECURE", "WARNING", "BLOCKED"
    val details: String,
    val fingerprint: String = ""
)
