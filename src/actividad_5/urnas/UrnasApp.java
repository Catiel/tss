package actividad_5.urnas;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

/** Ventana principal para la simulación de la urna con tres formas de ingresar números pseudoaleatorios. */
public class UrnasApp extends JFrame {
    private final PanelUrnaPredefinida panelPredefinido;   // Números dados por la práctica
    private final PanelUrnaAleatoria panelAleatoria;       // Números generados por el programa
    private final PanelUrnaPredefinidaManual panelManual;  // Números ingresados manualmente

    public UrnasApp(){
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } catch (Exception ignored) {}
        setTitle("Simulación de Urna - 10% Verdes, 40% Rojas, 50% Amarillas");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900,600);
        setLocationRelativeTo(null);

        panelPredefinido = new PanelUrnaPredefinida();
        panelAleatoria = new PanelUrnaAleatoria();
        panelManual = new PanelUrnaPredefinidaManual();

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Proporcionados", panelPredefinido);
        tabs.addTab("Aleatorios", panelAleatoria);
        tabs.addTab("Manual", panelManual);
        add(tabs);
    }

    public static void main(String[] args){
        SwingUtilities.invokeLater(() -> new UrnasApp().setVisible(true));
    }
}

