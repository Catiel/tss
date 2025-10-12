package actividad_9.ejercicio_1;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.jfree.chart.*;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.ui.RectangleAnchor;
import org.jfree.chart.ui.TextAnchor;
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class InventorySystemSimulator extends JFrame {

    private static class Config {
        static final double COSTO_PEDIDO = 50.0;
        static final double COSTO_TENENCIA = 0.20;
        static final double COSTO_VENTAS_PERDIDAS = 100.0;
        static final int PLAZO_ENTREGA = 2; // semanas
        static final int DUE_OFFSET = PLAZO_ENTREGA + 1;
        static final int DEMANDA_MEDIA = 100;
        static final int NUM_SEMANAS = 52;

        // Parámetros de optimización
        static final int ORDEN_MIN = 200;
        static final int ORDEN_MAX = 400;
        static final int ORDEN_PASO = 5;
        static final int REORDEN_MIN = 200;
        static final int REORDEN_MAX = 400;
        static final int REORDEN_PASO = 10;

        static final int NUM_SIMULACIONES = 563;
        static final int NUM_PRUEBAS_MC = 5000;
    }

    private static final DecimalFormat FMT_NUMBER = new DecimalFormat("#,##0.00");
    private static final DecimalFormat FMT_INT = new DecimalFormat("#,##0");

    private DefaultTableModel modeloTabla;
    private JLabel lblCantidadPedido, lblPuntoReorden, lblInventarioInicial;
    private JLabel lblCostoAlmacenamiento, lblCostoPedido, lblCostoFaltante, lblCostoTotal;
    private JProgressBar progressBar, progressBarPruebas;
    private JLabel lblSimulaciones, lblPruebas;
    private JButton btnOptimizar, btnGraficas;

    private double mejorCosto = Double.POSITIVE_INFINITY;
    private int mejorCantidadPedido = 250;
    private int mejorPuntoReorden = 250;
    private List<Double> costosFinales = new ArrayList<>();

    public InventorySystemSimulator() {
        super("Simulación de inventario con ventas perdidas");
        configurarUI();
        // Mostrar datos por defecto al iniciar
        simularYMostrarEnTabla(250, 250);
        setSize(1600, 850);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configurarUI() {
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30);
        main.add(crearTitulo(), BorderLayout.NORTH);
        main.add(crearPanelSuperior(), BorderLayout.NORTH);
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
        JLabel titulo = new JLabel("Simulación de inventario con ventas perdidas");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(new Color(31, 78, 120));
        panel.add(titulo);
        return panel;
    }

    private JPanel crearPanelSuperior() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));

        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5));
        info.setBackground(Color.WHITE);

        JLabel lblObjetivo = new JLabel("Optimizar la cantidad de pedidos y el punto de reorden...");
        lblObjetivo.setFont(new Font("Calibri", Font.PLAIN, 14));
        lblObjetivo.setForeground(new Color(50, 100, 150));

        JLabel lblObjetivo2 = new JLabel("...para minimizar costos");
        lblObjetivo2.setFont(new Font("Calibri", Font.PLAIN, 14));
        lblObjetivo2.setForeground(new Color(50, 100, 150));

        info.add(lblObjetivo);
        info.add(lblObjetivo2);

        JPanel parametros = new JPanel(new GridLayout(4, 4, 10, 5));
        parametros.setBackground(Color.WHITE);
        parametros.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        parametros.add(crearLabelParametro("Cantidad de pedido"));
        lblCantidadPedido = crearLabelValor("250", new Color(255, 255, 0));
        parametros.add(lblCantidadPedido);
        parametros.add(crearLabelParametro("Costo del pedido"));
        parametros.add(crearLabelValor("$ 50", Color.WHITE));

        parametros.add(crearLabelParametro("Punto de reorden"));
        lblPuntoReorden = crearLabelValor("250", new Color(255, 255, 0));
        parametros.add(lblPuntoReorden);
        parametros.add(crearLabelParametro("Costo de tenencia"));
        parametros.add(crearLabelValor("$ 0,20", Color.WHITE));

        parametros.add(crearLabelParametro("Inventario inicial"));
        lblInventarioInicial = crearLabelValor("250", Color.WHITE);
        parametros.add(lblInventarioInicial);
        parametros.add(crearLabelParametro("Costo de ventas perdidas"));
        parametros.add(crearLabelValor("$ 100", Color.WHITE));

        parametros.add(crearLabelParametro("plazo de entrega"));
        parametros.add(crearLabelValor("2 semanas", Color.WHITE));
        parametros.add(new JLabel());
        parametros.add(new JLabel());

        JPanel derecha = new JPanel(new BorderLayout());
        derecha.setBackground(Color.WHITE);
        derecha.add(parametros, BorderLayout.NORTH);

        panel.add(info, BorderLayout.WEST);
        panel.add(derecha, BorderLayout.CENTER);

        return panel;
    }

    private JLabel crearLabelParametro(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Calibri", Font.PLAIN, 11));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    private JLabel crearLabelValor(String texto, Color bg) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Calibri", Font.BOLD, 11));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBackground(bg);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return lbl;
    }

    private JScrollPane crearTabla() {
        String[] cols = {"Semana", "Posición de\ninventario", "Inventario\ninicial", "Pedido\nrecibido",
                        "Unidades\nrecibidas", "Demanda", "Inventario\nfinal", "Ventas\nperdidas",
                        "¿Pedido\nrealizado?", "Posición\ninventario\nfinal", "Semana\nvencimiento",
                        "Costo de\nalmacenamiento", "Costo del\npedido", "Costo por\nfaltante", "Costo\ntotal"};

        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tabla = new JTable(modeloTabla);
        configurarEstiloTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        return scroll;
    }

    private void configurarEstiloTabla(JTable tabla) {
        tabla.setFont(new Font("Calibri", Font.PLAIN, 10));
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 10));
        tabla.getTableHeader().setBackground(new Color(79, 129, 189));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setGridColor(new Color(200, 200, 200));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Calibri", Font.PLAIN, 10));

                if (c == 5) { // Demanda
                    setBackground(new Color(0, 255, 0));
                } else if (c == 8) { // ¿Pedido realizado?
                    setBackground(Color.WHITE);
                } else {
                    setBackground(Color.WHITE);
                }

                setForeground(Color.BLACK);
                return this;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 60 : 85);
        }
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JLabel titulo = new JLabel("Costos anuales totales");
        titulo.setFont(new Font("Calibri", Font.BOLD, 14));
        panel.add(titulo);

        lblCostoAlmacenamiento = crearLabelTotal("$", "1.040");
        lblCostoPedido = crearLabelTotal("$", "1.050");
        lblCostoFaltante = crearLabelTotal("$", "5.000");
        lblCostoTotal = crearLabelTotal("$", "7.090", new Color(0, 255, 255));

        panel.add(lblCostoAlmacenamiento);
        panel.add(lblCostoPedido);
        panel.add(lblCostoFaltante);
        panel.add(lblCostoTotal);

        return panel;
    }

    private JLabel crearLabelTotal(String prefijo, String valor) {
        return crearLabelTotal(prefijo, valor, Color.WHITE);
    }

    private JLabel crearLabelTotal(String prefijo, String valor, Color bg) {
        JLabel lbl = new JLabel(prefijo + " " + valor);
        lbl.setFont(new Font("Calibri", Font.BOLD, 14));
        lbl.setHorizontalAlignment(SwingConstants.CENTER);
        lbl.setBackground(bg);
        lbl.setForeground(Color.BLACK);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return lbl;
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

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
        costosFinales.clear();
        mejorCosto = Double.POSITIVE_INFINITY;
        modeloTabla.setRowCount(0);

        new SwingWorker<Void, int[]>() {
            protected Void doInBackground() {
                Random rand = new Random();
                int simCount = 0;

                for (int cantPedido = Config.ORDEN_MIN; cantPedido <= Config.ORDEN_MAX; cantPedido += Config.ORDEN_PASO) {
                    for (int puntoReorden = Config.REORDEN_MIN; puntoReorden <= Config.REORDEN_MAX; puntoReorden += Config.REORDEN_PASO) {
                        List<Double> costosSim = new ArrayList<>();

                        for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) {
                            double costoTotal = simularInventario(cantPedido, puntoReorden, rand);
                            costosSim.add(costoTotal);

                            if (mc % 250 == 0) {
                                publish(new int[]{simCount + 1, mc + 1});
                            }
                        }

                        double costoMedio = costosSim.stream().mapToDouble(d -> d).average().orElse(0);

                        if (costoMedio < mejorCosto) {
                            mejorCosto = costoMedio;
                            mejorCantidadPedido = cantPedido;
                            mejorPuntoReorden = puntoReorden;
                            costosFinales = new ArrayList<>(costosSim);
                        }

                        simCount++;
                        if (simCount >= Config.NUM_SIMULACIONES) break;
                    }
                    if (simCount >= Config.NUM_SIMULACIONES) break;
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

    private double simularInventario(int cantidadPedido, int puntoReorden, Random rand) {
        PoissonDistribution poissonDist = new PoissonDistribution(Config.DEMANDA_MEDIA);

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1];
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1];
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1];
        int[] demanda = new int[Config.NUM_SEMANAS + 1];
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1];
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1];
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1];
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1];
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1];

        double costoAlmacenamientoTotal = 0;
        double costoPedidoTotal = 0;
        double costoFaltanteTotal = 0;

        // Semana 1
        posicionInventario[1] = cantidadPedido;
        inventarioInicial[1] = cantidadPedido;
        unidadesRecibidas[1] = 0;
        demanda[1] = poissonDist.sample();

        int projected1 = posicionInventario[1] - demanda[1];
        pedidoRealizado[1] = projected1 <= puntoReorden;

        int satisfied1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]);
        ventasPerdidas[1] = demanda[1] - satisfied1;

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]);

        posicionInventarioFinal[1] = posicionInventario[1] - satisfied1 + (pedidoRealizado[1] ? cantidadPedido : 0);

        if (pedidoRealizado[1]) {
            semanaVencimiento[1] = 1 + Config.DUE_OFFSET;
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA;
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0;
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS;

        costoAlmacenamientoTotal += costoAlmacenamiento;
        costoPedidoTotal += costoPedido;
        costoFaltanteTotal += costoFaltante;

        // Semanas 2 a 52
        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) {
            posicionInventario[sem] = posicionInventarioFinal[sem - 1];
            inventarioInicial[sem] = inventarioFinal[sem - 1];

            // Count arriving orders
            int numArriving = 0;
            for (int s = 1; s < sem; s++) {
                if (semanaVencimiento[s] == sem) {
                    numArriving++;
                }
            }
            unidadesRecibidas[sem] = numArriving * cantidadPedido;
            demanda[sem] = poissonDist.sample();

            int projected = posicionInventario[sem] - demanda[sem];
            pedidoRealizado[sem] = projected <= puntoReorden;

            int satisfied = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]);
            ventasPerdidas[sem] = demanda[sem] - satisfied;

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]);

            posicionInventarioFinal[sem] = posicionInventario[sem] - satisfied + (pedidoRealizado[sem] ? cantidadPedido : 0);

            if (pedidoRealizado[sem]) {
                semanaVencimiento[sem] = sem + Config.DUE_OFFSET;
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA;
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0;
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS;

            costoAlmacenamientoTotal += costoAlmacenamiento;
            costoPedidoTotal += costoPedido;
            costoFaltanteTotal += costoFaltante;
        }

        return costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal;
    }

    private void actualizarResultados() {
        lblCantidadPedido.setText(String.valueOf(mejorCantidadPedido));
        lblPuntoReorden.setText(String.valueOf(mejorPuntoReorden));
        lblInventarioInicial.setText(String.valueOf(mejorCantidadPedido));

        // Simular una vez con los mejores parámetros para mostrar en la tabla (demanda fija para reproducibilidad)
        simularYMostrarEnTabla(mejorCantidadPedido, mejorPuntoReorden);
    }

    private void simularYMostrarEnTabla(int cantidadPedido, int puntoReorden) {
        modeloTabla.setRowCount(0);

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1];
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1];
        boolean[] pedidoRecibido = new boolean[Config.NUM_SEMANAS + 1];
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1];
        int[] demanda = new int[Config.NUM_SEMANAS + 1];
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1];
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1];
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1];
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1];
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1];

        double costoAlmacenamientoTotal = 0;
        double costoPedidoTotal = 0;
        double costoFaltanteTotal = 0;

        final int fixedDemand = 100;

        // Semana 1
        posicionInventario[1] = cantidadPedido;
        inventarioInicial[1] = cantidadPedido;
        pedidoRecibido[1] = false;
        unidadesRecibidas[1] = 0;
        demanda[1] = fixedDemand;

        int projected1 = posicionInventario[1] - demanda[1];
        pedidoRealizado[1] = projected1 <= puntoReorden;

        int satisfied1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]);
        ventasPerdidas[1] = demanda[1] - satisfied1;

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]);

        posicionInventarioFinal[1] = posicionInventario[1] - satisfied1 + (pedidoRealizado[1] ? cantidadPedido : 0);

        if (pedidoRealizado[1]) {
            semanaVencimiento[1] = 1 + Config.DUE_OFFSET;
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA;
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0;
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS;

        costoAlmacenamientoTotal += costoAlmacenamiento;
        costoPedidoTotal += costoPedido;
        costoFaltanteTotal += costoFaltante;

        modeloTabla.addRow(new Object[]{
            "1",
            FMT_INT.format(posicionInventario[1]),
            FMT_INT.format(inventarioInicial[1]),
            pedidoRecibido[1] ? "VERDADERO" : "FALSO",
            FMT_INT.format(unidadesRecibidas[1]),
            FMT_INT.format(demanda[1]),
            FMT_INT.format(inventarioFinal[1]),
            FMT_INT.format(ventasPerdidas[1]),
            pedidoRealizado[1] ? "VERDADERO" : "FALSO",
            FMT_INT.format(posicionInventarioFinal[1]),
            semanaVencimiento[1] > 0 ? String.valueOf(semanaVencimiento[1]) : "",
            "$    " + FMT_NUMBER.format(costoAlmacenamiento),
            "$    " + FMT_NUMBER.format(costoPedido),
            "$    " + FMT_NUMBER.format(costoFaltante),
            "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante)
        });

        // Semanas 2 a 52
        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) {
            posicionInventario[sem] = posicionInventarioFinal[sem - 1];
            inventarioInicial[sem] = inventarioFinal[sem - 1];

            // Count arriving orders
            int numArriving = 0;
            for (int s = 1; s < sem; s++) {
                if (semanaVencimiento[s] == sem) {
                    numArriving++;
                }
            }
            pedidoRecibido[sem] = numArriving > 0;
            unidadesRecibidas[sem] = numArriving * cantidadPedido;
            demanda[sem] = fixedDemand;

            int projected = posicionInventario[sem] - demanda[sem];
            pedidoRealizado[sem] = projected <= puntoReorden;

            int satisfied = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]);
            ventasPerdidas[sem] = demanda[sem] - satisfied;

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]);

            posicionInventarioFinal[sem] = posicionInventario[sem] - satisfied + (pedidoRealizado[sem] ? cantidadPedido : 0);

            if (pedidoRealizado[sem]) {
                semanaVencimiento[sem] = sem + Config.DUE_OFFSET;
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA;
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0;
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS;

            costoAlmacenamientoTotal += costoAlmacenamiento;
            costoPedidoTotal += costoPedido;
            costoFaltanteTotal += costoFaltante;

            modeloTabla.addRow(new Object[]{
                String.valueOf(sem),
                FMT_INT.format(posicionInventario[sem]),
                FMT_INT.format(inventarioInicial[sem]),
                pedidoRecibido[sem] ? "VERDADERO" : "FALSO",
                FMT_INT.format(unidadesRecibidas[sem]),
                FMT_INT.format(demanda[sem]),
                FMT_INT.format(inventarioFinal[sem]),
                FMT_INT.format(ventasPerdidas[sem]),
                pedidoRealizado[sem] ? "VERDADERO" : "FALSO",
                FMT_INT.format(posicionInventarioFinal[sem]),
                semanaVencimiento[sem] > 0 ? String.valueOf(semanaVencimiento[sem]) : "",
                "$    " + FMT_NUMBER.format(costoAlmacenamiento),
                "$    " + FMT_NUMBER.format(costoPedido),
                "$    " + FMT_NUMBER.format(costoFaltante),
                "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante)
            });
        }

        // Actualizar totales
        lblCostoAlmacenamiento.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal));
        lblCostoPedido.setText("$ " + FMT_NUMBER.format(costoPedidoTotal));
        lblCostoFaltante.setText("$ " + FMT_NUMBER.format(costoFaltanteTotal));
        lblCostoTotal.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal));
    }

    private void mostrarVentanaResultados() {
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false);
        dlg.setLayout(new BorderLayout());

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(Color.WHITE);

        JLabel lblHeader = new JLabel(Config.NUM_SIMULACIONES + " simulaciones");
        lblHeader.setFont(new Font("Calibri", Font.BOLD, 14));

        JLabel lblSubHeader = new JLabel("Vista de mejor solución");
        lblSubHeader.setFont(new Font("Calibri", Font.PLAIN, 12));

        headerPanel.add(lblHeader, BorderLayout.NORTH);
        headerPanel.add(lblSubHeader, BorderLayout.CENTER);

        main.add(headerPanel, BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 10));
        tablas.setBackground(Color.WHITE);

        // Gráfico de rendimiento
        JPanel graficoPanel = new JPanel(new BorderLayout());
        graficoPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de rendimiento"));
        graficoPanel.setBackground(Color.WHITE);
        graficoPanel.setPreferredSize(new Dimension(600, 150));

        JLabel lblGrafico = new JLabel("Mejores soluciones encontradas", SwingConstants.CENTER);
        lblGrafico.setFont(new Font("Calibri", Font.ITALIC, 11));
        graficoPanel.add(lblGrafico, BorderLayout.CENTER);

        tablas.add(graficoPanel);

        // Objetivos
        tablas.add(crearSeccionResultado("Objetivos", "Valor",
            new String[]{"Minimizar el/la Media de Total Annual Costs"},
            new String[]{"$" + FMT_NUMBER.format(mejorCosto)}));

        // Requisitos
        JPanel requisitosPanel = new JPanel(new BorderLayout());
        requisitosPanel.setBorder(BorderFactory.createTitledBorder("Requisitos:"));
        requisitosPanel.setBackground(Color.WHITE);
        JLabel lblRequisitos = new JLabel("(requisitos opcionales en previsiones)");
        lblRequisitos.setFont(new Font("Calibri", Font.ITALIC, 10));
        requisitosPanel.add(lblRequisitos, BorderLayout.CENTER);
        tablas.add(requisitosPanel);

        main.add(tablas, BorderLayout.CENTER);

        // Panel inferior con restricciones y variables
        JPanel inferior = new JPanel(new GridLayout(1, 2, 10, 0));
        inferior.setBackground(Color.WHITE);

        // Restricciones
        JPanel restriccionesPanel = new JPanel(new BorderLayout());
        restriccionesPanel.setBorder(BorderFactory.createTitledBorder("Restricciones"));
        restriccionesPanel.setBackground(Color.WHITE);

        JPanel restriccionesGrid = new JPanel(new GridLayout(1, 3, 5, 5));
        restriccionesGrid.setBackground(Color.WHITE);
        restriccionesGrid.add(new JLabel(""));
        restriccionesGrid.add(new JLabel("Lado izquierdo", SwingConstants.CENTER));
        restriccionesGrid.add(new JLabel("Lado derecho", SwingConstants.CENTER));

        restriccionesPanel.add(restriccionesGrid, BorderLayout.CENTER);
        inferior.add(restriccionesPanel);

        // Variables de decisión
        JPanel variablesPanel = new JPanel(new BorderLayout());
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables de decisión"));
        variablesPanel.setBackground(Color.WHITE);

        String[][] varsData = {
            {"Order Quantity", FMT_NUMBER.format(mejorCantidadPedido)},
            {"Reorder Point", FMT_NUMBER.format(mejorPuntoReorden)}
        };

        DefaultTableModel modeloVars = new DefaultTableModel(new String[]{"", "Valor"}, 0);
        for (String[] row : varsData) {
            modeloVars.addRow(row);
        }

        JTable tablaVars = new JTable(modeloVars);
        tablaVars.setFont(new Font("Calibri", Font.PLAIN, 11));
        tablaVars.setRowHeight(25);
        tablaVars.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11));

        JScrollPane scrollVars = new JScrollPane(tablaVars);
        scrollVars.setPreferredSize(new Dimension(250, 80));
        variablesPanel.add(scrollVars, BorderLayout.CENTER);

        inferior.add(variablesPanel);

        main.add(inferior, BorderLayout.SOUTH);

        dlg.add(main);
        dlg.setSize(750, 550);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(titulo));
        panel.setBackground(Color.WHITE);

        if (filas.length > 0) {
            DefaultTableModel modelo = new DefaultTableModel(new String[]{"Objetivos", col}, 0);
            for (int i = 0; i < filas.length; i++) {
                modelo.addRow(new Object[]{filas[i], vals[i]});
            }

            JTable tabla = new JTable(modelo);
            tabla.setFont(new Font("Calibri", Font.PLAIN, 11));
            tabla.setRowHeight(25);
            tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11));

            JScrollPane scroll = new JScrollPane(tabla);
            scroll.setPreferredSize(new Dimension(600, 60));
            panel.add(scroll, BorderLayout.CENTER);
        }
        return panel;
    }

    private void mostrarHistograma() {
        double[] datos = costosFinales.stream().mapToDouble(d -> d).toArray();
        if (datos.length == 0) return;

        HistogramDataset dataset = new HistogramDataset();
        dataset.addSeries("Total Annual Costs", datos, 50);

        JFreeChart chart = ChartFactory.createHistogram("Total Annual Costs", "Dollars",
            "Frecuencia", dataset, PlotOrientation.VERTICAL, true, true, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(0, 112, 192));
        renderer.setShadowVisible(false);

        double media = Arrays.stream(datos).average().orElse(0);

        ValueMarker marker = new ValueMarker(media);
        marker.setPaint(Color.BLACK);
        marker.setStroke(new BasicStroke(2.0f));
        marker.setLabel("Media = $" + FMT_NUMBER.format(media));
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT);
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT);
        plot.addDomainMarker(marker);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setNumberFormatOverride(new DecimalFormat("$#,##0"));

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

        chart.setBackgroundPaint(Color.WHITE);

        JFrame frame = new JFrame("Previsión: Total Annual Costs");
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(900, 600));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(chartPanel, BorderLayout.CENTER);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblStats = new JLabel("5.000 pruebas                Vista de frecuencia                4.824 mostrados");
        lblStats.setFont(new Font("Calibri", Font.PLAIN, 11));
        statsPanel.add(lblStats);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);

        frame.setContentPane(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(this);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() ->
            new InventorySystemSimulator().setVisible(true));
    }
}