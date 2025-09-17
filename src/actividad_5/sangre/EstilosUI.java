package actividad_5.sangre; // Define el paquete donde se encuentra esta clase utilitaria para el módulo de simulación de sangre

import javax.swing.*; // Importa todas las clases de Swing para crear componentes de interfaz gráfica
import java.awt.*; // Importa clases para manejo de componentes gráficos, colores y layouts

/** Estilos simples reutilizando los de actividad_3.ejercicio_1 */ // Comentario de documentación que explica que esta clase reutiliza estilos de otro módulo
public class EstilosUI { // Declara la clase utilitaria estática para aplicar estilos consistentes a los componentes GUI
    public static void aplicarEstiloPanel(JPanel panel){ panel.setBackground(Color.white); } // Método que establece el color de fondo blanco para cualquier panel pasado como parámetro
    public static void aplicarEstiloTitulo(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label); } // Método que delega el estilo de títulos a la clase EstilosUI del ejercicio_1 de actividad_3
    public static void aplicarEstiloTabla(JTable tabla){ actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla); } // Método que aplica estilos a tablas usando el método tablanueva del ejercicio_1 de actividad_3
    public static void aplicarEstiloBoton(JButton boton){ actividad_3.ejercicio_1.EstilosUI.botonuevo(boton); } // Método que aplica estilos a botones usando el método botonuevo del ejercicio_1 de actividad_3
    public static void aplicarEstiloLabel(JLabel label){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label); } // Método que aplica estilos a etiquetas usando el método aplicarEstiloLabel del ejercicio_1 de actividad_3
}
