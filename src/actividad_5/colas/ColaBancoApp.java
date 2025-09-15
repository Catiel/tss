package actividad_5.colas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Aplicación principal para la simulación de la cola del banco (ventanilla auto). */
public class ColaBancoApp extends JFrame {
    public ColaBancoApp(){
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        setTitle("Simulación Cola Banco - Tiempo de Espera");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1050,650);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Ejemplo", new PanelColaPredefinida());
        tabs.addTab("Aleatoria", new PanelColaAleatoria());
        tabs.addTab("Manual", new PanelColaManual());
        add(tabs);
    }

    public static void main(String[] args){ SwingUtilities.invokeLater(() -> new ColaBancoApp().setVisible(true)); }
}

