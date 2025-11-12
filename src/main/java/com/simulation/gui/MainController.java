package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.config.SimulationParameters;
import com.simulation.core.DigemicEngine; // NUEVO: Motor DIGEMIC
import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.animation.AnimationTimer; // Importa la clase AnimationTimer de JavaFX para crear un loop de animación de alto rendimiento
import javafx.application.Platform; // Importa la clase Platform de JavaFX para ejecutar código en el hilo de la interfaz gráfica
import javafx.fxml.FXML; // Importa la anotación FXML para inyectar elementos definidos en archivos FXML
import javafx.scene.control.*; // Importa todas las clases de controles de JavaFX (Button, Label, TableView, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Importa la clase PropertyValueFactory para vincular propiedades de objetos con columnas de tablas
import javafx.scene.layout.BorderPane; // Importa la clase BorderPane de JavaFX para el layout principal

import java.util.ArrayList; // Importa la clase ArrayList para usar listas dinámicas
import java.util.List; // Importa la interfaz List para trabajar con colecciones tipo lista
import java.util.Map; // Importa la interfaz Map para trabajar con mapas clave-valor
import java.util.Optional;

/** // Inicio del comentario Javadoc de la clase
 * Controlador principal con actualización en tiempo real de gráficas // Descripción del controlador
 */ // Fin del comentario Javadoc
public class MainController { // Declaración de la clase pública MainController que controla la interfaz principal de la aplicación
    @FXML private BorderPane rootPane; // Variable privada anotada con @FXML que representa el panel raíz con layout BorderPane inyectado desde FXML
    @FXML private Button startButton; // Variable privada anotada con @FXML que representa el botón de inicio inyectado desde FXML
    @FXML private Button pauseButton; // Variable privada anotada con @FXML que representa el botón de pausa inyectado desde FXML
    @FXML private Button resetButton; // Variable privada anotada con @FXML que representa el botón de reinicio inyectado desde FXML
    @FXML private Button parametersButton; // Variable privada anotada con @FXML que representa el botón de parámetros inyectado desde FXML
    @FXML private Button zoomInButton; // NUEVO: Botón para hacer zoom in
    @FXML private Button zoomOutButton; // NUEVO: Botón para hacer zoom out
    @FXML private Button zoomResetButton; // NUEVO: Botón para resetear zoom a 100%
    @FXML private Label statusLabel; // Variable privada anotada con @FXML que representa la etiqueta de estado inyectada desde FXML
    @FXML private Label timeLabel; // Variable privada anotada con @FXML que representa la etiqueta de tiempo inyectada desde FXML
    @FXML private Slider speedSlider; // Variable privada anotada con @FXML que representa el deslizador de velocidad inyectado desde FXML
    @FXML private Label speedLabel; // Variable privada anotada con @FXML que representa la etiqueta de velocidad inyectada desde FXML
    @FXML private TabPane mainTabPane; // Variable privada anotada con @FXML que representa el panel de pestañas principal inyectado desde FXML
    @FXML private Tab animationTab; // Variable privada anotada con @FXML que representa la pestaña de animación inyectada desde FXML
    @FXML private Tab resultsTab; // Variable privada anotada con @FXML que representa la pestaña de resultados inyectada desde FXML
    @FXML private Tab chartsTab; // Variable privada anotada con @FXML que representa la pestaña de gráficas inyectada desde FXML

