package actividad_4.ejercicio_1;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Ventana principal de la aplicación del Ejercicio 1.
 * Permite analizar el modelo de software con diferentes paneles interactivos:
 * - Comparación de Beneficios
 * - Configuración de parámetros
 */
public class Ejercicio1App extends JFrame {
    // Mantener referencias a los paneles para evitar que se pierdan los observadores
    private final PanelModeloPrecios panelModeloPrecios;
    private final PanelConfiguracion panelConfiguracion;

    /**
     * Constructor. Inicializa la ventana principal, los paneles y la interfaz gráfica.
     * Aplica el Look & Feel FlatLaf y configura las pestañas de la aplicación.
     */
    public Ejercicio1App() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar FlatLaf, usando el Look & Feel por defecto.");
        }
        setTitle("Ejercicio 1 - Proyecto de software");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        // Creamos las instancias de los paneles una sola vez
        panelModeloPrecios = new PanelModeloPrecios();
        panelConfiguracion = new PanelConfiguracion();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Comparación de Beneficios", panelModeloPrecios);
        pestanas.addTab("Configuración", panelConfiguracion);
        add(pestanas);
    }

    /**
     * Método principal. Lanza la aplicación Swing en el hilo de eventos.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ejercicio1App app = new Ejercicio1App();
            app.setVisible(true);
        });
    }
}
