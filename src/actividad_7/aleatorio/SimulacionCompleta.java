package actividad_7.aleatorio; // Declaración del paquete donde se encuentra la clase

import com.formdev.flatlaf.FlatLightLaf; // Importa el look and feel FlatLaf para la interfaz

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.border.EmptyBorder; // Importa el borde vacío para paneles
import javax.swing.table.*; // Importa clases para manipulación de tablas
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales
import java.awt.event.ActionEvent; // Importa la clase para eventos de acción

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 */
public class SimulacionCompleta extends JFrame { // Declaración de la clase principal que extiende JFrame

    // Componentes de la interfaz gráfica
    private JTable tabla; // Tabla principal para mostrar los datos
    private DefaultTableModel model; // Modelo de la tabla principal

    // Instancia del motor de simulación
    private final MotorSimulacion motorSimulacion; // Motor que ejecuta la simulación

    // Referencia al panel central actual para reemplazo seguro
    private JPanel panelCentralActual; // Panel central actual de la interfaz

    /**
     * Constructor que inicializa la ventana principal con simulación de 365 días
     */
    public SimulacionCompleta() { // Constructor de la clase
        super("Simulación de Inventario - Análisis Completo"); // Título de la ventana

        int dias = 365; // Número de días a simular
        motorSimulacion = new MotorSimulacion(); // Instancia del motor de simulación

        // Panel superior para controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel para controles superiores
        panelControles.setBorder(new EmptyBorder(10, 10, 10, 10)); // Borde del panel de controles
        panelControles.add(new JLabel("Política de producción: ")); // Etiqueta para el combo
        JComboBox<Integer> comboPolitica = new JComboBox<>(); // ComboBox para seleccionar política
        for (int op : Constantes.OPCIONES_POLITICA) { // Añade opciones al combo
            comboPolitica.addItem(op);
        }
        comboPolitica.setSelectedItem(Constantes.POLITICA_PRODUCCION); // Selecciona la política actual
        panelControles.add(comboPolitica); // Añade el combo al panel

        configurarTabla(); // Inicializa y configura la tabla principal

        // Ejecutar simulación completa de 365 días
        double[] costosTotales = motorSimulacion.generarSimulacionYllenarTabla(dias, model); // Ejecuta la simulación y llena la tabla
        motorSimulacion.calcularEstadisticasYPruebas(costosTotales); // Calcula estadísticas y pruebas de normalidad
        double[] minMax = CreadorPaneles.getMinMaxCosto(model); // Obtiene el mínimo y máximo de costos
        JPanel resumenPanel = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax[0], minMax[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado()); // Panel de resumen estadístico
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado"); // Botón para nueva tabla
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        botonNuevaTabla.setEnabled(motorSimulacion.getTamanoRecomendado() > 0); // Habilita el botón si hay tamaño recomendado
        resumenPanel.add(botonNuevaTabla); // Añade el botón al panel de resumen

        // Panel de gráficas
        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas

        // Panel principal
        JScrollPane scrollPaneTabla = new JScrollPane(tabla); // Scroll para la tabla
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde del scroll
        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel); // Panel principal
        JPanel panelConControles = new JPanel(new BorderLayout()); // Panel que contiene controles y el mainPanel
        panelConControles.add(panelControles, BorderLayout.NORTH); // Añade controles arriba
        panelConControles.add(mainPanel, BorderLayout.CENTER); // Añade el panel principal al centro
        this.panelCentralActual = mainPanel; // Guardar referencia al panel central actual
        setContentPane(panelConControles); // Establece el contenido de la ventana
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra la aplicación al salir

