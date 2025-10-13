package actividad_9.ejercicio_1; // Declaración del paquete donde reside la clase

import com.formdev.flatlaf.FlatLightLaf; // Importa tema visual moderno FlatLaf para interfaz gráfica
import org.apache.commons.math3.distribution.PoissonDistribution; // Importa distribución de Poisson para generar demanda aleatoria
import org.jfree.chart.*; // Importa todas las clases principales de JFreeChart para gráficos
import org.jfree.chart.plot.*; // Importa clases para configurar plots de gráficos
import javax.swing.*; // Importa todos los componentes de interfaz gráfica Swing
import javax.swing.table.*; // Importa componentes especializados para tablas
import java.awt.*; // Importa componentes gráficos y layouts de AWT
import java.text.DecimalFormat; // Importa clase para formatear números con patrones específicos
import java.util.*; // Importa utilidades generales (Random, Arrays, Collections)
import java.util.List; // Importa específicamente la interfaz List
import java.util.concurrent.*; // Importa clases para programación concurrente y multithreading

public class InventorySystemSimulatorEditable extends JFrame { // Declara clase pública que extiende JFrame - versión EDITABLE (sin botón generar aleatorio, más simple)

    private static class Config { // Clase interna estática para configuración del problema
        static double COSTO_PEDIDO = 50.0; // Costo por pedido - MUTABLE (no final) para cambiar en tiempo de ejecución
        static double COSTO_TENENCIA = 0.20; // Costo de tenencia por unidad por semana - MUTABLE
        static double COSTO_VENTAS_PERDIDAS = 100.0; // Costo por unidad de demanda no satisfecha - MUTABLE
        static final int PLAZO_ENTREGA = 2; // Plazo de entrega en semanas (fijo, no cambia)
        static final int DEMANDA_MEDIA = 100; // Demanda promedio por semana para distribución de Poisson (fijo)
        static final int NUM_SEMANAS = 52; // Número de semanas a simular: 52 (un año)

        static final int ORDEN_MIN = 200; // Cantidad mínima de pedido para optimización
        static final int ORDEN_MAX = 400; // Cantidad máxima de pedido para optimización
        static final int ORDEN_PASO = 5; // Incremento entre cantidades de pedido: cada 5 unidades
        static final int REORDEN_MIN = 200; // Punto de reorden mínimo para optimización
        static final int REORDEN_MAX = 400; // Punto de reorden máximo para optimización
        static final int REORDEN_PASO = 10; // Incremento entre puntos de reorden: cada 10 unidades

        static final int NUM_SIMULACIONES = ((ORDEN_MAX - ORDEN_MIN) / ORDEN_PASO + 1) * ((REORDEN_MAX - REORDEN_MIN) / REORDEN_PASO + 1); // Calcula número total de combinaciones: (400-200)/5+1 * (400-200)/10+1 = 41 * 21 = 861
        static final int NUM_PRUEBAS_MC = 5000; // Número de pruebas Monte Carlo por cada combinación
    }

    private static final DecimalFormat FMT_NUMBER = new DecimalFormat("#,##0.00"); // Formato para decimales con 2 decimales y separador de miles
    private static final DecimalFormat FMT_INT = new DecimalFormat("#,##0"); // Formato para enteros con separador de miles

    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla principal
    private JTextField txtCantidadPedido, txtPuntoReorden, txtInventarioInicial; // Campos de texto para parámetros principales
    private JTextField txtCostoPedido, txtCostoTenencia, txtCostoVentasPerdidas; // Campos de texto para costos (EDITABLES)
    private JLabel lblCostoAlmacenamiento, lblCostoPedido, lblCostoFaltante, lblCostoTotal; // Etiquetas para costos totales
    private JProgressBar progressBar, progressBarPruebas; // Barras de progreso
    private JLabel lblSimulaciones, lblPruebas; // Etiquetas descriptivas de progreso
    private JButton btnOptimizar, btnActualizar; // Dos botones: OPTIMIZAR y ACTUALIZAR (no hay generar aleatorio en esta versión)

    private double mejorCosto = Double.POSITIVE_INFINITY; // Mejor costo encontrado (infinito positivo inicial para minimizar)
    private int mejorCantidadPedido = 250; // Mejor cantidad de pedido encontrada
    private int mejorPuntoReorden = 250; // Mejor punto de reorden encontrado
    private List<Double> costosFinales = new ArrayList<>(); // Lista de costos de la mejor solución (para histograma)
    private List<Double> todosCostosMC = new ArrayList<>(); // NUEVA CARACTERÍSTICA: Lista con TODOS los costos de TODAS las simulaciones (no solo la mejor)

    private int cantidadPedidoActual = 250; // Cantidad de pedido actualmente en uso
    private int puntoReordenActual = 250; // Punto de reorden actualmente en uso

