package com.florientmanfo.api.module

import com.florientmanfo.data.table.*
import com.florientmanfo.models.user.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import java.io.File

fun Application.configureDatabase() {
    val config = this.environment.config
    val env = config.property("ktor.environment").getString()

    val dbConfig = HikariConfig().apply {
        username = config.property("ktor.database.user").getString()
        password = config.property("ktor.database.password").getString()
        maximumPoolSize = config.property("ktor.database.maximumPoolSize").getString().toInt()

        jdbcUrl = if (env == "prod") {
            val supabaseUrl = config.property("ktor.database.supabaseUrl").getString()
            String.format(supabaseUrl, password)
        } else {
            config.property("ktor.database.localUrl").getString()
        }
    }

    try {
        val dataSource = HikariDataSource(dbConfig)
        Database.connect(dataSource)
        if (env == "dev") {
            generateMigration()
        }
        runMigrations(dataSource)

        val userRepository: UserRepository by inject()
        CoroutineScope(Dispatchers.IO).launch {
            userRepository.createAdminIfNotExists()
        }
    } catch (e: HikariPool.PoolInitializationException) {
        println("Pool initialization exception: ${e.message}")
    } catch (e: Exception) {
        println("Database configuration error: ${e.message}")
        throw e
    }
}

private fun runMigrations(dataSource: HikariDataSource) {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .validateOnMigrate(true)
        .validateMigrationNaming(true)
        .cleanDisabled(true)
        .load()

    try {
        flyway.repair()
        val result = flyway.migrate()
        if (result.migrationsExecuted > 0) {
            println("✅ Database migrated successfully. Applied ${result.migrationsExecuted} migrations")
        } else {
            println("✅ Database is up-to-date. No migrations to apply.")
        }
    } catch (e: Exception) {
        println("❌ Migration failed: ${e.message}")
        throw e
    }
}

fun generateMigration(): String? {
    val tables = arrayOf(
        Users,
        Recipes,
        Ingredients,
        RecipeLikes,
        RecipeComments
    )

    val statements = transaction {
        SchemaUtils.statementsRequiredToActualizeScheme(*tables)
    }

    if (statements.isNotEmpty()) {
        val migrationDir = File("src/main/resources/db/migration")
        migrationDir.mkdirs()

        val existingFiles = migrationDir.listFiles()?.mapNotNull { file ->
            val name = file.name
            if (name.startsWith("V") && name.contains("__")) {
                name.substring(1, name.indexOf("__")).toIntOrNull()
            } else null
        }?.maxOrNull() ?: 0

        val version = (existingFiles + 1).toString()
        val file = File(migrationDir, "V${version}__auto_generated.sql")

        file.printWriter().use { writer ->
            statements.forEach { statement ->
                writer.println("$statement;")
            }
        }
        println("✅ Migration file generated: ${file.name} with ${statements.size} statements")
        return version
    } else {
        println("ℹ️ No database changes detected, skipping migration generation")
        return null
    }
}