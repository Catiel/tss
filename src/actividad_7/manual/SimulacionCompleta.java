package actividad_7.manual;

import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 * MODIFICADO: Permite entrada manual de valores Rn
 */
public class SimulacionCompleta extends JFrame {

    // Componentes de la interfaz gráfica
    private JTable tabla;
    private DefaultTableModel model;

    // Instancia del motor de simulación
    private final MotorSimulacion motorSimulacion;

    // Referencia al panel central actual para reemplazo seguro
    private JPanel panelCentralActual;

    // NUEVO: Array para almacenar valores Rn manuales
    private double[] valoresRnManuales;

    /**
     * Constructor que inicializa la ventana principal con simulación de 365 días
     */
    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo (Entrada Manual Rn)");

        int dias = 365;
        motorSimulacion = new MotorSimulacion();

        // Panel superior para controles
        JPanel panelControles = crearPanelControles(dias);

        configurarTabla();

        // MODIFICADO: Solicitar valores Rn manuales al inicio
        solicitarValoresRnManuales(dias);

        // Ejecutar simulación completa de 365 días con valores manuales
        double[] costosTotales = motorSimulacion.generarSimulacionYllenarTabla(dias, model, valoresRnManuales);
        motorSimulacion.calcularEstadisticasYPruebas(costosTotales);
        double[] minMax = CreadorPaneles.getMinMaxCosto(model);
        JPanel resumenPanel = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax[0], minMax[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado());
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado");
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        botonNuevaTabla.setEnabled(motorSimulacion.getTamanoRecomendado() > 0);
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

        // Asignar acción al botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada);

