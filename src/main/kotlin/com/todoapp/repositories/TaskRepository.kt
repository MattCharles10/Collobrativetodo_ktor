package com.todoapp.repositories

import com.todoapp.database.DatabaseFactory.dbQuery
import com.todoapp.models.Task
import com.todoapp.models.Tasks
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class TaskRepository {

    suspend fun create(task: Task): Task = dbQuery {
        val id = UUID.fromString(task.id)
        val createdAt = Instant.parse(task.createdAt)
        val updatedAt = Instant.parse(task.updatedAt)
        val dueDate = task.dueDate?.let { Instant.parse(it) }

        Tasks.insert {
            it[Tasks.id] = id
            it[Tasks.title] = task.title
            it[Tasks.description] = task.description
            it[Tasks.createdBy] = UUID.fromString(task.createdBy)
            it[Tasks.dueDate] = dueDate
            it[Tasks.priority] = task.priority
            it[Tasks.category] = task.category
            it[Tasks.createdAt] = createdAt
            it[Tasks.updatedAt] = updatedAt
        }

        task
    }

    suspend fun findById(id: String): Task? = dbQuery {
        Tasks.select { Tasks.id eq UUID.fromString(id) }
            .map { rowToTask(it) }
            .singleOrNull()
    }

    suspend fun findByUser(userId: String, page: Int = 1, pageSize: Int = 20): Pair<List<Task>, Int> = dbQuery {
        val query = Tasks.select { Tasks.createdBy eq UUID.fromString(userId) }
        val total = query.count().toInt()

        val tasks = query
            .orderBy(Tasks.createdAt to SortOrder.DESC)
            .limit(pageSize, offset = ((page - 1) * pageSize).toLong())
            .map { rowToTask(it) }

        Pair(tasks, total)
    }

    suspend fun update(id: String, updates: Map<String, Any?>): Task? = dbQuery {
        val task = findById(id) ?: return@dbQuery null

        Tasks.update({ Tasks.id eq UUID.fromString(id) }) {
            updates["title"]?.let { value -> it[Tasks.title] = value as String }
            updates["description"]?.let { value -> it[Tasks.description] = value as? String }
            updates["isCompleted"]?.let { value -> it[Tasks.isCompleted] = value as Boolean }
            updates["dueDate"]?.let { value ->
                it[Tasks.dueDate] = if (value == null) null else Instant.parse(value as String)
            }
            updates["priority"]?.let { value -> it[Tasks.priority] = value as Int }
            updates["category"]?.let { value -> it[Tasks.category] = value as? String }
            it[Tasks.updatedAt] = Instant.now()
        }

        findById(id)
    }

    suspend fun delete(id: String): Boolean = dbQuery {
        Tasks.deleteWhere { Tasks.id eq UUID.fromString(id) } > 0
    }

    suspend fun exists(id: String): Boolean = dbQuery {
        Tasks.select { Tasks.id eq UUID.fromString(id) }.count() > 0
    }

    suspend fun isOwner(taskId: String, userId: String): Boolean = dbQuery {
        Tasks.select {
            (Tasks.id eq UUID.fromString(taskId)) and
                    (Tasks.createdBy eq UUID.fromString(userId))
        }.count() > 0
    }

    private fun rowToTask(row: ResultRow): Task = Task(
        id = row[Tasks.id].toString(),
        title = row[Tasks.title],
        description = row[Tasks.description],
        isCompleted = row[Tasks.isCompleted],
        createdBy = row[Tasks.createdBy].toString(),
        dueDate = row[Tasks.dueDate]?.toString(),
        priority = row[Tasks.priority],
        category = row[Tasks.category],
        createdAt = row[Tasks.createdAt].toString(),
        updatedAt = row[Tasks.updatedAt].toString()
    )
}