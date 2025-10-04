package actividad_8.ejercicio_4.manual;

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
 * Simulador de Estimación de Costos con Ingreso Manual
 * Permite editar todos los valores de Estimado, Min y Max
 */
public class SimuladorManual extends JFrame {

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
    private JLabel lblTotalEstimado;
    private JLabel lblTotalMin;
    private JLabel lblTotalMax;
    private double[] resultadosSimulacion;

    public SimuladorManual() {
        super("Simulador de Estimación de Costos - Ingreso Manual");
        inicializarDatosVacios();
        configurarUI();
        setSize(1500, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Inicializa la estructura con valores en cero para ingreso manual
     */
    private void inicializarDatosVacios() {
        // Categoría 1
        lineas.add(new LineaPresupuesto("1", "ADMINSTRACION DEL PROYECTO", 0, 0, 0, true));

        // Categoría 2 - Ingeniería
        lineas.add(new LineaPresupuesto("21", "ENEGINEERING MANAGEMENT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("22", "TECHNICAL STUDIES", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("23", "DEFINITIVE DESIGN", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("24", "ENGINEERING INSPECTION", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("25", "EQUIPMENT REMOVAL DESINGN", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("2", "INGENIERIA", 0, 0, 0, true));

        // Categoría 3 - CENRTC
        lineas.add(new LineaPresupuesto("31", "CENRTC DEFINITIVE DESIGN", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("32", "CENRTC PROCUREMENT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("33", "CENRTC FABRICATION", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("3", "CENRTC", 0, 0, 0, true));

        // Categoría 4 - Construcción
        lineas.add(new LineaPresupuesto("41", "WHC CONSTRUCTION MANAGEMENT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("42", "INTER-FARM MODIFICATIONS", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("43", "C-FARM MODIFICATIONS", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("44", "AY-FARM MODIFICATIONS", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("45", "EXPENSE PROCUREMENT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("46", "FACILITY PREP", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("47", "CONSTRUCTION SERVICES", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("4", "CONSTRUCCION", 0, 0, 0, true));

        // Categoría 5 - Otros costos
        lineas.add(new LineaPresupuesto("51", "STARTUP ADMINISTRATION", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("52", "STARTUP SUPPORT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("54", "STARTUP READINESS PREVIEW", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("5", "OTROS COSTOS DEL PROYECTO", 0, 0, 0, true));

        // Categoría 6 - Seguridad
        lineas.add(new LineaPresupuesto("61", "ENVIROMENTAL MANAGEMENT", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("63", "SAFETY", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("64", "NEPA", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("65", "RCRA", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("66", "CAA", 0, 0, 0, false));
        lineas.add(new LineaPresupuesto("6", "SEGURIDAD & AMBIENTE", 0, 0, 0, true));
    }

    private void configurarUI() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior con tabla EDITABLE
        String[] columnas = {"Código", "Descripción", "Estimado", "Min", "Max", "Simulado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Solo las columnas 2, 3, 4 (Estimado, Min, Max) son editables
                return col >= 2 && col <= 4;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex >= 2 && columnIndex <= 5) {
                    return Double.class;
                }
                return String.class;
            }
        };

        tabla = new JTable(modeloTabla);
        configurarTabla();
        llenarTabla();

        JScrollPane scrollTabla = new JScrollPane(tabla);
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Proyecto de Estimación de Costos (Editable)"));

        // Panel de totales en tiempo real
        JPanel panelTotales = crearPanelTotales();

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));

        JButton btnCargarEjemplo = new JButton("Cargar Datos de Ejemplo");
        btnCargarEjemplo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCargarEjemplo.setBackground(new Color(34, 139, 34));
        btnCargarEjemplo.setForeground(Color.WHITE);
        btnCargarEjemplo.setFocusPainted(false);

        JButton btnLimpiar = new JButton("Limpiar Todo");
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLimpiar.setBackground(new Color(220, 53, 69));
        btnLimpiar.setForeground(Color.WHITE);
        btnLimpiar.setFocusPainted(false);

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo");
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnSimular.setBackground(new Color(30, 144, 255));
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFocusPainted(false);

        JSpinner spinnerIteraciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 10000, 1000));
        spinnerIteraciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        panelBotones.add(btnCargarEjemplo);
        panelBotones.add(btnLimpiar);
        panelBotones.add(new JLabel(" | Simulaciones:"));
        panelBotones.add(spinnerIteraciones);
        panelBotones.add(btnSimular);

        // Panel de estadísticas
        JPanel panelEstadisticas = crearPanelEstadisticas();

        // Panel central
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelCentral.add(scrollTabla, BorderLayout.NORTH);
        panelCentral.add(panelTotales, BorderLayout.CENTER);
        panelCentral.add(panelBotones, BorderLayout.SOUTH);

        // Panel inferior con estadísticas
        add(panelCentral, BorderLayout.CENTER);
        add(panelEstadisticas, BorderLayout.SOUTH);

        // Acciones de botones
        btnCargarEjemplo.addActionListener(e -> {
            cargarDatosEjemplo();
            actualizarTotales();
        });

        btnLimpiar.addActionListener(e -> {
            limpiarDatos();
            actualizarTotales();
        });

        btnSimular.addActionListener(e -> {
            if (validarDatos()) {
                sincronizarDatosTabla();
                int iteraciones = (int) spinnerIteraciones.getValue();
                ejecutarSimulacion(iteraciones);
            }
        });

        // Listener para actualizar totales al editar
        modeloTabla.addTableModelListener(e -> {
            SwingUtilities.invokeLater(() -> actualizarTotales());
        });
    }

    private void configurarTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(30);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(30, 144, 255));
        tabla.getTableHeader().setForeground(Color.WHITE);

        // Renderizador personalizado
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            DecimalFormat formato = new DecimalFormat("$#,##0.00");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Verificar si es categoría
                boolean esCategoria = false;
                if (row < lineas.size()) {
                    esCategoria = lineas.get(row).esCategoria;
                }

                if (esCategoria) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                    if (!isSelected) {
                        comp.setBackground(new Color(173, 216, 230)); // Azul claro
                    }
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                    if (!isSelected) {
                        comp.setBackground(Color.WHITE);
                    }
                }

                // Formatear números
                if (column >= 2 && value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    double val = ((Number) value).doubleValue();
                    if (val == 0 && column <= 4) {
                        setText("");
                    } else {
                        setText(formato.format(val));
                    }
                } else if (column >= 2) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return comp;
            }
        };

        for (int i = 0; i < 6; i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Editor para números con formato
        for (int i = 2; i <= 4; i++) {
            tabla.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) {
                @Override
                public Object getCellEditorValue() {
                    String text = ((JTextField) getComponent()).getText();
                    text = text.replace("$", "").replace(",", "").trim();
                    try {
                        return text.isEmpty() ? 0.0 : Double.parseDouble(text);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                }
            });
        }

        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(300);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(150);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(150);
    }

    private void llenarTabla() {
        modeloTabla.setRowCount(0);

        for (LineaPresupuesto linea : lineas) {
            Object[] fila = new Object[6];
            fila[0] = linea.codigo;
            fila[1] = linea.descripcion;
            fila[2] = linea.estimado;
            fila[3] = linea.min;
            fila[4] = linea.max;
            fila[5] = 0.0;

            modeloTabla.addRow(fila);
        }
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new GridLayout(1, 3, 20, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Totales Calculados (Solo Categorías Principales)"));
        panel.setBackground(Color.WHITE);

        Font fuenteLabel = new Font("Segoe UI", Font.BOLD, 14);
        Font fuenteValor = new Font("Segoe UI", Font.BOLD, 16);
        Color colorValor = new Color(0, 100, 0);

        lblTotalEstimado = new JLabel("$0.00", SwingConstants.CENTER);
        lblTotalMin = new JLabel("$0.00", SwingConstants.CENTER);
        lblTotalMax = new JLabel("$0.00", SwingConstants.CENTER);

        lblTotalEstimado.setFont(fuenteValor);
        lblTotalMin.setFont(fuenteValor);
        lblTotalMax.setFont(fuenteValor);

        lblTotalEstimado.setForeground(colorValor);
        lblTotalMin.setForeground(colorValor);
        lblTotalMax.setForeground(colorValor);

        JPanel p1 = new JPanel(new BorderLayout());
        p1.setBackground(Color.WHITE);
        p1.add(crearEtiqueta("TOTAL ESTIMADO:", fuenteLabel), BorderLayout.NORTH);
        p1.add(lblTotalEstimado, BorderLayout.CENTER);

        JPanel p2 = new JPanel(new BorderLayout());
        p2.setBackground(Color.WHITE);
        p2.add(crearEtiqueta("TOTAL MÍNIMO:", fuenteLabel), BorderLayout.NORTH);
        p2.add(lblTotalMin, BorderLayout.CENTER);

        JPanel p3 = new JPanel(new BorderLayout());
        p3.setBackground(Color.WHITE);
        p3.add(crearEtiqueta("TOTAL MÁXIMO:", fuenteLabel), BorderLayout.NORTH);
        p3.add(lblTotalMax, BorderLayout.CENTER);

        panel.add(p1);
        panel.add(p2);
        panel.add(p3);

        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10));
        panel.setBorder(BorderFactory.createTitledBorder("Resultados de la Simulación Monte Carlo"));
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

    private void actualizarTotales() {
        DecimalFormat df = new DecimalFormat("$#,##0.00");

        double totalEst = 0;
        double totalMin = 0;
        double totalMax = 0;

        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).esCategoria) {
                try {
                    Object valEst = modeloTabla.getValueAt(i, 2);
                    Object valMin = modeloTabla.getValueAt(i, 3);
                    Object valMax = modeloTabla.getValueAt(i, 4);

                    if (valEst instanceof Number) totalEst += ((Number) valEst).doubleValue();
                    if (valMin instanceof Number) totalMin += ((Number) valMin).doubleValue();
                    if (valMax instanceof Number) totalMax += ((Number) valMax).doubleValue();
                } catch (Exception e) {
                    // Ignorar errores durante edición
                }
            }
        }

        lblTotalEstimado.setText(df.format(totalEst));
        lblTotalMin.setText(df.format(totalMin));
        lblTotalMax.setText(df.format(totalMax));
    }

    private void cargarDatosEjemplo() {
        // Datos de ejemplo (los originales de la imagen)
        double[][] datos = {
            {4719278, 4500000, 5500000},  // 1
            {1344586, 0, 0}, {479725, 0, 0}, {10575071, 0, 0}, {5007916, 0, 0}, {2561272, 0, 0},
            {19968570, 19000000, 22000000}, // 2
            {668990, 0, 0}, {632731, 0, 0}, {902498, 0, 0},
            {2204219, 2000000, 2500000},    // 3
            {4976687, 0, 0}, {1307065, 0, 0}, {6602884, 0, 0}, {1636429, 0, 0},
            {4054629, 0, 0}, {9536166, 0, 0}, {7041973, 0, 0},
            {35155833, 34000000, 45000000}, // 4
            {1676355, 0, 0}, {1944661, 0, 0}, {1042521, 0, 0},
            {4663537, 4000000, 5500000},    // 5
            {424013, 0, 0}, {3579477, 0, 0}, {64106, 0, 0}, {11474, 0, 0}, {176869, 0, 0},
            {4255939, 4000000, 5000000}     // 6
        };

        for (int i = 0; i < datos.length && i < lineas.size(); i++) {
            modeloTabla.setValueAt(datos[i][0], i, 2);
            modeloTabla.setValueAt(datos[i][1], i, 3);
            modeloTabla.setValueAt(datos[i][2], i, 4);
        }
    }

    private void limpiarDatos() {
        for (int i = 0; i < lineas.size(); i++) {
            modeloTabla.setValueAt(0.0, i, 2);
            modeloTabla.setValueAt(0.0, i, 3);
            modeloTabla.setValueAt(0.0, i, 4);
            modeloTabla.setValueAt(0.0, i, 5);
        }

        lblResultadoSimulacion.setText("Pendiente");
        lblResultadoSimulacion.setForeground(Color.GRAY);
        lblPromedio.setText("--");
        lblDesviacion.setText("--");
        lblMin.setText("--");
        lblMax.setText("--");
    }

    private boolean validarDatos() {
        // Verificar que al menos una categoría tenga datos
        boolean hayDatos = false;
        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).esCategoria) {
                try {
                    double est = ((Number) modeloTabla.getValueAt(i, 2)).doubleValue();
                    double min = ((Number) modeloTabla.getValueAt(i, 3)).doubleValue();
                    double max = ((Number) modeloTabla.getValueAt(i, 4)).doubleValue();

                    if (est > 0 && min > 0 && max > 0) {
                        hayDatos = true;

                        // Validar coherencia
                        if (min > est || est > max) {
                            JOptionPane.showMessageDialog(this,
                                "Error en fila " + (i + 1) + ": El orden debe ser Min ≤ Estimado ≤ Max",
                                "Error de Validación", JOptionPane.ERROR_MESSAGE);
                            return false;
                        }
                    }
                } catch (Exception e) {
                    // Ignorar filas vacías
                }
            }
        }

