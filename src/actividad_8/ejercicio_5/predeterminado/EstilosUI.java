package actividad_8.ejercicio_5.predeterminado; // Define el paquete donde se encuentra esta clase

import javax.swing.*;
import java.awt.*;

/** Reutiliza estilos globales previos. */
public class EstilosUI { // Clase utilitaria que proporciona métodos estáticos para aplicar estilos consistentes a componentes UI en simulaciones de maximización de ganancias de Dulce Ada
    public static void aplicarEstiloPanel(JPanel p){ p.setBackground(Color.white); } // Establece el fondo blanco para cualquier panel pasado como parámetro
    public static void aplicarEstiloTitulo(JLabel l){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(l); } // Delega la aplicación de estilo de título a la clase EstilosUI de actividad_3.ejercicio_1
    public static void aplicarEstiloTabla(JTable t){ actividad_3.ejercicio_1.EstilosUI.tablanueva(t); } // Delega la aplicación de estilo de tabla al método tablanueva de la clase EstilosUI de actividad_3.ejercicio_1
    public static void aplicarEstiloBoton(JButton b){ actividad_3.ejercicio_1.EstilosUI.botonuevo(b); } // Delega la aplicación de estilo de botón al método botonuevo de la clase EstilosUI de actividad_3.ejercicio_1
    public static void aplicarEstiloLabel(JLabel l){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(l); } // Delega la aplicación de estilo de etiqueta a la clase EstilosUI de actividad_3.ejercicio_1
}
