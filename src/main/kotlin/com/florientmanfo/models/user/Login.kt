package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
data class Login(
    val token: Token,
    val activated: Boolean,
    val nextLink: String?,
)