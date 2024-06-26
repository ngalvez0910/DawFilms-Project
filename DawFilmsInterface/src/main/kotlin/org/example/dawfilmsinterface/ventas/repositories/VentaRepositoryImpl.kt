package org.example.dawfilmsinterface.ventas.repositories

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.onFailure
import database.VentaEntity
import org.example.dawfilmsinterface.clientes.models.Cliente
import org.example.dawfilmsinterface.clientes.repositories.ClienteRepository
import org.example.dawfilmsinterface.database.SqlDeLightManager
import org.example.dawfilmsinterface.productos.models.producto.Producto
import org.example.dawfilmsinterface.productos.repositories.butacas.ButacaRepository
import org.example.dawfilmsinterface.productos.repositories.complementos.ComplementoRepository
import org.example.dawfilmsinterface.ventas.errors.VentaError
import org.example.dawfilmsinterface.ventas.mappers.toLineaVenta
import org.example.dawfilmsinterface.ventas.mappers.toVenta
import org.example.dawfilmsinterface.ventas.models.LineaVenta
import org.example.dawfilmsinterface.ventas.models.Venta
import org.lighthousegames.logging.logging
import java.time.LocalDate
import java.util.*
import kotlin.math.log

private val logger = logging()

/**
 * Implementación concreta de [VentaRepository] que utiliza un [SqlDeLightManager] para interactuar con la base de datos.
 * @since 1.0.0
 * @author Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 */
