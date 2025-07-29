package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.Ingredients
import com.florientmanfo.com.florientmanfo.models.recipe.IngredientModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.id.EntityID

class IngredientsEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, IngredientsEntity>(Ingredients)

    var name by Ingredients.name
    var quantity by Ingredients.quantity
    var unit by Ingredients.unit
    var recipeId by Ingredients.recipeId
    var createdAt by Ingredients.createdAt
    var updatedAt by Ingredients.updatedAt

    fun toModel(): IngredientModel {
        return IngredientModel(
            id = id.value,
            recipeId = recipeId,
            name = name,
            quantity = quantity,
            unit = unit,
            createdAt = createdAt.toLong(),
            updatedAt = updatedAt.toLong()
        )
    }

}