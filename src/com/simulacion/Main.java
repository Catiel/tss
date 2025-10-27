package com.simulacion;

import com.simulacion.ui.SimulacionFrame;
import javax.swing.SwingUtilities;

/**
 * Clase principal que inicia la aplicacion de simulacion
 * Replica el modelo ProModel de linea de empaque
 *
 * @author Simulacion de Sistemas
 * @version 1.0
 */
public class Main {

    public static void main(String[] args) {
        // Iniciar la interfaz grafica en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Establecer el Look and Feel del sistema operativo
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
            } catch (Exception e) {
                System.err.println("No se pudo establecer el Look and Feel: " + e.getMessage());
            }

            // Crear y mostrar la ventana principal de simulacion
            SimulacionFrame frame = new SimulacionFrame();
            frame.setVisible(true);
        });
    }
}
