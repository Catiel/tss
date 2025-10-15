package actividad_9.ejercicio_2; // Define el paquete donde se encuentra esta clase

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal con pestañas para las tres simulaciones completas: Aleatorio, Manual y Predeterminado.
 */
public class SimulacionCompletaTabbedApp extends JFrame { // Clase principal que extiende JFrame
    public SimulacionCompletaTabbedApp() { // Constructor de la ventana principal
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {} // Aplica el tema visual FlatLaf
        setTitle("Simulación Completa - Todas las modalidades"); // Establece el título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra la aplicación al cerrar la ventana
        setSize(1400, 1000); // Define el tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JTabbedPane tabs = new JTabbedPane(); // Crea el componente de pestañas

        // Instanciar cada simulación usando el nombre completo del paquete para evitar conflictos
        OilReservesSimulatorAleatorio simAleatorio = new OilReservesSimulatorAleatorio(); // Instancia la simulación aleatoria
        OilReservesSimulatorEditable simManual = new OilReservesSimulatorEditable(); // Instancia la simulación manual
        OilReservesSimulator simPredeterminado = new OilReservesSimulator(); // Instancia la simulación predeterminada

        tabs.addTab("Predeterminado", wrapInPanel(simPredeterminado)); // Agrega la pestaña de simulación predeterminada
        tabs.addTab("Aleatorio", wrapInPanel(simAleatorio)); // Agrega la pestaña de simulación aleatoria
        tabs.addTab("Manual", wrapInPanel(simManual)); // Agrega la pestaña de simulación manual

        add(tabs, BorderLayout.CENTER); // Añade el JTabbedPane al centro del JFrame
    }

    // Método utilitario para extraer el contentPane de un JFrame y ponerlo en un JPanel
    private JPanel wrapInPanel(JFrame frame) { // Recibe un JFrame y devuelve su contentPane en un JPanel
        JPanel panel = new JPanel(new BorderLayout()); // Crea un nuevo JPanel con BorderLayout
        panel.add(frame.getContentPane(), BorderLayout.CENTER); // Añade el contentPane del JFrame al panel
        return panel; // Devuelve el panel resultante
    }

    public static void main(String[] args) { // Método principal de la aplicación
        SwingUtilities.invokeLater(() -> new SimulacionCompletaTabbedApp().setVisible(true)); // Lanza la ventana en el hilo de eventos de Swing
    }
}
