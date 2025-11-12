package com.simulation.gui;

import com.simulation.config.SimulationParameters;
import com.simulation.core.DigemicEngine;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación principal para el sistema DIGEMIC (expedición de pasaportes)
 */
public class DigemicMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(DigemicMain.class.getResource("/fxml/main-view.fxml"));
            BorderPane rootLayout = loader.load();

            Scene scene = new Scene(rootLayout, 1400, 900);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

            primaryStage.setTitle("DIGEMIC - Sistema de Expedición de Pasaportes");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1200);
            primaryStage.setMinHeight(800);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error al cargar la interfaz: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