        if (!hayDatos) {
            JOptionPane.showMessageDialog(this,
                "Debe ingresar al menos una categoría con Min, Estimado y Max",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void sincronizarDatosTabla() {
        for (int i = 0; i < lineas.size(); i++) {
            try {
                lineas.get(i).estimado = ((Number) modeloTabla.getValueAt(i, 2)).doubleValue();
                lineas.get(i).min = ((Number) modeloTabla.getValueAt(i, 3)).doubleValue();
                lineas.get(i).max = ((Number) modeloTabla.getValueAt(i, 4)).doubleValue();
            } catch (Exception e) {
                // Ignorar errores
            }
        }
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
                        if (linea.esCategoria && linea.min > 0 && linea.max > 0 && linea.estimado > 0) {
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
                    JOptionPane.showMessageDialog(SimuladorManual.this,
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
        for (int i = 0; i < lineas.size(); i++) {
            LineaPresupuesto linea = lineas.get(i);
            if (linea.esCategoria && linea.min > 0 && linea.max > 0 && linea.estimado > 0) {
                TriangularDistribution dist = new TriangularDistribution(
                    linea.min, linea.estimado, linea.max);
                double valorSimulado = dist.sample();
                modeloTabla.setValueAt(valorSimulado, i, 5);
            }
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
            SimuladorManual simulador = new SimuladorManual();
            simulador.setVisible(true);
        });
    }
}
