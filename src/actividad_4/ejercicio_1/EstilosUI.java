package actividad_4.ejercicio_1;

import javax.swing.*;
import java.awt.*;

public class EstilosUI {
    public static void aplicarEstiloBoton(JButton boton) {
        actividad_3.ejercicio_1.EstilosUI.botonuevo(boton);
    }

    public static void aplicarEstiloPanel(JPanel panel) {
        panel.setBackground(new Color(255, 255, 255));
    }

    public static void aplicarEstiloTabla(JTable tabla) {
        actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla);
    }

    public static void aplicarEstiloLabel(JLabel label) {
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label);
    }

    public static void aplicarEstiloTitulo(JLabel label) {
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label);
    }
}

