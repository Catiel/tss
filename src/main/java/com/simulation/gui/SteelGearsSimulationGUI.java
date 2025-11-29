package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.core.SimulationListener;
import com.simulation.entities.Entity;
import com.simulation.locations.Location;
import com.simulation.processing.ProcessingRule;
import com.simulation.resources.Resource;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import com.simulation.processing.BatchProcessingRule;

public class SteelGearsSimulationGUI extends Application implements SimulationListener {

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private final double endTime = 60000.0; // 1000 horas = 60000 min
    // Mapa de posiciones de locaciones
    private final Map<String, Point2D> locationPositions = new HashMap<>();
    private SimulationEngine engine;
    private Canvas canvas;
    private GraphicsContext gc;
    // Controles
    private Button startButton, pauseButton, resetButton, configButton, stopButton;
    private Slider speedSlider;
    private Label timeLabel, statusLabel;
    private ProgressBar progressBar;
    private TableView<LocationStatsRow> locationStatsTable;
    private TableView<EntityStatsRow> entityStatsTable;
    private BarChart<String, Number> locationExitsChart;
    private Stage primaryStage;
    // Animación
    private AnimationTimer animationTimer;
    private Thread simulationThread;
    private double simulationSpeed = 1.0;
    private double currentTime = 0;
    private long lastUIUpdate = 0; // Control de frecuencia de actualización UI
    // Sistema de visualización
    private VisualLocationManager locationManager;
    private VisualEntityManager entityManager;
    private VisualResourceManager resourceManager;
    // Gestor de rutas
    private PathNetworkManager pathManager;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Simulador de Manufactura - Engranes de Acero SA");

        // Crear motor de simulación
        setupSimulationEngine();

        // Crear layout principal
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e 0%, #16213e 100%);");

        // Panel superior - controles
        root.setTop(createControlPanel());

        // Centro - TabPane con animación y estadísticas
        root.setCenter(createMainTabPane());

        // Panel inferior - información
        root.setBottom(createInfoPanel());

        // Crear escena
        Scene scene = new Scene(root, 1600, 900);
        scene.getStylesheets().add(getClass().getResource("/styles/brewery-simulation.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();

        // Iniciar animación visual
        startAnimationTimer();

        primaryStage.setOnCloseRequest(event -> {
            running.set(false);
            if (simulationThread != null)
                simulationThread.interrupt();
            if (animationTimer != null)
                animationTimer.stop();
            Platform.exit();
            System.exit(0);
        });
    }

    private VBox createControlPanel() {
        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(15));
        controlPanel.setStyle("-fx-background-color: linear-gradient(to bottom, #34495E, #2C3E50);");

        // Título
        Label title = new Label("⚙ SIMULADOR ENGRANES DE ACERO SA");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        title.setTextFill(Color.web("#ECF0F1"));

        // Controles de simulación
        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);

