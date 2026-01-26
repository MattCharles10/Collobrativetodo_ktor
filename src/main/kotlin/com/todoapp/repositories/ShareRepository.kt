package com.todoapp.repositories

import com.todoapp.database.DatabaseFactory.dbQuery
import com.todoapp.models.Share
import com.todoapp.models.Shares
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class ShareRepository {

    suspend fun create(share: Share): Share = dbQuery {
        val id = UUID.fromString(share.id)
        val createdAt = Instant.parse(share.createdAt)

        Shares.insert {
            it[Shares.id] = id
            it[Shares.taskId] = UUID.fromString(share.taskId)
            it[Shares.sharedWith] = UUID.fromString(share.sharedWith)
            it[Shares.permission] = share.permission
            it[Shares.sharedBy] = UUID.fromString(share.sharedBy)
            it[Shares.createdAt] = createdAt
        }

        share
    }

    suspend fun findById(id: String): Share? = dbQuery {
        Shares.select { Shares.id eq UUID.fromString(id) }
            .map { rowToShare(it) }
            .singleOrNull()
    }

    suspend fun findByTask(taskId: String): List<Share> = dbQuery {
        Shares.select { Shares.taskId eq UUID.fromString(taskId) }
            .orderBy(Shares.createdAt to SortOrder.DESC)
            .map { rowToShare(it) }
    }

    suspend fun findBySharedWith(userId: String, page: Int = 1, pageSize: Int = 20): Pair<List<Share>, Int> = dbQuery {
        val query = Shares.select { Shares.sharedWith eq UUID.fromString(userId) }
        val total = query.count().toInt()

        val shares = query
            .orderBy(Shares.createdAt to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { rowToShare(it) }

        Pair(shares, total)
    }

    suspend fun findBySharedBy(userId: String): List<Share> = dbQuery {
        Shares.select { Shares.sharedBy eq UUID.fromString(userId) }
            .orderBy(Shares.createdAt to SortOrder.DESC)
            .map { rowToShare(it) }
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        Shares.deleteWhere { Shares.id eq UUID.fromString(id) } > 0
    }

    suspend fun exists(taskId: String, sharedWith: String): Boolean = dbQuery {
        Shares.select {
            (Shares.taskId eq UUID.fromString(taskId)) and
                    (Shares.sharedWith eq UUID.fromString(sharedWith))
        }.count() > 0
    }

    suspend fun hasPermission(userId: String, taskId: String, permission: String = "view"): Boolean = dbQuery {
        // Check if user is owner
        if (TaskRepository().isOwner(taskId, userId)) {
            return@dbQuery true
        }

        // Check if user has share with required permission
        when (permission) {
            "view" -> {
                Shares.select {
                    (Shares.taskId eq UUID.fromString(taskId)) and
                            (Shares.sharedWith eq UUID.fromString(userId)) and
                            (Shares.permission inList listOf("view", "edit"))
                }.count() > 0
            }
            "edit" -> {
                Shares.select {
                    (Shares.taskId eq UUID.fromString(taskId)) and
                            (Shares.sharedWith eq UUID.fromString(userId)) and
                            (Shares.permission eq "edit")
                }.count() > 0
            }
            else -> false
        }
    }

    suspend fun getUsersWithAccess(taskId: String): List<String> = dbQuery {
        Shares.select { Shares.taskId eq UUID.fromString(taskId) }
            .map { it[Shares.sharedWith].toString() }
            .toList()
    }

    private fun rowToShare(row: ResultRow): Share = Share(
        id = row[Shares.id].toString(),
        taskId = row[Shares.taskId].toString(),
        sharedWith = row[Shares.sharedWith].toString(),
        permission = row[Shares.permission],
        sharedBy = row[Shares.sharedBy].toString(),
        createdAt = row[Shares.createdAt].toString()
    )
}