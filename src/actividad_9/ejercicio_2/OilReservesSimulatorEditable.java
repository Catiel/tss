package actividad_9.ejercicio_2; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa componentes de interfaz gráfica Swing
import javax.swing.table.*; // Importa componentes de tablas de Swing
import java.awt.*; // Importa componentes básicos de AWT para interfaz gráfica
import java.text.DecimalFormat; // Importa clase para formatear números decimales
import java.util.*; // Importa utilidades de colecciones y estructuras de datos
import java.util.List; // Importa específicamente la interfaz List

import org.apache.commons.math3.distribution.*; // Importa distribuciones estadísticas de Apache Commons Math

public class OilReservesSimulatorEditable extends JFrame { // Define la clase principal que extiende JFrame para crear una ventana
    private static final int NUM_SIMULACIONES = 563; // Define el número total de simulaciones a ejecutar
    private static final int NUM_PRUEBAS_MC = 1000; // Define el número de pruebas Monte Carlo por simulación
    private static final int AÑOS = 50; // Define el horizonte de tiempo en años para la simulación
    private static final int MIN_TRIALS_FOR_CHECK = 500; // Define el número mínimo de pruebas antes de verificar convergencia
    private static final int CHECK_INTERVAL = 500; // Define el intervalo de verificación de convergencia
    private static final int NUM_BINS_HISTOGRAMA = 50; // Define el número de barras en el histograma

    private static final DecimalFormat FMT2 = new DecimalFormat("#,##0.00"); // Formateador para 2 decimales con separador de miles
    private static final DecimalFormat FMT0 = new DecimalFormat("#,##0"); // Formateador sin decimales con separador de miles

    private static final Color COLOR_HEADER = new Color(79, 129, 189); // Color azul para encabezados
    private static final Color COLOR_SUPOSICION = new Color(146, 208, 80); // Color verde para variables de suposición
    private static final Color COLOR_DECISION = new Color(255, 255, 0); // Color amarillo para variables de decisión
    private static final Color COLOR_CALCULADO = new Color(217, 217, 217); // Color gris para valores calculados
    private static final Color COLOR_NPV = new Color(0, 255, 255); // Color cian para el NPV
    private static final Color COLOR_PANEL_BG = new Color(248, 248, 248); // Color de fondo de los paneles

    private double stoiip = 1500.0; // Stock Tank Oil Initially In Place (petróleo originalmente en sitio)
    private double recuperacion = 42.0; // Porcentaje de recuperación del petróleo
    private double buenaTasa = 10.0; // Tasa de producción por pozo en miles de barriles por día
    private int pozosPerforar = 25; // Número de pozos a perforar
    private double factorDescuento = 10.0; // Factor de descuento para calcular valor presente
    private double buenCosto = 10.0; // Costo de perforación por pozo en millones de dólares
    private double tamañoInstalacion = 250.0; // Capacidad de la instalación en miles de barriles por día
    private double plateauRateIs = 10.0; // Tasa de plateau como porcentaje de reservas por año

    private double timeToPlateau = 2.0; // Tiempo para alcanzar el plateau en años
    private double tarifaMinima = 10.0; // Tasa mínima de producción en miles de barriles por día
    private double margenPetroleo = 2.0; // Margen de beneficio por barril en dólares
    private double plateauEndsAt = 65.0; // Porcentaje de reservas cuando termina el plateau

    private double[][] costosInstalaciones = {{50, 70}, {100, 130}, {150, 180}, {200, 220}, {250, 250}, {300, 270}, {350, 280}}; // Matriz de costos de instalaciones según capacidad

    private double reservas; // Variable para almacenar las reservas calculadas
    private double maxPlateauRate; // Tasa máxima de plateau
    private double plateauRate; // Tasa efectiva de plateau
    private double aumentarProduccion; // Producción durante la fase de aumento
    private double plateauProduction; // Producción durante la fase de plateau
    private double plateauEndsAtCalc; // Tiempo calculado cuando termina el plateau
    private double factorDeclive; // Factor de declive de la producción
    private double vidaProduccion; // Vida útil de producción del yacimiento
    private double reservasDescontadas; // Reservas con valor presente descontado
    private double costosPozo; // Costos totales de perforación de pozos
    private double costosInstalacionesCalc; // Costos calculados de instalaciones
    private double npv; // Valor Presente Neto (Net Present Value)

    private JTextField txtStoiip, txtRecuperacion, txtBuenaTasa, txtPozos; // Campos de texto para entrada de datos grupo 1
    private JTextField txtFactorDescuento, txtBuenCosto, txtTamañoInstalacion, txtPlateauRateIs; // Campos de texto para entrada de datos grupo 2
    private JTextField txtTimeToPlateau, txtTarifaMinima, txtMargenPetroleo, txtPlateauEndsAt; // Campos de texto para entrada de datos grupo 3
    private JLabel lblReservas, lblMaxPlateau, lblPlateauRate, lblAumentar; // Etiquetas para mostrar valores calculados grupo 1
    private JLabel lblPlateauProd, lblPlateauEnds, lblFactorDeclive, lblVidaProd; // Etiquetas para mostrar valores calculados grupo 2
    private JLabel lblReservasDesc, lblCostosPozo, lblCostosInst, lblNPV; // Etiquetas para mostrar resultados finales
    private DefaultTableModel modeloTabla; // Modelo de datos para la tabla de producción
    private DefaultTableModel modeloCostos; // Modelo de datos para la tabla de costos
    private JProgressBar progressBar; // Barra de progreso para las simulaciones
    private JLabel lblProgreso; // Etiqueta para mostrar el estado del progreso
    private JTabbedPane tabbedPane; // Panel con pestañas para organizar la interfaz

    private double mejorNPV = Double.NEGATIVE_INFINITY; // Almacena el mejor NPV encontrado (inicializado al valor más negativo)
    private int mejorPozos = 25; // Número óptimo de pozos encontrado
    private double mejorTamañoInst = 250.0; // Tamaño óptimo de instalación encontrado
    private double mejorPlateauRateIs = 10.0; // Tasa de plateau óptima encontrada
    private List<Double> todosNPV = new ArrayList<>(); // Lista para almacenar todos los valores NPV de todas las simulaciones
    private List<Double> mejorSimulacionNPVs = new ArrayList<>(); // Lista para almacenar los NPV de la mejor simulación

    public OilReservesSimulatorEditable() { // Constructor de la clase
        super("Simulación de Reservas Petroleras - Crystal Ball"); // Llama al constructor de JFrame con el título de la ventana
        configurarUI(); // Llama al método para configurar la interfaz de usuario
        calcularValores(); // Llama al método para calcular los valores iniciales
        actualizarUI(); // Llama al método para actualizar la interfaz con los valores calculados
        calcularTablaProduccion(); // Llama al método para calcular la tabla de producción
        setSize(1600, 950); // Establece el tamaño de la ventana en píxeles
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Define que la aplicación se cierre al cerrar la ventana
    }

    private void configurarUI() { // Método para configurar toda la interfaz de usuario
        JPanel main = new JPanel(new BorderLayout(15, 15)); // Crea el panel principal con diseño BorderLayout
        main.setBackground(COLOR_PANEL_BG); // Establece el color de fondo del panel principal
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Crea un borde vacío de 15 píxeles en todos los lados

        JLabel titulo = new JLabel("Oil Field Development - Simulación de Reservas Petroleras", SwingConstants.CENTER); // Crea la etiqueta del título centrada
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Establece la fuente del título en negrita tamaño 22
        titulo.setForeground(new Color(31, 78, 120)); // Establece el color del texto del título
        titulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Crea un borde vacío alrededor del título
        main.add(titulo, BorderLayout.NORTH); // Añade el título en la parte superior del panel principal

        tabbedPane = new JTabbedPane(JTabbedPane.TOP); // Crea un panel con pestañas en la parte superior
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece la fuente de las pestañas

        JPanel dashboardPanel = crearPanelDashboard(); // Crea el panel del dashboard principal
        tabbedPane.addTab("📊 Dashboard Principal", dashboardPanel); // Añade la pestaña del dashboard

        JPanel tablaPanel = crearPanelTablaCompleta(); // Crea el panel con la tabla completa de producción
        tabbedPane.addTab("📈 Perfil de Producción (50 años)", tablaPanel); // Añade la pestaña de la tabla de producción

        main.add(tabbedPane, BorderLayout.CENTER); // Añade el panel de pestañas en el centro
        main.add(crearPanelControl(), BorderLayout.SOUTH); // Añade el panel de control en la parte inferior

        add(main); // Añade el panel principal a la ventana
    }

