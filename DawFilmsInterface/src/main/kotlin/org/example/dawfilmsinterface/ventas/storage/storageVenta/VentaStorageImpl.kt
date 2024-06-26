package org.example.dawfilmsinterface.ventas.storage.storageVenta

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.example.dawfilmsinterface.ventas.dto.VentaDto
import org.example.dawfilmsinterface.ventas.errors.VentaError
import org.example.dawfilmsinterface.ventas.mappers.toVentaDtoList
import org.example.dawfilmsinterface.ventas.mappers.toVentaList
import org.example.dawfilmsinterface.ventas.models.Venta
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()

/**
 * Implementación de [VentaStorage] que maneja el almacenamiento de ventas en formato JSON.
 * @since 1.0.0
 * @author Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 */
class VentaStorageImpl : VentaStorage {
    override fun storeJson(file: File, data: List<Venta>): Result<Long, VentaError> {
        logger.debug { "Guardando datos en fichero $file" }
        return try {
            val json = Json {
                prettyPrint = true
                ignoreUnknownKeys = true
                coerceInputValues = true
            }
            val jsonString = json.encodeToString<List<VentaDto>>(data.toVentaDtoList())
            file.writeText(jsonString)
            Ok(data.size.toLong())
        }catch(e: Exception){
            Err(VentaError.VentaStorageError("Error al escribir el JSON ${e.message}"))
        }
    }

    override fun loadJson(file: File): Result<List<Venta>, VentaError> {
        logger.debug { "Cargando datos en fichero $file" }
        val json = Json {
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
        }
        return try {
            val jsonString = file.readText()
            val data = json.decodeFromString<List<VentaDto>>(jsonString)
            Ok(data.toVentaList())
        }catch (e: Exception) {
            Err(VentaError.VentaStorageError("Error al leer el JSON ${e.message}"))
        }
    }
}