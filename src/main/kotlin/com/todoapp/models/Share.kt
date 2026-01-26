package com.todoapp.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object Shares : Table("shares") {
    val id = uuid("id")
    val taskId = uuid("task_id").references(Tasks.id, onDelete = ReferenceOption.CASCADE)
    val sharedWith = uuid("shared_with").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val permission = varchar("permission", 50).default("view").check { it.inList(listOf("view", "edit")) }
    val sharedBy = uuid("shared_by").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at")

    override val primaryKey = PrimaryKey(id, name = "pk_shares")

    init {
        uniqueIndex(taskId, sharedWith)
        index(isUnique = false, columns = arrayOf(taskId))
        index(isUnique = false, columns = arrayOf(sharedWith))
        index(isUnique = false, columns = arrayOf(sharedBy))
    }
}

@Serializable
data class Share(
    val id: String,
    val taskId: String,
    val sharedWith: String,
    val permission: String,
    val sharedBy: String,
    val createdAt: String
)

@Serializable
data class ShareCreate(
    val taskId: String,
    val sharedWithEmail: String,
    val permission: String = "view"
)