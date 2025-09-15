package actividad_5.urnas;

import javax.swing.*;
import java.awt.*;

/** Estilos reutilizando la base ya existente en actividad_3.ejercicio_1 */
public class EstilosUI {
    public static void aplicarEstiloPanel(JPanel panel){ panel.setBackground(Color.white); }
    public static void aplicarEstiloTitulo(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label); }
    public static void aplicarEstiloTabla(JTable tabla){ actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla); }
    public static void aplicarEstiloBoton(JButton boton){ actividad_3.ejercicio_1.EstilosUI.botonuevo(boton); }
    public static void aplicarEstiloLabel(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label); }
}

