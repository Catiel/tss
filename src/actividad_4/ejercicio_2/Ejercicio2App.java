package actividad_4.ejercicio_2;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Ventana principal de la aplicación del Ejercicio 2.
 * Permite analizar el modelo de software con diferentes paneles interactivos:
 * - Payback Detalle
 * - Payback Sensibilidad 2D
 * - Comparación de Beneficios
 * - Tabla Automática (con valores predefinidos)
 * - Tabla Manual (con valores ingresados por el usuario)
 * - Tabla Random (con valores generados aleatoriamente)
 * - Configuración de parámetros
 */
public class Ejercicio2App extends JFrame {
    // Mantener referencias a los paneles para evitar que se pierdan los observadores
    private final PanelConfiguracion panelConfiguracion;
    private final PanelPaybackDetalle panelPaybackDetalle; // nuevo
    private final PanelPaybackSensibilidad2D panelPayback2D; // nuevo

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
        setTitle("Ejercicio 2 - Payback y VAN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        // Creamos las instancias de los paneles una sola vez
        panelConfiguracion = new PanelConfiguracion();
        panelPaybackDetalle = new PanelPaybackDetalle();
        panelPayback2D = new PanelPaybackSensibilidad2D();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Payback Detalle", panelPaybackDetalle);
        pestanas.addTab("Payback Sensibilidad 2D", panelPayback2D);
        pestanas.addTab("Configuración", panelConfiguracion);
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
