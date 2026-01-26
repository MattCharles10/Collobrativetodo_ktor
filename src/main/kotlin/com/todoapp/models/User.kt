package com.todoapp.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object Users : Table("users") {
    val id = uuid("id")
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 100).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id, name = "pk_users")
}

@Serializable
data class User(
    val id: String,
    val email: String,
    val username: String,
    val passwordHash: String,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class UserCreate(
    val email: String,
    val username: String,
    val password: String
)

@Serializable
data class UserLogin(
    val email: String,
    val password: String
)