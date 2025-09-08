package actividad_4.ejercicio_2; // Paquete del ejercicio 2 actividad 4

import com.formdev.flatlaf.FlatLightLaf; // Look & Feel plano (FlatLaf)

import javax.swing.*; // Componentes Swing

/**
 * Ventana principal de la aplicación del Ejercicio 2.
 * Contiene pestañas para cada vista relacionada al análisis de payback.
 */
public class Ejercicio2App extends JFrame { // Clase principal que extiende JFrame
    private final PanelConfiguracion panelConfiguracion;        // Pestaña de configuración de parámetros globales
    private final PanelPaybackDetalle panelPaybackDetalle;      // Pestaña detalle anual de payback
    private final PanelPaybackSensibilidad2D panelPayback2D;    // Pestaña tabla fija sensibilidad 2D
    private final PanelPaybackSensibilidad2DManual panelPayback2DManual; // Pestaña editable manual
    private final PanelPaybackSensibilidad2DRandom panelPayback2DRandom; // Pestaña con generación aleatoria

    public Ejercicio2App() { // Constructor: arma la ventana
        try { // Intenta aplicar Look & Feel FlatLaf
            UIManager.setLookAndFeel(new FlatLightLaf()); // Setea L&F moderno
        } catch (Exception ignored) { // Ignora cualquier error (no crítico)
        }
        setTitle("Ejercicio 2 - Payback y VAN"); // Título de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Cierra aplicación al cerrar ventana
        setSize(1100, 750); // Dimensiones iniciales
        setLocationRelativeTo(null); // Centrar en pantalla

        panelConfiguracion = new PanelConfiguracion(); // Instancia panel configuración
        panelPaybackDetalle = new PanelPaybackDetalle(); // Instancia panel detalle
        panelPayback2D = new PanelPaybackSensibilidad2D(); // Instancia panel sensibilidad fija
        panelPayback2DManual = new PanelPaybackSensibilidad2DManual(); // Instancia panel editable
        panelPayback2DRandom = new PanelPaybackSensibilidad2DRandom(); // Instancia panel aleatorio

        JTabbedPane pestanas = new JTabbedPane(); // Contenedor de pestañas
        pestanas.addTab("Payback Detalle", panelPaybackDetalle); // Agrega pestaña detalle
        pestanas.addTab("Payback Sensibilidad 2D", panelPayback2D); // Agrega pestaña sensibilidad fija
        pestanas.addTab("Payback 2D Editable", panelPayback2DManual); // Agrega pestaña editable manual
        pestanas.addTab("Payback 2D Random", panelPayback2DRandom); // Agrega pestaña aleatoria
        pestanas.addTab("Configuración", panelConfiguracion); // Agrega pestaña configuración
        add(pestanas); // Añade el componente de pestañas al frame
    }

    public static void main(String[] args) { // Punto de entrada de la aplicación
        SwingUtilities.invokeLater(() -> new Ejercicio2App().setVisible(true)); // Crea e inicia GUI en EDT
    }
}
