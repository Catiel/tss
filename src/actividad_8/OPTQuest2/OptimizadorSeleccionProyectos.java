package actividad_8.OPTQuest2; // Declaración del paquete

import com.formdev.flatlaf.FlatLightLaf; // Tema visual moderno para la interfaz
import org.apache.commons.math3.distribution.*; // Distribuciones estadísticas de Apache Commons Math
import org.jfree.chart.*; // Clases principales de JFreeChart
import org.jfree.chart.axis.NumberAxis; // Eje numérico para gráficos
import org.jfree.chart.plot.*; // Clases para plots de gráficos
import org.jfree.chart.renderer.xy.XYBarRenderer; // Renderizador de barras XY
import org.jfree.data.statistics.HistogramDataset; // Dataset para histogramas

import javax.swing.*; // Componentes de interfaz gráfica Swing
import javax.swing.table.*; // Componentes de tablas Swing
import java.awt.*; // Componentes gráficos y layouts
import java.text.DecimalFormat; // Formateo de números decimales
import java.util.*; // Utilidades generales de Java
import java.util.List; // Interfaz List
import java.util.stream.IntStream; // Stream de enteros

public class OptimizadorSeleccionProyectos extends JFrame { // Clase principal que extiende JFrame

    private static class Config { // Clase interna estática para configuración del problema
        static final double[] GANANCIA = {750000, 1500000, 600000, 1800000, 1250000, 150000, 900000, 250000}; // Ganancias esperadas de cada proyecto
        static final double[] PROB_EXITO = {0.90, 0.70, 0.60, 0.40, 0.80, 0.60, 0.70, 0.90}; // Probabilidades de éxito de cada proyecto
        static final double[] INVERSION = {250000, 650000, 250000, 500000, 700000, 30000, 350000, 70000}; // Inversión inicial requerida de cada proyecto
        static final double PRESUPUESTO = 2000000; // Presupuesto total disponible
        static final int NUM_PROYECTOS = 8; // Número total de proyectos
        static final int NUM_SIMULACIONES = 213; // Número de simulaciones de optimización
        static final int NUM_PRUEBAS_MC = 5000; // Número de pruebas Monte Carlo por simulación
    }

    private static final DecimalFormat FMT_MONEY = new DecimalFormat("$#,##0"); // Formato monetario sin decimales
    private static final DecimalFormat FMT_PERCENT = new DecimalFormat("0%"); // Formato de porcentaje

    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    private JLabel[] lblTotales = new JLabel[4]; // Array de etiquetas para totales (4 valores)
    private JProgressBar progressBar, progressBarPruebas; // Barras de progreso para simulaciones y pruebas
    private JLabel lblSimulaciones, lblPruebas; // Etiquetas para mostrar progreso
    private JButton btnOptimizar, btnGraficas; // Botones para optimizar y ver gráficas

    private double mejorGanancia = Double.NEGATIVE_INFINITY; // Mejor ganancia encontrada (inicializada en infinito negativo)
    private int[] mejorDecision = new int[Config.NUM_PROYECTOS]; // Array con mejor decisión encontrada (0 o 1 para cada proyecto)
    private List<Double> gananciasFinales = new ArrayList<>(); // Lista de ganancias de la mejor solución (para histograma)

    public OptimizadorSeleccionProyectos() { // Constructor de la clase principal
        super("Selección de proyectos por restricción de presupuesto"); // Título de la ventana
        configurarUI(); // Configurar interfaz de usuario
        setSize(1500, 750); // Establecer tamaño de ventana
        setLocationRelativeTo(null); // Centrar ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
    }

    private void configurarUI() { // Método para configurar interfaz de usuario
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30); // Crear panel principal con margen
        main.add(crearTitulo(), BorderLayout.NORTH); // Agregar título al norte
        main.add(crearTabla(), BorderLayout.CENTER); // Agregar tabla al centro

        JPanel sur = new JPanel(new BorderLayout(10, 10)); // Panel sur con BorderLayout
        sur.setBackground(Color.WHITE); // Fondo blanco
        sur.add(crearPanelTotales(), BorderLayout.NORTH); // Agregar panel de totales al norte
        sur.add(crearPanelControl(), BorderLayout.CENTER); // Agregar panel de control al centro

