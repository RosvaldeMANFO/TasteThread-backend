package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeLikeModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.dao.EntityClass
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.id.EntityID

class RecipeLikesEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, RecipeLikesEntity>(RecipeLikes)

    val user by UsersEntity referencedOn RecipeLikes.userId
    var userId by RecipeLikes.userId
    var recipeId by RecipeLikes.recipeId
    var createdAt by RecipeLikes.createdAt

    fun toModel(): RecipeLikeModel {
        return RecipeLikeModel(user.toModel(), recipeId, createdAt.toLong())
    }

}