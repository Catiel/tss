package actividad_8.ejercicio_5;

import com.formdev.flatlaf.FlatLightLaf;
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
import java.util.Arrays;
import java.util.Random;

/**
 * Simulador de Optimización de Inventario con Valores Aleatorios
 * Los parámetros se generan aleatoriamente al iniciar
 */
public class SimuladorInventarioAleatorio extends JFrame {

    // Parámetros aleatorios del problema
    private double precioVenta;
    private double costo;
    private double precioDescuento;
    private int demandaMin;
    private int demandaMax;
    private int paso;
    private double probabilidad;
    private double cantidadComprada;
    private int demandaEjemplo;

    private double[] resultadosGanancia;
    private JTable tabla;
    private DefaultTableModel modeloTabla;
    private JTextArea txtDistribucion;
    private JLabel lblEstado;
    private JLabel lblMedia;
    private JLabel lblMediana;
    private JLabel lblModo;
    private JLabel lblDesviacion;
    private JLabel lblVarianza;
    private JLabel lblMin;
    private JLabel lblMax;

    public SimuladorInventarioAleatorio() {
        super("Simulador de Inventario - Valores Aleatorios");
        generarParametrosAleatorios();
        configurarUI();
        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void generarParametrosAleatorios() {
        Random rand = new Random();

        // Generar precio de venta entre $10 y $20
        precioVenta = 10.0 + rand.nextDouble() * 10.0;

        // Generar costo entre 50% y 80% del precio de venta
        costo = precioVenta * (0.5 + rand.nextDouble() * 0.3);

        // Generar precio con descuento entre el costo y el precio de venta
        precioDescuento = costo + rand.nextDouble() * (precioVenta - costo) * 0.5;

        // Generar rango de demanda
        demandaMin = 20 + rand.nextInt(30); // Entre 20 y 50
        paso = 5 + rand.nextInt(11); // Entre 5 y 15
        int numValores = 4 + rand.nextInt(4); // Entre 4 y 7 valores
        demandaMax = demandaMin + (paso * (numValores - 1));

        // Calcular probabilidad uniforme
        probabilidad = 1.0 / numValores;

        // Generar cantidad comprada en el rango de demanda
        cantidadComprada = demandaMin + rand.nextInt(demandaMax - demandaMin + 1);

        // Generar demanda de ejemplo
        int[] valoresPosibles = generarValoresPosibles();
        demandaEjemplo = valoresPosibles[rand.nextInt(valoresPosibles.length)];
    }

    private int[] generarValoresPosibles() {
        int numValores = (demandaMax - demandaMin) / paso + 1;
        int[] valores = new int[numValores];
        int idx = 0;
        for (int valor = demandaMin; valor <= demandaMax; valor += paso) {
            valores[idx++] = valor;
        }
        return valores;
    }

    private void configurarUI() {
        setLayout(new BorderLayout(10, 10));

        JPanel panelSuperior = crearPanelParametros();
        JPanel panelCentral = crearPanelSimulacion();
        JPanel panelInferior = crearPanelEstadisticas();

        add(panelSuperior, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private JPanel crearPanelParametros() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] columnas = {"Parámetros", "Valores"};
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
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Parámetros del Problema (Generados Aleatoriamente)"));
        scrollTabla.setPreferredSize(new Dimension(600, 250));

        JPanel panelTablaYBoton = new JPanel(new BorderLayout(5, 5));
        panelTablaYBoton.add(scrollTabla, BorderLayout.CENTER);

        JButton btnRegenerar = new JButton("🔄 Regenerar Parámetros");
        btnRegenerar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnRegenerar.setBackground(new Color(255, 153, 0));
        btnRegenerar.setForeground(Color.WHITE);
        btnRegenerar.setFocusPainted(false);
        btnRegenerar.addActionListener(e -> regenerarParametros());
        panelTablaYBoton.add(btnRegenerar, BorderLayout.SOUTH);

        panel.add(panelTablaYBoton, BorderLayout.WEST);

        JPanel panelDistribucion = crearPanelDistribucion();
        panel.add(panelDistribucion, BorderLayout.CENTER);

        return panel;
    }

    private void regenerarParametros() {
        generarParametrosAleatorios();
        modeloTabla.setRowCount(0);
        llenarTabla();
        actualizarDistribucion();
        limpiarEstadisticas();
        JOptionPane.showMessageDialog(this,
            "Parámetros regenerados exitosamente.\nEjecute la simulación para ver los nuevos resultados.",
            "Parámetros Actualizados",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void limpiarEstadisticas() {
        lblEstado.setText("Pendiente");
        lblEstado.setForeground(Color.GRAY);
        lblMedia.setText("--");
        lblMediana.setText("--");
        lblModo.setText("--");
        lblDesviacion.setText("--");
        lblVarianza.setText("--");
        lblMin.setText("--");
        lblMax.setText("--");
        resultadosGanancia = null;
    }

    private void configurarTabla() {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tabla.setRowHeight(28);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabla.getTableHeader().setBackground(new Color(255, 153, 0));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setEnabled(false);

        DefaultTableCellRenderer valueRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (row == 3 && column == 1) {
                    setBackground(new Color(146, 208, 80));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (row == 4 && column == 1) {
                    setBackground(new Color(0, 176, 240));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else if (row == 6 && column == 1) {
                    setBackground(new Color(0, 176, 240));
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                } else {
                    setBackground(Color.WHITE);
                    setFont(new Font("Segoe UI", Font.PLAIN, 13));
                }

                if (column == 0) {
                    setFont(new Font("Segoe UI", Font.BOLD, 13));
                }

                return c;
            }
        };

        tabla.getColumnModel().getColumn(1).setCellRenderer(valueRenderer);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(250);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150);
    }

    private void llenarTabla() {
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("$ #,##0");

        double gananciaEjemplo = calcularGanancia(demandaEjemplo, cantidadComprada);

        modeloTabla.addRow(new Object[]{"Precio de venta", dfMoney.format(precioVenta)});
        modeloTabla.addRow(new Object[]{"Costo", dfMoney.format(costo)});
        modeloTabla.addRow(new Object[]{"Precio con descuento", dfMoney.format(precioDescuento)});
        modeloTabla.addRow(new Object[]{"Demanda", dfInt.format(demandaEjemplo)});
        modeloTabla.addRow(new Object[]{"Cantidad comprada", dfInt.format(cantidadComprada)});
        modeloTabla.addRow(new Object[]{"-", "-"});
        modeloTabla.addRow(new Object[]{"Ganancia", dfMoney.format(gananciaEjemplo)});
    }

    private JPanel crearPanelDistribucion() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Distribución Personalizada - Demanda"));

        txtDistribucion = new JTextArea();
        txtDistribucion.setEditable(false);
        txtDistribucion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        txtDistribucion.setBackground(new Color(255, 255, 224));

        actualizarDistribucion();

        JScrollPane scroll = new JScrollPane(txtDistribucion);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarDistribucion() {
        StringBuilder sb = new StringBuilder();
        sb.append("Distribución Discreta Uniforme:\n\n");
        sb.append(String.format("Rango: $%d - $%d\n", demandaMin, demandaMax));
        sb.append(String.format("Paso: $%d\n", paso));
        sb.append(String.format("Probabilidad por valor: %.4f (%.2f%%)\n\n", probabilidad, probabilidad * 100));
        sb.append("Valores posibles:\n");

        for (int valor = demandaMin; valor <= demandaMax; valor += paso) {
            sb.append(String.format("  $%d - Prob: %.2f%%\n", valor, probabilidad * 100));
        }

        txtDistribucion.setText(sb.toString());
    }

    private JPanel crearPanelSimulacion() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblSimulaciones = new JLabel("Número de Simulaciones:");
        lblSimulaciones.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JSpinner spinnerSimulaciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 100000, 1000));
        spinnerSimulaciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo");
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSimular.setBackground(new Color(30, 144, 255));
        btnSimular.setForeground(Color.WHITE);
        btnSimular.setFocusPainted(false);
        btnSimular.setPreferredSize(new Dimension(350, 50));

