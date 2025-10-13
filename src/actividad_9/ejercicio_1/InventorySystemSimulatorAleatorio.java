package actividad_9.ejercicio_1; // Declaración del paquete donde reside la clase

import com.formdev.flatlaf.FlatLightLaf; // Importa tema visual FlatLaf para interfaz moderna
import org.apache.commons.math3.distribution.PoissonDistribution; // Importa distribución de Poisson para generar demanda aleatoria
import org.jfree.chart.*; // Importa todas las clases principales de JFreeChart para crear gráficos
import org.jfree.chart.axis.NumberAxis; // Importa clase para crear y configurar ejes numéricos en gráficos
import org.jfree.chart.plot.*; // Importa clases para configurar plots (área de dibujo) de gráficos
import org.jfree.chart.renderer.xy.XYBarRenderer; // Importa renderizador específico para gráficos de barras XY
import org.jfree.chart.ui.RectangleAnchor; // Importa clase para definir puntos de anclaje de rectángulos en gráficos
import org.jfree.chart.ui.TextAnchor; // Importa clase para definir puntos de anclaje de texto en gráficos
import org.jfree.data.statistics.HistogramDataset; // Importa dataset especializado para crear histogramas

import javax.swing.*; // Importa todos los componentes de interfaz gráfica Swing (botones, etiquetas, paneles, etc.)
import javax.swing.table.*; // Importa componentes especializados para trabajar con tablas en Swing
import java.awt.*; // Importa componentes gráficos y layouts de AWT (Abstract Window Toolkit)
import java.text.DecimalFormat; // Importa clase para formatear números con patrones específicos (decimales, separadores)
import java.util.*; // Importa utilidades generales de Java (Random, Arrays, Collections, etc.)
import java.util.List; // Importa específicamente la interfaz List para listas
import java.util.concurrent.*; // Importa clases para programación concurrente y multithreading (ExecutorService, Future, etc.)

public class InventorySystemSimulatorAleatorio extends JFrame { // Declara clase pública que extiende JFrame para crear ventana principal - versión ALEATORIA con parámetros editables

    private static class Config { // Declara clase interna estática para almacenar todas las constantes de configuración del problema
        static double COSTO_PEDIDO = 50.0; // Define costo fijo por realizar un pedido: $50 - AHORA MUTABLE (sin final) para poder cambiar en tiempo de ejecución
        static double COSTO_TENENCIA = 0.20; // Define costo de mantener una unidad en inventario por semana: $0.20 - AHORA MUTABLE
        static double COSTO_VENTAS_PERDIDAS = 100.0; // Define costo por cada unidad de demanda no satisfecha: $100 - AHORA MUTABLE
        static final int PLAZO_ENTREGA = 2; // Define tiempo de entrega del pedido en semanas: 2 (permanece fijo)
        static final int DEMANDA_MEDIA = 100; // Define demanda promedio por semana para distribución de Poisson: 100 unidades (permanece fijo)
        static final int NUM_SEMANAS = 52; // Define número de semanas a simular: 52 (un año completo)

        static final int ORDEN_MIN = 200; // Define cantidad mínima de pedido a probar en optimización: 200 unidades
        static final int ORDEN_MAX = 400; // Define cantidad máxima de pedido a probar en optimización: 400 unidades
        static final int ORDEN_PASO = 5; // Define incremento entre cantidades de pedido a probar: cada 5 unidades (200, 205, 210, ..., 400)
        static final int REORDEN_MIN = 200; // Define punto de reorden mínimo a probar en optimización: 200 unidades
        static final int REORDEN_MAX = 400; // Define punto de reorden máximo a probar en optimización: 400 unidades
        static final int REORDEN_PASO = 10; // Define incremento entre puntos de reorden a probar: cada 10 unidades (200, 210, 220, ..., 400)

        static final int NUM_SIMULACIONES = ((ORDEN_MAX - ORDEN_MIN) / ORDEN_PASO + 1) * ((REORDEN_MAX - REORDEN_MIN) / REORDEN_PASO + 1); // Calcula número total de combinaciones a simular dinámicamente: (400-200)/5+1 * (400-200)/10+1 = 41 * 21 = 861 simulaciones
        static final int NUM_PRUEBAS_MC = 5000; // Define número de pruebas Monte Carlo por cada combinación de parámetros: 5000 repeticiones para obtener promedio estadísticamente significativo
    }

    private static final DecimalFormat FMT_NUMBER = new DecimalFormat("#,##0.00"); // Define formato para números decimales con 2 decimales y separador de miles (ejemplo: 1,234.56)
    private static final DecimalFormat FMT_INT = new DecimalFormat("#,##0"); // Define formato para números enteros con separador de miles (ejemplo: 1,234)

    private DefaultTableModel modeloTabla; // Declara variable para el modelo de datos de la tabla principal que muestra las 52 semanas
    private JTextField txtCantidadPedido, txtPuntoReorden, txtInventarioInicial; // Declara campos de texto para mostrar/editar los tres parámetros principales del sistema
    private JTextField txtCostoPedido, txtCostoTenencia, txtCostoVentasPerdidas; // Declara campos de texto para mostrar/editar los tres costos del problema (EDITABLES en esta versión)
    private JLabel lblCostoAlmacenamiento, lblCostoPedido, lblCostoFaltante, lblCostoTotal; // Declara etiquetas para mostrar los cuatro costos totales anuales
    private JProgressBar progressBar, progressBarPruebas; // Declara dos barras de progreso: una para simulaciones y otra para pruebas Monte Carlo
    private JLabel lblSimulaciones, lblPruebas; // Declara etiquetas para mostrar el texto descriptivo de las barras de progreso
    private JButton btnOptimizar, btnActualizar, btnGenerarAleatorio; // Declara cuatro botones principales: OPTIMIZAR, ACTUALIZAR, GENERAR ALEATORIO y VER GRÁFICAS

    private double mejorCosto = Double.POSITIVE_INFINITY; // Inicializa mejor costo encontrado en infinito positivo (para algoritmo de minimización)
    private int mejorCantidadPedido = 250; // Inicializa mejor cantidad de pedido encontrada en 250 unidades (valor por defecto inicial)
    private int mejorPuntoReorden = 250; // Inicializa mejor punto de reorden encontrado en 250 unidades (valor por defecto inicial)
    private List<Double> costosFinales = new ArrayList<>(); // Inicializa lista vacía para almacenar todos los costos de las 5000 pruebas de la mejor solución (para construir histograma)

    private int cantidadPedidoActual = 250; // Inicializa cantidad de pedido actualmente en uso para simulación: 250 unidades
    private int puntoReordenActual = 250; // Inicializa punto de reorden actualmente en uso para simulación: 250 unidades

