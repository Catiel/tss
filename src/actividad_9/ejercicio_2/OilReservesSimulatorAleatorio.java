package actividad_9.ejercicio_2; // Declaración del paquete donde reside la clase

import javax.swing.*; // Importa componentes Swing para interfaces gráficas
import javax.swing.table.*; // Importa clases para trabajar con tablas
import java.awt.*; // Importa componentes gráficos AWT
import java.text.DecimalFormat; // Importa clase para formatear números decimales
import java.text.DecimalFormatSymbols; // Importa símbolos de formato decimal personalizados
import java.util.*; // Importa utilidades generales de Java
import java.util.List; // Importa interfaz List
import java.util.Locale; // Importa clase Locale para configuración regional

import org.apache.commons.math3.distribution.*; // Importa distribuciones estadísticas de Apache Commons Math3

public class OilReservesSimulatorAleatorio extends JFrame { // Clase pública que extiende JFrame
    private static final int NUM_SIMULACIONES = 563; // Constante: número total de simulaciones
    private static final int NUM_PRUEBAS_MC = 1000; // Constante: pruebas Monte Carlo por simulación
    private static final int AÑOS = 50; // Constante: período de análisis en años
    private static final int MIN_TRIALS_FOR_CHECK = 500; // Constante: mínimo de pruebas para verificar early stopping
    private static final int CHECK_INTERVAL = 500; // Constante: intervalo de verificación early stopping
    private static final int NUM_BINS_HISTOGRAMA = 50; // Constante: número de barras en histograma

