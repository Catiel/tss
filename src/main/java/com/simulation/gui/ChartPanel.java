package com.simulation.gui;

import com.simulation.core.ReplicationManager;
import com.simulation.statistics.Statistics;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.VBox;

import java.util.List;

public class ChartPanel extends VBox {

    private BarChart<String, Number> utilizationChart;
    private BarChart<String, Number> throughputChart;
    private LineChart<String, Number> systemTimeChart;

    public ChartPanel() {
        setPadding(new Insets(10));
        setSpacing(10);
        initializeCharts();
    }

    private void initializeCharts() {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Gráfica de Utilización
        Tab utilizationTab = new Tab("📊 Utilización por Locación");
        utilizationTab.setContent(createUtilizationChart());

        // Gráfica de Throughput
        Tab throughputTab = new Tab("📈 Throughput por Réplica");
        throughputTab.setContent(createThroughputChart());

        // Gráfica de Tiempo en Sistema
        Tab systemTimeTab = new Tab("⏱ Tiempo en Sistema");
        systemTimeTab.setContent(createSystemTimeChart());

        tabPane.getTabs().addAll(utilizationTab, throughputTab, systemTimeTab);
        getChildren().add(tabPane);
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS);
    }

    private VBox createUtilizationChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Locación");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("% Utilización");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(100);

        utilizationChart = new BarChart<>(xAxis, yAxis);
        utilizationChart.setTitle("Utilización de Locaciones - Promedio de 3 Réplicas");
        utilizationChart.setLegendVisible(false);

        VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().add(utilizationChart);

        return container;
    }

    private VBox createThroughputChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Réplica");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Piezas por Hora");

        throughputChart = new BarChart<>(xAxis, yAxis);
        throughputChart.setTitle("Throughput del Sistema por Réplica");
        throughputChart.setLegendVisible(false);

        VBox.setVgrow(throughputChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().add(throughputChart);

        return container;
    }

    private VBox createSystemTimeChart() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Réplica");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Minutos");

        systemTimeChart = new LineChart<>(xAxis, yAxis);
        systemTimeChart.setTitle("Tiempo Promedio en Sistema por Réplica");
        systemTimeChart.setCreateSymbols(true);
        systemTimeChart.setLegendVisible(true);

        VBox.setVgrow(systemTimeChart, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().add(systemTimeChart);

        return container;
    }

    public void updateCharts(ReplicationManager.AggregatedStatistics aggStats) {
        if (aggStats == null) return;

        List<Statistics> allStats = aggStats.getAllStatistics();
        if (allStats.isEmpty()) return;

        updateUtilizationChart(allStats);
        updateThroughputChart(allStats);
        updateSystemTimeChart(allStats);
    }

    private void updateUtilizationChart(List<Statistics> allStats) {
        utilizationChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("% Utilización Promedio");

        // Calcular promedios por locación
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (String locName : locations) {
            double avgUtil = 0;
            int count = 0;
            for (Statistics stats : allStats) {
                if (stats.getLocation(locName) != null) {
                    avgUtil += stats.getLocation(locName).getUtilization(stats.getSimulationDuration());
                    count++;
                }
            }
            if (count > 0) {
                avgUtil /= count;
                series.getData().add(new XYChart.Data<>(locName, avgUtil));
            }
        }

        utilizationChart.getData().add(series);
    }

    private void updateThroughputChart(List<Statistics> allStats) {
        throughputChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Throughput");

        for (int i = 0; i < allStats.size(); i++) {
            Statistics stats = allStats.get(i);
            series.getData().add(new XYChart.Data<>("Réplica " + (i + 1), stats.getThroughput()));
        }

        throughputChart.getData().add(series);
    }

    private void updateSystemTimeChart(List<Statistics> allStats) {
        systemTimeChart.getData().clear();

        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>();
        avgSeries.setName("Tiempo Promedio");

        for (int i = 0; i < allStats.size(); i++) {
            Statistics stats = allStats.get(i);
            avgSeries.getData().add(new XYChart.Data<>("Réplica " + (i + 1), stats.getAverageSystemTime()));
        }

        systemTimeChart.getData().add(avgSeries);
    }

    public void clear() {
        utilizationChart.getData().clear();
        throughputChart.getData().clear();
        systemTimeChart.getData().clear();
    }
}
