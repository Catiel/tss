package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.Locale;

/**
 * Diálogo para editar los parámetros del sistema Multi-Engrane.
 */
public class ParametersDialog extends Stage {
    private final SimulationParameters parameters;
    private boolean accepted = false;

    // General
    private TextField durationField;
    private TextField seedField;

    // Arribos y transportes
    private TextField arrivalMeanField;
    private TextField conveyor1TimeField;
    private TextField conveyor2TimeField;
    private TextField transportWorkerField;

    // Capacidades
    private TextField conveyor1CapField;
    private TextField almacenCapField;
    private TextField cortadoraCapField;
    private TextField tornoCapField;
    private TextField conveyor2CapField;
    private TextField fresadoraCapField;
    private TextField almacen2CapField;
    private TextField pinturaCapField;
    private TextField inspeccion1CapField;
    private TextField inspeccion2CapField;
    private TextField empaqueCapField;
    private TextField embarqueCapField;

    // Procesos
    private TextField almacenMeanField;
    private TextField almacenStdField;
    private TextField cortadoraMeanField;
    private TextField tornoMeanField;
    private TextField tornoStdField;
    private TextField fresadoraMeanField;
    private TextField almacen2MeanField;
    private TextField almacen2StdField;
    private TextField pinturaMeanField;
    private TextField inspeccion1MeanField;
    private TextField inspeccion1StdField;
    private TextField inspeccion2MeanField;
    private TextField empaqueMeanField;
    private TextField empaqueStdField;
    private TextField embarqueMeanField;

    // Probabilidades
    private TextField probEmpaqueField;
    private TextField probInspeccion2Field;

    public ParametersDialog(SimulationParameters params) {
        this.parameters = params;

        setTitle("Parámetros de Simulación - Multi-Engrane");
        initModality(Modality.APPLICATION_MODAL);

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        tabPane.getTabs().add(new Tab("General", createGeneralPane()));
        tabPane.getTabs().add(new Tab("Arribos y Transportes", createArrivalPane()));
        tabPane.getTabs().add(new Tab("Capacidades", createCapacitiesPane()));
        tabPane.getTabs().add(new Tab("Procesos", createProcessesPane()));
        tabPane.getTabs().add(new Tab("Probabilidades", createProbabilitiesPane()));

        Button okButton = new Button("Aceptar");
        okButton.setOnAction(e -> handleOk());

        Button cancelButton = new Button("Cancelar");
        cancelButton.setOnAction(e -> handleCancel());

        Button defaultsButton = new Button("Restaurar por defecto");
        defaultsButton.setOnAction(e -> resetToDefaults());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox buttonRow = new HBox(10, defaultsButton, spacer, cancelButton, okButton);
        buttonRow.setPadding(new Insets(10, 0, 0, 0));

        VBox root = new VBox(12, tabPane, buttonRow);
        root.setPadding(new Insets(15));

        Scene scene = new Scene(root, 720, 640);
        setScene(scene);

        populateFields(parameters);
    }

    private GridPane createGeneralPane() {
        GridPane grid = buildGrid();
        int row = 0;

        durationField = createNumericField();
        addLabeledField(grid, row++, "Duración (min)", durationField, "Total de minutos simulados (ej. 3600 = 60 h)");

        seedField = createNumericField();
        addLabeledField(grid, row, "Semilla aleatoria", seedField, "Número entero para inicializar los generadores");

        return grid;
    }

    private GridPane createArrivalPane() {
        GridPane grid = buildGrid();
        int row = 0;

        arrivalMeanField = createNumericField();
        addLabeledField(grid, row++, "Arribos - media (min)", arrivalMeanField, "Tiempo medio entre arribos");

        conveyor1TimeField = createNumericField();
        addLabeledField(grid, row++, "Tiempo CONVEYOR 1 (min)", conveyor1TimeField, "Tiempo fijo de salida desde CONVEYOR 1");

        conveyor2TimeField = createNumericField();
        addLabeledField(grid, row++, "Tiempo CONVEYOR 2 (min)", conveyor2TimeField, "Tiempo fijo de salida desde CONVEYOR 2");

        transportWorkerField = createNumericField();
        addLabeledField(grid, row, "Tiempo transporte trabajador (min)", transportWorkerField, "Tiempo promedio de los recursos de transporte");

        return grid;
    }

