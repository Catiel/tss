package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters para acceder y modificar los parámetros de configuración
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para definir márgenes y espaciado interno
import javafx.scene.Scene; // Importa la clase Scene de JavaFX para crear la escena del diálogo
import javafx.scene.control.*; // Importa todas las clases de controles de JavaFX (Button, Label, TextField, TabPane, etc.)
import javafx.scene.layout.GridPane; // Importa la clase GridPane de JavaFX para layout en cuadrícula
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para layout vertical
import javafx.stage.Modality; // Importa la clase Modality de JavaFX para definir la modalidad del diálogo
import javafx.stage.Stage; // Importa la clase Stage de JavaFX que representa la ventana del diálogo

public class ParametersDialog extends Stage { // Declaración de la clase pública ParametersDialog que extiende Stage para crear un diálogo modal de parámetros
    private SimulationParameters parameters; // Variable privada que almacena la referencia al objeto de parámetros de simulación que será modificado
    private boolean accepted = false; // Variable privada booleana que indica si el usuario aceptó los cambios, inicializada en false

    // Campos de texto para parámetros
    private TextField durationField; // Variable privada que almacena el campo de texto para la duración de la simulación
    private TextField seedField; // Variable privada que almacena el campo de texto para la semilla aleatoria

    // Capacidades
    private TextField lavadoraCapField; // Variable privada que almacena el campo de texto para la capacidad de la lavadora
    private TextField almacenPinturaCapField; // Variable privada que almacena el campo de texto para la capacidad del almacén de pintura
    private TextField pinturaCapField; // Variable privada que almacena el campo de texto para la capacidad de la estación de pintura
    private TextField almacenHornoCapField; // Variable privada que almacena el campo de texto para la capacidad del almacén del horno
    private TextField hornoCapField; // Variable privada que almacena el campo de texto para la capacidad del horno
    private TextField inspeccionStationsField; // Variable privada que almacena el campo de texto para el número de estaciones de inspección
    private TextField inspeccionOpsField; // Variable privada que almacena el campo de texto para el número de operaciones por pieza en inspección

    // Distribuciones
    private TextField arrivalMeanField; // Variable privada que almacena el campo de texto para la media de tiempo entre arribos
    private TextField transportRecLavMeanField; // Variable privada que almacena el campo de texto para la media de tiempo de transporte de recepción a lavadora
    private TextField transportLavAlmMeanField; // Variable privada que almacena el campo de texto para la media de tiempo de transporte de lavadora a almacén
    private TextField lavadoraMeanField; // Variable privada que almacena el campo de texto para la media de tiempo de proceso en lavadora
    private TextField lavadoraStdDevField; // Variable privada que almacena el campo de texto para la desviación estándar de tiempo de proceso en lavadora
    private TextField pinturaMinField; // Variable privada que almacena el campo de texto para el tiempo mínimo de proceso en pintura
    private TextField pinturaModeField; // Variable privada que almacena el campo de texto para la moda de tiempo de proceso en pintura
    private TextField pinturaMaxField; // Variable privada que almacena el campo de texto para el tiempo máximo de proceso en pintura
    private TextField transportPintAlmMinField; // Variable privada que almacena el campo de texto para el tiempo mínimo de transporte de pintura a almacén
    private TextField transportPintAlmMaxField; // Variable privada que almacena el campo de texto para el tiempo máximo de transporte de pintura a almacén
    private TextField hornoMinField; // Variable privada que almacena el campo de texto para el tiempo mínimo de proceso en horno
    private TextField hornoMaxField; // Variable privada que almacena el campo de texto para el tiempo máximo de proceso en horno
    private TextField transportHorInspMinField; // Variable privada que almacena el campo de texto para el tiempo mínimo de transporte de horno a inspección
    private TextField transportHorInspMaxField; // Variable privada que almacena el campo de texto para el tiempo máximo de transporte de horno a inspección
    private TextField inspeccionMeanField; // Variable privada que almacena el campo de texto para la media de tiempo de cada operación de inspección

