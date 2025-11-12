package com.simulation.gui;

import com.simulation.statistics.Statistics;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Diálogo que muestra los resultados finales de la simulación DIGEMIC
 * según los incisos requeridos (a-e)
 */
public class ResultsDialog {
    private final Statistics stats;
    private final double currentTime;

    public ResultsDialog(Statistics stats, double currentTime) {
        this.stats = stats;
        this.currentTime = currentTime;
    }

    public void show() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Resultados de la Simulación DIGEMIC");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.setAlignment(Pos.TOP_CENTER);

        // Título
        Label titleLabel = new Label("RESULTADOS FINALES - SISTEMA DIGEMIC");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Subtítulo con duración
        int totalMinutes = (int) Math.floor(currentTime);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        Label durationLabel = new Label(String.format("Duración: %02d:%02d horas (%d minutos)", 
            hours, minutes, totalMinutes));
        durationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;");

        // Grid con los resultados (a-e)
        GridPane resultsGrid = new GridPane();
        resultsGrid.setHgap(15);
        resultsGrid.setVgap(15);
        resultsGrid.setPadding(new Insets(20, 0, 20, 0));
        resultsGrid.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10px; -fx-padding: 20px;");

        // Calcular métricas específicas
        double avgWaitTimeSillas = stats.getLocation("SALA_SILLAS") != null 
            ? stats.getLocation("SALA_SILLAS").getAverageTimePerEntry(currentTime) 
            : 0.0;
        
        double avgSeated = stats.getAverageSeated(currentTime);
        double avgStanding = stats.getAverageStanding(currentTime);
        int maxWaitingArea = stats.getMaxWaitingArea();
        
        double utilizationServidor1 = stats.getLocation("SERVIDOR_1") != null 
            ? stats.getLocation("SERVIDOR_1").getUtilization(currentTime) 
            : 0.0;
        
        double utilizationServidor2 = stats.getLocation("SERVIDOR_2") != null 
            ? stats.getLocation("SERVIDOR_2").getUtilization(currentTime) 
            : 0.0;

        // a) Tiempo promedio de espera en la fila
        addResultRow(resultsGrid, 0, "a)", "Tiempo promedio de espera en la fila:", 
            String.format("%.2f minutos", avgWaitTimeSillas), "#3498db");

        // b) Número promedio de personas sentadas
        addResultRow(resultsGrid, 1, "b)", "Número promedio de personas sentadas:", 
            String.format("%.2f personas", avgSeated), "#27ae60");

        // c) Número promedio de personas de pie
        addResultRow(resultsGrid, 2, "c)", "Número promedio de personas de pie:", 
            String.format("%.2f personas", avgStanding), "#f39c12");

        // d) Número máximo de personas en la sala de espera
        addResultRow(resultsGrid, 3, "d)", "Número máximo de personas en sala de espera:", 
            String.format("%d personas", maxWaitingArea), "#e74c3c");

        // e) Utilización de los servidores
        String utilizationText = String.format("Servidor 1: %.2f%%  |  Servidor 2: %.2f%%", 
            utilizationServidor1, utilizationServidor2);
        addResultRow(resultsGrid, 4, "e)", "Utilización de los servidores:", 
            utilizationText, "#9b59b6");

        // Estadísticas adicionales
        Label additionalLabel = new Label("Estadísticas Adicionales:");
        additionalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10px 0 5px 0;");

        GridPane additionalGrid = new GridPane();
        additionalGrid.setHgap(15);
        additionalGrid.setVgap(10);
        additionalGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;");

        addSimpleRow(additionalGrid, 0, "Total de Arribos:", String.format("%d clientes", stats.getTotalArrivals()));
        addSimpleRow(additionalGrid, 1, "Total de Salidas:", String.format("%d clientes", stats.getTotalExits()));
        addSimpleRow(additionalGrid, 2, "Clientes en Sistema:", String.format("%d clientes", 
            stats.getTotalArrivals() - stats.getTotalExits()));
        addSimpleRow(additionalGrid, 3, "Throughput:", String.format("%.2f clientes/hora", stats.getThroughput()));

        // Botón de cerrar
        Button closeButton = new Button("Cerrar");
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px;");
        closeButton.setOnAction(e -> dialog.close());
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;"));
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px;"));

        // Ensamblar layout
        mainLayout.getChildren().addAll(
            titleLabel,
            durationLabel,
            resultsGrid,
            additionalLabel,
            additionalGrid,
            closeButton
        );

        Scene scene = new Scene(mainLayout, 700, 650);
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }

    private void addResultRow(GridPane grid, int row, String letter, String description, String value, String color) {
        Label letterLabel = new Label(letter);
        letterLabel.setStyle(String.format("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;", color));

        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        Label valueLabel = new Label(value);
        valueLabel.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;", color));

        grid.add(letterLabel, 0, row);
        grid.add(descLabel, 1, row);
        grid.add(valueLabel, 2, row);
    }

    private void addSimpleRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;");

        Label valueNode = new Label(value);
        valueNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }
}
