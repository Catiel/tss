package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class SimulationGUI extends Application {

    private SimulationEngine engine;
    private AnimationController animationController;
    private ControlPanel controlPanel;
    private StatisticsPanel statisticsPanel;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Simulación de Producción de Cerveza - ProModel Java");

        // Crear el layout principal
        BorderPane root = new BorderPane();

        // Crear el controlador de animación (centro)
        animationController = new AnimationController(1200, 700);
        root.setCenter(animationController.getCanvas());

        // Crear panel de control (abajo)
        controlPanel = new ControlPanel(animationController);
        root.setBottom(controlPanel.getPanel());

        // Crear panel de estadísticas (derecha)
        statisticsPanel = new StatisticsPanel();
        root.setRight(statisticsPanel.getPanel());

        // Crear escena
        Scene scene = new Scene(root, 1600, 800);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Configurar cierre
        primaryStage.setOnCloseRequest(event -> {
            animationController.stop();
            Platform.exit();
            System.exit(0);
        });
    }
}

