package actividad_8.OPTQuest1; // Declaración del paquete

import com.formdev.flatlaf.FlatLightLaf; // Tema visual moderno para la interfaz
import org.jfree.chart.ChartFactory; // Fábrica para crear gráficos
import org.jfree.chart.ChartPanel; // Panel que contiene el gráfico
import org.jfree.chart.JFreeChart; // Clase principal del gráfico
import org.jfree.chart.axis.NumberAxis; // Eje numérico para gráficos
import org.jfree.chart.plot.PlotOrientation; // Orientación del gráfico
import org.jfree.chart.plot.ValueMarker; // Marcador de valor en el gráfico
import org.jfree.chart.plot.XYPlot; // Plot para gráficos XY
import org.jfree.chart.renderer.xy.XYBarRenderer; // Renderizador de barras XY
import org.jfree.data.statistics.HistogramDataset; // Dataset para histogramas

import javax.swing.*; // Componentes de interfaz gráfica Swing
import javax.swing.table.DefaultTableCellRenderer; // Renderizador de celdas de tabla
import javax.swing.table.DefaultTableModel; // Modelo de datos para tablas
import java.awt.*; // Componentes gráficos y layouts
import java.text.DecimalFormat; // Formateo de números decimales
import java.util.*; // Utilidades generales de Java
import java.util.List; // Interfaz List

public class OptimizadorPreciosHotelAleatorio extends JFrame { // Clase principal que extiende JFrame

    private double[] preciosInicial = new double[3]; // Array de precios iniciales (generados aleatoriamente)
    private double[] demandaPromedio = new double[3]; // Array de demandas promedio (generadas aleatoriamente)

    private double[][] elasticidadLimites = new double[3][2]; // Matriz de límites de elasticidad (generados aleatoriamente)
    private double[][] precioLimites = new double[3][2]; // Matriz de límites de precios (generados aleatoriamente)

    private int capacidadMaxima; // Capacidad máxima del hotel (generada aleatoriamente)
    private double percentilObjetivo = 0.80; // Percentil 80% para restricción
    private int numSimulaciones = 1000; // Número de iteraciones de optimización
    private int numPruebasMC = 5000; // Pruebas Monte Carlo por simulación

    private JTable tablaHotel; // Tabla para mostrar datos del hotel
    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    private JLabel lblTotalDemanda; // Etiqueta para mostrar demanda total
    private JLabel lblTotalGanancia; // Etiqueta para mostrar ganancia total
    private JLabel lblCapacidad; // Etiqueta para mostrar capacidad
    private JProgressBar progressBar; // Barra de progreso para simulaciones
    private JProgressBar progressBarPruebas; // Barra de progreso para pruebas MC
    private JLabel lblSimulacionesActual; // Etiqueta de simulaciones actuales
    private JLabel lblPruebasActual; // Etiqueta de pruebas actuales
    private JButton btnOptimizar; // Botón para ejecutar optimización
    private JButton btnGraficas; // Botón para ver gráficas
    private JButton btnGenerar; // Botón para generar nuevos valores aleatorios

    private double mejorGananciaTotal = Double.NEGATIVE_INFINITY; // Mejor ganancia encontrada (inicializada en infinito negativo)
    private double mejorDemandaTotal = 0; // Mejor demanda total encontrada
    private double[] mejoresPrecios = new double[3]; // Array con mejores precios encontrados
    private double[] mejoresElasticidades = new double[3]; // Array con mejores elasticidades
    private double[] mejoresProyeccionesDemanda = new double[3]; // Array con mejores proyecciones de demanda
    private double[] mejoresProyeccionesGanancia = new double[3]; // Array con mejores proyecciones de ganancia

    private List<ResultadoSimulacion> historialMejoras; // Lista con historial de mejoras
    private List<Double> gananciasFinales; // Lista de ganancias de la mejor solución (para histograma)
    private List<Double> demandasFinales; // Lista de demandas de la mejor solución (para histograma)

    private Random random; // Generador de números aleatorios

    public OptimizadorPreciosHotelAleatorio() { // Constructor de la clase principal
        super("Problema: precios de cuartos de hotel - Valores Aleatorios"); // Título de la ventana
        random = new Random(); // Inicializar generador aleatorio
        historialMejoras = new ArrayList<>(); // Inicializar lista de historial
        gananciasFinales = new ArrayList<>(); // Inicializar lista de ganancias finales
        demandasFinales = new ArrayList<>(); // Inicializar lista de demandas finales

        generarValoresAleatorios(); // Generar valores aleatorios iniciales
        configurarUI(); // Configurar interfaz de usuario

        setSize(1400, 750); // Establecer tamaño de ventana
        setLocationRelativeTo(null); // Centrar ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
    }

    private void generarValoresAleatorios() { // Método para generar valores aleatorios
        preciosInicial[0] = 70 + random.nextInt(30); // Precio Standard aleatorio: $70-$100
        preciosInicial[1] = 90 + random.nextInt(30); // Precio Gold aleatorio: $90-$120
        preciosInicial[2] = 120 + random.nextInt(40); // Precio Platinum aleatorio: $120-$160

        demandaPromedio[0] = 150 + random.nextInt(200); // Demanda Standard aleatoria: 150-350
        demandaPromedio[1] = 50 + random.nextInt(100); // Demanda Gold aleatoria: 50-150
        demandaPromedio[2] = 20 + random.nextInt(80); // Demanda Platinum aleatoria: 20-100

        elasticidadLimites[0][0] = -5.0 + random.nextDouble() * 0.5; // Elasticidad Standard mínima: -5.0 a -4.5
        elasticidadLimites[0][1] = -2.0 + random.nextDouble() * 0.5; // Elasticidad Standard máxima: -2.0 a -1.5

        elasticidadLimites[1][0] = -2.0 + random.nextDouble() * 0.5; // Elasticidad Gold mínima: -2.0 a -1.5
        elasticidadLimites[1][1] = -1.0 + random.nextDouble() * 0.5; // Elasticidad Gold máxima: -1.0 a -0.5

        elasticidadLimites[2][0] = -3.5 + random.nextDouble() * 0.5; // Elasticidad Platinum mínima: -3.5 a -3.0
        elasticidadLimites[2][1] = -1.5 + random.nextDouble() * 0.5; // Elasticidad Platinum máxima: -1.5 a -1.0

        for (int i = 0; i < 3; i++) { // Iterar sobre los 3 tipos de habitación
            double rango = preciosInicial[i] * 0.20; // Calcular rango de ±20% del precio inicial
            precioLimites[i][0] = Math.max(50, preciosInicial[i] - rango); // Límite inferior (mínimo $50)
            precioLimites[i][1] = preciosInicial[i] + rango; // Límite superior
        }

        double demandaTotal = demandaPromedio[0] + demandaPromedio[1] + demandaPromedio[2]; // Calcular demanda total
        capacidadMaxima = (int)(demandaTotal * (0.8 + random.nextDouble() * 0.4)); // Capacidad: 80%-120% de demanda total
    }

