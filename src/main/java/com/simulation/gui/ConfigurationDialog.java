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

import java.util.HashMap;
import java.util.Map;

/**
 * Ventana de diálogo FUNCIONAL para configurar todos los parámetros de la simulación.
 * Todos los cambios se aplican directamente al motor de simulación.
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

    // Controles de tiempos de procesamiento (NUEVO - funcional)
    private Map<String, TextField> processingTimeFields = new HashMap<>();

    // Controles de capacidades (NUEVO - funcional)
    private Map<String, TextField> capacityFields = new HashMap<>();

    // Control de inspección exponencial
    private CheckBox cbInspeccionExponential;

    public ConfigurationDialog(SimulationConfig config) {
        this.config = config;

        setTitle("⚙️ Configuración Completa de Simulación");
        initModality(Modality.APPLICATION_MODAL);
        setResizable(true);
        setMinWidth(800);
        setMinHeight(700);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #1a1a2e 0%, #16213e 100%);");

        // Crear título con información
        VBox titleBox = createTitleSection();
        root.setTop(titleBox);

        // Crear tabs
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: #0f3460;");

        tabPane.getTabs().addAll(
                createGeneralTab(),
                createProcessingTimesTab(),
                createCapacitiesTab(),
                createResourcesTab(),
                createArrivalsTab());

        root.setCenter(tabPane);

        // Botones de acción
        HBox buttonBox = createButtonBox();
        root.setBottom(buttonBox);

        Scene scene = new Scene(root, 850, 700);
        setScene(scene);

        loadConfigValues();
    }

    private VBox createTitleSection() {
        VBox titleBox = new VBox(5);
        titleBox.setPadding(new Insets(15));
        titleBox.setAlignment(Pos.CENTER);
        titleBox.setStyle("-fx-background-color: linear-gradient(to right, #e94560, #0f3460);");

        Label titleLabel = new Label("⚙ CONFIGURACIÓN DE SIMULACIÓN");
        titleLabel.setFont(Font.font("Segoe UI", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.WHITE);

        Label subtitleLabel = new Label("Todos los cambios se aplicarán automáticamente al reiniciar la simulación");
        subtitleLabel.setFont(Font.font("Segoe UI", 12));
        subtitleLabel.setTextFill(Color.web("#a8dadc"));

        titleBox.getChildren().addAll(titleLabel, subtitleLabel);
        return titleBox;
    }

    private Tab createGeneralTab() {
        Tab tab = new Tab("📊 General");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #0f3460;");

        int row = 0;

        // Título de sección
        Label sectionTitle = createSectionLabel("Parámetros Generales de Simulación");
        grid.add(sectionTitle, 0, row++, 2, 1);

        // Tiempo de simulación
        grid.add(createLabel("Tiempo de Simulación (horas):"), 0, row);
        tfSimulationTime = createTextField("1000.05");
        tfSimulationTime.setTooltip(new Tooltip("Duración total de la simulación en horas"));
        grid.add(tfSimulationTime, 1, row++);

        // Número de réplicas
        grid.add(createLabel("Número de Réplicas:"), 0, row);
        tfReplicas = createTextField("3");
        tfReplicas.setTooltip(new Tooltip("Cantidad de réplicas para promediar resultados"));
        grid.add(tfReplicas, 1, row++);

        // Warm-up time
        grid.add(createLabel("Tiempo de Calentamiento (horas):"), 0, row);
        tfWarmUp = createTextField("0.0");
        tfWarmUp.setTooltip(new Tooltip("Tiempo inicial descartado para estadísticas"));
        grid.add(tfWarmUp, 1, row++);

        // Semilla aleatoria
        grid.add(createLabel("Semilla Aleatoria:"), 0, row);
        HBox seedBox = new HBox(10);
        tfRandomSeed = createTextField(String.valueOf(System.currentTimeMillis()));
        tfRandomSeed.setPrefWidth(180);
        Button btnRandomSeed = new Button("🎲 Nueva");
        btnRandomSeed.setOnAction(e -> tfRandomSeed.setText(String.valueOf(System.currentTimeMillis())));
        styleSmallButton(btnRandomSeed);
        seedBox.getChildren().addAll(tfRandomSeed, btnRandomSeed);
        grid.add(seedBox, 1, row++);

        // Separador
        grid.add(new Separator(), 0, row++, 2, 1);

        // HORNO específico
        Label hornoTitle = createSectionLabel("⚡ Configuración del HORNO (Procesamiento por Lotes)");
        grid.add(hornoTitle, 0, row++, 2, 1);

        Label hornoBatchInfo = new Label("El horno acumula piezas hasta completar el lote, luego procesa todas juntas.\nEj: Capacidad=10, Lote=5 → procesa 2 lotes de 5 piezas.");
        hornoBatchInfo.setTextFill(Color.web("#a8dadc"));
        hornoBatchInfo.setFont(Font.font("Segoe UI", 11));
        hornoBatchInfo.setWrapText(true);
        grid.add(hornoBatchInfo, 0, row++, 2, 1);

        grid.add(createLabel("Tamaño del Lote (piezas):"), 0, row);
        tfHornoBatchSize = createTextField("10");
        tfHornoBatchSize.setTooltip(new Tooltip("Número de piezas a acumular antes de iniciar el procesamiento."));
        grid.add(tfHornoBatchSize, 1, row++);

        grid.add(createLabel("Tiempo de Procesamiento (min):"), 0, row);
        tfHornoProcessingTime = createTextField("100.0");
        tfHornoProcessingTime.setTooltip(new Tooltip("Tiempo que tarda en procesar un lote completo"));
        grid.add(tfHornoProcessingTime, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createProcessingTimesTab() {
        Tab tab = new Tab("⏱️ Tiempos de Proceso");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0f3460;");

        Label titleLabel = createSectionLabel("Tiempos de Procesamiento por Locación (minutos)");
        vbox.getChildren().add(titleLabel);

        Label infoLabel = new Label("💡 Modifica los tiempos de procesamiento de cada estación de trabajo");
        infoLabel.setTextFill(Color.web("#a8dadc"));
        infoLabel.setFont(Font.font("Segoe UI", 11));
        vbox.getChildren().add(infoLabel);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));

        // Locaciones con tiempos de procesamiento configurables
        String[][] locations = {
                {"TORNEADO", "Torneado (maquinado)"},
                {"FRESADO", "Fresado"},
                {"TALADRO", "Taladrado"},
                {"RECTIFICADO", "Rectificado"},
                {"CARGA", "Carga de pieza"},
                {"DESCARGA", "Descarga de pieza"},
                {"BANDA_1", "Banda transportadora 1"},
                {"BANDA_2", "Banda transportadora 2"},
                {"INSPECCION", "Inspección de calidad"}
        };

        int row = 0;
        for (String[] loc : locations) {
            String key = loc[0];
            String displayName = loc[1];

            Label label = createLabel(displayName + ":");
            grid.add(label, 0, row);

            TextField tf = createTextField(String.valueOf(config.getProcessingTime(key)));
            tf.setPrefWidth(100);
            processingTimeFields.put(key, tf);
            grid.add(tf, 1, row);

            Label unitLabel = new Label("min");
            unitLabel.setTextFill(Color.web("#a8dadc"));
            grid.add(unitLabel, 2, row);

            row++;
        }

        // Checkbox para inspección exponencial
        grid.add(new Separator(), 0, row++, 3, 1);

        cbInspeccionExponential = new CheckBox("Inspección usa distribución EXPONENCIAL");
        cbInspeccionExponential.setSelected(config.isInspeccionExponential());
        cbInspeccionExponential.setTextFill(Color.web("#f1faee"));
        cbInspeccionExponential.setTooltip(new Tooltip("Si está marcado, el tiempo de inspección sigue una distribución exponencial"));
        grid.add(cbInspeccionExponential, 0, row++, 3, 1);

        vbox.getChildren().add(grid);

        ScrollPane scroll = new ScrollPane(vbox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createCapacitiesTab() {
        Tab tab = new Tab("📦 Capacidades");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setStyle("-fx-background-color: #0f3460;");

        Label titleLabel = createSectionLabel("Capacidad de Locaciones (unidades)");
        vbox.getChildren().add(titleLabel);

        Label infoLabel = new Label("💡 Define cuántas piezas puede contener cada estación simultáneamente");
        infoLabel.setTextFill(Color.web("#a8dadc"));
        infoLabel.setFont(Font.font("Segoe UI", 11));
        vbox.getChildren().add(infoLabel);

        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(12);
        grid.setPadding(new Insets(15, 0, 0, 0));

        // Nota explicativa
        Label hornoNote = new Label("💡 HORNO: La capacidad define cuántas piezas caben. El tamaño del lote (pestaña General) define cuántas se procesan juntas.");
        hornoNote.setTextFill(Color.web("#00d9ff"));
        hornoNote.setFont(Font.font("Segoe UI", 11));
        hornoNote.setWrapText(true);
        grid.add(hornoNote, 0, 0, 3, 1);

        // Locaciones con capacidades configurables (incluyendo HORNO)
        String[][] locations = {
                {"HORNO", "Horno"},
                {"TORNEADO", "Torneado"},
                {"FRESADO", "Fresado"},
                {"TALADRO", "Taladrado"},
                {"RECTIFICADO", "Rectificado"},
                {"INSPECCION", "Inspección"}
        };

        int row = 1;
        for (String[] loc : locations) {
            String key = loc[0];
            String displayName = loc[1];

            Label label = createLabel(displayName + ":");
            grid.add(label, 0, row);

            int capacity = config.getLocationCapacity(key);
            TextField tf = createTextField(String.valueOf(capacity == Integer.MAX_VALUE ? 999999 : capacity));
            tf.setPrefWidth(100);
            capacityFields.put(key, tf);
            grid.add(tf, 1, row);

            Label unitLabel = new Label("piezas");
            unitLabel.setTextFill(Color.web("#a8dadc"));
            grid.add(unitLabel, 2, row);

            row++;
        }

        // Nota sobre capacidades infinitas
        Label noteLabel = new Label("⚠️ Las bandas, almacén, carga y descarga tienen capacidad infinita por diseño");
        noteLabel.setTextFill(Color.web("#ffa31a"));
        noteLabel.setFont(Font.font("Segoe UI", 10));
        noteLabel.setWrapText(true);
        grid.add(noteLabel, 0, row++, 3, 1);

        vbox.getChildren().add(grid);

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
        Label gruaLabel = createSectionLabel("🏗️ GRÚA VIAJERA");
        grid.add(gruaLabel, 0, row++, 2, 1);

        Label gruaInfo = new Label("Transporta piezas: Almacén → Horno → Banda 1");
        gruaInfo.setTextFill(Color.web("#a8dadc"));
        gruaInfo.setFont(Font.font("Segoe UI", 11));
        grid.add(gruaInfo, 0, row++, 2, 1);

        grid.add(createLabel("Cantidad de Grúas:"), 0, row);
        tfGruaQuantity = createTextField("1");
        tfGruaQuantity.setTooltip(new Tooltip("Número de grúas viajeras disponibles"));
        grid.add(tfGruaQuantity, 1, row++);

        grid.add(createLabel("Velocidad Vacía (metros/min):"), 0, row);
        tfGruaEmptySpeed = createTextField("25.0");
        tfGruaEmptySpeed.setTooltip(new Tooltip("Velocidad de desplazamiento sin carga"));
        grid.add(tfGruaEmptySpeed, 1, row++);

        grid.add(createLabel("Velocidad Cargada (metros/min):"), 0, row);
        tfGruaLoadedSpeed = createTextField("25.0");
        tfGruaLoadedSpeed.setTooltip(new Tooltip("Velocidad de desplazamiento con carga"));
        grid.add(tfGruaLoadedSpeed, 1, row++);

        grid.add(new Separator(), 0, row++, 2, 1);

        // ROBOT
        Label robotLabel = createSectionLabel("🤖 ROBOT");
        grid.add(robotLabel, 0, row++, 2, 1);

        Label robotInfo = new Label("Transporta piezas: Carga → Torneado → Fresado → Taladro → Rectificado → Descarga");
        robotInfo.setTextFill(Color.web("#a8dadc"));
        robotInfo.setFont(Font.font("Segoe UI", 11));
        robotInfo.setWrapText(true);
        grid.add(robotInfo, 0, row++, 2, 1);

        grid.add(createLabel("Cantidad de Robots:"), 0, row);
        tfRobotQuantity = createTextField("1");
        tfRobotQuantity.setTooltip(new Tooltip("Número de robots disponibles"));
        grid.add(tfRobotQuantity, 1, row++);

        grid.add(createLabel("Velocidad Vacía (metros/min):"), 0, row);
        tfRobotEmptySpeed = createTextField("45.0");
        tfRobotEmptySpeed.setTooltip(new Tooltip("Velocidad de desplazamiento sin carga"));
        grid.add(tfRobotEmptySpeed, 1, row++);

        grid.add(createLabel("Velocidad Cargada (metros/min):"), 0, row);
        tfRobotLoadedSpeed = createTextField("45.0");
        tfRobotLoadedSpeed.setTooltip(new Tooltip("Velocidad de desplazamiento con carga"));
        grid.add(tfRobotLoadedSpeed, 1, row++);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private Tab createArrivalsTab() {
        Tab tab = new Tab("📦 Llegadas");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setStyle("-fx-background-color: #0f3460;");

        int row = 0;

        Label titleLabel = createSectionLabel("Configuración de Llegadas de Piezas");
        grid.add(titleLabel, 0, row++, 2, 1);

        Label infoLabel = new Label("💡 Define cómo llegan las piezas automotrices al sistema");
        infoLabel.setTextFill(Color.web("#a8dadc"));
        infoLabel.setFont(Font.font("Segoe UI", 11));
        grid.add(infoLabel, 0, row++, 2, 1);

        grid.add(new Separator(), 0, row++, 2, 1);

        // Distribución
        grid.add(createLabel("Tipo de Distribución:"), 0, row);
        cbArrivalDistribution = new ComboBox<>();
        cbArrivalDistribution.getItems().addAll("EXPONENTIAL", "CONSTANT");
        cbArrivalDistribution.setValue("EXPONENTIAL");
        cbArrivalDistribution.setTooltip(new Tooltip("EXPONENTIAL: tiempos aleatorios, CONSTANT: tiempos fijos"));
        styleComboBox(cbArrivalDistribution);
        grid.add(cbArrivalDistribution, 1, row++);

        // Tiempo medio
        grid.add(createLabel("Tiempo Medio entre Llegadas (min):"), 0, row);
        tfArrivalMean = createTextField("5.0");
        tfArrivalMean.setTooltip(new Tooltip("Tiempo promedio entre llegadas de piezas"));
        grid.add(tfArrivalMean, 1, row++);

        // Primer arribo
        grid.add(createLabel("Tiempo del Primer Arribo (min):"), 0, row);
        tfArrivalFirst = createTextField("0.0");
        tfArrivalFirst.setTooltip(new Tooltip("Momento en que llega la primera pieza"));
        grid.add(tfArrivalFirst, 1, row++);

        // Máximo de entidades
        grid.add(createLabel("Máximo de Piezas a Generar:"), 0, row);
        tfArrivalMax = createTextField("12000");
        tfArrivalMax.setTooltip(new Tooltip("Cantidad máxima de piezas que entrarán al sistema"));
        grid.add(tfArrivalMax, 1, row++);

        // Información adicional
        grid.add(new Separator(), 0, row++, 2, 1);

        VBox infoBox = new VBox(5);
        Label formula = new Label("📐 Fórmula Exponencial: tiempo = -media × ln(1 - U)");
        formula.setTextFill(Color.web("#00d9ff"));
        formula.setFont(Font.font("Consolas", 11));

        Label note = new Label("Donde U es un número aleatorio uniforme entre 0 y 1");
        note.setTextFill(Color.web("#a8dadc"));
        note.setFont(Font.font("Segoe UI", 10));

        infoBox.getChildren().addAll(formula, note);
        grid.add(infoBox, 0, row++, 2, 1);

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #0f3460; -fx-background-color: #0f3460;");
        tab.setContent(scroll);

        return tab;
    }

    private HBox createButtonBox() {
        HBox box = new HBox(15);
        box.setPadding(new Insets(20));
        box.setAlignment(Pos.CENTER);
        box.setStyle("-fx-background-color: #0f3460;");

        Button btnOk = new Button("✅ Aplicar y Cerrar");
        btnOk.setPrefWidth(150);
        btnOk.setOnAction(e -> {
            if (validateAndSave()) {
                accepted = true;
                close();
            }
        });
        styleButton(btnOk);

        Button btnApply = new Button("💾 Aplicar");
        btnApply.setPrefWidth(120);
        btnApply.setOnAction(e -> {
            if (validateAndSave()) {
                showSuccessMessage("Configuración aplicada correctamente.\nReiniciar simulación para ver cambios.");
            }
        });
        styleApplyButton(btnApply);

        Button btnReset = new Button("🔄 Restablecer");
        btnReset.setPrefWidth(120);
        btnReset.setOnAction(e -> resetToDefaults());
        styleResetButton(btnReset);

        Button btnCancel = new Button("❌ Cancelar");
        btnCancel.setPrefWidth(120);
        btnCancel.setOnAction(e -> close());
        styleButton(btnCancel);

        box.getChildren().addAll(btnOk, btnApply, btnReset, btnCancel);
        return box;
    }

    private void loadConfigValues() {
        // General
        tfSimulationTime.setText(String.valueOf(config.getSimulationTime()));
        tfReplicas.setText(String.valueOf(config.getNumberOfReplicas()));
        tfWarmUp.setText(String.valueOf(config.getWarmUpTime()));
        tfRandomSeed.setText(String.valueOf(config.getRandomSeed()));

        // Horno
        tfHornoBatchSize.setText(String.valueOf(config.getHornoBatchSize()));
        tfHornoProcessingTime.setText(String.valueOf(config.getHornoProcessingTime()));

        // Recursos
        tfGruaQuantity.setText(String.valueOf(config.getGruaQuantity()));
        tfGruaEmptySpeed.setText(String.valueOf(config.getGruaEmptySpeed()));
        tfGruaLoadedSpeed.setText(String.valueOf(config.getGruaLoadedSpeed()));

        tfRobotQuantity.setText(String.valueOf(config.getRobotQuantity()));
        tfRobotEmptySpeed.setText(String.valueOf(config.getRobotEmptySpeed()));
        tfRobotLoadedSpeed.setText(String.valueOf(config.getRobotLoadedSpeed()));

        // Arribos
        cbArrivalDistribution.setValue(config.getArrivalDistribution());
        tfArrivalMean.setText(String.valueOf(config.getArrivalMeanTime()));
        tfArrivalFirst.setText(String.valueOf(config.getArrivalFirstTime()));
        tfArrivalMax.setText(String.valueOf(config.getArrivalMaxEntities()));

        // Tiempos de procesamiento
        for (Map.Entry<String, TextField> entry : processingTimeFields.entrySet()) {
            entry.getValue().setText(String.valueOf(config.getProcessingTime(entry.getKey())));
        }

        // Capacidades
        for (Map.Entry<String, TextField> entry : capacityFields.entrySet()) {
            int cap = config.getLocationCapacity(entry.getKey());
            entry.getValue().setText(String.valueOf(cap == Integer.MAX_VALUE ? 999999 : cap));
        }

        // Inspección
        if (cbInspeccionExponential != null) {
            cbInspeccionExponential.setSelected(config.isInspeccionExponential());
        }
    }

    private boolean validateAndSave() {
        try {
            // Validar y guardar parámetros generales
            double simTime = Double.parseDouble(tfSimulationTime.getText());
            if (simTime <= 0) throw new IllegalArgumentException("El tiempo de simulación debe ser positivo");
            config.setSimulationTime(simTime);

            int replicas = Integer.parseInt(tfReplicas.getText());
            if (replicas < 1 || replicas > 100) throw new IllegalArgumentException("Las réplicas deben estar entre 1 y 100");
            config.setNumberOfReplicas(replicas);

            config.setWarmUpTime(Double.parseDouble(tfWarmUp.getText()));
            config.setRandomSeed(Long.parseLong(tfRandomSeed.getText()));

            // Horno
            int batchSize = Integer.parseInt(tfHornoBatchSize.getText());
            if (batchSize < 1) throw new IllegalArgumentException("El tamaño de lote debe ser al menos 1");
            config.setHornoBatchSize(batchSize);

            double hornoTime = Double.parseDouble(tfHornoProcessingTime.getText());
            if (hornoTime < 0) throw new IllegalArgumentException("El tiempo de horno no puede ser negativo");
            config.setHornoProcessingTime(hornoTime);

            // Recursos - Grúa
            int gruaQty = Integer.parseInt(tfGruaQuantity.getText());
            if (gruaQty < 1) throw new IllegalArgumentException("Debe haber al menos 1 grúa");
            config.setGruaQuantity(gruaQty);

            double gruaEmptySpeed = Double.parseDouble(tfGruaEmptySpeed.getText());
            if (gruaEmptySpeed <= 0) throw new IllegalArgumentException("La velocidad de grúa debe ser positiva");
            config.setGruaEmptySpeed(gruaEmptySpeed);

            double gruaLoadedSpeed = Double.parseDouble(tfGruaLoadedSpeed.getText());
            if (gruaLoadedSpeed <= 0) throw new IllegalArgumentException("La velocidad de grúa cargada debe ser positiva");
            config.setGruaLoadedSpeed(gruaLoadedSpeed);

            // Recursos - Robot
            int robotQty = Integer.parseInt(tfRobotQuantity.getText());
            if (robotQty < 1) throw new IllegalArgumentException("Debe haber al menos 1 robot");
            config.setRobotQuantity(robotQty);

            double robotEmptySpeed = Double.parseDouble(tfRobotEmptySpeed.getText());
            if (robotEmptySpeed <= 0) throw new IllegalArgumentException("La velocidad del robot debe ser positiva");
            config.setRobotEmptySpeed(robotEmptySpeed);

            double robotLoadedSpeed = Double.parseDouble(tfRobotLoadedSpeed.getText());
            if (robotLoadedSpeed <= 0) throw new IllegalArgumentException("La velocidad del robot cargado debe ser positiva");
            config.setRobotLoadedSpeed(robotLoadedSpeed);

            // Arribos
            config.setArrivalDistribution(cbArrivalDistribution.getValue());

            double arrivalMean = Double.parseDouble(tfArrivalMean.getText());
            if (arrivalMean <= 0) throw new IllegalArgumentException("El tiempo medio de llegada debe ser positivo");
            config.setArrivalMeanTime(arrivalMean);

            config.setArrivalFirstTime(Double.parseDouble(tfArrivalFirst.getText()));

            int maxEntities = Integer.parseInt(tfArrivalMax.getText());
            if (maxEntities < 1) throw new IllegalArgumentException("El máximo de piezas debe ser al menos 1");
            config.setArrivalMaxEntities(maxEntities);

            // Tiempos de procesamiento
            for (Map.Entry<String, TextField> entry : processingTimeFields.entrySet()) {
                double time = Double.parseDouble(entry.getValue().getText());
                if (time < 0) throw new IllegalArgumentException("Los tiempos de proceso no pueden ser negativos");
                config.setProcessingTime(entry.getKey(), time);
            }

            // Capacidades
            for (Map.Entry<String, TextField> entry : capacityFields.entrySet()) {
                int capacity = Integer.parseInt(entry.getValue().getText());
                if (capacity < 1) throw new IllegalArgumentException("Las capacidades deben ser al menos 1");
                // Convertir 999999 a MAX_VALUE para representar infinito
                config.setLocationCapacity(entry.getKey(), capacity >= 999999 ? Integer.MAX_VALUE : capacity);
            }

            // Validar que la capacidad del HORNO >= tamaño del lote
            int hornoCapacity = config.getLocationCapacity("HORNO");
            int hornoBatch = config.getHornoBatchSize();
            if (hornoCapacity != Integer.MAX_VALUE && hornoCapacity < hornoBatch) {
                throw new IllegalArgumentException(
                    "La capacidad del HORNO (" + hornoCapacity + ") debe ser >= al tamaño del lote (" + hornoBatch + ").\n" +
                    "De lo contrario, no podrán acumularse suficientes piezas para procesar.");
            }

            // Inspección
            if (cbInspeccionExponential != null) {
                config.setInspeccionExponential(cbInspeccionExponential.isSelected());
            }

            System.out.println("✅ Configuración guardada: " + config);
            return true;

        } catch (NumberFormatException e) {
            showErrorMessage("Error de Formato", "Por favor verifica que todos los valores numéricos sean válidos.");
            return false;
        } catch (IllegalArgumentException e) {
            showErrorMessage("Error de Validación", e.getMessage());
            return false;
        }
    }

    private void resetToDefaults() {
        SimulationConfig defaults = new SimulationConfig();

        tfSimulationTime.setText(String.valueOf(defaults.getSimulationTime()));
        tfReplicas.setText(String.valueOf(defaults.getNumberOfReplicas()));
        tfWarmUp.setText(String.valueOf(defaults.getWarmUpTime()));
        tfRandomSeed.setText(String.valueOf(System.currentTimeMillis()));

        tfHornoBatchSize.setText(String.valueOf(defaults.getHornoBatchSize()));
        tfHornoProcessingTime.setText(String.valueOf(defaults.getHornoProcessingTime()));

        tfGruaQuantity.setText(String.valueOf(defaults.getGruaQuantity()));
        tfGruaEmptySpeed.setText(String.valueOf(defaults.getGruaEmptySpeed()));
        tfGruaLoadedSpeed.setText(String.valueOf(defaults.getGruaLoadedSpeed()));

        tfRobotQuantity.setText(String.valueOf(defaults.getRobotQuantity()));
        tfRobotEmptySpeed.setText(String.valueOf(defaults.getRobotEmptySpeed()));
        tfRobotLoadedSpeed.setText(String.valueOf(defaults.getRobotLoadedSpeed()));

        cbArrivalDistribution.setValue(defaults.getArrivalDistribution());
        tfArrivalMean.setText(String.valueOf(defaults.getArrivalMeanTime()));
        tfArrivalFirst.setText(String.valueOf(defaults.getArrivalFirstTime()));
        tfArrivalMax.setText(String.valueOf(defaults.getArrivalMaxEntities()));

        for (Map.Entry<String, TextField> entry : processingTimeFields.entrySet()) {
            entry.getValue().setText(String.valueOf(defaults.getProcessingTime(entry.getKey())));
        }

        for (Map.Entry<String, TextField> entry : capacityFields.entrySet()) {
            int cap = defaults.getLocationCapacity(entry.getKey());
            entry.getValue().setText(String.valueOf(cap == Integer.MAX_VALUE ? 999999 : cap));
        }

        if (cbInspeccionExponential != null) {
            cbInspeccionExponential.setSelected(true);
        }

        showSuccessMessage("Valores restablecidos a los predeterminados");
    }

    private void showErrorMessage(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
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

    private Label createSectionLabel(String text) {
        Label label = new Label(text);
        label.setTextFill(Color.web("#e94560"));
        label.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        return label;
    }

    private TextField createTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setPrefWidth(200);
        tf.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-text-fill: #f1faee;" +
                        "-fx-prompt-text-fill: #888;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;" +
                        "-fx-padding: 8;");
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
        button.setOnMouseEntered(e -> button.setOpacity(0.9));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void styleApplyButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #27ae60 0%, #1e8449 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setOpacity(0.9));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void styleResetButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #f39c12 0%, #d68910 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 12px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setOpacity(0.9));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void styleSmallButton(Button button) {
        button.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #3498db 0%, #2980b9 100%);" +
                        "-fx-text-fill: white;" +
                        "-fx-font-weight: bold;" +
                        "-fx-font-size: 11px;" +
                        "-fx-padding: 5 10;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;");
        button.setOnMouseEntered(e -> button.setOpacity(0.9));
        button.setOnMouseExited(e -> button.setOpacity(1.0));
    }

    private void styleComboBox(ComboBox<?> cb) {
        cb.setPrefWidth(200);
        cb.setStyle(
                "-fx-background-color: #1a1a2e;" +
                        "-fx-border-color: #e94560;" +
                        "-fx-border-radius: 5;" +
                        "-fx-background-radius: 5;");
    }

    // ===== CLASE AUXILIAR PARA TABLA (ya no se usa, pero se mantiene por compatibilidad) =====

    public static class LocationRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty time;

        public LocationRow(String name, String time) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.time = new javafx.beans.property.SimpleStringProperty(time);
        }
    }
}
