package com.simulation.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

import java.util.HashMap;
import java.util.Map;

public class StatisticsPanel {

    private final VBox panel;
    private final Map<String, Label> entityLabels;
    private final Map<String, Label> locationLabels;
    private Label timeLabel;

    public StatisticsPanel() {
        this.panel = new VBox(10);
        this.entityLabels = new HashMap<>();
        this.locationLabels = new HashMap<>();

        setupPanel();
    }

    private void setupPanel() {
        panel.setPadding(new Insets(10));
        panel.setPrefWidth(350);
        panel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #333; -fx-border-width: 2;");

        // Título
        Label title = new Label("Estadísticas de Simulación");
        title.setFont(new Font("Arial", 18));
        title.setStyle("-fx-font-weight: bold;");

        // Tiempo de simulación
        timeLabel = new Label("Tiempo: 0.00 min");
        timeLabel.setFont(new Font("Arial", 14));

        // Panel de entidades
        VBox entitiesBox = new VBox(5);
        entitiesBox.setPadding(new Insets(5));

        TitledPane entitiesPane = new TitledPane("Entidades", entitiesBox);
        entitiesPane.setExpanded(true);

        String[] entities = {
                "GRANOS_DE_CEBADA", "LUPULO", "LEVADURA", "MOSTO",
                "CERVEZA", "BOTELLA_CON_CERVEZA", "CAJA_VACIA", "CAJA_CON_CERVEZAS"
        };

        for (String entity : entities) {
            Label label = new Label(entity + ": 0");
            label.setFont(new Font("Courier New", 11));
            entityLabels.put(entity, label);
            entitiesBox.getChildren().add(label);
        }

        // Panel de locaciones (con scroll)
        VBox locationsBox = new VBox(5);
        locationsBox.setPadding(new Insets(5));

        String[] locations = {
                "SILO_GRANDE", "MALTEADO", "SECADO", "MOLIENDA", "MACERADO",
                "FILTRADO", "COCCION", "SILO_LUPULO", "SILO_LEVADURA",
                "ENFRIAMIENTO", "FERMENTACION", "MADURACION", "INSPECCION",
                "EMBOTELLADO", "ETIQUETADO", "EMPACADO", "ALMACEN_CAJAS",
                "ALMACENAJE", "MERCADO"
        };

        for (String location : locations) {
            Label label = new Label(location + ": 0/0");
            label.setFont(new Font("Courier New", 10));
            locationLabels.put(location, label);
            locationsBox.getChildren().add(label);
        }

        ScrollPane scrollPane = new ScrollPane(locationsBox);
        scrollPane.setPrefHeight(300);
        scrollPane.setFitToWidth(true);

        TitledPane locationsPane = new TitledPane("Locaciones (Actual/Cap)", scrollPane);
        locationsPane.setExpanded(true);

        panel.getChildren().addAll(title, timeLabel, entitiesPane, locationsPane);
    }

    public void updateEntityCount(String entityName, int count) {
        Label label = entityLabels.get(entityName);
        if (label != null) {
            label.setText(entityName + ": " + count);
        }
    }

    public void updateLocationOccupancy(String locationName, int current, int capacity) {
        Label label = locationLabels.get(locationName);
        if (label != null) {
            label.setText(locationName + ": " + current + "/" + capacity);
        }
    }

    public void updateTime(double time) {
        timeLabel.setText(String.format("Tiempo: %.2f min", time));
    }

    public VBox getPanel() {
        return panel;
    }
}

