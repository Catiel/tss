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

/** // Inicio de comentario Javadoc
 * Simulador de Estimación de Costos con Ingreso Manual // Descripción principal de la clase
 * Permite editar subcategorías y Min/Max de categorías // Detalle de funcionalidad editable
 * Las categorías se calculan automáticamente // Detalle de cálculo automático
 */ // Fin de comentario Javadoc
public class SimuladorManual extends JFrame { // Declaración de clase pública que hereda de JFrame para crear ventana

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
            this.estimado = 0; // Inicializa valor estimado en cero
            this.min = 0; // Inicializa valor mínimo en cero
            this.max = 0; // Inicializa valor máximo en cero
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
    private boolean actualizandoAutomaticamente = false; // Bandera para evitar cálculos recursivos al actualizar tabla

    // Índices de las filas de totales // Comentario de sección de índices
    private int filaIndexTotalProyecto; // Índice de fila para total del proyecto
    private int filaIndexContingencia; // Índice de fila para contingencia
    private int filaIndexTotalConContingencia; // Índice de fila para total con contingencia
    private final double PORCENTAJE_CONTINGENCIA = 0.20; // 20% // Constante para porcentaje de contingencia

    public SimuladorManual() { // Constructor público de la clase principal
        super("Simulador de Estimación de Costos - Ingreso Manual"); // Llama constructor padre JFrame con título de ventana
        inicializarEstructura(); // Llama método para crear estructura de líneas presupuestarias
        configurarUI(); // Llama método para configurar interfaz de usuario
        setSize(1500, 950); // Establece dimensiones de la ventana principal
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

        // Guardar índices de filas de totales // Comentario explicativo de asignación de índices
        filaIndexTotalProyecto = lineas.size(); // Asigna índice para fila de total proyecto
        filaIndexContingencia = lineas.size() + 1; // Asigna índice para fila de contingencia
        filaIndexTotalConContingencia = lineas.size() + 2; // Asigna índice para fila de total con contingencia
    } // Fin del método inicializarEstructura

