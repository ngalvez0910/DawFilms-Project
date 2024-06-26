package org.example.dawfilmsinterface.cine.viewmodels

import com.github.michaelbull.result.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Alert
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.ButtonType
import javafx.scene.image.Image
import org.example.dawfilmsinterface.productos.errors.ProductoError
import org.example.dawfilmsinterface.productos.mappers.toModel
import org.example.dawfilmsinterface.productos.models.complementos.Complemento
import org.example.dawfilmsinterface.productos.service.ProductoService
import org.example.dawfilmsinterface.productos.storage.genericStorage.ProductosStorage
import org.example.dawfilmsinterface.routes.RoutesManager
import org.lighthousegames.logging.logging
import java.io.File

private val logger = logging()
/**
 * Clase GestionComplementosViewModel
 *
 * Gestiona la lógica relacionada con los complementos, incluyendo la carga de tipos, la gestión de complementos y la actualización de imágenes.
 *
 * @param service Servicio para gestionar productos.
 * @param storage Servicio para gestionar el almacenamiento de imágenes de productos.
 * @autor Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 * @since 1.0.0
 */
class GestionComplementosViewModel(
    private val service : ProductoService,
    private val storage : ProductosStorage
) {
    val state : SimpleObjectProperty<GestionState> = SimpleObjectProperty(GestionState())
    /**
     * Carga los tipos de categoría y disponibilidad en el estado actual.
     */
     fun loadTypes() {
        logger.debug { "Cargando tipos" }
        state.value = state.value.copy(typesCategoria = TipoCategoria.entries.map { it.value })
        state.value = state.value.copy(availability = Disponibilidades.entries.map { it.value })
    }
    /**
     * Carga todos los complementos del servicio en el estado actual.
     */
     fun loadAllComplementos(){
        logger.debug { "Cargando complementos del repositorio" }
        service.getAllComplementos().onSuccess {
            logger.debug { "Cargando complementos del repositorio: ${it.size}" }
            state.value = state.value.copy(complementos = it)
            updateActualState()
        }
    }

    private fun updateActualState() {
        logger.debug { "Actualizando el estado de Complemento" }
        state.value = state.value.copy(
            complemento = ComplementoState()
        )
    }
    /**
     * Actualiza el estado del complemento seleccionado.
     *
     * @param complemento Complemento a actualizar en el estado.
     */
    fun updateComplementoSeleccionado(complemento: Complemento){
        logger.debug { "Actualizando estado de Complemento: $complemento" }

        var imagen = Image(RoutesManager.getResourceAsStream("icons/sinImagen.png"))
        var fileImage = File(RoutesManager.getResource("icons/sinImagen.png").toURI())

        storage.loadImage(complemento.imagen).onSuccess {
            imagen = Image(it.absoluteFile.toURI().toString())
            fileImage = it
        }

        state.value = state.value.copy(
            complemento = ComplementoState(
                id = complemento.id,
                nombre = complemento.nombre,
                precio = complemento.precio,
                stock = complemento.stock,
                categoria = if(complemento.categoria.name == "COMIDA") "COMIDA" else complemento.categoria.name,
                imagen = imagen,
                fileImage = fileImage,
                isDeleted = complemento.isDeleted!!
            )
        )
    }
    /**
     * Edita el complemento seleccionado.
     *
     * @return Resultado de la operación de edición.
     */
    fun editarComplemento(): Result<Complemento, ProductoError> {
        logger.debug { "Editando Complemento" }

        val updatedComplementoTemp = state.value.complemento.copy()
        val fileNameTemp = state.value.complemento.oldFileImage?.name
            ?: "sinImagen.png"
        var updatedComplemento = state.value.complemento.toModel().copy(imagen = fileNameTemp)

        logger.warn { updatedComplemento }
        logger.warn { updatedComplementoTemp }

        return updatedComplemento.validate().andThen {
            updatedComplementoTemp.fileImage?.let { newFileImage ->
                if (updatedComplemento.imagen == "sinImagen.png" || updatedComplemento.imagen == "") {
                    storage.saveImage(newFileImage).onSuccess {
                        updatedComplemento = updatedComplemento.copy(imagen = it.name)
                        logger.warn { updatedComplemento }
                    }
                } else {
                    storage.updateImage(fileNameTemp, newFileImage)
                }
            }

            service.updateComplemento(updatedComplemento.id, updatedComplemento).onSuccess {
                val index = state.value.complementos.indexOfFirst { complemento -> complemento.id == it.id }
                state.value = state.value.copy(
                    complementos = state.value.complementos.toMutableList().apply { this[index] = it }
                )
                updateActualState()
                Ok(it)
            }
        }
    }
    /**
     * Crea un nuevo complemento.
     *
     * @return Resultado de la operación de creación.
     */
    fun createComplemento(): Result<Complemento, ProductoError>{
        logger.debug { "Creando Complemento"}
        val newId = ((service.getAllComplementos().value.maxBy { it.id }.id.toInt()) + 1).toString()
        val newComplementoTemp = state.value.complemento.copy()
        var newComplemento = newComplementoTemp.toModel().copy(id = newId)

        return newComplemento.validate().andThen {
            newComplementoTemp.fileImage?.let { newFileImage ->
                storage.saveImage(newFileImage).onSuccess {
                    newComplemento = newComplemento.copy(imagen = it.name)
                }
            }

            service.saveComplemento(newComplemento).andThen {
                state.value = state.value.copy(
                    complementos = state.value.complementos + it
                )
                updateActualState()
                Ok(it)
            }
        }
    }
    /**
     * Valida el complemento.
     *
     * @return Resultado de la validación.
     */
    private fun Complemento.validate(): Result<Complemento, ProductoError> {
        if (this.nombre.isEmpty() || this.nombre.isBlank()) return Err(ProductoError.ProductoValidationError("El nombre no puede estar vacio"))
        return Ok(this)
    }
    /**
     * Elimina el complemento seleccionado.
     *
     * @return Resultado de la operación de eliminación.
     */
    fun eliminarComplemento(): Result<Unit, ProductoError>{
        logger.debug { "Eliminando Complemento" }
        val complemento = state.value.complemento.copy()
        val myId = complemento.id

        Alert(AlertType.CONFIRMATION).apply {
            this.title = "Borrar imagen"
            this.headerText = "¿Deseas borrar la imagen?"
            this.contentText = "El complemento quedará no disponible y sin imagen"
        }.showAndWait().ifPresent { opcion ->
            if (opcion == ButtonType.OK) {
                complemento.fileImage?.let {
                    if (it.name != "sinImagen.png") {
                        storage.deleteImage(it)
                    }
                }
            }
        }

        service.deleteComplemento(myId).onSuccess {
            val index = state.value.complementos.indexOfFirst { complemento -> complemento.id == it.id }
            state.value = state.value.copy(
                complementos = state.value.complementos.toMutableList().apply { this[index] = it }
            )
        }
        updateActualState()
        return Ok(Unit)
    }
    /**
     * Cambia el tipo de operación actual.
     *
     * @param newValue Nuevo tipo de operación.
     */
    fun changeComplementoOperacion(newValue: TipoOperacion){
        logger.debug { "Cambiando tipo de operacion: $newValue" }
        if (newValue == TipoOperacion.EDITAR){
            logger.debug { "Copiando estado de Complemento seleccionado a Operacion"}
            state.value = state.value.copy(
                complemento = state.value.complemento.copy(),
                tipoOperacion = newValue
            )
        } else {
            logger.debug { "Limpiando estado de Complemento Operacion" }
            state.value = state.value.copy(
                complemento = ComplementoState(id = ((service.getAllComplementos().value.maxBy { it.id }.id.toInt()) + 1).toString()),
                tipoOperacion = newValue
            )
        }
    }
    /**
     * Actualiza los datos del complemento en la operación actual.
     *
     * @param id Identificador del complemento.
     * @param nombre Nombre del complemento.
     * @param precio Precio del complemento.
     * @param stock Stock del complemento.
     * @param categoria Categoría del complemento.
     * @param imagen Imagen del complemento.
     * @param isDeleted Estado de eliminación del complemento.
     */
    fun updateDataComplementoOperacion(
        id: String,
        nombre: String,
        precio: Double,
        stock: Int,
        categoria: String,
        imagen: Image,
        isDeleted: Boolean
    ){
        logger.debug { "Actualizando estado de Complemento Operación" }
        state.value = state.value.copy(
            complemento = state.value.complemento.copy(
                id = id,
                nombre = nombre,
                precio = precio,
                stock = stock,
                categoria = categoria,
                imagen = imagen,
                isDeleted = isDeleted
            )
        )
    }
    /**
     * Actualiza la imagen del complemento en la operación actual.
     *
     * @param fileImage Archivo de imagen.
     */
    fun updateImageComplementoOperacion(fileImage: File) {
        logger.debug { "Actualizando imagen: $fileImage" }
        state.value = state.value.copy(
            complemento = state.value.complemento.copy(
                imagen = Image(fileImage.toURI().toString()),
                fileImage = fileImage,
                oldFileImage = state.value.complemento.fileImage
            )
        )
    }
    /**
     * Clase de datos GestionState
     *
     * Representa el estado de la gestión de complementos.
     *
     * @param typesCategoria Lista de tipos de categorías.
     * @param availability Lista de disponibilidades.
     * @param complementos Lista de complementos.
     * @param complemento Estado del complemento actual.
     * @param tipoOperacion Tipo de operación actual.
     * @param newId Nuevo identificador del complemento.
     */
    data class GestionState(
        val typesCategoria: List<String> = emptyList(),

        val availability: List<String> = emptyList(),

        val complementos : List<Complemento> = emptyList(),

        val complemento : ComplementoState = ComplementoState(),

        val tipoOperacion : TipoOperacion = TipoOperacion.NUEVO,

        val newId : String = ""
    )
    /**
     * Clase de datos ComplementoState
     *
     * Representa el estado de un complemento.
     *
     * @param id Identificador del complemento.
     * @param nombre Nombre del complemento.
     * @param precio Precio del complemento.
     * @param stock Stock del complemento.
     * @param categoria Categoría del complemento.
     * @param imagen Imagen del complemento.
     * @param fileImage Archivo de imagen del complemento.
     * @param oldFileImage Archivo de imagen antiguo del complemento.
     * @param isDeleted Estado de eliminación del complemento.
     */
    data class ComplementoState(
        var id : String = "",
        val nombre : String = "",
        val precio : Double = 0.0,
        val stock : Int = 0,
        val categoria : String = "",
        val imagen : Image = Image(RoutesManager.getResourceAsStream("icons/sinImagen.png")),
        val fileImage: File? = null,
        val oldFileImage: File? = null,
        val isDeleted : Boolean = false
    )
    /**
     * Enum TipoOperacion
     *
     * Representa los tipos de operación que se pueden realizar.
     *
     * @param value Valor del tipo de operación.
     */
    enum class TipoOperacion(val value : String){
        NUEVO("NUEVO"), EDITAR("EDITAR")
    }
    /**
     * Enum TipoCategoria
     *
     * Representa los tipos de categoría de los complementos.
     *
     * @param value Valor del tipo de categoría.
     */
    enum class TipoCategoria(val value : String){
        BEBIDA("BEBIDA"), COMIDA("COMIDA")
    }
    /**
     * Enum Disponibilidades
     *
     * Representa las disponibilidades de los complementos.
     *
     * @param value Valor de la disponibilidad.
     */
    enum class Disponibilidades(val value: String) {
        FALSE("SI"), TRUE("NO")
    }
}