    public ParametersDialog(SimulationParameters params) { // Constructor público que inicializa el diálogo de parámetros recibiendo el objeto de parámetros a modificar como parámetro
        this.parameters = params; // Asigna el objeto de parámetros recibido a la variable de instancia

        setTitle("Configuración de Parámetros de Simulación"); // Establece el título de la ventana del diálogo
        initModality(Modality.APPLICATION_MODAL); // Establece la modalidad como APPLICATION_MODAL para bloquear la interacción con otras ventanas

        VBox root = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos como contenedor raíz
        root.setPadding(new Insets(15)); // Establece un margen interno de 15 píxeles en todos los lados del contenedor

        // Crear pestañas
        TabPane tabPane = new TabPane(); // Crea un nuevo TabPane para organizar los parámetros en pestañas

        Tab generalTab = new Tab("General", createGeneralPane()); // Crea una nueva pestaña "General" con el contenido del panel general
        generalTab.setClosable(false); // Establece que la pestaña no puede ser cerrada por el usuario

        Tab capacitiesTab = new Tab("Capacidades", createCapacitiesPane()); // Crea una nueva pestaña "Capacidades" con el contenido del panel de capacidades
        capacitiesTab.setClosable(false); // Establece que la pestaña no puede ser cerrada por el usuario

        Tab distributionsTab = new Tab("Distribuciones", createDistributionsPane()); // Crea una nueva pestaña "Distribuciones" con el contenido del panel de distribuciones
        distributionsTab.setClosable(false); // Establece que la pestaña no puede ser cerrada por el usuario

        tabPane.getTabs().addAll(generalTab, capacitiesTab, distributionsTab); // Agrega las tres pestañas al TabPane en el orden especificado

        // Botones
        Button okButton = new Button("Aceptar"); // Crea un nuevo botón con el texto "Aceptar"
        okButton.setOnAction(e -> handleOk()); // Establece el manejador de eventos del botón para llamar a handleOk cuando se presiona

        Button cancelButton = new Button("Cancelar"); // Crea un nuevo botón con el texto "Cancelar"
        cancelButton.setOnAction(e -> handleCancel()); // Establece el manejador de eventos del botón para llamar a handleCancel cuando se presiona

        Button resetButton = new Button("Restaurar Valores por Defecto"); // Crea un nuevo botón con el texto "Restaurar Valores por Defecto"
        resetButton.setOnAction(e -> resetToDefaults()); // Establece el manejador de eventos del botón para llamar a resetToDefaults cuando se presiona

        VBox buttonBox = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles para contener los botones
        buttonBox.getChildren().addAll(okButton, cancelButton, resetButton); // Agrega los tres botones al VBox de botones
        buttonBox.setPadding(new Insets(10, 0, 0, 0)); // Establece un margen interno solo en la parte superior (10 píxeles)

        root.getChildren().addAll(tabPane, buttonBox); // Agrega el TabPane y el VBox de botones al contenedor raíz

        Scene scene = new Scene(root, 600, 650); // Crea una nueva escena con el contenedor raíz, ancho 600 y altura 650 píxeles
        setScene(scene); // Establece la escena creada como la escena de este diálogo

        loadParameters(); // Llama al método para cargar los valores actuales de los parámetros en los campos de texto
    } // Cierre del constructor ParametersDialog

