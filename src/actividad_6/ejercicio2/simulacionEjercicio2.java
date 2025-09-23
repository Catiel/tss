package actividad_6.ejercicio2;

import javax.swing.*;

public class simulacionEjercicio2 extends JFrame {

    public simulacionEjercicio2() {
        setTitle("Simulación de Inspección - Ejercicio 2 - Aplicación Completa");
        setSize(1500, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear instancias de los paneles existentes (sin repetir código)
        ejercicio2 panelPredefinido = new ejercicio2();
        ejercicio2Aleatorio panelAleatorio = new ejercicio2Aleatorio();
        ejercicio2Manual panelManual = new ejercicio2Manual();

        // Extraer el contenido de cada ventana y agregarlo como pestaña
        tabbedPane.addTab("Valores Predefinidos", panelPredefinido.getContentPane());
        tabbedPane.addTab("Valores Aleatorios", panelAleatorio.getContentPane());
        tabbedPane.addTab("Ingreso Manual", panelManual.getContentPane());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new simulacionEjercicio2().setVisible(true));
    }
}
