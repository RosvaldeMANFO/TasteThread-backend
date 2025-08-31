package com.florientmanfo.com.florientmanfo.services.user

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.models.user.Token
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import io.ktor.server.config.ApplicationConfig
import java.util.Date

class UserService(
    private val repository: UserRepository,
    private val validation: UserValidationService,
    private val config: ApplicationConfig
) {
    suspend fun login(dto: LoginDTO): Result<Token> {
        val result = validation.validateCredential(dto.email, dto.password)
        if (result.isValid.not()) {
            throw Exception(result.message)
        }
        return repository.login(dto)
    }

    suspend fun register(dto: RegisterDTO): Result<String> {
        val result = validation.validateCredential(dto.email, dto.password)
        if (result.isValid.not()) {
            throw Exception(result.message)
        }
        return repository.register(dto).fold(
            onSuccess = {
                val token = generateActivationToken(it.id)
                val activationLink = "${config.property("ktor.link.baseUrl").getString()}/activate?token=$token"
                println("Activation link: $activationLink")
                // TODO: Send activation email with the link
                Result.success("Check your email for account activation")
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun refreshToken(refreshToken: String): Result<Token> {
        return repository.refreshToken(refreshToken)
    }

    suspend fun getProfile(userId: String): Result<UserModel> {
        return repository.getProfile(userId)
    }

    suspend fun activateAccount(token: String): Result<String> {
        val userId = verifyActivationToken(token)
            ?: return Result.failure(Exception("Invalid or expired activation link"))
        return repository.activateAccount(userId)
    }

    private fun generateActivationToken(userId: String): String {
        val secret = config.property("ktor.link.secret").getString()
        val expiry = Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)
        return JWT.create()
            .withSubject(userId)
            .withExpiresAt(expiry)
            .sign(Algorithm.HMAC256(secret))
    }

    private fun verifyActivationToken(token: String): String? {
        val secret = config.property("ktor.link.secret").getString()
        return try {
            val verifier = JWT.require(Algorithm.HMAC256(secret)).build()
            val decoded = verifier.verify(token)
            decoded.subject
        } catch (_: Exception) {
            null
        }
    }
}