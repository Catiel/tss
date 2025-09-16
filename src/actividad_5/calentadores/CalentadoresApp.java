package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

import com.formdev.flatlaf.FlatLightLaf; // Importa el look and feel moderno FlatLaf
import javax.swing.*; // Importa todas las clases de Swing para la interfaz gráfica

/** Aplicación principal para la simulación de ventas de calentadores. */
public class CalentadoresApp extends JFrame { // Clase principal que extiende JFrame para crear una ventana
    private final PanelCalentadoresPredefinido panelPredefinido; // Panel con datos predefinidos de ejemplo
    private final PanelCalentadoresAleatorio panelAleatorio; // Panel que genera datos aleatorios
    private final PanelCalentadoresManual panelManual; // Panel para ingreso manual de datos

    public CalentadoresApp(){ // Constructor de la aplicación
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Establece el tema visual moderno, ignora errores si falla
        setTitle("Simulación de ventas de calentadores (inventario y demanda)"); // Define el título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setSize(900,600); // Establece el tamaño de la ventana en píxeles (ancho x alto)
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        panelPredefinido = new PanelCalentadoresPredefinido(); // Inicializa el panel con datos predefinidos
        panelAleatorio = new PanelCalentadoresAleatorio(); // Inicializa el panel con generación aleatoria
        panelManual = new PanelCalentadoresManual(); // Inicializa el panel de entrada manual

        JTabbedPane tabs = new JTabbedPane(); // Crea un componente de pestañas
        tabs.addTab("Ejemplo 20 sem", panelPredefinido); // Agrega pestaña con ejemplo de 20 semanas
        tabs.addTab("Aleatoria", panelAleatorio); // Agrega pestaña para simulación aleatoria
        tabs.addTab("Manual", panelManual); // Agrega pestaña para entrada manual
        add(tabs); // Añade el panel de pestañas a la ventana principal
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new CalentadoresApp().setVisible(true)); } // Método principal que inicia la aplicación en el hilo de eventos de Swing
}
