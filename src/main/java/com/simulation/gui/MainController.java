package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters para acceder a los parámetros de configuración de la simulación
import com.simulation.core.SimulationEngine; // Importa la clase SimulationEngine que es el motor principal que ejecuta la simulación
import com.simulation.resources.Location; // Importa la clase Location que representa las locaciones del sistema
import com.simulation.statistics.Statistics; // Importa la clase Statistics para acceder a las estadísticas de la simulación
import javafx.animation.AnimationTimer; // Importa la clase AnimationTimer de JavaFX para crear un loop de animación de alto rendimiento
import javafx.application.Platform; // Importa la clase Platform de JavaFX para ejecutar código en el hilo de la interfaz gráfica
import javafx.fxml.FXML; // Importa la anotación FXML para inyectar elementos definidos en archivos FXML
import javafx.scene.control.*; // Importa todas las clases de controles de JavaFX (Button, Label, TableView, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Importa la clase PropertyValueFactory para vincular propiedades de objetos con columnas de tablas
import javafx.scene.layout.BorderPane; // Importa la clase BorderPane de JavaFX para el layout principal

import java.util.ArrayList; // Importa la clase ArrayList para usar listas dinámicas
import java.util.List; // Importa la interfaz List para trabajar con colecciones tipo lista
import java.util.Map; // Importa la interfaz Map para trabajar con mapas clave-valor

/** // Inicio del comentario Javadoc de la clase
 * Controlador principal con actualización en tiempo real de gráficas // Descripción del controlador
 */ // Fin del comentario Javadoc
public class MainController { // Declaración de la clase pública MainController que controla la interfaz principal de la aplicación
    @FXML private BorderPane rootPane; // Variable privada anotada con @FXML que representa el panel raíz con layout BorderPane inyectado desde FXML
    @FXML private Button startButton; // Variable privada anotada con @FXML que representa el botón de inicio inyectado desde FXML
    @FXML private Button pauseButton; // Variable privada anotada con @FXML que representa el botón de pausa inyectado desde FXML
    @FXML private Button resetButton; // Variable privada anotada con @FXML que representa el botón de reinicio inyectado desde FXML
    @FXML private Button parametersButton; // Variable privada anotada con @FXML que representa el botón de parámetros inyectado desde FXML
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
    @FXML private TableColumn<LocationStats, Integer> locCapacityCol; // Variable privada anotada con @FXML que representa la columna de capacidad inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locEntriesCol; // Variable privada anotada con @FXML que representa la columna de entradas totales inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locTimePerEntryCol; // Variable privada anotada con @FXML que representa la columna de tiempo por entrada inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locAvgContentCol; // Variable privada anotada con @FXML que representa la columna de contenido promedio inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locMaxContentCol; // Variable privada anotada con @FXML que representa la columna de contenido máximo inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locCurrentContentCol; // Variable privada anotada con @FXML que representa la columna de contenido actual inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locUtilizationCol; // Variable privada anotada con @FXML que representa la columna de utilización inyectada desde FXML

    @FXML private TextArea entityStatsText; // Variable privada anotada con @FXML que representa el área de texto para estadísticas de entidades inyectada desde FXML

    private SimulationParameters parameters; // Variable privada que almacena los parámetros de configuración de la simulación
    private SimulationEngine engine; // Variable privada que almacena la referencia al motor de simulación
    private AnimationPanel animationPanel; // Variable privada que almacena el panel de animación personalizado
    @FXML private ScrollPane animationScrollPane; // ScrollPane definido en FXML que contiene la animación
    private RealTimeChartsPanel realTimeChartsPanel; // NUEVO: Panel de gráficas en tiempo real // Variable privada que almacena el panel de gráficas en tiempo real
    private AnimationTimer animationTimer; // Variable privada que almacena el timer de animación para actualizar la interfaz periódicamente
    private Thread simulationThread; // Variable privada que almacena el hilo donde se ejecuta la simulación

    public void initialize() { // Método público initialize que es llamado automáticamente por JavaFX después de cargar el FXML para inicializar el controlador
        parameters = new SimulationParameters(); // Crea una nueva instancia de SimulationParameters con valores por defecto
        engine = new SimulationEngine(parameters); // Crea una nueva instancia de SimulationEngine con los parámetros creados

        setupAnimationPanel(); // Llama al método para configurar el panel de animación
        setupRealTimeCharts(); // NUEVO // Llama al método para configurar el panel de gráficas en tiempo real
        setupControls(); // Llama al método para configurar los controles de la interfaz (botones, sliders, etc.)
        setupResultsTables(); // Llama al método para configurar las tablas de resultados
        setupAnimationLoop(); // Llama al método para configurar el loop de animación
        realTimeChartsPanel.initializeState(engine.getStatistics()); // Inicializa el estado del panel de gráficas en tiempo real con las estadísticas del motor

        updateStatus("Listo para iniciar"); // Actualiza el mensaje de estado a "Listo para iniciar"
    } // Cierre del método initialize

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

