package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel manual: permite editar los números aleatorios y evaluar decisiones Q */
public class PanelDulceManual extends JPanel {
    private final JTextField txtDecisiones;
    private final JSpinner spQDetalle;
    private final JButton btnCargarFijos;
    private final JButton btnGenerarAleatorios;
    private final JButton btnCalcular;
    private final JButton btnComparar;

    private final DefaultTableModel modeloDistribucion;
    private final DefaultTableModel modeloRandoms;
    private final DefaultTableModel modeloDetalle;
    private final DefaultTableModel modeloComparativa;
    private final JLabel lblPromedioDetalle;

    public PanelDulceManual() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación manual (editar números aleatorios)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel superior de controles
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Decisiones Q (coma):"));
        txtDecisiones = new JTextField("40,50,60,70,80,90", 16);
        controles.add(txtDecisiones);
        controles.add(new JLabel("Q detalle:"));
        spQDetalle = new JSpinner(new SpinnerNumberModel(60, 1, 500, 1));
        controles.add(spQDetalle);
        btnCargarFijos = new JButton("Cargar 100 fijos");
        EstilosUI.aplicarEstiloBoton(btnCargarFijos);
        controles.add(btnCargarFijos);
        btnGenerarAleatorios = new JButton("Aleatorios");
        EstilosUI.aplicarEstiloBoton(btnGenerarAleatorios);
        controles.add(btnGenerarAleatorios);
        btnCalcular = new JButton("Calcular detalle");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        controles.add(btnCalcular);
        btnComparar = new JButton("Comparar Qs");
        EstilosUI.aplicarEstiloBoton(btnComparar);
        controles.add(btnComparar);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo dividido verticalmente
        JPanel panelIzq = new JPanel(new GridLayout(3, 1, 5, 5));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // 1. Tabla de distribución
        modeloDistribucion = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rango del # aleatorio", "Demanda"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        llenarDistribucion();
        JTable tablaDist = new JTable(modeloDistribucion);
        EstilosUI.aplicarEstiloTabla(tablaDist);
        tablaDist.getTableHeader().setBackground(new Color(200, 240, 255));
        tablaDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaDist.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tablaDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaDist.getColumnModel().getColumn(3).setPreferredWidth(70);
        JScrollPane spDist = new JScrollPane(tablaDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades"));
        panelIzq.add(spDist);

        // 2. Tabla de números aleatorios editables
        modeloRandoms = new DefaultTableModel(new Object[]{"Replica", "r"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return c == 1; }
            @Override public Class<?> getColumnClass(int c) { return c == 0 ? Integer.class : String.class; }
        };
        JTable tablaR = new JTable(modeloRandoms);
        EstilosUI.aplicarEstiloTabla(tablaR);
        tablaR.getTableHeader().setBackground(new Color(255, 255, 200));
        tablaR.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaR.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaR.getColumnModel().getColumn(1).setPreferredWidth(80);
        JScrollPane spR = new JScrollPane(tablaR);
        spR.setBorder(BorderFactory.createTitledBorder("Números aleatorios r [0,1)"));
        panelIzq.add(spR);

        // 3. Tabla comparativa
        modeloComparativa = new DefaultTableModel(new Object[]{"Q", "Ganancia promedio"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaComp = new JTable(modeloComparativa);
        EstilosUI.aplicarEstiloTabla(tablaComp);
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp);
        spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones"));
        panelIzq.add(spComp);

        // Panel derecho - Tabla detalle
        modeloDetalle = new DefaultTableModel(new Object[]{
            "Replica", "r", "Demanda", "Ganancia"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tableDet = new JTable(modeloDetalle);
        EstilosUI.aplicarEstiloTabla(tableDet);
        tableDet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tableDet.getColumnModel().getColumn(0).setPreferredWidth(60);
        tableDet.getColumnModel().getColumn(1).setPreferredWidth(70);
        tableDet.getColumnModel().getColumn(2).setPreferredWidth(70);
        tableDet.getColumnModel().getColumn(3).setPreferredWidth(85);
        JScrollPane spDet = new JScrollPane(tableDet);
        spDet.setBorder(BorderFactory.createTitledBorder("Detalle para Q"));

        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, spDet);
        splitMain.setResizeWeight(0.45);
        splitMain.setOneTouchExpandable(true);
        add(splitMain, BorderLayout.CENTER);

        lblPromedioDetalle = new JLabel(" ");
        EstilosUI.aplicarEstiloLabel(lblPromedioDetalle);
        lblPromedioDetalle.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(lblPromedioDetalle, BorderLayout.SOUTH);

        // Listeners
        btnCargarFijos.addActionListener(this::cargarFijos);
        btnGenerarAleatorios.addActionListener(this::generarAleatorios);
        btnCalcular.addActionListener(this::calcularDetalle);
        btnComparar.addActionListener(this::comparar);

        // Carga inicial
        cargarFijos(null);
        calcularDetalle(null);
        comparar(null);
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

    private void cargarFijos(ActionEvent e) {
        modeloRandoms.setRowCount(0);
        for (int i = 0; i < DulceModelo.RAND_FIJOS.length; i++) {
            modeloRandoms.addRow(new Object[]{i + 1, String.format("%.4f", DulceModelo.RAND_FIJOS[i])});
        }
    }

    private void generarAleatorios(ActionEvent e) {
        int n = DulceModelo.RAND_FIJOS.length;
        modeloRandoms.setRowCount(0);
        for (int i = 0; i < n; i++) {
            modeloRandoms.addRow(new Object[]{i + 1, String.format("%.4f", Math.random())});
        }
    }

    private Double parseR(Object v) {
        if (v == null) return null;
        String t = v.toString().trim().replace(',', '.');
        if (t.isEmpty()) return null;
        try {
            double d = Double.parseDouble(t);
            if (d < 0 || d >= 1) return null;
            return d;
        } catch (Exception ex) {
            return null;
        }
    }

    private double[] obtenerRandoms() {
        int n = modeloRandoms.getRowCount();
        double[] arr = new double[n];
        for (int i = 0; i < n; i++) {
            Double r = parseR(modeloRandoms.getValueAt(i, 1));
            if (r == null) {
                throw new IllegalArgumentException("Fila " + (i + 1) + " r inválido");
            }
            arr[i] = r;
        }
        return arr;
    }

    private void calcularDetalle(ActionEvent e) {
        try {
            int Q = (int) spQDetalle.getValue();
            double[] rs = obtenerRandoms();
            modeloDetalle.setRowCount(0);
            double suma = 0;

            for (int i = 0; i < rs.length; i++) {
                int d = DulceModelo.demandaPara(rs[i]);
                double g = DulceModelo.ganancia(Q, d);
                suma += g;

                modeloDetalle.addRow(new Object[]{
                    i + 1,
                    String.format("%.4f", rs[i]),
                    d,
                    UtilFormatoDulce.m2(g)
                });
            }

            lblPromedioDetalle.setText("Ganancia promedio Q=" + Q + ": " + UtilFormatoDulce.m2(suma / rs.length));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void comparar(ActionEvent e) {
        try {
            double[] rs = obtenerRandoms();
            modeloComparativa.setRowCount(0);
            String[] partes = txtDecisiones.getText().split(",");

            for (String p : partes) {
                p = p.trim();
                if (p.isEmpty()) continue;
                try {
                    int Q = Integer.parseInt(p);
                    double[] g = DulceModelo.simularGanancias(Q, rs);
                    double prom = DulceModelo.promedio(g);
                    modeloComparativa.addRow(new Object[]{Q, UtilFormatoDulce.m2(prom)});
                } catch (Exception ignored) {}
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}