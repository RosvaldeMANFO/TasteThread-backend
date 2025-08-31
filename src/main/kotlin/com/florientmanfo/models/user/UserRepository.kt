package com.florientmanfo.com.florientmanfo.models.user

interface UserRepository {
    suspend fun register(dto: RegisterDTO): Result<UserModel>
    suspend fun login(dto: LoginDTO): Result<Token>
    suspend fun refreshToken(refreshToken: String): Result<Token>
    suspend fun getProfile(userId: String): Result<UserModel>
    suspend fun activateAccount(userId: String): Result<String>
}