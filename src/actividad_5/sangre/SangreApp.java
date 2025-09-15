package actividad_5.sangre;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Ventana principal para la simulación de plasma/sangre (tres formas: datos proporcionados, aleatorios y manual). */
public class SangreApp extends JFrame {
    private final PanelSangrePredefinida panelPredefinida;
    private final PanelSangreAleatoria panelAleatoria;
    private final PanelSangreManual panelManual;

    public SangreApp(){
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        setTitle("Simulación de Plasma - Suministro vs Demanda");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200,650);
        setLocationRelativeTo(null);

        panelPredefinida = new PanelSangrePredefinida();
        panelAleatoria = new PanelSangreAleatoria();
        panelManual = new PanelSangreManual();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ejemplo", panelPredefinida);
        tabs.addTab("Aleatoria", panelAleatoria);
        tabs.addTab("Manual", panelManual);
        add(tabs);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new SangreApp().setVisible(true));
    }
}

