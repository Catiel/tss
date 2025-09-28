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

        // Generar datos de réplicas una sola vez
        double[][] costosReplicas = new double[5][tamanoRecomendado];
        for (int replica = 0; replica < 5; replica++) {
            costosReplicas[replica] = motorSimulacion.simularReplicaCompleta(tamanoRecomendado);
        }

        // Crear panel principal con scroll
        JPanel contenidoPrincipal = crearContenidoCompleto(costosReplicas);
        JScrollPane scrollPrincipal = new JScrollPane(contenidoPrincipal);
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPrincipal.getVerticalScrollBar().setUnitIncrement(16);

        // Agregar nueva pestaña al sistema de pestañas
        tabbedPane.addTab("5 Réplicas", scrollPrincipal);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    /**
     * Crea todo el contenido de la pestaña de réplicas organizado verticalmente
     */
    private JPanel crearContenidoCompleto(double[][] costosReplicas) {
        JPanel panelCompleto = new JPanel();
        panelCompleto.setLayout(new BoxLayout(panelCompleto, BoxLayout.Y_AXIS));
        panelCompleto.setBackground(Color.WHITE);
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1. Panel con estadísticas individuales de cada réplica
        JPanel panelEstadisticasIndividuales = crearPanelEstadisticasReplicasConDatos(costosReplicas);
        panelCompleto.add(panelEstadisticasIndividuales);
        panelCompleto.add(Box.createVerticalStrut(15));

        // 2. Panel con resumen estadístico
        JPanel panelResumen = crearPanelResumenEstadistico(costosReplicas);
        panelCompleto.add(panelResumen);
        panelCompleto.add(Box.createVerticalStrut(15));

        // 3. Panel con gráfica
        JPanel panelGrafica = crearPanelGraficaReplicas(costosReplicas);
        panelCompleto.add(panelGrafica);
        panelCompleto.add(Box.createVerticalStrut(15));

        // 4. Panel con tabla de promedios acumulados
        JPanel panelTabla = crearTablaReplicasConDatos(costosReplicas);
        panelCompleto.add(panelTabla);

        return panelCompleto;
    }

    /**
     * Crea el panel con resumen estadístico de las 5 réplicas
     */
    private JPanel crearPanelResumenEstadistico(double[][] costosReplicas) {
        // Calcular promedio final de cada réplica
        double[] promediosFinales = new double[5];
        for (int replica = 0; replica < 5; replica++) {
            double suma = 0;
            for (int dia = 0; dia < tamanoRecomendado; dia++) {
                suma += costosReplicas[replica][dia];
            }
            promediosFinales[replica] = suma / tamanoRecomendado;
        }

        // Calcular estadísticas de los promedios de las réplicas
        double sumaPromedios = 0;
        for (double promedio : promediosFinales) {
            sumaPromedios += promedio;
        }
        double promedioGeneral = sumaPromedios / 5;

        // Calcular desviación estándar de los promedios (usando fórmula de muestra n-1)
        double sumaCuadradosDesviacion = 0;
        for (double promedio : promediosFinales) {
            sumaCuadradosDesviacion += Math.pow(promedio - promedioGeneral, 2);
        }
        double desviacionEstandarPromedios = Math.sqrt(sumaCuadradosDesviacion / 4); // n-1 = 4

        // Calcular intervalos de confianza (95%, t con 4 grados de libertad)
        double valorT = 2.776; // t(0.025, 4) para 95% confianza con 4 grados de libertad
        double errorEstandar = desviacionEstandarPromedios / Math.sqrt(5);
        double margenError = errorEstandar * valorT;

        double intervaloInferior = promedioGeneral - margenError;
        double intervaloSuperior = promedioGeneral + margenError;

        // Panel principal del resumen
        JPanel panelResumen = new JPanel(new BorderLayout(15, 10));
        panelResumen.setBackground(Color.WHITE);
        panelResumen.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico de Réplicas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));

        // Panel horizontal que contiene ambas tablas
        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 20, 0));
        panelTablas.setBackground(Color.WHITE);

        // Tabla izquierda: Promedios de réplicas
        JPanel panelTablaPromedios = crearTablaPromedios(promediosFinales, promedioGeneral, desviacionEstandarPromedios);
        panelTablas.add(panelTablaPromedios);

        // Tabla derecha: Intervalos de confianza
        JPanel panelTablaIntervalos = crearTablaIntervalos(intervaloInferior, intervaloSuperior);
        panelTablas.add(panelTablaIntervalos);

        panelResumen.add(panelTablas, BorderLayout.CENTER);
        return panelResumen;
    }

    /**
     * Crea la tabla con promedios de cada réplica
     */
    private JPanel crearTablaPromedios(double[] promediosFinales, double promedioGeneral, double desviacion) {
        String[] columnas = {"", "Promedios"};
        Object[][] datos = {
            {"replica 1", String.format("$%,.2f", promediosFinales[0])},
            {"replica 2", String.format("$%,.2f", promediosFinales[1])},
            {"replica 3", String.format("$%,.2f", promediosFinales[2])},
            {"replica 4", String.format("$%,.2f", promediosFinales[3])},
            {"replica 5", String.format("$%,.2f", promediosFinales[4])},
            {"promedio", String.format("$%,.2f", promedioGeneral)},
            {"desviación", String.format("$%,.2f", desviacion)}
        };

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setFont(Constantes.FUENTE_GENERAL);
        tabla.setRowHeight(25);
        tabla.setGridColor(Color.LIGHT_GRAY);
        tabla.setShowGrid(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Configurar header con fondo amarillo
        tabla.getTableHeader().setBackground(new Color(255, 255, 0));
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Renderizador personalizado
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fondo amarillo claro para promedio y desviación
                if (row == 5 || row == 6) {
                    comp.setBackground(new Color(255, 255, 200));
                } else {
                    comp.setBackground(Color.WHITE);
                }

                // Alineación
                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(250, 190));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);

        return panel;
    }

    /**
     * Crea la tabla con intervalos de confianza
     */
    private JPanel crearTablaIntervalos(double intervaloInferior, double intervaloSuperior) {
        String[] columnas = {"intervalos de confianza", ""};
        Object[][] datos = {
            {"inferior", String.format("$%,.2f", intervaloInferior)},
            {"superior", String.format("$%,.2f", intervaloSuperior)}
        };

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable tabla = new JTable(modelo);
        tabla.setFont(Constantes.FUENTE_GENERAL);
        tabla.setRowHeight(25);
        tabla.setGridColor(Color.LIGHT_GRAY);
        tabla.setShowGrid(true);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        // Configurar header
        tabla.getTableHeader().setBackground(Color.WHITE);
        tabla.getTableHeader().setForeground(Color.BLACK);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));

        // Renderizador para alineación
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setBackground(Color.WHITE);

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT);
                }

                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(300, 90));

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scroll, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);

        return panel;
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
        panel.setBorder(BorderFactory.createTitledBorder(null, "Estadísticas Individuales por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO));

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
        scrollReplicas.setPreferredSize(new Dimension(1200, 300));

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