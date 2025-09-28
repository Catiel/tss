package actividad_7.aleatorio;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 */
public class SimulacionCompleta extends JFrame {

    // Componentes de la interfaz gráfica
    private JTable tabla;
    private DefaultTableModel model;

    // Instancia del motor de simulación
    private final MotorSimulacion motorSimulacion;

    // Referencia al panel central actual para reemplazo seguro
    private JPanel panelCentralActual;

    /**
     * Constructor que inicializa la ventana principal con simulación de 365 días
     */
    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo");

        int dias = 365;
        motorSimulacion = new MotorSimulacion();

        // Panel superior para controles
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelControles.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelControles.add(new JLabel("Política de producción: "));
        JComboBox<Integer> comboPolitica = new JComboBox<>();
        for (int op : Constantes.OPCIONES_POLITICA) {
            comboPolitica.addItem(op);
        }
        comboPolitica.setSelectedItem(Constantes.POLITICA_PRODUCCION);
        panelControles.add(comboPolitica);

        configurarTabla();

        // Ejecutar simulación completa de 365 días
        double[] costosTotales = motorSimulacion.generarSimulacionYllenarTabla(dias, model);
        motorSimulacion.calcularEstadisticasYPruebas(costosTotales);
        double[] minMax = CreadorPaneles.getMinMaxCosto(model);
        JPanel resumenPanel = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax[0], minMax[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado());
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado");
        botonNuevaTabla.setEnabled(motorSimulacion.isEsNormal());
        resumenPanel.add(botonNuevaTabla);

        // Panel de gráficas
        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion());

        // Panel principal
        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel);
        JPanel panelConControles = new JPanel(new BorderLayout());
        panelConControles.add(panelControles, BorderLayout.NORTH);
        panelConControles.add(mainPanel, BorderLayout.CENTER);
        this.panelCentralActual = mainPanel; // Guardar referencia al panel central actual
        setContentPane(panelConControles);
        setSize(1400, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Acción al cambiar la política
        comboPolitica.addActionListener(e -> {
            int nuevaPolitica = (int) comboPolitica.getSelectedItem();
            Constantes.setPoliticaProduccion(nuevaPolitica);
            // Volver a ejecutar la simulación con la nueva política
            model.setRowCount(0);
            double[] nuevosCostos = motorSimulacion.generarSimulacionYllenarTabla(dias, model);
            motorSimulacion.calcularEstadisticasYPruebas(nuevosCostos);
            double[] minMax2 = CreadorPaneles.getMinMaxCosto(model);
            JPanel nuevoResumen = CreadorPaneles.crearResumenEstadisticoPanel(true,
                motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
                minMax2[0], minMax2[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
                motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado());
            JButton nuevoBoton = new JButton("Generar tabla tamaño recomendado");
            nuevoBoton.setEnabled(motorSimulacion.isEsNormal());
            nuevoResumen.add(nuevoBoton);
            JPanel nuevasGraficas = GeneradorGraficas.crearPanelGraficas(nuevosCostos,
                motorSimulacion.getPromedio(), motorSimulacion.getDesviacion());
            JScrollPane nuevoScroll = new JScrollPane(tabla);
            nuevoScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            JPanel nuevoMainPanel = CreadorPaneles.crearPanelPrincipal(nuevasGraficas, nuevoScroll, nuevoResumen);
            // Remover el panel central actual y agregar el nuevo
            panelConControles.remove(panelCentralActual);
            panelConControles.add(nuevoMainPanel, BorderLayout.CENTER);
            panelConControles.revalidate();
            panelConControles.repaint();
            // Actualizar referencia
            panelCentralActual = nuevoMainPanel;
            // Asignar acción al nuevo botón
            nuevoBoton.addActionListener(this::generarTablaRecomendada);
        });

        // Asignar acción al botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada);
    }

    /**
     * Inicializa y configura la tabla principal con sus estilos
     */
    private void configurarTabla() {
        model = new DefaultTableModel(Constantes.COLUMNAS, 0);
        tabla = new JTable(model);
        ConfiguradorTabla.configurarEstilosTabla(tabla);
    }

    /**
     * Configura el layout y componentes principales de la ventana
     */
    private void configurarInterfazPrincipal(double[] costosTotales, JPanel resumenPanel) {
        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion());

        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel);

        setContentPane(mainPanel);
        setSize(1400, 1000);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     */
    private void generarTablaRecomendada(ActionEvent e) {
        if (!motorSimulacion.isEsNormal() || motorSimulacion.getTamanoRecomendado() < 1) return;

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " +
            motorSimulacion.getTamanoRecomendado() + " corridas");

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(Constantes.COLUMNAS, 0);
        JTable nuevaTabla = new JTable(nuevoModel);
        ConfiguradorTabla.configurarEstilosTabla(nuevaTabla);

        JScrollPane scroll = new JScrollPane(nuevaTabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Generar simulación con tamaño recomendado
        double[] costosTotalesNueva = motorSimulacion.generarSimulacionYllenarTabla(
            motorSimulacion.getTamanoRecomendado(), nuevoModel);
        ModelosDeDatos.EstadisticasSimulacion statsNueva = motorSimulacion.calcularEstadisticas(costosTotalesNueva);

        // Crear panel de resumen para la nueva simulación
        JPanel resumenPanelNueva = CreadorPaneles.crearResumenEstadisticoPanel(false,
            statsNueva.promedio, statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo,
            0, 0, false, 0);

        // Crear botón para generar 5 réplicas
        JButton botonReplicas = new JButton("Generar 5 réplicas");
        botonReplicas.setFont(Constantes.FUENTE_GENERAL);
        resumenPanelNueva.add(botonReplicas);

        // Crear panel de gráficas para la nueva simulación
        JPanel graficasPanelNueva = GeneradorGraficas.crearPanelGraficas(costosTotalesNueva,
            statsNueva.promedio, statsNueva.desviacion);

        // Crear sistema de pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Crear pestaña principal con la simulación completa
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10));
        panelPrincipal.add(scroll, BorderLayout.CENTER);
        panelPrincipal.add(graficasPanelNueva, BorderLayout.NORTH);
        tabbedPane.addTab("Simulación Completa", panelPrincipal);

        // Configurar la nueva ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10));
        frameNuevaTabla.add(tabbedPane, BorderLayout.CENTER);
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH);

        frameNuevaTabla.setSize(1400, 950);
        frameNuevaTabla.setLocationRelativeTo(this);
        frameNuevaTabla.setVisible(true);

        // Crear manejador de réplicas y asignar acción
        ManejadorReplicas manejadorReplicas = new ManejadorReplicas(motorSimulacion, motorSimulacion.getTamanoRecomendado());
        botonReplicas.addActionListener(evt -> manejadorReplicas.generarReplicas(tabbedPane));
    }

    /**
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true));
    }
}
