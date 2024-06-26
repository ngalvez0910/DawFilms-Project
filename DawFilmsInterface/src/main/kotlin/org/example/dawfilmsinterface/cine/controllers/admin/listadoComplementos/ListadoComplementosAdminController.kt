package org.example.dawfilmsinterface.cine.controllers.admin.listadoComplementos

import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.control.Alert.AlertType
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import org.example.dawfilmsinterface.cine.viewmodels.LoginViewModel
import org.example.dawfilmsinterface.productos.models.complementos.Complemento
import org.example.dawfilmsinterface.cine.viewmodels.GestionComplementosViewModel
import org.example.dawfilmsinterface.locale.toDefaultMoneyString
import org.example.dawfilmsinterface.routes.RoutesManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

private val logger = logging()

/**
 * Clase controller para la administración de complementos a través de la IU.
 * Gestiona las acciones y eventos relacionados con la vista de administración de complementos en la aplicación.
 * @autor Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 * @since 1.0.0
 * @see org.example.dawfilmsinterface.productos.models.complementos
 * @property backMenuButton Botón para regresar al menú anterior.
 * @property deleteButton Botón para eliminar un complemento.
 * @property editButton Botón para editar un complemento.
 * @property addButton Botón para añadir un nuevo complemento.
 * @property stockSelectedField Campo de texto para el stock del complemento seleccionado.
 * @property precioSelectedField Campo de texto para el precio del complemento seleccionado.
 * @property nombreSelectedField Campo de texto para el nombre del complemento seleccionado.
 * @property idSelectedField Campo de texto para el ID del complemento seleccionado.
 * @property usernameField Etiqueta que muestra el nombre de usuario.
 * @property acercaDeMenuButton Elemento de menú para la opción "Acerca de".
 * @property backMenuMenuButton Elemento de menú para regresar al menú anterior.
 * @property complementosTable Tabla que muestra los complementos.
 */
class ListadoComplementosAdminController : KoinComponent {
    private val viewModel: GestionComplementosViewModel by inject()

    private val loginViewModel : LoginViewModel by inject()

    @FXML
    lateinit var backMenuButton: Button

    @FXML
    lateinit var deleteButton: Button

    @FXML
    lateinit var editButton: Button

    @FXML
    lateinit var addButton: Button

    @FXML
    lateinit var stockSelectedField: TextField

    @FXML
    lateinit var precioSelectedField: TextField

    @FXML
    lateinit var nombreSelectedField: TextField

    @FXML
    lateinit var idSelectedField: TextField

    @FXML
    lateinit var usernameField: Label

    @FXML
    lateinit var acercaDeMenuButton: MenuItem

    @FXML
    lateinit var backMenuMenuButton: MenuItem

    @FXML
    lateinit var complementosTable: TableView<Complemento>

    @FXML
    lateinit var nombreColumnTable: TableColumn<Complemento, String>

    @FXML
    lateinit var precioColumnTable: TableColumn<Complemento, String>

    @FXML
    lateinit var stockColumnTable: TableColumn<Complemento, String>

    @FXML
    lateinit var disponibilidadColumnTable: TableColumn<Complemento, String>

    @FXML
    lateinit var complementoImage: ImageView

    /**
     * Función que inicializa la vista de administración de complementos.
     * Asigna las acciones a los botones y elementos de menú.
     * @autor Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
     * @since 1.0.0
     */
    @FXML
    private fun initialize() {
        logger.debug { "Inicializando ListadoComplementosAdminController FXML" }

        initDefaultValues()

        initBindings()

        initEventos()
    }

    private fun initBindings() {
        logger.debug { "Inicializando bindings"}

        idSelectedField.textProperty().bind(viewModel.state.map { it.complemento.id })
        nombreSelectedField.textProperty().bind(viewModel.state.map { it.complemento.nombre })
        precioSelectedField.textProperty().bind(viewModel.state.map { it.complemento.precio.toDefaultMoneyString() })
        stockSelectedField.textProperty().bind(viewModel.state.map { it.complemento.stock.toString() })
        complementoImage.imageProperty().bind(viewModel.state.map { it.complemento.imagen })


        viewModel.state.addListener { _, _, newValue ->
            logger.debug { "Actualizando datos de la vista" }
            if (complementosTable.items != newValue.complementos){
                complementosTable.items = FXCollections.observableArrayList(newValue.complementos)
            }
        }
    }

