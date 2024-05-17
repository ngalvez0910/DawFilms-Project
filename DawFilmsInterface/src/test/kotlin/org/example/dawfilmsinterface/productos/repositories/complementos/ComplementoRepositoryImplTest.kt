package org.example.dawfilmsinterface.productos.repositories.complementos

import org.example.dawfilmsinterface.database.SqlDeLightManager
import org.example.dawfilmsinterface.productos.models.complementos.CategoriaComplemento
import org.example.dawfilmsinterface.productos.models.complementos.Complemento
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.fileProperties
import org.koin.test.junit5.AutoCloseKoinTest
import kotlin.test.assertEquals

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ComplementoRepositoryImplTest : AutoCloseKoinTest() {
    private val dbManager : SqlDeLightManager by inject()
    private val complementoRepository : ComplementoRepository by inject()

    @BeforeAll
    fun setUpAll() {
        println("Iniciando tests...")
        startKoin {
            fileProperties("/application.properties")
        }
    }

    @BeforeEach
    fun setUp() {
        dbManager.initialize()
    }

    @Test
    fun findAll() {
        val complementos = complementoRepository.findAll()

        assertEquals(15, complementos.size)
    }

    @Test
    fun findById() {
        val complemento = complementoRepository.findById("1")

        assertEquals("1", complemento?.id)
        assertEquals("Palomitas", complemento?.nombre)
        assertEquals(3.0, complemento?.precio)
        assertEquals(50, complemento?.stock)
        assertEquals(CategoriaComplemento.COMIDA, complemento?.categoria)
    }

    @Test
    fun findByIdNotFound() {
        val complemento = complementoRepository.findById("-1")

        assertEquals(null, complemento)
    }

    @Test
    fun save() {
        val complemento = complementoRepository.save(
            Complemento(
                id = "16",
                nombre = "Agua",
                precio = 2.0,
                stock = 40,
                categoria = CategoriaComplemento.BEBIDA
            )
        )

        assertEquals("16", complemento.id)
        assertEquals("Agua", complemento.nombre)
        assertEquals(2.0, complemento.precio)
        assertEquals(40, complemento.stock)
        assertEquals(CategoriaComplemento.BEBIDA, complemento.categoria)
    }

    @Test
    fun update() {
        val complemento = complementoRepository.update(
            "1",
            Complemento(
                id = "1",
                nombre = "Complemento actualizado",
                precio = 3.50,
                stock = 50,
                categoria = CategoriaComplemento.COMIDA
            )
        )

        assertEquals("1", complemento?.id)
        assertEquals("Complemento actualizado", complemento?.nombre)
        assertEquals(3.50, complemento?.precio)
        assertEquals(50, complemento?.stock)
        assertEquals(CategoriaComplemento.COMIDA, complemento?.categoria)
    }

    @Test
    fun updateNotFound() {
        val complemento = complementoRepository.update(
            "15",
            Complemento(
                id = "1",
                nombre = "Complemento actualizado",
                precio = 3.50,
                stock = 50,
                categoria = CategoriaComplemento.COMIDA
            )
        )

        assertEquals(null, complemento)
    }

    @Test
    fun delete() {
        val complemento = complementoRepository.delete("1")

        assertEquals("1", complemento?.id)
    }

    @Test
    fun deleteNotFound() {
        val complemento = complementoRepository.delete("20")

        assertEquals(null, complemento)
    }
}