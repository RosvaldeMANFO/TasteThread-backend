package com.florientmanfo.com.florientmanfo.services.user

import com.florientmanfo.com.florientmanfo.utils.ValidationResult

class UserValidationService {
    fun validateCredential(email: String, password: String): ValidationResult {
        val emailRegex = Regex("^[A-Za-z](.*)(@)(.+)(\\.)(.+)")
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d).{8,}$")
        if (!emailRegex.matches(email)) {
            return ValidationResult(
                false,
                "Email is not valid"
            )
        } else if (!passwordRegex.matches(password)) {
            return ValidationResult(
                false,
                "Password must contain letters, numbers, and have a length of at least 8 characters"
            )
        }
        return ValidationResult(true)
    }
}