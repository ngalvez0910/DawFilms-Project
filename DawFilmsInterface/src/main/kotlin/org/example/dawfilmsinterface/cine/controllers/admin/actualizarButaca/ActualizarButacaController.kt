package org.example.dawfilmsinterface.cine.controllers.admin.actualizarButaca

import javafx.collections.FXCollections
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import org.example.dawfilmsinterface.productos.models.butacas.Butaca
import org.example.dawfilmsinterface.productos.viewmodels.ActualizarButacaViewModel
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
    val viewModel : ActualizarButacaViewModel by inject()

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
    lateinit var idFilterComboBox: ComboBox<Any>

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
    lateinit var idColumnTable : TableColumn<Butaca, String>

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
        logger.debug { "Inicializando bindings"}

        estadoSelectedField.textProperty().bind(viewModel.state.map { it.butaca.estado })
        tipoSelectedField.textProperty().bind(viewModel.state.map { it.butaca.tipo})
        ocupacionSelectedField.textProperty().bind(viewModel.state.map { it.butaca.ocupacion })
        //precioSelectedField.textProperty().bind(viewModel.state.map { it.butaca.precio })

        viewModel.state.addListener { _, _, newValue ->
            logger.debug { "Actualizando datos de la vista" }
            if (butacaTable.items != newValue.butacas){
                butacaTable.items = FXCollections.observableArrayList(newValue.butacas)
            }
        }
    }

    private fun initDefaultValues() {
        logger.debug { "Inicializando valores por defecto" }

        tipoFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesTipo)
        tipoFilterComboBox.selectionModel.selectFirst()

        estadoFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesEstado)
        estadoFilterComboBox.selectionModel.selectFirst()

        ocupacionFilterComboBox.items = FXCollections.observableArrayList(viewModel.state.value.typesOcupacion)
        estadoFilterComboBox.selectionModel.selectFirst()

        butacaTable.items = FXCollections.observableArrayList(viewModel.state.value.butacas)

        idColumnTable.cellValueFactory = PropertyValueFactory("id")
        estadoColumnTable.cellValueFactory = PropertyValueFactory("estado")
        tipoColumnTable.cellValueFactory = PropertyValueFactory("tipo")
        ocupacionColumnTable.cellValueFactory = PropertyValueFactory("ocupacion")
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
        editButton.setOnAction { RoutesManager.initEditarButaca() }

        /*
        idFilterComboBox.selectionModel.selectedItemProperty().addListener{ _, _, newValue ->
            newValue?.let { onComboSelected(newValue) }
        }

         */

        butacaTable.selectionModel.selectedItemProperty().addListener { _,_, newValue ->
            newValue?.let { onTablaSelected(newValue) }
        }
    }

    private fun onComboSelected(newValue: String) {
        logger.debug { "onComboSelected: $newValue"}
        //filterDataTable()
    }

    /*
    private fun filterDataTable(){
        logger.debug { "filterDataTable" }
        complementosTable.items=
            FXCollections.observableList(viewModel.butacasFilteredList(idFilterComboBox.value))
    }
    */

    private fun onTablaSelected(newValue: Butaca){
        logger.debug { "onTablaSelected: $newValue" }
        viewModel.updateButacaSeleccionada(newValue)
    }
}