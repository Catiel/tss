package actividad_8.OPTQuest2; // Declaración del paquete

import com.formdev.flatlaf.FlatLightLaf; // Tema visual moderno FlatLaf
import org.apache.commons.math3.distribution.*; // Distribuciones estadísticas (Normal, Uniforme, LogNormal)
import org.jfree.chart.*; // Biblioteca JFreeChart para gráficos
import org.jfree.chart.axis.NumberAxis; // Eje numérico
import org.jfree.chart.plot.*; // Configuración de plots
import org.jfree.chart.renderer.xy.XYBarRenderer; // Renderizador de barras
import org.jfree.data.statistics.HistogramDataset; // Dataset para histogramas

import javax.swing.*; // Componentes de interfaz gráfica Swing
import javax.swing.table.*; // Componentes de tablas
import java.awt.*; // Layouts y componentes gráficos
import java.text.DecimalFormat; // Formateo de números
import java.util.*; // Utilidades (Random, List, Arrays)
import java.util.List; // Interfaz List
import java.util.stream.IntStream; // Stream de enteros

public class OptimizadorSeleccionProyectosAleatorio extends JFrame { // Clase principal - versión ALEATORIA (genera valores aleatorios)

    private static class Config { // Clase interna para configuración del problema
        static final double[] GANANCIA = {750000, 1500000, 600000, 1800000, 1250000, 150000, 900000, 250000}; // Ganancias esperadas (fijas)
        static final double[] PROB_EXITO = {0.90, 0.70, 0.60, 0.40, 0.80, 0.60, 0.70, 0.90}; // Probabilidades de éxito (fijas)
        static double[] INVERSION = {250000, 650000, 250000, 500000, 700000, 30000, 350000, 70000}; // Inversiones (GENERABLES ALEATORIAMENTE)
        static double PRESUPUESTO = 2000000; // Presupuesto total (GENERABLE ALEATORIAMENTE)
        static final int NUM_PROYECTOS = 8; // Número de proyectos
        static final int NUM_SIMULACIONES = 213; // Número de simulaciones OptQuest
        static final int NUM_PRUEBAS_MC = 5000; // Pruebas Monte Carlo por simulación

        static final double PRESUPUESTO_MIN = 500000; // Presupuesto mínimo para generación aleatoria ($500k)
        static final double PRESUPUESTO_MAX = 5000000; // Presupuesto máximo para generación aleatoria ($5M)
        static final double INVERSION_MIN = 10000; // Inversión mínima por proyecto ($10k)
        static final double INVERSION_MAX = 1000000; // Inversión máxima por proyecto ($1M)
    }

    private static final DecimalFormat FMT_MONEY = new DecimalFormat("$#,##0"); // Formato monetario
    private static final DecimalFormat FMT_PERCENT = new DecimalFormat("0%"); // Formato porcentaje

    private DefaultTableModel modeloTabla; // Modelo de la tabla principal
    private JLabel[] lblTotales = new JLabel[4]; // Etiquetas de totales (Presupuesto, Inversión, Sobrante, Ganancia)
    private JProgressBar progressBar, progressBarPruebas; // Barras de progreso
    private JLabel lblSimulaciones, lblPruebas; // Etiquetas de progreso
    private JButton btnOptimizar, btnGraficas, btnGenerarAleatorio; // Botones principales (INCLUYE GENERAR ALEATORIO)

    private double mejorGanancia = Double.NEGATIVE_INFINITY; // Mejor ganancia encontrada
    private int[] mejorDecision = new int[Config.NUM_PROYECTOS]; // Mejor combinación de proyectos (0 o 1)
    private List<Double> gananciasFinales = new ArrayList<>(); // Ganancias de la mejor solución para histograma
    private Random generadorAleatorio = new Random(); // Generador aleatorio para variables

    public OptimizadorSeleccionProyectosAleatorio() { // Constructor
        super("Selección de proyectos con generación aleatoria"); // Título de la ventana
        configurarUI(); // Configurar interfaz de usuario
        setSize(1500, 750); // Tamaño de ventana
        setLocationRelativeTo(null); // Centrar en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
    }

    private void configurarUI() { // Método para configurar interfaz
        JPanel main = crearPanelConMargen(new BorderLayout(15, 15), 25, 30); // Panel principal con margen
        main.add(crearTitulo(), BorderLayout.NORTH); // Título arriba
        main.add(crearTabla(), BorderLayout.CENTER); // Tabla en el centro

        JPanel sur = new JPanel(new BorderLayout(10, 10)); // Panel inferior
        sur.setBackground(Color.WHITE); // Fondo blanco
        sur.add(crearPanelTotales(), BorderLayout.NORTH); // Totales arriba del panel inferior
        sur.add(crearPanelControl(), BorderLayout.CENTER); // Controles en el centro del panel inferior

        main.add(sur, BorderLayout.SOUTH); // Panel inferior abajo
        add(main); // Agregar panel principal a la ventana
    }

