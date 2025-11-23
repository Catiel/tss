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
import javafx.geometry.Point2D;
import javafx.geometry.Insets;
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

public class BrewerySimulationGUI extends Application implements SimulationListener {

    private SimulationEngine engine;
    private Canvas canvas;
    private GraphicsContext gc;

    // Controles
    private Button startButton, pauseButton, stopButton, resetButton;
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
    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicBoolean paused = new AtomicBoolean(false);
    private double simulationSpeed = 1.0;
    private double currentTime = 0;
    private final double endTime = 4200.0; // 70 horas

    // Sistema de visualización
    private VisualLocationManager locationManager;
    private VisualEntityManager entityManager;
    private VisualResourceManager resourceManager;

    // Mapa de posiciones de locaciones
    private final Map<String, Point2D> locationPositions = new HashMap<>();

    // Gestor de rutas
    private PathNetworkManager pathManager;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Simulador de Producción de Cerveza - Sistema Integrado");

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
        Label title = new Label("🍺 SIMULADOR DE PRODUCCIÓN DE CERVEZA");
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

        resetButton = new Button("🔄 Reiniciar");
        resetButton.setStyle(
                "-fx-background-color: #3498DB; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20;");
        resetButton.setOnAction(e -> resetSimulation());

        Label speedLabel = new Label("Velocidad:");
        speedLabel.setTextFill(Color.web("#ECF0F1"));
        speedLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setPrefWidth(200);
        speedSlider.valueProperty().addListener((obs, old, newVal) -> simulationSpeed = newVal.doubleValue());

