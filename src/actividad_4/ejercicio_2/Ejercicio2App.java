package actividad_4.ejercicio_2;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

/**
 * Ventana principal de la aplicación del Ejercicio 2.
 * Pestañas:
 *  - Payback Detalle
 *  - Payback Sensibilidad 2D
 *  - Payback 2D Editable (tabla manual editable)
 *  - Configuración
 */
public class Ejercicio2App extends JFrame {
    private final PanelConfiguracion panelConfiguracion;
    private final PanelPaybackDetalle panelPaybackDetalle;
    private final PanelPaybackSensibilidad2D panelPayback2D;
    private final PanelPaybackSensibilidad2DManual panelPayback2DManual;

    public Ejercicio2App() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ignored) {
        }
        setTitle("Ejercicio 2 - Payback y VAN");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1100, 750);
        setLocationRelativeTo(null);

        panelConfiguracion = new PanelConfiguracion();
        panelPaybackDetalle = new PanelPaybackDetalle();
        panelPayback2D = new PanelPaybackSensibilidad2D();
        panelPayback2DManual = new PanelPaybackSensibilidad2DManual();

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Payback Detalle", panelPaybackDetalle);
        pestanas.addTab("Payback Sensibilidad 2D", panelPayback2D);
        pestanas.addTab("Payback 2D Editable", panelPayback2DManual);
        pestanas.addTab("Configuración", panelConfiguracion);
        add(pestanas);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Ejercicio2App().setVisible(true));
    }
}
