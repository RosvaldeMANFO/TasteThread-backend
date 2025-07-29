package com.florientmanfo.com.florientmanfo.api.module

import com.florientmanfo.com.florientmanfo.data.table.Ingredients
import com.florientmanfo.com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

fun Application.configureDatabase() {
    val config = this.environment.config
    val connectionName = config.property("ktor.database.connectionName").getString()
    val completeUrl = if (!connectionName.isBlank()) {
        println("Using Cloud SQL connection name $connectionName")
        config.property("ktor.database.url").getString().replace("CLOUD_SQL_CONNECTION_NAME", connectionName)
    } else {
        config.property("ktor.database.url").getString()
    }
    val dbConfig = HikariConfig().apply {
        driverClassName = config.property("ktor.database.driver").getString()
        jdbcUrl = completeUrl
        username = config.property("ktor.database.user").getString()
        password = config.property("ktor.database.password").getString()
        maximumPoolSize = config.property("ktor.database.maximumPoolSize").getString().toInt()
    }
    val dataSource = HikariDataSource(dbConfig)
    Database.connect(dataSource)

    if (config.property("ktor.environment").getString() == "dev") {
        generateMigrationFile()
    } else {
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()

        flyway.migrate()
    }
}

fun generateMigrationFile() {
    val statements = transaction {
        SchemaUtils.createStatements(
            Users,
            Recipes,
            Ingredients,
            RecipeLikes,
            RecipeComments
        )
    }

    if (statements.isNotEmpty()) {
        val migrationDir = File("src/main/resources/db/migration")
        migrationDir.mkdirs()

        val versionedName = "V${System.currentTimeMillis()}__auto_generated.sql"
        val migrationFile = File(migrationDir, versionedName)

        migrationFile.printWriter().use { writer ->
            statements.forEach { statement ->
                writer.println("$statement;")
            }
        }
        println("✅ Migration file generated: ${migrationFile.name}")
    }
}