    // Tablas de resultados
    @FXML private TableView<LocationStats> locationTable; // Variable privada anotada con @FXML que representa la tabla de estadísticas de locaciones inyectada desde FXML
    @FXML private TableColumn<LocationStats, String> locNameCol; // Variable privada anotada con @FXML que representa la columna de nombre de locación inyectada desde FXML
    @FXML private TableColumn<LocationStats, String> locCapacityCol; // Variable privada anotada con @FXML que representa la columna de capacidad inyectada desde FXML
    @FXML private TableColumn<LocationStats, String> locScheduledTimeCol; // Columna para mostrar el tiempo programado en horas
    @FXML private TableColumn<LocationStats, Integer> locEntriesCol; // Variable privada anotada con @FXML que representa la columna de entradas totales inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locTimePerEntryCol; // Variable privada anotada con @FXML que representa la columna de tiempo por entrada inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locAvgContentCol; // Variable privada anotada con @FXML que representa la columna de contenido promedio inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locMaxContentCol; // Variable privada anotada con @FXML que representa la columna de contenido máximo inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locCurrentContentCol; // Variable privada anotada con @FXML que representa la columna de contenido actual inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locUtilizationCol; // Variable privada anotada con @FXML que representa la columna de utilización inyectada desde FXML

    @FXML private TextArea entityStatsText; // Variable privada anotada con @FXML que representa el área de texto para estadísticas de entidades inyectada desde FXML

    private SimulationParameters parameters;
    private DigemicEngine engine; // Motor DIGEMIC
    private AnimationPanel animationPanel;
    @FXML private ScrollPane animationScrollPane; // ScrollPane definido en FXML que contiene la animación
    private RealTimeChartsPanel realTimeChartsPanel; // NUEVO: Panel de gráficas en tiempo real // Variable privada que almacena el panel de gráficas en tiempo real
    private AnimationTimer animationTimer; // Variable privada que almacena el timer de animación para actualizar la interfaz periódicamente
    private Thread simulationThread; // Variable privada que almacena el hilo donde se ejecuta la simulación

    public void initialize() {
        parameters = new SimulationParameters();
        
        // Crear motor DIGEMIC
        engine = new DigemicEngine(parameters);

        setupAnimationPanel();
        setupRealTimeCharts();
        setupControls();
        setupResultsTables();
        setupAnimationLoop();
        realTimeChartsPanel.initializeState(getStatistics());

        updateStatus("Listo para iniciar - Sistema DIGEMIC");
    }

    // === MÉTODOS HELPER PARA ACCESO AL MOTOR ===
    
    private Statistics getStatistics() {
        return ((DigemicEngine) engine).getStatistics();
    }

    private double getCurrentTime() {
        return ((DigemicEngine) engine).getCurrentTime();
    }

    private boolean isRunning() {
        return ((DigemicEngine) engine).isRunning();
    }

    private boolean isPaused() {
        return ((DigemicEngine) engine).isPaused();
    }

    private void setSimulationSpeed(double speed) {
        ((DigemicEngine) engine).setSimulationSpeed(speed);
    }

    private void initializeEngine() {
        ((DigemicEngine) engine).initialize();
    }

    private void runEngine() {
        ((DigemicEngine) engine).run();
    }

    private void stopEngine() {
        ((DigemicEngine) engine).stop();
    }

    private void pauseEngine() {
        ((DigemicEngine) engine).pause();
    }

    private void resumeEngine() {
        ((DigemicEngine) engine).resume();
    }

    private Location getLocation(String name) {
        return ((DigemicEngine) engine).getLocation(name);
    }


    private void setupAnimationPanel() { // Método privado que configura el panel de animación y lo agrega a la pestaña correspondiente
        animationPanel = new AnimationPanel(engine); // Crea una nueva instancia de AnimationPanel pasando el motor de simulación
        configureAnimationScrollPane(); // Configura las propiedades del ScrollPane definido en FXML
        animationScrollPane.setContent(animationPanel); // Inserta el panel de animación dentro del ScrollPane existente
        animationTab.setDisable(false); // Habilita la pestaña de animación para que pueda ser seleccionada
        animationPanel.render(); // Renderiza el panel de animación inicialmente para mostrar el estado inicial
    } // Cierre del método setupAnimationPanel

