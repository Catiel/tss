package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que genera números aleatorios para simular demanda diaria. */
public class PanelVentasAleatorio extends JPanel {
    private final JSpinner spDias;
    private final JButton btnSimular;
    private final DefaultTableModel modeloDist;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    public PanelVentasAleatorio() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación Ventas de Programas de Fútbol - Simulación aleatoria");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Número de días:"));
        spDias = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1));
        controles.add(spDias);
        btnSimular = new JButton("Ejecutar Simulación");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
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
        tDist.getTableHeader().setForeground(Color.BLACK); // texto negro header distribución
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tDist.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane spDist = new JScrollPane(tDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de programas vendidos (referencia)"));
        spDist.setPreferredSize(new Dimension(480, 280));
        panelPrincipal.add(spDist, BorderLayout.WEST);

        // Panel derecho: tabla de simulación
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
                if (row >= getRowCount() - 2) { // filas de totales y promedio
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
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación"));
        panelPrincipal.add(spSim, BorderLayout.CENTER);

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

        btnSimular.addActionListener(this::simular);

        // Ejecutar simulación inicial
        simular(null);
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

    private void simular(ActionEvent e) {
        int dias = (int) spDias.getValue();
        modeloSim.setRowCount(0);

        int totalDemanda = 0;
        int totalGanancia = 0;

        for (int d = 1; d <= dias; d++) {
            double r = Math.random();
            int demanda = VentasModelo.demandaPara(r);
            int ganancia = VentasModelo.gananciaParaDemanda(demanda);

            totalDemanda += demanda;
            totalGanancia += ganancia;

            modeloSim.addRow(new Object[]{
                d,
                UtilFormatoVentas.f2(r),
                demanda,
                ganancia
            });
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{
            "TOTALES",
            "",
            totalDemanda,
            totalGanancia
        });

        // Fila de promedio
        double promDemanda = totalDemanda / (double) dias;
        double promGanancia = totalGanancia / (double) dias;
        modeloSim.addRow(new Object[]{
            "PROMEDIO",
            "",
            UtilFormatoVentas.f2(promDemanda),
            UtilFormatoVentas.f2(promGanancia)
        });

        // Actualizar resumen
        actualizarResumen(dias, totalDemanda, totalGanancia, promDemanda, promGanancia);
    }

    private void actualizarResumen(int dias, int totalDemanda, int totalGanancia, double promDemanda, double promGanancia) {
        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE LA SIMULACIÓN ALEATORIA (").append(dias).append(" días):\n\n");

        sb.append("DEMANDA:\n");
        sb.append("• Total programas vendidos: ").append(totalDemanda).append(" programas\n");
        sb.append("• Promedio diario simulado: ").append(UtilFormatoVentas.f2(promDemanda)).append(" programas\n");

        sb.append("GANANCIAS:\n");
        sb.append("• Total ganancia: $").append(totalGanancia).append("\n");
        sb.append("• Ganancia promedio diaria: $").append(UtilFormatoVentas.f2(promGanancia)).append("\n");

        resumen.setText(sb.toString());
    }
}