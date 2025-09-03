package actividad_3.ejercicio_1;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import java.awt.*;

public class Ejercicio1App extends JFrame {
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

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Modelo de Precios", panelModeloPrecios);
        pestanas.addTab("Tabla Automática", panelTablaAuto);
        pestanas.addTab("Tabla Manual", panelTablaManual);
        pestanas.addTab("Tabla Aleatoria", panelTablaRandom);
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ejercicio1App app = new Ejercicio1App();
            app.setVisible(true);
        });
    }
}
