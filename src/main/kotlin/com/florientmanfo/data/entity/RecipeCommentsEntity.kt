package com.florientmanfo.com.florientmanfo.data.entity

import com.florientmanfo.com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.com.florientmanfo.models.recipe.RecipeCommentModel
import com.florientmanfo.com.florientmanfo.utils.toLong
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass

class RecipeCommentsEntity(id: EntityID<String>): Entity<String>(id) {
    companion object: EntityClass<String, RecipeCommentsEntity>(RecipeComments)

    private val author by UsersEntity referencedOn RecipeComments.authorId
    var recipeId by RecipeComments.recipeId
    var authorId by RecipeComments.authorId
    var content by RecipeComments.content
    var createdAt by RecipeComments.createdAt

    fun toModel(): RecipeCommentModel {
        return RecipeCommentModel(
            author= author.toModel(),
            content = content,
            createdAt = createdAt.toLong()
        )
    }
}