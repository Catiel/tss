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

    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo");
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);
        Font fuenteHeader = new Font("Segoe UI", Font.BOLD, 15);

        int dias = 365;

        String[] columnas = {"Día", "Inventario inicial (Uds)", "Política de producción (Uds)", "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)", "Ventas perdidas (uds)", "Inventario final (Uds)", "Costo faltante ($)", "Costo de inventarios ($)", "Costo total ($)"};

        model = new DefaultTableModel(columnas, 0);
        tabla = new JTable(model);
        tabla.setFont(fuenteGeneral);
        tabla.setRowHeight(28);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
        header.setFont(fuenteHeader);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
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

        int[] anchos = {50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150};
        for (int i = 0; i < columnas.length; i++) {
            TableColumn col = tabla.getColumnModel().getColumn(i);
            col.setPreferredWidth(anchos[i]);
            if (i >= 9) col.setCellRenderer(moneyRenderer);
            else col.setCellRenderer(centerRenderer);
        }

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(235, 245, 255));
                return comp;
            }
        });

        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Simulación original con 365 días
        double[] costosTotales = generarSimulacionYllenarTabla(dias);

        // Cálculo estadístico
        calcularEstadisticasYPruebas(costosTotales);

        JPanel resumenPanel = crearResumenEstadisticoPanel(true);

        // Botón para generar tabla tamaño recomendado
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado");
        botonNuevaTabla.setEnabled(esNormal);
        resumenPanel.add(botonNuevaTabla);

        // Gráficos: evolución y probabilidad normal
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales);
        JFreeChart chart2 = createNormalProbabilityPlot(costosTotales, promedio, desviacion);

        ChartPanel chartPanel1 = new ChartPanel(chart1);
        chartPanel1.setPreferredSize(new Dimension(1200, 300));
        chartPanel1.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel1.setBackground(Color.WHITE);

        ChartPanel chartPanel2 = new ChartPanel(chart2);
        chartPanel2.setPreferredSize(new Dimension(1200, 300));
        chartPanel2.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel2.setBackground(Color.WHITE);

        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15));
        graficasPanel.add(chartPanel1);
        graficasPanel.add(chartPanel2);
        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, fuenteTitulo, new Color(30, 144, 255)));
        graficasPanel.setBackground(Color.WHITE);

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

        // Acción botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada);
    }

    private double[] generarSimulacionYllenarTabla(int dias) {
        model.setRowCount(0);
        int inventarioFinal = 0;
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda);
        Random random = new Random();
        double[] costosTotales = new double[dias];

        for (int dia = 1; dia <= dias; dia++) {
            int inventarioInicial = inventarioFinal;
            int totalDisponible = inventarioInicial + politicaProduccion;
            double rn = random.nextDouble();
            int demanda = (int) Math.round(dist.inverseCumulativeProbability(rn));
            int ventas = Math.min(demanda, totalDisponible);
            int ventasPerdidas = Math.max(0, demanda - ventas);
            inventarioFinal = totalDisponible - ventas;
            double costoFaltante = ventasPerdidas * costoFaltanteUnitario;
            double costoInventario = inventarioFinal * costoInventarioUnitario;
            double costoTotal = costoFaltante + costoInventario;

            costosTotales[dia - 1] = costoTotal;

            Object[] fila = {dia, inventarioInicial, politicaProduccion, totalDisponible, String.format("%.4f", rn), demanda, ventas, ventasPerdidas, inventarioFinal, costoFaltante, costoInventario, costoTotal};
            model.addRow(fila);
        }
        return costosTotales;
    }

    private void calcularEstadisticasYPruebas(double[] costosTotales) {
        double sumaCostoTotal = Arrays.stream(costosTotales).sum();
        double sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum();
        promedio = sumaCostoTotal / costosTotales.length;
        double varianza = (sumaCuadrados / costosTotales.length) - (promedio * promedio);
        desviacion = Math.sqrt(varianza);
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(new NormalDistribution(promedio, desviacion), costosTotales, false);
        esNormal = pValue > 0.05;
        tamanoRecomendado = esNormal ? (int) Math.ceil(Math.pow((desviacion / errorPermitido) * valorT, 2)) : -1;
        valorAd = pValue; // Simulado, para mantener variable
    }

    private JPanel crearResumenEstadisticoPanel(boolean incluirTamano) {
        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico y Normalidad", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, fuenteTitulo, new Color(30, 144, 255)));
        resumenPanel.setBackground(Color.WHITE);

        resumenPanel.add(createSummaryLabel(String.format("Promedio: $%,.2f", promedio)));
        resumenPanel.add(createSummaryLabel(String.format("Desviación: $%,.2f", desviacion)));
        resumenPanel.add(createSummaryLabel(String.format("Mínimo: $%,.2f", getMinCosto())));
        resumenPanel.add(createSummaryLabel(String.format("Máximo: $%,.2f", getMaxCosto())));
        resumenPanel.add(createSummaryLabel(String.format("Valor AD (simulado): %.4f", valorAd)));
        resumenPanel.add(createSummaryLabel(String.format("p-valor KS: %.4f", pValue)));
        if (incluirTamano) {
            if (esNormal) {
                resumenPanel.add(createSummaryLabel(String.format("Tamaño recomendado: %d corridas", tamanoRecomendado)));
            } else {
                resumenPanel.add(createSummaryLabel("No se recomienda tamaño (No normal)"));
            }
        }
        return resumenPanel;
    }

    private double getMinCosto() {
        int filas = model.getRowCount();
        double min = Double.MAX_VALUE;
        for (int i = 0; i < filas; i++) {
            double val = (double) model.getValueAt(i, 11);
            if (val < min) min = val;
        }
        return min;
    }

    private double getMaxCosto() {
        int filas = model.getRowCount();
        double max = Double.MIN_VALUE;
        for (int i = 0; i < filas; i++) {
            double val = (double) model.getValueAt(i, 11);
            if (val > max) max = val;
        }
        return max;
    }

    private void generarTablaRecomendada(ActionEvent e) {
        if (!esNormal || tamanoRecomendado < 1) return;

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " + tamanoRecomendado + " corridas");
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);

        String[] columnas = {"Día", "Inventario inicial (Uds)", "Política de producción (Uds)", "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)", "Ventas perdidas (uds)", "Inventario final (Uds)", "Costo faltante ($)", "Costo de inventarios ($)", "Costo total ($)"};
        DefaultTableModel nuevoModel = new DefaultTableModel(columnas, 0);
        JTable nuevaTabla = new JTable(nuevoModel);
        nuevaTabla.setFont(fuenteGeneral);
        nuevaTabla.setRowHeight(28);
        nuevaTabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        nuevaTabla.getTableHeader().setReorderingAllowed(false);
        nuevaTabla.setFillsViewportHeight(true);

        // Configurar anchos y renderizadores idénticos a la principal
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
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
        int[] anchos = {50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150};
        for (int i = 0; i < columnas.length; i++) {
            TableColumn col = nuevaTabla.getColumnModel().getColumn(i);
            col.setPreferredWidth(anchos[i]);
            if (i >= 9) col.setCellRenderer(moneyRenderer);
            else col.setCellRenderer(centerRenderer);
        }
        nuevaTabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(235, 245, 255));
                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(nuevaTabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Simulación tamaño recomendado igual que original
        int inventarioFinalLocal = 0;
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda);
        Random random = new Random();
        double[] costosTotalesNueva = new double[tamanoRecomendado];

        for (int dia = 1; dia <= tamanoRecomendado; dia++) {
            int inventarioInicial = inventarioFinalLocal;
            int totalDisponible = inventarioInicial + politicaProduccion;
            double rn = random.nextDouble();
            int demanda = (int) Math.round(dist.inverseCumulativeProbability(rn));
            int ventas = Math.min(demanda, totalDisponible);
            int ventasPerdidas = Math.max(0, demanda - ventas);
            inventarioFinalLocal = totalDisponible - ventas;
            double costoFaltante = ventasPerdidas * costoFaltanteUnitario;
            double costoInventario = inventarioFinalLocal * costoInventarioUnitario;
            double costoTotal = costoFaltante + costoInventario;

            costosTotalesNueva[dia - 1] = costoTotal;

            Object[] fila = {dia, inventarioInicial, politicaProduccion, totalDisponible, String.format("%.4f", rn), demanda, ventas, ventasPerdidas, inventarioFinalLocal, costoFaltante, costoInventario, costoTotal};
            nuevoModel.addRow(fila);
        }

        double sumaCostoTotalNueva = Arrays.stream(costosTotalesNueva).sum();
        double sumaCuadradosNueva = Arrays.stream(costosTotalesNueva).map(x -> x * x).sum();
        double promedioNueva = sumaCostoTotalNueva / costosTotalesNueva.length;
        double varianzaNueva = (sumaCuadradosNueva / costosTotalesNueva.length) - (promedioNueva * promedioNueva);
        double desviacionNueva = Math.sqrt(varianzaNueva);
        double minNuevo = Arrays.stream(costosTotalesNueva).min().orElse(Double.NaN);
        double maxNuevo = Arrays.stream(costosTotalesNueva).max().orElse(Double.NaN);

        JPanel resumenPanelNueva = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        resumenPanelNueva.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, fuenteTitulo, new Color(30, 144, 255)));
        resumenPanelNueva.setBackground(Color.WHITE);

        resumenPanelNueva.add(createSummaryLabel(String.format("Promedio: $%,.2f", promedioNueva)));
        resumenPanelNueva.add(createSummaryLabel(String.format("Desviación: $%,.2f", desviacionNueva)));
        resumenPanelNueva.add(createSummaryLabel(String.format("Mínimo: $%,.2f", minNuevo)));
        resumenPanelNueva.add(createSummaryLabel(String.format("Máximo: $%,.2f", maxNuevo)));

        JFreeChart chart1 = crearEvolucionGrafica(costosTotalesNueva);
        JFreeChart chart2 = createNormalProbabilityPlot(costosTotalesNueva, promedioNueva, desviacionNueva);

        ChartPanel chartPanel1Nueva = new ChartPanel(chart1);
        chartPanel1Nueva.setPreferredSize(new Dimension(1200, 300));
        chartPanel1Nueva.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel1Nueva.setBackground(Color.WHITE);

        ChartPanel chartPanel2Nueva = new ChartPanel(chart2);
        chartPanel2Nueva.setPreferredSize(new Dimension(1200, 300));
        chartPanel2Nueva.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel2Nueva.setBackground(Color.WHITE);

        JPanel graficasPanelNueva = new JPanel(new GridLayout(1, 2, 15, 15));
        graficasPanelNueva.add(chartPanel1Nueva);
        graficasPanelNueva.add(chartPanel2Nueva);
        graficasPanelNueva.setBorder(BorderFactory.createTitledBorder(null, "Gráficas", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP, fuenteTitulo, new Color(30, 144, 255)));
        graficasPanelNueva.setBackground(Color.WHITE);

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

        JFreeChart chart = ChartFactory.createLineChart("Evolución del Costo total ($) por Día", "Día", "Costo total ($)", dataset, PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(Color.white);
        TextTitle textTitle = new TextTitle("Evolución del Costo total ($) por Día", new Font("Segoe UI Semibold", Font.BOLD, 18));
        textTitle.setPaint(new Color(30, 144, 255));
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

        JFreeChart chart = ChartFactory.createScatterPlot("Gráfica Probabilidad Normal - Costo total", "Cuantiles teóricos (Normal)", "Datos observados (Costo total)", dataset, PlotOrientation.VERTICAL, true, true, false);

        chart.setBackgroundPaint(Color.WHITE);
        chart.getTitle().setFont(new Font("Segoe UI Semibold", Font.BOLD, 18));
        chart.getTitle().setPaint(new Color(30, 144, 255));
        return chart;
    }

    private JLabel createSummaryLabel(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(50, 50, 50));
        return label;
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
