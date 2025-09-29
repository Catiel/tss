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
        JPanel resumenPanel = new JPanel();
        resumenPanel.setLayout(new BoxLayout(resumenPanel, BoxLayout.Y_AXIS));
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));
        resumenPanel.setBackground(Color.WHITE);

        // Primera fila: estadísticas básicas
        JPanel primeraFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        primeraFila.setBackground(Color.WHITE);
        primeraFila.add(crearEtiquetaResumen(String.format("Promedio: $%,.2f", promedio)));
        primeraFila.add(crearEtiquetaResumen(String.format("Desviación: $%,.2f", desviacion)));
        primeraFila.add(crearEtiquetaResumen(String.format("Mínimo: $%,.2f", minimo)));
        primeraFila.add(crearEtiquetaResumen(String.format("Máximo: $%,.2f", maximo)));

        resumenPanel.add(primeraFila);

        // Segunda fila: información de normalidad (si se solicita)
        if (incluirTamano) {
            JPanel segundaFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
            segundaFila.setBackground(Color.WHITE);
            segundaFila.add(crearEtiquetaResumen(String.format("Valor AD (simulado): %.4f", valorAd)));
            segundaFila.add(crearEtiquetaResumen(String.format("p-valor KS: %.4f", pValue)));

            // Mostrar si es normal o no y el tamaño recomendado
            if (esNormal) {
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (Normal)", tamanoRecomendado)));
            } else {
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (No Normal)", tamanoRecomendado)));
            }

            resumenPanel.add(segundaFila);
        }

        return resumenPanel;
    }

    /**
     * Crea una etiqueta con formato estándar para el resumen
     * MODIFICADO: Mejor padding y tamaño para evitar corte de texto
     */
    public static JLabel crearEtiquetaResumen(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Tamaño optimizado
        label.setForeground(new Color(50, 50, 50));
        label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8)); // Padding ajustado

        // Establecer un tamaño preferido basado en el contenido
        FontMetrics fm = label.getFontMetrics(label.getFont());
        int ancho = fm.stringWidth(texto) + 20; // Agregar padding extra
        label.setPreferredSize(new Dimension(ancho, 30));

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
     * MODIFICADO: Ajustado el padding para mejor distribución del espacio
     */
    public static JPanel crearPanelPrincipal(JPanel graficasPanel, JScrollPane scrollPaneTabla, JPanel resumenPanel) {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); // Reducido ligeramente el espacio
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Reducido el padding exterior
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(graficasPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER);
        mainPanel.add(resumenPanel, BorderLayout.SOUTH);

        return mainPanel;
    }
}