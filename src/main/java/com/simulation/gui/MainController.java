package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import com.simulation.core.ReplicationManager;
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
 * Controlador principal mejorado con gráficas y contadores en tiempo real
 */
public class MainController {
    @FXML private BorderPane rootPane;
    @FXML private Button startButton;
    @FXML private Button pauseButton;
    @FXML private Button resetButton;
    @FXML private Button parametersButton;
    @FXML private Button runReplicationsButton; // NUEVO
    @FXML private Label statusLabel;
    @FXML private Label timeLabel;
    @FXML private Slider speedSlider;
    @FXML private Label speedLabel;
    @FXML private TabPane mainTabPane;
    @FXML private Tab animationTab;
    @FXML private Tab resultsTab;
    @FXML private Tab chartsTab; // NUEVO

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
    private UtilizationChartPanel utilizationChartPanel; // NUEVO
    private AnimationTimer animationTimer;
    private Thread simulationThread;
    private ReplicationManager replicationManager; // NUEVO

    public void initialize() {
        parameters = new SimulationParameters();
        engine = new SimulationEngine(parameters);

        setupAnimationPanel();
        setupUtilizationChart(); // NUEVO
        setupControls();
        setupResultsTables();
        setupAnimationLoop();

        updateStatus("Listo para iniciar");
    }

    private void setupAnimationPanel() {
        animationPanel = new AnimationPanel(engine);
        animationTab.setContent(animationPanel);
    }

    /**
     * NUEVO: Configura el panel de gráfica de utilización
     */
    private void setupUtilizationChart() {
        utilizationChartPanel = new UtilizationChartPanel();

        // Si existe la pestaña de gráficas, agregar contenido
        if (chartsTab != null) {
            chartsTab.setContent(utilizationChartPanel);
        }
    }

    private void setupControls() {
        startButton.setOnAction(e -> handleStart());
        pauseButton.setOnAction(e -> handlePause());
        resetButton.setOnAction(e -> handleReset());
        parametersButton.setOnAction(e -> handleParameters());

        // NUEVO: Botón para ejecutar réplicas
        if (runReplicationsButton != null) {
            runReplicationsButton.setOnAction(e -> handleRunReplications());
        }

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
        if (runReplicationsButton != null) {
            runReplicationsButton.setDisable(true);
        }

        engine.initialize();

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
        animationPanel = new AnimationPanel(engine);
        animationTab.setContent(animationPanel);

        startButton.setDisable(false);
        pauseButton.setDisable(true);
        pauseButton.setText("Pausar");
        resetButton.setDisable(false);
        parametersButton.setDisable(false);
        if (runReplicationsButton != null) {
            runReplicationsButton.setDisable(false);
        }

        timeLabel.setText("Tiempo: 0 días 00:00");
        updateStatus("Listo para iniciar");

        locationTable.getItems().clear();
        entityStatsText.clear();
        utilizationChartPanel.clear();
    }

    @FXML
    private void handleParameters() {
        ParametersDialog dialog = new ParametersDialog(parameters);
        dialog.showAndWait();

        if (dialog.isAccepted()) {
            engine = new SimulationEngine(parameters);
            animationPanel = new AnimationPanel(engine);
            animationTab.setContent(animationPanel);
            updateStatus("Parámetros actualizados");
        }
    }

    /**
     * NUEVO: Ejecuta 3 réplicas de la simulación
     */
    @FXML
    private void handleRunReplications() {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Ejecutar Réplicas");
        confirmAlert.setHeaderText("Ejecutar 3 réplicas de la simulación");
        confirmAlert.setContentText("Esto ejecutará 3 corridas completas. ¿Desea continuar?");

        if (confirmAlert.showAndWait().get() != ButtonType.OK) {
            return;
        }

        // Deshabilitar controles
        startButton.setDisable(true);
        resetButton.setDisable(true);
        parametersButton.setDisable(true);
        runReplicationsButton.setDisable(true);

        updateStatus("Ejecutando réplicas...");

        // Crear gestor de réplicas
        replicationManager = new ReplicationManager(parameters, 3);

        // Ejecutar réplicas en segundo plano
        replicationManager.runReplications(
            // Callback de progreso
            replicationNum -> {
                Platform.runLater(() -> {
                    updateStatus(String.format("Ejecutando réplica %d de 3...", replicationNum));
                });
            },
            // Callback de completación
            () -> {
                Platform.runLater(() -> {
                    handleReplicationsComplete();
                });
            }
        );
    }

