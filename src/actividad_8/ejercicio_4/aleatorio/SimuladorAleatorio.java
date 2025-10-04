package actividad_8.ejercicio_4.aleatorio;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.TriangularDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * Simulador de Estimación de Costos con Generación Aleatoria
 * Los valores Estimado, Min y Max se generan aleatoriamente con criterios realistas
 */
public class SimuladorAleatorio extends JFrame {

    private static class LineaPresupuesto {
        String codigo;
        String descripcion;
        double estimado;
        double min;
        double max;
        boolean esCategoria;

        LineaPresupuesto(String codigo, String desc, boolean cat) {
            this.codigo = codigo;
            this.descripcion = desc;
            this.esCategoria = cat;
            // Los valores se generarán aleatoriamente
        }
    }

    private final List<LineaPresupuesto> lineas = new ArrayList<>();
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JLabel lblResultadoSimulacion;
    private JLabel lblPromedio;
    private JLabel lblDesviacion;
    private JLabel lblMin;
    private JLabel lblMax;
    private double[] resultadosSimulacion;
    private Random random = new Random();

    public SimuladorAleatorio() {
        super("Simulador de Estimación de Costos - Generación Aleatoria");
        inicializarEstructura();
        generarValoresAleatorios();
        configurarUI();
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Define la estructura del proyecto sin valores
     */
    private void inicializarEstructura() {
        // Categoría 1
        lineas.add(new LineaPresupuesto("1", "ADMINSTRACION DEL PROYECTO", true));

        // Categoría 2 - Ingeniería
        lineas.add(new LineaPresupuesto("21", "ENEGINEERING MANAGEMENT", false));
        lineas.add(new LineaPresupuesto("22", "TECHNICAL STUDIES", false));
        lineas.add(new LineaPresupuesto("23", "DEFINITIVE DESIGN", false));
        lineas.add(new LineaPresupuesto("24", "ENGINEERING INSPECTION", false));
        lineas.add(new LineaPresupuesto("25", "EQUIPMENT REMOVAL DESINGN", false));
        lineas.add(new LineaPresupuesto("2", "INGENIERIA", true));

        // Categoría 3 - CENRTC
        lineas.add(new LineaPresupuesto("31", "CENRTC DEFINITIVE DESIGN", false));
        lineas.add(new LineaPresupuesto("32", "CENRTC PROCUREMENT", false));
        lineas.add(new LineaPresupuesto("33", "CENRTC FABRICATION", false));
        lineas.add(new LineaPresupuesto("3", "CENRTC", true));

        // Categoría 4 - Construcción
        lineas.add(new LineaPresupuesto("41", "WHC CONSTRUCTION MANAGEMENT", false));
        lineas.add(new LineaPresupuesto("42", "INTER-FARM MODIFICATIONS", false));
        lineas.add(new LineaPresupuesto("43", "C-FARM MODIFICATIONS", false));
        lineas.add(new LineaPresupuesto("44", "AY-FARM MODIFICATIONS", false));
        lineas.add(new LineaPresupuesto("45", "EXPENSE PROCUREMENT", false));
        lineas.add(new LineaPresupuesto("46", "FACILITY PREP", false));
        lineas.add(new LineaPresupuesto("47", "CONSTRUCTION SERVICES", false));
        lineas.add(new LineaPresupuesto("4", "CONSTRUCCION", true));

        // Categoría 5 - Otros costos
        lineas.add(new LineaPresupuesto("51", "STARTUP ADMINISTRATION", false));
        lineas.add(new LineaPresupuesto("52", "STARTUP SUPPORT", false));
        lineas.add(new LineaPresupuesto("54", "STARTUP READINESS PREVIEW", false));
        lineas.add(new LineaPresupuesto("5", "OTROS COSTOS DEL PROYECTO", true));

        // Categoría 6 - Seguridad
        lineas.add(new LineaPresupuesto("61", "ENVIROMENTAL MANAGEMENT", false));
        lineas.add(new LineaPresupuesto("63", "SAFETY", false));
        lineas.add(new LineaPresupuesto("64", "NEPA", false));
        lineas.add(new LineaPresupuesto("65", "RCRA", false));
        lineas.add(new LineaPresupuesto("66", "CAA", false));
        lineas.add(new LineaPresupuesto("6", "SEGURIDAD & AMBIENTE", true));
    }

    /**
     * Genera valores aleatorios realistas para cada línea
     * - Subcategorías: valores entre $100K - $15M
     * - Categorías principales: suma de subcategorías con variación ±10-20%
     */
    private void generarValoresAleatorios() {
        LineaPresupuesto categoriaActual = null;
        double sumaSubcategorias = 0;
        int contadorSubcategorias = 0;

        for (int i = 0; i < lineas.size(); i++) {
            LineaPresupuesto linea = lineas.get(i);

            if (linea.esCategoria) {
                // Es una categoría principal
                if (contadorSubcategorias > 0) {
                    // Asignar valores a la categoría basados en subcategorías
                    categoriaActual.estimado = sumaSubcategorias;

                    // Min: 85-95% del estimado
                    double factorMin = 0.85 + random.nextDouble() * 0.10;
                    categoriaActual.min = Math.round(categoriaActual.estimado * factorMin / 100000) * 100000;

                    // Max: 110-130% del estimado
                    double factorMax = 1.10 + random.nextDouble() * 0.20;
                    categoriaActual.max = Math.round(categoriaActual.estimado * factorMax / 100000) * 100000;
                }

                // Preparar para la siguiente categoría
                categoriaActual = linea;
                sumaSubcategorias = 0;
                contadorSubcategorias = 0;

            } else {
                // Es una subcategoría
                // Generar valor estimado entre $100,000 y $12,000,000
                double baseMin = 100000;
                double baseMax = 12000000;
                linea.estimado = Math.round((baseMin + random.nextDouble() * (baseMax - baseMin)) / 10000) * 10000;

                // Las subcategorías no tienen min/max propios
                linea.min = 0;
                linea.max = 0;

                sumaSubcategorias += linea.estimado;
                contadorSubcategorias++;
            }
        }

        // Procesar la última categoría
        if (categoriaActual != null && contadorSubcategorias > 0) {
            categoriaActual.estimado = sumaSubcategorias;
            double factorMin = 0.85 + random.nextDouble() * 0.10;
            categoriaActual.min = Math.round(categoriaActual.estimado * factorMin / 100000) * 100000;
            double factorMax = 1.10 + random.nextDouble() * 0.20;
            categoriaActual.max = Math.round(categoriaActual.estimado * factorMax / 100000) * 100000;
        }
    }

    private void configurarUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior con tabla
        String[] columnas = {"Código", "Descripción", "Estimado", "Min", "Max", "Simulado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tabla = new JTable(modeloTabla);
        configurarTabla();
        llenarTabla();

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Proyecto de Estimación de Costos (Valores Aleatorios)"));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnRegenerar = new JButton("Regenerar Valores Aleatorios");
        btnRegenerar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnRegenerar.setBackground(new Color(255, 140, 0));
        btnRegenerar.setForeground(Color.WHITE);
        btnRegenerar.setFocusPainted(false);

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo");
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimular.setBackground(new Color(30, 144, 255));
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFocusPainted(false);

        JSpinner spinnerIteraciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 10000, 1000));
        spinnerIteraciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panelBotones.add(btnRegenerar);
        panelBotones.add(new JLabel("Simulaciones:"));
        panelBotones.add(spinnerIteraciones);
        panelBotones.add(btnSimular);