    private void configureAnimationScrollPane() { // Configura propiedades del ScrollPane proveniente del FXML
        if (animationScrollPane == null) {
            return; // Evita NullPointerException si la referencia no se resolvió
        }
        animationScrollPane.setFitToWidth(true); // Ajusta ancho automáticamente
        animationScrollPane.setFitToHeight(false); // Mantiene alto natural para permitir desplazamiento vertical
        animationScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Barra horizontal solo cuando se necesita
        animationScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Barra vertical solo cuando se necesita
        animationScrollPane.setPannable(true); // Permite arrastrar la vista con el mouse
        animationScrollPane.setStyle("-fx-background-color: transparent;"); // Fondo transparente para combinar con el estilo general
    }

    /** // Inicio del comentario Javadoc del método
     * NUEVO: Configura el panel de gráficas en tiempo real // Descripción del método nuevo
     */ // Fin del comentario Javadoc
    private void setupRealTimeCharts() { // Método privado que configura el panel de gráficas en tiempo real
        realTimeChartsPanel = new RealTimeChartsPanel(); // Crea una nueva instancia de RealTimeChartsPanel
        if (chartsTab != null) { // Condición que verifica si la pestaña de gráficas fue inyectada correctamente (no es null)
            chartsTab.setContent(realTimeChartsPanel); // Establece el contenido de la pestaña de gráficas con el panel de gráficas en tiempo real
            chartsTab.setDisable(false); // Habilita la pestaña de gráficas para que pueda ser seleccionada
        } // Cierre del bloque condicional if
    } // Cierre del método setupRealTimeCharts