    private void configurarUI() { // Método para configurar interfaz de usuario
        setLayout(new BorderLayout(20, 20)); // Establecer layout BorderLayout con espaciado
        getContentPane().setBackground(Color.WHITE); // Establecer fondo blanco

        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20)); // Panel principal con BorderLayout
        panelPrincipal.setBackground(Color.WHITE); // Fondo blanco
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40)); // Agregar margen vacío

        JLabel lblTitulo = new JLabel("Problema: precios de cuartos de hotel (Valores Aleatorios)"); // Crear etiqueta de título
        lblTitulo.setFont(new Font("Calibri", Font.BOLD, 26)); // Establecer fuente grande y negrita
        lblTitulo.setForeground(new Color(31, 78, 120)); // Color azul oscuro

        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel para título con FlowLayout
        panelTitulo.setBackground(Color.WHITE); // Fondo blanco
        panelTitulo.add(lblTitulo); // Agregar etiqueta de título

        JPanel panelTotales = crearPanelTotales(); // Crear panel de totales
        JPanel panelTabla = crearTablaPrincipal(); // Crear panel con tabla principal
        JPanel panelControl = crearPanelControl(); // Crear panel de controles

        panelPrincipal.add(panelTitulo, BorderLayout.NORTH); // Agregar título al norte
        panelPrincipal.add(panelTabla, BorderLayout.CENTER); // Agregar tabla al centro

        JPanel panelSur = new JPanel(new BorderLayout(10, 10)); // Panel sur con BorderLayout
        panelSur.setBackground(Color.WHITE); // Fondo blanco
        panelSur.add(panelTotales, BorderLayout.NORTH); // Agregar totales al norte del panel sur
        panelSur.add(panelControl, BorderLayout.CENTER); // Agregar controles al centro del panel sur

        panelPrincipal.add(panelSur, BorderLayout.SOUTH); // Agregar panel sur al sur del principal

        add(panelPrincipal); // Agregar panel principal a la ventana
    }

    private JPanel crearTablaPrincipal() { // Método para crear panel con tabla principal
        JPanel panel = new JPanel(new BorderLayout()); // Panel con BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco

        String[] columnas = { // Array con nombres de columnas
            "Tipo de habitación",
            "Precio",
            "Demanda\ndiaria\npromedio",
            "Ganancia",
            "Elasticidad",
            "Nuevo precio",
            "Proyección\nde\ndemanda",
            "Proyección\nde\nganancia"
        };

        modeloTabla = new DefaultTableModel(columnas, 0) { // Crear modelo de tabla
            @Override
            public boolean isCellEditable(int row, int col) { // Sobrescribir método de edición
                return false; // Hacer todas las celdas no editables
            }
        };

        tablaHotel = new JTable(modeloTabla); // Crear tabla con el modelo
        configurarTabla(); // Configurar formato de tabla
        llenarTablaInicial(); // Llenar tabla con datos iniciales

        JScrollPane scrollTabla = new JScrollPane(tablaHotel); // Crear scroll para tabla
        scrollTabla.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Agregar borde gris
        scrollTabla.setBackground(Color.WHITE); // Fondo blanco

        panel.add(scrollTabla, BorderLayout.CENTER); // Agregar scroll al centro

        return panel; // Retornar panel completo
    }

    private void configurarTabla() { // Método para configurar apariencia de tabla
        tablaHotel.setFont(new Font("Calibri", Font.PLAIN, 14)); // Fuente de la tabla
        tablaHotel.setRowHeight(45); // Altura de filas
        tablaHotel.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 13)); // Fuente del encabezado
        tablaHotel.getTableHeader().setBackground(new Color(242, 220, 219)); // Color de fondo encabezado (rosa claro)
        tablaHotel.getTableHeader().setForeground(Color.BLACK); // Color de texto encabezado (negro)
        tablaHotel.getTableHeader().setReorderingAllowed(false); // No permitir reordenar columnas
        tablaHotel.setGridColor(new Color(200, 200, 200)); // Color de líneas de cuadrícula

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Renderizador personalizado
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, // Sobrescribir método
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Obtener componente

                setHorizontalAlignment(SwingConstants.CENTER); // Alinear al centro
                setFont(new Font("Calibri", Font.PLAIN, 14)); // Fuente normal

                if (column == 0) { // Si es primera columna (nombres)
                    setFont(new Font("Calibri", Font.BOLD | Font.ITALIC, 14)); // Fuente negrita e itálica
                    setHorizontalAlignment(SwingConstants.LEFT); // Alinear a la izquierda
                    setBackground(Color.WHITE); // Fondo blanco
                }
                else if (column == 4) { // Si es columna Elasticidad
                    setBackground(new Color(0, 176, 80)); // Fondo verde
                    setForeground(Color.BLACK); // Texto negro
                    setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
                }
                else if (column == 5) { // Si es columna Nuevo precio
                    setBackground(new Color(255, 255, 0)); // Fondo amarillo
                    setForeground(Color.BLACK); // Texto negro
                    setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
                }
                else { // Otras columnas
                    setBackground(Color.WHITE); // Fondo blanco
                    setForeground(Color.BLACK); // Texto negro
                }

                setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(200, 200, 200))); // Agregar borde

                return c; // Retornar componente
            }
        };

        for (int i = 0; i < tablaHotel.getColumnCount(); i++) { // Iterar sobre todas las columnas
            tablaHotel.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplicar renderizador a cada columna
        }

        tablaHotel.getColumnModel().getColumn(0).setPreferredWidth(180); // Establecer ancho preferido columna 0
    }

    private void llenarTablaInicial() { // Método para llenar tabla con datos iniciales
        modeloTabla.setRowCount(0); // Limpiar todas las filas
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00"); // Formato monetario
        DecimalFormat dfInt = new DecimalFormat("0"); // Formato entero

        String[] tipos = {"Standard", "Gold", "Platinum"}; // Tipos de habitaciones

        for (int i = 0; i < 3; i++) { // Iterar sobre los 3 tipos
            double ganancia = preciosInicial[i] * demandaPromedio[i]; // Calcular ganancia inicial
            modeloTabla.addRow(new Object[]{ // Agregar fila con datos
                tipos[i],
                dfMoney.format(preciosInicial[i]),
                dfInt.format(demandaPromedio[i]),
                dfMoney.format(ganancia),
                "-",
                dfMoney.format(preciosInicial[i]),
                dfInt.format(demandaPromedio[i]),
                dfMoney.format(ganancia)
            });
        }

        actualizarTotales(); // Actualizar totales
    }

    private void actualizarTotales() { // Método para actualizar etiquetas de totales
        double totalDemanda = demandaPromedio[0] + demandaPromedio[1] + demandaPromedio[2]; // Sumar demanda total
        double totalGanancia = (preciosInicial[0] * demandaPromedio[0]) + // Calcular ganancia total
                               (preciosInicial[1] * demandaPromedio[1]) +
                               (preciosInicial[2] * demandaPromedio[2]);

        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00"); // Formato monetario
        DecimalFormat dfInt = new DecimalFormat("0"); // Formato entero

        lblTotalDemanda.setText(dfInt.format(totalDemanda)); // Actualizar etiqueta demanda
        lblTotalGanancia.setText(dfMoney.format(totalGanancia)); // Actualizar etiqueta ganancia
        lblCapacidad.setText(String.valueOf(capacidadMaxima)); // Actualizar etiqueta capacidad
    }

    private JPanel crearPanelTotales() { // Método para crear panel de totales
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 10)); // Panel con FlowLayout alineado a la derecha
        panel.setBackground(Color.WHITE); // Fondo blanco

        JPanel panelTotal = new JPanel(new BorderLayout(10, 5)); // Panel para total con BorderLayout
        panelTotal.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblTotal = new JLabel("Total", SwingConstants.CENTER); // Etiqueta "Total"
        lblTotal.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita

        JPanel panelValoresTotal = new JPanel(new GridLayout(1, 2, 10, 0)); // Panel para valores con GridLayout
        panelValoresTotal.setBackground(Color.WHITE); // Fondo blanco

        lblTotalDemanda = new JLabel("400", SwingConstants.CENTER); // Etiqueta demanda total
        lblTotalDemanda.setFont(new Font("Calibri", Font.BOLD, 16)); // Fuente grande y negrita
        lblTotalDemanda.setBackground(new Color(0, 255, 255)); // Fondo cian
        lblTotalDemanda.setOpaque(true); // Hacer opaco
        lblTotalDemanda.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Borde negro
        lblTotalDemanda.setPreferredSize(new Dimension(80, 35)); // Tamaño preferido

        lblTotalGanancia = new JLabel("$38.000,00", SwingConstants.CENTER); // Etiqueta ganancia total
        lblTotalGanancia.setFont(new Font("Calibri", Font.BOLD, 16)); // Fuente grande y negrita
        lblTotalGanancia.setBackground(new Color(0, 255, 255)); // Fondo cian
        lblTotalGanancia.setOpaque(true); // Hacer opaco
        lblTotalGanancia.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Borde negro
        lblTotalGanancia.setPreferredSize(new Dimension(120, 35)); // Tamaño preferido

        panelValoresTotal.add(lblTotalDemanda); // Agregar etiqueta demanda
        panelValoresTotal.add(lblTotalGanancia); // Agregar etiqueta ganancia

        panelTotal.add(lblTotal, BorderLayout.NORTH); // Agregar etiqueta "Total" al norte
        panelTotal.add(panelValoresTotal, BorderLayout.CENTER); // Agregar valores al centro

        JPanel panelCapacity = new JPanel(new BorderLayout(10, 5)); // Panel para capacidad
        panelCapacity.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblCapacityLabel = new JLabel("Capacity", SwingConstants.CENTER); // Etiqueta "Capacity"
        lblCapacityLabel.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita

        lblCapacidad = new JLabel("450", SwingConstants.CENTER); // Etiqueta capacidad
        lblCapacidad.setFont(new Font("Calibri", Font.BOLD, 16)); // Fuente grande y negrita
        lblCapacidad.setForeground(new Color(255, 102, 0)); // Color naranja
        lblCapacidad.setBackground(Color.WHITE); // Fondo blanco
        lblCapacidad.setOpaque(true); // Hacer opaco
        lblCapacidad.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1)); // Borde negro
        lblCapacidad.setPreferredSize(new Dimension(80, 35)); // Tamaño preferido

        panelCapacity.add(lblCapacityLabel, BorderLayout.NORTH); // Agregar etiqueta "Capacity" al norte
        panelCapacity.add(lblCapacidad, BorderLayout.CENTER); // Agregar valor al centro

        panel.add(panelTotal); // Agregar panel total
        panel.add(panelCapacity); // Agregar panel capacidad

        return panel; // Retornar panel completo
    }

    private JPanel crearPanelControl() { // Método para crear panel de controles
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 15)); // Panel con GridLayout de 2 filas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0)); // Agregar margen vacío

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Panel para botones con FlowLayout
        panelBotones.setBackground(Color.WHITE); // Fondo blanco

        btnGenerar = new JButton("🎲 Generar Nuevos Valores"); // Botón para generar nuevos valores
        btnGenerar.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
        btnGenerar.setBackground(new Color(255, 153, 0)); // Color de fondo naranja
        btnGenerar.setForeground(Color.WHITE); // Color de texto blanco
        btnGenerar.setFocusPainted(false); // Quitar borde de foco
        btnGenerar.setPreferredSize(new Dimension(250, 45)); // Establecer tamaño preferido

        btnOptimizar = new JButton("Ejecutar Optimización (OptQuest)"); // Botón para ejecutar optimización
        btnOptimizar.setFont(new Font("Calibri", Font.BOLD, 16)); // Fuente grande y negrita
        btnOptimizar.setBackground(new Color(68, 114, 196)); // Color de fondo azul
        btnOptimizar.setForeground(Color.WHITE); // Color de texto blanco
        btnOptimizar.setFocusPainted(false); // Quitar borde de foco
        btnOptimizar.setPreferredSize(new Dimension(350, 50)); // Establecer tamaño preferido

        btnGraficas = new JButton("Ver Gráficas"); // Botón para ver gráficas
        btnGraficas.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
        btnGraficas.setBackground(new Color(112, 173, 71)); // Color de fondo verde
        btnGraficas.setForeground(Color.WHITE); // Color de texto blanco
        btnGraficas.setFocusPainted(false); // Quitar borde de foco
        btnGraficas.setPreferredSize(new Dimension(200, 40)); // Establecer tamaño preferido
        btnGraficas.setEnabled(false); // Deshabilitar inicialmente

        panelBotones.add(btnGenerar); // Agregar botón generar
        panelBotones.add(btnOptimizar); // Agregar botón optimizar
        panelBotones.add(btnGraficas); // Agregar botón gráficas

        JPanel panelProgress = new JPanel(new BorderLayout(10, 10)); // Panel para barras de progreso
        panelProgress.setBackground(Color.WHITE); // Fondo blanco
        panelProgress.setBorder(BorderFactory.createTitledBorder( // Agregar borde con título
            BorderFactory.createLineBorder(Color.GRAY),
            "Panel de control: OptQuest",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Calibri", Font.BOLD, 12)
        ));

        JPanel panelBarras = new JPanel(new GridLayout(2, 1, 5, 10)); // Panel con GridLayout para las barras
        panelBarras.setBackground(Color.WHITE); // Fondo blanco
        panelBarras.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20)); // Agregar margen vacío

        JPanel panelSim = new JPanel(new BorderLayout(5, 5)); // Panel para barra de simulaciones
        panelSim.setBackground(Color.WHITE); // Fondo blanco

        lblSimulacionesActual = new JLabel("Simulaciones totales: 0 / " + numSimulaciones); // Etiqueta de simulaciones
        lblSimulacionesActual.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente pequeña

        progressBar = new JProgressBar(0, numSimulaciones); // Crear barra de progreso para simulaciones
        progressBar.setStringPainted(false); // No mostrar texto en barra
        progressBar.setPreferredSize(new Dimension(500, 25)); // Establecer tamaño preferido
        progressBar.setForeground(new Color(0, 32, 96)); // Color azul oscuro
        progressBar.setBackground(Color.WHITE); // Fondo blanco

        panelSim.add(lblSimulacionesActual, BorderLayout.WEST); // Agregar etiqueta al oeste
        panelSim.add(progressBar, BorderLayout.CENTER); // Agregar barra al centro
        JLabel lblMaxSim = new JLabel(numSimulaciones + "  "); // Etiqueta con máximo de simulaciones
        lblMaxSim.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente pequeña
        panelSim.add(lblMaxSim, BorderLayout.EAST); // Agregar al este

        JPanel panelPruebas = new JPanel(new BorderLayout(5, 5)); // Panel para barra de pruebas
        panelPruebas.setBackground(Color.WHITE); // Fondo blanco

        lblPruebasActual = new JLabel("Pruebas: 0 / " + numPruebasMC); // Etiqueta de pruebas
        lblPruebasActual.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente pequeña

        progressBarPruebas = new JProgressBar(0, numPruebasMC); // Crear barra de progreso para pruebas
        progressBarPruebas.setStringPainted(false); // No mostrar texto en barra
        progressBarPruebas.setPreferredSize(new Dimension(500, 25)); // Establecer tamaño preferido
        progressBarPruebas.setForeground(new Color(0, 176, 80)); // Color verde
        progressBarPruebas.setBackground(Color.WHITE); // Fondo blanco

        panelPruebas.add(lblPruebasActual, BorderLayout.WEST); // Agregar etiqueta al oeste
        panelPruebas.add(progressBarPruebas, BorderLayout.CENTER); // Agregar barra al centro
        JLabel lblMaxPruebas = new JLabel(numPruebasMC + "  "); // Etiqueta con máximo de pruebas
        lblMaxPruebas.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente pequeña
        panelPruebas.add(lblMaxPruebas, BorderLayout.EAST); // Agregar al este

        panelBarras.add(panelSim); // Agregar panel de simulaciones
        panelBarras.add(panelPruebas); // Agregar panel de pruebas

        panelProgress.add(panelBarras, BorderLayout.CENTER); // Agregar barras al panel de progreso

        panel.add(panelBotones); // Agregar panel de botones
        panel.add(panelProgress); // Agregar panel de progreso

        btnGenerar.addActionListener(e -> { // Agregar listener al botón generar
            generarValoresAleatorios(); // Generar nuevos valores aleatorios
            llenarTablaInicial(); // Actualizar tabla con nuevos valores
            gananciasFinales.clear(); // Limpiar ganancias finales
            demandasFinales.clear(); // Limpiar demandas finales
            btnGraficas.setEnabled(false); // Deshabilitar botón gráficas
            JOptionPane.showMessageDialog(this, // Mostrar diálogo de confirmación
                "Nuevos valores aleatorios generados",
                "Valores Actualizados",
                JOptionPane.INFORMATION_MESSAGE);
        });

        btnOptimizar.addActionListener(e -> { // Agregar listener al botón optimizar
            btnOptimizar.setEnabled(false); // Deshabilitar botón optimizar
            btnGraficas.setEnabled(false); // Deshabilitar botón gráficas
            btnGenerar.setEnabled(false); // Deshabilitar botón generar
            ejecutarOptimizacion(); // Ejecutar optimización
        });

        btnGraficas.addActionListener(e -> mostrarGraficas()); // Agregar listener al botón gráficas

        return panel; // Retornar panel completo
    }

    private void ejecutarOptimizacion() { // Método para ejecutar optimización OptQuest
        progressBar.setValue(0); // Reiniciar barra de simulaciones
        progressBarPruebas.setValue(0); // Reiniciar barra de pruebas
        historialMejoras.clear(); // Limpiar historial de mejoras
        gananciasFinales.clear(); // Limpiar ganancias finales
        demandasFinales.clear(); // Limpiar demandas finales
        mejorGananciaTotal = Double.NEGATIVE_INFINITY; // Reiniciar mejor ganancia

        SwingWorker<Void, ProgressUpdate> worker = new SwingWorker<>() { // Worker para ejecutar en segundo plano
            private long totalPruebasEjecutadas = 0; // Contador de pruebas ejecutadas

            @Override
            protected Void doInBackground() { // Método ejecutado en hilo separado
                Random random = new Random(); // Crear generador aleatorio

                for (int iter = 0; iter < numSimulaciones; iter++) { // Ejecutar simulaciones
                    double precioStandard = generarPrecioAleatorio(precioLimites[0], random); // Generar precio aleatorio Standard
                    double precioGold = generarPrecioAleatorio(precioLimites[1], random); // Generar precio aleatorio Gold
                    double precioPlatinum = generarPrecioAleatorio(precioLimites[2], random); // Generar precio aleatorio Platinum

                    double sumaGanancias = 0; // Acumulador de ganancias
                    double sumaDemandas = 0; // Acumulador de demandas
                    int simulacionesValidas = 0; // Contador de simulaciones válidas

                    ResultadoOptimizacion ultimoResultado = null; // Variable para último resultado
                    List<Double> gananciasTemp = new ArrayList<>(); // Lista temporal de ganancias
                    List<Double> demandasTemp = new ArrayList<>(); // Lista temporal de demandas

                    for (int mc = 0; mc < numPruebasMC; mc++) { // EJECUTAR PRUEBAS Monte Carlo
                        ResultadoOptimizacion resultado = simularConPrecios( // Simular con precios
                            precioStandard, precioGold, precioPlatinum, random);

                        totalPruebasEjecutadas++; // Incrementar contador de pruebas

                        gananciasTemp.add(resultado.gananciaTotal); // Guardar ganancia
                        demandasTemp.add(resultado.demandaTotal); // Guardar demanda

                        if (resultado.demandaTotal <= capacidadMaxima) { // Si cumple restricción de capacidad
                            sumaGanancias += resultado.gananciaTotal; // Sumar ganancia
                            sumaDemandas += resultado.demandaTotal; // Sumar demanda
                            simulacionesValidas++; // Incrementar contador válidas
                            ultimoResultado = resultado; // Guardar último resultado
                        }

                        if (mc % 250 == 0 || mc == numPruebasMC - 1) { // Actualizar progreso cada 250 pruebas
                            publish(new ProgressUpdate(iter + 1, mc + 1)); // Publicar actualización de progreso
                        }
                    }

                    if (simulacionesValidas > 0) { // Si hay simulaciones válidas
                        double gananciaMedia = sumaGanancias / simulacionesValidas; // Calcular ganancia media
                        double demandaMedia = sumaDemandas / simulacionesValidas; // Calcular demanda media

                        if (gananciaMedia > mejorGananciaTotal) { // Si esta combinación es mejor
                            mejorGananciaTotal = gananciaMedia; // Actualizar mejor ganancia
                            mejorDemandaTotal = demandaMedia; // Actualizar mejor demanda
                            mejoresPrecios[0] = precioStandard; // Guardar mejor precio Standard
                            mejoresPrecios[1] = precioGold; // Guardar mejor precio Gold
                            mejoresPrecios[2] = precioPlatinum; // Guardar mejor precio Platinum

                            gananciasFinales.clear(); // Limpiar ganancias finales
                            demandasFinales.clear(); // Limpiar demandas finales
                            gananciasFinales.addAll(gananciasTemp); // Guardar resultados de esta mejor simulación
                            demandasFinales.addAll(demandasTemp); // Guardar demandas de esta mejor simulación

                            if (ultimoResultado != null) { // Si hay resultado válido
                                mejoresElasticidades = ultimoResultado.elasticidades.clone(); // Guardar elasticidades
                                mejoresProyeccionesDemanda = ultimoResultado.proyeccionesDemanda.clone(); // Guardar proyecciones demanda
                                mejoresProyeccionesGanancia = ultimoResultado.proyeccionesGanancia.clone(); // Guardar proyecciones ganancia
                            }

                            historialMejoras.add(new ResultadoSimulacion( // Agregar al historial
                                iter + 1, gananciaMedia, demandaMedia,
                                precioStandard, precioGold, precioPlatinum
                            ));
                        }
                    }
                }

                System.out.println("Total pruebas ejecutadas: " + totalPruebasEjecutadas); // Log de pruebas totales

                return null; // Retornar null
            }

            @Override
            protected void process(List<ProgressUpdate> chunks) { // Método para procesar actualizaciones de progreso
                ProgressUpdate ultimo = chunks.get(chunks.size() - 1); // Obtener última actualización

                progressBar.setValue(ultimo.simulacion); // Actualizar barra de simulaciones
                lblSimulacionesActual.setText(String.format("Simulaciones totales: %d / %d", // Actualizar etiqueta
                    ultimo.simulacion, numSimulaciones));

                progressBarPruebas.setValue(ultimo.prueba); // Actualizar barra de pruebas
                lblPruebasActual.setText(String.format("Pruebas: %d / %d", // Actualizar etiqueta
                    ultimo.prueba, numPruebasMC));
            }

            @Override
            protected void done() { // Método ejecutado al terminar (en hilo de UI)
                actualizarTablaConMejoresResultados(); // Actualizar tabla con mejores resultados
                btnOptimizar.setEnabled(true); // Habilitar botón optimizar
                btnGraficas.setEnabled(true); // Habilitar botón gráficas
                btnGenerar.setEnabled(true); // Habilitar botón generar
                mostrarVentanaResultados(); // Mostrar ventana de resultados
            }
        };

        worker.execute(); // Iniciar ejecución del worker
    }

    private double generarPrecioAleatorio(double[] limites, Random random) { // Método para generar precio aleatorio
        int minimo = (int) limites[0]; // Obtener límite mínimo
        int maximo = (int) limites[1]; // Obtener límite máximo
        return minimo + random.nextInt(maximo - minimo + 1); // Retornar precio aleatorio en el rango
    }

    private ResultadoOptimizacion simularConPrecios( // Método para simular con precios dados
            double precioStd, double precioGold, double precioPlt, Random random) {

        double[] elasticidades = new double[3]; // Array para elasticidades
        double[] proyeccionesDemanda = new double[3]; // Array para proyecciones de demanda
        double[] proyeccionesGanancia = new double[3]; // Array para proyecciones de ganancia

        for (int i = 0; i < 3; i++) { // Generar elasticidades aleatorias para cada tipo
            elasticidades[i] = elasticidadLimites[i][0] + // Elasticidad aleatoria en rango
                random.nextDouble() * (elasticidadLimites[i][1] - elasticidadLimites[i][0]);
        }

        double[] nuevosPrecios = {precioStd, precioGold, precioPlt}; // Array con nuevos precios

        for (int i = 0; i < 3; i++) { // Calcular proyecciones para cada tipo
            proyeccionesDemanda[i] = demandaPromedio[i] + // Fórmula de elasticidad
                elasticidades[i] * (nuevosPrecios[i] - preciosInicial[i]) *
                (demandaPromedio[i] / preciosInicial[i]);

            proyeccionesDemanda[i] = Math.max(0, proyeccionesDemanda[i]); // Asegurar que demanda no sea negativa
            proyeccionesGanancia[i] = nuevosPrecios[i] * proyeccionesDemanda[i]; // Calcular ganancia (precio * demanda)
        }

        double demandaTotal = proyeccionesDemanda[0] + proyeccionesDemanda[1] + proyeccionesDemanda[2]; // Sumar demanda total
        double gananciaTotal = proyeccionesGanancia[0] + proyeccionesGanancia[1] + proyeccionesGanancia[2]; // Sumar ganancia total

        return new ResultadoOptimizacion( // Retornar resultado
            nuevosPrecios, elasticidades, proyeccionesDemanda,
            proyeccionesGanancia, demandaTotal, gananciaTotal
        );
    }

    private void actualizarTablaConMejoresResultados() { // Método para actualizar tabla con mejores resultados
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00"); // Formato monetario
        DecimalFormat dfDemanda = new DecimalFormat("0"); // Formato entero
        DecimalFormat dfElast = new DecimalFormat("0.00"); // Formato con 2 decimales

        for (int i = 0; i < 3; i++) { // Iterar sobre los 3 tipos de habitación
            modeloTabla.setValueAt(dfElast.format(mejoresElasticidades[i]), i, 4); // Actualizar elasticidad
            modeloTabla.setValueAt(dfMoney.format(mejoresPrecios[i]), i, 5); // Actualizar nuevo precio
            modeloTabla.setValueAt(dfDemanda.format(mejoresProyeccionesDemanda[i]), i, 6); // Actualizar proyección demanda
            modeloTabla.setValueAt(dfMoney.format(mejoresProyeccionesGanancia[i]), i, 7); // Actualizar proyección ganancia
        }

        lblTotalDemanda.setText(dfDemanda.format(mejorDemandaTotal)); // Actualizar demanda total
        lblTotalGanancia.setText(dfMoney.format(mejorGananciaTotal)); // Actualizar ganancia total
    }

        private void mostrarVentanaResultados() { // Método para mostrar ventana de resultados
        JDialog dialogo = new JDialog(this, "Resultados de OptQuest", false); // Crear diálogo no modal
        dialogo.setLayout(new BorderLayout(10, 10)); // Establecer layout
        dialogo.getContentPane().setBackground(Color.WHITE); // Fondo blanco

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Agregar margen
        panelPrincipal.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblTitulo = new JLabel(numSimulaciones + " simulaciones", SwingConstants.LEFT); // Etiqueta de título
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita

        JPanel panelTablas = new JPanel(new GridLayout(4, 1, 5, 5)); // Panel con GridLayout para tablas
        panelTablas.setBackground(Color.WHITE); // Fondo blanco

        DecimalFormat df = new DecimalFormat("$#,##0.00"); // Formato monetario

        panelTablas.add(crearTablaResultado("Objetivos", "Valor", // Tabla de objetivos
            new String[]{"Maximizar el/la Media de Total Revenue"},
            new String[]{df.format(mejorGananciaTotal)}));

        panelTablas.add(crearTablaResultado("Requisitos", "Valor", // Tabla de requisitos
            new String[]{"El/la Percentil 80% de Total room demand debe ser menor que"},
            new String[]{String.valueOf(capacidadMaxima)}));

        panelTablas.add(crearTablaResultado("Restricciones", "Lado izquierdo    Lado derecho", // Tabla de restricciones (vacía)
            new String[]{}, new String[]{}));

        String[][] variables = { // Datos de variables de decisión
            {"Standard price", df.format(mejoresPrecios[0])},
            {"Gold price", df.format(mejoresPrecios[1])},
            {"Platinum price", df.format(mejoresPrecios[2])}
        };

        JPanel panelVars = crearTablaVariables(variables); // Crear tabla de variables
        panelTablas.add(panelVars); // Agregar tabla

        panelPrincipal.add(lblTitulo, BorderLayout.NORTH); // Agregar título al norte
        panelPrincipal.add(panelTablas, BorderLayout.CENTER); // Agregar tablas al centro

        dialogo.add(panelPrincipal); // Agregar panel principal al diálogo
        dialogo.setSize(650, 450); // Establecer tamaño
        dialogo.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        dialogo.setVisible(true); // Hacer visible
    }

    private JPanel crearTablaResultado(String titulo, String columna, String[] filas, String[] valores) { // Método para crear tabla de resultado
        JPanel panel = new JPanel(new BorderLayout()); // Panel con BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Borde gris

        JLabel lblTitulo = new JLabel(" " + titulo); // Etiqueta de título
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        lblTitulo.setBackground(new Color(220, 220, 220)); // Fondo gris claro
        lblTitulo.setOpaque(true); // Hacer opaco
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5)); // Agregar margen

        JPanel panelContenido = new JPanel(new BorderLayout()); // Panel para contenido
        panelContenido.setBackground(Color.WHITE); // Fondo blanco
        panelContenido.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5)); // Agregar margen

        if (filas.length > 0) { // Si hay filas de datos
            JPanel panelDatos = new JPanel(new GridLayout(filas.length + 1, 2, 5, 3)); // Panel con GridLayout
            panelDatos.setBackground(Color.WHITE); // Fondo blanco

            JLabel lblCol1 = new JLabel(""); // Etiqueta vacía primera columna
            JLabel lblCol2 = new JLabel(columna); // Etiqueta segunda columna
            lblCol2.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Fuente negrita
            panelDatos.add(lblCol1); // Agregar columna 1
            panelDatos.add(lblCol2); // Agregar columna 2

            for (int i = 0; i < filas.length; i++) { // Iterar sobre filas
                JLabel lblFila = new JLabel(filas[i]); // Etiqueta de fila
                lblFila.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente normal

                JLabel lblValor = new JLabel(valores[i], SwingConstants.RIGHT); // Etiqueta de valor alineada a la derecha
                lblValor.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente normal

                panelDatos.add(lblFila); // Agregar fila
                panelDatos.add(lblValor); // Agregar valor
            }

            panelContenido.add(panelDatos); // Agregar datos al contenido
        } else { // Si no hay filas
            JLabel lblVacio = new JLabel(columna); // Etiqueta con nombre de columna
            lblVacio.setFont(new Font("Segoe UI", Font.BOLD, 10)); // Fuente negrita
            panelContenido.add(lblVacio); // Agregar etiqueta vacía
        }

        panel.add(lblTitulo, BorderLayout.NORTH); // Agregar título al norte
        panel.add(panelContenido, BorderLayout.CENTER); // Agregar contenido al centro

        return panel; // Retornar panel completo
    }

    private JPanel crearTablaVariables(String[][] datos) { // Método para crear tabla de variables
        JPanel panel = new JPanel(new BorderLayout()); // Panel con BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY)); // Borde gris

        JLabel lblTitulo = new JLabel(" Variables de decisión", SwingConstants.LEFT); // Etiqueta de título
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        lblTitulo.setBackground(new Color(220, 220, 220)); // Fondo gris claro
        lblTitulo.setOpaque(true); // Hacer opaco
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(3, 5, 3, 5)); // Agregar margen

        String[] columnas = {"", "Valor"}; // Nombres de columnas
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0); // Crear modelo de tabla

        for (String[] fila : datos) { // Iterar sobre datos
            modelo.addRow(fila); // Agregar fila
        }

        JTable tabla = new JTable(modelo); // Crear tabla con modelo
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente normal
        tabla.setRowHeight(20); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10)); // Fuente del encabezado

        panel.add(lblTitulo, BorderLayout.NORTH); // Agregar título al norte
        panel.add(new JScrollPane(tabla), BorderLayout.CENTER); // Agregar tabla con scroll al centro

        return panel; // Retornar panel completo
    }

    private void mostrarGraficas() { // Método para mostrar gráficas
        mostrarHistogramaRevenue(); // Mostrar histograma de revenue
        mostrarHistogramaDemanda(); // Mostrar histograma de demanda
    }

    private void mostrarHistogramaRevenue() { // Método para mostrar histograma de Revenue
        double[] datos = gananciasFinales.stream().mapToDouble(Double::doubleValue).toArray(); // Convertir lista a array

        if (datos.length == 0) { // Si no hay datos
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar", "Aviso", JOptionPane.WARNING_MESSAGE); // Mostrar mensaje de aviso
            return; // Salir
        }

        HistogramDataset dataset = new HistogramDataset(); // Crear dataset de histograma
        dataset.addSeries("Total Revenue", datos, 50); // Agregar serie con 50 bins

        JFreeChart chart = ChartFactory.createHistogram( // Crear gráfico de histograma
            "Total Revenue", // Título
            "Dollars", // Etiqueta eje X
            "Frecuencia", // Etiqueta eje Y
            dataset, // Dataset
            PlotOrientation.VERTICAL, // Orientación vertical
            false, // Sin leyenda
            true, // Con tooltips
            false // Sin URLs
        );

        XYPlot plot = chart.getXYPlot(); // Obtener plot del gráfico
        plot.setBackgroundPaint(Color.WHITE); // Color de fondo blanco
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Color de líneas de cuadrícula eje X
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Color de líneas de cuadrícula eje Y

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtener renderizador de barras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Color de barras (azul)
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter()); // Pintor estándar
        renderer.setShadowVisible(false); // Desactivar sombras

        double media = Arrays.stream(datos).average().orElse(0); // Calcular media
        ValueMarker marker = new ValueMarker(media); // Crear marcador en la media
        marker.setPaint(Color.BLACK); // Color negro
        marker.setLabel(String.format("Media = $%.2f", media)); // Etiqueta con valor de media
        marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_RIGHT); // Anclar etiqueta arriba derecha
        marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.BOTTOM_RIGHT); // Anclar texto abajo derecha
        plot.addDomainMarker(marker); // Agregar marcador al plot

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis(); // Obtener eje de dominio
        domainAxis.setNumberFormatOverride(new DecimalFormat("$#,##0")); // Establecer formato monetario

        chart.setBackgroundPaint(Color.WHITE); // Color de fondo del gráfico (blanco)

        mostrarVentanaGrafico(chart, "Previsión: Total Revenue - " + datos.length + " pruebas", 900, 600); // Mostrar ventana con gráfico
    }

    private void mostrarHistogramaDemanda() { // Método para mostrar histograma de Demanda
        double[] datos = demandasFinales.stream().mapToDouble(Double::doubleValue).toArray(); // Convertir lista a array

        if (datos.length == 0) { // Si no hay datos
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar", "Aviso", JOptionPane.WARNING_MESSAGE); // Mostrar mensaje de aviso
            return; // Salir
        }

        HistogramDataset dataset = new HistogramDataset(); // Crear dataset de histograma
        dataset.addSeries("Total room demand", datos, 50); // Agregar serie con 50 bins

        JFreeChart chart = ChartFactory.createHistogram( // Crear gráfico de histograma
            "Total room demand", // Título
            "Habitaciones", // Etiqueta eje X
            "Frecuencia", // Etiqueta eje Y
            dataset, // Dataset
            PlotOrientation.VERTICAL, // Orientación vertical
            false, // Sin leyenda
            true, // Con tooltips
            false // Sin URLs
        );

        XYPlot plot = chart.getXYPlot(); // Obtener plot del gráfico
        plot.setBackgroundPaint(Color.WHITE); // Color de fondo blanco
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Color de líneas de cuadrícula eje X
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Color de líneas de cuadrícula eje Y

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtener renderizador de barras
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter()); // Pintor estándar
        renderer.setShadowVisible(false); // Desactivar sombras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Color de barras (azul)

        double[] datosOrdenados = datos.clone(); // Clonar array de datos
        Arrays.sort(datosOrdenados); // Ordenar array
        int indicePercentil = (int) Math.ceil(percentilObjetivo * datosOrdenados.length) - 1; // Calcular índice del percentil 80
        double percentil80 = datosOrdenados[indicePercentil]; // Obtener valor del percentil 80

        ValueMarker marker = new ValueMarker(percentil80); // Crear marcador en el percentil 80
        marker.setPaint(Color.BLACK); // Color negro
        marker.setLabel(String.format("80%% = %.2f", percentil80)); // Etiqueta con valor del percentil
        marker.setLabelAnchor(org.jfree.chart.ui.RectangleAnchor.TOP_RIGHT); // Anclar etiqueta arriba derecha
        marker.setLabelTextAnchor(org.jfree.chart.ui.TextAnchor.TOP_LEFT); // Anclar texto arriba izquierda
        plot.addDomainMarker(marker); // Agregar marcador al plot

        org.jfree.chart.plot.IntervalMarker intervalo = new org.jfree.chart.plot.IntervalMarker( // Crear intervalo marcado
            percentil80, // Desde percentil 80
            datosOrdenados[datosOrdenados.length - 1] + 10 // Hasta el final más 10
        );
        intervalo.setPaint(new Color(255, 150, 150, 120)); // Color rojo claro transparente
        plot.addDomainMarker(intervalo); // Agregar intervalo al plot

        chart.setBackgroundPaint(Color.WHITE); // Color de fondo del gráfico (blanco)

        mostrarVentanaGrafico(chart, "Previsión: Total room demand - " + datos.length + " pruebas", 900, 600); // Mostrar ventana con gráfico
    }

    private void mostrarVentanaGrafico(JFreeChart chart, String titulo, int ancho, int alto) { // Método para mostrar ventana con gráfico
        JFrame frame = new JFrame(titulo); // Crear ventana con título
        ChartPanel chartPanel = new ChartPanel(chart); // Crear panel del gráfico
        chartPanel.setPreferredSize(new Dimension(ancho, alto)); // Establecer tamaño preferido

        frame.setContentPane(chartPanel); // Establecer panel como contenido
        frame.pack(); // Ajustar tamaño al contenido
        frame.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        frame.setVisible(true); // Hacer visible
    }

    private static class ResultadoOptimizacion { // Clase interna para resultado de optimización
        double[] precios; // Array de precios
        double[] elasticidades; // Array de elasticidades
        double[] proyeccionesDemanda; // Array de proyecciones de demanda
        double[] proyeccionesGanancia; // Array de proyecciones de ganancia
        double demandaTotal; // Demanda total
        double gananciaTotal; // Ganancia total

        ResultadoOptimizacion(double[] precios, double[] elasticidades, // Constructor
                             double[] proyeccionesDemanda, double[] proyeccionesGanancia,
                             double demandaTotal, double gananciaTotal) {
            this.precios = precios; // Asignar precios
            this.elasticidades = elasticidades; // Asignar elasticidades
            this.proyeccionesDemanda = proyeccionesDemanda; // Asignar proyecciones demanda
            this.proyeccionesGanancia = proyeccionesGanancia; // Asignar proyecciones ganancia
            this.demandaTotal = demandaTotal; // Asignar demanda total
            this.gananciaTotal = gananciaTotal; // Asignar ganancia total
        }
    }

    private static class ResultadoSimulacion { // Clase interna para resultado de simulación
        int iteracion; // Número de iteración
        double ganancia; // Ganancia obtenida
        double demanda; // Demanda obtenida
        double precioStandard; // Precio Standard
        double precioGold; // Precio Gold
        double precioPlatinum; // Precio Platinum

        ResultadoSimulacion(int iter, double gan, double dem, // Constructor
                          double pStd, double pGold, double pPlt) {
            this.iteracion = iter; // Asignar iteración
            this.ganancia = gan; // Asignar ganancia
            this.demanda = dem; // Asignar demanda
            this.precioStandard = pStd; // Asignar precio Standard
            this.precioGold = pGold; // Asignar precio Gold
            this.precioPlatinum = pPlt; // Asignar precio Platinum
        }
    }

    private static class ProgressUpdate { // Clase auxiliar para actualizaciones de progreso
        int simulacion; // Número de simulación actual
        int prueba; // Número de prueba actual

        ProgressUpdate(int sim, int pr) { // Constructor
            this.simulacion = sim; // Asignar simulación
            this.prueba = pr; // Asignar prueba
        }
    }

    public static void main(String[] args) { // Método main - punto de entrada
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establecer Look and Feel FlatLaf
        } catch (Exception e) { // Capturar excepciones
            e.printStackTrace(); // Imprimir error
        }

        SwingUtilities.invokeLater(() -> { // Ejecutar en hilo de eventos de Swing
            OptimizadorPreciosHotelAleatorio optimizador = new OptimizadorPreciosHotelAleatorio(); // Crear instancia del optimizador
            optimizador.setVisible(true); // Hacer visible la ventana
        });
    }
}
