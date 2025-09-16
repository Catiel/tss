package actividad_5.dulce;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Aplicación principal para la simulación de Dulce Ada (predefinido, aleatorio, manual). */
public class DulceApp extends JFrame {
    public DulceApp(){
        try{ UIManager.setLookAndFeel(new FlatLightLaf()); }catch(Exception ignored){}
        setTitle("Simulación Dulce Ada - Maximización de Ganancias");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1150,650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Predefinido (100 r)", new PanelDulcePredefinido());
        tabs.addTab("Aleatorio", new PanelDulceAleatorio());
        tabs.addTab("Manual", new PanelDulceManual());
        add(tabs);
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new DulceApp().setVisible(true)); }
}