    private JPanel crearPanelConMargen(LayoutManager layout, int top, int left) { // Método auxiliar para crear panel con margen
        JPanel panel = new JPanel(layout); // Crear panel con layout especificado
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(top, left, top, left)); // Margen uniforme
        return panel; // Retornar panel
    }

    private JPanel crearTitulo() { // Crear panel con título
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT)); // FlowLayout alineado a la izquierda
        panel.setBackground(Color.WHITE); // Fondo blanco
        JLabel titulo = new JLabel("Selección de proyectos con generación aleatoria"); // Texto del título
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24)); // Fuente grande y negrita
        titulo.setForeground(new Color(31, 78, 120)); // Color azul oscuro
        panel.add(titulo); // Agregar título al panel
        return panel; // Retornar panel
    }

    private JScrollPane crearTabla() { // Crear tabla principal con scroll
        String[] cols = {"Proyecto", "Decisión", "Ganancia\nesperada", "Prob. De\nÉxito", "Retorno\nEsperado", "Inversión\nInicial", "Beneficio\nEsperado"}; // Columnas

        modeloTabla = new DefaultTableModel(cols, 0) { // Modelo de tabla
            public boolean isCellEditable(int r, int c) { return false; } // Todas las celdas no editables
        };

        JTable tabla = new JTable(modeloTabla); // Crear tabla con modelo
        configurarEstiloTabla(tabla); // Aplicar estilos
        llenarTablaInicial(); // Llenar con datos iniciales

        JScrollPane scroll = new JScrollPane(tabla); // Agregar scroll
        scroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200))); // Borde gris
        return scroll; // Retornar scroll con tabla
    }

    private void configurarEstiloTabla(JTable tabla) { // Configurar colores y fuentes de tabla
        tabla.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente de celdas
        tabla.setRowHeight(35); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Calibri", Font.BOLD, 12)); // Fuente de encabezados
        tabla.getTableHeader().setBackground(new Color(79, 129, 189)); // Fondo azul de encabezados
        tabla.getTableHeader().setForeground(Color.WHITE); // Texto blanco en encabezados
        tabla.setGridColor(new Color(200, 200, 200)); // Color de líneas de cuadrícula

        Color[] colores = {new Color(197, 217, 241), new Color(255, 255, 0), new Color(146, 208, 80), new Color(146, 208, 80), Color.WHITE, Color.WHITE, new Color(0, 176, 240)}; // Colores por columna

        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() { // Renderizador personalizado
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) { // Método de renderizado
                super.getTableCellRendererComponent(t, v, sel, foc, r, c); // Llamar al método base
                setHorizontalAlignment(c == 0 ? SwingConstants.CENTER : SwingConstants.CENTER); // Alinear al centro
                setFont(new Font("Calibri", c == 0 || c == 1 ? Font.BOLD : Font.PLAIN, 12)); // Negrita para columnas 0 y 1
                setBackground(colores[c]); // Color de fondo según columna
                setForeground(Color.BLACK); // Texto negro
                return this; // Retornar componente
            }
        };

        IntStream.range(0, tabla.getColumnCount()).forEach(i -> { // Para cada columna
            tabla.getColumnModel().getColumn(i).setCellRenderer(renderer); // Aplicar renderizador
            tabla.getColumnModel().getColumn(i).setPreferredWidth(i == 0 ? 100 : 110); // Ancho preferido
        });
    }

    private void llenarTablaInicial() { // Llenar tabla con datos iniciales
        modeloTabla.setRowCount(0); // Limpiar filas
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i]; // Calcular retorno esperado
            modeloTabla.addRow(new Object[]{ // Agregar fila
                String.valueOf(i + 1), "1", // Número de proyecto y decisión inicial
                FMT_MONEY.format(Config.GANANCIA[i]), // Ganancia esperada
                FMT_PERCENT.format(Config.PROB_EXITO[i]), // Probabilidad de éxito
                FMT_MONEY.format(retorno), // Retorno esperado
                FMT_MONEY.format(Config.INVERSION[i]), // Inversión inicial
                FMT_MONEY.format(0) // Beneficio esperado inicial (0)
            });
        }
    }

    private JPanel crearPanelTotales() { // Crear panel con etiquetas de totales
        JPanel panel = new JPanel(new GridLayout(1, 4, 20, 0)); // GridLayout 1 fila x 4 columnas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // Margen superior e inferior

        String[] titulos = {"Presupuesto", "Inversión", "Sobrante", "Ganancia total"}; // Títulos de métricas
        Color[] colores = {Color.WHITE, Color.WHITE, new Color(255, 192, 0), new Color(0, 176, 240)}; // Colores de fondo
        double[] valores = {Config.PRESUPUESTO, 0, 0, 0}; // Valores iniciales

        for (int i = 0; i < 4; i++) { // Para cada métrica
            lblTotales[i] = crearLabelTotal(titulos[i], valores[i], colores[i]); // Crear etiqueta
            panel.add(lblTotales[i]); // Agregar al panel
        }
        return panel; // Retornar panel
    }

    private JLabel crearLabelTotal(String titulo, double valor, Color bg) { // Crear etiqueta de total
        JLabel lbl = new JLabel("<html><center>" + titulo + "<br><b>" + FMT_MONEY.format(valor) + "</b></center></html>"); // HTML para título y valor
        lbl.setFont(new Font("Calibri", Font.PLAIN, 14)); // Fuente
        lbl.setHorizontalAlignment(SwingConstants.CENTER); // Alinear al centro
        lbl.setBackground(bg); // Color de fondo
        lbl.setForeground(Color.BLACK); // Texto negro
        lbl.setOpaque(true); // Hacer opaco para mostrar color de fondo
        lbl.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Borde negro
        lbl.setPreferredSize(new Dimension(150, 50)); // Tamaño preferido
        return lbl; // Retornar etiqueta
    }

    private JPanel crearPanelControl() { // Crear panel de controles
        JPanel panel = new JPanel(new GridLayout(2, 1, 10, 10)); // GridLayout 2 filas
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createEmptyBorder(15, 0, 10, 0)); // Margen

        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0)); // Panel de botones
        panelBtns.setBackground(Color.WHITE); // Fondo blanco

        btnGenerarAleatorio = crearBoton("🎲 Generar Variables Aleatorias", new Color(220, 20, 60), 280, 40); // Botón GENERAR ALEATORIO (rojo carmesí)
        btnOptimizar = crearBoton("Ejecutar Optimización (OptQuest)", new Color(68, 114, 196), 320, 45); // Botón OPTIMIZAR (azul)
        btnGraficas = crearBoton("Ver Gráficas", new Color(112, 173, 71), 180, 40); // Botón GRÁFICAS (verde)
        btnGraficas.setEnabled(false); // Deshabilitar inicialmente

        btnGenerarAleatorio.addActionListener(e -> mostrarDialogoGeneracionAleatoria()); // Al hacer clic: mostrar diálogo de generación
        btnOptimizar.addActionListener(e -> ejecutarOptimizacion()); // Al hacer clic: ejecutar optimización
        btnGraficas.addActionListener(e -> mostrarHistograma()); // Al hacer clic: mostrar histograma

        panelBtns.add(btnGenerarAleatorio); // Agregar botón generar
        panelBtns.add(btnOptimizar); // Agregar botón optimizar
        panelBtns.add(btnGraficas); // Agregar botón gráficas

        JPanel panelProgress = crearPanelProgreso(); // Crear panel de barras de progreso

        panel.add(panelBtns); // Agregar botones
        panel.add(panelProgress); // Agregar barras de progreso
        return panel; // Retornar panel
    }

    private JButton crearBoton(String texto, Color bg, int ancho, int alto) { // Método auxiliar para crear botón
        JButton btn = new JButton(texto); // Crear botón con texto
        btn.setFont(new Font("Calibri", Font.BOLD, texto.length() > 25 ? 13 : 14)); // Fuente según longitud (más pequeña si es muy largo)
        btn.setBackground(bg); // Color de fondo
        btn.setForeground(Color.WHITE); // Texto blanco
        btn.setFocusPainted(false); // Sin borde de foco
        btn.setPreferredSize(new Dimension(ancho, alto)); // Tamaño
        return btn; // Retornar botón
    }

    private JPanel crearPanelProgreso() { // Crear panel de barras de progreso
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // BorderLayout
        panel.setBackground(Color.WHITE); // Fondo blanco
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Panel de control: OptQuest")); // Borde con título

        JPanel barras = new JPanel(new GridLayout(2, 1, 5, 8)); // GridLayout para 2 barras
        barras.setBackground(Color.WHITE); // Fondo blanco
        barras.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15)); // Margen

        progressBar = crearBarraConLabel(Config.NUM_SIMULACIONES, "Simulaciones totales", new Color(0, 32, 96), out -> lblSimulaciones = out); // Barra de simulaciones (azul oscuro)
        progressBarPruebas = crearBarraConLabel(Config.NUM_PRUEBAS_MC, "Pruebas", new Color(0, 176, 80), out -> lblPruebas = out); // Barra de pruebas (verde)

        barras.add(progressBar.getParent()); // Agregar barra de simulaciones
        barras.add(progressBarPruebas.getParent()); // Agregar barra de pruebas

        panel.add(barras); // Agregar barras al panel
        return panel; // Retornar panel
    }

    private JProgressBar crearBarraConLabel(int max, String texto, Color color, java.util.function.Consumer<JLabel> labelOut) { // Crear barra de progreso con etiqueta
        JPanel panel = new JPanel(new BorderLayout(5, 5)); // Panel contenedor
        panel.setBackground(Color.WHITE); // Fondo blanco

        JLabel lbl = new JLabel(texto + ": 0 / " + max); // Etiqueta con texto y valores
        lbl.setFont(new Font("Calibri", Font.PLAIN, 10)); // Fuente pequeña
        labelOut.accept(lbl); // Pasar etiqueta al consumer (para guardar referencia externa)

        JProgressBar bar = new JProgressBar(0, max); // Crear barra con rango 0-max
        bar.setPreferredSize(new Dimension(500, 22)); // Tamaño
        bar.setForeground(color); // Color de la barra
        bar.setBackground(Color.WHITE); // Fondo blanco

        panel.add(lbl, BorderLayout.WEST); // Etiqueta a la izquierda
        panel.add(bar, BorderLayout.CENTER); // Barra en el centro
        panel.add(new JLabel(max + "  "), BorderLayout.EAST); // Valor máximo a la derecha

        return bar; // Retornar barra (el panel se obtiene con bar.getParent())
    }

    private void mostrarDialogoGeneracionAleatoria() { // Mostrar diálogo para generar variables aleatorias
        JDialog dialogo = new JDialog(this, "Generar Variables Aleatorias", true); // Diálogo modal
        dialogo.setLayout(new BorderLayout(10, 10)); // BorderLayout

        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15)); // Panel principal
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Margen
        panelPrincipal.setBackground(Color.WHITE); // Fondo blanco

        JPanel panelInfo = new JPanel(new BorderLayout()); // Panel de información
        panelInfo.setBackground(new Color(173, 216, 230)); // Fondo azul claro
        panelInfo.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto
            BorderFactory.createLineBorder(new Color(70, 130, 180), 2), // Borde azul de 2px
            BorderFactory.createEmptyBorder(15, 15, 15, 15) // Margen interno
        ));

        JLabel lblInfo = new JLabel("<html><div style='text-align: center;'>" + // HTML con información
            "<b style='font-size: 14px;'>🎲 Generación Aleatoria de Variables</b><br><br>" +
            "Se generarán valores aleatorios para:<br>" +
            "• <b>Presupuesto:</b> Entre " + FMT_MONEY.format(Config.PRESUPUESTO_MIN) + " y " + FMT_MONEY.format(Config.PRESUPUESTO_MAX) + "<br>" +
            "• <b>Inversión de cada proyecto:</b> Entre " + FMT_MONEY.format(Config.INVERSION_MIN) + " y " + FMT_MONEY.format(Config.INVERSION_MAX) +
            "</div></html>");
        lblInfo.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente
        panelInfo.add(lblInfo); // Agregar etiqueta al panel

        JPanel panelOpciones = new JPanel(new GridLayout(3, 1, 10, 10)); // Panel de opciones con GridLayout
        panelOpciones.setBackground(Color.WHITE); // Fondo blanco
        panelOpciones.setBorder(BorderFactory.createTitledBorder("Opciones de Generación")); // Borde con título

        JCheckBox chkPresupuesto = new JCheckBox("Generar Presupuesto aleatorio", true); // Checkbox para presupuesto (activado por defecto)
        chkPresupuesto.setBackground(Color.WHITE); // Fondo blanco
        chkPresupuesto.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente

        JCheckBox chkInversiones = new JCheckBox("Generar Inversiones aleatorias", true); // Checkbox para inversiones (activado por defecto)
        chkInversiones.setBackground(Color.WHITE); // Fondo blanco
        chkInversiones.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente

        JCheckBox chkMostrarDetalles = new JCheckBox("Mostrar valores generados", true); // Checkbox para mostrar detalles (activado por defecto)
        chkMostrarDetalles.setBackground(Color.WHITE); // Fondo blanco
        chkMostrarDetalles.setFont(new Font("Calibri", Font.PLAIN, 13)); // Fuente

        panelOpciones.add(chkPresupuesto); // Agregar checkbox presupuesto
        panelOpciones.add(chkInversiones); // Agregar checkbox inversiones
        panelOpciones.add(chkMostrarDetalles); // Agregar checkbox detalles

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10)); // Panel de botones
        panelBotones.setBackground(Color.WHITE); // Fondo blanco

        JButton btnGenerar = new JButton("🎲 Generar"); // Botón generar
        btnGenerar.setFont(new Font("Calibri", Font.BOLD, 14)); // Fuente negrita
        btnGenerar.setBackground(new Color(34, 139, 34)); // Fondo verde
        btnGenerar.setForeground(Color.WHITE); // Texto blanco
        btnGenerar.setPreferredSize(new Dimension(150, 40)); // Tamaño

        JButton btnCancelar = new JButton("Cancelar"); // Botón cancelar
        btnCancelar.setFont(new Font("Calibri", Font.BOLD, 13)); // Fuente negrita
        btnCancelar.setBackground(new Color(192, 0, 0)); // Fondo rojo
        btnCancelar.setForeground(Color.WHITE); // Texto blanco
        btnCancelar.setPreferredSize(new Dimension(120, 40)); // Tamaño

        btnGenerar.addActionListener(e -> { // Al hacer clic en generar
            generarVariablesAleatorias(chkPresupuesto.isSelected(), chkInversiones.isSelected(), chkMostrarDetalles.isSelected()); // Llamar método con opciones seleccionadas
            dialogo.dispose(); // Cerrar diálogo
        });

        btnCancelar.addActionListener(e -> dialogo.dispose()); // Al hacer clic en cancelar: cerrar diálogo

        panelBotones.add(btnGenerar); // Agregar botón generar
        panelBotones.add(btnCancelar); // Agregar botón cancelar

        panelPrincipal.add(panelInfo, BorderLayout.NORTH); // Panel info arriba
        panelPrincipal.add(panelOpciones, BorderLayout.CENTER); // Panel opciones en el centro
        panelPrincipal.add(panelBotones, BorderLayout.SOUTH); // Panel botones abajo

        dialogo.add(panelPrincipal); // Agregar panel al diálogo
        dialogo.setSize(500, 400); // Tamaño del diálogo
        dialogo.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        dialogo.setVisible(true); // Mostrar diálogo
    }

    private void generarVariablesAleatorias(boolean generarPresupuesto, boolean generarInversiones, boolean mostrarDetalles) { // Método para generar variables aleatorias
        StringBuilder detalles = new StringBuilder(); // StringBuilder para detalles
        detalles.append("Variables generadas aleatoriamente:\n\n"); // Título

        if (generarPresupuesto) { // Si se debe generar presupuesto
            double nuevoPresupuesto = Config.PRESUPUESTO_MIN + (generadorAleatorio.nextDouble() * (Config.PRESUPUESTO_MAX - Config.PRESUPUESTO_MIN)); // Generar valor aleatorio en rango
            nuevoPresupuesto = Math.round(nuevoPresupuesto / 10000.0) * 10000.0; // Redondear a múltiplos de $10,000
            Config.PRESUPUESTO = nuevoPresupuesto; // Asignar nuevo presupuesto
            detalles.append("💰 Presupuesto: ").append(FMT_MONEY.format(Config.PRESUPUESTO)).append("\n\n"); // Agregar a detalles
        }

        if (generarInversiones) { // Si se deben generar inversiones
            detalles.append("📊 Inversiones por proyecto:\n"); // Subtítulo
            for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
                double nuevaInversion = Config.INVERSION_MIN + (generadorAleatorio.nextDouble() * (Config.INVERSION_MAX - Config.INVERSION_MIN)); // Generar valor aleatorio en rango
                nuevaInversion = Math.round(nuevaInversion / 1000.0) * 1000.0; // Redondear a múltiplos de $1,000
                Config.INVERSION[i] = nuevaInversion; // Asignar nueva inversión
                detalles.append("   Proyecto ").append(i + 1).append(": ").append(FMT_MONEY.format(Config.INVERSION[i])).append("\n"); // Agregar a detalles
            }
        }

        llenarTablaInicial(); // Actualizar tabla
        actualizarLabel(lblTotales[0], "Presupuesto", Config.PRESUPUESTO); // Actualizar etiqueta presupuesto
        actualizarLabel(lblTotales[1], "Inversión", 0); // Reiniciar inversión
        actualizarLabel(lblTotales[2], "Sobrante", 0); // Reiniciar sobrante
        actualizarLabel(lblTotales[3], "Ganancia total", 0); // Reiniciar ganancia

        if (mostrarDetalles) { // Si se deben mostrar detalles
            JTextArea textArea = new JTextArea(detalles.toString()); // Crear área de texto con detalles
            textArea.setFont(new Font("Consolas", Font.PLAIN, 12)); // Fuente monoespaciada
            textArea.setEditable(false); // No editable
            textArea.setBackground(new Color(245, 245, 245)); // Fondo gris claro

            JScrollPane scrollPane = new JScrollPane(textArea); // Agregar scroll
            scrollPane.setPreferredSize(new Dimension(450, 350)); // Tamaño

            JOptionPane.showMessageDialog(this, scrollPane, "Variables Generadas", JOptionPane.INFORMATION_MESSAGE); // Mostrar diálogo con detalles
        } else { // Si no se deben mostrar detalles
            JOptionPane.showMessageDialog(this, "✅ Variables generadas correctamente\n\nPresupuesto: " + FMT_MONEY.format(Config.PRESUPUESTO), "Generación Exitosa", JOptionPane.INFORMATION_MESSAGE); // Mensaje simple
        }
    }

    private void ejecutarOptimizacion() { // Ejecutar optimización OptQuest
        btnOptimizar.setEnabled(false); // Deshabilitar botón optimizar
        btnGraficas.setEnabled(false); // Deshabilitar botón gráficas
        btnGenerarAleatorio.setEnabled(false); // Deshabilitar botón generar
        gananciasFinales.clear(); // Limpiar ganancias finales
        mejorGanancia = Double.NEGATIVE_INFINITY; // Reiniciar mejor ganancia

        new SwingWorker<Void, int[]>() { // SwingWorker para ejecutar en segundo plano
            protected Void doInBackground() { // Método ejecutado en hilo separado
                Random rand = new Random(); // Generador aleatorio

                for (int sim = 0; sim < Config.NUM_SIMULACIONES; sim++) { // Para cada simulación (213)
                    int[] decision = generarDecision(rand); // Generar decisión aleatoria
                    List<Double> ganancias = new ArrayList<>(); // Lista temporal de ganancias

                    for (int mc = 0; mc < Config.NUM_PRUEBAS_MC; mc++) { // Para cada prueba Monte Carlo (5000)
                        double ganancia = simular(decision, rand); // Simular ganancia
                        if (calcularInversion(decision) <= Config.PRESUPUESTO) { // Si cumple restricción de presupuesto
                            ganancias.add(ganancia); // Guardar ganancia válida
                        }
                        if (mc % 250 == 0) publish(new int[]{sim + 1, mc + 1}); // Actualizar progreso cada 250 pruebas
                    }

                    if (!ganancias.isEmpty()) { // Si hay ganancias válidas
                        double media = ganancias.stream().mapToDouble(d -> d).average().orElse(0); // Calcular media
                        if (media > mejorGanancia) { // Si es mejor que la mejor actual
                            mejorGanancia = media; // Actualizar mejor ganancia
                            mejorDecision = decision.clone(); // Guardar mejor decisión
                            gananciasFinales = new ArrayList<>(ganancias); // Guardar ganancias para histograma
                        }
                    }
                }
                return null; // Retornar null
            }

            protected void process(List<int[]> chunks) { // Actualizar barras de progreso (en hilo de UI)
                int[] ultimo = chunks.get(chunks.size() - 1); // Obtener última actualización
                progressBar.setValue(ultimo[0]); // Actualizar barra de simulaciones
                progressBarPruebas.setValue(ultimo[1]); // Actualizar barra de pruebas
                lblSimulaciones.setText("Simulaciones totales: " + ultimo[0] + " / " + Config.NUM_SIMULACIONES); // Actualizar etiqueta
                lblPruebas.setText("Pruebas: " + ultimo[1] + " / " + Config.NUM_PRUEBAS_MC); // Actualizar etiqueta
            }

            protected void done() { // Al terminar (en hilo de UI)
                actualizarResultados(); // Actualizar tabla con mejores resultados
                btnOptimizar.setEnabled(true); // Habilitar botón optimizar
                btnGraficas.setEnabled(true); // Habilitar botón gráficas
                btnGenerarAleatorio.setEnabled(true); // Habilitar botón generar
                mostrarVentanaResultados(); // Mostrar ventana de resultados
            }
        }.execute(); // Iniciar ejecución del worker
    }

    private int[] generarDecision(Random rand) { // Generar decisión aleatoria (0 o 1 para cada proyecto)
        int[] dec = new int[Config.NUM_PROYECTOS]; // Array de decisiones
        do { // Repetir hasta generar decisión válida
            for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
                dec[i] = rand.nextBoolean() ? 1 : 0; // Seleccionar aleatoriamente
            }
        } while (calcularInversion(dec) > Config.PRESUPUESTO && rand.nextDouble() > 0.3); // Si excede presupuesto, repetir (con probabilidad)
        return dec; // Retornar decisión
    }

    private double calcularInversion(int[] decision) { // Calcular inversión total de una decisión
        return IntStream.range(0, Config.NUM_PROYECTOS).mapToDouble(i -> decision[i] * Config.INVERSION[i]).sum(); // Sumar inversiones de proyectos seleccionados
    }

    private double simular(int[] decision, Random rand) { // Simular ganancia con una decisión
        double ganancia = 0; // Inicializar ganancia
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
            if (decision[i] == 1) { // Si está seleccionado
                double g = generarGanancia(i, rand); // Generar ganancia aleatoria según distribución
                int exito = rand.nextDouble() < Config.PROB_EXITO[i] ? 1 : 0; // Determinar éxito (aleatorio según probabilidad)
                ganancia += (g * exito) - Config.INVERSION[i]; // Ganancia = (ganancia si éxito) - inversión
            }
        }
        return ganancia; // Retornar ganancia total
    }

    private double generarGanancia(int proyecto, Random rand) { // Generar ganancia según distribución del proyecto
        return switch (proyecto) { // Switch según proyecto
            case 0 -> new NormalDistribution(750000, 75000).sample(); // Proyecto 1: Normal(750k, 75k)
            case 1 -> triangular(1250000, 1500000, 1600000, rand); // Proyecto 2: Triangular(1.25M, 1.5M, 1.6M)
            case 2 -> logNormal(600000, 50000, rand); // Proyecto 3: LogNormal(600k, 50k)
            case 3 -> triangular(1600000, 1800000, 1900000, rand); // Proyecto 4: Triangular(1.6M, 1.8M, 1.9M)
            case 4 -> new UniformRealDistribution(1150000, 1350000).sample(); // Proyecto 5: Uniforme(1.15M, 1.35M)
            case 5 -> logNormal(150000, 30000, rand); // Proyecto 6: LogNormal(150k, 30k)
            case 6 -> new NormalDistribution(900000, 50000).sample(); // Proyecto 7: Normal(900k, 50k)
            case 7 -> triangular(220000, 250000, 320000, rand); // Proyecto 8: Triangular(220k, 250k, 320k)
            default -> 0; // Default: 0
        };
    }

    private double triangular(double min, double mode, double max, Random rand) { // Distribución triangular
        double u = rand.nextDouble(); // Uniforme [0,1]
        double f = (mode - min) / (max - min); // Factor
        return u < f ? min + Math.sqrt(u * (max - min) * (mode - min)) : max - Math.sqrt((1 - u) * (max - min) * (max - mode)); // Fórmula triangular
    }

    private double logNormal(double media, double std, Random rand) { // Distribución log-normal
        double var = std * std; // Varianza
        double mu = Math.log(media * media / Math.sqrt(var + media * media)); // Parámetro mu
        double sigma = Math.sqrt(Math.log(1 + var / (media * media))); // Parámetro sigma
        return new LogNormalDistribution(mu, sigma).sample(); // Retornar muestra
    }

    private void actualizarResultados() { // Actualizar tabla y etiquetas con mejores resultados
        double inv = calcularInversion(mejorDecision); // Calcular inversión de mejor decisión

        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
            double retorno = Config.GANANCIA[i] * Config.PROB_EXITO[i]; // Retorno esperado
            double beneficio = (retorno - Config.INVERSION[i]) * mejorDecision[i]; // Beneficio (solo si está seleccionado)
            modeloTabla.setValueAt(String.valueOf(mejorDecision[i]), i, 1); // Actualizar decisión (0 o 1)
            modeloTabla.setValueAt(FMT_MONEY.format(Config.INVERSION[i]), i, 5); // Actualizar inversión (puede haber cambiado)
            modeloTabla.setValueAt(FMT_MONEY.format(beneficio), i, 6); // Actualizar beneficio
        }

        actualizarLabel(lblTotales[1], "Inversión", inv); // Actualizar inversión total
        actualizarLabel(lblTotales[2], "Sobrante", Config.PRESUPUESTO - inv); // Actualizar sobrante
        actualizarLabel(lblTotales[3], "Ganancia total", mejorGanancia); // Actualizar ganancia total
    }

    private void actualizarLabel(JLabel lbl, String titulo, double valor) { // Actualizar etiqueta con HTML
        lbl.setText("<html><center>" + titulo + "<br><b>" + FMT_MONEY.format(valor) + "</b></center></html>"); // Título y valor formateado
    }

    private void mostrarVentanaResultados() { // Mostrar ventana con resultados de optimización
        JDialog dlg = new JDialog(this, "Resultados de OptQuest", false); // Diálogo no modal
        dlg.setLayout(new BorderLayout()); // BorderLayout

        JPanel main = crearPanelConMargen(new BorderLayout(10, 10), 15, 15); // Panel principal con margen
        main.add(new JLabel("Optimización terminada. Todas las variables enumeradas."), BorderLayout.NORTH); // Título

        JPanel tablas = new JPanel(new GridLayout(3, 1, 5, 5)); // GridLayout para 3 secciones
        tablas.setBackground(Color.WHITE); // Fondo blanco
        tablas.add(crearSeccionResultado("Objetivos", "Valor", new String[]{"Maximizar Media Total profit"}, new String[]{FMT_MONEY.format(mejorGanancia)})); // Sección objetivos
        tablas.add(crearSeccionResultado("Restricciones", "Lado izq <= Lado der", new String[]{"Inversión <= Presupuesto"}, new String[]{FMT_MONEY.format(calcularInversion(mejorDecision)) + " <= " + FMT_MONEY.format(Config.PRESUPUESTO)})); // Sección restricciones

        String[][] vars = new String[Config.NUM_PROYECTOS][2]; // Array de variables de decisión
        for (int i = 0; i < Config.NUM_PROYECTOS; i++) { // Para cada proyecto
            vars[i] = new String[]{"Project " + (i + 1), String.valueOf(mejorDecision[i])}; // Nombre y valor (0 o 1)
        }
        tablas.add(crearTablaVariables(vars)); // Tabla de variables

        main.add(tablas, BorderLayout.CENTER); // Agregar tablas al centro
        dlg.add(main); // Agregar panel al diálogo
        dlg.setSize(700, 480); // Tamaño
        dlg.setLocationRelativeTo(this); // Centrar
        dlg.setVisible(true); // Mostrar
    }

    private JPanel crearSeccionResultado(String titulo, String col, String[] filas, String[] vals) { // Crear sección de resultados
        JPanel panel = new JPanel(new BorderLayout()); // BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder(titulo)); // Borde con título
        panel.setBackground(Color.WHITE); // Fondo blanco

        if (filas.length > 0) { // Si hay filas
            JPanel grid = new JPanel(new GridLayout(filas.length, 2, 10, 5)); // GridLayout
            grid.setBackground(Color.WHITE); // Fondo blanco
            for (int i = 0; i < filas.length; i++) { // Para cada fila
                grid.add(new JLabel(filas[i])); // Etiqueta descripción
                grid.add(new JLabel(vals[i], SwingConstants.RIGHT)); // Etiqueta valor (alineada derecha)
            }
            panel.add(grid); // Agregar grid
        }
        return panel; // Retornar panel
    }

    private JPanel crearTablaVariables(String[][] datos) { // Crear tabla de variables de decisión
        JPanel panel = new JPanel(new BorderLayout()); // BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder("Variables de decisión")); // Borde con título
        DefaultTableModel modelo = new DefaultTableModel(new String[]{"", "Valor"}, 0); // Modelo de tabla
        Arrays.stream(datos).forEach(modelo::addRow); // Agregar todas las filas
        panel.add(new JScrollPane(new JTable(modelo))); // Agregar tabla con scroll
        return panel; // Retornar panel
    }

    private void mostrarHistograma() { // Mostrar histograma de ganancias
        double[] datos = gananciasFinales.stream().mapToDouble(d -> d).toArray(); // Convertir lista a array
        if (datos.length == 0) return; // Si no hay datos, salir

        HistogramDataset dataset = new HistogramDataset(); // Dataset de histograma
        dataset.addSeries("Total profit", datos, 50); // Agregar serie con 50 bins

        JFreeChart chart = ChartFactory.createHistogram("Total profit", "Dollars", "Frecuencia", dataset, PlotOrientation.VERTICAL, false, true, false); // Crear histograma

        XYPlot plot = chart.getXYPlot(); // Obtener plot
        plot.setBackgroundPaint(Color.WHITE); // Fondo blanco
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Renderizador de barras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Color azul
        renderer.setShadowVisible(false); // Sin sombras

        double media = Arrays.stream(datos).average().orElse(0); // Calcular media
        plot.addDomainMarker(new org.jfree.chart.plot.ValueMarker(media) {{ // Agregar marcador en la media (inicializador anónimo)
            setPaint(Color.BLACK); // Color negro
            setLabel("Media = " + FMT_MONEY.format(media)); // Etiqueta con valor
        }});

        ((NumberAxis) plot.getDomainAxis()).setNumberFormatOverride(FMT_MONEY); // Formato monetario en eje X

        JFrame frame = new JFrame("Previsión: Total profit"); // Ventana para histograma
        frame.setContentPane(new ChartPanel(chart)); // Panel del gráfico
        frame.setSize(900, 600); // Tamaño
        frame.setLocationRelativeTo(this); // Centrar
        frame.setVisible(true); // Mostrar
    }

    public static void main(String[] args) { // Método main - punto de entrada
        try { UIManager.setLookAndFeel(new FlatLightLaf()); } // Establecer tema FlatLaf
        catch (Exception e) { e.printStackTrace(); } // Capturar excepciones

        SwingUtilities.invokeLater(() -> new OptimizadorSeleccionProyectosAleatorio().setVisible(true)); // Crear y mostrar ventana en hilo de Swing
    }
}