        startButton = new Button("▶ Iniciar");
        startButton.setStyle(
                "-fx-background-color: #27AE60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        startButton.setOnAction(e -> startSimulation());

        pauseButton = new Button("⏸ Pausar");
        pauseButton.setStyle(
                "-fx-background-color: #F39C12; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> pauseSimulation());

        stopButton = new Button("⏹ Detener");
        stopButton.setStyle(
                "-fx-background-color: #E74C3C; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> stopSimulation());

        configButton = new Button("⚙ Configuración");
        configButton.setStyle(
                "-fx-background-color: #9B59B6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        configButton.setOnAction(e -> openConfigurationDialog());

        resetButton = new Button("🔄 Reiniciar");
        resetButton.setStyle(
                "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        resetButton.setOnAction(e -> resetSimulation());

        Label speedLabel = new Label("Velocidad:");
        speedLabel.setTextFill(Color.web("#ECF0F1"));
        speedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        speedSlider = new Slider(0.1, 100.0, 1.0); // Increased max speed to 100.0
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(25.0);
        speedSlider.setPrefWidth(200);
        speedSlider.valueProperty().addListener((obs, old, newVal) -> simulationSpeed = newVal.doubleValue());

        controls.getChildren().addAll(startButton, pauseButton, stopButton, resetButton, configButton,
                new Separator(), speedLabel, speedSlider);

        // Información de tiempo y progreso
        HBox timeInfo = new HBox(15);
        timeInfo.setAlignment(Pos.CENTER_LEFT);

        timeLabel = new Label("Tiempo: 0.00 min (0.00 hrs)");
        timeLabel.setTextFill(Color.web("#ECF0F1"));
        timeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        statusLabel = new Label("Listo para iniciar");
        statusLabel.setTextFill(Color.web("#2ECC71"));
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(300);

        timeInfo.getChildren().addAll(timeLabel, progressBar, statusLabel);

        controlPanel.getChildren().addAll(title, controls, timeInfo);
        return controlPanel;
    }

    private ScrollPane createAnimationCanvas() {
        canvas = new Canvas(2000, 1200);
        gc = canvas.getGraphicsContext2D();

        // Inicializar managers visuales
        setupLocationPositions();
        pathManager = new PathNetworkManager(); // Inicializar gestor de rutas
        locationManager = new VisualLocationManager(locationPositions);
        entityManager = new VisualEntityManager();
        // resourceManager se inicializa después de setupSimulationEngine()

        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setStyle("-fx-background: #1A252F;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        return scrollPane;
    }

    private TabPane createMainTabPane() {
        TabPane mainTabPane = new TabPane();
        mainTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        mainTabPane.setStyle("-fx-background-color: #1a1a2e;");

        // Tab 1: Animación + Tablas de Estadísticas
        Tab animationTab = new Tab("🎬 Simulación en Vivo");
        animationTab.setContent(createAnimationWithStatsView());
        animationTab.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Tab 2: Gráficas de Barras
        Tab chartsTab = new Tab("📊 Gráficas de Análisis");
        chartsTab.setContent(createChartsView());
        chartsTab.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        mainTabPane.getTabs().addAll(animationTab, chartsTab);

        return mainTabPane;
    }

    private BorderPane createAnimationWithStatsView() {
        BorderPane view = new BorderPane();
        view.setStyle("-fx-background-color: #1a1a2e;");

        // Canvas de animación en el centro
        ScrollPane canvasScroll = createAnimationCanvas();
        view.setCenter(canvasScroll);

        // Tablas de estadísticas en el lado derecho con menor ancho
        VBox statsBox = new VBox(10);
        statsBox.setPadding(new Insets(10));
        statsBox.setPrefWidth(650); // Ancho reducido para las tablas
        statsBox.setStyle("-fx-background-color: #16213e;");

        // TabPane para las dos tablas
        TabPane statsTabPane = new TabPane();
        statsTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Crear tablas
        locationStatsTable = createLocationStatsTable();
        entityStatsTable = createEntityStatsTable();

        // Tab de locaciones
        Tab locationTab = new Tab("📍 Locaciones");
        ScrollPane locScroll = new ScrollPane(locationStatsTable);
        locScroll.setFitToWidth(true);
        locScroll.setFitToHeight(true);
        locationTab.setContent(locScroll);

        // Tab de entidades
        Tab entityTab = new Tab("📦 Entidades");
        ScrollPane entScroll = new ScrollPane(entityStatsTable);
        entScroll.setFitToWidth(true);
        entScroll.setFitToHeight(true);
        entityTab.setContent(entScroll);

        statsTabPane.getTabs().addAll(locationTab, entityTab);
        VBox.setVgrow(statsTabPane, Priority.ALWAYS);

        // Header con título y botón
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.setPadding(new Insets(5, 0, 5, 0));

        Label statsTitle = new Label("📊 Estadísticas en Tiempo Real");
        statsTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        statsTitle.setTextFill(Color.web("#e94560"));

        // Botón para abrir ventana grande
        Button expandButton = new Button("🔍 Ver en Ventana Grande");
        expandButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e94560 0%, #c13551 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 12px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 8 15 8 15;" +
                        "-fx-background-radius: 6;" +
                        "-fx-cursor: hand;");
        expandButton.setOnAction(e -> openExpandedTablesWindow());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerBox.getChildren().addAll(statsTitle, spacer, expandButton);

        statsBox.getChildren().addAll(headerBox, statsTabPane);

        view.setRight(statsBox);

        return view;
    }

    private VBox createChartsView() {
        VBox view = new VBox(20);
        view.setPadding(new Insets(20));
        view.setStyle("-fx-background-color: linear-gradient(to bottom, #0f3460 0%, #16213e 100%);");

        // Título
        Label title = new Label("📊 Análisis de Flujo por Locación");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#e94560"));

        // Descripción
        Label description = new Label(
                "Gráfica de barras mostrando la cantidad total de entidades que pasaron por cada locación");
        description.setFont(Font.font("Segoe UI", 14));
        description.setTextFill(Color.web("#a8dadc"));
        description.setWrapText(true);

        // Crear gráfica de barras
        locationExitsChart = createLocationExitsChart();
        VBox.setVgrow(locationExitsChart, Priority.ALWAYS);

        view.getChildren().addAll(title, description, locationExitsChart);
        return view;
    }

    private BarChart<String, Number> createLocationExitsChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Locaciones de Producción");
        xAxis.setStyle("-fx-tick-label-fill: #f1faee; -fx-font-size: 11px; -fx-font-weight: bold;");
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelGap(5);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Total de Entradas");
        yAxis.setStyle("-fx-tick-label-fill: #f1faee; -fx-font-size: 12px; -fx-font-weight: bold;");
        yAxis.setAutoRanging(true);
        yAxis.setForceZeroInRange(true);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number value) {
                return String.format("%d", value.intValue());
            }
        });

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Flujo de Entidades: Total de Entradas por Locación");
        barChart.setLegendVisible(false);
        barChart.setAnimated(false); // Deshabilitar animación para evitar parpadeos
        barChart.setCategoryGap(10);
        barChart.setBarGap(3);
        barChart.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 3;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 15;");

        // Serie de datos (se actualizará en updateStatistics)
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Total Entradas");
        barChart.getData().add(series);

        return barChart;
    }

