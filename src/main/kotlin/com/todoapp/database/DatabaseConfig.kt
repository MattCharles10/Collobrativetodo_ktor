package com.todoapp.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import java.util.concurrent.TimeUnit

object DatabaseConfig {
    // HARDCODE EVERYTHING for testing
    private val jdbcUrl = "jdbc:postgresql://localhost:5432/todoapp"
    private val username = "postgres"
    private val password = ""  // ← HARDCODED PASSWORD
    private val maxPoolSize = 10

    init {
        println("🔍 Database Config (HARDCODED):")
        println("   URL: $jdbcUrl")
        println("   Username: $username")
        println("   Password: '${if (password.isNotEmpty()) "*** (${password.length} chars)" else "empty"}'")
    }

    // HikariCP DataSource - USE CONSTRUCTOR WITH CONFIG
    val dataSource: HikariDataSource by lazy {
        println("🔄 Creating HikariCP DataSource...")

        val config = HikariConfig().apply {
            this.jdbcUrl = this@DatabaseConfig.jdbcUrl
            this.username = this@DatabaseConfig.username
            this.password = this@DatabaseConfig.password  // ← THIS IS CRITICAL
            maximumPoolSize = maxPoolSize
            minimumIdle = 2
            connectionTimeout = TimeUnit.SECONDS.toMillis(30)
            validationTimeout = TimeUnit.SECONDS.toMillis(5)
            idleTimeout = TimeUnit.MINUTES.toMillis(10)
            maxLifetime = TimeUnit.MINUTES.toMillis(30)
            connectionTestQuery = "SELECT 1"
            poolName = "TodoAppPool"

            // PostgreSQL specific properties
            addDataSourceProperty("ssl", "false")
            addDataSourceProperty("sslmode", "disable")
        }

        println("📊 HikariCP Config Properties:")
        println("   jdbcUrl: ${config.jdbcUrl}")
        println("   username: ${config.username}")
        println("   password set: ${config.password != null}")

        HikariDataSource(config)
    }

    // Exposed Database instance
    val database: Database by lazy {
        println("🔗 Connecting Exposed Database...")
        Database.connect(dataSource)
    }
}