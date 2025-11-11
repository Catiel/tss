package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters para acceder a los parámetros de configuración de la simulación
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para especificar márgenes internos en los nodos
import javafx.scene.Scene; // Importa la clase Scene de JavaFX para crear una escena que contiene nodos de interfaz gráfica
import javafx.scene.control.Alert; // Importa la clase Alert de JavaFX para mostrar cuadros de diálogo de alerta
import javafx.scene.control.Button; // Importa la clase Button de JavaFX para crear botones interactivos
import javafx.scene.control.Label; // Importa la clase Label de JavaFX para mostrar texto no editable
import javafx.scene.control.ScrollPane; // Importa la clase ScrollPane de JavaFX para crear un panel desplazable
import javafx.scene.control.Tab; // Importa la clase Tab de JavaFX para crear pestañas en un TabPane
import javafx.scene.control.TabPane; // Importa la clase TabPane de JavaFX para crear un contenedor con múltiples pestañas
import javafx.scene.control.TextField; // Importa la clase TextField de JavaFX para crear campos de texto editable
import javafx.scene.layout.GridPane; // Importa la clase GridPane de JavaFX para crear un layout de cuadrícula
import javafx.scene.layout.HBox; // Importa la clase HBox de JavaFX para crear un contenedor horizontal
import javafx.scene.layout.Priority; // Importa la enumeración Priority de JavaFX para especificar prioridades de crecimiento
import javafx.scene.layout.Region; // Importa la clase Region de JavaFX para crear regiones de espaciado flexible
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para crear un contenedor vertical
import javafx.stage.Modality; // Importa la enumeración Modality de JavaFX para especificar tipos de modalidad de ventana
import javafx.stage.Stage; // Importa la clase Stage de JavaFX para crear ventanas secundarias

import java.util.Locale; // Importa la clase Locale de Java para especificar el idioma y formato regional

/** // Inicio del comentario Javadoc de la clase
 * Diálogo para editar los parámetros del sistema Multi-Engrane. // Descripción de la clase
 */ // Fin del comentario Javadoc
public class ParametersDialog extends Stage { // Declaración de la clase pública ParametersDialog que extiende Stage para crear una ventana de diálogo secundaria
    private final SimulationParameters parameters; // Variable privada final que almacena la referencia a los parámetros de la simulación
    private boolean accepted = false; // Variable privada booleana que indica si el usuario aceptó los cambios, inicializada en false

    // General
    private TextField durationField; // Variable privada que almacena el campo de texto para la duración de la simulación
    private TextField seedField; // Variable privada que almacena el campo de texto para la semilla aleatoria

    // Arribos y transportes
    private TextField arrivalMeanField; // Variable privada que almacena el campo de texto para el tiempo promedio entre arribos
    private TextField conveyor1TimeField; // Variable privada que almacena el campo de texto para el tiempo del primer conveyor
    private TextField conveyor2TimeField; // Variable privada que almacena el campo de texto para el tiempo del segundo conveyor
    private TextField transportWorkerField; // Variable privada que almacena el campo de texto para el tiempo de transporte de trabajadores

    // Capacidades
    private TextField conveyor1CapField; // Variable privada que almacena el campo de texto para la capacidad del primer conveyor
    private TextField almacenCapField; // Variable privada que almacena el campo de texto para la capacidad del almacén
    private TextField cortadoraCapField; // Variable privada que almacena el campo de texto para la capacidad de la cortadora
    private TextField tornoCapField; // Variable privada que almacena el campo de texto para la capacidad del torno
    private TextField conveyor2CapField; // Variable privada que almacena el campo de texto para la capacidad del segundo conveyor
    private TextField fresadoraCapField; // Variable privada que almacena el campo de texto para la capacidad de la fresadora
    private TextField almacen2CapField; // Variable privada que almacena el campo de texto para la capacidad del segundo almacén
    private TextField pinturaCapField; // Variable privada que almacena el campo de texto para la capacidad de pintura
    private TextField inspeccion1CapField; // Variable privada que almacena el campo de texto para la capacidad de la primera inspección
    private TextField inspeccion2CapField; // Variable privada que almacena el campo de texto para la capacidad de la segunda inspección
    private TextField empaqueCapField; // Variable privada que almacena el campo de texto para la capacidad de empaque
    private TextField embarqueCapField; // Variable privada que almacena el campo de texto para la capacidad de embarque

    // Procesos
    private TextField almacenMeanField; // Variable privada que almacena el campo de texto para la media del proceso en almacén
    private TextField almacenStdField; // Variable privada que almacena el campo de texto para la desviación estándar del proceso en almacén
    private TextField cortadoraMeanField; // Variable privada que almacena el campo de texto para la media del proceso en cortadora
    private TextField tornoMeanField; // Variable privada que almacena el campo de texto para la media del proceso en torno
    private TextField tornoStdField; // Variable privada que almacena el campo de texto para la desviación estándar del proceso en torno
    private TextField fresadoraMeanField; // Variable privada que almacena el campo de texto para la media del proceso en fresadora
    private TextField almacen2MeanField; // Variable privada que almacena el campo de texto para la media del proceso en almacén 2
    private TextField almacen2StdField; // Variable privada que almacena el campo de texto para la desviación estándar del proceso en almacén 2
    private TextField pinturaMeanField; // Variable privada que almacena el campo de texto para la media del proceso en pintura
    private TextField inspeccion1MeanField; // Variable privada que almacena el campo de texto para la media del proceso en inspección 1
    private TextField inspeccion1StdField; // Variable privada que almacena el campo de texto para la desviación estándar del proceso en inspección 1
    private TextField inspeccion2MeanField; // Variable privada que almacena el campo de texto para la media del proceso en inspección 2
    private TextField empaqueMeanField; // Variable privada que almacena el campo de texto para la media del proceso en empaque
    private TextField empaqueStdField; // Variable privada que almacena el campo de texto para la desviación estándar del proceso en empaque
    private TextField embarqueMeanField; // Variable privada que almacena el campo de texto para la media del proceso en embarque

