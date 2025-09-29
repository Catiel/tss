package actividad_7.aleatorio;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Clase responsable de crear paneles informativos y de resumen
 */
public class CreadorPaneles {

    /**
     * Crea el panel con resumen estadístico y información de normalidad
     */
    public static JPanel crearResumenEstadisticoPanel(boolean incluirTamano, double promedio,
            double desviacion, double minimo, double maximo, double valorAd, double pValue,
            boolean esNormal, int tamanoRecomendado) {

        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        String titulo = incluirTamano ? "Resumen Estadístico y Normalidad" : "Resumen Estadístico";

        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));
        resumenPanel.setBackground(Color.WHITE);

        // Agregar estadísticas básicas
        resumenPanel.add(crearEtiquetaResumen(String.format("Promedio: $%,.2f", promedio)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Desviación: $%,.2f", desviacion)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Mínimo: $%,.2f", minimo)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Máximo: $%,.2f", maximo)));

        // Agregar información de normalidad si se solicita
        if (incluirTamano) {
            resumenPanel.add(crearEtiquetaResumen(String.format("Valor AD (simulado): %.4f", valorAd)));
            resumenPanel.add(crearEtiquetaResumen(String.format("p-valor KS: %.4f", pValue)));

            // CAMBIO MÍNIMO: Mostrar si es normal o no y el tamaño recomendado
            if (esNormal) {
                resumenPanel.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (Normal)", tamanoRecomendado)));
            } else {
                resumenPanel.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (No Normal)", tamanoRecomendado)));
            }
        }

        return resumenPanel;
    }

    /**
     * Crea una etiqueta con formato estándar para el resumen
     */
    public static JLabel crearEtiquetaResumen(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    /**
     * Obtiene los valores mínimo y máximo de la columna de costo total
     */
    public static double[] getMinMaxCosto(DefaultTableModel modelo) {
        int filas = modelo.getRowCount();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < filas; i++) {
            double val = (double) modelo.getValueAt(i, 11);
            if (val < min) min = val;
            if (val > max) max = val;
        }

        return new double[]{min, max};
    }

    /**
     * Crea el panel principal con la configuración de layout
     */
    public static JPanel crearPanelPrincipal(JPanel graficasPanel, JScrollPane scrollPaneTabla, JPanel resumenPanel) {
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(graficasPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER);
        mainPanel.add(resumenPanel, BorderLayout.SOUTH);

        return mainPanel;
    }
}