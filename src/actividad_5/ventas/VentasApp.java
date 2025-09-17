package actividad_5.ventas; // Define el paquete donde se encuentra esta clase para el módulo de simulación de ventas

import com.formdev.flatlaf.FlatLightLaf; // Importa el tema FlatLaf Light para darle una apariencia moderna a la interfaz gráfica
import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica

/** Aplicación principal para simulación de ventas con tres pestañas (predefinido, aleatorio, manual). */ // Comentario de documentación que explica el propósito de la aplicación principal
public class VentasApp extends JFrame { // Declara la clase principal que extiende JFrame para crear una ventana de aplicación
    public VentasApp(){ // Constructor de la aplicación principal
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Intenta establecer el tema FlatLaf Light, si falla ignora la excepción y usa el tema por defecto
        setTitle("Simulación de Ventas de Programas"); // Establece el título de la ventana principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura la operación de cierre para terminar la aplicación al cerrar la ventana
        setSize(850,600); // Establece el tamaño inicial de la ventana (850 píxeles de ancho, 600 de alto)
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JTabbedPane tabs = new JTabbedPane(); // Crea un componente de pestañas para organizar los diferentes tipos de simulación de ventas
        tabs.addTab("Ejemplo", new PanelVentasPredefinido()); // Agrega la primera pestaña con el panel predefinido y la etiqueta "Ejemplo"
        tabs.addTab("Aleatoria", new PanelVentasAleatorio()); // Agrega la segunda pestaña con el panel aleatorio y la etiqueta "Aleatoria"
        tabs.addTab("Manual", new PanelVentasManual()); // Agrega la tercera pestaña con el panel manual y la etiqueta "Manual"
        add(tabs); // Agrega el componente de pestañas a la ventana principal
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new VentasApp().setVisible(true)); } // Método principal que sirve como punto de entrada de la aplicación y ejecuta la creación de la ventana en el hilo de eventos de Swing haciendo la aplicación visible
}
