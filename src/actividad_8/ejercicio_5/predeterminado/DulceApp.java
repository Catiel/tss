package actividad_8.ejercicio_5.predeterminado; // Define el paquete donde se encuentra esta clase

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/** Aplicación principal para la simulación de Dulce Ada (predefinido, aleatorio, manual). */
public class DulceApp extends JFrame { // Clase principal que extiende JFrame para crear una ventana de aplicación de simulación de maximización de ganancias de Dulce Ada
    public DulceApp(){ // Constructor de la aplicación de simulación de Dulce Ada
        try{ UIManager.setLookAndFeel(new FlatLightLaf()); }catch(Exception ignored){} // Establece el tema visual moderno, ignora errores si falla
        setTitle("Simulación Dulce Ada - Maximización de Ganancias"); // Define el título de la ventana indicando que simula maximización de ganancias para Dulce Ada
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setSize(1150,650); // Establece el tamaño de la ventana en píxeles (ancho x alto) - más ancho que las aplicaciones anteriores para acomodar más datos de ganancias
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JTabbedPane tabs = new JTabbedPane(); // Crea un componente de pestañas para organizar los diferentes tipos de simulación
        tabs.addTab("Predefinido (100 r)", new PanelDulcePredefinido()); // Agrega pestaña con ejemplo predefinido usando 100 números aleatorios específicos
        tabs.addTab("Aleatorio", new PanelDulceAleatorio()); // Agrega pestaña para simulación aleatoria de maximización de ganancias
        tabs.addTab("Manual", new PanelDulceManual()); // Agrega pestaña para simulación manual donde el usuario ingresa números aleatorios
        add(tabs); // Añade el panel de pestañas a la ventana principal
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new DulceApp().setVisible(true)); } // Método principal que inicia la aplicación en el hilo de eventos de Swing
}
