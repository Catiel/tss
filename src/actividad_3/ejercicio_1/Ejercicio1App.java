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

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Modelo de Precios", new PanelModeloPrecios());
        pestanas.addTab("Tabla Automática", new PanelTablaAuto());
        pestanas.addTab("Tabla Manual", new PanelTablaManual());
        pestanas.addTab("Tabla Aleatoria", new PanelTablaRandom());
        pestanas.addTab("Configuración", new PanelConfiguracion());
        add(pestanas);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Ejercicio1App app = new Ejercicio1App();
            app.setVisible(true);
        });
    }
}
