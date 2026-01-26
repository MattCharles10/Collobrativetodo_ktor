package com.todoapp.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        // Connect to database
        Database.connect(DatabaseConfig.dataSource)

        // Run any initialization queries
        transaction {
            // Enable UUID extension if available
            try {
                exec("CREATE EXTENSION IF NOT EXISTS \"uuid-ossp\"")
                println("✅ UUID extension enabled")
            } catch (e: Exception) {
                println("ℹ️  Could not create uuid-ossp extension: ${e.message}")
            }

            // Set timezone
            exec("SET TIME ZONE 'UTC'")
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}