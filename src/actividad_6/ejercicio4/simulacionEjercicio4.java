package actividad_6.ejercicio4;

import actividad_6.ejercicio4.ejercicio4;
import actividad_6.ejercicio4.ejercicio4_manual;
import actividad_6.ejercicio4.ejercicio4_aleatorio;

import javax.swing.*;

public class simulacionEjercicio4 extends JFrame {

    public simulacionEjercicio4() {
        setTitle("Simulación de Inspección - Ejercicio 2 - Aplicación Completa");
        setSize(1500, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el panel de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear instancias de los paneles existentes (sin repetir código)
        ejercicio4 panelPredefinido = new ejercicio4();
        ejercicio4_aleatorio panelAleatorio = new ejercicio4_aleatorio();
        ejercicio4_manual panelManual = new ejercicio4_manual();

        // Extraer el contenido de cada ventana y agregarlo como pestaña
        tabbedPane.addTab("Valores Predefinidos", panelPredefinido.getContentPane());
        tabbedPane.addTab("Valores Aleatorios", panelAleatorio.getContentPane());
        tabbedPane.addTab("Ingreso Manual", panelManual.getContentPane());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new simulacionEjercicio4().setVisible(true));
    }
}

