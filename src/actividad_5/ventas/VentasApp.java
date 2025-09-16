package actividad_5.ventas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Aplicación principal para simulación de ventas con tres pestañas (predefinido, aleatorio, manual). */
public class VentasApp extends JFrame {
    public VentasApp(){
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        setTitle("Simulación de Ventas de Programas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(850,600);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ejemplo", new PanelVentasPredefinido());
        tabs.addTab("Aleatoria", new PanelVentasAleatorio());
        tabs.addTab("Manual", new PanelVentasManual());
        add(tabs);
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new VentasApp().setVisible(true)); }
}