    // Probabilidades
    private TextField probEmpaqueField; // Variable privada que almacena el campo de texto para la probabilidad de ir a empaque desde inspección 1
    private TextField probInspeccion2Field; // Variable privada que almacena el campo de texto para la probabilidad de ir a inspección 2 desde inspección 1

    public ParametersDialog(SimulationParameters params) { // Constructor público que inicializa el diálogo de parámetros recibiendo los parámetros de simulación como parámetro
        this.parameters = params; // Asigna los parámetros recibidos a la variable de instancia final

        setTitle("Parámetros de Simulación - Multi-Engrane"); // Establece el título de la ventana del diálogo
        initModality(Modality.APPLICATION_MODAL); // Establece la modalidad de la ventana como modal de aplicación (bloquea otras ventanas)

        TabPane tabPane = new TabPane(); // Crea un nuevo contenedor de pestañas
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Establece que las pestañas no pueden cerrarse por el usuario

        tabPane.getTabs().add(new Tab("General", createGeneralPane())); // Agrega una pestaña "General" con el panel general
        tabPane.getTabs().add(new Tab("Arribos y Transportes", createArrivalPane())); // Agrega una pestaña "Arribos y Transportes" con el panel de arribos
        tabPane.getTabs().add(new Tab("Capacidades", createCapacitiesPane())); // Agrega una pestaña "Capacidades" con el panel de capacidades
        tabPane.getTabs().add(new Tab("Procesos", createProcessesPane())); // Agrega una pestaña "Procesos" con el panel de procesos
        tabPane.getTabs().add(new Tab("Probabilidades", createProbabilitiesPane())); // Agrega una pestaña "Probabilidades" con el panel de probabilidades

        Button okButton = new Button("Aceptar"); // Crea un botón con el texto "Aceptar"
        okButton.setOnAction(e -> handleOk()); // Establece el manejador de eventos cuando se hace clic en el botón Aceptar

        Button cancelButton = new Button("Cancelar"); // Crea un botón con el texto "Cancelar"
        cancelButton.setOnAction(e -> handleCancel()); // Establece el manejador de eventos cuando se hace clic en el botón Cancelar

        Button defaultsButton = new Button("Restaurar por defecto"); // Crea un botón con el texto "Restaurar por defecto"
        defaultsButton.setOnAction(e -> resetToDefaults()); // Establece el manejador de eventos cuando se hace clic en el botón Restaurar por defecto

        Region spacer = new Region(); // Crea una región de espaciado flexible
        HBox.setHgrow(spacer, Priority.ALWAYS); // Establece que la región crece horizontalmente con prioridad máxima

        HBox buttonRow = new HBox(10, defaultsButton, spacer, cancelButton, okButton); // Crea un contenedor horizontal con los botones y un espaciador
        buttonRow.setPadding(new Insets(10, 0, 0, 0)); // Establece márgenes internos del contenedor de botones

        VBox root = new VBox(12, tabPane, buttonRow); // Crea el contenedor raíz vertical con el panel de pestañas y la fila de botones
        root.setPadding(new Insets(15)); // Establece márgenes internos del contenedor raíz

        Scene scene = new Scene(root, 720, 640); // Crea una escena con el contenedor raíz y dimensiones de 720x640
        setScene(scene); // Establece la escena en esta ventana

        populateFields(parameters); // Llama al método para poblar los campos de texto con los valores actuales de los parámetros
    } // Cierre del constructor ParametersDialog

    private GridPane createGeneralPane() { // Método privado que crea y retorna el panel general de parámetros recibiendo GridPane como retorno
        GridPane grid = buildGrid(); // Crea una cuadrícula nueva llamando al método buildGrid
        int row = 0; // Inicializa el contador de filas en 0

        durationField = createNumericField(); // Crea un nuevo campo de texto numérico para la duración
        addLabeledField(grid, row++, "Duración (min)", durationField, "Total de minutos simulados (ej. 3600 = 60 h)"); // Agrega el campo de duración a la cuadrícula con etiqueta y tooltip

        seedField = createNumericField(); // Crea un nuevo campo de texto numérico para la semilla aleatoria
        addLabeledField(grid, row, "Semilla aleatoria", seedField, "Número entero para inicializar los generadores"); // Agrega el campo de semilla a la cuadrícula con etiqueta y tooltip

        return grid; // Retorna la cuadrícula con los campos de parámetros generales
    } // Cierre del método createGeneralPane

