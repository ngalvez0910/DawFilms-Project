package org.example.dawfilmsinterface.cine.controllers.admin.actualizarButaca

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.Alert.AlertType
import javafx.scene.control.cell.PropertyValueFactory
import org.example.dawfilmsinterface.cine.viewmodels.LoginViewModel
import org.example.dawfilmsinterface.productos.models.butacas.Butaca
import org.example.dawfilmsinterface.cine.viewmodels.GestionButacaViewModel
import org.example.dawfilmsinterface.locale.toDefaultMoneyString
import org.example.dawfilmsinterface.routes.RoutesManager
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.lighthousegames.logging.logging

private val logger = logging()

/**
 * Clase controller para la actualización de las butacas a través de la IU
 * @author Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
 * @since 1.0.0
 * @property backMenuMenuButton Botón que nos llevara de regreso al menú
 * @property precioSelectedField Campo de texto que nos indicara el precio de la butaca
 * @property ocupacionSelectedField Campo de texto que nos indicara la ocupación de la butaca
 * @property tipoSelectedField Campo de texto que nos indicará el tipo de la butaca
 * @property estadoSelectedField Campo de texto que nos indicará el estado de la butaca
 * @property idSelectedField Campo de texto que nos indicará el id de la butaca
 * @property editButton Botón que nos llevara a la opción de editar butaca
 * @property idFilterComboBox Combo box que nos ayudará a la selección
 * @property ocupacionFilterComboBox Combo box que filtrara las butacas dependiendo de si están ocupadas o no
 * @property tipoFilterComboBox Combo box que filtrara las butacas dependiendo de su tipo
 * @property estadoFilterComboBox Combo box que filtrara las butacas dependiendo de su estado
 * @property backMenuButton Botón que nos devolverá al menu anterior
 * @property usernameField Label que nos mostrará el nombre de usuario
 * @property acercaDeMenuButton Botón de menú que nos mostrará la información relevante de los desarrolladores
 * @property butacaTable Tabla donde se nos mostrará la información relativa a los complementos
 */
class ActualizarButacaController : KoinComponent {
    val viewModel: GestionButacaViewModel by inject()

    val loginViewModel: LoginViewModel by inject()

    @FXML
    lateinit var backMenuMenuButton: MenuItem

    @FXML
    lateinit var precioSelectedField: TextField

    @FXML
    lateinit var ocupacionSelectedField: TextField

    @FXML
    lateinit var tipoSelectedField: TextField

    @FXML
    lateinit var estadoSelectedField: TextField

    @FXML
    lateinit var idSelectedField: TextField

    @FXML
    lateinit var editButton: Button

    @FXML
    lateinit var ocupacionFilterComboBox: ComboBox<Any>

    @FXML
    lateinit var tipoFilterComboBox: ComboBox<Any>

    @FXML
    lateinit var estadoFilterComboBox: ComboBox<Any>

    @FXML
    lateinit var backMenuButton: Button

    @FXML
    lateinit var usernameField: Label

    @FXML
    lateinit var acercaDeMenuButton: MenuItem

    @FXML
    lateinit var butacaTable: TableView<Butaca>

    @FXML
    lateinit var idColumnTable: TableColumn<Butaca, String>

    @FXML
    lateinit var estadoColumnTable: TableColumn<Butaca, String>

    @FXML
    lateinit var tipoColumnTable: TableColumn<Butaca, String>

    @FXML
    lateinit var ocupacionColumnTable: TableColumn<Butaca, String>

    /**
     * Función que inicializa la vista de actualizar butaca
     * @author Jaime León, German Fernández, Natalia González, Alba García, Javier Ruiz
     * @since 1.0.0
     */
    @FXML
    private fun initialize() {
        logger.debug { "Inicializando ActualizarButacaController FXML" }

        initDefaultValues()

        initBindings()

        initEventos()
    }

    private fun initBindings() {
        logger.debug { "Inicializando bindings" }

        idSelectedField.textProperty().bind(viewModel.state.map { it.butaca.id })
        estadoSelectedField.textProperty().bind(viewModel.state.map { it.butaca.estado })
        tipoSelectedField.textProperty().bind(viewModel.state.map { it.butaca.tipo })
        ocupacionSelectedField.textProperty().bind(viewModel.state.map { it.butaca.ocupacion })
        precioSelectedField.textProperty().bind(viewModel.state.map { it.butaca.precio.toDefaultMoneyString() })

        viewModel.state.addListener { _, _, newValue ->
            logger.debug { "Actualizando datos de la vista" }
            if (butacaTable.items != newValue.butacas) {
                butacaTable.items = FXCollections.observableArrayList(newValue.butacas)
            }
        }
    }

    private fun initDefaultValues() {
        logger.debug { "Inicializando valores por defecto" }

        viewModel.initialize()

        tipoFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesTipoFiltro)
        tipoFilterComboBox.selectionModel.selectFirst()

        estadoFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesEstadoFiltro)
        estadoFilterComboBox.selectionModel.selectFirst()

        ocupacionFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesOcupacionFiltro)
        ocupacionFilterComboBox.selectionModel.selectFirst()

        butacaTable.items = FXCollections.observableArrayList(viewModel.state.value.butacas)
        butacaTable.columns.forEach {
            it.isResizable = false
            it.isReorderable = false
        }

        idColumnTable.cellValueFactory = PropertyValueFactory("id")
        estadoColumnTable.cellValueFactory = PropertyValueFactory("estadoButaca")
        tipoColumnTable.cellValueFactory = PropertyValueFactory("tipoButaca")
        ocupacionColumnTable.cellValueFactory = PropertyValueFactory("ocupacionButaca")

        usernameField.text = loginViewModel.state.value.currentAdmin
    }

    private fun initEventos() {
        acercaDeMenuButton.setOnAction { RoutesManager.initAcercaDeStage() }

        backMenuButton.setOnAction {
            logger.debug { "Cambiando de escena a ${RoutesManager.View.MENU_CINE_ADMIN}" }
            RoutesManager.changeScene(view = RoutesManager.View.MENU_CINE_ADMIN)
        }

        backMenuMenuButton.setOnAction {
            logger.debug { "Cambiando de escena a ${RoutesManager.View.MENU_CINE_ADMIN}" }
            RoutesManager.changeScene(view = RoutesManager.View.MENU_CINE_ADMIN)
        }
        editButton.setOnAction { onEditarAction() }

        estadoFilterComboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.let { onComboSelected(newValue.toString()) }
        }
        tipoFilterComboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.let { onComboSelected(newValue.toString()) }
        }
        ocupacionFilterComboBox.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.let { onComboSelected(newValue.toString()) }
        }

        butacaTable.selectionModel.selectedItemProperty().addListener { _, _, newValue ->
            newValue?.let { onTablaSelected(newValue) }
        }
    }

    private fun onComboSelected(newValue: String) {
        logger.debug { "onComboSelected: $newValue" }
        filterDataTable()
    }


    private fun filterDataTable() {
        logger.debug { "filterDataTable" }
        butacaTable.items =
            FXCollections.observableList(
                viewModel.butacasFilteredList(
                    estadoFilterComboBox.value.toString(),
                    tipoFilterComboBox.value.toString(),
                    ocupacionFilterComboBox.value.toString()
                )
            )
    }

    private fun onTablaSelected(newValue: Butaca) {
        logger.debug { "onTablaSelected: $newValue" }
        viewModel.updateButacaSeleccionada(newValue)
    }

    private fun onEditarAction() {
        if (butacaTable.selectionModel.selectedItem == null) {
            return
        }
        logger.debug { "onEditarAction" }
        RoutesManager.initEditarButaca()
    }
}