    private JPanel crearPanelDashboard() { // Método para crear el panel del dashboard principal
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea un panel con diseño BorderLayout
        panel.setBackground(COLOR_PANEL_BG); // Establece el color de fondo

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // Crea un panel superior con 3 columnas
        topPanel.setBackground(COLOR_PANEL_BG); // Establece el color de fondo del panel superior
        topPanel.add(crearPanelEntrada()); // Añade el panel de entrada de variables
        topPanel.add(crearPanelCalculado()); // Añade el panel de valores calculados
        topPanel.add(crearPanelResultadosFinales()); // Añade el panel de resultados finales

        panel.add(topPanel, BorderLayout.NORTH); // Añade el panel superior en la parte superior
        panel.add(crearPanelResumenTabla(), BorderLayout.CENTER); // Añade el panel de resumen de tabla en el centro

        return panel; // Retorna el panel del dashboard
    }

    private JPanel crearPanelEntrada() { // Método para crear el panel de variables de entrada
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea un panel con diseño BorderLayout
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea un borde compuesto con línea y espacio interno

        JLabel header = new JLabel("Variables de Entrada", SwingConstants.CENTER); // Crea la etiqueta del encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente del encabezado
        header.setForeground(COLOR_HEADER); // Establece el color del texto del encabezado
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea espacio inferior del encabezado
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado en la parte superior

        JPanel grid = new JPanel(new GridBagLayout()); // Crea un panel con diseño GridBagLayout para organizar los campos
        grid.setBackground(Color.WHITE); // Establece el fondo blanco
        GridBagConstraints gbc = new GridBagConstraints(); // Crea restricciones para el diseño GridBagLayout
        gbc.fill = GridBagConstraints.HORIZONTAL; // Establece que los componentes se expandan horizontalmente
        gbc.insets = new Insets(3, 5, 3, 5); // Establece el espacio entre componentes

        int row = 0; // Inicializa el contador de filas
        addGridRow(grid, gbc, row++, "STOIIP", txtStoiip = crearTextField("1500.00", COLOR_SUPOSICION), "mmbbls", COLOR_SUPOSICION); // Añade fila para STOIIP
        txtStoiip.setEditable(false); // Establece el campo STOIIP como no editable
        addGridRow(grid, gbc, row++, "Recuperación", txtRecuperacion = crearTextField("42.0", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Añade fila para Recuperación
        txtRecuperacion.setEditable(false); // Establece el campo Recuperación como no editable
        addGridRow(grid, gbc, row++, "Time to plateau", txtTimeToPlateau = crearTextField("2.00", Color.WHITE), "years", Color.WHITE); // Añade fila para Time to plateau
        addGridRow(grid, gbc, row++, "Buena tasa", txtBuenaTasa = crearTextField("10.00", COLOR_SUPOSICION), "mbd", COLOR_SUPOSICION); // Añade fila para Buena tasa
        txtBuenaTasa.setEditable(false); // Establece el campo Buena tasa como no editable
        addGridRow(grid, gbc, row++, "Pozos a perforar", txtPozos = crearTextField("25", COLOR_DECISION), "", COLOR_DECISION); // Añade fila para Pozos a perforar
        txtPozos.setEditable(false); // Establece el campo Pozos como no editable
        addGridRow(grid, gbc, row++, "Tarifa mínima", txtTarifaMinima = crearTextField("10.00", Color.WHITE), "mbd", Color.WHITE); // Añade fila para Tarifa mínima
        addGridRow(grid, gbc, row++, "Factor de descuento", txtFactorDescuento = crearTextField("10.00", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Añade fila para Factor de descuento
        txtFactorDescuento.setEditable(false); // Establece el campo Factor de descuento como no editable
        addGridRow(grid, gbc, row++, "Buen costo", txtBuenCosto = crearTextField("10.00", COLOR_SUPOSICION), "$mm", COLOR_SUPOSICION); // Añade fila para Buen costo
        txtBuenCosto.setEditable(false); // Establece el campo Buen costo como no editable
        addGridRow(grid, gbc, row++, "Tamaño instalación", txtTamañoInstalacion = crearTextField("250.00", COLOR_DECISION), "mbd", COLOR_DECISION); // Añade fila para Tamaño instalación
        txtTamañoInstalacion.setEditable(false); // Establece el campo Tamaño instalación como no editable
        addGridRow(grid, gbc, row++, "Margen petróleo", txtMargenPetroleo = crearTextField("2.00", Color.WHITE), "$/bbl", Color.WHITE); // Añade fila para Margen petróleo
        addGridRow(grid, gbc, row++, "Plateau ends at", txtPlateauEndsAt = crearTextField("65.0", Color.WHITE), "% reservas", Color.WHITE); // Añade fila para Plateau ends at
        addGridRow(grid, gbc, row++, "Plateau rate is", txtPlateauRateIs = crearTextField("10.0", COLOR_DECISION), "% res./año", COLOR_DECISION); // Añade fila para Plateau rate is
        txtPlateauRateIs.setEditable(false); // Establece el campo Plateau rate is como no editable

        panel.add(grid, BorderLayout.CENTER); // Añade el grid en el centro del panel
        panel.add(crearLeyenda(), BorderLayout.SOUTH); // Añade la leyenda en la parte inferior

        return panel; // Retorna el panel de entrada
    }

    private void addGridRow(JPanel grid, GridBagConstraints gbc, int row, String label, JComponent campo, String unidad, Color bgColor) { // Método para añadir una fila al grid
        gbc.gridy = row; // Establece la fila actual
        gbc.gridx = 0; // Establece la primera columna para la etiqueta
        gbc.weightx = 0.4; // Establece el peso horizontal de la etiqueta
        JLabel lbl = new JLabel(label + ":"); // Crea la etiqueta con el texto y dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece la fuente de la etiqueta
        grid.add(lbl, gbc); // Añade la etiqueta al grid

        gbc.gridx = 1; // Establece la segunda columna para el campo
        gbc.weightx = 0.4; // Establece el peso horizontal del campo
        grid.add(campo, gbc); // Añade el campo al grid

        gbc.gridx = 2; // Establece la tercera columna para la unidad
        gbc.weightx = 0.2; // Establece el peso horizontal de la unidad
        JLabel lblUnit = new JLabel(unidad); // Crea la etiqueta de la unidad
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece la fuente de la unidad
        lblUnit.setForeground(Color.GRAY); // Establece el color gris para la unidad
        grid.add(lblUnit, gbc); // Añade la etiqueta de unidad al grid
    }

    private JPanel crearLeyenda() { // Método para crear el panel de leyenda
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // Crea un panel con diseño FlowLayout
        leyenda.setBackground(Color.WHITE); // Establece el fondo blanco
        leyenda.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Crea espacio superior

        leyenda.add(crearLeyendaItem("Suposición", COLOR_SUPOSICION)); // Añade el item de leyenda para Suposición
        leyenda.add(crearLeyendaItem("Decisión", COLOR_DECISION)); // Añade el item de leyenda para Decisión
        leyenda.add(crearLeyendaItem("Fijo", Color.WHITE)); // Añade el item de leyenda para Fijo

        return leyenda; // Retorna el panel de leyenda
    }

    private JPanel crearLeyendaItem(String texto, Color color) { // Método para crear un item de leyenda
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); // Crea un panel para el item
        item.setBackground(Color.WHITE); // Establece el fondo blanco

        JLabel colorBox = new JLabel("  "); // Crea una etiqueta pequeña para mostrar el color
        colorBox.setOpaque(true); // Hace que la etiqueta sea opaca
        colorBox.setBackground(color); // Establece el color de fondo
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Crea un borde negro

        JLabel label = new JLabel(texto); // Crea la etiqueta con el texto
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9)); // Establece la fuente del texto

        item.add(colorBox); // Añade la caja de color al item
        item.add(label); // Añade la etiqueta de texto al item

        return item; // Retorna el item de leyenda
    }

    private JPanel crearPanelCalculado() { // Método para crear el panel de valores calculados
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea un panel con diseño BorderLayout
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea un borde compuesto

        JLabel header = new JLabel("Valores Calculados", SwingConstants.CENTER); // Crea el encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente del encabezado
        header.setForeground(COLOR_HEADER); // Establece el color del encabezado
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea espacio inferior
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado en la parte superior

        JPanel grid = new JPanel(new GridLayout(8, 2, 5, 8)); // Crea un grid de 8 filas y 2 columnas
        grid.setBackground(Color.WHITE); // Establece el fondo blanco

        grid.add(crearLabelParametro("Reservas")); // Añade etiqueta para Reservas
        lblReservas = crearLabelCalculado("630.00 mmbbls"); // Crea y asigna la etiqueta calculada
        grid.add(lblReservas); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Max plateau rate")); // Añade etiqueta para Max plateau rate
        lblMaxPlateau = crearLabelCalculado("172.60 mbd"); // Crea y asigna la etiqueta calculada
        grid.add(lblMaxPlateau); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Plateau rate")); // Añade etiqueta para Plateau rate
        lblPlateauRate = crearLabelCalculado("172.60 mbd"); // Crea y asigna la etiqueta calculada
        grid.add(lblPlateauRate); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Aumentar producción")); // Añade etiqueta para Aumentar producción
        lblAumentar = crearLabelCalculado("63.00 mmbbls"); // Crea y asigna la etiqueta calculada
        grid.add(lblAumentar); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Plateau production")); // Añade etiqueta para Plateau production
        lblPlateauProd = crearLabelCalculado("346.50 mmbbls"); // Crea y asigna la etiqueta calculada
        grid.add(lblPlateauProd); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Plateau ends at")); // Añade etiqueta para Plateau ends at
        lblPlateauEnds = crearLabelCalculado("7.50 años"); // Crea y asigna la etiqueta calculada
        grid.add(lblPlateauEnds); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Factor de declive")); // Añade etiqueta para Factor de declive
        lblFactorDeclive = crearLabelCalculado("0.2692"); // Crea y asigna la etiqueta calculada
        grid.add(lblFactorDeclive); // Añade la etiqueta calculada al grid

        grid.add(crearLabelParametro("Vida de producción")); // Añade etiqueta para Vida de producción
        lblVidaProd = crearLabelCalculado("18.08 años"); // Crea y asigna la etiqueta calculada
        grid.add(lblVidaProd); // Añade la etiqueta calculada al grid

        panel.add(grid, BorderLayout.CENTER); // Añade el grid en el centro del panel

        return panel; // Retorna el panel de valores calculados
    }

    private JPanel crearPanelResultadosFinales() { // Método para crear el panel de resultados finales
        JPanel container = new JPanel(new BorderLayout(5, 10)); // Crea un panel contenedor
        container.setBackground(COLOR_PANEL_BG); // Establece el color de fondo

        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel principal
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(237, 125, 49), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea un borde compuesto con color naranja

        JLabel header = new JLabel("Resultados Finales", SwingConstants.CENTER); // Crea el encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente
        header.setForeground(new Color(237, 125, 49)); // Establece el color naranja
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea espacio inferior
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado

        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 8)); // Crea un grid de 4 filas y 2 columnas
        grid.setBackground(Color.WHITE); // Establece el fondo blanco

