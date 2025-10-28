package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import com.simulation.core.SimulationEngine;
import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
    private AnimationTimer animationTimer;
    private Thread simulationThread;

    public void initialize() {
        parameters = new SimulationParameters();
        engine = new SimulationEngine(parameters);

        setupAnimationPanel();
        setupControls();
        setupResultsTables();
        setupAnimationLoop();

        updateStatus("Listo para iniciar");
    }

    private void setupAnimationPanel() {
        animationPanel = new AnimationPanel(engine);
        animationTab.setContent(animationPanel);
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
                }
            }
        });
    }

    private void setupAnimationLoop() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (engine.isRunning()) {
                    updateDisplay();
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

        timeLabel.setText("Tiempo: 0.00 min");
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
            animationPanel = new AnimationPanel(engine);
            animationTab.setContent(animationPanel);
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

        mainTabPane.getSelectionModel().select(resultsTab);
    }

    private void updateDisplay() {
        double currentTime = engine.getCurrentTime();

        Platform.runLater(() -> {
            timeLabel.setText(String.format("Tiempo: %.2f min", currentTime));
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
