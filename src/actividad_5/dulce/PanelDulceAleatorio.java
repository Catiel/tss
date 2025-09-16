package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que genera números aleatorios para simular demanda */
public class PanelDulceAleatorio extends JPanel {
    private final JSpinner spDias;
    private final JButton btnSimular;
    private final DefaultTableModel modeloDist;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComp;

    private double[] randoms; // números aleatorios generados

    private final JTextField txtValoresQ;
    private JLabel lblPromedio; // Para mostrar el promedio en el panel inferior

    public PanelDulceAleatorio() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación aleatoria");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de réplicas:"));
        spDias = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10));
        controles.add(spDias);
        controles.add(new JLabel("Valores Q a comparar (separados por coma):"));
        txtValoresQ = new JTextField("40,50,60,70,80,90", 15);
        controles.add(txtValoresQ);
        btnSimular = new JButton("Ejecutar Simulación");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: distribución y comparación
        JPanel panelIzq = new JPanel();
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // Tabla de distribución (referencia)
        modeloDist = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Demanda"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        llenarDistribucion();

        JTable tDist = new JTable(modeloDist);
        EstilosUI.aplicarEstiloTabla(tDist);
        tDist.getTableHeader().setBackground(new Color(200, 240, 255));
        tDist.getTableHeader().setForeground(Color.BLACK);
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tDist.getColumnModel().getColumn(3).setPreferredWidth(70);

        JScrollPane spDist = new JScrollPane(tDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades (referencia)"));
        spDist.setPreferredSize(new Dimension(430, 220));
        panelIzq.add(spDist);

        panelIzq.add(Box.createVerticalStrut(8));

        // Tabla de comparación de decisiones
        modeloComp = new DefaultTableModel(new Object[]{
            "Compra", "Ganancia promedio"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tablaComp = new JTable(modeloComp);
        EstilosUI.aplicarEstiloTabla(tablaComp);
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp);
        spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones"));
        spComp.setPreferredSize(new Dimension(210, 220));
        panelIzq.add(spComp);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Panel derecho: tabla de simulación
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

        JTable tablaSim = new JTable(modeloSim) {
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

        EstilosUI.aplicarEstiloTabla(tablaSim);
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(90);

        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación (Cantidad comprada = 60)"));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

        add(panelPrincipal, BorderLayout.CENTER);

        // Panel inferior para mostrar el promedio
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        EstilosUI.aplicarEstiloPanel(panelInferior);
        lblPromedio = new JLabel();
        EstilosUI.aplicarEstiloLabel(lblPromedio);
        lblPromedio.setFont(new Font("Arial", Font.BOLD, 14));
        lblPromedio.setForeground(new Color(0, 100, 0)); // Verde oscuro
        panelInferior.add(lblPromedio);
        add(panelInferior, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);

        // Ejecutar simulación inicial
        simular(null);
    }

    private void llenarDistribucion() {
        modeloDist.setRowCount(0);
        double[][] rangos = DulceModelo.getRangos();

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) {
            String rango = UtilFormatoDulce.p4(rangos[i][0]) + " - " + UtilFormatoDulce.p4(rangos[i][1]);
            modeloDist.addRow(new Object[]{
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

    private void simular(ActionEvent e) {
        int replicas = (int) spDias.getValue();

        // Generar números aleatorios
        randoms = new double[replicas];
        for (int i = 0; i < replicas; i++) {
            randoms[i] = Math.random();
        }

        // Llenar tabla de simulación con Q=60
        llenarSimulacion(60);

        // Llenar tabla comparativa
        llenarComparativa();
    }

    private void llenarSimulacion(int Q) {
        modeloSim.setRowCount(0);
        double suma = 0;

        for (int i = 0; i < randoms.length; i++) {
            double r = randoms[i];
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
        double promedio = suma / randoms.length;
        modeloSim.addRow(new Object[]{
            "TOTAL/PROMEDIO",
            "",
            "",
            UtilFormatoDulce.m2(promedio)
        });

        // Actualizar el label inferior
        lblPromedio.setText("Ganancia promedio para Q=60: " + UtilFormatoDulce.m2(promedio));
    }

    private void llenarComparativa() {
        modeloComp.setRowCount(0);
        String[] partes = txtValoresQ.getText().split(",");

        for (String parte : partes) {
            parte = parte.trim();
            if (parte.isEmpty()) continue;

            try {
                int Q = Integer.parseInt(parte);
                if (Q <= 0) continue; // Validar que sea un valor positivo

                double[] ganancias = DulceModelo.simularGanancias(Q, randoms);
                double promedio = DulceModelo.promedio(ganancias);
                modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)});
            } catch (NumberFormatException ex) {
                // Ignorar valores no numéricos
            }
        }
    }
}