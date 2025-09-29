package actividad_7.predeterminado;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.Arrays;

/**
 * Clase responsable de generar todas las gráficas de la aplicación
 */
public class GeneradorGraficas {

    /**
     * Crea el panel que contiene las dos gráficas principales
     */
    public static JPanel crearPanelGraficas(double[] costosTotales, double promedio, double desviacion) {
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales); // Gráfica de evolución de costos
        JFreeChart chart2 = crearGraficaProbabilidadNormal(costosTotales, promedio, desviacion); // Gráfica Q-Q plot

        ChartPanel chartPanel1 = crearChartPanel(chart1); // Panel para la primera gráfica
        ChartPanel chartPanel2 = crearChartPanel(chart2); // Panel para la segunda gráfica

        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15)); // Panel con grid para ambas gráficas
        graficasPanel.add(chartPanel1); // Agrega la primera gráfica
        graficasPanel.add(chartPanel2); // Agrega la segunda gráfica

        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título
        graficasPanel.setBackground(Color.WHITE); // Fondo blanco

        return graficasPanel; // Devuelve el panel de gráficas
    }

    /**
     * Configura un ChartPanel con las propiedades estándar
     */
    public static ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart); // Panel para la gráfica
        chartPanel.setPreferredSize(new Dimension(1200, 300)); // Tamaño preferido
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Borde vacío
        chartPanel.setBackground(Color.WHITE); // Fondo blanco
        return chartPanel; // Devuelve el panel
    }

    /**
     * Crea la gráfica de evolución temporal de costos totales
     */
    public static JFreeChart crearEvolucionGrafica(double[] costosTotales) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Dataset para la gráfica

        for (int i = 0; i < costosTotales.length; i++) { // Para cada día
            dataset.addValue(costosTotales[i], "Costo total", Integer.toString(i + 1)); // Agrega el valor al dataset
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo total ($) por Día",
            "Día",
            "Costo total ($)",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        ); // Crea la gráfica de líneas

        configurarAparienciaGrafica(chart, "Evolución del Costo total ($) por Día"); // Configura apariencia
        return chart; // Devuelve la gráfica
    }

    /**
     * Crea la gráfica Q-Q plot para verificar normalidad
     */
    public static JFreeChart crearGraficaProbabilidadNormal(double[] data, double mean, double stdDev) {
        Arrays.sort(data); // Ordena los datos
        int n = data.length; // Número de datos

        XYSeries serieObservados = new XYSeries("Datos observados"); // Serie de datos observados
        XYSeries serieTeoricos = new XYSeries("Normal Ideal"); // Serie de datos teóricos

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev); // Distribución normal teórica

        for (int i = 0; i < n; i++) {
            double percentile = (i + 1.0) / (n + 1.0); // Percentil para el dato
            double theoreticalQuantile = normalDist.inverseCumulativeProbability(percentile); // Cuantil teórico

            serieObservados.add(theoreticalQuantile, data[i]); // Agrega punto observado
            serieTeoricos.add(theoreticalQuantile, theoreticalQuantile); // Agrega punto teórico
        }

        XYSeriesCollection dataset = new XYSeriesCollection(); // Dataset para la gráfica
        dataset.addSeries(serieObservados); // Agrega serie observada
        dataset.addSeries(serieTeoricos); // Agrega serie teórica

        JFreeChart chart = ChartFactory.createScatterPlot(
            "Gráfica Probabilidad Normal - Costo total",
            "Cuantiles teóricos (Normal)",
            "Datos observados (Costo total)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        ); // Crea la gráfica de dispersión

        configurarAparienciaGrafica(chart, "Gráfica Probabilidad Normal - Costo total"); // Configura apariencia
        return chart; // Devuelve la gráfica
    }

    /**
     * Crea la gráfica de líneas que muestra la evolución del costo promedio de cada réplica
     */
    public static JFreeChart crearGraficaLineasReplicas(double[][] costosReplicas, int tamanoRecomendado) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Dataset para la gráfica

        for (int replica = 0; replica < 5; replica++) { // Para cada réplica
            String serieNombre = "Réplica " + (replica + 1); // Nombre de la serie

            for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
                double sumaAcumulada = 0; // Suma acumulada
                for (int i = 0; i <= dia; i++) {
                    sumaAcumulada += costosReplicas[replica][i]; // Suma el costo
                }
                double promedioAcumulado = sumaAcumulada / (dia + 1); // Promedio acumulado

                dataset.addValue(promedioAcumulado, serieNombre, Integer.toString(dia + 1)); // Agrega valor al dataset
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo Promedio Acumulado por Réplica",
            "Día",
            "Costo Promedio Acumulado ($)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        ); // Crea la gráfica de líneas

        configurarAparienciaGrafica(chart, "Evolución del Costo Promedio Acumulado por Réplica"); // Configura apariencia

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot(); // Obtiene el plot
        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer(); // Renderizador de líneas

        for (int i = 0; i < 5; i++) { // Para cada réplica
            renderer.setSeriesPaint(i, Constantes.COLORES_REPLICAS[i]); // Color de la serie
            renderer.setSeriesStroke(i, new BasicStroke(2.0f)); // Grosor de la línea
            renderer.setSeriesShapesVisible(i, false); // Sin marcadores
        }

        plot.setBackgroundPaint(Color.WHITE); // Fondo blanco
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Líneas de grilla
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Líneas de grilla

        return chart; // Devuelve la gráfica
    }

    /**
     * Configura la apariencia visual común de las gráficas
     */
    public static void configurarAparienciaGrafica(JFreeChart chart, String titulo) {
        chart.setBackgroundPaint(Color.WHITE); // Fondo blanco

        TextTitle textTitle = new TextTitle(titulo, Constantes.FUENTE_TITULO); // Título con fuente
        textTitle.setPaint(Constantes.COLOR_PRIMARIO); // Color del título
        chart.setTitle(textTitle); // Asigna el título
    }

    /**
     * Muestra la gráfica de réplicas en una ventana independiente más grande
     */
    public static void mostrarGraficaEnGrande(JFreeChart chart) {
        JFrame frameGrafica = new JFrame("Gráfica de Réplicas - Vista Ampliada"); // Ventana para la gráfica

        ChartPanel chartPanelGrande = new ChartPanel(chart); // Panel para la gráfica
        chartPanelGrande.setPreferredSize(new Dimension(1000, 600)); // Tamaño grande
        chartPanelGrande.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Borde vacío
        chartPanelGrande.setBackground(Color.WHITE); // Fondo blanco

        chartPanelGrande.setMouseWheelEnabled(true); // Habilita zoom con rueda
        chartPanelGrande.setMouseZoomable(true); // Habilita zoom con mouse
        chartPanelGrande.setDomainZoomable(true); // Zoom horizontal
        chartPanelGrande.setRangeZoomable(true); // Zoom vertical

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Panel de controles
        panelControles.setBackground(Color.WHITE); // Fondo blanco

        JButton botonResetZoom = new JButton("Resetear Zoom"); // Botón para resetear zoom
        botonResetZoom.setFont(Constantes.FUENTE_GENERAL); // Fuente
        botonResetZoom.addActionListener(e -> chartPanelGrande.restoreAutoBounds()); // Acción de reset

        JButton botonCerrar = new JButton("Cerrar"); // Botón para cerrar
        botonCerrar.setFont(Constantes.FUENTE_GENERAL); // Fuente
        botonCerrar.setBackground(new Color(220, 53, 69)); // Fondo rojo
        botonCerrar.setForeground(Color.WHITE); // Texto blanco
        botonCerrar.setFocusPainted(false); // Sin foco
        botonCerrar.addActionListener(e -> frameGrafica.dispose()); // Acción de cerrar

        JButton[] botones = {botonResetZoom, botonCerrar}; // Arreglo de botones
        for (JButton boton : botones) {
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano
            boton.setFocusPainted(false); // Sin foco
        }

        panelControles.add(botonResetZoom); // Agrega botón de reset
        panelControles.add(botonCerrar); // Agrega botón de cerrar

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15)); // Borde vacío
        panelPrincipal.setBackground(Color.WHITE); // Fondo blanco
        panelPrincipal.add(chartPanelGrande, BorderLayout.CENTER); // Agrega la gráfica
        panelPrincipal.add(panelControles, BorderLayout.SOUTH); // Agrega controles

        frameGrafica.setContentPane(panelPrincipal); // Establece el contenido
        frameGrafica.setSize(1100, 750); // Tamaño de la ventana
        frameGrafica.setLocationRelativeTo(null); // Centra la ventana
        frameGrafica.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo la ventana

        frameGrafica.setVisible(true); // Muestra la ventana
        frameGrafica.toFront(); // Trae al frente
        frameGrafica.requestFocus(); // Solicita foco
    }
}
