package com.example.model

data class AntiqueCategory(
    val id: String,
    val title: String,
    val description: String,
    val iconName: String,
    val urlPath: String,
    val estimatedItems: String
)

object AntiqueCategories {
    val ITEMS = listOf(
        AntiqueCategory(
            id = "mantels",
            title = "Fireplace Mantels",
            description = "18th & 19th Century French Marble & Limestone Carved Mantels",
            iconName = "fireplace",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "140+ Pieces"
        ),
        AntiqueCategory(
            id = "doors_gates",
            title = "Doors & Iron Gates",
            description = "Hand-forged French wrought iron gates, antique castle doors & grilles",
            iconName = "door",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "85+ Sets"
        ),
        AntiqueCategory(
            id = "statuary",
            title = "Garden & Statuary",
            description = "Carved limestone fountains, classical bronze urns, and garden statues",
            iconName = "park",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "210+ Sculptures"
        ),
        AntiqueCategory(
            id = "fountains",
            title = "Stone Fountains",
            description = "Historic French tiered wall fountains & center courtyard fountains",
            iconName = "water_drop",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "60+ Fountains"
        ),
        AntiqueCategory(
            id = "furniture",
            title = "Fine Furniture",
            description = "Louis XV, Louis XVI, and Empire bureaus, commodes, and salon suites",
            iconName = "chair",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "320+ Antiques"
        ),
        AntiqueCategory(
            id = "lighting",
            title = "Chandeliers & Sconces",
            description = "Baccarat crystal chandeliers, French gilded bronze wall sconces",
            iconName = "light",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "95+ Fixtures"
        ),
        AntiqueCategory(
            id = "architectural",
            title = "Architectural Salvage",
            description = "Corbels, stone columns, balustrades, terra cotta tiles, and beams",
            iconName = "account_balance",
            urlPath = "https://www.fromeuropetoyou.com/",
            estimatedItems = "450+ Elements"
        )
    )
}
