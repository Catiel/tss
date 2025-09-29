package actividad_7.aleatorio; // Declaración del paquete donde se encuentra la clase

import org.apache.commons.math3.distribution.NormalDistribution; // Importa la clase para distribuciones normales
import org.jfree.chart.ChartFactory; // Importa la fábrica para crear gráficos
import org.jfree.chart.ChartPanel; // Importa el panel para mostrar gráficos
import org.jfree.chart.JFreeChart; // Importa la clase principal de gráficos
import org.jfree.chart.plot.PlotOrientation; // Importa la orientación de los gráficos
import org.jfree.chart.title.TextTitle; // Importa la clase para títulos de gráficos
import org.jfree.data.category.DefaultCategoryDataset; // Importa el dataset para gráficos de categorías
import org.jfree.data.xy.XYSeries; // Importa la serie de datos XY
import org.jfree.data.xy.XYSeriesCollection; // Importa la colección de series XY

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.border.EmptyBorder; // Importa el borde vacío para paneles
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales
import java.awt.BasicStroke; // Importa la clase para grosor de líneas
import java.util.Arrays; // Importa utilidades para arreglos

/**
 * Clase responsable de generar todas las gráficas de la aplicación
 */
public class GeneradorGraficas { // Declaración de la clase GeneradorGraficas

    /**
     * Crea el panel que contiene las dos gráficas principales
     */
    public static JPanel crearPanelGraficas(double[] costosTotales, double promedio, double desviacion) { // Método para crear el panel de gráficas
        JFreeChart chart1 = crearEvolucionGrafica(costosTotales); // Crea la gráfica de evolución de costos
        JFreeChart chart2 = crearGraficaProbabilidadNormal(costosTotales, promedio, desviacion); // Crea la gráfica de probabilidad normal

        ChartPanel chartPanel1 = crearChartPanel(chart1); // Crea el panel para la primera gráfica
        ChartPanel chartPanel2 = crearChartPanel(chart2); // Crea el panel para la segunda gráfica

        JPanel graficasPanel = new JPanel(new GridLayout(1, 2, 15, 15)); // Panel con grid para las dos gráficas
        graficasPanel.add(chartPanel1); // Añade la primera gráfica
        graficasPanel.add(chartPanel2); // Añade la segunda gráfica

        graficasPanel.setBorder(BorderFactory.createTitledBorder(null, "Gráficas",
            javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
            Constantes.FUENTE_TITULO, Constantes.COLOR_PRIMARIO)); // Borde y título del panel
        graficasPanel.setBackground(Color.WHITE); // Fondo blanco

        return graficasPanel; // Devuelve el panel de gráficas
    }

