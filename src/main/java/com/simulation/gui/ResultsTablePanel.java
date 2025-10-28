package com.simulation.gui;

import com.simulation.resources.Location;
import com.simulation.statistics.Statistics;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.util.Map;

/**
 * Panel de resultados con tablas de estadísticas
 */
public class ResultsTablePanel extends BorderPane {

    private TableView<LocationRow> locationTable;
    private TableView<EntityRow> entityTable;
    private TabPane tabPane;

    public ResultsTablePanel() {
        initializeUI();
    }

    private void initializeUI() {
        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        // Pestaña de Locaciones
        Tab locationTab = new Tab("📍 Estadísticas de Locaciones");
        locationTab.setContent(createLocationTable());

        // Pestaña de Entidades
        Tab entityTab = new Tab("📦 Estadísticas de Entidades");
        entityTab.setContent(createEntityTable());

        tabPane.getTabs().addAll(locationTab, entityTab);

        setCenter(tabPane);
        setPadding(new Insets(10));
    }

    private VBox createLocationTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Resumen de Locaciones");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        locationTable = new TableView<>();

        // Columnas
        TableColumn<LocationRow, String> nameCol = new TableColumn<>("Locación");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(150);

        TableColumn<LocationRow, Integer> capacityCol = new TableColumn<>("Capacidad");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        capacityCol.setPrefWidth(80);
        capacityCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationRow, Integer> entriesCol = new TableColumn<>("Total Entradas");
        entriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries"));
        entriesCol.setPrefWidth(120);
        entriesCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationRow, Double> timePerEntryCol = new TableColumn<>("Tiempo/Entrada (min)");
        timePerEntryCol.setCellValueFactory(new PropertyValueFactory<>("timePerEntry"));
        timePerEntryCol.setPrefWidth(150);
        timePerEntryCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        timePerEntryCol.setCellFactory(col -> new TableCell<LocationRow, Double>() {
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

        TableColumn<LocationRow, Double> avgContentCol = new TableColumn<>("Contenido Promedio");
        avgContentCol.setCellValueFactory(new PropertyValueFactory<>("avgContent"));
        avgContentCol.setPrefWidth(140);
        avgContentCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        avgContentCol.setCellFactory(col -> new TableCell<LocationRow, Double>() {
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

        TableColumn<LocationRow, Integer> maxContentCol = new TableColumn<>("Contenido Máximo");
        maxContentCol.setCellValueFactory(new PropertyValueFactory<>("maxContent"));
        maxContentCol.setPrefWidth(130);
        maxContentCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationRow, Integer> currentContentCol = new TableColumn<>("Contenido Actual");
        currentContentCol.setCellValueFactory(new PropertyValueFactory<>("currentContent"));
        currentContentCol.setPrefWidth(130);
        currentContentCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<LocationRow, Double> utilizationCol = new TableColumn<>("% Utilización");
        utilizationCol.setCellValueFactory(new PropertyValueFactory<>("utilization"));
        utilizationCol.setPrefWidth(120);
        utilizationCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        utilizationCol.setCellFactory(col -> new TableCell<LocationRow, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.format("%.2f%%", item));

                    // Colorear según utilización
                    if (item < 50) {
                        setStyle("-fx-background-color: #C8E6C9;"); // Verde claro
                    } else if (item < 80) {
                        setStyle("-fx-background-color: #FFF9C4;"); // Amarillo claro
                    } else {
                        setStyle("-fx-background-color: #FFCDD2;"); // Rojo claro
                    }
                }
            }
        });

        locationTable.getColumns().addAll(
            nameCol, capacityCol, entriesCol, timePerEntryCol,
            avgContentCol, maxContentCol, currentContentCol, utilizationCol
        );

        VBox.setVgrow(locationTable, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, locationTable);