class VentaRepositoryImpl(
    private val dbManager: SqlDeLightManager,
    private val butacaRepository: ButacaRepository,
    private val complementoRepository: ComplementoRepository,
    private val clienteRepository: ClienteRepository
) : VentaRepository {

    private val db = dbManager.databaseQueries

    override fun findAllVentasCliente(cliente: Cliente, lineas: List<LineaVenta>, fechaCompra: LocalDate): List<Venta> {
        logger.debug { "Buscando todas las ventas" }
        return db.selectAllVentas().executeAsList().map { it.toVenta(cliente, lineas, fechaCompra) }
    }

    override fun findAllVentas(): List<VentaEntity> {
        logger.debug { "Buscando todas las ventas" }
        return db.selectAllVentas().executeAsList()
    }

    override fun findAllLineasByID(idVenta: String): List<LineaVenta> {
        return db.selectAllLineasVentaByVentaId(idVenta).executeAsList().map {
            if (it.producto_tipo == "Butaca") it.toLineaVenta(butacaRepository.findById(it.producto_id) as Producto)
            else it.toLineaVenta(complementoRepository.findById(it.producto_id) as Producto)
        }
    }

    override fun findAllLineas(): List<LineaVenta> {
        logger.debug { "Buscando todas las lineas" }
        return db.selectAllLineasVentas().executeAsList().map {
            if (it.producto_tipo == "Butaca") it.toLineaVenta(butacaRepository.findById(it.producto_id) as Producto)
            else it.toLineaVenta(complementoRepository.findById(it.producto_id) as Producto)
        }
    }

    override fun findById(id: UUID): Venta? {
        logger.debug { "Obteniendo venta por id: $id" }

        if (db.existsVenta(id.toString()).executeAsOne()) {
            val ventaEntity = db.selectVentaById(id.toString()).executeAsOne()
            val cliente = clienteRepository.findById(ventaEntity.cliente_id)!!
            val lineasVenta = db.selectAllLineasVentaByVentaId(ventaEntity.id).executeAsList()
                .map {
                    if (it.producto_tipo == "Butaca") {
                        it.toLineaVenta(butacaRepository.findById(it.producto_id)!!)
                    } else {
                        it.toLineaVenta(complementoRepository.findById(it.producto_id)!!)
                    }
                }
            return ventaEntity.toVenta(cliente, lineasVenta, LocalDate.parse(ventaEntity.fecha_compra))
        }
        return null
    }

    override fun findVentasByDate(fechaCompra: LocalDate): List<VentaEntity> {
        logger.debug { "Obteniendo todas las ventas de la fecha: $fechaCompra" }
        return db.selectVentasByDate(fechaCompra.toString()).executeAsList()
    }

    override fun save(venta: Venta): Venta {
        logger.debug { "Guardando venta: $venta" }
        db.transaction {
            db.insertVenta(
                id = venta.id.toString(),
                cliente_id = venta.cliente.id,
                total =venta.total,
                fecha_compra = LocalDate.now().toString(),
                created_at = venta.createdAt.toString(),
                updated_at = venta.updatedAt.toString(),
                is_deleted = 0
            )
        }

        venta.lineas.forEach {
            db.transaction {
                db.insertLineaVenta(
                    id = it.id.toString(),
                    venta_id = venta.id.toString(),
                    producto_id = it.producto.id,
                    producto_tipo = it.producto.tipoProducto,
                    cantidad = it.cantidad.toLong(),
                    precio = it.precio,
                    created_at = it.createdAt.toString(),
                    updated_at = it.updatedAt.toString(),
                    is_deleted = 0
                )
            }
        }
        return venta
    }

    override fun update(id: UUID, venta: Venta): Venta? {
        logger.debug { "Actualizando venta por id: $id" }
        var result = this.findById(id) ?: return null
        val timeStamp = LocalDate.now()

        result = result.copy(
            cliente = venta.cliente,
            lineas = venta.lineas,
            fechaCompra = venta.fechaCompra,
            updatedAt = timeStamp
        )

        db.updateVenta(
            id = venta.id.toString(),
            cliente_id = venta.cliente.id,
            total = venta.total,
            fecha_compra = venta.fechaCompra.toString(),
            updated_at = timeStamp.toString(),
            is_deleted = if (venta.isDeleted) 1 else 0
        )
        return result
    }

    override fun delete(id: UUID): Venta? {
        logger.debug { "Borrando venta por id: $id" }
        val result = this.findById(id) ?: return null
        val timeStamp = LocalDate.now()

        db.updateVenta(
            id = result.id.toString(),
            cliente_id = result.cliente.id,
            total = result.total,
            fecha_compra = result.fechaCompra.toString(),
            updated_at = timeStamp.toString(),
            is_deleted = if (result.isDeleted) 1 else 0
        )
        return result.copy(isDeleted = true, updatedAt = timeStamp)
    }

    override fun deleteAllVentas() {
        logger.debug { "Borrando todas las ventas" }
        db.transaction {
            db.deleteAllLineas()
            db.deleteAllVentas()
        }
    }

    override fun validateCliente(cliente: Cliente): Result<Cliente, VentaError> {
        logger.debug { "Validando cliente: $cliente" }
        return clienteRepository.findById(cliente.id)
            ?.let { Ok(it) }
            ?: Err(VentaError.VentaNoValida("Cliente no encontrado con id: ${cliente.id}"))
    }

    override fun validateLineas(lineas: List<LineaVenta>): Result<List<LineaVenta>, VentaError> {
        logger.debug { "Validando lineas - Existen Productos: $lineas" }
        lineas.forEach {
            if (it.tipoProducto == "Butaca") {
                butacaRepository.findById(it.producto.id)
                    ?: return Err(VentaError.VentaNoValida("Producto no encontrado con id: ${it.producto.id}"))
            } else {
                complementoRepository.findById(it.producto.id)
                    ?: return Err(VentaError.VentaNoValida("Producto no encontrado con id: ${it.producto.id}"))
            }
        }

        logger.debug { "Validando lineas - Cantidad y Stock de productos: $lineas" }
        lineas.forEach {
            if (it.cantidad <= 0) {
                return Err(VentaError.VentaNoValida("La cantidad de productos debe ser mayor que 0"))
            }
            if (it.tipoProducto == "Complemento") {
                complementoRepository.findById(it.producto.id)?.let { producto ->
                    if (it.cantidad > producto.stock) {
                        return Err(VentaError.VentaNoValida("No hay suficiente stock para el producto: ${producto.nombre}, stock: ${producto.stock} cantidad: ${it.cantidad}"))
                    }
                }
            }
        }
        return Ok(lineas)
    }
}