package actividad_7.predeterminado;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Clase responsable de manejar la generación y visualización de réplicas
 */
public class ManejadorReplicas { // Declaración de la clase ManejadorReplicas

    private final MotorSimulacion motorSimulacion; // Motor de simulación utilizado
    private final int tamanoRecomendado; // Tamaño recomendado para la simulación

    public ManejadorReplicas(MotorSimulacion motorSimulacion, int tamanoRecomendado) { // Constructor de la clase
        this.motorSimulacion = motorSimulacion; // Asigna el motor de simulación recibido
        this.tamanoRecomendado = tamanoRecomendado; // Asigna el tamaño recomendado recibido
    }

    /**
     * Genera 5 réplicas independientes y las muestra en una nueva pestaña
     */
    public void generarReplicas(JTabbedPane tabbedPane) { // Método para generar réplicas
        // Verificar si ya existe la pestaña de réplicas y eliminarla
        for (int i = 0; i < tabbedPane.getTabCount(); i++) { // Recorre todas las pestañas
            if (tabbedPane.getTitleAt(i).equals("5 Réplicas")) { // Si la pestaña se llama "5 Réplicas"
                tabbedPane.removeTabAt(i); // Elimina la pestaña
                break; // Sale del ciclo
            }
        }

        // Generar datos de réplicas una sola vez
        double[][] costosReplicas = new double[5][tamanoRecomendado]; // Crea matriz para los costos de las réplicas
        for (int replica = 0; replica < 5; replica++) { // Repite para cada réplica
            costosReplicas[replica] = motorSimulacion.simularReplicaCompleta(tamanoRecomendado); // Simula la réplica
        }

        // Crear panel principal con scroll
        JPanel contenidoPrincipal = crearContenidoCompleto(costosReplicas); // Crea el panel principal
        JScrollPane scrollPrincipal = new JScrollPane(contenidoPrincipal); // Agrega scroll al panel
        scrollPrincipal.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED); // Scroll vertical
        scrollPrincipal.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED); // Scroll horizontal
        scrollPrincipal.getVerticalScrollBar().setUnitIncrement(16); // Incremento del scroll

        // Agregar nueva pestaña al sistema de pestañas
        tabbedPane.addTab("5 Réplicas", scrollPrincipal); // Agrega la pestaña
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1); // Selecciona la nueva pestaña
    }

    /**
     * Crea todo el contenido de la pestaña de réplicas organizado verticalmente
     */
    private JPanel crearContenidoCompleto(double[][] costosReplicas) { // Método para crear el contenido completo
        JPanel panelCompleto = new JPanel(); // Crea el panel principal
        panelCompleto.setLayout(new BoxLayout(panelCompleto, BoxLayout.Y_AXIS)); // Layout vertical
        panelCompleto.setBackground(Color.WHITE); // Fondo blanco
        panelCompleto.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Borde vacío

        // 1. Panel con estadísticas individuales de cada réplica
        JPanel panelEstadisticasIndividuales = crearPanelEstadisticasReplicasConDatos(costosReplicas); // Panel de estadísticas
        panelCompleto.add(panelEstadisticasIndividuales); // Agrega el panel
        panelCompleto.add(Box.createVerticalStrut(15)); // Espacio vertical

        // 2. Panel con resumen estadístico
        JPanel panelResumen = crearPanelResumenEstadistico(costosReplicas); // Panel de resumen
        panelCompleto.add(panelResumen); // Agrega el panel
        panelCompleto.add(Box.createVerticalStrut(15)); // Espacio vertical

        // 3. Panel con gráfica
        JPanel panelGrafica = crearPanelGraficaReplicas(costosReplicas); // Panel de gráfica
        panelCompleto.add(panelGrafica); // Agrega el panel
        panelCompleto.add(Box.createVerticalStrut(15)); // Espacio vertical

        // 4. Panel con tabla de promedios acumulados
        JPanel panelTabla = crearTablaReplicasConDatos(costosReplicas); // Panel de tabla
        panelCompleto.add(panelTabla); // Agrega el panel

        return panelCompleto; // Devuelve el panel completo
    }

    /**
     * Crea el panel con resumen estadístico de las 5 réplicas
     * MODIFICADO: Solo cambia el cálculo de intervalos para casos NO normales
     */
    private JPanel crearPanelResumenEstadistico(double[][] costosReplicas) { // Método para crear el panel de resumen
        // Calcular promedio final de cada réplica
        double[] promediosFinales = new double[5]; // Arreglo de promedios
        for (int replica = 0; replica < 5; replica++) { // Para cada réplica
            double suma = 0; // Suma acumulada
            for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
                suma += costosReplicas[replica][dia]; // Suma el costo
            }
            promediosFinales[replica] = suma / tamanoRecomendado; // Calcula el promedio
        }

        // Calcular estadísticas de los promedios de las réplicas
        double sumaPromedios = 0; // Suma de promedios
        for (double promedio : promediosFinales) { // Para cada promedio
            sumaPromedios += promedio; // Suma el promedio
        }
        double promedioGeneral = sumaPromedios / 5; // Calcula el promedio general

        // Calcular desviación estándar de los promedios (usando fórmula de muestra n-1)
        double sumaCuadradosDesviacion = 0; // Suma de cuadrados
        for (double promedio : promediosFinales) { // Para cada promedio
            sumaCuadradosDesviacion += Math.pow(promedio - promedioGeneral, 2); // Suma el cuadrado de la diferencia
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
        } else { // Si la distribución NO es normal
            // CASO NO NORMAL: usar la nueva fórmula
            double[] intervalos = motorSimulacion.calcularIntervalosConfianzaNoNormal(promediosFinales); // Calcula intervalos
            intervaloInferior = intervalos[0]; // Límite inferior
            intervaloSuperior = intervalos[1]; // Límite superior
        }

        // Panel principal del resumen
        JPanel panelResumen = new JPanel(new BorderLayout(15, 10)); // Crea el panel
        panelResumen.setBackground(Color.WHITE); // Fondo blanco
        panelResumen.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico de Réplicas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título

        // Panel horizontal que contiene ambas tablas
        JPanel panelTablas = new JPanel(new GridLayout(1, 2, 20, 0)); // Panel de tablas
        panelTablas.setBackground(Color.WHITE); // Fondo blanco

        // Tabla izquierda: Promedios de réplicas
        JPanel panelTablaPromedios = crearTablaPromedios(promediosFinales, promedioGeneral, desviacionEstandarPromedios); // Tabla de promedios
        panelTablas.add(panelTablaPromedios); // Agrega la tabla

        // Tabla derecha: Intervalos de confianza
        JPanel panelTablaIntervalos = crearTablaIntervalos(intervaloInferior, intervaloSuperior); // Tabla de intervalos
        panelTablas.add(panelTablaIntervalos); // Agrega la tabla

        panelResumen.add(panelTablas, BorderLayout.CENTER); // Agrega las tablas al panel
        return panelResumen; // Devuelve el panel
    }

    /**
     * Crea la tabla con promedios de cada réplica
     */
    private JPanel crearTablaPromedios(double[] promediosFinales, double promedioGeneral, double desviacion) { // Método para crear la tabla de promedios
        String[] columnas = {"", "Promedios"}; // Nombres de columnas
        Object[][] datos = { // Datos de la tabla
            {"replica 1", String.format("$%,.2f", promediosFinales[0])},
            {"replica 2", String.format("$%,.2f", promediosFinales[1])},
            {"replica 3", String.format("$%,.2f", promediosFinales[2])},
            {"replica 4", String.format("$%,.2f", promediosFinales[3])},
            {"replica 5", String.format("$%,.2f", promediosFinales[4])},
            {"promedio", String.format("$%,.2f", promedioGeneral)},
            {"desviación", String.format("$%,.2f", desviacion)}
        };

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) { // Modelo de la tabla
            @Override
            public boolean isCellEditable(int row, int column) { // No permite editar
                return false;
            }
        };

        JTable tabla = new JTable(modelo); // Crea la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Fuente de la tabla
        tabla.setRowHeight(25); // Altura de las filas
        tabla.setGridColor(Color.LIGHT_GRAY); // Color de la cuadrícula
        tabla.setShowGrid(true); // Muestra la cuadrícula
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajuste automático de columnas

        // Configurar header con fondo amarillo
        tabla.getTableHeader().setBackground(new Color(255, 255, 0)); // Fondo del header
        tabla.getTableHeader().setForeground(Color.BLACK); // Color del texto del header
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del header

        // Renderizador personalizado
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() { // Renderizador
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) { // Método de renderizado
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fondo amarillo claro para promedio y desviación
                if (row == 5 || row == 6) { // Si es promedio o desviación
                    comp.setBackground(new Color(255, 255, 200)); // Fondo amarillo claro
                } else {
                    comp.setBackground(Color.WHITE); // Fondo blanco
                }

                // Alineación
                if (column == 1) { // Si es la columna de valores
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT); // Alinea a la izquierda
                }

                return comp; // Devuelve el componente
            }
        });

        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setPreferredSize(new Dimension(250, 190)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.add(scroll, BorderLayout.CENTER); // Agrega la tabla
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Crea la tabla con intervalos de confianza
     */
    private JPanel crearTablaIntervalos(double intervaloInferior, double intervaloSuperior) { // Método para crear la tabla de intervalos
        String[] columnas = {"intervalos de confianza", ""}; // Nombres de columnas
        Object[][] datos = { // Datos de la tabla
            {"inferior", String.format("$%,.2f", intervaloInferior)},
            {"superior", String.format("$%,.2f", intervaloSuperior)}
        };

        DefaultTableModel modelo = new DefaultTableModel(datos, columnas) { // Modelo de la tabla
            @Override
            public boolean isCellEditable(int row, int column) { // No permite editar
                return false;
            }
        };

        JTable tabla = new JTable(modelo); // Crea la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Fuente de la tabla
        tabla.setRowHeight(25); // Altura de las filas
        tabla.setGridColor(Color.LIGHT_GRAY); // Color de la cuadrícula
        tabla.setShowGrid(true); // Muestra la cuadrícula
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajuste automático de columnas

        // Configurar header
        tabla.getTableHeader().setBackground(Color.WHITE); // Fondo del header
        tabla.getTableHeader().setForeground(Color.BLACK); // Color del texto del header
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del header

        // Renderizador para alineación
        tabla.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() { // Renderizador
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) { // Método de renderizado
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                comp.setBackground(Color.WHITE); // Fondo blanco

                if (column == 1) { // Si es la columna de valores
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                } else {
                    setHorizontalAlignment(SwingConstants.LEFT); // Alinea a la izquierda
                }

                return comp; // Devuelve el componente
            }
        });

        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setPreferredSize(new Dimension(300, 90)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.add(scroll, BorderLayout.CENTER); // Agrega la tabla
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Crea el panel que contiene la gráfica de líneas con las 5 réplicas
     */
    private JPanel crearPanelGraficaReplicas(double[][] costosReplicas) { // Método para crear el panel de gráfica
        JFreeChart chartReplicas = GeneradorGraficas.crearGraficaLineasReplicas(costosReplicas, tamanoRecomendado); // Crea la gráfica

        ChartPanel chartPanel = new ChartPanel(chartReplicas); // Panel de la gráfica
        chartPanel.setPreferredSize(new Dimension(1200, 400)); // Tamaño preferido
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Borde vacío
        chartPanel.setBackground(Color.WHITE); // Fondo blanco

        JButton botonVerGrafica = new JButton("Ver Gráfica"); // Botón para ver la gráfica
        botonVerGrafica.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        botonVerGrafica.setBackground(Constantes.COLOR_PRIMARIO); // Fondo del botón
        botonVerGrafica.setForeground(Color.WHITE); // Color del texto
        botonVerGrafica.setFocusPainted(false); // Sin foco pintado
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
        panelContenedor.add(panelBoton, BorderLayout.SOUTH); // Agrega el botón

        botonVerGrafica.addActionListener(e -> GeneradorGraficas.mostrarGraficaEnGrande(chartReplicas)); // Acción del botón

        return panelContenedor; // Devuelve el panel
    }

    /**
     * Crea el panel con estadísticas usando datos ya generados
     */
    private JPanel crearPanelEstadisticasReplicasConDatos(double[][] costosReplicas) { // Método para crear el panel de estadísticas
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 15)); // Panel con grid de 5 columnas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder(null, "Estadísticas Individuales por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título

        for (int i = 0; i < 5; i++) { // Para cada réplica
            ModelosDeDatos.EstadisticasSimulacion stats = motorSimulacion.calcularEstadisticas(costosReplicas[i]); // Calcula estadísticas
            JPanel panelReplica = crearPanelReplicaIndividual(i + 1, stats); // Crea el panel individual
            panel.add(panelReplica); // Agrega el panel
        }

        return panel; // Devuelve el panel
    }

    /**
     * Crea la tabla usando datos ya generados
     */
    private JPanel crearTablaReplicasConDatos(double[][] costosReplicas) { // Método para crear la tabla de réplicas
        String[] columnasReplicas = { // Nombres de columnas
            "Día",
            "Costo promedio ($) Replica 1", "Costo promedio ($) Replica 2",
            "Costo promedio ($) Replica 3", "Costo promedio ($) Replica 4",
            "Costo promedio ($) Replica 5"
        };

        DefaultTableModel modelReplicas = new DefaultTableModel(columnasReplicas, 0); // Modelo de la tabla
        JTable tablaReplicas = new JTable(modelReplicas); // Crea la tabla

        ConfiguradorTabla.configurarTablaReplicas(tablaReplicas, columnasReplicas); // Configura la tabla

        llenarTablaConPromediosAcumulados(modelReplicas, costosReplicas); // Llena la tabla

        JScrollPane scrollReplicas = new JScrollPane(tablaReplicas); // Scroll para la tabla
        scrollReplicas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
        scrollReplicas.setPreferredSize(new Dimension(1200, 300)); // Tamaño preferido

        JPanel panel = new JPanel(new BorderLayout()); // Panel contenedor
        panel.setBorder(BorderFactory.createTitledBorder(null, "Tabla de Promedios Acumulados",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde con título
        panel.add(scrollReplicas, BorderLayout.CENTER); // Agrega la tabla
        panel.setBackground(Color.WHITE); // Fondo blanco

        return panel; // Devuelve el panel
    }

    /**
     * Llena la tabla con promedios acumulados de cada réplica
     */
    private void llenarTablaConPromediosAcumulados(DefaultTableModel modelReplicas, double[][] costosReplicas) { // Método para llenar la tabla
        for (int dia = 0; dia < tamanoRecomendado; dia++) { // Para cada día
            Object[] fila = new Object[6]; // Fila de la tabla
            fila[0] = dia + 1; // Día actual

            for (int replica = 0; replica < 5; replica++) { // Para cada réplica
                double sumaAcumulada = 0; // Suma acumulada

                for (int i = 0; i <= dia; i++) { // Para cada día hasta el actual
                    sumaAcumulada += costosReplicas[replica][i]; // Suma el costo
                }

                double promedioAcumulado = sumaAcumulada / (dia + 1); // Calcula el promedio acumulado
                fila[replica + 1] = promedioAcumulado; // Asigna el valor
            }

            modelReplicas.addRow(fila); // Agrega la fila
        }
    }

    /**
     * Crea el panel individual con estadísticas de una réplica específica
     */
    private JPanel crearPanelReplicaIndividual(int numeroReplica, ModelosDeDatos.EstadisticasSimulacion stats) { // Método para crear el panel individual
        JPanel panelReplica = new JPanel(); // Crea el panel
        panelReplica.setLayout(new BoxLayout(panelReplica, BoxLayout.Y_AXIS)); // Layout vertical

        panelReplica.setBorder(BorderFactory.createTitledBorder(null, "Replica " + numeroReplica,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_REPLICA, Constantes.COLOR_PRIMARIO)); // Borde con título
        panelReplica.setBackground(Constantes.COLOR_PANEL_REPLICA); // Fondo personalizado

        JLabel[] labels = crearEtiquetasEstadisticas(stats); // Crea las etiquetas

        for (int j = 0; j < labels.length; j++) { // Para cada etiqueta
            labels[j].setAlignmentX(Component.CENTER_ALIGNMENT); // Centra la etiqueta
            panelReplica.add(labels[j]); // Agrega la etiqueta
            if (j < labels.length - 1) { // Si no es la última
                panelReplica.add(Box.createVerticalStrut(3)); // Espacio vertical
            }
        }

        return panelReplica; // Devuelve el panel
    }

    /**
     * Crea las etiquetas con estadísticas formateadas para una réplica
     */
    private JLabel[] crearEtiquetasEstadisticas(ModelosDeDatos.EstadisticasSimulacion stats) { // Método para crear etiquetas
        JLabel[] labels = { // Arreglo de etiquetas
            new JLabel("Promedio", SwingConstants.CENTER), // Etiqueta promedio
            new JLabel(String.format("$%,.2f", stats.promedio), SwingConstants.CENTER), // Valor promedio
            new JLabel("Desviación", SwingConstants.CENTER), // Etiqueta desviación
            new JLabel(String.format("$%,.2f", stats.desviacion), SwingConstants.CENTER), // Valor desviación
            new JLabel("Min", SwingConstants.CENTER), // Etiqueta mínimo
            new JLabel(String.format("$%,.2f", stats.minimo), SwingConstants.CENTER), // Valor mínimo
            new JLabel("Max", SwingConstants.CENTER), // Etiqueta máximo
            new JLabel(String.format("$%,.2f", stats.maximo), SwingConstants.CENTER) // Valor máximo
        };

        for (int j = 0; j < labels.length; j++) { // Para cada etiqueta
            if (j % 2 == 1) { // Si es valor
                labels[j].setFont(Constantes.FUENTE_VALOR); // Fuente especial
            }
        }

        return labels; // Devuelve las etiquetas
    }
}