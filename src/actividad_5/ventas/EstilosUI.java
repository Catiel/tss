package actividad_5.ventas; // Define el paquete donde se encuentra esta clase utilitaria para el módulo de simulación de ventas

import javax.swing.*; // Importa todas las clases de Swing para crear componentes de interfaz gráfica
import java.awt.*; // Importa clases para manejo de componentes gráficos, colores y layouts

/** Reusa estilos globales definidos en actividad_3.ejercicio_1 */ // Comentario de documentación que explica que esta clase reutiliza estilos de otro módulo
public class EstilosUI { // Declara la clase utilitaria estática para aplicar estilos consistentes a los componentes GUI del módulo de ventas
    public static void aplicarEstiloPanel(JPanel p){ p.setBackground(Color.white); } // Método que establece el color de fondo blanco para cualquier panel pasado como parámetro
    public static void aplicarEstiloTitulo(JLabel l){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(l); } // Método que delega el estilo de títulos a la clase EstilosUI del ejercicio_1 de actividad_3
    public static void aplicarEstiloTabla(JTable t){ actividad_3.ejercicio_1.EstilosUI.tablanueva(t); } // Método que aplica estilos a tablas usando el método tablanueva del ejercicio_1 de actividad_3
    public static void aplicarEstiloBoton(JButton b){ actividad_3.ejercicio_1.EstilosUI.botonuevo(b); } // Método que aplica estilos a botones usando el método botonuevo del ejercicio_1 de actividad_3
    public static void aplicarEstiloLabel(JLabel l){ actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(l); } // Método que aplica estilos a etiquetas usando el método aplicarEstiloLabel del ejercicio_1 de actividad_3
}
