package actividad_7.predeterminado;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Clase responsable de crear paneles informativos y de resumen
 */
public class CreadorPaneles {

    /**
     * Crea el panel con resumen estadístico y información de normalidad
     * MODIFICADO: Usa FlowLayout con wrap para mejor distribución del texto
     */
    public static JPanel crearResumenEstadisticoPanel(boolean incluirTamano, double promedio,
            double desviacion, double minimo, double maximo, double valorAd, double pValue,
            boolean esNormal, int tamanoRecomendado) {

        String titulo = incluirTamano ? "Resumen Estadístico y Normalidad" : "Resumen Estadístico";

        // Usar BoxLayout vertical para mejor control
        JPanel resumenPanel = new JPanel(); // Panel principal de resumen
        resumenPanel.setLayout(new BoxLayout(resumenPanel, BoxLayout.Y_AXIS)); // Layout vertical
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título y color
        resumenPanel.setBackground(Color.WHITE); // Fondo blanco

        // Primera fila: estadísticas básicas
        JPanel primeraFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Panel horizontal
        primeraFila.setBackground(Color.WHITE); // Fondo blanco
        primeraFila.add(crearEtiquetaResumen(String.format("Promedio: $%,.2f", promedio))); // Promedio
        primeraFila.add(crearEtiquetaResumen(String.format("Desviación: $%,.2f", desviacion))); // Desviación
        primeraFila.add(crearEtiquetaResumen(String.format("Mínimo: $%,.2f", minimo))); // Mínimo
        primeraFila.add(crearEtiquetaResumen(String.format("Máximo: $%,.2f", maximo))); // Máximo

        resumenPanel.add(primeraFila); // Agrega la primera fila al panel principal

        // Segunda fila: información de normalidad (si se solicita)
        if (incluirTamano) {
            JPanel segundaFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // Panel horizontal
            segundaFila.setBackground(Color.WHITE); // Fondo blanco
            segundaFila.add(crearEtiquetaResumen(String.format("Valor AD (simulado): %.4f", valorAd))); // Valor AD
            segundaFila.add(crearEtiquetaResumen(String.format("p-valor KS: %.4f", pValue))); // p-valor KS

            // Mostrar si es normal o no y el tamaño recomendado
            if (esNormal) {
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (Normal)", tamanoRecomendado))); // Normal
            } else {
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (No Normal)", tamanoRecomendado))); // No normal
            }

            resumenPanel.add(segundaFila); // Agrega la segunda fila al panel principal
        }

        return resumenPanel; // Devuelve el panel de resumen
    }

    /**
     * Crea una etiqueta con formato estándar para el resumen
     * MODIFICADO: Mejor padding y tamaño para evitar corte de texto
     */
    public static JLabel crearEtiquetaResumen(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER); // Etiqueta centrada
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Tamaño optimizado
        label.setForeground(new Color(50, 50, 50)); // Color de texto
        label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8)); // Padding ajustado

        // Establecer un tamaño preferido basado en el contenido
        FontMetrics fm = label.getFontMetrics(label.getFont()); // Métricas de fuente
        int ancho = fm.stringWidth(texto) + 20; // Agregar padding extra
        label.setPreferredSize(new Dimension(ancho, 30)); // Tamaño preferido

        return label; // Devuelve la etiqueta
    }

    /**
     * Obtiene los valores mínimo y máximo de la columna de costo total
     */
    public static double[] getMinMaxCosto(DefaultTableModel modelo) {
        int filas = modelo.getRowCount(); // Número de filas
        double min = Double.MAX_VALUE; // Inicializa mínimo
        double max = Double.MIN_VALUE; // Inicializa máximo

        for (int i = 0; i < filas; i++) { // Recorre todas las filas
            double val = (double) modelo.getValueAt(i, 11); // Obtiene el valor de la columna 11
            if (val < min) min = val; // Actualiza mínimo
            if (val > max) max = val; // Actualiza máximo
        }

        return new double[]{min, max}; // Devuelve el mínimo y máximo
    }

    /**
     * Crea el panel principal con la configuración de layout
     * MODIFICADO: Ajustado el padding para mejor distribución del espacio
     */
    public static JPanel crearPanelPrincipal(JPanel graficasPanel, JScrollPane scrollPaneTabla, JPanel resumenPanel) {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); // Layout con espacios
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Padding exterior
        mainPanel.setBackground(Color.WHITE); // Fondo blanco

        mainPanel.add(graficasPanel, BorderLayout.NORTH); // Panel de gráficas arriba
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER); // Tabla en el centro
        mainPanel.add(resumenPanel, BorderLayout.SOUTH); // Resumen abajo

        return mainPanel; // Devuelve el panel principal
    }
}