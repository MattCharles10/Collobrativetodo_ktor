package com.todoapp.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.*

object Tasks : Table("tasks") {
    val id = uuid("id")
    val title = varchar("title", 255)
    val description = text("description").nullable()
    val isCompleted = bool("is_completed").default(false)
    val createdBy = uuid("created_by").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val dueDate = timestamp("due_date").nullable()
    val priority = integer("priority").default(0).check { it.between(0, 2) } // 0: low, 1: medium, 2: high
    val category = varchar("category", 100).nullable()
    val createdAt = timestamp("created_at")
    val updatedAt = timestamp("updated_at")

    override val primaryKey = PrimaryKey(id, name = "pk_tasks")

    init {
        index(isUnique = false, columns = arrayOf(createdBy))
        index(isUnique = false, columns = arrayOf(dueDate))
        index(isUnique = false, columns = arrayOf(isCompleted))
    }
}

@Serializable
data class Task(
    val id: String,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val createdBy: String,
    val dueDate: String? = null,
    val priority: Int = 0,
    val category: String? = null,
    val createdAt: String,
    val updatedAt: String
)

@Serializable
data class TaskCreate(
    val title: String,
    val description: String? = null,
    val dueDate: String? = null,
    val priority: Int = 0,
    val category: String? = null
)

@Serializable
data class TaskUpdate(
    val title: String? = null,
    val description: String? = null,
    val isCompleted: Boolean? = null,
    val dueDate: String? = null,
    val priority: Int? = null,
    val category: String? = null
)