package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import com.simulation.core.SimulationEngine;
import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controlador principal con actualización en tiempo real de gráficas
 */
public class MainController {
    @FXML private BorderPane rootPane;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resetButton;
    @FXML private Button parametersButton;
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab animationTab;
    @FXML private Tab resultsTab;
    @FXML private Tab chartsTab;

    // Pestañas de resultados
    @FXML private TabPane resultsTabPane;
    @FXML private Tab locationStatsTab;
    @FXML private Tab entityStatsTab;

    // Tablas de resultados
    @FXML private TableView<LocationStats> locationTable;
    @FXML private TableColumn<LocationStats, String> locNameCol;
    @FXML private TableColumn<LocationStats, Integer> locCapacityCol;
    @FXML private TableColumn<LocationStats, Integer> locEntriesCol;
    @FXML private TableColumn<LocationStats, Double> locTimePerEntryCol;
    @FXML private TableColumn<LocationStats, Double> locAvgContentCol;
    @FXML private TableColumn<LocationStats, Integer> locMaxContentCol;
    @FXML private TableColumn<LocationStats, Integer> locCurrentContentCol;
    @FXML private TableColumn<LocationStats, Double> locUtilizationCol;

    @FXML private TextArea entityStatsText;

    private SimulationParameters parameters;
    private SimulationEngine engine;
    private AnimationPanel animationPanel;
    private ScrollPane animationScrollPane;
    private RealTimeChartsPanel realTimeChartsPanel; // NUEVO: Panel de gráficas en tiempo real
    private AnimationTimer animationTimer;
    private Thread simulationThread;

    public void initialize() {
        parameters = new SimulationParameters();
        engine = new SimulationEngine(parameters);

        setupAnimationPanel();
        setupRealTimeCharts(); // NUEVO
        setupControls();
        setupResultsTables();
        setupAnimationLoop();
        realTimeChartsPanel.initializeState(engine.getStatistics());

        updateStatus("Listo para iniciar");
    }

    private void setupAnimationPanel() {
        animationPanel = new AnimationPanel(engine);
        animationScrollPane = createAnimationScrollPane(animationPanel);
        animationTab.setContent(animationScrollPane);
        animationTab.setDisable(false);
        animationPanel.render();
    }

    private ScrollPane createAnimationScrollPane(AnimationPanel panel) {
        ScrollPane scrollPane = new ScrollPane(panel);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(false);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setPannable(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        return scrollPane;
    }

    /**
     * NUEVO: Configura el panel de gráficas en tiempo real
     */
    private void setupRealTimeCharts() {
        realTimeChartsPanel = new RealTimeChartsPanel();
        if (chartsTab != null) {
            chartsTab.setContent(realTimeChartsPanel);
            chartsTab.setDisable(false);
        }
    }

    private void setupControls() {
        startButton.setOnAction(e -> handleStart());
        pauseButton.setOnAction(e -> handlePause());
        resetButton.setOnAction(e -> handleReset());
        parametersButton.setOnAction(e -> handleParameters());

        pauseButton.setDisable(true);

        speedSlider.setMin(1);
        speedSlider.setMax(1000);
        speedSlider.setValue(100);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = newVal.doubleValue();
            engine.setSimulationSpeed(speed);
            updateSpeedLabel(speed);
        });