        // Configurar acciones de los controles en el constructor
        configurarAccionesControles(panelControles, dias);
    }

    /**
     * NUEVO: Crea el panel de controles con botones adicionales
     */
    private JPanel crearPanelControles(int dias) {
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panelControles.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelControles.setBackground(Color.WHITE);

        // Control de política de producción
        panelControles.add(new JLabel("Política de producción: "));
        JComboBox<Integer> comboPolitica = new JComboBox<>();
        for (int op : Constantes.OPCIONES_POLITICA) {
            comboPolitica.addItem(op);
        }
        comboPolitica.setSelectedItem(Constantes.POLITICA_PRODUCCION);
        comboPolitica.setName("comboPolitica");
        panelControles.add(comboPolitica);

        // Separador
        panelControles.add(Box.createHorizontalStrut(20));

        // NUEVO: Botón para modificar valores Rn
        JButton botonModificarRn = new JButton("Modificar Valores Rn");
        botonModificarRn.setFont(Constantes.FUENTE_GENERAL);
        botonModificarRn.setBackground(new Color(40, 167, 69));
        botonModificarRn.setForeground(Color.WHITE);
        botonModificarRn.setFocusPainted(false);
        botonModificarRn.setName("botonModificarRn");
        panelControles.add(botonModificarRn);

        // NUEVO: Botón para regenerar simulación
        JButton botonRegenerarSimulacion = new JButton("Regenerar Simulación");
        botonRegenerarSimulacion.setFont(Constantes.FUENTE_GENERAL);
        botonRegenerarSimulacion.setBackground(new Color(255, 193, 7));
        botonRegenerarSimulacion.setForeground(Color.BLACK);
        botonRegenerarSimulacion.setFocusPainted(false);
        botonRegenerarSimulacion.setName("botonRegenerar");
        panelControles.add(botonRegenerarSimulacion);

        return panelControles;
    }

    /**
     * NUEVO: Configura las acciones de los controles
     */
    private void configurarAccionesControles(JPanel panelControles, int dias) {
        // Buscar componentes por nombre
        JComboBox<Integer> comboPolitica = null;
        JButton botonModificarRn = null;
        JButton botonRegenerar = null;

        for (Component comp : panelControles.getComponents()) {
            if (comp.getName() != null) {
                switch (comp.getName()) {
                    case "comboPolitica":
                        comboPolitica = (JComboBox<Integer>) comp;
                        break;
                    case "botonModificarRn":
                        botonModificarRn = (JButton) comp;
                        break;
                    case "botonRegenerar":
                        botonRegenerar = (JButton) comp;
                        break;
                }
            }
        }

        // Configurar acciones
        if (comboPolitica != null) {
            JComboBox<Integer> finalComboPolitica = comboPolitica;
            comboPolitica.addActionListener(e -> {
                int nuevaPolitica = (int) finalComboPolitica.getSelectedItem();
                Constantes.setPoliticaProduccion(nuevaPolitica);
                regenerarSimulacionConValoresActuales(dias);
            });
        }

        if (botonModificarRn != null) {
            botonModificarRn.addActionListener(e -> {
                EntradaManualDialog dialog = new EntradaManualDialog(this, dias);
                // Establecer valores actuales en el diálogo
                if (valoresRnManuales != null) {
                    System.arraycopy(valoresRnManuales, 0, dialog.getValoresRn(), 0, Math.min(valoresRnManuales.length, dialog.getValoresRn().length));
                }
                dialog.setVisible(true);

                if (dialog.isConfirmado()) {
                    valoresRnManuales = dialog.getValoresRn();
                    regenerarSimulacionConValoresActuales(dias);
                }
            });
        }

        if (botonRegenerar != null) {
            botonRegenerar.addActionListener(e -> regenerarSimulacionConValoresActuales(dias));
        }
    }

    /**
     * NUEVO: Solicita valores Rn manuales al usuario
     */
    private void solicitarValoresRnManuales(int dias) {
        EntradaManualDialog dialog = new EntradaManualDialog(this, dias);
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            valoresRnManuales = dialog.getValoresRn();
        } else {
            // Si cancela, generar valores aleatorios por defecto
            valoresRnManuales = new double[dias];
            for (int i = 0; i < dias; i++) {
                valoresRnManuales[i] = Math.random();
            }
        }
    }

    /**
     * NUEVO: Regenera la simulación con los valores Rn actuales
     */
    private void regenerarSimulacionConValoresActuales(int dias) {
        // Volver a ejecutar la simulación con los valores Rn manuales actuales
        model.setRowCount(0);
        double[] nuevosCostos = motorSimulacion.generarSimulacionYllenarTabla(dias, model, valoresRnManuales);
        motorSimulacion.calcularEstadisticasYPruebas(nuevosCostos);
        double[] minMax2 = CreadorPaneles.getMinMaxCosto(model);
        JPanel nuevoResumen = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax2[0], minMax2[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado());
        JButton nuevoBoton = new JButton("Generar tabla tamaño recomendado");
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        nuevoBoton.setEnabled(motorSimulacion.getTamanoRecomendado() > 0);
        nuevoResumen.add(nuevoBoton);
        JPanel nuevasGraficas = GeneradorGraficas.crearPanelGraficas(nuevosCostos,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion());
        JScrollPane nuevoScroll = new JScrollPane(tabla);
        nuevoScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JPanel nuevoMainPanel = CreadorPaneles.crearPanelPrincipal(nuevasGraficas, nuevoScroll, nuevoResumen);

        // Remover el panel central actual y agregar el nuevo
        JPanel panelConControles = (JPanel) getContentPane();
        panelConControles.remove(panelCentralActual);
        panelConControles.add(nuevoMainPanel, BorderLayout.CENTER);
        panelConControles.revalidate();
        panelConControles.repaint();
        // Actualizar referencia
        panelCentralActual = nuevoMainPanel;
        // Asignar acción al nuevo botón
        nuevoBoton.addActionListener(this::generarTablaRecomendada);
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
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     */
    private void generarTablaRecomendada(ActionEvent e) {
        if (motorSimulacion.getTamanoRecomendado() < 1) return;

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " +
            motorSimulacion.getTamanoRecomendado() + " corridas");

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(Constantes.COLUMNAS, 0);
        JTable nuevaTabla = new JTable(nuevoModel);
        ConfiguradorTabla.configurarEstilosTabla(nuevaTabla);

        JScrollPane scroll = new JScrollPane(nuevaTabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // MODIFICADO: Para tabla recomendada, usar valores aleatorios (como réplicas)
        // Generar simulación con tamaño recomendado usando método original (aleatorio)
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