package actividad_6;

import actividad_6.ejercicio1.simulacionEjercicio1;
import actividad_6.ejercicio2.simulacionEjercicio2;
import actividad_6.ejercicio3.simulacionEjercicio3;
import actividad_6.ejercicio4.simulacionEjercicio4;

import javax.swing.*;

public class SimulacionCompleta extends JFrame {

    public SimulacionCompleta() {
        setTitle("Actividad 6 - Simulaciones Completas - Todos los Ejercicios");
        setSize(1600, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Crear el panel de pestañas principal
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear instancias de cada simulación completa de ejercicio
        simulacionEjercicio1 simulacion1 = new simulacionEjercicio1();
        simulacionEjercicio2 simulacion2 = new simulacionEjercicio2();
        simulacionEjercicio3 simulacion3 = new simulacionEjercicio3();
        simulacionEjercicio4 simulacion4 = new simulacionEjercicio4();

        // Extraer el contenido de cada simulación y agregarlo como pestaña
        tabbedPane.addTab("Ejercicio 1 - Inspección", simulacion1.getContentPane());
        tabbedPane.addTab("Ejercicio 2 - Inspección completa", simulacion2.getContentPane());
        tabbedPane.addTab("Ejercicio 3 - Barras Defectuosas", simulacion3.getContentPane());
        tabbedPane.addTab("Ejercicio 4 - Inventario Azúcar", simulacion4.getContentPane());

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true));
    }
}
