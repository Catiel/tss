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
 * Permite editar subcategorías y Min/Max de categorías
 * Las categorías se calculan automáticamente
 */
public class SimuladorManual extends JFrame {

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
            this.estimado = 0;
            this.min = 0;
            this.max = 0;
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
    private boolean actualizandoAutomaticamente = false;

    // Índices de las filas de totales
    private int filaIndexTotalProyecto;
    private int filaIndexContingencia;
    private int filaIndexTotalConContingencia;
    private final double PORCENTAJE_CONTINGENCIA = 0.20; // 20%

    public SimuladorManual() {
        super("Simulador de Estimación de Costos - Ingreso Manual");
        inicializarEstructura();
        configurarUI();
        setSize(1500, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void inicializarEstructura() {
        // 11 - Big Co. PROJECT MANAGEMENT
        lineas.add(new LineaPresupuesto("11", "Big Co. PROYECT MANAGEMENT", false));

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

        // Guardar índices de filas de totales
        filaIndexTotalProyecto = lineas.size();
        filaIndexContingencia = lineas.size() + 1;
        filaIndexTotalConContingencia = lineas.size() + 2;
    }

    private void configurarUI() {
        setLayout(new BorderLayout(10, 10));

        String[] columnas = {"Código", "Descripción", "Estimado", "Min", "Max", "Simulado"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                if (row >= lineas.size()) return false; // Filas de totales no editables

                LineaPresupuesto linea = lineas.get(row);

                // Subcategorías: solo columna Estimado (columna 2)
                if (!linea.esCategoria) {
                    return col == 2;
                }

                // Categorías: solo columnas Min y Max (columnas 3 y 4)
                return col == 3 || col == 4;
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
        scrollTabla.setBorder(BorderFactory.createTitledBorder(
            "Proyecto de Estimación de Costos - Edite subcategorías (Estimado) y categorías (Min/Max)"));

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

        JPanel panelEstadisticas = crearPanelEstadisticas();

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelCentral.add(scrollTabla, BorderLayout.CENTER);
        panelCentral.add(panelBotones, BorderLayout.SOUTH);

        add(panelCentral, BorderLayout.CENTER);
        add(panelEstadisticas, BorderLayout.SOUTH);

        btnCargarEjemplo.addActionListener(e -> {
            cargarDatosEjemplo();
            calcularSumas();
        });

        btnLimpiar.addActionListener(e -> {
            limpiarDatos();
            calcularSumas();
        });

        btnSimular.addActionListener(e -> {
            if (validarDatos()) {
                sincronizarDatosTabla();
                int iteraciones = (int) spinnerIteraciones.getValue();
                ejecutarSimulacion(iteraciones);
            }
        });

        modeloTabla.addTableModelListener(e -> {
            if (!actualizandoAutomaticamente) {
                SwingUtilities.invokeLater(() -> {
                    calcularSumas();
                });
            }
        });
    }

    private void calcularSumas() {
        actualizandoAutomaticamente = true;

        // Categoría 1: suma de Big Co. (índice 0)
        double suma1 = getValorTabla(0, 2);
        modeloTabla.setValueAt(suma1, 1, 2);

        // Categoría 2: suma de índices 2-6
        double suma2 = 0;
        for (int i = 2; i <= 6; i++) {
            suma2 += getValorTabla(i, 2);
        }
        modeloTabla.setValueAt(suma2, 7, 2);

        // Categoría 3: suma de índices 8-10
        double suma3 = 0;
        for (int i = 8; i <= 10; i++) {
            suma3 += getValorTabla(i, 2);
        }
        modeloTabla.setValueAt(suma3, 11, 2);

        // Categoría 4: suma de índices 12-18
        double suma4 = 0;
        for (int i = 12; i <= 18; i++) {
            suma4 += getValorTabla(i, 2);
        }
        modeloTabla.setValueAt(suma4, 19, 2);

        // Categoría 5: suma de índices 20-22
        double suma5 = 0;
        for (int i = 20; i <= 22; i++) {
            suma5 += getValorTabla(i, 2);
        }
        modeloTabla.setValueAt(suma5, 23, 2);

        // Categoría 6: suma de índices 24-28
        double suma6 = 0;
        for (int i = 24; i <= 28; i++) {
            suma6 += getValorTabla(i, 2);
        }
        modeloTabla.setValueAt(suma6, 29, 2);

        // Calcular totales del proyecto
        actualizarFilasTotales();

        actualizandoAutomaticamente = false;
    }

    private void actualizarFilasTotales() {
        // Calcular TOTAL PROYECTO (suma de todas las categorías)
        double totalEstimado = 0;
        double totalMin = 0;
        double totalMax = 0;

        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).esCategoria) {
                totalEstimado += getValorTabla(i, 2);
                totalMin += getValorTabla(i, 3);
                totalMax += getValorTabla(i, 4);
            }
        }

        // Actualizar fila TOTAL PROYECTO
        modeloTabla.setValueAt(totalEstimado, filaIndexTotalProyecto, 2);
        modeloTabla.setValueAt(totalMin, filaIndexTotalProyecto, 3);
        modeloTabla.setValueAt(totalMax, filaIndexTotalProyecto, 4);

        // Actualizar fila CONTINGENCIA (mostrar como texto el porcentaje)
        modeloTabla.setValueAt("20%", filaIndexContingencia, 2);

        // Calcular TOTAL CON CONTINGENCIA
        double totalConContingencia = totalEstimado * (1 + PORCENTAJE_CONTINGENCIA);
        modeloTabla.setValueAt(totalConContingencia, filaIndexTotalConContingencia, 2);
    }

