package actividad_7;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Random;

public class SimulacionCompleta extends JFrame {

    private JTable tabla;
    private DefaultTableModel model;
    private double errorPermitido = 500;
    private double valorT = 1.9665;
    private int politicaProduccion = 60;
    private double mediaDemanda = 80;
    private double desviacionDemanda = 10;
    private double costoFaltanteUnitario = 800;
    private double costoInventarioUnitario = 500;

    private double desviacion;
    private double promedio;
    private int tamanoRecomendado;
    private boolean esNormal;
    private double pValue;
    private double valorAd;

    // Constantes para evitar valores mágicos
    private static final String[] COLUMNAS = {"Día", "Inventario inicial (Uds)", "Política de producción (Uds)",
        "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)", "Ventas perdidas (uds)",
        "Inventario final (Uds)", "Costo faltante ($)", "Costo de inventarios ($)", "Costo total ($)"};
    private static final int[] ANCHOS_COLUMNAS = {50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150};
    private static final Color COLOR_PRIMARIO = new Color(30, 144, 255);
    private static final Color COLOR_FILA_PAR = Color.WHITE;
    private static final Color COLOR_FILA_IMPAR = new Color(235, 245, 255);

    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo");

        int dias = 365;

        // Configurar tabla principal
        configurarTabla();

        // Simulación original con 365 días
        double[] costosTotales = generarSimulacionYllenarTabla(dias, model);

        // Cálculo estadístico
        calcularEstadisticasYPruebas(costosTotales);

        JPanel resumenPanel = crearResumenEstadisticoPanel(true, promedio, desviacion,
            getMinMaxCosto(model)[0], getMinMaxCosto(model)[1]);

        // Botón para generar tabla tamaño recomendado
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado");
        botonNuevaTabla.setEnabled(esNormal);
        resumenPanel.add(botonNuevaTabla);

        // Configurar UI principal
        configurarInterfazPrincipal(costosTotales, resumenPanel);

