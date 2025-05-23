package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
data class LoginDTO(val email: String, val password: String)