    private void configurarUI() { // Método que configura todos los componentes de la interfaz de usuario
        setLayout(new BorderLayout(10, 10)); // Establece layout BorderLayout con espaciado de 10 píxeles

        String[] columnas = {"Código", "Descripción", "Estimado", "Min", "Max", "Simulado"}; // Define nombres de columnas de la tabla
        modeloTabla = new DefaultTableModel(columnas, 0) { // Crea modelo de tabla con columnas definidas y 0 filas iniciales
            @Override // Indica sobrescritura de método padre
            public boolean isCellEditable(int row, int col) { // Sobrescribe método para definir editabilidad de celdas
                if (row >= lineas.size()) return false; // Filas de totales no editables // Si la fila es de totales, no es editable

                LineaPresupuesto linea = lineas.get(row); // Obtiene línea correspondiente a la fila

                // Subcategorías: solo columna Estimado (columna 2) // Comentario de regla para subcategorías
                if (!linea.esCategoria) { // Si la línea NO es categoría
                    return col == 2; // Solo columna Estimado es editable
                } // Fin de verificación de subcategoría

                // Categorías: solo columnas Min y Max (columnas 3 y 4) // Comentario de regla para categorías
                return col == 3 || col == 4; // Solo columnas Min y Max son editables
            } // Fin de método isCellEditable

            @Override // Indica sobrescritura de método padre
            public Class<?> getColumnClass(int columnIndex) { // Sobrescribe método para definir tipo de datos de columnas
                if (columnIndex >= 2 && columnIndex <= 5) { // Si es columna numérica (2 a 5)
                    return Double.class; // Retorna tipo Double
                } // Fin de verificación de columna numérica
                return String.class; // Retorna tipo String para columnas de texto
            } // Fin de método getColumnClass
        }; // Fin de clase anónima DefaultTableModel

        tabla = new JTable(modeloTabla); // Crea componente JTable con el modelo configurado
        configurarTabla(); // Llama método para configurar apariencia de la tabla
        llenarTabla(); // Llama método para poblar tabla con datos iniciales

        JScrollPane scrollTabla = new JScrollPane(tabla); // Crea panel con scroll para la tabla
        scrollTabla.setBorder(BorderFactory.createTitledBorder( // Establece borde con título explicativo
            "Proyecto de Estimación de Costos - Edite subcategorías (Estimado) y categorías (Min/Max)")); // Texto del borde con instrucciones

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10)); // Crea panel para botones con layout centrado

        JButton btnCargarEjemplo = new JButton("Cargar Datos de Ejemplo"); // Crea botón para cargar datos de ejemplo
        btnCargarEjemplo.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente del botón
        btnCargarEjemplo.setBackground(new Color(34, 139, 34)); // Establece color de fondo verde
        btnCargarEjemplo.setForeground(Color.WHITE); // Establece color de texto blanco
        btnCargarEjemplo.setFocusPainted(false); // Desactiva borde de foco al hacer clic

        JButton btnLimpiar = new JButton("Limpiar Todo"); // Crea botón para limpiar todos los datos
        btnLimpiar.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente del botón
        btnLimpiar.setBackground(new Color(220, 53, 69)); // Establece color de fondo rojo
        btnLimpiar.setForeground(Color.WHITE); // Establece color de texto blanco
        btnLimpiar.setFocusPainted(false); // Desactiva borde de foco al hacer clic

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo"); // Crea botón para ejecutar simulación
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establece fuente del botón
        btnSimular.setBackground(new Color(30, 144, 255)); // Establece color de fondo azul
        btnSimular.setForeground(Color.WHITE); // Establece color de texto blanco
        btnSimular.setFocusPainted(false); // Desactiva borde de foco al hacer clic

        JSpinner spinnerIteraciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 10000, 1000)); // Crea spinner con rango 1000-10000, valor inicial 5000, paso 1000
        spinnerIteraciones.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Establece fuente del spinner

        panelBotones.add(btnCargarEjemplo); // Agrega botón cargar ejemplo al panel
        panelBotones.add(btnLimpiar); // Agrega botón limpiar al panel
        panelBotones.add(new JLabel(" | Simulaciones:")); // Agrega separador y etiqueta descriptiva al panel
        panelBotones.add(spinnerIteraciones); // Agrega spinner de iteraciones al panel
        panelBotones.add(btnSimular); // Agrega botón simular al panel

        JPanel panelEstadisticas = crearPanelEstadisticas(); // Crea panel de estadísticas mediante método auxiliar

        JPanel panelCentral = new JPanel(new BorderLayout(10, 10)); // Crea panel central con layout BorderLayout
        panelCentral.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Establece margen interno de 10 píxeles
        panelCentral.add(scrollTabla, BorderLayout.CENTER); // Agrega tabla en zona central del panel
        panelCentral.add(panelBotones, BorderLayout.SOUTH); // Agrega panel de botones en zona sur

        add(panelCentral, BorderLayout.CENTER); // Agrega panel central a la ventana principal
        add(panelEstadisticas, BorderLayout.SOUTH); // Agrega panel de estadísticas en zona sur

        btnCargarEjemplo.addActionListener(e -> { // Agrega listener al botón cargar ejemplo con expresión lambda
            cargarDatosEjemplo(); // Carga datos predefinidos de ejemplo
            calcularSumas(); // Recalcula totales de categorías
        }); // Fin de listener de botón cargar ejemplo

        btnLimpiar.addActionListener(e -> { // Agrega listener al botón limpiar con expresión lambda
            limpiarDatos(); // Limpia todos los datos de la tabla
            calcularSumas(); // Recalcula totales (todos en cero)
        }); // Fin de listener de botón limpiar

        btnSimular.addActionListener(e -> { // Agrega listener al botón simular con expresión lambda
            if (validarDatos()) { // Valida que los datos ingresados sean correctos
                sincronizarDatosTabla(); // Sincroniza datos de tabla con objetos LineaPresupuesto
                int iteraciones = (int) spinnerIteraciones.getValue(); // Obtiene número de iteraciones desde spinner
                ejecutarSimulacion(iteraciones); // Ejecuta simulación con número de iteraciones especificado
            } // Fin de validación de datos
        }); // Fin de listener de botón simular

        modeloTabla.addTableModelListener(e -> { // Agrega listener a cambios en el modelo de tabla
            if (!actualizandoAutomaticamente) { // Verifica que no se esté actualizando automáticamente para evitar recursión
                SwingUtilities.invokeLater(() -> { // Ejecuta en el hilo de eventos de Swing
                    calcularSumas(); // Recalcula sumas cuando usuario modifica datos
                }); // Fin de invokeLater
            } // Fin de verificación de actualización automática
        }); // Fin de listener de modelo de tabla
    } // Fin del método configurarUI

    private void calcularSumas() { // Método que calcula automáticamente los totales de cada categoría
        actualizandoAutomaticamente = true; // Activa bandera para evitar recursión en listeners

        // Categoría 1: suma de Big Co. (índice 0) // Comentario de cálculo de categoría 1
        double suma1 = getValorTabla(0, 2); // Obtiene valor estimado de línea 11
        modeloTabla.setValueAt(suma1, 1, 2); // Establece suma en categoría 1

        // Categoría 2: suma de índices 2-6 // Comentario de cálculo de categoría 2
        double suma2 = 0; // Inicializa acumulador en cero
        for (int i = 2; i <= 6; i++) { // Itera sobre líneas de ingeniería
            suma2 += getValorTabla(i, 2); // Acumula valores estimados
        } // Fin de iteración de ingeniería
        modeloTabla.setValueAt(suma2, 7, 2); // Establece suma en categoría 2

        // Categoría 3: suma de índices 8-10 // Comentario de cálculo de categoría 3
        double suma3 = 0; // Inicializa acumulador en cero
        for (int i = 8; i <= 10; i++) { // Itera sobre líneas CENRTC
            suma3 += getValorTabla(i, 2); // Acumula valores estimados
        } // Fin de iteración de CENRTC
        modeloTabla.setValueAt(suma3, 11, 2); // Establece suma en categoría 3

        // Categoría 4: suma de índices 12-18 // Comentario de cálculo de categoría 4
        double suma4 = 0; // Inicializa acumulador en cero
        for (int i = 12; i <= 18; i++) { // Itera sobre líneas de construcción
            suma4 += getValorTabla(i, 2); // Acumula valores estimados
        } // Fin de iteración de construcción
        modeloTabla.setValueAt(suma4, 19, 2); // Establece suma en categoría 4

        // Categoría 5: suma de índices 20-22 // Comentario de cálculo de categoría 5
        double suma5 = 0; // Inicializa acumulador en cero
        for (int i = 20; i <= 22; i++) { // Itera sobre líneas de otros costos
            suma5 += getValorTabla(i, 2); // Acumula valores estimados
        } // Fin de iteración de otros costos
        modeloTabla.setValueAt(suma5, 23, 2); // Establece suma en categoría 5

        // Categoría 6: suma de índices 24-28 // Comentario de cálculo de categoría 6
        double suma6 = 0; // Inicializa acumulador en cero
        for (int i = 24; i <= 28; i++) { // Itera sobre líneas de seguridad
            suma6 += getValorTabla(i, 2); // Acumula valores estimados
        } // Fin de iteración de seguridad
        modeloTabla.setValueAt(suma6, 29, 2); // Establece suma en categoría 6

        // Calcular totales del proyecto // Comentario de cálculo de totales generales
        actualizarFilasTotales(); // Llama método para actualizar filas de totales del proyecto

        actualizandoAutomaticamente = false; // Desactiva bandera de actualización automática
    } // Fin del método calcularSumas

    private void actualizarFilasTotales() { // Método que actualiza las filas de totales del proyecto
        // Calcular TOTAL PROYECTO (suma de todas las categorías) // Comentario de cálculo de total
        double totalEstimado = 0; // Inicializa acumulador de estimados
        double totalMin = 0; // Inicializa acumulador de mínimos
        double totalMax = 0; // Inicializa acumulador de máximos

        for (int i = 0; i < lineas.size(); i++) { // Itera sobre todas las líneas
            if (lineas.get(i).esCategoria) { // Si la línea es una categoría
                totalEstimado += getValorTabla(i, 2); // Acumula valor estimado
                totalMin += getValorTabla(i, 3); // Acumula valor mínimo
                totalMax += getValorTabla(i, 4); // Acumula valor máximo
            } // Fin de verificación de categoría
        } // Fin de iteración sobre líneas

        // Actualizar fila TOTAL PROYECTO // Comentario de actualización de fila total
        modeloTabla.setValueAt(totalEstimado, filaIndexTotalProyecto, 2); // Establece total estimado
        modeloTabla.setValueAt(totalMin, filaIndexTotalProyecto, 3); // Establece total mínimo
        modeloTabla.setValueAt(totalMax, filaIndexTotalProyecto, 4); // Establece total máximo

        // Actualizar fila CONTINGENCIA (mostrar como texto el porcentaje) // Comentario de actualización de contingencia
        modeloTabla.setValueAt("20%", filaIndexContingencia, 2); // Establece texto de porcentaje de contingencia

        // Calcular TOTAL CON CONTINGENCIA // Comentario de cálculo de total con contingencia
        double totalConContingencia = totalEstimado * (1 + PORCENTAJE_CONTINGENCIA); // Multiplica total por 1.20
        modeloTabla.setValueAt(totalConContingencia, filaIndexTotalConContingencia, 2); // Establece total con contingencia
    } // Fin del método actualizarFilasTotales

    private double getValorTabla(int fila, int columna) { // Método auxiliar que obtiene valor numérico de una celda
        try { // Inicio de bloque try para manejo de excepciones
            Object val = modeloTabla.getValueAt(fila, columna); // Obtiene valor de celda especificada
            if (val instanceof Number) { // Si el valor es numérico
                return ((Number) val).doubleValue(); // Convierte a double y retorna
            } // Fin de verificación de tipo Number
        } catch (Exception e) { // Captura cualquier excepción
            // Ignorar // Comentario de manejo silencioso de error
        } // Fin de bloque catch
        return 0.0; // Retorna cero si hay error o valor no es numérico
    } // Fin del método getValorTabla

    private void configurarTabla() { // Método que configura apariencia y formato de la tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente de texto de la tabla
        tabla.setRowHeight(25); // Establece altura de filas en píxeles
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establece fuente de encabezados en negrita
        tabla.getTableHeader().setBackground(new Color(255, 153, 51)); // Establece color de fondo naranja de encabezados
        tabla.getTableHeader().setForeground(Color.WHITE); // Establece color de texto blanco de encabezados

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Crea renderizador personalizado de celdas
            DecimalFormat formato = new DecimalFormat("$#,##0"); // Define formato con símbolo de dólar y separadores de miles

            @Override // Indica sobrescritura de método padre
            public Component getTableCellRendererComponent(JTable table, Object value, // Método de renderizado de celda
                    boolean isSelected, boolean hasFocus, int row, int column) { // Parámetros de estado de celda
                Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Obtiene componente base

                boolean esCategoria = false; // Inicializa bandera de categoría
                boolean esFilaTotal = false; // Inicializa bandera de fila total

                if (row < lineas.size()) { // Si la fila corresponde a una línea del presupuesto
                    esCategoria = lineas.get(row).esCategoria; // Obtiene indicador de categoría
                } else if (row == filaIndexTotalProyecto || row == filaIndexTotalConContingencia) { // Si es fila de total proyecto o total con contingencia
                    esFilaTotal = true; // Marca como fila total
                } else if (row == filaIndexContingencia) { // Si es fila de contingencia
                    // Fila de contingencia // Comentario de identificación de fila
                    setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente en negrita
                    if (!isSelected) { // Si no está seleccionada
                        comp.setBackground(new Color(255, 255, 153)); // Amarillo claro // Establece fondo amarillo claro
                    } // Fin de verificación de selección
                    setHorizontalAlignment(column >= 2 ? SwingConstants.RIGHT : SwingConstants.LEFT); // Alinea según columna
                    return comp; // Retorna componente configurado
                } // Fin de verificación de tipo de fila

                if (esCategoria || esFilaTotal) { // Si es categoría o fila total
                    setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establece fuente en negrita
                    if (!isSelected) { // Si no está seleccionada
                        if (esFilaTotal) { // Si es fila total
                            comp.setBackground(new Color(255, 255, 153)); // Amarillo claro para totales // Establece fondo amarillo
                        } else { // Si es categoría
                            comp.setBackground(new Color(144, 238, 144)); // Verde claro para categorías // Establece fondo verde
                        } // Fin de verificación de tipo
                    } // Fin de verificación de selección
                } else { // Si es subcategoría
                    setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establece fuente normal
                    if (!isSelected) { // Si no está seleccionada
                        comp.setBackground(Color.WHITE); // Establece fondo blanco
                    } // Fin de verificación de selección
                } // Fin de verificación de tipo de línea

                if (column >= 2 && value instanceof Number) { // Si es columna numérica y valor es Number
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    double val = ((Number) value).doubleValue(); // Convierte a double
                    if (val == 0) { // Si el valor es cero
                        setText(""); // Muestra celda vacía
                    } else { // Si el valor no es cero
                        setText(formato.format(val)); // Aplica formato de moneda
                    } // Fin de verificación de valor cero
                } else if (column >= 2 && value instanceof String) { // Si es columna numérica y valor es String
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText((String) value); // Muestra texto tal cual
                } else if (column >= 2) { // Si es columna numérica sin valor específico
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                } else { // Si es columna de texto
                    setHorizontalAlignment(SwingConstants.LEFT); // Alinea a la izquierda
                } // Fin de verificaciones de columna

                return comp; // Retorna componente configurado
            } // Fin de método getTableCellRendererComponent
        }; // Fin de clase anónima DefaultTableCellRenderer

        for (int i = 0; i < 6; i++) { // Itera sobre todas las columnas
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplica renderizador personalizado a cada columna
        } // Fin de iteración sobre columnas

        for (int i = 2; i <= 4; i++) { // Itera sobre columnas editables numéricas
            tabla.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JTextField()) { // Establece editor personalizado
                @Override // Indica sobrescritura de método padre
                public Object getCellEditorValue() { // Método que obtiene valor editado
                    String text = ((JTextField) getComponent()).getText(); // Obtiene texto ingresado
                    text = text.replace("$", "").replace(",", "").trim(); // Elimina símbolos de formato y espacios
                    try { // Inicio de bloque try
                        return text.isEmpty() ? 0.0 : Double.parseDouble(text); // Convierte a double o retorna cero si vacío
                    } catch (NumberFormatException e) { // Captura error de formato
                        return 0.0; // Retorna cero si hay error
                    } // Fin de bloque catch
                } // Fin de método getCellEditorValue
            }); // Fin de clase anónima DefaultCellEditor
        } // Fin de iteración sobre columnas editables

        tabla.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece ancho preferido de columna Código
        tabla.getColumnModel().getColumn(1).setPreferredWidth(350); // Establece ancho preferido de columna Descripción
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Establece ancho preferido de columna Estimado
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Establece ancho preferido de columna Min
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Establece ancho preferido de columna Max
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120); // Establece ancho preferido de columna Simulado
    } // Fin del método configurarTabla

    private void llenarTabla() { // Método que puebla la tabla con datos iniciales
        modeloTabla.setRowCount(0); // Elimina todas las filas existentes de la tabla

        // Agregar líneas de presupuesto // Comentario de sección de líneas
        for (LineaPresupuesto linea : lineas) { // Itera sobre todas las líneas del presupuesto
            Object[] fila = new Object[6]; // Crea array de objetos para una fila con 6 columnas
            fila[0] = linea.codigo; // Asigna código a columna 0
            fila[1] = linea.descripcion; // Asigna descripción a columna 1
            fila[2] = 0.0; // Inicializa columna Estimado en cero
            fila[3] = 0.0; // Inicializa columna Min en cero
            fila[4] = 0.0; // Inicializa columna Max en cero
            fila[5] = 0.0; // Inicializa columna Simulado en cero

            modeloTabla.addRow(fila); // Agrega fila completa al modelo de tabla
        } // Fin de iteración sobre líneas

        // Agregar fila de TOTAL PROYECTO // Comentario de fila de total
        Object[] filaTotal = new Object[6]; // Crea array para fila de totales
        filaTotal[0] = ""; // Columna código vacía
        filaTotal[1] = "TOTAL PROYECTO"; // Etiqueta de fila total
        filaTotal[2] = 0.0; // Inicializa total estimado en cero
        filaTotal[3] = 0.0; // Inicializa total min en cero
        filaTotal[4] = 0.0; // Inicializa total max en cero
        filaTotal[5] = 0.0; // Inicializa total simulado en cero
        modeloTabla.addRow(filaTotal); // Agrega fila de totales a la tabla

        // Agregar fila de CONTINGENCIA // Comentario de fila de contingencia
        Object[] filaConting = new Object[6]; // Crea array para fila de contingencia
        filaConting[0] = ""; // Columna código vacía
        filaConting[1] = "CONTINGENCIA"; // Etiqueta de contingencia
        filaConting[2] = "20%"; // Porcentaje de contingencia
        filaConting[3] = ""; // Columna min vacía
        filaConting[4] = ""; // Columna max vacía
        filaConting[5] = ""; // Columna simulado vacía
        modeloTabla.addRow(filaConting); // Agrega fila de contingencia a la tabla

        // Agregar fila de TOTAL CON CONTINGENCIA // Comentario de fila de total con contingencia
        Object[] filaTotalConting = new Object[6]; // Crea array para fila de total con contingencia
        filaTotalConting[0] = ""; // Columna código vacía
        filaTotalConting[1] = "PROYECTO TOTAL CON CONTINGENCIA"; // Etiqueta de total con contingencia
        filaTotalConting[2] = 0.0; // Inicializa total con contingencia en cero
        filaTotalConting[3] = ""; // Columna min vacía
        filaTotalConting[4] = ""; // Columna max vacía
        filaTotalConting[5] = ""; // Columna simulado vacía
        modeloTabla.addRow(filaTotalConting); // Agrega fila de total con contingencia a la tabla
    } // Fin del método llenarTabla

    private JPanel crearPanelEstadisticas() { // Método que crea panel para mostrar estadísticas de simulación
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10)); // Crea panel con grid de 2 filas y 4 columnas
        panel.setBorder(BorderFactory.createTitledBorder("Resultados de la Simulación Monte Carlo")); // Establece borde con título
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

    private void cargarDatosEjemplo() { // Método que carga datos predefinidos de ejemplo en la tabla
        actualizandoAutomaticamente = true; // Activa bandera para evitar cálculos durante carga masiva

        // Subcategorías con valores // Comentario de carga de subcategorías
        modeloTabla.setValueAt(4719278.0, 0, 2); // Big Co // Establece valor de Big Co.

        modeloTabla.setValueAt(1344586.0, 2, 2); // Establece valor de Engineering Management
        modeloTabla.setValueAt(479725.0, 3, 2); // Establece valor de Technical Studies
        modeloTabla.setValueAt(10575071.0, 4, 2); // Establece valor de Definitive Design
        modeloTabla.setValueAt(5007916.0, 5, 2); // Establece valor de Engineering Inspection
        modeloTabla.setValueAt(2561272.0, 6, 2); // Establece valor de Equipment Removal Design

        modeloTabla.setValueAt(668990.0, 8, 2); // Establece valor de CENRTC Definitive Design
        modeloTabla.setValueAt(632731.0, 9, 2); // Establece valor de CENRTC Procurement
        modeloTabla.setValueAt(902498.0, 10, 2); // Establece valor de CENRTC Fabrication

        modeloTabla.setValueAt(4976687.0, 12, 2); // Establece valor de WHC Construction Management
        modeloTabla.setValueAt(1307065.0, 13, 2); // Establece valor de Inter-Farm Modifications
        modeloTabla.setValueAt(6602884.0, 14, 2); // Establece valor de C-Farm Modifications
        modeloTabla.setValueAt(1636429.0, 15, 2); // Establece valor de AY-Farm Modifications
        modeloTabla.setValueAt(4054629.0, 16, 2); // Establece valor de Expense Procurement
        modeloTabla.setValueAt(9536166.0, 17, 2); // Establece valor de Facility Prep
        modeloTabla.setValueAt(7041973.0, 18, 2); // Establece valor de Construction Services

        modeloTabla.setValueAt(1676355.0, 20, 2); // Establece valor de Startup Administration
        modeloTabla.setValueAt(1944661.0, 21, 2); // Establece valor de Startup Support
        modeloTabla.setValueAt(1042521.0, 22, 2); // Establece valor de Startup Readiness Preview

        modeloTabla.setValueAt(424013.0, 24, 2); // Establece valor de Environmental Management
        modeloTabla.setValueAt(3579477.0, 25, 2); // Establece valor de Safety
        modeloTabla.setValueAt(64106.0, 26, 2); // Establece valor de NEPA
        modeloTabla.setValueAt(11474.0, 27, 2); // Establece valor de RCRA
        modeloTabla.setValueAt(176869.0, 28, 2); // Establece valor de CAA

        // Min y Max de categorías // Comentario de carga de límites de categorías
        modeloTabla.setValueAt(4500000.0, 1, 3); // Establece mínimo de categoría Administración
        modeloTabla.setValueAt(5500000.0, 1, 4); // Establece máximo de categoría Administración

        modeloTabla.setValueAt(19000000.0, 7, 3); // Establece mínimo de categoría Ingeniería
        modeloTabla.setValueAt(22000000.0, 7, 4); // Establece máximo de categoría Ingeniería

        modeloTabla.setValueAt(2000000.0, 11, 3); // Establece mínimo de categoría CENRTC
        modeloTabla.setValueAt(2500000.0, 11, 4); // Establece máximo de categoría CENRTC

        modeloTabla.setValueAt(34000000.0, 19, 3); // Establece mínimo de categoría Construcción
        modeloTabla.setValueAt(45000000.0, 19, 4); // Establece máximo de categoría Construcción

        modeloTabla.setValueAt(4000000.0, 23, 3); // Establece mínimo de categoría Otros Costos
        modeloTabla.setValueAt(5500000.0, 23, 4); // Establece máximo de categoría Otros Costos

        modeloTabla.setValueAt(4000000.0, 29, 3); // Establece mínimo de categoría Seguridad
        modeloTabla.setValueAt(5000000.0, 29, 4); // Establece máximo de categoría Seguridad

        actualizandoAutomaticamente = false; // Desactiva bandera de actualización automática
    } // Fin del método cargarDatosEjemplo

    private void limpiarDatos() { // Método que elimina todos los datos ingresados en la tabla
        actualizandoAutomaticamente = true; // Activa bandera para evitar cálculos durante limpieza
        for (int i = 0; i < lineas.size(); i++) { // Itera sobre todas las líneas del presupuesto
            modeloTabla.setValueAt(0.0, i, 2); // Restablece columna Estimado a cero
            modeloTabla.setValueAt(0.0, i, 3); // Restablece columna Min a cero
            modeloTabla.setValueAt(0.0, i, 4); // Restablece columna Max a cero
            modeloTabla.setValueAt(0.0, i, 5); // Restablece columna Simulado a cero
        } // Fin de iteración sobre líneas

        // Limpiar filas de totales // Comentario de limpieza de totales
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 2); // Restablece total estimado a cero
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 3); // Restablece total min a cero
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 4); // Restablece total max a cero
        modeloTabla.setValueAt(0.0, filaIndexTotalProyecto, 5); // Restablece total simulado a cero

        modeloTabla.setValueAt("20%", filaIndexContingencia, 2); // Restablece texto de contingencia

        modeloTabla.setValueAt(0.0, filaIndexTotalConContingencia, 2); // Restablece total con contingencia a cero
        modeloTabla.setValueAt(0.0, filaIndexTotalConContingencia, 5); // Restablece simulado con contingencia a cero

        actualizandoAutomaticamente = false; // Desactiva bandera de actualización automática

        lblResultadoSimulacion.setText("Pendiente"); // Restablece texto de estado a pendiente
        lblResultadoSimulacion.setForeground(Color.GRAY); // Cambia color de estado a gris
        lblPromedio.setText("--"); // Restablece texto de promedio
        lblDesviacion.setText("--"); // Restablece texto de desviación
        lblMin.setText("--"); // Restablece texto de mínimo
        lblMax.setText("--"); // Restablece texto de máximo
    } // Fin del método limpiarDatos

    private boolean validarDatos() { // Método que valida la consistencia de los datos ingresados
        boolean hayDatos = false; // Inicializa bandera de existencia de datos
        for (int i = 0; i < lineas.size(); i++) { // Itera sobre todas las líneas
            if (lineas.get(i).esCategoria) { // Si la línea es una categoría
                double est = getValorTabla(i, 2); // Obtiene valor estimado
                double min = getValorTabla(i, 3); // Obtiene valor mínimo
                double max = getValorTabla(i, 4); // Obtiene valor máximo

                if (est > 0 && min > 0 && max > 0) { // Si todos los valores son positivos
                    hayDatos = true; // Marca que hay datos válidos

                    if (min > est || est > max) { // Verifica relación Min ≤ Estimado ≤ Max
                        JOptionPane.showMessageDialog(this, // Muestra diálogo de error
                            "Error en " + lineas.get(i).descripcion + ": Min ≤ Estimado ≤ Max", // Mensaje de error con descripción
                            "Error de Validación", JOptionPane.ERROR_MESSAGE); // Título y tipo de diálogo
                        return false; // Retorna falso indicando validación fallida
                    } // Fin de verificación de relación
                } // Fin de verificación de valores positivos
            } // Fin de verificación de categoría
        } // Fin de iteración sobre líneas

        if (!hayDatos) { // Si no se encontraron datos válidos
            JOptionPane.showMessageDialog(this, // Muestra diálogo de advertencia
                "Ingrese al menos una categoría completa (con Min, Estimado y Max)", // Mensaje de advertencia
                "Advertencia", JOptionPane.WARNING_MESSAGE); // Título y tipo de diálogo
            return false; // Retorna falso indicando validación fallida
        } // Fin de verificación de existencia de datos

        return true; // Retorna verdadero indicando validación exitosa
    } // Fin del método validarDatos

    private void sincronizarDatosTabla() { // Método que sincroniza datos de tabla con objetos LineaPresupuesto
        for (int i = 0; i < lineas.size(); i++) { // Itera sobre todas las líneas
            lineas.get(i).estimado = getValorTabla(i, 2); // Sincroniza valor estimado
            lineas.get(i).min = getValorTabla(i, 3); // Sincroniza valor mínimo
            lineas.get(i).max = getValorTabla(i, 4); // Sincroniza valor máximo
        } // Fin de iteración sobre líneas
    } // Fin del método sincronizarDatosTabla

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
                        if (linea.esCategoria && linea.min > 0 && linea.max > 0 && linea.estimado > 0) { // Verifica si es categoría con valores válidos
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
                    JOptionPane.showMessageDialog(SimuladorManual.this, // Muestra diálogo de error
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
        actualizandoAutomaticamente = true; // Activa bandera para evitar cálculos recursivos

        double totalSimulado = 0; // Inicializa acumulador de total simulado

        // Actualizar valores simulados para cada categoría // Comentario de actualización de categorías
        for (int i = 0; i < lineas.size(); i++) { // Itera sobre todas las líneas
            LineaPresupuesto linea = lineas.get(i); // Obtiene línea actual
            if (linea.esCategoria && linea.min > 0 && linea.max > 0 && linea.estimado > 0) { // Si es categoría con valores válidos
                TriangularDistribution dist = new TriangularDistribution( // Crea distribución triangular
                    linea.min, linea.estimado, linea.max); // Con parámetros de la categoría
                double valorSimulado = dist.sample(); // Genera una muestra aleatoria
                modeloTabla.setValueAt(valorSimulado, i, 5); // Actualiza valor en columna Simulado
                totalSimulado += valorSimulado; // Acumula valor simulado
            } // Fin de verificación de categoría
        } // Fin de iteración sobre líneas

        // Actualizar TOTAL PROYECTO simulado // Comentario de actualización de total
        if (resultadosSimulacion != null && resultadosSimulacion.length > 0) { // Verifica si existen resultados
            double promedioTotal = Arrays.stream(resultadosSimulacion).average().orElse(0); // Calcula promedio de todos los resultados
            modeloTabla.setValueAt(promedioTotal, filaIndexTotalProyecto, 5); // Actualiza fila TOTAL PROYECTO con promedio

            // Actualizar TOTAL CON CONTINGENCIA simulado // Comentario de actualización de total con contingencia
            double totalConContingenciaSimulado = promedioTotal * (1 + PORCENTAJE_CONTINGENCIA); // Calcula total con 20% de contingencia
            modeloTabla.setValueAt(totalConContingenciaSimulado, filaIndexTotalConContingencia, 5); // Actualiza fila de total con contingencia
        } // Fin de verificación de resultados

        actualizandoAutomaticamente = false; // Desactiva bandera de actualización automática
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
            SimuladorManual simulador = new SimuladorManual(); // Crea instancia del simulador manual
            simulador.setVisible(true); // Hace visible la ventana principal
        }); // Fin de invokeLater
    } // Fin del método main
} // Fin de la clase SimuladorManual