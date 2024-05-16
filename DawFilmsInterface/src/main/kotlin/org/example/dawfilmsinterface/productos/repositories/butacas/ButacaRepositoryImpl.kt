package org.example.dawfilmsinterface.productos.repositories.butacas

import org.example.dawfilmsinterface.clientes.mappers.toCliente
import org.example.dawfilmsinterface.database.SqlDeLightManager
import org.example.dawfilmsinterface.productos.mappers.toButacaDto
import org.example.dawfilmsinterface.productos.mappers.toProducto
import org.example.dawfilmsinterface.productos.models.butacas.Butaca
import org.lighthousegames.logging.logging
import java.time.LocalDate

private val logger = logging()

class ButacaRepositoryImpl(
    private val dbManager: SqlDeLightManager
) : ButacaRepository {

    private val db = dbManager.dbQueries

    override fun findAll(): List<Butaca> {
        logger.debug { "Buscando todas las butacas" }
        return db.selectAllButacas().executeAsList().map { it.toProducto() as Butaca }
    }

    override fun findById(id: String): Butaca? {
        logger.debug { "Buscando butaca con id: $id" }
        return db.selectProductoById(id).executeAsOneOrNull()?.toProducto() as Butaca
    }

    override fun save(item: Butaca): Butaca {
        logger.debug { "Guardando Butaca: $item" }
        val timeStamp = LocalDate.now().toString()
        db.transaction {
            db.insertButaca(
                id = item.id,
                tipo_producto = item.tipoProducto,
                fila_butaca = item.fila.toLong(),
                columna_butaca = item.columna.toLong(),
                precio = item.tipoButaca.precio,
                tipo_butaca = item.tipoButaca.toString(),
                estado_butaca = item.estadoButaca.toString(),
                ocupacion_butaca = item.ocupacionButaca.toString(),
                created_at = timeStamp,
                updated_at = timeStamp,
                is_deleted = 0
            )
        }
        return db.selectProductoLastInserted().executeAsOne().toProducto() as Butaca
    }

    override fun update(id: String, item: Butaca): Butaca? {
        logger.debug { "Actualizando butaca con id: $id" }
        this.findById(id) ?: return null
        val timeStamp = LocalDate.now()

        db.updateButaca(
            tipo_producto = item.tipoProducto,
            precio = item.tipoButaca.precio,
            fila_butaca = item.fila.toLong(),
            columna_butaca = item.columna.toLong(),
            tipo_butaca = item.tipoButaca.toString(),
            estado_butaca = item.estadoButaca.toString(),
            ocupacion_butaca = item.ocupacionButaca.toString(),
            updated_at = timeStamp.toString(),
            is_deleted = if (item.isDeleted!!) 1 else 0,
            id = item.id
        )

        return item
    }

    override fun delete(id: String): Butaca? {
        logger.debug { "Borrando butaca con id: $id" }
        this.findById(id) ?: return null
        val timeStamp = LocalDate.now()

        db.deleteLogicoProducto(
            updated_at = timeStamp.toString(),
            id = id
        )

        return this.findById(id)
    }
}