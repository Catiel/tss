package com.simulation.gui;

import com.simulation.core.ReplicationManager;
import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.geometry.Insets;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.List;

/**
 * Panel con gráfica de barras de utilización por locación
 * Similar a la gráfica de ProModel
 */
public class UtilizationChartPanel extends VBox {

    private BarChart<String, Number> utilizationChart;
    private Label titleLabel;
    private Label subtitleLabel;

    public UtilizationChartPanel() {
        setPadding(new Insets(15));
        setSpacing(10);
        initializeChart();
    }

    private void initializeChart() {
        // Título principal
        titleLabel = new Label("Locación Resumen - % Utilización (Prom. Reps)");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;");

        // Subtítulo
        subtitleLabel = new Label("Baseline");
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;");

        // Configurar ejes
        CategoryAxis xAxis = new CategoryAxis();
        xAxis.setLabel("Locación");
        xAxis.setTickLabelRotation(0);

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("% Utilización");
        yAxis.setAutoRanging(false);
        yAxis.setLowerBound(0);
        yAxis.setUpperBound(105);
        yAxis.setTickUnit(10);

        // Crear gráfica
        utilizationChart = new BarChart<>(xAxis, yAxis);
        utilizationChart.setLegendVisible(true);
        utilizationChart.setAnimated(true);
        utilizationChart.setTitle("");

        // Estilo
        utilizationChart.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #cccccc;" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 5;"
        );

        VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS);

        getChildren().addAll(titleLabel, subtitleLabel, utilizationChart);
    }

    /**
     * Actualiza la gráfica con estadísticas agregadas de réplicas
     */
    public void updateChart(ReplicationManager.AggregatedStatistics aggStats) {
        if (aggStats == null) return;

        List<Statistics> allStats = aggStats.getAllStatistics();
        if (allStats.isEmpty()) return;

        utilizationChart.getData().clear();

        // Serie de datos
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Baseline");

        // Locaciones a mostrar (mismo orden que ProModel)
        String[] locations = {
            "RECEPCION",
            "LAVADORA",
            "ALMACEN_PINTURA",
            "PINTURA",
            "ALMACEN_HORNO",
            "HORNO",
            "INSPECCION.1",
            "INSPECCION.2",
            "INSPECCION"
        };

        // Calcular utilizaciones promedio
        for (String locName : locations) {
            double avgUtil = calculateAverageUtilization(allStats, locName);

            // Agregar datos
            XYChart.Data<String, Number> data = new XYChart.Data<>(locName, avgUtil);
            series.getData().add(data);
        }

        utilizationChart.getData().add(series);

        // Aplicar colores a las barras después de que se rendericen
        applyBarColors();
    }

    /**
     * Actualiza la gráfica con estadísticas de una sola simulación
     */
    public void updateChart(Statistics stats, double currentTime) {
        if (stats == null) return;

        utilizationChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Baseline");

        String[] locations = {
            "RECEPCION",
            "LAVADORA",
            "ALMACEN_PINTURA",
            "PINTURA",
            "ALMACEN_HORNO",
            "HORNO",
            "INSPECCION.1",
            "INSPECCION.2",
            "INSPECCION"
        };

        for (String locName : locations) {
            double util = getLocationUtilization(stats, locName, currentTime);
            series.getData().add(new XYChart.Data<>(locName, util));
        }

        utilizationChart.getData().add(series);
        applyBarColors();
    }

    /**
     * Calcula la utilización promedio de una locación a través de réplicas
     */
    private double calculateAverageUtilization(List<Statistics> allStats, String locName) {
        double sum = 0;
        int count = 0;

        for (Statistics stats : allStats) {
            double util = getLocationUtilization(stats, locName, stats.getSimulationDuration());
            if (util >= 0) {
                sum += util;
                count++;
            }
        }

        return count > 0 ? sum / count : 0;
    }

    /**
     * Obtiene la utilización de una locación específica
     */
    private double getLocationUtilization(Statistics stats, String locName, double currentTime) {
        Location loc = null;

        // Manejo especial para INSPECCION
        if (locName.equals("INSPECCION.1") || locName.equals("INSPECCION.2")) {
            loc = stats.getLocation("INSPECCION");
            if (loc != null) {
                // Para las mesas individuales, dividir la utilización total entre 2
                return loc.getUtilization(currentTime) / 2.0;
            }
        } else if (locName.equals("INSPECCION")) {
            loc = stats.getLocation("INSPECCION");
            if (loc != null) {
                // Utilización total de ambas mesas
                return loc.getUtilization(currentTime);
            }
        } else {
            loc = stats.getLocation(locName);
        }

        return loc != null ? loc.getUtilization(currentTime) : 0;
    }

    /**
     * Aplica colores a las barras según su valor
     */
    private void applyBarColors() {
        // Esperar a que se renderice la gráfica
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

    /**
     * Retorna el color según el nivel de utilización
     */
    private String getColorForUtilization(double utilization) {
        if (utilization < 50) {
            return "#4CAF50"; // Verde
        } else if (utilization < 80) {
            return "#FFC107"; // Amarillo
        } else {
            return "#2196F3"; // Azul (similar a ProModel)
        }
    }

    /**
     * Limpia la gráfica
     */
    public void clear() {
        utilizationChart.getData().clear();
    }

    /**
     * Actualiza el subtítulo
     */
    public void setSubtitle(String subtitle) {
        subtitleLabel.setText(subtitle);
    }
}