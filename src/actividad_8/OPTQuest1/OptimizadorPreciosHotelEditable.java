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
import org.jfree.data.statistics.HistogramDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

public class OptimizadorPreciosHotelEditable extends JFrame {

    // Parámetros editables
    private double[] preciosInicial = {85.00, 98.00, 139.00};
    private double[] demandaPromedio = {250, 100, 50};

    private double[][] elasticidadLimites = {{-4.50, -1.50}, {-1.50, -0.50}, {-3.00, -1.00}};

    private double[][] precioLimites = {{70.00, 90.00}, {90.00, 110.00}, {120.00, 149.00}};

    private int capacidadMaxima = 450;
    private double percentilObjetivo = 0.80;
    private int numSimulaciones = 1000;
    private int numPruebasMC = 5000;

    private JTable tablaHotel;
    private DefaultTableModel modeloTabla;
    private JLabel lblTotalDemanda;
    private JLabel lblTotalGanancia;
    private JTextField txtCapacidad;
    private JProgressBar progressBar;
    private JProgressBar progressBarPruebas;
    private JLabel lblSimulacionesActual;
    private JLabel lblPruebasActual;
    private JButton btnOptimizar;
    private JButton btnGraficas;
    private JButton btnConfigurar;

    private double mejorGananciaTotal = Double.NEGATIVE_INFINITY;
    private double mejorDemandaTotal = 0;
    private double[] mejoresPrecios = new double[3];
    private double[] mejoresElasticidades = new double[3];
    private double[] mejoresProyeccionesDemanda = new double[3];
    private double[] mejoresProyeccionesGanancia = new double[3];

    private List<ResultadoSimulacion> historialMejoras;
    private List<Double> gananciasFinales;
    private List<Double> demandasFinales;

    public OptimizadorPreciosHotelEditable() {
        super("Problema: precios de cuartos de hotel - Versión Editable");
        historialMejoras = new ArrayList<>();
        gananciasFinales = new ArrayList<>();
        demandasFinales = new ArrayList<>();
        configurarUI();
        setSize(1400, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private void configurarUI() {
        setLayout(new BorderLayout(20, 20));
        getContentPane().setBackground(Color.WHITE);

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));

        JLabel lblTitulo = new JLabel("Problema: precios de cuartos de hotel");
        lblTitulo.setFont(new Font("Calibri", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(31, 78, 120));

        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelTitulo.setBackground(Color.WHITE);
        panelTitulo.add(lblTitulo);

        // IMPORTANTE: Crear primero el panel de totales (inicializa los labels)
        JPanel panelTotales = crearPanelTotales();

        // Luego crear la tabla (que llama a actualizarTotales)
        JPanel panelTabla = crearTablaPrincipal();

        // Finalmente crear el panel de control
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

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                setHorizontalAlignment(SwingConstants.CENTER);
                setFont(new Font("Calibri", Font.PLAIN, 14));

                if (column == 0) {
                    setFont(new Font("Calibri", Font.BOLD | Font.ITALIC, 14));
                    setHorizontalAlignment(SwingConstants.LEFT);
                    setBackground(Color.WHITE);
                } else if (column == 4) {
                    setBackground(new Color(0, 176, 80));
                    setForeground(Color.BLACK);
                    setFont(new Font("Calibri", Font.BOLD, 14));
                } else if (column == 5) {
                    setBackground(new Color(255, 255, 0));
                    setForeground(Color.BLACK);
                    setFont(new Font("Calibri", Font.BOLD, 14));
                } else {
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
        modeloTabla.setRowCount(0);
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("0");

        String[] tipos = {"Standard", "Gold", "Platinum"};

        for (int i = 0; i < 3; i++) {
            double ganancia = preciosInicial[i] * demandaPromedio[i];
            modeloTabla.addRow(new Object[]{tipos[i], dfMoney.format(preciosInicial[i]), dfInt.format(demandaPromedio[i]), dfMoney.format(ganancia), "-3", dfMoney.format(preciosInicial[i]), dfInt.format(demandaPromedio[i]), dfMoney.format(ganancia)});
        }

        actualizarTotales();
    }

    private void actualizarTotales() {
        double totalDemanda = demandaPromedio[0] + demandaPromedio[1] + demandaPromedio[2];
        double totalGanancia = (preciosInicial[0] * demandaPromedio[0]) + (preciosInicial[1] * demandaPromedio[1]) + (preciosInicial[2] * demandaPromedio[2]);

        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00");
        DecimalFormat dfInt = new DecimalFormat("0");

        lblTotalDemanda.setText(dfInt.format(totalDemanda));
        lblTotalGanancia.setText(dfMoney.format(totalGanancia));
    }

    private JPanel crearPanelTotales() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10));
        panel.setBackground(Color.WHITE);

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