    private double getValorTabla(int fila, int columna) {
        try {
            Object val = modeloTabla.getValueAt(fila, columna);
            if (val instanceof Number) {
                return ((Number) val).doubleValue();
            }
        } catch (Exception e) {
            // Ignorar
        }
        return 0.0;
    }

    private void configurarTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tabla.getTableHeader().setBackground(new Color(255, 153, 51));
        tabla.getTableHeader().setForeground(Color.WHITE);

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            DecimalFormat formato = new DecimalFormat("$#,##0");

            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                boolean esCategoria = false;
                boolean esFilaTotal = false;

                if (row < lineas.size()) {
                    esCategoria = lineas.get(row).esCategoria;
                } else if (row == filaIndexTotalProyecto || row == filaIndexTotalConContingencia) {
                    esFilaTotal = true;
                } else if (row == filaIndexContingencia) {
                    // Fila de contingencia
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (!isSelected) {
                        comp.setBackground(new Color(255, 255, 153)); // Amarillo claro
                    }
                    setHorizontalAlignment(column >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT);
                    return comp;
                }

                if (esCategoria || esFilaTotal) {
                    setFont(new Font("Segoe UI", Font.BOLD, 12));
                    if (!isSelected) {
                        if (esFilaTotal) {
                            comp.setBackground(new Color(255, 255, 153)); // Amarillo claro para totales
                        } else {
                            comp.setBackground(new Color(144, 238, 144)); // Verde claro para categorías
                        }
                    }
                } else {
                    setFont(new Font("Segoe UI", Font.PLAIN, 12));
                    if (!isSelected) {
                        comp.setBackground(Color.WHITE);
                    }
                }