    public InventorySystemSimulatorAleatorio() { // Constructor de la clase - se ejecuta al crear una nueva instancia
        super("Simulación de inventario con ventas perdidas"); // Llama al constructor de JFrame estableciendo el título de la ventana
        configurarUI(); // Llama al método que configura toda la interfaz de usuario
        simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual); // Ejecuta simulación inicial con valores por defecto (250, 250) y muestra resultados en tabla
        setSize(1600, 900); // Establece el tamaño de la ventana: 1600 píxeles de ancho x 900 de alto (más alto que versión fija para acomodar nuevos botones)
        setLocationRelativeTo(null); // Centra la ventana en la pantalla (null indica centrar respecto a pantalla completa)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Define que al cerrar la ventana se termine la aplicación completamente
    }

    private void configurarUI() { // Método para configurar toda la estructura de la interfaz de usuario
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30); // Crea panel principal con BorderLayout (gaps de 15) y margen de 25 arriba/abajo, 30 izquierda/derecha
        main.add(crearTitulo(), BorderLayout.NORTH); // Agrega panel de título en la parte superior (zona NORTH del BorderLayout)

        JPanel centro = new JPanel(new BorderLayout(10, 10)); // Crea panel central con BorderLayout y gaps de 10 píxeles
        centro.setBackground(Color.WHITE); // Establece fondo blanco para el panel central
        centro.add(crearPanelSuperior(), BorderLayout.NORTH); // Agrega panel de parámetros en la parte superior del panel central
        centro.add(crearTabla(), BorderLayout.CENTER); // Agrega tabla de 52 semanas en el centro del panel central

        main.add(centro, BorderLayout.CENTER); // Agrega panel central completo al centro del panel principal

        JPanel sur = new JPanel(new BorderLayout(10, 10)); // Crea panel inferior con BorderLayout y gaps de 10 píxeles
        sur.setBackground(Color.WHITE); // Establece fondo blanco para el panel inferior
        sur.add(crearPanelTotales(), BorderLayout.NORTH); // Agrega panel de costos totales en la parte superior del panel inferior
        sur.add(crearPanelControl(), BorderLayout.CENTER); // Agrega panel de controles (botones y barras de progreso) en el centro del panel inferior

        main.add(sur, BorderLayout.SOUTH); // Agrega panel inferior completo en la parte inferior del panel principal
        add(main); // Agrega panel principal a la ventana (JFrame) para que se muestre todo
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) { // Método auxiliar para crear panel con layout y margen personalizados - recibe layout, margen superior y margen lateral
        JPanel panel = new JPanel(layout); // Crea nuevo panel con el layout manager especificado (BorderLayout, GridLayout, etc.)
        panel.setBackground(Color.WHITE); // Establece fondo blanco para el panel
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left)); // Crea borde vacío (invisible) con margen: top píxeles arriba/abajo, left píxeles izquierda/derecha
        return panel; // Retorna el panel configurado
    }

    private JPanel crearTitulo() { // Método para crear panel que contiene el título principal de la aplicación
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel con FlowLayout alineado a la izquierda (componentes se alinean uno tras otro de izquierda a derecha)
        panel.setBackground(Color.WHITE); // Establece fondo blanco para el panel del título
        JLabel titulo = new JLabel("Simulación de inventario con ventas perdidas"); // Crea etiqueta con el texto del título de la aplicación
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Establece fuente Segoe UI, estilo negrita, tamaño 24 puntos (grande para título)
        titulo.setForeground(new Color(31, 78, 120)); // Establece color de texto azul oscuro personalizado usando valores RGB (31 rojo, 78 verde, 120 azul)
        panel.add(titulo); // Agrega la etiqueta del título al panel
        return panel; // Retorna el panel con el título configurado
    }

    private JPanel crearPanelSuperior() { // Método para crear panel superior que contiene los parámetros del problema
        JPanel panel = new JPanel(new BorderLayout()); // Crea panel con BorderLayout sin gaps
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0)); // Crea margen: 10 píxeles arriba, 0 lados, 15 píxeles abajo

        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5)); // Crea panel de información con GridLayout de 2 filas x 1 columna, gaps de 5 píxeles
        info.setBackground(Color.WHITE); // Establece fondo blanco para panel de información

        JLabel lblObjetivo = new JLabel("Optimizar la cantidad de pedidos y el punto de reorden..."); // Crea etiqueta con primera línea del objetivo del problema
        lblObjetivo.setFont(new Font("Calibri", Font.PLAIN, 14)); // Establece fuente Calibri, estilo normal, tamaño 14 puntos
        lblObjetivo.setForeground(new Color(50, 100, 150)); // Establece color azul para el texto usando valores RGB personalizados

        JLabel lblObjetivo2 = new JLabel("...para minimizar costos"); // Crea etiqueta con segunda línea del objetivo (continuación)
        lblObjetivo2.setFont(new Font("Calibri", Font.PLAIN, 14)); // Establece misma fuente que línea anterior
        lblObjetivo2.setForeground(new Color(50, 100, 150)); // Establece mismo color azul que línea anterior

        info.add(lblObjetivo); // Agrega primera línea del objetivo al panel de información
        info.add(lblObjetivo2); // Agrega segunda línea del objetivo al panel de información

        JPanel parametros = new JPanel(new GridLayout(4, 4, 10, 5)); // Crea panel de parámetros con GridLayout de 4 filas x 4 columnas, gap horizontal 10, gap vertical 5
        parametros.setBackground(Color.WHITE); // Establece fondo blanco para panel de parámetros

        parametros.add(crearLabelParametro("Cantidad de pedido")); // Agrega etiqueta "Cantidad de pedido" alineada a la derecha
        txtCantidadPedido = crearTextField("250", new Color(255, 255, 0)); // Crea campo de texto EDITABLE con valor inicial "250" y fondo amarillo (indica variable de decisión) - GUARDA REFERENCIA
        parametros.add(txtCantidadPedido); // Agrega campo de cantidad de pedido al grid
        parametros.add(crearLabelParametro("Costo del pedido")); // Agrega etiqueta "Costo del pedido" alineada a la derecha
        txtCostoPedido = crearTextField("50"); // Crea campo de texto EDITABLE con valor inicial "50" y fondo blanco - GUARDA REFERENCIA
        parametros.add(txtCostoPedido); // Agrega campo de costo de pedido al grid

        parametros.add(crearLabelParametro("Punto de reorden")); // Agrega etiqueta "Punto de reorden" alineada a la derecha
        txtPuntoReorden = crearTextField("250", new Color(255, 255, 0)); // Crea campo de texto EDITABLE con valor inicial "250" y fondo amarillo (indica variable de decisión) - GUARDA REFERENCIA
        parametros.add(txtPuntoReorden); // Agrega campo de punto de reorden al grid
        parametros.add(crearLabelParametro("Costo de tenencia")); // Agrega etiqueta "Costo de tenencia" alineada a la derecha
        txtCostoTenencia = crearTextField("0.20"); // Crea campo de texto EDITABLE con valor inicial "0.20" y fondo blanco - GUARDA REFERENCIA
        parametros.add(txtCostoTenencia); // Agrega campo de costo de tenencia al grid

        parametros.add(crearLabelParametro("Inventario inicial")); // Agrega etiqueta "Inventario inicial" alineada a la derecha
        txtInventarioInicial = crearTextField("250"); // Crea campo de texto con valor inicial "250" y fondo blanco - GUARDA REFERENCIA
        txtInventarioInicial.setEditable(false); // Hace el campo NO EDITABLE porque se calcula automáticamente (siempre igual a cantidad de pedido)
        parametros.add(txtInventarioInicial); // Agrega campo de inventario inicial al grid
        parametros.add(crearLabelParametro("Costo de ventas perdidas")); // Agrega etiqueta "Costo de ventas perdidas" alineada a la derecha
        txtCostoVentasPerdidas = crearTextField("100"); // Crea campo de texto EDITABLE con valor inicial "100" y fondo blanco - GUARDA REFERENCIA
        parametros.add(txtCostoVentasPerdidas); // Agrega campo de costo de ventas perdidas al grid

        parametros.add(crearLabelParametro("plazo de entrega")); // Agrega etiqueta "plazo de entrega" alineada a la derecha
        parametros.add(crearLabelValor("2 semanas", Color.WHITE)); // Agrega etiqueta fija (NO EDITABLE) con valor "2 semanas" y fondo blanco
        parametros.add(new JLabel()); // Agrega etiqueta vacía para llenar celda del grid
        parametros.add(new JLabel()); // Agrega otra etiqueta vacía para llenar celda del grid

        JPanel derecha = new JPanel(new BorderLayout()); // Crea panel derecho con BorderLayout
        derecha.setBackground(Color.WHITE); // Establece fondo blanco para panel derecho
        derecha.add(parametros, BorderLayout.NORTH); // Agrega panel de parámetros en la parte superior del panel derecho

        panel.add(info, BorderLayout.WEST); // Agrega panel de información a la izquierda del panel principal superior
        panel.add(derecha, BorderLayout.CENTER); // Agrega panel derecho al centro del panel principal superior

        return panel; // Retorna el panel superior completo configurado
    }

    private JLabel crearLabelParametro(String texto) { // Método para crear etiqueta de nombre de parámetro - recibe el texto a mostrar
        JLabel lbl = new JLabel(texto); // Crea nueva etiqueta con el texto especificado
        lbl.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente Calibri, estilo normal, tamaño 11 puntos (pequeño para etiquetas)
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea el texto a la derecha (para que quede cerca del valor correspondiente)
        return lbl; // Retorna la etiqueta configurada
    }

    private JLabel crearLabelValor(String texto, Color bg) { // Método para crear etiqueta de valor fijo (no editable) - recibe texto y color de fondo
        JLabel lbl = new JLabel(texto); // Crea nueva etiqueta con el texto especificado
        lbl.setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente Calibri, estilo negrita, tamaño 11 puntos
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea el texto al centro horizontalmente
        lbl.setBackground(bg); // Establece el color de fondo especificado
        lbl.setOpaque(true); // Hace la etiqueta opaca para que el color de fondo sea visible
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel alrededor de la etiqueta
        return lbl; // Retorna la etiqueta configurada
    }

    private JTextField crearTextField(String valor) { // Sobrecarga del método para crear campo de texto con fondo blanco por defecto - recibe solo el valor inicial
        return crearTextField(valor, Color.WHITE); // Llama al método completo con fondo blanco
    }

    private JTextField crearTextField(String valor, Color bg) { // Método completo para crear campo de texto editable - recibe valor inicial y color de fondo
        JTextField txt = new JTextField(valor); // Crea nuevo campo de texto con el valor inicial especificado
        txt.setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente Calibri, estilo negrita, tamaño 11 puntos
        txt.setHorizontalAlignment(SwingConstants.CENTER); // Alinea el texto al centro horizontalmente dentro del campo
        txt.setBackground(bg); // Establece el color de fondo especificado (amarillo para variables de decisión, blanco para otros)
        txt.setOpaque(true); // Hace el campo opaco para que el color de fondo sea visible
        txt.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel alrededor del campo
        return txt; // Retorna el campo de texto configurado
    }

    private JScrollPane crearTabla() { // Método para crear la tabla principal que muestra los datos de las 52 semanas
        String[] cols = {"Semana", "Posición de\ninventario", "Inventario\ninicial", "Pedido\nrecibido", "Unidades\nrecibidas", "Demanda", "Inventario\nfinal", "Ventas\nperdidas", "¿Pedido\nrealizado?", "Posición\ninventario\nfinal", "Semana\nvencimiento", "Costo de\nalmacenamiento", "Costo del\npedido", "Costo por\nfaltante", "Costo\ntotal"}; // Define array con nombres de las 15 columnas de la tabla (usa \n para dividir texto en múltiples líneas en encabezado)

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea modelo de tabla con las columnas especificadas y 0 filas iniciales - sobrescribe clase anónima
            public boolean isCellEditable(int r, int c) { return false; } // Sobrescribe método para hacer todas las celdas NO EDITABLES (retorna false siempre)
        };

        JTable tabla = new JTable(modeloTabla); // Crea tabla visual usando el modelo de datos creado
        configurarEstiloTabla(tabla); // Llama a método para configurar colores, fuentes y estilos de la tabla

        JScrollPane scroll = new JScrollPane(tabla); // Crea panel con barras de desplazamiento que contiene la tabla (permite ver todas las 52 filas)
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Agrega borde gris claro alrededor del scroll
        return scroll; // Retorna el scroll con la tabla dentro
    }

    private void configurarEstiloTabla(JTable tabla) { // Método para configurar todos los estilos visuales de la tabla - recibe la tabla a configurar
        tabla.setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente de las celdas: Calibri, estilo normal, tamaño 10 puntos (pequeño para que quepa mucha información)
        tabla.setRowHeight(25); // Establece altura de las filas en 25 píxeles
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 10)); // Establece fuente del encabezado: Calibri, estilo negrita, tamaño 10 puntos
        tabla.getTableHeader().setBackground(new Color(79, 129, 189)); // Establece color de fondo del encabezado: azul corporativo usando valores RGB
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece color de texto del encabezado: blanco para contraste con fondo azul
        tabla.setGridColor(new Color(200, 200, 200)); // Establece color de las líneas de cuadrícula: gris claro usando valores RGB

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado de celdas - clase anónima que sobrescribe DefaultTableCellRenderer
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Sobrescribe método de renderizado - recibe tabla, valor, si está seleccionada, si tiene foco, fila y columna
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al renderizado base de la superclase
                setHorizontalAlignment(SwingConstants.CENTER); // Alinea el contenido de la celda al centro horizontalmente
                setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente de la celda: Calibri, normal, tamaño 10

                if (c == 5) { // Si es la columna 5 (columna de "Demanda" - índice 0-based)
                    setBackground(new Color(146, 208, 80)); // Establece fondo verde claro usando RGB (diferente al verde brillante de la versión fija)
                } else if (c == 8) { // Si es la columna 8 (columna de "¿Pedido realizado?")
                    setBackground(Color.WHITE); // Establece fondo blanco
                } else { // Para todas las demás columnas
                    setBackground(Color.WHITE); // Establece fondo blanco
                }

                setForeground(Color.BLACK); // Establece color de texto negro para todas las celdas
                return this; // Retorna el componente renderizado
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre todas las columnas de la tabla (de 0 a 14)
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica el renderizador personalizado a cada columna
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 60 : 85); // Establece ancho preferido: 60 píxeles para columna 0 ("Semana"), 85 píxeles para las demás
        }
    }

    private JPanel crearPanelTotales() { // Método para crear panel que muestra los costos totales anuales
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0)); // Crea panel con GridLayout de 1 fila x 5 columnas (cambió de 4 a 5 respecto a versión fija), gap horizontal 15, gap vertical 0
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Crea margen: 15 píxeles arriba, 0 lados, 10 píxeles abajo

        JLabel titulo = new JLabel("Costos anuales totales"); // Crea etiqueta con título de la sección
        titulo.setFont(new Font("Calibri", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        panel.add(titulo); // Agrega título al panel

        lblCostoAlmacenamiento = crearLabelTotal("$ 1.040"); // Crea y guarda referencia a etiqueta de costo de almacenamiento con valor inicial por defecto
        lblCostoPedido = crearLabelTotal("$ 1.050"); // Crea y guarda referencia a etiqueta de costo de pedidos con valor inicial por defecto
        lblCostoFaltante = crearLabelTotal("$ 5.000"); // Crea y guarda referencia a etiqueta de costo por faltante con valor inicial por defecto
        lblCostoTotal = crearLabelTotal("$ 7.090", new Color(0, 255, 255)); // Crea y guarda referencia a etiqueta de costo total con valor inicial por defecto y fondo cian (destaca el total)

        panel.add(lblCostoAlmacenamiento); // Agrega etiqueta de costo almacenamiento al panel
        panel.add(lblCostoPedido); // Agrega etiqueta de costo pedido al panel
        panel.add(lblCostoFaltante); // Agrega etiqueta de costo faltante al panel
        panel.add(lblCostoTotal); // Agrega etiqueta de costo total al panel

        return panel; // Retorna el panel completo con todos los costos
    }

    private JLabel crearLabelTotal(String valor) { // Sobrecarga del método para crear etiqueta de total con fondo blanco por defecto - recibe solo el valor
        return crearLabelTotal(valor, Color.WHITE); // Llama al método completo con fondo blanco
    }

    private JLabel crearLabelTotal(String valor, Color bg) { // Método completo para crear etiqueta de total con valor y color de fondo personalizados
        JLabel lbl = new JLabel(valor); // Crea etiqueta con el valor especificado (incluye símbolo $ y número)
        lbl.setFont(new Font("Calibri", Font.BOLD, 14)); // Establece fuente negrita tamaño 14 (más grande que el resto)
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea texto al centro horizontalmente
        lbl.setBackground(bg); // Establece color de fondo especificado (blanco o cian)
        lbl.setForeground(Color.BLACK); // Establece color de texto negro
        lbl.setOpaque(true); // Hace la etiqueta opaca para mostrar el color de fondo
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel
        return lbl; // Retorna la etiqueta configurada
    }

    private JPanel crearPanelControl() { // Método para crear panel de controles (botones y barras de progreso)
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // Crea panel con GridLayout de 2 filas x 1 columna, gaps de 10 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Crea margen: 15 píxeles arriba, 0 lados, 10 píxeles abajo

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Crea panel para botones con FlowLayout centrado, gap horizontal 15, gap vertical 0
        panelBtns.setBackground(Color.WHITE); // Establece fondo blanco para panel de botones

        btnGenerarAleatorio = crearBoton("Generar Aleatorio", new Color(255, 165, 0), 170, 40); // Crea botón GENERAR ALEATORIO con color naranja, ancho 170, alto 40 - GUARDA REFERENCIA
        btnActualizar = crearBoton("Actualizar Tabla", new Color(237, 125, 49), 170, 40); // Crea botón ACTUALIZAR TABLA con color naranja oscuro, ancho 170, alto 40 - GUARDA REFERENCIA
        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 280, 45); // Crea botón OPTIMIZAR con color azul, ancho 280, alto 45 - GUARDA REFERENCIA

        btnGenerarAleatorio.addActionListener(e -> generarDatosAleatorios()); // Agrega listener al botón generar: al hacer clic ejecuta método generarDatosAleatorios()
        btnActualizar.addActionListener(e -> actualizarTablaConValoresActuales()); // Agrega listener al botón actualizar: al hacer clic ejecuta método actualizarTablaConValoresActuales()
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Agrega listener al botón optimizar: al hacer clic ejecuta método ejecutarOptimizacion()

        panelBtns.add(btnGenerarAleatorio); // Agrega botón generar al panel de botones
        panelBtns.add(btnActualizar); // Agrega botón actualizar al panel de botones
        panelBtns.add(btnOptimizar); // Agrega botón optimizar al panel de botones

        JPanel panelProgress = crearPanelProgreso(); // Crea panel con las dos barras de progreso

        panel.add(panelBtns); // Agrega panel de botones a la fila superior del panel de control
        panel.add(panelProgress); // Agrega panel de progreso a la fila inferior del panel de control
        return panel; // Retorna el panel de control completo
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar para crear botón personalizado - recibe texto, color de fondo, ancho y alto
        JButton btn = new JButton(texto); // Crea nuevo botón con el texto especificado
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 20 ? 13 : 14)); // Establece fuente negrita, tamaño 13 si texto largo (>20 caracteres), 14 si corto
        btn.setBackground(bg); // Establece color de fondo especificado (naranja, azul, verde según tipo de botón)
        btn.setForeground(Color.WHITE); // Establece color de texto blanco para contraste con fondo de color
        btn.setFocusPainted(false); // Desactiva el borde de foco que aparece al hacer clic (estética)
        btn.setPreferredSize(new Dimension(ancho, alto)); // Establece tamaño preferido del botón en píxeles
        return btn; // Retorna el botón configurado
    }

    private JPanel crearPanelProgreso() { // Método para crear panel que contiene las dos barras de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea panel con BorderLayout y gaps de 10 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest")); // Crea borde con título "Panel de control: OptQuest" y línea gris

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8)); // Crea panel para contener las dos barras con GridLayout de 2 filas x 1 columna, gap horizontal 5, gap vertical 8
        barras.setBackground(Color.WHITE); // Establece fondo blanco
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Crea margen: 8 píxeles arriba/abajo, 15 píxeles izquierda/derecha

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales", new Color(0, 32, 96), out -> lblSimulaciones = out); // Crea barra de progreso para simulaciones con máximo NUM_SIMULACIONES, texto descriptivo, color azul oscuro, guarda referencia de etiqueta
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas", new Color(0, 176, 80), out -> lblPruebas = out); // Crea barra de progreso para pruebas MC con máximo NUM_PRUEBAS_MC, texto descriptivo, color verde, guarda referencia de etiqueta

        barras.add(progressBar.getParent()); // Agrega panel contenedor de barra de simulaciones (getParent() obtiene el JPanel que contiene la barra)
        barras.add(progressBarPruebas.getParent()); // Agrega panel contenedor de barra de pruebas

        panel.add(barras); // Agrega panel de barras al panel principal de progreso (BorderLayout.CENTER por defecto)
        return panel; // Retorna el panel de progreso completo
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color, java.util.function.Consumer<JLabel> labelOut) { // Método para crear barra de progreso con etiqueta - recibe valor máximo, texto, color y consumer para devolver referencia de etiqueta
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel contenedor con BorderLayout y gaps de 5 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lbl = new JLabel(texto + ": 0 / " + max); // Crea etiqueta con texto descriptivo y contador inicial "0 / max"
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10)); // Establece fuente pequeña tamaño 10
        labelOut.accept(lbl); // Pasa la referencia de la etiqueta al consumer (permite actualizar el texto desde fuera de este método)

        JProgressBar bar = new JProgressBar(0, max); // Crea barra de progreso con rango de 0 a max (valor inicial 0)
        bar.setPreferredSize(new Dimension(500, 22)); // Establece tamaño preferido: 500 píxeles de ancho x 22 de alto
        bar.setForeground(color); // Establece color de la barra de progreso (azul oscuro o verde)
        bar.setBackground(Color.WHITE); // Establece color de fondo blanco

        panel.add(lbl, BorderLayout.WEST); // Agrega etiqueta a la izquierda del panel
        panel.add(bar, BorderLayout.CENTER); // Agrega barra de progreso al centro del panel
        panel.add(new JLabel(max + "  "), BorderLayout.EAST); // Agrega etiqueta con valor máximo a la derecha del panel (dos espacios para margen)

        return bar; // Retorna la barra de progreso (el panel se obtiene con bar.getParent() cuando se necesita)
    }

    private void generarDatosAleatorios() { // Método que genera valores aleatorios para todos los campos editables cuando se hace clic en botón "Generar Aleatorio"
        Random random = new Random(); // Crea generador de números aleatorios

        int cantidadPedido = Config.ORDEN_MIN + (random.nextInt((Config.ORDEN_MAX - Config.ORDEN_MIN) / Config.ORDEN_PASO + 1) * Config.ORDEN_PASO); // Genera cantidad de pedido aleatoria: entre 200 y 400 en múltiplos de 5 (ejemplo: 215, 290, 375)
        txtCantidadPedido.setText(String.valueOf(cantidadPedido)); // Establece el valor generado en el campo de texto de cantidad de pedido

        int puntoReorden = Config.REORDEN_MIN + (random.nextInt((Config.REORDEN_MAX - Config.REORDEN_MIN) / Config.REORDEN_PASO + 1) * Config.REORDEN_PASO); // Genera punto de reorden aleatorio: entre 200 y 400 en múltiplos de 10 (ejemplo: 230, 310, 380)
        txtPuntoReorden.setText(String.valueOf(puntoReorden)); // Establece el valor generado en el campo de texto de punto de reorden

        int costoPedido = 20 + random.nextInt(81); // Genera costo de pedido aleatorio: entre 20 y 100 (ENTERO) - nextInt(81) genera 0-80, sumar 20 da 20-100
        txtCostoPedido.setText(String.valueOf(costoPedido)); // Establece el valor generado en el campo de texto de costo pedido

        double costoTenencia = 0.10 + (random.nextDouble() * 0.40); // Genera costo de tenencia aleatorio: entre 0.10 y 0.50 (DECIMAL) - nextDouble() genera 0.0-1.0, multiplicar por 0.40 da 0.0-0.40, sumar 0.10 da 0.10-0.50
        txtCostoTenencia.setText(String.format("%.2f", costoTenencia)); // Establece el valor generado con formato de 2 decimales en el campo de texto de costo tenencia

        int costoVentasPerdidas = 50 + random.nextInt(151); // Genera costo de ventas perdidas aleatorio: entre 50 y 200 (ENTERO) - nextInt(151) genera 0-150, sumar 50 da 50-200
        txtCostoVentasPerdidas.setText(String.valueOf(costoVentasPerdidas)); // Establece el valor generado en el campo de texto de costo ventas perdidas

        txtInventarioInicial.setText(String.valueOf(cantidadPedido)); // Actualiza inventario inicial automáticamente para que sea igual a cantidad de pedido (regla del problema)

        Config.COSTO_PEDIDO = costoPedido; // Actualiza variable estática de configuración con nuevo costo de pedido
        Config.COSTO_TENENCIA = costoTenencia; // Actualiza variable estática de configuración con nuevo costo de tenencia
        Config.COSTO_VENTAS_PERDIDAS = costoVentasPerdidas; // Actualiza variable estática de configuración con nuevo costo de ventas perdidas

        cantidadPedidoActual = cantidadPedido; // Actualiza variable de instancia con nueva cantidad de pedido actual
        puntoReordenActual = puntoReorden; // Actualiza variable de instancia con nuevo punto de reorden actual

        simularYMostrarEnTabla(cantidadPedido, puntoReorden); // Simula automáticamente con los nuevos valores aleatorios y muestra resultados en tabla

        JOptionPane.showMessageDialog(this, String.format("Datos aleatorios generados:\n\nCantidad de pedido: %d\nPunto de reorden: %d\nCosto del pedido: $%d\nCosto de tenencia: $%.2f\nCosto de ventas perdidas: $%d", cantidadPedido, puntoReorden, costoPedido, costoTenencia, costoVentasPerdidas), "Datos Generados", JOptionPane.INFORMATION_MESSAGE); // Muestra ventana emergente con mensaje de confirmación mostrando todos los valores generados con formato
    }

    private void actualizarTablaConValoresActuales() { // Método que lee los valores de los campos de texto, los valida y actualiza la tabla cuando se hace clic en botón "Actualizar Tabla"
        try { // Inicia bloque try para capturar excepciones de formato de números
            String cantPedidoStr = txtCantidadPedido.getText().trim(); // Obtiene texto del campo cantidad de pedido y elimina espacios al inicio y final
            String puntoReordenStr = txtPuntoReorden.getText().trim(); // Obtiene texto del campo punto de reorden y elimina espacios
            String costoPedidoStr = txtCostoPedido.getText().trim(); // Obtiene texto del campo costo pedido y elimina espacios
            String costoTenenciaStr = txtCostoTenencia.getText().trim().replace(",", "."); // Obtiene texto del campo costo tenencia, elimina espacios y reemplaza coma por punto (acepta ambos separadores decimales)
            String costoVentasStr = txtCostoVentasPerdidas.getText().trim(); // Obtiene texto del campo costo ventas perdidas y elimina espacios

            if (cantPedidoStr.isEmpty() || puntoReordenStr.isEmpty() || costoPedidoStr.isEmpty() || costoTenenciaStr.isEmpty() || costoVentasStr.isEmpty()) { // Valida que ningún campo esté vacío
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos", "Campos Vacíos", JOptionPane.WARNING_MESSAGE); // Muestra mensaje de advertencia si hay campos vacíos
                return; // Sale del método sin continuar
            }

            cantidadPedidoActual = Integer.parseInt(cantPedidoStr); // Parsea (convierte) texto de cantidad de pedido a número entero
            puntoReordenActual = Integer.parseInt(puntoReordenStr); // Parsea texto de punto de reorden a número entero
            Config.COSTO_PEDIDO = Double.parseDouble(costoPedidoStr); // Parsea texto de costo pedido a número decimal (double)
            Config.COSTO_TENENCIA = Double.parseDouble(costoTenenciaStr); // Parsea texto de costo tenencia a número decimal
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(costoVentasStr); // Parsea texto de costo ventas perdidas a número decimal

            if (cantidadPedidoActual < 0 || puntoReordenActual < 0) { // Valida que cantidad de pedido y punto de reorden no sean negativos
                JOptionPane.showMessageDialog(this, "La cantidad de pedido y punto de reorden deben ser positivos", "Valores Inválidos", JOptionPane.WARNING_MESSAGE); // Muestra mensaje de advertencia si hay valores negativos
                return; // Sale del método sin continuar
            }

            if (Config.COSTO_PEDIDO < 0 || Config.COSTO_TENENCIA < 0 || Config.COSTO_VENTAS_PERDIDAS < 0) { // Valida que todos los costos no sean negativos
                JOptionPane.showMessageDialog(this, "Los costos deben ser positivos", "Valores Inválidos", JOptionPane.WARNING_MESSAGE); // Muestra mensaje de advertencia si hay costos negativos
                return; // Sale del método sin continuar
            }

            txtInventarioInicial.setText(String.valueOf(cantidadPedidoActual)); // Actualiza campo de inventario inicial para que sea igual a cantidad de pedido (regla del problema)

            simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual); // Simula sistema de inventario con valores actualizados y muestra resultados en tabla

        } catch (NumberFormatException ex) { // Captura excepción si algún texto no se puede convertir a número
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos.\n\nFormatos correctos:\n- Cantidad de pedido: 250 (entero)\n- Punto de reorden: 300 (entero)\n- Costo del pedido: 50 (entero)\n- Costo de tenencia: 0.20 o 0,20 (decimal)\n- Costo de ventas perdidas: 100 (entero)", "Error de Formato", JOptionPane.ERROR_MESSAGE); // Muestra mensaje de error detallado con ejemplos de formatos correctos
        }
    }

    private void ejecutarOptimizacion() { // Método principal que ejecuta el algoritmo de optimización completo usando múltiples threads para paralelizar el cálculo
        try { // Inicia bloque try para validar costos antes de empezar optimización
            String costoPedidoStr = txtCostoPedido.getText().trim(); // Obtiene texto del campo costo pedido y elimina espacios
            String costoTenenciaStr = txtCostoTenencia.getText().trim().replace(",", "."); // Obtiene texto del campo costo tenencia, elimina espacios y reemplaza coma por punto
            String costoVentasStr = txtCostoVentasPerdidas.getText().trim(); // Obtiene texto del campo costo ventas perdidas y elimina espacios

            if (costoPedidoStr.isEmpty() || costoTenenciaStr.isEmpty() || costoVentasStr.isEmpty()) { // Valida que campos de costos no estén vacíos
                JOptionPane.showMessageDialog(this, "Por favor complete todos los campos de costos antes de optimizar", "Campos Vacíos", JOptionPane.WARNING_MESSAGE); // Muestra advertencia si hay campos vacíos
                return; // Sale del método sin iniciar optimización
            }

            Config.COSTO_PEDIDO = Double.parseDouble(costoPedidoStr); // Parsea costo pedido a double y actualiza configuración
            Config.COSTO_TENENCIA = Double.parseDouble(costoTenenciaStr); // Parsea costo tenencia a double y actualiza configuración
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(costoVentasStr); // Parsea costo ventas perdidas a double y actualiza configuración

            if (Config.COSTO_PEDIDO < 0 || Config.COSTO_TENENCIA < 0 || Config.COSTO_VENTAS_PERDIDAS < 0) { // Valida que todos los costos sean positivos
                JOptionPane.showMessageDialog(this, "Los costos deben ser valores positivos", "Valores Inválidos", JOptionPane.WARNING_MESSAGE); // Muestra advertencia si hay costos negativos
                return; // Sale del método sin iniciar optimización
            }

        } catch (NumberFormatException ex) { // Captura excepción si algún costo no se puede convertir a número
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos en los costos.\n\nFormatos correctos:\n- Costo del pedido: 50 (entero)\n- Costo de tenencia: 0.20 o 0,20 (decimal)\n- Costo de ventas perdidas: 100 (entero)", "Error de Formato", JOptionPane.ERROR_MESSAGE); // Muestra error con ejemplos de formatos correctos
            return; // Sale del método sin iniciar optimización
        }

        btnOptimizar.setEnabled(false); // Deshabilita botón optimizar durante la ejecución (evita múltiples ejecuciones simultáneas)
        costosFinales.clear(); // Limpia lista de costos finales de ejecuciones anteriores
        mejorCosto = Double.POSITIVE_INFINITY; // Reinicia mejor costo a infinito positivo (algoritmo de minimización busca valor menor)
        modeloTabla.setRowCount(0); // Limpia todas las filas de la tabla (vacía la tabla)

        new SwingWorker<Void, int[]>() { // Crea SwingWorker para ejecutar optimización en hilo de fondo sin bloquear la interfaz - tipo genérico <resultado final, resultados intermedios>
            protected Void doInBackground() { // Método ejecutado en hilo separado (no en hilo de interfaz gráfica)
                int numThreads = Runtime.getRuntime().availableProcessors(); // Obtiene número de procesadores disponibles en el sistema (típicamente 4, 8, 16, etc.)
                ExecutorService executor = Executors.newFixedThreadPool(numThreads); // Crea pool de threads con tamaño igual al número de procesadores para paralelizar cálculos

                List<Future<ResultadoSimulacion>> futures = new ArrayList<>(); // Crea lista para almacenar futuros (promesas de resultados que se obtendrán más adelante)
                int simCount = 0; // Inicializa contador de simulaciones en 0

                for (int cantPedido = Config.ORDEN_MIN; cantPedido <= Config.ORDEN_MAX; cantPedido += Config.ORDEN_PASO) { // Bucle externo: itera sobre cantidades de pedido desde 200 hasta 400 de 5 en 5
                    for (int puntoReorden = Config.REORDEN_MIN; puntoReorden <= Config.REORDEN_MAX; puntoReorden += Config.REORDEN_PASO) { // Bucle interno: itera sobre puntos de reorden desde 200 hasta 400 de 10 en 10
                        final int cp = cantPedido; // Crea variable final local con cantidad de pedido actual (necesaria para usar en lambda)
                        final int pr = puntoReorden; // Crea variable final local con punto de reorden actual (necesaria para usar en lambda)
                        final int currentSim = simCount; // Crea variable final local con número de simulación actual

                        Future<ResultadoSimulacion> future = executor.submit(() -> { // Envía tarea al executor para ejecutar en paralelo - lambda que retorna ResultadoSimulacion
                            List<Double> costosSim = new ArrayList<>(); // Crea lista temporal para almacenar costos de las 5000 pruebas Monte Carlo
                            Random rand = new Random(); // Crea generador de números aleatorios

                            for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) { // Bucle para ejecutar 5000 pruebas Monte Carlo con esta combinación de parámetros
                                double costoTotal = simularInventario(cp, pr, rand); // Simula inventario por 52 semanas con demanda aleatoria y obtiene costo total
                                costosSim.add(costoTotal); // Agrega costo a la lista

                                if (mc % 500 == 0) { // Cada 500 pruebas (0, 500, 1000, ..., 4500)
                                    publish(new int[]{currentSim + 1, mc + 1}); // Publica actualización de progreso a la interfaz (número de simulación y número de prueba)
                                }
                            }

                            double costoMedio = costosSim.stream().mapToDouble(d -> d).average().orElse(0); // Calcula costo medio de las 5000 pruebas usando streams
                            return new ResultadoSimulacion(cp, pr, costoMedio, costosSim); // Retorna resultado encapsulado en objeto ResultadoSimulacion
                        });

                        futures.add(future); // Agrega futuro a la lista (se procesará después)
                        simCount++; // Incrementa contador de simulaciones
                    }
                }

                for (Future<ResultadoSimulacion> future : futures) { // Procesa todos los resultados de las tareas enviadas al executor
                    try { // Bloque try para manejo de excepciones al obtener resultados
                        ResultadoSimulacion resultado = future.get(); // Obtiene resultado del futuro (bloquea hasta que la tarea termine si aún no ha terminado)

                        if (resultado.costoMedio < mejorCosto) { // Si el costo medio de esta simulación es mejor (menor) que el mejor encontrado hasta ahora
                            mejorCosto = resultado.costoMedio; // Actualiza mejor costo con este nuevo valor
                            mejorCantidadPedido = resultado.cantidadPedido; // Actualiza mejor cantidad de pedido
                            mejorPuntoReorden = resultado.puntoReorden; // Actualiza mejor punto de reorden
                            costosFinales = new ArrayList<>(resultado.costos); // Guarda copia de todos los 5000 costos de esta simulación para construir histograma después
                        }
                    } catch (Exception e) { // Captura cualquier excepción que ocurra al obtener resultado
                        e.printStackTrace(); // Imprime error en consola
                    }
                }

                executor.shutdown(); // Apaga el executor (no acepta más tareas, espera a que terminen las actuales)
                return null; // Retorna null (SwingWorker requiere retornar algo aunque no se use)
            }

            protected void process(List<int[]> chunks) { // Método ejecutado en hilo de interfaz gráfica para actualizar progreso - recibe lista de actualizaciones publicadas
                int[] ultimo = chunks.get(chunks.size() - 1); // Obtiene última actualización de la lista (la más reciente)
                progressBar.setValue(ultimo[0]); // Actualiza valor de barra de simulaciones con número de simulación actual
                progressBarPruebas.setValue(ultimo[1]); // Actualiza valor de barra de pruebas con número de prueba actual
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES); // Actualiza texto de etiqueta de simulaciones
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC); // Actualiza texto de etiqueta de pruebas
            }

            protected void done() { // Método ejecutado en hilo de interfaz gráfica cuando termina la optimización completa
                actualizarResultados(); // Actualiza interfaz con los mejores resultados encontrados
                btnOptimizar.setEnabled(true); // Habilita botón optimizar nuevamente
                mostrarVentanaResultados(); // Muestra ventana emergente con resumen de resultados
            }
        }.execute(); // Inicia la ejecución del SwingWorker
    }

    private static class ResultadoSimulacion { // Clase interna estática para encapsular resultado de una simulación completa (mantiene datos organizados)
        int cantidadPedido; // Cantidad de pedido usada en esta simulación
        int puntoReorden; // Punto de reorden usado en esta simulación
        double costoMedio; // Costo medio obtenido de las 5000 pruebas Monte Carlo
        List<Double> costos; // Lista con los 5000 costos individuales

        ResultadoSimulacion(int cp, int pr, double cm, List<Double> c) { // Constructor que recibe los cuatro valores
            this.cantidadPedido = cp; // Inicializa cantidad de pedido
            this.puntoReorden = pr; // Inicializa punto de reorden
            this.costoMedio = cm; // Inicializa costo medio
            this.costos = c; // Inicializa lista de costos
        }
    }

    private double simularInventario(int cantidadPedido, int puntoReorden, Random rand) { // Método que simula el sistema de inventario por 52 semanas con demanda aleatoria y retorna costo total - recibe cantidad de pedido, punto de reorden y generador aleatorio
        PoissonDistribution poissonDist = new PoissonDistribution(Config.DEMANDA_MEDIA); // Crea distribución de Poisson con media 100 para generar demanda aleatoria cada semana

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar posición de inventario de cada semana (índices 1 a 52, índice 0 no se usa)
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar inventario inicial de cada semana
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar unidades recibidas de pedidos previos cada semana
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar demanda generada aleatoriamente cada semana
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar inventario final de cada semana
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar ventas perdidas (demanda no satisfecha) cada semana
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array booleano para indicar si se realizó pedido cada semana
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para almacenar posición de inventario final de cada semana
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para registrar en qué semana vence (llega) cada pedido realizado

        double costoAlmacenamientoTotal = 0; // Inicializa acumulador de costo total de almacenamiento en 0
        double costoPedidoTotal = 0; // Inicializa acumulador de costo total de pedidos en 0
        double costoFaltanteTotal = 0; // Inicializa acumulador de costo total por faltantes en 0

        posicionInventario[1] = cantidadPedido; // SEMANA 1: Establece posición inicial igual a cantidad de pedido (inventario inicial del sistema)
        inventarioInicial[1] = cantidadPedido; // SEMANA 1: Establece inventario inicial igual a cantidad de pedido
        unidadesRecibidas[1] = 0; // SEMANA 1: No se reciben unidades en la primera semana (no hay pedidos previos)
        demanda[1] = poissonDist.sample(); // SEMANA 1: Genera demanda aleatoria usando distribución de Poisson

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1: Calcula inventario final = inicial + recibidas - demanda (Math.max asegura que no sea negativo)

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1: Calcula demanda satisfecha = mínimo entre demanda e inventario disponible (no podemos vender más de lo que tenemos)
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1; // SEMANA 1: Calcula ventas perdidas = demanda total - demanda satisfecha

        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1]; // SEMANA 1: Calcula posición después de satisfacer demanda = posición inicial - demanda + ventas perdidas (las ventas perdidas se suman porque no se satisfacen)
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden; // SEMANA 1: Decide si realizar pedido: SÍ si posición después de demanda es menor o igual al punto de reorden

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1: Calcula posición final = posición después de demanda + cantidad de pedido (si se realizó pedido) o + 0 (si no se realizó)

        if (pedidoRealizado[1]) { // SEMANA 1: Si se realizó un pedido en esta semana
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1; // Registra que el pedido vencerá (llegará) en semana actual + plazo de entrega + 1 = semana 4 (1 + 2 + 1)
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1: Calcula costo de almacenamiento = inventario final * 0.20 (Math.max asegura no multiplicar negativos)
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1: Calcula costo de pedido = 50 si se realizó pedido, 0 si no
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1: Calcula costo por faltante = ventas perdidas * 100

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1: Acumula costo de almacenamiento al total
        costoPedidoTotal += costoPedido; // SEMANA 1: Acumula costo de pedido al total
        costoFaltanteTotal += costoFaltante; // SEMANA 1: Acumula costo de faltante al total

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Bucle que itera desde semana 2 hasta semana 52 (mismo proceso para cada semana)
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Establece posición inicial de esta semana = posición final de semana anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Establece inventario inicial de esta semana = inventario final de semana anterior

            int numArriving = 0; // Inicializa contador de pedidos que llegan esta semana en 0
            for (int s = 1; s < sem; s++) { // Revisa todas las semanas anteriores (desde 1 hasta semana actual - 1)
                if (semanaVencimiento[s] == sem) { // Si un pedido realizado en semana s vence (llega) en esta semana
                    numArriving++; // Incrementa contador de pedidos llegando
                }
            }
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas = número de pedidos llegando * cantidad por pedido
            demanda[sem] = poissonDist.sample(); // Genera demanda aleatoria para esta semana usando distribución de Poisson

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]); // Calcula inventario final (no negativo)

            int demandaSatisfecha = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]); // Calcula demanda satisfecha
            ventasPerdidas[sem] = demanda[sem] - demandaSatisfecha; // Calcula ventas perdidas

            int posicionDespuesDemanda = posicionInventario[sem] - demanda[sem] + ventasPerdidas[sem]; // Calcula posición después de demanda
            pedidoRealizado[sem] = posicionDespuesDemanda <= puntoReorden; // Decide si realizar pedido

            posicionInventarioFinal[sem] = posicionDespuesDemanda + (pedidoRealizado[sem] ? cantidadPedido : 0); // Calcula posición final

            if (pedidoRealizado[sem]) { // Si se realizó pedido esta semana
                semanaVencimiento[sem] = sem + Config.PLAZO_ENTREGA + 1; // Registra semana de vencimiento = semana actual + 2 + 1
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA; // Calcula costo de almacenamiento
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0; // Calcula costo de pedido
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS; // Calcula costo por faltante

            costoAlmacenamientoTotal += costoAlmacenamiento; // Acumula costo de almacenamiento
            costoPedidoTotal += costoPedido; // Acumula costo de pedido
            costoFaltanteTotal += costoFaltante; // Acumula costo por faltante
        }

        return costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal; // Retorna la suma de los tres costos totales de las 52 semanas
    }

    private void actualizarResultados() { // Método para actualizar interfaz con los mejores resultados encontrados por la optimización
        txtCantidadPedido.setText(String.valueOf(mejorCantidadPedido)); // Actualiza campo de cantidad de pedido con mejor valor encontrado
        txtPuntoReorden.setText(String.valueOf(mejorPuntoReorden)); // Actualiza campo de punto de reorden con mejor valor encontrado
        txtInventarioInicial.setText(String.valueOf(mejorCantidadPedido)); // Actualiza campo de inventario inicial (igual a mejor cantidad de pedido)

        cantidadPedidoActual = mejorCantidadPedido; // Actualiza variable de instancia con mejor cantidad de pedido
        puntoReordenActual = mejorPuntoReorden; // Actualiza variable de instancia con mejor punto de reorden

        simularYMostrarEnTabla(mejorCantidadPedido, mejorPuntoReorden); // Simula una vez más con los mejores parámetros encontrados y muestra resultados en tabla
    }

    private void simularYMostrarEnTabla(int cantidadPedido, int puntoReorden) { // Método que simula inventario y llena la tabla con datos detallados de las 52 semanas - usa demanda fija para visualización reproducible
        modeloTabla.setRowCount(0); // Limpia todas las filas existentes de la tabla (vacía la tabla)

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Crea array para datos de simulación (misma lógica que simularInventario pero con demanda fija y llenado de tabla)
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para inventario inicial
        boolean[] pedidoRecibido = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se recibió pedido (para mostrar en tabla)
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para unidades recibidas
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para demanda
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para inventario final
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para ventas perdidas
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se realizó pedido
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para posición final
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para semana de vencimiento

        double costoAlmacenamientoTotal = 0; // Inicializa acumulador de costo almacenamiento
        double costoPedidoTotal = 0; // Inicializa acumulador de costo pedidos
        double costoFaltanteTotal = 0; // Inicializa acumulador de costo faltantes

        final int fixedDemand = 100; // Define demanda fija de 100 unidades para todas las semanas (para que la tabla muestre valores reproducibles y no cambien cada vez)

        posicionInventario[1] = cantidadPedido; // SEMANA 1: Establece posición inicial (misma lógica que en simularInventario pero registrando datos para tabla)
        inventarioInicial[1] = cantidadPedido; // SEMANA 1: Establece inventario inicial
        pedidoRecibido[1] = false; // SEMANA 1: No se recibió pedido (primera semana)
        unidadesRecibidas[1] = 0; // SEMANA 1: Cero unidades recibidas
        demanda[1] = fixedDemand; // SEMANA 1: Usa demanda fija de 100 (en lugar de aleatoria)

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1: Calcula inventario final

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1: Calcula demanda satisfecha
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1; // SEMANA 1: Calcula ventas perdidas

        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1]; // SEMANA 1: Calcula posición después de demanda
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden; // SEMANA 1: Decide si realizar pedido

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1: Calcula posición final

        if (pedidoRealizado[1]) { // SEMANA 1: Si se realizó pedido
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1; // Registra semana de vencimiento
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1: Calcula costo almacenamiento
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1: Calcula costo pedido
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1: Calcula costo faltante

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1: Acumula costos
        costoPedidoTotal += costoPedido; // SEMANA 1: Acumula costos
        costoFaltanteTotal += costoFaltante; // SEMANA 1: Acumula costos

        modeloTabla.addRow(new Object[]{ // SEMANA 1: Agrega fila a la tabla con todos los datos de la semana 1 (array de objetos con 15 valores)
            "1", // Columna 1: Número de semana
            FMT_INT.format(posicionInventario[1]), // Columna 2: Posición de inventario formateada como entero
            FMT_INT.format(inventarioInicial[1]), // Columna 3: Inventario inicial formateado
            pedidoRecibido[1] ? "VERDADERO" : "FALSO", // Columna 4: Pedido recibido (texto VERDADERO o FALSO)
            FMT_INT.format(unidadesRecibidas[1]), // Columna 5: Unidades recibidas formateadas
            FMT_INT.format(demanda[1]), // Columna 6: Demanda formateada
            FMT_INT.format(inventarioFinal[1]), // Columna 7: Inventario final formateado
            FMT_INT.format(ventasPerdidas[1]), // Columna 8: Ventas perdidas formateadas
            pedidoRealizado[1] ? "VERDADERO" : "FALSO", // Columna 9: Pedido realizado (texto VERDADERO o FALSO)
            FMT_INT.format(posicionInventarioFinal[1]), // Columna 10: Posición final formateada
            semanaVencimiento[1] > 0 ? String.valueOf(semanaVencimiento[1]) : "", // Columna 11: Semana vencimiento (texto vacío si no hay pedido)
            "$    " + FMT_NUMBER.format(costoAlmacenamiento), // Columna 12: Costo almacenamiento con símbolo $ y formato decimal
            "$    " + FMT_NUMBER.format(costoPedido), // Columna 13: Costo pedido con símbolo $ y formato decimal
            "$    " + FMT_NUMBER.format(costoFaltante), // Columna 14: Costo faltante con símbolo $ y formato decimal
            "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante) // Columna 15: Costo total con símbolo $ y formato decimal
        });

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Bucle para procesar y agregar filas de semanas 2 a 52 (mismo proceso que semana 1)
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Posición inicial = posición final de semana anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Inventario inicial = inventario final de semana anterior

            int numArriving = 0; // Contador de pedidos llegando esta semana
            for (int s = 1; s < sem; s++) { // Revisa todas las semanas anteriores
                if (semanaVencimiento[s] == sem) { // Si un pedido vence esta semana
                    numArriving++; // Incrementa contador
                }
            }
            pedidoRecibido[sem] = numArriving > 0; // Marca true si llegó al menos un pedido
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas
            demanda[sem] = fixedDemand; // Usa demanda fija de 100

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]); // Calcula inventario final

            int demandaSatisfecha = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]); // Calcula demanda satisfecha
            ventasPerdidas[sem] = demanda[sem] - demandaSatisfecha; // Calcula ventas perdidas

            int posicionDespuesDemanda = posicionInventario[sem] - demanda[sem] + ventasPerdidas[sem]; // Calcula posición después de demanda
            pedidoRealizado[sem] = posicionDespuesDemanda <= puntoReorden; // Decide si realizar pedido

            posicionInventarioFinal[sem] = posicionDespuesDemanda + (pedidoRealizado[sem] ? cantidadPedido : 0); // Calcula posición final

            if (pedidoRealizado[sem]) { // Si se realizó pedido
                semanaVencimiento[sem] = sem + Config.PLAZO_ENTREGA + 1; // Registra semana de vencimiento
            }

            costoAlmacenamiento = Math.max(0, inventarioFinal[sem]) * Config.COSTO_TENENCIA; // Calcula costo almacenamiento
            costoPedido = pedidoRealizado[sem] ? Config.COSTO_PEDIDO : 0; // Calcula costo pedido
            costoFaltante = ventasPerdidas[sem] * Config.COSTO_VENTAS_PERDIDAS; // Calcula costo faltante

            costoAlmacenamientoTotal += costoAlmacenamiento; // Acumula costos
            costoPedidoTotal += costoPedido; // Acumula costos
            costoFaltanteTotal += costoFaltante; // Acumula costos

            modeloTabla.addRow(new Object[]{ // Agrega fila a la tabla con todos los datos de esta semana (mismo formato que semana 1)
                String.valueOf(sem), // Número de semana convertido a texto
                FMT_INT.format(posicionInventario[sem]), // Posición inventario formateada
                FMT_INT.format(inventarioInicial[sem]), // Inventario inicial formateado
                pedidoRecibido[sem] ? "VERDADERO" : "FALSO", // Pedido recibido (texto)
                FMT_INT.format(unidadesRecibidas[sem]), // Unidades recibidas formateadas
                FMT_INT.format(demanda[sem]), // Demanda formateada
                FMT_INT.format(inventarioFinal[sem]), // Inventario final formateado
                FMT_INT.format(ventasPerdidas[sem]), // Ventas perdidas formateadas
                pedidoRealizado[sem] ? "VERDADERO" : "FALSO", // Pedido realizado (texto)
                FMT_INT.format(posicionInventarioFinal[sem]), // Posición final formateada
                semanaVencimiento[sem] > 0 ? String.valueOf(semanaVencimiento[sem]) : "", // Semana vencimiento (vacío si no hay)
                "$    " + FMT_NUMBER.format(costoAlmacenamiento), // Costo almacenamiento formateado
                "$    " + FMT_NUMBER.format(costoPedido), // Costo pedido formateado
                "$    " + FMT_NUMBER.format(costoFaltante), // Costo faltante formateado
                "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante) // Costo total formateado
            });
        }

        lblCostoAlmacenamiento.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal)); // Actualiza etiqueta de costo almacenamiento total con formato
        lblCostoPedido.setText("$ " + FMT_NUMBER.format(costoPedidoTotal)); // Actualiza etiqueta de costo pedido total con formato
        lblCostoFaltante.setText("$ " + FMT_NUMBER.format(costoFaltanteTotal)); // Actualiza etiqueta de costo faltante total con formato
        lblCostoTotal.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal)); // Actualiza etiqueta de costo total general con formato
    }

    private void mostrarHistograma() { // Método para crear y mostrar histograma de distribución de costos - versión mejorada con fondo amarillo y eje secundario
        double[] datos = costosFinales.stream().mapToDouble(d -> d).toArray(); // Convierte lista de costos finales (List<Double>) a array de double[] usando streams

        if (datos.length == 0) { // Si no hay datos disponibles (no se ha ejecutado optimización)
            JOptionPane.showMessageDialog(this, "No hay datos para mostrar. Ejecute primero la optimización."); // Muestra mensaje de advertencia
            return; // Sale del método sin crear histograma
        }

        double media = Arrays.stream(datos).average().orElse(0); // Calcula media (promedio) de todos los costos usando streams

        HistogramDataset dataset = new HistogramDataset(); // Crea dataset especializado para histogramas
        dataset.addSeries("Total Annual Costs", datos, 50); // Agrega serie de datos con 50 bins (intervalos) para agrupar los datos

        JFreeChart chart = ChartFactory.createHistogram("Total Annual Costs", "Dollars", "Frecuencia", dataset, PlotOrientation.VERTICAL, true, true, false); // Crea histograma: título "Total Annual Costs", eje X "Dollars", eje Y "Frecuencia", orientación vertical, mostrar leyenda, mostrar tooltips, no generar URLs

        XYPlot plot = chart.getXYPlot(); // Obtiene plot (área de dibujo) del gráfico para configurarlo
        plot.setBackgroundPaint(new Color(255, 255, 204)); // Establece fondo AMARILLO CLARO usando RGB (diferencia con versión fija que usa blanco)
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Establece color gris claro para líneas de cuadrícula verticales (eje X)
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Establece color gris claro para líneas de cuadrícula horizontales (eje Y)

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtiene renderizador de barras del plot y lo castea a XYBarRenderer
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Establece color azul para las barras del histograma
        renderer.setShadowVisible(false); // Desactiva sombras en las barras (estética limpia)
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter()); // Usa pintor estándar de barras sin efectos 3D (estilo plano moderno)

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis(); // Obtiene eje X (dominio) y lo castea a NumberAxis
        domainAxis.setNumberFormatOverride(new DecimalFormat("$#,##0")); // Establece formato monetario para etiquetas del eje X (símbolo $ y separador de miles)
        domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10)); // Establece fuente Arial tamaño 10 para etiquetas del eje X

        NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis(); // Obtiene eje Y (rango - frecuencia) y lo castea a NumberAxis
        rangeAxis.setLabel("Frecuencia"); // Establece etiqueta "Frecuencia" para el eje Y
        rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 10)); // Establece fuente Arial tamaño 10 para etiquetas del eje Y
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits()); // Usa unidades enteras para el eje Y (0, 100, 200, etc. en lugar de decimales)

        NumberAxis rangeAxis2 = new NumberAxis("Probabilidad"); // Crea NUEVO EJE Y SECUNDARIO para mostrar probabilidad (característica adicional de esta versión)
        rangeAxis2.setTickLabelFont(new Font("Arial", Font.PLAIN, 10)); // Establece fuente para etiquetas del eje secundario
        double maxFreq = rangeAxis.getUpperBound(); // Obtiene valor máximo del eje Y primario (frecuencia máxima)
        rangeAxis2.setRange(0, maxFreq / datos.length); // Calcula rango de probabilidad dividiendo frecuencia máxima entre número total de datos
                rangeAxis2.setNumberFormatOverride(new DecimalFormat("0.00")); // Establece formato con 2 decimales para etiquetas del eje secundario (0.00, 0.05, 0.10, etc.)
        plot.setRangeAxis(1, rangeAxis2); // Establece el eje secundario como segundo eje Y (índice 1) del plot

        ValueMarker marker = new ValueMarker(media); // Crea marcador vertical en la posición de la media calculada
        marker.setPaint(Color.BLACK); // Establece color negro para la línea del marcador
        marker.setStroke(new BasicStroke(2.0f)); // Establece grosor de 2 píxeles para la línea del marcador
        marker.setLabel("Media = $" + FMT_NUMBER.format(media)); // Establece etiqueta con texto "Media = $" seguido del valor formateado
        marker.setLabelAnchor(RectangleAnchor.TOP_RIGHT); // Establece punto de anclaje de la etiqueta en esquina superior derecha del marcador
        marker.setLabelTextAnchor(TextAnchor.TOP_LEFT); // Establece punto de anclaje del texto en esquina superior izquierda (alineación del texto)
        marker.setLabelFont(new Font("Arial", Font.BOLD, 11)); // Establece fuente Arial negrita tamaño 11 para la etiqueta del marcador
        plot.addDomainMarker(marker); // Agrega el marcador vertical al plot (aparecerá sobre el histograma)

        chart.setBackgroundPaint(Color.WHITE); // Establece fondo blanco para todo el gráfico (área fuera del plot)
        chart.getTitle().setFont(new Font("Arial", Font.BOLD, 16)); // Establece fuente Arial negrita tamaño 16 para el título del gráfico

        JFrame frame = new JFrame("Previsión: Total Annual Costs"); // Crea nueva ventana con título "Previsión: Total Annual Costs"
        ChartPanel chartPanel = new ChartPanel(chart); // Crea panel especializado de JFreeChart que contiene el gráfico y permite interacción (zoom, guardar, etc.)
        chartPanel.setPreferredSize(new Dimension(900, 600)); // Establece tamaño preferido del panel: 900 píxeles de ancho x 600 de alto
        chartPanel.setBackground(Color.WHITE); // Establece fondo blanco para el panel del gráfico

        JPanel mainPanel = new JPanel(new BorderLayout()); // Crea panel principal con BorderLayout para organizar componentes
        mainPanel.setBackground(Color.WHITE); // Establece fondo blanco
        mainPanel.add(chartPanel, BorderLayout.CENTER); // Agrega panel del gráfico en el centro

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel para estadísticas con FlowLayout alineado a la izquierda
        statsPanel.setBackground(Color.WHITE); // Establece fondo blanco
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Crea margen de 10 píxeles en todos los lados

        JLabel lblStats = new JLabel(String.format("%,d pruebas                Vista de frecuencia                %,d mostrados", Config.NUM_PRUEBAS_MC, datos.length)); // Crea etiqueta con estadísticas: número de pruebas, tipo de vista y número de datos mostrados (formato con separador de miles)
        lblStats.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente Calibri normal tamaño 11
        statsPanel.add(lblStats); // Agrega etiqueta al panel de estadísticas

        mainPanel.add(statsPanel, BorderLayout.SOUTH); // Agrega panel de estadísticas en la parte inferior del panel principal

        frame.setContentPane(mainPanel); // Establece el panel principal como contenido de la ventana
        frame.pack(); // Ajusta el tamaño de la ventana automáticamente según el contenido
        frame.setLocationRelativeTo(this); // Centra la ventana respecto a la ventana principal del simulador
        frame.setVisible(true); // Hace visible la ventana del histograma
    }

    private void mostrarVentanaResultados() { // Método para crear y mostrar ventana emergente con resumen de resultados de la optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crea diálogo no modal (permite interactuar con ventana principal mientras está abierto) con título "Resultados de OptQuest"
        dlg.setLayout(new BorderLayout()); // Establece BorderLayout para el diálogo

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15); // Crea panel principal con BorderLayout y margen de 15 píxeles

        JPanel headerPanel = new JPanel(new BorderLayout()); // Crea panel para el encabezado
        headerPanel.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lblHeader = new JLabel(Config.NUM_SIMULACIONES + " simulaciones"); // Crea etiqueta mostrando número total de simulaciones ejecutadas
        lblHeader.setFont(new Font("Calibri", Font.BOLD, 14)); // Establece fuente Calibri negrita tamaño 14

        JLabel lblSubHeader = new JLabel("Vista de mejor solución"); // Crea etiqueta con subtítulo
        lblSubHeader.setFont(new Font("Calibri", Font.PLAIN, 12)); // Establece fuente Calibri normal tamaño 12

        headerPanel.add(lblHeader, BorderLayout.NORTH); // Agrega encabezado principal en la parte superior
        headerPanel.add(lblSubHeader, BorderLayout.CENTER); // Agrega subtítulo en el centro

        main.add(headerPanel, BorderLayout.NORTH); // Agrega panel de encabezado en la parte superior del panel principal

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 10)); // Crea panel para 3 secciones con GridLayout: 3 filas x 1 columna, gap horizontal 5, gap vertical 10
        tablas.setBackground(Color.WHITE); // Establece fondo blanco

        JPanel graficoPanel = new JPanel(new BorderLayout()); // Crea panel para gráfico de rendimiento (placeholder - no se implementa gráfico real)
        graficoPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de rendimiento")); // Crea borde con título "Gráfico de rendimiento"
        graficoPanel.setBackground(Color.WHITE); // Establece fondo blanco
        graficoPanel.setPreferredSize(new Dimension(600, 150)); // Establece tamaño preferido: 600 píxeles de ancho x 150 de alto

        JLabel lblGrafico = new JLabel("Mejores soluciones encontradas", SwingConstants.CENTER); // Crea etiqueta centrada con texto descriptivo
        lblGrafico.setFont(new Font("Calibri", Font.ITALIC, 11)); // Establece fuente Calibri itálica tamaño 11
        graficoPanel.add(lblGrafico, BorderLayout.CENTER); // Agrega etiqueta en el centro del panel de gráfico

        tablas.add(graficoPanel); // Agrega panel de gráfico a la primera fila

        tablas.add(crearSeccionResultado("Objetivos", "Valor", new String[]{"Minimizar el/la Media de Total Annual Costs"}, new String[]{"$ " + FMT_NUMBER.format(mejorCosto)})); // Agrega sección de objetivos mostrando el mejor costo encontrado formateado

        JPanel requisitosPanel = new JPanel(new BorderLayout()); // Crea panel para sección de requisitos
        requisitosPanel.setBorder(BorderFactory.createTitledBorder("Requisitos:")); // Crea borde con título "Requisitos:"
        requisitosPanel.setBackground(Color.WHITE); // Establece fondo blanco
        JLabel lblRequisitos = new JLabel("(requisitos opcionales en previsiones)"); // Crea etiqueta con texto entre paréntesis
        lblRequisitos.setFont(new Font("Calibri", Font.ITALIC, 10)); // Establece fuente Calibri itálica tamaño 10
        requisitosPanel.add(lblRequisitos, BorderLayout.CENTER); // Agrega etiqueta en el centro
        tablas.add(requisitosPanel); // Agrega panel de requisitos a la tercera fila

        main.add(tablas, BorderLayout.CENTER); // Agrega panel de secciones en el centro del panel principal

        JPanel inferior = new JPanel(new GridLayout(1, 2, 10, 0)); // Crea panel inferior con GridLayout: 1 fila x 2 columnas, gap horizontal 10
        inferior.setBackground(Color.WHITE); // Establece fondo blanco

        JPanel restriccionesPanel = new JPanel(new BorderLayout()); // Crea panel para sección de restricciones
        restriccionesPanel.setBorder(BorderFactory.createTitledBorder("Restricciones")); // Crea borde con título "Restricciones"
        restriccionesPanel.setBackground(Color.WHITE); // Establece fondo blanco

        JPanel restriccionesGrid = new JPanel(new GridLayout(1, 3, 5, 5)); // Crea grid para encabezados de restricciones: 1 fila x 3 columnas
        restriccionesGrid.setBackground(Color.WHITE); // Establece fondo blanco
        restriccionesGrid.add(new JLabel("")); // Agrega etiqueta vacía en primera columna
        restriccionesGrid.add(new JLabel("Lado izquierdo", SwingConstants.CENTER)); // Agrega encabezado "Lado izquierdo" centrado
        restriccionesGrid.add(new JLabel("Lado derecho", SwingConstants.CENTER)); // Agrega encabezado "Lado derecho" centrado

        restriccionesPanel.add(restriccionesGrid, BorderLayout.CENTER); // Agrega grid al panel de restricciones
        inferior.add(restriccionesPanel); // Agrega panel de restricciones a la primera columna del panel inferior

        JPanel variablesPanel = new JPanel(new BorderLayout()); // Crea panel para sección de variables de decisión
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables de decisión")); // Crea borde con título "Variables de decisión"
        variablesPanel.setBackground(Color.WHITE); // Establece fondo blanco

        String[][] varsData = {{"Order Quantity", String.valueOf(mejorCantidadPedido)}, {"Reorder Point", String.valueOf(mejorPuntoReorden)}}; // Crea matriz 2x2 con nombres y valores de las variables de decisión

        DefaultTableModel modeloVars = new DefaultTableModel(new String[]{"", "Valor"}, 0); // Crea modelo de tabla con 2 columnas: nombre vacío y "Valor", 0 filas iniciales
        for (String[] row : varsData) { // Itera sobre cada fila de datos
            modeloVars.addRow(row); // Agrega fila al modelo de tabla
        }

        JTable tablaVars = new JTable(modeloVars); // Crea tabla visual con el modelo
        tablaVars.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente Calibri normal tamaño 11
        tablaVars.setRowHeight(25); // Establece altura de filas en 25 píxeles
        tablaVars.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente Calibri negrita tamaño 11 para encabezado

        JScrollPane scrollVars = new JScrollPane(tablaVars); // Crea panel con scroll que contiene la tabla
        scrollVars.setPreferredSize(new Dimension(250, 80)); // Establece tamaño preferido: 250 píxeles de ancho x 80 de alto
        variablesPanel.add(scrollVars, BorderLayout.CENTER); // Agrega scroll al panel de variables

        inferior.add(variablesPanel); // Agrega panel de variables a la segunda columna del panel inferior

        main.add(inferior, BorderLayout.SOUTH); // Agrega panel inferior en la parte inferior del panel principal

        dlg.add(main); // Agrega panel principal al diálogo
        dlg.setSize(750, 550); // Establece tamaño del diálogo: 750 píxeles de ancho x 550 de alto
        dlg.setLocationRelativeTo(this); // Centra el diálogo respecto a la ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) { // Método para crear sección de resultados con tabla - recibe título, nombre de columna, array de filas y array de valores
        JPanel panel = new JPanel(new BorderLayout()); // Crea panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder(titulo)); // Crea borde con el título especificado
        panel.setBackground(Color.WHITE); // Establece fondo blanco

        if (filas.length > 0) { // Si hay al menos una fila de datos
            DefaultTableModel modelo = new DefaultTableModel(new String[]{"Objetivos", col}, 0); // Crea modelo de tabla con columnas "Objetivos" y el nombre de columna especificado, 0 filas iniciales
            for (int i = 0; i < filas.length; i++) { // Itera sobre todas las filas
                modelo.addRow(new Object[]{filas[i], vals[i]}); // Agrega fila con nombre y valor
            }

            JTable tabla = new JTable(modelo); // Crea tabla visual con el modelo
            tabla.setFont(new Font("Calibri", Font.PLAIN, 11)); // Establece fuente Calibri normal tamaño 11
            tabla.setRowHeight(25); // Establece altura de filas en 25 píxeles
            tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Establece fuente Calibri negrita tamaño 11 para encabezado

            JScrollPane scroll = new JScrollPane(tabla); // Crea panel con scroll que contiene la tabla
            scroll.setPreferredSize(new Dimension(600, 60)); // Establece tamaño preferido: 600 píxeles de ancho x 60 de alto
            panel.add(scroll, BorderLayout.CENTER); // Agrega scroll al panel en el centro
        }
        return panel; // Retorna el panel completo con la tabla
    }

    public static void main(String[] args) { // Método main - punto de entrada del programa cuando se ejecuta la aplicación
        try { // Bloque try para capturar excepciones al configurar Look and Feel
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece FlatLightLaf como Look and Feel (tema visual moderno y limpio)
        } catch (Exception e) { // Captura cualquier excepción que ocurra al configurar el tema
            e.printStackTrace(); // Imprime el stack trace del error en la consola (para debugging)
        }

        SwingUtilities.invokeLater(() -> new InventorySystemSimulatorAleatorio().setVisible(true)); // Ejecuta en el hilo de eventos de Swing (EDT - Event Dispatch Thread) la creación de la instancia del simulador y la hace visible - uso de expresión lambda
    }
}