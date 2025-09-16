package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios y simular ganancias */
public class PanelDulceManual extends JPanel {
    private final JSpinner spReplicas;
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final DefaultTableModel modeloDist;
    private final DefaultTableModel modeloInput;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComp;

    public PanelDulceManual() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación manual");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de réplicas:"));
        spReplicas = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10));
        controles.add(spReplicas);
        btnCrear = new JButton("Crear tabla de entrada");
        EstilosUI.aplicarEstiloBoton(btnCrear);
        controles.add(btnCrear);
        btnCalcular = new JButton("Calcular resultados");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: distribución y entrada de datos
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
        spDist.setPreferredSize(new Dimension(430, 180));
        panelIzq.add(spDist);

        panelIzq.add(Box.createVerticalStrut(5));

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{
            "Replica", "# Aleatorio [0,1)"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 1; // Solo la columna de números aleatorios es editable
            }
            @Override
            public Class<?> getColumnClass(int c) {
                return c == 0 ? Integer.class : String.class;
            }
        };
        JTable tablaInput = new JTable(modeloInput);
        EstilosUI.aplicarEstiloTabla(tablaInput);
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200));
        tablaInput.getTableHeader().setForeground(Color.BLACK);
        tablaInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaInput.getColumnModel().getColumn(1).setPreferredWidth(120);
        JScrollPane spInput = new JScrollPane(tablaInput);
        spInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)"));
        spInput.setPreferredSize(new Dimension(190, 180));
        panelIzq.add(spInput);

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Panel central dividido verticalmente
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 5, 5));
        EstilosUI.aplicarEstiloPanel(panelCentral);

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
        panelCentral.add(spComp);

        // Tabla de simulación (solo lectura)
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
        spSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación"));
        panelCentral.add(spSim);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);
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

    private void crearFilas(ActionEvent e) {
        int replicas = (int) spReplicas.getValue();
        modeloInput.setRowCount(0);
        modeloSim.setRowCount(0);
        modeloComp.setRowCount(0);

        // Crear filas con los números fijos como ejemplo
        for (int i = 0; i < Math.min(replicas, DulceModelo.RAND_FIJOS.length); i++) {
            modeloInput.addRow(new Object[]{i + 1, UtilFormatoDulce.p4(DulceModelo.RAND_FIJOS[i])});
        }

        // Si se necesitan más filas, agregar vacías
        for (int i = DulceModelo.RAND_FIJOS.length; i < replicas; i++) {
            modeloInput.addRow(new Object[]{i + 1, ""});
        }

        btnCalcular.setEnabled(true);
    }

    private Double parseRand(Object v) {
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

    private void calcular(ActionEvent e) {
        if (modeloInput.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Primero debe crear la tabla de entrada", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validar y obtener números aleatorios
        double[] randoms = new double[modeloInput.getRowCount()];
        for (int i = 0; i < modeloInput.getRowCount(); i++) {
            Double r = parseRand(modeloInput.getValueAt(i, 1));
            if (r == null) {
                JOptionPane.showMessageDialog(this,
                    "Réplica " + (i + 1) + ": Número aleatorio inválido\n" +
                    "Debe estar en el rango [0,1) y no estar vacío",
                    "Dato inválido",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            randoms[i] = r;
        }

        // Llenar tabla de simulación con Q=60
        llenarSimulacion(randoms, 60);

        // Llenar tabla comparativa
        llenarComparativa(randoms);

        JOptionPane.showMessageDialog(this,
            "Simulación completada exitosamente!",
            "Cálculo terminado",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void llenarSimulacion(double[] randoms, int Q) {
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

        // Fila de promedio
        double promedio = suma / randoms.length;
        modeloSim.addRow(new Object[]{
            "Ganancia promedio",
            "",
            "",
            UtilFormatoDulce.m2(promedio)
        });
    }

    private void llenarComparativa(double[] randoms) {
        modeloComp.setRowCount(0);
        int[] decisiones = {40, 50, 60, 70, 80, 90};

        for (int Q : decisiones) {
            double[] ganancias = DulceModelo.simularGanancias(Q, randoms);
            double promedio = DulceModelo.promedio(ganancias);
            modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)});
        }
    }
}