        return container;
    }

    private VBox createEntityTable() {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));

        Label title = new Label("Resumen de Entidades (Piezas)");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        entityTable = new TableView<>();

        // Columnas
        TableColumn<EntityRow, String> metricCol = new TableColumn<>("Métrica");
        metricCol.setCellValueFactory(new PropertyValueFactory<>("metric"));
        metricCol.setPrefWidth(250);

        TableColumn<EntityRow, String> valueCol = new TableColumn<>("Valor");
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueCol.setPrefWidth(200);
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;");

        TableColumn<EntityRow, String> unitCol = new TableColumn<>("Unidad");
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit"));
        unitCol.setPrefWidth(150);
        unitCol.setStyle("-fx-alignment: CENTER;");

        entityTable.getColumns().addAll(metricCol, valueCol, unitCol);

        VBox.setVgrow(entityTable, javafx.scene.layout.Priority.ALWAYS);
        container.getChildren().addAll(title, entityTable);

        return container;
    }

    /**
     * Actualiza las tablas con estadísticas de la simulación
     */
    public void updateStatistics(Statistics stats, double currentTime) {
        updateLocationTable(stats, currentTime);
        updateEntityTable(stats);
    }

    private void updateLocationTable(Statistics stats, double currentTime) {
        ObservableList<LocationRow> data = FXCollections.observableArrayList();

        Map<String, Location> locations = stats.getLocations();

        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            Location loc = entry.getValue();

            data.add(new LocationRow(
                loc.getName(),
                loc.getCapacity(),
                loc.getTotalEntries(),
                loc.getAverageTimePerEntry(currentTime),
                loc.getAverageContent(currentTime),
                loc.getCapacity(),
                loc.getCurrentContent(),
                loc.getUtilization(currentTime)
            ));
        }

        locationTable.setItems(data);
    }

    private void updateEntityTable(Statistics stats) {
        ObservableList<EntityRow> data = FXCollections.observableArrayList();

        data.add(new EntityRow("Total de Arribos",
            String.valueOf(stats.getTotalArrivals()), "piezas"));

        data.add(new EntityRow("Total de Salidas (Completadas)",
            String.valueOf(stats.getTotalExits()), "piezas"));

        data.add(new EntityRow("En Sistema Actualmente",
            String.valueOf(stats.getTotalArrivals() - stats.getTotalExits()), "piezas"));

        data.add(new EntityRow("Throughput",
            String.format("%.2f", stats.getThroughput()), "piezas/hora"));

        data.add(new EntityRow("", "", "")); // Separador

        data.add(new EntityRow("Tiempo en Sistema - Promedio",
            String.format("%.2f", stats.getAverageSystemTime()), "minutos"));

        data.add(new EntityRow("Tiempo en Sistema - Desviación Estándar",
            String.format("%.2f", stats.getStdDevSystemTime()), "minutos"));

        data.add(new EntityRow("Tiempo en Sistema - Mínimo",
            String.format("%.2f", stats.getMinSystemTime()), "minutos"));

        data.add(new EntityRow("Tiempo en Sistema - Máximo",
            String.format("%.2f", stats.getMaxSystemTime()), "minutos"));

        entityTable.setItems(data);
    }

    /**
     * Limpia las tablas
     */
    public void clear() {
        locationTable.getItems().clear();
        entityTable.getItems().clear();
    }

    // Clase interna para filas de locaciones
    public static class LocationRow {
        private final StringProperty name;
        private final IntegerProperty capacity;
        private final IntegerProperty totalEntries;
        private final DoubleProperty timePerEntry;
        private final DoubleProperty avgContent;
        private final IntegerProperty maxContent;
        private final IntegerProperty currentContent;
        private final DoubleProperty utilization;

        public LocationRow(String name, int capacity, int totalEntries,
                          double timePerEntry, double avgContent, int maxContent,
                          int currentContent, double utilization) {
            this.name = new SimpleStringProperty(name);
            this.capacity = new SimpleIntegerProperty(capacity == Integer.MAX_VALUE ? -1 : capacity);
            this.totalEntries = new SimpleIntegerProperty(totalEntries);
            this.timePerEntry = new SimpleDoubleProperty(timePerEntry);
            this.avgContent = new SimpleDoubleProperty(avgContent);
            this.maxContent = new SimpleIntegerProperty(maxContent == Integer.MAX_VALUE ? -1 : maxContent);
            this.currentContent = new SimpleIntegerProperty(currentContent);
            this.utilization = new SimpleDoubleProperty(utilization);
        }

        // Getters para JavaFX Properties
        public String getName() { return name.get(); }
        public StringProperty nameProperty() { return name; }

        public int getCapacity() { return capacity.get(); }
        public IntegerProperty capacityProperty() { return capacity; }

        public int getTotalEntries() { return totalEntries.get(); }
        public IntegerProperty totalEntriesProperty() { return totalEntries; }

        public double getTimePerEntry() { return timePerEntry.get(); }
        public DoubleProperty timePerEntryProperty() { return timePerEntry; }

        public double getAvgContent() { return avgContent.get(); }
        public DoubleProperty avgContentProperty() { return avgContent; }

        public int getMaxContent() { return maxContent.get(); }
        public IntegerProperty maxContentProperty() { return maxContent; }

        public int getCurrentContent() { return currentContent.get(); }
        public IntegerProperty currentContentProperty() { return currentContent; }

        public double getUtilization() { return utilization.get(); }
        public DoubleProperty utilizationProperty() { return utilization; }
    }

    // Clase interna para filas de entidades
    public static class EntityRow {
        private final StringProperty metric;
        private final StringProperty value;
        private final StringProperty unit;

        public EntityRow(String metric, String value, String unit) {
            this.metric = new SimpleStringProperty(metric);
            this.value = new SimpleStringProperty(value);
            this.unit = new SimpleStringProperty(unit);
        }

        public String getMetric() { return metric.get(); }
        public StringProperty metricProperty() { return metric; }

        public String getValue() { return value.get(); }
        public StringProperty valueProperty() { return value; }

        public String getUnit() { return unit.get(); }
        public StringProperty unitProperty() { return unit; }
    }
}
