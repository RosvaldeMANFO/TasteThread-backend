package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
data class Token(
    val refreshToken: String,
    val accessToken: String,
)