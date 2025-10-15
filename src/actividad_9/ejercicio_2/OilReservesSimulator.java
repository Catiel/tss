package actividad_9.ejercicio_2; // Declaración del paquete donde reside esta clase

import javax.swing.*; // Importa todos los componentes de interfaz gráfica Swing (JFrame, JButton, JLabel, JTable, etc.)
import javax.swing.table.*; // Importa componentes especializados para trabajar con tablas Swing (DefaultTableModel, TableCellRenderer, etc.)
import java.awt.*; // Importa componentes gráficos de AWT (Color, Font, Graphics, BorderLayout, etc.)
import java.text.DecimalFormat; // Importa clase para formatear números con patrones específicos (separador de miles, decimales)
import java.util.*; // Importa utilidades generales de Java (Random, Collections, Arrays, List, ArrayList)
import java.util.List; // Importa específicamente la interfaz List para trabajar con listas

import org.apache.commons.math3.distribution.*; // Importa todas las distribuciones estadísticas de Apache Commons Math3 (Normal, LogNormal, Triangular, Poisson)

public class OilReservesSimulator extends JFrame { // Declara clase pública que extiende JFrame para crear ventana principal del simulador de reservas petroleras
    private static final int NUM_SIMULACIONES = 563; // Define número total de simulaciones en optimización: 563 combinaciones de parámetros
    private static final int NUM_PRUEBAS_MC = 1000; // Define número de pruebas Monte Carlo por cada simulación: 1000 repeticiones
    private static final int ANOS = 50; // Define período de análisis: 50 años de producción petrolera
    private static final int MIN_TRIALS_FOR_CHECK = 500; // Define número mínimo de pruebas antes de verificar early stopping: 500 pruebas
    private static final int CHECK_INTERVAL = 500; // Define intervalo para verificar condición de early stopping: cada 500 pruebas
    private static final int NUM_BINS_HISTOGRAMA = 50; // Define número de barras (bins) en el histograma: 50 intervalos

    private static final DecimalFormat FMT2 = new DecimalFormat("#,##0.00"); // Define formato para números con 2 decimales y separador de miles (ejemplo: 1,234.56)
    private static final DecimalFormat FMT0 = new DecimalFormat("#,##0"); // Define formato para números enteros con separador de miles (ejemplo: 1,234)

    private static final Color COLOR_HEADER = new Color(79, 129, 189); // Define color azul corporativo para encabezados usando valores RGB
    private static final Color COLOR_SUPOSICION = new Color(146, 208, 80); // Define color verde para variables de suposición (assumption) usando RGB
    private static final Color COLOR_DECISION = new Color(255, 255, 0); // Define color amarillo para variables de decisión usando RGB
    private static final Color COLOR_CALCULADO = new Color(217, 217, 217); // Define color gris claro para valores calculados usando RGB
    private static final Color COLOR_NPV = new Color(0, 255, 255); // Define color cian para NPV (Net Present Value - resultado principal) usando RGB
    private static final Color COLOR_PANEL_BG = new Color(248, 248, 248); // Define color gris muy claro para fondo de paneles usando RGB

    private double stoiip = 1500.0; // STOIIP (Stock Tank Oil Initially In Place): petróleo inicialmente en lugar - valor inicial 1500 millones de barriles (mmbbls)
    private double recuperacion = 42.0; // Porcentaje de recuperación esperado del petróleo: 42% del STOIIP será extraído
    private double buenaTasa = 10.0; // Tasa de producción por pozo (good rate): 10 miles de barriles por día (mbd) por pozo
    private int pozosPerforar = 25; // Número de pozos a perforar (variable de decisión): 25 pozos inicialmente
    private double factorDescuento = 10.0; // Factor de descuento anual para cálculo de NPV: 10% tasa de descuento
    private double buenCosto = 10.0; // Costo por pozo (good cost): $10 millones por pozo
    private double tamanoInstalacion = 250.0; // Tamaño/capacidad de instalación de procesamiento: 250 miles de barriles por día (mbd)
    private double plateauRateIs = 10.0; // Tasa de plateau como porcentaje de reservas por año: 10% de las reservas por año

    private final double timeToPlateau = 2.0; // Tiempo para alcanzar plateau (meseta de producción constante): 2 años (CONSTANTE - no cambia)
    private final double tarifaMinima = 10.0; // Tarifa mínima de producción: 10 mbd (CONSTANTE - límite inferior de producción)
    private final double margenPetroleo = 2.0; // Margen de ganancia por barril de petróleo: $2 por barril (CONSTANTE)
    private final double plateauEndsAt = 65.0; // Porcentaje de reservas en que termina el plateau: 65% (CONSTANTE - después comienza declive)

    private final double[][] costosInstalaciones = { // Tabla de costos de instalaciones según capacidad: matriz con 7 filas [capacidad_mbd, costo_$millones]
            {50, 70},    // Instalación de 50 mbd cuesta $70 millones
            {100, 130},  // Instalación de 100 mbd cuesta $130 millones
            {150, 180},  // Instalación de 150 mbd cuesta $180 millones
            {200, 220},  // Instalación de 200 mbd cuesta $220 millones
            {250, 250},  // Instalación de 250 mbd cuesta $250 millones
            {300, 270},  // Instalación de 300 mbd cuesta $270 millones
            {350, 280}   // Instalación de 350 mbd cuesta $280 millones
    };

    private double reservas; // Reservas recuperables calculadas en millones de barriles (mmbbls) - se calcula como STOIIP × recuperación
    private double maxPlateauRate; // Tasa máxima de plateau calculada en mbd - límite teórico basado en reservas
    private double plateauRate; // Tasa de plateau efectiva en mbd - considerando todas las restricciones (pozos, instalación)
    private double aumentarProduccion; // Producción durante fase de ramp-up (aumento inicial) en mmbbls
    private double plateauProduction; // Producción total durante fase de plateau en mmbbls
    private double plateauEndsAtCalc; // Tiempo calculado en que termina el plateau en años
    private double factorDeclive; // Factor de declive exponencial después del plateau - controla velocidad de caída
    private double vidaProduccion; // Vida total de producción del campo petrolero en años
    private double reservasDescontadas; // Reservas descontadas (NPV de producción física) en mmbbls - valor presente de la producción
    private double costosPozo; // Costos totales de perforación de pozos en $millones
    private double costosInstalacionesCalc; // Costos calculados de instalaciones de procesamiento en $millones
    private double npv; // Net Present Value (Valor Presente Neto) final en $millones - resultado principal del análisis

    private JTextField txtStoiip, txtRecuperacion, txtBuenaTasa, txtPozos; // Campos de texto editables para STOIIP, recuperación, tasa por pozo y número de pozos
    private JTextField txtFactorDescuento, txtBuenCosto, txtTamanoInstalacion, txtPlateauRateIs; // Campos de texto para factor descuento, costo pozo, tamaño instalación y plateau rate
    private JLabel lblReservas, lblMaxPlateau, lblPlateauRate, lblAumentar; // Etiquetas para mostrar reservas calculadas, max plateau, plateau efectivo y producción ramp-up
    private JLabel lblPlateauProd, lblPlateauEnds, lblFactorDeclive, lblVidaProd; // Etiquetas para plateau production, tiempo fin plateau, factor declive y vida producción
    private JLabel lblReservasDesc, lblCostosPozo, lblCostosInst, lblNPV; // Etiquetas para reservas descontadas, costos pozos, costos instalaciones y NPV
    private DefaultTableModel modeloTabla; // Modelo de datos para la tabla de 50 años de producción
    private JProgressBar progressBar; // Barra de progreso visual para mostrar avance de optimización
    private JLabel lblProgreso; // Etiqueta de texto descriptivo del progreso actual
    private JTabbedPane tabbedPane; // Panel con pestañas para organizar dashboard y tabla completa

    private double mejorNPV = Double.NEGATIVE_INFINITY; // Mejor NPV (percentil 10) encontrado en optimización - inicializa en infinito negativo para maximizar
    private int mejorPozos = 25; // Mejor número de pozos encontrado durante optimización
    private double mejorTamanoInst = 250.0; // Mejor tamaño de instalación encontrado durante optimización
    private double mejorPlateauRateIs = 10.0; // Mejor plateau rate encontrado durante optimización
    private List<Double> todosNPV = new ArrayList<>(); // Lista con TODOS los NPVs de TODAS las simulaciones (563 × 1000 = 563,000 valores)
    private List<Double> mejorSimulacionNPVs = new ArrayList<>(); // Lista con los 1000 NPVs de la mejor simulación (para construir histograma)

    public OilReservesSimulator() { // Constructor de la clase - se ejecuta al crear nueva instancia
        super("Simulación de Reservas Petroleras - Crystal Ball"); // Llama al constructor de JFrame estableciendo título de ventana
        configurarUI(); // Llama a método que configura toda la interfaz de usuario
        calcularValores(); // Calcula todos los valores iniciales con parámetros por defecto
        actualizarUI(); // Actualiza todas las etiquetas de la interfaz con valores calculados
        calcularTablaProduccion(); // Calcula y llena tabla de producción de 50 años
        setSize(1600, 950); // Establece tamaño de ventana: 1600 píxeles de ancho × 950 de alto
        setLocationRelativeTo(null); // Centra ventana en pantalla (null indica centrar respecto a pantalla completa)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Define que al cerrar ventana se termine aplicación completamente
    }