    private GridPane createArrivalPane() { // Método privado que crea y retorna el panel de parámetros de arribos y transportes recibiendo GridPane como retorno
        GridPane grid = buildGrid(); // Crea una cuadrícula nueva llamando al método buildGrid
        int row = 0; // Inicializa el contador de filas en 0

        arrivalMeanField = createNumericField(); // Crea un nuevo campo de texto numérico para la media de arribos
        addLabeledField(grid, row++, "Arribos - media (min)", arrivalMeanField, "Tiempo medio entre arribos"); // Agrega el campo a la cuadrícula

        conveyor1TimeField = createNumericField(); // Crea un nuevo campo de texto numérico para el tiempo de conveyor 1
        addLabeledField(grid, row++, "Tiempo CONVEYOR 1 (min)", conveyor1TimeField, "Tiempo fijo de salida desde CONVEYOR 1"); // Agrega el campo a la cuadrícula

        conveyor2TimeField = createNumericField(); // Crea un nuevo campo de texto numérico para el tiempo de conveyor 2
        addLabeledField(grid, row++, "Tiempo CONVEYOR 2 (min)", conveyor2TimeField, "Tiempo fijo de salida desde CONVEYOR 2"); // Agrega el campo a la cuadrícula

        transportWorkerField = createNumericField(); // Crea un nuevo campo de texto numérico para el tiempo de transporte de trabajadores
        addLabeledField(grid, row, "Tiempo transporte trabajador (min)", transportWorkerField, "Tiempo promedio de los recursos de transporte"); // Agrega el campo a la cuadrícula

        return grid; // Retorna la cuadrícula con los campos de parámetros de arribos y transportes
    } // Cierre del método createArrivalPane

