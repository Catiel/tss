package actividad_8.ejercicio_4;

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

/**
 * Simulador de Estimación de Costos de Proyecto
 * Replica funcionalidad de Crystal Ball usando distribuciones triangulares
 */
public class SimuladorProyecto extends JFrame {

    // Datos del proyecto
    private static class LineaPresupuesto {
        String codigo;
        String descripcion;
        double estimado;
        double min;
        double max;
        boolean esCategoria;

        LineaPresupuesto(String codigo, String desc, double est, double min, double max, boolean cat) {
            this.codigo = codigo;
            this.descripcion = desc;
            this.estimado = est;
            this.min = min;
            this.max = max;
            this.esCategoria = cat;
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

    public SimuladorProyecto() {
        super("Simulador de Estimación de Costos - Proyecto");
        inicializarDatos();
        configurarUI();
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void inicializarDatos() {
        // 11 - Big Co. PROJECT MANAGEMENT
        lineas.add(new LineaPresupuesto("11", "Big Co. PROYECT MANAGEMENT", 4719278, 0, 0, false));

        // 1 - Administración del proyecto
        lineas.add(new LineaPresupuesto("1", "ADMINSTRACION DEL PROYECTO", 4719278, 4500000, 5500000, true));

        // 2 - Ingeniería (categoría padre)
        lineas.add(new LineaPresupuesto("21", "ENEGINEERING MANAGEMENT", 1344586, 0, 0, false));
        lineas.add(new LineaPresupuesto("22", "TECHNICAL STUDIES", 479725, 0, 0, false));
        lineas.add(new LineaPresupuesto("23", "DEFINITIVE DESIGN", 10575071, 0, 0, false));
        lineas.add(new LineaPresupuesto("24", "ENGINEERING INSPECTION", 5007916, 0, 0, false));
        lineas.add(new LineaPresupuesto("25", "EQUIPMENT REMOVAL DESINGN", 2561272, 0, 0, false));
        lineas.add(new LineaPresupuesto("2", "INGENIERIA", 19968570, 19000000, 22000000, true));

        // 3 - CENRTC (categoría padre)
        lineas.add(new LineaPresupuesto("31", "CENRTC DEFINITIVE DESIGN", 668990, 0, 0, false));
        lineas.add(new LineaPresupuesto("32", "CENRTC PROCUREMENT", 632731, 0, 0, false));
        lineas.add(new LineaPresupuesto("33", "CENRTC FABRICATION", 902498, 0, 0, false));
        lineas.add(new LineaPresupuesto("3", "CENRTC", 2204219, 2000000, 2500000, true));

        // 4 - Construcción (categoría padre)
        lineas.add(new LineaPresupuesto("41", "WHC CONSTRUCTION MANAGEMENT", 4976687, 0, 0, false));
        lineas.add(new LineaPresupuesto("42", "INTER-FARM MODIFICATIONS", 1307065, 0, 0, false));
        lineas.add(new LineaPresupuesto("43", "C-FARM MODIFICATIONS", 6602884, 0, 0, false));
        lineas.add(new LineaPresupuesto("44", "AY-FARM MODIFICATIONS", 1636429, 0, 0, false));
        lineas.add(new LineaPresupuesto("45", "EXPENSE PROCUREMENT", 4054629, 0, 0, false));
        lineas.add(new LineaPresupuesto("46", "FACILITY PREP", 9536166, 0, 0, false));
        lineas.add(new LineaPresupuesto("47", "CONSTRUCTION SERVICES", 7041973, 0, 0, false));
        lineas.add(new LineaPresupuesto("4", "CONSTRUCCION", 35155833, 34000000, 45000000, true));

        // 5 - Otros costos (categoría padre)
        lineas.add(new LineaPresupuesto("51", "STARTUP ADMINISTRATION", 1676355, 0, 0, false));
        lineas.add(new LineaPresupuesto("52", "STARTUP SUPPORT", 1944661, 0, 0, false));
        lineas.add(new LineaPresupuesto("54", "STARTUP READINESS PREVIEW", 1042521, 0, 0, false));
        lineas.add(new LineaPresupuesto("5", "OTROS COSTOS DEL PROYECTO", 4663537, 4000000, 5500000, true));

        // 6 - Seguridad y ambiente (categoría padre)
        lineas.add(new LineaPresupuesto("61", "ENVIROMENTAL MANAGEMENT", 424013, 0, 0, false));
        lineas.add(new LineaPresupuesto("63", "SAFETY", 3579477, 0, 0, false));
        lineas.add(new LineaPresupuesto("64", "NEPA", 64106, 0, 0, false));
        lineas.add(new LineaPresupuesto("65", "RCRA", 11474, 0, 0, false));
        lineas.add(new LineaPresupuesto("66", "CAA", 176869, 0, 0, false));
        lineas.add(new LineaPresupuesto("6", "SEGURIDAD & AMBIENTE", 4255939, 4000000, 5000000, true));
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
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Proyecto de Estimación de Costos"));

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo");
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimular.setBackground(new Color(30, 144, 255));
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFocusPainted(false);

        JSpinner spinnerIteraciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 10000, 1000));
        spinnerIteraciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panelBotones.add(new JLabel("Número de simulaciones:"));
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

        // Acción del botón
        btnSimular.addActionListener(e -> {
            int iteraciones = (int) spinnerIteraciones.getValue();
            ejecutarSimulacion(iteraciones);
        });
    }

