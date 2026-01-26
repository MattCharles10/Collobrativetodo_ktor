package com.todoapp.services

import at.favre.lib.crypto.bcrypt.BCrypt
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.todoapp.dtos.UserDTO
import com.todoapp.dtos.UserWithTokenDTO
import com.todoapp.models.User
import com.todoapp.models.UserCreate
import com.todoapp.models.UserLogin
import com.todoapp.repositories.UserRepository
import io.github.cdimascio.dotenv.dotenv
import java.util.*
import java.time.Instant

class AuthService(
    private val userRepository: UserRepository = UserRepository()
) {
    private val env = dotenv()
    private val secret = env["JWT_SECRET"] ?: "your-super-secret-jwt-key-change-in-production"
    private val issuer = env["JWT_ISSUER"] ?: "todo-app"
    private val audience = env["JWT_AUDIENCE"] ?: "todo-app-users"
    private val algorithm = Algorithm.HMAC256(secret)

    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\$")
    private val usernameRegex = Regex("^[a-zA-Z0-9._-]{3,20}\$")

    suspend fun register(request: UserCreate): Result<UserWithTokenDTO> {
        return try {
            // Validate input
            if (request.email.isBlank() || request.username.isBlank() || request.password.isBlank()) {
                return Result.failure(IllegalArgumentException("All fields are required"))
            }

            if (!emailRegex.matches(request.email)) {
                return Result.failure(IllegalArgumentException("Invalid email format"))
            }

            if (!usernameRegex.matches(request.username)) {
                return Result.failure(IllegalArgumentException(
                    "Username must be 3-20 characters and contain only letters, numbers, dots, dashes, and underscores"
                ))
            }

            if (request.password.length < 8) {
                return Result.failure(IllegalArgumentException("Password must be at least 8 characters"))
            }

            // Check if user exists
            if (userRepository.existsByEmail(request.email)) {
                return Result.failure(IllegalArgumentException("User with this email already exists"))
            }

            if (userRepository.existsByUsername(request.username)) {
                return Result.failure(IllegalArgumentException("Username already taken"))
            }

            // Hash password
            val passwordHash = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())

            // Create user
            val now = Instant.now()
            val user = User(
                id = UUID.randomUUID().toString(),
                email = request.email.trim(),
                username = request.username.trim(),
                passwordHash = passwordHash,
                createdAt = now.toString(),
                updatedAt = now.toString()
            )

            val savedUser = userRepository.create(user)

            // Generate token
            val token = generateToken(savedUser)
            val userDTO = savedUser.toDTO()

            Result.success(UserWithTokenDTO(userDTO, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(request: UserLogin): Result<UserWithTokenDTO> {
        return try {
            if (request.email.isBlank() || request.password.isBlank()) {
                return Result.failure(IllegalArgumentException("Email and password are required"))
            }

            val user = userRepository.findByEmail(request.email.trim())
                ?: return Result.failure(IllegalArgumentException("Invalid credentials"))

            // Verify password
            val result = BCrypt.verifyer().verify(
                request.password.toCharArray(),
                user.passwordHash
            )

            if (!result.verified) {
                return Result.failure(IllegalArgumentException("Invalid credentials"))
            }

            // Generate token
            val token = generateToken(user)
            val userDTO = user.toDTO()

            Result.success(UserWithTokenDTO(userDTO, token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun verifyToken(token: String): DecodedJWT? {
        return try {
            val verifier = JWT.require(algorithm)
                .withIssuer(issuer)
                .withAudience(audience)
                .build()

            verifier.verify(token)
        } catch (e: Exception) {
            null
        }
    }

    fun getUserIdFromToken(token: String): String? {
        return verifyToken(token)?.getClaim("userId")?.asString()
    }

    private fun generateToken(user: User): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("userId", user.id)
            .withClaim("email", user.email)
            .withClaim("username", user.username)
            .withExpiresAt(Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000)) // 7 days
            .withIssuedAt(Date())
            .sign(algorithm)
    }

    private fun User.toDTO(): UserDTO = UserDTO(
        id = this.id,
        email = this.email,
        username = this.username,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}