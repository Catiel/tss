package actividad_8.ejercicio_4; // Declaración del paquete donde se encuentra la clase

import com.formdev.flatlaf.FlatLightLaf; // Importa tema moderno FlatLaf para la interfaz
import org.apache.commons.math3.distribution.TriangularDistribution; // Importa distribución triangular para simulación Monte Carlo
import org.jfree.chart.ChartFactory; // Importa fábrica para crear gráficos JFreeChart
import org.jfree.chart.ChartPanel; // Importa panel contenedor de gráficos
import org.jfree.chart.JFreeChart; // Importa clase principal de gráficos
import org.jfree.chart.plot.PlotOrientation; // Importa orientación de gráficos (vertical/horizontal)
import org.jfree.chart.plot.XYPlot; // Importa objeto de gráfico de dispersión/barras XY
import org.jfree.chart.renderer.xy.XYBarRenderer; // Importa renderizador de barras para gráficos XY
import org.jfree.data.statistics.HistogramDataset; // Importa dataset para histogramas

import javax.swing.*; // Importa componentes de interfaz gráfica Swing
import javax.swing.table.DefaultTableCellRenderer; // Importa renderizador personalizado de celdas de tabla
import javax.swing.table.DefaultTableModel; // Importa modelo de datos para tablas
import java.awt.*; // Importa clases de diseño y componentes AWT
import java.text.DecimalFormat; // Importa formateador de números decimales
import java.util.ArrayList; // Importa lista dinámica ArrayList
import java.util.Arrays; // Importa utilidades para manipular arrays
import java.util.List; // Importa interfaz List
import java.util.Random; // Importa generador de números aleatorios

/** // Inicio de comentario Javadoc
 * Simulador de Estimación de Costos con Generación Aleatoria // Descripción principal de la clase
 * Los valores Estimado, Min y Max se generan aleatoriamente con criterios realistas // Detalle del comportamiento del simulador
 */ // Fin de comentario Javadoc
public class SimuladorAleatorio extends JFrame { // Declaración de clase pública que hereda de JFrame para crear ventana

    private static class LineaPresupuesto { // Clase interna estática para representar cada línea del presupuesto
        String codigo; // Código identificador de la línea presupuestaria
        String descripcion; // Descripción textual del concepto presupuestario
        double estimado; // Valor estimado del costo
        double min; // Valor mínimo posible del costo
        double max; // Valor máximo posible del costo
        boolean esCategoria; // Indica si la línea es categoría (true) o subcategoría (false)

        LineaPresupuesto(String codigo, String desc, boolean cat) { // Constructor con tres parámetros
            this.codigo = codigo; // Asigna código a la instancia
            this.descripcion = desc; // Asigna descripción a la instancia
            this.esCategoria = cat; // Asigna indicador de categoría a la instancia
        } // Fin del constructor
    } // Fin de clase interna LineaPresupuesto

    private final List<LineaPresupuesto> lineas = new ArrayList<>(); // Lista que almacena todas las líneas del presupuesto
    private JTable tabla; // Componente tabla para mostrar datos del presupuesto
    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    private JLabel lblResultadoSimulacion; // Etiqueta para mostrar estado de simulación
    private JLabel lblPromedio; // Etiqueta para mostrar promedio de resultados
    private JLabel lblDesviacion; // Etiqueta para mostrar desviación estándar
    private JLabel lblMin; // Etiqueta para mostrar valor mínimo simulado
    private JLabel lblMax; // Etiqueta para mostrar valor máximo simulado
    private double[] resultadosSimulacion; // Array que almacena resultados de todas las iteraciones
    private Random random = new Random(); // Generador de números aleatorios

    public SimuladorAleatorio() { // Constructor público de la clase principal
        super("Simulador de Estimación de Costos - Generación Aleatoria"); // Llama constructor padre JFrame con título de ventana
        inicializarEstructura(); // Llama método para crear estructura de líneas presupuestarias
        generarValoresAleatorios(); // Llama método para asignar valores aleatorios a las líneas
        configurarUI(); // Llama método para configurar interfaz de usuario
        setSize(1400, 900); // Establece dimensiones de la ventana principal
        setLocationRelativeTo(null); // Centra la ventana en la pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Termina programa al cerrar ventana
    } // Fin del constructor

