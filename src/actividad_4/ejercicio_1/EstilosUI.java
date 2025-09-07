package actividad_4.ejercicio_1;

import javax.swing.*;
import java.awt.*;

/**
 * Clase utilitaria para aplicar estilos a componentes de UI.
 * Refactorizada para evitar duplicidad de código con otros paquetes.
 */
public class EstilosUI {
    // Referencia estática a la clase de estilos base para evitar duplicación
    private static final actividad_3.ejercicio_1.EstilosUI estilosBase = new actividad_3.ejercicio_1.EstilosUI();

    /**
     * Aplica estilo a un botón
     */
    public static void aplicarEstiloBoton(JButton boton) {
        actividad_3.ejercicio_1.EstilosUI.botonuevo(boton);
    }

    /**
     * Aplica estilo a un panel
     */
    public static void aplicarEstiloPanel(JPanel panel) {
        panel.setBackground(new Color(255, 255, 255));
    }

    /**
     * Aplica estilo a una tabla
     */
    public static void aplicarEstiloTabla(JTable tabla) {
        actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla);
    }

    /**
     * Aplica estilo a una etiqueta
     */
    public static void aplicarEstiloLabel(JLabel label) {
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label);
    }

    /**
     * Aplica estilo a una etiqueta de título
     */
    public static void aplicarEstiloTitulo(JLabel label) {
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label);
    }
}