    private ScrollPane createCapacitiesPane() { // Método privado que crea y retorna el panel de capacidades recibiendo ScrollPane como retorno
        GridPane grid = buildGrid(); // Crea una cuadrícula nueva llamando al método buildGrid
        int row = 0; // Inicializa el contador de filas en 0

        conveyor1CapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de conveyor 1
        addLabeledField(grid, row++, "CONVEYOR 1", conveyor1CapField, "Use ∞ para capacidad ilimitada"); // Agrega el campo a la cuadrícula

        almacenCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad del almacén
        addLabeledField(grid, row++, "ALMACEN 1", almacenCapField, "Capacidad máxima de ALMACEN"); // Agrega el campo a la cuadrícula

        cortadoraCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de la cortadora
        addLabeledField(grid, row++, "CORTADORA", cortadoraCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        tornoCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad del torno
        addLabeledField(grid, row++, "TORNO", tornoCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        conveyor2CapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de conveyor 2
        addLabeledField(grid, row++, "CONVEYOR 2", conveyor2CapField, "Use ∞ para capacidad ilimitada"); // Agrega el campo a la cuadrícula

        fresadoraCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de la fresadora
        addLabeledField(grid, row++, "FRESADORA", fresadoraCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        almacen2CapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad del almacén 2
        addLabeledField(grid, row++, "ALMACEN 2", almacen2CapField, null); // Agrega el campo a la cuadrícula sin tooltip

        pinturaCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de pintura
        addLabeledField(grid, row++, "PINTURA", pinturaCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        inspeccion1CapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de inspección 1
        addLabeledField(grid, row++, "INSPECCION 1", inspeccion1CapField, null); // Agrega el campo a la cuadrícula sin tooltip

        inspeccion2CapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de inspección 2
        addLabeledField(grid, row++, "INSPECCION 2", inspeccion2CapField, null); // Agrega el campo a la cuadrícula sin tooltip

        empaqueCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de empaque
        addLabeledField(grid, row++, "EMPAQUE", empaqueCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        embarqueCapField = createNumericField(); // Crea un nuevo campo de texto numérico para la capacidad de embarque
        addLabeledField(grid, row, "EMBARQUE", embarqueCapField, null); // Agrega el campo a la cuadrícula sin tooltip

        return wrap(grid); // Retorna la cuadrícula envuelta en un ScrollPane
    } // Cierre del método createCapacitiesPane

    private ScrollPane createProcessesPane() { // Método privado que crea y retorna el panel de parámetros de procesos recibiendo ScrollPane como retorno
        GridPane grid = buildGrid(); // Crea una cuadrícula nueva llamando al método buildGrid
        int row = 0; // Inicializa el contador de filas en 0

        row = addProcessSection(grid, row, "ALMACEN", almacenMeanField = createNumericField(), almacenStdField = createNumericField()); // Agrega una sección de proceso para ALMACEN
        row = addProcessSection(grid, row, "CORTADORA (media exponencial)", cortadoraMeanField = createNumericField(), null); // Agrega una sección de proceso para CORTADORA sin desviación
        row = addProcessSection(grid, row, "TORNO", tornoMeanField = createNumericField(), tornoStdField = createNumericField()); // Agrega una sección de proceso para TORNO
        row = addProcessSection(grid, row, "FRESADORA (media exponencial)", fresadoraMeanField = createNumericField(), null); // Agrega una sección de proceso para FRESADORA sin desviación
        row = addProcessSection(grid, row, "ALMACEN 2", almacen2MeanField = createNumericField(), almacen2StdField = createNumericField()); // Agrega una sección de proceso para ALMACEN 2
        row = addProcessSection(grid, row, "PINTURA (media exponencial)", pinturaMeanField = createNumericField(), null); // Agrega una sección de proceso para PINTURA sin desviación
        row = addProcessSection(grid, row, "INSPECCION 1", inspeccion1MeanField = createNumericField(), inspeccion1StdField = createNumericField()); // Agrega una sección de proceso para INSPECCION 1
        row = addProcessSection(grid, row, "INSPECCION 2 (media exponencial)", inspeccion2MeanField = createNumericField(), null); // Agrega una sección de proceso para INSPECCION 2 sin desviación
        row = addProcessSection(grid, row, "EMPAQUE", empaqueMeanField = createNumericField(), empaqueStdField = createNumericField()); // Agrega una sección de proceso para EMPAQUE
        addProcessSection(grid, row, "EMBARQUE (media exponencial)", embarqueMeanField = createNumericField(), null); // Agrega una sección de proceso para EMBARQUE sin desviación

        return wrap(grid); // Retorna la cuadrícula envuelta en un ScrollPane
    } // Cierre del método createProcessesPane

    private GridPane createProbabilitiesPane() { // Método privado que crea y retorna el panel de probabilidades de routing recibiendo GridPane como retorno
        GridPane grid = buildGrid(); // Crea una cuadrícula nueva llamando al método buildGrid
        int row = 0; // Inicializa el contador de filas en 0

        probEmpaqueField = createNumericField(); // Crea un nuevo campo de texto numérico para la probabilidad a empaque
        addLabeledField(grid, row++, "Prob. INSPECCION 1 → EMPAQUE", probEmpaqueField, "Valor entre 0 y 1"); // Agrega el campo a la cuadrícula

        probInspeccion2Field = createNumericField(); // Crea un nuevo campo de texto numérico para la probabilidad a inspección 2
        addLabeledField(grid, row, "Prob. INSPECCION 1 → INSPECCION 2", probInspeccion2Field, "Valor entre 0 y 1 (ambas deben sumar 1)"); // Agrega el campo a la cuadrícula

        return grid; // Retorna la cuadrícula con los campos de probabilidades
    } // Cierre del método createProbabilitiesPane

    private void populateFields(SimulationParameters source) { // Método privado que llena todos los campos de texto con los valores actuales de los parámetros recibiendo SimulationParameters como parámetro
        durationField.setText(formatDouble(source.getSimulationDurationMinutes())); // Establece el valor de duración formateado en el campo
        seedField.setText(Long.toString(source.getBaseRandomSeed())); // Establece el valor de semilla en el campo

        arrivalMeanField.setText(formatDouble(source.getArrivalMeanTime())); // Establece el valor de tiempo medio de arribos en el campo
        conveyor1TimeField.setText(formatDouble(source.getConveyor1Time())); // Establece el valor de tiempo de conveyor 1 en el campo
        conveyor2TimeField.setText(formatDouble(source.getConveyor2Time())); // Establece el valor de tiempo de conveyor 2 en el campo
        transportWorkerField.setText(formatDouble(source.getTransportWorkerTime())); // Establece el valor de tiempo de transporte en el campo

        conveyor1CapField.setText(formatCapacity(source.getConveyor1Capacity())); // Establece el valor de capacidad de conveyor 1 en el campo
        almacenCapField.setText(formatCapacity(source.getAlmacenCapacity())); // Establece el valor de capacidad del almacén en el campo
        cortadoraCapField.setText(formatCapacity(source.getCortadoraCapacity())); // Establece el valor de capacidad de cortadora en el campo
        tornoCapField.setText(formatCapacity(source.getTornoCapacity())); // Establece el valor de capacidad del torno en el campo
        conveyor2CapField.setText(formatCapacity(source.getConveyor2Capacity())); // Establece el valor de capacidad de conveyor 2 en el campo
        fresadoraCapField.setText(formatCapacity(source.getFresadoraCapacity())); // Establece el valor de capacidad de fresadora en el campo
        almacen2CapField.setText(formatCapacity(source.getAlmacen2Capacity())); // Establece el valor de capacidad del almacén 2 en el campo
        pinturaCapField.setText(formatCapacity(source.getPinturaCapacity())); // Establece el valor de capacidad de pintura en el campo
        inspeccion1CapField.setText(formatCapacity(source.getInspeccion1Capacity())); // Establece el valor de capacidad de inspección 1 en el campo
        inspeccion2CapField.setText(formatCapacity(source.getInspeccion2Capacity())); // Establece el valor de capacidad de inspección 2 en el campo
        empaqueCapField.setText(formatCapacity(source.getEmpaqueCapacity())); // Establece el valor de capacidad de empaque en el campo
        embarqueCapField.setText(formatCapacity(source.getEmbarqueCapacity())); // Establece el valor de capacidad de embarque en el campo

        almacenMeanField.setText(formatDouble(source.getAlmacenProcessMean())); // Establece la media del proceso de almacén en el campo
        almacenStdField.setText(formatDouble(source.getAlmacenProcessStdDev())); // Establece la desviación estándar del proceso de almacén en el campo
        cortadoraMeanField.setText(formatDouble(source.getCortadoraProcessMean())); // Establece la media del proceso de cortadora en el campo
        tornoMeanField.setText(formatDouble(source.getTornoProcessMean())); // Establece la media del proceso del torno en el campo
        tornoStdField.setText(formatDouble(source.getTornoProcessStdDev())); // Establece la desviación estándar del proceso del torno en el campo
        fresadoraMeanField.setText(formatDouble(source.getFresadoraProcessMean())); // Establece la media del proceso de fresadora en el campo
        almacen2MeanField.setText(formatDouble(source.getAlmacen2ProcessMean())); // Establece la media del proceso del almacén 2 en el campo
        almacen2StdField.setText(formatDouble(source.getAlmacen2ProcessStdDev())); // Establece la desviación estándar del proceso del almacén 2 en el campo
        pinturaMeanField.setText(formatDouble(source.getPinturaProcessMean())); // Establece la media del proceso de pintura en el campo
        inspeccion1MeanField.setText(formatDouble(source.getInspeccion1ProcessMean())); // Establece la media del proceso de inspección 1 en el campo
        inspeccion1StdField.setText(formatDouble(source.getInspeccion1ProcessStdDev())); // Establece la desviación estándar del proceso de inspección 1 en el campo
        inspeccion2MeanField.setText(formatDouble(source.getInspeccion2ProcessMean())); // Establece la media del proceso de inspección 2 en el campo
        empaqueMeanField.setText(formatDouble(source.getEmpaqueProcessMean())); // Establece la media del proceso de empaque en el campo
        empaqueStdField.setText(formatDouble(source.getEmpaqueProcessStdDev())); // Establece la desviación estándar del proceso de empaque en el campo
        embarqueMeanField.setText(formatDouble(source.getEmbarqueProcessMean())); // Establece la media del proceso de embarque en el campo

        probEmpaqueField.setText(formatDouble(source.getInspeccion1ToEmpaqueProb())); // Establece la probabilidad a empaque en el campo
        probInspeccion2Field.setText(formatDouble(source.getInspeccion1ToInspeccion2Prob())); // Establece la probabilidad a inspección 2 en el campo
    } // Cierre del método populateFields

    private void handleOk() { // Método privado que maneja la aceptación de cambios cuando se hace clic en Aceptar sin recibir parámetros
        try { // Bloque try para capturar excepciones de validación
            double duration = parsePositiveDouble(durationField, "Duración de simulación", false); // Parsea y valida la duración
            long seed = parseLong(seedField, "Semilla aleatoria"); // Parsea y valida la semilla aleatoria

            double arrivalMean = parsePositiveDouble(arrivalMeanField, "Arribos - media", false); // Parsea y valida la media de arribos
            double conveyor1Time = parsePositiveDouble(conveyor1TimeField, "Tiempo CONVEYOR 1", false); // Parsea y valida el tiempo de conveyor 1
            double conveyor2Time = parsePositiveDouble(conveyor2TimeField, "Tiempo CONVEYOR 2", false); // Parsea y valida el tiempo de conveyor 2
            double transportWorker = parsePositiveDouble(transportWorkerField, "Tiempo de transporte", false); // Parsea y valida el tiempo de transporte

            int conveyor1Cap = parseCapacity(conveyor1CapField, "Capacidad CONVEYOR 1", true, 1); // Parsea y valida la capacidad de conveyor 1 (permite infinito)
            int almacenCap = parseCapacity(almacenCapField, "Capacidad ALMACEN 1", false, 1); // Parsea y valida la capacidad del almacén
            int cortadoraCap = parseCapacity(cortadoraCapField, "Capacidad CORTADORA", false, 1); // Parsea y valida la capacidad de cortadora
            int tornoCap = parseCapacity(tornoCapField, "Capacidad TORNO", false, 1); // Parsea y valida la capacidad del torno
            int conveyor2Cap = parseCapacity(conveyor2CapField, "Capacidad CONVEYOR 2", true, 1); // Parsea y valida la capacidad de conveyor 2 (permite infinito)
            int fresadoraCap = parseCapacity(fresadoraCapField, "Capacidad FRESADORA", false, 1); // Parsea y valida la capacidad de fresadora
            int almacen2Cap = parseCapacity(almacen2CapField, "Capacidad ALMACEN 2", false, 1); // Parsea y valida la capacidad del almacén 2
            int pinturaCap = parseCapacity(pinturaCapField, "Capacidad PINTURA", false, 1); // Parsea y valida la capacidad de pintura
            int inspeccion1Cap = parseCapacity(inspeccion1CapField, "Capacidad INSPECCION 1", false, 1); // Parsea y valida la capacidad de inspección 1
            int inspeccion2Cap = parseCapacity(inspeccion2CapField, "Capacidad INSPECCION 2", false, 1); // Parsea y valida la capacidad de inspección 2
            int empaqueCap = parseCapacity(empaqueCapField, "Capacidad EMPAQUE", false, 1); // Parsea y valida la capacidad de empaque
            int embarqueCap = parseCapacity(embarqueCapField, "Capacidad EMBARQUE", false, 1); // Parsea y valida la capacidad de embarque

            double almacenMean = parsePositiveDouble(almacenMeanField, "Proceso ALMACEN - media", false); // Parsea y valida la media del proceso de almacén
            double almacenStd = parsePositiveDouble(almacenStdField, "Proceso ALMACEN - desviación", true); // Parsea y valida la desviación estándar (permite 0)
            double cortadoraMean = parsePositiveDouble(cortadoraMeanField, "Proceso CORTADORA - media", false); // Parsea y valida la media del proceso de cortadora
            double tornoMean = parsePositiveDouble(tornoMeanField, "Proceso TORNO - media", false); // Parsea y valida la media del proceso del torno
            double tornoStd = parsePositiveDouble(tornoStdField, "Proceso TORNO - desviación", true); // Parsea y valida la desviación estándar del torno (permite 0)
            double fresadoraMean = parsePositiveDouble(fresadoraMeanField, "Proceso FRESADORA - media", false); // Parsea y valida la media del proceso de fresadora
            double almacen2Mean = parsePositiveDouble(almacen2MeanField, "Proceso ALMACEN 2 - media", false); // Parsea y valida la media del proceso del almacén 2
            double almacen2Std = parsePositiveDouble(almacen2StdField, "Proceso ALMACEN 2 - desviación", true); // Parsea y valida la desviación estándar del almacén 2 (permite 0)
            double pinturaMean = parsePositiveDouble(pinturaMeanField, "Proceso PINTURA - media", false); // Parsea y valida la media del proceso de pintura
            double inspeccion1Mean = parsePositiveDouble(inspeccion1MeanField, "Proceso INSPECCION 1 - media", false); // Parsea y valida la media del proceso de inspección 1
            double inspeccion1Std = parsePositiveDouble(inspeccion1StdField, "Proceso INSPECCION 1 - desviación", true); // Parsea y valida la desviación estándar de inspección 1 (permite 0)
            double inspeccion2Mean = parsePositiveDouble(inspeccion2MeanField, "Proceso INSPECCION 2 - media", false); // Parsea y valida la media del proceso de inspección 2
            double empaqueMean = parsePositiveDouble(empaqueMeanField, "Proceso EMPAQUE - media", false); // Parsea y valida la media del proceso de empaque
            double empaqueStd = parsePositiveDouble(empaqueStdField, "Proceso EMPAQUE - desviación", true); // Parsea y valida la desviación estándar de empaque (permite 0)
            double embarqueMean = parsePositiveDouble(embarqueMeanField, "Proceso EMBARQUE - media", false); // Parsea y valida la media del proceso de embarque

            double probEmpaque = parseProbability(probEmpaqueField, "Probabilidad a EMPAQUE"); // Parsea y valida la probabilidad a empaque (0-1)
            double probInspeccion2 = parseProbability(probInspeccion2Field, "Probabilidad a INSPECCION 2"); // Parsea y valida la probabilidad a inspección 2 (0-1)
            if (Math.abs((probEmpaque + probInspeccion2) - 1.0) > 1e-6) { // Condición que verifica si las probabilidades suman aproximadamente 1.0
                throw new IllegalArgumentException("Las probabilidades de INSPECCION 1 deben sumar 1.0"); // Lanza excepción si las probabilidades no suman 1.0
            } // Cierre del bloque condicional if

            parameters.setSimulationDurationMinutes(duration); // Establece la duración en los parámetros
            parameters.setBaseRandomSeed(seed); // Establece la semilla aleatoria en los parámetros

            parameters.setArrivalMeanTime(arrivalMean); // Establece la media de arribos en los parámetros
            parameters.setConveyor1Time(conveyor1Time); // Establece el tiempo de conveyor 1 en los parámetros
            parameters.setConveyor2Time(conveyor2Time); // Establece el tiempo de conveyor 2 en los parámetros
            parameters.setTransportWorkerTime(transportWorker); // Establece el tiempo de transporte en los parámetros

            parameters.setConveyor1Capacity(conveyor1Cap); // Establece la capacidad de conveyor 1 en los parámetros
            parameters.setAlmacenCapacity(almacenCap); // Establece la capacidad del almacén en los parámetros
            parameters.setCortadoraCapacity(cortadoraCap); // Establece la capacidad de cortadora en los parámetros
            parameters.setTornoCapacity(tornoCap); // Establece la capacidad del torno en los parámetros
            parameters.setConveyor2Capacity(conveyor2Cap); // Establece la capacidad de conveyor 2 en los parámetros
            parameters.setFresadoraCapacity(fresadoraCap); // Establece la capacidad de fresadora en los parámetros
            parameters.setAlmacen2Capacity(almacen2Cap); // Establece la capacidad del almacén 2 en los parámetros
            parameters.setPinturaCapacity(pinturaCap); // Establece la capacidad de pintura en los parámetros
            parameters.setInspeccion1Capacity(inspeccion1Cap); // Establece la capacidad de inspección 1 en los parámetros
            parameters.setInspeccion2Capacity(inspeccion2Cap); // Establece la capacidad de inspección 2 en los parámetros
            parameters.setEmpaqueCapacity(empaqueCap); // Establece la capacidad de empaque en los parámetros
            parameters.setEmbarqueCapacity(embarqueCap); // Establece la capacidad de embarque en los parámetros

            parameters.setAlmacenProcessMean(almacenMean); // Establece la media del proceso de almacén en los parámetros
            parameters.setAlmacenProcessStdDev(almacenStd); // Establece la desviación estándar del almacén en los parámetros
            parameters.setCortadoraProcessMean(cortadoraMean); // Establece la media del proceso de cortadora en los parámetros
            parameters.setTornoProcessMean(tornoMean); // Establece la media del proceso del torno en los parámetros
            parameters.setTornoProcessStdDev(tornoStd); // Establece la desviación estándar del torno en los parámetros
            parameters.setFresadoraProcessMean(fresadoraMean); // Establece la media del proceso de fresadora en los parámetros
            parameters.setAlmacen2ProcessMean(almacen2Mean); // Establece la media del proceso del almacén 2 en los parámetros
            parameters.setAlmacen2ProcessStdDev(almacen2Std); // Establece la desviación estándar del almacén 2 en los parámetros
            parameters.setPinturaProcessMean(pinturaMean); // Establece la media del proceso de pintura en los parámetros
            parameters.setInspeccion1ProcessMean(inspeccion1Mean); // Establece la media del proceso de inspección 1 en los parámetros
            parameters.setInspeccion1ProcessStdDev(inspeccion1Std); // Establece la desviación estándar de inspección 1 en los parámetros
            parameters.setInspeccion2ProcessMean(inspeccion2Mean); // Establece la media del proceso de inspección 2 en los parámetros
            parameters.setEmpaqueProcessMean(empaqueMean); // Establece la media del proceso de empaque en los parámetros
            parameters.setEmpaqueProcessStdDev(empaqueStd); // Establece la desviación estándar de empaque en los parámetros
            parameters.setEmbarqueProcessMean(embarqueMean); // Establece la media del proceso de embarque en los parámetros

            parameters.setInspeccion1ToEmpaqueProb(probEmpaque); // Establece la probabilidad a empaque en los parámetros
            parameters.setInspeccion1ToInspeccion2Prob(probInspeccion2); // Establece la probabilidad a inspección 2 en los parámetros

            accepted = true; // Marca los cambios como aceptados
            close(); // Cierra la ventana del diálogo
        } catch (IllegalArgumentException ex) { // Captura excepciones de validación de argumentos ilegales
            showValidationError(ex.getMessage()); // Muestra el mensaje de error de validación
        } // Cierre del bloque catch
    } // Cierre del método handleOk

    private void handleCancel() { // Método privado que maneja la cancelación sin recibir parámetros
        accepted = false; // Marca los cambios como no aceptados
        close(); // Cierra la ventana del diálogo
    } // Cierre del método handleCancel

    private void resetToDefaults() { // Método privado que restaura los valores por defecto en los campos sin recibir parámetros
        populateFields(new SimulationParameters()); // Llama al método populateFields con una nueva instancia de parámetros (valores por defecto)
    } // Cierre del método resetToDefaults

    public boolean isAccepted() { // Método público que retorna si el usuario aceptó los cambios de tipo boolean
        return accepted; // Retorna el valor de la variable accepted
    } // Cierre del método isAccepted

    private GridPane buildGrid() { // Método privado que crea una cuadrícula pre-configurada recibiendo GridPane como retorno
        GridPane grid = new GridPane(); // Crea una nueva instancia de GridPane
        grid.setHgap(10); // Establece el espaciado horizontal entre columnas en 10 píxeles
        grid.setVgap(8); // Establece el espaciado vertical entre filas en 8 píxeles
        grid.setPadding(new Insets(10)); // Establece márgenes internos de 10 píxeles
        return grid; // Retorna la cuadrícula configurada
    } // Cierre del método buildGrid

    private TextField createNumericField() { // Método privado que crea un campo de texto numérico recibiendo TextField como retorno
        TextField field = new TextField(); // Crea una nueva instancia de TextField
        field.setMaxWidth(Double.MAX_VALUE); // Establece el ancho máximo del campo a infinito para que se expanda
        return field; // Retorna el campo de texto configurado
    } // Cierre del método createNumericField

    private void addLabeledField(GridPane grid, int row, String labelText, TextField field, String tooltip) { // Método privado que agrega una etiqueta y un campo a la cuadrícula recibiendo los parámetros especificados sin retorno
        Label label = new Label(labelText + ":"); // Crea una etiqueta con el texto recibido más dos puntos
        grid.add(label, 0, row); // Agrega la etiqueta en la columna 0 de la fila especificada
        grid.add(field, 1, row); // Agrega el campo en la columna 1 de la fila especificada
        GridPane.setHgrow(field, Priority.ALWAYS); // Establece que el campo crece horizontalmente con prioridad máxima
        if (tooltip != null) { // Condición que verifica si el tooltip no es null
            label.setTooltip(new javafx.scene.control.Tooltip(tooltip)); // Establece el tooltip de la etiqueta
            field.setTooltip(new javafx.scene.control.Tooltip(tooltip)); // Establece el tooltip del campo
        } // Cierre del bloque condicional if
    } // Cierre del método addLabeledField

    private int addProcessSection(GridPane grid, int row, String title, TextField meanField, TextField stdField) { // Método privado que agrega una sección de proceso a la cuadrícula recibiendo los parámetros especificados y retornando el nuevo contador de filas
        Label header = new Label(title); // Crea una etiqueta de encabezado con el título
        header.setStyle("-fx-font-weight: bold; -fx-padding: 12 0 0 0;"); // Establece el estilo del encabezado como negrita con márgenes
        grid.add(header, 0, row++, 2, 1); // Agrega el encabezado a la cuadrícula ocupando 2 columnas e incrementa el contador de filas

        addLabeledField(grid, row++, "  Media (min)", meanField, null); // Agrega el campo de media con indentación
        if (stdField != null) { // Condición que verifica si el campo de desviación no es null
            addLabeledField(grid, row++, "  Desviación (min)", stdField, "Ingrese 0 si desea un valor determinístico"); // Agrega el campo de desviación con tooltip
        } // Cierre del bloque condicional if
        return row; // Retorna el nuevo contador de filas
    } // Cierre del método addProcessSection

    private ScrollPane wrap(GridPane grid) { // Método privado que envuelve una cuadrícula en un ScrollPane recibiendo GridPane como parámetro y retornando ScrollPane
        ScrollPane scrollPane = new ScrollPane(grid); // Crea un nuevo ScrollPane con la cuadrícula como contenido
        scrollPane.setFitToWidth(true); // Establece que el contenido se ajuste al ancho del ScrollPane
        scrollPane.setFitToHeight(true); // Establece que el contenido se ajuste a la altura del ScrollPane
        return scrollPane; // Retorna el ScrollPane configurado
    } // Cierre del método wrap

    private String formatDouble(double value) { // Método privado que formatea un número double a una cadena recibiendo el valor como parámetro y retornando String
        String formatted = String.format(Locale.US, "%.4f", value); // Formatea el valor con 4 decimales usando locale US
        while (formatted.contains(".") && (formatted.endsWith("0") || formatted.endsWith("."))) { // Bucle while que elimina ceros finales y puntos decimales
            formatted = formatted.substring(0, formatted.length() - 1); // Elimina el último carácter
        } // Cierre del bucle while
        return formatted; // Retorna la cadena formateada
    } // Cierre del método formatDouble

    private String formatCapacity(int capacity) { // Método privado que formatea una capacidad a una cadena recibiendo la capacidad como parámetro y retornando String
        return capacity >= Integer.MAX_VALUE ? "∞" : Integer.toString(capacity); // Retorna "∞" si la capacidad es infinita, o el valor como cadena si no lo es
    } // Cierre del método formatCapacity

    private double parsePositiveDouble(TextField field, String name, boolean allowZero) { // Método privado que parsea y valida un número double positivo recibiendo el campo, nombre y si permite cero como parámetros y retornando double
        String text = normalizeNumeric(field.getText()); // Normaliza el texto del campo (espacios en blanco y comas)
        if (text.isEmpty()) { // Condición que verifica si el texto está vacío
            throw new IllegalArgumentException("Ingrese un valor para " + name); // Lanza excepción si está vacío
        } // Cierre del bloque condicional if
        double value = Double.parseDouble(text); // Parsea el texto como un número double
        if (!allowZero && value <= 0) { // Condición que verifica si el valor no es positivo y no se permite cero
            throw new IllegalArgumentException(name + " debe ser mayor a 0"); // Lanza excepción si no es positivo
        } // Cierre del bloque condicional if
        if (allowZero && value < 0) { // Condición que verifica si el valor es negativo y se permite cero
            throw new IllegalArgumentException(name + " no puede ser negativo"); // Lanza excepción si es negativo
        } // Cierre del bloque condicional if
        return value; // Retorna el valor parseado
    } // Cierre del método parsePositiveDouble

    private double parseProbability(TextField field, String name) { // Método privado que parsea y valida una probabilidad recibiendo el campo y nombre como parámetros y retornando double
        double value = parsePositiveDouble(field, name, true); // Parsea el valor como un número positivo permitiendo cero
        if (value < 0 || value > 1) { // Condición que verifica si el valor está fuera del rango 0-1
            throw new IllegalArgumentException(name + " debe estar entre 0 y 1"); // Lanza excepción si está fuera del rango
        } // Cierre del bloque condicional if
        return value; // Retorna el valor parseado
    } // Cierre del método parseProbability

    private long parseLong(TextField field, String name) { // Método privado que parsea un número long recibiendo el campo y nombre como parámetros y retornando long
        String text = field.getText().trim(); // Obtiene el texto del campo y lo limpia de espacios en blanco
        if (text.isEmpty()) { // Condición que verifica si el texto está vacío
            throw new IllegalArgumentException("Ingrese un valor para " + name); // Lanza excepción si está vacío
        } // Cierre del bloque condicional if
        return Long.parseLong(text); // Parsea el texto como un número long y lo retorna
    } // Cierre del método parseLong

    private int parseCapacity(TextField field, String name, boolean allowInfinite, int minValue) { // Método privado que parsea y valida una capacidad recibiendo el campo, nombre, si permite infinito y valor mínimo como parámetros y retornando int
        String text = field.getText().trim(); // Obtiene el texto del campo y lo limpia de espacios en blanco
        if (text.isEmpty()) { // Condición que verifica si el texto está vacío
            throw new IllegalArgumentException("Ingrese un valor para " + name); // Lanza excepción si está vacío
        } // Cierre del bloque condicional if
        if (allowInfinite && (text.equalsIgnoreCase("inf") || text.equals("∞"))) { // Condición que verifica si se permite infinito y el texto es "inf" o "∞"
            return Integer.MAX_VALUE; // Retorna Integer.MAX_VALUE para representar capacidad infinita
        } // Cierre del bloque condicional if
        int value = Integer.parseInt(text); // Parsea el texto como un número entero
        if (value < minValue) { // Condición que verifica si el valor es menor al valor mínimo
            throw new IllegalArgumentException(name + " debe ser al menos " + minValue); // Lanza excepción si es menor al mínimo
        } // Cierre del bloque condicional if
        return value; // Retorna el valor parseado
    } // Cierre del método parseCapacity

    private String normalizeNumeric(String raw) { // Método privado que normaliza una cadena numérica recibiendo la cadena sin procesar como parámetro y retornando String
        return raw == null ? "" : raw.trim().replace(',', '.'); // Retorna una cadena vacía si es null, o la cadena limpiada de espacios con comas reemplazadas por puntos
    } // Cierre del método normalizeNumeric

    private void showValidationError(String message) { // Método privado que muestra un diálogo de error de validación recibiendo el mensaje como parámetro sin retorno
        Alert alert = new Alert(Alert.AlertType.ERROR); // Crea un nuevo cuadro de diálogo de alerta de tipo error
        alert.setTitle("Error de validación"); // Establece el título del diálogo
        alert.setHeaderText("No se pudieron guardar los parámetros"); // Establece el encabezado del diálogo
        alert.setContentText(message); // Establece el contenido del diálogo con el mensaje de error
        alert.showAndWait(); // Muestra el diálogo y espera a que el usuario lo cierre
    } // Cierre del método showValidationError
} // Cierre de la clase ParametersDialog
