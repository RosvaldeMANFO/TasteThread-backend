package com.florientmanfo.com.florientmanfo.services.user

import com.florientmanfo.com.florientmanfo.models.user.Login
import com.florientmanfo.com.florientmanfo.models.user.LoginDTO
import com.florientmanfo.com.florientmanfo.models.user.RegisterDTO
import com.florientmanfo.com.florientmanfo.models.user.Token
import com.florientmanfo.com.florientmanfo.models.user.UserModel
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import io.ktor.server.config.ApplicationConfig

class UserService(
    private val repository: UserRepository,
    private val validation: UserValidationService,
    private val config: ApplicationConfig
) {
    suspend fun login(dto: LoginDTO): Result<Login> {
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
            onSuccess = { token ->
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

    suspend fun activateAccount(userId: String): Result<String> {
        return repository.activateAccount(userId).fold(
            onSuccess = { Result.success("Account activated successfully") },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun requestPasswordReset(email: String): Result<String> {
        return repository.generateTokenFromEmail(email).fold(
            onFailure = {  Result.failure(it) },
            onSuccess = {
                if (it == null) {
                    Result.failure(Exception("No user found with this email"))
                } else {
                    val resetLink = "${config.property("ktor.link.baseUrl").getString()}/reset-password?token=$it"
                    println("Password reset link: $resetLink")
                    // TODO: Send password reset email with the link
                    Result.success("Check your email for password reset")
                }
            }
        )
    }

    suspend fun requestAccountActivation(email: String): Result<String> {
        return repository.generateTokenFromEmail(email).fold(
            onFailure = {  Result.failure(it) },
            onSuccess = {
                if (it == null) {
                    Result.failure(Exception("No user found with this email"))
                } else {
                    val activationLink = "${config.property("ktor.link.baseUrl").getString()}/activate?token=$it"
                    println("Account activation link: $activationLink")
                    // TODO: Send account activation email with the link
                    Result.success("Check your email for account activation")
                }
            }
        )
    }

    suspend fun resetPassword(userId: String, newPassword: String): Result<String> {
        val result = validation.validatePassword(newPassword)
        if (result.isValid.not()) {
            throw Exception(result.message)
        }
        return repository.resetPassword(userId, newPassword).fold(
            onSuccess = { Result.success("Password reset successfully") },
            onFailure = { Result.failure(it) }
        )
    }

}