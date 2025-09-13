package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val name: String? = null,
    val password: String? = null,
    val role: UserRole? = null,
    val activated: Boolean? = null,
)