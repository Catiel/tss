package com.simulation.gui;

import com.simulation.core.ReplicationManager;
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

    // Gráfica 1: Throughput en tiempo real
    private LineChart<Number, Number> throughputChart;
    private XYChart.Series<Number, Number> throughputSeries;

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

    private static final int MAX_DATA_POINTS = 200;
    private int updateCounter = 0;

    public RealTimeChartsPanel() {
        setPadding(new Insets(10));
        setSpacing(10);

        contentSeriesMap = new HashMap<>();

        initializeCharts();
    }

    private void initializeCharts() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Tab 1: Throughput
        Tab throughputTab = new Tab("📈 Throughput en Tiempo Real");
        throughputTab.setContent(createThroughputChart());

        // Tab 2: Piezas en Sistema
        Tab systemPiecesTab = new Tab("📦 Piezas en el Sistema");
        systemPiecesTab.setContent(createSystemPiecesChart());

        // Tab 3: Utilización de Locaciones
        Tab utilizationTab = new Tab("📊 Utilización de Locaciones");
        utilizationTab.setContent(createUtilizationChart());

        // Tab 4: Contenido de Locaciones
        Tab contentTab = new Tab("📍 Contenido por Locación");
        contentTab.setContent(createLocationContentChart());

        // Tab 5: Tiempo Promedio en Sistema
        Tab avgTimeTab = new Tab("⏱ Tiempo Promedio en Sistema");
        avgTimeTab.setContent(createAvgSystemTimeChart());

        tabPane.getTabs().addAll(throughputTab, systemPiecesTab, utilizationTab, contentTab, avgTimeTab);

        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
        getChildren().add(tabPane);
    }

    // ========== GRÁFICA 1: THROUGHPUT ==========
    private VBox createThroughputChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Throughput del Sistema (piezas/hora)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Tiempo (minutos)");
        xAxis.setAutoRanging(true);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Piezas por Hora");
        yAxis.setAutoRanging(true);

        throughputChart = new LineChart<>(xAxis, yAxis);
        throughputChart.setTitle("Evolución del Throughput");
        throughputChart.setCreateSymbols(false);
        throughputChart.setAnimated(false);

        throughputSeries = new XYChart.Series<>();
        throughputSeries.setName("Throughput");
        throughputChart.getData().add(throughputSeries);

        VBox.setVgrow(throughputChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, throughputChart);

        return container;
    }

    // ========== GRÁFICA 2: PIEZAS EN SISTEMA ==========
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

        systemPiecesChart.getData().addAll(arrivalsSeriesSystem, exitsSeriesSystem, inSystemSeries);

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

        // 1. Actualizar Throughput
        updateThroughputChart(stats, currentTime);

        // 2. Actualizar Piezas en Sistema
        updateSystemPiecesChart(stats, currentTime);

        // 3. Actualizar Utilización
        updateUtilizationChart(stats, currentTime);

        // 4. Actualizar Contenido de Locaciones
        updateLocationContentChart(stats, currentTime);

        // 5. Actualizar Tiempo Promedio en Sistema
        updateAvgSystemTimeChart(stats, currentTime);
    }

    private void updateThroughputChart(Statistics stats, double currentTime) {
        double throughput = stats.getThroughput();

        throughputSeries.getData().add(new XYChart.Data<>(currentTime, throughput));

        // Limitar número de puntos
        if (throughputSeries.getData().size() > MAX_DATA_POINTS) {
            throughputSeries.getData().remove(0);
        }
    }

    private void updateSystemPiecesChart(Statistics stats, double currentTime) {
        int arrivals = stats.getTotalArrivals();
        int exits = stats.getTotalExits();
        int inSystem = arrivals - exits;

        arrivalsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, arrivals));
        exitsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, exits));
        inSystemSeries.getData().add(new XYChart.Data<>(currentTime, inSystem));

        // Limitar puntos
        if (arrivalsSeriesSystem.getData().size() > MAX_DATA_POINTS) {
            arrivalsSeriesSystem.getData().remove(0);
            exitsSeriesSystem.getData().remove(0);
            inSystemSeries.getData().remove(0);
        }
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

                    // Limitar puntos
                    if (series.getData().size() > MAX_DATA_POINTS) {
                        series.getData().remove(0);
                    }
                }
            }
        }
    }

    private void updateAvgSystemTimeChart(Statistics stats, double currentTime) {
        if (stats.getTotalExits() > 0) {
            double avgTime = stats.getAverageSystemTime();
            avgTimeSeries.getData().add(new XYChart.Data<>(currentTime, avgTime));

            // Limitar puntos
            if (avgTimeSeries.getData().size() > MAX_DATA_POINTS) {
                avgTimeSeries.getData().remove(0);
            }
        }
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

    /**
     * Muestra resultados de réplicas
     */
    public void showReplicationResults(ReplicationManager.AggregatedStatistics aggStats) {
        // Limpiar gráficas
        reset();

        // Mostrar promedios en las gráficas
        Label summaryLabel = new Label(
            String.format("Resultados de 3 Réplicas - Throughput Promedio: %.2f piezas/hora (IC 95%%: [%.2f, %.2f])",
                aggStats.getAvgThroughput(),
                aggStats.getCiThroughput()[0],
                aggStats.getCiThroughput()[1])
        );
        summaryLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2196F3;");

        if (!getChildren().contains(summaryLabel)) {
            getChildren().add(0, summaryLabel);
        }

        // Actualizar gráfica de utilización con promedios
        utilizationChart.getData().clear();
        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Utilización Promedio");

        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (String locName : locations) {
            double avgUtil = 0;
            int count = 0;
            for (Statistics stat : aggStats.getAllStatistics()) {
                Location loc = stat.getLocation(locName);
                if (loc != null) {
                    avgUtil += loc.getUtilization(stat.getSimulationDuration());
                    count++;
                }
            }
            if (count > 0) {
                avgUtil /= count;
                avgSeries.getData().add(new XYChart.Data<>(locName, avgUtil));
            }
        }

        utilizationChart.getData().add(avgSeries);
        applyBarColors();
    }

    /**
     * Reinicia todas las gráficas
     */
    public void reset() {
        throughputSeries.getData().clear();
        arrivalsSeriesSystem.getData().clear();
        exitsSeriesSystem.getData().clear();
        inSystemSeries.getData().clear();
        utilizationChart.getData().clear();
        avgTimeSeries.getData().clear();

        for (XYChart.Series<Number, Number> series : contentSeriesMap.values()) {
            series.getData().clear();
        }

        updateCounter = 0;

        // Remover etiquetas de resumen si existen
        getChildren().removeIf(node -> node instanceof Label &&
            ((Label)node).getText().contains("Réplicas"));
    }
}