    private void inicializarEstructura() { // Método que crea la estructura completa de líneas del presupuesto
        // 11 - Big Co. PROJECT MANAGEMENT // Comentario descriptivo de la sección
        lineas.add(new LineaPresupuesto("11", "Big Co. PROYECT MANAGEMENT", false)); // Agrega línea de gestión del proyecto

        // Categoría 1 // Comentario de categoría administrativa
        lineas.add(new LineaPresupuesto("1", "ADMINSTRACION DEL PROYECTO", true)); // Agrega categoría 1 como línea total

        // Categoría 2 - Ingeniería // Comentario de categoría de ingeniería
        lineas.add(new LineaPresupuesto("21", "ENEGINEERING MANAGEMENT", false)); // Agrega línea de gestión de ingeniería
        lineas.add(new LineaPresupuesto("22", "TECHNICAL STUDIES", false)); // Agrega línea de estudios técnicos
        lineas.add(new LineaPresupuesto("23", "DEFINITIVE DESIGN", false)); // Agrega línea de diseño definitivo
        lineas.add(new LineaPresupuesto("24", "ENGINEERING INSPECTION", false)); // Agrega línea de inspección de ingeniería
        lineas.add(new LineaPresupuesto("25", "EQUIPMENT REMOVAL DESINGN", false)); // Agrega línea de diseño de remoción de equipos
        lineas.add(new LineaPresupuesto("2", "INGENIERIA", true)); // Agrega categoría 2 como línea total de ingeniería

        // Categoría 3 - CENRTC // Comentario de categoría CENRTC
        lineas.add(new LineaPresupuesto("31", "CENRTC DEFINITIVE DESIGN", false)); // Agrega línea de diseño definitivo CENRTC
        lineas.add(new LineaPresupuesto("32", "CENRTC PROCUREMENT", false)); // Agrega línea de adquisiciones CENRTC
        lineas.add(new LineaPresupuesto("33", "CENRTC FABRICATION", false)); // Agrega línea de fabricación CENRTC
        lineas.add(new LineaPresupuesto("3", "CENRTC", true)); // Agrega categoría 3 como línea total CENRTC

        // Categoría 4 - Construcción // Comentario de categoría de construcción
        lineas.add(new LineaPresupuesto("41", "WHC CONSTRUCTION MANAGEMENT", false)); // Agrega línea de gestión de construcción WHC
        lineas.add(new LineaPresupuesto("42", "INTER-FARM MODIFICATIONS", false)); // Agrega línea de modificaciones inter-granja
        lineas.add(new LineaPresupuesto("43", "C-FARM MODIFICATIONS", false)); // Agrega línea de modificaciones C-Farm
        lineas.add(new LineaPresupuesto("44", "AY-FARM MODIFICATIONS", false)); // Agrega línea de modificaciones AY-Farm
        lineas.add(new LineaPresupuesto("45", "EXPENSE PROCUREMENT", false)); // Agrega línea de adquisiciones de gastos
        lineas.add(new LineaPresupuesto("46", "FACILITY PREP", false)); // Agrega línea de preparación de instalaciones
        lineas.add(new LineaPresupuesto("47", "CONSTRUCTION SERVICES", false)); // Agrega línea de servicios de construcción
        lineas.add(new LineaPresupuesto("4", "CONSTRUCCION", true)); // Agrega categoría 4 como línea total de construcción

        // Categoría 5 - Otros costos // Comentario de categoría de otros costos
        lineas.add(new LineaPresupuesto("51", "STARTUP ADMINISTRATION", false)); // Agrega línea de administración de arranque
        lineas.add(new LineaPresupuesto("52", "STARTUP SUPPORT", false)); // Agrega línea de soporte de arranque
        lineas.add(new LineaPresupuesto("54", "STARTUP READINESS PREVIEW", false)); // Agrega línea de revisión de preparación
        lineas.add(new LineaPresupuesto("5", "OTROS COSTOS DEL PROYECTO", true)); // Agrega categoría 5 como línea total de otros costos

        // Categoría 6 - Seguridad // Comentario de categoría de seguridad y ambiente
        lineas.add(new LineaPresupuesto("61", "ENVIROMENTAL MANAGEMENT", false)); // Agrega línea de gestión ambiental
        lineas.add(new LineaPresupuesto("63", "SAFETY", false)); // Agrega línea de seguridad
        lineas.add(new LineaPresupuesto("64", "NEPA", false)); // Agrega línea NEPA (ley ambiental)
        lineas.add(new LineaPresupuesto("65", "RCRA", false)); // Agrega línea RCRA (ley de residuos)
        lineas.add(new LineaPresupuesto("66", "CAA", false)); // Agrega línea CAA (ley de aire limpio)
        lineas.add(new LineaPresupuesto("6", "SEGURIDAD & AMBIENTE", true)); // Agrega categoría 6 como línea total de seguridad
    } // Fin del método inicializarEstructura

