package com.florientmanfo.com.florientmanfo.data.repository

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.florientmanfo.com.florientmanfo.data.entity.RecipesEntity
import com.florientmanfo.com.florientmanfo.data.entity.UsersEntity
import com.florientmanfo.com.florientmanfo.data.repository.FirebaseRepositoryImpl.Companion.BucketPath
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.models.firebase.FirebaseRepository
import com.florientmanfo.com.florientmanfo.models.user.*
import com.florientmanfo.com.florientmanfo.utils.IDGenerator
import com.florientmanfo.com.florientmanfo.utils.IDSuffix
import com.florientmanfo.com.florientmanfo.utils.Password
import com.florientmanfo.com.florientmanfo.utils.suspendTransaction
import org.jetbrains.exposed.v1.core.or
import io.ktor.server.config.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.exposed.v1.jdbc.selectAll
import java.time.LocalDateTime
import java.util.*

class UserRepositoryImpl(
    private val config: ApplicationConfig,
    private val firebase: FirebaseRepository
) : UserRepository {

    override suspend fun register(dto: RegisterDTO): Result<String> = suspendTransaction {
        try {
            val user = UsersEntity.new(IDGenerator.generate(IDSuffix.USER)) {
                email = dto.email
                password = Password.hash(dto.password)
                name = dto.username
                activated = false
                role = UserRole.USER.name
                createdAt = LocalDateTime.now()
                updatedAt = LocalDateTime.now()
            }
            Result.success(generateToken(user.id.value).accessToken)
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
                        activated = entity.activated,
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

    override suspend fun getProfile(userIdOrEmail: String): Result<UserModel> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { (Users.id eq userIdOrEmail) or  (Users.email eq userIdOrEmail) }.firstOrNull()
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

    override suspend fun activateAccount(userId: String): Result<Unit> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.id eq userId }.firstOrNull()
                if (entity != null) {
                    entity.activated = true
                    entity.updatedAt = LocalDateTime.now()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun generateTokenFromEmail(email: String): Result<String?> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.email eq email }.firstOrNull()
                val token = entity?.let { generateToken(it.id.value).accessToken }
                Result.success(token)
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

    override suspend fun resetPassword(userId: String, newPassword: String): Result<Unit> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.id eq userId }.firstOrNull()
                if (entity != null) {
                    entity.password = Password.hash(newPassword)
                    entity.updatedAt = LocalDateTime.now()
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun updateUser(
        userId: String,
        dto: UserDTO,
        imageFile: ByteArray?
    ): Result<UserModel> {
        return suspendTransaction {
            try {
                val entity = UsersEntity.find { Users.id eq userId }.firstOrNull()
                if (entity != null) {
                    imageFile?.let { newPhoto ->
                        entity.imageUrl?.let {
                            firebase.deleteFile(
                                entity.id.value,
                                BucketPath.USERS
                            )
                        }
                        entity.imageUrl = firebase.uploadFile(
                            newPhoto,
                            entity.id.value,
                            BucketPath.USERS
                        )
                    }

                    dto.password?.let {
                        entity.password = Password.hash(it)
                    }
                    dto.name?.let {
                        entity.name = it
                    }
                    dto.activated?.let {
                        entity.activated = it
                    }
                    dto.role?.let {
                        entity.role = it.name
                    }
                    entity.updatedAt = LocalDateTime.now()
                    if (dto.deleteImage) {
                        entity.imageUrl = null
                        firebase.deleteFile(entity.id.value, BucketPath.USERS)
                    }
                    CoroutineScope(Dispatchers.IO).launch {
                        updateUserRecipe(userId)
                    }
                    Result.success(entity.toModel())
                } else {
                    Result.failure(Exception("User not found"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private suspend fun updateUserRecipe(userId: String): Result<Unit> {
        return suspendTransaction {
            try {
                RecipesEntity.find { Recipes.authorId eq userId }.forEach { entity ->
                    entity.updatedAt = LocalDateTime.now()
                }
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun deleteAccount(userId: String): Result<Unit> {
        return try {
            UsersEntity.find { Users.id eq userId }.firstOrNull()?.let {
                it.imageUrl?.let { imageUrl ->
                    firebase.deleteFile(it.id.value, BucketPath.USERS)
                    it.imageUrl = null
                }
                it.delete()
                Result.success(Unit)
            } ?: Result.failure(Exception("User not found"))
        } catch (e: Exception) {
            Result.failure(e)
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