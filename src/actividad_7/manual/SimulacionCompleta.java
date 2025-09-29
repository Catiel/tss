package actividad_7.manual;

// Importa el tema visual FlatLaf para Swing
import com.formdev.flatlaf.FlatLightLaf;

// Importa componentes de Swing
import javax.swing.*;
// Importa borde vacío para paneles
import javax.swing.border.EmptyBorder;
// Importa el modelo de tabla por defecto
import javax.swing.table.DefaultTableModel;
// Importa utilidades de diseño y eventos
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Clase principal que simula un sistema de inventario con análisis estadístico completo
 * Incluye análisis de normalidad, cálculo de tamaño de muestra y generación de réplicas
 * MODIFICADO: Permite entrada manual de valores Rn
 */
public class SimulacionCompleta extends JFrame {

    // Componentes de la interfaz gráfica
    private JTable tabla; // Tabla principal de resultados
    private DefaultTableModel model; // Modelo de la tabla

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
        super("Simulación de Inventario - Análisis Completo (Entrada Manual Rn)"); // Título de la ventana

        int dias = 365; // Número de días a simular
        motorSimulacion = new MotorSimulacion(); // Instancia del motor de simulación

        // Panel superior para controles
        JPanel panelControles = crearPanelControles(dias); // Panel de controles

        configurarTabla(); // Configura la tabla principal

        // MODIFICADO: Solicitar valores Rn manuales al inicio
        solicitarValoresRnManuales(dias); // Solicita los valores Rn

        // Ejecutar simulación completa de 365 días con valores manuales
        double[] costosTotales = motorSimulacion.generarSimulacionYllenarTabla(dias, model, valoresRnManuales); // Ejecuta simulación
        motorSimulacion.calcularEstadisticasYPruebas(costosTotales); // Calcula estadísticas
        double[] minMax = CreadorPaneles.getMinMaxCosto(model); // Obtiene mínimo y máximo
        JPanel resumenPanel = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax[0], minMax[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado()); // Panel de resumen
        JButton botonNuevaTabla = new JButton("Generar tabla tamaño recomendado"); // Botón para nueva tabla
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        botonNuevaTabla.setEnabled(motorSimulacion.getTamanoRecomendado() > 0); // Habilita el botón si hay tamaño recomendado
        resumenPanel.add(botonNuevaTabla); // Agrega el botón al panel de resumen

        // Panel de gráficas
        JPanel graficasPanel = GeneradorGraficas.crearPanelGraficas(costosTotales,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas

        // Panel principal
        JScrollPane scrollPaneTabla = new JScrollPane(tabla); // Scroll para la tabla
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
        JPanel mainPanel = CreadorPaneles.crearPanelPrincipal(graficasPanel, scrollPaneTabla, resumenPanel); // Panel principal
        JPanel panelConControles = new JPanel(new BorderLayout()); // Panel con controles y principal
        panelConControles.add(panelControles, BorderLayout.NORTH); // Agrega controles arriba
        panelConControles.add(mainPanel, BorderLayout.CENTER); // Agrega panel principal al centro
        this.panelCentralActual = mainPanel; // Guardar referencia al panel central actual
        setContentPane(panelConControles); // Establece el contenido de la ventana
        setSize(1400, 1000); // Tamaño de la ventana
        setLocationRelativeTo(null); // Centra la ventana
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra la aplicación al salir

        // Asignar acción al botón
        botonNuevaTabla.addActionListener(this::generarTablaRecomendada); // Acción para generar tabla recomendada

        // Configurar acciones de los controles en el constructor
        configurarAccionesControles(panelControles, dias); // Configura acciones de controles
    }

