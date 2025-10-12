package actividad_9.ejercicio_1;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;

public class InventorySystemSimulatorEditable extends JFrame {

    private static class Config {
        static double COSTO_PEDIDO = 50.0;
        static double COSTO_TENENCIA = 0.20;
        static double COSTO_VENTAS_PERDIDAS = 100.0;
        static final int PLAZO_ENTREGA = 2; // semanas (fijo)
        static final int DEMANDA_MEDIA = 100;
        static final int NUM_SEMANAS = 52;

        // Parámetros de optimización
        static final int ORDEN_MIN = 200;
        static final int ORDEN_MAX = 400;
        static final int ORDEN_PASO = 5;
        static final int REORDEN_MIN = 200;
        static final int REORDEN_MAX = 400;
        static final int REORDEN_PASO = 10;

        static final int NUM_SIMULACIONES = ((ORDEN_MAX - ORDEN_MIN) / ORDEN_PASO + 1) *
                                           ((REORDEN_MAX - REORDEN_MIN) / REORDEN_PASO + 1);
        static final int NUM_PRUEBAS_MC = 5000;
    }

    private static final DecimalFormat FMT_NUMBER = new DecimalFormat("#,##0.00");
    private static final DecimalFormat FMT_INT = new DecimalFormat("#,##0");

    private DefaultTableModel modeloTabla;
    private JTextField txtCantidadPedido, txtPuntoReorden, txtInventarioInicial;
    private JTextField txtCostoPedido, txtCostoTenencia, txtCostoVentasPerdidas;
    private JLabel lblCostoAlmacenamiento, lblCostoPedido, lblCostoFaltante, lblCostoTotal;
    private JProgressBar progressBar, progressBarPruebas;
    private JLabel lblSimulaciones, lblPruebas;
    private JButton btnOptimizar, btnActualizar;

    private double mejorCosto = Double.POSITIVE_INFINITY;
    private int mejorCantidadPedido = 250;
    private int mejorPuntoReorden = 250;
    private List<Double> costosFinales = new ArrayList<>();
    private List<Double> todosCostosMC = new ArrayList<>(); // NUEVO: Todos los costos MC

    private int cantidadPedidoActual = 250;
    private int puntoReordenActual = 250;

    public InventorySystemSimulatorEditable() {
        super("Simulación de inventario con ventas perdidas");
        configurarUI();
        simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual);
        setSize(1600, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configurarUI() {
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30);
        main.add(crearTitulo(), BorderLayout.NORTH);

        JPanel centro = new JPanel(new BorderLayout(10, 10));
        centro.setBackground(Color.WHITE);
        centro.add(crearPanelSuperior(), BorderLayout.NORTH);
        centro.add(crearTabla(), BorderLayout.CENTER);

        main.add(centro, BorderLayout.CENTER);

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

        parametros.add(crearLabelParametro("Cantidad de pedido"));
        txtCantidadPedido = crearTextField("250", new Color(255, 255, 0));
        parametros.add(txtCantidadPedido);
        parametros.add(crearLabelParametro("Costo del pedido"));
        txtCostoPedido = crearTextField("50");
        parametros.add(txtCostoPedido);

        parametros.add(crearLabelParametro("Punto de reorden"));
        txtPuntoReorden = crearTextField("250", new Color(255, 255, 0));
        parametros.add(txtPuntoReorden);
        parametros.add(crearLabelParametro("Costo de tenencia"));
        txtCostoTenencia = crearTextField("0.20");
        parametros.add(txtCostoTenencia);

        parametros.add(crearLabelParametro("Inventario inicial"));
        txtInventarioInicial = crearTextField("250");
        txtInventarioInicial.setEditable(false);
        parametros.add(txtInventarioInicial);
        parametros.add(crearLabelParametro("Costo de ventas perdidas"));
        txtCostoVentasPerdidas = crearTextField("100");
        parametros.add(txtCostoVentasPerdidas);

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

    private JTextField crearTextField(String valor) {
        return crearTextField(valor, Color.WHITE);
    }

    private JTextField crearTextField(String valor, Color bg) {
        JTextField txt = new JTextField(valor);
        txt.setFont(new Font("Calibri", Font.BOLD, 11));
        txt.setHorizontalAlignment(SwingConstants.CENTER);
        txt.setBackground(bg);
        txt.setOpaque(true);
        txt.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        return txt;
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
                    setBackground(new Color(146, 208, 80));
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
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0));