        main.add(sur, BorderLayout.SOUTH); // Agregar panel sur al sur del principal
        add(main); // Agregar panel principal a la ventana
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) { // Método auxiliar para crear panel con margen
        JPanel panel = new JPanel(layout); // Crear panel con layout especificado
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left)); // Agregar margen vacío
        return panel; // Retornar panel
    }

    private JPanel crearTitulo() { // Método para crear panel con título
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel con FlowLayout alineado a la izquierda
        panel.setBackground(Color.WHITE); // Fondo blanco
        JLabel titulo = new JLabel("Selección de proyectos por restricción de presupuesto"); // Etiqueta de título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Fuente grande y negrita
        titulo.setForeground(new Color(31, 78, 120)); // Color azul oscuro
        panel.add(titulo); // Agregar título al panel
        return panel; // Retornar panel
    }

    private JScrollPane crearTabla() { // Método para crear tabla con scroll
        String[] cols = {"Proyecto", "Decisión", "Ganancia\nesperada", "Prob. De\nÉxito", // Nombres de columnas
                        "Retorno\nEsperado", "Inversión\nInicial", "Beneficio\nEsperado"};

        modeloTabla = new DefaultTableModel(cols, 0) { // Crear modelo de tabla
            public boolean isCellEditable(int r, int c) { return false; } // Hacer todas las celdas no editables
        };

        JTable tabla = new JTable(modeloTabla); // Crear tabla con el modelo
        configurarEstiloTabla(tabla); // Configurar estilo de la tabla
        llenarTablaInicial(); // Llenar tabla con datos iniciales

        JScrollPane scroll = new JScrollPane(tabla); // Crear scroll para tabla
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Agregar borde gris
        return scroll; // Retornar scroll
    }

    private void configurarEstiloTabla(JTable tabla) { // Método para configurar estilo de tabla
        tabla.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente de la tabla
        tabla.setRowHeight(35); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 12)); // Fuente del encabezado
        tabla.getTableHeader().setBackground(new Color(79, 129, 189)); // Color de fondo encabezado (azul)
        tabla.getTableHeader().setForeground(Color.WHITE); // Color de texto encabezado (blanco)
        tabla.setGridColor(new Color(200, 200, 200)); // Color de líneas de cuadrícula

        Color[] colores = {new Color(197, 217, 241), new Color(255, 255, 0), // Array de colores por columna
                          new Color(146, 208, 80), new Color(146, 208, 80),
                          Color.WHITE, Color.WHITE, new Color(0, 176, 240)};

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Renderizador personalizado
            public Component getTableCellRendererComponent(JTable t, Object v, // Sobrescribir método
                    boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Obtener componente base
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.CENTER); // Alinear al centro
                setFont(new Font("Calibri", c == 0 || c == 1 ? Font.BOLD : Font.PLAIN, 12)); // Fuente según columna
                setBackground(colores[c]); // Establecer color de fondo según columna
                setForeground(Color.BLACK); // Texto negro
                return this; // Retornar componente
            }
        };

        IntStream.range(0, tabla.getColumnCount()).forEach(i -> { // Iterar sobre todas las columnas usando stream
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplicar renderizador a cada columna
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 100 : 110); // Establecer ancho preferido
        });
    }

    private void llenarTablaInicial() { // Método para llenar tabla con datos iniciales
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Iterar sobre todos los proyectos
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i]; // Calcular retorno esperado
            modeloTabla.addRow(new Object[]{ // Agregar fila con datos
                String.valueOf(i + 1), "1", // Número de proyecto y decisión inicial (1 = seleccionado)
                FMT_MONEY.format(Config.GANANCIA[i]), // Ganancia esperada formateada
                FMT_PERCENT.format(Config.PROB_EXITO[i]), // Probabilidad formateada
                FMT_MONEY.format(retorno), // Retorno esperado formateado
                FMT_MONEY.format(Config.INVERSION[i]), // Inversión formateada
                FMT_MONEY.format(0) // Beneficio inicial (0)
            });
        }
    }

    private JPanel crearPanelTotales() { // Método para crear panel de totales
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0)); // Panel con GridLayout de 1 fila y 4 columnas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Agregar margen vacío

        String[] titulos = {"Presupuesto", "Inversión", "Sobrante", "Ganancia total"}; // Títulos de las 4 métricas
        Color[] colores = {Color.WHITE, Color.WHITE, new Color(255, 192, 0), new Color(0, 176, 240)}; // Colores de fondo
        double[] valores = {Config.PRESUPUESTO, 0, 0, 0}; // Valores iniciales

        for (int i = 0; i < 4; i++) { // Iterar sobre las 4 métricas
            lblTotales[i] = crearLabelTotal(titulos[i], valores[i], colores[i]); // Crear etiqueta para cada métrica
            panel.add(lblTotales[i]); // Agregar etiqueta al panel
        }
        return panel; // Retornar panel completo
    }

    private JLabel crearLabelTotal(String titulo, double valor, Color bg) { // Método para crear etiqueta de total
        JLabel lbl = new JLabel("<html><center>" + titulo + "<br><b>" + // Crear etiqueta con HTML para título y valor
                               FMT_MONEY.format(valor) + "</b></center></html>");
        lbl.setFont(new Font("Calibri", Font.PLAIN, 14)); // Establecer fuente
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinear al centro
        lbl.setBackground(bg); // Establecer color de fondo
        lbl.setForeground(Color.BLACK); // Texto negro
        lbl.setOpaque(true); // Hacer opaco
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Agregar borde negro
        lbl.setPreferredSize(new Dimension(150, 50)); // Establecer tamaño preferido
        return lbl; // Retornar etiqueta
    }

    private JPanel crearPanelControl() { // Método para crear panel de control
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // Panel con GridLayout de 2 filas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Agregar margen vacío

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0)); // Panel para botones con FlowLayout
        panelBtns.setBackground(Color.WHITE); // Fondo blanco

        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)", // Crear botón optimizar
                                  new Color(68, 114, 196), 320, 45);
        btnGraficas = crearBoton("Ver Gráficas", new Color(112, 173, 71), 180, 40); // Crear botón gráficas
        btnGraficas.setEnabled(false); // Deshabilitar inicialmente

        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Listener para ejecutar optimización
        btnGraficas.addActionListener(e -> mostrarHistograma()); // Listener para mostrar histograma

        panelBtns.add(btnOptimizar); // Agregar botón optimizar
        panelBtns.add(btnGraficas); // Agregar botón gráficas

        JPanel panelProgress = crearPanelProgreso(); // Crear panel de progreso

        panel.add(panelBtns); // Agregar panel de botones
        panel.add(panelProgress); // Agregar panel de progreso
        return panel; // Retornar panel completo
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar para crear botón
        JButton btn = new JButton(texto); // Crear botón con texto
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 15 ? 15 : 14)); // Fuente según longitud del texto
        btn.setBackground(bg); // Establecer color de fondo
        btn.setForeground(Color.WHITE); // Texto blanco
        btn.setFocusPainted(false); // Quitar borde de foco
        btn.setPreferredSize(new Dimension(ancho, alto)); // Establecer tamaño preferido
        return btn; // Retornar botón
    }

    private JPanel crearPanelProgreso() { // Método para crear panel de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Panel con BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder( // Agregar borde con título
            BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest"));

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8)); // Panel para barras con GridLayout
        barras.setBackground(Color.WHITE); // Fondo blanco
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Agregar margen vacío

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales", // Crear barra de simulaciones
                                         new Color(0, 32, 96), out -> lblSimulaciones = out);
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas", // Crear barra de pruebas
                                                new Color(0, 176, 80), out -> lblPruebas = out);

        barras.add(progressBar.getParent()); // Agregar barra de simulaciones
        barras.add(progressBarPruebas.getParent()); // Agregar barra de pruebas

        panel.add(barras); // Agregar panel de barras
        return panel; // Retornar panel completo
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color, // Método para crear barra de progreso con etiqueta
                                           java.util.function.Consumer<JLabel> labelOut) {
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Panel con BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco

        JLabel lbl = new JLabel(texto + ": 0 / " + max); // Etiqueta con texto y valores
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10)); // Fuente pequeña
        labelOut.accept(lbl); // Pasar etiqueta al consumer (para guardar referencia)

        JProgressBar bar = new JProgressBar(0, max); // Crear barra de progreso con rango 0-max
        bar.setPreferredSize(new Dimension(500, 22)); // Establecer tamaño preferido
        bar.setForeground(color); // Establecer color de la barra
        bar.setBackground(Color.WHITE); // Fondo blanco

        panel.add(lbl, BorderLayout.WEST); // Agregar etiqueta al oeste
        panel.add(bar, BorderLayout.CENTER); // Agregar barra al centro
        panel.add(new JLabel(max + "  "), BorderLayout.EAST); // Agregar etiqueta con máximo al este

        return bar; // Retornar barra de progreso
    }

    private void ejecutarOptimizacion() { // Método para ejecutar optimización
        btnOptimizar.setEnabled(false); // Deshabilitar botón optimizar
        btnGraficas.setEnabled(false); // Deshabilitar botón gráficas
        gananciasFinales.clear(); // Limpiar ganancias finales
        mejorGanancia = Double.NEGATIVE_INFINITY; // Reiniciar mejor ganancia

        new SwingWorker<Void, int[]>() { // Worker para ejecutar en segundo plano
            protected Void doInBackground() { // Método ejecutado en hilo separado
                Random rand = new Random(); // Crear generador aleatorio

                for (int sim = 0; sim < Config.NUM_SIMULACIONES; sim++) { // Ejecutar simulaciones
                    int[] decision = generarDecision(rand); // Generar decisión aleatoria
                    List<Double> ganancias = new ArrayList<>(); // Lista temporal de ganancias

                    for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) { // EJECUTAR PRUEBAS Monte Carlo
                        double ganancia = simular(decision, rand); // Simular con decisión
                        if (calcularInversion(decision) <= Config.PRESUPUESTO) { // Si cumple restricción de presupuesto
                            ganancias.add(ganancia); // Guardar ganancia
                        }
                        if (mc % 250 == 0) publish(new int[]{sim + 1, mc + 1}); // Publicar actualización cada 250 pruebas
                    }

                    if (!ganancias.isEmpty()) { // Si hay ganancias válidas
                        double media = ganancias.stream().mapToDouble(d -> d).average().orElse(0); // Calcular media
                        if (media > mejorGanancia) { // Si esta decisión es mejor
                            mejorGanancia = media; // Actualizar mejor ganancia
                            mejorDecision = decision.clone(); // Guardar mejor decisión
                            gananciasFinales = new ArrayList<>(ganancias); // Guardar ganancias de esta mejor solución
                        }
                    }
                }
                return null; // Retornar null
            }

            protected void process(List<int[]> chunks) { // Método para procesar actualizaciones de progreso
                int[] ultimo = chunks.get(chunks.size() - 1); // Obtener última actualización
                progressBar.setValue(ultimo[0]); // Actualizar barra de simulaciones
                progressBarPruebas.setValue(ultimo[1]); // Actualizar barra de pruebas
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES); // Actualizar etiqueta
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC); // Actualizar etiqueta
            }

            protected void done() { // Método ejecutado al terminar (en hilo de UI)
                actualizarResultados(); // Actualizar resultados en UI
                btnOptimizar.setEnabled(true); // Habilitar botón optimizar
                btnGraficas.setEnabled(true); // Habilitar botón gráficas
                mostrarVentanaResultados(); // Mostrar ventana de resultados
            }
        }.execute(); // Iniciar ejecución del worker
    }

    private int[] generarDecision(Random rand) { // Método para generar decisión aleatoria (qué proyectos seleccionar)
        int[] dec = new int[Config.NUM_PROYECTOS]; // Array de decisiones (0 o 1)
        do { // Repetir hasta generar decisión válida
            for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Iterar sobre proyectos
                dec[i] = rand.nextBoolean() ? 1 : 0; // Seleccionar aleatoriamente (1) o no (0)
            }
        } while (calcularInversion(dec) > Config.PRESUPUESTO && rand.nextDouble() > 0.3); // Repetir si excede presupuesto (con probabilidad)
        return dec; // Retornar decisión
    }

    private double calcularInversion(int[] decision) { // Método para calcular inversión total de una decisión
        return IntStream.range(0, Config.NUM_PROYECTOS) // Stream de índices
                       .mapToDouble(i -> decision[i] * Config.INVERSION[i]) // Multiplicar decisión por inversión
                       .sum(); // Sumar todas las inversiones
    }

    private double simular(int[] decision, Random rand) { // Método para simular ganancia con una decisión
        double ganancia = 0; // Inicializar ganancia en 0
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Iterar sobre proyectos
            if (decision[i] == 1) { // Si el proyecto está seleccionado
                double g = generarGanancia(i, rand); // Generar ganancia aleatoria según distribución del proyecto
                int exito = rand.nextDouble() < Config.PROB_EXITO[i] ? 1 : 0; // Determinar si el proyecto tiene éxito
                ganancia += (g * exito) - Config.INVERSION[i]; // Calcular ganancia (ganancia si éxito - inversión)
            }
        }
        return ganancia; // Retornar ganancia total
    }

    private double generarGanancia(int proyecto, Random rand) { // Método para generar ganancia según distribución del proyecto
        return switch (proyecto) { // Switch según número de proyecto
            case 0 -> new NormalDistribution(750000, 75000).sample(); // Proyecto 1: distribución normal
            case 1 -> triangular(1250000, 1500000, 1600000, rand); // Proyecto 2: distribución triangular
            case 2 -> logNormal(600000, 50000, rand); // Proyecto 3: distribución log-normal
            case 3 -> triangular(1600000, 1800000, 1900000, rand); // Proyecto 4: distribución triangular
            case 4 -> new UniformRealDistribution(1150000, 1350000).sample(); // Proyecto 5: distribución uniforme
            case 5 -> logNormal(150000, 30000, rand); // Proyecto 6: distribución log-normal
            case 6 -> new NormalDistribution(900000, 50000).sample(); // Proyecto 7: distribución normal
            case 7 -> triangular(220000, 250000, 320000, rand); // Proyecto 8: distribución triangular
            default -> 0; // Por defecto retornar 0
        };
    }

    private double triangular(double min, double mode, double max, Random rand) { // Método para generar valor de distribución triangular
        double u = rand.nextDouble(); // Generar número aleatorio uniforme [0,1]
        double f = (mode - min) / (max - min); // Calcular factor
        return u < f ? min + Math.sqrt(u * (max - min) * (mode - min)) // Si u < f, usar fórmula ascendente
                     : max - Math.sqrt((1 - u) * (max - min) * (max - mode)); // Si no, usar fórmula descendente
    }

    private double logNormal(double media, double std, Random rand) { // Método para generar valor de distribución log-normal
        double var = std * std; // Calcular varianza
        double mu = Math.log(media * media / Math.sqrt(var + media * media)); // Calcular mu de la log-normal
        double sigma = Math.sqrt(Math.log(1 + var / (media * media))); // Calcular sigma de la log-normal
        return new LogNormalDistribution(mu, sigma).sample(); // Retornar muestra de la distribución
    }

    private void actualizarResultados() { // Método para actualizar resultados en UI
        double inv = calcularInversion(mejorDecision); // Calcular inversión total de la mejor decisión

        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Iterar sobre proyectos
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i]; // Calcular retorno esperado
            double beneficio = (retorno - Config.INVERSION[i]) * mejorDecision[i]; // Calcular beneficio
            modeloTabla.setValueAt(String.valueOf(mejorDecision[i]), i, 1); // Actualizar decisión en tabla
            modeloTabla.setValueAt(FMT_MONEY.format(beneficio), i, 6); // Actualizar beneficio en tabla
        }

        actualizarLabel(lblTotales[1], "Inversión", inv); // Actualizar etiqueta de inversión
        actualizarLabel(lblTotales[2], "Sobrante", Config.PRESUPUESTO - inv); // Actualizar etiqueta de sobrante
        actualizarLabel(lblTotales[3], "Ganancia total", mejorGanancia); // Actualizar etiqueta de ganancia total
    }

    private void actualizarLabel(JLabel lbl, String titulo, double valor) { // Método auxiliar para actualizar etiqueta
        lbl.setText("<html><center>" + titulo + "<br><b>" + FMT_MONEY.format(valor) + "</b></center></html>"); // Actualizar texto con HTML
    }

    private void mostrarVentanaResultados() { // Método para mostrar ventana de resultados
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Crear diálogo no modal
        dlg.setLayout(new BorderLayout()); // Establecer layout

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15); // Crear panel principal con margen
        main.add(new JLabel("Optimización terminada. Todas las variables enumeradas."), // Agregar etiqueta de título
                BorderLayout.NORTH);

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 5)); // Panel para tablas con GridLayout
        tablas.setBackground(Color.WHITE); // Fondo blanco
        tablas.add(crearSeccionResultado("Objetivos", "Valor", // Agregar sección de objetivos
            new String[]{"Maximizar Media Total profit"},
            new String[]{FMT_MONEY.format(mejorGanancia)}));
        tablas.add(crearSeccionResultado("Restricciones", "Lado izq <= Lado der", // Agregar sección de restricciones
            new String[]{"Inversión <= Presupuesto"},
            new String[]{FMT_MONEY.format(calcularInversion(mejorDecision)) + " <= " +
                        FMT_MONEY.format(Config.PRESUPUESTO)}));

        String[][] vars = new String[Config.NUM_PROYECTOS][2]; // Array de variables de decisión
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Iterar sobre proyectos
            vars[i] = new String[]{"Project " + (i + 1), String.valueOf(mejorDecision[i])}; // Nombre y valor
        }
        tablas.add(crearTablaVariables(vars)); // Agregar tabla de variables

        main.add(tablas, BorderLayout.CENTER); // Agregar tablas al centro
        dlg.add(main); // Agregar panel principal al diálogo
        dlg.setSize(700, 480); // Establecer tamaño
        dlg.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        dlg.setVisible(true); // Hacer visible
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) { // Método para crear sección de resultado
        JPanel panel = new JPanel(new BorderLayout()); // Panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder(titulo)); // Agregar borde con título
        panel.setBackground(Color.WHITE); // Fondo blanco

        if (filas.length > 0) { // Si hay filas
            JPanel grid = new JPanel(new GridLayout(filas.length, 2, 10, 5)); // Panel con GridLayout
            grid.setBackground(Color.WHITE); // Fondo blanco
            for (int i = 0; i < filas.length; i++) { // Iterar sobre filas
                grid.add(new JLabel(filas[i])); // Agregar etiqueta de fila
                grid.add(new JLabel(vals[i], SwingConstants.RIGHT)); // Agregar etiqueta de valor alineada a la derecha
            }
            panel.add(grid); // Agregar grid al panel
        }
        return panel; // Retornar panel
    }

    private JPanel crearTablaVariables(String[][] datos) { // Método para crear tabla de variables
        JPanel panel = new JPanel(new BorderLayout()); // Panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder("Variables de decisión")); // Agregar borde con título
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"", "Valor"}, 0); // Crear modelo de tabla
        Arrays.stream(datos).forEach(modelo::addRow); // Agregar todas las filas usando stream
        panel.add(new JScrollPane(new JTable(modelo))); // Agregar tabla con scroll al panel
        return panel; // Retornar panel
    }

    private void mostrarHistograma() { // Método para mostrar histograma
        double[] datos = gananciasFinales.stream().mapToDouble(d -> d).toArray(); // Convertir lista a array
        if (datos.length == 0) return; // Si no hay datos, salir

        HistogramDataset dataset = new HistogramDataset(); // Crear dataset de histograma
        dataset.addSeries("Total profit", datos, 50); // Agregar serie con 50 bins

        JFreeChart chart = ChartFactory.createHistogram("Total profit", "Dollars", // Crear gráfico de histograma
            "Frecuencia", dataset, PlotOrientation.VERTICAL, false, true, false);

        XYPlot plot = chart.getXYPlot(); // Obtener plot del gráfico
        plot.setBackgroundPaint(Color.WHITE); // Color de fondo blanco
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtener renderizador de barras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Color de barras (azul)
        renderer.setShadowVisible(false); // Desactivar sombras

        double media = Arrays.stream(datos).average().orElse(0); // Calcular media
        plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(media) {{ // Agregar marcador en la media (inicializador anónimo)
            setPaint(Color.BLACK); // Color negro
            setLabel("Media = " + FMT_MONEY.format(media)); // Etiqueta con valor de media
        }});

        ((NumberAxis) plot.getDomainAxis()).setNumberFormatOverride(FMT_MONEY); // Establecer formato monetario en eje X

        JFrame frame = new JFrame("Previsión: Total profit"); // Crear ventana para histograma
        frame.setContentPane(new ChartPanel(chart)); // Establecer panel del gráfico como contenido
        frame.setSize(900, 600); // Establecer tamaño
        frame.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        frame.setVisible(true); // Hacer visible
    }

    public static void main(String[] args) { // Método main - punto de entrada
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } // Establecer Look and Feel FlatLaf
        catch (Exception e) { e.printStackTrace(); } // Capturar y mostrar excepciones

        SwingUtilities.invokeLater(() -> // Ejecutar en hilo de eventos de Swing
            new OptimizadorSeleccionProyectos().setVisible(true)); // Crear instancia y hacer visible
    }
}