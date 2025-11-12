package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase de parámetros de configuración
import com.simulation.core.DigemicEngine; // Importa el motor de simulación DIGEMIC
import com.simulation.resources.Location; // Importa la clase base de locaciones
import com.simulation.statistics.Statistics; // Importa la clase de estadísticas
import javafx.animation.AnimationTimer; // Importa la clase AnimationTimer de JavaFX para crear un loop de animación de alto rendimiento
import javafx.application.Platform; // Importa la clase Platform de JavaFX para ejecutar código en el hilo de la interfaz gráfica
import javafx.fxml.FXML; // Importa la anotación FXML para inyectar elementos definidos en archivos FXML
import javafx.scene.control.*; // Importa todas las clases de controles de JavaFX (Button, Label, TableView, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Importa la clase PropertyValueFactory para vincular propiedades de objetos con columnas de tablas
import javafx.scene.layout.BorderPane; // Importa la clase BorderPane de JavaFX para el layout principal

import java.util.ArrayList; // Importa la clase ArrayList para usar listas dinámicas
import java.util.List; // Importa la interfaz List para trabajar con colecciones tipo lista
import java.util.Map; // Importa la interfaz Map para trabajar con mapas clave-valor
import java.util.Optional; // Importa Optional para manejar valores que pueden estar presentes o ausentes

/**
 * Controlador principal con actualización en tiempo real de gráficas
 */
public class MainController { // Declaración de la clase pública MainController que controla la interfaz principal de la aplicación
    @FXML private BorderPane rootPane; // Variable privada anotada con @FXML que representa el panel raíz con layout BorderPane inyectado desde FXML
    @FXML private Button startButton; // Variable privada anotada con @FXML que representa el botón de inicio inyectado desde FXML
    @FXML private Button pauseButton; // Variable privada anotada con @FXML que representa el botón de pausa inyectado desde FXML
    @FXML private Button resetButton; // Variable privada anotada con @FXML que representa el botón de reinicio inyectado desde FXML
    @FXML private Button parametersButton; // Variable privada anotada con @FXML que representa el botón de parámetros inyectado desde FXML
    @FXML private Button zoomInButton; // Variable privada anotada con @FXML que representa el botón de zoom in inyectado desde FXML
    @FXML private Button zoomOutButton; // Variable privada anotada con @FXML que representa el botón de zoom out inyectado desde FXML
    @FXML private Button zoomResetButton; // Variable privada anotada con @FXML que representa el botón de reseteo de zoom inyectado desde FXML
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
    @FXML private TableColumn<LocationStats, String> locScheduledTimeCol; // Variable privada anotada con @FXML que representa la columna de tiempo programado inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locEntriesCol; // Variable privada anotada con @FXML que representa la columna de entradas totales inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locTimePerEntryCol; // Variable privada anotada con @FXML que representa la columna de tiempo por entrada inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locAvgContentCol; // Variable privada anotada con @FXML que representa la columna de contenido promedio inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locMaxContentCol; // Variable privada anotada con @FXML que representa la columna de contenido máximo inyectada desde FXML
    @FXML private TableColumn<LocationStats, Integer> locCurrentContentCol; // Variable privada anotada con @FXML que representa la columna de contenido actual inyectada desde FXML
    @FXML private TableColumn<LocationStats, Double> locUtilizationCol; // Variable privada anotada con @FXML que representa la columna de utilización inyectada desde FXML

    @FXML private TextArea entityStatsText; // Variable privada anotada con @FXML que representa el área de texto para estadísticas de entidades inyectada desde FXML

    private SimulationParameters parameters; // Variable privada que almacena los parámetros de configuración de la simulación
    private DigemicEngine engine; // Variable privada que almacena el motor de simulación DIGEMIC
    private AnimationPanel animationPanel; // Variable privada que almacena el panel de animación gráfica
    @FXML private ScrollPane animationScrollPane; // Variable privada anotada con @FXML que representa el contenedor con scroll para la animación inyectado desde FXML
    private RealTimeChartsPanel realTimeChartsPanel; // Variable privada que almacena el panel de gráficas en tiempo real
    private AnimationTimer animationTimer; // Variable privada que almacena el timer de animación para actualizar la interfaz periódicamente
    private Thread simulationThread; // Variable privada que almacena el hilo donde se ejecuta la simulación