    private static final DecimalFormat FMT2 = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US)); // Formato con 2 decimales y punto decimal
    private static final DecimalFormat FMT0 = new DecimalFormat("#,##0", DecimalFormatSymbols.getInstance(Locale.US)); // Formato sin decimales y punto decimal

    private static final Color COLOR_HEADER = new Color(79, 129, 189); // Constante: color azul para encabezados
    private static final Color COLOR_SUPOSICION = new Color(146, 208, 80); // Constante: color verde para suposiciones
    private static final Color COLOR_DECISION = new Color(255, 255, 0); // Constante: color amarillo para decisiones
    private static final Color COLOR_CALCULADO = new Color(217, 217, 217); // Constante: color gris para valores calculados
    private static final Color COLOR_NPV = new Color(0, 255, 255); // Constante: color cian para NPV
    private static final Color COLOR_PANEL_BG = new Color(248, 248, 248); // Constante: color gris claro para fondo de paneles

    private double stoiip = 1500.0; // Variable: STOIIP inicial en millones de barriles
    private double recuperacion = 42.0; // Variable: porcentaje de recuperación inicial
    private double buenaTasa = 10.0; // Variable: tasa de producción por pozo en mbd
    private int pozosPerforar = 25; // Variable: número de pozos a perforar
    private double factorDescuento = 10.0; // Variable: factor de descuento anual en porcentaje
    private double buenCosto = 10.0; // Variable: costo por pozo en millones de dólares
    private double tamañoInstalacion = 250.0; // Variable: tamaño de instalación en mbd
    private double plateauRateIs = 10.0; // Variable: plateau rate como porcentaje de reservas por año

    private double timeToPlateau = 2.0; // Variable EDITABLE: tiempo para alcanzar plateau en años
    private double tarifaMinima = 10.0; // Variable EDITABLE: tarifa mínima de producción en mbd
    private double margenPetroleo = 2.0; // Variable EDITABLE: margen por barril en dólares
    private double plateauEndsAt = 65.0; // Variable EDITABLE: porcentaje de reservas donde termina plateau

    private double[][] costosInstalaciones = {{50, 70}, {100, 130}, {150, 180}, {200, 220}, {250, 250}, {300, 270}, {350, 280}}; // Array bidimensional EDITABLE: tabla de costos de instalaciones

    private double reservas; // Variable: reservas recuperables calculadas en mmbbls
    private double maxPlateauRate; // Variable: tasa máxima de plateau calculada en mbd
    private double plateauRate; // Variable: tasa de plateau efectiva en mbd
    private double aumentarProduccion; // Variable: producción durante ramp-up en mmbbls
    private double plateauProduction; // Variable: producción durante plateau en mmbbls
    private double plateauEndsAtCalc; // Variable: tiempo calculado cuando termina plateau en años
    private double factorDeclive; // Variable: factor de declive exponencial
    private double vidaProduccion; // Variable: vida total de producción en años
    private double reservasDescontadas; // Variable: reservas descontadas en mmbbls
    private double costosPozo; // Variable: costos totales de pozos en millones
    private double costosInstalacionesCalc; // Variable: costos de instalaciones calculados en millones
    private double npv; // Variable: NPV final en millones de dólares

    private JTextField txtStoiip, txtRecuperacion, txtBuenaTasa, txtPozos; // Variables: campos de texto no editables
    private JTextField txtFactorDescuento, txtBuenCosto, txtTamañoInstalacion, txtPlateauRateIs; // Variables: más campos de texto no editables
    private JTextField txtTimeToPlateau, txtTarifaMinima, txtMargenPetroleo, txtPlateauEndsAt; // Variables: campos de texto EDITABLES
    private JLabel lblReservas, lblMaxPlateau, lblPlateauRate, lblAumentar; // Variables: etiquetas para valores calculados
    private JLabel lblPlateauProd, lblPlateauEnds, lblFactorDeclive, lblVidaProd; // Variables: más etiquetas para valores calculados
    private JLabel lblReservasDesc, lblCostosPozo, lblCostosInst, lblNPV; // Variables: etiquetas para resultados finales
    private DefaultTableModel modeloTabla; // Variable: modelo de datos para tabla de 50 años
    private DefaultTableModel modeloCostos; // Variable: modelo de datos para tabla de costos EDITABLE
    private JProgressBar progressBar; // Variable: barra de progreso visual
    private JLabel lblProgreso; // Variable: etiqueta de texto de progreso
    private JTabbedPane tabbedPane; // Variable: panel con pestañas

    private double mejorNPV = Double.NEGATIVE_INFINITY; // Variable: mejor NPV encontrado inicializado en infinito negativo
    private int mejorPozos = 25; // Variable: mejor número de pozos encontrado
    private double mejorTamañoInst = 250.0; // Variable: mejor tamaño de instalación encontrado
    private double mejorPlateauRateIs = 10.0; // Variable: mejor plateau rate encontrado
    private List<Double> todosNPV = new ArrayList<>(); // Variable: lista con todos los NPVs de todas las simulaciones
    private List<Double> mejorSimulacionNPVs = new ArrayList<>(); // Variable: lista con NPVs de mejor simulación
    private Random randomGenerator = new Random(); // Variable: generador de números aleatorios sin semilla fija

    public OilReservesSimulatorAleatorio() { // Constructor de la clase
        super("Simulación de Reservas Petroleras - Crystal Ball"); // Llama al constructor padre con título de ventana
        configurarUI(); // Llama al método para configurar interfaz de usuario
        generarValoresAleatorios(); // Llama al método para generar valores aleatorios iniciales
        calcularValores(); // Llama al método para calcular valores derivados
        actualizarUI(); // Llama al método para actualizar etiquetas de interfaz
        calcularTablaProduccion(); // Llama al método para calcular tabla de 50 años
        setSize(1600, 950); // Establece tamaño de ventana en píxeles
        setLocationRelativeTo(null); // Centra ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cierra aplicación al cerrar ventana
    } // Fin del constructor

    private void generarValoresAleatorios() { // Método para generar valores aleatorios
        timeToPlateau = 1.0 + randomGenerator.nextDouble() * 4.0; // Genera valor aleatorio entre 1.0 y 5.0 años
        tarifaMinima = 5.0 + randomGenerator.nextDouble() * 10.0; // Genera valor aleatorio entre 5.0 y 15.0 mbd
        margenPetroleo = 1.0 + randomGenerator.nextDouble() * 3.0; // Genera valor aleatorio entre 1.0 y 4.0 dólares por barril
        plateauEndsAt = 50.0 + randomGenerator.nextDouble() * 30.0; // Genera valor aleatorio entre 50.0% y 80.0%

        if (txtTimeToPlateau != null) { // Verifica si campo de texto existe
            txtTimeToPlateau.setText(String.format(Locale.US, "%.2f", timeToPlateau)); // Actualiza campo con valor aleatorio formateado
        } // Fin del if
        if (txtTarifaMinima != null) { // Verifica si campo existe
            txtTarifaMinima.setText(String.format(Locale.US, "%.2f", tarifaMinima)); // Actualiza campo con formato US
        } // Fin del if
        if (txtMargenPetroleo != null) { // Verifica si campo existe
            txtMargenPetroleo.setText(String.format(Locale.US, "%.2f", margenPetroleo)); // Actualiza campo con formato US
        } // Fin del if
        if (txtPlateauEndsAt != null) { // Verifica si campo existe
            txtPlateauEndsAt.setText(String.format(Locale.US, "%.2f", plateauEndsAt)); // Actualiza campo con formato US
        } // Fin del if
    } // Fin del método generarValoresAleatorios

    private void configurarUI() { // Método para configurar interfaz de usuario
        JPanel main = new JPanel(new BorderLayout(15, 15)); // Crea panel principal con BorderLayout y gaps de 15 píxeles
        main.setBackground(COLOR_PANEL_BG); // Establece color de fondo gris claro
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Crea margen de 15 píxeles en todos los lados

        JLabel titulo = new JLabel("Oil Field Development - Simulación de Reservas Petroleras", SwingConstants.CENTER); // Crea etiqueta de título centrada
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Establece fuente Segoe UI negrita tamaño 22
        titulo.setForeground(new Color(31, 78, 120)); // Establece color azul oscuro para texto
        titulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Crea margen de 5px arriba y 10px abajo
        main.add(titulo, BorderLayout.NORTH); // Agrega título en parte superior del panel

        tabbedPane = new JTabbedPane(JTabbedPane.TOP); // Crea panel con pestañas en parte superior
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente negrita tamaño 12 para pestañas

        JPanel dashboardPanel = crearPanelDashboard(); // Llama método que retorna panel dashboard
        tabbedPane.addTab("📊 Dashboard Principal", dashboardPanel); // Agrega pestaña con emoji y panel dashboard

        JPanel tablaPanel = crearPanelTablaCompleta(); // Llama método que retorna panel tabla
        tabbedPane.addTab("📈 Perfil de Producción (50 años)", tablaPanel); // Agrega segunda pestaña con emoji y panel tabla

        main.add(tabbedPane, BorderLayout.CENTER); // Agrega panel de pestañas al centro
        main.add(crearPanelControl(), BorderLayout.SOUTH); // Agrega panel de controles en parte inferior

        add(main); // Agrega panel principal completo a la ventana JFrame
    } // Fin del método configurarUI

    private JPanel crearPanelDashboard() { // Método que crea y retorna panel dashboard
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea panel con BorderLayout y gaps de 10 píxeles
        panel.setBackground(COLOR_PANEL_BG); // Establece color de fondo gris claro

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // Crea panel con GridLayout de 1 fila por 3 columnas con gap horizontal de 15
        topPanel.setBackground(COLOR_PANEL_BG); // Establece color de fondo gris claro
        topPanel.add(crearPanelEntrada()); // Agrega panel de entrada en primera columna
        topPanel.add(crearPanelCalculado()); // Agrega panel de valores calculados en segunda columna
        topPanel.add(crearPanelResultadosFinales()); // Agrega panel de resultados finales en tercera columna

        panel.add(topPanel, BorderLayout.NORTH); // Agrega panel superior con 3 columnas en parte superior
        panel.add(crearPanelResumenTabla(), BorderLayout.CENTER); // Agrega panel resumen tabla en centro

        return panel; // Retorna panel dashboard completo
    } // Fin del método crearPanelDashboard

    private JPanel crearPanelEntrada() { // Método que crea panel de variables de entrada
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea azul de 2px y margen interno de 10px

        JLabel header = new JLabel("Variables de Entrada", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Establece color azul para texto
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea margen inferior de 10px
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        JPanel grid = new JPanel(new GridBagLayout()); // Crea panel con GridBagLayout para control fino
        grid.setBackground(Color.WHITE); // Establece fondo blanco
        GridBagConstraints gbc = new GridBagConstraints(); // Crea objeto de restricciones de posicionamiento
        gbc.fill = GridBagConstraints.HORIZONTAL; // Los componentes se expanden horizontalmente
        gbc.insets = new Insets(3, 5, 3, 5); // Establece márgenes: 3px arriba/abajo, 5px izquierda/derecha

        int row = 0; // Inicializa contador de filas en 0
        addGridRow(grid, gbc, row++, "STOIIP", txtStoiip = crearTextField("1500.00", COLOR_SUPOSICION), "mmbbls", COLOR_SUPOSICION); // Agrega fila STOIIP con fondo verde y post-incrementa row
        txtStoiip.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Recuperación", txtRecuperacion = crearTextField("42.0", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Agrega fila Recuperación con fondo verde
        txtRecuperacion.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Time to plateau", txtTimeToPlateau = crearTextField("2.00", Color.WHITE), "years", Color.WHITE); // Agrega fila Time to plateau con fondo blanco EDITABLE
        addGridRow(grid, gbc, row++, "Buena tasa", txtBuenaTasa = crearTextField("10.00", COLOR_SUPOSICION), "mbd", COLOR_SUPOSICION); // Agrega fila Buena tasa con fondo verde
        txtBuenaTasa.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Pozos a perforar", txtPozos = crearTextField("25", COLOR_DECISION), "", COLOR_DECISION); // Agrega fila Pozos con fondo amarillo
        txtPozos.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Tarifa mínima", txtTarifaMinima = crearTextField("10.00", Color.WHITE), "mbd", Color.WHITE); // Agrega fila Tarifa mínima con fondo blanco EDITABLE
        addGridRow(grid, gbc, row++, "Factor de descuento", txtFactorDescuento = crearTextField("10.00", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Agrega fila Factor descuento con fondo verde
        txtFactorDescuento.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Buen costo", txtBuenCosto = crearTextField("10.00", COLOR_SUPOSICION), "$mm", COLOR_SUPOSICION); // Agrega fila Buen costo con fondo verde
        txtBuenCosto.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Tamaño instalación", txtTamañoInstalacion = crearTextField("250.00", COLOR_DECISION), "mbd", COLOR_DECISION); // Agrega fila Tamaño instalación con fondo amarillo
        txtTamañoInstalacion.setEditable(false); // Establece campo como NO editable
        addGridRow(grid, gbc, row++, "Margen petróleo", txtMargenPetroleo = crearTextField("2.00", Color.WHITE), "$/bbl", Color.WHITE); // Agrega fila Margen petróleo con fondo blanco EDITABLE
        addGridRow(grid, gbc, row++, "Plateau ends at", txtPlateauEndsAt = crearTextField("65.0", Color.WHITE), "% reservas", Color.WHITE); // Agrega fila Plateau ends at con fondo blanco EDITABLE
        addGridRow(grid, gbc, row++, "Plateau rate is", txtPlateauRateIs = crearTextField("10.0", COLOR_DECISION), "% res./año", COLOR_DECISION); // Agrega fila Plateau rate is con fondo amarillo
        txtPlateauRateIs.setEditable(false); // Establece campo como NO editable

        panel.add(grid, BorderLayout.CENTER); // Agrega grid con todas las filas al centro del panel
        panel.add(crearLeyenda(), BorderLayout.SOUTH); // Agrega leyenda de colores en parte inferior

        return panel; // Retorna panel de entrada completo
    } // Fin del método crearPanelEntrada

    private void addGridRow(JPanel grid, GridBagConstraints gbc, int row, String label, JComponent campo, String unidad, Color bgColor) { // Método auxiliar para agregar fila al grid
        gbc.gridy = row; // Establece número de fila
        gbc.gridx = 0; // Establece columna 0 para etiqueta
        gbc.weightx = 0.4; // Asigna 40% del ancho a esta columna
        JLabel lbl = new JLabel(label + ":"); // Crea etiqueta con nombre y dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente normal tamaño 11
        grid.add(lbl, gbc); // Agrega etiqueta al grid con restricciones actuales

        gbc.gridx = 1; // Cambia a columna 1 para campo
        gbc.weightx = 0.4; // Asigna 40% del ancho a esta columna
        grid.add(campo, gbc); // Agrega campo (TextField o Label) al grid

        gbc.gridx = 2; // Cambia a columna 2 para unidad
        gbc.weightx = 0.2; // Asigna 20% del ancho a esta columna
        JLabel lblUnit = new JLabel(unidad); // Crea etiqueta con unidad de medida
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece fuente pequeña tamaño 10
        lblUnit.setForeground(Color.GRAY); // Establece color gris para unidad
        grid.add(lblUnit, gbc); // Agrega etiqueta de unidad al grid
    } // Fin del método addGridRow

    private JPanel crearLeyenda() { // Método que crea panel con leyenda de colores
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // Crea panel con FlowLayout alineado a izquierda con gaps de 10px horizontal y 5px vertical
        leyenda.setBackground(Color.WHITE); // Establece fondo blanco
        leyenda.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Crea margen superior de 5px

        leyenda.add(crearLeyendaItem("Suposición", COLOR_SUPOSICION)); // Agrega item de leyenda verde "Suposición"
        leyenda.add(crearLeyendaItem("Decisión", COLOR_DECISION)); // Agrega item de leyenda amarillo "Decisión"
        leyenda.add(crearLeyendaItem("Fijo", Color.WHITE)); // Agrega item de leyenda blanco "Fijo"

        return leyenda; // Retorna panel de leyenda completo
    } // Fin del método crearLeyenda

    private JPanel crearLeyendaItem(String texto, Color color) { // Método que crea un item individual de leyenda
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); // Crea panel con FlowLayout alineado a izquierda con gap pequeño
        item.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel colorBox = new JLabel("  "); // Crea etiqueta con dos espacios que será el cuadrito de color
        colorBox.setOpaque(true); // Hace la etiqueta opaca para que se vea el fondo
        colorBox.setBackground(color); // Establece color de fondo especificado
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel

        JLabel label = new JLabel(texto); // Crea etiqueta con texto descriptivo
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9)); // Establece fuente muy pequeña tamaño 9

        item.add(colorBox); // Agrega cuadro de color al panel
        item.add(label); // Agrega texto descriptivo al panel

        return item; // Retorna item de leyenda completo
    } // Fin del método crearLeyendaItem

    private JPanel crearPanelCalculado() { // Método que crea panel de valores calculados
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea azul de 2px y margen de 10px

        JLabel header = new JLabel("Valores Calculados", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Establece color azul para texto
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea margen inferior de 10px
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        JPanel grid = new JPanel(new GridLayout(8, 2, 5, 8)); // Crea grid de 8 filas por 2 columnas con gaps de 5px horizontal y 8px vertical
        grid.setBackground(Color.WHITE); // Establece fondo blanco

        grid.add(crearLabelParametro("Reservas")); // Agrega etiqueta "Reservas:"
        lblReservas = crearLabelCalculado("630.00 mmbbls"); // Crea y guarda referencia a etiqueta calculada con valor inicial
        grid.add(lblReservas); // Agrega etiqueta de valor de reservas

        grid.add(crearLabelParametro("Max plateau rate")); // Agrega etiqueta "Max plateau rate:"
        lblMaxPlateau = crearLabelCalculado("172.60 mbd"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblMaxPlateau); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Plateau rate")); // Agrega etiqueta "Plateau rate:"
        lblPlateauRate = crearLabelCalculado("172.60 mbd"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblPlateauRate); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Aumentar producción")); // Agrega etiqueta "Aumentar producción:"
        lblAumentar = crearLabelCalculado("63.00 mmbbls"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblAumentar); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Plateau production")); // Agrega etiqueta "Plateau production:"
        lblPlateauProd = crearLabelCalculado("346.50 mmbbls"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblPlateauProd); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Plateau ends at")); // Agrega etiqueta "Plateau ends at:"
        lblPlateauEnds = crearLabelCalculado("7.50 años"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblPlateauEnds); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Factor de declive")); // Agrega etiqueta "Factor de declive:"
        lblFactorDeclive = crearLabelCalculado("0.2692"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblFactorDeclive); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Vida de producción")); // Agrega etiqueta "Vida de producción:"
        lblVidaProd = crearLabelCalculado("18.08 años"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblVidaProd); // Agrega etiqueta de valor

        panel.add(grid, BorderLayout.CENTER); // Agrega grid completo al centro del panel

        return panel; // Retorna panel de valores calculados completo
    } // Fin del método crearPanelCalculado

    private JPanel crearPanelResultadosFinales() { // Método que crea panel de resultados finales
        JPanel container = new JPanel(new BorderLayout(5, 10)); // Crea contenedor con BorderLayout y gaps de 5px horizontal y 10px vertical
        container.setBackground(COLOR_PANEL_BG); // Establece color de fondo gris claro

        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel interno con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(237, 125, 49), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea naranja de 2px y margen de 10px

        JLabel header = new JLabel("Resultados Finales", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        header.setForeground(new Color(237, 125, 49)); // Establece color naranja para texto
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Crea margen inferior de 10px
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 8)); // Crea grid de 4 filas por 2 columnas con gaps de 5px horizontal y 8px vertical
        grid.setBackground(Color.WHITE); // Establece fondo blanco

        grid.add(crearLabelParametro("Reservas descontadas")); // Agrega etiqueta "Reservas descontadas:"
        lblReservasDesc = crearLabelCalculado("379.45 mmbbls"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblReservasDesc); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Costos del pozo")); // Agrega etiqueta "Costos del pozo:"
        lblCostosPozo = crearLabelCalculado("250.00 $mm"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblCostosPozo); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("Costos instalaciones")); // Agrega etiqueta "Costos instalaciones:"
        lblCostosInst = crearLabelCalculado("250.00 $mm"); // Crea y guarda referencia a etiqueta calculada
        grid.add(lblCostosInst); // Agrega etiqueta de valor

        grid.add(crearLabelParametro("NPV")); // Agrega etiqueta "NPV:"
        lblNPV = crearLabelCalculado("258.89 $mm"); // Crea y guarda referencia a etiqueta calculada para NPV
        lblNPV.setBackground(COLOR_NPV); // Establece fondo cian para destacar NPV
        lblNPV.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita más grande tamaño 13
        grid.add(lblNPV); // Agrega etiqueta de valor NPV

        panel.add(grid, BorderLayout.CENTER); // Agrega grid al centro del panel

        JLabel objetivo = new JLabel("🎯 Objetivo: Maximizar Percentil 10 de NPV", SwingConstants.CENTER); // Crea etiqueta con objetivo de optimización con emoji
        objetivo.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente negrita tamaño 12
        objetivo.setForeground(Color.RED); // Establece color rojo para destacar
        objetivo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Crea margen superior de 10px
        panel.add(objetivo, BorderLayout.SOUTH); // Agrega etiqueta objetivo en parte inferior

        container.add(panel, BorderLayout.NORTH); // Agrega panel de resultados en parte superior del contenedor
        container.add(crearPanelCostosInstalaciones(), BorderLayout.CENTER); // Agrega panel con tabla de costos en centro del contenedor

        return container; // Retorna contenedor completo con resultados y tabla de costos
    } // Fin del método crearPanelResultadosFinales

    private JPanel crearPanelCostosInstalaciones() { // Método que crea panel con tabla EDITABLE de costos de instalaciones
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 192, 203), 2), BorderFactory.createEmptyBorder(8, 8, 8, 8))); // Crea borde compuesto: línea rosa de 2px y margen de 8px

        JLabel header = new JLabel("Costos de Instalaciones", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada
        header.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente negrita tamaño 12
        header.setForeground(new Color(192, 80, 77)); // Establece color rojo oscuro para texto
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        String[] cols = {"Producción (mbd)", "Costo ($mm)"}; // Crea array con nombres de columnas de la tabla
        modeloCostos = new DefaultTableModel(cols, 0) { // Crea modelo de tabla con columnas especificadas y 0 filas iniciales y guarda referencia en variable de instancia
            public boolean isCellEditable(int r, int c) { // Sobrescribe método para definir si celdas son editables
                return true; // Retorna true: TODAS las celdas SON EDITABLES
            } // Fin del método isCellEditable
        }; // Fin de la creación del modelo

        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre array de costos de instalaciones que tiene 7 filas
            modeloCostos.addRow(new Object[]{FMT0.format(costosInstalaciones[i][0]), FMT0.format(costosInstalaciones[i][1])}); // Agrega fila con producción formateada en columna 0 y costo formateado en columna 1
        } // Fin del bucle for

        JTable tabla = new JTable(modeloCostos); // Crea tabla visual usando el modelo EDITABLE
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece fuente normal tamaño 10 para celdas
        tabla.setRowHeight(22); // Establece altura de filas en 22 píxeles
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10)); // Establece fuente negrita tamaño 10 para encabezado
        tabla.getTableHeader().setBackground(new Color(255, 192, 203)); // Establece fondo rosa para encabezado
        tabla.getTableHeader().setForeground(Color.BLACK); // Establece color negro para texto del encabezado

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Crea renderizador personalizado para centrar texto
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Establece alineación horizontal al centro
        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre cada columna de la tabla
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Aplica renderizador centrado a la columna
        } // Fin del bucle for

        JScrollPane scroll = new JScrollPane(tabla); // Crea panel con scroll que contiene la tabla
        scroll.setPreferredSize(new Dimension(250, 180)); // Establece tamaño preferido de 250px ancho por 180px alto
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll con tabla al centro del panel

        return panel; // Retorna panel completo con tabla de costos editable
    } // Fin del método crearPanelCostosInstalaciones

    private JPanel crearPanelResumenTabla() { // Método que crea panel con resumen de primeros años
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea azul de 2px y margen de 10px

        JLabel header = new JLabel("Perfil de Producción - Primeros 15 Años (Ver pestaña para datos completos)", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada con instrucción
        header.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita tamaño 13
        header.setForeground(COLOR_HEADER); // Establece color azul para texto
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        String[] cols = {"Año", "Tasa Anualizada\n(mbd)", "Producción Anual\n(mmb)", "Petróleo Acumulado\n(mmb)", "Petróleo Desc. Acum.\n(mmb)"}; // Crea array con nombres de columnas usando \n para saltos de línea

        DefaultTableModel modeloResumen = new DefaultTableModel(cols, 0) { // Crea modelo de tabla con columnas especificadas y 0 filas iniciales
            public boolean isCellEditable(int r, int c) { // Sobrescribe método para definir si celdas son editables
                return false; // Retorna false: celdas NO son editables
            } // Fin del método isCellEditable
        }; // Fin de la creación del modelo

        JTable tablaResumen = new JTable(modeloResumen); // Crea tabla visual usando el modelo no editable
        configurarEstiloTabla(tablaResumen); // Llama método para configurar estilos visuales de la tabla

        JScrollPane scroll = new JScrollPane(tablaResumen); // Crea panel con scroll que contiene la tabla
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll con tabla al centro del panel

        return panel; // Retorna panel completo con tabla resumen
    } // Fin del método crearPanelResumenTabla

    private JPanel crearPanelTablaCompleta() { // Método que crea panel con tabla completa de 50 años
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Crea margen de 10px en todos los lados

        JLabel header = new JLabel("Perfil de Producción Calculado - 50 Años", SwingConstants.CENTER); // Crea etiqueta de encabezado centrada
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Establece color azul para texto
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Crea margen de 5px arriba y 10px abajo
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        String[] cols = {"Año", "Tasa Anualizada (mbd)", "Producción Anual (mmb)", "Petróleo Acumulado (mmb)", "Petróleo con Descuento Acumulado (mmb)"}; // Crea array con nombres de columnas

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea modelo de tabla y guarda referencia en variable de instancia
            public boolean isCellEditable(int r, int c) { // Sobrescribe método para definir si celdas son editables
                return false; // Retorna false: celdas NO son editables
            } // Fin del método isCellEditable
        }; // Fin de la creación del modelo

        JTable tabla = new JTable(modeloTabla); // Crea tabla visual usando el modelo no editable
        configurarEstiloTabla(tabla); // Llama método para configurar estilos visuales de la tabla

        JScrollPane scroll = new JScrollPane(tabla); // Crea panel con scroll que contiene la tabla
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll con tabla al centro del panel

        return panel; // Retorna panel completo con tabla de 50 años
    } // Fin del método crearPanelTablaCompleta

    private void configurarEstiloTabla(JTable tabla) { // Método que configura estilos visuales de cualquier tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente normal tamaño 11 para celdas
        tabla.setRowHeight(26); // Establece altura de filas en 26 píxeles
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11 para encabezado
        tabla.getTableHeader().setBackground(COLOR_HEADER); // Establece fondo azul para encabezado
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece color blanco para texto del encabezado
        tabla.setGridColor(new Color(220, 220, 220)); // Establece color gris claro para líneas de cuadrícula
        tabla.setShowGrid(true); // Activa visualización de cuadrícula
        tabla.setIntercellSpacing(new Dimension(1, 1)); // Establece espacio entre celdas de 1px por 1px

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado anónimo
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Sobrescribe método de renderizado
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al método de la clase padre
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT); // Establece alineación: centro para columna 0, derecha para resto
                setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente normal tamaño 11

                if (r % 2 == 0) { // Si número de fila es par
                    setBackground(Color.WHITE); // Establece fondo blanco
                } else { // Si número de fila es impar
                    setBackground(new Color(245, 245, 245)); // Establece fondo gris muy claro para efecto zebra
                } // Fin del if-else

                if (c == 0) { // Si es la columna 0 (columna de años)
                    setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita
                } // Fin del if

                setForeground(Color.BLACK); // Establece color negro para texto
                return this; // Retorna el componente renderizado
            } // Fin del método getTableCellRendererComponent
        }; // Fin de la creación del renderizador

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Itera sobre cada columna de la tabla
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica renderizador personalizado a la columna
            if (i == 0) { // Si es la columna 0
                tabla.getColumnModel().getColumn(i).setPreferredWidth(60); // Establece ancho preferido de 60px
            } else { // Si es cualquier otra columna
                tabla.getColumnModel().getColumn(i).setPreferredWidth(160); // Establece ancho preferido de 160px
            } // Fin del if-else
        } // Fin del bucle for
    } // Fin del método configurarEstiloTabla

    private JPanel crearPanelControl() { // Método que crea panel de controles con botones y barra de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea panel con BorderLayout y gaps de 10px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 15, 10, 15))); // Crea borde compuesto: línea azul de 2px y margen de 10px vertical y 15px horizontal

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // Crea panel con FlowLayout centrado con gaps de 20px horizontal y 5px vertical
        botones.setBackground(Color.WHITE); // Establece fondo blanco

        JButton btnGenerar = crearBoton("🎲 Generar Aleatorios", new Color(156, 39, 176), 200, 40); // Crea botón morado con emoji y tamaño 200px por 40px
        btnGenerar.addActionListener(e -> { // Agrega listener que se ejecuta al hacer clic
            generarValoresAleatorios(); // Llama método para generar nuevos valores aleatorios
            leerValoresUI(); // Llama método para leer todos los valores de la interfaz incluyendo tabla de costos
            calcularValores(); // Llama método para calcular todos los valores derivados
            actualizarUI(); // Llama método para actualizar etiquetas de interfaz
            calcularTablaProduccion(); // Llama método para recalcular tabla de 50 años
            JOptionPane.showMessageDialog(this, "✓ Valores aleatorios generados correctamente", "Aleatorios", JOptionPane.INFORMATION_MESSAGE); // Muestra ventana de diálogo con mensaje de confirmación
        }); // Fin del listener

        JButton btnActualizar = crearBoton("🔄 Actualizar Cálculos", new Color(237, 125, 49), 200, 40); // Crea botón naranja con emoji y tamaño 200px por 40px
        btnActualizar.addActionListener(e -> { // Agrega listener que se ejecuta al hacer clic
            leerValoresUI(); // Llama método para leer valores de campos y tabla
            calcularValores(); // Llama método para calcular valores
            actualizarUI(); // Llama método para actualizar etiquetas
            calcularTablaProduccion(); // Llama método para recalcular tabla
            JOptionPane.showMessageDialog(this, "✓ Cálculos actualizados correctamente", "Actualización", JOptionPane.INFORMATION_MESSAGE); // Muestra mensaje de confirmación
        }); // Fin del listener

        JButton btnOptimizar = crearBoton("🚀 Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 300, 40); // Crea botón azul con emoji y tamaño 300px por 40px
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Agrega listener que llama al método de optimización al hacer clic

        botones.add(btnGenerar); // Agrega botón generar aleatorios al panel de botones
        botones.add(btnActualizar); // Agrega botón actualizar al panel de botones
        botones.add(btnOptimizar); // Agrega botón optimizar al panel de botones

        JPanel progreso = new JPanel(new BorderLayout(8, 8)); // Crea panel con BorderLayout y gaps de 8px para barra de progreso
        progreso.setBackground(Color.WHITE); // Establece fondo blanco

        lblProgreso = new JLabel("Listo para comenzar optimización", SwingConstants.CENTER); // Crea y guarda etiqueta centrada con mensaje inicial
        lblProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente normal tamaño 12

        progressBar = new JProgressBar(0, NUM_SIMULACIONES); // Crea y guarda barra de progreso con rango de 0 a NUM_SIMULACIONES (563)
        progressBar.setStringPainted(true); // Activa visualización de porcentaje en la barra
        progressBar.setPreferredSize(new Dimension(700, 30)); // Establece tamaño preferido de 700px ancho por 30px alto
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11
        progressBar.setForeground(new Color(76, 175, 80)); // Establece color verde para la barra de progreso

        progreso.add(lblProgreso, BorderLayout.NORTH); // Agrega etiqueta de progreso en parte superior
        progreso.add(progressBar, BorderLayout.CENTER); // Agrega barra de progreso en centro

        panel.add(botones, BorderLayout.NORTH); // Agrega panel de botones en parte superior
        panel.add(progreso, BorderLayout.CENTER); // Agrega panel de progreso en centro

        return panel; // Retorna panel de control completo
    } // Fin del método crearPanelControl

    private JLabel crearLabelParametro(String texto) { // Método auxiliar que crea etiqueta de nombre de parámetro
        JLabel lbl = new JLabel(texto + ":"); // Crea etiqueta con texto y dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente normal tamaño 11
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Establece alineación horizontal a la derecha
        return lbl; // Retorna etiqueta creada
    } // Fin del método crearLabelParametro

    private JTextField crearTextField(String valor, Color bg) { // Método auxiliar que crea campo de texto con valor y color de fondo
        JTextField txt = new JTextField(valor); // Crea campo de texto con valor inicial especificado
        txt.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11
        txt.setHorizontalAlignment(SwingConstants.RIGHT); // Establece alineación horizontal del texto a la derecha
        txt.setBackground(bg); // Establece color de fondo especificado
        txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Crea borde compuesto: línea gris oscuro de 1px y margen interno de 2px vertical y 5px horizontal
        return txt; // Retorna campo de texto creado
    } // Fin del método crearTextField

    private JLabel crearLabelCalculado(String texto) { // Método auxiliar que crea etiqueta para valor calculado con fondo gris
        JLabel lbl = new JLabel(texto); // Crea etiqueta con texto especificado
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Establece alineación horizontal a la derecha
        lbl.setBackground(COLOR_CALCULADO); // Establece color de fondo gris claro
        lbl.setOpaque(true); // Hace la etiqueta opaca para que se vea el fondo
        lbl.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.GRAY, 1), BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Crea borde compuesto: línea gris de 1px y margen interno
        return lbl; // Retorna etiqueta creada
    } // Fin del método crearLabelCalculado

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar que crea botón personalizado con efectos hover
        JButton btn = new JButton(texto); // Crea botón con texto especificado
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita tamaño 13
        btn.setBackground(bg); // Establece color de fondo especificado
        btn.setForeground(Color.WHITE); // Establece color blanco para texto
        btn.setFocusPainted(false); // Desactiva borde de foco
        btn.setBorderPainted(false); // Desactiva borde pintado
        btn.setPreferredSize(new Dimension(ancho, alto)); // Establece tamaño preferido con ancho y alto especificados
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Establece cursor de mano al pasar sobre el botón

        btn.addMouseListener(new java.awt.event.MouseAdapter() { // Agrega listener de eventos del mouse
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Método que se ejecuta cuando mouse entra al botón
                btn.setBackground(bg.brighter()); // Aclara el color de fondo para efecto hover
            } // Fin del método mouseEntered

            public void mouseExited(java.awt.event.MouseEvent evt) { // Método que se ejecuta cuando mouse sale del botón
                btn.setBackground(bg); // Restaura color de fondo original
            } // Fin del método mouseExited
        }); // Fin del listener

        return btn; // Retorna botón creado
    } // Fin del método crearBoton

    private JLabel crearStatLabel(String texto) { // Método auxiliar que crea etiqueta de estadística
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER); // Crea etiqueta centrada con texto especificado
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente negrita tamaño 12
        lbl.setForeground(COLOR_HEADER); // Establece color azul para texto
        return lbl; // Retorna etiqueta creada
    } // Fin del método crearStatLabel

    private void leerValoresUI() { // Método que lee TODOS los valores de la interfaz incluyendo tabla de costos
        try { // Inicia bloque try para capturar excepciones
            timeToPlateau = Double.parseDouble(txtTimeToPlateau.getText().replace(",", ".")); // Lee texto del campo, reemplaza coma por punto y convierte a double
            tarifaMinima = Double.parseDouble(txtTarifaMinima.getText().replace(",", ".")); // Lee y convierte tarifa mínima
            margenPetroleo = Double.parseDouble(txtMargenPetroleo.getText().replace(",", ".")); // Lee y convierte margen petróleo
            plateauEndsAt = Double.parseDouble(txtPlateauEndsAt.getText().replace(",", ".")); // Lee y convierte plateau ends at

            for (int i = 0; i < modeloCostos.getRowCount(); i++) { // Itera sobre todas las filas de la tabla de costos
                String produccionStr = modeloCostos.getValueAt(i, 0).toString().replace(",", ""); // Obtiene valor de columna 0, convierte a string y elimina comas
                String costoStr = modeloCostos.getValueAt(i, 1).toString().replace(",", ""); // Obtiene valor de columna 1, convierte a string y elimina comas
                costosInstalaciones[i][0] = Double.parseDouble(produccionStr); // Convierte string a double y guarda en array posición [i][0]
                costosInstalaciones[i][1] = Double.parseDouble(costoStr); // Convierte string a double y guarda en array posición [i][1]
            } // Fin del bucle for
        } catch (Exception e) { // Captura cualquier excepción durante la lectura
            JOptionPane.showMessageDialog(this, "Error al leer valores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Muestra ventana de diálogo con mensaje de error
        } // Fin del bloque catch
    } // Fin del método leerValoresUI

    private void calcularValores() { // Método que calcula todos los valores derivados del modelo de producción petrolera
        reservas = stoiip * recuperacion / 100.0; // Calcula reservas recuperables multiplicando STOIIP por porcentaje de recuperación dividido entre 100
        maxPlateauRate = (plateauRateIs / 100.0) * reservas / 0.365; // Calcula tasa máxima de plateau dividiendo plateau rate entre 100, multiplicando por reservas y dividiendo entre 0.365
        plateauRate = Math.min(maxPlateauRate, Math.min(buenaTasa * pozosPerforar, tamañoInstalacion)); // Calcula tasa efectiva de plateau como el mínimo entre max plateau, capacidad de pozos y tamaño de instalación
        aumentarProduccion = 0.365 * plateauRate * 0.5 * timeToPlateau; // Calcula producción durante ramp-up multiplicando 0.365 por plateau rate por 0.5 por tiempo a plateau
        plateauProduction = Math.max(0, plateauEndsAt * (reservas / 100.0) - aumentarProduccion); // Calcula producción durante plateau como el máximo entre 0 y (plateau ends at por reservas entre 100 menos ramp-up)
        plateauEndsAtCalc = plateauProduction / (0.365 * plateauRate) + timeToPlateau; // Calcula cuando termina plateau dividiendo plateau production entre tasa anual y sumando tiempo a plateau
        factorDeclive = 0.365 * (plateauRate - tarifaMinima) / (reservas - plateauProduction - aumentarProduccion); // Calcula factor de declive multiplicando 0.365 por diferencia de tasas dividido entre reservas restantes

        if (tarifaMinima > 0) { // Si tarifa mínima es mayor que cero
            vidaProduccion = plateauEndsAtCalc - Math.log(tarifaMinima / plateauRate) / factorDeclive; // Calcula vida de producción restando logaritmo natural de ratio de tasas dividido entre factor declive
        } else { // Si tarifa mínima es cero o negativa
            vidaProduccion = 1e20; // Establece vida de producción como infinito (1 por 10 elevado a 20)
        } // Fin del if-else

        costosPozo = buenCosto * pozosPerforar; // Calcula costos totales de pozos multiplicando costo por pozo por número de pozos
        costosInstalacionesCalc = buscarCostoInstalacion(tamañoInstalacion); // Llama método para buscar costo de instalación según tamaño y guarda resultado
    } // Fin del método calcularValores

    private double buscarCostoInstalacion(double produccion) { // Método que busca costo de instalación en tabla según capacidad de producción
        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre cada fila del array de costos de instalaciones
            if (produccion <= costosInstalaciones[i][0]) { // Si producción es menor o igual a capacidad de este nivel
                return costosInstalaciones[i][1]; // Retorna costo correspondiente a este nivel
            } // Fin del if
        } // Fin del bucle for
        return costosInstalaciones[costosInstalaciones.length - 1][1]; // Si no encontró nivel, retorna costo del último nivel (máxima capacidad)
    } // Fin del método buscarCostoInstalacion

    private void calcularTablaProduccion() { // Método que calcula perfil completo de producción de 50 años y llena tabla
        modeloTabla.setRowCount(0); // Limpia todas las filas existentes de la tabla estableciendo contador en 0

        double[] tasaAnualizada = new double[AÑOS + 1]; // Crea array de doubles con tamaño AÑOS+1 para almacenar tasa anualizada (índices 1 a 50)
        double[] produccionAnual = new double[AÑOS + 1]; // Crea array para almacenar producción anual de cada año
        double[] petroleoAcumulado = new double[AÑOS + 1]; // Crea array para almacenar petróleo acumulado hasta cada año
        double[] petroleoDescuentoAcum = new double[AÑOS + 1]; // Crea array para almacenar petróleo descontado acumulado hasta cada año

        for (int año = 1; año <= AÑOS; año++) { // Itera desde año 1 hasta año 50 inclusive
            if (año < timeToPlateau + 1) { // Si año actual es menor que tiempo a plateau más 1 (fase de ramp-up)
                produccionAnual[año] = año * 0.365 * plateauRate / (timeToPlateau + 1); // Calcula producción lineal creciente multiplicando año por tasa anualizada dividido entre tiempo total
            } else { // Si año está después del ramp-up
                double maxMin1 = Math.min(plateauEndsAtCalc + 1 - año, 1); // Calcula mínimo entre tiempo restante de plateau y 1
                double part1 = 0.365 * plateauRate * Math.max(0, maxMin1); // Calcula parte 1: producción durante plateau multiplicando tasa por tiempo en plateau

                double minVidaAño1 = Math.min(vidaProduccion, año - 1); // Calcula mínimo entre vida total y año anterior
                double maxExp1 = Math.max(0, minVidaAño1 - plateauEndsAtCalc); // Calcula máximo entre 0 y tiempo en declive hasta año anterior
                double exp1 = Math.exp(-factorDeclive * maxExp1); // Calcula exponencial negativa del factor declive por tiempo

                double minVidaAño = Math.min(vidaProduccion, año); // Calcula mínimo entre vida total y año actual
                double maxExp2 = Math.max(minVidaAño - plateauEndsAtCalc, 0); // Calcula máximo entre tiempo en declive y 0
                double exp2 = Math.exp(-factorDeclive * maxExp2); // Calcula exponencial negativa para año actual

                double part2 = 0.365 * plateauRate * (exp1 - exp2) / factorDeclive; // Calcula parte 2: producción durante declive usando integral de exponencial

                produccionAnual[año] = part1 + part2; // Suma producción de plateau más producción de declive
            } // Fin del if-else

            tasaAnualizada[año] = produccionAnual[año] / 0.365; // Calcula tasa anualizada dividiendo producción anual entre 0.365

            if (año == 1) { // Si es el primer año
                petroleoAcumulado[año] = produccionAnual[año]; // Petróleo acumulado es igual a producción del año 1
            } else { // Si es año 2 o posterior
                petroleoAcumulado[año] = petroleoAcumulado[año - 1] + produccionAnual[año]; // Suma producción actual a acumulado del año anterior
            } // Fin del if-else

            if (año == 1) { // Si es el primer año
                petroleoDescuentoAcum[año] = produccionAnual[año]; // Petróleo descontado es igual a producción del año 1 sin descuento
            } else { // Si es año 2 o posterior
                double descuento = Math.pow(1.0 + 0.01 * factorDescuento, año - 1); // Calcula factor de descuento elevando 1 más tasa a la potencia año menos 1
                petroleoDescuentoAcum[año] = petroleoDescuentoAcum[año - 1] + (produccionAnual[año] / descuento); // Suma producción descontada a acumulado anterior
            } // Fin del if-else

            modeloTabla.addRow(new Object[]{ // Agrega nueva fila a la tabla con array de objetos
                    año, // Columna 0: número de año
                    FMT2.format(tasaAnualizada[año]), // Columna 1: tasa anualizada formateada con 2 decimales
                    FMT2.format(produccionAnual[año]), // Columna 2: producción anual formateada
                    FMT2.format(petroleoAcumulado[año]), // Columna 3: petróleo acumulado formateado
                    FMT2.format(petroleoDescuentoAcum[año]) // Columna 4: petróleo descontado acumulado formateado
            }); // Fin del addRow
        } // Fin del bucle for

        reservasDescontadas = petroleoDescuentoAcum[AÑOS]; // Guarda valor final de reservas descontadas del año 50
        npv = reservasDescontadas * margenPetroleo - costosPozo - costosInstalacionesCalc; // Calcula NPV multiplicando reservas por margen y restando costos
    } // Fin del método calcularTablaProduccion

    private void actualizarUI() { // Método que actualiza todas las etiquetas de la interfaz con valores calculados
        lblReservas.setText(FMT2.format(reservas) + " mmbbls"); // Actualiza texto de etiqueta reservas con valor formateado y unidad
        lblMaxPlateau.setText(FMT2.format(maxPlateauRate) + " mbd"); // Actualiza texto de etiqueta max plateau rate
        lblPlateauRate.setText(FMT2.format(plateauRate) + " mbd"); // Actualiza texto de etiqueta plateau rate
        lblAumentar.setText(FMT2.format(aumentarProduccion) + " mmbbls"); // Actualiza texto de etiqueta aumentar producción
        lblPlateauProd.setText(FMT2.format(plateauProduction) + " mmbbls"); // Actualiza texto de etiqueta plateau production
        lblPlateauEnds.setText(FMT2.format(plateauEndsAtCalc) + " años"); // Actualiza texto de etiqueta plateau ends at calculado
        lblFactorDeclive.setText(FMT2.format(factorDeclive)); // Actualiza texto de etiqueta factor de declive
        lblVidaProd.setText(FMT2.format(vidaProduccion) + " años"); // Actualiza texto de etiqueta vida de producción
        lblReservasDesc.setText(FMT2.format(reservasDescontadas) + " mmbbls"); // Actualiza texto de etiqueta reservas descontadas
        lblCostosPozo.setText(FMT2.format(costosPozo) + " $mm"); // Actualiza texto de etiqueta costos de pozo
        lblCostosInst.setText(FMT2.format(costosInstalacionesCalc) + " $mm"); // Actualiza texto de etiqueta costos de instalaciones
        lblNPV.setText(FMT2.format(npv) + " $mm"); // Actualiza texto de etiqueta NPV
    } // Fin del método actualizarUI

    private void ejecutarOptimizacion() { // Método que ejecuta proceso completo de optimización Monte Carlo con OptQuest
        todosNPV.clear(); // Limpia lista de todos los NPVs llamando al método clear
        mejorSimulacionNPVs.clear(); // Limpia lista de NPVs de mejor simulación
        mejorNPV = Double.NEGATIVE_INFINITY; // Reinicia mejor NPV a infinito negativo

        progressBar.setValue(0); // Reinicia barra de progreso estableciendo valor en 0
        lblProgreso.setText("⏳ Ejecutando optimización Monte Carlo..."); // Actualiza texto de etiqueta con mensaje de inicio

        new SwingWorker<Void, Integer>() { // Crea instancia anónima de SwingWorker para ejecutar en hilo de fondo
            protected Void doInBackground() { // Método que se ejecuta en hilo separado para no bloquear interfaz
                Random rand = new Random(12345); // Crea generador de números aleatorios con semilla fija 12345 para reproducibilidad

                for (int sim = 1; sim <= NUM_SIMULACIONES; sim++) { // Bucle principal que itera 563 simulaciones
                    int pozos = rand.nextInt(49) + 2; // Genera número aleatorio de pozos entre 2 y 50
                    int instIndex = rand.nextInt(7); // Genera índice aleatorio entre 0 y 6
                    double tamañoInst = 50 + 50 * instIndex; // Calcula tamaño de instalación: 50, 100, 150, 200, 250, 300 o 350 mbd
                    double plateauIs = 4.5 + rand.nextDouble() * (15.0 - 4.5); // Genera plateau rate aleatorio entre 4.5% y 15%

                    List<Double> npvsPrueba = new ArrayList<>(); // Crea lista vacía para almacenar 1000 NPVs de esta simulación

                    for (int mc = 0; mc < NUM_PRUEBAS_MC; mc++) { // Bucle interno que ejecuta 1000 pruebas Monte Carlo
                        LogNormalDistribution stoiipDist = new LogNormalDistribution(Math.log(1500.0), 300.0 / 1500.0); // Crea distribución LogNormal para STOIIP con media logarítmica y desviación estándar
                        NormalDistribution recupDist = new NormalDistribution(42.0, 1.2); // Crea distribución Normal para recuperación con media 42 y desviación 1.2
                        NormalDistribution tasaDist = new NormalDistribution(10.0, 3.0); // Crea distribución Normal para tasa con media 10 y desviación 3
                        LogNormalDistribution descDist = new LogNormalDistribution(Math.log(10.0), 1.2 / 10.0); // Crea distribución LogNormal para factor descuento
                        TriangularDistribution costoDist = new TriangularDistribution(9.0, 10.0, 12.0); // Crea distribución Triangular para costo con mínimo 9, moda 10 y máximo 12

                        double stoiipSample = stoiipDist.sample(); // Genera muestra aleatoria de STOIIP según distribución
                        double recupSample = recupDist.sample(); // Genera muestra aleatoria de recuperación
                        double tasaSample = tasaDist.sample(); // Genera muestra aleatoria de tasa
                        double descSample = descDist.sample(); // Genera muestra aleatoria de factor descuento
                        double costoSample = costoDist.sample(); // Genera muestra aleatoria de costo

                        double npvSample = calcularNPVSimulacion(stoiipSample, recupSample, tasaSample, pozos, descSample, costoSample, tamañoInst, plateauIs); // Llama método para calcular NPV con parámetros aleatorios

                        npvsPrueba.add(npvSample); // Agrega NPV calculado a lista de esta simulación
                        todosNPV.add(npvSample); // Agrega NPV a lista global de todos los NPVs

                        if (mc + 1 >= MIN_TRIALS_FOR_CHECK && (mc + 1) % CHECK_INTERVAL == 0) { // Si ejecutó al menos 500 pruebas Y número de pruebas es múltiplo de 500
                            List<Double> temp = new ArrayList<>(npvsPrueba); // Crea copia temporal de lista de NPVs
                            Collections.sort(temp); // Ordena lista temporal de menor a mayor
                            double currentP10 = temp.get((int) (temp.size() * 0.10)); // Calcula percentil 10 obteniendo elemento en posición 10% de la lista

                            if (currentP10 < mejorNPV - 50.0) { // Si percentil 10 actual es menor que mejor NPV menos 50
                                break; // Sale del bucle de pruebas (early stopping porque esta combinación no es prometedora)
                            } // Fin del if
                        } // Fin del if
                    } // Fin del bucle for de pruebas Monte Carlo

                    Collections.sort(npvsPrueba); // Ordena lista completa de NPVs de esta simulación
                    double percentil10 = npvsPrueba.get((int) (npvsPrueba.size() * 0.10)); // Calcula percentil 10 de esta simulación

                    if (percentil10 > mejorNPV) { // Si percentil 10 de esta simulación es mejor que el mejor encontrado hasta ahora
                        mejorNPV = percentil10; // Actualiza mejor NPV con percentil 10 actual
                        mejorPozos = pozos; // Actualiza mejor número de pozos
                        mejorTamañoInst = tamañoInst; // Actualiza mejor tamaño de instalación
                        mejorPlateauRateIs = plateauIs; // Actualiza mejor plateau rate
                        mejorSimulacionNPVs = new ArrayList<>(npvsPrueba); // Crea copia de lista de NPVs para guardar como mejor simulación
                    } // Fin del if

                    if (sim % 5 == 0) { // Si número de simulación es múltiplo de 5
                        publish(sim); // Publica actualización de progreso para actualizar interfaz
                    } // Fin del if
                } // Fin del bucle for de simulaciones

                return null; // Retorna null porque método debe retornar Void
            } // Fin del método doInBackground

            protected void process(List<Integer> chunks) { // Método que se ejecuta en hilo de interfaz para actualizar progreso
                int ultimo = chunks.get(chunks.size() - 1); // Obtiene último valor publicado de la lista
                progressBar.setValue(ultimo); // Actualiza valor de barra de progreso
                int porcentaje = (int) ((ultimo * 100.0) / NUM_SIMULACIONES); // Calcula porcentaje multiplicando por 100 y dividiendo entre total
                lblProgreso.setText(String.format("⏳ Progreso: %d / %d simulaciones (%d%%)", ultimo, NUM_SIMULACIONES, porcentaje)); // Actualiza texto con formato incluyendo números y porcentaje
            } // Fin del método process

            protected void done() { // Método que se ejecuta al terminar optimización en hilo de interfaz
                progressBar.setValue(NUM_SIMULACIONES); // Establece barra de progreso al máximo (563)
                lblProgreso.setText("✅ Optimización completada - " + NUM_SIMULACIONES + " simulaciones"); // Actualiza texto con mensaje de completado

                txtPozos.setText(String.valueOf(mejorPozos)); // Actualiza campo de texto de pozos con mejor valor convertido a string
                txtTamañoInstalacion.setText(FMT2.format(mejorTamañoInst)); // Actualiza campo de tamaño de instalación con mejor valor formateado
                txtPlateauRateIs.setText(FMT2.format(mejorPlateauRateIs)); // Actualiza campo de plateau rate con mejor valor formateado

                pozosPerforar = mejorPozos; // Actualiza variable de instancia pozosPerforar con mejor valor
                tamañoInstalacion = mejorTamañoInst; // Actualiza variable de instancia tamañoInstalacion
                plateauRateIs = mejorPlateauRateIs; // Actualiza variable de instancia plateauRateIs

                calcularValores(); // Llama método para recalcular todos los valores con mejores parámetros
                actualizarUI(); // Llama método para actualizar todas las etiquetas
                calcularTablaProduccion(); // Llama método para recalcular tabla de 50 años

                mostrarResultadosOptimizacion(); // Llama método para mostrar ventana emergente con resultados
            } // Fin del método done
        }.execute(); // Ejecuta el SwingWorker iniciando el hilo de fondo
    } // Fin del método ejecutarOptimizacion

    private double calcularNPVSimulacion(double stoiip, double recup, double buenaTasa, int pozos, double descuento, double costo, double tamañoInst, double plateauIs) { // Método que calcula NPV para una simulación Monte Carlo con parámetros específicos
        double res = stoiip * recup / 100.0; // Calcula reservas multiplicando STOIIP por recuperación dividido entre 100
        double maxPR = (plateauIs / 100.0) * res / 0.365; // Calcula max plateau rate
        double pr = Math.min(maxPR, Math.min(buenaTasa * pozos, tamañoInst)); // Calcula plateau rate efectivo como mínimo de tres valores
        double aum = 0.365 * pr * 0.5 * timeToPlateau; // Calcula producción durante ramp-up
        double pp = Math.max(0, plateauEndsAt * (res / 100.0) - aum); // Calcula producción durante plateau
        double pea = pp / (0.365 * pr) + timeToPlateau; // Calcula cuando termina plateau
        double fd = 0.365 * (pr - tarifaMinima) / (res - pp - aum); // Calcula factor de declive
        double vp = (tarifaMinima > 0) ? pea - Math.log(tarifaMinima / pr) / fd : 1e20; // Calcula vida de producción usando operador ternario

        double resDesc = 0; // Inicializa reservas descontadas en cero

        for (int año = 1; año <= AÑOS; año++) { // Itera sobre 50 años
            double prodAnual; // Declara variable para producción anual

            if (año < timeToPlateau + 1) { // Si está en fase de ramp-up
                prodAnual = año * 0.365 * pr / (timeToPlateau + 1); // Calcula producción lineal
            } else { // Si está después del ramp-up
                double term1 = 0.365 * pr * Math.max(0, Math.min(pea + 1 - año, 1)); // Calcula término de plateau
                double exp1 = Math.exp(-fd * Math.max(0, Math.min(vp, año - 1) - pea)); // Calcula exponencial para año anterior
                double exp2 = Math.exp(-fd * Math.max(Math.min(vp, año) - pea, 0)); // Calcula exponencial para año actual
                double term2 = 0.365 * pr * (exp1 - exp2) / fd; // Calcula término de declive
                prodAnual = term1 + term2; // Suma ambos términos
            } // Fin del if-else

            if (año == 1) { // Si es primer año
                resDesc = prodAnual; // Reservas descontadas igual a producción sin descuento
            } else { // Si es año 2 o posterior
                double desc = Math.pow(1.0 + 0.01 * descuento, año - 1); // Calcula factor de descuento
                resDesc += prodAnual / desc; // Suma producción descontada a acumulado
            } // Fin del if-else
        } // Fin del bucle for

        double costoPozos = costo * pozos; // Calcula costos totales de pozos
        double costoInst = buscarCostoInstalacion(tamañoInst); // Busca costo de instalación en tabla

        return resDesc * margenPetroleo - costoPozos - costoInst; // Retorna NPV restando costos de ingresos
    } // Fin del método calcularNPVSimulacion

    private void mostrarResultadosOptimizacion() { // Método que muestra ventana emergente con resultados de optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crea diálogo modal false (no modal) con título especificado
        dlg.setLayout(new BorderLayout(15, 15)); // Establece BorderLayout con gaps de 15px

        JPanel main = new JPanel(new BorderLayout(15, 15)); // Crea panel principal con BorderLayout
        main.setBackground(Color.WHITE); // Establece fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Crea margen de 20px en todos los lados

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5)); // Crea panel de encabezado con grid de 2 filas por 1 columna
        header.setBackground(Color.WHITE); // Establece fondo blanco

        JLabel lblSim = new JLabel("📊 " + NUM_SIMULACIONES + " simulaciones completadas", SwingConstants.CENTER); // Crea etiqueta con emoji y número de simulaciones centrada
        lblSim.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Establece fuente negrita tamaño 16
        lblSim.setForeground(COLOR_HEADER); // Establece color azul para texto

        JLabel lblVista = new JLabel("Vista de mejor solución encontrada", SwingConstants.CENTER); // Crea etiqueta de subtítulo centrada
        lblVista.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Establece fuente normal tamaño 13
        lblVista.setForeground(Color.GRAY); // Establece color gris para texto

        header.add(lblSim); // Agrega etiqueta de simulaciones al encabezado
        header.add(lblVista); // Agrega etiqueta de vista al encabezado

        main.add(header, BorderLayout.NORTH); // Agrega panel de encabezado en parte superior

        JPanel centro = new JPanel(new GridLayout(3, 1, 15, 15)); // Crea panel central con grid de 3 filas por 1 columna con gaps de 15px
        centro.setBackground(Color.WHITE); // Establece fondo blanco

        JPanel npvPanel = new JPanel(new BorderLayout()); // Crea panel para mostrar NPV con BorderLayout
        npvPanel.setBackground(new Color(232, 245, 233)); // Establece fondo verde muy claro
        npvPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(76, 175, 80), 2), BorderFactory.createEmptyBorder(15, 15, 15, 15))); // Crea borde compuesto: línea verde de 2px y margen de 15px

        JLabel lblNPVTitle = new JLabel("🎯 NPV Percentil 10% (Optimizado)", SwingConstants.CENTER); // Crea etiqueta de título NPV con emoji centrada
        lblNPVTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        lblNPVTitle.setForeground(new Color(27, 94, 32)); // Establece color verde oscuro para texto

        JLabel lblNPVValue = new JLabel("$ " + FMT2.format(mejorNPV) + " mm", SwingConstants.CENTER); // Crea etiqueta con valor NPV formateado centrada
        lblNPVValue.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Establece fuente negrita tamaño 28 grande
        lblNPVValue.setForeground(new Color(27, 94, 32)); // Establece color verde oscuro para texto

        npvPanel.add(lblNPVTitle, BorderLayout.NORTH); // Agrega título NPV en parte superior
        npvPanel.add(lblNPVValue, BorderLayout.CENTER); // Agrega valor NPV en centro

        centro.add(npvPanel); // Agrega panel NPV al centro
        centro.add(crearPanelVariablesOptimas()); // Llama método para crear panel de variables óptimas y lo agrega
        centro.add(crearPanelEstadisticas()); // Llama método para crear panel de estadísticas y lo agrega

        main.add(centro, BorderLayout.CENTER); // Agrega panel central completo al centro del panel principal

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Crea panel de botones con FlowLayout centrado
        botonesPanel.setBackground(Color.WHITE); // Establece fondo blanco

        JButton btnHistograma = crearBoton("📊 Ver Histograma NPV", new Color(33, 150, 243), 200, 35); // Crea botón azul para histograma
        btnHistograma.addActionListener(e -> mostrarDistribucionNPV()); // Agrega listener que llama método de histograma al hacer clic

        JButton btnCerrar = crearBoton("✓ Cerrar", new Color(76, 175, 80), 120, 35); // Crea botón verde para cerrar
        btnCerrar.addActionListener(e -> dlg.dispose()); // Agrega listener que cierra diálogo al hacer clic

        botonesPanel.add(btnHistograma); // Agrega botón histograma al panel
        botonesPanel.add(btnCerrar); // Agrega botón cerrar al panel

        main.add(botonesPanel, BorderLayout.SOUTH); // Agrega panel de botones en parte inferior

        dlg.add(main); // Agrega panel principal completo al diálogo
        dlg.setSize(800, 700); // Establece tamaño del diálogo en 800px ancho por 700px alto
        dlg.setLocationRelativeTo(this); // Centra diálogo respecto a ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    } // Fin del método mostrarResultadosOptimizacion

    private JPanel crearPanelVariablesOptimas() { // Método que crea panel con variables de decisión óptimas encontradas
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(COLOR_HEADER, 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea azul de 2px y margen de 10px

        JLabel titulo = new JLabel("Variables de Decisión Óptimas", SwingConstants.CENTER); // Crea etiqueta de título centrada
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita tamaño 13
        titulo.setForeground(COLOR_HEADER); // Establece color azul para texto
        panel.add(titulo, BorderLayout.NORTH); // Agrega título en parte superior

        String[][] datos = { // Crea matriz bidimensional de strings con datos de variables óptimas
                {"Pozos a perforar", String.valueOf(mejorPozos), "pozos"}, // Fila 0: nombre, valor convertido a string, unidad
                {"Tamaño de instalación", FMT2.format(mejorTamañoInst), "mbd"}, // Fila 1: tamaño formateado
                {"Plateau rate is", FMT2.format(mejorPlateauRateIs), "% reservas/año"} // Fila 2: plateau rate formateado
        }; // Fin de la matriz

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 8)); // Crea grid de 3 filas por 3 columnas con gaps
        grid.setBackground(Color.WHITE); // Establece fondo blanco
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Crea margen de 10px

        for (String[] row : datos) { // Itera sobre cada fila de la matriz de datos
            JLabel lblNombre = new JLabel(row[0]); // Crea etiqueta con nombre (elemento 0 de la fila)
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente normal tamaño 12

            JLabel lblValor = new JLabel(row[1]); // Crea etiqueta con valor (elemento 1 de la fila)
            lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita tamaño 13
            lblValor.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea texto a la derecha
            lblValor.setOpaque(true); // Hace etiqueta opaca para mostrar fondo
            lblValor.setBackground(COLOR_DECISION); // Establece fondo amarillo de decisión
            lblValor.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.DARK_GRAY), BorderFactory.createEmptyBorder(3, 8, 3, 8))); // Crea borde compuesto: línea gris oscuro y margen

            JLabel lblUnidad = new JLabel(row[2]); // Crea etiqueta con unidad (elemento 2 de la fila)
            lblUnidad.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Establece fuente normal tamaño 11
            lblUnidad.setForeground(Color.GRAY); // Establece color gris para texto

            grid.add(lblNombre); // Agrega etiqueta de nombre al grid
            grid.add(lblValor); // Agrega etiqueta de valor al grid
            grid.add(lblUnidad); // Agrega etiqueta de unidad al grid
        } // Fin del bucle for

        panel.add(grid, BorderLayout.CENTER); // Agrega grid al centro del panel

        return panel; // Retorna panel completo de variables óptimas
    } // Fin del método crearPanelVariablesOptimas

    private JPanel crearPanelEstadisticas() { // Método que crea panel con estadísticas de NPV de mejor simulación
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5px
        panel.setBackground(Color.WHITE); // Establece fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(255, 152, 0), 2), BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Crea borde compuesto: línea naranja de 2px y margen de 10px

        JLabel titulo = new JLabel("Estadísticas NPV (Mejor simulación)", SwingConstants.CENTER); // Crea etiqueta de título centrada
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente negrita tamaño 13
        titulo.setForeground(new Color(230, 81, 0)); // Establece color naranja oscuro para texto
        panel.add(titulo, BorderLayout.NORTH); // Agrega título en parte superior

        if (!mejorSimulacionNPVs.isEmpty()) { // Si la lista de NPVs de mejor simulación no está vacía
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de lista de NPVs
            Collections.sort(npvs); // Ordena lista de menor a mayor

            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media usando stream convertido a double stream y obteniendo promedio
            double min = npvs.get(0); // Obtiene valor mínimo (primer elemento de lista ordenada)
            double max = npvs.get(npvs.size() - 1); // Obtiene valor máximo (último elemento de lista ordenada)
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula percentil 10 obteniendo elemento en posición 10%
            double p50 = npvs.get((int) (npvs.size() * 0.50)); // Calcula percentil 50 (mediana) en posición 50%
            double p90 = npvs.get((int) (npvs.size() * 0.90)); // Calcula percentil 90 en posición 90%

            JPanel grid = new JPanel(new GridLayout(6, 2, 8, 6)); // Crea grid de 6 filas por 2 columnas con gaps de 8px horizontal y 6px vertical
            grid.setBackground(Color.WHITE); // Establece fondo blanco
            grid.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Crea margen de 10px vertical y 15px horizontal

            addStatRow(grid, "Media:", "$ " + FMT2.format(media) + " mm"); // Llama método para agregar fila de media al grid
            addStatRow(grid, "Percentil 10%:", "$ " + FMT2.format(p10) + " mm"); // Agrega fila de percentil 10
            addStatRow(grid, "Mediana (P50):", "$ " + FMT2.format(p50) + " mm"); // Agrega fila de mediana
            addStatRow(grid, "Percentil 90%:", "$ " + FMT2.format(p90) + " mm"); // Agrega fila de percentil 90
            addStatRow(grid, "Mínimo:", "$ " + FMT2.format(min) + " mm"); // Agrega fila de mínimo
            addStatRow(grid, "Máximo:", "$ " + FMT2.format(max) + " mm"); // Agrega fila de máximo

            panel.add(grid, BorderLayout.CENTER); // Agrega grid al centro del panel
        } // Fin del if

        return panel; // Retorna panel completo de estadísticas
    } // Fin del método crearPanelEstadisticas

    private void addStatRow(JPanel grid, String label, String value) { // Método auxiliar que agrega fila de estadística al grid
        JLabel lblLabel = new JLabel(label); // Crea etiqueta con nombre de estadística
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente normal tamaño 12
        lblLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea texto a la derecha

        JLabel lblValue = new JLabel(value); // Crea etiqueta con valor de estadística
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente negrita tamaño 12
        lblValue.setHorizontalAlignment(SwingConstants.LEFT); // Alinea texto a la izquierda

        grid.add(lblLabel); // Agrega etiqueta de nombre al grid
        grid.add(lblValue); // Agrega etiqueta de valor al grid
    } // Fin del método addStatRow

    private void mostrarDistribucionNPV() { // Método que muestra ventana con histograma de distribución de NPV
        JDialog dlg = new JDialog(this, "Previsión: NPV - Distribución", false); // Crea diálogo no modal con título
        dlg.setLayout(new BorderLayout(10, 10)); // Establece BorderLayout con gaps de 10px

        JPanel main = new JPanel(new BorderLayout(10, 10)); // Crea panel principal con BorderLayout
        main.setBackground(Color.WHITE); // Establece fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Crea margen de 15px

        JLabel header = new JLabel(NUM_PRUEBAS_MC + " pruebas - Vista de frecuencia", SwingConstants.CENTER); // Crea etiqueta de encabezado con número de pruebas
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Establece color azul para texto
        main.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        JPanel histograma = new JPanel() { // Crea panel anónimo personalizado para dibujar histograma
            @Override // Anotación que indica sobrescritura de método
            protected void paintComponent(Graphics g) { // Sobrescribe método paintComponent para dibujo personalizado
                super.paintComponent(g); // Llama al método de la clase padre
                if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos de NPVs
                    dibujarHistograma(g, getWidth(), getHeight()); // Llama método para dibujar histograma pasando contexto gráfico y dimensiones
                } // Fin del if
            } // Fin del método paintComponent
        }; // Fin de la creación del panel anónimo
        histograma.setBackground(Color.WHITE); // Establece fondo blanco para panel de histograma
        histograma.setPreferredSize(new Dimension(750, 450)); // Establece tamaño preferido de 750px ancho por 450px alto
        histograma.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Crea borde de línea gris claro

        main.add(histograma, BorderLayout.CENTER); // Agrega panel de histograma al centro

        if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos de NPVs
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de lista
            Collections.sort(npvs); // Ordena lista
            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula percentil 10

            JPanel stats = new JPanel(new GridLayout(1, 3, 20, 5)); // Crea panel de estadísticas con grid de 1 fila por 3 columnas
            stats.setBackground(Color.WHITE); // Establece fondo blanco
            stats.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0)); // Crea margen de 10px arriba y 5px abajo

            stats.add(crearStatLabel("10% = $ " + FMT2.format(p10) + " mm")); // Agrega etiqueta con percentil 10
            stats.add(crearStatLabel("Media = $ " + FMT2.format(media) + " mm")); // Agrega etiqueta con media
            stats.add(crearStatLabel(FMT0.format(npvs.size()) + " muestras")); // Agrega etiqueta con número de muestras

            main.add(stats, BorderLayout.SOUTH); // Agrega panel de estadísticas en parte inferior
        } // Fin del if

        dlg.add(main); // Agrega panel principal al diálogo
        dlg.setSize(800, 600); // Establece tamaño del diálogo en 800px por 600px
        dlg.setLocationRelativeTo(this); // Centra diálogo respecto a ventana principal
        dlg.setVisible(true); // Hace visible el diálogo
    } // Fin del método mostrarDistribucionNPV

    private void dibujarHistograma(Graphics g, int width, int height) { // Método que dibuja histograma personalizado usando Graphics2D
        if (mejorSimulacionNPVs == null || mejorSimulacionNPVs.isEmpty()) { // Si no hay datos de NPVs
            return; // Sale del método sin dibujar nada
        } // Fin del if

        Graphics2D g2 = (Graphics2D) g; // Convierte Graphics a Graphics2D para capacidades avanzadas
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Activa antialiasing para líneas suaves

        int margin = 60; // Define margen de 60 píxeles alrededor del gráfico
        int chartWidth = width - 2 * margin; // Calcula ancho del gráfico restando márgenes
        int chartHeight = height - 2 * margin; // Calcula alto del gráfico restando márgenes

        if (chartWidth <= 0 || chartHeight <= 0) { // Si dimensiones son inválidas
            return; // Sale del método
        } // Fin del if

        List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Crea copia de lista de NPVs
        Collections.sort(npvs); // Ordena lista de menor a mayor

        double minVal = npvs.get(0); // Obtiene valor mínimo
        double maxVal = npvs.get(npvs.size() - 1); // Obtiene valor máximo
        double range = maxVal - minVal; // Calcula rango restando mínimo de máximo

        if (range <= 0) { // Si rango es cero (todos valores iguales)
            g2.setColor(new Color(100, 181, 246)); // Establece color azul
            int barX = margin + chartWidth / 2 - 10; // Calcula posición X centrada
            int barWidth = 20; // Define ancho de barra de 20px
            int barHeight = chartHeight; // Define alto completo
            g2.fillRect(barX, margin, barWidth, barHeight); // Dibuja rectángulo relleno
            dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, npvs.size()); // Llama método para dibujar ejes
            return; // Sale del método
        } // Fin del if

        double binWidth = range / NUM_BINS_HISTOGRAMA; // Calcula ancho de cada bin dividiendo rango entre número de bins
        int[] bins = new int[NUM_BINS_HISTOGRAMA]; // Crea array de enteros para contar frecuencias

        for (double val : npvs) { // Itera sobre cada valor NPV
            int binIndex = (int) ((val - minVal) / binWidth); // Calcula índice de bin dividiendo diferencia entre ancho de bin
            if (binIndex >= NUM_BINS_HISTOGRAMA) { // Si índice excede máximo
                binIndex = NUM_BINS_HISTOGRAMA - 1; // Ajusta al último bin
            } // Fin del if
            if (binIndex < 0) { // Si índice es negativo
                binIndex = 0; // Ajusta al primer bin
            } // Fin del if
            bins[binIndex]++; // Incrementa contador del bin
        } // Fin del bucle for

        int maxBin = 0; // Inicializa máximo bin en 0
        for (int bin : bins) { // Itera sobre cada bin
            if (bin > maxBin) { // Si frecuencia de este bin es mayor que máximo
                maxBin = bin; // Actualiza máximo
            } // Fin del if
        } // Fin del bucle for
        if (maxBin == 0) { // Si máximo es cero
            maxBin = 1; // Establece en 1 para evitar división por cero
        } // Fin del if

        double barWidthPixels = (double) chartWidth / NUM_BINS_HISTOGRAMA; // Calcula ancho de barra en píxeles
        int minBarWidth = Math.max(1, (int) Math.floor(barWidthPixels) - 1); // Calcula ancho mínimo dejando gap de 1px

        g2.setColor(new Color(100, 181, 246)); // Establece color azul para barras

        for (int i = 0; i < NUM_BINS_HISTOGRAMA; i++) { // Itera sobre cada bin
            if (bins[i] > 0) { // Si bin tiene frecuencia mayor que cero
                int barHeight = (int) Math.round(((double) bins[i] / maxBin) * chartHeight); // Calcula altura proporcional de barra
                int x = margin + (int) Math.round(i * barWidthPixels); // Calcula posición X de barra
                int y = height - margin - barHeight; // Calcula posición Y desde abajo
                if (barHeight < 1) { // Si altura es menor que 1
                    barHeight = 1; // Establece altura mínima de 1px
                } // Fin del if
                g2.fillRect(x, y, minBarWidth, barHeight); // Dibuja rectángulo relleno de barra
            } // Fin del if
        } // Fin del bucle for

        dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, maxBin); // Llama método para dibujar ejes y etiquetas
        dibujarLineasPercentiles(g2, width, height, margin, chartWidth, npvs, minVal, maxVal); // Llama método para dibujar líneas de percentiles
    } // Fin del método dibujarHistograma

    private void dibujarEjesYEtiquetas(Graphics2D g2, int width, int height, int margin, int chartWidth, int chartHeight, double minVal, double maxVal, int maxBin) { // Método que dibuja ejes X e Y con etiquetas
        g2.setColor(Color.BLACK); // Establece color negro para ejes
        g2.setStroke(new BasicStroke(2)); // Establece grosor de línea de 2 píxeles
        g2.drawLine(margin, height - margin, width - margin, height - margin); // Dibuja línea horizontal para eje X
        g2.drawLine(margin, margin, margin, height - margin); // Dibuja línea vertical para eje Y

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Establece fuente normal tamaño 10 para etiquetas

        for (int i = 0; i <= 5; i++) { // Itera 6 veces para crear 6 etiquetas en eje X
            double val = minVal + (maxVal - minVal) * i / 5.0; // Calcula valor equidistante entre mínimo y máximo
            int x = margin + (int) Math.round(chartWidth * i / 5.0); // Calcula posición X
            String label = FMT0.format(val); // Formatea valor como entero
            FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
            int labelWidth = fm.stringWidth(label); // Calcula ancho de texto
            g2.drawString(label, x - labelWidth / 2, height - margin + 20); // Dibuja texto centrado debajo del eje
            g2.drawLine(x, height - margin, x, height - margin + 5); // Dibuja tick (marca pequeña)
        } // Fin del bucle for

        for (int i = 0; i <= 5; i++) { // Itera 6 veces para crear 6 etiquetas en eje Y
            int val = (int) Math.round(maxBin * i / 5.0); // Calcula valor de frecuencia
            int y = height - margin - (int) Math.round(chartHeight * i / 5.0); // Calcula posición Y
            String label = String.valueOf(val); // Convierte valor a string
            FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
            int labelWidth = fm.stringWidth(label); // Calcula ancho de texto
            g2.drawString(label, margin - labelWidth - 10, y + 5); // Dibuja texto a la izquierda del eje
            g2.drawLine(margin - 5, y, margin, y); // Dibuja tick
        } // Fin del bucle for

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11 para etiquetas de ejes

        String xLabel = "NPV ($mm)"; // Define etiqueta del eje X
        FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
        int xLabelWidth = fm.stringWidth(xLabel); // Calcula ancho de texto
        g2.drawString(xLabel, width / 2 - xLabelWidth / 2, height - 10); // Dibuja etiqueta centrada debajo del gráfico

        g2.rotate(-Math.PI / 2); // Rota contexto gráfico -90 grados para texto vertical
        String yLabel = "Frecuencia"; // Define etiqueta del eje Y
        int yLabelWidth = fm.stringWidth(yLabel); // Calcula ancho de texto
        g2.drawString(yLabel, -height / 2 - yLabelWidth / 2, 15); // Dibuja etiqueta vertical a la izquierda
        g2.rotate(Math.PI / 2); // Restaura rotación del contexto gráfico
    } // Fin del método dibujarEjesYEtiquetas

    private void dibujarLineasPercentiles(Graphics2D g2, int width, int height, int margin, int chartWidth, List<Double> npvs, double minVal, double maxVal) { // Método que dibuja líneas verticales para percentil 10 y media
        double range = maxVal - minVal; // Calcula rango
        if (range <= 0) { // Si rango es cero
            return; // Sale del método
        } // Fin del if

        double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula percentil 10
        int xP10 = margin + (int) Math.round((p10 - minVal) / range * chartWidth); // Calcula posición X de percentil 10

        g2.setColor(new Color(244, 67, 54)); // Establece color rojo para línea de percentil 10
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Establece estilo de línea punteada
        g2.drawLine(xP10, margin, xP10, height - margin); // Dibuja línea vertical para percentil 10

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Establece fuente negrita tamaño 11
        g2.drawString("P10", xP10 - 15, margin - 10); // Dibuja etiqueta "P10" arriba de la línea

        double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media de NPVs
        int xMedia = margin + (int) Math.round((media - minVal) / range * chartWidth); // Calcula posición X de media

        g2.setColor(new Color(76, 175, 80)); // Establece color verde para línea de media
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Establece estilo de línea punteada
        g2.drawLine(xMedia, margin, xMedia, height - margin); // Dibuja línea vertical para media
        g2.drawString("Media", xMedia - 20, margin - 10); // Dibuja etiqueta "Media" arriba de la línea
    } // Fin del método dibujarLineasPercentiles

    public static void main(String[] args) { // Método main estático que es punto de entrada del programa
        try { // Inicia bloque try
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Establece look and feel del sistema operativo
        } catch (Exception e) { // Captura cualquier excepción
            e.printStackTrace(); // Imprime stack trace del error
        } // Fin del bloque catch

        SwingUtilities.invokeLater(() -> { // Ejecuta código en hilo de eventos de Swing (EDT)
            OilReservesSimulatorAleatorio sim = new OilReservesSimulatorAleatorio(); // Crea nueva instancia del simulador
            sim.setVisible(true); // Hace visible la ventana principal
        }); // Fin de la expresión lambda
    } // Fin del método main
} // Fin de la clase OilReservesSimulatorAleatorio