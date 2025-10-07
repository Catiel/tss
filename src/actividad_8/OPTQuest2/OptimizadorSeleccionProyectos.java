package actividad_8.OPTQuest2;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.stream.IntStream;

public class OptimizadorSeleccionProyectos extends JFrame {

    // Configuración del problema
    private static class Config {
        static final double[] GANANCIA = {750000, 1500000, 600000, 1800000, 1250000, 150000, 900000, 250000};
        static final double[] PROB_EXITO = {0.90, 0.70, 0.60, 0.40, 0.80, 0.60, 0.70, 0.90};
        static final double[] INVERSION = {250000, 650000, 250000, 500000, 700000, 30000, 350000, 70000};
        static final double PRESUPUESTO = 2000000;
        static final int NUM_PROYECTOS = 8;
        static final int NUM_SIMULACIONES = 213;
        static final int NUM_PRUEBAS_MC = 5000;
    }

    // Formatters estáticos
    private static final DecimalFormat FMT_MONEY = new DecimalFormat("$#,##0");
    private static final DecimalFormat FMT_PERCENT = new DecimalFormat("0%");

    // Componentes UI
    private DefaultTableModel modeloTabla;
    private JLabel[] lblTotales = new JLabel[4];
    private JProgressBar progressBar, progressBarPruebas;
    private JLabel lblSimulaciones, lblPruebas;
    private JButton btnOptimizar, btnGraficas;

    // Estado de optimización
    private double mejorGanancia = Double.NEGATIVE_INFINITY;
    private int[] mejorDecision = new int[Config.NUM_PROYECTOS];
    private List<Double> gananciasFinales = new ArrayList<>();

