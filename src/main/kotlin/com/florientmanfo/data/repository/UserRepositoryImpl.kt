package com.florientmanfo.com.florientmanfo.data.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.florientmanfo.com.florientmanfo.data.dao.UserDAO
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.Password
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import io.ktor.server.config.*
import java.util.*

class UserRepositoryImpl(private val config: ApplicationConfig) : UserRepository {

    override suspend fun register(dto: RegisterDTO): Result<UserModel> = suspendTransaction {
        try {
            val user = UserDAO.new(IDGenerator.generate(IDSuffix.USER)) {
                email = dto.email
                password = Password.hash(dto.password)
                name = dto.username
                createdAt = java.time.LocalDateTime.now()
                updatedAt = java.time.LocalDateTime.now()
            }.toModel()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(dto: LoginDTO): Result<String> = suspendTransaction {
        try {
            val userDAO = UserDAO.find { Users.email eq dto.email }.firstOrNull()
            if (userDAO != null && Password.verify(dto.password, userDAO.password)) {
                Result.success(generateToken(userDAO.id.value))
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun generateToken(userId: String): String {
        val secret = config.property("ktor.jwt.secret").getString()
        val algorithm = Algorithm.HMAC256(secret)
        return JWT.create()
            .withClaim("userId", userId)
            .withExpiresAt(
                Date(System.currentTimeMillis() + TOKEN_VALIDITY)
            ).sign(algorithm)
    }

    companion object {
        private const val TOKEN_VALIDITY = 36_000_00 * 10
    }
}