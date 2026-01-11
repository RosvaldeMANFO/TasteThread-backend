package com.florientmanfo.data.entity

import com.florientmanfo.data.table.Instructions
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class InstructionsEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, InstructionsEntity>(Instructions)

    var description by Instructions.description
    var recipe by RecipesEntity referencedOn Instructions.recipeId
    var recipeId by Instructions.recipeId
    var createdAt by Instructions.createdAt
}