    private void setupControls() { // Método privado que configura todos los controles de la interfaz (botones, sliders, listeners)
        startButton.setOnAction(e -> handleStart()); // Establece el manejador de eventos del botón de inicio para llamar a handleStart cuando se presiona
        pauseButton.setOnAction(e -> handlePause()); // Establece el manejador de eventos del botón de pausa para llamar a handlePause cuando se presiona
        resetButton.setOnAction(e -> handleReset()); // Establece el manejador de eventos del botón de reinicio para llamar a handleReset cuando se presiona
        parametersButton.setOnAction(e -> handleParameters()); // Establece el manejador de eventos del botón de parámetros para llamar a handleParameters cuando se presiona

        // NUEVO: Controles de zoom
        zoomInButton.setOnAction(e -> animationPanel.zoomIn());
        zoomOutButton.setOnAction(e -> animationPanel.zoomOut());
        zoomResetButton.setOnAction(e -> animationPanel.resetZoom());

        pauseButton.setDisable(true); // Deshabilita el botón de pausa inicialmente porque la simulación no está corriendo

        speedSlider.setMin(0.0); // Permite pausar la simulación con 0x
        speedSlider.setMax(200); // Límite superior más moderado
        speedSlider.setValue(0.4); // Velocidad inicial muy lenta para visualizar el flujo
        speedSlider.setBlockIncrement(0.05);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = Math.max(0.0, newVal.doubleValue());
            setSimulationSpeed(speed); // Usar método helper
            updateSpeedLabel(speed);
        });

        setSimulationSpeed(speedSlider.getValue());
        updateSpeedLabel(speedSlider.getValue());
    } // Cierre del método setupControls

    private void setupResultsTables() { // Método privado que configura las tablas de resultados vinculando columnas con propiedades y formateando celdas
        // Configurar tabla de locaciones
    locNameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // Vincula la columna de nombre con la propiedad "name" de LocationStats
    locCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity")); // Vincula la columna de capacidad con la propiedad "capacity" de LocationStats
    locScheduledTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledTime")); // Vincula la columna de tiempo programado con la propiedad "scheduledTime"
        locEntriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries")); // Vincula la columna de entradas con la propiedad "totalEntries" de LocationStats
        locTimePerEntryCol.setCellValueFactory(new PropertyValueFactory<>("timePerEntry")); // Vincula la columna de tiempo por entrada con la propiedad "timePerEntry" de LocationStats
        locAvgContentCol.setCellValueFactory(new PropertyValueFactory<>("avgContent")); // Vincula la columna de contenido promedio con la propiedad "avgContent" de LocationStats
        locMaxContentCol.setCellValueFactory(new PropertyValueFactory<>("maxContent")); // Vincula la columna de contenido máximo con la propiedad "maxContent" de LocationStats
        locCurrentContentCol.setCellValueFactory(new PropertyValueFactory<>("currentContent")); // Vincula la columna de contenido actual con la propiedad "currentContent" de LocationStats
        locUtilizationCol.setCellValueFactory(new PropertyValueFactory<>("utilization")); // Vincula la columna de utilización con la propiedad "utilization" de LocationStats

        // Formato de columnas numéricas
        locTimePerEntryCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de tiempo por entrada
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                } // Cierre del bloque else
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory

        locAvgContentCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de contenido promedio
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                } // Cierre del bloque else
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory

        locUtilizationCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de utilización con colores
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f%%", item)); // Formatea el valor como string con 2 decimales y símbolo de porcentaje

                    // Colorear según utilización
                    if (item < 50) { // Condición que verifica si la utilización es menor al 50%
                        setStyle("-fx-background-color: #C8E6C9;"); // Establece el fondo de la celda en verde claro para utilización baja
                    } else if (item < 80) { // Condición que verifica si la utilización está entre 50% y 80%
                        setStyle("-fx-background-color: #FFF9C4;"); // Establece el fondo de la celda en amarillo claro para utilización media
                    } else { // Bloque else que se ejecuta si la utilización es mayor o igual al 80%
                        setStyle("-fx-background-color: #FFCDD2;"); // Establece el fondo de la celda en rojo claro para utilización alta
                    } // Cierre del bloque else
                } // Cierre del bloque else externo
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory
    } // Cierre del método setupResultsTables

    private void setupAnimationLoop() { // Método privado que configura el loop de animación que actualiza la interfaz periódicamente
        animationTimer = new AnimationTimer() { // Crea una nueva instancia anónima de AnimationTimer
            private long lastUpdate = 0; // Variable privada que almacena el tiempo del último update en nanosegundos, inicializada en 0
            private static final long UPDATE_INTERVAL = 50_000_000; // 50ms // Constante estática final que define el intervalo entre updates en nanosegundos (50 millones = 50ms)

            @Override
            public void handle(long now) {
                if (isRunning() && now - lastUpdate >= UPDATE_INTERVAL) {
                    updateDisplay();
                    updateRealTimeCharts(); // NUEVO: Actualizar gráficas en tiempo real
                    lastUpdate = now;
                }
            }
        }; // Cierre de la declaración de AnimationTimer
    } // Cierre del método setupAnimationLoop

    @FXML
    private void handleStart() {
        if (isRunning()) {
            return;
        }

        startButton.setDisable(true);
        pauseButton.setDisable(false);
        resetButton.setDisable(true);
        parametersButton.setDisable(true);

        initializeEngine(); // Usar método helper
    setSimulationSpeed(speedSlider.getValue());

        // Inicializar gráficas en tiempo real
        realTimeChartsPanel.initializeState(getStatistics()); // Usar método helper

        simulationThread = new Thread(() -> {
            runEngine(); // Usar método helper

            Platform.runLater(() -> {
                handleSimulationComplete();
            });
        }); // Cierre del paréntesis del constructor Thread

        simulationThread.setDaemon(true); // Configura el hilo como daemon para que no impida el cierre de la aplicación
        simulationThread.start(); // Inicia la ejecución del hilo de simulación

        animationTimer.start(); // Inicia el timer de animación para comenzar a actualizar la interfaz periódicamente
        updateStatus("Simulación en ejecución..."); // Actualiza el mensaje de estado indicando que la simulación está corriendo
    } // Cierre del método handleStart

    @FXML
    private void handlePause() {
        if (isPaused()) { // Usar método helper
            resumeEngine(); // Usar método helper
            pauseButton.setText("Pausar");
            updateStatus("Simulación en ejecución...");
        } else {
            pauseEngine(); // Usar método helper
            pauseButton.setText("Reanudar");
            updateStatus("Simulación pausada");
        }
    }

    @FXML
    private void handleReset() {
        if (animationTimer != null) {
            animationTimer.stop();
        }

        if (simulationThread != null && simulationThread.isAlive()) {
            stopEngine(); // Usar método helper
            try {
                simulationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // Recrear motor DIGEMIC
        engine = new DigemicEngine(parameters);
    setSimulationSpeed(speedSlider.getValue());
    updateSpeedLabel(speedSlider.getValue());
        
        setupAnimationPanel();
        realTimeChartsPanel.initializeState(getStatistics()); // Usar método helper

        startButton.setDisable(false);
        pauseButton.setDisable(true); // Deshabilita el botón de pausa porque no hay simulación corriendo
        pauseButton.setText("Pausar"); // Restablece el texto del botón de pausa a "Pausar"
        resetButton.setDisable(false); // Habilita el botón de reinicio
        parametersButton.setDisable(false); // Habilita el botón de parámetros para permitir cambios

    updateTimeLabel(0); // Restablece la etiqueta de tiempo al estado inicial
        updateStatus("Listo para iniciar"); // Actualiza el mensaje de estado indicando que está listo

        locationTable.getItems().clear(); // Limpia todos los items de la tabla de locaciones
        entityStatsText.clear(); // Limpia el área de texto de estadísticas de entidades
    } // Cierre del método handleReset

    @FXML
    private void handleParameters() {
        if (engine != null && isRunning()) {
            updateStatus("Detén la simulación antes de cambiar parámetros");
            return;
        }

        ParametersDialog dialog = new ParametersDialog(parameters);
        Optional<Boolean> result = dialog.showAndWait();
        if (result.orElse(false)) {
            engine = new DigemicEngine(parameters);
            setSimulationSpeed(speedSlider.getValue());
            updateSpeedLabel(speedSlider.getValue());

            setupAnimationPanel();
            realTimeChartsPanel.initializeState(getStatistics());
            locationTable.getItems().clear();
            entityStatsText.clear();

            updateTimeLabel(0);
            updateStatus("Parámetros actualizados");
        } else {
            updateStatus("Cambios de parámetros cancelados");
        }
    }

    private void handleSimulationComplete() {
        animationTimer.stop();

        startButton.setDisable(true);
        pauseButton.setDisable(true);
        resetButton.setDisable(false);
        parametersButton.setDisable(false);

        updateStatus("Simulación completada");
        updateTimeLabel(getCurrentTime()); // Usar método helper
        updateResults();

        // NUEVO: Actualizar gráficas finales
        updateRealTimeCharts();

        // NUEVO: Mostrar cuadro de resultados finales (incisos a-e)
        ResultsDialog resultsDialog = new ResultsDialog(getStatistics(), getCurrentTime());
        resultsDialog.show();

        mainTabPane.getSelectionModel().select(chartsTab);
    }

    private void updateDisplay() {
        double currentTime = getCurrentTime(); // Usar método helper

        Platform.runLater(() -> {
            // Actualizar tiempo
            updateTimeLabel(currentTime); // Refresca la etiqueta de tiempo en formato HH:MM

            // Renderizar animación
            animationPanel.render(); // Renderiza el panel de animación con el estado actual del motor

            updateLocationStats(); // Refresca la tabla de locaciones en tiempo real
        }); // Cierre del paréntesis de runLater
    } // Cierre del método updateDisplay

    private void updateTimeLabel(double currentTime) { // Actualiza la etiqueta con formato HH:MM h
        int totalMinutes = (int) Math.floor(currentTime); // Minutos transcurridos totales
        int hours = totalMinutes / 60; // Conversión a horas completas
        int minutes = totalMinutes % 60; // Minutos residuales
        timeLabel.setText(String.format("Tiempo: %02d:%02d h", hours, minutes)); // Muestra el tiempo en formato HH:MM
    }

    /**
     * NUEVO: Actualiza las gráficas en tiempo real durante la simulación
     */
    private void updateRealTimeCharts() {
        Statistics stats = getStatistics(); // Usar método helper
        double currentTime = getCurrentTime(); // Usar método helper

        Platform.runLater(() -> {
            realTimeChartsPanel.updateCharts(stats, currentTime);
        });
    }

    private void updateStatus(String message) { // Método privado que actualiza el mensaje de estado recibiendo el mensaje como parámetro
        statusLabel.setText("Estado: " + message); // Establece el texto de la etiqueta de estado concatenando "Estado: " con el mensaje recibido
    } // Cierre del método updateStatus

    private void updateSpeedLabel(double speed) {
        String descriptor;
        if (speed == 0.0) {
            descriptor = "pausada";
        } else if (speed <= 0.1) {
            descriptor = "ultra lenta";
        } else if (speed <= 0.5) {
            descriptor = "muy lenta";
        } else if (speed <= 1.5) {
            descriptor = "lenta";
        } else if (speed <= 5) {
            descriptor = "normal";
        } else if (speed <= 20) {
            descriptor = "rápida";
        } else {
            descriptor = "turbo";
        }

        speedLabel.setText(String.format("Velocidad: %.2fx (%s)", speed, descriptor));
    } // Cierre del método updateSpeedLabel

    private void updateResults() { // Método privado que actualiza todas las tablas y áreas de texto de resultados
        updateLocationStats(); // Llama al método para actualizar la tabla de estadísticas de locaciones
        updateEntityStats(); // Llama al método para actualizar el área de texto de estadísticas de entidades
    } // Cierre del método updateResults

    private void updateLocationStats() {
        Statistics stats = getStatistics(); // Usar método helper
        double currentTime = getCurrentTime(); // Usar método helper

        List<LocationStats> locationStatsList = new ArrayList<>();
        Map<String, Location> locations = stats.getLocations();
        String scheduledTime = formatScheduledHours(parameters.getSimulationDurationMinutes());
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            Location loc = entry.getValue();

            String name = loc.getName();
            int rawCapacity = loc.getCapacity();
            String capacityDisplay = formatCapacity(rawCapacity);
            int totalEntries = loc.getTotalEntries();
            double timePerEntry = loc.getAverageTimePerEntry(currentTime);
            double avgContent = loc.getAverageContent(currentTime);
            int maxContent = loc.getMaxContent();
            int currentContent = loc.getCurrentContent();
            double utilization = loc.getUtilization(currentTime);

            locationStatsList.add(new LocationStats(
                name, // Parámetro 1: nombre
                capacityDisplay, // Parámetro 2: capacidad
                scheduledTime, // Parámetro 3: tiempo programado
                totalEntries, // Parámetro 3: entradas totales
                timePerEntry, // Parámetro 4: tiempo por entrada
                avgContent, // Parámetro 5: contenido promedio
                maxContent, // Parámetro 6: contenido máximo
                currentContent, // Parámetro 7: contenido actual
                utilization // Parámetro 8: utilización
            )); // Cierre del paréntesis de add y new LocationStats
        } // Cierre del bucle for-each

        locationTable.getItems().setAll(locationStatsList); // Reemplaza todos los items de la tabla con la nueva lista de estadísticas
    } // Cierre del método updateLocationStats

    private void updateEntityStats() {
        Statistics stats = getStatistics(); // Usar método helper

        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADÍSTICAS DE ENTIDADES ===\n\n");
        sb.append(String.format("Total de Arribos: %d\n", stats.getTotalArrivals()));
        sb.append(String.format("Total de Salidas (Completadas): %d\n", stats.getTotalExits()));
        sb.append(String.format("En Sistema Actualmente: %d\n\n", stats.getTotalArrivals() - stats.getTotalExits()));

        sb.append(String.format("Throughput: %.2f piezas/hora\n\n", stats.getThroughput()));
        sb.append("=== TIEMPO EN SISTEMA ===\n");
        sb.append(String.format("Promedio: %.2f min\n", stats.getAverageSystemTime()));
        sb.append(String.format("Desviación Estándar: %.2f min\n", stats.getStdDevSystemTime()));
        sb.append(String.format("Mínimo: %.2f min\n", stats.getMinSystemTime()));
        sb.append(String.format("Máximo: %.2f min\n", stats.getMaxSystemTime()));

        entityStatsText.setText(sb.toString());
    }

    public static class LocationStats { // Declaración de clase estática pública interna LocationStats que representa las estadísticas de una locación para la tabla
        private final String name; // Variable final que almacena el nombre de la locación
        private final String capacity; // Variable final que almacena la capacidad mostrada (formateada)
        private final String scheduledTime; // Tiempo programado en horas para la simulación
        private final Integer totalEntries; // Variable final que almacena el total de entradas a la locación
        private final Double timePerEntry; // Variable final que almacena el tiempo promedio por entrada
        private final Double avgContent; // Variable final que almacena el contenido promedio de la locación
        private final Integer maxContent; // Variable final que almacena el contenido máximo de la locación
        private final Integer currentContent; // Variable final que almacena el contenido actual de la locación
        private final Double utilization; // Variable final que almacena el porcentaje de utilización de la locación

        public LocationStats(String name, // Constructor público que inicializa una instancia de LocationStats recibiendo el nombre como primer parámetro
                              String capacity, // Segundo parámetro: capacidad formateada
                              String scheduledTime, // Tercer parámetro: tiempo programado
                              Integer totalEntries, // Cuarto parámetro: entradas totales
                              Double timePerEntry, // Quinto parámetro: tiempo por entrada
                              Double avgContent, // Sexto parámetro: contenido promedio
                              Integer maxContent, // Séptimo parámetro: contenido máximo
                              Integer currentContent, // Octavo parámetro: contenido actual
                              Double utilization) { // Noveno parámetro: utilización
            this.name = name; // Asigna el nombre recibido a la variable de instancia
            this.capacity = capacity; // Asigna directamente la capacidad formateada
            this.scheduledTime = scheduledTime; // Almacena el tiempo programado
            this.totalEntries = totalEntries; // Asigna las entradas totales recibidas a la variable de instancia
            this.timePerEntry = timePerEntry; // Asigna el tiempo por entrada recibido a la variable de instancia
            this.avgContent = avgContent; // Asigna el contenido promedio recibido a la variable de instancia
            this.maxContent = maxContent; // Asigna el contenido máximo observado
            this.currentContent = currentContent; // Asigna el contenido actual recibido a la variable de instancia
            this.utilization = utilization; // Asigna la utilización recibida a la variable de instancia
        } // Cierre del constructor LocationStats

        public String getName() { return name; } // Método público getter que retorna el nombre de la locación
        public String getCapacity() { return capacity; } // Método público getter que retorna la capacidad formateada
        public String getScheduledTime() { return scheduledTime; } // Getter para el tiempo programado
        public Integer getTotalEntries() { return totalEntries; } // Método público getter que retorna el total de entradas
        public Double getTimePerEntry() { return timePerEntry; } // Método público getter que retorna el tiempo por entrada
        public Double getAvgContent() { return avgContent; } // Método público getter que retorna el contenido promedio
        public Integer getMaxContent() { return maxContent; } // Método público getter que retorna el contenido máximo
        public Integer getCurrentContent() { return currentContent; } // Método público getter que retorna el contenido actual
        public Double getUtilization() { return utilization; } // Método público getter que retorna la utilización
    } // Cierre de la clase LocationStats

    private String formatCapacity(int capacity) { // Devuelve la capacidad formateada para coincidir con la tabla de resultados
        if (capacity == Integer.MAX_VALUE) {
            return "999.999,00"; // Formato similar a ProModel para capacidades infinitas
        }
        return String.valueOf(capacity);
    }

    private String formatScheduledHours(double durationMinutes) { // Convierte minutos programados a horas con dos decimales
        double hours = durationMinutes / 60.0;
        return String.format("%.2f", hours);
    }
} // Cierre de la clase MainController
