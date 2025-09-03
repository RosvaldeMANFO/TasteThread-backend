package com.florientmanfo.com.florientmanfo.models.user

interface UserRepository {
    suspend fun register(dto: RegisterDTO): Result<String>
    suspend fun login(dto: LoginDTO): Result<Login>
    suspend fun refreshToken(refreshToken: String): Result<Token>
    suspend fun getProfile(userId: String): Result<UserModel>
    suspend fun activateAccount(userId: String): Result<Unit>
    suspend fun generateTokenFromEmail(email: String): Result<String?>
    suspend fun createAdminIfNotExists(): Result<Unit>
    suspend fun resetPassword(userId: String, newPassword: String): Result<Unit>
}