    public OptimizadorSeleccionProyectos() {
        super("Selección de proyectos por restricción de presupuesto");
        configurarUI();
        setSize(1500, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configurarUI() {
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30);
        main.add(crearTitulo(), BorderLayout.NORTH);
        main.add(crearTabla(), BorderLayout.CENTER);

        JPanel sur = new JPanel(new BorderLayout(10, 10));
        sur.setBackground(Color.WHITE);
        sur.add(crearPanelTotales(), BorderLayout.NORTH);
        sur.add(crearPanelControl(), BorderLayout.CENTER);

        main.add(sur, BorderLayout.SOUTH);
        add(main);
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) {
        JPanel panel = new JPanel(layout);
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left));
        return panel;
    }

    private JPanel crearTitulo() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(Color.WHITE);
        JLabel titulo = new JLabel("Selección de proyectos por restricción de presupuesto");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(31, 78, 120));
        panel.add(titulo);
        return panel;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"Proyecto", "Decisión", "Ganancia\nesperada", "Prob. De\nÉxito",
                        "Retorno\nEsperado", "Inversión\nInicial", "Beneficio\nEsperado"};

        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = new JTable(modeloTabla);
        configurarEstiloTabla(tabla);
        llenarTablaInicial();

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    private void configurarEstiloTabla(JTable tabla) {
        tabla.setFont(new Font("Calibri", Font.PLAIN, 13));
        tabla.setRowHeight(35);
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(79, 129, 189));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setGridColor(new Color(200, 200, 200));

        Color[] colores = {new Color(197, 217, 241), new Color(255, 255, 0),
                          new Color(146, 208, 80), new Color(146, 208, 80),
                          Color.WHITE, Color.WHITE, new Color(0, 176, 240)};

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.CENTER);
                setFont(new Font("Calibri", c == 0 || c == 1 ? Font.BOLD : Font.PLAIN, 12));
                setBackground(colores[c]);
                setForeground(Color.BLACK);
                return this;
            }
        };

        IntStream.range(0, tabla.getColumnCount()).forEach(i -> {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 100 : 110);
        });
    }

    private void llenarTablaInicial() {
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) {
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i];
            modeloTabla.addRow(new Object[]{
                String.valueOf(i + 1), "1",
                FMT_MONEY.format(Config.GANANCIA[i]),
                FMT_PERCENT.format(Config.PROB_EXITO[i]),
                FMT_MONEY.format(retorno),
                FMT_MONEY.format(Config.INVERSION[i]),
                FMT_MONEY.format(0)
            });
        }
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String[] titulos = {"Presupuesto", "Inversión", "Sobrante", "Ganancia total"};
        Color[] colores = {Color.WHITE, Color.WHITE, new Color(255, 192, 0), new Color(0, 176, 240)};
        double[] valores = {Config.PRESUPUESTO, 0, 0, 0};

        for (int i = 0; i < 4; i++) {
            lblTotales[i] = crearLabelTotal(titulos[i], valores[i], colores[i]);
            panel.add(lblTotales[i]);
        }
        return panel;
    }

    private JLabel crearLabelTotal(String titulo, double valor, Color bg) {
        JLabel lbl = new JLabel("<html><center>" + titulo + "<br><b>" +
                               FMT_MONEY.format(valor) + "</b></center></html>");
        lbl.setFont(new Font("Calibri", Font.PLAIN, 14));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBackground(bg);
        lbl.setForeground(Color.BLACK);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        lbl.setPreferredSize(new Dimension(150, 50));
        return lbl;
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        // Botones
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBtns.setBackground(Color.WHITE);

        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)",
                                  new Color(68, 114, 196), 320, 45);
        btnGraficas = crearBoton("Ver Gráficas", new Color(112, 173, 71), 180, 40);
        btnGraficas.setEnabled(false);

        btnOptimizar.addActionListener(e -> ejecutarOptimizacion());
        btnGraficas.addActionListener(e -> mostrarHistograma());

        panelBtns.add(btnOptimizar);
        panelBtns.add(btnGraficas);

        // Barras de progreso
        JPanel panelProgress = crearPanelProgreso();

        panel.add(panelBtns);
        panel.add(panelProgress);
        return panel;
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 15 ? 15 : 14));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(ancho, alto));
        return btn;
    }

    private JPanel crearPanelProgreso() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest"));

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8));
        barras.setBackground(Color.WHITE);
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales",
                                         new Color(0, 32, 96), out -> lblSimulaciones = out);
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas",
                                                new Color(0, 176, 80), out -> lblPruebas = out);

        barras.add(progressBar.getParent());
        barras.add(progressBarPruebas.getParent());

        panel.add(barras);
        return panel;
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color,
                                           java.util.function.Consumer<JLabel> labelOut) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JLabel lbl = new JLabel(texto + ": 0 / " + max);
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10));
        labelOut.accept(lbl);

        JProgressBar bar = new JProgressBar(0, max);
        bar.setPreferredSize(new Dimension(500, 22));
        bar.setForeground(color);
        bar.setBackground(Color.WHITE);

        panel.add(lbl, BorderLayout.WEST);
        panel.add(bar, BorderLayout.CENTER);
        panel.add(new JLabel(max + "  "), BorderLayout.EAST);

        return bar;
    }

    private void ejecutarOptimizacion() {
        btnOptimizar.setEnabled(false);
        btnGraficas.setEnabled(false);
        gananciasFinales.clear();
        mejorGanancia = Double.NEGATIVE_INFINITY;

        new SwingWorker<Void, int[]>() {
            protected Void doInBackground() {
                Random rand = new Random();

                for (int sim = 0; sim < Config.NUM_SIMULACIONES; sim++) {
                    int[] decision = generarDecision(rand);
                    List<Double> ganancias = new ArrayList<>();

                    for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) {
                        double ganancia = simular(decision, rand);
                        if (calcularInversion(decision) <= Config.PRESUPUESTO) {
                            ganancias.add(ganancia);
                        }
                        if (mc % 250 == 0) publish(new int[]{sim + 1, mc + 1});
                    }

                    if (!ganancias.isEmpty()) {
                        double media = ganancias.stream().mapToDouble(d -> d).average().orElse(0);
                        if (media > mejorGanancia) {
                            mejorGanancia = media;
                            mejorDecision = decision.clone();
                            gananciasFinales = new ArrayList<>(ganancias);
                        }
                    }
                }
                return null;
            }

            protected void process(List<int[]> chunks) {
                int[] ultimo = chunks.get(chunks.size() - 1);
                progressBar.setValue(ultimo[0]);
                progressBarPruebas.setValue(ultimo[1]);
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES);
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC);
            }

            protected void done() {
                actualizarResultados();
                btnOptimizar.setEnabled(true);
                btnGraficas.setEnabled(true);
                mostrarVentanaResultados();
            }
        }.execute();
    }

    private int[] generarDecision(Random rand) {
        int[] dec = new int[Config.NUM_PROYECTOS];
        do {
            for (int i = 0; i < Config.NUM_PROYECTOS; i++) {
                dec[i] = rand.nextBoolean() ? 1 : 0;
            }
        } while (calcularInversion(dec) > Config.PRESUPUESTO && rand.nextDouble() > 0.3);
        return dec;
    }

    private double calcularInversion(int[] decision) {
        return IntStream.range(0, Config.NUM_PROYECTOS)
                       .mapToDouble(i -> decision[i] * Config.INVERSION[i])
                       .sum();
    }

    private double simular(int[] decision, Random rand) {
        double ganancia = 0;
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) {
            if (decision[i] == 1) {
                double g = generarGanancia(i, rand);
                int exito = rand.nextDouble() < Config.PROB_EXITO[i] ? 1 : 0;
                ganancia += (g * exito) - Config.INVERSION[i];
            }
        }
        return ganancia;
    }

    private double generarGanancia(int proyecto, Random rand) {
        return switch (proyecto) {
            case 0 -> new NormalDistribution(750000, 75000).sample();
            case 1 -> triangular(1250000, 1500000, 1600000, rand);
            case 2 -> logNormal(600000, 50000, rand);
            case 3 -> triangular(1600000, 1800000, 1900000, rand);
            case 4 -> new UniformRealDistribution(1150000, 1350000).sample();
            case 5 -> logNormal(150000, 30000, rand);
            case 6 -> new NormalDistribution(900000, 50000).sample();
            case 7 -> triangular(220000, 250000, 320000, rand);
            default -> 0;
        };
    }

    private double triangular(double min, double mode, double max, Random rand) {
        double u = rand.nextDouble();
        double f = (mode - min) / (max - min);
        return u < f ? min + Math.sqrt(u * (max - min) * (mode - min))
                     : max - Math.sqrt((1 - u) * (max - min) * (max - mode));
    }

    private double logNormal(double media, double std, Random rand) {
        double var = std * std;
        double mu = Math.log(media * media / Math.sqrt(var + media * media));
        double sigma = Math.sqrt(Math.log(1 + var / (media * media)));
        return new LogNormalDistribution(mu, sigma).sample();
    }

    private void actualizarResultados() {
        double inv = calcularInversion(mejorDecision);

        for (int i = 0; i < Config.NUM_PROYECTOS; i++) {
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i];
            double beneficio = (retorno - Config.INVERSION[i]) * mejorDecision[i];
            modeloTabla.setValueAt(String.valueOf(mejorDecision[i]), i, 1);
            modeloTabla.setValueAt(FMT_MONEY.format(beneficio), i, 6);
        }

        actualizarLabel(lblTotales[1], "Inversión", inv);
        actualizarLabel(lblTotales[2], "Sobrante", Config.PRESUPUESTO - inv);
        actualizarLabel(lblTotales[3], "Ganancia total", mejorGanancia);
    }

    private void actualizarLabel(JLabel lbl, String titulo, double valor) {
        lbl.setText("<html><center>" + titulo + "<br><b>" + FMT_MONEY.format(valor) + "</b></center></html>");
    }

    private void mostrarVentanaResultados() {
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false);
        dlg.setLayout(new BorderLayout());

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15);
        main.add(new JLabel("Optimización terminada. Todas las variables enumeradas."),
                BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 5));
        tablas.setBackground(Color.WHITE);
        tablas.add(crearSeccionResultado("Objetivos", "Valor",
            new String[]{"Maximizar Media Total profit"},
            new String[]{FMT_MONEY.format(mejorGanancia)}));
        tablas.add(crearSeccionResultado("Restricciones", "Lado izq <= Lado der",
            new String[]{"Inversión <= Presupuesto"},
            new String[]{FMT_MONEY.format(calcularInversion(mejorDecision)) + " <= " +
                        FMT_MONEY.format(Config.PRESUPUESTO)}));

        String[][] vars = new String[Config.NUM_PROYECTOS][2];
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) {
            vars[i] = new String[]{"Project " + (i + 1), String.valueOf(mejorDecision[i])};
        }
        tablas.add(crearTablaVariables(vars));

        main.add(tablas, BorderLayout.CENTER);
        dlg.add(main);
        dlg.setSize(700, 480);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.setBackground(Color.WHITE);

        if (filas.length > 0) {
            JPanel grid = new JPanel(new GridLayout(filas.length, 2, 10, 5));
            grid.setBackground(Color.WHITE);
            for (int i = 0; i < filas.length; i++) {
                grid.add(new JLabel(filas[i]));
                grid.add(new JLabel(vals[i], SwingConstants.RIGHT));
            }
            panel.add(grid);
        }
        return panel;
    }

    private JPanel crearTablaVariables(String[][] datos) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Variables de decisión"));
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"", "Valor"}, 0);
        Arrays.stream(datos).forEach(modelo::addRow);
        panel.add(new JScrollPane(new JTable(modelo)));
        return panel;
    }

    private void mostrarHistograma() {
        double[] datos = gananciasFinales.stream().mapToDouble(d -> d).toArray();
        if (datos.length == 0) return;

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Total profit", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram("Total profit", "Dollars",
            "Frecuencia", dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 112, 192));
        renderer.setShadowVisible(false);

        double media = Arrays.stream(datos).average().orElse(0);
        plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(media) {{
            setPaint(Color.BLACK);
            setLabel("Media = " + FMT_MONEY.format(media));
        }});

        ((NumberAxis) plot.getDomainAxis()).setNumberFormatOverride(FMT_MONEY);

        JFrame frame = new JFrame("Previsión: Total profit");
        frame.setContentPane(new ChartPanel(chart));
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(new FlatLightLaf()); }
        catch (Exception e) { e.printStackTrace(); }

        SwingUtilities.invokeLater(() ->
            new OptimizadorSeleccionProyectos().setVisible(true));
    }
}