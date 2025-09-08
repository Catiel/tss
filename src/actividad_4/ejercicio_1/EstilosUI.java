package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Importa componentes Swing (JButton, JPanel, JTable, JLabel)
import java.awt.*;    // Importa clases AWT (Color)

/**
 * Clase utilitaria para aplicar estilos a componentes de UI.
 * Refactorizada para evitar duplicidad de código con otros paquetes.
 */
public class EstilosUI { // Declaración de la clase de utilidades de estilo
    // Referencia estática a la clase de estilos base para evitar duplicación (no se usa directamente aquí, sirve como referencia futura)
    private static final actividad_3.ejercicio_1.EstilosUI estilosBase = new actividad_3.ejercicio_1.EstilosUI();

    /**
     * Aplica estilo a un botón
     */
    public static void aplicarEstiloBoton(JButton boton) { // Método estático para estilizar botones
        actividad_3.ejercicio_1.EstilosUI.botonuevo(boton); // Delegación a implementación existente en actividad 3
    }

    /**
     * Aplica estilo a un panel
     */
    public static void aplicarEstiloPanel(JPanel panel) { // Método estático para estilizar paneles
        panel.setBackground(new Color(255, 255, 255)); // Define fondo blanco puro
    }

    /**
     * Aplica estilo a una tabla
     */
    public static void aplicarEstiloTabla(JTable tabla) { // Método estático para estilizar tablas
        actividad_3.ejercicio_1.EstilosUI.tablanueva(tabla); // Delegación al estilo base compartido
    }

    /**
     * Aplica estilo a una etiqueta
     */
    public static void aplicarEstiloLabel(JLabel label) { // Método estático para estilizar labels
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloLabel(label); // Usa método reutilizado (colores/fuente)
    }

    /**
     * Aplica estilo a una etiqueta de título
     */
    public static void aplicarEstiloTitulo(JLabel label) { // Método estático para estilizar títulos
        actividad_3.ejercicio_1.EstilosUI.aplicarEstiloTitulo(label); // Delegación: aplica fuente y peso definidos
    }
}