    private ScrollPane createCapacitiesPane() {
        GridPane grid = buildGrid();
        int row = 0;

        conveyor1CapField = createNumericField();
        addLabeledField(grid, row++, "CONVEYOR 1", conveyor1CapField, "Use ∞ para capacidad ilimitada");

        almacenCapField = createNumericField();
        addLabeledField(grid, row++, "ALMACEN 1", almacenCapField, "Capacidad máxima de ALMACEN");

        cortadoraCapField = createNumericField();
        addLabeledField(grid, row++, "CORTADORA", cortadoraCapField, null);

        tornoCapField = createNumericField();
        addLabeledField(grid, row++, "TORNO", tornoCapField, null);

        conveyor2CapField = createNumericField();
        addLabeledField(grid, row++, "CONVEYOR 2", conveyor2CapField, "Use ∞ para capacidad ilimitada");

        fresadoraCapField = createNumericField();
        addLabeledField(grid, row++, "FRESADORA", fresadoraCapField, null);

        almacen2CapField = createNumericField();
        addLabeledField(grid, row++, "ALMACEN 2", almacen2CapField, null);

        pinturaCapField = createNumericField();
        addLabeledField(grid, row++, "PINTURA", pinturaCapField, null);

        inspeccion1CapField = createNumericField();
        addLabeledField(grid, row++, "INSPECCION 1", inspeccion1CapField, null);

        inspeccion2CapField = createNumericField();
        addLabeledField(grid, row++, "INSPECCION 2", inspeccion2CapField, null);

        empaqueCapField = createNumericField();
        addLabeledField(grid, row++, "EMPAQUE", empaqueCapField, null);

        embarqueCapField = createNumericField();
        addLabeledField(grid, row, "EMBARQUE", embarqueCapField, null);

        return wrap(grid);
    }

    private ScrollPane createProcessesPane() {
        GridPane grid = buildGrid();
        int row = 0;

        row = addProcessSection(grid, row, "ALMACEN", almacenMeanField = createNumericField(), almacenStdField = createNumericField());
        row = addProcessSection(grid, row, "CORTADORA (media exponencial)", cortadoraMeanField = createNumericField(), null);
        row = addProcessSection(grid, row, "TORNO", tornoMeanField = createNumericField(), tornoStdField = createNumericField());
        row = addProcessSection(grid, row, "FRESADORA (media exponencial)", fresadoraMeanField = createNumericField(), null);
        row = addProcessSection(grid, row, "ALMACEN 2", almacen2MeanField = createNumericField(), almacen2StdField = createNumericField());
        row = addProcessSection(grid, row, "PINTURA (media exponencial)", pinturaMeanField = createNumericField(), null);
        row = addProcessSection(grid, row, "INSPECCION 1", inspeccion1MeanField = createNumericField(), inspeccion1StdField = createNumericField());
        row = addProcessSection(grid, row, "INSPECCION 2 (media exponencial)", inspeccion2MeanField = createNumericField(), null);
        row = addProcessSection(grid, row, "EMPAQUE", empaqueMeanField = createNumericField(), empaqueStdField = createNumericField());
        addProcessSection(grid, row, "EMBARQUE (media exponencial)", embarqueMeanField = createNumericField(), null);

        return wrap(grid);
    }

    private GridPane createProbabilitiesPane() {
        GridPane grid = buildGrid();
        int row = 0;

        probEmpaqueField = createNumericField();
        addLabeledField(grid, row++, "Prob. INSPECCION 1 → EMPAQUE", probEmpaqueField, "Valor entre 0 y 1");

        probInspeccion2Field = createNumericField();
        addLabeledField(grid, row, "Prob. INSPECCION 1 → INSPECCION 2", probInspeccion2Field, "Valor entre 0 y 1 (ambas deben sumar 1)");

        return grid;
    }