        JLabel titulo = new JLabel("Costos anuales totales");
        titulo.setFont(new Font("Calibri", Font.BOLD, 14));
        panel.add(titulo);

        lblCostoAlmacenamiento = crearLabelTotal("$ 1.040");
        lblCostoPedido = crearLabelTotal("$ 1.050");
        lblCostoFaltante = crearLabelTotal("$ 5.000");
        lblCostoTotal = crearLabelTotal("$ 7.090", new Color(0, 255, 255));

        panel.add(lblCostoAlmacenamiento);
        panel.add(lblCostoPedido);
        panel.add(lblCostoFaltante);
        panel.add(lblCostoTotal);

        return panel;
    }

    private JLabel crearLabelTotal(String valor) {
        return crearLabelTotal(valor, Color.WHITE);
    }

    private JLabel crearLabelTotal(String valor, Color bg) {
        JLabel lbl = new JLabel(valor);
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

        btnActualizar = crearBoton("Actualizar Tabla", new Color(237, 125, 49), 180, 40);
        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)",
                                  new Color(68, 114, 196), 320, 45);

        btnActualizar.addActionListener(e -> actualizarTablaConValoresActuales());
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion());

        panelBtns.add(btnActualizar);
        panelBtns.add(btnOptimizar);

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

    private void actualizarTablaConValoresActuales() {
        try {
            cantidadPedidoActual = Integer.parseInt(txtCantidadPedido.getText());
            puntoReordenActual = Integer.parseInt(txtPuntoReorden.getText());
            Config.COSTO_PEDIDO = Double.parseDouble(txtCostoPedido.getText());
            Config.COSTO_TENENCIA = Double.parseDouble(txtCostoTenencia.getText());
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(txtCostoVentasPerdidas.getText());

            // Actualizar inventario inicial para que sea igual a cantidad de pedido
            txtInventarioInicial.setText(String.valueOf(cantidadPedidoActual));

            simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos");
        }
    }

    private void ejecutarOptimizacion() {
        // Actualizar costos desde los campos de texto
        try {
            Config.COSTO_PEDIDO = Double.parseDouble(txtCostoPedido.getText());
            Config.COSTO_TENENCIA = Double.parseDouble(txtCostoTenencia.getText());
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(txtCostoVentasPerdidas.getText());
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos en los costos");
            return;
        }

        btnOptimizar.setEnabled(false);
        costosFinales.clear();
        todosCostosMC.clear(); // Limpiar todos los costos
        mejorCosto = Double.POSITIVE_INFINITY;
        modeloTabla.setRowCount(0);

        new SwingWorker<Void, int[]>() {
            protected Void doInBackground() {
                int numThreads = Runtime.getRuntime().availableProcessors();
                ExecutorService executor = Executors.newFixedThreadPool(numThreads);

                List<Future<ResultadoSimulacion>> futures = new ArrayList<>();
                int simCount = 0;

                for (int cantPedido = Config.ORDEN_MIN; cantPedido <= Config.ORDEN_MAX; cantPedido += Config.ORDEN_PASO) {
                    for (int puntoReorden = Config.REORDEN_MIN; puntoReorden <= Config.REORDEN_MAX; puntoReorden += Config.REORDEN_PASO) {
                        final int cp = cantPedido;
                        final int pr = puntoReorden;
                        final int currentSim = simCount;

                        Future<ResultadoSimulacion> future = executor.submit(() -> {
                            List<Double> costosSim = new ArrayList<>();
                            Random rand = new Random();

                            for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) {
                                double costoTotal = simularInventario(cp, pr, rand);
                                costosSim.add(costoTotal);

                                if (mc % 500 == 0) {
                                    publish(new int[]{currentSim + 1, mc + 1});
                                }
                            }

                            double costoMedio = costosSim.stream().mapToDouble(d -> d).average().orElse(0);
                            return new ResultadoSimulacion(cp, pr, costoMedio, costosSim);
                        });

                        futures.add(future);
                        simCount++;
                    }
                }

                // Procesar resultados
                for (Future<ResultadoSimulacion> future : futures) {
                    try {
                        ResultadoSimulacion resultado = future.get();

                        // Guardar TODOS los costos de TODAS las simulaciones
                        synchronized(todosCostosMC) {
                            todosCostosMC.addAll(resultado.costos);
                        }

                        if (resultado.costoMedio < mejorCosto) {
                            mejorCosto = resultado.costoMedio;
                            mejorCantidadPedido = resultado.cantidadPedido;
                            mejorPuntoReorden = resultado.puntoReorden;
                            costosFinales = new ArrayList<>(resultado.costos);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                executor.shutdown();
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
                mostrarVentanaResultados();
            }
        }.execute();
    }

    private static class ResultadoSimulacion {
        int cantidadPedido;
        int puntoReorden;
        double costoMedio;
        List<Double> costos;

        ResultadoSimulacion(int cp, int pr, double cm, List<Double> c) {
            this.cantidadPedido = cp;
            this.puntoReorden = pr;
            this.costoMedio = cm;
            this.costos = c;
        }
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

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]);

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]);
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1;

        // Correcta fórmula para pedido realizado en semana 1
        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1];
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden;

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0);

        if (pedidoRealizado[1]) {
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1;
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

            // Contar pedidos que llegan
            int numArriving = 0;
            for (int s = 1; s < sem; s++) {
                if (semanaVencimiento[s] == sem) {
                    numArriving++;
                }
            }
            unidadesRecibidas[sem] = numArriving * cantidadPedido;
            demanda[sem] = poissonDist.sample();

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]);

            int demandaSatisfecha = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]);
            ventasPerdidas[sem] = demanda[sem] - demandaSatisfecha;

            // Correcta fórmula para pedido realizado en semanas 2+
            int posicionDespuesDemanda = posicionInventario[sem] - demanda[sem] + ventasPerdidas[sem];
            pedidoRealizado[sem] = posicionDespuesDemanda <= puntoReorden;

            posicionInventarioFinal[sem] = posicionDespuesDemanda + (pedidoRealizado[sem] ? cantidadPedido : 0);

            if (pedidoRealizado[sem]) {
                semanaVencimiento[sem] = sem + Config.PLAZO_ENTREGA + 1;
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
        txtCantidadPedido.setText(String.valueOf(mejorCantidadPedido));
        txtPuntoReorden.setText(String.valueOf(mejorPuntoReorden));
        txtInventarioInicial.setText(String.valueOf(mejorCantidadPedido));

        cantidadPedidoActual = mejorCantidadPedido;
        puntoReordenActual = mejorPuntoReorden;

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

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]);

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]);
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1;

        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1];
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden;

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0);

        if (pedidoRealizado[1]) {
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1;
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

            // Contar pedidos que llegan
            int numArriving = 0;
            for (int s = 1; s < sem; s++) {
                if (semanaVencimiento[s] == sem) {
                    numArriving++;
                }
            }
            pedidoRecibido[sem] = numArriving > 0;
            unidadesRecibidas[sem] = numArriving * cantidadPedido;
            demanda[sem] = fixedDemand;

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]);

            int demandaSatisfecha = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]);
            ventasPerdidas[sem] = demanda[sem] - demandaSatisfecha;

            int posicionDespuesDemanda = posicionInventario[sem] - demanda[sem] + ventasPerdidas[sem];
            pedidoRealizado[sem] = posicionDespuesDemanda <= puntoReorden;

            posicionInventarioFinal[sem] = posicionDespuesDemanda + (pedidoRealizado[sem] ? cantidadPedido : 0);

            if (pedidoRealizado[sem]) {
                semanaVencimiento[sem] = sem + Config.PLAZO_ENTREGA + 1;
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
            new String[]{"$ " + FMT_NUMBER.format(mejorCosto)}));

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
            {"Order Quantity", String.valueOf(mejorCantidadPedido)},
            {"Reorder Point", String.valueOf(mejorPuntoReorden)}
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

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() ->
            new InventorySystemSimulatorEditable().setVisible(true));
    }
}