    private GridPane createGeneralPane() { // Método privado que crea y retorna un GridPane con los parámetros generales
        GridPane grid = new GridPane(); // Crea un nuevo GridPane para organizar los controles en cuadrícula
        grid.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados
        grid.setHgap(10); // Establece el espaciado horizontal entre columnas en 10 píxeles
        grid.setVgap(10); // Establece el espaciado vertical entre filas en 10 píxeles

        int row = 0; // Inicializa el contador de fila en 0

        grid.add(new Label("Duración de Simulación (minutos):"), 0, row); // Agrega una etiqueta en la columna 0 de la fila actual
        durationField = new TextField(); // Crea un nuevo campo de texto para la duración de la simulación
        grid.add(durationField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Semilla Aleatoria:"), 0, row); // Agrega una etiqueta para la semilla aleatoria en la columna 0 de la fila actual
        seedField = new TextField(); // Crea un nuevo campo de texto para la semilla aleatoria
        grid.add(seedField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Nota: 2160 min = 36 horas = 1.5 días"), 0, row, 2, 1); // Agrega una etiqueta informativa que ocupa 2 columnas en la fila actual

        return grid; // Retorna el GridPane configurado con los parámetros generales
    } // Cierre del método createGeneralPane

    private GridPane createCapacitiesPane() { // Método privado que crea y retorna un GridPane con los parámetros de capacidades
        GridPane grid = new GridPane(); // Crea un nuevo GridPane para organizar los controles en cuadrícula
        grid.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados
        grid.setHgap(10); // Establece el espaciado horizontal entre columnas en 10 píxeles
        grid.setVgap(10); // Establece el espaciado vertical entre filas en 10 píxeles

        int row = 0; // Inicializa el contador de fila en 0

        grid.add(new Label("Capacidad LAVADORA:"), 0, row); // Agrega una etiqueta para la capacidad de lavadora en la columna 0 de la fila actual
        lavadoraCapField = new TextField(); // Crea un nuevo campo de texto para la capacidad de lavadora
        grid.add(lavadoraCapField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Capacidad ALMACEN_PINTURA:"), 0, row); // Agrega una etiqueta para la capacidad del almacén de pintura en la columna 0 de la fila actual
        almacenPinturaCapField = new TextField(); // Crea un nuevo campo de texto para la capacidad del almacén de pintura
        grid.add(almacenPinturaCapField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Capacidad PINTURA:"), 0, row); // Agrega una etiqueta para la capacidad de pintura en la columna 0 de la fila actual
        pinturaCapField = new TextField(); // Crea un nuevo campo de texto para la capacidad de pintura
        grid.add(pinturaCapField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Capacidad ALMACEN_HORNO:"), 0, row); // Agrega una etiqueta para la capacidad del almacén del horno en la columna 0 de la fila actual
        almacenHornoCapField = new TextField(); // Crea un nuevo campo de texto para la capacidad del almacén del horno
        grid.add(almacenHornoCapField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Capacidad HORNO:"), 0, row); // Agrega una etiqueta para la capacidad del horno en la columna 0 de la fila actual
        hornoCapField = new TextField(); // Crea un nuevo campo de texto para la capacidad del horno
        grid.add(hornoCapField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Número de Estaciones INSPECCION:"), 0, row); // Agrega una etiqueta para el número de estaciones de inspección en la columna 0 de la fila actual
        inspeccionStationsField = new TextField(); // Crea un nuevo campo de texto para el número de estaciones de inspección
        grid.add(inspeccionStationsField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Operaciones por Pieza INSPECCION:"), 0, row); // Agrega una etiqueta para las operaciones por pieza en inspección en la columna 0 de la fila actual
        inspeccionOpsField = new TextField(); // Crea un nuevo campo de texto para las operaciones por pieza en inspección
        grid.add(inspeccionOpsField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        return grid; // Retorna el GridPane configurado con los parámetros de capacidades
    } // Cierre del método createCapacitiesPane

    private ScrollPane createDistributionsPane() { // Método privado que crea y retorna un ScrollPane conteniendo un GridPane con los parámetros de distribuciones
        GridPane grid = new GridPane(); // Crea un nuevo GridPane para organizar los controles en cuadrícula
        grid.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados
        grid.setHgap(10); // Establece el espaciado horizontal entre columnas en 10 píxeles
        grid.setVgap(10); // Establece el espaciado vertical entre filas en 10 píxeles

        int row = 0; // Inicializa el contador de fila en 0

        // Arribos
        grid.add(new Label("Arribos E(mean):"), 0, row); // Agrega una etiqueta para el parámetro de arribos en la columna 0 de la fila actual
        arrivalMeanField = new TextField(); // Crea un nuevo campo de texto para la media de arribos
        grid.add(arrivalMeanField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Exponencial: E(2))"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        // Transporte Recepción -> Lavadora
        grid.add(new Label("Transporte REC->LAV E(mean):"), 0, row); // Agrega una etiqueta para el transporte de recepción a lavadora en la columna 0 de la fila actual
        transportRecLavMeanField = new TextField(); // Crea un nuevo campo de texto para la media de transporte recepción-lavadora
        grid.add(transportRecLavMeanField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(E(3))"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        // Transporte Lavadora -> Almacén Pintura
        grid.add(new Label("Transporte LAV->ALM_PINT E(mean):"), 0, row); // Agrega una etiqueta para el transporte de lavadora a almacén de pintura en la columna 0 de la fila actual
        transportLavAlmMeanField = new TextField(); // Crea un nuevo campo de texto para la media de transporte lavadora-almacén
        grid.add(transportLavAlmMeanField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(E(2))"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        // Proceso Lavadora
        grid.add(new Label("Proceso LAVADORA N(mean):"), 0, row); // Agrega una etiqueta para la media del proceso de lavadora en la columna 0 de la fila actual
        lavadoraMeanField = new TextField(); // Crea un nuevo campo de texto para la media del proceso de lavadora
        grid.add(lavadoraMeanField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Normal: N(10, 2))"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        grid.add(new Label("Proceso LAVADORA N(stddev):"), 0, row); // Agrega una etiqueta para la desviación estándar del proceso de lavadora en la columna 0 de la fila actual
        lavadoraStdDevField = new TextField(); // Crea un nuevo campo de texto para la desviación estándar del proceso de lavadora
        grid.add(lavadoraStdDevField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        // Proceso Pintura
        grid.add(new Label("Proceso PINTURA T(min):"), 0, row); // Agrega una etiqueta para el tiempo mínimo del proceso de pintura en la columna 0 de la fila actual
        pinturaMinField = new TextField(); // Crea un nuevo campo de texto para el tiempo mínimo del proceso de pintura
        grid.add(pinturaMinField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Triangular: T(4, 8, 10))"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        grid.add(new Label("Proceso PINTURA T(mode):"), 0, row); // Agrega una etiqueta para la moda del proceso de pintura en la columna 0 de la fila actual
        pinturaModeField = new TextField(); // Crea un nuevo campo de texto para la moda del proceso de pintura
        grid.add(pinturaModeField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        grid.add(new Label("Proceso PINTURA T(max):"), 0, row); // Agrega una etiqueta para el tiempo máximo del proceso de pintura en la columna 0 de la fila actual
        pinturaMaxField = new TextField(); // Crea un nuevo campo de texto para el tiempo máximo del proceso de pintura
        grid.add(pinturaMaxField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        // Transporte Pintura -> Almacén Horno
        grid.add(new Label("Transporte PINT->ALM_HOR U(min):"), 0, row); // Agrega una etiqueta para el tiempo mínimo de transporte de pintura a almacén del horno en la columna 0 de la fila actual
        transportPintAlmMinField = new TextField(); // Crea un nuevo campo de texto para el tiempo mínimo de transporte pintura-almacén
        grid.add(transportPintAlmMinField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Uniforme: U(3.5, 1.5) = [2, 5])"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        grid.add(new Label("Transporte PINT->ALM_HOR U(max):"), 0, row); // Agrega una etiqueta para el tiempo máximo de transporte de pintura a almacén del horno en la columna 0 de la fila actual
        transportPintAlmMaxField = new TextField(); // Crea un nuevo campo de texto para el tiempo máximo de transporte pintura-almacén
        grid.add(transportPintAlmMaxField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        // Proceso Horno
        grid.add(new Label("Proceso HORNO U(min):"), 0, row); // Agrega una etiqueta para el tiempo mínimo del proceso del horno en la columna 0 de la fila actual
        hornoMinField = new TextField(); // Crea un nuevo campo de texto para el tiempo mínimo del proceso del horno
        grid.add(hornoMinField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Uniforme: U(3, 1) = [2, 4])"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        grid.add(new Label("Proceso HORNO U(max):"), 0, row); // Agrega una etiqueta para el tiempo máximo del proceso del horno en la columna 0 de la fila actual
        hornoMaxField = new TextField(); // Crea un nuevo campo de texto para el tiempo máximo del proceso del horno
        grid.add(hornoMaxField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        // Transporte Horno -> Inspección
        grid.add(new Label("Transporte HOR->INSP U(min):"), 0, row); // Agrega una etiqueta para el tiempo mínimo de transporte de horno a inspección en la columna 0 de la fila actual
        transportHorInspMinField = new TextField(); // Crea un nuevo campo de texto para el tiempo mínimo de transporte horno-inspección
        grid.add(transportHorInspMinField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(Uniforme: U(2, 1) = [1, 3])"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        grid.add(new Label("Transporte HOR->INSP U(max):"), 0, row); // Agrega una etiqueta para el tiempo máximo de transporte de horno a inspección en la columna 0 de la fila actual
        transportHorInspMaxField = new TextField(); // Crea un nuevo campo de texto para el tiempo máximo de transporte horno-inspección
        grid.add(transportHorInspMaxField, 1, row++); // Agrega el campo de texto en la columna 1 de la fila actual y luego incrementa row

        // Operaciones Inspección
        grid.add(new Label("Operación INSPECCION E(mean):"), 0, row); // Agrega una etiqueta para la media de cada operación de inspección en la columna 0 de la fila actual
        inspeccionMeanField = new TextField(); // Crea un nuevo campo de texto para la media de operación de inspección
        grid.add(inspeccionMeanField, 1, row); // Agrega el campo de texto en la columna 1 de la fila actual
        grid.add(new Label("(E(2) x 3 operaciones)"), 2, row++); // Agrega una etiqueta informativa en la columna 2 de la fila actual y luego incrementa row

        ScrollPane scrollPane = new ScrollPane(grid); // Crea un nuevo ScrollPane conteniendo el GridPane para permitir desplazamiento vertical si hay muchos parámetros
        scrollPane.setFitToWidth(true); // Establece que el contenido debe ajustarse al ancho del ScrollPane
        return scrollPane; // Retorna el ScrollPane configurado con los parámetros de distribuciones
    } // Cierre del método createDistributionsPane

    private void loadParameters() { // Método privado que carga los valores actuales de los parámetros en los campos de texto
        durationField.setText(String.valueOf(parameters.getSimulationDurationMinutes())); // Obtiene la duración de la simulación y la convierte a String para mostrarla en el campo de texto
        seedField.setText(String.valueOf(parameters.getBaseRandomSeed())); // Obtiene la semilla aleatoria y la convierte a String para mostrarla en el campo de texto

        lavadoraCapField.setText(String.valueOf(parameters.getLavadoraCapacity())); // Obtiene la capacidad de lavadora y la convierte a String para mostrarla en el campo de texto
        almacenPinturaCapField.setText(String.valueOf(parameters.getAlmacenPinturaCapacity())); // Obtiene la capacidad del almacén de pintura y la convierte a String para mostrarla en el campo de texto
        pinturaCapField.setText(String.valueOf(parameters.getPinturaCapacity())); // Obtiene la capacidad de pintura y la convierte a String para mostrarla en el campo de texto
        almacenHornoCapField.setText(String.valueOf(parameters.getAlmacenHornoCapacity())); // Obtiene la capacidad del almacén del horno y la convierte a String para mostrarla en el campo de texto
        hornoCapField.setText(String.valueOf(parameters.getHornoCapacity())); // Obtiene la capacidad del horno y la convierte a String para mostrarla en el campo de texto
        inspeccionStationsField.setText(String.valueOf(parameters.getInspeccionNumStations())); // Obtiene el número de estaciones de inspección y lo convierte a String para mostrarlo en el campo de texto
        inspeccionOpsField.setText(String.valueOf(parameters.getInspeccionOperationsPerPiece())); // Obtiene las operaciones por pieza en inspección y las convierte a String para mostrarlas en el campo de texto

        arrivalMeanField.setText(String.valueOf(parameters.getArrivalMeanTime())); // Obtiene la media de tiempo entre arribos y la convierte a String para mostrarla en el campo de texto
        transportRecLavMeanField.setText(String.valueOf(parameters.getTransportRecepcionLavadoraMean())); // Obtiene la media de transporte recepción-lavadora y la convierte a String para mostrarla en el campo de texto
        transportLavAlmMeanField.setText(String.valueOf(parameters.getTransportLavadoraAlmacenMean())); // Obtiene la media de transporte lavadora-almacén y la convierte a String para mostrarla en el campo de texto
        lavadoraMeanField.setText(String.valueOf(parameters.getLavadoraProcessMean())); // Obtiene la media del proceso de lavadora y la convierte a String para mostrarla en el campo de texto
        lavadoraStdDevField.setText(String.valueOf(parameters.getLavadoraProcessStdDev())); // Obtiene la desviación estándar del proceso de lavadora y la convierte a String para mostrarla en el campo de texto
        pinturaMinField.setText(String.valueOf(parameters.getPinturaProcessMin())); // Obtiene el tiempo mínimo del proceso de pintura y lo convierte a String para mostrarlo en el campo de texto
        pinturaModeField.setText(String.valueOf(parameters.getPinturaProcessMode())); // Obtiene la moda del proceso de pintura y la convierte a String para mostrarla en el campo de texto
        pinturaMaxField.setText(String.valueOf(parameters.getPinturaProcessMax())); // Obtiene el tiempo máximo del proceso de pintura y lo convierte a String para mostrarlo en el campo de texto
        transportPintAlmMinField.setText(String.valueOf(parameters.getTransportPinturaAlmacenMin())); // Obtiene el tiempo mínimo de transporte pintura-almacén y lo convierte a String para mostrarlo en el campo de texto
        transportPintAlmMaxField.setText(String.valueOf(parameters.getTransportPinturaAlmacenMax())); // Obtiene el tiempo máximo de transporte pintura-almacén y lo convierte a String para mostrarlo en el campo de texto
        hornoMinField.setText(String.valueOf(parameters.getHornoProcessMin())); // Obtiene el tiempo mínimo del proceso del horno y lo convierte a String para mostrarlo en el campo de texto
        hornoMaxField.setText(String.valueOf(parameters.getHornoProcessMax())); // Obtiene el tiempo máximo del proceso del horno y lo convierte a String para mostrarlo en el campo de texto
        transportHorInspMinField.setText(String.valueOf(parameters.getTransportHornoInspeccionMin())); // Obtiene el tiempo mínimo de transporte horno-inspección y lo convierte a String para mostrarlo en el campo de texto
        transportHorInspMaxField.setText(String.valueOf(parameters.getTransportHornoInspeccionMax())); // Obtiene el tiempo máximo de transporte horno-inspección y lo convierte a String para mostrarlo en el campo de texto
        inspeccionMeanField.setText(String.valueOf(parameters.getInspeccionOperationMean())); // Obtiene la media de cada operación de inspección y la convierte a String para mostrarla en el campo de texto
    } // Cierre del método loadParameters

    private void handleOk() { // Método privado que maneja el evento de presionar el botón "Aceptar"
        try { // Bloque try para capturar excepciones de conversión de valores
            // Validar y guardar los parámetros
            SimulationParameters newParams = new SimulationParameters(); // Crea una nueva instancia de SimulationParameters para validar los valores

            newParams.setSimulationDurationMinutes(Double.parseDouble(durationField.getText())); // Obtiene el texto del campo de duración, lo convierte a double y lo establece en los nuevos parámetros
            newParams.setBaseRandomSeed(Long.parseLong(seedField.getText())); // Obtiene el texto del campo de semilla, lo convierte a long y lo establece en los nuevos parámetros

            // Copiar todos los parámetros al objeto original
            parameters.setSimulationDurationMinutes(newParams.getSimulationDurationMinutes()); // Copia la duración validada al objeto de parámetros original
            parameters.setBaseRandomSeed(newParams.getBaseRandomSeed()); // Copia la semilla validada al objeto de parámetros original

            accepted = true; // Establece la bandera accepted como true indicando que el usuario aceptó los cambios
            close(); // Cierra el diálogo

        } catch (NumberFormatException e) { // Captura la excepción NumberFormatException si la conversión de algún valor falla
            Alert alert = new Alert(Alert.AlertType.ERROR); // Crea un nuevo diálogo de alerta de tipo ERROR
            alert.setTitle("Error de Validación"); // Establece el título de la alerta
            alert.setHeaderText("Valores inválidos"); // Establece el encabezado de la alerta
            alert.setContentText("Por favor, ingrese valores numéricos válidos."); // Establece el mensaje de contenido de la alerta
            alert.showAndWait(); // Muestra la alerta de forma modal y espera a que el usuario la cierre
        } // Cierre del bloque catch
    } // Cierre del método handleOk

    private void handleCancel() { // Método privado que maneja el evento de presionar el botón "Cancelar"
        accepted = false; // Establece la bandera accepted como false indicando que el usuario canceló sin aceptar cambios
        close(); // Cierra el diálogo
    } // Cierre del método handleCancel

    private void resetToDefaults() { // Método privado que restablece los parámetros a sus valores por defecto
        SimulationParameters defaults = new SimulationParameters(); // Crea una nueva instancia de SimulationParameters con valores por defecto
        parameters.setSimulationDurationMinutes(defaults.getSimulationDurationMinutes()); // Copia la duración por defecto al objeto de parámetros actual
        parameters.setBaseRandomSeed(defaults.getBaseRandomSeed()); // Copia la semilla por defecto al objeto de parámetros actual
        loadParameters(); // Llama al método loadParameters para actualizar los campos de texto con los valores por defecto
    } // Cierre del método resetToDefaults

    public boolean isAccepted() { // Método público getter que retorna si el usuario aceptó los cambios de tipo boolean
        return accepted; // Retorna el valor de la variable accepted
    } // Cierre del método isAccepted
} // Cierre de la clase ParametersDialog
