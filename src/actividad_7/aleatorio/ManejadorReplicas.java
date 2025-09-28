package actividad_7.aleatorio;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Clase responsable de manejar la generación y visualización de réplicas
 */
public class ManejadorReplicas {

    private final MotorSimulacion motorSimulacion;
    private final int tamanoRecomendado;

    public ManejadorReplicas(MotorSimulacion motorSimulacion, int tamanoRecomendado) {
        this.motorSimulacion = motorSimulacion;
        this.tamanoRecomendado = tamanoRecomendado;
    }

    /**
     * Genera 5 réplicas independientes y las muestra en una nueva pestaña
     */
    public void generarReplicas(JTabbedPane tabbedPane) {
        // Verificar si ya existe la pestaña de réplicas y eliminarla
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals("5 Réplicas")) {
                tabbedPane.removeTabAt(i);
                break;
            }
        }

        JPanel panelReplicas = new JPanel(new BorderLayout(15, 15));
        panelReplicas.setBackground(Color.WHITE);

        // Generar datos de réplicas una sola vez
        double[][] costosReplicas = new double[5][tamanoRecomendado];
        for (int replica = 0; replica < 5; replica++) {
            costosReplicas[replica] = motorSimulacion.simularReplicaCompleta(tamanoRecomendado);
        }

        // Crear panel superior con estadísticas de cada réplica
        JPanel panelEstadisticasReplicas = crearPanelEstadisticasReplicasConDatos(costosReplicas);
        panelReplicas.add(panelEstadisticasReplicas, BorderLayout.NORTH);

        // Crear gráfica de réplicas
        JPanel panelGraficaReplicas = crearPanelGraficaReplicas(costosReplicas);
        panelReplicas.add(panelGraficaReplicas, BorderLayout.CENTER);

        // Crear panel inferior con tabla comparativa de réplicas
        JPanel panelTablaReplicas = crearTablaReplicasConDatos(costosReplicas);
        panelReplicas.add(panelTablaReplicas, BorderLayout.SOUTH);

        // Agregar nueva pestaña al sistema de pestañas
        tabbedPane.addTab("5 Réplicas", panelReplicas);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    /**
     * Crea el panel que contiene la gráfica de líneas con las 5 réplicas
     */
    private JPanel crearPanelGraficaReplicas(double[][] costosReplicas) {
        JFreeChart chartReplicas = GeneradorGraficas.crearGraficaLineasReplicas(costosReplicas, tamanoRecomendado);

        ChartPanel chartPanel = new ChartPanel(chartReplicas);
        chartPanel.setPreferredSize(new Dimension(1200, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel.setBackground(Color.WHITE);

        JButton botonVerGrafica = new JButton("Ver Gráfica");
        botonVerGrafica.setFont(Constantes.FUENTE_GENERAL);
        botonVerGrafica.setBackground(Constantes.COLOR_PRIMARIO);
        botonVerGrafica.setForeground(Color.WHITE);
        botonVerGrafica.setFocusPainted(false);
        botonVerGrafica.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBoton.setBackground(Color.WHITE);
        panelBoton.add(botonVerGrafica);

        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBorder(BorderFactory.createTitledBorder(null, "Evolución del Costo Promedio por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));
        panelContenedor.setBackground(Color.WHITE);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);
        panelContenedor.add(panelBoton, BorderLayout.SOUTH);

        botonVerGrafica.addActionListener(e -> GeneradorGraficas.mostrarGraficaEnGrande(chartReplicas));

        return panelContenedor;
    }

    /**
     * Crea el panel con estadísticas usando datos ya generados
     */
    private JPanel crearPanelEstadisticasReplicasConDatos(double[][] costosReplicas) {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        for (int i = 0; i < 5; i++) {
            ModelosDeDatos.EstadisticasSimulacion stats = motorSimulacion.calcularEstadisticas(costosReplicas[i]);
            JPanel panelReplica = crearPanelReplicaIndividual(i + 1, stats);
            panel.add(panelReplica);
        }

        return panel;
    }

    /**
     * Crea la tabla usando datos ya generados
     */
    private JPanel crearTablaReplicasConDatos(double[][] costosReplicas) {
        String[] columnasReplicas = {
            "Día",
            "Costo promedio ($) Replica 1", "Costo promedio ($) Replica 2",
            "Costo promedio ($) Replica 3", "Costo promedio ($) Replica 4",
            "Costo promedio ($) Replica 5"
        };

        DefaultTableModel modelReplicas = new DefaultTableModel(columnasReplicas, 0);
        JTable tablaReplicas = new JTable(modelReplicas);

        ConfiguradorTabla.configurarTablaReplicas(tablaReplicas, columnasReplicas);

        llenarTablaConPromediosAcumulados(modelReplicas, costosReplicas);

        JScrollPane scrollReplicas = new JScrollPane(tablaReplicas);
        scrollReplicas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollReplicas.setPreferredSize(new Dimension(1200, 200));

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(null, "Tabla de Promedios Acumulados",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));
        panel.add(scrollReplicas, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);

        return panel;
    }

    /**
     * Llena la tabla con promedios acumulados de cada réplica
     */
    private void llenarTablaConPromediosAcumulados(DefaultTableModel modelReplicas, double[][] costosReplicas) {
        for (int dia = 0; dia < tamanoRecomendado; dia++) {
            Object[] fila = new Object[6];
            fila[0] = dia + 1;

            for (int replica = 0; replica < 5; replica++) {
                double sumaAcumulada = 0;

                for (int i = 0; i <= dia; i++) {
                    sumaAcumulada += costosReplicas[replica][i];
                }

                double promedioAcumulado = sumaAcumulada / (dia + 1);
                fila[replica + 1] = promedioAcumulado;
            }

            modelReplicas.addRow(fila);
        }
    }

    /**
     * Crea el panel individual con estadísticas de una réplica específica
     */
    private JPanel crearPanelReplicaIndividual(int numeroReplica, ModelosDeDatos.EstadisticasSimulacion stats) {
        JPanel panelReplica = new JPanel();
        panelReplica.setLayout(new BoxLayout(panelReplica, BoxLayout.Y_AXIS));

        panelReplica.setBorder(BorderFactory.createTitledBorder(null, "Replica " + numeroReplica,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_REPLICA, Constantes.COLOR_PRIMARIO));
        panelReplica.setBackground(Constantes.COLOR_PANEL_REPLICA);

        JLabel[] labels = crearEtiquetasEstadisticas(stats);

        for (int j = 0; j < labels.length; j++) {
            labels[j].setAlignmentX(Component.CENTER_ALIGNMENT);
            panelReplica.add(labels[j]);
            if (j < labels.length - 1) {
                panelReplica.add(Box.createVerticalStrut(3));
            }
        }

        return panelReplica;
    }

    /**
     * Crea las etiquetas con estadísticas formateadas para una réplica
     */
    private JLabel[] crearEtiquetasEstadisticas(ModelosDeDatos.EstadisticasSimulacion stats) {
        JLabel[] labels = {
            new JLabel("Promedio", SwingConstants.CENTER),
            new JLabel(String.format("$%,.2f", stats.promedio), SwingConstants.CENTER),
            new JLabel("Desviación", SwingConstants.CENTER),
            new JLabel(String.format("$%,.2f", stats.desviacion), SwingConstants.CENTER),
            new JLabel("Min", SwingConstants.CENTER),
            new JLabel(String.format("$%,.2f", stats.minimo), SwingConstants.CENTER),
            new JLabel("Max", SwingConstants.CENTER),
            new JLabel(String.format("$%,.2f", stats.maximo), SwingConstants.CENTER)
        };

        for (int j = 0; j < labels.length; j++) {
            if (j % 2 == 1) {
                labels[j].setFont(Constantes.FUENTE_VALOR);
            }
        }

        return labels;
    }
}
