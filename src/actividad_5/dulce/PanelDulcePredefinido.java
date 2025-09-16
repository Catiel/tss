package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con simulación usando los 100 números aleatorios fijos del Excel */
public class PanelDulcePredefinido extends JPanel {
    private final JSpinner spDecision;
    private final DefaultTableModel modeloDistribucion;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComparacion;
    private final JLabel lblPromedio;

    private static final int[] DECISIONES_DEFECTO = {40, 50, 60, 70, 80, 90};

    public PanelDulcePredefinido() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación (100 réplicas fijas)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel superior de controles
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(top);
        top.add(new JLabel("Cantidad comprada (Q):"));
        spDecision = new JSpinner(new SpinnerNumberModel(60, 40, 120, 10));
        top.add(spDecision);
        JButton btnActualizar = new JButton("Actualizar detalle");
        EstilosUI.aplicarEstiloBoton(btnActualizar);
        top.add(btnActualizar);
        add(top, BorderLayout.BEFORE_FIRST_LINE);

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
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades de la demanda"));
        spDist.setPreferredSize(new Dimension(420, 200));
        panelIzq.add(spDist);

        panelIzq.add(Box.createVerticalStrut(8));

        // Tabla de comparación de ganancias
        modeloComparacion = new DefaultTableModel(new Object[]{
            "Compra", "Ganancia promedio"
        }, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable tablaComp = new JTable(modeloComparacion);
        EstilosUI.aplicarEstiloTabla(tablaComp);
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp);
        spComp.setBorder(BorderFactory.createTitledBorder("Ganancias generales de la simulación dulce hada"));
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
        spSim.setBorder(BorderFactory.createTitledBorder("Tabla de simulación"));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, spSim);
        split.setResizeWeight(0.45);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblPromedio = new JLabel(" ");
        EstilosUI.aplicarEstiloLabel(lblPromedio);
        lblPromedio.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(lblPromedio, BorderLayout.SOUTH);

        btnActualizar.addActionListener(e -> refrescarSimulacion());

        // Carga inicial
        llenarComparativa();
        refrescarSimulacion();
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

    private void refrescarSimulacion() {
        int Q = (int) spDecision.getValue();
        modeloSim.setRowCount(0);

        double suma = 0;
        for (int i = 0; i < DulceModelo.RAND_FIJOS.length; i++) {
            double r = DulceModelo.RAND_FIJOS[i];
            int demanda = DulceModelo.demandaPara(r);
            double ganancia = DulceModelo.ganancia(Q, demanda);
            suma += ganancia;

            modeloSim.addRow(new Object[]{
                i + 1,
                String.format("%.4f", r),
                demanda,
                UtilFormatoDulce.m2(ganancia)
            });
        }

        double promedio = suma / DulceModelo.RAND_FIJOS.length;
        lblPromedio.setText("Ganancia promedio: " + UtilFormatoDulce.m2(promedio));
    }

    private void llenarComparativa() {
        modeloComparacion.setRowCount(0);

        for (int Q : DECISIONES_DEFECTO) {
            double[] ganancias = DulceModelo.simularGanancias(Q, DulceModelo.RAND_FIJOS);
            double promedio = DulceModelo.promedio(ganancias);

            modeloComparacion.addRow(new Object[]{
                Q,
                UtilFormatoDulce.m2(promedio)
            });
        }
    }
}