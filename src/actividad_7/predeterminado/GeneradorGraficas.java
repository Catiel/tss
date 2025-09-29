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
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales);
        JFreeChart chart2 = crearGraficaProbabilidadNormal(costosTotales, promedio, desviacion);

        ChartPanel chartPanel1 = crearChartPanel(chart1);
        ChartPanel chartPanel2 = crearChartPanel(chart2);

        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        graficasPanel.add(chartPanel1);
        graficasPanel.add(chartPanel2);

        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));
        graficasPanel.setBackground(Color.WHITE);

        return graficasPanel;
    }

    /**
     * Configura un ChartPanel con las propiedades estándar
     */
    public static ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1200, 300));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel.setBackground(Color.WHITE);
        return chartPanel;
    }

    /**
     * Crea la gráfica de evolución temporal de costos totales
     */
    public static JFreeChart crearEvolucionGrafica(double[] costosTotales) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int i = 0; i < costosTotales.length; i++) {
            dataset.addValue(costosTotales[i], "Costo total", Integer.toString(i + 1));
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
        );

        configurarAparienciaGrafica(chart, "Evolución del Costo total ($) por Día");
        return chart;
    }

    /**
     * Crea la gráfica Q-Q plot para verificar normalidad
     */
    public static JFreeChart crearGraficaProbabilidadNormal(double[] data, double mean, double stdDev) {
        Arrays.sort(data);
        int n = data.length;

        XYSeries serieObservados = new XYSeries("Datos observados");
        XYSeries serieTeoricos = new XYSeries("Normal Ideal");

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev);

        for (int i = 0; i < n; i++) {
            double percentile = (i + 1.0) / (n + 1.0);
            double theoreticalQuantile = normalDist.inverseCumulativeProbability(percentile);

            serieObservados.add(theoreticalQuantile, data[i]);
            serieTeoricos.add(theoreticalQuantile, theoreticalQuantile);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serieObservados);
        dataset.addSeries(serieTeoricos);

        JFreeChart chart = ChartFactory.createScatterPlot(
            "Gráfica Probabilidad Normal - Costo total",
            "Cuantiles teóricos (Normal)",
            "Datos observados (Costo total)",
            dataset,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        configurarAparienciaGrafica(chart, "Gráfica Probabilidad Normal - Costo total");
        return chart;
    }

    /**
     * Crea la gráfica de líneas que muestra la evolución del costo promedio de cada réplica
     */
    public static JFreeChart crearGraficaLineasReplicas(double[][] costosReplicas, int tamanoRecomendado) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int replica = 0; replica < 5; replica++) {
            String serieNombre = "Réplica " + (replica + 1);

            for (int dia = 0; dia < tamanoRecomendado; dia++) {
                double sumaAcumulada = 0;
                for (int i = 0; i <= dia; i++) {
                    sumaAcumulada += costosReplicas[replica][i];
                }
                double promedioAcumulado = sumaAcumulada / (dia + 1);

                dataset.addValue(promedioAcumulado, serieNombre, Integer.toString(dia + 1));
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
        );

        configurarAparienciaGrafica(chart, "Evolución del Costo Promedio Acumulado por Réplica");

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();

        for (int i = 0; i < 5; i++) {
            renderer.setSeriesPaint(i, Constantes.COLORES_REPLICAS[i]);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f));
            renderer.setSeriesShapesVisible(i, false);
        }

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        return chart;
    }

    /**
     * Configura la apariencia visual común de las gráficas
     */
    public static void configurarAparienciaGrafica(JFreeChart chart, String titulo) {
        chart.setBackgroundPaint(Color.WHITE);

        TextTitle textTitle = new TextTitle(titulo, Constantes.FUENTE_TITULO);
        textTitle.setPaint(Constantes.COLOR_PRIMARIO);
        chart.setTitle(textTitle);
    }

    /**
     * Muestra la gráfica de réplicas en una ventana independiente más grande
     */
    public static void mostrarGraficaEnGrande(JFreeChart chart) {
        JFrame frameGrafica = new JFrame("Gráfica de Réplicas - Vista Ampliada");

        ChartPanel chartPanelGrande = new ChartPanel(chart);
        chartPanelGrande.setPreferredSize(new Dimension(1000, 600));
        chartPanelGrande.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanelGrande.setBackground(Color.WHITE);

        chartPanelGrande.setMouseWheelEnabled(true);
        chartPanelGrande.setMouseZoomable(true);
        chartPanelGrande.setDomainZoomable(true);
        chartPanelGrande.setRangeZoomable(true);

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelControles.setBackground(Color.WHITE);

        JButton botonResetZoom = new JButton("Resetear Zoom");
        botonResetZoom.setFont(Constantes.FUENTE_GENERAL);
        botonResetZoom.addActionListener(e -> chartPanelGrande.restoreAutoBounds());

        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.setFont(Constantes.FUENTE_GENERAL);
        botonCerrar.setBackground(new Color(220, 53, 69));
        botonCerrar.setForeground(Color.WHITE);
        botonCerrar.setFocusPainted(false);
        botonCerrar.addActionListener(e -> frameGrafica.dispose());

        JButton[] botones = {botonResetZoom, botonCerrar};
        for (JButton boton : botones) {
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            boton.setFocusPainted(false);
        }

        panelControles.add(botonResetZoom);
        panelControles.add(botonCerrar);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.add(chartPanelGrande, BorderLayout.CENTER);
        panelPrincipal.add(panelControles, BorderLayout.SOUTH);

        frameGrafica.setContentPane(panelPrincipal);
        frameGrafica.setSize(1100, 750);
        frameGrafica.setLocationRelativeTo(null);
        frameGrafica.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        frameGrafica.setVisible(true);
        frameGrafica.toFront();
        frameGrafica.requestFocus();
    }
}
