package com.simulation.gui;

import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Side;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.Map;

/**
 * Panel con múltiples gráficas que se actualizan en tiempo real
 */
public class RealTimeChartsPanel extends VBox {

    private TabPane tabPane;

    // Gráfica 2: Piezas en el sistema
    private LineChart<Number, Number> systemPiecesChart;
    private XYChart.Series<Number, Number> arrivalsSeriesSystem;
    private XYChart.Series<Number, Number> exitsSeriesSystem;
    private XYChart.Series<Number, Number> inSystemSeries;

    // Gráfica 3: Utilización de locaciones
    private BarChart<String, Number> utilizationChart;

    // Gráfica 4: Contenido de locaciones en tiempo real
    private LineChart<Number, Number> locationContentChart;
    private Map<String, XYChart.Series<Number, Number>> contentSeriesMap;

    // Gráfica 5: Tiempo promedio en sistema
    private LineChart<Number, Number> avgSystemTimeChart;
    private XYChart.Series<Number, Number> avgTimeSeries;

    private static final int MAX_DATA_POINTS = 5000;
    private int updateCounter = 0;

    public RealTimeChartsPanel() {
        setPadding(new Insets(10));
        setSpacing(10);

        contentSeriesMap = new HashMap<>();

        initializeCharts();
    }

    public void initializeState(Statistics stats) {
        reset();
        double initialTime = Math.max(0, stats.getSimulationDuration());
        updateSystemPiecesChart(stats, initialTime);
        updateUtilizationChart(stats, initialTime);
        updateLocationContentChart(stats, initialTime);
        updateAvgSystemTimeChart(stats, initialTime);
    }

    private void initializeCharts() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
    tabPane.setStyle("-fx-font-size: 14px;");

        // Tab 1: Throughput
    // Tab 1: Piezas en Sistema
    Tab systemPiecesTab = new Tab("📦 Piezas en el Sistema");
        systemPiecesTab.setContent(createSystemPiecesChart());

    // Tab 2: Utilización de Locaciones
    Tab utilizationTab = new Tab("📊 Utilización de Locaciones");
        utilizationTab.setContent(createUtilizationChart());

    // Tab 3: Contenido de Locaciones
    Tab contentTab = new Tab("📍 Contenido por Locación");
        contentTab.setContent(createLocationContentChart());

    // Tab 4: Tiempo Promedio en Sistema
    Tab avgTimeTab = new Tab("⏱ Tiempo Promedio en Sistema");
        avgTimeTab.setContent(createAvgSystemTimeChart());

