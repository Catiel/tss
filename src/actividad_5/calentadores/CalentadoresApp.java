package actividad_5.calentadores;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Aplicación principal para la simulación de ventas de calentadores. */
public class CalentadoresApp extends JFrame {
    private final PanelCalentadoresPredefinido panelPredefinido;
    private final PanelCalentadoresAleatorio panelAleatorio;
    private final PanelCalentadoresManual panelManual;

    public CalentadoresApp(){
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        setTitle("Simulación de ventas de calentadores (inventario y demanda)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);

        panelPredefinido = new PanelCalentadoresPredefinido();
        panelAleatorio = new PanelCalentadoresAleatorio();
        panelManual = new PanelCalentadoresManual();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ejemplo 20 sem", panelPredefinido);
        tabs.addTab("Aleatoria", panelAleatorio);
        tabs.addTab("Manual", panelManual);
        add(tabs);
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new CalentadoresApp().setVisible(true)); }
}

