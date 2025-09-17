package actividad_5.sangre; // Define el paquete donde se encuentra esta clase para el módulo de simulación de sangre

import com.formdev.flatlaf.FlatLightLaf; // Importa el tema FlatLaf Light para darle una apariencia moderna a la interfaz gráfica
import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica

/** Ventana principal para la simulación de plasma/sangre (tres formas: datos proporcionados, aleatorios y manual). */ // Comentario de documentación que explica el propósito de la aplicación principal
public class SangreApp extends JFrame { // Declara la clase principal que extiende JFrame para crear una ventana de aplicación
    private final PanelSangrePredefinida panelPredefinida; // Declara el panel para la simulación con datos predefinidos del ejemplo
    private final PanelSangreAleatoria panelAleatoria; // Declara el panel para la simulación con números aleatorios generados automáticamente
    private final PanelSangreManual panelManual; // Declara el panel para la simulación con entrada manual de números aleatorios

    public SangreApp(){ // Constructor de la aplicación principal
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Intenta establecer el tema FlatLaf Light, si falla ignora la excepción y usa el tema por defecto
        setTitle("Simulación de Plasma - Suministro vs Demanda"); // Establece el título de la ventana principal
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura la operación de cierre para terminar la aplicación al cerrar la ventana
        setSize(1200,650); // Establece el tamaño inicial de la ventana (1200 píxeles de ancho, 650 de alto)
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        panelPredefinida = new PanelSangrePredefinida(); // Crea una instancia del panel de simulación predefinida
        panelAleatoria = new PanelSangreAleatoria(); // Crea una instancia del panel de simulación aleatoria
        panelManual = new PanelSangreManual(); // Crea una instancia del panel de simulación manual

        JTabbedPane tabs = new JTabbedPane(); // Crea un componente de pestañas para organizar los diferentes tipos de simulación
        tabs.addTab("Ejemplo", panelPredefinida); // Agrega la primera pestaña con el panel predefinido y la etiqueta "Ejemplo"
        tabs.addTab("Aleatoria", panelAleatoria); // Agrega la segunda pestaña con el panel aleatorio y la etiqueta "Aleatoria"
        tabs.addTab("Manual", panelManual); // Agrega la tercera pestaña con el panel manual y la etiqueta "Manual"
        add(tabs); // Agrega el componente de pestañas a la ventana principal
    }

    public static void main(String[] args){ // Método principal que sirve como punto de entrada de la aplicación
        SwingUtilities.invokeLater(() -> new SangreApp().setVisible(true)); // Ejecuta la creación de la ventana en el hilo de eventos de Swing y la hace visible
    }
}
