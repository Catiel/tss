package com.simulation.gui; // Declaración del paquete de la interfaz gráfica de usuario

import com.simulation.config.SimulationParameters; // Importa la clase de parámetros de configuración del sistema
import com.simulation.core.DigemicEngine; // Importa el motor de simulación DIGEMIC
import javafx.application.Application; // Importa la clase base Application de JavaFX para crear aplicaciones gráficas
import javafx.fxml.FXMLLoader; // Importa FXMLLoader para cargar archivos FXML que definen la interfaz
import javafx.scene.Scene; // Importa Scene que representa el contenedor principal de elementos visuales
import javafx.scene.layout.BorderPane; // Importa BorderPane como layout principal con 5 regiones (top, bottom, left, right, center)
import javafx.stage.Stage; // Importa Stage que representa la ventana principal de la aplicación

import java.io.IOException; // Importa IOException para manejar errores de entrada/salida al cargar archivos

/**
 * Aplicación principal para el sistema DIGEMIC (expedición de pasaportes)
 */
public class DigemicMain extends Application { // Declaración de la clase principal que extiende Application de JavaFX

    @Override // Anotación que indica sobrescritura del método abstracto start de Application
    public void start(Stage primaryStage) { // Método principal que inicia la interfaz gráfica recibiendo el Stage principal
        try { // Bloque try para capturar posibles errores al cargar la interfaz
            FXMLLoader loader = new FXMLLoader(); // Crea una nueva instancia del cargador de archivos FXML
            loader.setLocation(DigemicMain.class.getResource("/fxml/main-view.fxml")); // Establece la ubicación del archivo FXML en la carpeta resources
            BorderPane rootLayout = loader.load(); // Carga el archivo FXML y lo convierte en un BorderPane como layout raíz

            Scene scene = new Scene(rootLayout, 1400, 900); // Crea una nueva escena con el layout cargado y dimensiones de 1400x900 píxeles
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm()); // Agrega la hoja de estilos CSS a la escena para personalizar la apariencia

            primaryStage.setTitle("DIGEMIC - Sistema de Expedición de Pasaportes"); // Establece el título de la ventana principal
            primaryStage.setScene(scene); // Asigna la escena creada al stage principal
            primaryStage.setMinWidth(1200); // Establece el ancho mínimo de la ventana en 1200 píxeles
            primaryStage.setMinHeight(800); // Establece el alto mínimo de la ventana en 800 píxeles
            primaryStage.show(); // Hace visible la ventana principal y muestra la interfaz al usuario

        } catch (IOException e) { // Captura excepciones de entrada/salida si falla la carga del FXML
            e.printStackTrace(); // Imprime el stack trace completo del error en la consola
            System.err.println("Error al cargar la interfaz: " + e.getMessage()); // Imprime mensaje de error descriptivo en el stream de error estándar
        }
    }

    public static void main(String[] args) { // Método main que sirve como punto de entrada de la aplicación Java
        launch(args); // Llama al método launch de Application que inicia el ciclo de vida de JavaFX y ejecuta start()
    }
}
