package com.florientmanfo.com.florientmanfo.data.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.florientmanfo.com.florientmanfo.data.entity.UsersEntity
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.user.Login
import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.models.user.Token
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import com.florientmanfo.com.florientmanfo.models.user.UserRole
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.Password
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import io.ktor.server.config.*
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.time.LocalDateTime
import java.util.*

class UserRepositoryImpl(private val config: ApplicationConfig) : UserRepository {

    override suspend fun register(dto: RegisterDTO): Result<UserModel> = suspendTransaction {
        try {
            val user = UsersEntity.new(IDGenerator.generate(IDSuffix.USER)) {
                email = dto.email
                password = Password.hash(dto.password)
                name = dto.username
                activated = false
                role = UserRole.USER.name
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }.toModel()
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(dto: LoginDTO): Result<Login> = suspendTransaction {
        try {
            val entity = UsersEntity.find { Users.email eq dto.email }.firstOrNull()
            if (entity != null && Password.verify(dto.password, entity.password)) {
                val nextLink = if (entity.role == UserRole.ADMIN.name)
                    config.property("ktor.admin.afterAuthLink").getString()
                else null
                Result.success(
                    Login(
                        nextLink = nextLink,
                        token = generateToken(entity.id.value)
                    )
                )
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

    override suspend fun activateAccount(userId: String): Result<String> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.id eq userId }.firstOrNull()
                if (entity != null) {
                    entity.activated = true
                    entity.updatedAt = LocalDateTime.now()
                    Result.success("Account activated successfully")
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun createAdminIfNotExists(): Result<Unit> {
        val adminEmail = config.property("ktor.admin.email").getString()
        val adminPassword = config.property("ktor.admin.password").getString()
        val adminName = config.property("ktor.admin.name").getString()
        return suspendTransaction {
            try {
                val admin = Users.selectAll().where { Users.email eq adminEmail }.firstOrNull()
                if (admin == null) {
                    UsersEntity.new(IDGenerator.generate(IDSuffix.USER)) {
                        email = adminEmail
                        password = Password.hash(adminPassword)
                        name = adminName
                        activated = false
                        role = UserRole.ADMIN.name
                        createdAt = LocalDateTime.now()
                        updatedAt = LocalDateTime.now()
                    }
                }
                Result.success(Unit)
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