    /**
     * NUEVO: Crea el panel de controles con botones adicionales
     */
    private JPanel crearPanelControles(int dias) {
        JPanel panelControles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Layout de controles
        panelControles.setBorder(new EmptyBorder(10, 10, 10, 10)); // Borde vacío
        panelControles.setBackground(Color.WHITE); // Fondo blanco

        // Control de política de producción
        panelControles.add(new JLabel("Política de producción: ")); // Etiqueta
        JComboBox<Integer> comboPolitica = new JComboBox<>(); // ComboBox para política
        for (int op : Constantes.OPCIONES_POLITICA) {
            comboPolitica.addItem(op); // Agrega opciones
        }
        comboPolitica.setSelectedItem(Constantes.POLITICA_PRODUCCION); // Selecciona la política actual
        comboPolitica.setName("comboPolitica"); // Nombre para búsqueda
        panelControles.add(comboPolitica); // Agrega el combo al panel

        // Separador
        panelControles.add(Box.createHorizontalStrut(20)); // Espaciado

        // NUEVO: Botón para modificar valores Rn
        JButton botonModificarRn = new JButton("Modificar Valores Rn"); // Botón para modificar Rn
        botonModificarRn.setFont(Constantes.FUENTE_GENERAL); // Fuente
        botonModificarRn.setBackground(new Color(40, 167, 69)); // Fondo verde
        botonModificarRn.setForeground(Color.WHITE); // Texto blanco
        botonModificarRn.setFocusPainted(false); // Sin foco
        botonModificarRn.setName("botonModificarRn"); // Nombre para búsqueda
        panelControles.add(botonModificarRn); // Agrega el botón

        // NUEVO: Botón para regenerar simulación
        JButton botonRegenerarSimulacion = new JButton("Regenerar Simulación"); // Botón para regenerar
        botonRegenerarSimulacion.setFont(Constantes.FUENTE_GENERAL); // Fuente
        botonRegenerarSimulacion.setBackground(new Color(255, 193, 7)); // Fondo amarillo
        botonRegenerarSimulacion.setForeground(Color.BLACK); // Texto negro
        botonRegenerarSimulacion.setFocusPainted(false); // Sin foco
        botonRegenerarSimulacion.setName("botonRegenerar"); // Nombre para búsqueda
        panelControles.add(botonRegenerarSimulacion); // Agrega el botón

        return panelControles; // Devuelve el panel de controles
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
                int nuevaPolitica = (int) finalComboPolitica.getSelectedItem(); // Nueva política
                Constantes.setPoliticaProduccion(nuevaPolitica); // Actualiza la política
                regenerarSimulacionConValoresActuales(dias); // Regenera la simulación
            });
        }

        if (botonModificarRn != null) {
            botonModificarRn.addActionListener(e -> {
                EntradaManualDialog dialog = new EntradaManualDialog(this, dias); // Diálogo para entrada manual
                // Establecer valores actuales en el diálogo
                if (valoresRnManuales != null) {
                    System.arraycopy(valoresRnManuales, 0, dialog.getValoresRn(), 0, Math.min(valoresRnManuales.length, dialog.getValoresRn().length));
                }
                dialog.setVisible(true);

                if (dialog.isConfirmado()) {
                    valoresRnManuales = dialog.getValoresRn(); // Actualiza los valores Rn
                    regenerarSimulacionConValoresActuales(dias); // Regenera la simulación
                }
            });
        }

        if (botonRegenerar != null) {
            botonRegenerar.addActionListener(e -> regenerarSimulacionConValoresActuales(dias)); // Regenera la simulación
        }
    }

    /**
     * NUEVO: Solicita valores Rn manuales al usuario
     */
    private void solicitarValoresRnManuales(int dias) {
        EntradaManualDialog dialog = new EntradaManualDialog(this, dias); // Diálogo para entrada manual
        dialog.setVisible(true);

        if (dialog.isConfirmado()) {
            valoresRnManuales = dialog.getValoresRn(); // Asigna los valores ingresados
        } else {
            // Si cancela, generar valores aleatorios por defecto
            valoresRnManuales = new double[dias]; // Arreglo de valores
            for (int i = 0; i < dias; i++) {
                valoresRnManuales[i] = Math.random(); // Valor aleatorio
            }
        }
    }

    /**
     * NUEVO: Regenera la simulación con los valores Rn actuales
     */
    private void regenerarSimulacionConValoresActuales(int dias) {
        // Volver a ejecutar la simulación con los valores Rn manuales actuales
        model.setRowCount(0); // Limpia la tabla
        double[] nuevosCostos = motorSimulacion.generarSimulacionYllenarTabla(dias, model, valoresRnManuales); // Ejecuta simulación
        motorSimulacion.calcularEstadisticasYPruebas(nuevosCostos); // Calcula estadísticas
        double[] minMax2 = CreadorPaneles.getMinMaxCosto(model); // Obtiene mínimo y máximo
        JPanel nuevoResumen = CreadorPaneles.crearResumenEstadisticoPanel(true,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion(),
            minMax2[0], minMax2[1], motorSimulacion.getValorAd(), motorSimulacion.getPValue(),
            motorSimulacion.isEsNormal(), motorSimulacion.getTamanoRecomendado()); // Panel de resumen
        JButton nuevoBoton = new JButton("Generar tabla tamaño recomendado"); // Botón para nueva tabla
        // CAMBIO MÍNIMO: Habilitar botón tanto para normales como no normales
        nuevoBoton.setEnabled(motorSimulacion.getTamanoRecomendado() > 0); // Habilita el botón si hay tamaño recomendado
        nuevoResumen.add(nuevoBoton); // Agrega el botón
        JPanel nuevasGraficas = GeneradorGraficas.crearPanelGraficas(nuevosCostos,
            motorSimulacion.getPromedio(), motorSimulacion.getDesviacion()); // Panel de gráficas
        JScrollPane nuevoScroll = new JScrollPane(tabla); // Scroll para la tabla
        nuevoScroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío
        JPanel nuevoMainPanel = CreadorPaneles.crearPanelPrincipal(nuevasGraficas, nuevoScroll, nuevoResumen); // Panel principal

        // Remover el panel central actual y agregar el nuevo
        JPanel panelConControles = (JPanel) getContentPane(); // Panel principal
        panelConControles.remove(panelCentralActual); // Quita el panel anterior
        panelConControles.add(nuevoMainPanel, BorderLayout.CENTER); // Agrega el nuevo panel
        panelConControles.revalidate(); // Refresca la interfaz
        panelConControles.repaint(); // Repinta la interfaz
        // Actualizar referencia
        panelCentralActual = nuevoMainPanel; // Actualiza la referencia
        // Asignar acción al nuevo botón
        nuevoBoton.addActionListener(this::generarTablaRecomendada); // Acción para nueva tabla
    }

    /**
     * Inicializa y configura la tabla principal con sus estilos
     */
    private void configurarTabla() {
        model = new DefaultTableModel(Constantes.COLUMNAS, 0); // Modelo de la tabla
        tabla = new JTable(model); // Tabla principal
        ConfiguradorTabla.configurarEstilosTabla(tabla); // Configura estilos
    }

    /**
     * Maneja la acción de generar una nueva tabla con el tamaño recomendado
     */
    private void generarTablaRecomendada(ActionEvent e) {
        if (motorSimulacion.getTamanoRecomendado() < 1) return; // Si no hay tamaño recomendado, no hace nada

        JFrame frameNuevaTabla = new JFrame("Simulación tamaño recomendado - " +
            motorSimulacion.getTamanoRecomendado() + " corridas"); // Nueva ventana

        // Crear y configurar nueva tabla
        DefaultTableModel nuevoModel = new DefaultTableModel(Constantes.COLUMNAS, 0); // Modelo de la nueva tabla
        JTable nuevaTabla = new JTable(nuevoModel); // Nueva tabla
        ConfiguradorTabla.configurarEstilosTabla(nuevaTabla); // Configura estilos

        JScrollPane scroll = new JScrollPane(nuevaTabla); // Scroll para la tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Borde vacío

        // MODIFICADO: Para tabla recomendada, usar valores aleatorios (como réplicas)
        // Generar simulación con tamaño recomendado usando método original (aleatorio)
        double[] costosTotalesNueva = motorSimulacion.generarSimulacionYllenarTabla(
            motorSimulacion.getTamanoRecomendado(), nuevoModel); // Ejecuta simulación
        ModelosDeDatos.EstadisticasSimulacion statsNueva = motorSimulacion.calcularEstadisticas(costosTotalesNueva); // Estadísticas

        // Crear panel de resumen para la nueva simulación
        JPanel resumenPanelNueva = CreadorPaneles.crearResumenEstadisticoPanel(false,
            statsNueva.promedio, statsNueva.desviacion, statsNueva.minimo, statsNueva.maximo,
            0, 0, false, 0); // Panel de resumen

        // Crear botón para generar 5 réplicas
        JButton botonReplicas = new JButton("Generar 5 réplicas"); // Botón para réplicas
        botonReplicas.setFont(Constantes.FUENTE_GENERAL); // Fuente
        resumenPanelNueva.add(botonReplicas); // Agrega el botón

        // Crear panel de gráficas para la nueva simulación
        JPanel graficasPanelNueva = GeneradorGraficas.crearPanelGraficas(costosTotalesNueva,
            statsNueva.promedio, statsNueva.desviacion); // Panel de gráficas

        // Crear sistema de pestañas
        JTabbedPane tabbedPane = new JTabbedPane(); // Pestañas

        // Crear pestaña principal con la simulación completa
        JPanel panelPrincipal = new JPanel(new BorderLayout(10, 10)); // Panel principal
        panelPrincipal.add(scroll, BorderLayout.CENTER); // Agrega la tabla
        panelPrincipal.add(graficasPanelNueva, BorderLayout.NORTH); // Agrega gráficas
        tabbedPane.addTab("Simulación Completa", panelPrincipal); // Agrega pestaña

        // Configurar la nueva ventana
        frameNuevaTabla.setLayout(new BorderLayout(10, 10)); // Layout
        frameNuevaTabla.add(tabbedPane, BorderLayout.CENTER); // Agrega pestañas
        frameNuevaTabla.add(resumenPanelNueva, BorderLayout.SOUTH); // Agrega resumen

        frameNuevaTabla.setSize(1400, 950); // Tamaño de la ventana
        frameNuevaTabla.setLocationRelativeTo(this); // Centra la ventana
        frameNuevaTabla.setVisible(true); // Muestra la ventana

        // Crear manejador de réplicas y asignar acción
        ManejadorReplicas manejadorReplicas = new ManejadorReplicas(motorSimulacion, motorSimulacion.getTamanoRecomendado()); // Manejador de réplicas
        botonReplicas.addActionListener(evt -> manejadorReplicas.generarReplicas(tabbedPane)); // Acción para generar réplicas
    }

    /**
     * Punto de entrada de la aplicación
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece el tema visual
        } catch (Exception e) {
            e.printStackTrace(); // Imprime errores
        }

        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true)); // Lanza la aplicación
    }
}