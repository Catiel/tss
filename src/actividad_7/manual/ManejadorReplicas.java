// Paquete donde se encuentra la clase
package actividad_7.manual;

// Importa el panel de gráficos de JFreeChart
import org.jfree.chart.ChartPanel;
// Importa la clase principal de gráficos de JFreeChart
import org.jfree.chart.JFreeChart;

// Importa componentes de interfaz gráfica de Java Swing
import javax.swing.*;
// Importa el modelo de tabla por defecto de Swing
import javax.swing.table.DefaultTableModel;
// Importa clases para manejo de layouts y colores
import java.awt.*;

/**
 * Clase responsable de manejar la generación y visualización de réplicas
 */
public class ManejadorReplicas {

    // Motor de simulación utilizado para generar réplicas
    private final MotorSimulacion motorSimulacion;
    // Tamaño recomendado para la simulación
    private final int tamanoRecomendado;

    // Constructor que inicializa el motor de simulación y el tamaño recomendado
    public ManejadorReplicas(MotorSimulacion motorSimulacion, int tamanoRecomendado) {
        this.motorSimulacion = motorSimulacion; // Asigna el motor de simulación
        this.tamanoRecomendado = tamanoRecomendado; // Asigna el tamaño recomendado
    }

    /**
     * Genera 5 réplicas independientes y las muestra en una nueva pestaña
     */
    public void generarReplicas(JTabbedPane tabbedPane) {
        // Verificar si ya existe la pestaña de réplicas y eliminarla
        for (int i = 0; i < tabbedPane.getTabCount(); i++) { // Itera sobre las pestañas
            if (tabbedPane.getTitleAt(i).equals("5 Réplicas")) { // Si la pestaña es de réplicas
                tabbedPane.removeTabAt(i); // Elimina la pestaña existente
                break; // Sale del ciclo
            }
        }

        // Generar datos de réplicas una sola vez
        double[][] costosReplicas = new double[5][tamanoRecomendado]; // Matriz para almacenar los costos de cada réplica
        for (int replica = 0; replica < 5; replica++) { // Para cada réplica
            costosReplicas[replica] = motorSimulacion.simularReplicaCompleta(tamanoRecomendado); // Simula y almacena los costos
        }

        // Crear panel principal con scroll
        JPanel contenidoPrincipal = crearContenidoCompleto(costosReplicas); // Crea el contenido principal
        JScrollPane scrollPrincipal = new JScrollPane(contenidoPrincipal); // Agrega scroll al panel principal
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Barra vertical según necesidad
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Barra horizontal según necesidad
        scrollPrincipal.getVerticalScrollBar().setUnitIncrement(16); // Incremento de scroll

        // Agregar nueva pestaña al sistema de pestañas
        tabbedPane.addTab("5 Réplicas", scrollPrincipal); // Añade la pestaña de réplicas
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1); // Selecciona la nueva pestaña
    }

    /**
     * Crea todo el contenido de la pestaña de réplicas organizado verticalmente
     */
    private JPanel crearContenidoCompleto(double[][] costosReplicas) {
        JPanel panelCompleto = new JPanel(); // Panel principal
        panelCompleto.setLayout(new BoxLayout(panelCompleto, BoxLayout.Y_AXIS)); // Layout vertical
        panelCompleto.setBackground(Color.WHITE); // Fondo blanco
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Borde vacío

        // 1. Panel con estadísticas individuales de cada réplica
        JPanel panelEstadisticasIndividuales = crearPanelEstadisticasReplicasConDatos(costosReplicas); // Panel de estadísticas
        panelCompleto.add(panelEstadisticasIndividuales); // Agrega al panel principal
        panelCompleto.add(Box.createVerticalStrut(15)); // Espaciado vertical

        // 2. Panel con resumen estadístico
        JPanel panelResumen = crearPanelResumenEstadistico(costosReplicas); // Panel de resumen estadístico
        panelCompleto.add(panelResumen); // Agrega al panel principal
        panelCompleto.add(Box.createVerticalStrut(15)); // Espaciado vertical

        // 3. Panel con gráfica
        JPanel panelGrafica = crearPanelGraficaReplicas(costosReplicas); // Panel de gráfica
        panelCompleto.add(panelGrafica); // Agrega al panel principal
        panelCompleto.add(Box.createVerticalStrut(15)); // Espaciado vertical

        // 4. Panel con tabla de promedios acumulados
        JPanel panelTabla = crearTablaReplicasConDatos(costosReplicas); // Panel de tabla
        panelCompleto.add(panelTabla); // Agrega al panel principal

        return panelCompleto; // Devuelve el panel completo
    }

    /**
     * Crea el panel con resumen estadístico de las 5 réplicas
     * MODIFICADO: Solo cambia el cálculo de intervalos para casos NO normales
     */
    private JPanel crearPanelResumenEstadistico(double[][] costosReplicas) {
        // Calcular promedio final de cada réplica
        double[] promediosFinales = new double[5]; // Arreglo para promedios finales
        for (int replica = 0; replica < 5; replica++) { // Para cada réplica
            double suma = 0; // Suma acumulada
            for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
                suma += costosReplicas[replica][dia]; // Suma el costo del día
            }
            promediosFinales[replica] = suma / tamanoRecomendado; // Calcula el promedio
        }

        // Calcular estadísticas de los promedios de las réplicas
        double sumaPromedios = 0; // Suma de promedios
        for (double promedio : promediosFinales) { // Para cada promedio
            sumaPromedios += promedio; // Suma el promedio
        }
        double promedioGeneral = sumaPromedios / 5; // Promedio general

        // Calcular desviación estándar de los promedios (usando fórmula de muestra n-1)
        double sumaCuadradosDesviacion = 0; // Suma de cuadrados de desviación
        for (double promedio : promediosFinales) { // Para cada promedio
            sumaCuadradosDesviacion += Math.pow(promedio - promedioGeneral, 2); // Suma el cuadrado de la desviación
        }
        double desviacionEstandarPromedios = Math.sqrt(sumaCuadradosDesviacion / 4); // n-1 = 4

        // CAMBIO PRINCIPAL: Calcular intervalos según normalidad
        double intervaloInferior, intervaloSuperior; // Límites del intervalo

        if (motorSimulacion.isEsNormal()) { // Si la distribución es normal
            // CASO NORMAL: usar distribución t (código original)
            double valorT = 2.776; // t(0.025, 4) para 95% confianza con 4 grados de libertad
            double errorEstandar = desviacionEstandarPromedios / Math.sqrt(5); // Error estándar
            double margenError = errorEstandar * valorT; // Margen de error

            intervaloInferior = promedioGeneral - margenError; // Límite inferior
            intervaloSuperior = promedioGeneral + margenError; // Límite superior
        } else {
            // CASO NO NORMAL: usar la nueva fórmula
            double[] intervalos = motorSimulacion.calcularIntervalosConfianzaNoNormal(promediosFinales); // Calcula intervalos
            intervaloInferior = intervalos[0]; // Límite inferior
            intervaloSuperior = intervalos[1]; // Límite superior
        }

        // Panel principal del resumen
        JPanel panelResumen = new JPanel(new BorderLayout(15, 10)); // Panel con BorderLayout
        panelResumen.setBackground(Color.WHITE); // Fondo blanco
        panelResumen.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico de Réplicas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título

        // Panel horizontal que contiene ambas tablas
        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 20, 0)); // Panel con GridLayout
        panelTablas.setBackground(Color.WHITE); // Fondo blanco

        // Tabla izquierda: Promedios de réplicas
        JPanel panelTablaPromedios = crearTablaPromedios(promediosFinales, promedioGeneral, desviacionEstandarPromedios); // Tabla de promedios
        panelTablas.add(panelTablaPromedios); // Agrega la tabla de promedios

        // Tabla derecha: Intervalos de confianza
        JPanel panelTablaIntervalos = crearTablaIntervalos(intervaloInferior, intervaloSuperior); // Tabla de intervalos
        panelTablas.add(panelTablaIntervalos); // Agrega la tabla de intervalos

        panelResumen.add(panelTablas, BorderLayout.CENTER); // Agrega las tablas al panel resumen
        return panelResumen; // Devuelve el panel resumen
    }

    /**
     * Crea la tabla con promedios de cada réplica
     */
    private JPanel crearTablaPromedios(double[] promediosFinales, double promedioGeneral, double desviacion) {
        String[] columnas = {"", "Promedios"}; // Nombres de columnas
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
                return false; // Las celdas no son editables
            }
        };

        JTable tabla = new JTable(modelo); // Crea la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Fuente general
        tabla.setRowHeight(25); // Altura de fila
        tabla.setGridColor(Color.LIGHT_GRAY); // Color de la cuadrícula
        tabla.setShowGrid(true); // Mostrar cuadrícula
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajuste automático de columnas

        // Configurar header con fondo amarillo
        tabla.getTableHeader().setBackground(new Color(255, 255, 0)); // Fondo amarillo
        tabla.getTableHeader().setForeground(Color.BLACK); // Texto negro
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del header

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

        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setPreferredSize(new Dimension(250, 190)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.add(scroll, BorderLayout.CENTER); // Agrega el scroll al panel
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Crea la tabla con intervalos de confianza
     */
    private JPanel crearTablaIntervalos(double intervaloInferior, double intervaloSuperior) {
        String[] columnas = {"intervalos de confianza", ""}; // Nombres de columnas
        Object[][] datos = {
            {"inferior", String.format("$%,.2f", intervaloInferior)},
            {"superior", String.format("$%,.2f", intervaloSuperior)}
        };

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Las celdas no son editables
            }
        };

        JTable tabla = new JTable(modelo); // Crea la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Fuente general
        tabla.setRowHeight(25); // Altura de fila
        tabla.setGridColor(Color.LIGHT_GRAY); // Color de la cuadrícula
        tabla.setShowGrid(true); // Mostrar cuadrícula
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajuste automático de columnas

        // Configurar header
        tabla.getTableHeader().setBackground(Color.WHITE); // Fondo blanco
        tabla.getTableHeader().setForeground(Color.BLACK); // Texto negro
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del header

        // Renderizador para alineación
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setBackground(Color.WHITE); // Fondo blanco

                if (column == 1) {
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT); // Alinea a la izquierda
                }

                return comp;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setPreferredSize(new Dimension(300, 90)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.add(scroll, BorderLayout.CENTER); // Agrega el scroll al panel
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Crea el panel que contiene la gráfica de líneas con las 5 réplicas
     */
    private JPanel crearPanelGraficaReplicas(double[][] costosReplicas) {
        JFreeChart chartReplicas = GeneradorGraficas.crearGraficaLineasReplicas(costosReplicas, tamanoRecomendado); // Crea la gráfica

        ChartPanel chartPanel = new ChartPanel(chartReplicas); // Panel de la gráfica
        chartPanel.setPreferredSize(new Dimension(1200, 400)); // Tamaño preferido
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Borde vacío
        chartPanel.setBackground(Color.WHITE); // Fondo blanco

        JButton botonVerGrafica = new JButton("Ver Gráfica"); // Botón para ver la gráfica
        botonVerGrafica.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        botonVerGrafica.setBackground(Constantes.COLOR_PRIMARIO); // Fondo del botón
        botonVerGrafica.setForeground(Color.WHITE); // Texto blanco
        botonVerGrafica.setFocusPainted(false); // Sin borde de foco
        botonVerGrafica.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano

        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5)); // Panel para el botón
        panelBoton.setBackground(Color.WHITE); // Fondo blanco
        panelBoton.add(botonVerGrafica); // Agrega el botón

        JPanel panelContenedor = new JPanel(new BorderLayout()); // Panel contenedor
        panelContenedor.setBorder(BorderFactory.createTitledBorder(null, "Evolución del Costo Promedio por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título
        panelContenedor.setBackground(Color.WHITE); // Fondo blanco
        panelContenedor.add(chartPanel, BorderLayout.CENTER); // Agrega la gráfica
        panelContenedor.add(panelBoton, BorderLayout.SOUTH); // Agrega el botón abajo

        botonVerGrafica.addActionListener(e -> GeneradorGraficas.mostrarGraficaEnGrande(chartReplicas)); // Acción del botón

        return panelContenedor; // Devuelve el panel
    }

    /**
     * Crea el panel con estadísticas usando datos ya generados
     */
    private JPanel crearPanelEstadisticasReplicasConDatos(double[][] costosReplicas) {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 15)); // Panel con GridLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder(null, "Estadísticas Individuales por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título

        for (int i = 0; i < 5; i++) { // Para cada réplica
            ModelosDeDatos.EstadisticasSimulacion stats = motorSimulacion.calcularEstadisticas(costosReplicas[i]); // Calcula estadísticas
            JPanel panelReplica = crearPanelReplicaIndividual(i + 1, stats); // Crea panel individual
            panel.add(panelReplica); // Agrega al panel principal
        }

        return panel; // Devuelve el panel
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
        }; // Nombres de columnas

        DefaultTableModel modelReplicas = new DefaultTableModel(columnasReplicas, 0); // Modelo de tabla
        JTable tablaReplicas = new JTable(modelReplicas); // Tabla de réplicas

        ConfiguradorTabla.configurarTablaReplicas(tablaReplicas, columnasReplicas); // Configura la tabla

        llenarTablaConPromediosAcumulados(modelReplicas, costosReplicas); // Llena la tabla con datos

        JScrollPane scrollReplicas = new JScrollPane(tablaReplicas); // Scroll para la tabla
        scrollReplicas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
        scrollReplicas.setPreferredSize(new Dimension(1200, 300)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.setBorder(BorderFactory.createTitledBorder(null, "Tabla de Promedios Acumulados",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título
        panel.add(scrollReplicas, BorderLayout.CENTER); // Agrega el scroll al panel
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Llena la tabla con promedios acumulados de cada réplica
     */
    private void llenarTablaConPromediosAcumulados(DefaultTableModel modelReplicas, double[][] costosReplicas) {
        for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
            Object[] fila = new Object[6]; // Fila de la tabla
            fila[0] = dia + 1; // Día actual

            for (int replica = 0; replica < 5; replica++) { // Para cada réplica
                double sumaAcumulada = 0; // Suma acumulada

                for (int i = 0; i <= dia; i++) { // Suma hasta el día actual
                    sumaAcumulada += costosReplicas[replica][i]; // Suma el costo
                }

                double promedioAcumulado = sumaAcumulada / (dia + 1); // Calcula el promedio acumulado
                fila[replica + 1] = promedioAcumulado; // Asigna el promedio a la fila
            }

            modelReplicas.addRow(fila); // Agrega la fila al modelo
        }
    }

    /**
     * Crea el panel individual con estadísticas de una réplica específica
     */
    private JPanel crearPanelReplicaIndividual(int numeroReplica, ModelosDeDatos.EstadisticasSimulacion stats) {
        JPanel panelReplica = new JPanel(); // Panel individual
        panelReplica.setLayout(new BoxLayout(panelReplica, BoxLayout.Y_AXIS)); // Layout vertical

        panelReplica.setBorder(BorderFactory.createTitledBorder(null, "Replica " + numeroReplica,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_REPLICA, Constantes.COLOR_PRIMARIO)); // Borde con título
        panelReplica.setBackground(Constantes.COLOR_PANEL_REPLICA); // Fondo personalizado

        JLabel[] labels = crearEtiquetasEstadisticas(stats); // Etiquetas de estadísticas

        for (int j = 0; j < labels.length; j++) { // Para cada etiqueta
            labels[j].setAlignmentX(Component.CENTER_ALIGNMENT); // Centra la etiqueta
            panelReplica.add(labels[j]); // Agrega la etiqueta
            if (j < labels.length - 1) { // Si no es la última
                panelReplica.add(Box.createVerticalStrut(3)); // Espaciado vertical
            }
        }

        return panelReplica; // Devuelve el panel
    }

    /**
     * Crea las etiquetas con estadísticas formateadas para una réplica
     */
    private JLabel[] crearEtiquetasEstadisticas(ModelosDeDatos.EstadisticasSimulacion stats) {
        JLabel[] labels = {
            new JLabel("Promedio", SwingConstants.CENTER), // Etiqueta de promedio
            new JLabel(String.format("$%,.2f", stats.promedio), SwingConstants.CENTER), // Valor promedio
            new JLabel("Desviación", SwingConstants.CENTER), // Etiqueta de desviación
            new JLabel(String.format("$%,.2f", stats.desviacion), SwingConstants.CENTER), // Valor desviación
            new JLabel("Min", SwingConstants.CENTER), // Etiqueta mínimo
            new JLabel(String.format("$%,.2f", stats.minimo), SwingConstants.CENTER), // Valor mínimo
            new JLabel("Max", SwingConstants.CENTER), // Etiqueta máximo
            new JLabel(String.format("$%,.2f", stats.maximo), SwingConstants.CENTER) // Valor máximo
        };

        for (int j = 0; j < labels.length; j++) { // Para cada etiqueta
            if (j % 2 == 1) { // Si es valor
                labels[j].setFont(Constantes.FUENTE_VALOR); // Fuente de valor
            }
        }

        return labels; // Devuelve las etiquetas
    }
}