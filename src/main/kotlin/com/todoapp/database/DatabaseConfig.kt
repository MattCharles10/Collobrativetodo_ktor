package com.todoapp.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.github.cdimascio.dotenv.dotenv
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.TimeUnit

object DatabaseConfig {
    private val env = dotenv {
        ignoreIfMissing = true
    }

    // Database configuration
    private val jdbcUrl = env["DB_URL"] ?: "jdbc:postgresql://localhost:5432/todoapp"
    private val username = env["DB_USERNAME"] ?: "postgres"
    private val password = env["DB_PASSWORD"] ?: "password"
    private val maxPoolSize = env["DB_MAX_POOL_SIZE"]?.toIntOrNull() ?: 10

    // HikariCP DataSource
    val dataSource: HikariDataSource by lazy {
        val hikariConfig = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            maximumPoolSize = maxPoolSize
            minimumIdle = 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_READ_COMMITTED"
            connectionTestQuery = "SELECT 1"
            connectionTimeout = TimeUnit.SECONDS.toMillis(30)
            validationTimeout = TimeUnit.SECONDS.toMillis(5)
            idleTimeout = TimeUnit.MINUTES.toMillis(10)
            maxLifetime = TimeUnit.MINUTES.toMillis(30)
            poolName = "TodoAppPool"
            validate()
        }

        HikariDataSource(hikariConfig)
    }

    // Exposed Database instance
    val database: Database by lazy {
        Database.connect(dataSource)
    }
}