        panel.add(lblSimulaciones);
        panel.add(spinnerSimulaciones);
        panel.add(btnSimular);

        btnSimular.addActionListener(e -> {
            int numSimulaciones = (int) spinnerSimulaciones.getValue();
            ejecutarSimulacion(numSimulaciones);
        });

        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10));
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Resultados de la Simulación"),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        panel.setBackground(Color.WHITE);

        Font fuenteLabel = new Font("Segoe UI", Font.BOLD, 13);
        Font fuenteValor = new Font("Segoe UI", Font.PLAIN, 13);

        lblEstado = new JLabel("Pendiente", SwingConstants.CENTER);
        lblMedia = new JLabel("--", SwingConstants.CENTER);
        lblMediana = new JLabel("--", SwingConstants.CENTER);
        lblModo = new JLabel("--", SwingConstants.CENTER);
        lblDesviacion = new JLabel("--", SwingConstants.CENTER);
        lblVarianza = new JLabel("--", SwingConstants.CENTER);
        lblMin = new JLabel("--", SwingConstants.CENTER);
        lblMax = new JLabel("--", SwingConstants.CENTER);

        lblEstado.setFont(fuenteValor);
        lblMedia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblMediana.setFont(fuenteValor);
        lblModo.setFont(fuenteValor);
        lblDesviacion.setFont(fuenteValor);
        lblVarianza.setFont(fuenteValor);
        lblMin.setFont(fuenteValor);
        lblMax.setFont(fuenteValor);

        panel.add(crearEtiqueta("Estado:", fuenteLabel));
        panel.add(crearEtiqueta("Media:", fuenteLabel));
        panel.add(crearEtiqueta("Mediana:", fuenteLabel));
        panel.add(crearEtiqueta("Modo:", fuenteLabel));

        panel.add(lblEstado);
        panel.add(lblMedia);
        panel.add(lblMediana);
        panel.add(lblModo);

        panel.add(crearEtiqueta("Desv. Est.:", fuenteLabel));
        panel.add(crearEtiqueta("Varianza:", fuenteLabel));
        panel.add(crearEtiqueta("Mínimo:", fuenteLabel));
        panel.add(crearEtiqueta("Máximo:", fuenteLabel));

        panel.add(lblDesviacion);
        panel.add(lblVarianza);
        panel.add(lblMin);
        panel.add(lblMax);

        return panel;
    }

    private JLabel crearEtiqueta(String texto, Font fuente) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(fuente);
        return lbl;
    }

    private int generarDemanda(Random random) {
        int[] valoresPosibles = generarValoresPosibles();
        return valoresPosibles[random.nextInt(valoresPosibles.length)];
    }

    private double calcularGanancia(int demanda, double cantidad) {
        double margenVenta = precioVenta - precioDescuento;
        double margenDescuento = precioDescuento - costo;

        if (demanda <= cantidad) {
            return margenVenta * demanda - margenDescuento * cantidad;
        } else {
            return margenVenta * cantidad;
        }
    }

    private void ejecutarSimulacion(int iteraciones) {
        lblEstado.setText("Simulando...");
        lblEstado.setForeground(Color.ORANGE);

        SwingWorker<double[], Void> worker = new SwingWorker<>() {
            @Override
            protected double[] doInBackground() {
                resultadosGanancia = new double[iteraciones];
                Random random = new Random();

                for (int i = 0; i < iteraciones; i++) {
                    int demanda = generarDemanda(random);
                    double ganancia = calcularGanancia(demanda, cantidadComprada);
                    resultadosGanancia[i] = ganancia;
                }

                return resultadosGanancia;
            }

            @Override
            protected void done() {
                try {
                    double[] resultados = get();
                    actualizarEstadisticas(resultados);
                    actualizarTabla();
                    mostrarHistograma(resultados, iteraciones);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(SimuladorInventarioAleatorio.this,
                        "Error en la simulación: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute();
    }

    private void actualizarEstadisticas(double[] resultados) {
        DecimalFormat df = new DecimalFormat("$#,##0.00");

        double suma = Arrays.stream(resultados).sum();
        double media = suma / resultados.length;

        double varianza = Arrays.stream(resultados)
            .map(x -> Math.pow(x - media, 2))
            .sum() / resultados.length;
        double desviacion = Math.sqrt(varianza);

        double[] ordenados = resultados.clone();
        Arrays.sort(ordenados);
        double mediana;
        if (ordenados.length % 2 == 0) {
            mediana = (ordenados[ordenados.length / 2 - 1] + ordenados[ordenados.length / 2]) / 2;
        } else {
            mediana = ordenados[ordenados.length / 2];
        }

        double min = Arrays.stream(resultados).min().orElse(0);
        double max = Arrays.stream(resultados).max().orElse(0);
        double modo = calcularModo(resultados);

        lblEstado.setText("Completado (" + resultados.length + " pruebas)");
        lblEstado.setForeground(new Color(0, 150, 0));
        lblMedia.setText(df.format(media));
        lblMedia.setForeground(new Color(0, 100, 200));
        lblMediana.setText(df.format(mediana));
        lblModo.setText(df.format(modo));
        lblDesviacion.setText(df.format(desviacion));
        lblVarianza.setText(df.format(varianza));
        lblMin.setText(df.format(min));
        lblMax.setText(df.format(max));
    }

    private double calcularModo(double[] datos) {
        Arrays.sort(datos);
        double moda = datos[0];
        int maxFrecuencia = 1;
        int frecuenciaActual = 1;
        double valorActual = datos[0];

        for (int i = 1; i < datos.length; i++) {
            if (Math.abs(datos[i] - valorActual) < 1.0) {
                frecuenciaActual++;
            } else {
                if (frecuenciaActual > maxFrecuencia) {
                    maxFrecuencia = frecuenciaActual;
                    moda = valorActual;
                }
                valorActual = datos[i];
                frecuenciaActual = 1;
            }
        }

        if (frecuenciaActual > maxFrecuencia) {
            moda = valorActual;
        }

        return moda;
    }

    private void actualizarTabla() {
        DecimalFormat df = new DecimalFormat("$#,##0.00");

        if (resultadosGanancia != null && resultadosGanancia.length > 0) {
            double media = Arrays.stream(resultadosGanancia).average().orElse(0);
            modeloTabla.setValueAt(df.format(media), 6, 1);
        }
    }

    private void mostrarHistograma(double[] datos, int numSimulaciones) {
        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Ganancia", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram(
            "Distribución de Ganancia",
            "Ganancia ($)",
            "Frecuencia",
            dataset,
            PlotOrientation.VERTICAL,
            false,
            true,
            false
        );

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(new Color(200, 200, 200));
        plot.setRangeGridlinePaint(new Color(200, 200, 200));

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 112, 192));
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        renderer.setShadowVisible(false);

        chart.setBackgroundPaint(Color.WHITE);

        DecimalFormat df = new DecimalFormat("$#,##0.00");
        double media = Arrays.stream(datos).average().orElse(0);
        chart.addSubtitle(new org.jfree.chart.title.TextTitle(
            String.format("%d pruebas | Media: %s | Certeza: 100.00%%",
                numSimulaciones, df.format(media)),
            new Font("Segoe UI", Font.PLAIN, 12)
        ));

        JFrame frameHistograma = new JFrame("Vista de Frecuencia - Ganancia");
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
            SimuladorInventarioAleatorio simulador = new SimuladorInventarioAleatorio();
            simulador.setVisible(true);
        });
    }
}