        // Acción botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada);
    }

    private void configurarTabla() {
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteHeader = new Font("Segoe UI", Font.BOLD, 15);

        model = new DefaultTableModel(COLUMNAS, 0);
        tabla = new JTable(model);
        configurarEstilosTabla(tabla, fuenteGeneral, fuenteHeader);
    }

    private void configurarEstilosTabla(JTable tabla, Font fuenteGeneral, Font fuenteHeader) {
        tabla.setFont(fuenteGeneral);
        tabla.setRowHeight(28);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setFont(fuenteHeader);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        // Configurar renderizadores y anchos
        configurarRenderizadoresYAnchos(tabla);
    }

    private void configurarRenderizadoresYAnchos(JTable tabla) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();

        for (int i = 0; i < COLUMNAS.length; i++) {
            TableColumn col = tabla.getColumnModel().getColumn(i);
            col.setPreferredWidth(ANCHOS_COLUMNAS[i]);
            if (i >= 9) col.setCellRenderer(moneyRenderer);
            else col.setCellRenderer(centerRenderer);
        }

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) comp.setBackground(row % 2 == 0 ? COLOR_FILA_PAR : COLOR_FILA_IMPAR);
                return comp;
            }
        });
    }

    private DefaultTableCellRenderer crearRenderizadorMoneda() {
        return new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(value.toString());
                } else {
                    super.setValue(value);
                }
            }
        };
    }

    private void configurarInterfazPrincipal(double[] costosTotales, JPanel resumenPanel) {
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);

        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Gráficos: evolución y probabilidad normal
        JPanel graficasPanel = crearPanelGraficas(costosTotales, promedio, desviacion, fuenteTitulo);

        // Layout principal
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(graficasPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER);
        mainPanel.add(resumenPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setSize(1400, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JPanel crearPanelGraficas(double[] costosTotales, double promedio, double desviacion, Font fuenteTitulo) {
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales);
        JFreeChart chart2 = createNormalProbabilityPlot(costosTotales, promedio, desviacion);

        ChartPanel chartPanel1 = crearChartPanel(chart1);
        ChartPanel chartPanel2 = crearChartPanel(chart2);

        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        graficasPanel.add(chartPanel1);
        graficasPanel.add(chartPanel2);
        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            fuenteTitulo, COLOR_PRIMARIO));
        graficasPanel.setBackground(Color.WHITE);

        return graficasPanel;
    }

    private ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1200, 300));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel.setBackground(Color.WHITE);
        return chartPanel;
    }

    private double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) {
        modeloTabla.setRowCount(0);
        int inventarioFinal = 0;
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda);
        Random random = new Random();
        double[] costosTotales = new double[dias];

        for (int dia = 1; dia <= dias; dia++) {
            ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random);
            costosTotales[dia - 1] = resultado.costoTotal;
            inventarioFinal = resultado.inventarioFinal;

            Object[] fila = {dia, resultado.inventarioInicial, politicaProduccion, resultado.totalDisponible,
                String.format("%.4f", resultado.rn), resultado.demanda, resultado.ventas, resultado.ventasPerdidas,
                resultado.inventarioFinal, resultado.costoFaltante, resultado.costoInventario, resultado.costoTotal};
            modeloTabla.addRow(fila);
        }
        return costosTotales;
    }

    private ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, Random random) {
        ResultadoSimulacion resultado = new ResultadoSimulacion();
        resultado.inventarioInicial = inventarioFinalAnterior;
        resultado.totalDisponible = resultado.inventarioInicial + politicaProduccion;
        resultado.rn = random.nextDouble();
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn));
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible);
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas);
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas;
        resultado.costoFaltante = resultado.ventasPerdidas * costoFaltanteUnitario;
        resultado.costoInventario = resultado.inventarioFinal * costoInventarioUnitario;
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario;
        return resultado;
    }

    private EstadisticasSimulacion calcularEstadisticas(double[] costosTotales) {
        EstadisticasSimulacion stats = new EstadisticasSimulacion();
        stats.suma = Arrays.stream(costosTotales).sum();
        stats.sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum();
        stats.promedio = stats.suma / costosTotales.length;
        stats.varianza = (stats.sumaCuadrados / costosTotales.length) - (stats.promedio * stats.promedio);
        stats.desviacion = Math.sqrt(stats.varianza);
        stats.minimo = Arrays.stream(costosTotales).min().orElse(Double.NaN);
        stats.maximo = Arrays.stream(costosTotales).max().orElse(Double.NaN);
        return stats;
    }

    private void calcularEstadisticasYPruebas(double[] costosTotales) {
        EstadisticasSimulacion stats = calcularEstadisticas(costosTotales);
        promedio = stats.promedio;
        desviacion = stats.desviacion;

        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false);
        esNormal = pValue > 0.05;
        tamanoRecomendado = esNormal ? (int) Math.ceil(Math.pow((desviacion / errorPermitido) * valorT, 2)) : -1;
        valorAd = pValue; // Simulado, para mantener variable
    }

    private JPanel crearResumenEstadisticoPanel(boolean incluirTamano, double promedio, double desviacion, double minimo, double maximo) {
        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);
        String titulo = incluirTamano ? "Resumen Estadístico y Normalidad" : "Resumen Estadístico";

        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            fuenteTitulo, COLOR_PRIMARIO));
        resumenPanel.setBackground(Color.WHITE);

        resumenPanel.add(createSummaryLabel(String.format("Promedio: $%,.2f", promedio)));
        resumenPanel.add(createSummaryLabel(String.format("Desviación: $%,.2f", desviacion)));
        resumenPanel.add(createSummaryLabel(String.format("Mínimo: $%,.2f", minimo)));
        resumenPanel.add(createSummaryLabel(String.format("Máximo: $%,.2f", maximo)));

        if (incluirTamano) {
            resumenPanel.add(createSummaryLabel(String.format("Valor AD (simulado): %.4f", valorAd)));
            resumenPanel.add(createSummaryLabel(String.format("p-valor KS: %.4f", pValue)));

            if (esNormal) {
                resumenPanel.add(createSummaryLabel(String.format("Tamaño recomendado: %d corridas", tamanoRecomendado)));
            } else {
                resumenPanel.add(createSummaryLabel("No se recomienda tamaño (No normal)"));
            }
        }
        return resumenPanel;
    }

    private double[] getMinMaxCosto(DefaultTableModel modelo) {
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

    private void generarTablaRecomendada(ActionEvent e) {
        if (!esNormal || tamanoRecomendado < 1) return;

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " + tamanoRecomendado + " corridas");
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteHeader = new Font("Segoe UI", Font.BOLD, 15);
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(COLUMNAS, 0);
        JTable nuevaTabla = new JTable(nuevoModel);
        configurarEstilosTabla(nuevaTabla, fuenteGeneral, fuenteHeader);

        JScrollPane scroll = new JScrollPane(nuevaTabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Generar simulación para tamaño recomendado
        double[] costosTotalesNueva = generarSimulacionYllenarTabla(tamanoRecomendado, nuevoModel);
        EstadisticasSimulacion statsNueva = calcularEstadisticas(costosTotalesNueva);

        // Crear paneles de resumen y gráficas
        JPanel resumenPanelNueva = crearResumenEstadisticoPanel(false, statsNueva.promedio,
            statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo);

        JPanel graficasPanelNueva = crearPanelGraficas(costosTotalesNueva, statsNueva.promedio,
            statsNueva.desviacion, fuenteTitulo);

        // Configurar ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10));
        frameNuevaTabla.add(scroll, BorderLayout.NORTH);
        frameNuevaTabla.add(graficasPanelNueva, BorderLayout.CENTER);
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH);

        frameNuevaTabla.setSize(1400, 950);
        frameNuevaTabla.setLocationRelativeTo(this);
        frameNuevaTabla.setVisible(true);
    }

    private JFreeChart crearEvolucionGrafica(double[] costosTotales) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int i = 0; i < costosTotales.length; i++) {
            dataset.addValue(costosTotales[i], "Costo total", Integer.toString(i + 1));
        }

        JFreeChart chart = ChartFactory.createLineChart("Evolución del Costo total ($) por Día",
            "Día", "Costo total ($)", dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(Color.white);
        TextTitle textTitle = new TextTitle("Evolución del Costo total ($) por Día",
            new Font("Segoe UI Semibold", Font.BOLD, 18));
        textTitle.setPaint(COLOR_PRIMARIO);
        chart.setTitle(textTitle);

        return chart;
    }

    private JFreeChart createNormalProbabilityPlot(double[] data, double mean, double stdDev) {
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

        JFreeChart chart = ChartFactory.createScatterPlot("Gráfica Probabilidad Normal - Costo total",
            "Cuantiles teóricos (Normal)", "Datos observados (Costo total)", dataset,
            PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        chart.getTitle().setPaint(COLOR_PRIMARIO);
        return chart;
    }

    private JLabel createSummaryLabel(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    // Clases auxiliares para organizar datos
    private static class ResultadoSimulacion {
        int inventarioInicial, totalDisponible, demanda, ventas, ventasPerdidas, inventarioFinal;
        double rn, costoFaltante, costoInventario, costoTotal;
    }

    private static class EstadisticasSimulacion {
        double suma, sumaCuadrados, promedio, varianza, desviacion, minimo, maximo;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true));
    }
}
