package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import javafx.application.Application; // Importa la clase Application de JavaFX que es la clase base para todas las aplicaciones JavaFX
import javafx.fxml.FXMLLoader; // Importa la clase FXMLLoader de JavaFX para cargar archivos FXML que definen la interfaz gráfica
import javafx.scene.Scene; // Importa la clase Scene de JavaFX que representa el contenedor de todos los elementos visuales
import javafx.scene.image.Image; // Importa la clase Image de JavaFX para cargar y manejar imágenes
import javafx.stage.Stage; // Importa la clase Stage de JavaFX que representa la ventana principal de la aplicación

import java.io.IOException; // Importa la clase IOException para manejar excepciones de entrada/salida

/** // Inicio del comentario Javadoc de la clase
 * Aplicación principal de la simulación de línea de producción // Descripción de la aplicación
 * Implementación en Java del modelo ProModel // Nota sobre la implementación
 */ // Fin del comentario Javadoc
public class MainApplication extends Application { // Declaración de la clase pública MainApplication que extiende Application para crear una aplicación JavaFX

    @Override // Anotación que indica que este método sobrescribe el método start de la clase Application
    public void start(Stage primaryStage) throws IOException { // Método público start que es el punto de entrada de la aplicación JavaFX recibiendo el Stage principal y lanzando IOException
        try { // Bloque try para capturar y manejar excepciones durante la inicialización de la aplicación
            FXMLLoader fxmlLoader = new FXMLLoader( // Crea una nueva instancia de FXMLLoader para cargar el archivo FXML
                MainApplication.class.getResource("/fxml/main-view.fxml") // Obtiene la URL del archivo FXML main-view.fxml desde la carpeta resources/fxml
            ); // Cierre del paréntesis del constructor FXMLLoader

            Scene scene = new Scene(fxmlLoader.load(), 1200, 800); // Crea una nueva escena cargando el contenido del FXML con ancho 1200 y altura 800 píxeles

            // Aplicar CSS si existe
            try { // Bloque try interno para intentar cargar la hoja de estilos CSS sin interrumpir la aplicación si falla
                var cssUrl = MainApplication.class.getResource("/styles.css"); // Obtiene la URL del archivo CSS styles.css desde la carpeta resources usando var para inferencia de tipos
                if (cssUrl != null) { // Condición que verifica si la URL del CSS fue encontrada (no es null)
                    scene.getStylesheets().add(cssUrl.toExternalForm()); // Agrega la hoja de estilos CSS a la escena convirtiendo la URL a formato externo
                } // Cierre del bloque condicional if
            } catch (Exception e) { // Captura cualquier excepción que ocurra al cargar el CSS
                // No hay CSS, continuar sin él
            } // Cierre del bloque catch interno

            primaryStage.setTitle("Simulación de Línea de Producción - ProModel Java Implementation"); // Establece el título de la ventana principal de la aplicación
            primaryStage.setScene(scene); // Asigna la escena creada al stage principal para mostrar la interfaz
            primaryStage.setMinWidth(1000); // Establece el ancho mínimo de la ventana en 1000 píxeles
            primaryStage.setMinHeight(700); // Establece la altura mínima de la ventana en 700 píxeles

            // Agregar icono si existe
            try { // Bloque try interno para intentar cargar el icono de la aplicación sin interrumpir si falla
                Image icon = new Image(MainApplication.class.getResourceAsStream("/images/icon.png")); // Crea una nueva imagen cargando el archivo icon.png desde la carpeta resources/images usando un stream
                primaryStage.getIcons().add(icon); // Agrega el icono cargado a la lista de iconos del stage principal
            } catch (Exception e) { // Captura cualquier excepción que ocurra al cargar el icono
                // No hay icono, continuar sin él
            } // Cierre del bloque catch interno

            primaryStage.show(); // Muestra la ventana principal haciendo visible la aplicación al usuario

        } catch (Exception e) { // Captura cualquier excepción general que ocurra durante la inicialización de la aplicación
            e.printStackTrace(); // Imprime la traza completa de la excepción en la consola para propósitos de depuración
            System.err.println("Error al cargar la aplicación: " + e.getMessage()); // Imprime un mensaje de error en el flujo de error estándar con el mensaje de la excepción
            throw e; // Relanza la excepción para que sea manejada por el sistema JavaFX
        } // Cierre del bloque catch externo
    } // Cierre del método start

    @Override // Anotación que indica que este método sobrescribe el método stop de la clase Application
    public void stop() { // Método público stop que se llama cuando la aplicación se está cerrando sin recibir parámetros
        System.out.println("Cerrando aplicación..."); // Imprime un mensaje en la consola indicando que la aplicación se está cerrando
        // Aquí podrías agregar limpieza de recursos si fuera necesario
    } // Cierre del método stop

    public static void main(String[] args) { // Método estático main que es el punto de entrada de la aplicación Java recibiendo los argumentos de línea de comandos como parámetro
        launch(args); // Llama al método launch heredado de Application para iniciar la aplicación JavaFX pasando los argumentos recibidos
    } // Cierre del método main
} // Cierre de la clase MainApplication