        // Acción al cambiar la política
        comboPolitica.addActionListener(e -> { // Listener para cambios en el combo
            int nuevaPolitica = (int) comboPolitica.getSelectedItem(); // Obtiene la nueva política
            Constantes.setPoliticaProduccion(nuevaPolitica); // Actualiza la política
            // Volver a ejecutar la simulación con la nueva política
            model.setRowCount(0); // Limpia la tabla
            double[] nuevosCostos = motorSimulacion.generarSimulacionYllenarTabla(dias, model); // Nueva simulación
            motorSimulacion.calcularEstadisticasYPruebas(nuevosCostos); // Nuevas estadísticas
            double[] minMax2 = CreadorPaneles.getMinMaxCosto(model); // Nuevo min y max
            JPanel nuevoResumen = CreadorPaneles.crearResumenEstadisticoPanel(true,
                motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
                minMax2[0], minMax2[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
                motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado()); // Nuevo panel de resumen
            JButton nuevoBoton = new JButton("Generar tabla tamaño recomendado"); // Nuevo botón
            // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
            nuevoBoton.setEnabled(motorSimulacion.getTamanoRecomendado() > 0); // Habilita el botón si hay tamaño recomendado
            nuevoResumen.add(nuevoBoton); // Añade el botón al resumen
            JPanel nuevasGraficas = GeneradorGraficas.crearPanelGraficas(nuevosCostos,
                motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Nuevas gráficas
            JScrollPane nuevoScroll = new JScrollPane(tabla); // Nuevo scroll para la tabla
            nuevoScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde del scroll
            JPanel nuevoMainPanel = CreadorPaneles.crearPanelPrincipal(nuevasGraficas, nuevoScroll, nuevoResumen); // Nuevo panel principal
            // Remover el panel central actual y agregar el nuevo
            panelConControles.remove(panelCentralActual); // Quita el panel anterior
            panelConControles.add(nuevoMainPanel, BorderLayout.CENTER); // Añade el nuevo panel
            panelConControles.revalidate(); // Revalida el layout
            panelConControles.repaint(); // Repinta la interfaz
            // Actualizar referencia
            panelCentralActual = nuevoMainPanel; // Actualiza la referencia
            // Asignar acción al nuevo botón
            nuevoBoton.addActionListener(this::generarTablaRecomendada); // Listener para el nuevo botón
        });

        // Asignar acción al botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada); // Listener para el botón principal
    }

    /**
     * Inicializa y configura la tabla principal con sus estilos
     */
    private void configurarTabla() { // Método para configurar la tabla principal
        model = new DefaultTableModel(Constantes.COLUMNAS, 0); // Modelo de la tabla
        tabla = new JTable(model); // Crea la tabla
        ConfiguradorTabla.configurarEstilosTabla(tabla); // Aplica estilos a la tabla
    }

    /**
     * Configura el layout y componentes principales de la ventana
     */
    private void configurarInterfazPrincipal(double[] costosTotales, JPanel resumenPanel) { // Método para configurar la interfaz principal
        JScrollPane scrollPaneTabla = new JScrollPane(tabla); // Scroll para la tabla
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde del scroll

        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas

        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel); // Panel principal

        setContentPane(mainPanel); // Establece el contenido de la ventana
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra la aplicación al salir
    }

    /**
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     */
    private void generarTablaRecomendada(ActionEvent e) { // Método para generar la tabla recomendada
        if (motorSimulacion.getTamanoRecomendado() < 1) return; // Si no hay tamaño recomendado, no hace nada

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " +
            motorSimulacion.getTamanoRecomendado() + " corridas"); // Nueva ventana para la tabla recomendada

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(Constantes.COLUMNAS, 0); // Modelo de la nueva tabla
        JTable nuevaTabla = new JTable(nuevoModel); // Nueva tabla
        ConfiguradorTabla.configurarEstilosTabla(nuevaTabla); // Aplica estilos

        JScrollPane scroll = new JScrollPane(nuevaTabla); // Scroll para la nueva tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde del scroll

        // Generar simulación con tamaño recomendado
        double[] costosTotalesNueva = motorSimulacion.generarSimulacionYllenarTabla(
            motorSimulacion.getTamanoRecomendado(), nuevoModel); // Ejecuta la simulación
        ModelosDeDatos.EstadisticasSimulacion statsNueva = motorSimulacion.calcularEstadisticas(costosTotalesNueva); // Calcula estadísticas

        // Crear panel de resumen para la nueva simulación
        JPanel resumenPanelNueva = CreadorPaneles.crearResumenEstadisticoPanel(false,
            statsNueva.promedio, statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo,
            0, 0, false, 0); // Panel de resumen para la nueva simulación

        // Crear botón para generar 5 réplicas
        JButton botonReplicas = new JButton("Generar 5 réplicas"); // Botón para réplicas
        botonReplicas.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        resumenPanelNueva.add(botonReplicas); // Añade el botón al resumen

        // Crear panel de gráficas para la nueva simulación
        JPanel graficasPanelNueva = GeneradorGraficas.crearPanelGraficas(costosTotalesNueva,
            statsNueva.promedio, statsNueva.desviacion); // Panel de gráficas

        // Crear sistema de pestañas
        JTabbedPane tabbedPane = new JTabbedPane(); // Sistema de pestañas

        // Crear pestaña principal con la simulación completa
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal de la pestaña
        panelPrincipal.add(scroll, BorderLayout.CENTER); // Añade la tabla
        panelPrincipal.add(graficasPanelNueva, BorderLayout.NORTH); // Añade las gráficas
        tabbedPane.addTab("Simulación Completa", panelPrincipal); // Añade la pestaña principal

        // Configurar la nueva ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10)); // Layout de la ventana
        frameNuevaTabla.add(tabbedPane, BorderLayout.CENTER); // Añade las pestañas
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH); // Añade el resumen

        frameNuevaTabla.setSize(1400, 950); // Tamaño de la ventana
        frameNuevaTabla.setLocationRelativeTo(this); // Centra la ventana respecto a la principal
        frameNuevaTabla.setVisible(true); // Hace visible la ventana

        // Crear manejador de réplicas y asignar acción
        ManejadorReplicas manejadorReplicas = new ManejadorReplicas(motorSimulacion, motorSimulacion.getTamanoRecomendado()); // Manejador de réplicas
        botonReplicas.addActionListener(evt -> manejadorReplicas.generarReplicas(tabbedPane)); // Listener para el botón de réplicas
    }

    /**
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) { // Método main de la aplicación
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece el look and feel
        } catch (Exception e) {
            e.printStackTrace(); // Imprime errores si los hay
        }

        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true)); // Lanza la aplicación en el hilo de eventos
    }
}