        // Panel de estadísticas
        JPanel panelEstadisticas = crearPanelEstadisticas();

        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelCentral.add(scrollTabla, BorderLayout.NORTH);
        panelCentral.add(panelBotones, BorderLayout.CENTER);
        panelCentral.add(panelEstadisticas, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);

        // Acciones de botones
        btnRegenerar.addActionListener(e -> {
            generarValoresAleatorios();
            actualizarTabla();
            lblResultadoSimulacion.setText("Pendiente");
            lblResultadoSimulacion.setForeground(Color.GRAY);
            lblPromedio.setText("--");
            lblDesviacion.setText("--");
            lblMin.setText("--");
            lblMax.setText("--");
        });

        btnSimular.addActionListener(e -> {
            int iteraciones = (int) spinnerIteraciones.getValue();
            ejecutarSimulacion(iteraciones);
        });
    }

    private void configurarTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(30, 144, 255));
        tabla.getTableHeader().setForeground(Color.WHITE);

        // Renderizador para categorías (negrita y fondo)
        DefaultTableCellRenderer categoriaRenderer = new DefaultTableCellRenderer() {
            DecimalFormat formato = new DecimalFormat("$#,##0.00");
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Verificar si es categoría
                boolean esCategoria = false;
                if (row < lineas.size()) {
                    esCategoria = lineas.get(row).esCategoria;
                } else if (row >= lineas.size()) {
                    esCategoria = true; // Filas totales
                }

                if (esCategoria) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    comp.setBackground(new Color(144, 238, 144)); // Verde claro
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    comp.setBackground(Color.WHITE);
                }

                if (column >= 2 && value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(formato.format(value));
                } else if (column >= 2 && value != null && !value.toString().isEmpty()) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return comp;
            }
        };

        for (int i = 0; i < 6; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(categoriaRenderer);
        }

        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
    }

    private void llenarTabla() {
        modeloTabla.setRowCount(0);

        for (LineaPresupuesto linea : lineas) {
            Object[] fila = new Object[6];
            fila[0] = linea.codigo;
            fila[1] = linea.descripcion;
            fila[2] = linea.estimado;
            fila[3] = linea.min > 0 ? linea.min : "";
            fila[4] = linea.max > 0 ? linea.max : "";
            fila[5] = "";

            modeloTabla.addRow(fila);
        }

        // Calcular totales
        double totalEstimado = lineas.stream()
            .filter(l -> l.esCategoria)
            .mapToDouble(l -> l.estimado)
            .sum();

        double totalMin = lineas.stream()
            .filter(l -> l.esCategoria)
            .mapToDouble(l -> l.min)
            .sum();

        double totalMax = lineas.stream()
            .filter(l -> l.esCategoria)
            .mapToDouble(l -> l.max)
            .sum();

        // Fila de TOTAL PROYECTO
        Object[] filaTotal = new Object[6];
        filaTotal[0] = "";
        filaTotal[1] = "TOTAL PROYECTO";
        filaTotal[2] = totalEstimado;
        filaTotal[3] = totalMin;
        filaTotal[4] = totalMax;
        filaTotal[5] = "";
        modeloTabla.addRow(filaTotal);

        // Fila de CONTINGENCIA
        Object[] filaConting = new Object[6];
        filaConting[0] = "";
        filaConting[1] = "CONTINGENCIA";
        filaConting[2] = "";
        filaConting[3] = "";
        filaConting[4] = "20%";
        filaConting[5] = "";
        modeloTabla.addRow(filaConting);

        // Fila de TOTAL CON CONTINGENCIA
        Object[] filaTotalConting = new Object[6];
        filaTotalConting[0] = "";
        filaTotalConting[1] = "PROYECTO TOTAL CON CONTINGENCIA";
        filaTotalConting[2] = totalEstimado * 1.20;
        filaTotalConting[3] = "";
        filaTotalConting[4] = "";
        filaTotalConting[5] = "";
        modeloTabla.addRow(filaTotalConting);
    }

    private void actualizarTabla() {
        llenarTabla();
        tabla.revalidate();
        tabla.repaint();
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Resultados de la Simulación"));
        panel.setBackground(Color.WHITE);

        Font fuenteLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fuenteValor = new Font("Segoe UI", Font.PLAIN, 13);

        lblResultadoSimulacion = new JLabel("Pendiente", SwingConstants.CENTER);
        lblPromedio = new JLabel("--", SwingConstants.CENTER);
        lblDesviacion = new JLabel("--", SwingConstants.CENTER);
        lblMin = new JLabel("--", SwingConstants.CENTER);
        lblMax = new JLabel("--", SwingConstants.CENTER);

        lblResultadoSimulacion.setFont(fuenteValor);
        lblPromedio.setFont(fuenteValor);
        lblDesviacion.setFont(fuenteValor);
        lblMin.setFont(fuenteValor);
        lblMax.setFont(fuenteValor);

        panel.add(crearEtiqueta("Estado:", fuenteLabel));
        panel.add(crearEtiqueta("Promedio:", fuenteLabel));
        panel.add(crearEtiqueta("Desv. Est.:", fuenteLabel));
        panel.add(crearEtiqueta("Mín/Máx:", fuenteLabel));

        panel.add(lblResultadoSimulacion);
        panel.add(lblPromedio);
        panel.add(lblDesviacion);

        JPanel panelMinMax = new JPanel(new GridLayout(2, 1));
        panelMinMax.setBackground(Color.WHITE);
        panelMinMax.add(lblMin);
        panelMinMax.add(lblMax);
        panel.add(panelMinMax);

        return panel;
    }

    private JLabel crearEtiqueta(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(fuente);
        return lbl;
    }

    private void ejecutarSimulacion(int iteraciones) {
        lblResultadoSimulacion.setText("Simulando...");
        lblResultadoSimulacion.setForeground(Color.ORANGE);

        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            @Override
            protected double[] doInBackground() {
                resultadosSimulacion = new double[iteraciones];

                for (int i = 0; i < iteraciones; i++) {
                    double totalSimulacion = 0;

                    for (LineaPresupuesto linea : lineas) {
                        if (linea.esCategoria && linea.min > 0 && linea.max > 0) {
                            TriangularDistribution dist = new TriangularDistribution(
                                linea.min, linea.estimado, linea.max);
                            totalSimulacion += dist.sample();
                        }
                    }

                    resultadosSimulacion[i] = totalSimulacion;
                }

                return resultadosSimulacion;
            }

            @Override
            protected void done() {
                try {
                    double[] resultados = get();
                    actualizarResultados(resultados);
                    actualizarTablaConSimulacion();
                    mostrarHistograma(resultados, iteraciones);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SimuladorAleatorio.this,
                        "Error en la simulación: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void actualizarResultados(double[] resultados) {
        DecimalFormat df = new DecimalFormat("$#,##0.00");

        double suma = Arrays.stream(resultados).sum();
        double promedio = suma / resultados.length;

        double varianza = Arrays.stream(resultados)
            .map(x -> Math.pow(x - promedio, 2))
            .sum() / resultados.length;
        double desviacion = Math.sqrt(varianza);

        double min = Arrays.stream(resultados).min().orElse(0);
        double max = Arrays.stream(resultados).max().orElse(0);

        lblResultadoSimulacion.setText("Completado");
        lblResultadoSimulacion.setForeground(new Color(0, 150, 0));
        lblPromedio.setText(df.format(promedio));
        lblDesviacion.setText(df.format(desviacion));
        lblMin.setText("Mín: " + df.format(min));
        lblMax.setText("Máx: " + df.format(max));
    }

    private void actualizarTablaConSimulacion() {
        DecimalFormat df = new DecimalFormat("#,##0.00");

        int filaIdx = 0;
        for (LineaPresupuesto linea : lineas) {
            if (linea.esCategoria && linea.min > 0 && linea.max > 0) {
                TriangularDistribution dist = new TriangularDistribution(
                    linea.min, linea.estimado, linea.max);
                double valorSimulado = dist.sample();
                modeloTabla.setValueAt(valorSimulado, filaIdx, 5);
            }
            filaIdx++;
        }

        if (resultadosSimulacion != null && resultadosSimulacion.length > 0) {
            double promedioTotal = Arrays.stream(resultadosSimulacion).average().orElse(0);
            modeloTabla.setValueAt(promedioTotal, lineas.size(), 5);
        }
    }

    private void mostrarHistograma(double[] datos, int numSimulaciones) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Total Proyecto", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram(
            "TOTAL PROYECTO",
            "Costo Total ($)",
            "Frecuencia",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(new Color(255, 255, 204));
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 0, 255));
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        renderer.setShadowVisible(false);

        chart.setBackgroundPaint(Color.WHITE);

        DecimalFormat df = new DecimalFormat("#,##0.00");
        double promedio = Arrays.stream(datos).average().orElse(0);
        chart.addSubtitle(new org.jfree.chart.title.TextTitle(
            String.format("%d pruebas | Certeza: 100.00%% | Promedio: $%s",
                numSimulaciones, df.format(promedio)),
            new Font("Segoe UI", Font.PLAIN, 11)
        ));

        JFrame frameHistograma = new JFrame("Vista de Frecuencia - TOTAL PROYECTO");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 600));

        frameHistograma.setContentPane(chartPanel);
        frameHistograma.pack();
        frameHistograma.setLocationRelativeTo(this);
        frameHistograma.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            SimuladorAleatorio simulador = new SimuladorAleatorio();
            simulador.setVisible(true);
        });
    }
}
