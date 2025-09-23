package actividad_6.ejercicio3;

import actividad_6.ejercicio3.ejercicio3;
import actividad_6.ejercicio3.ejercicio3Random;
import actividad_6.ejercicio3.ejercicio3Manual;

import javax.swing.*;

public class simulacionEjercicio3 extends JFrame {

    public simulacionEjercicio3() {
        setTitle("Simulación de Inspección - Ejercicio 2 - Aplicación Completa");
        setSize(1500, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear instancias de los paneles existentes (sin repetir código)
        ejercicio3 panelPredefinido = new ejercicio3();
        ejercicio3Random panelAleatorio = new ejercicio3Random();
        ejercicio3Manual panelManual = new ejercicio3Manual();

        // Extraer el contenido de cada ventana y agregarlo como pestaña
        tabbedPane.addTab("Valores Predefinidos", panelPredefinido.getContentPane());
        tabbedPane.addTab("Valores Aleatorios", panelAleatorio.getContentPane());
        tabbedPane.addTab("Ingreso Manual", panelManual.getContentPane());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new actividad_6.ejercicio3.simulacionEjercicio3().setVisible(true));
    }
}