    public void initialize() { // Método público que se ejecuta automáticamente después de cargar el FXML para inicializar el controlador
        parameters = new SimulationParameters(); // Crea una nueva instancia de SimulationParameters con valores por defecto
        
        engine = new DigemicEngine(parameters); // Crea una nueva instancia del motor DIGEMIC pasándole los parámetros

        setupAnimationPanel(); // Llama al método que configura el panel de animación
        setupRealTimeCharts(); // Llama al método que configura el panel de gráficas en tiempo real
        setupControls(); // Llama al método que configura los controles de la interfaz
        setupResultsTables(); // Llama al método que configura las tablas de resultados
        setupAnimationLoop(); // Llama al método que configura el loop de animación
        realTimeChartsPanel.initializeState(getStatistics()); // Inicializa el estado de las gráficas con las estadísticas actuales

        updateStatus("Listo para iniciar - Sistema DIGEMIC"); // Actualiza el mensaje de estado indicando que está listo
    }

    // === MÉTODOS HELPER PARA ACCESO AL MOTOR ===
    
    private Statistics getStatistics() { // Método privado helper que retorna el objeto de estadísticas del motor
        return ((DigemicEngine) engine).getStatistics(); // Hace casting a DigemicEngine y obtiene las estadísticas
    }

    private double getCurrentTime() { // Método privado helper que retorna el tiempo actual de simulación
        return ((DigemicEngine) engine).getCurrentTime(); // Hace casting a DigemicEngine y obtiene el tiempo actual
    }

    private boolean isRunning() { // Método privado helper que verifica si la simulación está en ejecución
        return ((DigemicEngine) engine).isRunning(); // Hace casting a DigemicEngine y retorna el estado de ejecución
    }

    private boolean isPaused() { // Método privado helper que verifica si la simulación está pausada
        return ((DigemicEngine) engine).isPaused(); // Hace casting a DigemicEngine y retorna el estado de pausa
    }

    private void setSimulationSpeed(double speed) { // Método privado helper que establece la velocidad de simulación
        ((DigemicEngine) engine).setSimulationSpeed(speed); // Hace casting a DigemicEngine y establece la velocidad
    }

    private void initializeEngine() { // Método privado helper que inicializa el motor de simulación
        ((DigemicEngine) engine).initialize(); // Hace casting a DigemicEngine y llama a initialize
    }

    private void runEngine() { // Método privado helper que ejecuta el motor de simulación
        ((DigemicEngine) engine).run(); // Hace casting a DigemicEngine y llama a run para iniciar simulación
    }

    private void stopEngine() { // Método privado helper que detiene el motor de simulación
        ((DigemicEngine) engine).stop(); // Hace casting a DigemicEngine y llama a stop para detener simulación
    }

    private void pauseEngine() { // Método privado helper que pausa el motor de simulación
        ((DigemicEngine) engine).pause(); // Hace casting a DigemicEngine y llama a pause
    }

    private void resumeEngine() { // Método privado helper que reanuda el motor de simulación
        ((DigemicEngine) engine).resume(); // Hace casting a DigemicEngine y llama a resume para continuar
    }

    private Location getLocation(String name) { // Método privado helper que obtiene una locación por nombre
        return ((DigemicEngine) engine).getLocation(name); // Hace casting a DigemicEngine y retorna la locación solicitada
    }

    private void setupAnimationPanel() { // Método privado que configura el panel de animación y lo agrega a la pestaña correspondiente
        animationPanel = new AnimationPanel(engine); // Crea una nueva instancia de AnimationPanel pasando el motor de simulación
        configureAnimationScrollPane(); // Configura las propiedades del ScrollPane definido en FXML
        animationScrollPane.setContent(animationPanel); // Inserta el panel de animación dentro del ScrollPane existente
        animationTab.setDisable(false); // Habilita la pestaña de animación para que pueda ser seleccionada
        animationPanel.render(); // Renderiza el panel de animación inicialmente para mostrar el estado inicial
    }