    private void generarValoresAleatorios() { // Método que genera valores aleatorios para todas las líneas
        // Primero generar valores para todas las subcategorías // Comentario explicativo del primer paso
        for (LineaPresupuesto linea : lineas) { // Itera sobre todas las líneas del presupuesto
            if (!linea.esCategoria) { // Verifica si la línea NO es una categoría total
                // Generar valor estimado entre $100,000 y $12,000,000 // Comentario del rango de valores
                double baseMin = 100000; // Define valor base mínimo
                double baseMax = 12000000; // Define valor base máximo
                linea.estimado = Math.round((baseMin + random.nextDouble() * (baseMax - baseMin)) / 10000) * 10000; // Genera valor aleatorio redondeado a decenas de miles
                linea.min = 0; // Inicializa valor mínimo en cero (se calculará después para categorías)
                linea.max = 0; // Inicializa valor máximo en cero (se calculará después para categorías)
            } // Fin de verificación de subcategoría
        } // Fin de iteración sobre líneas

        // Ahora calcular los totales de las categorías // Comentario del segundo paso
        // Categoría 1: ADMINISTRACION DEL PROYECTO (suma de línea 11) // Comentario de categoría 1
        LineaPresupuesto cat1 = lineas.get(1); // Obtiene objeto de categoría 1 desde la lista
        cat1.estimado = lineas.get(0).estimado; // suma de Big Co. (índice 0) // Asigna valor estimado igual a línea 11
        calcularMinMax(cat1); // Calcula valores min y max para categoría 1

        // Categoría 2: INGENIERIA (suma de líneas 21-25) // Comentario de categoría 2
        LineaPresupuesto cat2 = lineas.get(7); // índice 7 es la categoría 2 // Obtiene objeto de categoría 2
        cat2.estimado = 0; // Inicializa estimado en cero para sumar
        for (int i = 2; i <= 6; i++) { // índices 2-6 son las subcategorías de ingeniería // Itera sobre subcategorías de ingeniería
            cat2.estimado += lineas.get(i).estimado; // Acumula valores estimados de subcategorías
        } // Fin de iteración de subcategorías de ingeniería
        calcularMinMax(cat2); // Calcula valores min y max para categoría 2

        // Categoría 3: CENRTC (suma de líneas 31-33) // Comentario de categoría 3
        LineaPresupuesto cat3 = lineas.get(11); // índice 11 es la categoría 3 // Obtiene objeto de categoría 3
        cat3.estimado = 0; // Inicializa estimado en cero para sumar
        for (int i = 8; i <= 10; i++) { // índices 8-10 son las subcategorías de CENRTC // Itera sobre subcategorías CENRTC
            cat3.estimado += lineas.get(i).estimado; // Acumula valores estimados de subcategorías
        } // Fin de iteración de subcategorías CENRTC
        calcularMinMax(cat3); // Calcula valores min y max para categoría 3

        // Categoría 4: CONSTRUCCION (suma de líneas 41-47) // Comentario de categoría 4
        LineaPresupuesto cat4 = lineas.get(19); // índice 19 es la categoría 4 // Obtiene objeto de categoría 4
        cat4.estimado = 0; // Inicializa estimado en cero para sumar
        for (int i = 12; i <= 18; i++) { // índices 12-18 son las subcategorías de construcción // Itera sobre subcategorías de construcción
            cat4.estimado += lineas.get(i).estimado; // Acumula valores estimados de subcategorías
        } // Fin de iteración de subcategorías de construcción
        calcularMinMax(cat4); // Calcula valores min y max para categoría 4

        // Categoría 5: OTROS COSTOS (suma de líneas 51, 52, 54) // Comentario de categoría 5
        LineaPresupuesto cat5 = lineas.get(23); // índice 23 es la categoría 5 // Obtiene objeto de categoría 5
        cat5.estimado = 0; // Inicializa estimado en cero para sumar
        for (int i = 20; i <= 22; i++) { // índices 20-22 son las subcategorías // Itera sobre subcategorías de otros costos
            cat5.estimado += lineas.get(i).estimado; // Acumula valores estimados de subcategorías
        } // Fin de iteración de subcategorías de otros costos
        calcularMinMax(cat5); // Calcula valores min y max para categoría 5

        // Categoría 6: SEGURIDAD & AMBIENTE (suma de líneas 61, 63-66) // Comentario de categoría 6
        LineaPresupuesto cat6 = lineas.get(29); // índice 29 es la categoría 6 // Obtiene objeto de categoría 6
        cat6.estimado = 0; // Inicializa estimado en cero para sumar
        for (int i = 24; i <= 28; i++) { // índices 24-28 son las subcategorías // Itera sobre subcategorías de seguridad
            cat6.estimado += lineas.get(i).estimado; // Acumula valores estimados de subcategorías
        } // Fin de iteración de subcategorías de seguridad
        calcularMinMax(cat6); // Calcula valores min y max para categoría 6
    } // Fin del método generarValoresAleatorios

    private void calcularMinMax(LineaPresupuesto categoria) { // Método que calcula valores mínimo y máximo para una categoría
        // Min: 85-95% del estimado // Comentario de rango para valor mínimo
        double factorMin = 0.85 + random.nextDouble() * 0.10; // Genera factor aleatorio entre 0.85 y 0.95
        categoria.min = Math.round(categoria.estimado * factorMin / 100000) * 100000; // Calcula y redondea valor mínimo a centenas de miles

        // Max: 110-130% del estimado // Comentario de rango para valor máximo
        double factorMax = 1.10 + random.nextDouble() * 0.20; // Genera factor aleatorio entre 1.10 y 1.30
        categoria.max = Math.round(categoria.estimado * factorMax / 100000) * 100000; // Calcula y redondea valor máximo a centenas de miles
    } // Fin del método calcularMinMax

