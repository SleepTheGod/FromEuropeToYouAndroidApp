package com.example.model

data class UserProfile(
    val name: String = "Collector Guest",
    val email: String = "collector@antique-vault.com",
    val isAuthenticated: Boolean = false,
    val memberTier: String = "VIP Connoisseur",
    val tokenVaultStatus: String = "Hardware Keystore AES-256-GCM Active",
    val joinedDate: String = "August 2026",
    val preferredCategories: List<String> = listOf("Fireplace Mantels", "French Architectural", "Statuary")
)
