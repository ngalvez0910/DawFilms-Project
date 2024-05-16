package org.example.dawfilmsinterface.ventas.services

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import org.example.dawfilmsinterface.ventas.errors.VentaError
import org.example.dawfilmsinterface.ventas.models.Venta
import org.example.dawfilmsinterface.ventas.repositories.VentaRepository
import org.lighthousegames.logging.logging
import java.util.*

private val logger = logging()

class VentaSericeImpl(
    private val ventaRepository: VentaRepository,
) : VentaService {
    override fun getById(id: UUID): Result<Venta, VentaError> {
        logger.debug { "Obteniendo venta por id: $id" }
        return ventaRepository.findById(id)
            ?.let { Ok(it) }
            ?: Err(VentaError.VentaNoEncontrada("Venta no encontrada con id: $id"))
    }

    override fun createVenta(venta: Venta): Result<Venta, VentaError> {
        logger.debug { "Creando venta: $venta" }
        return ventaRepository.validateCliente(venta.cliente)
            .andThen { ventaRepository.validateLineas(venta.lineas) }
            .andThen { Ok(ventaRepository.save(venta)) }
    }
}