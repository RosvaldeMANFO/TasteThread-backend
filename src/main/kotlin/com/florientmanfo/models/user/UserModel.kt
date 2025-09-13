package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    val id: String,
    val name: String,
    val password: String,
    val email: String,
    val imageUrl: String?,
    val role: UserRole,
    val activated: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)