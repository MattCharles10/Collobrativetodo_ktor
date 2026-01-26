package com.todoapp.utils

import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.UUID
import java.util.regex.Pattern

object Validators {

    // Email validation regex
    private val EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    )

    // Username validation regex (3-20 chars, letters, numbers, dots, dashes, underscores)
    private val USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._-]{3,20}$"
    )

    // Password validation (at least 8 chars, one uppercase, one lowercase, one digit)
    private val PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{8,}$"
    )

    fun isValidEmail(email: String): Boolean {
        return EMAIL_PATTERN.matcher(email).matches()
    }

    fun isValidUsername(username: String): Boolean {
        return USERNAME_PATTERN.matcher(username).matches()
    }

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_PATTERN.matcher(password).matches()
    }

    fun isStrongPassword(password: String): Boolean {
        // Additional strength checks
        if (password.length < 12) return false

        val hasSpecialChar = password.any { it in "!@#$%^&*()_+-=[]{}|;:,.<>?" }
        val hasNumber = password.any { it.isDigit() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasUppercase = password.any { it.isUpperCase() }

        return hasSpecialChar && hasNumber && hasLowercase && hasUppercase
    }

    fun isValidUUID(uuid: String): Boolean {
        return try {
            UUID.fromString(uuid)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun isValidDate(dateString: String): Boolean {
        return try {
            Instant.parse(dateString)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun sanitizeInput(input: String?): String? {
        return input?.trim()?.takeIf { it.isNotBlank() }
    }

    fun validateTaskTitle(title: String): ValidationResult {
        return when {
            title.isBlank() -> ValidationResult.error("Title is required")
            title.length > 255 -> ValidationResult.error("Title cannot exceed 255 characters")
            else -> ValidationResult.success()
        }
    }

    fun validateTaskDescription(description: String?): ValidationResult {
        return if (description != null && description.length > 2000) {
            ValidationResult.error("Description cannot exceed 2000 characters")
        } else {
            ValidationResult.success()
        }
    }

    fun validatePriority(priority: Int): ValidationResult {
        return if (priority in 0..2) {
            ValidationResult.success()
        } else {
            ValidationResult.error("Priority must be between 0 (low) and 2 (high)")
        }
    }

    fun validateCategory(category: String?): ValidationResult {
        return if (category != null && category.length > 100) {
            ValidationResult.error("Category cannot exceed 100 characters")
        } else {
            ValidationResult.success()
        }
    }

    fun validatePermission(permission: String): ValidationResult {
        return if (permission in listOf("view", "edit")) {
            ValidationResult.success()
        } else {
            ValidationResult.error("Permission must be either 'view' or 'edit'")
        }
    }

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    ) {
        companion object {
            fun success(): ValidationResult = ValidationResult(true)
            fun error(message: String): ValidationResult = ValidationResult(false, message)
        }
    }

    // Extension functions for common validations
    fun String?.ifNotBlankThen(action: (String) -> Unit) {
        if (!this.isNullOrBlank()) {
            action(this)
        }
    }

    fun <T> validateAll(vararg validations: ValidationResult): ValidationResult {
        return validations.find { !it.isValid } ?: ValidationResult.success()
    }
}

// Additional utility functions
object StringUtils {

    fun generateRandomString(length: Int): String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charset.random() }
            .joinToString("")
    }

    fun generateVerificationCode(): String {
        return (100000..999999).random().toString()
    }

    fun maskEmail(email: String): String {
        val parts = email.split("@")
        if (parts.size != 2) return email

        val username = parts[0]
        val domain = parts[1]

        return if (username.length <= 2) {
            "***@$domain"
        } else {
            "${username.first()}***${username.last()}@$domain"
        }
    }

    fun maskPassword(password: String): String {
        return "*".repeat(password.length.coerceAtMost(12))
    }

    fun formatDate(date: Instant, format: String = "yyyy-MM-dd HH:mm:ss"): String {
        return java.time.format.DateTimeFormatter.ofPattern(format)
            .withZone(java.time.ZoneOffset.UTC)
            .format(date)
    }

    fun parseDate(dateString: String): Instant? {
        return try {
            Instant.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }
}

object PaginationUtils {

    fun calculateOffset(page: Int, pageSize: Int): Long {
        return ((page - 1) * pageSize).toLong()
    }

    fun calculateTotalPages(totalItems: Int, pageSize: Int): Int {
        return (totalItems + pageSize - 1) / pageSize
    }

    fun validatePageParams(page: Int, pageSize: Int): Validators.ValidationResult {
        return when {
            page < 1 -> Validators.ValidationResult.error("Page must be greater than 0")
            pageSize < 1 -> Validators.ValidationResult.error("Page size must be greater than 0")
            pageSize > 100 -> Validators.ValidationResult.error("Page size cannot exceed 100")
            else -> Validators.ValidationResult.success()
        }
    }
}