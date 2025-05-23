package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable


@Serializable
data class UserModel(
    val name: String,
    val email: String,
    val createdAt: Long,
    val updatedAt: Long,
)