        pauseButton.setDisable(true); // Deshabilita el botón de pausa inicialmente porque la simulación no está corriendo

        speedSlider.setMin(1); // Establece el valor mínimo del slider de velocidad en 1
        speedSlider.setMax(1000); // Establece el valor máximo del slider de velocidad en 1000
        speedSlider.setValue(100); // Establece el valor inicial del slider de velocidad en 100 (velocidad normal)

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> { // Agrega un listener a la propiedad value del slider que se ejecuta cuando cambia el valor
            double speed = newVal.doubleValue(); // Convierte el nuevo valor del slider a double
            engine.setSimulationSpeed(speed); // Establece la velocidad de simulación en el motor con el valor del slider
            updateSpeedLabel(speed); // Actualiza la etiqueta de velocidad para mostrar el nuevo valor
        }); // Cierre del paréntesis de addListener

        updateSpeedLabel(100); // Actualiza la etiqueta de velocidad inicialmente con el valor 100
    } // Cierre del método setupControls

    private void setupResultsTables() { // Método privado que configura las tablas de resultados vinculando columnas con propiedades y formateando celdas
        // Configurar tabla de locaciones
        locNameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // Vincula la columna de nombre con la propiedad "name" de LocationStats
        locCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity")); // Vincula la columna de capacidad con la propiedad "capacity" de LocationStats
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

            @Override // Anotación que indica que este método sobrescribe el método handle de AnimationTimer
            public void handle(long now) { // Método que es llamado en cada frame de la animación recibiendo el tiempo actual en nanosegundos como parámetro
                if (engine.isRunning() && now - lastUpdate >= UPDATE_INTERVAL) { // Condición que verifica si el motor está corriendo y ha pasado suficiente tiempo desde el último update
                    updateDisplay(); // Llama al método para actualizar la visualización (tiempo, animación)
                    updateRealTimeCharts(); // NUEVO: Actualizar gráficas en tiempo real // Llama al método para actualizar las gráficas en tiempo real
                    lastUpdate = now; // Actualiza el tiempo del último update con el tiempo actual
                } // Cierre del bloque condicional if
            } // Cierre del método handle
        }; // Cierre de la declaración de AnimationTimer
    } // Cierre del método setupAnimationLoop

    @FXML // Anotación que indica que este método puede ser llamado desde FXML
    private void handleStart() { // Método privado que maneja el evento de presionar el botón de inicio
        if (engine.isRunning()) { // Condición que verifica si el motor ya está corriendo
            return; // Sale del método prematuramente si ya está corriendo para evitar múltiples inicios
        } // Cierre del bloque condicional if

        startButton.setDisable(true); // Deshabilita el botón de inicio para evitar múltiples clicks
        pauseButton.setDisable(false); // Habilita el botón de pausa para permitir pausar la simulación
        resetButton.setDisable(true); // Deshabilita el botón de reinicio mientras la simulación está corriendo
        parametersButton.setDisable(true); // Deshabilita el botón de parámetros para evitar cambios durante la ejecución

        engine.initialize(); // Inicializa el motor de simulación programando el primer evento

        // Inicializar gráficas en tiempo real
        realTimeChartsPanel.initializeState(engine.getStatistics()); // Inicializa el estado del panel de gráficas con las estadísticas del motor

        simulationThread = new Thread(() -> { // Crea un nuevo hilo con una expresión lambda para ejecutar la simulación
            engine.run(); // Ejecuta el loop principal de la simulación (bloqueante)

            Platform.runLater(() -> { // Programa la ejecución de código en el hilo de JavaFX cuando la simulación termine
                handleSimulationComplete(); // Llama al método que maneja la finalización de la simulación
            }); // Cierre del paréntesis de runLater
        }); // Cierre del paréntesis del constructor Thread

        simulationThread.setDaemon(true); // Configura el hilo como daemon para que no impida el cierre de la aplicación
        simulationThread.start(); // Inicia la ejecución del hilo de simulación

        animationTimer.start(); // Inicia el timer de animación para comenzar a actualizar la interfaz periódicamente
        updateStatus("Simulación en ejecución..."); // Actualiza el mensaje de estado indicando que la simulación está corriendo
    } // Cierre del método handleStart

    @FXML // Anotación que indica que este método puede ser llamado desde FXML
    private void handlePause() { // Método privado que maneja el evento de presionar el botón de pausa/reanudar
        if (engine.isPaused()) { // Condición que verifica si el motor está actualmente pausado
            engine.resume(); // Reanuda la simulación cambiando el estado interno del motor
            pauseButton.setText("Pausar"); // Cambia el texto del botón a "Pausar"
            updateStatus("Simulación en ejecución..."); // Actualiza el mensaje de estado indicando que se reanudó
        } else { // Bloque else que se ejecuta si la simulación está corriendo
            engine.pause(); // Pausa la simulación cambiando el estado interno del motor
            pauseButton.setText("Reanudar"); // Cambia el texto del botón a "Reanudar"
            updateStatus("Simulación pausada"); // Actualiza el mensaje de estado indicando que está pausada
        } // Cierre del bloque else
    } // Cierre del método handlePause

    @FXML // Anotación que indica que este método puede ser llamado desde FXML
    private void handleReset() { // Método privado que maneja el evento de presionar el botón de reinicio
        if (animationTimer != null) { // Condición que verifica si el timer de animación existe
            animationTimer.stop(); // Detiene el timer de animación para dejar de actualizar la interfaz
        } // Cierre del bloque condicional if

        if (simulationThread != null && simulationThread.isAlive()) { // Condición que verifica si el hilo de simulación existe y está vivo (corriendo)
            engine.stop(); // Detiene el motor de simulación cambiando su estado interno
            try { // Bloque try para intentar esperar a que el hilo termine
                simulationThread.join(1000); // Espera hasta 1000 milisegundos (1 segundo) a que el hilo termine
            } catch (InterruptedException e) { // Captura la excepción si el hilo actual es interrumpido mientras espera
                Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo actual
            } // Cierre del bloque catch
        } // Cierre del bloque condicional if

        engine = new SimulationEngine(parameters); // Crea una nueva instancia del motor de simulación con los parámetros actuales
        setupAnimationPanel(); // Reconfigura el panel de animación con el nuevo motor
        realTimeChartsPanel.initializeState(engine.getStatistics()); // Reinicializa el estado del panel de gráficas con el nuevo motor

        startButton.setDisable(false); // Habilita el botón de inicio para poder comenzar una nueva simulación
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
        if (engine != null && engine.isRunning()) {
            updateStatus("Detén la simulación antes de cambiar parámetros");
            return;
        }

        ParametersDialog dialog = new ParametersDialog(parameters);
        dialog.showAndWait();
        if (dialog.isAccepted()) {
            engine = new SimulationEngine(parameters);
            setupAnimationPanel();
            realTimeChartsPanel.initializeState(engine.getStatistics());
            locationTable.getItems().clear();
            entityStatsText.clear();
            updateStatus("Parámetros actualizados");
        } else {
            updateStatus("Cambios de parámetros cancelados");
        }
    }

    private void handleSimulationComplete() { // Método privado que maneja la finalización de la simulación
        animationTimer.stop(); // Detiene el timer de animación porque la simulación terminó

        startButton.setDisable(true); // Deshabilita el botón de inicio porque ya no se puede reiniciar sin reset
        pauseButton.setDisable(true); // Deshabilita el botón de pausa porque no hay nada que pausar
        resetButton.setDisable(false); // Habilita el botón de reinicio para permitir comenzar de nuevo
        parametersButton.setDisable(false); // Habilita el botón de parámetros para permitir cambios

        updateStatus("Simulación completada"); // Actualiza el mensaje de estado indicando que la simulación finalizó
    updateTimeLabel(engine.getCurrentTime()); // Refresca la etiqueta de tiempo con el valor final (ej. 60 horas)
        updateResults(); // Llama al método para actualizar las tablas de resultados con las estadísticas finales

        // NUEVO: Actualizar gráficas finales
        updateRealTimeCharts(); // Actualiza las gráficas en tiempo real una última vez con los datos finales

        mainTabPane.getSelectionModel().select(chartsTab); // Cambia automáticamente a la pestaña de gráficas para mostrar los resultados
    } // Cierre del método handleSimulationComplete

    private void updateDisplay() { // Método privado que actualiza la visualización de la interfaz (tiempo y animación)
        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación en minutos

        Platform.runLater(() -> { // Programa la ejecución de código en el hilo de JavaFX para actualizar la interfaz de forma thread-safe
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

    /** // Inicio del comentario Javadoc del método
     * NUEVO: Actualiza las gráficas en tiempo real durante la simulación // Descripción del método nuevo
     */ // Fin del comentario Javadoc
    private void updateRealTimeCharts() { // Método privado que actualiza las gráficas en tiempo real con los datos actuales
        Statistics stats = engine.getStatistics(); // Obtiene el objeto de estadísticas del motor
        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación en minutos

        Platform.runLater(() -> { // Programa la ejecución de código en el hilo de JavaFX para actualizar las gráficas de forma thread-safe
            realTimeChartsPanel.updateCharts(stats, currentTime); // Llama al método del panel de gráficas para actualizar con las estadísticas y tiempo actuales
        }); // Cierre del paréntesis de runLater
    } // Cierre del método updateRealTimeCharts

    private void updateStatus(String message) { // Método privado que actualiza el mensaje de estado recibiendo el mensaje como parámetro
        statusLabel.setText("Estado: " + message); // Establece el texto de la etiqueta de estado concatenando "Estado: " con el mensaje recibido
    } // Cierre del método updateStatus

    private void updateSpeedLabel(double speed) { // Método privado que actualiza la etiqueta de velocidad recibiendo el valor de velocidad como parámetro
        if (speed >= 1000) { // Condición que verifica si la velocidad es mayor o igual a 1000 (velocidad máxima)
            speedLabel.setText("Velocidad: Máxima"); // Establece el texto de la etiqueta como "Velocidad: Máxima"
        } else if (speed >= 100) { // Condición que verifica si la velocidad es mayor o igual a 100
            speedLabel.setText(String.format("Velocidad: %.0fx", speed / 100.0)); // Formatea y muestra la velocidad como multiplicador sin decimales (ej: "5x")
        } else { // Bloque else que se ejecuta si la velocidad es menor a 100
            speedLabel.setText(String.format("Velocidad: %.1fx", speed / 100.0)); // Formatea y muestra la velocidad como multiplicador con un decimal (ej: "0.5x")
        } // Cierre del bloque else
    } // Cierre del método updateSpeedLabel

    private void updateResults() { // Método privado que actualiza todas las tablas y áreas de texto de resultados
        updateLocationStats(); // Llama al método para actualizar la tabla de estadísticas de locaciones
        updateEntityStats(); // Llama al método para actualizar el área de texto de estadísticas de entidades
    } // Cierre del método updateResults

    private void updateLocationStats() { // Método privado que actualiza la tabla de estadísticas de locaciones con los datos finales
        Statistics stats = engine.getStatistics(); // Obtiene el objeto de estadísticas del motor
        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación en minutos

        List<LocationStats> locationStatsList = new ArrayList<>(); // Crea una nueva lista para almacenar objetos LocationStats
        Map<String, Location> locations = stats.getLocations(); // Obtiene el mapa de locaciones desde las estadísticas
        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Bucle for-each que itera sobre cada entrada del mapa de locaciones
            Location loc = entry.getValue(); // Obtiene el objeto Location de la entrada actual

            String name = loc.getName(); // Obtiene el nombre de la locación
            int capacity = loc.getCapacity(); // Obtiene la capacidad de la locación
            int totalEntries = loc.getTotalEntries(); // Obtiene el número total de entradas a la locación
            double timePerEntry = loc.getAverageTimePerEntry(currentTime); // Obtiene el tiempo promedio por entrada en la locación
            double avgContent = loc.getAverageContent(currentTime); // Obtiene el contenido promedio de la locación
            int maxContent = capacity; // Asigna la capacidad como contenido máximo
            int currentContent = loc.getCurrentContent(); // Obtiene el contenido actual de la locación
            double utilization = loc.getUtilization(currentTime); // Obtiene el porcentaje de utilización de la locación

            locationStatsList.add(new LocationStats( // Crea y agrega un nuevo objeto LocationStats a la lista con todos los valores obtenidos
                name, // Parámetro 1: nombre
                capacity, // Parámetro 2: capacidad
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

    private void updateEntityStats() { // Método privado que actualiza el área de texto con estadísticas de entidades
        Statistics stats = engine.getStatistics(); // Obtiene el objeto de estadísticas del motor

        StringBuilder sb = new StringBuilder(); // Crea un nuevo StringBuilder para construir el texto de forma eficiente
        sb.append("=== ESTADÍSTICAS DE ENTIDADES ===\n\n"); // Agrega el título de la sección
        sb.append(String.format("Total de Arribos: %d\n", stats.getTotalArrivals())); // Agrega el total de arribos formateado
        sb.append(String.format("Total de Salidas (Completadas): %d\n", stats.getTotalExits())); // Agrega el total de salidas formateado
        sb.append(String.format("En Sistema Actualmente: %d\n\n", stats.getTotalArrivals() - stats.getTotalExits())); // Agrega el número de entidades en sistema calculado como la diferencia

        sb.append(String.format("Throughput: %.2f piezas/hora\n\n", stats.getThroughput())); // Agrega el throughput formateado con 2 decimales
        sb.append("=== TIEMPO EN SISTEMA ===\n"); // Agrega el subtítulo de tiempo en sistema
        sb.append(String.format("Promedio: %.2f min\n", stats.getAverageSystemTime())); // Agrega el tiempo promedio en sistema formateado con 2 decimales
        sb.append(String.format("Desviación Estándar: %.2f min\n", stats.getStdDevSystemTime())); // Agrega la desviación estándar del tiempo en sistema formateada con 2 decimales
        sb.append(String.format("Mínimo: %.2f min\n", stats.getMinSystemTime())); // Agrega el tiempo mínimo en sistema formateado con 2 decimales
        sb.append(String.format("Máximo: %.2f min\n", stats.getMaxSystemTime())); // Agrega el tiempo máximo en sistema formateado con 2 decimales

        entityStatsText.setText(sb.toString()); // Establece el texto completo del área de texto con el contenido del StringBuilder
    } // Cierre del método updateEntityStats

    public static class LocationStats { // Declaración de clase estática pública interna LocationStats que representa las estadísticas de una locación para la tabla
        private final String name; // Variable final que almacena el nombre de la locación
        private final Integer capacity; // Variable final que almacena la capacidad de la locación
        private final Integer totalEntries; // Variable final que almacena el total de entradas a la locación
        private final Double timePerEntry; // Variable final que almacena el tiempo promedio por entrada
        private final Double avgContent; // Variable final que almacena el contenido promedio de la locación
        private final Integer maxContent; // Variable final que almacena el contenido máximo de la locación
        private final Integer currentContent; // Variable final que almacena el contenido actual de la locación
        private final Double utilization; // Variable final que almacena el porcentaje de utilización de la locación

        public LocationStats(String name, // Constructor público que inicializa una instancia de LocationStats recibiendo el nombre como primer parámetro
                              Integer capacity, // Segundo parámetro: capacidad
                              Integer totalEntries, // Tercer parámetro: entradas totales
                              Double timePerEntry, // Cuarto parámetro: tiempo por entrada
                              Double avgContent, // Quinto parámetro: contenido promedio
                              Integer maxContent, // Sexto parámetro: contenido máximo
                              Integer currentContent, // Séptimo parámetro: contenido actual
                              Double utilization) { // Octavo parámetro: utilización
            this.name = name; // Asigna el nombre recibido a la variable de instancia
            this.capacity = capacity == Integer.MAX_VALUE ? -1 : capacity; // Asigna la capacidad, convirtiendo Integer.MAX_VALUE a -1 para mostrar como infinito
            this.totalEntries = totalEntries; // Asigna las entradas totales recibidas a la variable de instancia
            this.timePerEntry = timePerEntry; // Asigna el tiempo por entrada recibido a la variable de instancia
            this.avgContent = avgContent; // Asigna el contenido promedio recibido a la variable de instancia
            this.maxContent = maxContent == Integer.MAX_VALUE ? -1 : maxContent; // Asigna el contenido máximo, convirtiendo Integer.MAX_VALUE a -1 para mostrar como infinito
            this.currentContent = currentContent; // Asigna el contenido actual recibido a la variable de instancia
            this.utilization = utilization; // Asigna la utilización recibida a la variable de instancia
        } // Cierre del constructor LocationStats

        public String getName() { return name; } // Método público getter que retorna el nombre de la locación
        public Integer getCapacity() { return capacity; } // Método público getter que retorna la capacidad de la locación
        public Integer getTotalEntries() { return totalEntries; } // Método público getter que retorna el total de entradas
        public Double getTimePerEntry() { return timePerEntry; } // Método público getter que retorna el tiempo por entrada
        public Double getAvgContent() { return avgContent; } // Método público getter que retorna el contenido promedio
        public Integer getMaxContent() { return maxContent; } // Método público getter que retorna el contenido máximo
        public Integer getCurrentContent() { return currentContent; } // Método público getter que retorna el contenido actual
        public Double getUtilization() { return utilization; } // Método público getter que retorna la utilización
    } // Cierre de la clase LocationStats
} // Cierre de la clase MainController
