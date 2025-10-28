package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ParametersDialog extends Stage {
    private SimulationParameters parameters;
    private boolean accepted = false;

    // Campos de texto para parámetros
    private TextField durationField;
    private TextField seedField;

    // Capacidades
    private TextField lavadoraCapField;
    private TextField almacenPinturaCapField;
    private TextField pinturaCapField;
    private TextField almacenHornoCapField;
    private TextField hornoCapField;
    private TextField inspeccionStationsField;
    private TextField inspeccionOpsField;

    // Distribuciones
    private TextField arrivalMeanField;
    private TextField transportRecLavMeanField;
    private TextField transportLavAlmMeanField;
    private TextField lavadoraMeanField;
    private TextField lavadoraStdDevField;
    private TextField pinturaMinField;
    private TextField pinturaModeField;
    private TextField pinturaMaxField;
    private TextField transportPintAlmMinField;
    private TextField transportPintAlmMaxField;
    private TextField hornoMinField;
    private TextField hornoMaxField;
    private TextField transportHorInspMinField;
    private TextField transportHorInspMaxField;
    private TextField inspeccionMeanField;

    public ParametersDialog(SimulationParameters params) {
        this.parameters = params;

        setTitle("Configuración de Parámetros de Simulación");
        initModality(Modality.APPLICATION_MODAL);

        VBox root = new VBox(10);
        root.setPadding(new Insets(15));

        // Crear pestañas
        TabPane tabPane = new TabPane();

        Tab generalTab = new Tab("General", createGeneralPane());
        generalTab.setClosable(false);

        Tab capacitiesTab = new Tab("Capacidades", createCapacitiesPane());
        capacitiesTab.setClosable(false);

        Tab distributionsTab = new Tab("Distribuciones", createDistributionsPane());
        distributionsTab.setClosable(false);

        tabPane.getTabs().addAll(generalTab, capacitiesTab, distributionsTab);

        // Botones
        Button okButton = new Button("Aceptar");
        okButton.setOnAction(e -> handleOk());

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> handleCancel());

        Button resetButton = new Button("Restaurar Valores por Defecto");
        resetButton.setOnAction(e -> resetToDefaults());

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(okButton, cancelButton, resetButton);
        buttonBox.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(tabPane, buttonBox);

        Scene scene = new Scene(root, 600, 650);
        setScene(scene);

        loadParameters();
    }

    private GridPane createGeneralPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        grid.add(new Label("Duración de Simulación (minutos):"), 0, row);
        durationField = new TextField();
        grid.add(durationField, 1, row++);

        grid.add(new Label("Semilla Aleatoria:"), 0, row);
        seedField = new TextField();
        grid.add(seedField, 1, row++);

        grid.add(new Label("Nota: 2160 min = 36 horas = 1.5 días"), 0, row, 2, 1);

        return grid;
    }

    private GridPane createCapacitiesPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        grid.add(new Label("Capacidad LAVADORA:"), 0, row);
        lavadoraCapField = new TextField();
        grid.add(lavadoraCapField, 1, row++);

        grid.add(new Label("Capacidad ALMACEN_PINTURA:"), 0, row);
        almacenPinturaCapField = new TextField();
        grid.add(almacenPinturaCapField, 1, row++);

        grid.add(new Label("Capacidad PINTURA:"), 0, row);
        pinturaCapField = new TextField();
        grid.add(pinturaCapField, 1, row++);

        grid.add(new Label("Capacidad ALMACEN_HORNO:"), 0, row);
        almacenHornoCapField = new TextField();
        grid.add(almacenHornoCapField, 1, row++);

        grid.add(new Label("Capacidad HORNO:"), 0, row);
        hornoCapField = new TextField();
        grid.add(hornoCapField, 1, row++);

        grid.add(new Label("Número de Estaciones INSPECCION:"), 0, row);
        inspeccionStationsField = new TextField();
        grid.add(inspeccionStationsField, 1, row++);

        grid.add(new Label("Operaciones por Pieza INSPECCION:"), 0, row);
        inspeccionOpsField = new TextField();
        grid.add(inspeccionOpsField, 1, row++);

        return grid;
    }

    private ScrollPane createDistributionsPane() {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10));
        grid.setHgap(10);
        grid.setVgap(10);

        int row = 0;

        // Arribos
        grid.add(new Label("Arribos E(mean):"), 0, row);
        arrivalMeanField = new TextField();
        grid.add(arrivalMeanField, 1, row);
        grid.add(new Label("(Exponencial: E(2))"), 2, row++);

        // Transporte Recepción -> Lavadora
        grid.add(new Label("Transporte REC->LAV E(mean):"), 0, row);
        transportRecLavMeanField = new TextField();
        grid.add(transportRecLavMeanField, 1, row);
        grid.add(new Label("(E(3))"), 2, row++);

        // Transporte Lavadora -> Almacén Pintura
        grid.add(new Label("Transporte LAV->ALM_PINT E(mean):"), 0, row);
        transportLavAlmMeanField = new TextField();
        grid.add(transportLavAlmMeanField, 1, row);
        grid.add(new Label("(E(2))"), 2, row++);

        // Proceso Lavadora
        grid.add(new Label("Proceso LAVADORA N(mean):"), 0, row);
        lavadoraMeanField = new TextField();
        grid.add(lavadoraMeanField, 1, row);
        grid.add(new Label("(Normal: N(10, 2))"), 2, row++);

        grid.add(new Label("Proceso LAVADORA N(stddev):"), 0, row);
        lavadoraStdDevField = new TextField();
        grid.add(lavadoraStdDevField, 1, row++);

        // Proceso Pintura
        grid.add(new Label("Proceso PINTURA T(min):"), 0, row);
        pinturaMinField = new TextField();
        grid.add(pinturaMinField, 1, row);
        grid.add(new Label("(Triangular: T(4, 8, 10))"), 2, row++);

        grid.add(new Label("Proceso PINTURA T(mode):"), 0, row);
        pinturaModeField = new TextField();
        grid.add(pinturaModeField, 1, row++);

        grid.add(new Label("Proceso PINTURA T(max):"), 0, row);
        pinturaMaxField = new TextField();
        grid.add(pinturaMaxField, 1, row++);

        // Transporte Pintura -> Almacén Horno
        grid.add(new Label("Transporte PINT->ALM_HOR U(min):"), 0, row);
        transportPintAlmMinField = new TextField();
        grid.add(transportPintAlmMinField, 1, row);
        grid.add(new Label("(Uniforme: U(3.5, 1.5) = [2, 5])"), 2, row++);

        grid.add(new Label("Transporte PINT->ALM_HOR U(max):"), 0, row);
        transportPintAlmMaxField = new TextField();
        grid.add(transportPintAlmMaxField, 1, row++);

        // Proceso Horno
        grid.add(new Label("Proceso HORNO U(min):"), 0, row);
        hornoMinField = new TextField();
        grid.add(hornoMinField, 1, row);
        grid.add(new Label("(Uniforme: U(3, 1) = [2, 4])"), 2, row++);

        grid.add(new Label("Proceso HORNO U(max):"), 0, row);
        hornoMaxField = new TextField();
        grid.add(hornoMaxField, 1, row++);

        // Transporte Horno -> Inspección
        grid.add(new Label("Transporte HOR->INSP U(min):"), 0, row);
        transportHorInspMinField = new TextField();
        grid.add(transportHorInspMinField, 1, row);
        grid.add(new Label("(Uniforme: U(2, 1) = [1, 3])"), 2, row++);

        grid.add(new Label("Transporte HOR->INSP U(max):"), 0, row);
        transportHorInspMaxField = new TextField();
        grid.add(transportHorInspMaxField, 1, row++);

        // Operaciones Inspección
        grid.add(new Label("Operación INSPECCION E(mean):"), 0, row);
        inspeccionMeanField = new TextField();
        grid.add(inspeccionMeanField, 1, row);
        grid.add(new Label("(E(2) x 3 operaciones)"), 2, row++);

        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        return scrollPane;
    }

    private void loadParameters() {
        durationField.setText(String.valueOf(parameters.getSimulationDurationMinutes()));
        seedField.setText(String.valueOf(parameters.getBaseRandomSeed()));

        lavadoraCapField.setText(String.valueOf(parameters.getLavadoraCapacity()));
        almacenPinturaCapField.setText(String.valueOf(parameters.getAlmacenPinturaCapacity()));
        pinturaCapField.setText(String.valueOf(parameters.getPinturaCapacity()));
        almacenHornoCapField.setText(String.valueOf(parameters.getAlmacenHornoCapacity()));
        hornoCapField.setText(String.valueOf(parameters.getHornoCapacity()));
        inspeccionStationsField.setText(String.valueOf(parameters.getInspeccionNumStations()));
        inspeccionOpsField.setText(String.valueOf(parameters.getInspeccionOperationsPerPiece()));

        arrivalMeanField.setText(String.valueOf(parameters.getArrivalMeanTime()));
        transportRecLavMeanField.setText(String.valueOf(parameters.getTransportRecepcionLavadoraMean()));
        transportLavAlmMeanField.setText(String.valueOf(parameters.getTransportLavadoraAlmacenMean()));
        lavadoraMeanField.setText(String.valueOf(parameters.getLavadoraProcessMean()));
        lavadoraStdDevField.setText(String.valueOf(parameters.getLavadoraProcessStdDev()));
        pinturaMinField.setText(String.valueOf(parameters.getPinturaProcessMin()));
        pinturaModeField.setText(String.valueOf(parameters.getPinturaProcessMode()));
        pinturaMaxField.setText(String.valueOf(parameters.getPinturaProcessMax()));
        transportPintAlmMinField.setText(String.valueOf(parameters.getTransportPinturaAlmacenMin()));
        transportPintAlmMaxField.setText(String.valueOf(parameters.getTransportPinturaAlmacenMax()));
        hornoMinField.setText(String.valueOf(parameters.getHornoProcessMin()));
        hornoMaxField.setText(String.valueOf(parameters.getHornoProcessMax()));
        transportHorInspMinField.setText(String.valueOf(parameters.getTransportHornoInspeccionMin()));
        transportHorInspMaxField.setText(String.valueOf(parameters.getTransportHornoInspeccionMax()));
        inspeccionMeanField.setText(String.valueOf(parameters.getInspeccionOperationMean()));
    }

    private void handleOk() {
        try {
            // Validar y guardar los parámetros
            SimulationParameters newParams = new SimulationParameters();

            newParams.setSimulationDurationMinutes(Double.parseDouble(durationField.getText()));
            newParams.setBaseRandomSeed(Long.parseLong(seedField.getText()));

            // Copiar todos los parámetros al objeto original
            parameters.setSimulationDurationMinutes(newParams.getSimulationDurationMinutes());
            parameters.setBaseRandomSeed(newParams.getBaseRandomSeed());

            accepted = true;
            close();

        } catch (NumberFormatException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error de Validación");
            alert.setHeaderText("Valores inválidos");
            alert.setContentText("Por favor, ingrese valores numéricos válidos.");
            alert.showAndWait();
        }
    }

    private void handleCancel() {
        accepted = false;
        close();
    }

    private void resetToDefaults() {
        SimulationParameters defaults = new SimulationParameters();
        parameters.setSimulationDurationMinutes(defaults.getSimulationDurationMinutes());
        parameters.setBaseRandomSeed(defaults.getBaseRandomSeed());
        loadParameters();
    }

    public boolean isAccepted() {
        return accepted;
    }
}
