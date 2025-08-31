package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    USER,
    ADMIN
}