package com.simulation.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Aplicación principal de la simulación de línea de producción
 * Implementación en Java del modelo ProModel
 */
public class MainApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws IOException {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(
                MainApplication.class.getResource("/fxml/main-view.fxml")
            );

            Scene scene = new Scene(fxmlLoader.load(), 1200, 800);

            // Aplicar CSS si existe
            try {
                var cssUrl = MainApplication.class.getResource("/styles.css");
                if (cssUrl != null) {
                    scene.getStylesheets().add(cssUrl.toExternalForm());
                }
            } catch (Exception e) {
                // No hay CSS, continuar sin él
            }

            primaryStage.setTitle("Simulación de Línea de Producción - ProModel Java Implementation");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // Agregar icono si existe
            try {
                Image icon = new Image(MainApplication.class.getResourceAsStream("/images/icon.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                // No hay icono, continuar sin él
            }

            primaryStage.show();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar la aplicación: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void stop() {
        System.out.println("Cerrando aplicación...");
        // Aquí podrías agregar limpieza de recursos si fuera necesario
    }

    public static void main(String[] args) {
        launch(args);
    }
}
