package actividad_9.ejercicio_1; // Declaración del paquete donde reside la clase

import com.formdev.flatlaf.FlatLightLaf; // Importa el tema visual moderno FlatLaf para la interfaz
import org.apache.commons.math3.distribution.PoissonDistribution; // Importa distribución de Poisson para generar demanda aleatoria
import org.jfree.chart.*; // Importa todas las clases principales de JFreeChart
import org.jfree.chart.axis.NumberAxis; // Importa clase para ejes numéricos en gráficos
import org.jfree.chart.plot.*; // Importa clases para configurar plots de gráficos
import org.jfree.chart.renderer.xy.XYBarRenderer; // Importa renderizador para gráficos de barras XY
import org.jfree.chart.ui.RectangleAnchor; // Importa clase para anclaje de rectángulos en gráficos
import org.jfree.chart.ui.TextAnchor; // Importa clase para anclaje de texto en gráficos
import org.jfree.data.statistics.HistogramDataset; // Importa dataset especializado para histogramas

import javax.swing.*; // Importa todos los componentes de interfaz gráfica Swing
import javax.swing.table.*; // Importa componentes de tablas de Swing
import java.awt.*; // Importa componentes gráficos y layouts de AWT
import java.text.DecimalFormat; // Importa clase para formatear números decimales
import java.util.*; // Importa utilidades generales (Random, Arrays, etc.)
import java.util.List; // Importa específicamente la interfaz List

public class InventorySystemSimulator extends JFrame { // Declara clase pública que extiende JFrame (ventana principal)

    private static class Config { // Declara clase interna estática para almacenar constantes de configuración
        static final double COSTO_PEDIDO = 50.0; // Define costo fijo por realizar un pedido: $50
        static final double COSTO_TENENCIA = 0.20; // Define costo de mantener una unidad en inventario por semana: $0.20
        static final double COSTO_VENTAS_PERDIDAS = 100.0; // Define costo por cada unidad de demanda no satisfecha: $100
        static final int PLAZO_ENTREGA = 2; // Define tiempo de entrega del pedido en semanas: 2
        static final int DUE_OFFSET = PLAZO_ENTREGA + 1; // Calcula offset para semana de vencimiento: 2 + 1 = 3
        static final int DEMANDA_MEDIA = 100; // Define demanda promedio por semana: 100 unidades
        static final int NUM_SEMANAS = 52; // Define número de semanas a simular: 52 (1 año)

        static final int ORDEN_MIN = 200; // Define cantidad mínima de pedido a probar en optimización
        static final int ORDEN_MAX = 400; // Define cantidad máxima de pedido a probar en optimización
        static final int ORDEN_PASO = 5; // Define incremento entre cantidades de pedido a probar: cada 5 unidades
        static final int REORDEN_MIN = 200; // Define punto de reorden mínimo a probar en optimización
        static final int REORDEN_MAX = 400; // Define punto de reorden máximo a probar en optimización
        static final int REORDEN_PASO = 10; // Define incremento entre puntos de reorden a probar: cada 10 unidades

        static final int NUM_SIMULACIONES = 563; // Define número total de combinaciones a simular (OptQuest)
        static final int NUM_PRUEBAS_MC = 5000; // Define número de pruebas Monte Carlo por cada combinación
    }

    private static final DecimalFormat FMT_NUMBER = new DecimalFormat("#,##0.00"); // Define formato decimal con 2 decimales y separador de miles
    private static final DecimalFormat FMT_INT = new DecimalFormat("#,##0"); // Define formato entero con separador de miles

    private DefaultTableModel modeloTabla; // Declara variable para el modelo de datos de la tabla principal
    private JLabel lblCantidadPedido, lblPuntoReorden, lblInventarioInicial; // Declara etiquetas para mostrar parámetros principales
    private JLabel lblCostoAlmacenamiento, lblCostoPedido, lblCostoFaltante, lblCostoTotal; // Declara etiquetas para mostrar costos totales
    private JProgressBar progressBar, progressBarPruebas; // Declara barras de progreso para simulaciones y pruebas
    private JLabel lblSimulaciones, lblPruebas; // Declara etiquetas para mostrar texto de progreso
    private JButton btnOptimizar, btnGraficas; // Declara botones para optimizar y ver gráficas

    private double mejorCosto = Double.POSITIVE_INFINITY; // Inicializa mejor costo en infinito positivo (para minimizar)
    private int mejorCantidadPedido = 250; // Inicializa mejor cantidad de pedido en 250 (valor por defecto)
    private int mejorPuntoReorden = 250; // Inicializa mejor punto de reorden en 250 (valor por defecto)
    private List<Double> costosFinales = new ArrayList<>(); // Inicializa lista vacía para almacenar costos de la mejor solución

    public InventorySystemSimulator() { // Constructor de la clase
        super("Simulación de inventario con ventas perdidas"); // Llama al constructor de JFrame con el título de la ventana
        configurarUI(); // Llama al método para configurar la interfaz de usuario
        simularYMostrarEnTabla(250, 250); // Simula y muestra datos por defecto con cantidad pedido=250 y punto reorden=250
        setSize(1600, 850); // Establece el tamaño de la ventana: 1600x850 píxeles
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Define que al cerrar la ventana se cierre la aplicación
    }

    private void configurarUI() { // Método para configurar la interfaz de usuario completa
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30); // Crea panel principal con BorderLayout y margen
        main.add(crearTitulo(), BorderLayout.NORTH); // Agrega panel de título en la parte superior
        main.add(crearPanelSuperior(), BorderLayout.NORTH); // Agrega panel de parámetros en la parte superior
        main.add(crearTabla(), BorderLayout.CENTER); // Agrega tabla en el centro

        JPanel sur = new JPanel(new BorderLayout(10, 10)); // Crea panel inferior con BorderLayout
        sur.setBackground(Color.WHITE); // Establece fondo blanco para el panel inferior
        sur.add(crearPanelTotales(), BorderLayout.NORTH); // Agrega panel de totales en la parte superior del panel sur
        sur.add(crearPanelControl(), BorderLayout.CENTER); // Agrega panel de controles en el centro del panel sur

