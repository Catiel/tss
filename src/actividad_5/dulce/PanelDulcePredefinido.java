package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con los datos predefinidos del Excel - 100 números aleatorios fijos */
public class PanelDulcePredefinido extends JPanel {
    private final DefaultTableModel modeloDistrib;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComparacion;

    public PanelDulcePredefinido() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Ejemplo predefinido (100 réplicas)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: distribución y comparación
        JPanel panelIzq = new JPanel();
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // Tabla de distribución
        modeloDistrib = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Demanda"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        llenarDistribucion();

        JTable tDist = new JTable(modeloDistrib);
        EstilosUI.aplicarEstiloTabla(tDist);
        tDist.getTableHeader().setBackground(new Color(200, 240, 255));
        tDist.getTableHeader().setForeground(Color.BLACK);
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tDist.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane spDist = new JScrollPane(tDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Tabla de distribución de probabilidades de la demanda"));
        spDist.setPreferredSize(new Dimension(430, 220));
        panelIzq.add(spDist);

        panelIzq.add(Box.createVerticalStrut(8));

        // Tabla de comparación de ganancias generales
        modeloComparacion = new DefaultTableModel(new Object[]{
            "Compra", "Ganancia promedio"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        llenarComparacion();

        JTable tablaComp = new JTable(modeloComparacion);
        EstilosUI.aplicarEstiloTabla(tablaComp);
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp);
        spComp.setBorder(BorderFactory.createTitledBorder("Ganancias generales de la simulación dulce hada"));
        spComp.setPreferredSize(new Dimension(210, 220));
        panelIzq.add(spComp);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Panel derecho: tabla de simulación (con Q=60 por defecto)
        modeloSim = new DefaultTableModel(new Object[]{
            "Replica", "# Aleatorio", "Demanda", "Ganancia"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 2) return Integer.class;
                return String.class;
            }
        };

        JTable tSim = new JTable(modeloSim) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row == getRowCount() - 1) { // fila de promedio
                    c.setBackground(new Color(220, 220, 220));
                } else if (row % 2 == 0) {
                    c.setBackground(Color.WHITE);
                } else {
                    c.setBackground(new Color(245, 245, 245));
                }
                return c;
            }
        };

        EstilosUI.aplicarEstiloTabla(tSim);
        tSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tSim.getColumnModel().getColumn(1).setPreferredWidth(80);
        tSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tSim.getColumnModel().getColumn(3).setPreferredWidth(90);

        simular();
        JScrollPane spSim = new JScrollPane(tSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Tabla de simulación (Variable de decisión: Cantidad comprada = 60)"));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Panel inferior para mostrar el promedio
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        EstilosUI.aplicarEstiloPanel(panelInferior);
        JLabel lblPromedio = new JLabel();
        EstilosUI.aplicarEstiloLabel(lblPromedio);
        lblPromedio.setFont(new Font("Arial", Font.BOLD, 14));
        lblPromedio.setForeground(new Color(0, 100, 0)); // Verde oscuro

        // Calcular y mostrar el promedio
        double[] ganancias = DulceModelo.simularGanancias(60, DulceModelo.RAND_FIJOS);
        double promedio = DulceModelo.promedio(ganancias);
        lblPromedio.setText("Ganancia promedio para Q=60: " + UtilFormatoDulce.m2(promedio));

        panelInferior.add(lblPromedio);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void llenarDistribucion() {
        modeloDistrib.setRowCount(0);
        double[][] rangos = DulceModelo.getRangos();

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) {
            String rango = UtilFormatoDulce.p4(rangos[i][0]) + " - " + UtilFormatoDulce.p4(rangos[i][1]);
            modeloDistrib.addRow(new Object[]{
                UtilFormatoDulce.p4(DulceModelo.PROB[i]),
                UtilFormatoDulce.p4(getSumaAcumulada(DulceModelo.PROB, i)),
                rango,
                DulceModelo.DEMANDAS[i]
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta) {
        double suma = 0;
        for (int i = 0; i <= hasta; i++) {
            suma += probs[i];
        }
        return suma;
    }

    private void llenarComparacion() {
        modeloComparacion.setRowCount(0);
        int[] decisiones = {40, 50, 60, 70, 80, 90};

        for (int Q : decisiones) {
            double[] ganancias = DulceModelo.simularGanancias(Q, DulceModelo.RAND_FIJOS);
            double promedio = DulceModelo.promedio(ganancias);
            modeloComparacion.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)});
        }
    }

    private void simular() {
        modeloSim.setRowCount(0);
        int Q = 60; // Cantidad fija para la tabla de simulación
        double suma = 0;

        for (int i = 0; i < DulceModelo.RAND_FIJOS.length; i++) {
            double r = DulceModelo.RAND_FIJOS[i];
            int demanda = DulceModelo.demandaPara(r);
            double ganancia = DulceModelo.ganancia(Q, demanda);
            suma += ganancia;

            modeloSim.addRow(new Object[]{
                i + 1,
                UtilFormatoDulce.p4(r),
                demanda,
                UtilFormatoDulce.m2(ganancia)
            });
        }

        // Fila de promedio con mejor formato
        double promedio = suma / DulceModelo.RAND_FIJOS.length;
        modeloSim.addRow(new Object[]{
            "TOTAL/PROMEDIO",
            "",
            "",
            UtilFormatoDulce.m2(promedio)
        });
    }
}