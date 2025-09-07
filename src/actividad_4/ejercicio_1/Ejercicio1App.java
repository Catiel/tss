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

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Comparación de Beneficios", new PanelModeloPrecios());
        pestanas.addTab("Configuración", new PanelConfiguracion());
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
