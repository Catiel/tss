package actividad_6.ejercicio1;

import javax.swing.*;

public class simulacionEjercicio1 extends JFrame {

    public simulacionEjercicio1() {
        setTitle("Simulación de Inspección - Aplicación Completa");
        setSize(1500, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear instancias de los paneles existentes (sin repetir código)
        ejercicio1 panelPredefinido = new ejercicio1();
        ejercicio1_aleatorio panelAleatorio = new ejercicio1_aleatorio();
        ejercicio1_manual panelManual = new ejercicio1_manual();

        // Extraer el contenido de cada ventana y agregarlo como pestaña
        tabbedPane.addTab("Valores Predefinidos", panelPredefinido.getContentPane());
        tabbedPane.addTab("Valores Aleatorios", panelAleatorio.getContentPane());
        tabbedPane.addTab("Ingreso Manual", panelManual.getContentPane());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new simulacionEjercicio1().setVisible(true));
    }
}
