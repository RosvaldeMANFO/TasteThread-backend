package com.florientmanfo.com.florientmanfo.api.module

import com.florientmanfo.com.florientmanfo.data.table.*
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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

    val generatedDir = File("build/generated-migrations").apply { mkdirs() }
    val locations = mutableListOf("classpath:db/migration", "filesystem:${generatedDir.absolutePath}")

    generateMigrationFile(generatedDir)?.let { version ->
        val flyway = Flyway.configure()
            .dataSource(dataSource)
            .locations(*locations.toTypedArray())
            .load()

        flyway.repair()
        val result = flyway.migrate()
        if (result.migrationsExecuted > 0) {
            println("✅ Database migrated to version $version")
        } else {
            println("✅ No new migrations to apply. Database is up-to-date.")
        }
    }

    val userRepository: UserRepository by inject()
    CoroutineScope(Dispatchers.IO).launch {
        userRepository.createAdminIfNotExists()
    }
}

fun generateMigrationFile(outputDir: File): String? {
    val tables = arrayOf(
        Users,
        Recipes,
        Ingredients,
        RecipeLikes,
        RecipeComments
    )
    val statements = transaction {
        SchemaUtils.createStatements(*tables).let {
            it.ifEmpty {
                SchemaUtils.statementsRequiredToActualizeScheme(*tables)
            }
        }
    }

    if (statements.isNotEmpty()) {
        val migrationDir = File("src/main/resources/db/migration")
        migrationDir.mkdirs()

        val version = "${System.currentTimeMillis()}"
        val file = File(outputDir, "V${version}__auto.sql")

        file.printWriter().use { writer ->
            statements.forEach { statement ->
                writer.println("$statement;")
            }
        }
        println("✅ Migration file generated: ${file.name}")
        return version
    }
    return null
}