    private fun initDefaultValues() {
        logger.debug { "Inicializando valores por defecto" }

        viewModel.loadAllComplementos()

        complementosTable.items = FXCollections.observableArrayList(viewModel.state.value.complementos)
        complementosTable.columns.forEach {
            it.isResizable = false
            it.isReorderable = false
        }
        complementosTable.columns[1].style = "-fx-font-size: 15; -fx-alignment: CENTER;"
        complementosTable.columns[2].style = "-fx-font-size: 15; -fx-alignment: CENTER;"
        complementosTable.columns[3].style = "-fx-font-size: 15; -fx-alignment: CENTER;"

        nombreColumnTable.cellValueFactory = PropertyValueFactory("nombre")
        precioColumnTable.setCellValueFactory { cellData ->
            val precio = cellData.value.precio
            SimpleStringProperty(precio.toDefaultMoneyString())
        }
        stockColumnTable.cellValueFactory = PropertyValueFactory("stock")
        disponibilidadColumnTable.setCellValueFactory { cellData ->
            val disponible = if (cellData.value.isDeleted == true) "No" else "Sí"
            SimpleStringProperty(disponible)
        }

        usernameField.text = loginViewModel.state.value.currentAdmin
    }

    @FXML
    private fun initEventos() {
        complementosTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.let { onTablaSelected(newValue) }
        }
        acercaDeMenuButton.setOnAction { RoutesManager.initAcercaDeStage() }
        backMenuMenuButton.setOnAction {
            logger.debug { "Cambiando de escena a ${RoutesManager.View.MENU_CINE_ADMIN}" }
            RoutesManager.changeScene(view = RoutesManager.View.MENU_CINE_ADMIN)
        }

        addButton.setOnAction {
            onNuevoAction()
            complementosTable.selectionModel.clearSelection()
        }
        editButton.setOnAction {
            onEditarAction()
            complementosTable.selectionModel.clearSelection()
        }
        deleteButton.setOnAction {
            onEliminarAction()
        }

        backMenuButton.setOnAction {
            logger.debug { "Cambiando de escena a ${RoutesManager.View.MENU_CINE_ADMIN}" }
            RoutesManager.changeScene(view = RoutesManager.View.MENU_CINE_ADMIN)
        }
    }

    private fun onTablaSelected(newValue: Complemento){
        logger.debug { "onTablaSelected: $newValue" }
        viewModel.updateComplementoSeleccionado(newValue)
        if (complementosTable.selectionModel.selectedItem.isDeleted == true) deleteButton.isDisable = true
        if (complementosTable.selectionModel.selectedItem.isDeleted == false) deleteButton.isDisable = false
    }

    private fun onEliminarAction() {
        logger.debug { "onEliminarAction" }

        if (complementosTable.selectionModel.selectedItem == null){
            return
        }

        val complementoSelected = complementosTable.selectionModel.selectedItem.nombre

        Alert(AlertType.CONFIRMATION).apply {
            title = "Eliminando complemento"
            headerText = "¿Desea eliminar el complemento: $complementoSelected?"
        }.showAndWait().ifPresent{
            if (it == ButtonType.OK) {
                viewModel.eliminarComplemento().onSuccess {
                    logger.debug { "Complemento eliminado correctamente" }
                    showAlertOperacion(
                        alerta = AlertType.INFORMATION,
                        "Complemento eliminado",
                        "Se ha eliminado el complemento: $complementoSelected"
                    )
                    complementosTable.selectionModel.clearSelection()
                }.onFailure {
                    logger.error { "Error al eliminar el complemento: ${it.message}" }
                    showAlertOperacion(
                        alerta = AlertType.ERROR,
                        "Error al eliminar el complemento",
                        "No se ha podido eliminar el complemento: $complementoSelected"
                    )
                }
            }
        }
    }

    private fun onNuevoAction() {
        logger.debug { "Cambiando de escena a ${RoutesManager.View.EDITAR_COMPLEMENTO}" }
        viewModel.changeComplementoOperacion(GestionComplementosViewModel.TipoOperacion.NUEVO)
        RoutesManager.initEditarComplemento("AÑADIR COMPLEMENTO")
    }

    private fun onEditarAction(){
        if (complementosTable.selectionModel.selectedItem == null){
            return
        }
        logger.debug { "Cambiando de escena a ${RoutesManager.View.EDITAR_COMPLEMENTO}" }
        viewModel.changeComplementoOperacion(GestionComplementosViewModel.TipoOperacion.EDITAR)
        RoutesManager.initEditarComplemento("EDITAR COMPLEMENTO")
    }

    private fun showAlertOperacion(
        alerta: AlertType = AlertType.CONFIRMATION,
        title: String = "",
        header: String = "",
        mensaje: String = ""
    ) {
        Alert(alerta).apply {
            this.title = title
            this.headerText = header
            this.contentText = mensaje
        }.showAndWait()
    }
}