        main.add(sur, BorderLayout.SOUTH); // Agrega panel sur en la parte inferior del panel principal
        add(main); // Agrega panel principal a la ventana (JFrame)
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) { // Método auxiliar para crear panel con margen personalizado
        JPanel panel = new JPanel(layout); // Crea nuevo panel con el layout especificado
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left)); // Crea borde vacío con margen especificado
        return panel; // Retorna el panel creado
    }

    private JPanel crearTitulo() { // Método para crear panel que contiene el título principal
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel con FlowLayout alineado a la izquierda
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        JLabel titulo = new JLabel("Simulación de inventario con ventas perdidas"); // Crea etiqueta con el texto del título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Establece fuente Segoe UI, negrita, tamaño 24
        titulo.setForeground(new Color(31, 78, 120)); // Establece color de texto azul oscuro (RGB: 31, 78, 120)
        panel.add(titulo); // Agrega la etiqueta al panel
        return panel; // Retorna el panel con el título
    }

    private JPanel crearPanelSuperior() { // Método para crear panel superior con parámetros del problema
        JPanel panel = new JPanel(new BorderLayout()); // Crea panel con BorderLayout
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0)); // Crea margen: 10 arriba, 0 lados, 15 abajo

        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5)); // Crea panel para información con GridLayout de 2 filas x 1 columna
        info.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lblObjetivo = new JLabel("Optimizar la cantidad de pedidos y el punto de reorden..."); // Crea etiqueta con primera línea del objetivo
        lblObjetivo.setFont(new Font("Calibri", Font.PLAIN, 14)); // Establece fuente Calibri, normal, tamaño 14
        lblObjetivo.setForeground(new Color(50, 100, 150)); // Establece color de texto azul

        JLabel lblObjetivo2 = new JLabel("...para minimizar costos"); // Crea etiqueta con segunda línea del objetivo
        lblObjetivo2.setFont(new Font("Calibri", Font.PLAIN, 14)); // Establece fuente Calibri, normal, tamaño 14
        lblObjetivo2.setForeground(new Color(50, 100, 150)); // Establece color de texto azul

        info.add(lblObjetivo); // Agrega primera línea del objetivo al panel info
        info.add(lblObjetivo2); // Agrega segunda línea del objetivo al panel info

        JPanel parametros = new JPanel(new GridLayout(4, 4, 10, 5)); // Crea panel para parámetros con GridLayout de 4 filas x 4 columnas
        parametros.setBackground(Color.WHITE); // Establece fondo blanco
        parametros.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Sin margen adicional

        parametros.add(crearLabelParametro("Cantidad de pedido")); // Agrega etiqueta "Cantidad de pedido"
        lblCantidadPedido = crearLabelValor("250", new Color(255, 255, 0)); // Crea y guarda referencia a etiqueta de valor con fondo amarillo
        parametros.add(lblCantidadPedido); // Agrega etiqueta de cantidad de pedido
        parametros.add(crearLabelParametro("Costo del pedido")); // Agrega etiqueta "Costo del pedido"
        parametros.add(crearLabelValor("$ 50", Color.WHITE)); // Agrega etiqueta con valor fijo $50

        parametros.add(crearLabelParametro("Punto de reorden")); // Agrega etiqueta "Punto de reorden"
        lblPuntoReorden = crearLabelValor("250", new Color(255, 255, 0)); // Crea y guarda referencia a etiqueta de valor con fondo amarillo
        parametros.add(lblPuntoReorden); // Agrega etiqueta de punto de reorden
        parametros.add(crearLabelParametro("Costo de tenencia")); // Agrega etiqueta "Costo de tenencia"
        parametros.add(crearLabelValor("$ 0,20", Color.WHITE)); // Agrega etiqueta con valor fijo $0.20

        parametros.add(crearLabelParametro("Inventario inicial")); // Agrega etiqueta "Inventario inicial"
        lblInventarioInicial = crearLabelValor("250", Color.WHITE); // Crea y guarda referencia a etiqueta de valor
        parametros.add(lblInventarioInicial); // Agrega etiqueta de inventario inicial
        parametros.add(crearLabelParametro("Costo de ventas perdidas")); // Agrega etiqueta "Costo de ventas perdidas"
        parametros.add(crearLabelValor("$ 100", Color.WHITE)); // Agrega etiqueta con valor fijo $100

        parametros.add(crearLabelParametro("plazo de entrega")); // Agrega etiqueta "plazo de entrega"
        parametros.add(crearLabelValor("2 semanas", Color.WHITE)); // Agrega etiqueta con valor fijo "2 semanas"
        parametros.add(new JLabel()); // Agrega etiqueta vacía (celda vacía en grid)
        parametros.add(new JLabel()); // Agrega etiqueta vacía (celda vacía en grid)

        JPanel derecha = new JPanel(new BorderLayout()); // Crea panel derecho con BorderLayout
        derecha.setBackground(Color.WHITE); // Establece fondo blanco
        derecha.add(parametros, BorderLayout.NORTH); // Agrega panel de parámetros en la parte superior

        panel.add(info, BorderLayout.WEST); // Agrega panel de información a la izquierda del panel principal
        panel.add(derecha, BorderLayout.CENTER); // Agrega panel derecho al centro del panel principal

        return panel; // Retorna el panel superior completo
    }

    private JLabel crearLabelParametro(String texto) { // Método para crear etiqueta de nombre de parámetro
        JLabel lbl = new JLabel(texto); // Crea nueva etiqueta con el texto especificado
        lbl.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente Calibri, normal, tamaño 11
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea el texto a la derecha
        return lbl; // Retorna la etiqueta creada
    }

    private JLabel crearLabelValor(String texto, Color bg) { // Método para crear etiqueta de valor de parámetro
        JLabel lbl = new JLabel(texto); // Crea nueva etiqueta con el texto especificado
        lbl.setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente Calibri, negrita, tamaño 11
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea el texto al centro
        lbl.setBackground(bg); // Establece el color de fondo especificado
        lbl.setOpaque(true); // Hace la etiqueta opaca para que se vea el color de fondo
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel
        return lbl; // Retorna la etiqueta creada
    }

    private JScrollPane crearTabla() { // Método para crear la tabla principal de simulación
        String[] cols = {"Semana", "Posición de\ninventario", "Inventario\ninicial", "Pedido\nrecibido", // Define array con nombres de columnas (15 columnas en total)
                        "Unidades\nrecibidas", "Demanda", "Inventario\nfinal", "Ventas\nperdidas",
                        "¿Pedido\nrealizado?", "Posición\ninventario\nfinal", "Semana\nvencimiento",
                        "Costo de\nalmacenamiento", "Costo del\npedido", "Costo por\nfaltante", "Costo\ntotal"};

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea modelo de tabla con columnas especificadas y 0 filas iniciales
            public boolean isCellEditable(int r, int c) { return false; } // Sobrescribe método para hacer todas las celdas no editables
        };

        JTable tabla = new JTable(modeloTabla); // Crea tabla visual usando el modelo creado
        configurarEstiloTabla(tabla); // Llama a método para configurar estilos de la tabla

        JScrollPane scroll = new JScrollPane(tabla); // Crea panel con scroll que contiene la tabla
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Agrega borde gris claro al scroll
        return scroll; // Retorna el scroll con la tabla
    }

    private void configurarEstiloTabla(JTable tabla) { // Método para configurar estilos visuales de la tabla
        tabla.setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente de las celdas: Calibri, normal, tamaño 10
        tabla.setRowHeight(25); // Establece altura de las filas: 25 píxeles
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 10)); // Establece fuente del encabezado: Calibri, negrita, tamaño 10
        tabla.getTableHeader().setBackground(new Color(79, 129, 189)); // Establece color de fondo del encabezado: azul
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece color de texto del encabezado: blanco
        tabla.setGridColor(new Color(200, 200, 200)); // Establece color de las líneas de cuadrícula: gris claro

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado para las celdas
            public Component getTableCellRendererComponent(JTable t, Object v, // Sobrescribe método de renderizado
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al renderizado base
                setHorizontalAlignment(SwingConstants.CENTER); // Alinea el contenido de la celda al centro
                setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente de la celda

                if (c == 5) { // Si es la columna 5 (Demanda)
                    setBackground(new Color(0, 255, 0)); // Establece fondo verde (indica dato aleatorio)
                } else if (c == 8) { // Si es la columna 8 (¿Pedido realizado?)
                    setBackground(Color.WHITE); // Establece fondo blanco
                } else { // Para todas las demás columnas
                    setBackground(Color.WHITE); // Establece fondo blanco
                }

                setForeground(Color.BLACK); // Establece color de texto: negro
                return this; // Retorna el componente renderizado
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre todas las columnas de la tabla
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica el renderizador personalizado a cada columna
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 60 : 85); // Establece ancho preferido: 60 para columna 0, 85 para las demás
        }
    }

    private JPanel crearPanelTotales() { // Método para crear panel que muestra costos totales anuales
        JPanel panel = new JPanel(new GridLayout(1, 4, 15, 0)); // Crea panel con GridLayout de 1 fila x 4 columnas
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Crea margen: 15 arriba, 10 abajo

        JLabel titulo = new JLabel("Costos anuales totales"); // Crea etiqueta con título de la sección
        titulo.setFont(new Font("Calibri", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        panel.add(titulo); // Agrega título al panel

        lblCostoAlmacenamiento = crearLabelTotal("$", "1.040"); // Crea y guarda referencia a etiqueta de costo de almacenamiento
        lblCostoPedido = crearLabelTotal("$", "1.050"); // Crea y guarda referencia a etiqueta de costo de pedidos
        lblCostoFaltante = crearLabelTotal("$", "5.000"); // Crea y guarda referencia a etiqueta de costo por faltante
        lblCostoTotal = crearLabelTotal("$", "7.090", new Color(0, 255, 255)); // Crea y guarda referencia a etiqueta de costo total con fondo cian

        panel.add(lblCostoAlmacenamiento); // Agrega etiqueta de costo almacenamiento al panel
        panel.add(lblCostoPedido); // Agrega etiqueta de costo pedido al panel
        panel.add(lblCostoFaltante); // Agrega etiqueta de costo faltante al panel
        panel.add(lblCostoTotal); // Agrega etiqueta de costo total al panel

        return panel; // Retorna el panel completo con todos los costos
    }

    private JLabel crearLabelTotal(String prefijo, String valor) { // Sobrecarga del método para crear etiqueta de total con fondo blanco por defecto
        return crearLabelTotal(prefijo, valor, Color.WHITE); // Llama al método completo con fondo blanco
    }

    private JLabel crearLabelTotal(String prefijo, String valor, Color bg) { // Método completo para crear etiqueta de total con fondo personalizado
        JLabel lbl = new JLabel(prefijo + " " + valor); // Crea etiqueta concatenando prefijo (ej: "$") y valor
        lbl.setFont(new Font("Calibri", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea texto al centro
        lbl.setBackground(bg); // Establece color de fondo especificado
        lbl.setForeground(Color.BLACK); // Establece color de texto: negro
        lbl.setOpaque(true); // Hace la etiqueta opaca para mostrar el color de fondo
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro
        return lbl; // Retorna la etiqueta creada
    }

    private JPanel crearPanelControl() { // Método para crear panel de controles (botones y barras de progreso)
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // Crea panel con GridLayout de 2 filas x 1 columna
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Crea margen: 15 arriba, 10 abajo

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Crea panel para botones con FlowLayout centrado
        panelBtns.setBackground(Color.WHITE); // Establece fondo blanco

        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)", // Crea y guarda referencia al botón de optimización
                                  new Color(68, 114, 196), 320, 45); // Con color azul, ancho 320, alto 45
        btnGraficas = crearBoton("Ver Gráficas", new Color(112, 173, 71), 180, 40); // Crea y guarda referencia al botón de gráficas con color verde
        btnGraficas.setEnabled(false); // Deshabilita el botón de gráficas inicialmente

        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Agrega listener al botón optimizar para ejecutar optimización al hacer clic
        btnGraficas.addActionListener(e -> mostrarHistograma()); // Agrega listener al botón gráficas para mostrar histograma al hacer clic

        panelBtns.add(btnOptimizar); // Agrega botón optimizar al panel de botones
        panelBtns.add(btnGraficas); // Agrega botón gráficas al panel de botones

        JPanel panelProgress = crearPanelProgreso(); // Crea panel con barras de progreso

        panel.add(panelBtns); // Agrega panel de botones a la fila superior del panel de control
        panel.add(panelProgress); // Agrega panel de progreso a la fila inferior del panel de control
        return panel; // Retorna el panel de control completo
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar para crear botón con parámetros personalizados
        JButton btn = new JButton(texto); // Crea nuevo botón con el texto especificado
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 15 ? 15 : 14)); // Establece fuente negrita, tamaño 15 si texto largo, 14 si corto
        btn.setBackground(bg); // Establece color de fondo del botón
        btn.setForeground(Color.WHITE); // Establece color de texto: blanco
        btn.setFocusPainted(false); // Desactiva el borde de foco al hacer clic
        btn.setPreferredSize(new Dimension(ancho, alto)); // Establece tamaño preferido del botón
        return btn; // Retorna el botón creado
    }

    private JPanel crearPanelProgreso() { // Método para crear panel con barras de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea panel con BorderLayout
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder( // Crea borde con título
            BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest")); // Borde gris con texto "Panel de control: OptQuest"

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8)); // Crea panel para contener las barras con GridLayout de 2 filas
        barras.setBackground(Color.WHITE); // Establece fondo blanco
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Crea margen de 8 arriba/abajo, 15 izquierda/derecha

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales", // Crea barra de progreso para simulaciones
                                         new Color(0, 32, 96), out -> lblSimulaciones = out); // Color azul oscuro, guarda referencia a etiqueta
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas", // Crea barra de progreso para pruebas MC
                                                new Color(0, 176, 80), out -> lblPruebas = out); // Color verde, guarda referencia a etiqueta

        barras.add(progressBar.getParent()); // Agrega panel contenedor de barra de simulaciones
        barras.add(progressBarPruebas.getParent()); // Agrega panel contenedor de barra de pruebas

        panel.add(barras); // Agrega panel de barras al panel principal de progreso
        return panel; // Retorna el panel de progreso completo
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color, // Método para crear barra de progreso con etiqueta
                                           java.util.function.Consumer<JLabel> labelOut) { // Consumer para devolver referencia a etiqueta
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel contenedor con BorderLayout
        panel.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lbl = new JLabel(texto + ": 0 / " + max); // Crea etiqueta con texto descriptivo y contador inicial
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente pequeña tamaño 10
        labelOut.accept(lbl); // Pasa la referencia de la etiqueta al consumer (para poder actualizarla externamente)

        JProgressBar bar = new JProgressBar(0, max); // Crea barra de progreso con rango de 0 a max
        bar.setPreferredSize(new Dimension(500, 22)); // Establece tamaño preferido de la barra: 500x22 píxeles
        bar.setForeground(color); // Establece color de la barra de progreso
        bar.setBackground(Color.WHITE); // Establece color de fondo de la barra

        panel.add(lbl, BorderLayout.WEST); // Agrega etiqueta a la izquierda del panel
        panel.add(bar, BorderLayout.CENTER); // Agrega barra de progreso al centro del panel
        panel.add(new JLabel(max + "  "), BorderLayout.EAST); // Agrega etiqueta con valor máximo a la derecha

        return bar; // Retorna la barra de progreso (el panel se obtiene con getParent())
    }

    private void ejecutarOptimizacion() { // Método principal que ejecuta el algoritmo de optimización
        btnOptimizar.setEnabled(false); // Deshabilita botón de optimización durante la ejecución
        btnGraficas.setEnabled(false); // Deshabilita botón de gráficas durante la ejecución
        costosFinales.clear(); // Limpia lista de costos finales de ejecuciones anteriores
        mejorCosto = Double.POSITIVE_INFINITY; // Reinicia mejor costo a infinito positivo (para minimizar)
        modeloTabla.setRowCount(0); // Limpia todas las filas de la tabla

        new SwingWorker<Void, int[]>() { // Crea SwingWorker para ejecutar optimización en hilo separado (no bloquea UI)
            protected Void doInBackground() { // Método ejecutado en hilo de fondo
                Random rand = new Random(); // Crea generador de números aleatorios
                int simCount = 0; // Inicializa contador de simulaciones en 0

                for (int cantPedido = Config.ORDEN_MIN; cantPedido <= Config.ORDEN_MAX; cantPedido += Config.ORDEN_PASO) { // Itera sobre cantidades de pedido desde 200 hasta 400 de 5 en 5
                    for (int puntoReorden = Config.REORDEN_MIN; puntoReorden <= Config.REORDEN_MAX; puntoReorden += Config.REORDEN_PASO) { // Itera sobre puntos de reorden desde 200 hasta 400 de 10 en 10
                        List<Double> costosSim = new ArrayList<>(); // Crea lista temporal para costos de esta combinación

                        for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) { // Ejecuta 5000 pruebas Monte Carlo para esta combinación
                            double costoTotal = simularInventario(cantPedido, puntoReorden, rand); // Simula inventario y obtiene costo total
                            costosSim.add(costoTotal); // Agrega costo a la lista

                            if (mc % 250 == 0) { // Cada 250 pruebas
                                publish(new int[]{simCount + 1, mc + 1}); // Publica actualización de progreso
                            }
                        }

                        double costoMedio = costosSim.stream().mapToDouble(d -> d).average().orElse(0); // Calcula costo medio de las 5000 pruebas

                        if (costoMedio < mejorCosto) { // Si este costo medio es mejor (menor) que el mejor encontrado
                            mejorCosto = costoMedio; // Actualiza mejor costo
                            mejorCantidadPedido = cantPedido; // Guarda mejor cantidad de pedido
                            mejorPuntoReorden = puntoReorden; // Guarda mejor punto de reorden
                            costosFinales = new ArrayList<>(costosSim); // Guarda copia de todos los costos para el histograma
                        }

                        simCount++; // Incrementa contador de simulaciones
                        if (simCount >= Config.NUM_SIMULACIONES) break; // Si alcanzó límite de 563 simulaciones, sale del bucle interno
                    }
                    if (simCount >= Config.NUM_SIMULACIONES) break; // Si alcanzó límite de 563 simulaciones, sale del bucle externo
                }
                return null; // Retorna null (requerido por SwingWorker)
            }

            protected void process(List<int[]> chunks) { // Método ejecutado en hilo de UI para actualizar progreso
                int[] ultimo = chunks.get(chunks.size() - 1); // Obtiene última actualización de progreso publicada
                progressBar.setValue(ultimo[0]); // Actualiza valor de barra de simulaciones
                progressBarPruebas.setValue(ultimo[1]); // Actualiza valor de barra de pruebas
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES); // Actualiza texto de simulaciones
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC); // Actualiza texto de pruebas
            }

            protected void done() { // Método ejecutado en hilo de UI cuando termina la optimización
                actualizarResultados(); // Actualiza interfaz con mejores resultados encontrados
                btnOptimizar.setEnabled(true); // Habilita botón de optimización
                btnGraficas.setEnabled(true); // Habilita botón de gráficas
                mostrarVentanaResultados(); // Muestra ventana emergente con resultados
            }
        }.execute(); // Inicia la ejecución del SwingWorker
    }

    private double simularInventario(int cantidadPedido, int puntoReorden, Random rand) { // Método que simula el sistema de inventario por 52 semanas y retorna costo total
        PoissonDistribution poissonDist = new PoissonDistribution(Config.DEMANDA_MEDIA); // Crea distribución de Poisson con media 100 para generar demanda aleatoria

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Array para posición de inventario (índices 1 a 52, índice 0 no se usa)
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para inventario inicial de cada semana
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para unidades recibidas de pedidos
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para demanda de cada semana
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para inventario final de cada semana
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para ventas perdidas (demanda no satisfecha)
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array booleano para indicar si se realizó pedido
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para posición de inventario final
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para registrar en qué semana vence cada pedido

        double costoAlmacenamientoTotal = 0; // Acumulador para costo total de almacenamiento
        double costoPedidoTotal = 0; // Acumulador para costo total de pedidos
        double costoFaltanteTotal = 0; // Acumulador para costo total por faltantes

        posicionInventario[1] = cantidadPedido; // SEMANA 1: Posición inicial es igual a cantidad de pedido
        inventarioInicial[1] = cantidadPedido; // SEMANA 1: Inventario inicial es igual a cantidad de pedido
        unidadesRecibidas[1] = 0; // SEMANA 1: No se reciben unidades (no hay pedidos previos)
        demanda[1] = poissonDist.sample(); // SEMANA 1: Genera demanda aleatoria usando distribución de Poisson

        int projected1 = posicionInventario[1] - demanda[1]; // SEMANA 1: Calcula proyección de inventario después de satisfacer demanda
        pedidoRealizado[1] = projected1 <= puntoReorden; // SEMANA 1: Decide si realizar pedido (si proyección <= punto de reorden)

        int satisfied1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1: Calcula demanda satisfecha (mínimo entre demanda e inventario disponible)
        ventasPerdidas[1] = demanda[1] - satisfied1; // SEMANA 1: Calcula ventas perdidas (demanda no satisfecha)

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1: Calcula inventario final (no puede ser negativo)

        posicionInventarioFinal[1] = posicionInventario[1] - satisfied1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1: Calcula posición final = posición - satisfecha + pedido (si se hizo)

        if (pedidoRealizado[1]) { // SEMANA 1: Si se realizó un pedido
            semanaVencimiento[1] = 1 + Config.DUE_OFFSET; // Registra que vencerá en semana 1 + 3 = 4
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1: Calcula costo de almacenamiento = inventario final * 0.20
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1: Calcula costo de pedido = 50 si se hizo pedido, 0 si no
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1: Calcula costo de faltante = ventas perdidas * 100

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1: Acumula costo de almacenamiento
        costoPedidoTotal += costoPedido; // SEMANA 1: Acumula costo de pedido
        costoFaltanteTotal += costoFaltante; // SEMANA 1: Acumula costo de faltante

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Itera desde semana 2 hasta 52
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Posición inicial = posición final de semana anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Inventario inicial = inventario final de semana anterior

            int numArriving = 0; // Inicializa contador de pedidos que llegan esta semana
            for (int s = 1; s < sem; s++) { // Revisa todas las semanas anteriores
                if (semanaVencimiento[s] == sem) { // Si un pedido de semana s vence esta semana
                    numArriving++; // Incrementa contador de pedidos llegando
                }
            }
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas = número de pedidos * cantidad por pedido
            demanda[sem] = poissonDist.sample(); // Genera demanda aleatoria para esta semana

            int projected = posicionInventario[sem] - demanda[sem]; // Calcula proyección de inventario
            pedidoRealizado[sem] = projected <= puntoReorden; // Decide si realizar pedido

            int satisfied = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]); // Calcula demanda satisfecha
            ventasPerdidas[sem] = demanda[sem] - satisfied; // Calcula ventas perdidas

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]); // Calcula inventario final (no negativo)

            posicionInventarioFinal[sem] = posicionInventario[sem] - satisfied + (pedidoRealizado[sem] ? cantidadPedido : 0); // Calcula posición final

            if (pedidoRealizado[sem]) { // Si se realizó pedido esta semana
                semanaVencimiento[sem] = sem + Config.DUE_OFFSET; // Registra semana de vencimiento = semana actual + 3
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA; // Calcula costo de almacenamiento
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0; // Calcula costo de pedido
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS; // Calcula costo de faltante

            costoAlmacenamientoTotal += costoAlmacenamiento; // Acumula costo de almacenamiento
            costoPedidoTotal += costoPedido; // Acumula costo de pedido
            costoFaltanteTotal += costoFaltante; // Acumula costo de faltante
        }

        return costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal; // Retorna costo total de las 52 semanas
    }

    private void actualizarResultados() { // Método para actualizar interfaz con mejores resultados encontrados
        lblCantidadPedido.setText(String.valueOf(mejorCantidadPedido)); // Actualiza etiqueta de cantidad de pedido con mejor valor
        lblPuntoReorden.setText(String.valueOf(mejorPuntoReorden)); // Actualiza etiqueta de punto de reorden con mejor valor
        lblInventarioInicial.setText(String.valueOf(mejorCantidadPedido)); // Actualiza etiqueta de inventario inicial (igual a cantidad de pedido)

        simularYMostrarEnTabla(mejorCantidadPedido, mejorPuntoReorden); // Simula una vez más con mejores parámetros y llena la tabla para visualización
    }

    private void simularYMostrarEnTabla(int cantidadPedido, int puntoReorden) { // Método para simular y mostrar resultados en tabla (usa demanda fija para reproducibilidad visual)
        modeloTabla.setRowCount(0); // Limpia todas las filas de la tabla

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Array para posición de inventario
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para inventario inicial
        boolean[] pedidoRecibido = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se recibió pedido (para mostrar en tabla)
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para unidades recibidas
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para demanda
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para inventario final
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para ventas perdidas
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se realizó pedido
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para posición final
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para semana de vencimiento

        double costoAlmacenamientoTotal = 0; // Acumulador de costo de almacenamiento
        double costoPedidoTotal = 0; // Acumulador de costo de pedidos
        double costoFaltanteTotal = 0; // Acumulador de costo por faltantes

        final int fixedDemand = 100; // Define demanda fija de 100 para todas las semanas (para visualización reproducible)

        posicionInventario[1] = cantidadPedido; // SEMANA 1: Establece posición inicial
        inventarioInicial[1] = cantidadPedido; // SEMANA 1: Establece inventario inicial
        pedidoRecibido[1] = false; // SEMANA 1: No se recibió pedido
        unidadesRecibidas[1] = 0; // SEMANA 1: Cero unidades recibidas
        demanda[1] = fixedDemand; // SEMANA 1: Usa demanda fija de 100

        int projected1 = posicionInventario[1] - demanda[1]; // SEMANA 1: Calcula proyección
        pedidoRealizado[1] = projected1 <= puntoReorden; // SEMANA 1: Decide si realizar pedido

        int satisfied1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1: Calcula demanda satisfecha
        ventasPerdidas[1] = demanda[1] - satisfied1; // SEMANA 1: Calcula ventas perdidas

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1: Calcula inventario final

        posicionInventarioFinal[1] = posicionInventario[1] - satisfied1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1: Calcula posición final

        if (pedidoRealizado[1]) { // SEMANA 1: Si se realizó pedido
            semanaVencimiento[1] = 1 + Config.DUE_OFFSET; // Registra semana de vencimiento
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1: Calcula costo almacenamiento
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1: Calcula costo pedido
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1: Calcula costo faltante

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1: Acumula costos
        costoPedidoTotal += costoPedido; // SEMANA 1: Acumula costos
        costoFaltanteTotal += costoFaltante; // SEMANA 1: Acumula costos

        modeloTabla.addRow(new Object[]{ // SEMANA 1: Agrega fila a la tabla con todos los datos de la semana 1
            "1", // Número de semana
            FMT_INT.format(posicionInventario[1]), // Posición de inventario formateada
            FMT_INT.format(inventarioInicial[1]), // Inventario inicial formateado
            pedidoRecibido[1] ? "VERDADERO" : "FALSO", // Pedido recibido (texto)
            FMT_INT.format(unidadesRecibidas[1]), // Unidades recibidas formateadas
            FMT_INT.format(demanda[1]), // Demanda formateada
            FMT_INT.format(inventarioFinal[1]), // Inventario final formateado
            FMT_INT.format(ventasPerdidas[1]), // Ventas perdidas formateadas
            pedidoRealizado[1] ? "VERDADERO" : "FALSO", // Pedido realizado (texto)
            FMT_INT.format(posicionInventarioFinal[1]), // Posición final formateada
            semanaVencimiento[1] > 0 ? String.valueOf(semanaVencimiento[1]) : "", // Semana vencimiento (vacío si no hay)
            "$    " + FMT_NUMBER.format(costoAlmacenamiento), // Costo almacenamiento con formato
            "$    " + FMT_NUMBER.format(costoPedido), // Costo pedido con formato
            "$    " + FMT_NUMBER.format(costoFaltante), // Costo faltante con formato
            "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante) // Costo total con formato
        });

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Itera desde semana 2 hasta 52
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Posición inicial = posición final anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Inventario inicial = inventario final anterior

            int numArriving = 0; // Contador de pedidos llegando
            for (int s = 1; s < sem; s++) { // Revisa semanas anteriores
                if (semanaVencimiento[s] == sem) { // Si pedido vence esta semana
                    numArriving++; // Incrementa contador
                }
            }
            pedidoRecibido[sem] = numArriving > 0; // Marca si se recibió al menos un pedido
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas
            demanda[sem] = fixedDemand; // Usa demanda fija

            int projected = posicionInventario[sem] - demanda[sem]; // Calcula proyección
            pedidoRealizado[sem] = projected <= puntoReorden; // Decide si realizar pedido

            int satisfied = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]); // Calcula demanda satisfecha
            ventasPerdidas[sem] = demanda[sem] - satisfied; // Calcula ventas perdidas

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]); // Calcula inventario final

            posicionInventarioFinal[sem] = posicionInventario[sem] - satisfied + (pedidoRealizado[sem] ? cantidadPedido : 0); // Calcula posición final

            if (pedidoRealizado[sem]) { // Si se realizó pedido
                semanaVencimiento[sem] = sem + Config.DUE_OFFSET; // Registra semana de vencimiento
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA; // Calcula costo almacenamiento
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0; // Calcula costo pedido
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS; // Calcula costo faltante

            costoAlmacenamientoTotal += costoAlmacenamiento; // Acumula costos
            costoPedidoTotal += costoPedido; // Acumula costos
            costoFaltanteTotal += costoFaltante; // Acumula costos

            modeloTabla.addRow(new Object[]{ // Agrega fila a la tabla con todos los datos de esta semana
                String.valueOf(sem), // Número de semana
                FMT_INT.format(posicionInventario[sem]), // Posición inventario
                FMT_INT.format(inventarioInicial[sem]), // Inventario inicial
                pedidoRecibido[sem] ? "VERDADERO" : "FALSO", // Pedido recibido
                FMT_INT.format(unidadesRecibidas[sem]), // Unidades recibidas
                FMT_INT.format(demanda[sem]), // Demanda
                FMT_INT.format(inventarioFinal[sem]), // Inventario final
                FMT_INT.format(ventasPerdidas[sem]), // Ventas perdidas
                pedidoRealizado[sem] ? "VERDADERO" : "FALSO", // Pedido realizado
                FMT_INT.format(posicionInventarioFinal[sem]), // Posición final
                semanaVencimiento[sem] > 0 ? String.valueOf(semanaVencimiento[sem]) : "", // Semana vencimiento
                "$    " + FMT_NUMBER.format(costoAlmacenamiento), // Costo almacenamiento
                "$    " + FMT_NUMBER.format(costoPedido), // Costo pedido
                "$    " + FMT_NUMBER.format(costoFaltante), // Costo faltante
                "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante) // Costo total
            });
        }

        lblCostoAlmacenamiento.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal)); // Actualiza etiqueta de costo almacenamiento total
        lblCostoPedido.setText("$ " + FMT_NUMBER.format(costoPedidoTotal)); // Actualiza etiqueta de costo pedido total
        lblCostoFaltante.setText("$ " + FMT_NUMBER.format(costoFaltanteTotal)); // Actualiza etiqueta de costo faltante total
        lblCostoTotal.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal)); // Actualiza etiqueta de costo total
    }

    private void mostrarVentanaResultados() { // Método para mostrar ventana emergente con resultados de optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crea diálogo no modal
        dlg.setLayout(new BorderLayout()); // Establece layout

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15); // Crea panel principal con margen

        JPanel headerPanel = new JPanel(new BorderLayout()); // Crea panel para encabezado
        headerPanel.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblHeader = new JLabel(Config.NUM_SIMULACIONES + " simulaciones"); // Crea etiqueta con número de simulaciones
        lblHeader.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita

        JLabel lblSubHeader = new JLabel("Vista de mejor solución"); // Crea etiqueta de subtítulo
        lblSubHeader.setFont(new Font("Calibri", Font.PLAIN, 12)); // Fuente normal

        headerPanel.add(lblHeader, BorderLayout.NORTH); // Agrega encabezado arriba
        headerPanel.add(lblSubHeader, BorderLayout.CENTER); // Agrega subtítulo al centro

        main.add(headerPanel, BorderLayout.NORTH); // Agrega panel de encabezado arriba

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 10)); // Crea panel para 3 secciones con GridLayout
        tablas.setBackground(Color.WHITE); // Fondo blanco

        JPanel graficoPanel = new JPanel(new BorderLayout()); // Crea panel para gráfico (placeholder)
        graficoPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de rendimiento")); // Agrega borde con título
        graficoPanel.setBackground(Color.WHITE); // Fondo blanco
        graficoPanel.setPreferredSize(new Dimension(600, 150)); // Establece tamaño

        JLabel lblGrafico = new JLabel("Mejores soluciones encontradas", SwingConstants.CENTER); // Crea etiqueta centrada
        lblGrafico.setFont(new Font("Calibri", Font.ITALIC, 11)); // Fuente itálica
        graficoPanel.add(lblGrafico, BorderLayout.CENTER); // Agrega al centro

        tablas.add(graficoPanel); // Agrega panel de gráfico

        tablas.add(crearSeccionResultado("Objetivos", "Valor", // Agrega sección de objetivos
            new String[]{"Minimizar el/la Media de Total Annual Costs"}, // Texto del objetivo
            new String[]{"$" + FMT_NUMBER.format(mejorCosto)})); // Valor del objetivo

        JPanel requisitosPanel = new JPanel(new BorderLayout()); // Crea panel para requisitos
        requisitosPanel.setBorder(BorderFactory.createTitledBorder("Requisitos:")); // Agrega borde con título
        requisitosPanel.setBackground(Color.WHITE); // Fondo blanco
        JLabel lblRequisitos = new JLabel("(requisitos opcionales en previsiones)"); // Crea etiqueta
        lblRequisitos.setFont(new Font("Calibri", Font.ITALIC, 10)); // Fuente itálica pequeña
        requisitosPanel.add(lblRequisitos, BorderLayout.CENTER); // Agrega al centro
        tablas.add(requisitosPanel); // Agrega panel de requisitos

        main.add(tablas, BorderLayout.CENTER); // Agrega secciones al centro

        JPanel inferior = new JPanel(new GridLayout(1, 2, 10, 0)); // Crea panel inferior con 2 columnas
        inferior.setBackground(Color.WHITE); // Fondo blanco

        JPanel restriccionesPanel = new JPanel(new BorderLayout()); // Crea panel para restricciones
        restriccionesPanel.setBorder(BorderFactory.createTitledBorder("Restricciones")); // Agrega borde con título
        restriccionesPanel.setBackground(Color.WHITE); // Fondo blanco

        JPanel restriccionesGrid = new JPanel(new GridLayout(1, 3, 5, 5)); // Crea grid para encabezados
        restriccionesGrid.setBackground(Color.WHITE); // Fondo blanco
        restriccionesGrid.add(new JLabel("")); // Etiqueta vacía
        restriccionesGrid.add(new JLabel("Lado izquierdo", SwingConstants.CENTER)); // Encabezado columna
        restriccionesGrid.add(new JLabel("Lado derecho", SwingConstants.CENTER)); // Encabezado columna

        restriccionesPanel.add(restriccionesGrid, BorderLayout.CENTER); // Agrega grid al panel
        inferior.add(restriccionesPanel); // Agrega panel de restricciones

        JPanel variablesPanel = new JPanel(new BorderLayout()); // Crea panel para variables de decisión
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables de decisión")); // Agrega borde con título
        variablesPanel.setBackground(Color.WHITE); // Fondo blanco

        String[][] varsData = { // Define datos de variables de decisión
            {"Order Quantity", FMT_NUMBER.format(mejorCantidadPedido)}, // Cantidad de pedido
            {"Reorder Point", FMT_NUMBER.format(mejorPuntoReorden)} // Punto de reorden
        };

        DefaultTableModel modeloVars = new DefaultTableModel(new String[]{"", "Valor"}, 0); // Crea modelo de tabla
        for (String[] row : varsData) { // Itera sobre filas de datos
            modeloVars.addRow(row); // Agrega cada fila al modelo
        }

        JTable tablaVars = new JTable(modeloVars); // Crea tabla con modelo
        tablaVars.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente
        tablaVars.setRowHeight(25); // Establece altura de filas
        tablaVars.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente del encabezado

        JScrollPane scrollVars = new JScrollPane(tablaVars); // Crea scroll para tabla
        scrollVars.setPreferredSize(new Dimension(250, 80)); // Establece tamaño
        variablesPanel.add(scrollVars, BorderLayout.CENTER); // Agrega scroll al panel

        inferior.add(variablesPanel); // Agrega panel de variables

        main.add(inferior, BorderLayout.SOUTH); // Agrega panel inferior abajo

        dlg.add(main); // Agrega panel principal al diálogo
        dlg.setSize(750, 550); // Establece tamaño del diálogo
        dlg.setLocationRelativeTo(this); // Centra respecto a ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) { // Método para crear sección de resultados con tabla
        JPanel panel = new JPanel(new BorderLayout()); // Crea panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder(titulo)); // Agrega borde con título
        panel.setBackground(Color.WHITE); // Fondo blanco

        if (filas.length > 0) { // Si hay filas de datos
            DefaultTableModel modelo = new DefaultTableModel(new String[]{"Objetivos", col}, 0); // Crea modelo de tabla con columnas
            for (int i = 0; i < filas.length; i++) { // Itera sobre filas
                modelo.addRow(new Object[]{filas[i], vals[i]}); // Agrega cada fila al modelo
            }

            JTable tabla = new JTable(modelo); // Crea tabla con modelo
            tabla.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente
            tabla.setRowHeight(25); // Establece altura de filas
            tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente del encabezado

            JScrollPane scroll = new JScrollPane(tabla); // Crea scroll para tabla
            scroll.setPreferredSize(new Dimension(600, 60)); // Establece tamaño
            panel.add(scroll, BorderLayout.CENTER); // Agrega scroll al panel
        }
        return panel; // Retorna panel completo
    }

    private void mostrarHistograma() { // Método para mostrar histograma de distribución de costos
        double[] datos = costosFinales.stream().mapToDouble(d -> d).toArray(); // Convierte lista de costos a array de double
        if (datos.length == 0) return; // Si no hay datos, sale del método

        HistogramDataset dataset = new HistogramDataset(); // Crea dataset para histograma
        dataset.addSeries("Total Annual Costs", datos, 50); // Agrega serie con 50 bins (intervalos)

        JFreeChart chart = ChartFactory.createHistogram("Total Annual Costs", "Dollars", // Crea histograma con JFreeChart
            "Frecuencia", dataset, PlotOrientation.VERTICAL, true, true, false); // Orientación vertical, con leyenda, con tooltips, sin URLs

        XYPlot plot = chart.getXYPlot(); // Obtiene plot del gráfico
        plot.setBackgroundPaint(Color.WHITE); // Establece fondo blanco
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Establece color de líneas de cuadrícula del eje X
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Establece color de líneas de cuadrícula del eje Y

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtiene renderizador de barras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Establece color azul para las barras
        renderer.setShadowVisible(false); // Desactiva sombras en las barras

        double media = Arrays.stream(datos).average().orElse(0); // Calcula media de los costos

        ValueMarker marker = new ValueMarker(media); // Crea marcador vertical en la media
        marker.setPaint(Color.BLACK); // Establece color negro para el marcador
        marker.setStroke(new BasicStroke(2.0f)); // Establece grosor de línea de 2 píxeles
        marker.setLabel("Media = $" + FMT_NUMBER.format(media)); // Establece etiqueta con valor de la media
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT); // Establece ancla de etiqueta arriba a la derecha
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT); // Establece ancla de texto arriba a la izquierda
        plot.addDomainMarker(marker); // Agrega marcador al plot

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis(); // Obtiene eje X (dominio)
        domainAxis.setNumberFormatOverride(new DecimalFormat("$#,##0")); // Establece formato monetario para eje X

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); // Obtiene eje Y (rango)
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // Establece unidades enteras para eje Y

        chart.setBackgroundPaint(Color.WHITE); // Establece fondo blanco para el gráfico completo

        JFrame frame = new JFrame("Previsión: Total Annual Costs"); // Crea ventana para el histograma
        ChartPanel chartPanel = new ChartPanel(chart); // Crea panel que contiene el gráfico
        chartPanel.setPreferredSize(new Dimension(900, 600)); // Establece tamaño preferido

        JPanel mainPanel = new JPanel(new BorderLayout()); // Crea panel principal con BorderLayout
        mainPanel.add(chartPanel, BorderLayout.CENTER); // Agrega gráfico al centro

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel para estadísticas con FlowLayout a la izquierda
        statsPanel.setBackground(Color.WHITE); // Fondo blanco
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Agrega margen

        JLabel lblStats = new JLabel("5.000 pruebas                Vista de frecuencia                4.824 mostrados"); // Crea etiqueta con estadísticas
        lblStats.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente pequeña
        statsPanel.add(lblStats); // Agrega etiqueta al panel

        mainPanel.add(statsPanel, BorderLayout.SOUTH); // Agrega panel de estadísticas abajo

        frame.setContentPane(mainPanel); // Establece contenido de la ventana
        frame.pack(); // Ajusta tamaño de la ventana al contenido
        frame.setLocationRelativeTo(this); // Centra respecto a ventana principal
        frame.setVisible(true); // Hace visible la ventana del histograma
    }

    public static void main(String[] args) { // Método main - punto de entrada del programa
        try { // Bloque try para manejo de excepciones
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece Look and Feel FlatLaf (tema moderno)
        } catch (Exception e) { // Captura cualquier excepción
            e.printStackTrace(); // Imprime el error en consola
        }

        SwingUtilities.invokeLater(() -> // Ejecuta en el hilo de eventos de Swing (EDT - Event Dispatch Thread)
            new InventorySystemSimulator().setVisible(true)); // Crea instancia de la aplicación y la hace visible
    }
}