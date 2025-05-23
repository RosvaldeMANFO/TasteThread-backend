package com.florientmanfo.com.florientmanfo.models.recipe

import com.florientmanfo.com.florientmanfo.models.user.UserModel
import kotlinx.serialization.Serializable

@Serializable
data class RecipeCommentModel(
    val author: UserModel,
    val content: String,
    val createdAt: Long,
)