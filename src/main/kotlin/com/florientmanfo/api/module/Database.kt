package com.florientmanfo.com.florientmanfo.api.module

import com.florientmanfo.com.florientmanfo.data.table.*
import com.florientmanfo.com.florientmanfo.models.user.UserRepository
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.koin.ktor.ext.inject
import java.io.File
import kotlin.jvm.javaClass

fun Application.configureDatabase() {
    val config = this.environment.config
    val dbName = config.property("ktor.database.name").getString()
    val connectionName = config.property("ktor.database.connectionName").getString()
    val baseUrl = config.property("ktor.database.url").getString()
    val dbUrl = "$baseUrl${dbName}"

    val dbConfig = HikariConfig().apply {
        jdbcUrl = dbUrl
        username = config.property("ktor.database.user").getString()
        password = config.property("ktor.database.password").getString()
        maximumPoolSize = config.property("ktor.database.maximumPoolSize").getString().toInt()
    }

    if(!connectionName.isBlank()){
        dbConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
        dbConfig.addDataSourceProperty("cloudSqlInstance", connectionName);
    }

    val dataSource = try {
        HikariDataSource(dbConfig)
    } catch (e: HikariPool.PoolInitializationException) {
        createDatabase(config, connectionName)
        HikariDataSource(dbConfig)
    }

    Database.connect(dataSource)
    if (config.property("ktor.environment").getString() == "dev") {
        generateMigration()
    }
    runMigrations(dataSource)

    val userRepository: UserRepository by inject()
    CoroutineScope(Dispatchers.IO).launch {
        userRepository.createAdminIfNotExists()
    }
}

private fun createDatabase(config: ApplicationConfig, connectionName: String) {
    try {
        val dbName = config.property("ktor.database.name").getString()
        val dbConfig = HikariConfig().apply {
            driverClassName = config.property("ktor.database.driver").getString()
            jdbcUrl = config.property("ktor.database.url").getString() + "postgres"
            username = config.property("ktor.database.user").getString()
            password = config.property("ktor.database.password").getString()
            maximumPoolSize = config.property("ktor.database.maximumPoolSize").getString().toInt()
        }

        if(!connectionName.isBlank()){
            dbConfig.addDataSourceProperty("socketFactory", "com.google.cloud.sql.postgres.SocketFactory");
            dbConfig.addDataSourceProperty("cloudSqlInstance", connectionName);
        }
        HikariDataSource(dbConfig).use { dataSource ->
            dataSource.connection.use { connection ->
                connection.prepareStatement("CREATE DATABASE $dbName WITH ENCODING='UTF8'\n").execute()
            }
        }
    } catch (e: Exception) {
        println("Error while creating database ${e.message}")
    }
}

private fun runMigrations(dataSource: HikariDataSource) {
    val flyway = Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration/")
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