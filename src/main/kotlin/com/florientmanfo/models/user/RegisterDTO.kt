package com.florientmanfo.com.florientmanfo.models.user

import kotlinx.serialization.Serializable

@Serializable
data class RegisterDTO(val email: String, val password: String, val username: String)