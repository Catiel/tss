package actividad_3.ejercicio_1;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

/**
 * Ventana principal de la aplicación del Ejercicio 1.
 * Permite analizar el modelo de ganancias con diferentes paneles interactivos:
 * - Modelo de precios
 * - Tabla automática
 * - Tabla manual
 * - Tabla aleatoria
 * - Sensibilidad al tipo de cambio
 * - Configuración de parámetros
 * Incluye un listener para actualizar la tabla automática al cambiar de pestaña.
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
        setTitle("Ejercicio 1 - Modelo de Ganancias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);

        PanelModeloPrecios panelModeloPrecios = new PanelModeloPrecios();
        PanelTablaAuto panelTablaAuto = new PanelTablaAuto();
        PanelTablaManual panelTablaManual = new PanelTablaManual();
        PanelTablaRandom panelTablaRandom = new PanelTablaRandom();
        PanelConfiguracion panelConfiguracion = new PanelConfiguracion();
        PanelSensibilidadTipoCambio panelSensibilidadTipoCambio = new PanelSensibilidadTipoCambio();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Modelo de Precios", panelModeloPrecios);
        pestanas.addTab("Tabla Automática", panelTablaAuto);
        pestanas.addTab("Tabla Manual", panelTablaManual);
        pestanas.addTab("Tabla Aleatoria", panelTablaRandom);
        pestanas.addTab("Sensibilidad Tipo de Cambio", panelSensibilidadTipoCambio);
        pestanas.addTab("Configuración", panelConfiguracion);
        add(pestanas);

        // Listener para actualizar la tabla automática al cambiar de pestaña
        pestanas.addChangeListener(e -> {
            int idx = pestanas.getSelectedIndex();
            String title = pestanas.getTitleAt(idx);
            if ("Tabla Automática".equals(title)) {
                panelTablaAuto.generarTabla();
            }
        });
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
