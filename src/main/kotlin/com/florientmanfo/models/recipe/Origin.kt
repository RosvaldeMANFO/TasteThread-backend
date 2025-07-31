package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
enum class Origin(val displayName: String) {
    AMERICAN("American"),
    ITALIAN("Italian"),
    MEXICAN("Mexican"),
    CHINESE("Chinese"),
    INDIAN("Indian"),
    FRENCH("French"),
    JAPANESE("Japanese"),
    GERMAN("German"),
    SPANISH("Spanish"),
    THAI("Thai"),
    MEDITERRANEAN("Mediterranean"),
    KOREAN("Korean"),
    VIETNAMESE("Vietnamese"),
    BRAZILIAN("Brazilian"),
    RUSSIAN("Russian"),
    NIGERIAN("Nigerian"),
    SOUTH_AFRICAN("South African"),
    MOROCCAN("Moroccan"),
    ETHIOPIAN("Ethiopian"),
    EGYPTIAN("Egyptian"),
    SENEGALESE("Senegalese"),
    GHANAIAN("Ghanaian"),
    CAMEROONIAN("Cameroonian"),
    IVORIAN("Ivorian"),
    TUNISIAN("Tunisian"),
    UNKNOWN("Unknown");

    fun toDisplayName(): String {
        return this.displayName
    }

    companion object {
        fun fromDisplayName(displayName: String): Origin {
            return entries.find { it.displayName.equals(displayName, ignoreCase = true) } ?: UNKNOWN
        }
    }
}