        JPanel panelCapacity = new JPanel(new BorderLayout(10, 5));
        panelCapacity.setBackground(Color.WHITE);

        JLabel lblCapacityLabel = new JLabel("Capacity", SwingConstants.CENTER);
        lblCapacityLabel.setFont(new Font("Calibri", Font.BOLD, 14));

        txtCapacidad = new JTextField(String.valueOf(capacidadMaxima));
        txtCapacidad.setFont(new Font("Calibri", Font.BOLD, 16));
        txtCapacidad.setForeground(new Color(255, 102, 0));
        txtCapacidad.setHorizontalAlignment(JTextField.CENTER);
        txtCapacidad.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        txtCapacidad.setPreferredSize(new Dimension(80, 35));

        panelCapacity.add(lblCapacityLabel, BorderLayout.NORTH);
        panelCapacity.add(txtCapacidad, BorderLayout.CENTER);

        panel.add(panelTotal);
        panel.add(panelCapacity);

        return panel;
    }

    private JPanel crearPanelControl() {
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        panelBotones.setBackground(Color.WHITE);

        btnConfigurar = new JButton("⚙ Configurar Parámetros");
        btnConfigurar.setFont(new Font("Calibri", Font.BOLD, 14));
        btnConfigurar.setBackground(new Color(255, 153, 0));
        btnConfigurar.setForeground(Color.WHITE);
        btnConfigurar.setFocusPainted(false);
        btnConfigurar.setPreferredSize(new Dimension(250, 45));

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

        panelBotones.add(btnConfigurar);
        panelBotones.add(btnOptimizar);
        panelBotones.add(btnGraficas);

        JPanel panelProgress = new JPanel(new BorderLayout(10, 10));
        panelProgress.setBackground(Color.WHITE);
        panelProgress.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest", javax.swing.border.TitledBorder.LEFT, javax.swing.border.TitledBorder.TOP, new Font("Calibri", Font.BOLD, 12)));

        JPanel panelBarras = new JPanel(new GridLayout(2, 1, 5, 10));
        panelBarras.setBackground(Color.WHITE);
        panelBarras.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel panelSim = new JPanel(new BorderLayout(5, 5));
        panelSim.setBackground(Color.WHITE);

        lblSimulacionesActual = new JLabel("Simulaciones totales: 0 / " + numSimulaciones);
        lblSimulacionesActual.setFont(new Font("Calibri", Font.PLAIN, 11));

        progressBar = new JProgressBar(0, numSimulaciones);
        progressBar.setStringPainted(false);
        progressBar.setPreferredSize(new Dimension(500, 25));
        progressBar.setForeground(new Color(0, 32, 96));
        progressBar.setBackground(Color.WHITE);

        panelSim.add(lblSimulacionesActual, BorderLayout.WEST);
        panelSim.add(progressBar, BorderLayout.CENTER);
        JLabel lblMaxSim = new JLabel(numSimulaciones + "  ");
        lblMaxSim.setFont(new Font("Calibri", Font.PLAIN, 11));
        panelSim.add(lblMaxSim, BorderLayout.EAST);

        JPanel panelPruebas = new JPanel(new BorderLayout(5, 5));
        panelPruebas.setBackground(Color.WHITE);

        lblPruebasActual = new JLabel("Pruebas: 0 / " + numPruebasMC);
        lblPruebasActual.setFont(new Font("Calibri", Font.PLAIN, 11));

        progressBarPruebas = new JProgressBar(0, numPruebasMC);
        progressBarPruebas.setStringPainted(false);
        progressBarPruebas.setPreferredSize(new Dimension(500, 25));
        progressBarPruebas.setForeground(new Color(0, 176, 80));
        progressBarPruebas.setBackground(Color.WHITE);

        panelPruebas.add(lblPruebasActual, BorderLayout.WEST);
        panelPruebas.add(progressBarPruebas, BorderLayout.CENTER);
        JLabel lblMaxPruebas = new JLabel(numPruebasMC + "  ");
        lblMaxPruebas.setFont(new Font("Calibri", Font.PLAIN, 11));
        panelPruebas.add(lblMaxPruebas, BorderLayout.EAST);

        panelBarras.add(panelSim);
        panelBarras.add(panelPruebas);

        panelProgress.add(panelBarras, BorderLayout.CENTER);

        panel.add(panelBotones);
        panel.add(panelProgress);

        btnConfigurar.addActionListener(e -> mostrarDialogoConfiguracion());

        btnOptimizar.addActionListener(e -> {
            try {
                capacidadMaxima = Integer.parseInt(txtCapacidad.getText());
                btnOptimizar.setEnabled(false);
                btnGraficas.setEnabled(false);
                btnConfigurar.setEnabled(false);
                ejecutarOptimizacion();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Capacidad inválida", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnGraficas.addActionListener(e -> mostrarGraficas());

        return panel;
    }

    private void mostrarDialogoConfiguracion() {
        JDialog dialogo = new JDialog(this, "Configurar Parámetros", true);
        dialogo.setLayout(new BorderLayout(10, 10));
        dialogo.setSize(850, 700);

        JTabbedPane tabbedPane = new JTabbedPane();

        // TAB 1: Parámetros Básicos
        JPanel panelBasicos = new JPanel(new GridLayout(0, 1, 10, 10));
        panelBasicos.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField[] txtPrecios = new JTextField[3];
        JTextField[] txtDemandas = new JTextField[3];
        String[] tipos = {"Standard", "Gold", "Platinum"};

        panelBasicos.add(crearLabelTitulo("Precios y Demandas por Tipo de Habitación"));

        for (int i = 0; i < 3; i++) {
            panelBasicos.add(crearLabelSubtitulo(tipos[i]));

            JPanel panelFila = new JPanel(new GridLayout(1, 4, 10, 0));

            txtPrecios[i] = new JTextField(String.valueOf(preciosInicial[i]));
            txtDemandas[i] = new JTextField(String.valueOf((int) demandaPromedio[i]));

            panelFila.add(new JLabel("Precio inicial ($):"));
            panelFila.add(txtPrecios[i]);
            panelFila.add(new JLabel("Demanda promedio:"));
            panelFila.add(txtDemandas[i]);

            panelBasicos.add(panelFila);
        }

        panelBasicos.add(crearLabelTitulo("Parámetros Generales"));

        JPanel panelCapacidad = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelCapacidad.add(new JLabel("Capacidad Máxima:"));
        JTextField txtCapacidadConfig = new JTextField(String.valueOf(capacidadMaxima), 10);
        panelCapacidad.add(txtCapacidadConfig);
        panelBasicos.add(panelCapacidad);

        JPanel panelSimulaciones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelSimulaciones.add(new JLabel("Número de Simulaciones:"));
        JTextField txtSimulaciones = new JTextField(String.valueOf(numSimulaciones), 10);
        panelSimulaciones.add(txtSimulaciones);
        panelBasicos.add(panelSimulaciones);

        JPanel panelPruebas = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelPruebas.add(new JLabel("Pruebas Monte Carlo por Simulación:"));
        JTextField txtPruebas = new JTextField(String.valueOf(numPruebasMC), 10);
        panelPruebas.add(txtPruebas);
        panelBasicos.add(panelPruebas);

        // TAB 2: Elasticidades
        JPanel panelElasticidades = new JPanel(new GridLayout(0, 1, 10, 10));
        panelElasticidades.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelElasticidades.add(crearLabelTitulo("Límites de Elasticidad (Distribución Uniforme)"));

        JTextField[][] txtElasticidades = new JTextField[3][2];
        for (int i = 0; i < 3; i++) {
            panelElasticidades.add(crearLabelSubtitulo(tipos[i]));

            JPanel panelFila = new JPanel(new GridLayout(1, 4, 10, 0));

            txtElasticidades[i][0] = new JTextField(String.valueOf(elasticidadLimites[i][0]));
            txtElasticidades[i][1] = new JTextField(String.valueOf(elasticidadLimites[i][1]));

            panelFila.add(new JLabel("Mínimo:"));
            panelFila.add(txtElasticidades[i][0]);
            panelFila.add(new JLabel("Máximo:"));
            panelFila.add(txtElasticidades[i][1]);

            panelElasticidades.add(panelFila);
        }

        // TAB 3: Límites de Precios
        JPanel panelLimites = new JPanel(new GridLayout(0, 1, 10, 10));
        panelLimites.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        panelLimites.add(crearLabelTitulo("Límites de Precios para Optimización (Paso $1)"));

        JTextField[][] txtLimites = new JTextField[3][2];
        for (int i = 0; i < 3; i++) {
            panelLimites.add(crearLabelSubtitulo(tipos[i]));

            JPanel panelFila = new JPanel(new GridLayout(1, 4, 10, 0));

            txtLimites[i][0] = new JTextField(String.valueOf((int) precioLimites[i][0]));
            txtLimites[i][1] = new JTextField(String.valueOf((int) precioLimites[i][1]));

            panelFila.add(new JLabel("Precio Inferior ($):"));
            panelFila.add(txtLimites[i][0]);
            panelFila.add(new JLabel("Precio Superior ($):"));
            panelFila.add(txtLimites[i][1]);

            panelLimites.add(panelFila);
        }

        // Agregar tabs
        JScrollPane scrollBasicos = new JScrollPane(panelBasicos);
        JScrollPane scrollElasticidades = new JScrollPane(panelElasticidades);
        JScrollPane scrollLimites = new JScrollPane(panelLimites);

        tabbedPane.addTab("Básico", scrollBasicos);
        tabbedPane.addTab("Elasticidades", scrollElasticidades);
        tabbedPane.addTab("Límites de Precios", scrollLimites);

        dialogo.add(tabbedPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar Todo");
        JButton btnCancelar = new JButton("Cancelar");

        btnGuardar.setBackground(new Color(0, 120, 215));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Calibri", Font.BOLD, 13));

        btnGuardar.addActionListener(e -> {
            try {
                // Guardar precios y demandas
                for (int i = 0; i < 3; i++) {
                    preciosInicial[i] = Double.parseDouble(txtPrecios[i].getText());
                    demandaPromedio[i] = Double.parseDouble(txtDemandas[i].getText());
                }

                // Guardar parámetros generales
                capacidadMaxima = Integer.parseInt(txtCapacidadConfig.getText());
                numSimulaciones = Integer.parseInt(txtSimulaciones.getText());
                numPruebasMC = Integer.parseInt(txtPruebas.getText());

                // Guardar elasticidades
                for (int i = 0; i < 3; i++) {
                    elasticidadLimites[i][0] = Double.parseDouble(txtElasticidades[i][0].getText());
                    elasticidadLimites[i][1] = Double.parseDouble(txtElasticidades[i][1].getText());
                }

                // Guardar límites de precios
                for (int i = 0; i < 3; i++) {
                    precioLimites[i][0] = Double.parseDouble(txtLimites[i][0].getText());
                    precioLimites[i][1] = Double.parseDouble(txtLimites[i][1].getText());
                }

                // Actualizar UI
                txtCapacidad.setText(String.valueOf(capacidadMaxima));
                lblSimulacionesActual.setText("Simulaciones totales: 0 / " + numSimulaciones);
                lblPruebasActual.setText("Pruebas: 0 / " + numPruebasMC);
                progressBar.setMaximum(numSimulaciones);
                progressBarPruebas.setMaximum(numPruebasMC);

                llenarTablaInicial();
                dialogo.dispose();

                JOptionPane.showMessageDialog(this, "Todos los parámetros fueron actualizados correctamente", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialogo, "Error: Todos los valores deben ser numéricos válidos", "Error de Validación", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnCancelar.addActionListener(e -> dialogo.dispose());

        panelBotones.add(btnGuardar);
        panelBotones.add(btnCancelar);
        dialogo.add(panelBotones, BorderLayout.SOUTH);

        dialogo.setLocationRelativeTo(this);
        dialogo.setVisible(true);
    }

    private JLabel crearLabelTitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Calibri", Font.BOLD, 16));
        lbl.setForeground(new Color(31, 78, 120));
        return lbl;
    }

    private JLabel crearLabelSubtitulo(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Calibri", Font.BOLD, 14));
        return lbl;
    }

    private void ejecutarOptimizacion() {
        progressBar.setValue(0);
        progressBarPruebas.setValue(0);
        progressBar.setMaximum(numSimulaciones);
        progressBarPruebas.setMaximum(numPruebasMC);

        historialMejoras.clear();
        gananciasFinales.clear();
        demandasFinales.clear();
        mejorGananciaTotal = Double.NEGATIVE_INFINITY;

        SwingWorker<Void, ProgressUpdate> worker = new SwingWorker<>() {
            private long totalPruebasEjecutadas = 0;

            @Override
            protected Void doInBackground() {
                Random random = new Random();

                for (int iter = 0; iter < numSimulaciones; iter++) {
                    double precioStandard = generarPrecioAleatorio(precioLimites[0], random);
                    double precioGold = generarPrecioAleatorio(precioLimites[1], random);
                    double precioPlatinum = generarPrecioAleatorio(precioLimites[2], random);

                    double sumaGanancias = 0;
                    double sumaDemandas = 0;
                    int simulacionesValidas = 0;

                    ResultadoOptimizacion ultimoResultado = null;
                    List<Double> gananciasTemp = new ArrayList<>();
                    List<Double> demandasTemp = new ArrayList<>();

                    for (int mc = 0; mc < numPruebasMC; mc++) {
                        ResultadoOptimizacion resultado = simularConPrecios(precioStandard, precioGold, precioPlatinum, random);

                        totalPruebasEjecutadas++;

                        gananciasTemp.add(resultado.gananciaTotal);
                        demandasTemp.add(resultado.demandaTotal);

                        if (resultado.demandaTotal <= capacidadMaxima) {
                            sumaGanancias += resultado.gananciaTotal;
                            sumaDemandas += resultado.demandaTotal;
                            simulacionesValidas++;
                            ultimoResultado = resultado;
                        }

                        if (mc % 250 == 0 || mc == numPruebasMC - 1) {
                            publish(new ProgressUpdate(iter + 1, mc + 1));
                        }
                    }

                    if (simulacionesValidas > 0) {
                        double gananciaMedia = sumaGanancias / simulacionesValidas;
                        double demandaMedia = sumaDemandas / simulacionesValidas;

                        if (gananciaMedia > mejorGananciaTotal) {
                            mejorGananciaTotal = gananciaMedia;
                            mejorDemandaTotal = demandaMedia;
                            mejoresPrecios[0] = precioStandard;
                            mejoresPrecios[1] = precioGold;
                            mejoresPrecios[2] = precioPlatinum;

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
                        }
                    }
                }

                return null;
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) {
                ProgressUpdate ultimo = chunks.get(chunks.size() - 1);

                progressBar.setValue(ultimo.simulacion);
                lblSimulacionesActual.setText(String.format("Simulaciones totales: %d / %d", ultimo.simulacion, numSimulaciones));

                progressBarPruebas.setValue(ultimo.prueba);
                lblPruebasActual.setText(String.format("Pruebas: %d / %d", ultimo.prueba, numPruebasMC));
            }

            @Override
            protected void done() {
                actualizarTablaConMejoresResultados();
                btnOptimizar.setEnabled(true);
                btnGraficas.setEnabled(true);
                btnConfigurar.setEnabled(true);
                mostrarVentanaResultados();
            }
        };

        worker.execute();
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
            elasticidades[i] = elasticidadLimites[i][0] + random.nextDouble() * (elasticidadLimites[i][1] - elasticidadLimites[i][0]);
        }

        double[] nuevosPrecios = {precioStd, precioGold, precioPlt};

        for (int i = 0; i < 3; i++) {
            proyeccionesDemanda[i] = demandaPromedio[i] + elasticidades[i] * (nuevosPrecios[i] - preciosInicial[i]) * (demandaPromedio[i] / preciosInicial[i]);

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

        JLabel lblTitulo = new JLabel(numSimulaciones + " simulaciones", SwingConstants.LEFT);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JPanel panelTablas = new JPanel(new GridLayout(4, 1, 5, 5));
        panelTablas.setBackground(Color.WHITE);

        DecimalFormat df = new DecimalFormat("$#,##0.00");

        panelTablas.add(crearTablaResultado("Objetivos", "Valor", new String[]{"Maximizar el/la Media de Total Revenue"}, new String[]{df.format(mejorGananciaTotal)}));

        panelTablas.add(crearTablaResultado("Requisitos", "Valor", new String[]{"El/la Percentil 80% de Total room demand debe ser menor que"}, new String[]{String.valueOf(capacidadMaxima)}));

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
        int indicePercentil = (int) Math.ceil(percentilObjetivo * datosOrdenados.length) - 1;
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

    private static class ProgressUpdate {
        int simulacion;
        int prueba;

        ProgressUpdate(int sim, int pr) {
            this.simulacion = sim;
            this.prueba = pr;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            OptimizadorPreciosHotelEditable optimizador = new OptimizadorPreciosHotelEditable();
            optimizador.setVisible(true);
        });
    }
}