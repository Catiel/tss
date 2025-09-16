package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con los datos predefinidos y simulación de 10 días usando números aleatorios dados. */
public class PanelVentasPredefinido extends JPanel {
    private final DefaultTableModel modeloDistrib;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    // Números aleatorios predefinidos según las imágenes
    private static final double[] RAND = {0.07, 0.60, 0.77, 0.49, 0.76, 0.95, 0.51, 0.16, 0.14, 0.85};

    public PanelVentasPredefinido() {
        setLayout(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación Ventas de Programas de Fútbol - Ejemplo predefinido (10 días)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo: tabla de distribución
        modeloDistrib = new DefaultTableModel(new Object[]{
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Programas vendidos"
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
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90);
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120);
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140);
        tDist.getColumnModel().getColumn(3).setPreferredWidth(120);

        JScrollPane spDist = new JScrollPane(tDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de programas vendidos"));
        spDist.setPreferredSize(new Dimension(480, 250));
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

        JTable tSim = new JTable(modeloSim) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row == getRowCount() - 1) { // fila de totales/promedio
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
        tSim.getColumnModel().getColumn(0).setPreferredWidth(50);
        tSim.getColumnModel().getColumn(1).setPreferredWidth(80);
        tSim.getColumnModel().getColumn(2).setPreferredWidth(80);
        tSim.getColumnModel().getColumn(3).setPreferredWidth(80);

        simular();
        JScrollPane spSim = new JScrollPane(tSim);
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

        actualizarResumen();
        add(resumen, BorderLayout.SOUTH);
    }

    private void llenarDistribucion() {
        modeloDistrib.setRowCount(0);
        double[][] rangos = VentasModelo.getRangos();

        for (int i = 0; i < VentasModelo.DEMANDA.length; i++) {
            String rango = UtilFormatoVentas.f2(rangos[i][0]) + " - " + UtilFormatoVentas.f2(rangos[i][1]);
            modeloDistrib.addRow(new Object[]{
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

    private void simular() {
        modeloSim.setRowCount(0);
        int totalDemanda = 0;
        int totalGanancia = 0;

        for (int i = 0; i < RAND.length; i++) {
            double r = RAND[i];
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
        modeloSim.addRow(new Object[]{
            "Total",
            "",
            totalDemanda,
            totalGanancia
        });
    }

    private void actualizarResumen() {
        int totalDemanda = 0;
        int totalGanancia = 0;

        for (double r : RAND) {
            int demanda = VentasModelo.demandaPara(r);
            totalDemanda += demanda;
            totalGanancia += VentasModelo.gananciaParaDemanda(demanda);
        }

        double promDemanda = totalDemanda / (double) RAND.length;
        double promGanancia = totalGanancia / (double) RAND.length;

        StringBuilder sb = new StringBuilder();
        sb.append("RESULTADOS DE LA SIMULACIÓN (").append(RAND.length).append(" días):\n\n");
        sb.append("DEMANDA:\n");
        sb.append("• Total programas vendidos: ").append(totalDemanda).append(" programas\n");
        sb.append("• Promedio diario: ").append(UtilFormatoVentas.f2(promDemanda)).append(" programas\n");
        sb.append("• Valor esperado teórico: ").append(UtilFormatoVentas.f2(VentasModelo.esperadoDemanda())).append(" programas\n\n");

        sb.append("GANANCIAS:\n");
        sb.append("• Total ganancia: $").append(totalGanancia).append("\n");
        sb.append("• Ganancia promedio diaria: $").append(UtilFormatoVentas.f2(promGanancia)).append("\n");
        sb.append("• Valor esperado teórico: $").append(UtilFormatoVentas.f2(VentasModelo.esperadoGanancia())).append("\n\n");

        sb.append("ANÁLISIS:\n");
        sb.append("La simulación muestra los resultados para 10 días de venta de programas de fútbol.\n");
        sb.append("Los valores se acercan a los esperados teóricamente, validando el modelo de simulación.\n");
        sb.append("Con más días simulados, los promedios convergen hacia los valores esperados.");

        resumen.setText(sb.toString());
    }
}