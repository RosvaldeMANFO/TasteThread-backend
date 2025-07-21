package com.florientmanfo.com.florientmanfo.api.module

import com.florientmanfo.com.florientmanfo.data.table.Ingredients
import com.florientmanfo.com.florientmanfo.data.table.RecipeComments
import com.florientmanfo.com.florientmanfo.data.table.Recipes
import com.florientmanfo.com.florientmanfo.data.table.Users
import com.florientmanfo.com.florientmanfo.data.table.RecipeLikes
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureDatabase() {
    val config = this.environment.config
    val connectionName = config.property("ktor.database.connectionName").getString()
    val completeUrl  = if(!connectionName.isBlank()){
        println("Using Cloud SQL connection name $connectionName")
        config.property("ktor.database.url").getString().replace("CLOUD_SQL_CONNECTION_NAME",connectionName)
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

    transaction {
        SchemaUtils.create(
            Users,
            Recipes,
            Ingredients,
            RecipeLikes,
            RecipeComments
        )
    }
}