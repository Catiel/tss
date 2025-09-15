package actividad_5.urnas; // Paquete donde se ubica la aplicación principal de urnas

import com.formdev.flatlaf.FlatLightLaf; // Importa el Look & Feel FlatLightLaf
import javax.swing.*; // Importa clases Swing (JFrame, JTabbedPane, UIManager, SwingUtilities)

/** Ventana principal para la simulación de la urna con tres formas de ingresar números pseudoaleatorios. */ // Descripción de la clase
public class UrnasApp extends JFrame { // Declaración de la clase UrnasApp que extiende JFrame
    private final PanelUrnaPredefinida panelPredefinido;   // Referencia al panel con números proporcionados
    private final PanelUrnaAleatoria panelAleatoria;       // Referencia al panel que genera números aleatorios
    private final PanelUrnaPredefinidaManual panelManual;  // Referencia al panel de ingreso manual

    public UrnasApp(){ // Constructor de la ventana principal
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Intenta aplicar Look & Feel FlatLaf, ignora errores
        setTitle("Simulación de Urna - 10% Verdes, 40% Rojas, 50% Amarillas"); // Establece el título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Indica que al cerrar se termine la aplicación
        setSize(900,600); // Define tamaño inicial del frame (ancho 900, alto 600)
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        panelPredefinido = new PanelUrnaPredefinida(); // Inicializa panel de números proporcionados
        panelAleatoria = new PanelUrnaAleatoria(); // Inicializa panel de números aleatorios
        panelManual = new PanelUrnaPredefinidaManual(); // Inicializa panel de ingreso manual

        JTabbedPane tabs = new JTabbedPane(); // Crea contenedor de pestañas
        tabs.addTab("Proporcionados", panelPredefinido); // Agrega pestaña con panel predefinido
        tabs.addTab("Aleatorios", panelAleatoria); // Agrega pestaña con panel aleatorio
        tabs.addTab("Manual", panelManual); // Agrega pestaña con panel manual
        add(tabs); // Añade el contenedor de pestañas al frame
    } // Fin constructor UrnasApp

    public static void main(String[] args){ // Método principal de la aplicación
        SwingUtilities.invokeLater(() -> new UrnasApp().setVisible(true)); // Lanza la creación y visualización del frame en el hilo de eventos
    } // Fin main
} // Fin clase UrnasApp