    private void populateFields(SimulationParameters source) {
        durationField.setText(formatDouble(source.getSimulationDurationMinutes()));
        seedField.setText(Long.toString(source.getBaseRandomSeed()));

        arrivalMeanField.setText(formatDouble(source.getArrivalMeanTime()));
        conveyor1TimeField.setText(formatDouble(source.getConveyor1Time()));
        conveyor2TimeField.setText(formatDouble(source.getConveyor2Time()));
        transportWorkerField.setText(formatDouble(source.getTransportWorkerTime()));

        conveyor1CapField.setText(formatCapacity(source.getConveyor1Capacity()));
        almacenCapField.setText(formatCapacity(source.getAlmacenCapacity()));
        cortadoraCapField.setText(formatCapacity(source.getCortadoraCapacity()));
        tornoCapField.setText(formatCapacity(source.getTornoCapacity()));
        conveyor2CapField.setText(formatCapacity(source.getConveyor2Capacity()));
        fresadoraCapField.setText(formatCapacity(source.getFresadoraCapacity()));
        almacen2CapField.setText(formatCapacity(source.getAlmacen2Capacity()));
        pinturaCapField.setText(formatCapacity(source.getPinturaCapacity()));
        inspeccion1CapField.setText(formatCapacity(source.getInspeccion1Capacity()));
        inspeccion2CapField.setText(formatCapacity(source.getInspeccion2Capacity()));
        empaqueCapField.setText(formatCapacity(source.getEmpaqueCapacity()));
        embarqueCapField.setText(formatCapacity(source.getEmbarqueCapacity()));

        almacenMeanField.setText(formatDouble(source.getAlmacenProcessMean()));
        almacenStdField.setText(formatDouble(source.getAlmacenProcessStdDev()));
        cortadoraMeanField.setText(formatDouble(source.getCortadoraProcessMean()));
        tornoMeanField.setText(formatDouble(source.getTornoProcessMean()));
        tornoStdField.setText(formatDouble(source.getTornoProcessStdDev()));
        fresadoraMeanField.setText(formatDouble(source.getFresadoraProcessMean()));
        almacen2MeanField.setText(formatDouble(source.getAlmacen2ProcessMean()));
        almacen2StdField.setText(formatDouble(source.getAlmacen2ProcessStdDev()));
        pinturaMeanField.setText(formatDouble(source.getPinturaProcessMean()));
        inspeccion1MeanField.setText(formatDouble(source.getInspeccion1ProcessMean()));
        inspeccion1StdField.setText(formatDouble(source.getInspeccion1ProcessStdDev()));
        inspeccion2MeanField.setText(formatDouble(source.getInspeccion2ProcessMean()));
        empaqueMeanField.setText(formatDouble(source.getEmpaqueProcessMean()));
        empaqueStdField.setText(formatDouble(source.getEmpaqueProcessStdDev()));
        embarqueMeanField.setText(formatDouble(source.getEmbarqueProcessMean()));

        probEmpaqueField.setText(formatDouble(source.getInspeccion1ToEmpaqueProb()));
        probInspeccion2Field.setText(formatDouble(source.getInspeccion1ToInspeccion2Prob()));
    }

