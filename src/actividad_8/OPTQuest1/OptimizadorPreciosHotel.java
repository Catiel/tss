package actividad_8.OPTQuest1;

import com.formdev.flatlaf.FlatLightLaf;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class OptimizadorPreciosHotel extends JFrame {

    private static final double[] PRECIOS_INICIAL = {85.00, 98.00, 139.00};
    private static final double[] DEMANDA_PROMEDIO = {250, 100, 50};
    private static final double[] GANANCIA_INICIAL = {21250.00, 9800.00, 6950.00};

    private static final double[][] ELASTICIDAD_LIMITES = {{-4.50, -1.50}, // Standard
            {-1.50, -0.50}, // Gold
            {-3.00, -1.00}  // Platinum
    };

    private static final double[][] PRECIO_LIMITES = {{70.00, 90.00},   // Standard
            {90.00, 110.00},  // Gold
            {120.00, 149.00}  // Platinum
    };

    private static final int CAPACIDAD_MAXIMA = 450;
    private static final double PERCENTIL_OBJETIVO = 0.80;
    private static final int NUM_SIMULACIONES = 1000; // Iteraciones de optimización
    private static final int NUM_PRUEBAS_MC = 5000; // Pruebas Monte Carlo por simulación

    private JTable tablaHotel;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalDemanda;
    private JLabel lblTotalGanancia;
    private JLabel lblCapacidad;
    private JProgressBar progressBar;
    private JProgressBar progressBarPruebas;
    private JLabel lblSimulacionesActual;
    private JLabel lblPruebasActual;
    private JButton btnOptimizar;
    private JButton btnGraficas;

    private double mejorGananciaTotal = Double.NEGATIVE_INFINITY;
    private double mejorDemandaTotal = 0;
    private double[] mejoresPrecios = new double[3];
    private double[] mejoresElasticidades = new double[3];
    private double[] mejoresProyeccionesDemanda = new double[3];
    private double[] mejoresProyeccionesGanancia = new double[3];

    private List<ResultadoSimulacion> historialMejoras;
    private List<Double> todasLasGanancias;
    private List<Double> todasLasDemandas;
    private List<Double> gananciasFinales; // Para histograma final
    private List<Double> demandasFinales;  // Para histograma final

    public OptimizadorPreciosHotel() {
        super("Problema: precios de cuartos de hotel");
        historialMejoras = new ArrayList<>();
        todasLasGanancias = new ArrayList<>();
        todasLasDemandas = new ArrayList<>();
        gananciasFinales = new ArrayList<>();
        demandasFinales = new ArrayList<>();
        configurarUI();
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configurarUI() {
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(Color.WHITE);

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        // Título
        JLabel lblTitulo = new JLabel("Problema: precios de cuartos de hotel");
        lblTitulo.setFont(new Font("Calibri", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(31, 78, 120));

        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(Color.WHITE);
        panelTitulo.add(lblTitulo);

        // Tabla principal
        JPanel panelTabla = crearTablaPrincipal();

        // Panel de totales y capacidad
        JPanel panelTotales = crearPanelTotales();

        // Panel de control
        JPanel panelControl = crearPanelControl();

        panelPrincipal.add(panelTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        JPanel panelSur = new JPanel(new BorderLayout(10, 10));
        panelSur.setBackground(Color.WHITE);
        panelSur.add(panelTotales, BorderLayout.NORTH);
        panelSur.add(panelControl, BorderLayout.CENTER);

        panelPrincipal.add(panelSur, BorderLayout.SOUTH);

        add(panelPrincipal);
    }

    private JPanel crearTablaPrincipal() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);

        String[] columnas = {"Tipo de habitación", "Precio", "Demanda\ndiaria\npromedio", "Ganancia", "Elasticidad", "Nuevo precio", "Proyección\nde\ndemanda", "Proyección\nde\nganancia"};

        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        tablaHotel = new JTable(modeloTabla);
        configurarTabla();
        llenarTablaInicial();

        JScrollPane scrollTabla = new JScrollPane(tablaHotel);
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
        scrollTabla.setBackground(Color.WHITE);

        panel.add(scrollTabla, BorderLayout.CENTER);

        return panel;
    }

    private void configurarTabla() {
        tablaHotel.setFont(new Font("Calibri", Font.PLAIN, 14));
        tablaHotel.setRowHeight(45);
        tablaHotel.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 13));
        tablaHotel.getTableHeader().setBackground(new Color(242, 220, 219));
        tablaHotel.getTableHeader().setForeground(Color.BLACK);
        tablaHotel.getTableHeader().setReorderingAllowed(false);
        tablaHotel.setGridColor(new Color(200, 200, 200));

        // Renderizador personalizado
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Calibri", Font.PLAIN, 14));

                // Primera columna (nombres) - negrita e itálica
                if (column == 0) {
                    setFont(new Font("Calibri", Font.BOLD | Font.ITALIC, 14));
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBackground(Color.WHITE);
                }
                // Columna Elasticidad - verde
                else if (column == 4) {
                    setBackground(new Color(0, 176, 80));
                    setForeground(Color.BLACK);
                    setFont(new Font("Calibri", Font.BOLD, 14));
                }
                // Columna Nuevo precio - amarillo
                else if (column == 5) {
                    setBackground(new Color(255, 255, 0));
                    setForeground(Color.BLACK);
                    setFont(new Font("Calibri", Font.BOLD, 14));
                }
                // Otras columnas - blanco
                else {
                    setBackground(Color.WHITE);
                    setForeground(Color.BLACK);
                }

                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200)));

                return c;
            }
        };

        for (int i = 0; i < tablaHotel.getColumnCount(); i++) {
            tablaHotel.getColumnModel().getColumn(i).setCellRenderer(renderer);
        }

        // Ajustar anchos
        tablaHotel.getColumnModel().getColumn(0).setPreferredWidth(180);
        tablaHotel.getColumnModel().getColumn(1).setPreferredWidth(100);
        tablaHotel.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaHotel.getColumnModel().getColumn(3).setPreferredWidth(120);
        tablaHotel.getColumnModel().getColumn(4).setPreferredWidth(100);
        tablaHotel.getColumnModel().getColumn(5).setPreferredWidth(120);
        tablaHotel.getColumnModel().getColumn(6).setPreferredWidth(120);
        tablaHotel.getColumnModel().getColumn(7).setPreferredWidth(140);
    }

    private void llenarTablaInicial() {
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("0");

        String[] tipos = {"Standard", "Gold", "Platinum"};

        for (int i = 0; i < 3; i++) {
            modeloTabla.addRow(new Object[]{tipos[i], dfMoney.format(PRECIOS_INICIAL[i]), dfInt.format(DEMANDA_PROMEDIO[i]), dfMoney.format(GANANCIA_INICIAL[i]), "-3",  // Ejemplo
                    dfMoney.format(PRECIOS_INICIAL[i]), dfInt.format(DEMANDA_PROMEDIO[i]), dfMoney.format(GANANCIA_INICIAL[i])});
        }
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10));
        panel.setBackground(Color.WHITE);

        // Total
        JPanel panelTotal = new JPanel(new BorderLayout(10, 5));
        panelTotal.setBackground(Color.WHITE);

        JLabel lblTotal = new JLabel("Total", SwingConstants.CENTER);
        lblTotal.setFont(new Font("Calibri", Font.BOLD, 14));

        JPanel panelValoresTotal = new JPanel(new GridLayout(1, 2, 10, 0));
        panelValoresTotal.setBackground(Color.WHITE);

        lblTotalDemanda = new JLabel("400", SwingConstants.CENTER);
        lblTotalDemanda.setFont(new Font("Calibri", Font.BOLD, 16));
        lblTotalDemanda.setBackground(new Color(0, 255, 255));
        lblTotalDemanda.setOpaque(true);
        lblTotalDemanda.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lblTotalDemanda.setPreferredSize(new Dimension(80, 35));

        lblTotalGanancia = new JLabel("$38.000,00", SwingConstants.CENTER);
        lblTotalGanancia.setFont(new Font("Calibri", Font.BOLD, 16));
        lblTotalGanancia.setBackground(new Color(0, 255, 255));
        lblTotalGanancia.setOpaque(true);
        lblTotalGanancia.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lblTotalGanancia.setPreferredSize(new Dimension(120, 35));

        panelValoresTotal.add(lblTotalDemanda);
        panelValoresTotal.add(lblTotalGanancia);

        panelTotal.add(lblTotal, BorderLayout.NORTH);
        panelTotal.add(panelValoresTotal, BorderLayout.CENTER);

        // Capacity
        JPanel panelCapacity = new JPanel(new BorderLayout(10, 5));
        panelCapacity.setBackground(Color.WHITE);

        JLabel lblCapacityLabel = new JLabel("Capacity", SwingConstants.CENTER);
        lblCapacityLabel.setFont(new Font("Calibri", Font.BOLD, 14));

        lblCapacidad = new JLabel("450", SwingConstants.CENTER);
        lblCapacidad.setFont(new Font("Calibri", Font.BOLD, 16));
        lblCapacidad.setForeground(new Color(255, 102, 0));
        lblCapacidad.setBackground(Color.WHITE);
        lblCapacidad.setOpaque(true);
        lblCapacidad.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        lblCapacidad.setPreferredSize(new Dimension(80, 35));

        panelCapacity.add(lblCapacityLabel, BorderLayout.NORTH);
        panelCapacity.add(lblCapacidad, BorderLayout.CENTER);

        panel.add(panelTotal);
        panel.add(panelCapacity);

        return panel;
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setBackground(Color.WHITE);

        btnOptimizar = new JButton("Ejecutar Optimización (OptQuest)");
        btnOptimizar.setFont(new Font("Calibri", Font.BOLD, 16));
        btnOptimizar.setBackground(new Color(68, 114, 196));
        btnOptimizar.setForeground(Color.WHITE);
        btnOptimizar.setFocusPainted(false);
        btnOptimizar.setPreferredSize(new Dimension(350, 50));

        btnGraficas = new JButton("Ver Gráficas");
        btnGraficas.setFont(new Font("Calibri", Font.BOLD, 14));
        btnGraficas.setBackground(new Color(112, 173, 71));
        btnGraficas.setForeground(Color.WHITE);
        btnGraficas.setFocusPainted(false);
        btnGraficas.setPreferredSize(new Dimension(200, 40));
        btnGraficas.setEnabled(false);

        panelBotones.add(btnOptimizar);
        panelBotones.add(btnGraficas);

        // Progress bars - Panel de control de OptQuest
        JPanel panelProgress = new JPanel(new BorderLayout(10, 10));
        panelProgress.setBackground(Color.WHITE);
        panelProgress.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new Font("Calibri", Font.BOLD, 12)));

        JPanel panelBarras = new JPanel(new GridLayout(2, 1, 5, 10));
        panelBarras.setBackground(Color.WHITE);
        panelBarras.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Barra de simulaciones (azul oscuro)
        JPanel panelSim = new JPanel(new BorderLayout(5, 5));
        panelSim.setBackground(Color.WHITE);

        lblSimulacionesActual = new JLabel("Simulaciones totales: 0 / " + NUM_SIMULACIONES);
        lblSimulacionesActual.setFont(new Font("Calibri", Font.PLAIN, 11));

        progressBar = new JProgressBar(0, NUM_SIMULACIONES);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(500, 25));
        progressBar.setForeground(new Color(0, 32, 96)); // Azul oscuro
        progressBar.setBackground(Color.WHITE);

        panelSim.add(lblSimulacionesActual, BorderLayout.WEST);
        panelSim.add(progressBar, BorderLayout.CENTER);
        JLabel lblMaxSim = new JLabel(NUM_SIMULACIONES + "  ");
        lblMaxSim.setFont(new Font("Calibri", Font.PLAIN, 11));
        panelSim.add(lblMaxSim, BorderLayout.EAST);

        // Barra de pruebas (verde)
        JPanel panelPruebas = new JPanel(new BorderLayout(5, 5));
        panelPruebas.setBackground(Color.WHITE);

        lblPruebasActual = new JLabel("Pruebas: 0 / " + NUM_PRUEBAS_MC);
        lblPruebasActual.setFont(new Font("Calibri", Font.PLAIN, 11));

        progressBarPruebas = new JProgressBar(0, NUM_PRUEBAS_MC);
        progressBarPruebas.setStringPainted(false);
        progressBarPruebas.setPreferredSize(new Dimension(500, 25));
        progressBarPruebas.setForeground(new Color(0, 176, 80)); // Verde
        progressBarPruebas.setBackground(Color.WHITE);

        panelPruebas.add(lblPruebasActual, BorderLayout.WEST);
        panelPruebas.add(progressBarPruebas, BorderLayout.CENTER);
        JLabel lblMaxPruebas = new JLabel(NUM_PRUEBAS_MC + "  ");
        lblMaxPruebas.setFont(new Font("Calibri", Font.PLAIN, 11));
        panelPruebas.add(lblMaxPruebas, BorderLayout.EAST);

        panelBarras.add(panelSim);
        panelBarras.add(panelPruebas);

        panelProgress.add(panelBarras, BorderLayout.CENTER);

        panel.add(panelBotones);
        panel.add(panelProgress);

        // Listeners
        btnOptimizar.addActionListener(e -> {
            btnOptimizar.setEnabled(false);
            btnGraficas.setEnabled(false);
            ejecutarOptimizacion();
        });

        btnGraficas.addActionListener(e -> mostrarGraficas());

        return panel;
    }

    private void ejecutarOptimizacion() {
        progressBar.setValue(0);
        progressBarPruebas.setValue(0);
        historialMejoras.clear();
        todasLasGanancias.clear();
        todasLasDemandas.clear();
        gananciasFinales.clear();
        demandasFinales.clear();
        mejorGananciaTotal = Double.NEGATIVE_INFINITY;

        SwingWorker<Void, ProgressUpdate> worker = new SwingWorker<>() {
            private long totalPruebasEjecutadas = 0;

            @Override
            protected Void doInBackground() {
                Random random = new Random();
                long tiempoInicio = System.currentTimeMillis();

                System.out.println("=== INICIANDO OPTIMIZACIÓN ===");
                System.out.println("Simulaciones: " + NUM_SIMULACIONES);
                System.out.println("Pruebas por simulación: " + NUM_PRUEBAS_MC);
                System.out.println("Total esperado: " + (NUM_SIMULACIONES * NUM_PRUEBAS_MC) + " evaluaciones");

                // Hacer 1000 simulaciones, cada una con 5000 pruebas MC
                for (int iter = 0; iter < NUM_SIMULACIONES; iter++) {
                    // Generar una nueva combinación de precios para esta simulación
                    double precioStandard = generarPrecioAleatorio(PRECIO_LIMITES[0], random);
                    double precioGold = generarPrecioAleatorio(PRECIO_LIMITES[1], random);
                    double precioPlatinum = generarPrecioAleatorio(PRECIO_LIMITES[2], random);

                    double sumaGanancias = 0;
                    double sumaDemandas = 0;
                    int simulacionesValidas = 0;

                    ResultadoOptimizacion ultimoResultado = null;
                    List<Double> gananciasTemp = new ArrayList<>();
                    List<Double> demandasTemp = new ArrayList<>();

                    // EJECUTAR EXACTAMENTE 5000 PRUEBAS Monte Carlo para esta combinación
                    for (int mc = 0; mc < NUM_PRUEBAS_MC; mc++) {
                        ResultadoOptimizacion resultado = simularConPrecios(precioStandard, precioGold, precioPlatinum, random);

                        totalPruebasEjecutadas++;

                        gananciasTemp.add(resultado.gananciaTotal);
                        demandasTemp.add(resultado.demandaTotal);

                        if (resultado.demandaTotal <= CAPACIDAD_MAXIMA) {
                            sumaGanancias += resultado.gananciaTotal;
                            sumaDemandas += resultado.demandaTotal;
                            simulacionesValidas++;
                            ultimoResultado = resultado;
                        }

                        // Actualizar progreso cada 250 pruebas
                        if (mc % 250 == 0 || mc == NUM_PRUEBAS_MC - 1) {
                            publish(new ProgressUpdate(iter + 1, mc + 1));
                        }
                    }

                    // Verificar que se ejecutaron todas las pruebas
                    if (gananciasTemp.size() != NUM_PRUEBAS_MC) {
                        System.err.println("ERROR: Simulación " + (iter + 1) + " solo ejecutó " + gananciasTemp.size() + " pruebas!");
                    }

                    if (simulacionesValidas > 0) {
                        double gananciaMedia = sumaGanancias / simulacionesValidas;
                        double demandaMedia = sumaDemandas / simulacionesValidas;

                        // Si esta combinación es mejor, guardarla
                        if (gananciaMedia > mejorGananciaTotal) {
                            mejorGananciaTotal = gananciaMedia;
                            mejorDemandaTotal = demandaMedia;
                            mejoresPrecios[0] = precioStandard;
                            mejoresPrecios[1] = precioGold;
                            mejoresPrecios[2] = precioPlatinum;

                            // Guardar los 5000 resultados de esta mejor simulación
                            gananciasFinales.clear();
                            demandasFinales.clear();
                            gananciasFinales.addAll(gananciasTemp);
                            demandasFinales.addAll(demandasTemp);

                            if (ultimoResultado != null) {
                                mejoresElasticidades = ultimoResultado.elasticidades.clone();
                                mejoresProyeccionesDemanda = ultimoResultado.proyeccionesDemanda.clone();
                                mejoresProyeccionesGanancia = ultimoResultado.proyeccionesGanancia.clone();
                            }

                            historialMejoras.add(new ResultadoSimulacion(iter + 1, gananciaMedia, demandaMedia, precioStandard, precioGold, precioPlatinum));

                            System.out.println("Nueva mejor solución en simulación " + (iter + 1) + ": Ganancia=$" + String.format("%.2f", gananciaMedia) + ", Demanda=" + String.format("%.2f", demandaMedia));
                        }
                    }

                    // Log cada 100 simulaciones
                    if ((iter + 1) % 100 == 0) {
                        System.out.println("Completadas " + (iter + 1) + "/" + NUM_SIMULACIONES + " simulaciones (" + totalPruebasEjecutadas + " pruebas totales)");
                    }
                }

                long tiempoTotal = System.currentTimeMillis() - tiempoInicio;
                System.out.println("=== OPTIMIZACIÓN COMPLETADA ===");
                System.out.println("Total pruebas ejecutadas: " + totalPruebasEjecutadas);
                System.out.println("Resultados finales guardados: " + gananciasFinales.size());
                System.out.println("Mejoras encontradas: " + historialMejoras.size());
                System.out.println("Tiempo total: " + (tiempoTotal / 1000.0) + " segundos");

                return null;
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) {
                ProgressUpdate ultimo = chunks.get(chunks.size() - 1);

                progressBar.setValue(ultimo.simulacion);
                lblSimulacionesActual.setText(String.format("Simulaciones totales: %d / %d", ultimo.simulacion, NUM_SIMULACIONES));

                progressBarPruebas.setValue(ultimo.prueba);
                lblPruebasActual.setText(String.format("Pruebas: %d / %d", ultimo.prueba, NUM_PRUEBAS_MC));
            }

            @Override
            protected void done() {
                actualizarTablaConMejoresResultados();
                btnOptimizar.setEnabled(true);
                btnGraficas.setEnabled(true);
                mostrarVentanaResultados();
            }
        };

        worker.execute();
    }

    // Clase auxiliar para el progreso
    private static class ProgressUpdate {
        int simulacion;
        int prueba;

        ProgressUpdate(int sim, int pr) {
            this.simulacion = sim;
            this.prueba = pr;
        }
    }

    private double generarPrecioAleatorio(double[] limites, Random random) {
        int minimo = (int) limites[0];
        int maximo = (int) limites[1];
        return minimo + random.nextInt(maximo - minimo + 1);
    }

    private ResultadoOptimizacion simularConPrecios(double precioStd, double precioGold, double precioPlt, Random random) {

        double[] elasticidades = new double[3];
        double[] proyeccionesDemanda = new double[3];
        double[] proyeccionesGanancia = new double[3];

        for (int i = 0; i < 3; i++) {
            elasticidades[i] = ELASTICIDAD_LIMITES[i][0] + random.nextDouble() * (ELASTICIDAD_LIMITES[i][1] - ELASTICIDAD_LIMITES[i][0]);
        }

        double[] nuevosPrecios = {precioStd, precioGold, precioPlt};

        for (int i = 0; i < 3; i++) {
            proyeccionesDemanda[i] = DEMANDA_PROMEDIO[i] + elasticidades[i] * (nuevosPrecios[i] - PRECIOS_INICIAL[i]) * (DEMANDA_PROMEDIO[i] / PRECIOS_INICIAL[i]);

            proyeccionesDemanda[i] = Math.max(0, proyeccionesDemanda[i]);
            proyeccionesGanancia[i] = nuevosPrecios[i] * proyeccionesDemanda[i];
        }

        double demandaTotal = proyeccionesDemanda[0] + proyeccionesDemanda[1] + proyeccionesDemanda[2];
        double gananciaTotal = proyeccionesGanancia[0] + proyeccionesGanancia[1] + proyeccionesGanancia[2];

        return new ResultadoOptimizacion(nuevosPrecios, elasticidades, proyeccionesDemanda, proyeccionesGanancia, demandaTotal, gananciaTotal);
    }

    private void actualizarTablaConMejoresResultados() {
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00");
        DecimalFormat dfDemanda = new DecimalFormat("0");
        DecimalFormat dfElast = new DecimalFormat("0");

        for (int i = 0; i < 3; i++) {
            modeloTabla.setValueAt(dfElast.format(mejoresElasticidades[i]), i, 4);
            modeloTabla.setValueAt(dfMoney.format(mejoresPrecios[i]), i, 5);
            modeloTabla.setValueAt(dfDemanda.format(mejoresProyeccionesDemanda[i]), i, 6);
            modeloTabla.setValueAt(dfMoney.format(mejoresProyeccionesGanancia[i]), i, 7);
        }

        // Actualizar totales
        lblTotalDemanda.setText(dfDemanda.format(mejorDemandaTotal));
        lblTotalGanancia.setText(dfMoney.format(mejorGananciaTotal));
    }

    private void mostrarVentanaResultados() {
        JDialog dialogo = new JDialog(this, "Resultados de OptQuest", false);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.getContentPane().setBackground(Color.WHITE);

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel(NUM_SIMULACIONES + " simulaciones", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panelTablas = new JPanel(new GridLayout(4, 1, 5, 5));
        panelTablas.setBackground(Color.WHITE);

        DecimalFormat df = new DecimalFormat("$#,##0.00");

        panelTablas.add(crearTablaResultado("Objetivos", "Valor", new String[]{"Maximizar el/la Media de Total Revenue"}, new String[]{df.format(mejorGananciaTotal)}));

        panelTablas.add(crearTablaResultado("Requisitos", "Valor", new String[]{"El/la Percentil 80% de Total room demand debe ser menor que"}, new String[]{String.valueOf(CAPACIDAD_MAXIMA)}));

        panelTablas.add(crearTablaResultado("Restricciones", "Lado izquierdo    Lado derecho", new String[]{}, new String[]{}));

        String[][] variables = {{"Standard price", df.format(mejoresPrecios[0])}, {"Gold price", df.format(mejoresPrecios[1])}, {"Platinum price", df.format(mejoresPrecios[2])}};

        JPanel panelVars = crearTablaVariables(variables);
        panelTablas.add(panelVars);

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH);
        panelPrincipal.add(panelTablas, BorderLayout.CENTER);

        dialogo.add(panelPrincipal);
        dialogo.setSize(650, 450);
        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private JPanel crearTablaResultado(String titulo, String columna, String[] filas, String[] valores) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel lblTitulo = new JLabel(" " + titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitulo.setBackground(new Color(220, 220, 220));
        lblTitulo.setOpaque(true);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        JPanel panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(Color.WHITE);
        panelContenido.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        if (filas.length > 0) {
            JPanel panelDatos = new JPanel(new GridLayout(filas.length + 1, 2, 5, 3));
            panelDatos.setBackground(Color.WHITE);

            JLabel lblCol1 = new JLabel("");
            JLabel lblCol2 = new JLabel(columna);
            lblCol2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            panelDatos.add(lblCol1);
            panelDatos.add(lblCol2);

            for (int i = 0; i < filas.length; i++) {
                JLabel lblFila = new JLabel(filas[i]);
                lblFila.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                JLabel lblValor = new JLabel(valores[i], SwingConstants.RIGHT);
                lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 10));

                panelDatos.add(lblFila);
                panelDatos.add(lblValor);
            }

            panelContenido.add(panelDatos);
        } else {
            JLabel lblVacio = new JLabel(columna);
            lblVacio.setFont(new Font("Segoe UI", Font.BOLD, 10));
            panelContenido.add(lblVacio);
        }

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(panelContenido, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearTablaVariables(String[][] datos) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        JLabel lblTitulo = new JLabel(" Variables de decisión", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitulo.setBackground(new Color(220, 220, 220));
        lblTitulo.setOpaque(true);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5));

        String[] columnas = {"", "Valor"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0);

        for (String[] fila : datos) {
            modelo.addRow(fila);
        }

        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tabla.setRowHeight(20);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));

        panel.add(lblTitulo, BorderLayout.NORTH);
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER);

        return panel;
    }

    private void mostrarGraficas() {
        mostrarHistogramaRevenue();
        mostrarHistogramaDemanda();
    }

    private void mostrarHistogramaRevenue() {
        // Usar los 5000 resultados finales de la mejor solución
        double[] datos = gananciasFinales.stream().mapToDouble(Double::doubleValue).toArray();

        if (datos.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Total Revenue", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram("Total Revenue", "Dollars", "Frecuencia", dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 112, 192));
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        renderer.setShadowVisible(false);

        double media = Arrays.stream(datos).average().orElse(0);
        ValueMarker marker = new ValueMarker(media);
        marker.setPaint(Color.BLACK);
        marker.setLabel(String.format("Media = $%.2f", media));
        marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.BOTTOM_RIGHT);
        plot.addDomainMarker(marker);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setNumberFormatOverride(new DecimalFormat("$#,##0"));

        chart.setBackgroundPaint(Color.WHITE);

        mostrarVentanaGrafico(chart, "Previsión: Total Revenue - " + datos.length + " pruebas", 900, 600);
    }

    private void mostrarHistogramaDemanda() {
        // Usar los 5000 resultados finales de la mejor solución
        double[] datos = demandasFinales.stream().mapToDouble(Double::doubleValue).toArray();

        if (datos.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Total room demand", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram("Total room demand", "Habitaciones", "Frecuencia", dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter());
        renderer.setShadowVisible(false);
        renderer.setSeriesPaint(0, new Color(0, 112, 192));

        double[] datosOrdenados = datos.clone();
        Arrays.sort(datosOrdenados);
        int indicePercentil = (int) Math.ceil(PERCENTIL_OBJETIVO * datosOrdenados.length) - 1;
        double percentil80 = datosOrdenados[indicePercentil];

        ValueMarker marker = new ValueMarker(percentil80);
        marker.setPaint(Color.BLACK);
        marker.setLabel(String.format("80%% = %.2f", percentil80));
        marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.TOP_LEFT);
        plot.addDomainMarker(marker);

        org.jfree.chart.plot.IntervalMarker intervalo = new org.jfree.chart.plot.IntervalMarker(percentil80, datosOrdenados[datosOrdenados.length - 1] + 10);
        intervalo.setPaint(new Color(255, 150, 150, 120));
        plot.addDomainMarker(intervalo);

        chart.setBackgroundPaint(Color.WHITE);

        mostrarVentanaGrafico(chart, "Previsión: Total room demand - " + datos.length + " pruebas", 900, 600);
    }

    private void mostrarVentanaGrafico(JFreeChart chart, String titulo, int ancho, int alto) {
        JFrame frame = new JFrame(titulo);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(ancho, alto));

        frame.setContentPane(chartPanel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    private static class ResultadoOptimizacion {
        double[] precios;
        double[] elasticidades;
        double[] proyeccionesDemanda;
        double[] proyeccionesGanancia;
        double demandaTotal;
        double gananciaTotal;

        ResultadoOptimizacion(double[] precios, double[] elasticidades, double[] proyeccionesDemanda, double[] proyeccionesGanancia, double demandaTotal, double gananciaTotal) {
            this.precios = precios;
            this.elasticidades = elasticidades;
            this.proyeccionesDemanda = proyeccionesDemanda;
            this.proyeccionesGanancia = proyeccionesGanancia;
            this.demandaTotal = demandaTotal;
            this.gananciaTotal = gananciaTotal;
        }
    }

    private static class ResultadoSimulacion {
        int iteracion;
        double ganancia;
        double demanda;
        double precioStandard;
        double precioGold;
        double precioPlatinum;

        ResultadoSimulacion(int iter, double gan, double dem, double pStd, double pGold, double pPlt) {
            this.iteracion = iter;
            this.ganancia = gan;
            this.demanda = dem;
            this.precioStandard = pStd;
            this.precioGold = pGold;
            this.precioPlatinum = pPlt;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            OptimizadorPreciosHotel optimizador = new OptimizadorPreciosHotel();
            optimizador.setVisible(true);
        });
    }
}