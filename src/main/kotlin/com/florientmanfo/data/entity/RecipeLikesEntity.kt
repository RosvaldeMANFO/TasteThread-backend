package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeLikeModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.EntityClass
import org.jetbrains.exposed.v1.dao.IntEntity

class RecipeLikesEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : EntityClass<Int, RecipeLikesEntity>(RecipeLikes)

    val user by UsersEntity referencedOn RecipeLikes.userId
    var userId by RecipeLikes.userId
    var recipeId by RecipeLikes.recipeId
    var createdAt by RecipeLikes.createdAt

    fun toModel(): RecipeLikeModel {
        return RecipeLikeModel(
            user = user.toModel(),
            recipeId = recipeId,
            createdAt = createdAt.toLong()
        )
    }

}