    private void configurarTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(255, 153, 51)); // Naranja como en Excel
        tabla.getTableHeader().setForeground(Color.WHITE);

        // Renderizador para formato moneda
        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            DecimalFormat formato = new DecimalFormat("$#,##0");
            @Override
            protected void setValue(Object value) {
                if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(formato.format(value));
                } else {
                    super.setValue(value);
                }
            }
        };

        // Renderizador especial para categorías (filas en negrita)
        DefaultTableCellRenderer categoryRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Verificar si es una categoría (las que tienen código de 1 dígito)
                String codigo = (String) table.getValueAt(row, 0);
                boolean esCategoria = codigo.length() == 1 && !codigo.isEmpty();

                if (esCategoria) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                }

                return c;
            }
        };

        // Aplicar renderizadores
        tabla.getColumnModel().getColumn(0).setCellRenderer(categoryRenderer);
        tabla.getColumnModel().getColumn(1).setCellRenderer(categoryRenderer);

        for (int i = 2; i < 6; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer);
        }

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(350);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);
    }

    private void llenarTabla() {
        DecimalFormat df = new DecimalFormat("#,##0");

        for (LineaPresupuesto linea : lineas) {
            Object[] fila = new Object[6];
            fila[0] = linea.codigo;
            fila[1] = linea.descripcion;
            fila[2] = linea.estimado;
            fila[3] = linea.min > 0 ? linea.min : "";
            fila[4] = linea.max > 0 ? linea.max : "";
            fila[5] = linea.esCategoria ? linea.estimado : ""; // Mostrar simulado para categorías

            modeloTabla.addRow(fila);
        }

        // Fila de TOTAL PROYECTO
        Object[] filaTotal = new Object[6];
        filaTotal[0] = "";
        filaTotal[1] = "TOTAL PROYECTO";
        filaTotal[2] = 70967376.0;
        filaTotal[3] = 67500000.0;
        filaTotal[4] = 85500000.0;
        filaTotal[5] = 70967376.0;
        modeloTabla.addRow(filaTotal);

        // Fila de CONTINGENCIA
        Object[] filaConting = new Object[6];
        filaConting[0] = "";
        filaConting[1] = "CONTINGENCIA";
        filaConting[2] = "20%";
        filaConting[3] = "";
        filaConting[4] = "";
        filaConting[5] = "";
        modeloTabla.addRow(filaConting);

        // Fila de TOTAL CON CONTINGENCIA
        Object[] filaTotalConting = new Object[6];
        filaTotalConting[0] = "";
        filaTotalConting[1] = "PROYECTO TOTAL CON CONTINGENCIA";
        filaTotalConting[2] = 85160851.0;
        filaTotalConting[3] = "";
        filaTotalConting[4] = "";
        filaTotalConting[5] = "";
        modeloTabla.addRow(filaTotalConting);
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

                    // Simular cada categoría
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
                    JOptionPane.showMessageDialog(SimuladorProyecto.this,
                        "Error en la simulación: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void actualizarResultados(double[] resultados) {
        DecimalFormat df = new DecimalFormat("$#,##0");

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
        DecimalFormat df = new DecimalFormat("#,##0");

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

        // Actualizar total proyecto con color verde claro
        if (resultadosSimulacion != null && resultadosSimulacion.length > 0) {
            double promedioTotal = Arrays.stream(resultadosSimulacion).average().orElse(0);
            modeloTabla.setValueAt(promedioTotal, lineas.size(), 5);

            // Aplicar color verde a las celdas de la columna "Simulado" para las categorías
            tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
                DecimalFormat formato = new DecimalFormat("$#,##0");
                @Override
                public Component getTableCellRendererComponent(JTable table, Object value,
                        boolean isSelected, boolean hasFocus, int row, int column) {
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                    setHorizontalAlignment(SwingConstants.RIGHT);

                    if (value instanceof Number) {
                        setText(formato.format(value));
                        // Fondo verde claro para valores simulados
                        if (!isSelected) {
                            setBackground(new Color(144, 238, 144));
                        }
                    } else {
                        setText("");
                        if (!isSelected) {
                            setBackground(Color.WHITE);
                        }
                    }

                    return c;
                }
            });
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

        // Añadir información de certeza
        DecimalFormat df = new DecimalFormat("#,##0");
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
            SimuladorProyecto simulador = new SimuladorProyecto();
            simulador.setVisible(true);
        });
    }
}