    /**
     * NUEVO: Maneja la completación de las réplicas
     */
    private void handleReplicationsComplete() {
        ReplicationManager.AggregatedStatistics aggStats = replicationManager.getAggregatedStatistics();

        if (aggStats != null) {
            // Actualizar gráfica de utilización
            utilizationChartPanel.updateChart(aggStats);
            utilizationChartPanel.setSubtitle(
                String.format("3 Réplicas - IC 95%%: [%.2f, %.2f] piezas/hora",
                    aggStats.getCiThroughput()[0], aggStats.getCiThroughput()[1])
            );

            // Mostrar resultados agregados
            showAggregatedResults(aggStats);

            // Cambiar a pestaña de gráficas
            if (chartsTab != null) {
                mainTabPane.getSelectionModel().select(chartsTab);
            }
        }

        startButton.setDisable(false);
        resetButton.setDisable(false);
        parametersButton.setDisable(false);
        runReplicationsButton.setDisable(false);

        updateStatus("Réplicas completadas");
    }

    /**
     * NUEVO: Muestra resultados agregados de réplicas
     */
    private void showAggregatedResults(ReplicationManager.AggregatedStatistics aggStats) {
        Alert resultAlert = new Alert(Alert.AlertType.INFORMATION);
        resultAlert.setTitle("Resultados de Réplicas");
        resultAlert.setHeaderText("Estadísticas Agregadas (3 Réplicas)");

        StringBuilder content = new StringBuilder();
        content.append(String.format("📊 Throughput Promedio: %.2f piezas/hora\n", aggStats.getAvgThroughput()));
        content.append(String.format("   IC 95%%: [%.2f, %.2f]\n\n",
            aggStats.getCiThroughput()[0], aggStats.getCiThroughput()[1]));

        content.append(String.format("⏱ Tiempo en Sistema Promedio: %.2f min\n", aggStats.getAvgAverageSystemTime()));
        content.append(String.format("   IC 95%%: [%.2f, %.2f]\n\n",
            aggStats.getCiAverageSystemTime()[0], aggStats.getCiAverageSystemTime()[1]));

        content.append(String.format("📦 Piezas Completadas Promedio: %.0f\n", aggStats.getAvgTotalExits()));
        content.append(String.format("   IC 95%%: [%.0f, %.0f]\n",
            aggStats.getCiTotalExits()[0], aggStats.getCiTotalExits()[1]));

        resultAlert.setContentText(content.toString());
        resultAlert.showAndWait();
    }

    private void handleSimulationComplete() {
        animationTimer.stop();

        startButton.setDisable(true);
        pauseButton.setDisable(true);
        resetButton.setDisable(false);
        parametersButton.setDisable(false);
        if (runReplicationsButton != null) {
            runReplicationsButton.setDisable(false);
        }

        updateStatus("Simulación completada");
        updateResults();

        // Actualizar gráfica de utilización
        Statistics stats = engine.getStatistics();
        utilizationChartPanel.updateChart(stats, engine.getCurrentTime());
        utilizationChartPanel.setSubtitle("Simulación Individual");

        mainTabPane.getSelectionModel().select(resultsTab);
    }

    private void updateDisplay() {
        double currentTime = engine.getCurrentTime();

        Platform.runLater(() -> {
            // Actualizar tiempo en formato días:horas:minutos
            int days = (int) (currentTime / (24 * 60));
            int hours = (int) ((currentTime % (24 * 60)) / 60);
            int minutes = (int) (currentTime % 60);
            timeLabel.setText(String.format("Tiempo: %d días %02d:%02d", days, hours, minutes));

            // Renderizar animación con contadores
            animationPanel.render();
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
                name, capacity, totalEntries, timePerEntry, avgContent,
                maxContent, currentContent, utilization
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
        sb.append(String.format("En Sistema Actualmente: %d\n\n",
            stats.getTotalArrivals() - stats.getTotalExits()));

        sb.append(String.format("Throughput: %.2f piezas/hora\n\n", stats.getThroughput()));

        sb.append("=== TIEMPO EN SISTEMA ===\n");
        sb.append(String.format("Promedio: %.2f min\n", stats.getAverageSystemTime()));
        sb.append(String.format("Desviación Estándar: %.2f min\n", stats.getStdDevSystemTime()));
        sb.append(String.format("Mínimo: %.2f min\n", stats.getMinSystemTime()));
        sb.append(String.format("Máximo: %.2f min\n", stats.getMaxSystemTime()));

        entityStatsText.setText(sb.toString());
    }

    // Clase interna para datos de la tabla
    public static class LocationStats {
        private String name;
        private Integer capacity;
        private Integer totalEntries;
        private Double timePerEntry;
        private Double avgContent;
        private Integer maxContent;
        private Integer currentContent;
        private Double utilization;

        public LocationStats(String name, Integer capacity, Integer totalEntries,
                           Double timePerEntry, Double avgContent, Integer maxContent,
                           Integer currentContent, Double utilization) {
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