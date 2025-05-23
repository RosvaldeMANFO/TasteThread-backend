package com.florientmanfo.com.florientmanfo.models.user

interface UserRepository {
    suspend fun register(dto: RegisterDTO): Result<UserModel>
    suspend fun login(dto: LoginDTO): Result<String>
}