package actividad_7.predeterminado;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 */
public class SimulacionCompleta extends JFrame { // Declaración de la clase principal que hereda de JFrame

    // Componentes de la interfaz gráfica
    private JTable tabla; // Tabla principal para mostrar datos
    private DefaultTableModel model; // Modelo de la tabla principal

    // Instancia del motor de simulación
    private final MotorSimulacion motorSimulacion; // Motor de simulación para cálculos y lógica

    // Referencia al panel central actual para reemplazo seguro
    private JPanel panelCentralActual; // Panel central actual de la ventana

    /**
     * Constructor que inicializa la ventana principal con simulación de 365 días
     */
    public SimulacionCompleta() { // Constructor de la clase
        super("Simulación de Inventario - Análisis Completo"); // Llama al constructor de JFrame con título

        int dias = 365; // Número de días para la simulación inicial
        motorSimulacion = new MotorSimulacion(); // Crea la instancia del motor de simulación

        // Panel superior para controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel para controles con FlowLayout a la izquierda
        panelControles.setBorder(new EmptyBorder(10, 10, 10, 10)); // Borde vacío
        panelControles.add(new JLabel("Política de producción: ")); // Etiqueta para la política
        JComboBox<Integer> comboPolitica = new JComboBox<>(); // ComboBox para seleccionar política
        for (int op : Constantes.OPCIONES_POLITICA) { // Agrega opciones al ComboBox
            comboPolitica.addItem(op);
        }
        comboPolitica.setSelectedItem(Constantes.POLITICA_PRODUCCION); // Selecciona la política actual
        panelControles.add(comboPolitica); // Agrega el ComboBox al panel

        configurarTabla(); // Inicializa y configura la tabla principal

        // Ejecutar simulación completa de 365 días
        double[] costosTotales = motorSimulacion.generarSimulacionYllenarTabla(dias, model); // Ejecuta la simulación y llena la tabla
        motorSimulacion.calcularEstadisticasYPruebas(costosTotales); // Calcula estadísticas y pruebas de normalidad
        double[] minMax = CreadorPaneles.getMinMaxCosto(model); // Obtiene el mínimo y máximo de costos
        JPanel resumenPanel = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax[0], minMax[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado()); // Panel de resumen estadístico
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado"); // Botón para generar tabla con tamaño recomendado
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        botonNuevaTabla.setEnabled(motorSimulacion.getTamanoRecomendado() > 0); // Habilita el botón si hay tamaño recomendado
        resumenPanel.add(botonNuevaTabla); // Agrega el botón al panel de resumen

        // Panel de gráficas
        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas

        // Panel principal
        JScrollPane scrollPaneTabla = new JScrollPane(tabla); // Scroll para la tabla
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel); // Panel principal con gráficas, tabla y resumen
        JPanel panelConControles = new JPanel(new BorderLayout()); // Panel que contiene controles y el panel principal
        panelConControles.add(panelControles, BorderLayout.NORTH); // Agrega controles arriba
        panelConControles.add(mainPanel, BorderLayout.CENTER); // Agrega el panel principal al centro
        this.panelCentralActual = mainPanel; // Guardar referencia al panel central actual
        setContentPane(panelConControles); // Establece el contenido de la ventana
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra la aplicación al cerrar la ventana

        // Acción al cambiar la política
        comboPolitica.addActionListener(e -> { // Listener para cambios en el ComboBox
            int nuevaPolitica = (int) comboPolitica.getSelectedItem(); // Obtiene la nueva política
            Constantes.setPoliticaProduccion(nuevaPolitica); // Actualiza la política en las constantes
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
            nuevoResumen.add(nuevoBoton); // Agrega el botón al resumen
            JPanel nuevasGraficas = GeneradorGraficas.crearPanelGraficas(nuevosCostos,
                motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Nuevas gráficas
            JScrollPane nuevoScroll = new JScrollPane(tabla); // Nuevo scroll para la tabla
            nuevoScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
            JPanel nuevoMainPanel = CreadorPaneles.crearPanelPrincipal(nuevasGraficas, nuevoScroll, nuevoResumen); // Nuevo panel principal
            // Remover el panel central actual y agregar el nuevo
            panelConControles.remove(panelCentralActual); // Quita el panel anterior
            panelConControles.add(nuevoMainPanel, BorderLayout.CENTER); // Agrega el nuevo panel
            panelConControles.revalidate(); // Revalida el layout
            panelConControles.repaint(); // Repinta la ventana
            // Actualizar referencia
            panelCentralActual = nuevoMainPanel; // Actualiza la referencia al panel central
            // Asignar acción al nuevo botón
            nuevoBoton.addActionListener(this::generarTablaRecomendada); // Listener para el nuevo botón
        });

        // Asignar acción al botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada); // Listener para el botón de la tabla recomendada
    }

