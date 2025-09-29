package actividad_7.manual; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel para manipular datos de tablas
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales

/**
 * Clase responsable de crear paneles informativos y de resumen
 */
public class CreadorPaneles { // Declaración de la clase CreadorPaneles

    /**
     * Crea el panel con resumen estadístico y información de normalidad
     * MODIFICADO: Usa FlowLayout con wrap para mejor distribución del texto
     */
    public static JPanel crearResumenEstadisticoPanel(boolean incluirTamano, double promedio,
            double desviacion, double minimo, double maximo, double valorAd, double pValue,
            boolean esNormal, int tamanoRecomendado) { // Método para crear el panel de resumen estadístico

        String titulo = incluirTamano ? "Resumen Estadístico y Normalidad" : "Resumen Estadístico"; // Define el título según si se incluye tamaño

        // Usar BoxLayout vertical para mejor control
        JPanel resumenPanel = new JPanel(); // Crea el panel principal de resumen
        resumenPanel.setLayout(new BoxLayout(resumenPanel, BoxLayout.Y_AXIS)); // Establece el layout vertical
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Establece el borde y título del panel
        resumenPanel.setBackground(Color.WHITE); // Fondo blanco para el panel

        // Primera fila: estadísticas básicas
        JPanel primeraFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Panel para la primera fila
        primeraFila.setBackground(Color.WHITE); // Fondo blanco para la primera fila
        primeraFila.add(crearEtiquetaResumen(String.format("Promedio: $%,.2f", promedio))); // Etiqueta de promedio
        primeraFila.add(crearEtiquetaResumen(String.format("Desviación: $%,.2f", desviacion))); // Etiqueta de desviación
        primeraFila.add(crearEtiquetaResumen(String.format("Mínimo: $%,.2f", minimo))); // Etiqueta de mínimo
        primeraFila.add(crearEtiquetaResumen(String.format("Máximo: $%,.2f", maximo))); // Etiqueta de máximo

        resumenPanel.add(primeraFila); // Añade la primera fila al panel principal

        // Segunda fila: información de normalidad (si se solicita)
        if (incluirTamano) { // Si se debe incluir información de tamaño
            JPanel segundaFila = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // Panel para la segunda fila
            segundaFila.setBackground(Color.WHITE); // Fondo blanco para la segunda fila
            segundaFila.add(crearEtiquetaResumen(String.format("Valor AD (simulado): %.4f", valorAd))); // Etiqueta de valor AD
            segundaFila.add(crearEtiquetaResumen(String.format("p-valor KS: %.4f", pValue))); // Etiqueta de p-valor

            // Mostrar si es normal o no y el tamaño recomendado
            if (esNormal) { // Si la distribución es normal
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (Normal)", tamanoRecomendado))); // Etiqueta de tamaño recomendado normal
            } else { // Si la distribución no es normal
                segundaFila.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas (No Normal)", tamanoRecomendado))); // Etiqueta de tamaño recomendado no normal
            }

            resumenPanel.add(segundaFila); // Añade la segunda fila al panel principal
        }

        return resumenPanel; // Devuelve el panel de resumen
    }

    /**
     * Crea una etiqueta con formato estándar para el resumen
     * MODIFICADO: Mejor padding y tamaño para evitar corte de texto
     */
    public static JLabel crearEtiquetaResumen(String texto) { // Método para crear una etiqueta de resumen
        JLabel label = new JLabel(texto, SwingConstants.CENTER); // Crea la etiqueta centrada
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Tamaño optimizado
        label.setForeground(new Color(50, 50, 50)); // Color del texto
        label.setBorder(BorderFactory.createEmptyBorder(5, 8, 5, 8)); // Padding ajustado

        // Establecer un tamaño preferido basado en el contenido
        FontMetrics fm = label.getFontMetrics(label.getFont()); // Obtiene las métricas de la fuente
        int ancho = fm.stringWidth(texto) + 20; // Calcula el ancho con padding extra
        label.setPreferredSize(new Dimension(ancho, 30)); // Establece el tamaño preferido

        return label; // Devuelve la etiqueta
    }

    /**
     * Obtiene los valores mínimo y máximo de la columna de costo total
     */
    public static double[] getMinMaxCosto(DefaultTableModel modelo) { // Método para obtener el mínimo y máximo de la columna de costo
        int filas = modelo.getRowCount(); // Obtiene el número de filas
        double min = Double.MAX_VALUE; // Inicializa el mínimo
        double max = Double.MIN_VALUE; // Inicializa el máximo

        for (int i = 0; i < filas; i++) { // Recorre todas las filas
            double val = (double) modelo.getValueAt(i, 11); // Obtiene el valor de la columna 11 (costo total)
            if (val < min) min = val; // Actualiza el mínimo si corresponde
            if (val > max) max = val; // Actualiza el máximo si corresponde
        }

        return new double[]{min, max}; // Devuelve el mínimo y máximo en un arreglo
    }

    /**
     * Crea el panel principal con la configuración de layout
     * MODIFICADO: Ajustado el padding para mejor distribución del espacio
     */
    public static JPanel crearPanelPrincipal(JPanel graficasPanel, JScrollPane scrollPaneTabla, JPanel resumenPanel) { // Método para crear el panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15)); // Reducido ligeramente el espacio
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Reducido el padding exterior
        mainPanel.setBackground(Color.WHITE); // Fondo blanco

        mainPanel.add(graficasPanel, BorderLayout.NORTH); // Añade el panel de gráficas al norte
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER); // Añade el scroll de la tabla al centro
        mainPanel.add(resumenPanel, BorderLayout.SOUTH); // Añade el panel de resumen al sur

        return mainPanel; // Devuelve el panel principal
    }
}