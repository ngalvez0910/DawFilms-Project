package org.example.dawfilmsinterface.clientes.storage

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.dawfilmsinterface.clientes.dto.ClienteDto
import org.example.dawfilmsinterface.clientes.errors.ClienteError
import org.example.dawfilmsinterface.clientes.mappers.toClienteDtoList
import org.example.dawfilmsinterface.clientes.mappers.toClienteList
import org.example.dawfilmsinterface.clientes.models.Cliente
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()

/**
 * Implementación de [ClienteStorage] que gestiona el almacenamiento y la carga de datos de clientes en formato JSON.
 * @autor Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 * @since 1.0.0
 */
class ClienteStorageImpl: ClienteStorage {
    override fun storeJson(file: File, data: List<Cliente>): Result<Long, ClienteError> {
        logger.debug { "Guardando datos en fichero $file" }
        return try {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
            }
            val jsonString = json.encodeToString<List<ClienteDto>>(data.toClienteDtoList())
            file.writeText(jsonString)
            Ok(data.size.toLong())
        }catch(e:Exception){
            Err(ClienteError.ClienteStorageError("Error al escribir el JSON: ${e.message}"))
        }
    }

    override fun loadJson(file: File): Result<List<Cliente>, ClienteError> {
        logger.debug { "Cargando datos en fichero $file" }
        val json = Json{
            prettyPrint = true
            ignoreUnknownKeys = true
        }
        return try {
            val jsonString = file.readText()
            val data = json.decodeFromString<List<ClienteDto>>(jsonString)
            Ok(data.toClienteList())
        }catch (e: Exception) {
            Err(ClienteError.ClienteStorageError("Error al leer el JSON: ${e.message}"))
        }
    }
}