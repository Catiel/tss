package actividad_5.urnas; // Paquete donde se define la clase de estilos específica de la actividad 5 (urnas)

import javax.swing.*; // Importa clases Swing: JPanel, JTable, JButton, JLabel
import java.awt.*; // Importa clases AWT: Color, Font, etc.

/** Estilos reutilizando la base ya existente en actividad_3.ejercicio_1 */ // Comentario descriptivo general
public class EstilosUI { // Declaración de la clase utilitaria (solo métodos estáticos)
    public static void aplicarEstiloPanel(JPanel panel){ panel.setBackground(Color.white); } // Método: establece color de fondo blanco
    public static void aplicarEstiloTitulo(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label); } // Método: delega el estilo de título a utilidades previas
    public static void aplicarEstiloTabla(JTable tabla){ actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla); } // Método: aplica formato estándar a la tabla (cabecera, fuente, selección)
    public static void aplicarEstiloBoton(JButton boton){ actividad_3.ejercicio_1.EstilosUI.botonuevo(boton); } // Método: aplica estilo visual corporativo a botones
    public static void aplicarEstiloLabel(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label); } // Método: aplica estilo básico a etiquetas normales
}
