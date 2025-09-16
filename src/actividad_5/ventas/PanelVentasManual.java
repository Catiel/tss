package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios y simular ventas. */
public class PanelVentasManual extends JPanel {
    private final JSpinner spDias;
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final DefaultTableModel modeloDist;
    private final DefaultTableModel modeloInput;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    public PanelVentasManual() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación Ventas de Programas de Fútbol - Simulación manual");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de días:"));
        spDias = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        controles.add(spDias);
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

        // Panel izquierdo: tabla de distribución (referencia)
        modeloDist = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Programas vendidos"
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
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tDist.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane spDist = new JScrollPane(tDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de programas vendidos (referencia)"));
        spDist.setPreferredSize(new Dimension(480, 280));
        panelPrincipal.add(spDist, BorderLayout.WEST);

        // Panel central dividido verticalmente
        JPanel panelCentral = new JPanel(new GridLayout(2, 1, 5, 5));
        EstilosUI.aplicarEstiloPanel(panelCentral);

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{
            "Día", "# Aleatorio [0,1)"
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
        tablaInput.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaInput.getColumnModel().getColumn(1).setPreferredWidth(120);
        JScrollPane spInput = new JScrollPane(tablaInput);
        spInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)"));
        panelCentral.add(spInput);

        // Tabla de simulación (solo lectura)
        modeloSim = new DefaultTableModel(new Object[]{
            "Día", "# Aleatorio", "Demanda", "Ganancia"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
            @Override
            public Class<?> getColumnClass(int c) {
                if (c == 0 || c == 2 || c == 3) return Integer.class;
                return String.class;
            }
        };

        JTable tablaSim = new JTable(modeloSim) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row == getRowCount() - 1) { // fila de totales
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
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(80);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(80);

        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación"));
        panelCentral.add(spSim);

        panelPrincipal.add(panelCentral, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        // Área de resumen
        resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setBackground(getBackground());
        resumen.setLineWrap(true);
        resumen.setWrapStyleWord(true);
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados"));
        resumen.setFont(new Font("Arial", Font.PLAIN, 12));
        resumen.setPreferredSize(new Dimension(0, 120));
        add(resumen, BorderLayout.SOUTH);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);
    }

    private void llenarDistribucion() {
        modeloDist.setRowCount(0);
        double[][] rangos = VentasModelo.getRangos();

        for (int i = 0; i < VentasModelo.DEMANDA.length; i++) {
            String rango = UtilFormatoVentas.f2(rangos[i][0]) + " - " + UtilFormatoVentas.f2(rangos[i][1]);
            modeloDist.addRow(new Object[]{
                UtilFormatoVentas.f2(VentasModelo.PROBS[i]),
                UtilFormatoVentas.f2(getSumaAcumulada(VentasModelo.PROBS, i)),
                rango,
                VentasModelo.DEMANDA[i]
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
        int dias = (int) spDias.getValue();
        modeloInput.setRowCount(0);
        modeloSim.setRowCount(0);
        resumen.setText("");

        for (int i = 1; i <= dias; i++) {
            modeloInput.addRow(new Object[]{i, ""});
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

        modeloSim.setRowCount(0);
        int totalDemanda = 0;
        int totalGanancia = 0;

        for (int i = 0; i < modeloInput.getRowCount(); i++) {
            Double r = parseRand(modeloInput.getValueAt(i, 1));
            if (r == null) {
                JOptionPane.showMessageDialog(this,
                    "Día " + (i + 1) + ": Número aleatorio inválido\n" +
                    "Debe estar en el rango [0,1) y no estar vacío",
                    "Dato inválido",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            int demanda = VentasModelo.demandaPara(r);
            int ganancia = VentasModelo.gananciaParaDemanda(demanda);

            totalDemanda += demanda;
            totalGanancia += ganancia;

            modeloSim.addRow(new Object[]{
                i + 1,
                UtilFormatoVentas.f2(r),
                demanda,
                ganancia
            });
        }

        // Fila de totales
        int dias = modeloInput.getRowCount();
        modeloSim.addRow(new Object[]{
            "Total",
            "",
            totalDemanda,
            totalGanancia
        });

        double promDemanda = totalDemanda / (double) dias;
        double promGanancia = totalGanancia / (double) dias;

        // Actualizar resumen
        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE LA SIMULACIÓN MANUAL (").append(dias).append(" días):\n\n");

        sb.append("DEMANDA:\n");
        sb.append("• Total programas vendidos: ").append(totalDemanda).append(" programas\n");
        sb.append("• Promedio diario: ").append(UtilFormatoVentas.f2(promDemanda)).append(" programas\n");
        sb.append("• Valor esperado teórico: ").append(UtilFormatoVentas.f2(VentasModelo.esperadoDemanda())).append(" programas\n\n");

        sb.append("GANANCIAS:\n");
        sb.append("• Total ganancia: $").append(totalGanancia).append("\n");
        sb.append("• Ganancia promedio diaria: $").append(UtilFormatoVentas.f2(promGanancia)).append("\n");
        sb.append("• Valor esperado teórico: $").append(UtilFormatoVentas.f2(VentasModelo.esperadoGanancia())).append("\n\n");

        double difDemanda = Math.abs(promDemanda - VentasModelo.esperadoDemanda());
        double difGanancia = Math.abs(promGanancia - VentasModelo.esperadoGanancia());

        sb.append("ANÁLISIS:\n");
        sb.append("• Diferencia en demanda vs esperado: ").append(UtilFormatoVentas.f2(difDemanda)).append(" programas\n");
        sb.append("• Diferencia en ganancia vs esperado: $").append(UtilFormatoVentas.f2(difGanancia)).append("\n\n");

        sb.append("INTERPRETACIÓN:\n");
        sb.append("Los resultados dependen de los números aleatorios ingresados manualmente.\n");
        sb.append("La simulación permite evaluar diferentes escenarios cambiando los números aleatorios.\n");
        sb.append("Con números aleatorios bien distribuidos, los resultados se acercan a los valores teóricos.");

        resumen.setText(sb.toString());

        JOptionPane.showMessageDialog(this,
            "Simulación completada exitosamente!\n" +
            "Ganancia promedio: $" + UtilFormatoVentas.f2(promGanancia) + "\n" +
            "Demanda promedio: " + UtilFormatoVentas.f2(promDemanda) + " programas",
            "Cálculo terminado",
            JOptionPane.INFORMATION_MESSAGE);
    }
}