    private HBox createInfoPanel() {
        HBox infoPanel = new HBox(20);
        infoPanel.setPadding(new Insets(10));
        infoPanel.setAlignment(Pos.CENTER);
        infoPanel.setStyle("-fx-background-color: #2C3E50;");

        Label legend = new Label("🟢 Disponible  🟡 Procesando  🔴 Saturado  🔵 En Cola");
        legend.setTextFill(Color.web("#BDC3C7"));
        legend.setFont(Font.font("Arial", 11));

        infoPanel.getChildren().add(legend);
        return infoPanel;
    }

    private void setupSimulationEngine() {
        engine = new SimulationEngine();
        setupEntityTypes(engine);
        setupLocations(engine);
        setupResources(engine);
        setupProcessingRules(engine);
        setupArrivals(engine);

        // Configurar tiempo final de simulación
        engine.setEndTime(endTime);

        // Registrar este GUI como listener de eventos de simulación
        engine.addListener(this);

        // Inicializar resource manager después de crear recursos
        resourceManager = new VisualResourceManager(engine);
    }

    private void setupEntityTypes(SimulationEngine engine) {
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0);
    }

    private void setupLocations(SimulationEngine engine) {
        engine.addLocation("ALMACEN_MP", Integer.MAX_VALUE, 1);
        engine.addLocation("HORNO", 10, 1);
        engine.addLocation("BANDA_1", Integer.MAX_VALUE, 1);
        engine.addLocation("CARGA", Integer.MAX_VALUE, 1);
        engine.addLocation("TORNEADO", 1, 1);
        engine.addLocation("FRESADO", 1, 1);
        engine.addLocation("TALADRO", 1, 1);
        engine.addLocation("RECTIFICADO", 1, 1);
        engine.addLocation("DESCARGA", Integer.MAX_VALUE, 1);
        engine.addLocation("BANDA_2", Integer.MAX_VALUE, 1);
        engine.addLocation("INSPECCION", 1, 1);
        engine.addLocation("SALIDA", Integer.MAX_VALUE, 1);
    }

    private void setupResources(SimulationEngine engine) {
        engine.addResource("GRUA_VIAJERA", 1, 25.0);
        engine.addResource("ROBOT", 1, 45.0);
    }

    private void setupProcessingRules(SimulationEngine engine) {
        // Flujo de proceso para PIEZA_AUTOMOTRIZ

        // 1. ALMACEN_MP: Sin procesamiento, solo recepción
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 2. HORNO: Procesamiento por lotes (ACCUM 10), 100.0 minutos (ProModel)
        engine.addProcessingRule(new BatchProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 100.0, 10));

        // 3. BANDA_1: Transporte 0.94 minutos (ProModel)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("BANDA_1", "PIEZA_AUTOMOTRIZ", 0.94) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 4. CARGA: 0.5 minutos
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("CARGA", "PIEZA_AUTOMOTRIZ", 0.5) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 5. TORNEADO: 5.2 minutos (ProModel)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("TORNEADO", "PIEZA_AUTOMOTRIZ", 5.2) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 6. FRESADO: 9.17 minutos
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("FRESADO", "PIEZA_AUTOMOTRIZ", 9.17) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 7. TALADRO: 1.6 minutos (ProModel)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("TALADRO", "PIEZA_AUTOMOTRIZ", 1.6) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 8. RECTIFICADO: 2.85 minutos (ProModel)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("RECTIFICADO", "PIEZA_AUTOMOTRIZ", 2.85) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 9. DESCARGA: 0.5 minutos
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("DESCARGA", "PIEZA_AUTOMOTRIZ", 0.5) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 10. BANDA_2: 1.02 minutos (ProModel)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("BANDA_2", "PIEZA_AUTOMOTRIZ", 1.02) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 11. INSPECCION: Exponencial(3) minutos
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("INSPECCION", "PIEZA_AUTOMOTRIZ", 3.0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 12. SALIDA: Sin procesamiento (EXIT)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("SALIDA", "PIEZA_AUTOMOTRIZ", 0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });
    }

    private void setupArrivals(SimulationEngine engine) {
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 0, 12000, 5.0);
    }

    private void setupLocationPositions() {
        // Diseño del flujo de producción (coordenadas x, y)
        locationPositions.put("ALMACEN_MP", new Point2D(50, 300));
        locationPositions.put("HORNO", new Point2D(250, 300));
        locationPositions.put("BANDA_1", new Point2D(450, 300));
        locationPositions.put("CARGA", new Point2D(650, 300));

        // Celda de manufactura (Robot)
        locationPositions.put("TORNEADO", new Point2D(850, 150));
        locationPositions.put("FRESADO", new Point2D(1050, 150));
        locationPositions.put("TALADRO", new Point2D(1050, 450));
        locationPositions.put("RECTIFICADO", new Point2D(850, 450));

        locationPositions.put("DESCARGA", new Point2D(650, 500));
        locationPositions.put("BANDA_2", new Point2D(450, 500));
        locationPositions.put("INSPECCION", new Point2D(250, 500));
        locationPositions.put("SALIDA", new Point2D(50, 500));
    }

    private void startAnimationTimer() {
        animationTimer = new AnimationTimer() {
            private int frameCount = 0;

            @Override
            public void handle(long now) {
                renderScene();

                // Actualizar estadísticas cada 30 frames (2 veces por segundo a 60fps)
                // Esto reduce el parpadeo al actualizar menos frecuentemente
                frameCount++;
                if (frameCount >= 30) {
                    updateStatistics();
                    frameCount = 0;
                }
            }
        };
        animationTimer.start();
    }

    private void renderScene() {
        // Limpiar canvas
        gc.setFill(Color.web("#1A252F"));
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dibujar conexiones entre locaciones
        drawConnections();

        // Dibujar locaciones
        locationManager.render(gc, engine);

        // Dibujar entidades en movimiento
        entityManager.render(gc);

        // Dibujar recursos (solo si ya está inicializado)
        if (resourceManager != null) {
            resourceManager.render(gc, entityManager);
        }
    }

    private void drawConnections() {
        gc.setStroke(Color.web("#34495E"));
        gc.setLineWidth(2);
        gc.setLineDashes(5, 5);

        // Dibujar líneas de flujo
        drawLine("ALMACEN_MP", "HORNO");
        drawLine("HORNO", "BANDA_1");
        drawLine("BANDA_1", "CARGA");

        // Flujo Robot
        drawLine("CARGA", "TORNEADO");
        drawLine("TORNEADO", "FRESADO");
        drawLine("FRESADO", "TALADRO");
        drawLine("TALADRO", "RECTIFICADO");
        drawLine("RECTIFICADO", "DESCARGA");

        drawLine("DESCARGA", "BANDA_2");
        drawLine("BANDA_2", "INSPECCION");
        drawLine("INSPECCION", "SALIDA");

        gc.setLineDashes(null);
    }

    private void drawLine(String from, String to) {
        Point2D p1 = locationPositions.get(from);
        Point2D p2 = locationPositions.get(to);
        if (p1 != null && p2 != null) {
            gc.strokeLine(p1.getX() + 40, p1.getY() + 40, p2.getX() + 40, p2.getY() + 40);
        }
    }

    private List<com.simulation.statistics.StatisticsCollector> replicaStats = new ArrayList<>();

    private void startSimulation() {
        if (running.get())
            return;

        running.set(true);
        paused.set(false);
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        statusLabel.setText("Iniciando...");
        statusLabel.setTextFill(Color.web("#2ECC71"));
        replicaStats.clear();

        // Iniciar hilo de simulación
        simulationThread = new Thread(() -> {
            try {
                for (int replica = 1; replica <= 3; replica++) {
                    final int currentReplica = replica;
                    Platform.runLater(() -> {
                        statusLabel.setText("Ejecutando Réplica " + currentReplica + " / 3");
                        // Reset visual managers for new replica
                        entityManager.clear();
                    });

                    // Reiniciar motor para cada réplica
                    setupSimulationEngine();
                    currentTime = 0;

                    while (running.get() && currentTime < endTime) {
                        // Esperar si está pausado
                        while (paused.get() && running.get()) {
                            Thread.sleep(100);
                        }

                        if (!running.get())
                            break;

                        // Ejecutar paso de simulación
                        double speedMultiplier = simulationSpeed;
                        boolean hasMoreEvents = engine.step(speedMultiplier);

                        if (!hasMoreEvents) {
                            break;
                        }

                        // Actualizar tiempo actual
                        currentTime = engine.getClock().getCurrentTime();

                        // Actualizar UI (Throttled)
                        // Solo actualizar si ha pasado suficiente tiempo real o si la velocidad es baja
                        long now = System.currentTimeMillis();
                        if (speedMultiplier < 50 || (now - lastUIUpdate) > 33) { // ~30 FPS max updates
                            updateUI();
                            lastUIUpdate = now;
                        }

                        // Controlar velocidad de simulación
                        if (speedMultiplier < 50) {
                            int sleepTime = (int) (50 / speedMultiplier);
                            Thread.sleep(Math.max(1, sleepTime));
                        } else {
                            // Yield para permitir que otros hilos (como JavaFX) respiren
                            if ((now - lastUIUpdate) > 100) { // Yield ocasional si vamos muy rápido
                                Thread.yield();
                            }
                        }
                    }

                    if (!running.get())
                        break;

                    // Guardar estadísticas de esta réplica
                    replicaStats.add(engine.getStatistics());
                }

                // Simulación completada (todas las réplicas)
                Platform.runLater(() -> {
                    statusLabel.setText("Completado (Promedio de 3 Réplicas)");
                    statusLabel.setTextFill(Color.web("#3498DB"));
                    startButton.setDisable(false);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
                    displayAveragedStatistics();
                });

            } catch (InterruptedException e) {
                System.out.println("Simulación interrumpida");
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    statusLabel.setText("Error: " + e.getMessage());
                    statusLabel.setTextFill(Color.web("#E74C3C"));
                });
            }
        });

        simulationThread.setDaemon(true);
        simulationThread.start();
    }

    private void displayAveragedStatistics() {
        if (replicaStats.isEmpty())
            return;

        Platform.runLater(() -> {
            // Actualizar tabla de locaciones con promedios
            for (LocationStatsRow row : locationStatsTable.getItems()) {
                String locName = row.name.get();
                // Calcular promedio para esta locación
                double totalEntriesSum = 0;
                double utilizationSum = 0;
                double avgContentsSum = 0;
                double maxContentsSum = 0;
                double currentContentsSum = 0;
                double avgTimePerEntrySum = 0;

                int count = 0;
                for (com.simulation.statistics.StatisticsCollector stats : replicaStats) {
                    com.simulation.locations.LocationStatistics ls = stats.getLocationStats().get(locName);
                    if (ls != null) {
                        totalEntriesSum += ls.getTotalEntries();
                        utilizationSum += ls.getUtilizationPercent();
                        avgContentsSum += ls.getAverageContents();
                        maxContentsSum += ls.getMaxContents();
                        currentContentsSum += ls.getCurrentContents();
                        avgTimePerEntrySum += ls.getAverageTimePerEntry();
                        count++;
                    }
                }

                if (count > 0) {
                    row.totalEntries.set((int) (totalEntriesSum / count));
                    row.utilization.set(String.format("%.2f", utilizationSum / count));
                    row.avgContents.set(String.format("%.2f", avgContentsSum / count));
                    row.maxContents.set(String.format("%.2f", maxContentsSum / count));
                    row.currentContents.set((int) (currentContentsSum / count));
                    row.avgTimePerEntry.set(String.format("%.2f", avgTimePerEntrySum / count));
                }
            }

            // Actualizar tabla de entidades con promedios
            for (EntityStatsRow row : entityStatsTable.getItems()) {
                String entName = row.name.get();
                double totalExitsSum = 0;
                double avgSysTimeSum = 0;
                double avgWaitTimeSum = 0;
                double avgValueTimeSum = 0;
                double avgNonValueTimeSum = 0;
                double inSystemSum = 0;

                int count = 0;
                for (com.simulation.statistics.StatisticsCollector stats : replicaStats) {
                    com.simulation.entities.EntityStatistics es = stats.getEntityStats().get(entName);
                    if (es != null) {
                        totalExitsSum += es.getTotalExits();
                        avgSysTimeSum += es.getAverageSystemTime();
                        avgWaitTimeSum += es.getAverageWaitTime();
                        avgValueTimeSum += es.getAverageValueAddedTime();
                        avgNonValueTimeSum += es.getAverageNonValueAddedTime();
                        inSystemSum += es.getCurrentInSystem();
                        count++;
                    }
                }

                if (count > 0) {
                    row.exits.set((int) (totalExitsSum / count));
                    row.avgSystemTime.set(String.format("%.2f", avgSysTimeSum / count));
                    row.avgWaitTime.set(String.format("%.2f", avgWaitTimeSum / count));
                    row.avgValueTime.set(String.format("%.2f", avgValueTimeSum / count));
                    row.avgNonValueTime.set(String.format("%.2f", avgNonValueTimeSum / count));
                    row.inSystem.set((int) (inSystemSum / count));
                }
            }
        });
    }

    private void pauseSimulation() {
        paused.set(!paused.get());
        pauseButton.setText(paused.get() ? "▶ Reanudar" : "⏸ Pausar");
        statusLabel.setText(paused.get() ? "Pausado" : "Ejecutando...");
        statusLabel.setTextFill(paused.get() ? Color.web("#F39C12") : Color.web("#2ECC71"));
    }

    private void stopSimulation() {
        running.set(false);
        if (simulationThread != null) {
            simulationThread.interrupt();
        }
        startButton.setDisable(false);
        pauseButton.setDisable(true);
        stopButton.setDisable(true);
        statusLabel.setText("Detenido");
        statusLabel.setTextFill(Color.web("#E74C3C"));
    }

    private void resetSimulation() {
        stopSimulation();
        currentTime = 0;
        setupSimulationEngine();
        entityManager.clear();
        updateUI();
        statusLabel.setText("Reiniciado");
    }

    private void updateUI() {
        Platform.runLater(() -> {
            timeLabel.setText(String.format("Tiempo: %.2f min (%.2f hrs)", currentTime, currentTime / 60.0));
            progressBar.setProgress(currentTime / endTime);
        });
    }

    private void updateStatistics() {
        if (engine == null)
            return;

        Platform.runLater(() -> {
            // Actualizar tiempo
            timeLabel.setText(String.format("Tiempo: %.2f min", engine.getClock().getCurrentTime()));

            // Actualizar estadísticas de locaciones con TODAS las columnas
            Map<String, com.simulation.locations.LocationStatistics> locStats = engine.getStatistics()
                    .getLocationStats();

            // Si la tabla está vacía, poblarla por primera vez
            if (locationStatsTable.getItems().isEmpty()) {
                for (com.simulation.locations.Location location : engine.getAllLocations().values()) {
                    LocationStatsRow row = new LocationStatsRow();
                    row.name.set(formatLocationName(location.getName()));
                    locationStatsTable.getItems().add(row);
                }
            }

            // Actualizar valores existentes sin reconstruir
            int index = 0;
            for (com.simulation.locations.Location location : engine.getAllLocations().values()) {
                if (index < locationStatsTable.getItems().size()) {
                    LocationStatsRow row = locationStatsTable.getItems().get(index);
                    String locName = location.getName();
                    com.simulation.locations.LocationStatistics stats = locStats.get(locName);

                    // Actualizar contador visual en el nodo de la locación
                    int totalEntries = (stats != null) ? stats.getTotalEntries() : 0;
                    locationManager.setTotalEntries(locName, totalEntries);

                    if (stats != null) {
                        row.scheduledTime.set(String.format("%.2f", stats.getScheduledTime() / 60.0));
                        row.capacity.set(stats.getCapacity());
                        row.totalEntries.set(stats.getTotalEntries());
                        row.avgTimePerEntry.set(String.format("%.2f", stats.getAverageTimePerEntry()));
                        row.avgContents.set(String.format("%.2f", stats.getAverageContents()));
                        row.maxContents.set(String.format("%.2f", stats.getMaxContents()));
                        row.currentContents.set((int) stats.getCurrentContents());
                        row.utilization.set(String.format("%.2f", stats.getUtilizationPercent()));
                    } else {
                        row.scheduledTime.set(String.format("%.2f", currentTime / 60.0));
                        row.capacity.set(location.getType().capacity());
                        row.totalEntries.set(0);
                        row.avgTimePerEntry.set("0.00");
                        row.avgContents.set("0.00");
                        row.maxContents.set("0.00");
                        row.currentContents.set(location.getCurrentOccupancy());
                        row.utilization.set("0.00");
                    }
                }
                index++;
            }

            // Actualizar estadísticas de entidades con TODAS las columnas
            Map<String, com.simulation.entities.EntityStatistics> entityStats = engine.getStatistics().getEntityStats();

            // Lista de TODOS los tipos de entidades
            String[] allEntityTypes = {
                    "PIEZA_AUTOMOTRIZ"
            };

            // Si la tabla está vacía, poblarla por primera vez con TODAS las entidades
            if (entityStatsTable.getItems().isEmpty()) {
                for (String entityType : allEntityTypes) {
                    EntityStatsRow row = new EntityStatsRow();
                    row.name.set(formatEntityName(entityType));
                    entityStatsTable.getItems().add(row);
                }
            }

            // Actualizar valores existentes sin reconstruir
            for (int i = 0; i < allEntityTypes.length && i < entityStatsTable.getItems().size(); i++) {
                EntityStatsRow row = entityStatsTable.getItems().get(i);
                String entityType = allEntityTypes[i];
                com.simulation.entities.EntityStatistics stats = entityStats.get(entityType);

                if (stats != null) {
                    row.exits.set(stats.getTotalExits());
                    row.inSystem.set(stats.getCurrentInSystem());
                    row.avgSystemTime.set(String.format("%.2f", stats.getAverageSystemTime()));
                    row.avgNonValueTime.set(String.format("%.2f", stats.getAverageNonValueAddedTime()));
                    row.avgWaitTime.set(String.format("%.2f", stats.getAverageWaitTime()));
                    row.avgValueTime.set(String.format("%.2f", stats.getAverageValueAddedTime()));
                } else {
                    // Si no hay estadísticas aún, mostrar ceros
                    row.exits.set(0);
                    row.inSystem.set(0);
                    row.avgSystemTime.set("0.00");
                    row.avgNonValueTime.set("0.00");
                    row.avgWaitTime.set("0.00");
                    row.avgValueTime.set("0.00");
                }
            }

            // Actualizar gráfica de barras con entradas de locaciones (solo si existe)
            if (locationExitsChart != null && !locationExitsChart.getData().isEmpty()) {
                XYChart.Series<String, Number> series = locationExitsChart.getData().get(0);

                // Crear lista ordenada de TODAS las locaciones con sus entradas
                List<Map.Entry<String, Integer>> locationEntries = new ArrayList<>();
                for (com.simulation.locations.Location location : engine.getAllLocations().values()) {
                    String locName = location.getName();
                    com.simulation.locations.LocationStatistics stats = locStats.get(locName);
                    int entries = (stats != null) ? stats.getTotalEntries() : 0;
                    String formattedName = formatLocationName(locName);
                    locationEntries.add(new AbstractMap.SimpleEntry<>(formattedName, entries));
                }

                // Ordenar por cantidad de entradas (descendente)
                locationEntries.sort((a, b) -> b.getValue().compareTo(a.getValue()));

                // Actualizar datos directamente (sin animación ya está deshabilitada)
                series.getData().clear();
                for (Map.Entry<String, Integer> entry : locationEntries) {
                    XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(entry.getKey(), entry.getValue());
                    series.getData().add(dataPoint);
                }
            }
        });
    }

    // --- Implementación de SimulationListener ---

    @Override
    public void onEntityCreated(Entity entity, Location location, double time) {
        // Delegar a AnimationController si tuviéramos acceso directo,
        // pero aquí usamos VisualEntityManager
        if (entityManager != null) {
            Point2D pos = locationPositions.get(location.getName());
            if (pos != null) {
                // Crear sprite visual
                // Nota: VisualEntityManager maneja la creación implícitamente al renderizar si
                // se añade a una lista,
                // pero para animación fluida podríamos necesitar notificarle.
                // Por ahora, el render loop lo captará si consultamos el estado del engine.
            }
        }
    }

    @Override
    public void onEntityArrival(Entity entity, Location location, double time) {
        // Actualizar estadísticas o efectos visuales
    }

    @Override
    public void onEntityMove(Entity entity, Location from, Location to, double time) {
        // Iniciar animación de movimiento
        if (entityManager != null) {
            Point2D start = locationPositions.get(from.getName());
            Point2D end = locationPositions.get(to.getName());
            if (start != null && end != null) {
                // Aquí podríamos disparar una animación específica
                // Por simplicidad, el VisualEntityManager interpolará si se implementa lógica
                // de movimiento
                // O el AnimationController manejaría PathAnimators.

                // Si tuviéramos una referencia a AnimationController aquí, llamaríamos a
                // animateEntityMovement
                // Como no la tenemos expuesta fácilmente en esta estructura refactorizada (está
                // dentro de createAnimationCanvas pero no como campo de clase accesible
                // globalmente para estos callbacks sin refactorizar más),
                // asumiremos que el render loop actualiza posiciones instantáneas o
                // interpoladas.

                // Para mejorar esto, deberíamos promover AnimationController a campo de clase o
                // usar un bus de eventos.
                // Dado el tiempo, dejaremos que el updateUI periódico refleje los cambios.
            }
        }
    }

    @Override
    public void onEntityExit(Entity entity, Location location, double time) {
        // Entidad sale del sistema o de una locación
    }

    @Override
    public void onResourceAcquired(Resource resource, Entity entity, double time) {
        // Visualizar recurso ocupado
    }

    @Override
    public void onResourceReleased(Resource resource, Entity entity, double time) {
        // Visualizar recurso libre
    }

    private void openExpandedTablesWindow() {
        Stage expandedStage = new Stage();
        expandedStage.setTitle("Estadísticas Detalladas");

        // Crear contenedor principal
        BorderPane mainPane = new BorderPane();
        mainPane.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e 0%, #16213e 100%);");

        // TabPane para las tablas
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setPrefSize(1400, 800);

        // Tab de Locaciones
        Tab locationTab = new Tab("📍 Estadísticas de Locaciones");
        VBox locBox = new VBox(15);
        locBox.setPadding(new Insets(20));
        locBox.setStyle("-fx-background-color: linear-gradient(to bottom, #0f3460 0%, #16213e 100%);");

        Label locTitle = new Label("📍 Estadísticas Completas de Locaciones");
        locTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        locTitle.setTextFill(Color.web("#e94560"));

        TableView<LocationStatsRow> expandedLocationTable = createLocationStatsTable();
        expandedLocationTable.setItems(locationStatsTable.getItems());
        expandedLocationTable.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-control-inner-background: #1a1a2e;" +
                        "-fx-table-cell-border-color: #16213e;" +
                        "-fx-text-fill: #f1faee;" +
                        "-fx-font-size: 13px;");

        ScrollPane locScroll = new ScrollPane(expandedLocationTable);
        locScroll.setFitToWidth(true);
        locScroll.setFitToHeight(true);
        VBox.setVgrow(locScroll, Priority.ALWAYS);

        locBox.getChildren().addAll(locTitle, locScroll);
        locationTab.setContent(locBox);

        // Tab de Entidades
        Tab entityTab = new Tab("📦 Estadísticas de Entidades");
        VBox entBox = new VBox(15);
        entBox.setPadding(new Insets(20));
        entBox.setStyle("-fx-background-color: linear-gradient(to bottom, #0f3460 0%, #16213e 100%);");

        Label entTitle = new Label("📦 Estadísticas Completas de Entidades");
        entTitle.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        entTitle.setTextFill(Color.web("#e94560"));

        TableView<EntityStatsRow> expandedEntityTable = createEntityStatsTable();
        expandedEntityTable.setItems(entityStatsTable.getItems());
        expandedEntityTable.setStyle(
                "-fx-background-color: #0f3460;" +
                        "-fx-control-inner-background: #1a1a2e;" +
                        "-fx-table-cell-border-color: #16213e;" +
                        "-fx-text-fill: #f1faee;" +
                        "-fx-font-size: 13px;");

        ScrollPane entScroll = new ScrollPane(expandedEntityTable);
        entScroll.setFitToWidth(true);
        entScroll.setFitToHeight(true);
        VBox.setVgrow(entScroll, Priority.ALWAYS);

        entBox.getChildren().addAll(entTitle, entScroll);
        entityTab.setContent(entBox);

        tabPane.getTabs().addAll(locationTab, entityTab);
        mainPane.setCenter(tabPane);

        Scene scene = new Scene(mainPane);
        expandedStage.setScene(scene);
        expandedStage.show();
    }

    private void openConfigurationDialog() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Configuración");
        alert.setHeaderText("Configuración de Simulación");
        alert.setContentText("La configuración de parámetros se realiza en el código (Main.java) para este ejercicio.");
        alert.showAndWait();
    }

    private TableView<LocationStatsRow> createLocationStatsTable() {
        TableView<LocationStatsRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<LocationStatsRow, String> nameCol = new TableColumn<>("Locación");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().name);

        TableColumn<LocationStatsRow, String> schedTimeCol = new TableColumn<>("Tiempo Prog. (Hr)");
        schedTimeCol.setCellValueFactory(cellData -> cellData.getValue().scheduledTime);

        TableColumn<LocationStatsRow, Number> capacityCol = new TableColumn<>("Capacidad");
        capacityCol.setCellValueFactory(cellData -> cellData.getValue().capacity);

        TableColumn<LocationStatsRow, Number> entriesCol = new TableColumn<>("Total Entradas");
        entriesCol.setCellValueFactory(cellData -> cellData.getValue().totalEntries);

        TableColumn<LocationStatsRow, String> avgTimeCol = new TableColumn<>("Tiempo/Entrada (Min)");
        avgTimeCol.setCellValueFactory(cellData -> cellData.getValue().avgTimePerEntry);

        TableColumn<LocationStatsRow, String> avgContCol = new TableColumn<>("Contenido Prom.");
        avgContCol.setCellValueFactory(cellData -> cellData.getValue().avgContents);

        TableColumn<LocationStatsRow, String> maxContCol = new TableColumn<>("Contenido Max.");
        maxContCol.setCellValueFactory(cellData -> cellData.getValue().maxContents);

        TableColumn<LocationStatsRow, Number> currContCol = new TableColumn<>("Contenido Actual");
        currContCol.setCellValueFactory(cellData -> cellData.getValue().currentContents);

        TableColumn<LocationStatsRow, String> utilCol = new TableColumn<>("% Utilización");
        utilCol.setCellValueFactory(cellData -> cellData.getValue().utilization);

        table.getColumns().addAll(nameCol, schedTimeCol, capacityCol, entriesCol, avgTimeCol, avgContCol, maxContCol,
                currContCol, utilCol);
        return table;
    }

    private TableView<EntityStatsRow> createEntityStatsTable() {
        TableView<EntityStatsRow> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<EntityStatsRow, String> nameCol = new TableColumn<>("Entidad");
        nameCol.setCellValueFactory(cellData -> cellData.getValue().name);

        TableColumn<EntityStatsRow, Number> exitsCol = new TableColumn<>("Total Salidas");
        exitsCol.setCellValueFactory(cellData -> cellData.getValue().exits);

        TableColumn<EntityStatsRow, Number> inSystemCol = new TableColumn<>("En Sistema");
        inSystemCol.setCellValueFactory(cellData -> cellData.getValue().inSystem);

        TableColumn<EntityStatsRow, String> avgSysTimeCol = new TableColumn<>("Tiempo Sistema (Min)");
        avgSysTimeCol.setCellValueFactory(cellData -> cellData.getValue().avgSystemTime);

        TableColumn<EntityStatsRow, String> avgMoveTimeCol = new TableColumn<>("Tiempo Movimiento (Min)");
        avgMoveTimeCol.setCellValueFactory(cellData -> cellData.getValue().avgNonValueTime);

        TableColumn<EntityStatsRow, String> avgWaitTimeCol = new TableColumn<>("Tiempo Espera (Min)");
        avgWaitTimeCol.setCellValueFactory(cellData -> cellData.getValue().avgWaitTime);

        TableColumn<EntityStatsRow, String> avgOpTimeCol = new TableColumn<>("Tiempo Operación (Min)");
        avgOpTimeCol.setCellValueFactory(cellData -> cellData.getValue().avgValueTime);

        table.getColumns().addAll(nameCol, exitsCol, inSystemCol, avgSysTimeCol, avgMoveTimeCol, avgWaitTimeCol,
                avgOpTimeCol);
        return table;
    }

    // Clases auxiliares para las tablas
    public static class LocationStatsRow {
        public final javafx.beans.property.StringProperty name = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty scheduledTime = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.IntegerProperty capacity = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.IntegerProperty totalEntries = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.StringProperty avgTimePerEntry = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty avgContents = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty maxContents = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.IntegerProperty currentContents = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.StringProperty utilization = new javafx.beans.property.SimpleStringProperty();
    }

    public static class EntityStatsRow {
        public final javafx.beans.property.StringProperty name = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.IntegerProperty exits = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.IntegerProperty inSystem = new javafx.beans.property.SimpleIntegerProperty();
        public final javafx.beans.property.StringProperty avgSystemTime = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty avgNonValueTime = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty avgWaitTime = new javafx.beans.property.SimpleStringProperty();
        public final javafx.beans.property.StringProperty avgValueTime = new javafx.beans.property.SimpleStringProperty();
    }

    private String formatLocationName(String name) {
        return name.replace("_", " ");
    }

    private String formatEntityName(String name) {
        return name.replace("_", " ");
    }
}