    private void configurarUI() { // Método para configurar toda la estructura de la interfaz de usuario
        JPanel main = new JPanel(new BorderLayout(15, 15)); // Crea panel principal con BorderLayout y gaps de 15 píxeles entre componentes
        main.setBackground(COLOR_PANEL_BG); // Establece fondo gris muy claro para panel principal
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Crea margen vacío de 15 píxeles en todos los lados

        JLabel titulo = new JLabel("Oil Field Development - Simulación de Reservas Petroleras", SwingConstants.CENTER); // Crea título centrado
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Establece fuente Segoe UI negrita tamaño 22 puntos
        titulo.setForeground(new Color(31, 78, 120)); // Establece color azul oscuro para texto del título
        titulo.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Crea margen: 5px arriba, 0 lados, 10px abajo
        main.add(titulo, BorderLayout.NORTH); // Agrega título en parte superior del panel principal

        tabbedPane = new JTabbedPane(JTabbedPane.TOP); // Crea panel con pestañas ubicadas en parte superior
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente para títulos de pestañas

        JPanel dashboardPanel = crearPanelDashboard(); // Crea panel del dashboard principal llamando a método
        tabbedPane.addTab("📊 Dashboard Principal", dashboardPanel); // Agrega primera pestaña con icono de gráfico y panel dashboard

        JPanel tablaPanel = crearPanelTablaCompleta(); // Crea panel con tabla completa de 50 años
        tabbedPane.addTab("📈 Perfil de Producción (50 años)", tablaPanel); // Agrega segunda pestaña con icono de tendencia y tabla

        main.add(tabbedPane, BorderLayout.CENTER); // Agrega panel de pestañas al centro del panel principal
        main.add(crearPanelControl(), BorderLayout.SOUTH); // Agrega panel de controles (botones y progreso) en parte inferior

        add(main); // Agrega panel principal completo a la ventana JFrame
    }

    private JPanel crearPanelDashboard() { // Método para crear panel principal del dashboard
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crea panel con BorderLayout y gaps de 10 píxeles
        panel.setBackground(COLOR_PANEL_BG); // Establece fondo gris claro

        JPanel topPanel = new JPanel(new GridLayout(1, 3, 15, 0)); // Crea panel superior con GridLayout: 1 fila × 3 columnas, gap horizontal 15px
        topPanel.setBackground(COLOR_PANEL_BG); // Establece fondo gris claro
        topPanel.add(crearPanelEntrada()); // Agrega panel de variables de entrada (columna izquierda)
        topPanel.add(crearPanelCalculado()); // Agrega panel de valores calculados (columna centro)
        topPanel.add(crearPanelResultadosFinales()); // Agrega panel de resultados finales (columna derecha)

        panel.add(topPanel, BorderLayout.NORTH); // Agrega los 3 paneles en parte superior del dashboard
        panel.add(crearPanelResumenTabla(), BorderLayout.CENTER); // Agrega panel con resumen de tabla en centro

        return panel; // Retorna panel completo del dashboard
    }

    private JPanel crearPanelEntrada() { // Método para crear panel de variables de entrada
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Crea panel con BorderLayout y gaps de 5 píxeles
        panel.setBackground(Color.WHITE); // Establece fondo blanco para panel
        panel.setBorder(BorderFactory.createCompoundBorder( // Crea borde compuesto (dos bordes juntos)
                BorderFactory.createLineBorder(COLOR_HEADER, 2), // Borde externo: línea azul de 2 píxeles
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Borde interno: margen vacío de 10 píxeles

        JLabel header = new JLabel("Variables de Entrada", SwingConstants.CENTER); // Crea encabezado centrado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Color azul para texto
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Margen inferior de 10px
        panel.add(header, BorderLayout.NORTH); // Agrega encabezado en parte superior

        JPanel grid = new JPanel(new GridBagLayout()); // Crea panel con GridBagLayout para control fino de posicionamiento
        grid.setBackground(Color.WHITE); // Fondo blanco
        GridBagConstraints gbc = new GridBagConstraints(); // Crea objeto de restricciones para posicionamiento
        gbc.fill = GridBagConstraints.HORIZONTAL; // Los componentes se expanden horizontalmente
        gbc.insets = new Insets(3, 5, 3, 5); // Margen: 3px arriba/abajo, 5px izquierda/derecha

        int row = 0; // Inicializa contador de filas en 0
        addGridRow(grid, gbc, row++, "STOIIP", txtStoiip = crearTextField("1500.00", COLOR_SUPOSICION), "mmbbls", COLOR_SUPOSICION); // Fila 0: STOIIP con fondo verde suposición
        addGridRow(grid, gbc, row++, "Recuperación", txtRecuperacion = crearTextField("42.0", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Fila 1: Recuperación verde
        addGridRow(grid, gbc, row++, "Time to plateau", crearLabelFijo("2.00"), "years", Color.WHITE); // Fila 2: Time to plateau blanco fijo (no editable)
        addGridRow(grid, gbc, row++, "Buena tasa", txtBuenaTasa = crearTextField("10.00", COLOR_SUPOSICION), "mbd", COLOR_SUPOSICION); // Fila 3: Buena tasa verde
        addGridRow(grid, gbc, row++, "Pozos a perforar", txtPozos = crearTextField("25", COLOR_DECISION), "", COLOR_DECISION); // Fila 4: Pozos amarillo decisión
        addGridRow(grid, gbc, row++, "Tarifa mínima", crearLabelFijo("10.00"), "mbd", Color.WHITE); // Fila 5: Tarifa mínima blanco fijo
        addGridRow(grid, gbc, row++, "Factor de descuento", txtFactorDescuento = crearTextField("10.00", COLOR_SUPOSICION), "%", COLOR_SUPOSICION); // Fila 6: Factor descuento verde
        addGridRow(grid, gbc, row++, "Buen costo", txtBuenCosto = crearTextField("10.00", COLOR_SUPOSICION), "$mm", COLOR_SUPOSICION); // Fila 7: Buen costo verde
        addGridRow(grid, gbc, row++, "Tamaño instalación", txtTamanoInstalacion = crearTextField("250.00", COLOR_DECISION), "mbd", COLOR_DECISION); // Fila 8: Tamaño instalación amarillo
        addGridRow(grid, gbc, row++, "Margen petróleo", crearLabelFijo("2.00"), "$/bbl", Color.WHITE); // Fila 9: Margen petróleo blanco fijo
        addGridRow(grid, gbc, row++, "Plateau ends at", crearLabelFijo("65.0"), "% reservas", Color.WHITE); // Fila 10: Plateau ends blanco fijo
        addGridRow(grid, gbc, row++, "Plateau rate is", txtPlateauRateIs = crearTextField("10.0", COLOR_DECISION), "% res./año", COLOR_DECISION); // Fila 11: Plateau rate amarillo

        panel.add(grid, BorderLayout.CENTER); // Agrega grid con todas las filas al centro del panel
        panel.add(crearLeyenda(), BorderLayout.SOUTH); // Agrega leyenda de colores en parte inferior

        return panel; // Retorna panel completo de entrada
    }

    private void addGridRow(JPanel grid, GridBagConstraints gbc, int row, String label, JComponent campo, String unidad, Color bgColor) { // Método auxiliar para agregar una fila al grid - recibe grid, restricciones, número fila, etiqueta, campo (TextField o Label), unidad y color fondo
        gbc.gridy = row; // Establece número de fila actual
        gbc.gridx = 0; // Columna 0: etiqueta del parámetro
        gbc.weightx = 0.4; // Asigna 40% del ancho disponible a esta columna
        JLabel lbl = new JLabel(label + ":"); // Crea etiqueta con nombre del parámetro seguido de dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente normal tamaño 11
        grid.add(lbl, gbc); // Agrega etiqueta al grid en posición actual

        gbc.gridx = 1; // Columna 1: campo de valor (TextField o Label)
        gbc.weightx = 0.4; // Asigna 40% del ancho disponible a esta columna
        grid.add(campo, gbc); // Agrega campo al grid en posición actual

        gbc.gridx = 2; // Columna 2: unidad de medida
        gbc.weightx = 0.2; // Asigna 20% del ancho disponible a esta columna
        JLabel lblUnit = new JLabel(unidad); // Crea etiqueta con unidad de medida
        lblUnit.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente pequeña tamaño 10
        lblUnit.setForeground(Color.GRAY); // Color gris para unidad
        grid.add(lblUnit, gbc); // Agrega unidad al grid en posición actual
    }

    private JPanel crearLeyenda() { // Método para crear panel con leyenda de colores
        JPanel leyenda = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5)); // FlowLayout alineado a izquierda, gap horizontal 10px, vertical 5px
        leyenda.setBackground(Color.WHITE); // Fondo blanco
        leyenda.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0)); // Margen superior 5px

        leyenda.add(crearLeyendaItem("Suposición", COLOR_SUPOSICION)); // Agrega item verde "Suposición"
        leyenda.add(crearLeyendaItem("Decisión", COLOR_DECISION)); // Agrega item amarillo "Decisión"
        leyenda.add(crearLeyendaItem("Fijo", Color.WHITE)); // Agrega item blanco "Fijo"

        return leyenda; // Retorna panel de leyenda completo
    }