    private void configureAnimationScrollPane() { // Método privado que configura las propiedades del ScrollPane que contiene la animación
        if (animationScrollPane == null) { // Verifica si la referencia del ScrollPane es null
            return; // Sale del método para evitar NullPointerException si no se resolvió la inyección
        }
        animationScrollPane.setFitToWidth(true); // Establece que el contenido se ajuste automáticamente al ancho disponible
        animationScrollPane.setFitToHeight(false); // Mantiene el alto natural del contenido para permitir desplazamiento vertical
        animationScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Muestra barra de desplazamiento horizontal solo cuando se necesita
        animationScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED); // Muestra barra de desplazamiento vertical solo cuando se necesita
        animationScrollPane.setPannable(true); // Permite arrastrar la vista con el mouse para desplazarse
        animationScrollPane.setStyle("-fx-background-color: transparent;"); // Establece el fondo como transparente para combinar con el estilo general
    }

    private void setupRealTimeCharts() { // Método privado que configura el panel de gráficas en tiempo real
        realTimeChartsPanel = new RealTimeChartsPanel(); // Crea una nueva instancia de RealTimeChartsPanel
        if (chartsTab != null) { // Verifica si la pestaña de gráficas fue inyectada correctamente (no es null)
            chartsTab.setContent(realTimeChartsPanel); // Establece el contenido de la pestaña de gráficas con el panel de gráficas en tiempo real
            chartsTab.setDisable(false); // Habilita la pestaña de gráficas para que pueda ser seleccionada
        }
    }

    private void setupControls() { // Método privado que configura todos los controles de la interfaz (botones, sliders, listeners)
        startButton.setOnAction(e -> handleStart()); // Establece el manejador de eventos del botón de inicio para llamar a handleStart cuando se presiona
        pauseButton.setOnAction(e -> handlePause()); // Establece el manejador de eventos del botón de pausa para llamar a handlePause cuando se presiona
        resetButton.setOnAction(e -> handleReset()); // Establece el manejador de eventos del botón de reinicio para llamar a handleReset cuando se presiona
        parametersButton.setOnAction(e -> handleParameters()); // Establece el manejador de eventos del botón de parámetros para llamar a handleParameters cuando se presiona

        zoomInButton.setOnAction(e -> animationPanel.zoomIn()); // Establece el manejador del botón zoom in para aumentar el zoom del panel de animación
        zoomOutButton.setOnAction(e -> animationPanel.zoomOut()); // Establece el manejador del botón zoom out para disminuir el zoom del panel de animación
        zoomResetButton.setOnAction(e -> animationPanel.resetZoom()); // Establece el manejador del botón reset zoom para volver al 100%

        pauseButton.setDisable(true); // Deshabilita el botón de pausa inicialmente porque la simulación no está corriendo

        speedSlider.setMin(0.0); // Establece el valor mínimo del slider en 0.0 para permitir pausar la simulación
        speedSlider.setMax(200); // Establece el valor máximo del slider en 200 para velocidad máxima moderada
        speedSlider.setValue(0.4); // Establece el valor inicial del slider en 0.4 (velocidad muy lenta para visualizar flujo)
        speedSlider.setBlockIncrement(0.05); // Establece el incremento de bloque del slider en 0.05 para ajustes finos

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> { // Agrega un listener que detecta cambios en el valor del slider
            double speed = Math.max(0.0, newVal.doubleValue()); // Calcula la velocidad asegurando que no sea negativa
            setSimulationSpeed(speed); // Establece la nueva velocidad en el motor de simulación
            updateSpeedLabel(speed); // Actualiza la etiqueta de velocidad con el nuevo valor
        });

        setSimulationSpeed(speedSlider.getValue()); // Establece la velocidad inicial del motor según el valor del slider
        updateSpeedLabel(speedSlider.getValue()); // Actualiza la etiqueta de velocidad con el valor inicial
    }

    private void setupResultsTables() { // Método privado que configura las tablas de resultados vinculando columnas con propiedades y formateando celdas
        locNameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // Vincula la columna de nombre con la propiedad "name" de LocationStats
        locCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity")); // Vincula la columna de capacidad con la propiedad "capacity" de LocationStats
        locScheduledTimeCol.setCellValueFactory(new PropertyValueFactory<>("scheduledTime")); // Vincula la columna de tiempo programado con la propiedad "scheduledTime"
        locEntriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries")); // Vincula la columna de entradas con la propiedad "totalEntries" de LocationStats
        locTimePerEntryCol.setCellValueFactory(new PropertyValueFactory<>("timePerEntry")); // Vincula la columna de tiempo por entrada con la propiedad "timePerEntry" de LocationStats
        locAvgContentCol.setCellValueFactory(new PropertyValueFactory<>("avgContent")); // Vincula la columna de contenido promedio con la propiedad "avgContent" de LocationStats
        locMaxContentCol.setCellValueFactory(new PropertyValueFactory<>("maxContent")); // Vincula la columna de contenido máximo con la propiedad "maxContent" de LocationStats
        locCurrentContentCol.setCellValueFactory(new PropertyValueFactory<>("currentContent")); // Vincula la columna de contenido actual con la propiedad "currentContent" de LocationStats
        locUtilizationCol.setCellValueFactory(new PropertyValueFactory<>("utilization")); // Vincula la columna de utilización con la propiedad "utilization" de LocationStats

        locTimePerEntryCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de tiempo por entrada
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                }
            }
        });

        locAvgContentCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de contenido promedio
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                }
            }
        });

        locUtilizationCol.setCellFactory(col -> new TableCell<LocationStats, Double>() { // Establece una fábrica de celdas personalizada para formatear la columna de utilización con colores
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f%%", item)); // Formatea el valor como string con 2 decimales y símbolo de porcentaje

                    if (item < 50) { // Verifica si la utilización es menor al 50%
                        setStyle("-fx-background-color: #C8E6C9;"); // Establece el fondo de la celda en verde claro para utilización baja
                    } else if (item < 80) { // Verifica si la utilización está entre 50% y 80%
                        setStyle("-fx-background-color: #FFF9C4;"); // Establece el fondo de la celda en amarillo claro para utilización media
                    } else { // Bloque else que se ejecuta si la utilización es mayor o igual al 80%
                        setStyle("-fx-background-color: #FFCDD2;"); // Establece el fondo de la celda en rojo claro para utilización alta
                    }
                }
            }
        });
    }

    private void setupAnimationLoop() { // Método privado que configura el loop de animación que actualiza la interfaz periódicamente
        animationTimer = new AnimationTimer() { // Crea una nueva instancia anónima de AnimationTimer
            private long lastUpdate = 0; // Variable privada que almacena el tiempo del último update en nanosegundos, inicializada en 0
            private static final long UPDATE_INTERVAL = 50_000_000; // Constante estática final que define el intervalo entre updates en nanosegundos (50ms)

            @Override // Anotación que indica sobrescritura del método abstracto handle de AnimationTimer
            public void handle(long now) { // Método que se ejecuta en cada frame de animación recibiendo el timestamp actual en nanosegundos
                if (isRunning() && now - lastUpdate >= UPDATE_INTERVAL) { // Verifica si la simulación está corriendo y si ha pasado el intervalo mínimo
                    updateDisplay(); // Llama al método que actualiza los elementos visuales de la interfaz
                    updateRealTimeCharts(); // Llama al método que actualiza las gráficas en tiempo real
                    lastUpdate = now; // Actualiza la marca de tiempo del último update con el tiempo actual
                }
            }
        };
    }

    @FXML // Anotación que indica que este método es manejador de evento definido en FXML
    private void handleStart() { // Método privado que maneja el evento de clic en el botón de inicio
        if (isRunning()) { // Verifica si la simulación ya está en ejecución
            return; // Sale del método sin hacer nada si ya está corriendo
        }

        startButton.setDisable(true); // Deshabilita el botón de inicio para evitar múltiples inicios
        pauseButton.setDisable(false); // Habilita el botón de pausa
        resetButton.setDisable(true); // Deshabilita el botón de reinicio durante la ejecución
        parametersButton.setDisable(true); // Deshabilita el botón de parámetros durante la ejecución

        initializeEngine(); // Inicializa el motor de simulación preparándolo para ejecutar
        setSimulationSpeed(speedSlider.getValue()); // Establece la velocidad de simulación según el valor del slider

        realTimeChartsPanel.initializeState(getStatistics()); // Inicializa el estado de las gráficas con estadísticas actuales

        simulationThread = new Thread(() -> { // Crea un nuevo hilo para ejecutar la simulación sin bloquear la interfaz
            runEngine(); // Ejecuta el motor de simulación en este hilo separado

            Platform.runLater(() -> { // Programa la ejecución en el hilo de JavaFX cuando termine la simulación
                handleSimulationComplete(); // Llama al método que maneja la finalización de la simulación
            });
        });

        simulationThread.setDaemon(true); // Configura el hilo como daemon para que no impida el cierre de la aplicación
        simulationThread.start(); // Inicia la ejecución del hilo de simulación

        animationTimer.start(); // Inicia el timer de animación para comenzar a actualizar la interfaz periódicamente
        updateStatus("Simulación en ejecución..."); // Actualiza el mensaje de estado indicando que la simulación está corriendo
    }

    @FXML // Anotación que indica que este método es manejador de evento definido en FXML
    private void handlePause() { // Método privado que maneja el evento de clic en el botón de pausa/reanudar
        if (isPaused()) { // Verifica si la simulación está actualmente pausada
            resumeEngine(); // Reanuda la ejecución de la simulación
            pauseButton.setText("Pausar"); // Cambia el texto del botón a "Pausar"
            updateStatus("Simulación en ejecución..."); // Actualiza el mensaje de estado indicando que está corriendo
        } else { // Bloque else si la simulación está corriendo
            pauseEngine(); // Pausa la ejecución de la simulación
            pauseButton.setText("Reanudar"); // Cambia el texto del botón a "Reanudar"
            updateStatus("Simulación pausada"); // Actualiza el mensaje de estado indicando que está pausada
        }
    }

    @FXML // Anotación que indica que este método es manejador de evento definido en FXML
    private void handleReset() { // Método privado que maneja el evento de clic en el botón de reinicio
        if (animationTimer != null) { // Verifica si el timer de animación existe
            animationTimer.stop(); // Detiene el timer de animación
        }

        if (simulationThread != null && simulationThread.isAlive()) { // Verifica si el hilo de simulación existe y está vivo
            stopEngine(); // Detiene el motor de simulación
            try { // Bloque try para manejar la interrupción del hilo
                simulationThread.join(1000); // Espera máximo 1000ms (1 segundo) a que el hilo termine
            } catch (InterruptedException e) { // Captura excepción si el hilo actual es interrumpido
                Thread.currentThread().interrupt(); // Restaura el estado de interrupción del hilo
            }
        }

        engine = new DigemicEngine(parameters); // Recrea el motor DIGEMIC con los parámetros actuales
        setSimulationSpeed(speedSlider.getValue()); // Restablece la velocidad según el slider
        updateSpeedLabel(speedSlider.getValue()); // Actualiza la etiqueta de velocidad
        
        setupAnimationPanel(); // Reconfigura el panel de animación con el nuevo motor
        realTimeChartsPanel.initializeState(getStatistics()); // Reinicializa las gráficas con estadísticas vacías

        startButton.setDisable(false); // Habilita el botón de inicio
        pauseButton.setDisable(true); // Deshabilita el botón de pausa porque no hay simulación corriendo
        pauseButton.setText("Pausar"); // Restablece el texto del botón de pausa a "Pausar"
        resetButton.setDisable(false); // Habilita el botón de reinicio
        parametersButton.setDisable(false); // Habilita el botón de parámetros para permitir cambios

        updateTimeLabel(0); // Restablece la etiqueta de tiempo a 00:00
        updateStatus("Listo para iniciar"); // Actualiza el mensaje de estado indicando que está listo

        locationTable.getItems().clear(); // Limpia todos los items de la tabla de locaciones
        entityStatsText.clear(); // Limpia el área de texto de estadísticas de entidades
    }

    @FXML // Anotación que indica que este método es manejador de evento definido en FXML
    private void handleParameters() { // Método privado que maneja el evento de clic en el botón de parámetros
        if (engine != null && isRunning()) { // Verifica si el motor existe y la simulación está corriendo
            updateStatus("Detén la simulación antes de cambiar parámetros"); // Muestra mensaje de error indicando que debe detener primero
            return; // Sale del método sin abrir el diálogo
        }

        ParametersDialog dialog = new ParametersDialog(parameters); // Crea un nuevo diálogo de parámetros pasando los parámetros actuales
        Optional<Boolean> result = dialog.showAndWait(); // Muestra el diálogo y espera a que el usuario lo cierre, retorna el resultado
        if (result.orElse(false)) { // Verifica si el usuario aceptó los cambios (retorna true)
            engine = new DigemicEngine(parameters); // Recrea el motor con los nuevos parámetros modificados
            setSimulationSpeed(speedSlider.getValue()); // Establece la velocidad según el slider
            updateSpeedLabel(speedSlider.getValue()); // Actualiza la etiqueta de velocidad

            setupAnimationPanel(); // Reconfigura el panel de animación con el nuevo motor
            realTimeChartsPanel.initializeState(getStatistics()); // Reinicializa las gráficas
            locationTable.getItems().clear(); // Limpia la tabla de locaciones
            entityStatsText.clear(); // Limpia el área de estadísticas

            updateTimeLabel(0); // Reinicia la etiqueta de tiempo a 00:00
            updateStatus("Parámetros actualizados"); // Actualiza el mensaje de estado indicando que los parámetros fueron actualizados
        } else { // Bloque else si el usuario canceló los cambios
            updateStatus("Cambios de parámetros cancelados"); // Actualiza el mensaje de estado indicando que se canceló
        }
    }

    private void handleSimulationComplete() { // Método privado que maneja la finalización de la simulación
        animationTimer.stop(); // Detiene el timer de animación

        startButton.setDisable(true); // Deshabilita el botón de inicio
        pauseButton.setDisable(true); // Deshabilita el botón de pausa
        resetButton.setDisable(false); // Habilita el botón de reinicio
        parametersButton.setDisable(false); // Habilita el botón de parámetros

        updateStatus("Simulación completada"); // Actualiza el mensaje de estado indicando que completó
        updateTimeLabel(getCurrentTime()); // Actualiza la etiqueta de tiempo con el tiempo final
        updateResults(); // Actualiza todas las tablas y áreas de resultados

        updateRealTimeCharts(); // Actualiza las gráficas una última vez con datos finales

        ResultsDialog resultsDialog = new ResultsDialog(getStatistics(), getCurrentTime()); // Crea diálogo de resultados finales con estadísticas y tiempo
        resultsDialog.show(); // Muestra el diálogo de resultados al usuario

        mainTabPane.getSelectionModel().select(chartsTab); // Selecciona automáticamente la pestaña de gráficas
    }

    private void updateDisplay() { // Método privado que actualiza todos los elementos visuales de la interfaz
        double currentTime = getCurrentTime(); // Obtiene el tiempo actual de simulación del motor

        Platform.runLater(() -> { // Ejecuta el siguiente código en el hilo de JavaFX para thread-safety
            updateTimeLabel(currentTime); // Actualiza la etiqueta de tiempo con el tiempo actual

            animationPanel.render(); // Renderiza el panel de animación con el estado actual del motor

            updateLocationStats(); // Actualiza la tabla de estadísticas de locaciones en tiempo real
        });
    }

    private void updateTimeLabel(double currentTime) { // Método privado que actualiza la etiqueta de tiempo con formato HH:MM
        int totalMinutes = (int) Math.floor(currentTime); // Convierte el tiempo a minutos enteros redondeando hacia abajo
        int hours = totalMinutes / 60; // Calcula las horas dividiendo minutos entre 60
        int minutes = totalMinutes % 60; // Calcula los minutos residuales usando módulo
        timeLabel.setText(String.format("Tiempo: %02d:%02d h", hours, minutes)); // Formatea y establece el texto en formato HH:MM
    }

    private void updateRealTimeCharts() { // Método privado que actualiza las gráficas en tiempo real durante la simulación
        Statistics stats = getStatistics(); // Obtiene el objeto de estadísticas del motor
        double currentTime = getCurrentTime(); // Obtiene el tiempo actual de simulación

        Platform.runLater(() -> { // Ejecuta en el hilo de JavaFX para thread-safety
            realTimeChartsPanel.updateCharts(stats, currentTime); // Actualiza las gráficas con las estadísticas y tiempo actuales
        });
    }

    private void updateStatus(String message) { // Método privado que actualiza el mensaje de estado recibiendo el mensaje como parámetro
        statusLabel.setText("Estado: " + message); // Establece el texto de la etiqueta de estado concatenando "Estado: " con el mensaje recibido
    }

    private void updateSpeedLabel(double speed) { // Método privado que actualiza la etiqueta de velocidad con descriptor textual
        String descriptor; // Declara variable para almacenar el descriptor textual de la velocidad
        if (speed == 0.0) { // Verifica si la velocidad es exactamente 0
            descriptor = "pausada"; // Establece descriptor como "pausada"
        } else if (speed <= 0.1) { // Verifica si la velocidad es menor o igual a 0.1
            descriptor = "ultra lenta"; // Establece descriptor como "ultra lenta"
        } else if (speed <= 0.5) { // Verifica si la velocidad está entre 0.1 y 0.5
            descriptor = "muy lenta"; // Establece descriptor como "muy lenta"
        } else if (speed <= 1.5) { // Verifica si la velocidad está entre 0.5 y 1.5
            descriptor = "lenta"; // Establece descriptor como "lenta"
        } else if (speed <= 5) { // Verifica si la velocidad está entre 1.5 y 5
            descriptor = "normal"; // Establece descriptor como "normal"
        } else if (speed <= 20) { // Verifica si la velocidad está entre 5 y 20
            descriptor = "rápida"; // Establece descriptor como "rápida"
        } else { // Bloque else para velocidades mayores a 20
            descriptor = "turbo"; // Establece descriptor como "turbo"
        }

        speedLabel.setText(String.format("Velocidad: %.2fx (%s)", speed, descriptor)); // Formatea y establece el texto con velocidad numérica y descriptor
    }

    private void updateResults() { // Método privado que actualiza todas las tablas y áreas de texto de resultados
        updateLocationStats(); // Llama al método para actualizar la tabla de estadísticas de locaciones
        updateEntityStats(); // Llama al método para actualizar el área de texto de estadísticas de entidades
    }

    private void updateLocationStats() { // Método privado que actualiza la tabla de estadísticas de locaciones con datos actuales
        Statistics stats = getStatistics(); // Obtiene el objeto de estadísticas del motor
        double currentTime = getCurrentTime(); // Obtiene el tiempo actual de simulación

        List<LocationStats> locationStatsList = new ArrayList<>(); // Crea lista para almacenar objetos LocationStats
        Map<String, Location> locations = stats.getLocations(); // Obtiene el mapa de todas las locaciones desde estadísticas
        String scheduledTime = formatScheduledHours(parameters.getSimulationDurationMinutes()); // Formatea la duración programada en horas
        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Itera sobre cada entrada del mapa de locaciones
            Location loc = entry.getValue(); // Obtiene el objeto Location de la entrada actual

            String name = loc.getName(); // Obtiene el nombre de la locación
            int rawCapacity = loc.getCapacity(); // Obtiene la capacidad cruda de la locación
            String capacityDisplay = formatCapacity(rawCapacity); // Formatea la capacidad para mostrar (∞ si es MAX_VALUE)
            int totalEntries = loc.getTotalEntries(); // Obtiene el total de entradas a la locación
            double timePerEntry = loc.getAverageTimePerEntry(currentTime); // Obtiene el tiempo promedio por entrada
            double avgContent = loc.getAverageContent(currentTime); // Obtiene el contenido promedio en la locación
            int maxContent = loc.getMaxContent(); // Obtiene el contenido máximo observado
            int currentContent = loc.getCurrentContent(); // Obtiene el contenido actual de la locación
            double utilization = loc.getUtilization(currentTime); // Obtiene el porcentaje de utilización

            locationStatsList.add(new LocationStats( // Crea y agrega nuevo objeto LocationStats a la lista
                name, // Parámetro 1: nombre de la locación
                capacityDisplay, // Parámetro 2: capacidad formateada
                scheduledTime, // Parámetro 3: tiempo programado en horas
                totalEntries, // Parámetro 4: entradas totales
                timePerEntry, // Parámetro 5: tiempo promedio por entrada
                avgContent, // Parámetro 6: contenido promedio
                maxContent, // Parámetro 7: contenido máximo
                currentContent, // Parámetro 8: contenido actual
                utilization // Parámetro 9: porcentaje de utilización
            ));
        }

        locationTable.getItems().setAll(locationStatsList); // Reemplaza todos los items de la tabla con la nueva lista de estadísticas
    }

    private void updateEntityStats() { // Método privado que actualiza el área de texto con estadísticas de entidades
        Statistics stats = getStatistics(); // Obtiene el objeto de estadísticas del motor

        StringBuilder sb = new StringBuilder(); // Crea StringBuilder para construir el texto de estadísticas eficientemente
        sb.append("=== ESTADÍSTICAS DE ENTIDADES ===\n\n"); // Agrega título de la sección
        sb.append(String.format("Total de Arribos: %d\n", stats.getTotalArrivals())); // Agrega total de arribos formateado
        sb.append(String.format("Total de Salidas (Completadas): %d\n", stats.getTotalExits())); // Agrega total de salidas formateado
        sb.append(String.format("En Sistema Actualmente: %d\n\n", stats.getTotalArrivals() - stats.getTotalExits())); // Agrega cantidad en sistema (arribos - salidas)

        sb.append(String.format("Throughput: %.2f piezas/hora\n\n", stats.getThroughput())); // Agrega throughput formateado con 2 decimales
        sb.append("=== TIEMPO EN SISTEMA ===\n"); // Agrega subtítulo de tiempo en sistema
        sb.append(String.format("Promedio: %.2f min\n", stats.getAverageSystemTime())); // Agrega tiempo promedio en sistema
        sb.append(String.format("Desviación Estándar: %.2f min\n", stats.getStdDevSystemTime())); // Agrega desviación estándar
        sb.append(String.format("Mínimo: %.2f min\n", stats.getMinSystemTime())); // Agrega tiempo mínimo observado
        sb.append(String.format("Máximo: %.2f min\n", stats.getMaxSystemTime())); // Agrega tiempo máximo observado

        entityStatsText.setText(sb.toString()); // Establece el texto construido en el área de texto
    }

    public static class LocationStats { // Declaración de clase estática pública interna LocationStats que representa las estadísticas de una locación para la tabla
        private final String name; // Variable final que almacena el nombre de la locación
        private final String capacity; // Variable final que almacena la capacidad formateada para mostrar
        private final String scheduledTime; // Variable final que almacena el tiempo programado en horas
        private final Integer totalEntries; // Variable final que almacena el total de entradas a la locación
        private final Double timePerEntry; // Variable final que almacena el tiempo promedio por entrada
        private final Double avgContent; // Variable final que almacena el contenido promedio de la locación
        private final Integer maxContent; // Variable final que almacena el contenido máximo observado
        private final Integer currentContent; // Variable final que almacena el contenido actual de la locación
        private final Double utilization; // Variable final que almacena el porcentaje de utilización de la locación

        public LocationStats(String name, // Constructor público que inicializa una instancia de LocationStats recibiendo nombre
                              String capacity, // Segundo parámetro: capacidad formateada
                              String scheduledTime, // Tercer parámetro: tiempo programado
                              Integer totalEntries, // Cuarto parámetro: entradas totales
                              Double timePerEntry, // Quinto parámetro: tiempo por entrada
                              Double avgContent, // Sexto parámetro: contenido promedio
                              Integer maxContent, // Séptimo parámetro: contenido máximo
                              Integer currentContent, // Octavo parámetro: contenido actual
                              Double utilization) { // Noveno parámetro: utilización
            this.name = name; // Asigna el nombre recibido a la variable de instancia
            this.capacity = capacity; // Asigna la capacidad formateada a la variable de instancia
            this.scheduledTime = scheduledTime; // Asigna el tiempo programado a la variable de instancia
            this.totalEntries = totalEntries; // Asigna las entradas totales a la variable de instancia
            this.timePerEntry = timePerEntry; // Asigna el tiempo por entrada a la variable de instancia
            this.avgContent = avgContent; // Asigna el contenido promedio a la variable de instancia
            this.maxContent = maxContent; // Asigna el contenido máximo a la variable de instancia
            this.currentContent = currentContent; // Asigna el contenido actual a la variable de instancia
            this.utilization = utilization; // Asigna la utilización a la variable de instancia
        }

        public String getName() { return name; } // Método público getter que retorna el nombre de la locación
        public String getCapacity() { return capacity; } // Método público getter que retorna la capacidad formateada
        public String getScheduledTime() { return scheduledTime; } // Método público getter que retorna el tiempo programado
        public Integer getTotalEntries() { return totalEntries; } // Método público getter que retorna el total de entradas
        public Double getTimePerEntry() { return timePerEntry; } // Método público getter que retorna el tiempo por entrada
        public Double getAvgContent() { return avgContent; } // Método público getter que retorna el contenido promedio
        public Integer getMaxContent() { return maxContent; } // Método público getter que retorna el contenido máximo
        public Integer getCurrentContent() { return currentContent; } // Método público getter que retorna el contenido actual
        public Double getUtilization() { return utilization; } // Método público getter que retorna la utilización
    }

    private String formatCapacity(int capacity) { // Método privado que formatea la capacidad para mostrar en la tabla
        if (capacity == Integer.MAX_VALUE) { // Verifica si la capacidad es el valor máximo de entero (infinito)
            return "999.999,00"; // Retorna formato estilo ProModel para capacidades infinitas
        }
        return String.valueOf(capacity); // Retorna el valor de capacidad como string si es finito
    }

    private String formatScheduledHours(double durationMinutes) { // Método privado que convierte minutos programados a horas con dos decimales
        double hours = durationMinutes / 60.0; // Convierte minutos a horas dividiendo entre 60
        return String.format("%.2f", hours); // Retorna las horas formateadas con 2 decimales
    }
}