    private void handleOk() {
        try {
            double duration = parsePositiveDouble(durationField, "Duración de simulación", false);
            long seed = parseLong(seedField, "Semilla aleatoria");

            double arrivalMean = parsePositiveDouble(arrivalMeanField, "Arribos - media", false);
            double conveyor1Time = parsePositiveDouble(conveyor1TimeField, "Tiempo CONVEYOR 1", false);
            double conveyor2Time = parsePositiveDouble(conveyor2TimeField, "Tiempo CONVEYOR 2", false);
            double transportWorker = parsePositiveDouble(transportWorkerField, "Tiempo de transporte", false);

            int conveyor1Cap = parseCapacity(conveyor1CapField, "Capacidad CONVEYOR 1", true, 1);
            int almacenCap = parseCapacity(almacenCapField, "Capacidad ALMACEN 1", false, 1);
            int cortadoraCap = parseCapacity(cortadoraCapField, "Capacidad CORTADORA", false, 1);
            int tornoCap = parseCapacity(tornoCapField, "Capacidad TORNO", false, 1);
            int conveyor2Cap = parseCapacity(conveyor2CapField, "Capacidad CONVEYOR 2", true, 1);
            int fresadoraCap = parseCapacity(fresadoraCapField, "Capacidad FRESADORA", false, 1);
            int almacen2Cap = parseCapacity(almacen2CapField, "Capacidad ALMACEN 2", false, 1);
            int pinturaCap = parseCapacity(pinturaCapField, "Capacidad PINTURA", false, 1);
            int inspeccion1Cap = parseCapacity(inspeccion1CapField, "Capacidad INSPECCION 1", false, 1);
            int inspeccion2Cap = parseCapacity(inspeccion2CapField, "Capacidad INSPECCION 2", false, 1);
            int empaqueCap = parseCapacity(empaqueCapField, "Capacidad EMPAQUE", false, 1);
            int embarqueCap = parseCapacity(embarqueCapField, "Capacidad EMBARQUE", false, 1);

            double almacenMean = parsePositiveDouble(almacenMeanField, "Proceso ALMACEN - media", false);
            double almacenStd = parsePositiveDouble(almacenStdField, "Proceso ALMACEN - desviación", true);
            double cortadoraMean = parsePositiveDouble(cortadoraMeanField, "Proceso CORTADORA - media", false);
            double tornoMean = parsePositiveDouble(tornoMeanField, "Proceso TORNO - media", false);
            double tornoStd = parsePositiveDouble(tornoStdField, "Proceso TORNO - desviación", true);
            double fresadoraMean = parsePositiveDouble(fresadoraMeanField, "Proceso FRESADORA - media", false);
            double almacen2Mean = parsePositiveDouble(almacen2MeanField, "Proceso ALMACEN 2 - media", false);
            double almacen2Std = parsePositiveDouble(almacen2StdField, "Proceso ALMACEN 2 - desviación", true);
            double pinturaMean = parsePositiveDouble(pinturaMeanField, "Proceso PINTURA - media", false);
            double inspeccion1Mean = parsePositiveDouble(inspeccion1MeanField, "Proceso INSPECCION 1 - media", false);
            double inspeccion1Std = parsePositiveDouble(inspeccion1StdField, "Proceso INSPECCION 1 - desviación", true);
            double inspeccion2Mean = parsePositiveDouble(inspeccion2MeanField, "Proceso INSPECCION 2 - media", false);
            double empaqueMean = parsePositiveDouble(empaqueMeanField, "Proceso EMPAQUE - media", false);
            double empaqueStd = parsePositiveDouble(empaqueStdField, "Proceso EMPAQUE - desviación", true);
            double embarqueMean = parsePositiveDouble(embarqueMeanField, "Proceso EMBARQUE - media", false);

            double probEmpaque = parseProbability(probEmpaqueField, "Probabilidad a EMPAQUE");
            double probInspeccion2 = parseProbability(probInspeccion2Field, "Probabilidad a INSPECCION 2");
            if (Math.abs((probEmpaque + probInspeccion2) - 1.0) > 1e-6) {
                throw new IllegalArgumentException("Las probabilidades de INSPECCION 1 deben sumar 1.0");
            }

            parameters.setSimulationDurationMinutes(duration);
            parameters.setBaseRandomSeed(seed);

            parameters.setArrivalMeanTime(arrivalMean);
            parameters.setConveyor1Time(conveyor1Time);
            parameters.setConveyor2Time(conveyor2Time);
            parameters.setTransportWorkerTime(transportWorker);

            parameters.setConveyor1Capacity(conveyor1Cap);
            parameters.setAlmacenCapacity(almacenCap);
            parameters.setCortadoraCapacity(cortadoraCap);
            parameters.setTornoCapacity(tornoCap);
            parameters.setConveyor2Capacity(conveyor2Cap);
            parameters.setFresadoraCapacity(fresadoraCap);
            parameters.setAlmacen2Capacity(almacen2Cap);
            parameters.setPinturaCapacity(pinturaCap);
            parameters.setInspeccion1Capacity(inspeccion1Cap);
            parameters.setInspeccion2Capacity(inspeccion2Cap);
            parameters.setEmpaqueCapacity(empaqueCap);
            parameters.setEmbarqueCapacity(embarqueCap);

            parameters.setAlmacenProcessMean(almacenMean);
            parameters.setAlmacenProcessStdDev(almacenStd);
            parameters.setCortadoraProcessMean(cortadoraMean);
            parameters.setTornoProcessMean(tornoMean);
            parameters.setTornoProcessStdDev(tornoStd);
            parameters.setFresadoraProcessMean(fresadoraMean);
            parameters.setAlmacen2ProcessMean(almacen2Mean);
            parameters.setAlmacen2ProcessStdDev(almacen2Std);
            parameters.setPinturaProcessMean(pinturaMean);
            parameters.setInspeccion1ProcessMean(inspeccion1Mean);
            parameters.setInspeccion1ProcessStdDev(inspeccion1Std);
            parameters.setInspeccion2ProcessMean(inspeccion2Mean);
            parameters.setEmpaqueProcessMean(empaqueMean);
            parameters.setEmpaqueProcessStdDev(empaqueStd);
            parameters.setEmbarqueProcessMean(embarqueMean);

            parameters.setInspeccion1ToEmpaqueProb(probEmpaque);
            parameters.setInspeccion1ToInspeccion2Prob(probInspeccion2);

            accepted = true;
            close();
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
        }
    }