    tabPane.getTabs().addAll(systemPiecesTab, utilizationTab, contentTab, avgTimeTab);

        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(tabPane);
    }

    // ========== GRÁFICA 1: PIEZAS EN SISTEMA ==========
    private VBox createSystemPiecesChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Arribos, Salidas y Piezas en Sistema");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (minutos)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad de Piezas");
        yAxis.setAutoRanging(true);

        systemPiecesChart = new LineChart<>(xAxis, yAxis);
        systemPiecesChart.setTitle("Estado del Sistema");
        systemPiecesChart.setCreateSymbols(false);
        systemPiecesChart.setAnimated(false);

        arrivalsSeriesSystem = new XYChart.Series<>();
        arrivalsSeriesSystem.setName("Arribos Acumulados");

        exitsSeriesSystem = new XYChart.Series<>();
        exitsSeriesSystem.setName("Salidas Acumuladas");

        inSystemSeries = new XYChart.Series<>();
        inSystemSeries.setName("En Sistema");

    systemPiecesChart.getData().add(arrivalsSeriesSystem);
    systemPiecesChart.getData().add(exitsSeriesSystem);
    systemPiecesChart.getData().add(inSystemSeries);

    applyLargeFont(systemPiecesChart);
    VBox.setVgrow(systemPiecesChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, systemPiecesChart);

        return container;
    }

    // ========== GRÁFICA 3: UTILIZACIÓN ==========
    private VBox createUtilizationChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Utilización Actual de Locaciones (%)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Locación");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("% Utilización");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(105);

        utilizationChart = new BarChart<>(xAxis, yAxis);
        utilizationChart.setTitle("Utilización en Tiempo Real");
        utilizationChart.setLegendVisible(false);
        utilizationChart.setAnimated(false);

    applyLargeFont(utilizationChart);
    VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, utilizationChart);

        return container;
    }

    // ========== GRÁFICA 4: CONTENIDO DE LOCACIONES ==========
    private VBox createLocationContentChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Contenido de Locaciones en Tiempo Real");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (minutos)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Cantidad de Piezas");
        yAxis.setAutoRanging(true);

        locationContentChart = new LineChart<>(xAxis, yAxis);
        locationContentChart.setTitle("Piezas por Locación");
        locationContentChart.setCreateSymbols(false);
        locationContentChart.setAnimated(false);
        locationContentChart.setLegendSide(Side.RIGHT);

        // Crear series para cada locación
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (String loc : locations) {
            XYChart.Series<Number, Number> series = new XYChart.Series<>();
            series.setName(loc);
            contentSeriesMap.put(loc, series);
            locationContentChart.getData().add(series);
        }

    applyLargeFont(locationContentChart);
    VBox.setVgrow(locationContentChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, locationContentChart);

        return container;
    }

    // ========== GRÁFICA 5: TIEMPO PROMEDIO EN SISTEMA ==========
    private VBox createAvgSystemTimeChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Tiempo Promedio en Sistema (minutos)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (minutos)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Tiempo Promedio (min)");
        yAxis.setAutoRanging(true);

        avgSystemTimeChart = new LineChart<>(xAxis, yAxis);
        avgSystemTimeChart.setTitle("Evolución del Tiempo Promedio");
        avgSystemTimeChart.setCreateSymbols(false);
        avgSystemTimeChart.setAnimated(false);

        avgTimeSeries = new XYChart.Series<>();
        avgTimeSeries.setName("Tiempo Promedio");
        avgSystemTimeChart.getData().add(avgTimeSeries);

    applyLargeFont(avgSystemTimeChart);
    VBox.setVgrow(avgSystemTimeChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, avgSystemTimeChart);

        return container;
    }

    /**
     * Actualiza todas las gráficas con nuevos datos
     */
    public void updateCharts(Statistics stats, double currentTime) {
        if (currentTime <= 0) return;

        // Actualizar solo cada N llamadas para mejor rendimiento
        updateCounter++;
        if (updateCounter % 5 != 0 && currentTime < stats.getSimulationDuration()) {
            return; // Saltar algunas actualizaciones durante la simulación
        }

        // 1. Actualizar Piezas en Sistema
        updateSystemPiecesChart(stats, currentTime);

        // 2. Actualizar Utilización
        updateUtilizationChart(stats, currentTime);

        // 3. Actualizar Contenido de Locaciones
        updateLocationContentChart(stats, currentTime);

        // 4. Actualizar Tiempo Promedio en Sistema
        updateAvgSystemTimeChart(stats, currentTime);
    }

    private void updateSystemPiecesChart(Statistics stats, double currentTime) {
        int arrivals = stats.getTotalArrivals();
        int exits = stats.getTotalExits();
        int inSystem = arrivals - exits;

        arrivalsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, arrivals));
        exitsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, exits));
        inSystemSeries.getData().add(new XYChart.Data<>(currentTime, inSystem));

        enforceSeriesLimit(arrivalsSeriesSystem);
        enforceSeriesLimit(exitsSeriesSystem);
        enforceSeriesLimit(inSystemSeries);
    }

    private void updateUtilizationChart(Statistics stats, double currentTime) {
        utilizationChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Utilización");

        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (String locName : locations) {
            Location loc = stats.getLocation(locName);
            if (loc != null) {
                double util = loc.getUtilization(currentTime);
                XYChart.Data<String, Number> data = new XYChart.Data<>(locName, util);
                series.getData().add(data);
            }
        }

        utilizationChart.getData().add(series);

        // Aplicar colores a las barras
        applyBarColors();
    }

    private void updateLocationContentChart(Statistics stats, double currentTime) {
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (String locName : locations) {
            Location loc = stats.getLocation(locName);
            if (loc != null) {
                XYChart.Series<Number, Number> series = contentSeriesMap.get(locName);
                if (series != null) {
                    int content = loc.getCurrentContent();
                    series.getData().add(new XYChart.Data<>(currentTime, content));
                    enforceSeriesLimit(series);
                }
            }
        }
    }

    private void updateAvgSystemTimeChart(Statistics stats, double currentTime) {
        if (stats.getTotalExits() > 0) {
            double avgTime = stats.getAverageSystemTime();
            avgTimeSeries.getData().add(new XYChart.Data<>(currentTime, avgTime));
            enforceSeriesLimit(avgTimeSeries);
        }
    }

    private void enforceSeriesLimit(XYChart.Series<Number, Number> series) {
        ObservableList<XYChart.Data<Number, Number>> data = series.getData();
        if (data.size() <= MAX_DATA_POINTS) {
            return;
        }

        ObservableList<XYChart.Data<Number, Number>> downsampled = FXCollections.observableArrayList();
        for (int i = 0; i < data.size(); i += 2) {
            downsampled.add(data.get(i));
        }

        // Garantizar que el último punto siempre se conserve.
        XYChart.Data<Number, Number> lastPoint = data.get(data.size() - 1);
        if (downsampled.isEmpty() || downsampled.get(downsampled.size() - 1) != lastPoint) {
            downsampled.add(lastPoint);
        }

        data.setAll(downsampled);
    }

    private void applyBarColors() {
        utilizationChart.applyCss();
        utilizationChart.layout();

        for (XYChart.Series<String, Number> series : utilizationChart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                if (data.getNode() != null) {
                    double value = data.getYValue().doubleValue();
                    String color = getColorForUtilization(value);
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";");
                }
            }
        }
    }

    private String getColorForUtilization(double utilization) {
        if (utilization < 50) {
            return "#4CAF50"; // Verde
        } else if (utilization < 80) {
            return "#FFC107"; // Amarillo
        } else {
            return "#2196F3"; // Azul
        }
    }

    private void applyLargeFont(Chart chart) {
        chart.setStyle("-fx-font-size: 14px;");
    }

    /**
     * Reinicia todas las gráficas
     */
    public void reset() {
        arrivalsSeriesSystem.getData().clear();
        exitsSeriesSystem.getData().clear();
        inSystemSeries.getData().clear();
        utilizationChart.getData().clear();
        avgTimeSeries.getData().clear();

        for (XYChart.Series<Number, Number> series : contentSeriesMap.values()) {
            series.getData().clear();
        }

        updateCounter = 0;

    }
}