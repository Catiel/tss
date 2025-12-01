package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.core.SimulationListener;
import com.simulation.entities.Entity;
import com.simulation.locations.Location;
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
    // endTime ahora se obtiene de simulationConfig.getSimulationTimeInMinutes()
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
    private BarChart<String, Number> locationUtilizationChart;
    private Stage primaryStage;
    // Animación
    private AnimationTimer animationTimer;
    private Thread simulationThread;
    private double simulationSpeed = 1.0;
    private double currentTime = 0;
    private long lastUIUpdate = 0; // Control de frecuencia de actualización UI
    // Sistema de visualización mejorado
    private AnimationController animationController;

    // Sistema de configuración parametrizable
    private com.simulation.config.SimulationConfig simulationConfig;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Simulador de Manufactura - Engranes de Acero SA");

        // Inicializar configuración
        this.simulationConfig = new com.simulation.config.SimulationConfig();

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

    // ==================== SISTEMA DE ZOOM POR GESTOS ====================
    private javafx.scene.Group zoomGroup;
    private javafx.scene.layout.Pane canvasHolder;
    private ScrollPane animationScrollPane;
    private Label zoomPercentLabel;
    private static final double MIN_ZOOM = 0.25;
    private static final double MAX_ZOOM = 3.0;
    private double currentScale = 1.0;

    private BorderPane createAnimationCanvas() {
        // Crear el AnimationController
        animationController = new AnimationController(1400, 800);
        canvas = animationController.getCanvas();
        gc = canvas.getGraphicsContext2D();

        // Group que contendrá el canvas - usamos Scale transform con pivot en (0,0)
        zoomGroup = new javafx.scene.Group(canvas);
        
        // Pane que contiene el group y se redimensiona según el zoom
        canvasHolder = new javafx.scene.layout.Pane(zoomGroup);
        canvasHolder.setStyle("-fx-background-color: #1A252F;");
        // Tamaño inicial igual al canvas
        canvasHolder.setPrefSize(canvas.getWidth(), canvas.getHeight());
        canvasHolder.setMinSize(canvas.getWidth(), canvas.getHeight());
        
        // ScrollPane para navegación
        animationScrollPane = new ScrollPane(canvasHolder);
        animationScrollPane.setStyle("-fx-background: #1A252F; -fx-background-color: #1A252F;");
        animationScrollPane.setPannable(true);
        animationScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        animationScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        animationScrollPane.setFitToWidth(false);
        animationScrollPane.setFitToHeight(false);
        
        // Zoom con Ctrl + rueda del mouse hacia la posición del cursor
        animationScrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, event -> {
            if (event.isControlDown()) {
                event.consume();
                
                double delta = event.getDeltaY();
                if (delta == 0) return;
                
                double zoomFactor = (delta > 0) ? 1.15 : 0.85;
                
                // Posición del mouse en el viewport
                double viewportX = event.getX();
                double viewportY = event.getY();
                
                // Posición actual del scroll (0.0 a 1.0)
                double hValue = animationScrollPane.getHvalue();
                double vValue = animationScrollPane.getVvalue();
                
                // Calcular posición del mouse en el contenido
                double viewportWidth = animationScrollPane.getViewportBounds().getWidth();
                double viewportHeight = animationScrollPane.getViewportBounds().getHeight();
                double contentWidth = canvasHolder.getPrefWidth();
                double contentHeight = canvasHolder.getPrefHeight();
                
                // Posición del mouse en coordenadas del contenido
                double scrollableWidth = Math.max(0, contentWidth - viewportWidth);
                double scrollableHeight = Math.max(0, contentHeight - viewportHeight);
                double contentX = hValue * scrollableWidth + viewportX;
                double contentY = vValue * scrollableHeight + viewportY;
                
                // Aplicar zoom
                double oldScale = currentScale;
                double newScale = currentScale * zoomFactor;
                newScale = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, newScale));
                
                if (newScale != oldScale) {
                    currentScale = newScale;
                    applyZoomTransform();
                    
                    // Recalcular scroll para mantener el punto bajo el cursor
                    double newContentWidth = canvasHolder.getPrefWidth();
                    double newContentHeight = canvasHolder.getPrefHeight();
                    double scaleRatio = newScale / oldScale;
                    
                    double newContentX = contentX * scaleRatio;
                    double newContentY = contentY * scaleRatio;
                    
                    double newScrollableWidth = Math.max(1, newContentWidth - viewportWidth);
                    double newScrollableHeight = Math.max(1, newContentHeight - viewportHeight);
                    
                    double newHValue = (newContentX - viewportX) / newScrollableWidth;
                    double newVValue = (newContentY - viewportY) / newScrollableHeight;
                    
                    animationScrollPane.setHvalue(Math.max(0, Math.min(1, newHValue)));
                    animationScrollPane.setVvalue(Math.max(0, Math.min(1, newVValue)));
                }
            }
        });

        // Layout principal - SIN barra de herramientas, solo gestos
        BorderPane mainContainer = new BorderPane();
        mainContainer.setCenter(animationScrollPane);
        mainContainer.setStyle("-fx-background-color: #1A252F;");
        
        // Indicador de zoom discreto en esquina (solo texto, desaparece)
        zoomPercentLabel = new Label("100%");
        zoomPercentLabel.setTextFill(Color.web("#e94560"));
        zoomPercentLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        zoomPercentLabel.setStyle("-fx-background-color: rgba(0,0,0,0.5); -fx-padding: 3 8; -fx-background-radius: 3;");
        zoomPercentLabel.setOpacity(0);
        
        javafx.scene.layout.StackPane stackContainer = new javafx.scene.layout.StackPane(mainContainer, zoomPercentLabel);
        javafx.scene.layout.StackPane.setAlignment(zoomPercentLabel, Pos.BOTTOM_RIGHT);
        javafx.scene.layout.StackPane.setMargin(zoomPercentLabel, new Insets(0, 15, 15, 0));

        BorderPane wrapper = new BorderPane(stackContainer);
        wrapper.setStyle("-fx-background-color: #1A252F;");
        
        return wrapper;
    }
    
    private void applyZoomTransform() {
        // Aplicar escala usando Scale transform con pivot en (0,0)
        zoomGroup.getTransforms().clear();
        javafx.scene.transform.Scale scaleTransform = new javafx.scene.transform.Scale(currentScale, currentScale, 0, 0);
        zoomGroup.getTransforms().add(scaleTransform);
        
        // Actualizar tamaño del contenedor para que el scroll funcione correctamente
        double newWidth = canvas.getWidth() * currentScale;
        double newHeight = canvas.getHeight() * currentScale;
        canvasHolder.setPrefSize(newWidth, newHeight);
        canvasHolder.setMinSize(newWidth, newHeight);
        
        // Mostrar indicador de zoom temporalmente
        if (zoomPercentLabel != null) {
            zoomPercentLabel.setText(String.format("%.0f%%", currentScale * 100));
            zoomPercentLabel.setOpacity(1);
            
            // Fade out después de 1 segundo
            javafx.animation.PauseTransition pause = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(1));
            pause.setOnFinished(e -> {
                javafx.animation.FadeTransition fade = new javafx.animation.FadeTransition(javafx.util.Duration.millis(500), zoomPercentLabel);
                fade.setFromValue(1);
                fade.setToValue(0);
                fade.play();
            });
            pause.play();
        }
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

        // Canvas de animación en el centro (ahora incluye controles de zoom)
        BorderPane animationPane = createAnimationCanvas();
        view.setCenter(animationPane);

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
        Label title = new Label("📊 Análisis de Utilización por Locación");
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#e94560"));

        // Descripción
        Label description = new Label(
                "Gráfica de barras mostrando el porcentaje de utilización de cada locación");
        description.setFont(Font.font("Segoe UI", 14));
        description.setTextFill(Color.web("#a8dadc"));
        description.setWrapText(true);

        // Crear gráfica de barras
        locationUtilizationChart = createLocationUtilizationChart();
        VBox.setVgrow(locationUtilizationChart, Priority.ALWAYS);

        view.getChildren().addAll(title, description, locationUtilizationChart);
        return view;
    }

    private BarChart<String, Number> createLocationUtilizationChart() {
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("");
        xAxis.setStyle("-fx-tick-label-fill: #f1faee; -fx-font-size: 9px;");
        xAxis.setTickLabelRotation(45);
        xAxis.setTickLabelGap(2);

        NumberAxis yAxis = new NumberAxis(0, 100, 10);
        yAxis.setLabel("Utilización %");
        yAxis.setStyle("-fx-tick-label-fill: #f1faee; -fx-font-size: 10px;");
        yAxis.setAutoRanging(false);
        yAxis.setMinorTickVisible(true);
        yAxis.setMinorTickCount(5);
        yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
            @Override
            public String toString(Number value) {
                return String.format("%.0f%%", value.doubleValue());
            }
        });

        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("Utilización de Locaciones");
        barChart.setTitleSide(javafx.geometry.Side.TOP);
        barChart.setLegendVisible(false);
        barChart.setAnimated(false);
        barChart.setCategoryGap(20);
        barChart.setBarGap(1);
        barChart.setMinHeight(400);
        barChart.setPrefWidth(900);
        barChart.setHorizontalGridLinesVisible(true);
        barChart.setVerticalGridLinesVisible(false);
        barChart.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-width: 2;" +
                        "-fx-border-radius: 8;" +
                        "-fx-background-radius: 8;" +
                        "-fx-padding: 10;");

        // Inicializar serie con todas las locaciones en orden fijo
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Utilización %");
        
        // Agregar locaciones en orden lógico del proceso
        String[] locationOrder = {"HORNO", "FRESADO", "TORNEADO", "CARGA", "RECTIFICADO", 
                                  "TALADRO", "INSPECCION", "DESCARGA", "BANDA 1", "BANDA 2", 
                                  "ALMACEN MP", "SALIDA"};
        for (String loc : locationOrder) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(loc, 0);
            series.getData().add(data);
        }
        barChart.getData().add(series);

        // Agregar tooltips a cada barra
        for (XYChart.Data<String, Number> data : series.getData()) {
            Tooltip tooltip = new Tooltip();
            tooltip.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
            
            // Actualizar tooltip cuando cambie el valor
            data.YValueProperty().addListener((obs, oldVal, newVal) -> {
                tooltip.setText(String.format("%s: %.2f%%", data.getXValue(), newVal.doubleValue()));
            });
            
            // Asociar tooltip al nodo cuando esté disponible
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip.install(newNode, tooltip);
                    newNode.setStyle("-fx-bar-fill: #e94560;");
                    
                    // Efecto hover
                    newNode.setOnMouseEntered(e -> newNode.setStyle("-fx-bar-fill: #ff6b6b;"));
                    newNode.setOnMouseExited(e -> newNode.setStyle("-fx-bar-fill: #e94560;"));
                }
            });
        }

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
        setupRoutes(engine);
        setupProcessingRules(engine);
        setupArrivals(engine);

        // Configurar tiempo final de simulación desde la configuración
        engine.setEndTime(simulationConfig.getSimulationTimeInMinutes());

        // Registrar este GUI como listener de eventos de simulación
        engine.addListener(this);

        // Configurar el engine en el AnimationController
        if (animationController != null) {
            animationController.setEngine(engine);
        }
    }

    private void setupEntityTypes(SimulationEngine engine) {
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0);
    }

    private void setupLocations(SimulationEngine engine) {
        // Usar capacidades desde la configuración
        engine.addLocation("ALMACEN_MP", Integer.MAX_VALUE, 1);
        // HORNO: capacidad independiente del tamaño del lote
        engine.addLocation("HORNO", simulationConfig.getLocationCapacity("HORNO"), 1);
        engine.addLocation("BANDA_1", Integer.MAX_VALUE, 1);
        engine.addLocation("CARGA", Integer.MAX_VALUE, 1);
        engine.addLocation("TORNEADO", simulationConfig.getLocationCapacity("TORNEADO"), 1);
        engine.addLocation("FRESADO", simulationConfig.getLocationCapacity("FRESADO"), 1);
        engine.addLocation("TALADRO", simulationConfig.getLocationCapacity("TALADRO"), 1);
        engine.addLocation("RECTIFICADO", simulationConfig.getLocationCapacity("RECTIFICADO"), 1);
        engine.addLocation("DESCARGA", Integer.MAX_VALUE, 1);
        engine.addLocation("BANDA_2", Integer.MAX_VALUE, 1);
        engine.addLocation("INSPECCION", simulationConfig.getLocationCapacity("INSPECCION"), 1);
        engine.addLocation("SALIDA", Integer.MAX_VALUE, 1);
    }

    private void setupResources(SimulationEngine engine) {
        // Usar configuración para recursos
        engine.addResource("GRUA_VIAJERA", simulationConfig.getGruaQuantity(), simulationConfig.getGruaEmptySpeed());
        engine.addResource("ROBOT", simulationConfig.getRobotQuantity(), simulationConfig.getRobotEmptySpeed());
    }

    private void setupRoutes(SimulationEngine engine) {
        // RUTAS CON GRUA_VIAJERA (25 min por movimiento)
        engine.addRoute("ALMACEN_MP", "HORNO", "GRUA_VIAJERA", 25.0);
        engine.addRoute("HORNO", "BANDA_1", "GRUA_VIAJERA", 25.0);

        // RUTAS CON ROBOT (45 min por movimiento)
        engine.addRoute("CARGA", "TORNEADO", "ROBOT", 45.0);
        engine.addRoute("TORNEADO", "FRESADO", "ROBOT", 45.0);
        engine.addRoute("FRESADO", "TALADRO", "ROBOT", 45.0);
        engine.addRoute("TALADRO", "RECTIFICADO", "ROBOT", 45.0);
        engine.addRoute("RECTIFICADO", "DESCARGA", "ROBOT", 45.0);

        // RUTAS SIN RECURSO (bandas transportadoras)
        engine.addRoute("BANDA_1", "CARGA", null, 0.94);
        engine.addRoute("DESCARGA", "BANDA_2", null, 1.02);
        engine.addRoute("BANDA_2", "INSPECCION", null, 0.5);
        engine.addRoute("INSPECCION", "SALIDA", null, 0.5);
    }

    private void setupProcessingRules(SimulationEngine engine) {
        // Flujo de proceso para PIEZA_AUTOMOTRIZ - usando tiempos de configuración

        // 1. ALMACEN_MP: Sin procesamiento, solo recepción
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 2. HORNO: Procesamiento por lotes desde configuración
        engine.addProcessingRule(new BatchProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getHornoProcessingTime(), simulationConfig.getHornoBatchSize()));

        // 3. BANDA_1: Transporte desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("BANDA_1", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("BANDA_1")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 4. CARGA: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("CARGA", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("CARGA")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 5. TORNEADO: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("TORNEADO", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("TORNEADO")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 6. FRESADO: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("FRESADO", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("FRESADO")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 7. TALADRO: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("TALADRO", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("TALADRO")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 8. RECTIFICADO: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("RECTIFICADO", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("RECTIFICADO")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 9. DESCARGA: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("DESCARGA", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("DESCARGA")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 10. BANDA_2: desde configuración
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("BANDA_2", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("BANDA_2")) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 11. INSPECCION: desde configuración (puede ser exponencial o fija)
        engine.addProcessingRule(new com.simulation.processing.ProcessingRule("INSPECCION", "PIEZA_AUTOMOTRIZ", 
                simulationConfig.getProcessingTime("INSPECCION")) {
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
        // Usar parámetros de llegada desde la configuración
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 
                simulationConfig.getArrivalFirstTime(), 
                simulationConfig.getArrivalMaxEntities(), 
                simulationConfig.getArrivalMeanTime());
    }

    private void setupLocationPositions() {
        // Ya no se necesita - AnimationController tiene sus propias posiciones
        // Mantener por compatibilidad pero no se usa
    }

    private void startAnimationTimer() {
        // Iniciar el AnimationController mejorado
        if (animationController != null) {
            animationController.start();
        }

        // Timer separado solo para estadísticas UI
        animationTimer = new AnimationTimer() {
            private int frameCount = 0;

            @Override
            public void handle(long now) {
                // Actualizar estadísticas cada 30 frames (2 veces por segundo a 60fps)
                frameCount++;
                if (frameCount >= 30) {
                    updateStatistics();
                    frameCount = 0;
                }
            }
        };
        animationTimer.start();
    }

    // Ya no es necesario - AnimationController maneja el renderizado completo

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
                int numReplicas = simulationConfig.getNumberOfReplicas();
                for (int replica = 1; replica <= numReplicas; replica++) {
                    final int currentReplica = replica;
                    Platform.runLater(() -> {
                        statusLabel.setText("Ejecutando Réplica " + currentReplica + " / " + simulationConfig.getNumberOfReplicas());
                        // AnimationController se reinicia automáticamente
                    });

                    // Reiniciar motor para cada réplica
                    setupSimulationEngine();
                    currentTime = 0;
                    double simEndTime = simulationConfig.getSimulationTimeInMinutes();

                    while (running.get() && currentTime < simEndTime) {
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
                        // Fórmula ProModel para capacidades infinitas
                        if (locName.equals("ALMACEN_MP") || locName.equals("CARGA") || 
                            locName.equals("DESCARGA") || locName.equals("SALIDA")) {
                            utilizationSum += (ls.getAverageContents() / 999999.0) * 100.0;
                        } else if (locName.equals("BANDA_1")) {
                            utilizationSum += (ls.getAverageContents() / 17.0) * 100.0;
                        } else if (locName.equals("BANDA_2")) {
                            utilizationSum += (ls.getAverageContents() / 22.0) * 100.0;
                        } else {
                            utilizationSum += ls.getBusyUtilizationPercent();
                        }
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

        // Pausar/reanudar AnimationController
        if (animationController != null) {
            if (paused.get()) {
                animationController.pause();
            } else {
                animationController.resume();
            }
        }
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
        // AnimationController se reinicia con el engine
        updateUI();
        statusLabel.setText("Reiniciado");
    }

    private void updateUI() {
        Platform.runLater(() -> {
            timeLabel.setText(String.format("Tiempo: %.2f min (%.2f hrs)", currentTime, currentTime / 60.0));
            progressBar.setProgress(currentTime / simulationConfig.getSimulationTimeInMinutes());
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

                    // Actualizar ocupación en LocationNode a través de AnimationController
                    if (animationController != null) {
                        animationController.updateLocationState(locName,
                                location.getCurrentOccupancy() > 0,
                                stats != null && stats.getTotalEntries() > 0);
                    }

                    if (stats != null) {
                        row.scheduledTime.set(String.format("%.2f", stats.getScheduledTime() / 60.0));
                        row.capacity.set(stats.getCapacity());
                        row.totalEntries.set(stats.getTotalEntries());
                        row.avgTimePerEntry.set(String.format("%.2f", stats.getAverageTimePerEntry()));
                        row.avgContents.set(String.format("%.2f", stats.getAverageContents()));
                        row.maxContents.set(String.format("%.2f", stats.getMaxContents()));
                        row.currentContents.set((int) stats.getCurrentContents());
                        // Fórmula ProModel para capacidades infinitas
                        if (locName.equals("ALMACEN_MP") || locName.equals("CARGA") || 
                            locName.equals("DESCARGA") || locName.equals("SALIDA")) {
                            double promodelUtil = (stats.getAverageContents() / 999999.0) * 100.0;
                            row.utilization.set(String.format("%.2f", promodelUtil));
                        } else if (locName.equals("BANDA_1")) {
                            double promodelUtil = (stats.getAverageContents() / 17.0) * 100.0;
                            row.utilization.set(String.format("%.2f", promodelUtil));
                        } else if (locName.equals("BANDA_2")) {
                            double promodelUtil = (stats.getAverageContents() / 22.0) * 100.0;
                            row.utilization.set(String.format("%.2f", promodelUtil));
                        } else {
                            row.utilization.set(String.format("%.2f", stats.getBusyUtilizationPercent()));
                        }
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

            // Actualizar gráfica de barras con utilización de locaciones (sin parpadeo)
            if (locationUtilizationChart != null && !locationUtilizationChart.getData().isEmpty()) {
                XYChart.Series<String, Number> series = locationUtilizationChart.getData().get(0);

                // Mapear nombres formateados a utilización
                Map<String, Double> utilizationMap = new HashMap<>();
                for (com.simulation.locations.Location location : engine.getAllLocations().values()) {
                    String locName = location.getName();
                    com.simulation.locations.LocationStatistics stats = locStats.get(locName);
                    double utilization = 0.0;
                    if (stats != null) {
                        // Usar la misma fórmula que en la tabla
                        if (locName.equals("ALMACEN_MP") || locName.equals("CARGA") || 
                            locName.equals("DESCARGA") || locName.equals("SALIDA")) {
                            utilization = (stats.getAverageContents() / 999999.0) * 100.0;
                        } else if (locName.equals("BANDA_1")) {
                            utilization = (stats.getAverageContents() / 17.0) * 100.0;
                        } else if (locName.equals("BANDA_2")) {
                            utilization = (stats.getAverageContents() / 22.0) * 100.0;
                        } else {
                            utilization = stats.getBusyUtilizationPercent();
                        }
                    }
                    String formattedName = formatLocationName(locName);
                    utilizationMap.put(formattedName, utilization);
                }

                // Actualizar valores existentes sin recrear (evita parpadeo)
                for (XYChart.Data<String, Number> data : series.getData()) {
                    String locName = data.getXValue();
                    Double util = utilizationMap.get(locName);
                    if (util != null) {
                        data.setYValue(util);
                    }
                }
            }
        });
    }

    // --- Implementación de SimulationListener ---

    @Override
    public void onEntityCreated(Entity entity, Location location, double time) {
        // El AnimationController maneja esto automáticamente
    }

    @Override
    public void onEntityArrival(Entity entity, Location location, double time) {
        // El estado visual ya está sincronizado con entity.getState()
        // No es necesario actualizar nada aquí
    }

    @Override
    public void onEntityMove(Entity entity, Location from, Location to, double time) {
        // ACTUALIZAR ESTADOS AL SALIR DE PROCESAMIENTO
        if ("HORNO".equals(from.getName())) {
            // Al salir del horno → pieza tratada térmicamente
            entity.setState(com.simulation.entities.EntityState.HEAT_TREATED);
        } else if ("TORNEADO".equals(from.getName())) {
            // Al salir del torneado → pieza maquinada (engrane)
            entity.setState(com.simulation.entities.EntityState.MACHINED);
        }

        // Iniciar animación de movimiento con AnimationController mejorado
        if (animationController != null) {
            // Determinar si usa recurso (GRUA_VIAJERA o ROBOT)
            String resourceName = null;

            if (("ALMACEN_MP".equals(from.getName()) && "HORNO".equals(to.getName())) ||
                    ("HORNO".equals(from.getName()) && "BANDA_1".equals(to.getName()))) {
                resourceName = "GRUA_VIAJERA";
            } else if (("CARGA".equals(from.getName()) && "TORNEADO".equals(to.getName())) ||
                    ("TORNEADO".equals(from.getName()) && "FRESADO".equals(to.getName())) ||
                    ("FRESADO".equals(from.getName()) && "TALADRO".equals(to.getName())) ||
                    ("TALADRO".equals(from.getName()) && "RECTIFICADO".equals(to.getName())) ||
                    ("RECTIFICADO".equals(from.getName()) && "DESCARGA".equals(to.getName()))) {
                resourceName = "ROBOT";
            }

            // Animar el movimiento
            animationController.animateEntityMovement(
                    entity,
                    from.getName(),
                    to.getName(),
                    resourceName,
                    null // callback
            );
        }
    }

    @Override
    public void onEntityExit(Entity entity, Location location, double time) {
        // Entidad sale del sistema - limpiar sprite para evitar fantasmas
        if (animationController != null) {
            animationController.removeEntitySprite(entity.getId());
        }
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
        if (simulationConfig == null) {
            simulationConfig = new com.simulation.config.SimulationConfig();
        }

        ConfigurationDialog dialog = new ConfigurationDialog(simulationConfig);
        dialog.showAndWait();

        if (dialog.wasAccepted()) {
            // La configuración ya está actualizada en simulationConfig
            // Reconstruir el motor de simulación con los nuevos parámetros
            resetSimulation();
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("✅ Configuración Actualizada");
            alert.setHeaderText("¡Configuración aplicada exitosamente!");
            alert.setContentText("El motor de simulación ha sido reconfigurado con los nuevos parámetros.\n\n" +
                "Resumen:\n" +
                "• Tiempo de simulación: " + String.format("%.2f", simulationConfig.getSimulationTime()) + " hrs\n" +
                "• Réplicas: " + simulationConfig.getNumberOfReplicas() + "\n" +
                "• Llegadas (media): " + simulationConfig.getArrivalMeanTime() + " min\n" +
                "• Lote del Horno: " + simulationConfig.getHornoBatchSize() + " piezas\n\n" +
                "Presione 'Iniciar' para comenzar la simulación.");
            alert.showAndWait();
        }
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