    public InventorySystemSimulatorEditable() { // Constructor
        super("Simulación de inventario con ventas perdidas"); // Título de la ventana
        configurarUI(); // Configura interfaz de usuario
        simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual); // Simula con valores iniciales por defecto
        setSize(1600, 900); // Tamaño de ventana: 1600x900 píxeles
        setLocationRelativeTo(null); // Centra ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra aplicación al cerrar ventana
    }

    private void configurarUI() { // Método para configurar toda la interfaz de usuario
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30); // Panel principal con BorderLayout y margen
        main.add(crearTitulo(), BorderLayout.NORTH); // Título arriba

        JPanel centro = new JPanel(new BorderLayout(10, 10)); // Panel central
        centro.setBackground(Color.WHITE); // Fondo blanco
        centro.add(crearPanelSuperior(), BorderLayout.NORTH); // Parámetros arriba
        centro.add(crearTabla(), BorderLayout.CENTER); // Tabla en el centro

        main.add(centro, BorderLayout.CENTER); // Agrega panel central

        JPanel sur = new JPanel(new BorderLayout(10, 10)); // Panel inferior
        sur.setBackground(Color.WHITE); // Fondo blanco
        sur.add(crearPanelTotales(), BorderLayout.NORTH); // Totales arriba
        sur.add(crearPanelControl(), BorderLayout.CENTER); // Controles en el centro

        main.add(sur, BorderLayout.SOUTH); // Agrega panel inferior
        add(main); // Agrega panel principal a la ventana
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) { // Crea panel con layout y margen personalizados
        JPanel panel = new JPanel(layout); // Crea panel con layout especificado
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left)); // Margen uniforme
        return panel; // Retorna panel
    }

    private JPanel crearTitulo() { // Crea panel con título principal
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // FlowLayout alineado a la izquierda
        panel.setBackground(Color.WHITE); // Fondo blanco
        JLabel titulo = new JLabel("Simulación de inventario con ventas perdidas"); // Texto del título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Fuente grande negrita
        titulo.setForeground(new Color(31, 78, 120)); // Color azul oscuro
        panel.add(titulo); // Agrega título al panel
        return panel; // Retorna panel
    }

    private JPanel crearPanelSuperior() { // Crea panel superior con parámetros del problema
        JPanel panel = new JPanel(new BorderLayout()); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0)); // Margen

        JPanel info = new JPanel(new GridLayout(2, 1, 5, 5)); // Panel de información con 2 filas
        info.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblObjetivo = new JLabel("Optimizar la cantidad de pedidos y el punto de reorden..."); // Primera línea del objetivo
        lblObjetivo.setFont(new Font("Calibri", Font.PLAIN, 14)); // Fuente
        lblObjetivo.setForeground(new Color(50, 100, 150)); // Color azul

        JLabel lblObjetivo2 = new JLabel("...para minimizar costos"); // Segunda línea del objetivo
        lblObjetivo2.setFont(new Font("Calibri", Font.PLAIN, 14)); // Fuente
        lblObjetivo2.setForeground(new Color(50, 100, 150)); // Color azul

        info.add(lblObjetivo); // Agrega primera línea
        info.add(lblObjetivo2); // Agrega segunda línea

        JPanel parametros = new JPanel(new GridLayout(4, 4, 10, 5)); // Panel de parámetros 4x4
        parametros.setBackground(Color.WHITE); // Fondo blanco

        parametros.add(crearLabelParametro("Cantidad de pedido")); // Etiqueta
        txtCantidadPedido = crearTextField("250", new Color(255, 255, 0)); // Campo EDITABLE con fondo amarillo
        parametros.add(txtCantidadPedido); // Agrega campo
        parametros.add(crearLabelParametro("Costo del pedido")); // Etiqueta
        txtCostoPedido = crearTextField("50"); // Campo EDITABLE
        parametros.add(txtCostoPedido); // Agrega campo

        parametros.add(crearLabelParametro("Punto de reorden")); // Etiqueta
        txtPuntoReorden = crearTextField("250", new Color(255, 255, 0)); // Campo EDITABLE con fondo amarillo
        parametros.add(txtPuntoReorden); // Agrega campo
        parametros.add(crearLabelParametro("Costo de tenencia")); // Etiqueta
        txtCostoTenencia = crearTextField("0.20"); // Campo EDITABLE
        parametros.add(txtCostoTenencia); // Agrega campo

        parametros.add(crearLabelParametro("Inventario inicial")); // Etiqueta
        txtInventarioInicial = crearTextField("250"); // Campo de texto
        txtInventarioInicial.setEditable(false); // NO EDITABLE (se calcula automáticamente = cantidad de pedido)
        parametros.add(txtInventarioInicial); // Agrega campo
        parametros.add(crearLabelParametro("Costo de ventas perdidas")); // Etiqueta
        txtCostoVentasPerdidas = crearTextField("100"); // Campo EDITABLE
        parametros.add(txtCostoVentasPerdidas); // Agrega campo

        parametros.add(crearLabelParametro("plazo de entrega")); // Etiqueta
        parametros.add(crearLabelValor("2 semanas", Color.WHITE)); // Valor fijo no editable
        parametros.add(new JLabel()); // Celda vacía
        parametros.add(new JLabel()); // Celda vacía

        JPanel derecha = new JPanel(new BorderLayout()); // Panel derecho
        derecha.setBackground(Color.WHITE); // Fondo blanco
        derecha.add(parametros, BorderLayout.NORTH); // Agrega parámetros arriba

        panel.add(info, BorderLayout.WEST); // Info a la izquierda
        panel.add(derecha, BorderLayout.CENTER); // Parámetros al centro

        return panel; // Retorna panel completo
    }

    private JLabel crearLabelParametro(String texto) { // Crea etiqueta de nombre de parámetro
        JLabel lbl = new JLabel(texto); // Crea etiqueta
        lbl.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente pequeña
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
        return lbl; // Retorna etiqueta
    }

    private JLabel crearLabelValor(String texto, Color bg) { // Crea etiqueta de valor fijo no editable
        JLabel lbl = new JLabel(texto); // Crea etiqueta
        lbl.setFont(new Font("Calibri", Font.BOLD, 11)); // Fuente negrita
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea al centro
        lbl.setBackground(bg); // Color de fondo
        lbl.setOpaque(true); // Hace opaco
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borde negro
        return lbl; // Retorna etiqueta
    }

    private JTextField crearTextField(String valor) { // Sobrecarga: crea campo de texto con fondo blanco por defecto
        return crearTextField(valor, Color.WHITE); // Llama al método completo
    }

    private JTextField crearTextField(String valor, Color bg) { // Crea campo de texto editable con valor y color de fondo
        JTextField txt = new JTextField(valor); // Crea campo con valor inicial
        txt.setFont(new Font("Calibri", Font.BOLD, 11)); // Fuente negrita
        txt.setHorizontalAlignment(SwingConstants.CENTER); // Alinea texto al centro
        txt.setBackground(bg); // Color de fondo (amarillo para variables de decisión, blanco para otros)
        txt.setOpaque(true); // Hace opaco
        txt.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borde negro
        return txt; // Retorna campo
    }

    private JScrollPane crearTabla() { // Crea tabla principal con 15 columnas
        String[] cols = {"Semana", "Posición de\ninventario", "Inventario\ninicial", "Pedido\nrecibido", "Unidades\nrecibidas", "Demanda", "Inventario\nfinal", "Ventas\nperdidas", "¿Pedido\nrealizado?", "Posición\ninventario\nfinal", "Semana\nvencimiento", "Costo de\nalmacenamiento", "Costo del\npedido", "Costo por\nfaltante", "Costo\ntotal"}; // Array con nombres de columnas

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea modelo con columnas y 0 filas iniciales
            public boolean isCellEditable(int r, int c) { return false; } // Todas las celdas NO EDITABLES
        };

        JTable tabla = new JTable(modeloTabla); // Crea tabla visual
        configurarEstiloTabla(tabla); // Configura estilos

        JScrollPane scroll = new JScrollPane(tabla); // Agrega scroll
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Borde gris
        return scroll; // Retorna scroll
    }

    private void configurarEstiloTabla(JTable tabla) { // Configura colores, fuentes y estilos de tabla
        tabla.setFont(new Font("Calibri", Font.PLAIN, 10)); // Fuente pequeña
        tabla.setRowHeight(25); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 10)); // Fuente del encabezado
        tabla.getTableHeader().setBackground(new Color(79, 129, 189)); // Fondo azul del encabezado
        tabla.getTableHeader().setForeground(Color.WHITE); // Texto blanco del encabezado
        tabla.setGridColor(new Color(200, 200, 200)); // Color de líneas de cuadrícula

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Renderizador personalizado
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Método de renderizado
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al método base
                setHorizontalAlignment(SwingConstants.CENTER); // Alinea al centro
                setFont(new Font("Calibri", Font.PLAIN, 10)); // Fuente

                if (c == 5) { // Si es columna 5 (Demanda)
                    setBackground(new Color(146, 208, 80)); // Fondo verde claro
                } else if (c == 8) { // Si es columna 8 (¿Pedido realizado?)
                    setBackground(Color.WHITE); // Fondo blanco
                } else { // Para otras columnas
                    setBackground(Color.WHITE); // Fondo blanco
                }

                setForeground(Color.BLACK); // Texto negro
                return this; // Retorna componente
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Para cada columna
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica renderizador
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 60 : 85); // Ancho: 60 para columna 0, 85 para las demás
        }
    }

    private JPanel crearPanelTotales() { // Crea panel de costos totales anuales
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 0)); // GridLayout 1 fila x 5 columnas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Margen

        JLabel titulo = new JLabel("Costos anuales totales"); // Título
        titulo.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
        panel.add(titulo); // Agrega título

        lblCostoAlmacenamiento = crearLabelTotal("$ 1.040"); // Crea etiqueta costo almacenamiento
        lblCostoPedido = crearLabelTotal("$ 1.050"); // Crea etiqueta costo pedido
        lblCostoFaltante = crearLabelTotal("$ 5.000"); // Crea etiqueta costo faltante
        lblCostoTotal = crearLabelTotal("$ 7.090", new Color(0, 255, 255)); // Crea etiqueta costo total con fondo cian

        panel.add(lblCostoAlmacenamiento); // Agrega etiqueta
        panel.add(lblCostoPedido); // Agrega etiqueta
        panel.add(lblCostoFaltante); // Agrega etiqueta
        panel.add(lblCostoTotal); // Agrega etiqueta

        return panel; // Retorna panel
    }

    private JLabel crearLabelTotal(String valor) { // Sobrecarga: crea etiqueta con fondo blanco
        return crearLabelTotal(valor, Color.WHITE); // Llama al método completo
    }

    private JLabel crearLabelTotal(String valor, Color bg) { // Crea etiqueta de total con valor y color de fondo
        JLabel lbl = new JLabel(valor); // Crea etiqueta
        lbl.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinea al centro
        lbl.setBackground(bg); // Color de fondo
        lbl.setForeground(Color.BLACK); // Texto negro
        lbl.setOpaque(true); // Hace opaco
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borde negro
        return lbl; // Retorna etiqueta
    }

    private JPanel crearPanelControl() { // Crea panel de controles (botones y barras de progreso)
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // GridLayout 2 filas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Margen

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Panel de botones centrado
        panelBtns.setBackground(Color.WHITE); // Fondo blanco

        btnActualizar = crearBoton("Actualizar Tabla", new Color(237, 125, 49), 180, 40); // Botón ACTUALIZAR naranja
        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 320, 45); // Botón OPTIMIZAR azul

        btnActualizar.addActionListener(e -> actualizarTablaConValoresActuales()); // Al hacer clic actualizar: ejecuta método actualizarTablaConValoresActuales
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Al hacer clic optimizar: ejecuta método ejecutarOptimizacion

        panelBtns.add(btnActualizar); // Agrega botón actualizar
        panelBtns.add(btnOptimizar); // Agrega botón optimizar

        JPanel panelProgress = crearPanelProgreso(); // Crea panel de progreso

        panel.add(panelBtns); // Agrega botones a fila superior
        panel.add(panelProgress); // Agrega progreso a fila inferior
        return panel; // Retorna panel
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Crea botón personalizado
        JButton btn = new JButton(texto); // Crea botón con texto
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 15 ? 15 : 14)); // Fuente según longitud
        btn.setBackground(bg); // Color de fondo
        btn.setForeground(Color.WHITE); // Texto blanco
        btn.setFocusPainted(false); // Sin borde de foco
        btn.setPreferredSize(new Dimension(ancho, alto)); // Tamaño
        return btn; // Retorna botón
    }

    private JPanel crearPanelProgreso() { // Crea panel con barras de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest")); // Borde con título

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8)); // GridLayout para 2 barras
        barras.setBackground(Color.WHITE); // Fondo blanco
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Margen

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales", new Color(0, 32, 96), out -> lblSimulaciones = out); // Barra de simulaciones azul oscuro
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas", new Color(0, 176, 80), out -> lblPruebas = out); // Barra de pruebas verde

        barras.add(progressBar.getParent()); // Agrega panel contenedor de barra de simulaciones
        barras.add(progressBarPruebas.getParent()); // Agrega panel contenedor de barra de pruebas

        panel.add(barras); // Agrega barras al panel
        return panel; // Retorna panel
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color, java.util.function.Consumer<JLabel> labelOut) { // Crea barra de progreso con etiqueta descriptiva
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Panel contenedor
        panel.setBackground(Color.WHITE); // Fondo blanco

        JLabel lbl = new JLabel(texto + ": 0 / " + max); // Etiqueta con texto y contador inicial
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10)); // Fuente pequeña
        labelOut.accept(lbl); // Pasa referencia al consumer para actualizar desde fuera

        JProgressBar bar = new JProgressBar(0, max); // Crea barra con rango 0-max
        bar.setPreferredSize(new Dimension(500, 22)); // Tamaño
        bar.setForeground(color); // Color de la barra
        bar.setBackground(Color.WHITE); // Fondo blanco

        panel.add(lbl, BorderLayout.WEST); // Etiqueta a la izquierda
        panel.add(bar, BorderLayout.CENTER); // Barra en el centro
        panel.add(new JLabel(max + "  "), BorderLayout.EAST); // Valor máximo a la derecha

        return bar; // Retorna barra (el panel se obtiene con getParent())
    }

    private void actualizarTablaConValoresActuales() { // Método que lee valores de campos de texto y actualiza la tabla
        try { // Bloque try para capturar excepciones
            cantidadPedidoActual = Integer.parseInt(txtCantidadPedido.getText()); // Parsea cantidad de pedido a entero
            puntoReordenActual = Integer.parseInt(txtPuntoReorden.getText()); // Parsea punto de reorden a entero
            Config.COSTO_PEDIDO = Double.parseDouble(txtCostoPedido.getText()); // Parsea costo pedido a double
            Config.COSTO_TENENCIA = Double.parseDouble(txtCostoTenencia.getText()); // Parsea costo tenencia a double
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(txtCostoVentasPerdidas.getText()); // Parsea costo ventas perdidas a double

            txtInventarioInicial.setText(String.valueOf(cantidadPedidoActual)); // Actualiza inventario inicial (igual a cantidad de pedido)

            simularYMostrarEnTabla(cantidadPedidoActual, puntoReordenActual); // Simula y muestra en tabla con valores actualizados
        } catch (NumberFormatException ex) { // Captura excepción si algún texto no se puede convertir a número
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos"); // Muestra mensaje de error
        }
    }

    private void ejecutarOptimizacion() { // Método principal que ejecuta optimización completa con multithreading
        try { // Bloque try para validar costos antes de empezar
            Config.COSTO_PEDIDO = Double.parseDouble(txtCostoPedido.getText()); // Parsea y actualiza costo pedido
            Config.COSTO_TENENCIA = Double.parseDouble(txtCostoTenencia.getText()); // Parsea y actualiza costo tenencia
            Config.COSTO_VENTAS_PERDIDAS = Double.parseDouble(txtCostoVentasPerdidas.getText()); // Parsea y actualiza costo ventas perdidas
        } catch (NumberFormatException ex) { // Captura excepción de formato
            JOptionPane.showMessageDialog(this, "Por favor ingrese valores numéricos válidos en los costos"); // Muestra error
            return; // Sale del método sin iniciar optimización
        }

        btnOptimizar.setEnabled(false); // Deshabilita botón optimizar durante ejecución
        costosFinales.clear(); // Limpia lista de costos finales
        todosCostosMC.clear(); // Limpia lista de TODOS los costos (NUEVA característica: guarda costos de todas las simulaciones, no solo la mejor)
        mejorCosto = Double.POSITIVE_INFINITY; // Reinicia mejor costo
        modeloTabla.setRowCount(0); // Limpia tabla

        new SwingWorker<Void, int[]>() { // Crea SwingWorker para ejecutar en segundo plano
            protected Void doInBackground() { // Método ejecutado en hilo separado
                int numThreads = Runtime.getRuntime().availableProcessors(); // Obtiene número de procesadores disponibles
                ExecutorService executor = Executors.newFixedThreadPool(numThreads); // Crea pool de threads para paralelizar

                List<Future<ResultadoSimulacion>> futures = new ArrayList<>(); // Lista de futuros (resultados asíncronos)
                int simCount = 0; // Contador de simulaciones

                for (int cantPedido = Config.ORDEN_MIN; cantPedido <= Config.ORDEN_MAX; cantPedido += Config.ORDEN_PASO) { // Itera sobre cantidades de pedido
                    for (int puntoReorden = Config.REORDEN_MIN; puntoReorden <= Config.REORDEN_MAX; puntoReorden += Config.REORDEN_PASO) { // Itera sobre puntos de reorden
                        final int cp = cantPedido; // Variable final para uso en lambda
                        final int pr = puntoReorden; // Variable final para uso en lambda
                        final int currentSim = simCount; // Variable final para número de simulación

                        Future<ResultadoSimulacion> future = executor.submit(() -> { // Envía tarea al executor (se ejecuta en paralelo)
                            List<Double> costosSim = new ArrayList<>(); // Lista temporal para costos de esta combinación
                            Random rand = new Random(); // Generador aleatorio

                            for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) { // Ejecuta 5000 pruebas Monte Carlo
                                double costoTotal = simularInventario(cp, pr, rand); // Simula inventario y obtiene costo
                                costosSim.add(costoTotal); // Agrega costo a lista

                                if (mc % 500 == 0) { // Cada 500 pruebas
                                    publish(new int[]{currentSim + 1, mc + 1}); // Publica actualización de progreso
                                }
                            }

                            double costoMedio = costosSim.stream().mapToDouble(d -> d).average().orElse(0); // Calcula costo medio
                            return new ResultadoSimulacion(cp, pr, costoMedio, costosSim); // Retorna resultado
                        });

                        futures.add(future); // Agrega futuro a lista
                        simCount++; // Incrementa contador
                    }
                }

                for (Future<ResultadoSimulacion> future : futures) { // Procesa todos los resultados
                    try { // Bloque try para manejo de excepciones
                        ResultadoSimulacion resultado = future.get(); // Obtiene resultado (bloquea hasta que termine)

                        synchronized(todosCostosMC) { // Bloque sincronizado para acceso thread-safe a lista compartida
                            todosCostosMC.addAll(resultado.costos); // NUEVA CARACTERÍSTICA: Guarda TODOS los costos de esta simulación (no solo la mejor) - permite análisis más completo
                        }

                        if (resultado.costoMedio < mejorCosto) { // Si este resultado es mejor
                            mejorCosto = resultado.costoMedio; // Actualiza mejor costo
                            mejorCantidadPedido = resultado.cantidadPedido; // Actualiza mejor cantidad
                            mejorPuntoReorden = resultado.puntoReorden; // Actualiza mejor punto
                            costosFinales = new ArrayList<>(resultado.costos); // Guarda costos de la mejor solución
                        }
                    } catch (Exception e) { // Captura cualquier excepción
                        e.printStackTrace(); // Imprime error
                    }
                }

                executor.shutdown(); // Apaga el executor
                return null; // Retorna null
            }

            protected void process(List<int[]> chunks) { // Actualiza progreso en hilo de UI
                int[] ultimo = chunks.get(chunks.size() - 1); // Obtiene última actualización
                progressBar.setValue(ultimo[0]); // Actualiza barra de simulaciones
                progressBarPruebas.setValue(ultimo[1]); // Actualiza barra de pruebas
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES); // Actualiza texto
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC); // Actualiza texto
            }

            protected void done() { // Al terminar (en hilo de UI)
                actualizarResultados(); // Actualiza resultados
                btnOptimizar.setEnabled(true); // Habilita botón optimizar
                mostrarVentanaResultados(); // Muestra ventana de resultados
            }
        }.execute(); // Inicia ejecución del worker
    }

    private static class ResultadoSimulacion { // Clase interna para encapsular resultado de una simulación
        int cantidadPedido; // Cantidad de pedido usada
        int puntoReorden; // Punto de reorden usado
        double costoMedio; // Costo medio obtenido
        List<Double> costos; // Lista de costos individuales

        ResultadoSimulacion(int cp, int pr, double cm, List<Double> c) { // Constructor
            this.cantidadPedido = cp; // Inicializa cantidad
            this.puntoReorden = pr; // Inicializa punto
            this.costoMedio = cm; // Inicializa costo medio
            this.costos = c; // Inicializa lista de costos
        }
    }

    private double simularInventario(int cantidadPedido, int puntoReorden, Random rand) { // Simula sistema de inventario por 52 semanas y retorna costo total
        PoissonDistribution poissonDist = new PoissonDistribution(Config.DEMANDA_MEDIA); // Crea distribución de Poisson con media 100

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Array para posición de inventario (índices 1-52)
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para inventario inicial
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para unidades recibidas
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para demanda
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para inventario final
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para ventas perdidas
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se realizó pedido
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para posición final
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para semana de vencimiento

        double costoAlmacenamientoTotal = 0; // Acumulador de costo almacenamiento
        double costoPedidoTotal = 0; // Acumulador de costo pedidos
        double costoFaltanteTotal = 0; // Acumulador de costo faltantes

        posicionInventario[1] = cantidadPedido; // SEMANA 1: Posición inicial = cantidad de pedido
        inventarioInicial[1] = cantidadPedido; // SEMANA 1: Inventario inicial = cantidad de pedido
        unidadesRecibidas[1] = 0; // SEMANA 1: Cero unidades recibidas (no hay pedidos previos)
        demanda[1] = poissonDist.sample(); // SEMANA 1: Genera demanda aleatoria usando Poisson

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1: Calcula inventario final (no negativo)

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1: Calcula demanda satisfecha (mínimo entre demanda e inventario disponible)
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1; // SEMANA 1: Calcula ventas perdidas

        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1]; // SEMANA 1: FÓRMULA CORRECTA para posición después de demanda (suma ventas perdidas porque no se satisfacen)
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden; // SEMANA 1: Decide si realizar pedido (SÍ si posición <= punto de reorden)

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1: Calcula posición final

        if (pedidoRealizado[1]) { // SEMANA 1: Si se realizó pedido
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1; // Registra semana de vencimiento = 1 + 2 + 1 = 4
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1: Calcula costo almacenamiento
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1: Calcula costo pedido
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1: Calcula costo faltante

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1: Acumula costos
        costoPedidoTotal += costoPedido; // SEMANA 1: Acumula costos
        costoFaltanteTotal += costoFaltante; // SEMANA 1: Acumula costos

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Bucle para procesar semanas 2 a 52
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Posición inicial = posición final de semana anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Inventario inicial = inventario final de semana anterior

            int numArriving = 0; // Contador de pedidos que llegan esta semana
            for (int s = 1; s < sem; s++) { // Revisa todas las semanas anteriores
                if (semanaVencimiento[s] == sem) { // Si un pedido vence esta semana
                    numArriving++; // Incrementa contador
                }
            }
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas
            demanda[sem] = poissonDist.sample(); // Genera demanda aleatoria

            inventarioFinal[sem] = Math.max(0, inventarioInicial[sem] + unidadesRecibidas[sem] - demanda[sem]); // Calcula inventario final

            int demandaSatisfecha = Math.min(demanda[sem], inventarioInicial[sem] + unidadesRecibidas[sem]); // Calcula demanda satisfecha
            ventasPerdidas[sem] = demanda[sem] - demandaSatisfecha; // Calcula ventas perdidas

            int posicionDespuesDemanda = posicionInventario[sem] - demanda[sem] + ventasPerdidas[sem]; // FÓRMULA CORRECTA para posición después de demanda
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
        }

        return costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal; // Retorna suma de los tres costos totales
    }

    private void actualizarResultados() { // Actualiza interfaz con mejores resultados encontrados
        txtCantidadPedido.setText(String.valueOf(mejorCantidadPedido)); // Actualiza campo cantidad
        txtPuntoReorden.setText(String.valueOf(mejorPuntoReorden)); // Actualiza campo punto
        txtInventarioInicial.setText(String.valueOf(mejorCantidadPedido)); // Actualiza inventario inicial

        cantidadPedidoActual = mejorCantidadPedido; // Actualiza variable actual
        puntoReordenActual = mejorPuntoReorden; // Actualiza variable actual

        simularYMostrarEnTabla(mejorCantidadPedido, mejorPuntoReorden); // Simula con mejores parámetros
    }

    private void simularYMostrarEnTabla(int cantidadPedido, int puntoReorden) { // Simula y llena tabla con demanda fija (para visualización reproducible)
        modeloTabla.setRowCount(0); // Limpia tabla

        int[] posicionInventario = new int[Config.NUM_SEMANAS + 1]; // Arrays para datos de simulación (mismo código que simularInventario pero con demanda fija)
        int[] inventarioInicial = new int[Config.NUM_SEMANAS + 1]; // Array para inventario inicial
        boolean[] pedidoRecibido = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se recibió pedido (para tabla)
        int[] unidadesRecibidas = new int[Config.NUM_SEMANAS + 1]; // Array para unidades recibidas
        int[] demanda = new int[Config.NUM_SEMANAS + 1]; // Array para demanda
        int[] inventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para inventario final
        int[] ventasPerdidas = new int[Config.NUM_SEMANAS + 1]; // Array para ventas perdidas
        boolean[] pedidoRealizado = new boolean[Config.NUM_SEMANAS + 1]; // Array para indicar si se realizó pedido
        int[] posicionInventarioFinal = new int[Config.NUM_SEMANAS + 1]; // Array para posición final
        int[] semanaVencimiento = new int[Config.NUM_SEMANAS + 1]; // Array para semana de vencimiento

        double costoAlmacenamientoTotal = 0; // Acumulador costos
        double costoPedidoTotal = 0; // Acumulador costos
        double costoFaltanteTotal = 0; // Acumulador costos

        final int fixedDemand = 100; // Demanda FIJA de 100 para todas las semanas (para tabla reproducible)

        posicionInventario[1] = cantidadPedido; // SEMANA 1: (mismo proceso que simularInventario pero con demanda fija y llenando tabla)
        inventarioInicial[1] = cantidadPedido; // SEMANA 1
        pedidoRecibido[1] = false; // SEMANA 1
        unidadesRecibidas[1] = 0; // SEMANA 1
        demanda[1] = fixedDemand; // SEMANA 1: Usa demanda fija

        inventarioFinal[1] = Math.max(0, inventarioInicial[1] + unidadesRecibidas[1] - demanda[1]); // SEMANA 1

        int demandaSatisfecha1 = Math.min(demanda[1], inventarioInicial[1] + unidadesRecibidas[1]); // SEMANA 1
        ventasPerdidas[1] = demanda[1] - demandaSatisfecha1; // SEMANA 1

        int posicionDespuesDemanda1 = posicionInventario[1] - demanda[1] + ventasPerdidas[1]; // SEMANA 1
        pedidoRealizado[1] = posicionDespuesDemanda1 <= puntoReorden; // SEMANA 1

        posicionInventarioFinal[1] = posicionDespuesDemanda1 + (pedidoRealizado[1] ? cantidadPedido : 0); // SEMANA 1

        if (pedidoRealizado[1]) { // SEMANA 1
            semanaVencimiento[1] = 1 + Config.PLAZO_ENTREGA + 1; // SEMANA 1
        }

        double costoAlmacenamiento = Math.max(0, inventarioFinal[1]) * Config.COSTO_TENENCIA; // SEMANA 1
        double costoPedido = pedidoRealizado[1] ? Config.COSTO_PEDIDO : 0; // SEMANA 1
        double costoFaltante = ventasPerdidas[1] * Config.COSTO_VENTAS_PERDIDAS; // SEMANA 1

        costoAlmacenamientoTotal += costoAlmacenamiento; // SEMANA 1
        costoPedidoTotal += costoPedido; // SEMANA 1
        costoFaltanteTotal += costoFaltante; // SEMANA 1

        modeloTabla.addRow(new Object[]{ // SEMANA 1: Agrega fila a tabla con 15 valores
            "1", // Columna 1: Número de semana
            FMT_INT.format(posicionInventario[1]), // Columna 2: Posición inventario
            FMT_INT.format(inventarioInicial[1]), // Columna 3: Inventario inicial
            pedidoRecibido[1] ? "VERDADERO" : "FALSO", // Columna 4: Pedido recibido
            FMT_INT.format(unidadesRecibidas[1]), // Columna 5: Unidades recibidas
            FMT_INT.format(demanda[1]), // Columna 6: Demanda
            FMT_INT.format(inventarioFinal[1]), // Columna 7: Inventario final
            FMT_INT.format(ventasPerdidas[1]), // Columna 8: Ventas perdidas
            pedidoRealizado[1] ? "VERDADERO" : "FALSO", // Columna 9: Pedido realizado
            FMT_INT.format(posicionInventarioFinal[1]), // Columna 10: Posición final
            semanaVencimiento[1] > 0 ? String.valueOf(semanaVencimiento[1]) : "", // Columna 11: Semana vencimiento
            "$    " + FMT_NUMBER.format(costoAlmacenamiento), // Columna 12: Costo almacenamiento
            "$    " + FMT_NUMBER.format(costoPedido), // Columna 13: Costo pedido
            "$    " + FMT_NUMBER.format(costoFaltante), // Columna 14: Costo faltante
            "$    " + FMT_NUMBER.format(costoAlmacenamiento + costoPedido + costoFaltante) // Columna 15: Costo total
        });

        for (int sem = 2; sem <= Config.NUM_SEMANAS; sem++) { // SEMANAS 2-52: Bucle para agregar filas 2 a 52
            posicionInventario[sem] = posicionInventarioFinal[sem - 1]; // Posición inicial = posición final anterior
            inventarioInicial[sem] = inventarioFinal[sem - 1]; // Inventario inicial = inventario final anterior

            int numArriving = 0; // Contador de pedidos llegando
            for (int s = 1; s < sem; s++) { // Revisa semanas anteriores
                if (semanaVencimiento[s] == sem) { // Si pedido vence esta semana
                    numArriving++; // Incrementa contador
                }
            }
            pedidoRecibido[sem] = numArriving > 0; // Marca si llegó al menos un pedido
            unidadesRecibidas[sem] = numArriving * cantidadPedido; // Calcula unidades recibidas
            demanda[sem] = fixedDemand; // Usa demanda fija

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

            modeloTabla.addRow(new Object[]{ // Agrega fila a tabla
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

        lblCostoAlmacenamiento.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal)); // Actualiza etiqueta costo almacenamiento
        lblCostoPedido.setText("$ " + FMT_NUMBER.format(costoPedidoTotal)); // Actualiza etiqueta costo pedido
        lblCostoFaltante.setText("$ " + FMT_NUMBER.format(costoFaltanteTotal)); // Actualiza etiqueta costo faltante
        lblCostoTotal.setText("$ " + FMT_NUMBER.format(costoAlmacenamientoTotal + costoPedidoTotal + costoFaltanteTotal)); // Actualiza etiqueta costo total
    }

    private void mostrarVentanaResultados() { // Muestra ventana emergente con resumen de resultados de optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crea diálogo no modal
        dlg.setLayout(new BorderLayout()); // BorderLayout

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15); // Panel principal

        JPanel headerPanel = new JPanel(new BorderLayout()); // Panel de encabezado
        headerPanel.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblHeader = new JLabel(Config.NUM_SIMULACIONES + " simulaciones"); // Etiqueta con número de simulaciones
        lblHeader.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita

        JLabel lblSubHeader = new JLabel("Vista de mejor solución"); // Subtítulo
        lblSubHeader.setFont(new Font("Calibri", Font.PLAIN, 12)); // Fuente normal

        headerPanel.add(lblHeader, BorderLayout.NORTH); // Agrega encabezado arriba
        headerPanel.add(lblSubHeader, BorderLayout.CENTER); // Agrega subtítulo al centro

        main.add(headerPanel, BorderLayout.NORTH); // Agrega panel de encabezado arriba

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 10)); // Panel para 3 secciones
        tablas.setBackground(Color.WHITE); // Fondo blanco

        JPanel graficoPanel = new JPanel(new BorderLayout()); // Panel de gráfico (placeholder)
        graficoPanel.setBorder(BorderFactory.createTitledBorder("Gráfico de rendimiento")); // Borde con título
        graficoPanel.setBackground(Color.WHITE); // Fondo blanco
        graficoPanel.setPreferredSize(new Dimension(600, 150)); // Tamaño

        JLabel lblGrafico = new JLabel("Mejores soluciones encontradas", SwingConstants.CENTER); // Etiqueta centrada
        lblGrafico.setFont(new Font("Calibri", Font.ITALIC, 11)); // Fuente itálica
        graficoPanel.add(lblGrafico, BorderLayout.CENTER); // Agrega al centro

        tablas.add(graficoPanel); // Agrega panel de gráfico

        tablas.add(crearSeccionResultado("Objetivos", "Valor", new String[]{"Minimizar el/la Media de Total Annual Costs"}, new String[]{"$ " + FMT_NUMBER.format(mejorCosto)})); // Agrega sección de objetivos

        JPanel requisitosPanel = new JPanel(new BorderLayout()); // Panel de requisitos
        requisitosPanel.setBorder(BorderFactory.createTitledBorder("Requisitos:")); // Borde con título
        requisitosPanel.setBackground(Color.WHITE); // Fondo blanco
        JLabel lblRequisitos = new JLabel("(requisitos opcionales en previsiones)"); // Etiqueta
        lblRequisitos.setFont(new Font("Calibri", Font.ITALIC, 10)); // Fuente itálica pequeña
        requisitosPanel.add(lblRequisitos, BorderLayout.CENTER); // Agrega al centro
        tablas.add(requisitosPanel); // Agrega panel de requisitos

        main.add(tablas, BorderLayout.CENTER); // Agrega secciones al centro

        JPanel inferior = new JPanel(new GridLayout(1, 2, 10, 0)); // Panel inferior con 2 columnas
        inferior.setBackground(Color.WHITE); // Fondo blanco

        JPanel restriccionesPanel = new JPanel(new BorderLayout()); // Panel de restricciones
        restriccionesPanel.setBorder(BorderFactory.createTitledBorder("Restricciones")); // Borde con título
        restriccionesPanel.setBackground(Color.WHITE); // Fondo blanco

        JPanel restriccionesGrid = new JPanel(new GridLayout(1, 3, 5, 5)); // Grid para encabezados
        restriccionesGrid.setBackground(Color.WHITE); // Fondo blanco
        restriccionesGrid.add(new JLabel("")); // Etiqueta vacía
        restriccionesGrid.add(new JLabel("Lado izquierdo", SwingConstants.CENTER)); // Encabezado
        restriccionesGrid.add(new JLabel("Lado derecho", SwingConstants.CENTER)); // Encabezado

        restriccionesPanel.add(restriccionesGrid, BorderLayout.CENTER); // Agrega grid
        inferior.add(restriccionesPanel); // Agrega panel de restricciones

        JPanel variablesPanel = new JPanel(new BorderLayout()); // Panel de variables
        variablesPanel.setBorder(BorderFactory.createTitledBorder("Variables de decisión")); // Borde con título
        variablesPanel.setBackground(Color.WHITE); // Fondo blanco

        String[][] varsData = {{"Order Quantity", String.valueOf(mejorCantidadPedido)}, {"Reorder Point", String.valueOf(mejorPuntoReorden)}}; // Datos de variables

        DefaultTableModel modeloVars = new DefaultTableModel(new String[]{"", "Valor"}, 0); // Modelo de tabla
        for (String[] row : varsData) { // Itera sobre filas
            modeloVars.addRow(row); // Agrega fila
        }

        JTable tablaVars = new JTable(modeloVars); // Crea tabla
        tablaVars.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente
        tablaVars.setRowHeight(25); // Altura de filas
        tablaVars.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Fuente del encabezado

        JScrollPane scrollVars = new JScrollPane(tablaVars); // Scroll para tabla
        scrollVars.setPreferredSize(new Dimension(250, 80)); // Tamaño
        variablesPanel.add(scrollVars, BorderLayout.CENTER); // Agrega scroll

        inferior.add(variablesPanel); // Agrega panel de variables

        main.add(inferior, BorderLayout.SOUTH); // Agrega panel inferior abajo

        dlg.add(main); // Agrega panel principal al diálogo
        dlg.setSize(750, 550); // Tamaño del diálogo
        dlg.setLocationRelativeTo(this); // Centra respecto a ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) { // Crea sección de resultados con tabla
        JPanel panel = new JPanel(new BorderLayout()); // BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder(titulo)); // Borde con título
        panel.setBackground(Color.WHITE); // Fondo blanco

        if (filas.length > 0) { // Si hay filas
            DefaultTableModel modelo = new DefaultTableModel(new String[]{"Objetivos", col}, 0); // Modelo de tabla
            for (int i = 0; i < filas.length; i++) { // Itera sobre filas
                modelo.addRow(new Object[]{filas[i], vals[i]}); // Agrega fila
            }

            JTable tabla = new JTable(modelo); // Crea tabla
            tabla.setFont(new Font("Calibri", Font.PLAIN, 11)); // Fuente
            tabla.setRowHeight(25); // Altura
            tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 11)); // Fuente del encabezado

            JScrollPane scroll = new JScrollPane(tabla); // Scroll
            scroll.setPreferredSize(new Dimension(600, 60)); // Tamaño
            panel.add(scroll, BorderLayout.CENTER); // Agrega scroll
        }
        return panel; // Retorna panel
    }

    public static void main(String[] args) { // Método main - punto de entrada del programa
        try { // Bloque try
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece Look and Feel FlatLaf
        } catch (Exception e) { // Captura excepción
            e.printStackTrace(); // Imprime error
        }

        SwingUtilities.invokeLater(() -> new InventorySystemSimulatorEditable().setVisible(true)); // Crea y muestra ventana en hilo de Swing
    }
}