package com.florientmanfo.com.florientmanfo.models.recipe

import kotlinx.serialization.Serializable

@Serializable
enum class MealType(val displayName: String) {
    BREAKFAST("Breakfast"),
    LUNCH("Lunch"),
    DINNER("Dinner"),
    SNACK("Snack"),
    DESSERT("Dessert"),
    BRUNCH("Brunch"),
    APPETIZER("Appetizer"),
    SIDE_DISH("Side Dish"),
    MAIN_COURSE("Main Course"),
    SALAD("Salad"),
    SOUP("Soup"),
    BEVERAGE("Beverage"),
    UNKNOWN("Unknown");

    fun toDisplayName(): String {
        return this.displayName
    }

    companion object {
        fun fromDisplayName(displayName: String): MealType {
            return entries.find { it.displayName.equals(displayName, ignoreCase = true) } ?: UNKNOWN
        }
    }
}