        grid.add(crearLabelParametro("Reservas descontadas")); // Añade etiqueta para Reservas descontadas
        lblReservasDesc = crearLabelCalculado("379.45 mmbbls"); // Crea y asigna la etiqueta calculada
        grid.add(lblReservasDesc); // Añade la etiqueta al grid

        grid.add(crearLabelParametro("Costos del pozo")); // Añade etiqueta para Costos del pozo
        lblCostosPozo = crearLabelCalculado("250.00 $mm"); // Crea y asigna la etiqueta calculada
        grid.add(lblCostosPozo); // Añade la etiqueta al grid

        grid.add(crearLabelParametro("Costos instalaciones")); // Añade etiqueta para Costos instalaciones
        lblCostosInst = crearLabelCalculado("250.00 $mm"); // Crea y asigna la etiqueta calculada
        grid.add(lblCostosInst); // Añade la etiqueta al grid

        grid.add(crearLabelParametro("NPV")); // Añade etiqueta para NPV
        lblNPV = crearLabelCalculado("258.89 $mm"); // Crea y asigna la etiqueta calculada
        lblNPV.setBackground(COLOR_NPV); // Establece el color de fondo cian
        lblNPV.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece la fuente en negrita
        grid.add(lblNPV); // Añade la etiqueta al grid

        panel.add(grid, BorderLayout.CENTER); // Añade el grid al panel

        JLabel objetivo = new JLabel("🎯 Objetivo: Maximizar Percentil 10 de NPV", SwingConstants.CENTER); // Crea etiqueta de objetivo
        objetivo.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece la fuente
        objetivo.setForeground(Color.RED); // Establece el color rojo
        objetivo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Crea espacio superior
        panel.add(objetivo, BorderLayout.SOUTH); // Añade el objetivo en la parte inferior

        container.add(panel, BorderLayout.NORTH); // Añade el panel al contenedor en la parte superior
        container.add(crearPanelCostosInstalaciones(), BorderLayout.CENTER); // Añade el panel de costos en el centro