    private JPanel crearLeyendaItem(String texto, Color color) { // Método para crear un item individual de leyenda - recibe texto y color
        JPanel item = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0)); // FlowLayout alineado a izquierda con gap pequeño
        item.setBackground(Color.WHITE); // Fondo blanco

        JLabel colorBox = new JLabel("  "); // Crea etiqueta con dos espacios (será el cuadrito de color)
        colorBox.setOpaque(true); // Hace la etiqueta opaca para que muestre el color de fondo
        colorBox.setBackground(color); // Establece color de fondo especificado
        colorBox.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agrega borde negro de 1 píxel

        JLabel label = new JLabel(texto); // Crea etiqueta con texto descriptivo
        label.setFont(new Font("Segoe UI", Font.PLAIN, 9)); // Fuente muy pequeña tamaño 9

        item.add(colorBox); // Agrega cuadro de color al item
        item.add(label); // Agrega texto al item

        return item; // Retorna item completo
    }

    private JPanel crearPanelCalculado() { // Método para crear panel de valores calculados
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout con gaps de 5px
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(COLOR_HEADER, 2), // Línea azul de 2px
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Margen de 10px

        JLabel header = new JLabel("Valores Calculados", SwingConstants.CENTER); // Encabezado centrado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita
        header.setForeground(COLOR_HEADER); // Color azul
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Margen inferior
        panel.add(header, BorderLayout.NORTH); // Encabezado arriba

        JPanel grid = new JPanel(new GridLayout(8, 2, 5, 8)); // GridLayout 8 filas × 2 columnas, gaps 5px horizontal, 8px vertical
        grid.setBackground(Color.WHITE); // Fondo blanco

        grid.add(crearLabelParametro("Reservas")); // Agrega etiqueta "Reservas:"
        lblReservas = crearLabelCalculado("630.00 mmbbls"); // Crea y guarda referencia a etiqueta de valor calculado (fondo gris)
        grid.add(lblReservas); // Agrega valor de reservas

        grid.add(crearLabelParametro("Max plateau rate")); // Agrega etiqueta "Max plateau rate:"
        lblMaxPlateau = crearLabelCalculado("172.60 mbd"); // Crea y guarda referencia
        grid.add(lblMaxPlateau); // Agrega valor

        grid.add(crearLabelParametro("Plateau rate")); // Agrega etiqueta "Plateau rate:"
        lblPlateauRate = crearLabelCalculado("172.60 mbd"); // Crea y guarda referencia
        grid.add(lblPlateauRate); // Agrega valor

        grid.add(crearLabelParametro("Aumentar producción")); // Agrega etiqueta "Aumentar producción:"
        lblAumentar = crearLabelCalculado("63.00 mmbbls"); // Crea y guarda referencia
        grid.add(lblAumentar); // Agrega valor

        grid.add(crearLabelParametro("Plateau production")); // Agrega etiqueta "Plateau production:"
        lblPlateauProd = crearLabelCalculado("346.50 mmbbls"); // Crea y guarda referencia
        grid.add(lblPlateauProd); // Agrega valor

        grid.add(crearLabelParametro("Plateau ends at")); // Agrega etiqueta "Plateau ends at:"
        lblPlateauEnds = crearLabelCalculado("7.50 años"); // Crea y guarda referencia
        grid.add(lblPlateauEnds); // Agrega valor

        grid.add(crearLabelParametro("Factor de declive")); // Agrega etiqueta "Factor de declive:"
        lblFactorDeclive = crearLabelCalculado("0.2692"); // Crea y guarda referencia
        grid.add(lblFactorDeclive); // Agrega valor

        grid.add(crearLabelParametro("Vida de producción")); // Agrega etiqueta "Vida de producción:"
        lblVidaProd = crearLabelCalculado("18.08 años"); // Crea y guarda referencia
        grid.add(lblVidaProd); // Agrega valor

        panel.add(grid, BorderLayout.CENTER); // Agrega grid al centro del panel

        return panel; // Retorna panel completo de valores calculados
    }

    private JPanel crearPanelResultadosFinales() { // Método para crear panel de resultados finales (NPV y costos)
        JPanel container = new JPanel(new BorderLayout(5, 10)); // Container con BorderLayout
        container.setBackground(COLOR_PANEL_BG); // Fondo gris claro

        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Panel interno
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(new Color(237, 125, 49), 2), // Línea naranja de 2px
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Margen de 10px

        JLabel header = new JLabel("Resultados Finales", SwingConstants.CENTER); // Encabezado centrado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita
        header.setForeground(new Color(237, 125, 49)); // Color naranja
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0)); // Margen inferior
        panel.add(header, BorderLayout.NORTH); // Encabezado arriba

        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 8)); // GridLayout 4 filas × 2 columnas
        grid.setBackground(Color.WHITE); // Fondo blanco

        grid.add(crearLabelParametro("Reservas descontadas")); // Agrega etiqueta
        lblReservasDesc = crearLabelCalculado("379.45 mmbbls"); // Crea y guarda referencia
        grid.add(lblReservasDesc); // Agrega valor

        grid.add(crearLabelParametro("Costos del pozo")); // Agrega etiqueta
        lblCostosPozo = crearLabelCalculado("250.00 $mm"); // Crea y guarda referencia
        grid.add(lblCostosPozo); // Agrega valor

        grid.add(crearLabelParametro("Costos instalaciones")); // Agrega etiqueta
        lblCostosInst = crearLabelCalculado("250.00 $mm"); // Crea y guarda referencia
        grid.add(lblCostosInst); // Agrega valor

        grid.add(crearLabelParametro("NPV")); // Agrega etiqueta NPV (resultado principal)
        lblNPV = crearLabelCalculado("258.89 $mm"); // Crea y guarda referencia
        lblNPV.setBackground(COLOR_NPV); // Establece fondo cian para destacar NPV
        lblNPV.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente más grande y negrita
        grid.add(lblNPV); // Agrega valor NPV

        panel.add(grid, BorderLayout.CENTER); // Agrega grid al centro

        JLabel objetivo = new JLabel("🎯 Objetivo: Maximizar Percentil 10 de NPV", SwingConstants.CENTER); // Etiqueta con objetivo de optimización
        objetivo.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Fuente negrita
        objetivo.setForeground(Color.RED); // Color rojo para destacar
        objetivo.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0)); // Margen superior
        panel.add(objetivo, BorderLayout.SOUTH); // Agrega objetivo abajo

        container.add(panel, BorderLayout.NORTH); // Agrega panel de resultados arriba del container
        container.add(crearPanelCostosInstalaciones(), BorderLayout.CENTER); // Agrega tabla de costos de instalaciones abajo

        return container; // Retorna container completo
    }

    private JPanel crearPanelCostosInstalaciones() { // Método para crear panel con tabla de costos de instalaciones
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(new Color(255, 192, 203), 2), // Línea rosa de 2px
                BorderFactory.createEmptyBorder(8, 8, 8, 8))); // Margen de 8px

        JLabel header = new JLabel("Costos de Instalaciones", SwingConstants.CENTER); // Encabezado centrado
        header.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Fuente negrita
        header.setForeground(new Color(192, 80, 77)); // Color rojo oscuro
        panel.add(header, BorderLayout.NORTH); // Encabezado arriba

        String[] cols = {"Producción (mbd)", "Costo ($mm)"}; // Define columnas de la tabla
        DefaultTableModel modelo = new DefaultTableModel(cols, 0) { // Crea modelo de tabla con 0 filas iniciales
            public boolean isCellEditable(int r, int c) { // Sobrescribe método para hacer celdas no editables
                return false; // Retorna false: ninguna celda es editable
            }
        };

        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre el array de costos (7 filas)
            modelo.addRow(new Object[]{ // Agrega fila a la tabla
                    FMT0.format(costosInstalaciones[i][0]), // Columna 0: producción formateada como entero
                    FMT0.format(costosInstalaciones[i][1])  // Columna 1: costo formateado como entero
            });
        }

        JTable tabla = new JTable(modelo); // Crea tabla visual con el modelo
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente pequeña tamaño 10
        tabla.setRowHeight(22); // Altura de filas 22px
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 10)); // Fuente del encabezado negrita
        tabla.getTableHeader().setBackground(new Color(255, 192, 203)); // Fondo rosa del encabezado
        tabla.getTableHeader().setForeground(Color.BLACK); // Texto negro del encabezado

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Crea renderizador personalizado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Alinea texto al centro
        for (int i = 0; i < tabla.getColumnCount(); i++) { // Para cada columna de la tabla
            tabla.getColumnModel().getColumn(i).setCellRenderer(centerRenderer); // Aplica renderizador centrado
        }

        JScrollPane scroll = new JScrollPane(tabla); // Crea scroll que contiene la tabla
        scroll.setPreferredSize(new Dimension(250, 180)); // Tamaño preferido 250×180px
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll al centro del panel

        return panel; // Retorna panel completo con tabla
    }

    private JPanel crearPanelResumenTabla() { // Método para crear panel con resumen de primeros años (mostrado en dashboard)
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout con gaps de 5px
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(COLOR_HEADER, 2), // Línea azul de 2px
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Margen de 10px

        JLabel header = new JLabel("Perfil de Producción - Primeros 15 Años (Ver pestaña para datos completos)", SwingConstants.CENTER); // Encabezado centrado con instrucción
        header.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita tamaño 13
        header.setForeground(COLOR_HEADER); // Color azul
        panel.add(header, BorderLayout.NORTH); // Encabezado arriba

        String[] cols = {"Año", "Tasa Anualizada\n(mbd)", "Producción Anual\n(mmb)", "Petróleo Acumulado\n(mmb)", "Petróleo Desc. Acum.\n(mmb)"}; // Columnas con \n para múltiples líneas

        DefaultTableModel modeloResumen = new DefaultTableModel(cols, 0) { // Crea modelo de tabla
            public boolean isCellEditable(int r, int c) {
                return false;
            } // Celdas no editables
        };

        JTable tablaResumen = new JTable(modeloResumen); // Crea tabla visual
        configurarEstiloTabla(tablaResumen); // Configura estilos (colores, fuentes)

        JScrollPane scroll = new JScrollPane(tablaResumen); // Crea scroll
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll al centro

        return panel; // Retorna panel completo (NOTA: esta tabla se llena en calcularTablaProduccion())
    }

    private JPanel crearPanelTablaCompleta() { // Método para crear panel con tabla completa de 50 años (segunda pestaña)
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen de 10px

        JLabel header = new JLabel("Perfil de Producción Calculado - 50 Años", SwingConstants.CENTER); // Encabezado centrado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita tamaño 14
        header.setForeground(COLOR_HEADER); // Color azul
        header.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0)); // Margen
        panel.add(header, BorderLayout.NORTH); // Encabezado arriba

        String[] cols = {"Año", "Tasa Anualizada (mbd)", "Producción Anual (mmb)", "Petróleo Acumulado (mmb)", "Petróleo con Descuento Acumulado (mmb)"}; // Columnas

        modeloTabla = new DefaultTableModel(cols, 0) { // Crea modelo y GUARDA REFERENCIA en variable de instancia
            public boolean isCellEditable(int r, int c) {
                return false;
            } // Celdas no editables
        };

        JTable tabla = new JTable(modeloTabla); // Crea tabla visual
        configurarEstiloTabla(tabla); // Configura estilos

        JScrollPane scroll = new JScrollPane(tabla); // Crea scroll
        panel.add(scroll, BorderLayout.CENTER); // Agrega scroll al centro

        return panel; // Retorna panel completo
    }

    private void configurarEstiloTabla(JTable tabla) { // Método para configurar estilos visuales de cualquier tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente de celdas normal tamaño 11
        tabla.setRowHeight(26); // Altura de filas 26px
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente del encabezado negrita
        tabla.getTableHeader().setBackground(COLOR_HEADER); // Fondo azul del encabezado
        tabla.getTableHeader().setForeground(Color.WHITE); // Texto blanco del encabezado
        tabla.setGridColor(new Color(220, 220, 220)); // Color de líneas de cuadrícula gris claro
        tabla.setShowGrid(true); // Muestra cuadrícula
        tabla.setIntercellSpacing(new Dimension(1, 1)); // Espacio entre celdas 1×1px

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Método de renderizado
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llama al método base
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.RIGHT); // Columna 0 (Año) centrada, resto alineadas a derecha
                setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente

                if (r % 2 == 0) { // Si fila es par
                    setBackground(Color.WHITE); // Fondo blanco
                } else { // Si fila es impar
                    setBackground(new Color(245, 245, 245)); // Fondo gris muy claro (efecto zebra)
                }

                if (c == 0) { // Si es columna 0 (Año)
                    setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
                }

                setForeground(Color.BLACK); // Texto negro
                return this; // Retorna componente renderizado
            }
        };

        for (int i = 0; i < tabla.getColumnCount(); i++) { // Para cada columna
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica renderizador personalizado
            if (i == 0) { // Si es columna 0 (Año)
                tabla.getColumnModel().getColumn(i).setPreferredWidth(60); // Ancho 60px
            } else { // Para las demás columnas
                tabla.getColumnModel().getColumn(i).setPreferredWidth(160); // Ancho 160px
            }
        }
    }

    private JPanel crearPanelControl() { // Método para crear panel de controles (botones y barra de progreso)
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // BorderLayout con gaps de 10px
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(COLOR_HEADER, 2), // Línea azul de 2px
                BorderFactory.createEmptyBorder(10, 15, 10, 15))); // Margen de 10px arriba/abajo, 15px lados

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5)); // FlowLayout centrado para botones, gap horizontal 20px
        botones.setBackground(Color.WHITE); // Fondo blanco

        JButton btnActualizar = crearBoton("🔄 Actualizar Cálculos", new Color(237, 125, 49), 200, 40); // Crea botón ACTUALIZAR naranja con icono
        btnActualizar.addActionListener(e -> { // Al hacer clic en actualizar
            leerValoresUI(); // Lee valores de los campos de texto
            calcularValores(); // Calcula todos los valores
            actualizarUI(); // Actualiza todas las etiquetas
            calcularTablaProduccion(); // Recalcula tabla de 50 años
            JOptionPane.showMessageDialog(this, "✓ Cálculos actualizados correctamente", "Actualización", JOptionPane.INFORMATION_MESSAGE); // Muestra mensaje de confirmación
        });

        JButton btnOptimizar = crearBoton("🚀 Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 300, 40); // Crea botón OPTIMIZAR azul con icono
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Al hacer clic ejecuta método de optimización

        botones.add(btnActualizar); // Agrega botón actualizar
        botones.add(btnOptimizar); // Agrega botón optimizar

        JPanel progreso = new JPanel(new BorderLayout(8, 8)); // Panel para barra de progreso
        progreso.setBackground(Color.WHITE); // Fondo blanco

        lblProgreso = new JLabel("Listo para comenzar optimización", SwingConstants.CENTER); // Etiqueta de estado inicial
        lblProgreso.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Fuente normal

        progressBar = new JProgressBar(0, NUM_SIMULACIONES); // Crea barra de progreso de 0 a NUM_SIMULACIONES (563)
        progressBar.setStringPainted(true); // Muestra porcentaje en la barra
        progressBar.setPreferredSize(new Dimension(700, 30)); // Tamaño 700×30px
        progressBar.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        progressBar.setForeground(new Color(76, 175, 80)); // Color verde

        progreso.add(lblProgreso, BorderLayout.NORTH); // Agrega etiqueta arriba
        progreso.add(progressBar, BorderLayout.CENTER); // Agrega barra en el centro

        panel.add(botones, BorderLayout.NORTH); // Agrega botones arriba
        panel.add(progreso, BorderLayout.CENTER); // Agrega progreso en el centro

        return panel; // Retorna panel completo
    }

    private JLabel crearLabelParametro(String texto) { // Método auxiliar para crear etiqueta de nombre de parámetro
        JLabel lbl = new JLabel(texto + ":"); // Crea etiqueta con texto seguido de dos puntos
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente normal tamaño 11
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea texto a la derecha
        return lbl; // Retorna etiqueta
    }

    private JTextField crearTextField(String valor, Color bg) { // Método auxiliar para crear campo de texto editable con color de fondo
        JTextField txt = new JTextField(valor); // Crea campo con valor inicial
        txt.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita tamaño 11
        txt.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea texto a la derecha
        txt.setBackground(bg); // Establece color de fondo (verde suposición, amarillo decisión o blanco)
        txt.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1), // Línea gris oscuro de 1px
                BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Margen interno 2px arriba/abajo, 5px lados
        return txt; // Retorna campo
    }

    private JLabel crearLabelFijo(String texto) { // Método auxiliar para crear etiqueta de valor fijo (no editable)
        JLabel lbl = new JLabel(texto); // Crea etiqueta con texto
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
        lbl.setBackground(Color.WHITE); // Fondo blanco
        lbl.setOpaque(true); // Hace opaco
        lbl.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(Color.DARK_GRAY, 1), // Línea gris oscuro
                BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Margen interno
        return lbl; // Retorna etiqueta
    }

    private JLabel crearLabelCalculado(String texto) { // Método auxiliar para crear etiqueta de valor calculado (gris)
        JLabel lbl = new JLabel(texto); // Crea etiqueta con texto
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        lbl.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
        lbl.setBackground(COLOR_CALCULADO); // Fondo gris claro
        lbl.setOpaque(true); // Hace opaco
        lbl.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(Color.GRAY, 1), // Línea gris
                BorderFactory.createEmptyBorder(2, 5, 2, 5))); // Margen interno
        return lbl; // Retorna etiqueta
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar para crear botón personalizado con hover effect
        JButton btn = new JButton(texto); // Crea botón con texto
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita tamaño 13
        btn.setBackground(bg); // Color de fondo
        btn.setForeground(Color.WHITE); // Texto blanco
        btn.setFocusPainted(false); // Sin borde de foco
        btn.setBorderPainted(false); // Sin borde pintado
        btn.setPreferredSize(new Dimension(ancho, alto)); // Tamaño especificado
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); // Cursor de mano al pasar sobre botón

        btn.addMouseListener(new java.awt.event.MouseAdapter() { // Agrega listener para efectos hover
            public void mouseEntered(java.awt.event.MouseEvent evt) { // Al entrar el mouse
                btn.setBackground(bg.brighter()); // Aclara el color (efecto hover)
            }

            public void mouseExited(java.awt.event.MouseEvent evt) { // Al salir el mouse
                btn.setBackground(bg); // Restaura color original
            }
        });

        return btn; // Retorna botón
    }

    private JLabel crearStatLabel(String texto) { // Método auxiliar para crear etiqueta de estadística
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER); // Crea etiqueta centrada
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Fuente negrita tamaño 12
        lbl.setForeground(COLOR_HEADER); // Color azul
        return lbl; // Retorna etiqueta
    }

    private void leerValoresUI() { // Método para leer todos los valores de los campos de texto de la interfaz
        try { // Bloque try para capturar excepciones de formato
            stoiip = Double.parseDouble(txtStoiip.getText().replace(",", "")); // Lee STOIIP, elimina comas y parsea a double
            recuperacion = Double.parseDouble(txtRecuperacion.getText().replace(",", "")); // Lee recuperación
            buenaTasa = Double.parseDouble(txtBuenaTasa.getText().replace(",", "")); // Lee buena tasa
            pozosPerforar = Integer.parseInt(txtPozos.getText().replace(",", "")); // Lee pozos y parsea a entero
            factorDescuento = Double.parseDouble(txtFactorDescuento.getText().replace(",", "")); // Lee factor descuento
            buenCosto = Double.parseDouble(txtBuenCosto.getText().replace(",", "")); // Lee buen costo
            tamanoInstalacion = Double.parseDouble(txtTamanoInstalacion.getText().replace(",", "")); // Lee tamaño instalación
            plateauRateIs = Double.parseDouble(txtPlateauRateIs.getText().replace(",", "")); // Lee plateau rate
        } catch (Exception e) { // Captura cualquier excepción al parsear
            JOptionPane.showMessageDialog(this, "Error al leer valores: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Muestra mensaje de error
        }
    }

    private void calcularValores() { // Método principal que calcula TODOS los valores derivados del modelo de producción petrolera
        reservas = stoiip * recuperacion / 100.0; // Calcula reservas recuperables = STOIIP × porcentaje_recuperación / 100
        maxPlateauRate = (plateauRateIs / 100.0) * reservas / 0.365; // Calcula tasa máxima de plateau = (plateau_rate_% / 100) × reservas / 0.365 (conversión anual a diaria)
        plateauRate = Math.min(maxPlateauRate, Math.min(buenaTasa * pozosPerforar, tamanoInstalacion)); // Calcula tasa efectiva = mínimo entre: max_plateau, capacidad_pozos, capacidad_instalación
        aumentarProduccion = 0.365 * plateauRate * 0.5 * timeToPlateau; // Calcula producción durante ramp-up = 0.365 × tasa × 0.5 × tiempo_plateau (área del triángulo)
        plateauProduction = Math.max(0, plateauEndsAt * (reservas / 100.0) - aumentarProduccion); // Calcula producción durante plateau = 65% de reservas - producción de ramp-up (no negativo)
        plateauEndsAtCalc = plateauProduction / (0.365 * plateauRate) + timeToPlateau; // Calcula cuándo termina plateau (años) = producción_plateau / tasa_anual + tiempo_ramp-up
        factorDeclive = 0.365 * (plateauRate - tarifaMinima) / (reservas - plateauProduction - aumentarProduccion); // Calcula factor de declive exponencial = 0.365 × (tasa_plateau - tasa_mínima) / reservas_restantes

        if (tarifaMinima > 0) { // Si hay tarifa mínima (caso normal)
            vidaProduccion = plateauEndsAtCalc - Math.log(tarifaMinima / plateauRate) / factorDeclive; // Calcula vida total de producción usando ecuación de declive exponencial
        } else { // Si tarifa mínima es cero (caso especial)
            vidaProduccion = 1e20; // Establece vida "infinita" (número muy grande)
        }

        costosPozo = buenCosto * pozosPerforar; // Calcula costos totales de pozos = costo_por_pozo × número_pozos
        costosInstalacionesCalc = buscarCostoInstalacion(tamanoInstalacion); // Busca costo de instalaciones en la tabla según tamaño
    }

    private double buscarCostoInstalacion(double produccion) { // Método para buscar costo de instalación en tabla según capacidad de producción
        for (int i = 0; i < costosInstalaciones.length; i++) { // Itera sobre array de costos de instalaciones (7 filas)
            if (produccion <= costosInstalaciones[i][0]) { // Si la producción es menor o igual a la capacidad de este nivel
                return costosInstalaciones[i][1]; // Retorna el costo correspondiente
            }
        }
        return costosInstalaciones[costosInstalaciones.length - 1][1]; // Si no encontró (producción muy alta), retorna costo del nivel máximo
    }

    private void calcularTablaProduccion() { // Método que calcula el perfil de producción completo de 50 años y llena la tabla
        modeloTabla.setRowCount(0); // Limpia todas las filas existentes de la tabla

        double[] tasaAnualizada = new double[ANOS + 1]; // Array para almacenar tasa anualizada de cada año (índices 1-50)
        double[] produccionAnual = new double[ANOS + 1]; // Array para almacenar producción anual de cada año
        double[] petroleoAcumulado = new double[ANOS + 1]; // Array para almacenar petróleo acumulado hasta cada año
        double[] petroleoDescuentoAcum = new double[ANOS + 1]; // Array para almacenar petróleo descontado acumulado hasta cada año

        for (int ano = 1; ano <= ANOS; ano++) { // Itera sobre cada año de 1 a 50
            if (ano < timeToPlateau + 1) { // Si estamos en fase de ramp-up (primeros 2 años)
                produccionAnual[ano] = ano * 0.365 * plateauRate / (timeToPlateau + 1); // Calcula producción lineal creciente
            } else { // Si estamos después del ramp-up (años 3+)
                double maxMin1 = Math.min(plateauEndsAtCalc + 1 - ano, 1); // Calcula cuánto del año está en plateau (máximo 1)
                double part1 = 0.365 * plateauRate * Math.max(0, maxMin1); // Calcula parte 1: producción durante plateau

                double minVidaAno1 = Math.min(vidaProduccion, ano - 1); // Calcula tiempo hasta año anterior
                double maxExp1 = Math.max(0, minVidaAno1 - plateauEndsAtCalc); // Calcula tiempo en declive hasta año anterior
                double exp1 = Math.exp(-factorDeclive * maxExp1); // Calcula factor exponencial para año anterior

                double minVidaAno = Math.min(vidaProduccion, ano); // Calcula tiempo hasta año actual
                double maxExp2 = Math.max(minVidaAno - plateauEndsAtCalc, 0); // Calcula tiempo en declive hasta año actual
                double exp2 = Math.exp(-factorDeclive * maxExp2); // Calcula factor exponencial para año actual

                double part2 = 0.365 * plateauRate * (exp1 - exp2) / factorDeclive; // Calcula parte 2: producción durante declive (integral exponencial)

                produccionAnual[ano] = part1 + part2; // Producción total = plateau + declive
            }

            tasaAnualizada[ano] = produccionAnual[ano] / 0.365; // Calcula tasa anualizada = producción_anual / 0.365 (convierte mmb/año a mbd)

            if (ano == 1) { // Si es el primer año
                petroleoAcumulado[ano] = produccionAnual[ano]; // Acumulado = producción del año 1
            } else { // Si es año 2+
                petroleoAcumulado[ano] = petroleoAcumulado[ano - 1] + produccionAnual[ano]; // Acumula: suma producción actual a acumulado anterior
            }

            if (ano == 1) { // Si es el primer año
                petroleoDescuentoAcum[ano] = produccionAnual[ano]; // Descontado = producción del año 1 (sin descuento porque es año base)
            } else { // Si es año 2+
                double descuento = Math.pow(1.0 + 0.01 * factorDescuento, ano - 1); // Calcula factor de descuento = (1 + tasa)^(año-1)
                petroleoDescuentoAcum[ano] = petroleoDescuentoAcum[ano - 1] + (produccionAnual[ano] / descuento); // Acumula producción descontada
            }

            modeloTabla.addRow(new Object[]{ // Agrega fila a la tabla con 5 valores formateados
                    ano, FMT2.format(tasaAnualizada[ano]), FMT2.format(produccionAnual[ano]), FMT2.format(petroleoAcumulado[ano]), FMT2.format(petroleoDescuentoAcum[ano])});
        }

        reservasDescontadas = petroleoDescuentoAcum[ANOS]; // Guarda reservas descontadas totales (valor del año 50)
        npv = reservasDescontadas * margenPetroleo - costosPozo - costosInstalacionesCalc; // Calcula NPV final = reservas_descontadas × margen - costos
    }

    private void actualizarUI() { // Método para actualizar todas las etiquetas de la interfaz con los valores calculados
        lblReservas.setText(FMT2.format(reservas) + " mmbbls"); // Actualiza etiqueta reservas
        lblMaxPlateau.setText(FMT2.format(maxPlateauRate) + " mbd"); // Actualiza max plateau rate
        lblPlateauRate.setText(FMT2.format(plateauRate) + " mbd"); // Actualiza plateau rate
        lblAumentar.setText(FMT2.format(aumentarProduccion) + " mmbbls"); // Actualiza aumentar producción
        lblPlateauProd.setText(FMT2.format(plateauProduction) + " mmbbls"); // Actualiza plateau production
        lblPlateauEnds.setText(FMT2.format(plateauEndsAtCalc) + " años"); // Actualiza plateau ends
        lblFactorDeclive.setText(FMT2.format(factorDeclive)); // Actualiza factor declive
        lblVidaProd.setText(FMT2.format(vidaProduccion) + " años"); // Actualiza vida producción
        lblReservasDesc.setText(FMT2.format(reservasDescontadas) + " mmbbls"); // Actualiza reservas descontadas
        lblCostosPozo.setText(FMT2.format(costosPozo) + " $mm"); // Actualiza costos pozo
        lblCostosInst.setText(FMT2.format(costosInstalacionesCalc) + " $mm"); // Actualiza costos instalaciones
        lblNPV.setText(FMT2.format(npv) + " $mm"); // Actualiza NPV
    }

    private void ejecutarOptimizacion() { // Método principal que ejecuta la optimización completa usando Monte Carlo y OptQuest
        todosNPV.clear(); // Limpia lista de todos los NPVs
        mejorSimulacionNPVs.clear(); // Limpia lista de NPVs de mejor simulación
        mejorNPV = Double.NEGATIVE_INFINITY; // Reinicia mejor NPV a infinito negativo

        progressBar.setValue(0); // Reinicia barra de progreso
        lblProgreso.setText("⏳ Ejecutando optimización Monte Carlo..."); // Actualiza mensaje

        new SwingWorker<Void, Integer>() { // Crea SwingWorker para ejecutar en hilo de fondo
            protected Void doInBackground() { // Método ejecutado en hilo separado
                Random rand = new Random(12345); // Generador aleatorio con semilla fija para reproducibilidad

                for (int sim = 1; sim <= NUM_SIMULACIONES; sim++) { // Bucle principal: 563 simulaciones
                    int pozos = rand.nextInt(49) + 2; // Genera pozos aleatorios entre 2 y 50
                    int instIndex = rand.nextInt(7); // Genera índice instalación 0-6
                    double tamanoInst = 50 + 50 * instIndex; // Calcula tamaño: 50, 100, 150, 200, 250, 300 o 350 mbd
                    double plateauIs = 4.5 + rand.nextDouble() * (15.0 - 4.5); // Genera plateau rate 4.5% a 15%

                    List<Double> npvsPrueba = new ArrayList<>(); // Lista para 1000 NPVs de esta simulación

                    for (int mc = 0; mc < NUM_PRUEBAS_MC; mc++) { // Bucle interno: 1000 pruebas Monte Carlo
                        LogNormalDistribution stoiipDist = new LogNormalDistribution(Math.log(1500.0), 300.0 / 1500.0); // Distribución LogNormal para STOIIP
                        NormalDistribution recupDist = new NormalDistribution(42.0, 1.2); // Distribución Normal para recuperación
                        NormalDistribution tasaDist = new NormalDistribution(10.0, 3.0); // Distribución Normal para tasa
                        LogNormalDistribution descDist = new LogNormalDistribution(Math.log(10.0), 1.2 / 10.0); // Distribución LogNormal para descuento
                        TriangularDistribution costoDist = new TriangularDistribution(9.0, 10.0, 12.0); // Distribución Triangular para costo

                        double stoiipSample = stoiipDist.sample(); // Genera muestra STOIIP
                        double recupSample = recupDist.sample(); // Genera muestra recuperación
                        double tasaSample = tasaDist.sample(); // Genera muestra tasa
                        double descSample = descDist.sample(); // Genera muestra descuento
                        double costoSample = costoDist.sample(); // Genera muestra costo

                        double npvSample = calcularNPVSimulacion(stoiipSample, recupSample, tasaSample, pozos, descSample, costoSample, tamanoInst, plateauIs); // Calcula NPV

                        npvsPrueba.add(npvSample); // Agrega a lista de esta simulación
                        todosNPV.add(npvSample); // Agrega a lista global

                        if (mc + 1 >= MIN_TRIALS_FOR_CHECK && (mc + 1) % CHECK_INTERVAL == 0) { // EARLY STOPPING: cada 500 pruebas después de 500
                            List<Double> temp = new ArrayList<>(npvsPrueba); // Crea copia
                            Collections.sort(temp); // Ordena
                            double currentP10 = temp.get((int) (temp.size() * 0.10)); // Calcula P10 actual

                            if (currentP10 < mejorNPV - 50.0) { // Si P10 es más de $50mm peor que mejor
                                break; // Detiene pruebas (early stopping - esta combinación no es prometedora)
                            }
                        }
                    }

                    Collections.sort(npvsPrueba); // Ordena NPVs de esta simulación
                    double percentil10 = npvsPrueba.get((int) (npvsPrueba.size() * 0.10)); // Calcula percentil 10

                    if (percentil10 > mejorNPV) { // Si este P10 es mejor que el mejor encontrado
                        mejorNPV = percentil10; // Actualiza mejor NPV
                        mejorPozos = pozos; // Actualiza mejor pozos
                        mejorTamanoInst = tamanoInst; // Actualiza mejor tamaño
                        mejorPlateauRateIs = plateauIs; // Actualiza mejor plateau rate
                        mejorSimulacionNPVs = new ArrayList<>(npvsPrueba); // Guarda 1000 NPVs para histograma
                    }

                    if (sim % 5 == 0) { // Cada 5 simulaciones
                        publish(sim); // Publica actualización de progreso
                    }
                }

                return null; // Retorna null
            }

            protected void process(List<Integer> chunks) { // Actualiza progreso en hilo de UI
                int ultimo = chunks.get(chunks.size() - 1); // Obtiene última actualización
                progressBar.setValue(ultimo); // Actualiza barra
                int porcentaje = (int) ((ultimo * 100.0) / NUM_SIMULACIONES); // Calcula porcentaje
                lblProgreso.setText(String.format("⏳ Progreso: %d / %d simulaciones (%d%%)", ultimo, NUM_SIMULACIONES, porcentaje)); // Actualiza texto
            }

            protected void done() { // Al terminar (en hilo de UI)
                progressBar.setValue(NUM_SIMULACIONES); // Completa barra al 100%
                lblProgreso.setText("✅ Optimización completada - " + NUM_SIMULACIONES + " simulaciones"); // Mensaje completado

                txtPozos.setText(String.valueOf(mejorPozos)); // Actualiza campo pozos con mejor valor
                txtTamanoInstalacion.setText(FMT2.format(mejorTamanoInst)); // Actualiza campo tamaño con mejor valor
                txtPlateauRateIs.setText(FMT2.format(mejorPlateauRateIs)); // Actualiza campo plateau rate con mejor valor

                pozosPerforar = mejorPozos; // Actualiza variable de instancia
                tamanoInstalacion = mejorTamanoInst; // Actualiza variable de instancia
                plateauRateIs = mejorPlateauRateIs; // Actualiza variable de instancia

                calcularValores(); // Recalcula todos los valores con mejores parámetros
                actualizarUI(); // Actualiza todas las etiquetas
                calcularTablaProduccion(); // Recalcula tabla de 50 años

                mostrarResultadosOptimizacion(); // Muestra ventana emergente con resultados
            }
        }.execute(); // Inicia ejecución del SwingWorker
    }

    private double calcularNPVSimulacion(double stoiip, double recup, double buenaTasa, int pozos, double descuento, double costo, double tamanoInst, double plateauIs) { // Calcula NPV para simulación Monte Carlo con parámetros dados
        double res = stoiip * recup / 100.0; // Calcula reservas
        double maxPR = (plateauIs / 100.0) * res / 0.365; // Calcula max plateau rate
        double pr = Math.min(maxPR, Math.min(buenaTasa * pozos, tamanoInst)); // Calcula plateau rate efectivo
        double aum = 0.365 * pr * 0.5 * timeToPlateau; // Calcula ramp-up
        double pp = Math.max(0, plateauEndsAt * (res / 100.0) - aum); // Calcula plateau production
        double pea = pp / (0.365 * pr) + timeToPlateau; // Calcula plateau ends
        double fd = 0.365 * (pr - tarifaMinima) / (res - pp - aum); // Calcula factor declive
        double vp = (tarifaMinima > 0) ? pea - Math.log(tarifaMinima / pr) / fd : 1e20; // Calcula vida producción

        double resDesc = 0; // Inicializa reservas descontadas

        for (int ano = 1; ano <= ANOS; ano++) { // Itera 50 años
            double prodAnual; // Variable producción anual

            if (ano < timeToPlateau + 1) { // Si ramp-up
                prodAnual = ano * 0.365 * pr / (timeToPlateau + 1); // Producción lineal
            } else { // Si después
                double term1 = 0.365 * pr * Math.max(0, Math.min(pea + 1 - ano, 1)); // Término plateau
                double exp1 = Math.exp(-fd * Math.max(0, Math.min(vp, ano - 1) - pea)); // Exponencial año anterior
                double exp2 = Math.exp(-fd * Math.max(Math.min(vp, ano) - pea, 0)); // Exponencial año actual
                double term2 = 0.365 * pr * (exp1 - exp2) / fd; // Término declive
                prodAnual = term1 + term2; // Suma
            }

            if (ano == 1) { // Primer año
                resDesc = prodAnual; // Sin descuento
            } else { // Año 2+
                double desc = Math.pow(1.0 + 0.01 * descuento, ano - 1); // Factor descuento
                resDesc += prodAnual / desc; // Acumula descontado
            }
        }

        double costoPozos = costo * pozos; // Calcula costos pozos
        double costoInst = buscarCostoInstalacion(tamanoInst); // Busca costo instalación

        return resDesc * margenPetroleo - costoPozos - costoInst; // Retorna NPV
    }

    private void mostrarResultadosOptimizacion() { // Muestra ventana emergente con resultados de optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Diálogo no modal
        dlg.setLayout(new BorderLayout(15, 15)); // BorderLayout

        JPanel main = new JPanel(new BorderLayout(15, 15)); // Panel principal
        main.setBackground(Color.WHITE); // Fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen 20px

        JPanel header = new JPanel(new GridLayout(2, 1, 5, 5)); // Encabezado 2 filas
        header.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblSim = new JLabel("📊 " + NUM_SIMULACIONES + " simulaciones completadas", SwingConstants.CENTER); // Título con icono
        lblSim.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Fuente grande
        lblSim.setForeground(COLOR_HEADER); // Color azul

        JLabel lblVista = new JLabel("Vista de mejor solución encontrada", SwingConstants.CENTER); // Subtítulo
        lblVista.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Fuente normal
        lblVista.setForeground(Color.GRAY); // Color gris

        header.add(lblSim); // Agrega título
        header.add(lblVista); // Agrega subtítulo

        main.add(header, BorderLayout.NORTH); // Encabezado arriba

        JPanel centro = new JPanel(new GridLayout(3, 1, 15, 15)); // Panel centro 3 filas
        centro.setBackground(Color.WHITE); // Fondo blanco

        JPanel npvPanel = new JPanel(new BorderLayout()); // Panel para NPV
        npvPanel.setBackground(new Color(232, 245, 233)); // Fondo verde muy claro
        npvPanel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(new Color(76, 175, 80), 2), // Línea verde de 2px
                BorderFactory.createEmptyBorder(15, 15, 15, 15))); // Margen 15px

        JLabel lblNPVTitle = new JLabel("🎯 NPV Percentil 10% (Optimizado)", SwingConstants.CENTER); // Título NPV con icono
        lblNPVTitle.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita
        lblNPVTitle.setForeground(new Color(27, 94, 32)); // Color verde oscuro

        JLabel lblNPVValue = new JLabel("$ " + FMT2.format(mejorNPV) + " mm", SwingConstants.CENTER); // Valor NPV
        lblNPVValue.setFont(new Font("Segoe UI", Font.BOLD, 28)); // Fuente muy grande
        lblNPVValue.setForeground(new Color(27, 94, 32)); // Color verde oscuro

        npvPanel.add(lblNPVTitle, BorderLayout.NORTH); // Título arriba
        npvPanel.add(lblNPVValue, BorderLayout.CENTER); // Valor centro

        centro.add(npvPanel); // Agrega panel NPV
        centro.add(crearPanelVariablesOptimas()); // Agrega panel variables óptimas
        centro.add(crearPanelEstadisticas()); // Agrega panel estadísticas

        main.add(centro, BorderLayout.CENTER); // Centro al centro

        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Panel botones
        botonesPanel.setBackground(Color.WHITE); // Fondo blanco

        JButton btnHistograma = crearBoton("📊 Ver Histograma NPV", new Color(33, 150, 243), 200, 35); // Botón histograma azul
        btnHistograma.addActionListener(e -> mostrarDistribucionNPV()); // Al clic muestra histograma

        JButton btnCerrar = crearBoton("✓ Cerrar", new Color(76, 175, 80), 120, 35); // Botón cerrar verde
        btnCerrar.addActionListener(e -> dlg.dispose()); // Al clic cierra diálogo

        botonesPanel.add(btnHistograma); // Agrega botón histograma
        botonesPanel.add(btnCerrar); // Agrega botón cerrar

        main.add(botonesPanel, BorderLayout.SOUTH); // Botones abajo

        dlg.add(main); // Agrega panel principal
        dlg.setSize(800, 700); // Tamaño 800×700px
        dlg.setLocationRelativeTo(this); // Centra
        dlg.setVisible(true); // Muestra
    }

    private JPanel crearPanelVariablesOptimas() { // Crea panel variables decisión óptimas
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(COLOR_HEADER, 2), // Línea azul
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Margen

        JLabel titulo = new JLabel("Variables de Decisión Óptimas", SwingConstants.CENTER); // Título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita
        titulo.setForeground(COLOR_HEADER); // Color azul
        panel.add(titulo, BorderLayout.NORTH); // Título arriba

        String[][] datos = { // Matriz con datos variables óptimas
                {"Pozos a perforar", String.valueOf(mejorPozos), "pozos"}, {"Tamaño de instalación", FMT2.format(mejorTamanoInst), "mbd"}, {"Plateau rate is", FMT2.format(mejorPlateauRateIs), "% reservas/año"}};

        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 8)); // Grid 3×3
        grid.setBackground(Color.WHITE); // Fondo blanco
        grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen

        for (String[] row : datos) { // Para cada fila
            JLabel lblNombre = new JLabel(row[0]); // Etiqueta nombre
            lblNombre.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Fuente normal

            JLabel lblValor = new JLabel(row[1]); // Etiqueta valor
            lblValor.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita
            lblValor.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea derecha
            lblValor.setOpaque(true); // Opaco
            lblValor.setBackground(COLOR_DECISION); // Fondo amarillo
            lblValor.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                    BorderFactory.createLineBorder(Color.DARK_GRAY), // Línea gris oscuro
                    BorderFactory.createEmptyBorder(3, 8, 3, 8))); // Margen

            JLabel lblUnidad = new JLabel(row[2]); // Etiqueta unidad
            lblUnidad.setFont(new Font("Segoe UI", Font.PLAIN, 11)); // Fuente pequeña
            lblUnidad.setForeground(Color.GRAY); // Color gris

            grid.add(lblNombre); // Agrega nombre
            grid.add(lblValor); // Agrega valor
            grid.add(lblUnidad); // Agrega unidad
        }

        panel.add(grid, BorderLayout.CENTER); // Grid al centro

        return panel; // Retorna panel
    }

    private JPanel crearPanelEstadisticas() { // Crea panel estadísticas NPV mejor simulación
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
                BorderFactory.createLineBorder(new Color(255, 152, 0), 2), // Línea naranja
                BorderFactory.createEmptyBorder(10, 10, 10, 10))); // Margen

        JLabel titulo = new JLabel("Estadísticas NPV (Mejor simulación)", SwingConstants.CENTER); // Título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente negrita
        titulo.setForeground(new Color(230, 81, 0)); // Color naranja oscuro
        panel.add(titulo, BorderLayout.NORTH); // Título arriba

        if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Copia lista
            Collections.sort(npvs); // Ordena

            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
            double min = npvs.get(0); // Obtiene mínimo
            double max = npvs.get(npvs.size() - 1); // Obtiene máximo
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Percentil 10
            double p50 = npvs.get((int) (npvs.size() * 0.50)); // Percentil 50 (mediana)
            double p90 = npvs.get((int) (npvs.size() * 0.90)); // Percentil 90

            JPanel grid = new JPanel(new GridLayout(6, 2, 8, 6)); // Grid 6×2
            grid.setBackground(Color.WHITE); // Fondo blanco
            grid.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15)); // Margen

            addStatRow(grid, "Media:", "$ " + FMT2.format(media) + " mm"); // Fila media
            addStatRow(grid, "Percentil 10%:", "$ " + FMT2.format(p10) + " mm"); // Fila P10
            addStatRow(grid, "Mediana (P50):", "$ " + FMT2.format(p50) + " mm"); // Fila P50
            addStatRow(grid, "Percentil 90%:", "$ " + FMT2.format(p90) + " mm"); // Fila P90
            addStatRow(grid, "Mínimo:", "$ " + FMT2.format(min) + " mm"); // Fila mínimo
            addStatRow(grid, "Máximo:", "$ " + FMT2.format(max) + " mm"); // Fila máximo

            panel.add(grid, BorderLayout.CENTER); // Grid al centro
        }

        return panel; // Retorna panel
    }

    private void addStatRow(JPanel grid, String label, String value) { // Agrega fila estadística a grid
        JLabel lblLabel = new JLabel(label); // Etiqueta nombre
        lblLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Fuente normal
        lblLabel.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea derecha

        JLabel lblValue = new JLabel(value); // Etiqueta valor
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Fuente negrita
        lblValue.setHorizontalAlignment(SwingConstants.LEFT); // Alinea izquierda

        grid.add(lblLabel); // Agrega nombre
        grid.add(lblValue); // Agrega valor
    }

    private void mostrarDistribucionNPV() { // Muestra ventana con histograma distribución NPV
        JDialog dlg = new JDialog(this, "Previsión: NPV - Distribución", false); // Diálogo no modal
        dlg.setLayout(new BorderLayout(10, 10)); // BorderLayout

        JPanel main = new JPanel(new BorderLayout(10, 10)); // Panel principal
        main.setBackground(Color.WHITE); // Fondo blanco
        main.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15)); // Margen 15px

        JLabel header = new JLabel(NUM_PRUEBAS_MC + " pruebas - Vista de frecuencia", SwingConstants.CENTER); // Encabezado
        header.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente negrita
        header.setForeground(COLOR_HEADER); // Color azul
        main.add(header, BorderLayout.NORTH); // Encabezado arriba

        JPanel histograma = new JPanel() { // Panel personalizado para dibujar histograma
            @Override
            protected void paintComponent(Graphics g) { // Sobrescribe método de pintado
                super.paintComponent(g); // Llama al método base
                if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos
                    dibujarHistograma(g, getWidth(), getHeight()); // Dibuja histograma
                }
            }
        };
        histograma.setBackground(Color.WHITE); // Fondo blanco
        histograma.setPreferredSize(new Dimension(750, 450)); // Tamaño 750×450px
        histograma.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY)); // Borde gris claro

        main.add(histograma, BorderLayout.CENTER); // Histograma al centro

        if (!mejorSimulacionNPVs.isEmpty()) { // Si hay datos
            List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Copia lista
            Collections.sort(npvs); // Ordena
            double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
            double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula P10

            JPanel stats = new JPanel(new GridLayout(1, 3, 20, 5)); // Panel estadísticas 1×3
            stats.setBackground(Color.WHITE); // Fondo blanco
            stats.setBorder(BorderFactory.createEmptyBorder(10, 0, 5, 0)); // Margen

            stats.add(crearStatLabel("10% = $ " + FMT2.format(p10) + " mm")); // P10
            stats.add(crearStatLabel("Media = $ " + FMT2.format(media) + " mm")); // Media
            stats.add(crearStatLabel(FMT0.format(npvs.size()) + " muestras")); // Número muestras

            main.add(stats, BorderLayout.SOUTH); // Estadísticas abajo
        }

        dlg.add(main); // Agrega panel principal
        dlg.setSize(800, 600); // Tamaño 800×600px
        dlg.setLocationRelativeTo(this); // Centra
        dlg.setVisible(true); // Muestra
    }

    private void dibujarHistograma(Graphics g, int width, int height) { // Dibuja histograma personalizado usando Graphics2D
        if (mejorSimulacionNPVs == null || mejorSimulacionNPVs.isEmpty()) {
            return;
        } // Si no hay datos retorna

        Graphics2D g2 = (Graphics2D) g; // Convierte a Graphics2D para capacidades avanzadas
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); // Activa antialiasing para suavizar líneas

        int margin = 60; // Margen de 60px alrededor del gráfico
        int chartWidth = width - 2 * margin; // Ancho del gráfico = ancho total - 2×margen
        int chartHeight = height - 2 * margin; // Alto del gráfico = alto total - 2×margen

        if (chartWidth <= 0 || chartHeight <= 0) {
            return;
        } // Si dimensiones inválidas retorna

        List<Double> npvs = new ArrayList<>(mejorSimulacionNPVs); // Copia lista de NPVs
        Collections.sort(npvs); // Ordena de menor a mayor

        double minVal = npvs.get(0); // Obtiene valor mínimo
        double maxVal = npvs.get(npvs.size() - 1); // Obtiene valor máximo
        double range = maxVal - minVal; // Calcula rango

        if (range <= 0) { // Si rango es cero (todos valores iguales)
            g2.setColor(new Color(100, 181, 246)); // Color azul
            int barX = margin + chartWidth / 2 - 10; // Posición X centrada
            int barWidth = 20; // Ancho barra 20px
            int barHeight = chartHeight; // Alto completo
            g2.fillRect(barX, margin, barWidth, barHeight); // Dibuja barra central
            dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, npvs.size()); // Dibuja ejes
            return; // Retorna
        }

        double binWidth = range / NUM_BINS_HISTOGRAMA; // Calcula ancho de cada bin
        int[] bins = new int[NUM_BINS_HISTOGRAMA]; // Array para contar frecuencias (50 bins)

        for (double val : npvs) { // Para cada valor NPV
            int binIndex = (int) ((val - minVal) / binWidth); // Calcula índice del bin
            if (binIndex >= NUM_BINS_HISTOGRAMA) {
                binIndex = NUM_BINS_HISTOGRAMA - 1;
            } // Si excede ajusta al último
            if (binIndex < 0) {
                binIndex = 0;
            } // Si negativo ajusta al primero
            bins[binIndex]++; // Incrementa contador del bin
        }

        int maxBin = 0; // Inicializa máximo bin
        for (int bin : bins) {
            if (bin > maxBin) {
                maxBin = bin;
            }
        } // Busca bin con mayor frecuencia
        if (maxBin == 0) {
            maxBin = 1;
        } // Si todos cero establece 1 (evita división por cero)

        double barWidthPixels = (double) chartWidth / NUM_BINS_HISTOGRAMA; // Calcula ancho barra en píxeles
        int minBarWidth = Math.max(1, (int) Math.floor(barWidthPixels) - 1); // Ancho mínimo 1px, deja gap de 1px

        g2.setColor(new Color(100, 181, 246)); // Color azul para barras

        for (int i = 0; i < NUM_BINS_HISTOGRAMA; i++) { // Para cada bin
            if (bins[i] > 0) { // Si tiene frecuencia
                int barHeight = (int) Math.round(((double) bins[i] / maxBin) * chartHeight); // Calcula altura proporcional
                int x = margin + (int) Math.round(i * barWidthPixels); // Calcula posición X
                int y = height - margin - barHeight; // Calcula posición Y (desde abajo)
                if (barHeight < 1) {
                    barHeight = 1;
                } // Altura mínima 1px
                g2.fillRect(x, y, minBarWidth, barHeight); // Dibuja barra
            }
        }

        dibujarEjesYEtiquetas(g2, width, height, margin, chartWidth, chartHeight, minVal, maxVal, maxBin); // Dibuja ejes y etiquetas
        dibujarLineasPercentiles(g2, width, height, margin, chartWidth, npvs, minVal, maxVal); // Dibuja líneas P10 y media
    }

    private void dibujarEjesYEtiquetas(Graphics2D g2, int width, int height, int margin, int chartWidth, int chartHeight, double minVal, double maxVal, int maxBin) { // Dibuja ejes X e Y con etiquetas
        g2.setColor(Color.BLACK); // Color negro para ejes
        g2.setStroke(new BasicStroke(2)); // Grosor 2px
        g2.drawLine(margin, height - margin, width - margin, height - margin); // Dibuja eje X (horizontal)
        g2.drawLine(margin, margin, margin, height - margin); // Dibuja eje Y (vertical)

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10)); // Fuente pequeña para etiquetas

        for (int i = 0; i <= 5; i++) { // 6 etiquetas en eje X (0%, 20%, 40%, 60%, 80%, 100%)
            double val = minVal + (maxVal - minVal) * i / 5.0; // Calcula valor
            int x = margin + (int) Math.round(chartWidth * i / 5.0); // Calcula posición X
            String label = FMT0.format(val); // Formatea valor
            FontMetrics fm = g2.getFontMetrics(); // Obtiene métricas de fuente
            int labelWidth = fm.stringWidth(label); // Obtiene ancho etiqueta
            g2.drawString(label, x - labelWidth / 2, height - margin + 20); // Dibuja etiqueta centrada
            g2.drawLine(x, height - margin, x, height - margin + 5); // Dibuja tick (marca pequeña)
        }

        for (int i = 0; i <= 5; i++) { // 6 etiquetas en eje Y (frecuencias)
            int val = (int) Math.round(maxBin * i / 5.0); // Calcula valor
            int y = height - margin - (int) Math.round(chartHeight * i / 5.0); // Calcula posición Y
            String label = String.valueOf(val); // Convierte a string
            FontMetrics fm = g2.getFontMetrics(); // Métricas fuente
            int labelWidth = fm.stringWidth(label); // Ancho etiqueta
            g2.drawString(label, margin - labelWidth - 10, y + 5); // Dibuja etiqueta a la izquierda
            g2.drawLine(margin - 5, y, margin, y); // Dibuja tick
        }

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita para etiquetas de ejes

        String xLabel = "NPV ($mm)"; // Etiqueta eje X
        FontMetrics fm = g2.getFontMetrics(); // Métricas
        int xLabelWidth = fm.stringWidth(xLabel); // Ancho
        g2.drawString(xLabel, width / 2 - xLabelWidth / 2, height - 10); // Dibuja centrada abajo

        g2.rotate(-Math.PI / 2); // Rota -90° para texto vertical
        String yLabel = "Frecuencia"; // Etiqueta eje Y
        int yLabelWidth = fm.stringWidth(yLabel); // Ancho
        g2.drawString(yLabel, -height / 2 - yLabelWidth / 2, 15); // Dibuja centrada a la izquierda
        g2.rotate(Math.PI / 2); // Restaura rotación
    }

    private void dibujarLineasPercentiles(Graphics2D g2, int width, int height, int margin, int chartWidth, List<Double> npvs, double minVal, double maxVal) { // Dibuja líneas verticales para P10 y media
        double range = maxVal - minVal; // Calcula rango
        if (range <= 0) {
            return;
        } // Si rango cero retorna

        double p10 = npvs.get((int) (npvs.size() * 0.10)); // Calcula P10
        int xP10 = margin + (int) Math.round((p10 - minVal) / range * chartWidth); // Calcula posición X de P10

        g2.setColor(new Color(244, 67, 54)); // Color rojo para P10
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Línea punteada
        g2.drawLine(xP10, margin, xP10, height - margin); // Dibuja línea vertical P10

        g2.setFont(new Font("Segoe UI", Font.BOLD, 11)); // Fuente negrita
        g2.drawString("P10", xP10 - 15, margin - 10); // Dibuja etiqueta "P10" arriba

        double media = npvs.stream().mapToDouble(d -> d).average().orElse(0); // Calcula media
        int xMedia = margin + (int) Math.round((media - minVal) / range * chartWidth); // Calcula posición X media

        g2.setColor(new Color(76, 175, 80)); // Color verde para media
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0)); // Línea punteada
        g2.drawLine(xMedia, margin, xMedia, height - margin); // Dibuja línea vertical media
        g2.drawString("Media", xMedia - 20, margin - 10); // Dibuja etiqueta "Media" arriba
    }

    public static void main(String[] args) { // Método main - punto de entrada del programa
        try { // Bloque try
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); // Establece Look and Feel del sistema operativo
        } catch (Exception e) { // Captura excepción
            e.printStackTrace(); // Imprime error
        }

        SwingUtilities.invokeLater(() -> { // Ejecuta en hilo de eventos Swing (EDT)
            OilReservesSimulator sim = new OilReservesSimulator(); // Crea instancia del simulador
            sim.setVisible(true); // Hace visible la ventana
        });
    }
}