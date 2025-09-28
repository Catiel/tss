package actividad_7;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.inference.KolmogorovSmirnovTest;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.BasicStroke;
import java.util.Arrays;
import java.util.Random;

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 */
public class SimulacionCompleta extends JFrame {

    // ========================== ATRIBUTOS DE LA CLASE ==========================

    // Componentes de la interfaz gráfica
    private JTable tabla; // Tabla principal que muestra la simulación día a día
    private DefaultTableModel model; // Modelo de datos para la tabla principal

    // Parámetros de configuración del sistema
    private final double errorPermitido = 500; // Error máximo permitido en la estimación
    private final double valorT = 1.9665; // Valor t de Student para nivel de confianza 95%
    private final int politicaProduccion = 60; // Unidades producidas diariamente (constante)
    private final double mediaDemanda = 80; // Media de la distribución normal de demanda
    private final double desviacionDemanda = 10; // Desviación estándar de la demanda
    private final double costoFaltanteUnitario = 800; // Costo por unidad no vendida (faltante)
    private final double costoInventarioUnitario = 500; // Costo por unidad en inventario

    // Variables calculadas durante la simulación
    private double desviacion; // Desviación estándar de los costos totales
    private double promedio; // Promedio de los costos totales
    private int tamanoRecomendado; // Tamaño de muestra recomendado para precisión deseada
    private boolean esNormal; // Indica si los datos siguen distribución normal
    private double pValue; // Valor p de la prueba de normalidad
    private double valorAd; // Valor Anderson-Darling (simulado para compatibilidad)

    // ========================== CONSTANTES DE DISEÑO ==========================

    // Definición de columnas de la tabla principal
    private static final String[] COLUMNAS = {
        "Día", "Inventario inicial (Uds)", "Política de producción (Uds)",
        "Total disponible (Uds)", "Rn", "Demanda (uds)", "Ventas (Uds)",
        "Ventas perdidas (uds)", "Inventario final (Uds)", "Costo faltante ($)",
        "Costo de inventarios ($)", "Costo total ($)"
    };

    // Anchos preferidos para cada columna de la tabla
    private static final int[] ANCHOS_COLUMNAS = {
        50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150
    };

    // Colores del tema visual de la aplicación
    private static final Color COLOR_PRIMARIO = new Color(30, 144, 255); // Azul principal
    private static final Color COLOR_FILA_PAR = Color.WHITE; // Fondo filas pares
    private static final Color COLOR_FILA_IMPAR = new Color(235, 245, 255); // Fondo filas impares
    private static final Color COLOR_FONDO_REPLICA = new Color(255, 255, 200); // Amarillo para réplicas
    private static final Color COLOR_PANEL_REPLICA = new Color(248, 250, 255); // Fondo paneles de réplica

    // Fuentes utilizadas en la interfaz
    private static final Font FUENTE_GENERAL = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font FUENTE_HEADER = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font FUENTE_TITULO = new Font("Segoe UI Semibold", Font.BOLD, 18);
    private static final Font FUENTE_VALOR = new Font("Segoe UI", Font.BOLD, 14);
    private static final Font FUENTE_REPLICA = new Font("Segoe UI Semibold", Font.BOLD, 16);

    // ========================== CONSTRUCTOR PRINCIPAL ==========================

    /**
     * Constructor que inicializa la ventana principal con simulación de 365 días
     */
    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo"); // Título de la ventana

        int dias = 365; // Número de días a simular inicialmente

        configurarTabla(); // Inicializar y configurar la tabla principal

        // Ejecutar simulación completa de 365 días y obtener array de costos totales
        double[] costosTotales = generarSimulacionYllenarTabla(dias, model);

        // Realizar cálculos estadísticos y pruebas de normalidad
        calcularEstadisticasYPruebas(costosTotales);

        // Crear panel con resumen estadístico y datos de normalidad
        JPanel resumenPanel = crearResumenEstadisticoPanel(true, promedio, desviacion,
            getMinMaxCosto(model)[0], getMinMaxCosto(model)[1]);

        // Crear botón para generar simulación con tamaño recomendado
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado");
        botonNuevaTabla.setEnabled(esNormal); // Solo habilitado si los datos son normales
        resumenPanel.add(botonNuevaTabla); // Agregar botón al panel de resumen

        // Configurar la interfaz principal de la ventana
        configurarInterfazPrincipal(costosTotales, resumenPanel);