                if (column >= 2 && value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    double val = ((Number) value).doubleValue();
                    if (val == 0) {
                        setText("");
                    } else {
                        setText(formato.format(val));
                    }
                } else if (column >= 2 && value instanceof String) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText((String) value);
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

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(350);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120);
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120);
    }

    private void llenarTabla() {
        modeloTabla.setRowCount(0);

        // Agregar líneas de presupuesto
        for (LineaPresupuesto linea : lineas) {
            Object[] fila = new Object[6];
            fila[0] = linea.codigo;
            fila[1] = linea.descripcion;
            fila[2] = 0.0;
            fila[3] = 0.0;
            fila[4] = 0.0;
            fila[5] = 0.0;

            modeloTabla.addRow(fila);
        }

        // Agregar fila de TOTAL PROYECTO
        Object[] filaTotal = new Object[6];
        filaTotal[0] = "";
        filaTotal[1] = "TOTAL PROYECTO";
        filaTotal[2] = 0.0;
        filaTotal[3] = 0.0;
        filaTotal[4] = 0.0;
        filaTotal[5] = 0.0;
        modeloTabla.addRow(filaTotal);

        // Agregar fila de CONTINGENCIA
        Object[] filaConting = new Object[6];
        filaConting[0] = "";
        filaConting[1] = "CONTINGENCIA";
        filaConting[2] = "20%";
        filaConting[3] = "";
        filaConting[4] = "";
        filaConting[5] = "";
        modeloTabla.addRow(filaConting);

        // Agregar fila de TOTAL CON CONTINGENCIA
        Object[] filaTotalConting = new Object[6];
        filaTotalConting[0] = "";
        filaTotalConting[1] = "PROYECTO TOTAL CON CONTINGENCIA";
        filaTotalConting[2] = 0.0;
        filaTotalConting[3] = "";
        filaTotalConting[4] = "";
        filaTotalConting[5] = "";
        modeloTabla.addRow(filaTotalConting);
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

    private void cargarDatosEjemplo() {
        actualizandoAutomaticamente = true;

        // Subcategorías con valores
        modeloTabla.setValueAt(4719278.0, 0, 2); // Big Co

        modeloTabla.setValueAt(1344586.0, 2, 2);
        modeloTabla.setValueAt(479725.0, 3, 2);
        modeloTabla.setValueAt(10575071.0, 4, 2);
        modeloTabla.setValueAt(5007916.0, 5, 2);
        modeloTabla.setValueAt(2561272.0, 6, 2);

        modeloTabla.setValueAt(668990.0, 8, 2);
        modeloTabla.setValueAt(632731.0, 9, 2);
        modeloTabla.setValueAt(902498.0, 10, 2);

        modeloTabla.setValueAt(4976687.0, 12, 2);
        modeloTabla.setValueAt(1307065.0, 13, 2);
        modeloTabla.setValueAt(6602884.0, 14, 2);
        modeloTabla.setValueAt(1636429.0, 15, 2);
        modeloTabla.setValueAt(4054629.0, 16, 2);
        modeloTabla.setValueAt(9536166.0, 17, 2);
        modeloTabla.setValueAt(7041973.0, 18, 2);

        modeloTabla.setValueAt(1676355.0, 20, 2);
        modeloTabla.setValueAt(1944661.0, 21, 2);
        modeloTabla.setValueAt(1042521.0, 22, 2);

        modeloTabla.setValueAt(424013.0, 24, 2);
        modeloTabla.setValueAt(3579477.0, 25, 2);
        modeloTabla.setValueAt(64106.0, 26, 2);
        modeloTabla.setValueAt(11474.0, 27, 2);
        modeloTabla.setValueAt(176869.0, 28, 2);

        // Min y Max de categorías
        modeloTabla.setValueAt(4500000.0, 1, 3);
        modeloTabla.setValueAt(5500000.0, 1, 4);

        modeloTabla.setValueAt(19000000.0, 7, 3);
        modeloTabla.setValueAt(22000000.0, 7, 4);

        modeloTabla.setValueAt(2000000.0, 11, 3);
        modeloTabla.setValueAt(2500000.0, 11, 4);

        modeloTabla.setValueAt(34000000.0, 19, 3);
        modeloTabla.setValueAt(45000000.0, 19, 4);

        modeloTabla.setValueAt(4000000.0, 23, 3);
        modeloTabla.setValueAt(5500000.0, 23, 4);

        modeloTabla.setValueAt(4000000.0, 29, 3);
        modeloTabla.setValueAt(5000000.0, 29, 4);

        actualizandoAutomaticamente = false;
    }

    private void limpiarDatos() {
        actualizandoAutomaticamente = true;
        for (int i = 0; i < lineas.size(); i++) {
            modeloTabla.setValueAt(0.0, i, 2);
            modeloTabla.setValueAt(0.0, i, 3);
            modeloTabla.setValueAt(0.0, i, 4);
            modeloTabla.setValueAt(0.0, i, 5);
        }

        // Limpiar filas de totales
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 2);
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 3);
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 4);
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 5);

        modeloTabla.setValueAt("20%", filaIndexContingencia, 2);

        modeloTabla.setValueAt(0.0, filaIndexTotalConContingencia, 2);
        modeloTabla.setValueAt(0.0, filaIndexTotalConContingencia, 5);

        actualizandoAutomaticamente = false;

        lblResultadoSimulacion.setText("Pendiente");
        lblResultadoSimulacion.setForeground(Color.GRAY);
        lblPromedio.setText("--");
        lblDesviacion.setText("--");
        lblMin.setText("--");
        lblMax.setText("--");
    }

    private boolean validarDatos() {
        boolean hayDatos = false;
        for (int i = 0; i < lineas.size(); i++) {
            if (lineas.get(i).esCategoria) {
                double est = getValorTabla(i, 2);
                double min = getValorTabla(i, 3);
                double max = getValorTabla(i, 4);

                if (est > 0 && min > 0 && max > 0) {
                    hayDatos = true;

                    if (min > est || est > max) {
                        JOptionPane.showMessageDialog(this,
                            "Error en " + lineas.get(i).descripcion + ": Min ≤ Estimado ≤ Max",
                            "Error de Validación", JOptionPane.ERROR_MESSAGE);
                        return false;
                    }
                }
            }
        }

        if (!hayDatos) {
            JOptionPane.showMessageDialog(this,
                "Ingrese al menos una categoría completa (con Min, Estimado y Max)",
                "Advertencia", JOptionPane.WARNING_MESSAGE);
            return false;
        }

        return true;
    }

    private void sincronizarDatosTabla() {
        for (int i = 0; i < lineas.size(); i++) {
            lineas.get(i).estimado = getValorTabla(i, 2);
            lineas.get(i).min = getValorTabla(i, 3);
            lineas.get(i).max = getValorTabla(i, 4);
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
        actualizandoAutomaticamente = true;

        double totalSimulado = 0;

        // Actualizar valores simulados para cada categoría
        for (int i = 0; i < lineas.size(); i++) {
            LineaPresupuesto linea = lineas.get(i);
            if (linea.esCategoria && linea.min > 0 && linea.max > 0 && linea.estimado > 0) {
                TriangularDistribution dist = new TriangularDistribution(
                    linea.min, linea.estimado, linea.max);
                double valorSimulado = dist.sample();
                modeloTabla.setValueAt(valorSimulado, i, 5);
                totalSimulado += valorSimulado;
            }
        }

        // Actualizar TOTAL PROYECTO simulado
        if (resultadosSimulacion != null && resultadosSimulacion.length > 0) {
            double promedioTotal = Arrays.stream(resultadosSimulacion).average().orElse(0);
            modeloTabla.setValueAt(promedioTotal, filaIndexTotalProyecto, 5);

            // Actualizar TOTAL CON CONTINGENCIA simulado
            double totalConContingenciaSimulado = promedioTotal * (1 + PORCENTAJE_CONTINGENCIA);
            modeloTabla.setValueAt(totalConContingenciaSimulado, filaIndexTotalConContingencia, 5);
        }

        actualizandoAutomaticamente = false;
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
            SimuladorManual simulador = new SimuladorManual();
            simulador.setVisible(true);
        });
    }
}