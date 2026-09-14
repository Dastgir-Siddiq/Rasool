package com.orbitmessenger.backend.database

import com.orbitmessenger.backend.models.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/orbitmessenger"
            username = System.getenv("DB_USER") ?: "orbit"
            password = System.getenv("DB_PASSWORD") ?: "secret_password"
            maximumPoolSize = 3
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"
            validate()
        }
        val dataSource = HikariDataSource(config)
        Database.connect(dataSource)

        transaction {
            SchemaUtils.create(Users)
        }
    }
}
