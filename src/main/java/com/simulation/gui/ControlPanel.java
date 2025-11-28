package com.simulation.gui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class ControlPanel {

    private final VBox panel;
    private final AnimationController animationController;

    private Button startButton;
    private Button pauseButton;
    private Button resumeButton;
    private Button stopButton;
    private Slider speedSlider;
    private Label speedLabel;

    public ControlPanel(AnimationController animationController) {
        this.animationController = animationController;
        this.panel = new VBox(10);

        setupPanel();
    }

    private void setupPanel() {
        panel.setPadding(new Insets(10));
        panel.setStyle("-fx-background-color: #e0e0e0; -fx-border-color: #333; -fx-border-width: 2;");

        // Botones de control
        HBox buttonBox = new HBox(10);
        buttonBox.setPadding(new Insets(5));

        startButton = new Button("Iniciar");
        startButton.setPrefWidth(100);
        startButton.setOnAction(e -> {
            animationController.start();
            startButton.setDisable(true);
            pauseButton.setDisable(false);
            stopButton.setDisable(false);
        });

        pauseButton = new Button("Pausar");
        pauseButton.setPrefWidth(100);
        pauseButton.setDisable(true);
        pauseButton.setOnAction(e -> {
            animationController.pause();
            pauseButton.setDisable(true);
            resumeButton.setDisable(false);
        });

        resumeButton = new Button("Reanudar");
        resumeButton.setPrefWidth(100);
        resumeButton.setDisable(true);
        resumeButton.setOnAction(e -> {
            animationController.resume();
            resumeButton.setDisable(true);
            pauseButton.setDisable(false);
        });

        stopButton = new Button("Detener");
        stopButton.setPrefWidth(100);
        stopButton.setDisable(true);
        stopButton.setOnAction(e -> {
            animationController.stop();
            startButton.setDisable(false);
            pauseButton.setDisable(true);
            resumeButton.setDisable(true);
            stopButton.setDisable(true);
        });

        buttonBox.getChildren().addAll(startButton, pauseButton, resumeButton, stopButton);

        // Control de velocidad
        HBox speedBox = new HBox(10);
        speedBox.setPadding(new Insets(5));

        Label speedTitleLabel = new Label("Velocidad de simulación:");

        speedSlider = new Slider(0.1, 10.0, 1.0);
        speedSlider.setPrefWidth(300);
        speedSlider.setShowTickLabels(true);
        speedSlider.setShowTickMarks(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setBlockIncrement(0.1);

        speedLabel = new Label("1.0x");
        speedLabel.setPrefWidth(50);

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = newVal.doubleValue();
            animationController.setSimulationSpeed(speed);
            speedLabel.setText(String.format("%.1fx", speed));
        });

        speedBox.getChildren().addAll(speedTitleLabel, speedSlider, speedLabel);

        // Información
        Label infoLabel = new Label("Controles: Iniciar para comenzar la simulación | " +
                "Pausar/Reanudar para controlar | " +
                "Velocidad para ajustar rapidez");
        infoLabel.setWrapText(true);
        infoLabel.setStyle("-fx-font-size: 10; -fx-text-fill: #666;");

        panel.getChildren().addAll(buttonBox, speedBox, infoLabel);
    }

    public VBox getPanel() {
        return panel;
    }
}
