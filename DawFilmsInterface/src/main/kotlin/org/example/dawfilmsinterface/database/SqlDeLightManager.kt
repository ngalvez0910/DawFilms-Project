package org.example.dawfilmsinterface.database

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import database.DatabaseQueries
import org.example.database.AppDatabase
import org.example.dawfilmsinterface.config.Config
import org.lighthousegames.logging.logging
import java.io.File
import java.nio.file.Files


private val logger = logging()

class SqlDeLightManager(
    private val config: Config
) {
    val dbQueries: DatabaseQueries by lazy {
        JdbcSqliteDriver(config.dataBaseUrl).let { driver ->
            // Creamos la base de datos
            logger.debug { "SqlDeLightManager.init() - Create Schemas" }
            AppDatabase.Schema.create(driver)
            AppDatabase(driver)
        }.databaseQueries
    }

    fun initialize() {
        logger.debug { "Inicializando el gestor de Bases de Datos" }
        // Borramos la base de datos
        if (config.databaseInit) {
            logger.debug { "Borrando la base de datos" }
            Files.deleteIfExists(File(config.dataBaseUrl.removePrefix("jdbc:sqlite:")).toPath())
        }

        if (config.databaseRemoveData) {
            clearData()
        }
    }

    private fun clearData() {
        logger.debug { "Borrando datos de la base de datos" }
        dbQueries.transaction {
            //dbQueries.deleteAll()
        }
    }


}