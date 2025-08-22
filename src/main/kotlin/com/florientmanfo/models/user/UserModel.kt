package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    val id: String,
    val name: String,
    val email: String,
    val activated: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)