    /**
     * Configura un ChartPanel con las propiedades estándar
     */
    public static ChartPanel crearChartPanel(JFreeChart chart) { // Método para crear un ChartPanel estándar
        ChartPanel chartPanel = new ChartPanel(chart); // Crea el panel del gráfico
        chartPanel.setPreferredSize(new Dimension(1200, 300)); // Tamaño preferido
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12)); // Borde vacío
        chartPanel.setBackground(Color.WHITE); // Fondo blanco
        return chartPanel; // Devuelve el panel
    }

    /**
     * Crea la gráfica de evolución temporal de costos totales
     */
    public static JFreeChart crearEvolucionGrafica(double[] costosTotales) { // Método para crear la gráfica de evolución
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Dataset para la gráfica

        for (int i = 0; i < costosTotales.length; i++) { // Recorre los costos totales
            dataset.addValue(costosTotales[i], "Costo total", Integer.toString(i + 1)); // Añade el valor al dataset
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo total ($) por Día", // Título
            "Día", // Etiqueta eje X
            "Costo total ($)", // Etiqueta eje Y
            dataset, // Datos
            PlotOrientation.VERTICAL, // Orientación vertical
            false, // Sin leyenda
            true, // Tooltips
            false // URLs
        );

        configurarAparienciaGrafica(chart, "Evolución del Costo total ($) por Día"); // Configura la apariencia
        return chart; // Devuelve la gráfica
    }

    /**
     * Crea la gráfica Q-Q plot para verificar normalidad
     */
    public static JFreeChart crearGraficaProbabilidadNormal(double[] data, double mean, double stdDev) { // Método para crear la gráfica Q-Q
        Arrays.sort(data); // Ordena los datos
        int n = data.length; // Número de datos

        XYSeries serieObservados = new XYSeries("Datos observados"); // Serie de datos observados
        XYSeries serieTeoricos = new XYSeries("Normal Ideal"); // Serie de datos teóricos

        NormalDistribution normalDist = new NormalDistribution(mean, stdDev); // Distribución normal teórica

        for (int i = 0; i < n; i++) { // Recorre los datos
            double percentile = (i + 1.0) / (n + 1.0); // Percentil para el dato
            double theoreticalQuantile = normalDist.inverseCumulativeProbability(percentile); // Cuantil teórico

            serieObservados.add(theoreticalQuantile, data[i]); // Añade el punto observado
            serieTeoricos.add(theoreticalQuantile, theoreticalQuantile); // Añade el punto teórico
        }

        XYSeriesCollection dataset = new XYSeriesCollection(); // Colección de series
        dataset.addSeries(serieObservados); // Añade la serie observada
        dataset.addSeries(serieTeoricos); // Añade la serie teórica

        JFreeChart chart = ChartFactory.createScatterPlot(
            "Gráfica Probabilidad Normal - Costo total", // Título
            "Cuantiles teóricos (Normal)", // Eje X
            "Datos observados (Costo total)", // Eje Y
            dataset, // Datos
            PlotOrientation.VERTICAL, // Orientación
            true, // Leyenda
            true, // Tooltips
            false // URLs
        );

        configurarAparienciaGrafica(chart, "Gráfica Probabilidad Normal - Costo total"); // Configura la apariencia
        return chart; // Devuelve la gráfica
    }

    /**
     * Crea la gráfica de líneas que muestra la evolución del costo promedio de cada réplica
     */
    public static JFreeChart crearGraficaLineasReplicas(double[][] costosReplicas, int tamanoRecomendado) { // Método para crear la gráfica de líneas de réplicas
        DefaultCategoryDataset dataset = new DefaultCategoryDataset(); // Dataset para la gráfica

        for (int replica = 0; replica < 5; replica++) { // Recorre las réplicas
            String serieNombre = "Réplica " + (replica + 1); // Nombre de la serie

            for (int dia = 0; dia < tamanoRecomendado; dia++) { // Recorre los días
                double sumaAcumulada = 0; // Inicializa la suma acumulada
                for (int i = 0; i <= dia; i++) { // Suma los costos hasta el día actual
                    sumaAcumulada += costosReplicas[replica][i]; // Acumula el costo
                }
                double promedioAcumulado = sumaAcumulada / (dia + 1); // Calcula el promedio acumulado

                dataset.addValue(promedioAcumulado, serieNombre, Integer.toString(dia + 1)); // Añade el valor al dataset
            }
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Evolución del Costo Promedio Acumulado por Réplica", // Título
            "Día", // Eje X
            "Costo Promedio Acumulado ($)", // Eje Y
            dataset, // Datos
            PlotOrientation.VERTICAL, // Orientación
            true, // Leyenda
            true, // Tooltips
            false // URLs
        );

        configurarAparienciaGrafica(chart, "Evolución del Costo Promedio Acumulado por Réplica"); // Configura la apariencia

        org.jfree.chart.plot.CategoryPlot plot = chart.getCategoryPlot(); // Obtiene el plot de la gráfica
        org.jfree.chart.renderer.category.LineAndShapeRenderer renderer =
            (org.jfree.chart.renderer.category.LineAndShapeRenderer) plot.getRenderer(); // Obtiene el renderizador

        for (int i = 0; i < 5; i++) { // Configura los colores y estilos de las series
            renderer.setSeriesPaint(i, Constantes.COLORES_REPLICAS[i]); // Color de la serie
            renderer.setSeriesStroke(i, new BasicStroke(2.0f)); // Grosor de la línea
            renderer.setSeriesShapesVisible(i, false); // Oculta los puntos
        }

        plot.setBackgroundPaint(Color.WHITE); // Fondo blanco del plot
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY); // Líneas de la grilla en Y
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY); // Líneas de la grilla en X

        return chart; // Devuelve la gráfica
    }

    /**
     * Configura la apariencia visual común de las gráficas
     */
    public static void configurarAparienciaGrafica(JFreeChart chart, String titulo) { // Método para configurar la apariencia
        chart.setBackgroundPaint(Color.WHITE); // Fondo blanco para el gráfico

        TextTitle textTitle = new TextTitle(titulo, Constantes.FUENTE_TITULO); // Crea el título con fuente personalizada
        textTitle.setPaint(Constantes.COLOR_PRIMARIO); // Color del título
        chart.setTitle(textTitle); // Establece el título
    }

    /**
     * Muestra la gráfica de réplicas en una ventana independiente más grande
     */
    public static void mostrarGraficaEnGrande(JFreeChart chart) { // Método para mostrar la gráfica en ventana grande
        JFrame frameGrafica = new JFrame("Gráfica de Réplicas - Vista Ampliada"); // Crea la ventana

        ChartPanel chartPanelGrande = new ChartPanel(chart); // Panel para la gráfica
        chartPanelGrande.setPreferredSize(new Dimension(1000, 600)); // Tamaño preferido
        chartPanelGrande.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Borde vacío
        chartPanelGrande.setBackground(Color.WHITE); // Fondo blanco

        chartPanelGrande.setMouseWheelEnabled(true); // Habilita zoom con rueda del ratón
        chartPanelGrande.setMouseZoomable(true); // Habilita zoom con arrastre
        chartPanelGrande.setDomainZoomable(true); // Habilita zoom en eje X
        chartPanelGrande.setRangeZoomable(true); // Habilita zoom en eje Y

        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Panel para los botones
        panelControles.setBackground(Color.WHITE); // Fondo blanco

        JButton botonResetZoom = new JButton("Resetear Zoom"); // Botón para resetear zoom
        botonResetZoom.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        botonResetZoom.addActionListener(e -> chartPanelGrande.restoreAutoBounds()); // Acción para resetear zoom

        JButton botonCerrar = new JButton("Cerrar"); // Botón para cerrar la ventana
        botonCerrar.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        botonCerrar.setBackground(new Color(220, 53, 69)); // Color de fondo del botón
        botonCerrar.setForeground(Color.WHITE); // Color del texto del botón
        botonCerrar.setFocusPainted(false); // Quita el foco pintado
        botonCerrar.addActionListener(e -> frameGrafica.dispose()); // Acción para cerrar la ventana

        JButton[] botones = {botonResetZoom, botonCerrar}; // Arreglo de botones
        for (JButton boton : botones) { // Configura el cursor y el foco de los botones
            boton.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano
            boton.setFocusPainted(false); // Quita el foco pintado
        }

        panelControles.add(botonResetZoom); // Añade el botón de resetear zoom
        panelControles.add(botonCerrar); // Añade el botón de cerrar

        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal con BorderLayout
        panelPrincipal.setBorder(new EmptyBorder(15, 15, 15, 15)); // Borde vacío
        panelPrincipal.setBackground(Color.WHITE); // Fondo blanco
        panelPrincipal.add(chartPanelGrande, BorderLayout.CENTER); // Añade la gráfica al centro
        panelPrincipal.add(panelControles, BorderLayout.SOUTH); // Añade los controles abajo

        frameGrafica.setContentPane(panelPrincipal); // Establece el contenido de la ventana
        frameGrafica.setSize(1100, 750); // Tamaño de la ventana
        frameGrafica.setLocationRelativeTo(null); // Centra la ventana
        frameGrafica.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Cierra solo esta ventana

        frameGrafica.setVisible(true); // Hace visible la ventana
        frameGrafica.toFront(); // Trae la ventana al frente
        frameGrafica.requestFocus(); // Solicita el foco
    }
}
