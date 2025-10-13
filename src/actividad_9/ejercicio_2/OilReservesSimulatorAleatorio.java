package actividad_9.ejercicio_2;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.Locale;

import org.apache.commons.math3.distribution.*;

public class OilReservesSimulatorAleatorio extends JFrame {
    private static final int NUM_SIMULACIONES = 563;
    private static final int NUM_PRUEBAS_MC = 1000;
    private static final int AÑOS = 50;
    private static final int MIN_TRIALS_FOR_CHECK = 500;
    private static final int CHECK_INTERVAL = 500;
    private static final int NUM_BINS_HISTOGRAMA = 50;

    private static final DecimalFormat FMT2 = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
    private static final DecimalFormat FMT0 = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US));

    private static final Color COLOR_HEADER = new Color(79, 129, 189);
    private static final Color COLOR_SUPOSICION = new Color(146, 208, 80);
    private static final Color COLOR_DECISION = new Color(255, 255, 0);
    private static final Color COLOR_CALCULADO = new Color(217, 217, 217);
    private static final Color COLOR_NPV = new Color(0, 255, 255);
    private static final Color COLOR_PANEL_BG = new Color(248, 248, 248);

    private double stoiip = 1500.0;
    private double recuperacion = 42.0;
    private double buenaTasa = 10.0;
    private int pozosPerforar = 25;
    private double factorDescuento = 10.0;
    private double buenCosto = 10.0;
    private double tamañoInstalacion = 250.0;
    private double plateauRateIs = 10.0;

    private double timeToPlateau = 2.0;
    private double tarifaMinima = 10.0;
    private double margenPetroleo = 2.0;
    private double plateauEndsAt = 65.0;

    private double[][] costosInstalaciones = {{50, 70}, {100, 130}, {150, 180}, {200, 220}, {250, 250}, {300, 270}, {350, 280}};

    private double reservas;
    private double maxPlateauRate;
    private double plateauRate;
    private double aumentarProduccion;
    private double plateauProduction;
    private double plateauEndsAtCalc;
    private double factorDeclive;
    private double vidaProduccion;
    private double reservasDescontadas;
    private double costosPozo;
    private double costosInstalacionesCalc;
    private double npv;

    private JTextField txtStoiip, txtRecuperacion, txtBuenaTasa, txtPozos;
    private JTextField txtFactorDescuento, txtBuenCosto, txtTamañoInstalacion, txtPlateauRateIs;
    private JTextField txtTimeToPlateau, txtTarifaMinima, txtMargenPetroleo, txtPlateauEndsAt;
    private JLabel lblReservas, lblMaxPlateau, lblPlateauRate, lblAumentar;
    private JLabel lblPlateauProd, lblPlateauEnds, lblFactorDeclive, lblVidaProd;
    private JLabel lblReservasDesc, lblCostosPozo, lblCostosInst, lblNPV;
    private DefaultTableModel modeloTabla;
    private DefaultTableModel modeloCostos;
    private JProgressBar progressBar;
    private JLabel lblProgreso;
    private JTabbedPane tabbedPane;

    private double mejorNPV = Double.NEGATIVE_INFINITY;
    private int mejorPozos = 25;
    private double mejorTamañoInst = 250.0;
    private double mejorPlateauRateIs = 10.0;
    private List<Double> todosNPV = new ArrayList<>();
    private List<Double> mejorSimulacionNPVs = new ArrayList<>();
    private Random randomGenerator = new Random();

    public OilReservesSimulatorAleatorio() {
        super("Simulación de Reservas Petroleras - Crystal Ball");
        configurarUI();
        generarValoresAleatorios();
        calcularValores();
        actualizarUI();
        calcularTablaProduccion();
        setSize(1600, 950);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void generarValoresAleatorios() {
        // Generar valores aleatorios para los campos editables
        timeToPlateau = 1.0 + randomGenerator.nextDouble() * 4.0; // Entre 1 y 5 años
        tarifaMinima = 5.0 + randomGenerator.nextDouble() * 10.0; // Entre 5 y 15 mbd
        margenPetroleo = 1.0 + randomGenerator.nextDouble() * 3.0; // Entre 1 y 4 $/bbl
        plateauEndsAt = 50.0 + randomGenerator.nextDouble() * 30.0; // Entre 50% y 80%

        // Actualizar los campos de texto solo si ya fueron creados
        // Usar String.format con Locale.US para forzar punto decimal
        if (txtTimeToPlateau != null) {
            txtTimeToPlateau.setText(String.format(Locale.US, "%.2f", timeToPlateau));
        }
        if (txtTarifaMinima != null) {
            txtTarifaMinima.setText(String.format(Locale.US, "%.2f", tarifaMinima));
        }
        if (txtMargenPetroleo != null) {
            txtMargenPetroleo.setText(String.format(Locale.US, "%.2f", margenPetroleo));
        }
        if (txtPlateauEndsAt != null) {
            txtPlateauEndsAt.setText(String.format(Locale.US, "%.2f", plateauEndsAt));
        }
    }

    private void configurarUI() {
        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(COLOR_PANEL_BG);
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titulo = new JLabel("Oil Field Development - Simulación de Reservas Petroleras", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(new Color(31, 78, 120));
        titulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        main.add(titulo, BorderLayout.NORTH);

        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12));

        JPanel dashboardPanel = crearPanelDashboard();
        tabbedPane.addTab("📊 Dashboard Principal", dashboardPanel);

        JPanel tablaPanel = crearPanelTablaCompleta();
        tabbedPane.addTab("📈 Perfil de Producción (50 años)", tablaPanel);

        main.add(tabbedPane, BorderLayout.CENTER);
        main.add(crearPanelControl(), BorderLayout.SOUTH);

        add(main);
    }

    private JPanel crearPanelDashboard() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_PANEL_BG);

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        topPanel.setBackground(COLOR_PANEL_BG);
        topPanel.add(crearPanelEntrada());
        topPanel.add(crearPanelCalculado());
        topPanel.add(crearPanelResultadosFinales());

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(crearPanelResumenTabla(), BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelEntrada() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel header = new JLabel("Variables de Entrada", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(COLOR_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(3, 5, 3, 5);

        int row = 0;
        addGridRow(grid, gbc, row++, "STOIIP", txtStoiip = crearTextField("1500.00", COLOR_SUPOSICION), "mmbbls", COLOR_SUPOSICION);
        txtStoiip.setEditable(false);
        addGridRow(grid, gbc, row++, "Recuperación", txtRecuperacion = crearTextField("42.0", COLOR_SUPOSICION), "%", COLOR_SUPOSICION);
        txtRecuperacion.setEditable(false);
        addGridRow(grid, gbc, row++, "Time to plateau", txtTimeToPlateau = crearTextField("2.00", Color.WHITE), "years", Color.WHITE);
        addGridRow(grid, gbc, row++, "Buena tasa", txtBuenaTasa = crearTextField("10.00", COLOR_SUPOSICION), "mbd", COLOR_SUPOSICION);
        txtBuenaTasa.setEditable(false);
        addGridRow(grid, gbc, row++, "Pozos a perforar", txtPozos = crearTextField("25", COLOR_DECISION), "", COLOR_DECISION);
        txtPozos.setEditable(false);
        addGridRow(grid, gbc, row++, "Tarifa mínima", txtTarifaMinima = crearTextField("10.00", Color.WHITE), "mbd", Color.WHITE);
        addGridRow(grid, gbc, row++, "Factor de descuento", txtFactorDescuento = crearTextField("10.00", COLOR_SUPOSICION), "%", COLOR_SUPOSICION);
        txtFactorDescuento.setEditable(false);
        addGridRow(grid, gbc, row++, "Buen costo", txtBuenCosto = crearTextField("10.00", COLOR_SUPOSICION), "$mm", COLOR_SUPOSICION);
        txtBuenCosto.setEditable(false);
        addGridRow(grid, gbc, row++, "Tamaño instalación", txtTamañoInstalacion = crearTextField("250.00", COLOR_DECISION), "mbd", COLOR_DECISION);
        txtTamañoInstalacion.setEditable(false);
        addGridRow(grid, gbc, row++, "Margen petróleo", txtMargenPetroleo = crearTextField("2.00", Color.WHITE), "$/bbl", Color.WHITE);
        addGridRow(grid, gbc, row++, "Plateau ends at", txtPlateauEndsAt = crearTextField("65.0", Color.WHITE), "% reservas", Color.WHITE);
        addGridRow(grid, gbc, row++, "Plateau rate is", txtPlateauRateIs = crearTextField("10.0", COLOR_DECISION), "% res./año", COLOR_DECISION);
        txtPlateauRateIs.setEditable(false);

        panel.add(grid, BorderLayout.CENTER);
        panel.add(crearLeyenda(), BorderLayout.SOUTH);

        return panel;
    }

    private void addGridRow(JPanel grid, GridBagConstraints gbc, int row, String label, JComponent campo, String unidad, Color bgColor) {
        gbc.gridy = row;
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        JLabel lbl = new JLabel(label + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        grid.add(lbl, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.4;
        grid.add(campo, gbc);

        gbc.gridx = 2;
        gbc.weightx = 0.2;
        JLabel lblUnit = new JLabel(unidad);
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblUnit.setForeground(Color.GRAY);
        grid.add(lblUnit, gbc);
    }

    private JPanel crearLeyenda() {
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        leyenda.setBackground(Color.WHITE);
        leyenda.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        leyenda.add(crearLeyendaItem("Suposición", COLOR_SUPOSICION));
        leyenda.add(crearLeyendaItem("Decisión", COLOR_DECISION));
        leyenda.add(crearLeyendaItem("Fijo", Color.WHITE));

        return leyenda;
    }

    private JPanel crearLeyendaItem(String texto, Color color) {
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
        item.setBackground(Color.WHITE);

        JLabel colorBox = new JLabel("  ");
        colorBox.setOpaque(true);
        colorBox.setBackground(color);
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        JLabel label = new JLabel(texto);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9));

        item.add(colorBox);
        item.add(label);

        return item;
    }

    private JPanel crearPanelCalculado() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel header = new JLabel("Valores Calculados", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(COLOR_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(8, 2, 5, 8));
        grid.setBackground(Color.WHITE);

        grid.add(crearLabelParametro("Reservas"));
        lblReservas = crearLabelCalculado("630.00 mmbbls");
        grid.add(lblReservas);

        grid.add(crearLabelParametro("Max plateau rate"));
        lblMaxPlateau = crearLabelCalculado("172.60 mbd");
        grid.add(lblMaxPlateau);

        grid.add(crearLabelParametro("Plateau rate"));
        lblPlateauRate = crearLabelCalculado("172.60 mbd");
        grid.add(lblPlateauRate);

        grid.add(crearLabelParametro("Aumentar producción"));
        lblAumentar = crearLabelCalculado("63.00 mmbbls");
        grid.add(lblAumentar);

        grid.add(crearLabelParametro("Plateau production"));
        lblPlateauProd = crearLabelCalculado("346.50 mmbbls");
        grid.add(lblPlateauProd);

        grid.add(crearLabelParametro("Plateau ends at"));
        lblPlateauEnds = crearLabelCalculado("7.50 años");
        grid.add(lblPlateauEnds);

        grid.add(crearLabelParametro("Factor de declive"));
        lblFactorDeclive = crearLabelCalculado("0.2692");
        grid.add(lblFactorDeclive);

        grid.add(crearLabelParametro("Vida de producción"));
        lblVidaProd = crearLabelCalculado("18.08 años");
        grid.add(lblVidaProd);

        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelResultadosFinales() {
        JPanel container = new JPanel(new BorderLayout(5, 10));
        container.setBackground(COLOR_PANEL_BG);

        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(237, 125, 49), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel header = new JLabel("Resultados Finales", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(new Color(237, 125, 49));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 8));
        grid.setBackground(Color.WHITE);

        grid.add(crearLabelParametro("Reservas descontadas"));
        lblReservasDesc = crearLabelCalculado("379.45 mmbbls");
        grid.add(lblReservasDesc);

        grid.add(crearLabelParametro("Costos del pozo"));
        lblCostosPozo = crearLabelCalculado("250.00 $mm");
        grid.add(lblCostosPozo);

        grid.add(crearLabelParametro("Costos instalaciones"));
        lblCostosInst = crearLabelCalculado("250.00 $mm");
        grid.add(lblCostosInst);

        grid.add(crearLabelParametro("NPV"));
        lblNPV = crearLabelCalculado("258.89 $mm");
        lblNPV.setBackground(COLOR_NPV);
        lblNPV.setFont(new Font("Segoe UI", Font.BOLD, 13));
        grid.add(lblNPV);

        panel.add(grid, BorderLayout.CENTER);

        JLabel objetivo = new JLabel("🎯 Objetivo: Maximizar Percentil 10 de NPV", SwingConstants.CENTER);
        objetivo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        objetivo.setForeground(Color.RED);
        objetivo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        panel.add(objetivo, BorderLayout.SOUTH);

        container.add(panel, BorderLayout.NORTH);
        container.add(crearPanelCostosInstalaciones(), BorderLayout.CENTER);

        return container;
    }

    private JPanel crearPanelCostosInstalaciones() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 192, 203), 2), BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        JLabel header = new JLabel("Costos de Instalaciones", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setForeground(new Color(192, 80, 77));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Producción (mbd)", "Costo ($mm)"};
        modeloCostos = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return true;
            }
        };

        for (int i = 0; i < costosInstalaciones.length; i++) {
            modeloCostos.addRow(new Object[]{FMT0.format(costosInstalaciones[i][0]), FMT0.format(costosInstalaciones[i][1])});
        }

        JTable tabla = new JTable(modeloCostos);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        tabla.setRowHeight(22);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10));
        tabla.getTableHeader().setBackground(new Color(255, 192, 203));
        tabla.getTableHeader().setForeground(Color.BLACK);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(250, 180));
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelResumenTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel header = new JLabel("Perfil de Producción - Primeros 15 Años (Ver pestaña para datos completos)", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 13));
        header.setForeground(COLOR_HEADER);
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Año", "Tasa Anualizada\n(mbd)", "Producción Anual\n(mmb)", "Petróleo Acumulado\n(mmb)", "Petróleo Desc. Acum.\n(mmb)"};

        DefaultTableModel modeloResumen = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tablaResumen = new JTable(modeloResumen);
        configurarEstiloTabla(tablaResumen);

        JScrollPane scroll = new JScrollPane(tablaResumen);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelTablaCompleta() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel header = new JLabel("Perfil de Producción Calculado - 50 Años", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(COLOR_HEADER);
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        panel.add(header, BorderLayout.NORTH);

        String[] cols = {"Año", "Tasa Anualizada (mbd)", "Producción Anual (mmb)", "Petróleo Acumulado (mmb)", "Petróleo con Descuento Acumulado (mmb)"};

        modeloTabla = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tabla = new JTable(modeloTabla);
        configurarEstiloTabla(tabla);

        JScrollPane scroll = new JScrollPane(tabla);
        panel.add(scroll, BorderLayout.CENTER);

        return panel;
    }

    private void configurarEstiloTabla(JTable tabla) {
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        tabla.setRowHeight(26);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11));
        tabla.getTableHeader().setBackground(COLOR_HEADER);
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setGridColor(new Color(220, 220, 220));
        tabla.setShowGrid(true);
        tabla.setIntercellSpacing(new Dimension(1, 1));

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT);
                setFont(new Font("Segoe UI", Font.PLAIN, 11));

                if (r % 2 == 0) {
                    setBackground(Color.WHITE);
                } else {
                    setBackground(new Color(245, 245, 245));
                }

                if (c == 0) {
                    setFont(new Font("Segoe UI", Font.BOLD, 11));
                }

                setForeground(Color.BLACK);
                return this;
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) {
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer);
            if (i == 0) {
                tabla.getColumnModel().getColumn(i).setPreferredWidth(60);
            } else {
                tabla.getColumnModel().getColumn(i).setPreferredWidth(160);
            }
        }
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 15, 10, 15)));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));
        botones.setBackground(Color.WHITE);

        JButton btnGenerarAleatorios = crearBoton("🎲 Generar Valores Aleatorios", new Color(156, 39, 176), 250, 40);
        btnGenerarAleatorios.addActionListener(e -> {
            generarValoresAleatorios();
            calcularValores();
            actualizarUI();
            calcularTablaProduccion();
            JOptionPane.showMessageDialog(this, "✓ Nuevos valores aleatorios generados", "Valores Aleatorios", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnActualizar = crearBoton("🔄 Actualizar Cálculos", new Color(237, 125, 49), 200, 40);
        btnActualizar.addActionListener(e -> {
            leerValoresEditablesUI();
            calcularValores();
            actualizarUI();
            calcularTablaProduccion();
            JOptionPane.showMessageDialog(this, "✓ Cálculos actualizados correctamente", "Actualización", JOptionPane.INFORMATION_MESSAGE);
        });

        JButton btnOptimizar = crearBoton("🚀 Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 300, 40);
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion());

        botones.add(btnGenerarAleatorios);
        botones.add(btnActualizar);
        botones.add(btnOptimizar);

        JPanel progreso = new JPanel(new BorderLayout(8, 8));
        progreso.setBackground(Color.WHITE);

        lblProgreso = new JLabel("Listo para comenzar optimización", SwingConstants.CENTER);
        lblProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        progressBar = new JProgressBar(0, NUM_SIMULACIONES);
        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(700, 30));
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11));
        progressBar.setForeground(new Color(76, 175, 80));

        progreso.add(lblProgreso, BorderLayout.NORTH);
        progreso.add(progressBar, BorderLayout.CENTER);

        panel.add(botones, BorderLayout.NORTH);
        panel.add(progreso, BorderLayout.CENTER);

        return panel;
    }

    private JLabel crearLabelParametro(String texto) {
        JLabel lbl = new JLabel(texto + ":");
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        return lbl;
    }

    private JTextField crearTextField(String valor, Color bg) {
        JTextField txt = new JTextField(valor);
        txt.setFont(new Font("Segoe UI", Font.BOLD, 11));
        txt.setHorizontalAlignment(SwingConstants.RIGHT);
        txt.setBackground(bg);
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return txt;
    }

    private JLabel crearLabelCalculado(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lbl.setHorizontalAlignment(SwingConstants.RIGHT);
        lbl.setBackground(COLOR_CALCULADO);
        lbl.setOpaque(true);
        lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5)));
        return lbl;
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setPreferredSize(new Dimension(ancho, alto));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg.brighter());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(bg);
            }
        });

        return btn;
    }

    private JLabel crearStatLabel(String texto) {
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(COLOR_HEADER);
        return lbl;
    }

    private void leerValoresUI() {
        try {
            stoiip = Double.parseDouble(txtStoiip.getText().replace(",", ""));
            recuperacion = Double.parseDouble(txtRecuperacion.getText().replace(",", ""));
            buenaTasa = Double.parseDouble(txtBuenaTasa.getText().replace(",", ""));
            pozosPerforar = Integer.parseInt(txtPozos.getText().replace(",", ""));
            factorDescuento = Double.parseDouble(txtFactorDescuento.getText().replace(",", ""));
            buenCosto = Double.parseDouble(txtBuenCosto.getText().replace(",", ""));
            tamañoInstalacion = Double.parseDouble(txtTamañoInstalacion.getText().replace(",", ""));
            plateauRateIs = Double.parseDouble(txtPlateauRateIs.getText().replace(",", ""));
            timeToPlateau = Double.parseDouble(txtTimeToPlateau.getText().replace(",", ""));
            tarifaMinima = Double.parseDouble(txtTarifaMinima.getText().replace(",", ""));
            margenPetroleo = Double.parseDouble(txtMargenPetroleo.getText().replace(",", ""));
            plateauEndsAt = Double.parseDouble(txtPlateauEndsAt.getText().replace(",", ""));

            // Leer costosInstalaciones desde la tabla
            int rows = modeloCostos.getRowCount();
            costosInstalaciones = new double[rows][2];
            for (int i = 0; i < rows; i++) {
                String prodStr = modeloCostos.getValueAt(i, 0).toString().replace(",", "");
                String costStr = modeloCostos.getValueAt(i, 1).toString().replace(",", "");
                costosInstalaciones[i][0] = Double.parseDouble(prodStr);
                costosInstalaciones[i][1] = Double.parseDouble(costStr);
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al leer valores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void leerValoresEditablesUI() {
        try {
            // Solo leer los campos EDITABLES (con fondo blanco)
            timeToPlateau = Double.parseDouble(txtTimeToPlateau.getText().replace(",", "").trim());
            tarifaMinima = Double.parseDouble(txtTarifaMinima.getText().replace(",", "").trim());
            margenPetroleo = Double.parseDouble(txtMargenPetroleo.getText().replace(",", "").trim());
            plateauEndsAt = Double.parseDouble(txtPlateauEndsAt.getText().replace(",", "").trim());
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al leer valores editables: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcularValores() {
        reservas = stoiip * recuperacion / 100.0;
        maxPlateauRate = (plateauRateIs / 100.0) * reservas / 0.365;
        plateauRate = Math.min(maxPlateauRate, Math.min(buenaTasa * pozosPerforar, tamañoInstalacion));
        aumentarProduccion = 0.365 * plateauRate * 0.5 * timeToPlateau;
        plateauProduction = Math.max(0, plateauEndsAt * (reservas / 100.0) - aumentarProduccion);
        plateauEndsAtCalc = plateauProduction / (0.365 * plateauRate) + timeToPlateau;
        factorDeclive = 0.365 * (plateauRate - tarifaMinima) / (reservas - plateauProduction - aumentarProduccion);

        if (tarifaMinima > 0) {
            vidaProduccion = plateauEndsAtCalc - Math.log(tarifaMinima / plateauRate) / factorDeclive;
        } else {
            vidaProduccion = 1e20;
        }

        costosPozo = buenCosto * pozosPerforar;
        costosInstalacionesCalc = buscarCostoInstalacion(tamañoInstalacion);
    }

    private double buscarCostoInstalacion(double produccion) {
        for (int i = 0; i < costosInstalaciones.length; i++) {
            if (produccion <= costosInstalaciones[i][0]) {
                return costosInstalaciones[i][1];
            }
        }
        return costosInstalaciones[costosInstalaciones.length - 1][1];
    }

    private void calcularTablaProduccion() {
        modeloTabla.setRowCount(0);

        double[] tasaAnualizada = new double[AÑOS + 1];
        double[] produccionAnual = new double[AÑOS + 1];
        double[] petroleoAcumulado = new double[AÑOS + 1];
        double[] petroleoDescuentoAcum = new double[AÑOS + 1];

        for (int año = 1; año <= AÑOS; año++) {
            if (año < timeToPlateau + 1) {
                produccionAnual[año] = año * 0.365 * plateauRate / (timeToPlateau + 1);
            } else {
                double maxMin1 = Math.min(plateauEndsAtCalc + 1 - año, 1);
                double part1 = 0.365 * plateauRate * Math.max(0, maxMin1);

                double minVidaAño1 = Math.min(vidaProduccion, año - 1);
                double maxExp1 = Math.max(0, minVidaAño1 - plateauEndsAtCalc);
                double exp1 = Math.exp(-factorDeclive * maxExp1);

                double minVidaAño = Math.min(vidaProduccion, año);
                double maxExp2 = Math.max(minVidaAño - plateauEndsAtCalc, 0);
                double exp2 = Math.exp(-factorDeclive * maxExp2);

                double part2 = 0.365 * plateauRate * (exp1 - exp2) / factorDeclive;

                produccionAnual[año] = part1 + part2;
            }

            tasaAnualizada[año] = produccionAnual[año] / 0.365;

            if (año == 1) {
                petroleoAcumulado[año] = produccionAnual[año];
            } else {
                petroleoAcumulado[año] = petroleoAcumulado[año - 1] + produccionAnual[año];
            }

            if (año == 1) {
                petroleoDescuentoAcum[año] = produccionAnual[año];
            } else {
                double descuento = Math.pow(1.0 + 0.01 * factorDescuento, año - 1);
                petroleoDescuentoAcum[año] = petroleoDescuentoAcum[año - 1] + (produccionAnual[año] / descuento);
            }

            modeloTabla.addRow(new Object[]{año, FMT2.format(tasaAnualizada[año]), FMT2.format(produccionAnual[año]), FMT2.format(petroleoAcumulado[año]), FMT2.format(petroleoDescuentoAcum[año])});
        }

        reservasDescontadas = petroleoDescuentoAcum[AÑOS];
        npv = reservasDescontadas * margenPetroleo - costosPozo - costosInstalacionesCalc;
    }

    private void actualizarUI() {
        lblReservas.setText(FMT2.format(reservas) + " mmbbls");
        lblMaxPlateau.setText(FMT2.format(maxPlateauRate) + " mbd");
        lblPlateauRate.setText(FMT2.format(plateauRate) + " mbd");
        lblAumentar.setText(FMT2.format(aumentarProduccion) + " mmbbls");
        lblPlateauProd.setText(FMT2.format(plateauProduction) + " mmbbls");
        lblPlateauEnds.setText(FMT2.format(plateauEndsAtCalc) + " años");
        lblFactorDeclive.setText(FMT2.format(factorDeclive));
        lblVidaProd.setText(FMT2.format(vidaProduccion) + " años");
        lblReservasDesc.setText(FMT2.format(reservasDescontadas) + " mmbbls");
        lblCostosPozo.setText(FMT2.format(costosPozo) + " $mm");
        lblCostosInst.setText(FMT2.format(costosInstalacionesCalc) + " $mm");
        lblNPV.setText(FMT2.format(npv) + " $mm");
    }

    private void ejecutarOptimizacion() {
        todosNPV.clear();
        mejorSimulacionNPVs.clear();
        mejorNPV = Double.NEGATIVE_INFINITY;

        progressBar.setValue(0);
        lblProgreso.setText("⏳ Ejecutando optimización Monte Carlo...");

        new SwingWorker<Void, Integer>() {
            protected Void doInBackground() {
                Random rand = new Random(12345);

                for (int sim = 1; sim <= NUM_SIMULACIONES; sim++) {
                    int pozos = rand.nextInt(49) + 2;
                    int instIndex = rand.nextInt(7);
                    double tamañoInst = 50 + 50 * instIndex;
                    double plateauIs = 4.5 + rand.nextDouble() * (15.0 - 4.5);

                    List<Double> npvsPrueba = new ArrayList<>();

                    for (int mc = 0; mc < NUM_PRUEBAS_MC; mc++) {
                        LogNormalDistribution stoiipDist = new LogNormalDistribution(Math.log(1500.0), 300.0 / 1500.0);
                        NormalDistribution recupDist = new NormalDistribution(42.0, 1.2);
                        NormalDistribution tasaDist = new NormalDistribution(10.0, 3.0);
                        LogNormalDistribution descDist = new LogNormalDistribution(Math.log(10.0), 1.2 / 10.0);
                        TriangularDistribution costoDist = new TriangularDistribution(9.0, 10.0, 12.0);

                        double stoiipSample = stoiipDist.sample();
                        double recupSample = recupDist.sample();
                        double tasaSample = tasaDist.sample();
                        double descSample = descDist.sample();
                        double costoSample = costoDist.sample();

                        double npvSample = calcularNPVSimulacion(stoiipSample, recupSample, tasaSample, pozos, descSample, costoSample, tamañoInst, plateauIs);

                        npvsPrueba.add(npvSample);
                        todosNPV.add(npvSample);

                        if (mc + 1 >= MIN_TRIALS_FOR_CHECK && (mc + 1) % CHECK_INTERVAL == 0) {
                            List<Double> temp = new ArrayList<>(npvsPrueba);
                            Collections.sort(temp);
                            double currentP10 = temp.get((int) (temp.size() * 0.10));

                            if (currentP10 < mejorNPV - 50.0) {
                                break;
                            }
                        }
                    }

                    Collections.sort(npvsPrueba);
                    double percentil10 = npvsPrueba.get((int) (npvsPrueba.size() * 0.10));

                    if (percentil10 > mejorNPV) {
                        mejorNPV = percentil10;
                        mejorPozos = pozos;
                        mejorTamañoInst = tamañoInst;
                        mejorPlateauRateIs = plateauIs;
                        mejorSimulacionNPVs = new ArrayList<>(npvsPrueba);
                    }

                    if (sim % 5 == 0) {
                        publish(sim);
                    }
                }

                return null;
            }

            protected void process(List<Integer> chunks) {
                int ultimo = chunks.get(chunks.size() - 1);
                progressBar.setValue(ultimo);
                int porcentaje = (int) ((ultimo * 100.0) / NUM_SIMULACIONES);
                lblProgreso.setText(String.format("⏳ Progreso: %d / %d simulaciones (%d%%)", ultimo, NUM_SIMULACIONES, porcentaje));
            }

            protected void done() {
                progressBar.setValue(NUM_SIMULACIONES);
                lblProgreso.setText("✅ Optimización completada - " + NUM_SIMULACIONES + " simulaciones");

                txtPozos.setText(String.valueOf(mejorPozos));
                txtTamañoInstalacion.setText(FMT2.format(mejorTamañoInst));
                txtPlateauRateIs.setText(FMT2.format(mejorPlateauRateIs));

                pozosPerforar = mejorPozos;
                tamañoInstalacion = mejorTamañoInst;
                plateauRateIs = mejorPlateauRateIs;

                calcularValores();
                actualizarUI();
                calcularTablaProduccion();

                mostrarResultadosOptimizacion();
            }
        }.execute();
    }

    private double calcularNPVSimulacion(double stoiip, double recup, double buenaTasa, int pozos, double descuento, double costo, double tamañoInst, double plateauIs) {
        double res = stoiip * recup / 100.0;
        double maxPR = (plateauIs / 100.0) * res / 0.365;
        double pr = Math.min(maxPR, Math.min(buenaTasa * pozos, tamañoInst));
        double aum = 0.365 * pr * 0.5 * timeToPlateau;
        double pp = Math.max(0, plateauEndsAt * (res / 100.0) - aum);
        double pea = pp / (0.365 * pr) + timeToPlateau;
        double fd = 0.365 * (pr - tarifaMinima) / (res - pp - aum);
        double vp = (tarifaMinima > 0) ? pea - Math.log(tarifaMinima / pr) / fd : 1e20;

        double resDesc = 0;

        for (int año = 1; año <= AÑOS; año++) {
            double prodAnual;

            if (año < timeToPlateau + 1) {
                prodAnual = año * 0.365 * pr / (timeToPlateau + 1);
            } else {
                double term1 = 0.365 * pr * Math.max(0, Math.min(pea + 1 - año, 1));
                double exp1 = Math.exp(-fd * Math.max(0, Math.min(vp, año - 1) - pea));
                double exp2 = Math.exp(-fd * Math.max(Math.min(vp, año) - pea, 0));
                double term2 = 0.365 * pr * (exp1 - exp2) / fd;
                prodAnual = term1 + term2;
            }

            if (año == 1) {
                resDesc = prodAnual;
            } else {
                double desc = Math.pow(1.0 + 0.01 * descuento, año - 1);
                resDesc += prodAnual / desc;
            }
        }

        double costoPozos = costo * pozos;
        double costoInst = buscarCostoInstalacion(tamañoInst);

        return resDesc * margenPetroleo - costoPozos - costoInst;
    }

    private void mostrarResultadosOptimizacion() {
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false);
        dlg.setLayout(new BorderLayout(15, 15));

        JPanel main = new JPanel(new BorderLayout(15, 15));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5));
        header.setBackground(Color.WHITE);

        JLabel lblSim = new JLabel("📊 " + NUM_SIMULACIONES + " simulaciones completadas", SwingConstants.CENTER);
        lblSim.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblSim.setForeground(COLOR_HEADER);

        JLabel lblVista = new JLabel("Vista de mejor solución encontrada", SwingConstants.CENTER);
        lblVista.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblVista.setForeground(Color.GRAY);

        header.add(lblSim);
        header.add(lblVista);

        main.add(header, BorderLayout.NORTH);

        JPanel centro = new JPanel(new GridLayout(3, 1, 15, 15));
        centro.setBackground(Color.WHITE);

        JPanel npvPanel = new JPanel(new BorderLayout());
        npvPanel.setBackground(new Color(232, 245, 233));
        npvPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2), BorderFactory.createEmptyBorder(15, 15, 15, 15)));

        JLabel lblNPVTitle = new JLabel("🎯 NPV Percentil 10% (Optimizado)", SwingConstants.CENTER);
        lblNPVTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblNPVTitle.setForeground(new Color(27, 94, 32));

        JLabel lblNPVValue = new JLabel("$ " + FMT2.format(mejorNPV) + " mm", SwingConstants.CENTER);
        lblNPVValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblNPVValue.setForeground(new Color(27, 94, 32));

        npvPanel.add(lblNPVTitle, BorderLayout.NORTH);
        npvPanel.add(lblNPVValue, BorderLayout.CENTER);

        centro.add(npvPanel);
        centro.add(crearPanelVariablesOptimas());
        centro.add(crearPanelEstadisticas());

        main.add(centro, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        botonesPanel.setBackground(Color.WHITE);

        JButton btnHistograma = crearBoton("📊 Ver Histograma NPV", new Color(33, 150, 243), 200, 35);
        btnHistograma.addActionListener(e -> mostrarDistribucionNPV());

        JButton btnCerrar = crearBoton("✓ Cerrar", new Color(76, 175, 80), 120, 35);
        btnCerrar.addActionListener(e -> dlg.dispose());

        botonesPanel.add(btnHistograma);
        botonesPanel.add(btnCerrar);

        main.add(botonesPanel, BorderLayout.SOUTH);

        dlg.add(main);
        dlg.setSize(800, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private JPanel crearPanelVariablesOptimas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titulo = new JLabel("Variables de Decisión Óptimas", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(COLOR_HEADER);
        panel.add(titulo, BorderLayout.NORTH);

        String[][] datos = {{"Pozos a perforar", String.valueOf(mejorPozos), "pozos"}, {"Tamaño de instalación", FMT2.format(mejorTamañoInst), "mbd"}, {"Plateau rate is", FMT2.format(mejorPlateauRateIs), "% reservas/año"}};

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 8));
        grid.setBackground(Color.WHITE);
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (String[] row : datos) {
            JLabel lblNombre = new JLabel(row[0]);
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12));

            JLabel lblValor = new JLabel(row[1]);
            lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblValor.setHorizontalAlignment(SwingConstants.RIGHT);
            lblValor.setOpaque(true);
            lblValor.setBackground(COLOR_DECISION);
            lblValor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), BorderFactory.createEmptyBorder(3, 8, 3, 8)));

            JLabel lblUnidad = new JLabel(row[2]);
            lblUnidad.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblUnidad.setForeground(Color.GRAY);

            grid.add(lblNombre);
            grid.add(lblValor);
            grid.add(lblUnidad);
        }

        panel.add(grid, BorderLayout.CENTER);

        return panel;
    }

    private JPanel crearPanelEstadisticas() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10)));

        JLabel titulo = new JLabel("Estadísticas NPV (Mejor simulación)", SwingConstants.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(new Color(230, 81, 0));
        panel.add(titulo, BorderLayout.NORTH);

        if (!mejorSimulacionNPVs.isEmpty()) {
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs);
            Collections.sort(npvs);

            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0);
            double min = npvs.get(0);
            double max = npvs.get(npvs.size() - 1);
            double p10 = npvs.get((int) (npvs.size() * 0.10));
            double p50 = npvs.get((int) (npvs.size() * 0.50));
            double p90 = npvs.get((int) (npvs.size() * 0.90));

            JPanel grid = new JPanel(new GridLayout(6, 2, 8, 6));
            grid.setBackground(Color.WHITE);
            grid.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));

            addStatRow(grid, "Media:", "$ " + FMT2.format(media) + " mm");
            addStatRow(grid, "Percentil 10%:", "$ " + FMT2.format(p10) + " mm");
            addStatRow(grid, "Mediana (P50):", "$ " + FMT2.format(p50) + " mm");
            addStatRow(grid, "Percentil 90%:", "$ " + FMT2.format(p90) + " mm");
            addStatRow(grid, "Mínimo:", "$ " + FMT2.format(min) + " mm");
            addStatRow(grid, "Máximo:", "$ " + FMT2.format(max) + " mm");

            panel.add(grid, BorderLayout.CENTER);
        }

        return panel;
    }

    private void addStatRow(JPanel grid, String label, String value) {
        JLabel lblLabel = new JLabel(label);
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblLabel.setHorizontalAlignment(SwingConstants.RIGHT);

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblValue.setHorizontalAlignment(SwingConstants.LEFT);

        grid.add(lblLabel);
        grid.add(lblValue);
    }

    private void mostrarDistribucionNPV() {
        JDialog dlg = new JDialog(this, "Previsión: NPV - Distribución", false);
        dlg.setLayout(new BorderLayout(10, 10));

        JPanel main = new JPanel(new BorderLayout(10, 10));
        main.setBackground(Color.WHITE);
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(NUM_PRUEBAS_MC + " pruebas - Vista de frecuencia", SwingConstants.CENTER);
        header.setFont(new Font("Segoe UI", Font.BOLD, 14));
        header.setForeground(COLOR_HEADER);
        main.add(header, BorderLayout.NORTH);

        JPanel histograma = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (!mejorSimulacionNPVs.isEmpty()) {
                    dibujarHistograma(g, getWidth(), getHeight());
                }
            }
        };
        histograma.setBackground(Color.WHITE);
        histograma.setPreferredSize(new Dimension(750, 450));
        histograma.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        main.add(histograma, BorderLayout.CENTER);

        if (!mejorSimulacionNPVs.isEmpty()) {
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs);
            Collections.sort(npvs);
            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0);
            double p10 = npvs.get((int) (npvs.size() * 0.10));

            JPanel stats = new JPanel(new GridLayout(1, 3, 20, 5));
            stats.setBackground(Color.WHITE);
            stats.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0));

            stats.add(crearStatLabel("10% = $ " + FMT2.format(p10) + " mm"));
            stats.add(crearStatLabel("Media = $ " + FMT2.format(media) + " mm"));
            stats.add(crearStatLabel(FMT0.format(npvs.size()) + " muestras"));

            main.add(stats, BorderLayout.SOUTH);
        }

        dlg.add(main);
        dlg.setSize(800, 600);
        dlg.setLocationRelativeTo(this);
        dlg.setVisible(true);
    }

    private void dibujarHistograma(Graphics g, int width, int height) {
        if (mejorSimulacionNPVs == null || mejorSimulacionNPVs.isEmpty()) {
            return;
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int margin = 60;
        int chartWidth = width - 2 * margin;
        int chartHeight = height - 2 * margin;

        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        }

        List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs);
        Collections.sort(npvs);

        double minVal = npvs.get(0);
        double maxVal = npvs.get(npvs.size() - 1);

        double range = maxVal - minVal;
        if (range <= 0) {
            g2.setColor(new Color(100, 181, 246));
            int barX = margin + chartWidth / 2 - 10;
            int barWidth = 20;
            int barHeight = chartHeight;
            g2.fillRect(barX, margin, barWidth, barHeight);

            dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, npvs.size());
            return;
        }

        double binWidth = range / NUM_BINS_HISTOGRAMA;
        int[] bins = new int[NUM_BINS_HISTOGRAMA];

        for (double val : npvs) {
            int binIndex = (int) ((val - minVal) / binWidth);
            if (binIndex >= NUM_BINS_HISTOGRAMA) {
                binIndex = NUM_BINS_HISTOGRAMA - 1;
            }
            if (binIndex < 0) {
                binIndex = 0;
            }
            bins[binIndex]++;
        }

        int maxBin = 0;
        for (int bin : bins) {
            if (bin > maxBin) {
                maxBin = bin;
            }
        }

        if (maxBin == 0) {
            maxBin = 1;
        }

        double barWidthPixels = (double) chartWidth / NUM_BINS_HISTOGRAMA;

        int minBarWidth = Math.max(1, (int) Math.floor(barWidthPixels) - 1);

        g2.setColor(new Color(100, 181, 246));

        for (int i = 0; i < NUM_BINS_HISTOGRAMA; i++) {
            if (bins[i] > 0) {
                int barHeight = (int) Math.round(((double) bins[i] / maxBin) * chartHeight);
                int x = margin + (int) Math.round(i * barWidthPixels);
                int y = height - margin - barHeight;
                if (barHeight < 1) {
                    barHeight = 1;
                }
                g2.fillRect(x, y, minBarWidth, barHeight);
            }
        }

        dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, maxBin);
        dibujarLineasPercentiles(g2, width, height, margin, chartWidth, npvs, minVal, maxVal);
    }

    private void dibujarEjesYEtiquetas(Graphics2D g2, int width, int height, int margin, int chartWidth, int chartHeight, double minVal, double maxVal, int maxBin) {
        g2.setColor(Color.BLACK);
        g2.setStroke(new BasicStroke(2));
        g2.drawLine(margin, height - margin, width - margin, height - margin); // Eje X
        g2.drawLine(margin, margin, margin, height - margin); // Eje Y

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        for (int i = 0; i <= 5; i++) {
            double val = minVal + (maxVal - minVal) * i / 5.0;
            int x = margin + (int) Math.round(chartWidth * i / 5.0);
            String label = FMT0.format(val);
            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2.drawString(label, x - labelWidth / 2, height - margin + 20);
            g2.drawLine(x, height - margin, x, height - margin + 5);
        }

        for (int i = 0; i <= 5; i++) {
            int val = (int) Math.round(maxBin * i / 5.0);
            int y = height - margin - (int) Math.round(chartHeight * i / 5.0);
            String label = String.valueOf(val);

            FontMetrics fm = g2.getFontMetrics();
            int labelWidth = fm.stringWidth(label);
            g2.drawString(label, margin - labelWidth - 10, y + 5);

            g2.drawLine(margin - 5, y, margin, y);
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        String xLabel = "NPV ($mm)";
        FontMetrics fm = g2.getFontMetrics();
        int xLabelWidth = fm.stringWidth(xLabel);
        g2.drawString(xLabel, width / 2 - xLabelWidth / 2, height - 10);

        g2.rotate(-Math.PI / 2);
        String yLabel = "Frecuencia";
        int yLabelWidth = fm.stringWidth(yLabel);
        g2.drawString(yLabel, -height / 2 - yLabelWidth / 2, 15);
        g2.rotate(Math.PI / 2);
    }

    private void dibujarLineasPercentiles(Graphics2D g2, int width, int height, int margin, int chartWidth, List<Double> npvs, double minVal, double maxVal) {
        double range = maxVal - minVal;
        if (range <= 0) {
            return;
        }

        double p10 = npvs.get((int) (npvs.size() * 0.10));
        int xP10 = margin + (int) Math.round((p10 - minVal) / range * chartWidth);

        g2.setColor(new Color(244, 67, 54));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        g2.drawLine(xP10, margin, xP10, height - margin);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
        g2.drawString("P10", xP10 - 15, margin - 10);

        double media = npvs.stream().mapToDouble(d -> d).average().orElse(0);
        int xMedia = margin + (int) Math.round((media - minVal) / range * chartWidth);

        g2.setColor(new Color(76, 175, 80));
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0));
        g2.drawLine(xMedia, margin, xMedia, height - margin);
        g2.drawString("Media", xMedia - 20, margin - 10);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            OilReservesSimulatorAleatorio sim = new OilReservesSimulatorAleatorio();
            sim.setVisible(true);
        });
    }
}