        updateSpeedLabel(100);
    }

    private void setupResultsTables() {
        // Configurar tabla de locaciones
        locNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        locCapacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        locEntriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries"));
        locTimePerEntryCol.setCellValueFactory(new PropertyValueFactory<>("timePerEntry"));
        locAvgContentCol.setCellValueFactory(new PropertyValueFactory<>("avgContent"));
        locMaxContentCol.setCellValueFactory(new PropertyValueFactory<>("maxContent"));
        locCurrentContentCol.setCellValueFactory(new PropertyValueFactory<>("currentContent"));
        locUtilizationCol.setCellValueFactory(new PropertyValueFactory<>("utilization"));

        // Formato de columnas numéricas
        locTimePerEntryCol.setCellFactory(col -> new TableCell<LocationStats, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        locAvgContentCol.setCellFactory(col -> new TableCell<LocationStats, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                }
            }
        });

        locUtilizationCol.setCellFactory(col -> new TableCell<LocationStats, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item));

                    // Colorear según utilización
                    if (item < 50) {
                        setStyle("-fx-background-color: #C8E6C9;");
                    } else if (item < 80) {
                        setStyle("-fx-background-color: #FFF9C4;");
                    } else {
                        setStyle("-fx-background-color: #FFCDD2;");
                    }
                }
            }
        });
    }

    private void setupAnimationLoop() {
        animationTimer = new AnimationTimer() {
            private long lastUpdate = 0;
            private static final long UPDATE_INTERVAL = 50_000_000; // 50ms

            @Override
            public void handle(long now) {
                if (engine.isRunning() && now - lastUpdate >= UPDATE_INTERVAL) {
                    updateDisplay();
                    updateRealTimeCharts(); // NUEVO: Actualizar gráficas en tiempo real
                    lastUpdate = now;
                }
            }
        };
    }

    @FXML
    private void handleStart() {
        if (engine.isRunning()) {
            return;
        }

        startButton.setDisable(true);
        pauseButton.setDisable(false);
        resetButton.setDisable(true);
        parametersButton.setDisable(true);

        engine.initialize();

        // Inicializar gráficas en tiempo real
        realTimeChartsPanel.initializeState(engine.getStatistics());

        simulationThread = new Thread(() -> {
            engine.run();

            Platform.runLater(() -> {
                handleSimulationComplete();
            });
        });

        simulationThread.setDaemon(true);
        simulationThread.start();

        animationTimer.start();
        updateStatus("Simulación en ejecución...");
    }

    @FXML
    private void handlePause() {
        if (engine.isPaused()) {
            engine.resume();
            pauseButton.setText("Pausar");
            updateStatus("Simulación en ejecución...");
        } else {
            engine.pause();
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
            engine.stop();
            try {
                simulationThread.join(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        engine = new SimulationEngine(parameters);
        setupAnimationPanel();
        realTimeChartsPanel.initializeState(engine.getStatistics());

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        pauseButton.setText("Pausar");
        resetButton.setDisable(false);
        parametersButton.setDisable(false);

        timeLabel.setText("Tiempo: 0 días 00:00");
        updateStatus("Listo para iniciar");

        locationTable.getItems().clear();
        entityStatsText.clear();
    }

    @FXML
    private void handleParameters() {
        ParametersDialog dialog = new ParametersDialog(parameters);
        dialog.showAndWait();

        if (dialog.isAccepted()) {
            engine = new SimulationEngine(parameters);
            setupAnimationPanel();
            realTimeChartsPanel.initializeState(engine.getStatistics());
            updateStatus("Parámetros actualizados");
        }
    }

    private void handleSimulationComplete() {
        animationTimer.stop();

        startButton.setDisable(true);
        pauseButton.setDisable(true);
        resetButton.setDisable(false);
        parametersButton.setDisable(false);

        updateStatus("Simulación completada");
        updateResults();

        // NUEVO: Actualizar gráficas finales
        updateRealTimeCharts();

        mainTabPane.getSelectionModel().select(chartsTab);
    }

    private void updateDisplay() {
        double currentTime = engine.getCurrentTime();

        Platform.runLater(() -> {
            // Actualizar tiempo
            int days = (int) (currentTime / (24 * 60));
            int hours = (int) ((currentTime % (24 * 60)) / 60);
            int minutes = (int) (currentTime % 60);
            timeLabel.setText(String.format("Tiempo: %d días %02d:%02d", days, hours, minutes));

            // Renderizar animación
            animationPanel.render();
        });
    }

    /**
     * NUEVO: Actualiza las gráficas en tiempo real durante la simulación
     */
    private void updateRealTimeCharts() {
        Statistics stats = engine.getStatistics();
        double currentTime = engine.getCurrentTime();

        Platform.runLater(() -> {
            realTimeChartsPanel.updateCharts(stats, currentTime);
        });
    }

    private void updateStatus(String message) {
        statusLabel.setText("Estado: " + message);
    }

    private void updateSpeedLabel(double speed) {
        if (speed >= 1000) {
            speedLabel.setText("Velocidad: Máxima");
        } else if (speed >= 100) {
            speedLabel.setText(String.format("Velocidad: %.0fx", speed / 100.0));
        } else {
            speedLabel.setText(String.format("Velocidad: %.1fx", speed / 100.0));
        }
    }

    private void updateResults() {
        updateLocationStats();
        updateEntityStats();
    }

    private void updateLocationStats() {
        Statistics stats = engine.getStatistics();
        double currentTime = engine.getCurrentTime();

        List<LocationStats> locationStatsList = new ArrayList<>();
        Map<String, Location> locations = stats.getLocations();
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            Location loc = entry.getValue();

            String name = loc.getName();
            int capacity = loc.getCapacity();
            int totalEntries = loc.getTotalEntries();
            double timePerEntry = loc.getAverageTimePerEntry(currentTime);
            double avgContent = loc.getAverageContent(currentTime);
            int maxContent = capacity;
            int currentContent = loc.getCurrentContent();
            double utilization = loc.getUtilization(currentTime);

            locationStatsList.add(new LocationStats(
                name,
                capacity,
                totalEntries,
                timePerEntry,
                avgContent,
                maxContent,
                currentContent,
                utilization
            ));
        }

        locationTable.getItems().setAll(locationStatsList);
    }

    private void updateEntityStats() {
        Statistics stats = engine.getStatistics();

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

    public static class LocationStats {
        private final String name;
        private final Integer capacity;
        private final Integer totalEntries;
        private final Double timePerEntry;
        private final Double avgContent;
        private final Integer maxContent;
        private final Integer currentContent;
        private final Double utilization;

        public LocationStats(String name,
                              Integer capacity,
                              Integer totalEntries,
                              Double timePerEntry,
                              Double avgContent,
                              Integer maxContent,
                              Integer currentContent,
                              Double utilization) {
            this.name = name;
            this.capacity = capacity == Integer.MAX_VALUE ? -1 : capacity;
            this.totalEntries = totalEntries;
            this.timePerEntry = timePerEntry;
            this.avgContent = avgContent;
            this.maxContent = maxContent == Integer.MAX_VALUE ? -1 : maxContent;
            this.currentContent = currentContent;
            this.utilization = utilization;
        }

        public String getName() { return name; }
        public Integer getCapacity() { return capacity; }
        public Integer getTotalEntries() { return totalEntries; }
        public Double getTimePerEntry() { return timePerEntry; }
        public Double getAvgContent() { return avgContent; }
        public Integer getMaxContent() { return maxContent; }
        public Integer getCurrentContent() { return currentContent; }
        public Double getUtilization() { return utilization; }
    }
}