        controls.getChildren().addAll(startButton, pauseButton, stopButton, resetButton,
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

    private void setupLocationPositions() {
        // Diseño del flujo de producción (coordenadas x, y)
        locationPositions.put("SILO_GRANDE", new Point2D(100, 100));
        locationPositions.put("MALTEADO", new Point2D(100, 250));
        locationPositions.put("SECADO", new Point2D(100, 400));
        locationPositions.put("MOLIENDA", new Point2D(300, 400));
        locationPositions.put("MACERADO", new Point2D(500, 400));
        locationPositions.put("FILTRADO", new Point2D(700, 400));

        locationPositions.put("SILO_LUPULO", new Point2D(900, 100));
        locationPositions.put("COCCION", new Point2D(900, 400));

        locationPositions.put("ENFRIAMIENTO", new Point2D(1100, 400));

        locationPositions.put("SILO_LEVADURA", new Point2D(100, 600));
        locationPositions.put("FERMENTACION", new Point2D(700, 600));

        locationPositions.put("MADURACION", new Point2D(1100, 600));
        locationPositions.put("INSPECCION", new Point2D(1300, 600));
        locationPositions.put("EMBOTELLADO", new Point2D(1500, 600));
        locationPositions.put("ETIQUETADO", new Point2D(1700, 600));

        locationPositions.put("ALMACEN_CAJAS", new Point2D(1500, 200));
        locationPositions.put("EMPACADO", new Point2D(1700, 400));

        locationPositions.put("ALMACENAJE", new Point2D(1700, 800));
        locationPositions.put("MERCADO", new Point2D(1700, 1000));
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

        // Dibujar líneas de flujo entre locaciones relacionadas
        drawLine("SILO_GRANDE", "MALTEADO");
        drawLine("MALTEADO", "SECADO");
        drawLine("SECADO", "MOLIENDA");
        drawLine("MOLIENDA", "MACERADO");
        drawLine("MACERADO", "FILTRADO");
        drawLine("FILTRADO", "COCCION");
        drawLine("SILO_LUPULO", "COCCION");
        drawLine("COCCION", "ENFRIAMIENTO");
        drawLine("ENFRIAMIENTO", "FERMENTACION");
        drawLine("SILO_LEVADURA", "FERMENTACION");
        drawLine("FERMENTACION", "MADURACION");
        drawLine("MADURACION", "INSPECCION");
        drawLine("INSPECCION", "EMBOTELLADO");
        drawLine("EMBOTELLADO", "ETIQUETADO");
        drawLine("ETIQUETADO", "EMPACADO");
        drawLine("ALMACEN_CAJAS", "EMPACADO");
        drawLine("EMPACADO", "ALMACENAJE");
        drawLine("ALMACENAJE", "MERCADO");

        gc.setLineDashes(null);
    }

    private void drawLine(String from, String to) {
        Point2D p1 = locationPositions.get(from);
        Point2D p2 = locationPositions.get(to);
        if (p1 != null && p2 != null) {
            gc.strokeLine(p1.getX() + 40, p1.getY() + 40, p2.getX() + 40, p2.getY() + 40);
        }
    }

    private void startSimulation() {
        if (running.get())
            return;

        running.set(true);
        paused.set(false);
        startButton.setDisable(true);
        pauseButton.setDisable(false);
        stopButton.setDisable(false);
        statusLabel.setText("Ejecutando...");
        statusLabel.setTextFill(Color.web("#2ECC71"));

        // Iniciar hilo de simulación
        simulationThread = new Thread(() -> {
            try {
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

                    // Actualizar UI
                    updateUI();

                    // Controlar velocidad de simulación
                    int sleepTime = (int) (50 / speedMultiplier);
                    Thread.sleep(Math.max(10, sleepTime));
                }

                // Simulación completada
                Platform.runLater(() -> {
                    statusLabel.setText("Completado");
                    statusLabel.setTextFill(Color.web("#3498DB"));
                    startButton.setDisable(false);
                    pauseButton.setDisable(true);
                    stopButton.setDisable(true);
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

            // Lista de TODOS los tipos de entidades (8 tipos)
            String[] allEntityTypes = {
                    "GRANOS_DE_CEBADA", "LUPULO", "LEVADURA", "MOSTO",
                    "CERVEZA", "BOTELLA_CON_CERVEZA", "CAJA_VACIA", "CAJA_CON_CERVEZAS"
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

        // Botón para cerrar
        HBox bottomBar = new HBox(10);
        bottomBar.setPadding(new Insets(10));
        bottomBar.setAlignment(Pos.CENTER);
        bottomBar.setStyle("-fx-background-color: #0f3460;");

        Button closeButton = new Button("✖ Cerrar Ventana");
        closeButton.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e94560 0%, #c13551 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-padding: 10 25 10 25;" +
                        "-fx-background-radius: 8;" +
                        "-fx-cursor: hand;");
        closeButton.setOnAction(e -> expandedStage.close());

        bottomBar.getChildren().add(closeButton);
        mainPane.setBottom(bottomBar);

        // Crear escena y mostrar
        Scene scene = new Scene(mainPane, 1400, 850);
        try {
            scene.getStylesheets().add(getClass().getResource("/styles/brewery-simulation.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("No se pudo cargar el CSS: " + ex.getMessage());
        }

        expandedStage.setScene(scene);
        expandedStage.show();
    }

    private String formatLocationName(String name) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = name.split("_");
        for (String part : parts) {
            if (formatted.length() > 0)
                formatted.append(" ");
            formatted.append(Character.toUpperCase(part.charAt(0)));
            formatted.append(part.substring(1).toLowerCase());
        }
        return formatted.toString();
    }

    private String formatEntityName(String name) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = name.split("_");
        for (String part : parts) {
            if (formatted.length() > 0)
                formatted.append(" ");
            formatted.append(Character.toUpperCase(part.charAt(0)));
            formatted.append(part.substring(1).toLowerCase());
        }
        return formatted.toString();
    }

    private TableView<LocationStatsRow> createLocationStatsTable() {
        TableView<LocationStatsRow> table = new TableView<>();
        table.setStyle("-fx-background-color: #2C3E50;");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<LocationStatsRow, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(data -> data.getValue().name);
        nameCol.setPrefWidth(150);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<LocationStatsRow, String> schedTimeCol = new TableColumn<>("T. Programado (Hr)");
        schedTimeCol.setCellValueFactory(data -> data.getValue().scheduledTime);
        schedTimeCol.setPrefWidth(130);
        schedTimeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, Integer> capacityCol = new TableColumn<>("Capacidad");
        capacityCol.setCellValueFactory(data -> data.getValue().capacity.asObject());
        capacityCol.setPrefWidth(85);
        capacityCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, Integer> entriesCol = new TableColumn<>("Total Entradas");
        entriesCol.setCellValueFactory(data -> data.getValue().totalEntries.asObject());
        entriesCol.setPrefWidth(110);
        entriesCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, String> avgTimeCol = new TableColumn<>("T. Por Entrada Prom. (Min)");
        avgTimeCol.setCellValueFactory(data -> data.getValue().avgTimePerEntry);
        avgTimeCol.setPrefWidth(170);
        avgTimeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, String> avgContentsCol = new TableColumn<>("Contenido Promedio");
        avgContentsCol.setCellValueFactory(data -> data.getValue().avgContents);
        avgContentsCol.setPrefWidth(130);
        avgContentsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, String> maxContentsCol = new TableColumn<>("Contenido Máximo");
        maxContentsCol.setCellValueFactory(data -> data.getValue().maxContents);
        maxContentsCol.setPrefWidth(130);
        maxContentsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, Integer> currentCol = new TableColumn<>("Contenido Actual");
        currentCol.setCellValueFactory(data -> data.getValue().currentContents.asObject());
        currentCol.setPrefWidth(120);
        currentCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationStatsRow, String> utilCol = new TableColumn<>("% Utilización");
        utilCol.setCellValueFactory(data -> data.getValue().utilization);
        utilCol.setPrefWidth(100);
        utilCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(nameCol, schedTimeCol, capacityCol, entriesCol,
                avgTimeCol, avgContentsCol, maxContentsCol, currentCol, utilCol);
        return table;
    }

    private TableView<EntityStatsRow> createEntityStatsTable() {
        TableView<EntityStatsRow> table = new TableView<>();
        table.setStyle("-fx-background-color: #2C3E50;");
        table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        TableColumn<EntityStatsRow, String> nameCol = new TableColumn<>("Nombre");
        nameCol.setCellValueFactory(data -> data.getValue().name);
        nameCol.setPrefWidth(180);
        nameCol.setStyle("-fx-alignment: CENTER-LEFT;");

        TableColumn<EntityStatsRow, Integer> exitsCol = new TableColumn<>("Total Salida");
        exitsCol.setCellValueFactory(data -> data.getValue().exits.asObject());
        exitsCol.setPrefWidth(95);
        exitsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<EntityStatsRow, Integer> inSystemCol = new TableColumn<>("Cantidad en Sistema");
        inSystemCol.setCellValueFactory(data -> data.getValue().inSystem.asObject());
        inSystemCol.setPrefWidth(130);
        inSystemCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<EntityStatsRow, String> avgSystemTimeCol = new TableColumn<>("T. Sistema Prom. (Min)");
        avgSystemTimeCol.setCellValueFactory(data -> data.getValue().avgSystemTime);
        avgSystemTimeCol.setPrefWidth(150);
        avgSystemTimeCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<EntityStatsRow, String> avgNonValueCol = new TableColumn<>("T. Movimiento Prom. (Min)");
        avgNonValueCol.setCellValueFactory(data -> data.getValue().avgNonValueTime);
        avgNonValueCol.setPrefWidth(170);
        avgNonValueCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<EntityStatsRow, String> avgWaitCol = new TableColumn<>("T. Espera Prom. (Min)");
        avgWaitCol.setCellValueFactory(data -> data.getValue().avgWaitTime);
        avgWaitCol.setPrefWidth(140);
        avgWaitCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<EntityStatsRow, String> avgValueCol = new TableColumn<>("T. Operación Prom. (Min)");
        avgValueCol.setCellValueFactory(data -> data.getValue().avgValueTime);
        avgValueCol.setPrefWidth(160);
        avgValueCol.setStyle("-fx-alignment: CENTER;");

        table.getColumns().addAll(nameCol, exitsCol, inSystemCol, avgSystemTimeCol,
                avgNonValueCol, avgWaitCol, avgValueCol);
        return table;
    }

    // Implementación de SimulationListener - eventos sincronizados con la
    // simulación real

    @Override
    public void onEntityCreated(Entity entity, Location location, double time) {
        // La entidad se creará visualmente cuando llegue a su primera locación
    }

    @Override
    public void onEntityArrival(Entity entity, Location location, double time) {
        // Entidad llega a una locación - no animamos este evento directamente
        // Las animaciones ocurren durante el movimiento
    }

    @Override
    public void onEntityMove(Entity entity, Location from, Location to, double time) {
        // Este evento se dispara cuando se inicia el movimiento

        // Verificar si ya se está animando (por onResourceAcquired)
        if (entityManager.isBeingTransported(entity.getId())) {
            return;
        }

        // Si no se está transportando, es un movimiento propio (self-transport)
        Platform.runLater(() -> {
            if (from != null && to != null) {
                Point2D startPos = locationPositions.get(from.getName());
                Point2D endPos = locationPositions.get(to.getName());

                if (startPos != null && endPos != null) {
                    // Obtener ruta (sin recurso)
                    List<Point2D> path = pathManager.getPath(
                            from.getName(),
                            to.getName(),
                            null);

                    // Fallback a línea directa si no hay ruta
                    if (path.isEmpty()) {
                        path.add(startPos);
                        path.add(endPos);
                    }

                    // Iniciar transporte de entidad
                    entityManager.startEntityTransport(
                            entity.getId(),
                            entity.getType().getName(),
                            path);
                }
            }
        });
    }

    @Override
    public void onEntityExit(Entity entity, Location from, double time) {
        // Entidad sale del sistema - podríamos mostrar un efecto visual
    }

    @Override
    public void onResourceAcquired(Resource resource, Entity entity, double time) {
        // Recurso adquirido - INICIAR transporte animado
        Platform.runLater(() -> {
            Location from = entity.getCurrentLocation();
            if (from != null) {
                Point2D startPos = locationPositions.get(from.getName());

                // Encontrar la locación de destino del movimiento
                // (esto se determina por las reglas de routing)
                Point2D endPos = findDestinationForEntity(entity);

                if (startPos != null && endPos != null) {
                    // Obtener la ruta completa usando PathNetworkManager
                    // Nota: Necesitamos los nombres de las locaciones, no solo coordenadas
                    String toLocationName = getDestinationLocationName(entity);

                    List<Point2D> path = pathManager.getPath(
                            from.getName(),
                            toLocationName,
                            resource.getName());

                    // Si no hay ruta definida (lista vacía), usar línea directa como fallback
                    if (path.isEmpty()) {
                        path.add(startPos);
                        path.add(endPos);
                    }

                    // Iniciar transporte con ruta compleja
                    entityManager.startResourceTransport(
                            entity.getId(),
                            entity.getType().getName(),
                            resource.getName(),
                            path);
                }
            }
        });
    }

    @Override
    public void onResourceReleased(Resource resource, Entity entity, double time) {
        // Recurso liberado - No detenemos la animación visualmente aquí.
        // La animación debe completar su trayecto natural.
        // El VisualEntityManager se encargará de limpiar cuando termine el movimiento.
    }

    /**
     * Encontrar la locación de destino para una entidad basándose en las reglas de
     * routing
     */
    private Point2D findDestinationForEntity(Entity entity) {
        String destName = getDestinationLocationName(entity);
        return destName != null ? locationPositions.get(destName) : null;
    }

    private String getDestinationLocationName(Entity entity) {
        if (entity == null || entity.getCurrentLocation() == null) {
            return null;
        }

        String fromLocation = entity.getCurrentLocation().getName();
        String entityType = entity.getType().getName();

        return getDestinationLocation(fromLocation, entityType);
    }

    /**
     * Obtener locación de destino según reglas de routing completas
     */
    private String getDestinationLocation(String fromLocation, String entityType) {
        // Rutas para GRANOS_DE_CEBADA
        if (entityType.equals("GRANOS_DE_CEBADA")) {
            switch (fromLocation) {
                case "SILO_GRANDE":
                    return "MALTEADO";
                case "MALTEADO":
                    return "SECADO";
                case "SECADO":
                    return "MOLIENDA";
                case "MOLIENDA":
                    return "MACERADO";
                case "MACERADO":
                    return "FILTRADO";
                case "FILTRADO":
                    return "COCCION";
            }
        }

        // Rutas para LUPULO
        if (entityType.equals("LUPULO")) {
            if (fromLocation.equals("SILO_LUPULO"))
                return "COCCION";
        }

        // Rutas para LEVADURA
        if (entityType.equals("LEVADURA")) {
            if (fromLocation.equals("SILO_LEVADURA"))
                return "FERMENTACION";
        }

        // Rutas para MOSTO
        if (entityType.equals("MOSTO")) {
            switch (fromLocation) {
                case "COCCION":
                    return "ENFRIAMIENTO";
                case "ENFRIAMIENTO":
                    return "FERMENTACION";
            }
        }

        // Rutas para CERVEZA
        if (entityType.equals("CERVEZA")) {
            switch (fromLocation) {
                case "FERMENTACION":
                    return "MADURACION";
                case "MADURACION":
                    return "INSPECCION";
                case "INSPECCION":
                    return "EMBOTELLADO";
            }
        }

        // Rutas para BOTELLA_CON_CERVEZA
        if (entityType.equals("BOTELLA_CON_CERVEZA")) {
            switch (fromLocation) {
                case "EMBOTELLADO":
                    return "ETIQUETADO";
                case "ETIQUETADO":
                    return "EMPACADO";
            }
        }

        // Rutas para CAJA_VACIA
        if (entityType.equals("CAJA_VACIA")) {
            if (fromLocation.equals("ALMACEN_CAJAS"))
                return "EMPACADO";
        }

        // Rutas para CAJA_CON_CERVEZAS
        if (entityType.equals("CAJA_CON_CERVEZAS")) {
            switch (fromLocation) {
                case "EMPACADO":
                    return "ALMACENAJE";
                case "ALMACENAJE":
                    return "MERCADO";
            }
        }

        return null;
    }

    // Métodos de configuración (copiados de Main.java)
    private void setupEntityTypes(SimulationEngine engine) {
        engine.addEntityType("GRANOS_DE_CEBADA", 150.0);
        engine.addEntityType("LUPULO", 150.0);
        engine.addEntityType("LEVADURA", 150.0);
        engine.addEntityType("MOSTO", 150.0);
        engine.addEntityType("CERVEZA", 150.0);
        engine.addEntityType("BOTELLA_CON_CERVEZA", 150.0);
        engine.addEntityType("CAJA_VACIA", 150.0);
        engine.addEntityType("CAJA_CON_CERVEZAS", 150.0);
    }

    private void setupLocations(SimulationEngine engine) {
        engine.addLocation("SILO_GRANDE", 3, 1);
        engine.addLocation("MALTEADO", 3, 1);
        engine.addLocation("SECADO", 3, 1);
        engine.addLocation("MOLIENDA", 2, 1);
        engine.addLocation("MACERADO", 3, 1);
        engine.addLocation("FILTRADO", 2, 1);
        engine.addLocation("COCCION", 10, 1);
        engine.addLocation("ALMACEN_CAJAS", 30, 1);
        engine.addLocation("SILO_LUPULO", 10, 1);
        engine.addLocation("ENFRIAMIENTO", 10, 1);
        engine.addLocation("EMPACADO", 1, 1);
        engine.addLocation("ETIQUETADO", 6, 1);
        engine.addLocation("EMBOTELLADO", 6, 1);
        engine.addLocation("INSPECCION", 3, 1);
        engine.addLocation("MADURACION", 10, 1);
        engine.addLocation("FERMENTACION", 10, 1);
        engine.addLocation("SILO_LEVADURA", 10, 1);
        engine.addLocation("ALMACENAJE", 6, 1);
        engine.addLocation("MERCADO", Integer.MAX_VALUE, 1);
    }

    private void setupResources(SimulationEngine engine) {
        engine.addResource("OPERADOR_RECEPCION", 1, 90.0);
        engine.addResource("OPERADOR_LUPULO", 1, 100.0);
        engine.addResource("OPERADOR_LEVADURA", 1, 100.0);
        engine.addResource("OPERADOR_EMPACADO", 1, 100.0);
        engine.addResource("CAMION", 1, 100.0);
    }

    private void setupProcessingRules(SimulationEngine engine) {
        engine.addProcessingRule(new SimpleProcessingRule("SILO_GRANDE", "GRANOS_DE_CEBADA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("MALTEADO", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("SECADO", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("MOLIENDA", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("MACERADO", "GRANOS_DE_CEBADA", 90));
        engine.addProcessingRule(new SimpleProcessingRule("FILTRADO", "GRANOS_DE_CEBADA", 30));
        engine.addProcessingRule(new SimpleProcessingRule("COCCION", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("SILO_LUPULO", "LUPULO", 0));
        engine.addProcessingRule(new SimpleProcessingRule("COCCION", "LUPULO", 0));
        engine.addProcessingRule(new SimpleProcessingRule("ENFRIAMIENTO", "MOSTO", 60));
        engine.addProcessingRule(new SimpleProcessingRule("FERMENTACION", "MOSTO", 120));
        engine.addProcessingRule(new SimpleProcessingRule("SILO_LEVADURA", "LEVADURA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("FERMENTACION", "LEVADURA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("MADURACION", "CERVEZA", 90));
        engine.addProcessingRule(new SimpleProcessingRule("INSPECCION", "CERVEZA", 30));
        engine.addProcessingRule(new SimpleProcessingRule("EMBOTELLADO", "CERVEZA", 3));
        engine.addProcessingRule(new SimpleProcessingRule("ETIQUETADO", "BOTELLA_CON_CERVEZA", 1));
        engine.addProcessingRule(new SimpleProcessingRule("EMPACADO", "BOTELLA_CON_CERVEZA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("ALMACEN_CAJAS", "CAJA_VACIA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("EMPACADO", "CAJA_VACIA", 0)); // WAIT 10 está después del
                                                                                         // JOIN
        engine.addProcessingRule(new SimpleProcessingRule("ALMACENAJE", "CAJA_CON_CERVEZAS", 5));
        engine.addProcessingRule(new SimpleProcessingRule("MERCADO", "CAJA_CON_CERVEZAS", 0));
    }

    private void setupArrivals(SimulationEngine engine) {
        double simulationTime = 4200.0;
        engine.scheduleArrival("GRANOS_DE_CEBADA", "SILO_GRANDE", 0, (int) (simulationTime / 25), 25);
        engine.scheduleArrival("LUPULO", "SILO_LUPULO", 0, (int) (simulationTime / 10), 10);
        engine.scheduleArrival("LEVADURA", "SILO_LEVADURA", 0, (int) (simulationTime / 20), 20);
        engine.scheduleArrival("CAJA_VACIA", "ALMACEN_CAJAS", 0, (int) (simulationTime / 30), 30);
    }

    // Clase interna para reglas
    private static class SimpleProcessingRule extends ProcessingRule {
        public SimpleProcessingRule(String locationName, String entityTypeName, double processingTime) {
            super(locationName, entityTypeName, processingTime);
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            entity.addValueAddedTime(processingTime);
        }
    }

    // Clases de datos para tablas
    static class LocationStatsRow {
        javafx.beans.property.SimpleStringProperty name = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty scheduledTime = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleIntegerProperty capacity = new javafx.beans.property.SimpleIntegerProperty();
        javafx.beans.property.SimpleIntegerProperty totalEntries = new javafx.beans.property.SimpleIntegerProperty();
        javafx.beans.property.SimpleStringProperty avgTimePerEntry = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty avgContents = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty maxContents = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleIntegerProperty currentContents = new javafx.beans.property.SimpleIntegerProperty();
        javafx.beans.property.SimpleStringProperty utilization = new javafx.beans.property.SimpleStringProperty();
    }

    static class EntityStatsRow {
        javafx.beans.property.SimpleStringProperty name = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleIntegerProperty exits = new javafx.beans.property.SimpleIntegerProperty();
        javafx.beans.property.SimpleIntegerProperty inSystem = new javafx.beans.property.SimpleIntegerProperty();
        javafx.beans.property.SimpleStringProperty avgSystemTime = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty avgNonValueTime = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty avgWaitTime = new javafx.beans.property.SimpleStringProperty();
        javafx.beans.property.SimpleStringProperty avgValueTime = new javafx.beans.property.SimpleStringProperty();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
