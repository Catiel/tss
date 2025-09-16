package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que genera números aleatorios para evaluar decisiones Q */
public class PanelDulceAleatorio extends JPanel {
    private final JSpinner spReplicas;
    private final JTextField txtDecisiones;
    private final JSpinner spQDetalle;
    private final JButton btnSimular;
    private final DefaultTableModel modeloDistribucion;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComp;
    private final JLabel lblPromedio;

    private double[] randoms;

    public PanelDulceAleatorio() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación aleatoria (misma muestra para comparar Q)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel superior de controles
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Réplicas:"));
        spReplicas = new JSpinner(new SpinnerNumberModel(100, 10, 1000, 10));
        controles.add(spReplicas);
        controles.add(new JLabel("Decisiones Q (coma):"));
        txtDecisiones = new JTextField("40,50,60,70,80,90", 18);
        controles.add(txtDecisiones);
        controles.add(new JLabel("Q detalle:"));
        spQDetalle = new JSpinner(new SpinnerNumberModel(60, 1, 500, 1));
        controles.add(spQDetalle);
        btnSimular = new JButton("Generar y evaluar");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo - Distribución y Comparación
        JPanel panelIzq = new JPanel();
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // Tabla de distribución
        modeloDistribucion = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rango del # aleatorio", "Demanda"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        llenarDistribucion();
        JTable tablaDist = new JTable(modeloDistribucion);
        EstilosUI.aplicarEstiloTabla(tablaDist);
        tablaDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaDist.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaDist.getColumnModel().getColumn(3).setPreferredWidth(70);
        JScrollPane spDist = new JScrollPane(tablaDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades"));
        spDist.setPreferredSize(new Dimension(420, 200));
        panelIzq.add(spDist);

        panelIzq.add(Box.createVerticalStrut(8));

        // Tabla comparativa
        modeloComp = new DefaultTableModel(new Object[]{
            "Q", "Ganancia promedio"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaComp = new JTable(modeloComp);
        EstilosUI.aplicarEstiloTabla(tablaComp);
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp);
        spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones"));
        spComp.setPreferredSize(new Dimension(200, 200));
        panelIzq.add(spComp);

        // Tabla de simulación detallada
        modeloSim = new DefaultTableModel(new Object[]{
            "Replica", "# Aleatorio", "Demanda", "Ganancia"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaSim = new JTable(modeloSim);
        EstilosUI.aplicarEstiloTabla(tablaSim);
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(85);
        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Tabla de simulación detallada"));

        JSplitPane splitIzq = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, spSim);
        splitIzq.setResizeWeight(0.45);
        splitIzq.setOneTouchExpandable(true);
        add(splitIzq, BorderLayout.CENTER);

        lblPromedio = new JLabel(" ");
        EstilosUI.aplicarEstiloLabel(lblPromedio);
        lblPromedio.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(lblPromedio, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);

        // Simulación inicial
        simular(null);
    }

    private void llenarDistribucion() {
        modeloDistribucion.setRowCount(0);
        double[][] rangos = DulceModelo.getRangos();

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) {
            String rango = String.format("%.4f", rangos[i][0]) + " - " + String.format("%.4f", rangos[i][1]);
            modeloDistribucion.addRow(new Object[]{
                String.format("%.4f", DulceModelo.PROB[i]),
                String.format("%.4f", rangos[i][1]),
                rango,
                DulceModelo.DEMANDAS[i]
            });
        }
    }

    private void simular(ActionEvent e) {
        int n = (int) spReplicas.getValue();
        randoms = new double[n];
        for (int i = 0; i < n; i++) {
            randoms[i] = Math.random();
        }

        int Qdet = (int) spQDetalle.getValue();
        llenarTablaDetalle(Qdet);
        llenarComparativa();
    }

    private void llenarTablaDetalle(int Q) {
        modeloSim.setRowCount(0);
        double suma = 0;

        for (int i = 0; i < randoms.length; i++) {
            double r = randoms[i];
            int d = DulceModelo.demandaPara(r);
            double g = DulceModelo.ganancia(Q, d);
            suma += g;

            modeloSim.addRow(new Object[]{
                i + 1,
                String.format("%.4f", r),
                d,
                UtilFormatoDulce.m2(g)
            });
        }

        double prom = suma / randoms.length;
        lblPromedio.setText("Ganancia promedio para Q=" + Q + ": " + UtilFormatoDulce.m2(prom));
    }

    private void llenarComparativa() {
        modeloComp.setRowCount(0);
        String[] partes = txtDecisiones.getText().split(",");

        for (String p : partes) {
            p = p.trim();
            if (p.isEmpty()) continue;
            try {
                int Q = Integer.parseInt(p);
                double[] g = DulceModelo.simularGanancias(Q, randoms);
                double prom = DulceModelo.promedio(g);
                modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(prom)});
            } catch (Exception ignored) {}
        }
    }
}