        // Asignar acción al botón para generar tabla con tamaño recomendado
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada);
    }

    // ========================== CONFIGURACIÓN DE TABLA ==========================

    /**
     * Inicializa y configura la tabla principal con sus estilos
     */
    private void configurarTabla() {
        model = new DefaultTableModel(COLUMNAS, 0); // Crear modelo con columnas definidas, 0 filas
        tabla = new JTable(model); // Crear tabla con el modelo
        configurarEstilosTabla(tabla); // Aplicar estilos visuales a la tabla
    }

    /**
     * Aplica todos los estilos visuales a una tabla dada
     * @param tabla La tabla a la que aplicar estilos
     */
    private void configurarEstilosTabla(JTable tabla) {
        // Configuración básica de la tabla
        tabla.setFont(FUENTE_GENERAL); // Fuente del contenido de la tabla
        tabla.setRowHeight(28); // Altura de cada fila en píxeles
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactivar redimensionado automático
        tabla.setFillsViewportHeight(true); // Llenar toda la altura del viewport
        tabla.getTableHeader().setReorderingAllowed(false); // Impedir reordenar columnas

        configurarEncabezadoTabla(tabla); // Configurar estilos del encabezado
        configurarRenderizadoresYAnchos(tabla); // Configurar anchos y renderizadores de columnas
        configurarColoresAlternados(tabla); // Aplicar colores alternados a las filas
    }

    /**
     * Configura los estilos del encabezado de la tabla
     * @param tabla La tabla cuyo encabezado configurar
     */
    private void configurarEncabezadoTabla(JTable tabla) {
        JTableHeader header = tabla.getTableHeader(); // Obtener referencia al encabezado
        header.setBackground(COLOR_PRIMARIO); // Color de fondo azul
        header.setForeground(Color.WHITE); // Texto en color blanco
        header.setFont(FUENTE_HEADER); // Fuente en negrita para el encabezado
        header.setPreferredSize(new Dimension(header.getWidth(), 32)); // Altura del encabezado
    }

    /**
     * Configura anchos de columnas y renderizadores específicos para cada tipo de dato
     * @param tabla La tabla a configurar
     */
    private void configurarRenderizadoresYAnchos(JTable tabla) {
        // Crear renderizador para centrar texto
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Crear renderizador específico para valores monetarios
        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();

        // Configurar cada columna individualmente
        for (int i = 0; i < COLUMNAS.length; i++) {
            TableColumn col = tabla.getColumnModel().getColumn(i); // Obtener columna por índice
            col.setPreferredWidth(ANCHOS_COLUMNAS[i]); // Establecer ancho preferido

            // Aplicar renderizador según el tipo de columna
            if (i >= 9) { // Columnas 9, 10, 11 son valores monetarios
                col.setCellRenderer(moneyRenderer);
            } else { // Otras columnas centradas
                col.setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Aplica colores alternados a las filas de la tabla
     * @param tabla La tabla a la que aplicar colores alternados
     */
    private void configurarColoresAlternados(JTable tabla) {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                // Obtener componente base del renderizador
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Aplicar color de fondo solo si la fila no está seleccionada
                if (!isSelected) {
                    // Alternar colores: filas pares blancas, impares azul claro
                    comp.setBackground(row % 2 == 0 ? COLOR_FILA_PAR : COLOR_FILA_IMPAR);
                }
                return comp;
            }
        });
    }

    /**
     * Crea un renderizador especializado para valores monetarios
     * @return Renderizador que formatea números como moneda
     */
    private DefaultTableCellRenderer crearRenderizadorMoneda() {
        return new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) { // Si el valor es numérico
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinear a la derecha
                    setText(String.format("$%,.2f", value)); // Formato monetario con separadores de miles
                } else if (value instanceof String) { // Si es texto
                    setHorizontalAlignment(SwingConstants.RIGHT); // Mantener alineación derecha
                    setText(value.toString()); // Mostrar como texto
                } else {
                    super.setValue(value); // Usar renderizador por defecto para otros tipos
                }
            }
        };
    }

    // ========================== CONFIGURACIÓN DE INTERFAZ ==========================

    /**
     * Configura el layout y componentes principales de la ventana
     * @param costosTotales Array de costos para generar gráficas
     * @param resumenPanel Panel con información estadística
     */
    private void configurarInterfazPrincipal(double[] costosTotales, JPanel resumenPanel) {
        // Crear panel de desplazamiento para la tabla
        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Márgenes internos

        // Crear panel con las dos gráficas principales
        JPanel graficasPanel = crearPanelGraficas(costosTotales, promedio, desviacion);

        // Configurar panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20)); // Espaciado de 20px
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Márgenes externos
        mainPanel.setBackground(Color.WHITE); // Fondo blanco

        // Distribuir componentes en el layout
        mainPanel.add(graficasPanel, BorderLayout.NORTH); // Gráficas en la parte superior
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER); // Tabla en el centro (expandible)
        mainPanel.add(resumenPanel, BorderLayout.SOUTH); // Resumen en la parte inferior

        // Configurar ventana principal
        setContentPane(mainPanel); // Establecer contenido principal
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centrar en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
    }

    /**
     * Crea el panel que contiene las dos gráficas principales
     * @param costosTotales Datos para la gráfica de evolución
     * @param promedio Media para la gráfica de probabilidad normal
     * @param desviacion Desviación estándar para la gráfica de probabilidad normal
     * @return Panel con ambas gráficas
     */
    private JPanel crearPanelGraficas(double[] costosTotales, double promedio, double desviacion) {
        // Crear las dos gráficas principales
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales); // Gráfica de evolución temporal
        JFreeChart chart2 = crearGraficaProbabilidadNormal(costosTotales, promedio, desviacion); // Q-Q plot

        // Convertir gráficas en paneles para Swing
        ChartPanel chartPanel1 = crearChartPanel(chart1);
        ChartPanel chartPanel2 = crearChartPanel(chart2);

        // Crear panel contenedor con layout de grilla 1x2
        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15)); // 1 fila, 2 columnas, espaciado 15px
        graficasPanel.add(chartPanel1); // Agregar primera gráfica
        graficasPanel.add(chartPanel2); // Agregar segunda gráfica

        // Configurar borde y título del panel
        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            FUENTE_TITULO, COLOR_PRIMARIO));
        graficasPanel.setBackground(Color.WHITE); // Fondo blanco

        return graficasPanel;
    }

    /**
     * Configura un ChartPanel con las propiedades estándar
     * @param chart La gráfica a encapsular
     * @return ChartPanel configurado
     */
    private ChartPanel crearChartPanel(JFreeChart chart) {
        ChartPanel chartPanel = new ChartPanel(chart); // Crear panel para la gráfica
        chartPanel.setPreferredSize(new Dimension(1200, 300)); // Tamaño preferido
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Márgenes internos
        chartPanel.setBackground(Color.WHITE); // Fondo blanco
        return chartPanel;
    }

    // ========================== SIMULACIÓN Y CÁLCULOS ==========================

    /**
     * Ejecuta la simulación completa y llena la tabla con los resultados
     * @param dias Número de días a simular
     * @param modeloTabla Modelo de la tabla donde mostrar resultados
     * @return Array con los costos totales de cada día
     */
    private double[] generarSimulacionYllenarTabla(int dias, DefaultTableModel modeloTabla) {
        modeloTabla.setRowCount(0); // Limpiar tabla existente
        int inventarioFinal = 0; // Inventario inicial es cero al comenzar
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda); // Distribución de demanda
        Random random = new Random(); // Generador de números aleatorios
        double[] costosTotales = new double[dias]; // Array para almacenar costos de cada día

        // Simular cada día individualmente
        for (int dia = 1; dia <= dias; dia++) {
            // Simular un día y obtener todos los resultados
            ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random);
            costosTotales[dia - 1] = resultado.costoTotal; // Guardar costo total (índice base 0)
            inventarioFinal = resultado.inventarioFinal; // El inventario final se convierte en inicial del siguiente día

            // Crear fila para mostrar en la tabla
            Object[] fila = {
                dia, // Número de día
                resultado.inventarioInicial, // Inventario al inicio del día
                politicaProduccion, // Producción fija diaria
                resultado.totalDisponible, // Inventario inicial + producción
                String.format("%.4f", resultado.rn), // Número aleatorio con 4 decimales
                resultado.demanda, // Demanda generada
                resultado.ventas, // Unidades vendidas (min entre demanda y disponible)
                resultado.ventasPerdidas, // Demanda no satisfecha
                resultado.inventarioFinal, // Inventario al final del día
                resultado.costoFaltante, // Costo por ventas perdidas
                resultado.costoInventario, // Costo por mantener inventario
                resultado.costoTotal // Suma de ambos costos
            };
            modeloTabla.addRow(fila); // Agregar fila a la tabla
        }
        return costosTotales; // Retornar array de costos para análisis posterior
    }

    /**
     * Simula las operaciones de un día específico
     * @param inventarioFinalAnterior Inventario que quedó del día anterior
     * @param dist Distribución de probabilidad para la demanda
     * @param random Generador de números aleatorios
     * @return Objeto con todos los resultados del día simulado
     */
    private ResultadoSimulacion simularDia(int inventarioFinalAnterior, NormalDistribution dist, Random random) {
        ResultadoSimulacion resultado = new ResultadoSimulacion();

        // Valores iniciales del día
        resultado.inventarioInicial = inventarioFinalAnterior; // Inventario heredado del día anterior
        resultado.totalDisponible = resultado.inventarioInicial + politicaProduccion; // Inventario + producción del día

        // Generación de demanda aleatoria
        resultado.rn = random.nextDouble(); // Número aleatorio entre 0 y 1
        resultado.demanda = (int) Math.round(dist.inverseCumulativeProbability(resultado.rn)); // Transformar a demanda normal

        // Cálculos de ventas y faltantes
        resultado.ventas = Math.min(resultado.demanda, resultado.totalDisponible); // No se puede vender más de lo disponible
        resultado.ventasPerdidas = Math.max(0, resultado.demanda - resultado.ventas); // Demanda no satisfecha
        resultado.inventarioFinal = resultado.totalDisponible - resultado.ventas; // Lo que queda después de vender

        // Cálculos de costos
        resultado.costoFaltante = resultado.ventasPerdidas * costoFaltanteUnitario; // Costo por oportunidad perdida
        resultado.costoInventario = resultado.inventarioFinal * costoInventarioUnitario; // Costo por mantener stock
        resultado.costoTotal = resultado.costoFaltante + resultado.costoInventario; // Costo total del día

        return resultado;
    }

    /**
     * Calcula estadísticas descriptivas de un array de datos
     * @param costosTotales Array de valores a analizar
     * @return Objeto con todas las estadísticas calculadas
     */
    private EstadisticasSimulacion calcularEstadisticas(double[] costosTotales) {
        EstadisticasSimulacion stats = new EstadisticasSimulacion();

        // Cálculos usando streams de Java 8 para eficiencia
        stats.suma = Arrays.stream(costosTotales).sum(); // Suma total
        stats.sumaCuadrados = Arrays.stream(costosTotales).map(x -> x * x).sum(); // Suma de cuadrados
        stats.promedio = stats.suma / costosTotales.length; // Media aritmética

        // Varianza usando fórmula: E[X²] - (E[X])²
        stats.varianza = (stats.sumaCuadrados / costosTotales.length) - (stats.promedio * stats.promedio);
        stats.desviacion = Math.sqrt(stats.varianza); // Desviación estándar

        // Valores extremos
        stats.minimo = Arrays.stream(costosTotales).min().orElse(Double.NaN);
        stats.maximo = Arrays.stream(costosTotales).max().orElse(Double.NaN);

        return stats;
    }

    /**
     * Ejecuta cálculos estadísticos y pruebas de normalidad sobre los datos
     * @param costosTotales Array de costos totales para analizar
     */
    private void calcularEstadisticasYPruebas(double[] costosTotales) {
        // Calcular estadísticas descriptivas básicas
        EstadisticasSimulacion stats = calcularEstadisticas(costosTotales);
        promedio = stats.promedio; // Guardar promedio global
        desviacion = stats.desviacion; // Guardar desviación estándar global

        // Prueba de normalidad Kolmogorov-Smirnov
        pValue = new KolmogorovSmirnovTest().kolmogorovSmirnovTest(
            new NormalDistribution(promedio, desviacion), costosTotales, false);

        // Determinar si los datos siguen distribución normal (α = 0.05)
        esNormal = pValue > 0.05;

        // Calcular tamaño de muestra recomendado usando fórmula estadística
        // n = ((σ/E) * t)² donde σ=desviación, E=error permitido, t=valor crítico
        tamanoRecomendado = esNormal ? (int) Math.ceil(Math.pow((desviacion / errorPermitido) * valorT, 2)) : -1;

        valorAd = pValue; // Mantener compatibilidad (simulado)
    }

    // ========================== CREACIÓN DE PANELES DE RESUMEN ==========================

    /**
     * Crea el panel con resumen estadístico y información de normalidad
     * @param incluirTamano Si incluir información de tamaño recomendado
     * @param promedio Media de los datos
     * @param desviacion Desviación estándar
     * @param minimo Valor mínimo
     * @param maximo Valor máximo
     * @return Panel configurado con toda la información
     */
    private JPanel crearResumenEstadisticoPanel(boolean incluirTamano, double promedio,
            double desviacion, double minimo, double maximo) {

        // Crear panel con layout horizontal centrado
        JPanel resumenPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));

        // Determinar título según si incluye información de normalidad
        String titulo = incluirTamano ? "Resumen Estadístico y Normalidad" : "Resumen Estadístico";

        // Configurar borde con título
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, titulo,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            FUENTE_TITULO, COLOR_PRIMARIO));
        resumenPanel.setBackground(Color.WHITE);

        // Agregar estadísticas básicas (siempre presentes)
        resumenPanel.add(crearEtiquetaResumen(String.format("Promedio: $%,.2f", promedio)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Desviación: $%,.2f", desviacion)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Mínimo: $%,.2f", minimo)));
        resumenPanel.add(crearEtiquetaResumen(String.format("Máximo: $%,.2f", maximo)));

        // Agregar información de normalidad si se solicita
        if (incluirTamano) {
            resumenPanel.add(crearEtiquetaResumen(String.format("Valor AD (simulado): %.4f", valorAd)));
            resumenPanel.add(crearEtiquetaResumen(String.format("p-valor KS: %.4f", pValue)));

            // Mostrar tamaño recomendado o mensaje de no normalidad
            if (esNormal) {
                resumenPanel.add(crearEtiquetaResumen(String.format("Tamaño recomendado: %d corridas", tamanoRecomendado)));
            } else {
                resumenPanel.add(crearEtiquetaResumen("No se recomienda tamaño (No normal)"));
            }
        }

        return resumenPanel;
    }

    /**
     * Crea una etiqueta con formato estándar para el resumen
     * @param texto Texto a mostrar en la etiqueta
     * @return JLabel configurado con estilos estándar
     */
    private JLabel crearEtiquetaResumen(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER); // Crear etiqueta centrada
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Fuente de tamaño medio
        label.setForeground(new Color(50, 50, 50)); // Color gris oscuro para el texto
        return label;
    }

    /**
     * Obtiene los valores mínimo y máximo de la columna de costo total
     * @param modelo Modelo de tabla del cual extraer los valores
     * @return Array con [mínimo, máximo]
     */
    private double[] getMinMaxCosto(DefaultTableModel modelo) {
        int filas = modelo.getRowCount(); // Número de filas en la tabla
        double min = Double.MAX_VALUE; // Inicializar con valor máximo posible
        double max = Double.MIN_VALUE; // Inicializar with valor mínimo posible

        // Recorrer todas las filas y examinar la columna 11 (costo total)
        for (int i = 0; i < filas; i++) {
            double val = (double) modelo.getValueAt(i, 11); // Obtener costo total de la fila i
            if (val < min) min = val; // Actualizar mínimo si es necesario
            if (val > max) max = val; // Actualizar máximo si es necesario
        }

        return new double[]{min, max}; // Retornar array con ambos valores
    }

    // ========================== GENERACIÓN DE TABLA RECOMENDADA ==========================

    /**
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     * @param e Evento de acción del botón
     */
    private void generarTablaRecomendada(ActionEvent e) {
        // Verificar que los datos sean normales y el tamaño sea válido
        if (!esNormal || tamanoRecomendado < 1) return;

        // Crear nueva ventana para la simulación con tamaño recomendado
        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " + tamanoRecomendado + " corridas");

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(COLUMNAS, 0);
        JTable nuevaTabla = new JTable(nuevoModel);
        configurarEstilosTabla(nuevaTabla); // Aplicar mismos estilos que tabla principal

        // Crear panel de desplazamiento para la nueva tabla
        JScrollPane scroll = new JScrollPane(nuevaTabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Generar simulación con tamaño recomendado
        double[] costosTotalesNueva = generarSimulacionYllenarTabla(tamanoRecomendado, nuevoModel);
        EstadisticasSimulacion statsNueva = calcularEstadisticas(costosTotalesNueva);

        // Crear panel de resumen para la nueva simulación
        JPanel resumenPanelNueva = crearResumenEstadisticoPanel(false,
            statsNueva.promedio, statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo);

        // Crear botón para generar 5 réplicas
        JButton botonReplicas = new JButton("Generar 5 réplicas");
        botonReplicas.setFont(FUENTE_GENERAL); // Aplicar fuente estándar
        resumenPanelNueva.add(botonReplicas); // Agregar botón al panel de resumen

        // Crear panel de gráficas para la nueva simulación
        JPanel graficasPanelNueva = crearPanelGraficas(costosTotalesNueva,
            statsNueva.promedio, statsNueva.desviacion);

        // Crear sistema de pestañas para organizar contenido
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear pestaña principal con la simulación completa
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.add(scroll, BorderLayout.CENTER); // Tabla en el centro
        panelPrincipal.add(graficasPanelNueva, BorderLayout.NORTH); // Gráficas arriba
        tabbedPane.addTab("Simulación Completa", panelPrincipal); // Agregar como primera pestaña

        // Configurar la nueva ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10)); // Layout principal
        frameNuevaTabla.add(tabbedPane, BorderLayout.CENTER); // Pestañas en el centro
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH); // Resumen abajo

        frameNuevaTabla.setSize(1400, 950); // Tamaño de ventana
        frameNuevaTabla.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        frameNuevaTabla.setVisible(true); // Hacer visible

        // Asignar acción al botón de réplicas
        botonReplicas.addActionListener(evt -> generarReplicas(tabbedPane));
    }

    // ========================== GENERACIÓN DE RÉPLICAS ==========================

    /**
     * Genera 5 réplicas independientes y las muestra en una nueva pestaña
     * @param tabbedPane Panel de pestañas donde agregar la nueva pestaña de réplicas
     */
    private void generarReplicas(JTabbedPane tabbedPane) {
        // Verificar si ya existe la pestaña de réplicas y eliminarla
        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
            if (tabbedPane.getTitleAt(i).equals("5 Réplicas")) {
                tabbedPane.removeTabAt(i); // Remover pestaña existente
                break;
            }
        }

        // Crear panel principal para las réplicas
        JPanel panelReplicas = new JPanel(new BorderLayout(15, 15));
        panelReplicas.setBackground(Color.WHITE); // Fondo blanco

        // NUEVA FUNCIONALIDAD: Generar datos de réplicas una sola vez
        double[][] costosReplicas = new double[5][tamanoRecomendado];
        for (int replica = 0; replica < 5; replica++) {
            costosReplicas[replica] = simularReplicaCompleta();
        }

        // Crear panel superior con estadísticas de cada réplica
        JPanel panelEstadisticasReplicas = crearPanelEstadisticasReplicasConDatos(costosReplicas);
        panelReplicas.add(panelEstadisticasReplicas, BorderLayout.NORTH);

        // NUEVA FUNCIONALIDAD: Crear gráfica de réplicas
        JPanel panelGraficaReplicas = crearPanelGraficaReplicas(costosReplicas);
        panelReplicas.add(panelGraficaReplicas, BorderLayout.CENTER);

        // Crear panel inferior con tabla comparativa de réplicas
        JPanel panelTablaReplicas = crearTablaReplicasConDatos(costosReplicas);
        panelReplicas.add(panelTablaReplicas, BorderLayout.SOUTH);

        // Agregar nueva pestaña al sistema de pestañas
        tabbedPane.addTab("5 Réplicas", panelReplicas);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount() - 1);
    }

    // ========================== NUEVOS MÉTODOS PARA GRÁFICA DE RÉPLICAS ==========================

    /**
     * Crea el panel que contiene la gráfica de líneas con las 5 réplicas
     * @param costosReplicas Matriz con costos de las 5 réplicas
     * @return Panel con la gráfica de réplicas
     */
    private JPanel crearPanelGraficaReplicas(double[][] costosReplicas) {
        // Crear gráfica de líneas para las réplicas
        JFreeChart chartReplicas = crearGraficaLineasReplicas(costosReplicas);

        // Crear panel para la gráfica
        ChartPanel chartPanel = new ChartPanel(chartReplicas);
        chartPanel.setPreferredSize(new Dimension(1200, 400));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel.setBackground(Color.WHITE);

        // Crear botón para ver gráfica en grande
        JButton botonVerGrafica = new JButton("Ver Gráfica");
        botonVerGrafica.setFont(FUENTE_GENERAL);
        botonVerGrafica.setBackground(COLOR_PRIMARIO);
        botonVerGrafica.setForeground(Color.WHITE);
        botonVerGrafica.setFocusPainted(false);
        botonVerGrafica.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Panel para el botón (alineado a la derecha)
        JPanel panelBoton = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBoton.setBackground(Color.WHITE);
        panelBoton.add(botonVerGrafica);

        // Crear panel contenedor con borde
        JPanel panelContenedor = new JPanel(new BorderLayout());
        panelContenedor.setBorder(BorderFactory.createTitledBorder(null, "Evolución del Costo Promedio por Réplica",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            FUENTE_TITULO, COLOR_PRIMARIO));
        panelContenedor.setBackground(Color.WHITE);
        panelContenedor.add(chartPanel, BorderLayout.CENTER);
        panelContenedor.add(panelBoton, BorderLayout.SOUTH);

        // Agregar acción al botón
        botonVerGrafica.addActionListener(e -> mostrarGraficaEnGrande(chartReplicas));

        return panelContenedor;
    }

    /**
     * Muestra la gráfica de réplicas en una ventana independiente más grande
     * @param chart Gráfica a mostrar en grande
     */
    private void mostrarGraficaEnGrande(JFreeChart chart) {
        // Crear nueva ventana para la gráfica grande
        JFrame frameGrafica = new JFrame("Gráfica de Réplicas - Vista Ampliada");

        // Crear panel de la gráfica con tamaño más grande
        ChartPanel chartPanelGrande = new ChartPanel(chart);
        chartPanelGrande.setPreferredSize(new Dimension(1000, 600));
        chartPanelGrande.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        chartPanelGrande.setBackground(Color.WHITE);

        // Configurar opciones del ChartPanel
        chartPanelGrande.setMouseWheelEnabled(true); // Zoom con rueda del ratón
        chartPanelGrande.setMouseZoomable(true); // Zoom con ratón
        chartPanelGrande.setDomainZoomable(true); // Zoom horizontal
        chartPanelGrande.setRangeZoomable(true); // Zoom vertical

        // Panel para botones de control
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        panelControles.setBackground(Color.WHITE);

        // Botón para resetear zoom
        JButton botonResetZoom = new JButton("Resetear Zoom");
        botonResetZoom.setFont(FUENTE_GENERAL);
        botonResetZoom.addActionListener(e -> chartPanelGrande.restoreAutoBounds());

        // Botón para cerrar ventana
        JButton botonCerrar = new JButton("Cerrar");
        botonCerrar.setFont(FUENTE_GENERAL);
        botonCerrar.setBackground(new Color(220, 53, 69)); // Color rojo
        botonCerrar.setForeground(Color.WHITE);
        botonCerrar.setFocusPainted(false);
        botonCerrar.addActionListener(e -> frameGrafica.dispose());

        // Aplicar estilos a los botones
        JButton[] botones = {botonResetZoom, botonCerrar};
        for (JButton boton : botones) {
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            boton.setFocusPainted(false);
        }

        panelControles.add(botonResetZoom);
        panelControles.add(botonCerrar);

        // Panel principal con la gráfica y controles
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.add(chartPanelGrande, BorderLayout.CENTER);
        panelPrincipal.add(panelControles, BorderLayout.SOUTH);

        // Configurar la ventana
        frameGrafica.setContentPane(panelPrincipal);
        frameGrafica.setSize(1100, 750);
        frameGrafica.setLocationRelativeTo(null); // Centrar en pantalla
        frameGrafica.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Solo cerrar esta ventana

        // Hacer visible la ventana
        frameGrafica.setVisible(true);

        // Opcional: Traer ventana al frente
        frameGrafica.toFront();
        frameGrafica.requestFocus();
    }

    /**
     * Crea la gráfica de líneas que muestra la evolución del costo promedio de cada réplica
     * @param costosReplicas Matriz con costos de las 5 réplicas
     * @return Gráfica de líneas configurada
     */
    private JFreeChart crearGraficaLineasReplicas(double[][] costosReplicas) {
        // Crear dataset para múltiples series
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Colores para cada réplica
        Color[] coloresReplicas = {
            new Color(255, 99, 132),   // Rojo/Rosa
            new Color(54, 162, 235),   // Azul
            new Color(255, 205, 86),   // Amarillo
            new Color(75, 192, 192),   // Verde/Turquesa
            new Color(153, 102, 255)   // Púrpura
        };

        // Para cada réplica, calcular promedios acumulados y agregar al dataset
        for (int replica = 0; replica < 5; replica++) {
            String serieNombre = "Réplica " + (replica + 1);

            for (int dia = 0; dia < tamanoRecomendado; dia++) {
                // Calcular promedio acumulado hasta el día actual
                double sumaAcumulada = 0;
                for (int i = 0; i <= dia; i++) {
                    sumaAcumulada += costosReplicas[replica][i];
                }
                double promedioAcumulado = sumaAcumulada / (dia + 1);

                // Agregar punto al dataset
                dataset.addValue(promedioAcumulado, serieNombre, Integer.toString(dia + 1));
            }
        }

        // Crear gráfica de líneas
        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo Promedio Acumulado por Réplica",
            "Día",
            "Costo Promedio Acumulado ($)",
            dataset,
            PlotOrientation.VERTICAL,
            true, // Mostrar leyenda
            true, // Tooltips
            false // URLs
        );

        // Configurar apariencia básica
        configurarAparienciaGrafica(chart, "Evolución del Costo Promedio Acumulado por Réplica");

        // Personalizar colores de las líneas
        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot();
        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer();

        // Configurar cada serie con su color específico
        for (int i = 0; i < 5; i++) {
            renderer.setSeriesPaint(i, coloresReplicas[i]);
            renderer.setSeriesStroke(i, new BasicStroke(2.0f)); // Líneas más gruesas
            renderer.setSeriesShapesVisible(i, false); // Sin puntos en las líneas
        }

        // Configurar fondo del plot
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        return chart;
    }

    /**
     * Crea el panel con estadísticas usando datos ya generados
     * @param costosReplicas Matriz con costos de las réplicas
     * @return Panel con estadísticas
     */
    private JPanel crearPanelEstadisticasReplicasConDatos(double[][] costosReplicas) {
        JPanel panel = new JPanel(new GridLayout(1, 5, 15, 15));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Calcular estadísticas para cada réplica usando los datos ya generados
        for (int i = 0; i < 5; i++) {
            EstadisticasSimulacion stats = calcularEstadisticas(costosReplicas[i]);
            JPanel panelReplica = crearPanelReplicaIndividual(i + 1, stats);
            panel.add(panelReplica);
        }

        return panel;
    }

    /**
     * Crea la tabla usando datos ya generados
     * @param costosReplicas Matriz con costos de las réplicas
     * @return Panel con la tabla
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

        configurarTablaReplicas(tablaReplicas, columnasReplicas);

        // Llenar tabla con datos ya generados
        llenarTablaConPromediosAcumulados(modelReplicas, costosReplicas);

        JScrollPane scrollReplicas = new JScrollPane(tablaReplicas);
        scrollReplicas.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        scrollReplicas.setPreferredSize(new Dimension(1200, 200)); // Altura reducida

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(null, "Tabla de Promedios Acumulados",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            FUENTE_TITULO, COLOR_PRIMARIO));
        panel.add(scrollReplicas, BorderLayout.CENTER);
        panel.setBackground(Color.WHITE);

        return panel;
    }

    /**
     * Simula una réplica completa de forma independiente
     * @return Array con costos totales de cada día de la réplica
     */
    private double[] simularReplicaCompleta() {
        double[] costos = new double[tamanoRecomendado]; // Array para almacenar costos
        int inventarioFinal = 0; // Inventario inicial cero
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda); // Distribución de demanda
        Random random = new Random(); // Nuevo generador para independencia

        // Simular cada día de la réplica
        for (int dia = 0; dia < tamanoRecomendado; dia++) {
            ResultadoSimulacion resultado = simularDia(inventarioFinal, dist, random); // Simular día
            costos[dia] = resultado.costoTotal; // Guardar costo total
            inventarioFinal = resultado.inventarioFinal; // Actualizar inventario para siguiente día
        }

        return costos; // Retornar array de costos de la réplica
    }

    /**
     * Llena la tabla con promedios acumulados de cada réplica
     * @param modelReplicas Modelo donde insertar los datos
     * @param costosReplicas Matriz con costos de todas las réplicas
     */
    private void llenarTablaConPromediosAcumulados(DefaultTableModel modelReplicas, double[][] costosReplicas) {
        // Para cada día, calcular el promedio acumulado de cada réplica
        for (int dia = 0; dia < tamanoRecomendado; dia++) {
            Object[] fila = new Object[6]; // Array para la fila: 1 día + 5 réplicas
            fila[0] = dia + 1; // Número de día (base 1)

            // Calcular promedio acumulado para cada réplica hasta el día actual
            for (int replica = 0; replica < 5; replica++) {
                double sumaAcumulada = 0; // Suma desde día 1 hasta día actual

                // Sumar costos desde el primer día hasta el día actual
                for (int i = 0; i <= dia; i++) {
                    sumaAcumulada += costosReplicas[replica][i];
                }

                // Calcular promedio acumulado: suma total / número de días transcurridos
                double promedioAcumulado = sumaAcumulada / (dia + 1);
                fila[replica + 1] = promedioAcumulado; // Guardar en fila (columnas 1-5)
            }

            modelReplicas.addRow(fila); // Agregar fila completa a la tabla
        }
    }

    /**
     * Configura los estilos específicos de la tabla de réplicas
     * @param tablaReplicas Tabla a configurar
     * @param columnasReplicas Array con nombres de columnas
     */
    private void configurarTablaReplicas(JTable tablaReplicas, String[] columnasReplicas) {
        // Configuración básica
        tablaReplicas.setFont(FUENTE_GENERAL); // Fuente estándar
        tablaReplicas.setRowHeight(28); // Altura de filas
        tablaReplicas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Redimensionar automáticamente
        tablaReplicas.setFillsViewportHeight(true); // Llenar altura completa
        tablaReplicas.getTableHeader().setReorderingAllowed(false); // No permitir reordenar

        // Configurar encabezado
        JTableHeader header = tablaReplicas.getTableHeader();
        header.setBackground(COLOR_PRIMARIO); // Fondo azul
        header.setForeground(Color.WHITE); // Texto blanco
        header.setFont(FUENTE_HEADER); // Fuente en negrita

        // Configurar renderizadores por columna
        configurarRenderizadoresReplicas(tablaReplicas, columnasReplicas.length);
    }

    /**
     * Configura renderizadores específicos para la tabla de réplicas
     * @param tablaReplicas Tabla a configurar
     * @param numColumnas Número total de columnas
     */
    private void configurarRenderizadoresReplicas(JTable tablaReplicas, int numColumnas) {
        // Renderizador para centrar la columna de días
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaReplicas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Renderizador monetario para columnas de réplicas
        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();
        for (int i = 1; i < numColumnas; i++) {
            tablaReplicas.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer);
        }

        // Renderizador personalizado para colores de fondo
        tablaReplicas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                // Obtener componente base
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Aplicar colores de fondo si no está seleccionado
                if (!isSelected) {
                    if (column > 0) { // Columnas de réplicas con fondo amarillo
                        comp.setBackground(COLOR_FONDO_REPLICA);
                    } else { // Columna de días con colores alternados
                        comp.setBackground(row % 2 == 0 ? COLOR_FILA_PAR : COLOR_FILA_IMPAR);
                    }
                }

                // Aplicar formato según tipo de columna
                aplicarFormatoColumna(column, value);

                return comp;
            }

            /**
             * Aplica formato específico según el tipo de columna
             * @param column Índice de la columna
             * @param value Valor de la celda
             */
            private void aplicarFormatoColumna(int column, Object value) {
                if (column == 0) { // Columna de días
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof Number) { // Columnas monetarias
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String && column > 0) { // Texto en columnas monetarias
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
        });
    }

    /**
     * Crea el panel individual con estadísticas de una réplica específica
     * @param numeroReplica Número de la réplica (1-5)
     * @param stats Estadísticas calculadas de la réplica
     * @return Panel configurado con información de la réplica
     */
    private JPanel crearPanelReplicaIndividual(int numeroReplica, EstadisticasSimulacion stats) {
        // Panel con layout vertical
        JPanel panelReplica = new JPanel();
        panelReplica.setLayout(new BoxLayout(panelReplica, BoxLayout.Y_AXIS));

        // Configurar borde con título
        panelReplica.setBorder(BorderFactory.createTitledBorder(null, "Replica " + numeroReplica,
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            FUENTE_REPLICA, COLOR_PRIMARIO));
        panelReplica.setBackground(COLOR_PANEL_REPLICA); // Fondo azul muy claro

        // Crear array de etiquetas con estadísticas
        JLabel[] labels = crearEtiquetasEstadisticas(stats);

        // Agregar todas las etiquetas al panel con espaciado
        for (int j = 0; j < labels.length; j++) {
            labels[j].setAlignmentX(Component.CENTER_ALIGNMENT); // Centrar horizontalmente
            panelReplica.add(labels[j]); // Agregar etiqueta
            if (j < labels.length - 1) { // Agregar espaciado entre etiquetas (excepto la última)
                panelReplica.add(Box.createVerticalStrut(3));
            }
        }

        return panelReplica;
    }

    /**
     * Crea las etiquetas con estadísticas formateadas para una réplica
     * @param stats Estadísticas de la réplica
     * @return Array de etiquetas configuradas
     */
    private JLabel[] crearEtiquetasEstadisticas(EstadisticasSimulacion stats) {
        // Array de etiquetas: título y valor para cada estadística
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

        // Aplicar fuente en negrita a los valores (índices impares)
        for (int j = 0; j < labels.length; j++) {
            if (j % 2 == 1) { // Índices impares son los valores
                labels[j].setFont(FUENTE_VALOR);
            }
        }

        return labels;
    }

    // ========================== CREACIÓN DE GRÁFICAS ==========================

    /**
     * Crea la gráfica de evolución temporal de costos totales
     * @param costosTotales Array con costos de cada día
     * @return Gráfica configurada
     */
    private JFreeChart crearEvolucionGrafica(double[] costosTotales) {
        // Crear dataset para gráfica de líneas
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        // Agregar cada punto: día vs costo total
        for (int i = 0; i < costosTotales.length; i++) {
            dataset.addValue(costosTotales[i], "Costo total", Integer.toString(i + 1));
        }

        // Crear gráfica de líneas
        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo total ($) por Día", // Título
            "Día", // Etiqueta eje X
            "Costo total ($)", // Etiqueta eje Y
            dataset, // Datos
            PlotOrientation.VERTICAL, // Orientación vertical
            false, // Sin leyenda
            true, // Tooltips habilitados
            false // URLs deshabilitadas
        );

        // Configurar apariencia
        configurarAparienciaGrafica(chart, "Evolución del Costo total ($) por Día");

        return chart;
    }

    /**
     * Crea la gráfica Q-Q plot para verificar normalidad
     * @param data Datos observados a analizar
     * @param mean Media de los datos
     * @param stdDev Desviación estándar de los datos
     * @return Gráfica de probabilidad normal
     */
    private JFreeChart crearGraficaProbabilidadNormal(double[] data, double mean, double stdDev) {
        Arrays.sort(data); // Ordenar datos de menor a mayor
        int n = data.length; // Número total de observaciones

        // Crear series para puntos observados y línea teórica
        XYSeries serieObservados = new XYSeries("Datos observados");
        XYSeries serieTeoricos = new XYSeries("Normal Ideal");

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev); // Distribución teórica

        // Calcular cuantiles teóricos y observados
        for (int i = 0; i < n; i++) {
            double percentile = (i + 1.0) / (n + 1.0); // Percentil empírico
            double theoreticalQuantile = normalDist.inverseCumulativeProbability(percentile); // Cuantil teórico

            serieObservados.add(theoreticalQuantile, data[i]); // Punto observado
            serieTeoricos.add(theoreticalQuantile, theoreticalQuantile); // Línea diagonal ideal
        }

        // Crear dataset con ambas series
        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(serieObservados); // Serie de puntos observados
        dataset.addSeries(serieTeoricos); // Serie de línea teórica

        // Crear gráfica de dispersión
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Gráfica Probabilidad Normal - Costo total", // Título
            "Cuantiles teóricos (Normal)", // Etiqueta eje X
            "Datos observados (Costo total)", // Etiqueta eje Y
            dataset, // Datos
            PlotOrientation.VERTICAL, // Orientación vertical
            true, // Mostrar leyenda
            true, // Tooltips habilitados
            false // URLs deshabilitadas
        );

        // Configurar apariencia
        configurarAparienciaGrafica(chart, "Gráfica Probabilidad Normal - Costo total");

        return chart;
    }

    /**
     * Configura la apariencia visual común de las gráficas
     * @param chart Gráfica a configurar
     * @param titulo Título a aplicar
     */
    private void configurarAparienciaGrafica(JFreeChart chart, String titulo) {
        chart.setBackgroundPaint(Color.WHITE); // Fondo blanco

        // Configurar título con estilo
        TextTitle textTitle = new TextTitle(titulo, FUENTE_TITULO);
        textTitle.setPaint(COLOR_PRIMARIO); // Color azul
        chart.setTitle(textTitle); // Aplicar título
    }

    // ========================== CLASES INTERNAS PARA DATOS ==========================

    /**
     * Clase que encapsula todos los resultados de simular un día
     */
    private static class ResultadoSimulacion {
        int inventarioInicial; // Inventario al comenzar el día
        int totalDisponible; // Inventario inicial + producción del día
        int demanda; // Demanda generada aleatoriamente
        int ventas; // Unidades efectivamente vendidas
        int ventasPerdidas; // Demanda no satisfecha
        int inventarioFinal; // Inventario al final del día
        double rn; // Número aleatorio utilizado
        double costoFaltante; // Costo por ventas perdidas
        double costoInventario; // Costo por mantener inventario
        double costoTotal; // Suma de ambos costos
    }

    /**
     * Clase que encapsula estadísticas descriptivas de una muestra
     */
    private static class EstadisticasSimulacion {
        double suma; // Suma de todos los valores
        double sumaCuadrados; // Suma de cuadrados (para varianza)
        double promedio; // Media aritmética
        double varianza; // Varianza de la muestra
        double desviacion; // Desviación estándar
        double minimo; // Valor mínimo observado
        double maximo; // Valor máximo observado
    }

    // ========================== MÉTODO PRINCIPAL ==========================

    /**
     * Punto de entrada de la aplicación
     * @param args Argumentos de línea de comandos (no utilizados)
     */
    public static void main(String[] args) {
        try {
            // Aplicar tema visual FlatLaf para mejor apariencia
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace(); // Mostrar error si no se puede aplicar el tema
        }

        // Ejecutar aplicación en el Event Dispatch Thread de Swing
        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true));
    }
}