    /**
     * Inicializa y configura la tabla principal con sus estilos
     */
    private void configurarTabla() {
        model = new DefaultTableModel(Constantes.COLUMNAS, 0); // Crea el modelo de la tabla
        tabla = new JTable(model); // Crea la tabla
        ConfiguradorTabla.configurarEstilosTabla(tabla); // Aplica estilos a la tabla
    }

    /**
     * Configura el layout y componentes principales de la ventana
     */
    private void configurarInterfazPrincipal(double[] costosTotales, JPanel resumenPanel) {
        JScrollPane scrollPaneTabla = new JScrollPane(tabla); // Scroll para la tabla
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío

        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas

        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel); // Panel principal

        setContentPane(mainPanel); // Establece el contenido de la ventana
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra la aplicación al cerrar la ventana
    }

    /**
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     */
    private void generarTablaRecomendada(ActionEvent e) {
        if (motorSimulacion.getTamanoRecomendado() < 1) return; // Si no hay tamaño recomendado, no hace nada

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " +
            motorSimulacion.getTamanoRecomendado() + " corridas"); // Nueva ventana para la simulación recomendada

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(Constantes.COLUMNAS, 0); // Modelo de la nueva tabla
        JTable nuevaTabla = new JTable(nuevoModel); // Nueva tabla
        ConfiguradorTabla.configurarEstilosTabla(nuevaTabla); // Aplica estilos

        JScrollPane scroll = new JScrollPane(nuevaTabla); // Scroll para la nueva tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío

        // Generar simulación con tamaño recomendado
        double[] costosTotalesNueva = motorSimulacion.generarSimulacionYllenarTabla(
            motorSimulacion.getTamanoRecomendado(), nuevoModel); // Ejecuta la simulación
        ModelosDeDatos.EstadisticasSimulacion statsNueva = motorSimulacion.calcularEstadisticas(costosTotalesNueva); // Calcula estadísticas

        // Crear panel de resumen para la nueva simulación
        JPanel resumenPanelNueva = CreadorPaneles.crearResumenEstadisticoPanel(false,
            statsNueva.promedio, statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo,
            0, 0, false, 0); // Panel de resumen

        // Crear botón para generar 5 réplicas
        JButton botonReplicas = new JButton("Generar 5 réplicas"); // Botón para réplicas
        botonReplicas.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        resumenPanelNueva.add(botonReplicas); // Agrega el botón al resumen

        // Crear panel de gráficas para la nueva simulación
        JPanel graficasPanelNueva = GeneradorGraficas.crearPanelGraficas(costosTotalesNueva,
            statsNueva.promedio, statsNueva.desviacion); // Panel de gráficas

        // Crear sistema de pestañas
        JTabbedPane tabbedPane = new JTabbedPane(); // Sistema de pestañas

        // Crear pestaña principal con la simulación completa
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal
        panelPrincipal.add(scroll, BorderLayout.CENTER); // Agrega la tabla
        panelPrincipal.add(graficasPanelNueva, BorderLayout.NORTH); // Agrega las gráficas
        tabbedPane.addTab("Simulación Completa", panelPrincipal); // Agrega la pestaña

        // Configurar la nueva ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10)); // Layout de la ventana
        frameNuevaTabla.add(tabbedPane, BorderLayout.CENTER); // Agrega el sistema de pestañas
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH); // Agrega el resumen

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
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece el look and feel
        } catch (Exception e) {
            e.printStackTrace(); // Imprime el error si ocurre
        }

        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true)); // Lanza la aplicación en el hilo de eventos
    }
}