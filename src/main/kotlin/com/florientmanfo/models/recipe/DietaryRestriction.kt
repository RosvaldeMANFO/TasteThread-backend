package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
enum class DietaryRestriction( val displayName: String) {
    VEGETARIAN("Vegetarian"),
    VEGAN("Vegan"),
    GLUTEN_FREE("Gluten Free"),
    DAIRY_FREE("Dairy Free"),
    NUT_FREE("Nut Free"),
    NONE("None");

    fun toDisplayName(): String {
        return this.displayName
    }
    companion object {
        fun fromDisplayName(displayName: String): DietaryRestriction {
            return entries.find { it.displayName.equals(displayName, ignoreCase = true) } ?: NONE
        }
    }
}