        return container; // Retorna el contenedor
    }

    private JPanel crearPanelCostosInstalaciones() { // Método para crear el panel de costos de instalaciones
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 192, 203), 2), BorderFactory.createEmptyBorder(8, 8, 8, 8))); // Crea un borde compuesto con color rosa

        JLabel header = new JLabel("Costos de Instalaciones", SwingConstants.CENTER); // Crea el encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece la fuente
        header.setForeground(new Color(192, 80, 77)); // Establece el color
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado

        String[] cols = {"Producción (mbd)", "Costo ($mm)"}; // Define las columnas de la tabla
        modeloCostos = new DefaultTableModel(cols, 0) { // Crea el modelo de tabla
            public boolean isCellEditable(int r, int c) { // Sobrescribe método para hacer celdas editables
                return true; // Retorna true para que todas las celdas sean editables
            }
        };

        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre la matriz de costos
            modeloCostos.addRow(new Object[]{FMT0.format(costosInstalaciones[i][0]), FMT0.format(costosInstalaciones[i][1])}); // Añade cada fila formateada
        }

        JTable tabla = new JTable(modeloCostos); // Crea la tabla con el modelo
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece la fuente de la tabla
        tabla.setRowHeight(22); // Establece la altura de las filas
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10)); // Establece la fuente del encabezado
        tabla.getTableHeader().setBackground(new Color(255, 192, 203)); // Establece el color de fondo del encabezado
        tabla.getTableHeader().setForeground(Color.BLACK); // Establece el color del texto del encabezado

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Crea un renderer para centrar el texto
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Establece alineación centrada
        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre las columnas
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Aplica el renderer a cada columna
        }

        JScrollPane scroll = new JScrollPane(tabla); // Crea un scroll pane para la tabla
        scroll.setPreferredSize(new Dimension(250, 180)); // Establece el tamaño preferido
        panel.add(scroll, BorderLayout.CENTER); // Añade el scroll pane al panel

        return panel; // Retorna el panel
    }

    private JPanel crearPanelResumenTabla() { // Método para crear el panel de resumen de tabla
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea un borde compuesto

        JLabel header = new JLabel("Perfil de Producción - Primeros 15 Años (Ver pestaña para datos completos)", SwingConstants.CENTER); // Crea el encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece la fuente
        header.setForeground(COLOR_HEADER); // Establece el color
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado

        String[] cols = {"Año", "Tasa Anualizada\\n(mbd)", "Producción Anual\\n(mmb)", "Petróleo Acumulado\\n(mmb)", "Petróleo Desc. Acum.\\n(mmb)"}; // Define las columnas

        DefaultTableModel modeloResumen = new DefaultTableModel(cols, 0) { // Crea el modelo de tabla
            public boolean isCellEditable(int r, int c) { // Sobrescribe método de editabilidad
                return false; // Retorna false para que las celdas no sean editables
            }
        };

        JTable tablaResumen = new JTable(modeloResumen); // Crea la tabla con el modelo
        configurarEstiloTabla(tablaResumen); // Configura el estilo de la tabla

        JScrollPane scroll = new JScrollPane(tablaResumen); // Crea un scroll pane para la tabla
        panel.add(scroll, BorderLayout.CENTER); // Añade el scroll pane al panel

        return panel; // Retorna el panel
    }

    private JPanel crearPanelTablaCompleta() { // Método para crear el panel de tabla completa
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Crea un borde vacío

        JLabel header = new JLabel("Perfil de Producción Calculado - 50 Años", SwingConstants.CENTER); // Crea el encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente
        header.setForeground(COLOR_HEADER); // Establece el color
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Crea espacio alrededor
        panel.add(header, BorderLayout.NORTH); // Añade el encabezado

        String[] cols = {"Año", "Tasa Anualizada (mbd)", "Producción Anual (mmb)", "Petróleo Acumulado (mmb)", "Petróleo con Descuento Acumulado (mmb)"}; // Define las columnas

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea el modelo de tabla
            public boolean isCellEditable(int r, int c) { // Sobrescribe método de editabilidad
                return false; // Retorna false para que las celdas no sean editables
            }
        };

        JTable tabla = new JTable(modeloTabla); // Crea la tabla con el modelo
        configurarEstiloTabla(tabla); // Configura el estilo de la tabla

        JScrollPane scroll = new JScrollPane(tabla); // Crea un scroll pane
        panel.add(scroll, BorderLayout.CENTER); // Añade el scroll pane al panel

        return panel; // Retorna el panel
    }

    private void configurarEstiloTabla(JTable tabla) { // Método para configurar el estilo de una tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece la fuente de la tabla
        tabla.setRowHeight(26); // Establece la altura de las filas
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece la fuente del encabezado
        tabla.getTableHeader().setBackground(COLOR_HEADER); // Establece el color de fondo del encabezado
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece el color del texto del encabezado
        tabla.setGridColor(new Color(220, 220, 220)); // Establece el color de las líneas de la cuadrícula
        tabla.setShowGrid(true); // Hace visible la cuadrícula
        tabla.setIntercellSpacing(new Dimension(1, 1)); // Establece el espacio entre celdas

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea un renderer personalizado
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Sobrescribe método de renderizado
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al método padre
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT); // Centra primera columna, alinea a la derecha las demás
                setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece la fuente

                if (r % 2 == 0) { // Si la fila es par
                    setBackground(Color.WHITE); // Establece fondo blanco
                } else { // Si la fila es impar
                    setBackground(new Color(245, 245, 245)); // Establece fondo gris claro
                }

                if (c == 0) { // Si es la primera columna
                    setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente en negrita
                }

                setForeground(Color.BLACK); // Establece color negro para el texto
                return this; // Retorna el componente renderizado
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre las columnas
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica el renderer a cada columna
            if (i == 0) { // Si es la primera columna
                tabla.getColumnModel().getColumn(i).setPreferredWidth(60); // Establece ancho preferido de 60 píxeles
            } else { // Para las demás columnas
                tabla.getColumnModel().getColumn(i).setPreferredWidth(160); // Establece ancho preferido de 160 píxeles
            }
        }
    }

    private JPanel crearPanelControl() { // Método para crear el panel de control
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece el fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 15, 10, 15))); // Crea un borde compuesto

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // Crea un panel para los botones
        botones.setBackground(Color.WHITE); // Establece el fondo blanco

        JButton btnActualizar = crearBoton("🔄 Actualizar Cálculos", new Color(237, 125, 49), 200, 40); // Crea el botón de actualizar
        btnActualizar.addActionListener(e -> { // Añade un listener al botón
            leerValoresUI(); // Lee los valores de la interfaz
            calcularValores(); // Calcula los nuevos valores
            actualizarUI(); // Actualiza la interfaz
            calcularTablaProduccion(); // Calcula la tabla de producción
            JOptionPane.showMessageDialog(this, "✓ Cálculos actualizados correctamente", "Actualización", JOptionPane.INFORMATION_MESSAGE); // Muestra mensaje de confirmación
        });

        JButton btnOptimizar = crearBoton("🚀 Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 300, 40); // Crea el botón de optimizar
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Añade listener para ejecutar optimización

        botones.add(btnActualizar); // Añade botón de actualizar
        botones.add(btnOptimizar); // Añade botón de optimizar

        JPanel progreso = new JPanel(new BorderLayout(8, 8)); // Crea panel para mostrar progreso
        progreso.setBackground(Color.WHITE); // Establece fondo blanco

        lblProgreso = new JLabel("Listo para comenzar optimización", SwingConstants.CENTER); // Crea etiqueta de progreso
        lblProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece la fuente

        progressBar = new JProgressBar(0, NUM_SIMULACIONES); // Crea barra de progreso con rango de 0 a NUM_SIMULACIONES
        progressBar.setStringPainted(true); // Hace que se muestre el texto en la barra
        progressBar.setPreferredSize(new Dimension(700, 30)); // Establece el tamaño preferido
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece la fuente
        progressBar.setForeground(new Color(76, 175, 80)); // Establece el color verde

        progreso.add(lblProgreso, BorderLayout.NORTH); // Añade la etiqueta en la parte superior
        progreso.add(progressBar, BorderLayout.CENTER); // Añade la barra en el centro

        panel.add(botones, BorderLayout.NORTH); // Añade el panel de botones en la parte superior
        panel.add(progreso, BorderLayout.CENTER); // Añade el panel de progreso en el centro

        return panel; // Retorna el panel de control
    }

    private JLabel crearLabelParametro(String texto) { // Método para crear etiquetas de parámetros
        JLabel lbl = new JLabel(texto + ":"); // Crea la etiqueta con dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece la fuente
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea el texto a la derecha
        return lbl; // Retorna la etiqueta
    }

    private JTextField crearTextField(String valor, Color bg) { // Método para crear campos de texto
        JTextField txt = new JTextField(valor); // Crea el campo de texto con valor inicial
        txt.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece la fuente en negrita
        txt.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea el texto a la derecha
        txt.setBackground(bg); // Establece el color de fondo
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Crea un borde compuesto
        return txt; // Retorna el campo de texto
    }

    private JLabel crearLabelCalculado(String texto) { // Método para crear etiquetas de valores calculados
        JLabel lbl = new JLabel(texto); // Crea la etiqueta con el texto
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece la fuente en negrita
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea el texto a la derecha
        lbl.setBackground(COLOR_CALCULADO); // Establece el color de fondo gris
        lbl.setOpaque(true); // Hace la etiqueta opaca
        lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Crea un borde compuesto
        return lbl; // Retorna la etiqueta
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método para crear botones personalizados
        JButton btn = new JButton(texto); // Crea el botón con el texto
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece la fuente
        btn.setBackground(bg); // Establece el color de fondo
        btn.setForeground(Color.WHITE); // Establece el color del texto en blanco
        btn.setFocusPainted(false); // Desactiva el indicador de foco
        btn.setBorderPainted(false); // Desactiva el borde del botón
        btn.setPreferredSize(new Dimension(ancho, alto)); // Establece el tamaño preferido
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Establece el cursor como mano

        btn.addMouseListener(new java.awt.event.MouseAdapter() { // Añade listener de ratón
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Cuando el ratón entra
                btn.setBackground(bg.brighter()); // Aclara el color de fondo
            }

            public void mouseExited(java.awt.event.MouseEvent evt) { // Cuando el ratón sale
                btn.setBackground(bg); // Restaura el color de fondo original
            }
        });

        return btn; // Retorna el botón
    }

    private JLabel crearStatLabel(String texto) { // Método para crear etiquetas de estadísticas
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER); // Crea la etiqueta centrada
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece la fuente
        lbl.setForeground(COLOR_HEADER); // Establece el color
        return lbl; // Retorna la etiqueta
    }

    private void leerValoresUI() { // Método para leer valores de la interfaz de usuario
        try { // Inicia bloque try para capturar excepciones
            stoiip = Double.parseDouble(txtStoiip.getText().replace(",", "")); // Lee y convierte STOIIP removiendo comas
            recuperacion = Double.parseDouble(txtRecuperacion.getText().replace(",", "")); // Lee y convierte recuperación
            buenaTasa = Double.parseDouble(txtBuenaTasa.getText().replace(",", "")); // Lee y convierte buena tasa
            pozosPerforar = Integer.parseInt(txtPozos.getText().replace(",", "")); // Lee y convierte pozos a entero
            factorDescuento = Double.parseDouble(txtFactorDescuento.getText().replace(",", "")); // Lee y convierte factor de descuento
            buenCosto = Double.parseDouble(txtBuenCosto.getText().replace(",", "")); // Lee y convierte buen costo
            tamañoInstalacion = Double.parseDouble(txtTamañoInstalacion.getText().replace(",", "")); // Lee y convierte tamaño de instalación
            plateauRateIs = Double.parseDouble(txtPlateauRateIs.getText().replace(",", "")); // Lee y convierte plateau rate
            timeToPlateau = Double.parseDouble(txtTimeToPlateau.getText().replace(",", "")); // Lee y convierte time to plateau
            tarifaMinima = Double.parseDouble(txtTarifaMinima.getText().replace(",", "")); // Lee y convierte tarifa mínima
            margenPetroleo = Double.parseDouble(txtMargenPetroleo.getText().replace(",", "")); // Lee y convierte margen de petróleo
            plateauEndsAt = Double.parseDouble(txtPlateauEndsAt.getText().replace(",", "")); // Lee y convierte plateau ends at

            int rows = modeloCostos.getRowCount(); // Obtiene el número de filas de la tabla de costos
            costosInstalaciones = new double[rows][2]; // Inicializa la matriz de costos
            for (int i = 0; i < rows; i++) { // Itera sobre las filas
                String prodStr = modeloCostos.getValueAt(i, 0).toString().replace(",", ""); // Lee producción de la tabla
                String costStr = modeloCostos.getValueAt(i, 1).toString().replace(",", ""); // Lee costo de la tabla
                costosInstalaciones[i][0] = Double.parseDouble(prodStr); // Convierte y guarda producción
                costosInstalaciones[i][1] = Double.parseDouble(costStr); // Convierte y guarda costo
            }
        } catch (Exception e) { // Captura cualquier excepción
            JOptionPane.showMessageDialog(this, "Error al leer valores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Muestra mensaje de error
        }
    }

    private void calcularValores() { // Método para calcular todos los valores intermedios
        reservas = stoiip * recuperacion / 100.0; // Calcula reservas recuperables
        maxPlateauRate = (plateauRateIs / 100.0) * reservas / 0.365; // Calcula tasa máxima de plateau
        plateauRate = Math.min(maxPlateauRate, Math.min(buenaTasa * pozosPerforar, tamañoInstalacion)); // Calcula tasa efectiva de plateau como el mínimo de tres valores
        aumentarProduccion = 0.365 * plateauRate * 0.5 * timeToPlateau; // Calcula producción durante fase de aumento
        plateauProduction = Math.max(0, plateauEndsAt * (reservas / 100.0) - aumentarProduccion); // Calcula producción durante plateau
        plateauEndsAtCalc = plateauProduction / (0.365 * plateauRate) + timeToPlateau; // Calcula tiempo cuando termina el plateau
        factorDeclive = 0.365 * (plateauRate - tarifaMinima) / (reservas - plateauProduction - aumentarProduccion); // Calcula factor de declive

        if (tarifaMinima > 0) { // Si hay tarifa mínima definida
            vidaProduccion = plateauEndsAtCalc - Math.log(tarifaMinima / plateauRate) / factorDeclive; // Calcula vida de producción usando logaritmo
        } else { // Si no hay tarifa mínima
            vidaProduccion = 1e20; // Establece vida de producción muy alta (infinito práctico)
        }

        costosPozo = buenCosto * pozosPerforar; // Calcula costos totales de pozos
        costosInstalacionesCalc = buscarCostoInstalacion(tamañoInstalacion); // Busca el costo de instalación correspondiente
    }

    private double buscarCostoInstalacion(double produccion) { // Método para buscar el costo de instalación según la producción
        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre la tabla de costos
            if (produccion <= costosInstalaciones[i][0]) { // Si la producción es menor o igual al límite
                return costosInstalaciones[i][1]; // Retorna el costo correspondiente
            }
        }
        return costosInstalaciones[costosInstalaciones.length - 1][1]; // Si no encuentra, retorna el último costo
    }

    private void calcularTablaProduccion() { // Método para calcular la tabla de producción de 50 años
        modeloTabla.setRowCount(0); // Limpia la tabla

        double[] tasaAnualizada = new double[AÑOS + 1]; // Array para almacenar tasas anualizadas
        double[] produccionAnual = new double[AÑOS + 1]; // Array para almacenar producción anual
        double[] petroleoAcumulado = new double[AÑOS + 1]; // Array para almacenar petróleo acumulado
        double[] petroleoDescuentoAcum = new double[AÑOS + 1]; // Array para almacenar petróleo descontado acumulado

        for (int año = 1; año <= AÑOS; año++) { // Itera sobre cada año
            if (año < timeToPlateau + 1) { // Si está en fase de aumento
                produccionAnual[año] = año * 0.365 * plateauRate / (timeToPlateau + 1); // Calcula producción en fase de aumento
            } else { // Si está en plateau o declive
                double maxMin1 = Math.min(plateauEndsAtCalc + 1 - año, 1); // Calcula término de plateau
                double part1 = 0.365 * plateauRate * Math.max(0, maxMin1); // Calcula primera parte de la producción

                double minVidaAño1 = Math.min(vidaProduccion, año - 1); // Calcula mínimo entre vida y año anterior
                double maxExp1 = Math.max(0, minVidaAño1 - plateauEndsAtCalc); // Calcula argumento del exponencial anterior
                double exp1 = Math.exp(-factorDeclive * maxExp1); // Calcula exponencial del año anterior

                double minVidaAño = Math.min(vidaProduccion, año); // Calcula mínimo entre vida y año actual
                double maxExp2 = Math.max(minVidaAño - plateauEndsAtCalc, 0); // Calcula argumento del exponencial actual
                double exp2 = Math.exp(-factorDeclive * maxExp2); // Calcula exponencial del año actual

                double part2 = 0.365 * plateauRate * (exp1 - exp2) / factorDeclive; // Calcula segunda parte de la producción

                produccionAnual[año] = part1 + part2; // Suma ambas partes para obtener producción total
            }

            tasaAnualizada[año] = produccionAnual[año] / 0.365; // Calcula tasa anualizada

            if (año == 1) { // Si es el primer año
                petroleoAcumulado[año] = produccionAnual[año]; // El acumulado es igual a la producción del año
            } else { // Para años subsiguientes
                petroleoAcumulado[año] = petroleoAcumulado[año - 1] + produccionAnual[año]; // Suma la producción del año al acumulado
            }

            if (año == 1) { // Si es el primer año
                petroleoDescuentoAcum[año] = produccionAnual[año]; // El descontado es igual a la producción del año
            } else { // Para años subsiguientes
                double descuento = Math.pow(1.0 + 0.01 * factorDescuento, año - 1); // Calcula factor de descuento
                petroleoDescuentoAcum[año] = petroleoDescuentoAcum[año - 1] + (produccionAnual[año] / descuento); // Suma la producción descontada
            }

            modeloTabla.addRow(new Object[]{año, FMT2.format(tasaAnualizada[año]), FMT2.format(produccionAnual[año]), FMT2.format(petroleoAcumulado[año]), FMT2.format(petroleoDescuentoAcum[año])}); // Añade fila a la tabla
        }

        reservasDescontadas = petroleoDescuentoAcum[AÑOS]; // Guarda las reservas descontadas del último año
        npv = reservasDescontadas * margenPetroleo - costosPozo - costosInstalacionesCalc; // Calcula el NPV
    }

    private void actualizarUI() { // Método para actualizar la interfaz con los valores calculados
        lblReservas.setText(FMT2.format(reservas) + " mmbbls"); // Actualiza etiqueta de reservas
        lblMaxPlateau.setText(FMT2.format(maxPlateauRate) + " mbd"); // Actualiza etiqueta de max plateau rate
        lblPlateauRate.setText(FMT2.format(plateauRate) + " mbd"); // Actualiza etiqueta de plateau rate
        lblAumentar.setText(FMT2.format(aumentarProduccion) + " mmbbls"); // Actualiza etiqueta de aumentar producción
        lblPlateauProd.setText(FMT2.format(plateauProduction) + " mmbbls"); // Actualiza etiqueta de plateau production
        lblPlateauEnds.setText(FMT2.format(plateauEndsAtCalc) + " años"); // Actualiza etiqueta de plateau ends at
        lblFactorDeclive.setText(FMT2.format(factorDeclive)); // Actualiza etiqueta de factor de declive
        lblVidaProd.setText(FMT2.format(vidaProduccion) + " años"); // Actualiza etiqueta de vida de producción
        lblReservasDesc.setText(FMT2.format(reservasDescontadas) + " mmbbls"); // Actualiza etiqueta de reservas descontadas
        lblCostosPozo.setText(FMT2.format(costosPozo) + " $mm"); // Actualiza etiqueta de costos de pozo
        lblCostosInst.setText(FMT2.format(costosInstalacionesCalc) + " $mm"); // Actualiza etiqueta de costos de instalaciones
        lblNPV.setText(FMT2.format(npv) + " $mm"); // Actualiza etiqueta de NPV
    }

    private void ejecutarOptimizacion() { // Método para ejecutar la optimización Monte Carlo
        todosNPV.clear(); // Limpia la lista de todos los NPV
        mejorSimulacionNPVs.clear(); // Limpia la lista de NPV de la mejor simulación
        mejorNPV = Double.NEGATIVE_INFINITY; // Reinicia el mejor NPV al valor más negativo

        progressBar.setValue(0); // Reinicia la barra de progreso a 0
        lblProgreso.setText("⏳ Ejecutando optimización Monte Carlo..."); // Actualiza el mensaje de progreso

        new SwingWorker<Void, Integer>() { // Crea un SwingWorker para ejecutar la optimización en segundo plano
            protected Void doInBackground() { // Método que se ejecuta en segundo plano
                Random rand = new Random(12345); // Inicializa generador de números aleatorios con semilla fija

                for (int sim = 1; sim <= NUM_SIMULACIONES; sim++) { // Itera sobre cada simulación
                    int pozos = rand.nextInt(49) + 2; // Genera número aleatorio de pozos entre 2 y 50
                    int instIndex = rand.nextInt(7); // Genera índice aleatorio para tamaño de instalación
                    double tamañoInst = 50 + 50 * instIndex; // Calcula tamaño de instalación (50, 100, 150, etc.)
                    double plateauIs = 4.5 + rand.nextDouble() * (15.0 - 4.5); // Genera plateau rate aleatorio entre 4.5 y 15

                    List<Double> npvsPrueba = new ArrayList<>(); // Lista para almacenar NPVs de esta simulación

                    for (int mc = 0; mc < NUM_PRUEBAS_MC; mc++) { // Itera sobre cada prueba Monte Carlo
                        LogNormalDistribution stoiipDist = new LogNormalDistribution(Math.log(1500.0), 300.0 / 1500.0); // Crea distribución log-normal para STOIIP
                        NormalDistribution recupDist = new NormalDistribution(42.0, 1.2); // Crea distribución normal para recuperación
                        NormalDistribution tasaDist = new NormalDistribution(10.0, 3.0); // Crea distribución normal para tasa
                        LogNormalDistribution descDist = new LogNormalDistribution(Math.log(10.0), 1.2 / 10.0); // Crea distribución log-normal para descuento
                        TriangularDistribution costoDist = new TriangularDistribution(9.0, 10.0, 12.0); // Crea distribución triangular para costo

                        double stoiipSample = stoiipDist.sample(); // Genera muestra aleatoria de STOIIP
                        double recupSample = recupDist.sample(); // Genera muestra aleatoria de recuperación
                        double tasaSample = tasaDist.sample(); // Genera muestra aleatoria de tasa
                        double descSample = descDist.sample(); // Genera muestra aleatoria de descuento
                        double costoSample = costoDist.sample(); // Genera muestra aleatoria de costo

                        double npvSample = calcularNPVSimulacion(stoiipSample, recupSample, tasaSample, pozos, descSample, costoSample, tamañoInst, plateauIs); // Calcula NPV para esta muestra

                        npvsPrueba.add(npvSample); // Añade el NPV a la lista de la simulación
                        todosNPV.add(npvSample); // Añade el NPV a la lista global

                        if (mc + 1 >= MIN_TRIALS_FOR_CHECK && (mc + 1) % CHECK_INTERVAL == 0) { // Si se alcanzó el número mínimo y es un intervalo de verificación
                            List<Double> temp = new ArrayList<>(npvsPrueba); // Crea copia temporal de la lista
                            Collections.sort(temp); // Ordena la lista temporal
                            double currentP10 = temp.get((int) (temp.size() * 0.10)); // Calcula percentil 10 actual

                            if (currentP10 < mejorNPV - 50.0) { // Si el percentil 10 actual es mucho peor que el mejor
                                break; // Sale del loop de Monte Carlo (poda)
                            }
                        }
                    }

                    Collections.sort(npvsPrueba); // Ordena los NPVs de esta simulación
                    double percentil10 = npvsPrueba.get((int) (npvsPrueba.size() * 0.10)); // Calcula el percentil 10

                    if (percentil10 > mejorNPV) { // Si este percentil 10 es mejor que el mejor encontrado
                        mejorNPV = percentil10; // Actualiza el mejor NPV
                        mejorPozos = pozos; // Actualiza el mejor número de pozos
                        mejorTamañoInst = tamañoInst; // Actualiza el mejor tamaño de instalación
                        mejorPlateauRateIs = plateauIs; // Actualiza el mejor plateau rate
                        mejorSimulacionNPVs = new ArrayList<>(npvsPrueba); // Guarda los NPVs de la mejor simulación
                    }

                    if (sim % 5 == 0) { // Cada 5 simulaciones
                        publish(sim); // Publica el progreso
                    }
                }

                return null; // Retorna null cuando termina
            }

            protected void process(List<Integer> chunks) { // Método para procesar actualizaciones de progreso
                int ultimo = chunks.get(chunks.size() - 1); // Obtiene el último valor publicado
                progressBar.setValue(ultimo); // Actualiza la barra de progreso
                int porcentaje = (int) ((ultimo * 100.0) / NUM_SIMULACIONES); // Calcula el porcentaje
                lblProgreso.setText(String.format("⏳ Progreso: %d / %d simulaciones (%d%%)", ultimo, NUM_SIMULACIONES, porcentaje)); // Actualiza el mensaje de progreso
            }

            protected void done() { // Método que se ejecuta cuando termina el trabajo en segundo plano
                progressBar.setValue(NUM_SIMULACIONES); // Establece la barra de progreso al 100%
                lblProgreso.setText("✅ Optimización completada - " + NUM_SIMULACIONES + " simulaciones"); // Actualiza el mensaje

                txtPozos.setText(String.valueOf(mejorPozos)); // Actualiza el campo de pozos con el mejor valor
                txtTamañoInstalacion.setText(FMT2.format(mejorTamañoInst)); // Actualiza el campo de tamaño de instalación
                txtPlateauRateIs.setText(FMT2.format(mejorPlateauRateIs)); // Actualiza el campo de plateau rate

                pozosPerforar = mejorPozos; // Actualiza la variable de pozos
                tamañoInstalacion = mejorTamañoInst; // Actualiza la variable de tamaño de instalación
                plateauRateIs = mejorPlateauRateIs; // Actualiza la variable de plateau rate

                calcularValores(); // Recalcula todos los valores
                actualizarUI(); // Actualiza la interfaz
                calcularTablaProduccion(); // Recalcula la tabla de producción

                mostrarResultadosOptimizacion(); // Muestra el diálogo de resultados
            }
        }.execute(); // Ejecuta el SwingWorker
    }

    private double calcularNPVSimulacion(double stoiip, double recup, double buenaTasa, int pozos, double descuento, double costo, double tamañoInst, double plateauIs) { // Método para calcular NPV en una simulación
        double res = stoiip * recup / 100.0; // Calcula reservas
        double maxPR = (plateauIs / 100.0) * res / 0.365; // Calcula max plateau rate
        double pr = Math.min(maxPR, Math.min(buenaTasa * pozos, tamañoInst)); // Calcula plateau rate efectivo
        double aum = 0.365 * pr * 0.5 * timeToPlateau; // Calcula aumento de producción
        double pp = Math.max(0, plateauEndsAt * (res / 100.0) - aum); // Calcula plateau production
        double pea = pp / (0.365 * pr) + timeToPlateau; // Calcula plateau ends at
        double fd = 0.365 * (pr - tarifaMinima) / (res - pp - aum); // Calcula factor de declive
        double vp = (tarifaMinima > 0) ? pea - Math.log(tarifaMinima / pr) / fd : 1e20; // Calcula vida de producción

        double resDesc = 0; // Inicializa reservas descontadas

        for (int año = 1; año <= AÑOS; año++) { // Itera sobre cada año
            double prodAnual; // Variable para producción anual

            if (año < timeToPlateau + 1) { // Si está en fase de aumento
                prodAnual = año * 0.365 * pr / (timeToPlateau + 1); // Calcula producción anual
            } else { // Si está en plateau o declive
                double term1 = 0.365 * pr * Math.max(0, Math.min(pea + 1 - año, 1)); // Calcula primer término
                double exp1 = Math.exp(-fd * Math.max(0, Math.min(vp, año - 1) - pea)); // Calcula exponencial anterior
                double exp2 = Math.exp(-fd * Math.max(Math.min(vp, año) - pea, 0)); // Calcula exponencial actual
                double term2 = 0.365 * pr * (exp1 - exp2) / fd; // Calcula segundo término
                prodAnual = term1 + term2; // Suma ambos términos
            }

            if (año == 1) { // Si es el primer año
                resDesc = prodAnual; // Reservas descontadas es igual a producción
            } else { // Para años subsiguientes
                double desc = Math.pow(1.0 + 0.01 * descuento, año - 1); // Calcula factor de descuento
                resDesc += prodAnual / desc; // Suma producción descontada
            }
        }

        double costoPozos = costo * pozos; // Calcula costo total de pozos
        double costoInst = buscarCostoInstalacion(tamañoInst); // Busca costo de instalación

        return resDesc * margenPetroleo - costoPozos - costoInst; // Calcula y retorna NPV
    }

    private void mostrarResultadosOptimizacion() { // Método para mostrar diálogo de resultados
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crea diálogo no modal
        dlg.setLayout(new BorderLayout(15, 15)); // Establece el layout

        JPanel main = new JPanel(new BorderLayout(15, 15)); // Crea panel principal
        main.setBackground(Color.WHITE); // Establece fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Crea borde vacío

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5)); // Crea panel de encabezado
        header.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lblSim = new JLabel("📊 " + NUM_SIMULACIONES + " simulaciones completadas", SwingConstants.CENTER); // Crea etiqueta de simulaciones
        lblSim.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Establece la fuente
        lblSim.setForeground(COLOR_HEADER); // Establece el color

        JLabel lblVista = new JLabel("Vista de mejor solución encontrada", SwingConstants.CENTER); // Crea etiqueta de vista
        lblVista.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Establece la fuente
        lblVista.setForeground(Color.GRAY); // Establece color gris

        header.add(lblSim); // Añade etiqueta de simulaciones
        header.add(lblVista); // Añade etiqueta de vista

        main.add(header, BorderLayout.NORTH); // Añade encabezado en la parte superior

        JPanel centro = new JPanel(new GridLayout(3, 1, 15, 15)); // Crea panel central con 3 filas
        centro.setBackground(Color.WHITE); // Establece fondo blanco

        JPanel npvPanel = new JPanel(new BorderLayout()); // Crea panel para NPV
        npvPanel.setBackground(new Color(232, 245, 233)); // Establece fondo verde claro
        npvPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2), BorderFactory.createEmptyBorder(15, 15, 15, 15))); // Crea borde compuesto verde

        JLabel lblNPVTitle = new JLabel("🎯 NPV Percentil 10% (Optimizado)", SwingConstants.CENTER); // Crea título de NPV
        lblNPVTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece la fuente
        lblNPVTitle.setForeground(new Color(27, 94, 32)); // Establece color verde oscuro

        JLabel lblNPVValue = new JLabel("$ " + FMT2.format(mejorNPV) + " mm", SwingConstants.CENTER); // Crea etiqueta con valor de NPV
        lblNPVValue.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Establece fuente grande
        lblNPVValue.setForeground(new Color(27, 94, 32)); // Establece color verde oscuro

        npvPanel.add(lblNPVTitle, BorderLayout.NORTH); // Añade título en la parte superior
        npvPanel.add(lblNPVValue, BorderLayout.CENTER); // Añade valor en el centro

        centro.add(npvPanel); // Añade panel de NPV al centro
        centro.add(crearPanelVariablesOptimas()); // Añade panel de variables óptimas
        centro.add(crearPanelEstadisticas()); // Añade panel de estadísticas

        main.add(centro, BorderLayout.CENTER); // Añade el centro al panel principal

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Crea panel de botones
        botonesPanel.setBackground(Color.WHITE); // Establece fondo blanco

        JButton btnHistograma = crearBoton("📊 Ver Histograma NPV", new Color(33, 150, 243), 200, 35); // Crea botón de histograma
        btnHistograma.addActionListener(e -> mostrarDistribucionNPV()); // Añade acción para mostrar histograma

        JButton btnCerrar = crearBoton("✓ Cerrar", new Color(76, 175, 80), 120, 35); // Crea botón de cerrar
        btnCerrar.addActionListener(e -> dlg.dispose()); // Añade acción para cerrar el diálogo

        botonesPanel.add(btnHistograma); // Añade botón de histograma
        botonesPanel.add(btnCerrar); // Añade botón de cerrar

        main.add(botonesPanel, BorderLayout.SOUTH); // Añade panel de botones en la parte inferior

        dlg.add(main); // Añade el panel principal al diálogo
        dlg.setSize(800, 700); // Establece el tamaño del diálogo
        dlg.setLocationRelativeTo(this); // Centra el diálogo respecto a la ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    }

    private JPanel crearPanelVariablesOptimas() { // Método para crear panel de variables óptimas
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto

        JLabel titulo = new JLabel("Variables de Decisión Óptimas", SwingConstants.CENTER); // Crea título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente
        titulo.setForeground(COLOR_HEADER); // Establece color
        panel.add(titulo, BorderLayout.NORTH); // Añade título

        String[][] datos = {{"Pozos a perforar", String.valueOf(mejorPozos), "pozos"}, {"Tamaño de instalación", FMT2.format(mejorTamañoInst), "mbd"}, {"Plateau rate is", FMT2.format(mejorPlateauRateIs), "% reservas/año"}}; // Define los datos a mostrar

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 8)); // Crea grid de 3x3
        grid.setBackground(Color.WHITE); // Establece fondo blanco
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Crea borde vacío

        for (String[] row : datos) { // Itera sobre cada fila de datos
            JLabel lblNombre = new JLabel(row[0]); // Crea etiqueta para el nombre
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente

            JLabel lblValor = new JLabel(row[1]); // Crea etiqueta para el valor
            lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente en negrita
            lblValor.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
            lblValor.setOpaque(true); // Hace la etiqueta opaca
            lblValor.setBackground(COLOR_DECISION); // Establece color de fondo amarillo
            lblValor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), BorderFactory.createEmptyBorder(3, 8, 3, 8))); // Crea borde compuesto

            JLabel lblUnidad = new JLabel(row[2]); // Crea etiqueta para la unidad
            lblUnidad.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente
            lblUnidad.setForeground(Color.GRAY); // Establece color gris

            grid.add(lblNombre); // Añade nombre al grid
            grid.add(lblValor); // Añade valor al grid
            grid.add(lblUnidad); // Añade unidad al grid
        }

        panel.add(grid, BorderLayout.CENTER); // Añade grid al panel

        return panel; // Retorna el panel
    }

    private JPanel crearPanelEstadisticas() { // Método para crear panel de estadísticas
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea el panel
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto naranja

        JLabel titulo = new JLabel("Estadísticas NPV (Mejor simulación)", SwingConstants.CENTER); // Crea título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente
        titulo.setForeground(new Color(230, 81, 0)); // Establece color naranja oscuro
        panel.add(titulo, BorderLayout.NORTH); // Añade título

        if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos de la mejor simulación
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de la lista
            Collections.sort(npvs); // Ordena la lista

            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula la media
            double min = npvs.get(0); // Obtiene el mínimo
            double max = npvs.get(npvs.size() - 1); // Obtiene el máximo
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula percentil 10
            double p50 = npvs.get((int) (npvs.size() * 0.50)); // Calcula mediana
            double p90 = npvs.get((int) (npvs.size() * 0.90)); // Calcula percentil 90

            JPanel grid = new JPanel(new GridLayout(6, 2, 8, 6)); // Crea grid de 6x2
            grid.setBackground(Color.WHITE); // Establece fondo blanco
            grid.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Crea borde vacío

            addStatRow(grid, "Media:", "$ " + FMT2.format(media) + " mm"); // Añade fila de media
            addStatRow(grid, "Percentil 10%:", "$ " + FMT2.format(p10) + " mm"); // Añade fila de percentil 10
            addStatRow(grid, "Mediana (P50):", "$ " + FMT2.format(p50) + " mm"); // Añade fila de mediana
            addStatRow(grid, "Percentil 90%:", "$ " + FMT2.format(p90) + " mm"); // Añade fila de percentil 90
            addStatRow(grid, "Mínimo:", "$ " + FMT2.format(min) + " mm"); // Añade fila de mínimo
            addStatRow(grid, "Máximo:", "$ " + FMT2.format(max) + " mm"); // Añade fila de máximo

            panel.add(grid, BorderLayout.CENTER); // Añade grid al panel
        }

        return panel; // Retorna el panel
    }

    private void addStatRow(JPanel grid, String label, String value) { // Método para añadir fila de estadística
        JLabel lblLabel = new JLabel(label); // Crea etiqueta para el nombre
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente
        lblLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha

        JLabel lblValue = new JLabel(value); // Crea etiqueta para el valor
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente en negrita
        lblValue.setHorizontalAlignment(SwingConstants.LEFT); // Alinea a la izquierda

        grid.add(lblLabel); // Añade etiqueta al grid
        grid.add(lblValue); // Añade valor al grid
    }

    private void mostrarDistribucionNPV() { // Método para mostrar el diálogo de distribución NPV
        JDialog dlg = new JDialog(this, "Previsión: NPV - Distribución", false); // Crea diálogo no modal
        dlg.setLayout(new BorderLayout(10, 10)); // Establece layout

        JPanel main = new JPanel(new BorderLayout(10, 10)); // Crea panel principal
        main.setBackground(Color.WHITE); // Establece fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Crea borde vacío

        JLabel header = new JLabel(NUM_PRUEBAS_MC + " pruebas - Vista de frecuencia", SwingConstants.CENTER); // Crea encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente
        header.setForeground(COLOR_HEADER); // Establece color
        main.add(header, BorderLayout.NORTH); // Añade encabezado

        JPanel histograma = new JPanel() { // Crea panel personalizado para el histograma
            @Override
            protected void paintComponent(Graphics g) { // Sobrescribe método de pintado
                super.paintComponent(g); // Llama al método padre
                if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos
                    dibujarHistograma(g, getWidth(), getHeight()); // Dibuja el histograma
                }
            }
        };
        histograma.setBackground(Color.WHITE); // Establece fondo blanco
        histograma.setPreferredSize(new Dimension(750, 450)); // Establece tamaño preferido
        histograma.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Crea borde gris

        main.add(histograma, BorderLayout.CENTER); // Añade histograma al centro

        if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de la lista
            Collections.sort(npvs); // Ordena la lista
            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula percentil 10

            JPanel stats = new JPanel(new GridLayout(1, 3, 20, 5)); // Crea panel de estadísticas con 3 columnas
            stats.setBackground(Color.WHITE); // Establece fondo blanco
            stats.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0)); // Crea borde vacío

            stats.add(crearStatLabel("10% = $ " + FMT2.format(p10) + " mm")); // Añade etiqueta de percentil 10
            stats.add(crearStatLabel("Media = $ " + FMT2.format(media) + " mm")); // Añade etiqueta de media
            stats.add(crearStatLabel(FMT0.format(npvs.size()) + " muestras")); // Añade etiqueta de número de muestras

            main.add(stats, BorderLayout.SOUTH); // Añade estadísticas en la parte inferior
        }

        dlg.add(main); // Añade panel principal al diálogo
        dlg.setSize(800, 600); // Establece tamaño del diálogo
        dlg.setLocationRelativeTo(this); // Centra el diálogo
        dlg.setVisible(true); // Hace visible el diálogo
    }

    private void dibujarHistograma(Graphics g, int width, int height) { // Método para dibujar el histograma
        if (mejorSimulacionNPVs == null || mejorSimulacionNPVs.isEmpty()) { // Si no hay datos
            return; // Sale del método
        }

        Graphics2D g2 = (Graphics2D) g; // Convierte a Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Activa antialiasing

        int margin = 60; // Define margen
        int chartWidth = width - 2 * margin; // Calcula ancho del gráfico
        int chartHeight = height - 2 * margin; // Calcula altura del gráfico

        if (chartWidth <= 0 || chartHeight <= 0) { // Si el tamaño no es válido
            return; // Sale del método
        }

        List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de la lista
        Collections.sort(npvs); // Ordena la lista

        double minVal = npvs.get(0); // Obtiene valor mínimo
        double maxVal = npvs.get(npvs.size() - 1); // Obtiene valor máximo

        double range = maxVal - minVal; // Calcula rango
        if (range <= 0) { // Si el rango es 0 o negativo
            g2.setColor(new Color(100, 181, 246)); // Establece color azul
            int barX = margin + chartWidth / 2 - 10; // Calcula posición X de la barra
            int barWidth = 20; // Define ancho de barra
            int barHeight = chartHeight; // Define altura de barra
            g2.fillRect(barX, margin, barWidth, barHeight); // Dibuja barra única

            dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, npvs.size()); // Dibuja ejes
            return; // Sale del método
        }

        double binWidth = range / NUM_BINS_HISTOGRAMA; // Calcula ancho de cada bin
        int[] bins = new int[NUM_BINS_HISTOGRAMA]; // Array para contar frecuencias

        for (double val : npvs) { // Itera sobre cada valor
            int binIndex = (int) ((val - minVal) / binWidth); // Calcula índice del bin
            if (binIndex >= NUM_BINS_HISTOGRAMA) { // Si el índice está fuera de rango
                binIndex = NUM_BINS_HISTOGRAMA - 1; // Ajusta al último bin
            }
            if (binIndex < 0) { // Si el índice es negativo
                binIndex = 0; // Ajusta al primer bin
            }
            bins[binIndex]++; // Incrementa contador del bin
        }

        int maxBin = 0; // Inicializa máxima frecuencia
        for (int bin : bins) { // Itera sobre los bins
            if (bin > maxBin) { // Si este bin tiene más elementos
                maxBin = bin; // Actualiza máxima frecuencia
            }
        }

        if (maxBin == 0) { // Si no hay bins con datos
            maxBin = 1; // Establece 1 para evitar división por cero
        }

        double barWidthPixels = (double) chartWidth / NUM_BINS_HISTOGRAMA; // Calcula ancho de barra en píxeles

        int minBarWidth = Math.max(1, (int) Math.floor(barWidthPixels) - 1); // Calcula ancho mínimo de barra

        g2.setColor(new Color(100, 181, 246)); // Establece color azul para las barras

        for (int i = 0; i < NUM_BINS_HISTOGRAMA; i++) { // Itera sobre cada bin
            if (bins[i] > 0) { // Si el bin tiene datos
                int barHeight = (int) Math.round(((double) bins[i] / maxBin) * chartHeight); // Calcula altura de barra
                int x = margin + (int) Math.round(i * barWidthPixels); // Calcula posición X
                int y = height - margin - barHeight; // Calcula posición Y
                if (barHeight < 1) { // Si la altura es menor a 1
                    barHeight = 1; // Establece altura mínima de 1
                }
                g2.fillRect(x, y, minBarWidth, barHeight); // Dibuja la barra
            }
        }

        dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, maxBin); // Dibuja ejes y etiquetas
        dibujarLineasPercentiles(g2, width, height, margin, chartWidth, npvs, minVal, maxVal); // Dibuja líneas de percentiles
    }

    private void dibujarEjesYEtiquetas(Graphics2D g2, int width, int height, int margin, int chartWidth, int chartHeight, double minVal, double maxVal, int maxBin) { // Método para dibujar ejes y etiquetas
        g2.setColor(Color.BLACK); // Establece color negro
        g2.setStroke(new BasicStroke(2)); // Establece grosor de línea
        g2.drawLine(margin, height - margin, width - margin, height - margin); // Dibuja eje X
        g2.drawLine(margin, margin, margin, height - margin); // Dibuja eje Y

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece fuente
        for (int i = 0; i <= 5; i++) { // Itera sobre 6 puntos del eje X
            double val = minVal + (maxVal - minVal) * i / 5.0; // Calcula valor en este punto
            int x = margin + (int) Math.round(chartWidth * i / 5.0); // Calcula posición X
            String label = FMT0.format(val); // Formatea el valor
            FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
            int labelWidth = fm.stringWidth(label); // Calcula ancho de la etiqueta
            g2.drawString(label, x - labelWidth / 2, height - margin + 20); // Dibuja la etiqueta
            g2.drawLine(x, height - margin, x, height - margin + 5); // Dibuja marca en el eje
        }

        for (int i = 0; i <= 5; i++) { // Itera sobre 6 puntos del eje Y
            int val = (int) Math.round(maxBin * i / 5.0); // Calcula valor en este punto
            int y = height - margin - (int) Math.round(chartHeight * i / 5.0); // Calcula posición Y
            String label = String.valueOf(val); // Convierte valor a String

            FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
            int labelWidth = fm.stringWidth(label); // Calcula ancho de la etiqueta
            g2.drawString(label, margin - labelWidth - 10, y + 5); // Dibuja la etiqueta

            g2.drawLine(margin - 5, y, margin, y); // Dibuja marca en el eje
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente en negrita
        String xLabel = "NPV ($mm)"; // Define etiqueta del eje X
        FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
        int xLabelWidth = fm.stringWidth(xLabel); // Calcula ancho de la etiqueta
        g2.drawString(xLabel, width / 2 - xLabelWidth / 2, height - 10); // Dibuja etiqueta del eje X

        g2.rotate(-Math.PI / 2); // Rota el contexto gráfico 90 grados
        String yLabel = "Frecuencia"; // Define etiqueta del eje Y
        int yLabelWidth = fm.stringWidth(yLabel); // Calcula ancho de la etiqueta
        g2.drawString(yLabel, -height / 2 - yLabelWidth / 2, 15); // Dibuja etiqueta del eje Y
        g2.rotate(Math.PI / 2); // Restaura la rotación
    }

    private void dibujarLineasPercentiles(Graphics2D g2, int width, int height, int margin, int chartWidth, List<Double> npvs, double minVal, double maxVal) { // Método para dibujar líneas de percentiles
        double range = maxVal - minVal; // Calcula rango
        if (range <= 0) { // Si el rango no es válido
            return; // Sale del método
        }

        double p10 = npvs.get((int) (npvs.size() * 0.10)); // Obtiene percentil 10
        int xP10 = margin + (int) Math.round((p10 - minVal) / range * chartWidth); // Calcula posición X del percentil 10

        g2.setColor(new Color(244, 67, 54)); // Establece color rojo
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Establece línea discontinua
        g2.drawLine(xP10, margin, xP10, height - margin); // Dibuja línea vertical del percentil 10

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente
        g2.drawString("P10", xP10 - 15, margin - 10); // Dibuja etiqueta P10

        double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
        int xMedia = margin + (int) Math.round((media - minVal) / range * chartWidth); // Calcula posición X de la media

        g2.setColor(new Color(76, 175, 80)); // Establece color verde
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Establece línea discontinua
        g2.drawLine(xMedia, margin, xMedia, height - margin); // Dibuja línea vertical de la media
        g2.drawString("Media", xMedia - 20, margin - 10); // Dibuja etiqueta Media
    }

    public static void main(String[] args) { // Método principal para iniciar la aplicación
        try { // Inicia bloque try
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Establece el look and feel del sistema
        } catch (Exception e) { // Captura excepciones
            e.printStackTrace(); // Imprime el stack trace
        }

        SwingUtilities.invokeLater(() -> { // Ejecuta en el hilo de eventos de Swing
            OilReservesSimulatorEditable sim = new OilReservesSimulatorEditable(); // Crea instancia del simulador
            sim.setVisible(true); // Hace visible la ventana
        });
    }
}