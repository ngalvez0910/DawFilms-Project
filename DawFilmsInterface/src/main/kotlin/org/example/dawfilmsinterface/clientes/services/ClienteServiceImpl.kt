package org.example.dawfilmsinterface.clientes.services

import com.github.michaelbull.result.*
import org.example.dawfilmsinterface.clientes.cache.ClienteCache
import org.example.dawfilmsinterface.clientes.errors.ClienteError
import org.example.dawfilmsinterface.clientes.models.Cliente
import org.example.dawfilmsinterface.clientes.repositories.ClienteRepository
import org.example.dawfilmsinterface.clientes.validators.ClienteValidator
import org.example.dawfilmsinterface.productos.errors.ProductoError
import org.lighthousegames.logging.logging

private val logger = logging()

class ClienteServiceImpl(
    private val clienteRepository: ClienteRepository,
    private val clienteCache: ClienteCache,
    private val clienteValidator: ClienteValidator
) : ClienteService {
    override fun getAll(): Result<List<Cliente>, ClienteError> {
        logger.debug { "Obteniendo todos los clientes" }
        val clientes: MutableList<Cliente> = mutableListOf()
        clienteRepository.findAll().forEach { clientes.add(it) }
        return Ok(clientes)
    }

    override fun getById(id: Long): Result<Cliente, ClienteError> {
        logger.debug { "Obteniendo cliente con id: $id" }
        return clienteCache.get(id.toInt()).mapBoth(
            success = {
                logger.debug { "Cliente encontrado en cache" }
                Ok(it)
            },
            failure = {
                logger.debug { "Cliente no encontrado en cache" }
                clienteRepository.findById(id)
                    ?.let { Ok(it) }
                    ?: Err(ClienteError.ClienteNoEncontrado("Cliente no encontrado con id: $id"))
            }
        )
    }

    override fun save(cliente: Cliente): Result<Cliente, ClienteError> {
        logger.debug { "Guardando cliente: $cliente" }
        return clienteValidator.validate(cliente).andThen {
            Ok(clienteRepository.save(it))
        }.andThen { c ->
            println("Guardando en cache")
            clienteCache.put(c.id.toInt(), c)
        }
    }

    override fun update(id: Long, cliente: Cliente): Result<Cliente, ClienteError> {
        logger.debug { "Actualizando cliente con id: $id" }
        return clienteValidator.validate(cliente).andThen { p ->
            clienteRepository.update(id, p)
                ?.let { Ok(it) }
                ?: Err(ClienteError.ClienteNoActualizado("Cliente no actualizado con id: $id"))
        }.andThen {
            clienteCache.put(id.toInt(), it)
        }
    }

    override fun delete(id: Long): Result<Cliente, ClienteError> {
        logger.debug { "Borrando cliente con id: $id" }
        return clienteRepository.delete(id)
            ?.let {
                clienteCache.remove(id.toInt())
                Ok(it)
            } ?: Err(ClienteError.ClienteNoEliminado("Butaca no eliminada con id: $id"))
    }

}