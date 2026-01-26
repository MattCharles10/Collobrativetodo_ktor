package com.todoapp.repositories

import com.todoapp.database.DatabaseFactory.dbQuery
import com.todoapp.models.User
import com.todoapp.models.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class UserRepository {

    suspend fun create(user: com.todoapp.models.User): User = dbQuery {
        val id = UUID.fromString(user.id)
        val createdAt = Instant.parse(user.createdAt)
        val updatedAt = Instant.parse(user.updatedAt)

        Users.insert {
            it[Users.id] = id
            it[Users.email] = user.email
            it[Users.username] = user.username
            it[Users.passwordHash] = user.passwordHash
            it[Users.createdAt] = createdAt
            it[Users.updatedAt] = updatedAt
        }

        user
    }

    suspend fun findByEmail(email: String): User? = dbQuery {
        Users.select { Users.email eq email }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findById(id: String): User? = dbQuery {
        Users.select { Users.id eq UUID.fromString(id) }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun findByUsername(username: String): User? = dbQuery {
        Users.select { Users.username eq username }
            .map { rowToUser(it) }
            .singleOrNull()
    }

    suspend fun searchByEmailOrUsername(query: String, excludeUserId: String): List<User> = dbQuery {
        Users.select {
            (Users.email like "%$query%") or (Users.username like "%$query%") and
                    (Users.id neq UUID.fromString(excludeUserId))
        }.limit(20)
            .map { rowToUser(it) }
    }

    suspend fun existsByEmail(email: String): Boolean = dbQuery {
        Users.select { Users.email eq email }.count() > 0
    }

    suspend fun existsByUsername(username: String): Boolean = dbQuery {
        Users.select { Users.username eq username }.count() > 0
    }

    private fun rowToUser(row: ResultRow): User = User(
        id = row[Users.id].toString(),
        email = row[Users.email],
        username = row[Users.username],
        passwordHash = row[Users.passwordHash],
        createdAt = row[Users.createdAt].toString(),
        updatedAt = row[Users.updatedAt].toString()
    )
}