    private void configurarUI() { // Método que configura todos los componentes de la interfaz de usuario
        setLayout(new BorderLayout(10, 10)); // Establece layout BorderLayout con espaciado de 10 píxeles

        String[] columnas = {"Código", "Descripción", "Estimado", "Min", "Max", "Simulado"}; // Define nombres de columnas de la tabla
        modeloTabla = new DefaultTableModel(columnas, 0) { // Crea modelo de tabla con columnas definidas y 0 filas iniciales
            @Override // Indica sobrescritura de método padre
            public boolean isCellEditable(int row, int col) { // Sobrescribe método para definir editabilidad de celdas
                return false; // Hace todas las celdas no editables
            } // Fin de método isCellEditable
        }; // Fin de clase anónima DefaultTableModel

        tabla = new JTable(modeloTabla); // Crea componente JTable con el modelo configurado
        configurarTabla(); // Llama método para configurar apariencia de la tabla
        llenarTabla(); // Llama método para poblar tabla con datos iniciales

        JScrollPane scrollTabla = new JScrollPane(tabla); // Crea panel con scroll para la tabla
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Proyecto de Estimación de Costos (Valores Aleatorios)")); // Establece borde con título

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Crea panel para botones con layout centrado

        JButton btnRegenerar = new JButton("Regenerar Valores Aleatorios"); // Crea botón para regenerar valores
        btnRegenerar.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente del botón
        btnRegenerar.setBackground(new Color(255, 140, 0)); // Establece color de fondo naranja
        btnRegenerar.setForeground(Color.WHITE); // Establece color de texto blanco
        btnRegenerar.setFocusPainted(false); // Desactiva borde de foco al hacer clic

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo"); // Crea botón para ejecutar simulación
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente del botón
        btnSimular.setBackground(new Color(30, 144, 255)); // Establece color de fondo azul
        btnSimular.setForeground(Color.WHITE); // Establece color de texto blanco
        btnSimular.setFocusPainted(false); // Desactiva borde de foco al hacer clic

        JSpinner spinnerIteraciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 10000, 1000)); // Crea spinner con rango 1000-10000, valor inicial 5000, paso 1000
        spinnerIteraciones.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Establece fuente del spinner

        panelBotones.add(btnRegenerar); // Agrega botón regenerar al panel
        panelBotones.add(new JLabel("Simulaciones:")); // Agrega etiqueta descriptiva al panel
        panelBotones.add(spinnerIteraciones); // Agrega spinner de iteraciones al panel
        panelBotones.add(btnSimular); // Agrega botón simular al panel

        JPanel panelEstadisticas = crearPanelEstadisticas(); // Crea panel de estadísticas mediante método auxiliar

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10)); // Crea panel central con layout BorderLayout
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Establece margen interno de 10 píxeles
        panelCentral.add(scrollTabla, BorderLayout.NORTH); // Agrega tabla en zona norte del panel
        panelCentral.add(panelBotones, BorderLayout.CENTER); // Agrega panel de botones en zona central
        panelCentral.add(panelEstadisticas, BorderLayout.SOUTH); // Agrega panel de estadísticas en zona sur

        add(panelCentral, BorderLayout.CENTER); // Agrega panel central a la ventana principal

        btnRegenerar.addActionListener(e -> { // Agrega listener al botón regenerar con expresión lambda
            generarValoresAleatorios(); // Regenera valores aleatorios
            actualizarTabla(); // Actualiza visualización de tabla
            lblResultadoSimulacion.setText("Pendiente"); // Restablece texto de estado a pendiente
            lblResultadoSimulacion.setForeground(Color.GRAY); // Cambia color de estado a gris
            lblPromedio.setText("--"); // Restablece texto de promedio
            lblDesviacion.setText("--"); // Restablece texto de desviación
            lblMin.setText("--"); // Restablece texto de mínimo
            lblMax.setText("--"); // Restablece texto de máximo
        }); // Fin de listener de botón regenerar

        btnSimular.addActionListener(e -> { // Agrega listener al botón simular con expresión lambda
            int iteraciones = (int) spinnerIteraciones.getValue(); // Obtiene número de iteraciones desde spinner
            ejecutarSimulacion(iteraciones); // Ejecuta simulación con número de iteraciones especificado
        }); // Fin de listener de botón simular
    } // Fin del método configurarUI

    private void configurarTabla() { // Método que configura apariencia y formato de la tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente de texto de la tabla
        tabla.setRowHeight(25); // Establece altura de filas en píxeles
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente de encabezados en negrita
        tabla.getTableHeader().setBackground(new Color(255, 153, 51)); // Establece color de fondo naranja de encabezados
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece color de texto blanco de encabezados

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado para formato de moneda
            DecimalFormat formato = new DecimalFormat("$#,##0"); // Define formato con símbolo de dólar y separadores de miles
            @Override // Indica sobrescritura de método padre
            protected void setValue(Object value) { // Sobrescribe método para establecer valor en celda
                if (value instanceof Number) { // Verifica si el valor es numérico
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea números a la derecha
                    setText(formato.format(value)); // Aplica formato de moneda al valor
                } else { // Si no es numérico
                    super.setValue(value); // Usa método padre para establecer valor
                } // Fin de verificación de tipo
            } // Fin de método setValue
        }; // Fin de clase anónima moneyRenderer

        DefaultTableCellRenderer categoryRenderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado para categorías
            @Override // Indica sobrescritura de método padre
            public Component getTableCellRendererComponent(JTable table, Object value, // Sobrescribe método de renderizado de celda
                    boolean isSelected, boolean hasFocus, int row, int column) { // Parámetros de estado de celda
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Obtiene componente renderizado por método padre

                String codigo = (String) table.getValueAt(row, 0); // Obtiene código de la fila actual
                boolean esCategoria = codigo.length() == 1 && !codigo.isEmpty(); // Determina si es categoría (código de 1 carácter)

                if (esCategoria) { // Si la línea es una categoría
                    setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente en negrita para categorías
                } else { // Si la línea es una subcategoría
                    setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente normal para subcategorías
                } // Fin de verificación de categoría

                return c; // Retorna componente configurado
            } // Fin de método getTableCellRendererComponent
        }; // Fin de clase anónima categoryRenderer

        tabla.getColumnModel().getColumn(0).setCellRenderer(categoryRenderer); // Aplica renderizador de categorías a columna 0 (Código)
        tabla.getColumnModel().getColumn(1).setCellRenderer(categoryRenderer); // Aplica renderizador de categorías a columna 1 (Descripción)

        for (int i = 2; i < 6; i++) { // Itera sobre columnas numéricas (2 a 5)
            tabla.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer); // Aplica renderizador de moneda a columnas numéricas
        } // Fin de iteración sobre columnas

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece ancho preferido de columna Código
        tabla.getColumnModel().getColumn(1).setPreferredWidth(350); // Establece ancho preferido de columna Descripción
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho preferido de columna Estimado
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Establece ancho preferido de columna Min
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Establece ancho preferido de columna Max
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120); // Establece ancho preferido de columna Simulado
    } // Fin del método configurarTabla

    private void llenarTabla() { // Método que puebla la tabla con datos de las líneas presupuestarias
        modeloTabla.setRowCount(0); // Elimina todas las filas existentes de la tabla

        for (LineaPresupuesto linea : lineas) { // Itera sobre todas las líneas del presupuesto
            Object[] fila = new Object[6]; // Crea array de objetos para una fila con 6 columnas
            fila[0] = linea.codigo; // Asigna código a columna 0
            fila[1] = linea.descripcion; // Asigna descripción a columna 1
            fila[2] = linea.estimado; // Asigna valor estimado a columna 2
            fila[3] = linea.min > 0 ? linea.min : ""; // Asigna valor mínimo a columna 3 si es mayor a 0, sino vacío
            fila[4] = linea.max > 0 ? linea.max : ""; // Asigna valor máximo a columna 4 si es mayor a 0, sino vacío
            fila[5] = linea.esCategoria ? linea.estimado : ""; // Asigna valor simulado a columna 5 si es categoría, sino vacío

            modeloTabla.addRow(fila); // Agrega fila completa al modelo de tabla
        } // Fin de iteración sobre líneas

        double totalEstimado = lineas.stream() // Inicia stream sobre líneas
            .filter(l -> l.esCategoria) // Filtra solo las líneas que son categorías
            .mapToDouble(l -> l.estimado) // Mapea a valores estimados de tipo double
            .sum(); // Calcula suma de todos los estimados de categorías

        double totalMin = lineas.stream() // Inicia stream sobre líneas
            .filter(l -> l.esCategoria) // Filtra solo las líneas que son categorías
            .mapToDouble(l -> l.min) // Mapea a valores mínimos de tipo double
            .sum(); // Calcula suma de todos los mínimos de categorías

        double totalMax = lineas.stream() // Inicia stream sobre líneas
            .filter(l -> l.esCategoria) // Filtra solo las líneas que son categorías
            .mapToDouble(l -> l.max) // Mapea a valores máximos de tipo double
            .sum(); // Calcula suma de todos los máximos de categorías

        Object[] filaTotal = new Object[6]; // Crea array para fila de totales
        filaTotal[0] = ""; // Columna código vacía para fila total
        filaTotal[1] = "TOTAL PROYECTO"; // Etiqueta de fila total
        filaTotal[2] = totalEstimado; // Total de valores estimados
        filaTotal[3] = totalMin; // Total de valores mínimos
        filaTotal[4] = totalMax; // Total de valores máximos
        filaTotal[5] = totalEstimado; // Total estimado en columna simulado
        modeloTabla.addRow(filaTotal); // Agrega fila de totales a la tabla

        Object[] filaConting = new Object[6]; // Crea array para fila de contingencia
        filaConting[0] = ""; // Columna código vacía
        filaConting[1] = "CONTINGENCIA"; // Etiqueta de contingencia
        filaConting[2] = "20%"; // Porcentaje de contingencia
        filaConting[3] = ""; // Columna min vacía
        filaConting[4] = ""; // Columna max vacía
        filaConting[5] = ""; // Columna simulado vacía
        modeloTabla.addRow(filaConting); // Agrega fila de contingencia a la tabla

        Object[] filaTotalConting = new Object[6]; // Crea array para fila de total con contingencia
        filaTotalConting[0] = ""; // Columna código vacía
        filaTotalConting[1] = "PROYECTO TOTAL CON CONTINGENCIA"; // Etiqueta de total con contingencia
        filaTotalConting[2] = totalEstimado * 1.20; // Calcula total con 20% de contingencia
        filaTotalConting[3] = ""; // Columna min vacía
        filaTotalConting[4] = ""; // Columna max vacía
        filaTotalConting[5] = ""; // Columna simulado vacía
        modeloTabla.addRow(filaTotalConting); // Agrega fila de total con contingencia a la tabla
    } // Fin del método llenarTabla

    private void actualizarTabla() { // Método que refresca la visualización de la tabla
        llenarTabla(); // Vuelve a llenar tabla con datos actualizados
        tabla.revalidate(); // Revalida la estructura de la tabla
        tabla.repaint(); // Repinta la tabla en pantalla
    } // Fin del método actualizarTabla

    private JPanel crearPanelEstadisticas() { // Método que crea panel para mostrar estadísticas de simulación
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10)); // Crea panel con grid de 2 filas y 4 columnas
        panel.setBorder(BorderFactory.createTitledBorder("Resultados de la Simulación")); // Establece borde con título
        panel.setBackground(Color.WHITE); // Establece fondo blanco del panel

        Font fuenteLabel = new Font("Segoe UI", Font.BOLD, 13); // Define fuente en negrita para etiquetas
        Font fuenteValor = new Font("Segoe UI", Font.PLAIN, 13); // Define fuente normal para valores

        lblResultadoSimulacion = new JLabel("Pendiente", SwingConstants.CENTER); // Crea etiqueta centrada con estado inicial
        lblPromedio = new JLabel("--", SwingConstants.CENTER); // Crea etiqueta centrada para promedio
        lblDesviacion = new JLabel("--", SwingConstants.CENTER); // Crea etiqueta centrada para desviación estándar
        lblMin = new JLabel("--", SwingConstants.CENTER); // Crea etiqueta centrada para mínimo
        lblMax = new JLabel("--", SwingConstants.CENTER); // Crea etiqueta centrada para máximo

        lblResultadoSimulacion.setFont(fuenteValor); // Aplica fuente a etiqueta de estado
        lblPromedio.setFont(fuenteValor); // Aplica fuente a etiqueta de promedio
        lblDesviacion.setFont(fuenteValor); // Aplica fuente a etiqueta de desviación
        lblMin.setFont(fuenteValor); // Aplica fuente a etiqueta de mínimo
        lblMax.setFont(fuenteValor); // Aplica fuente a etiqueta de máximo

        panel.add(crearEtiqueta("Estado:", fuenteLabel)); // Agrega etiqueta "Estado:" al panel
        panel.add(crearEtiqueta("Promedio:", fuenteLabel)); // Agrega etiqueta "Promedio:" al panel
        panel.add(crearEtiqueta("Desv. Est.:", fuenteLabel)); // Agrega etiqueta "Desv. Est.:" al panel
        panel.add(crearEtiqueta("Mín/Máx:", fuenteLabel)); // Agrega etiqueta "Mín/Máx:" al panel

        panel.add(lblResultadoSimulacion); // Agrega etiqueta de valor de estado
        panel.add(lblPromedio); // Agrega etiqueta de valor de promedio
        panel.add(lblDesviacion); // Agrega etiqueta de valor de desviación

        JPanel panelMinMax = new JPanel(new GridLayout(2, 1)); // Crea subpanel con grid de 2 filas y 1 columna
        panelMinMax.setBackground(Color.WHITE); // Establece fondo blanco del subpanel
        panelMinMax.add(lblMin); // Agrega etiqueta de mínimo al subpanel
        panelMinMax.add(lblMax); // Agrega etiqueta de máximo al subpanel
        panel.add(panelMinMax); // Agrega subpanel min/max al panel principal

        return panel; // Retorna panel de estadísticas configurado
    } // Fin del método crearPanelEstadisticas

    private JLabel crearEtiqueta(String texto, Font fuente) { // Método auxiliar para crear etiquetas con formato
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER); // Crea etiqueta centrada con texto especificado
        lbl.setFont(fuente); // Aplica fuente especificada a la etiqueta
        return lbl; // Retorna etiqueta configurada
    } // Fin del método crearEtiqueta

    private void ejecutarSimulacion(int iteraciones) { // Método que ejecuta simulación Monte Carlo
        lblResultadoSimulacion.setText("Simulando..."); // Actualiza texto de estado a "Simulando..."
        lblResultadoSimulacion.setForeground(Color.ORANGE); // Cambia color de estado a naranja

        SwingWorker<double[], Void> worker = new SwingWorker<>() { // Crea worker para ejecutar simulación en hilo separado
            @Override // Indica sobrescritura de método padre
            protected double[] doInBackground() { // Método que se ejecuta en segundo plano
                resultadosSimulacion = new double[iteraciones]; // Crea array para almacenar resultados de todas las iteraciones

                for (int i = 0; i < iteraciones; i++) { // Itera número especificado de veces
                    double totalSimulacion = 0; // Inicializa acumulador de total para esta iteración

                    for (LineaPresupuesto linea : lineas) { // Itera sobre todas las líneas del presupuesto
                        if (linea.esCategoria && linea.min > 0 && linea.max > 0) { // Verifica si es categoría con valores min y max válidos
                            TriangularDistribution dist = new TriangularDistribution( // Crea distribución triangular
                                linea.min, linea.estimado, linea.max); // Con parámetros mínimo, moda (estimado) y máximo
                            totalSimulacion += dist.sample(); // Genera muestra aleatoria y la suma al total
                        } // Fin de verificación de categoría válida
                    } // Fin de iteración sobre líneas

                    resultadosSimulacion[i] = totalSimulacion; // Almacena total de esta iteración en el array
                } // Fin de iteraciones de simulación

                return resultadosSimulacion; // Retorna array con todos los resultados
            } // Fin de método doInBackground

            @Override // Indica sobrescritura de método padre
            protected void done() { // Método que se ejecuta al terminar la simulación
                try { // Inicio de bloque try para manejo de excepciones
                    double[] resultados = get(); // Obtiene resultados de la simulación
                    actualizarResultados(resultados); // Actualiza estadísticas con resultados obtenidos
                    actualizarTablaConSimulacion(); // Actualiza tabla con valores simulados
                    mostrarHistograma(resultados, iteraciones); // Muestra histograma de resultados
                } catch (Exception ex) { // Captura cualquier excepción durante procesamiento
                    ex.printStackTrace(); // Imprime traza de error en consola
                    JOptionPane.showMessageDialog(SimuladorAleatorio.this, // Muestra diálogo de error
                        "Error en la simulación: " + ex.getMessage(), // Mensaje de error con detalles
                        "Error", JOptionPane.ERROR_MESSAGE); // Tipo de diálogo de error
                } // Fin de bloque catch
            } // Fin de método done
        }; // Fin de clase anónima SwingWorker

        worker.execute(); // Inicia ejecución del worker en hilo separado
    } // Fin del método ejecutarSimulacion

    private void actualizarResultados(double[] resultados) { // Método que calcula y actualiza estadísticas de resultados
        DecimalFormat df = new DecimalFormat("$#,##0"); // Crea formateador de moneda

        double suma = Arrays.stream(resultados).sum(); // Calcula suma de todos los resultados
        double promedio = suma / resultados.length; // Calcula promedio dividiendo suma entre número de iteraciones

        double varianza = Arrays.stream(resultados) // Inicia stream sobre resultados
            .map(x -> Math.pow(x - promedio, 2)) // Calcula cuadrado de diferencia con promedio para cada valor
            .sum() / resultados.length; // Suma todas las diferencias cuadradas y divide entre n para obtener varianza
        double desviacion = Math.sqrt(varianza); // Calcula desviación estándar como raíz cuadrada de varianza

        double min = Arrays.stream(resultados).min().orElse(0); // Obtiene valor mínimo de resultados, 0 si no existe
        double max = Arrays.stream(resultados).max().orElse(0); // Obtiene valor máximo de resultados, 0 si no existe

        lblResultadoSimulacion.setText("Completado"); // Actualiza texto de estado a "Completado"
        lblResultadoSimulacion.setForeground(new Color(0, 150, 0)); // Cambia color de estado a verde
        lblPromedio.setText(df.format(promedio)); // Actualiza etiqueta de promedio con formato de moneda
        lblDesviacion.setText(df.format(desviacion)); // Actualiza etiqueta de desviación con formato de moneda
        lblMin.setText("Mín: " + df.format(min)); // Actualiza etiqueta de mínimo con formato de moneda
        lblMax.setText("Máx: " + df.format(max)); // Actualiza etiqueta de máximo con formato de moneda
    } // Fin del método actualizarResultados

    private void actualizarTablaConSimulacion() { // Método que actualiza columna "Simulado" de la tabla
        int filaIdx = 0; // Inicializa índice de fila
        for (LineaPresupuesto linea : lineas) { // Itera sobre todas las líneas del presupuesto
            if (linea.esCategoria && linea.min > 0 && linea.max > 0) { // Verifica si es categoría con valores válidos
                TriangularDistribution dist = new TriangularDistribution( // Crea distribución triangular
                    linea.min, linea.estimado, linea.max); // Con parámetros mínimo, moda y máximo
                double valorSimulado = dist.sample(); // Genera una muestra aleatoria
                modeloTabla.setValueAt(valorSimulado, filaIdx, 5); // Actualiza valor en columna 5 (Simulado)
            } // Fin de verificación de categoría
            filaIdx++; // Incrementa índice de fila
        } // Fin de iteración sobre líneas

        if (resultadosSimulacion != null && resultadosSimulacion.length > 0) { // Verifica si existen resultados de simulación
            double promedioTotal = Arrays.stream(resultadosSimulacion).average().orElse(0); // Calcula promedio de todos los resultados
            modeloTabla.setValueAt(promedioTotal, lineas.size(), 5); // Actualiza fila TOTAL PROYECTO con promedio

            tabla.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() { // Establece renderizador personalizado para columna Simulado
                DecimalFormat formato = new DecimalFormat("$#,##0"); // Define formato de moneda
                @Override // Indica sobrescritura de método padre
                public Component getTableCellRendererComponent(JTable table, Object value, // Método de renderizado de celda
                        boolean isSelected, boolean hasFocus, int row, int column) { // Parámetros de estado de celda
                    Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Obtiene componente base

                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea contenido a la derecha

                    if (value instanceof Number) { // Si el valor es numérico
                        setText(formato.format(value)); // Aplica formato de moneda
                        if (!isSelected) { // Si la celda no está seleccionada
                            setBackground(new Color(144, 238, 144)); // Establece fondo verde claro
                        } // Fin de verificación de selección
                    } else { // Si el valor no es numérico
                        setText(""); // Establece texto vacío
                        if (!isSelected) { // Si la celda no está seleccionada
                            setBackground(Color.WHITE); // Establece fondo blanco
                        } // Fin de verificación de selección
                    } // Fin de verificación de tipo

                    return c; // Retorna componente configurado
                } // Fin de método getTableCellRendererComponent
            }); // Fin de clase anónima DefaultTableCellRenderer
        } // Fin de verificación de resultados
    } // Fin del método actualizarTablaConSimulacion

    private void mostrarHistograma(double[] datos, int numSimulaciones) { // Método que crea y muestra ventana con histograma
        HistogramDataset dataset = new HistogramDataset(); // Crea dataset para histograma
        dataset.addSeries("Total Proyecto", datos, 50); // Agrega serie de datos con 50 bins (barras)

        JFreeChart chart = ChartFactory.createHistogram( // Crea gráfico de histograma
            "TOTAL PROYECTO", // Título del gráfico
            "Costo Total ($)", // Etiqueta del eje X
            "Frecuencia", // Etiqueta del eje Y
            dataset, // Dataset con los datos
            PlotOrientation.VERTICAL, // Orientación vertical del gráfico
            false, // No mostrar leyenda
            true, // Mostrar tooltips
            false // No mostrar URLs
        ); // Fin de creación de gráfico

        XYPlot plot = chart.getXYPlot(); // Obtiene objeto plot del gráfico
        plot.setBackgroundPaint(new Color(255, 255, 204)); // Establece fondo amarillo claro del plot
        plot.setDomainGridlinePaint(Color.GRAY); // Establece color gris de líneas de cuadrícula verticales
        plot.setRangeGridlinePaint(Color.GRAY); // Establece color gris de líneas de cuadrícula horizontales

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtiene renderizador de barras del plot
        renderer.setSeriesPaint(0, new Color(0, 0, 255)); // Establece color azul para las barras
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter()); // Establece pintor estándar sin efectos 3D
        renderer.setShadowVisible(false); // Desactiva sombras de barras

        chart.setBackgroundPaint(Color.WHITE); // Establece fondo blanco del gráfico completo

        DecimalFormat df = new DecimalFormat("#,##0"); // Crea formateador numérico con separadores de miles
        double promedio = Arrays.stream(datos).average().orElse(0); // Calcula promedio de los datos
        chart.addSubtitle(new org.jfree.chart.title.TextTitle( // Agrega subtítulo al gráfico
            String.format("%d pruebas | Certeza: 100.00%% | Promedio: $%s", // Formato de texto con estadísticas
                numSimulaciones, df.format(promedio)), // Número de simulaciones y promedio formateado
            new Font("Segoe UI", Font.PLAIN, 11) // Fuente del subtítulo
        )); // Fin de adición de subtítulo

        JFrame frameHistograma = new JFrame("Vista de Frecuencia - TOTAL PROYECTO"); // Crea nueva ventana para histograma
        ChartPanel chartPanel = new ChartPanel(chart); // Crea panel con el gráfico
        chartPanel.setPreferredSize(new Dimension(900, 600)); // Establece dimensión preferida del panel

        frameHistograma.setContentPane(chartPanel); // Establece panel como contenido de la ventana
        frameHistograma.pack(); // Ajusta tamaño de ventana al contenido
        frameHistograma.setLocationRelativeTo(this); // Centra ventana respecto a ventana principal
        frameHistograma.setVisible(true); // Hace visible la ventana del histograma
    } // Fin del método mostrarHistograma

    public static void main(String[] args) { // Método principal de entrada del programa
        try { // Inicio de bloque try para manejo de excepciones
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establece tema FlatLaf Light para toda la aplicación
        } catch (Exception e) { // Captura excepción si falla establecer look and feel
            e.printStackTrace(); // Imprime traza de error en consola
        } // Fin de bloque catch

        SwingUtilities.invokeLater(() -> { // Ejecuta código en el hilo de eventos de Swing
            SimuladorAleatorio simulador = new SimuladorAleatorio(); // Crea instancia del simulador
            simulador.setVisible(true); // Hace visible la ventana principal
        }); // Fin de invokeLater
    } // Fin del método main
} // Fin de la clase SimuladorAleatorio