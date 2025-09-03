package actividad_3.ejercicio_2;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Ventana principal de la aplicación del Ejercicio 2.
 * Permite analizar el modelo de Wozac con diferentes paneles interactivos:
 * - Modelo de precios y utilidades
 * - Tabla automática
 * - Tabla manual
 * - Tabla aleatoria
 * - Configuración de parámetros
 */
public class Ejercicio2App extends JFrame {
    /**
     * Constructor. Inicializa la ventana principal, los paneles y la interfaz gráfica.
     * Aplica el Look & Feel FlatLaf y configura las pestañas de la aplicación.
     */
    public Ejercicio2App() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            System.out.println("No se pudo aplicar FlatLaf, usando el Look & Feel por defecto.");
        }
        setTitle("Ejercicio 2 - Modelo para Wozac");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Modelo de Precios", new PanelModeloPrecios());
        pestanas.addTab("Tabla Automática", new PanelTablaAuto());
        pestanas.addTab("Tabla Manual", new PanelTablaManual());
        pestanas.addTab("Tabla Aleatoria", new PanelTablaRandom());
        pestanas.addTab("Configuración", new PanelConfiguracion());
        add(pestanas);
    }

    /**
     * Método principal. Lanza la aplicación Swing en el hilo de eventos.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ejercicio2App app = new Ejercicio2App();
            app.setVisible(true);
        });
    }
}
