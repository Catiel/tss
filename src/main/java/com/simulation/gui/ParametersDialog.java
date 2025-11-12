package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

/**
 * Diálogo sencillo para editar los parámetros básicos de la simulación DIGEMIC.
 */
public class ParametersDialog extends Dialog<Boolean> {
    private final TextField durationField;
    private final TextField seedField;
    private final TextField arrivalField;
    private final TextField zonaMinField;
    private final TextField zonaMaxField;
    private final TextField servicioField;
    private final TextField pausaField;
    private final TextField pasaportesField;
    private final TextField entradaCapField;
    private final TextField zonaCapField;
    private final TextField sillasCapField;
    private final TextField pieCapField;
    private final TextField servidor1CapField;
    private final TextField servidor2CapField;
    private final TextField probSalaField;
    private final TextField probFormasField;
    private final Label errorLabel;

    private boolean accepted = false;

    public ParametersDialog(SimulationParameters parameters) {
        setTitle("Parámetros de la simulación");
        setHeaderText("Ajusta los valores y confirma para aplicar los cambios");

        ButtonType applyButtonType = new ButtonType("Aplicar", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(applyButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(8);
        grid.setPadding(new Insets(16, 20, 10, 20));

        int row = 0;
        grid.add(sectionLabel("Generales"), 0, row++, 2, 1);

        durationField = createField(Double.toString(parameters.getSimulationDurationMinutes()));
        addRow(grid, row++, "Duración (min)", durationField);

        seedField = createField(Long.toString(parameters.getBaseRandomSeed()));
        addRow(grid, row++, "Semilla base", seedField);

        arrivalField = createField(Double.toString(parameters.getArrivalMeanTime()));
        addRow(grid, row++, "Media arribos (min)", arrivalField);

        grid.add(sectionLabel("Zona de formularios"), 0, row++, 2, 1);

        zonaMinField = createField(Double.toString(parameters.getZonaFormasMin()));
        addRow(grid, row++, "Tiempo mínimo", zonaMinField);

        zonaMaxField = createField(Double.toString(parameters.getZonaFormasMax()));
        addRow(grid, row++, "Tiempo máximo", zonaMaxField);

        grid.add(sectionLabel("Servidores"), 0, row++, 2, 1);

        servicioField = createField(Double.toString(parameters.getServicioMean()));
        addRow(grid, row++, "Tiempo servicio medio", servicioField);

        pausaField = createField(Double.toString(parameters.getPausaServidorMean()));
        addRow(grid, row++, "Pausa media (cada tanda)", pausaField);

        pasaportesField = createField(Integer.toString(parameters.getPasaportesPorPausa()));
        addRow(grid, row++, "Pasaportes por pausa", pasaportesField);

        grid.add(sectionLabel("Capacidades"), 0, row++, 2, 1);

        entradaCapField = createField(formatCapacity(parameters.getEntradaCapacity()));
        addRow(grid, row++, "Entrada", entradaCapField);

        zonaCapField = createField(formatCapacity(parameters.getZonaFormasCapacity()));
        addRow(grid, row++, "Zona de formas", zonaCapField);

        sillasCapField = createField(formatCapacity(parameters.getSalaSillasCapacity()));
        addRow(grid, row++, "Sala sillas", sillasCapField);

        pieCapField = createField(formatCapacity(parameters.getSalaDePieCapacity()));
        addRow(grid, row++, "Sala de pie", pieCapField);

        servidor1CapField = createField(formatCapacity(parameters.getServidor1Capacity()));
        addRow(grid, row++, "Servidor 1", servidor1CapField);

        servidor2CapField = createField(formatCapacity(parameters.getServidor2Capacity()));
        addRow(grid, row++, "Servidor 2", servidor2CapField);

        grid.add(sectionLabel("Probabilidades de ruteo"), 0, row++, 2, 1);

        probSalaField = createField(Double.toString(parameters.getDirectoASalaProb()));
        addRow(grid, row++, "Directo a sala", probSalaField);

        probFormasField = createField(Double.toString(parameters.getAFormasProb()));
        addRow(grid, row++, "A llenar formas", probFormasField);

        errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: #d32f2f;");
        grid.add(errorLabel, 0, row, 2, 1);

        getDialogPane().setContent(grid);

        Node applyButton = getDialogPane().lookupButton(applyButtonType);
        applyButton.addEventFilter(ActionEvent.ACTION, event -> {
            if (!applyChanges(parameters)) {
                event.consume();
            } else {
                accepted = true;
            }
        });

        setResultConverter(buttonType -> buttonType == applyButtonType && accepted);
    }

    public boolean isAccepted() {
        return accepted;
    }

    private TextField createField(String initialValue) {
        TextField field = new TextField(initialValue);
        GridPane.setHgrow(field, Priority.ALWAYS);
        field.setPrefWidth(160);
        return field;
    }

    private void addRow(GridPane grid, int row, String labelText, TextField field) {
        Label label = new Label(labelText + ":");
        grid.add(label, 0, row);
        grid.add(field, 1, row);
    }

    private Label sectionLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 2 0;");
        return label;
    }

    private boolean applyChanges(SimulationParameters params) {
        try {
            double duration = parseDouble(durationField, "Duración", 1.0, Double.MAX_VALUE, true, false);
            long seed = parseLong(seedField, "Semilla");
            double arrival = parseDouble(arrivalField, "Media de arribos", 0.01, Double.MAX_VALUE, false, false);

            double zonaMin = parseDouble(zonaMinField, "Tiempo mínimo de formularios", 0.01, Double.MAX_VALUE, false, false);
            double zonaMax = parseDouble(zonaMaxField, "Tiempo máximo de formularios", 0.01, Double.MAX_VALUE, false, false);
            if (zonaMax < zonaMin) {
                throw new IllegalArgumentException("El tiempo máximo debe ser mayor o igual al mínimo en formularios");
            }

            double servicio = parseDouble(servicioField, "Tiempo medio de servicio", 0.01, Double.MAX_VALUE, false, false);
            double pausa = parseDouble(pausaField, "Pausa media", 0.01, Double.MAX_VALUE, false, false);
            int pasaportes = parseInt(pasaportesField, "Pasaportes por pausa", 1, Integer.MAX_VALUE);

            int entradaCap = parseCapacity(entradaCapField, "Capacidad entrada");
            int zonaCap = parseCapacity(zonaCapField, "Capacidad zona de formas");
            int sillasCap = parseCapacity(sillasCapField, "Capacidad sala sillas");
            int pieCap = parseCapacity(pieCapField, "Capacidad sala de pie");
            int servidor1Cap = parseCapacity(servidor1CapField, "Capacidad servidor 1");
            int servidor2Cap = parseCapacity(servidor2CapField, "Capacidad servidor 2");

            double probSala = parseDouble(probSalaField, "Probabilidad directo a sala", 0.0, 1.0, true, true);
            double probFormas = parseDouble(probFormasField, "Probabilidad a formularios", 0.0, 1.0, true, true);
            double probSum = probSala + probFormas;
            if (Math.abs(probSum - 1.0) > 1e-6) {
                throw new IllegalArgumentException("La suma de probabilidades debe ser 1.0");
            }

            params.setSimulationDurationMinutes(duration);
            params.setBaseRandomSeed(seed);
            params.setArrivalMeanTime(arrival);
            params.setZonaFormasMin(zonaMin);
            params.setZonaFormasMax(zonaMax);
            params.setServicioMean(servicio);
            params.setPausaServidorMean(pausa);
            params.setPasaportesPorPausa(pasaportes);

            params.setEntradaCapacity(entradaCap);
            params.setZonaFormasCapacity(zonaCap);
            params.setSalaSillasCapacity(sillasCap);
            params.setSalaDePieCapacity(pieCap);
            params.setServidor1Capacity(servidor1Cap);
            params.setServidor2Capacity(servidor2Cap);

            params.setDirectoASalaProb(probSala);
            params.setAFormasProb(probFormas);

            errorLabel.setText("");
            return true;
        } catch (IllegalArgumentException ex) {
            errorLabel.setText(ex.getMessage());
            return false;
        }
    }

    private double parseDouble(TextField field, String label, double min, double max, boolean inclusiveMin, boolean inclusiveMax) {
        String raw = field.getText().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(label + ": ingresa un valor numérico");
        }
        double value;
        try {
            value = Double.parseDouble(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + ": formato inválido");
        }
        if ((inclusiveMin ? value < min : value <= min) || (inclusiveMax ? value > max : value >= max)) {
            String lower = inclusiveMin ? "≥ " : "> ";
            String upper = inclusiveMax ? "≤ " : "< ";
            throw new IllegalArgumentException(label + ": debe estar en el rango " + lower + min + " y " + upper + max);
        }
        return value;
    }

    private int parseInt(TextField field, String label, int min, int max) {
        String raw = field.getText().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(label + ": ingresa un entero válido");
        }
        int value;
        try {
            value = Integer.parseInt(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + ": formato inválido");
        }
        if (value < min || value > max) {
            throw new IllegalArgumentException(label + ": debe estar entre " + min + " y " + max);
        }
        return value;
    }

    private long parseLong(TextField field, String label) {
        String raw = field.getText().trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException(label + ": ingresa un entero válido");
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(label + ": formato inválido");
        }
    }

    private int parseCapacity(TextField field, String label) {
        String raw = field.getText().trim();
        if (raw.isEmpty() || "inf".equalsIgnoreCase(raw)) {
            return Integer.MAX_VALUE;
        }
        return parseInt(field, label, 1, Integer.MAX_VALUE);
    }

    private String formatCapacity(int capacity) {
        return capacity == Integer.MAX_VALUE ? "inf" : Integer.toString(capacity);
    }
}
