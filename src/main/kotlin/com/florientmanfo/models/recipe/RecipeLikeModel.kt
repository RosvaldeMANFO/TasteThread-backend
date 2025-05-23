package com.florientmanfo.com.florientmanfo.models.recipe

import com.florientmanfo.com.florientmanfo.models.user.UserModel
import kotlinx.serialization.Serializable

@Serializable
class RecipeLikeModel(
    val user: UserModel,
    val recipeId: String,
    val createdAt: Long,
)