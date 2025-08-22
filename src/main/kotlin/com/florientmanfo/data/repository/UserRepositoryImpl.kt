package com.florientmanfo.com.florientmanfo.data.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.florientmanfo.com.florientmanfo.data.entity.UsersEntity
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.models.user.Token
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.Password
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import io.ktor.server.config.*
import java.time.LocalDateTime
import java.util.*

class UserRepositoryImpl(private val config: ApplicationConfig) : UserRepository {

    override suspend fun register(dto: RegisterDTO): Result<UserModel> = suspendTransaction {
        try {
            val user = UsersEntity.new(IDGenerator.generate(IDSuffix.USER)) {
                email = dto.email
                password = Password.hash(dto.password)
                name = dto.username
                activated = true
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }.toModel()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(dto: LoginDTO): Result<Token> = suspendTransaction {
        try {
            val entity = UsersEntity.find { Users.email eq dto.email }.firstOrNull()
            if (entity != null && Password.verify(dto.password, entity.password)) {
                Result.success(generateToken(entity.id.value))
            } else {
                Result.failure(Exception("Invalid credentials"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun refreshToken(refreshToken: String): Result<Token> {
        return try {
            val secret = config.property("ktor.jwt.secret").getString()
            val algorithm = Algorithm.HMAC256(secret)
            val verifier = JWT.require(algorithm).build()
            val decodedJWT = verifier.verify(refreshToken)

            val userId = decodedJWT.getClaim("userId").asString()
            Result.success(generateToken(userId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getProfile(userId: String): Result<UserModel> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.id eq userId }.firstOrNull()
                if (entity != null) {
                    Result.success(entity.toModel())
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun generateToken(userId: String): Token {
        val secret = config.property("ktor.jwt.secret").getString()
        val algorithm = Algorithm.HMAC256(secret)

        val accessToken = JWT.create()
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + TOKEN_VALIDITY))
            .sign(algorithm)

        val refreshToken = JWT.create()
            .withClaim("userId", userId)
            .withExpiresAt(Date(System.currentTimeMillis() + REFRESH_TOKEN_VALIDITY))
            .sign(algorithm)

        return Token(
            accessToken = accessToken,
            refreshToken = refreshToken
        )
    }


    companion object {
        private const val TOKEN_VALIDITY = 36_000_00
        private const val REFRESH_TOKEN_VALIDITY = 36_000_00 * 24 * 7
    }
}