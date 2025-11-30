package com.simulation.gui;

import com.simulation.config.SimulationConfig;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Map;

/**
 * Ventana de diálogo para configurar todos los parámetros de la simulación
 */
public class ConfigurationDialog extends Stage {

    private final SimulationConfig config;
    private boolean accepted = false;

    // Controles de parámetros generales
    private TextField tfSimulationTime;
    private TextField tfReplicas;
    private TextField tfWarmUp;
    private TextField tfRandomSeed;

    // Controles de recursos
    private TextField tfGruaQuantity, tfGruaEmptySpeed, tfGruaLoadedSpeed;
    private TextField tfRobotQuantity, tfRobotEmptySpeed, tfRobotLoadedSpeed;

    // Controles de arribos
    private ComboBox<String> cbArrivalDistribution;
    private TextField tfArrivalMean, tfArrivalFirst, tfArrivalMax;

    // Controles de HORNO
    private TextField tfHornoBatchSize, tfHornoProcessingTime;

    public ConfigurationDialog(SimulationConfig config) {
        this.config = config;

        setTitle("⚙️ Configuración de Simulación");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(false);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e 0%, #16213e 100%);");

        // Crear título
        Label titleLabel = new Label("⚙ Configuración de Parámetros");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 24));
        titleLabel.setTextFill(Color.web("#e94560"));
        titleLabel.setPadding(new Insets(20));
        VBox titleBox = new VBox(titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        root.setTop(titleBox);

        // Crear tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #0f3460;");

        tabPane.getTabs().addAll(
                createGeneralTab(),
                createLocationsTab(),
                createResourcesTab(),
                createArrivalsTab());

        root.setCenter(tabPane);

        // Botones de acción
        HBox buttonBox = createButtonBox();
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 700, 600);
        setScene(scene);

        loadConfigValues();
    }

    private Tab createGeneralTab() {
        Tab tab = new Tab("📊 General");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #0f3460;");

        int row = 0;

        // Tiempo de simulación
        grid.add(createLabel("Tiempo de Simulación (hrs):"), 0, row);
        tfSimulationTime = createTextField("1000.05");
        grid.add(tfSimulationTime, 1, row++);

        // Número de réplicas
        grid.add(createLabel("Número de Réplicas:"), 0, row);
        tfReplicas = createTextField("3");
        grid.add(tfReplicas, 1, row++);

        // Warm-up time
        grid.add(createLabel("Tiempo Warm-up (hrs):"), 0, row);
        tfWarmUp = createTextField("0.0");
        grid.add(tfWarmUp, 1, row++);

        // Semilla aleatoria
        grid.add(createLabel("Semilla Aleatoria:"), 0, row);
        tfRandomSeed = createTextField(String.valueOf(System.currentTimeMillis()));
        Button btnRandomSeed = new Button("🎲 Aleatoria");
        btnRandomSeed.setOnAction(e -> tfRandomSeed.setText(String.valueOf(System.currentTimeMillis())));
        styleButton(btnRandomSeed);
        HBox seedBox = new HBox(10, tfRandomSeed, btnRandomSeed);
        grid.add(seedBox, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createLocationsTab() {
        Tab tab = new Tab("📍 Locaciones");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0f3460;");

        // HORNO (especial con batch)
        Label hornoLabel = createLabel("⚡ HORNO (Procesamiento por Lotes)");
        hornoLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        GridPane hornoGrid = new GridPane();
        hornoGrid.setHgap(15);
        hornoGrid.setVgap(10);

        hornoGrid.add(createLabel("Tamaño Lote (batch):"), 0, 0);
        tfHornoBatchSize = createTextField("10");
        hornoGrid.add(tfHornoBatchSize, 1, 0);

        hornoGrid.add(createLabel("Tiempo Procesamiento (min):"), 0, 1);
        tfHornoProcessingTime = createTextField("100.0");
        hornoGrid.add(tfHornoProcessingTime, 1, 1);

        // Tabla de otras locaciones
        Label otherLabel = createLabel("Tiempos de Procesamiento (minutos)");
        otherLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));

        TableView<LocationRow> table = createLocationTable();

        vbox.getChildren().addAll(hornoLabel, hornoGrid, new Separator(), otherLabel, table);

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createResourcesTab() {
        Tab tab = new Tab("🚁 Recursos");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #0f3460;");

        int row = 0;

        // GRÚA VIAJERA
        Label gruaLabel = createLabel("🏗️ GRÚA VIAJERA");
        gruaLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        grid.add(gruaLabel, 0, row++, 2, 1);

        grid.add(createLabel("Cantidad:"), 0, row);
        tfGruaQuantity = createTextField("1");
        grid.add(tfGruaQuantity, 1, row++);

        grid.add(createLabel("Velocidad Vacía (pies/min):"), 0, row);
        tfGruaEmptySpeed = createTextField("25.0");
        grid.add(tfGruaEmptySpeed, 1, row++);

        grid.add(createLabel("Velocidad Cargada (pies/min):"), 0, row);
        tfGruaLoadedSpeed = createTextField("25.0");
        grid.add(tfGruaLoadedSpeed, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        // ROBOT
        Label robotLabel = createLabel("🤖 ROBOT");
        robotLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 16));
        grid.add(robotLabel, 0, row++, 2, 1);

        grid.add(createLabel("Cantidad:"), 0, row);
        tfRobotQuantity = createTextField("1");
        grid.add(tfRobotQuantity, 1, row++);

        grid.add(createLabel("Velocidad Vacía (pies/min):"), 0, row);
        tfRobotEmptySpeed = createTextField("45.0");
        grid.add(tfRobotEmptySpeed, 1, row++);

        grid.add(createLabel("Velocidad Cargada (pies/min):"), 0, row);
        tfRobotLoadedSpeed = createTextField("45.0");
        grid.add(tfRobotLoadedSpeed, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createArrivalsTab() {
        Tab tab = new Tab("📦 Arribos");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #0f3460;");

        int row = 0;

        grid.add(createLabel("Distribución:"), 0, row);
        cbArrivalDistribution = new ComboBox<>();
        cbArrivalDistribution.getItems().addAll("EXPONENTIAL", "CONSTANT");
        cbArrivalDistribution.setValue("EXPONENTIAL");
        styleComboBox(cbArrivalDistribution);
        grid.add(cbArrivalDistribution, 1, row++);

        grid.add(createLabel("Tiempo Medio entre Arribos (min):"), 0, row);
        tfArrivalMean = createTextField("5.0");
        grid.add(tfArrivalMean, 1, row++);

        grid.add(createLabel("Primer Arribo (min):"), 0, row);
        tfArrivalFirst = createTextField("0.0");
        grid.add(tfArrivalFirst, 1, row++);

        grid.add(createLabel("Máximo de Entidades:"), 0, row);
        tfArrivalMax = createTextField("12000");
        grid.add(tfArrivalMax, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private TableView<LocationRow> createLocationTable() {
        TableView<LocationRow> table = new TableView<>();
        table.setPrefHeight(300);
        table.setStyle("-fx-background-color: #1a1a2e;");

        TableColumn<LocationRow, String> nameCol = new TableColumn<>("Locación");
        nameCol.setCellValueFactory(data -> data.getValue().name);
        nameCol.setPrefWidth(200);

        TableColumn<LocationRow, String> timeCol = new TableColumn<>("Tiempo (min)");
        timeCol.setCellValueFactory(data -> data.getValue().time);
        timeCol.setPrefWidth(150);
        timeCol.setEditable(true);
        timeCol.setCellFactory(column -> new javafx.scene.control.cell.TextFieldTableCell<>());

        table.getColumns().addAll(nameCol, timeCol);
        table.setEditable(true);

        // Agregar datos
        String[] locations = { "TORNEADO", "FRESADO", "TALADRO", "RECTIFICADO", "CARGA", "DESCARGA", "BANDA_1",
                "BANDA_2", "INSPECCION" };
        for (String loc : locations) {
            double time = config.getProcessingTimes().getOrDefault(loc, 0.0);
            table.getItems().add(new LocationRow(loc, String.valueOf(time)));
        }

        return table;
    }

    private HBox createButtonBox() {
        HBox box = new HBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #0f3460;");

        Button btnOk = new Button("✅ Aceptar");
        btnOk.setPrefWidth(120);
        btnOk.setOnAction(e -> {
            if (validateAndSave()) {
                accepted = true;
                close();
            }
        });
        styleButton(btnOk);

        Button btnCancel = new Button("❌ Cancelar");
        btnCancel.setPrefWidth(120);
        btnCancel.setOnAction(e -> close());
        styleButton(btnCancel);

        box.getChildren().addAll(btnOk, btnCancel);
        return box;
    }

    private void loadConfigValues() {
        tfSimulationTime.setText(String.valueOf(config.getSimulationTime()));
        tfReplicas.setText(String.valueOf(config.getNumberOfReplicas()));
        tfWarmUp.setText(String.valueOf(config.getWarmUpTime()));
        tfRandomSeed.setText(String.valueOf(config.getRandomSeed()));

        tfGruaQuantity.setText(String.valueOf(config.getGruaQuantity()));
        tfGruaEmptySpeed.setText(String.valueOf(config.getGruaEmptySpeed()));
        tfGruaLoadedSpeed.setText(String.valueOf(config.getGruaLoadedSpeed()));

        tfRobotQuantity.setText(String.valueOf(config.getRobotQuantity()));
        tfRobotEmptySpeed.setText(String.valueOf(config.getRobotEmptySpeed()));
        tfRobotLoadedSpeed.setText(String.valueOf(config.getRobotLoadedSpeed()));

        cbArrivalDistribution.setValue(config.getArrivalDistribution());
        tfArrivalMean.setText(String.valueOf(config.getArrivalMeanTime()));
        tfArrivalFirst.setText(String.valueOf(config.getArrivalFirstTime()));
        tfArrivalMax.setText(String.valueOf(config.getArrivalMaxEntities()));

        tfHornoBatchSize.setText(String.valueOf(config.getHornoBatchSize()));
        tfHornoProcessingTime.setText(String.valueOf(config.getHornoProcessingTime()));
    }

    private boolean validateAndSave() {
        try {
            config.setSimulationTime(Double.parseDouble(tfSimulationTime.getText()));
            config.setNumberOfReplicas(Integer.parseInt(tfReplicas.getText()));
            config.setWarmUpTime(Double.parseDouble(tfWarmUp.getText()));
            config.setRandomSeed(Long.parseLong(tfRandomSeed.getText()));

            config.setGruaQuantity(Integer.parseInt(tfGruaQuantity.getText()));
            config.setGruaEmptySpeed(Double.parseDouble(tfGruaEmptySpeed.getText()));
            config.setGruaLoadedSpeed(Double.parseDouble(tfGruaLoadedSpeed.getText()));

            config.setRobotQuantity(Integer.parseInt(tfRobotQuantity.getText()));
            config.setRobotEmptySpeed(Double.parseDouble(tfRobotEmptySpeed.getText()));
            config.setRobotLoadedSpeed(Double.parseDouble(tfRobotLoadedSpeed.getText()));

            config.setArrivalDistribution(cbArrivalDistribution.getValue());
            config.setArrivalMeanTime(Double.parseDouble(tfArrivalMean.getText()));
            config.setArrivalFirstTime(Double.parseDouble(tfArrivalFirst.getText()));
            config.setArrivalMaxEntities(Integer.parseInt(tfArrivalMax.getText()));

            config.setHornoBatchSize(Integer.parseInt(tfHornoBatchSize.getText()));
            config.setHornoProcessingTime(Double.parseDouble(tfHornoProcessingTime.getText()));

            return true;
        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Validación");
            alert.setHeaderText("Valor numérico inválido");
            alert.setContentText("Por favor verifica que todos los valores sean numéricos válidos.");
            alert.showAndWait();
            return false;
        }
    }

    public boolean wasAccepted() {
        return accepted;
    }

    // ===== MÉTODOS DE ESTILO =====

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#f1faee"));
        label.setFont(Font.font("Segoe UI", 12));
        return label;
    }

    private TextField createTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setPrefWidth(200);
        tf.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-text-fill: #f1faee;" +
                        "-fx-prompt-text-fill: #888;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;");
        return tf;
    }

    private void styleButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e94560 0%, #c23550 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ff5670 0%, #e94560 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #e94560 0%, #c23550 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"));
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setPrefWidth(200);
        cb.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-text-fill: #f1faee;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;");
    }

    // ===== CLASE AUXILIAR PARA TABLA =====

    public static class LocationRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty time;

        public LocationRow(String name, String time) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.time = new javafx.beans.property.SimpleStringProperty(time);
        }
    }
}