    private void handleCancel() {
        accepted = false;
        close();
    }

    private void resetToDefaults() {
        populateFields(new SimulationParameters());
    }

    public boolean isAccepted() {
        return accepted;
    }

    private GridPane buildGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(8);
        grid.setPadding(new Insets(10));
        return grid;
    }

    private TextField createNumericField() {
        TextField field = new TextField();
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }

    private void addLabeledField(GridPane grid, int row, String labelText, TextField field, String tooltip) {
        Label label = new Label(labelText + ":");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
        GridPane.setHgrow(field, Priority.ALWAYS);
        if (tooltip != null) {
            label.setTooltip(new javafx.scene.control.Tooltip(tooltip));
            field.setTooltip(new javafx.scene.control.Tooltip(tooltip));
        }
    }

    private int addProcessSection(GridPane grid, int row, String title, TextField meanField, TextField stdField) {
        Label header = new Label(title);
        header.setStyle("-fx-font-weight: bold; -fx-padding: 12 0 0 0;");
        grid.add(header, 0, row++, 2, 1);

        addLabeledField(grid, row++, "  Media (min)", meanField, null);
        if (stdField != null) {
            addLabeledField(grid, row++, "  Desviación (min)", stdField, "Ingrese 0 si desea un valor determinístico");
        }
        return row;
    }

    private ScrollPane wrap(GridPane grid) {
        ScrollPane scrollPane = new ScrollPane(grid);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
        return scrollPane;
    }

    private String formatDouble(double value) {
        String formatted = String.format(Locale.US, "%.4f", value);
        while (formatted.contains(".") && (formatted.endsWith("0") || formatted.endsWith("."))) {
            formatted = formatted.substring(0, formatted.length() - 1);
        }
        return formatted;
    }

    private String formatCapacity(int capacity) {
        return capacity >= Integer.MAX_VALUE ? "∞" : Integer.toString(capacity);
    }

    private double parsePositiveDouble(TextField field, String name, boolean allowZero) {
        String text = normalizeNumeric(field.getText());
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Ingrese un valor para " + name);
        }
        double value = Double.parseDouble(text);
        if (!allowZero && value <= 0) {
            throw new IllegalArgumentException(name + " debe ser mayor a 0");
        }
        if (allowZero && value < 0) {
            throw new IllegalArgumentException(name + " no puede ser negativo");
        }
        return value;
    }

    private double parseProbability(TextField field, String name) {
        double value = parsePositiveDouble(field, name, true);
        if (value < 0 || value > 1) {
            throw new IllegalArgumentException(name + " debe estar entre 0 y 1");
        }
        return value;
    }

    private long parseLong(TextField field, String name) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Ingrese un valor para " + name);
        }
        return Long.parseLong(text);
    }

    private int parseCapacity(TextField field, String name, boolean allowInfinite, int minValue) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException("Ingrese un valor para " + name);
        }
        if (allowInfinite && (text.equalsIgnoreCase("inf") || text.equals("∞"))) {
            return Integer.MAX_VALUE;
        }
        int value = Integer.parseInt(text);
        if (value < minValue) {
            throw new IllegalArgumentException(name + " debe ser al menos " + minValue);
        }
        return value;
    }

    private String normalizeNumeric(String raw) {
        return raw == null ? "" : raw.trim().replace(',', '.');
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de validación");
        alert.setHeaderText("No se pudieron guardar los parámetros");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
