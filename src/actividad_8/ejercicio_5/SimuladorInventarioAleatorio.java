package actividad_8.ejercicio_5; // Declaración del paquete

import com.formdev.flatlaf.FlatLightLaf; // Tema visual moderno para la interfaz
import org.jfree.chart.ChartFactory; // Fábrica para crear gráficos
import org.jfree.chart.ChartPanel; // Panel que contiene el gráfico
import org.jfree.chart.JFreeChart; // Clase principal del gráfico
import org.jfree.chart.plot.PlotOrientation; // Orientación del gráfico (vertical/horizontal)
import org.jfree.chart.plot.XYPlot; // Plot para gráficos XY
import org.jfree.chart.renderer.xy.XYBarRenderer; // Renderizador de barras para gráficos XY
import org.jfree.data.statistics.HistogramDataset; // Dataset para histogramas

import javax.swing.*; // Componentes de interfaz gráfica Swing
import javax.swing.table.DefaultTableCellRenderer; // Renderizador de celdas de tabla
import javax.swing.table.DefaultTableModel; // Modelo de datos para tablas
import java.awt.*; // Componentes gráficos y layouts
import java.text.DecimalFormat; // Formateo de números decimales
import java.util.Arrays; // Utilidades para arrays
import java.util.Random; // Generador de números aleatorios

/**
 * Simulador de Optimización de Inventario con Valores Aleatorios
 * Los parámetros se generan aleatoriamente al iniciar
 */
public class SimuladorInventarioAleatorio extends JFrame { // Clase principal que extiende JFrame

    private double precioVenta; // Precio de venta del producto
    private double costo; // Costo del producto
    private double precioDescuento; // Precio con descuento aplicado
    private int demandaMin; // Demanda mínima posible
    private int demandaMax; // Demanda máxima posible
    private int paso; // Incremento entre valores de demanda
    private double probabilidad; // Probabilidad uniforme para cada valor
    private double cantidadComprada; // Cantidad de productos comprados
    private int demandaEjemplo; // Demanda de ejemplo para mostrar en la tabla

    private double[] resultadosGanancia; // Array con resultados de ganancia de todas las iteraciones
    private JTable tabla; // Tabla para mostrar parámetros
    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    private JTextArea txtDistribucion; // Área de texto para mostrar distribución
    private JLabel lblEstado; // Etiqueta para mostrar estado de simulación
    private JLabel lblMedia; // Etiqueta para mostrar media
    private JLabel lblMediana; // Etiqueta para mostrar mediana
    private JLabel lblModo; // Etiqueta para mostrar moda
    private JLabel lblDesviacion; // Etiqueta para mostrar desviación estándar
    private JLabel lblVarianza; // Etiqueta para mostrar varianza
    private JLabel lblMin; // Etiqueta para mostrar valor mínimo
    private JLabel lblMax; // Etiqueta para mostrar valor máximo

    public SimuladorInventarioAleatorio() { // Constructor de la clase principal
        super("Simulador de Inventario - Valores Aleatorios"); // Título de la ventana
        generarParametrosAleatorios(); // Generar parámetros aleatorios
        configurarUI(); // Configurar interfaz de usuario
        setSize(1400, 900); // Establecer tamaño de ventana
        setLocationRelativeTo(null); // Centrar ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
    }

    private void generarParametrosAleatorios() { // Método para generar parámetros aleatorios
        Random rand = new Random(); // Crear generador de números aleatorios

        precioVenta = 10.0 + rand.nextDouble() * 10.0; // Generar precio de venta entre $10 y $20

        costo = precioVenta * (0.5 + rand.nextDouble() * 0.3); // Generar costo entre 50% y 80% del precio de venta

        precioDescuento = costo + rand.nextDouble() * (precioVenta - costo) * 0.5; // Generar precio con descuento entre costo y precio de venta

        demandaMin = 20 + rand.nextInt(30); // Generar demanda mínima entre 20 y 50
        paso = 5 + rand.nextInt(11); // Generar paso entre 5 y 15
        int numValores = 4 + rand.nextInt(4); // Generar número de valores entre 4 y 7
        demandaMax = demandaMin + (paso * (numValores - 1)); // Calcular demanda máxima

        probabilidad = 1.0 / numValores; // Calcular probabilidad uniforme

        cantidadComprada = demandaMin + rand.nextInt(demandaMax - demandaMin + 1); // Generar cantidad comprada en el rango

        int[] valoresPosibles = generarValoresPosibles(); // Obtener valores posibles de demanda
        demandaEjemplo = valoresPosibles[rand.nextInt(valoresPosibles.length)]; // Seleccionar demanda de ejemplo aleatoria
    }

    private int[] generarValoresPosibles() { // Método para generar array de valores posibles
        int numValores = (demandaMax - demandaMin) / paso + 1; // Calcular cantidad de valores
        int[] valores = new int[numValores]; // Crear array de valores
        int idx = 0; // Inicializar índice
        for (int valor = demandaMin; valor <= demandaMax; valor += paso) { // Iterar desde min hasta max con paso
            valores[idx++] = valor; // Agregar valor al array
        }
        return valores; // Retornar array de valores
    }

    private void configurarUI() { // Método para configurar interfaz de usuario
        setLayout(new BorderLayout(10, 10)); // Establecer layout BorderLayout con espaciado

        JPanel panelSuperior = crearPanelParametros(); // Crear panel de parámetros
        JPanel panelCentral = crearPanelSimulacion(); // Crear panel de simulación
        JPanel panelInferior = crearPanelEstadisticas(); // Crear panel de estadísticas

        add(panelSuperior, BorderLayout.NORTH); // Agregar panel superior al norte
        add(panelCentral, BorderLayout.CENTER); // Agregar panel central al centro
        add(panelInferior, BorderLayout.SOUTH); // Agregar panel inferior al sur
    }

    private JPanel crearPanelParametros() { // Método para crear panel de parámetros
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crear panel con BorderLayout
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Agregar margen vacío

        String[] columnas = {"Parámetros", "Valores"}; // Nombres de columnas
        modeloTabla = new DefaultTableModel(columnas, 0) { // Crear modelo de tabla
            @Override
            public boolean isCellEditable(int row, int col) { // Sobrescribir método de edición
                return false; // Hacer todas las celdas no editables
            }
        };

        tabla = new JTable(modeloTabla); // Crear tabla con el modelo
        configurarTabla(); // Configurar formato de tabla
        llenarTabla(); // Llenar tabla con datos

        JScrollPane scrollTabla = new JScrollPane(tabla); // Crear panel con scroll para tabla
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Parámetros del Problema (Generados Aleatoriamente)")); // Agregar borde con título
        scrollTabla.setPreferredSize(new Dimension(600, 250)); // Establecer tamaño preferido

        JPanel panelTablaYBoton = new JPanel(new BorderLayout(5, 5)); // Crear panel para tabla y botón
        panelTablaYBoton.add(scrollTabla, BorderLayout.CENTER); // Agregar scroll de tabla al centro

        JButton btnRegenerar = new JButton("🔄 Regenerar Parámetros"); // Crear botón para regenerar
        btnRegenerar.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Establecer fuente
        btnRegenerar.setBackground(new Color(255, 153, 0)); // Color de fondo naranja
        btnRegenerar.setForeground(Color.WHITE); // Color de texto blanco
        btnRegenerar.setFocusPainted(false); // Quitar borde de foco
        btnRegenerar.addActionListener(e -> regenerarParametros()); // Agregar listener para regenerar
        panelTablaYBoton.add(btnRegenerar, BorderLayout.SOUTH); // Agregar botón al sur

        panel.add(panelTablaYBoton, BorderLayout.WEST); // Agregar panel tabla y botón al oeste

        JPanel panelDistribucion = crearPanelDistribucion(); // Crear panel de distribución
        panel.add(panelDistribucion, BorderLayout.CENTER); // Agregar panel de distribución al centro

        return panel; // Retornar panel completo
    }

    private void regenerarParametros() { // Método para regenerar parámetros
        generarParametrosAleatorios(); // Generar nuevos parámetros aleatorios
        modeloTabla.setRowCount(0); // Limpiar todas las filas de la tabla
        llenarTabla(); // Volver a llenar tabla con nuevos datos
        actualizarDistribucion(); // Actualizar texto de distribución
        limpiarEstadisticas(); // Limpiar estadísticas mostradas
        JOptionPane.showMessageDialog(this, // Mostrar diálogo de confirmación
            "Parámetros regenerados exitosamente.\nEjecute la simulación para ver los nuevos resultados.",
            "Parámetros Actualizados",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void limpiarEstadisticas() { // Método para limpiar estadísticas
        lblEstado.setText("Pendiente"); // Cambiar estado a pendiente
        lblEstado.setForeground(Color.GRAY); // Color gris
        lblMedia.setText("--"); // Limpiar media
        lblMediana.setText("--"); // Limpiar mediana
        lblModo.setText("--"); // Limpiar moda
        lblDesviacion.setText("--"); // Limpiar desviación
        lblVarianza.setText("--"); // Limpiar varianza
        lblMin.setText("--"); // Limpiar mínimo
        lblMax.setText("--"); // Limpiar máximo
        resultadosGanancia = null; // Limpiar resultados de ganancia
    }

    private void configurarTabla() { // Método para configurar apariencia de tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Fuente de la tabla
        tabla.setRowHeight(28); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del encabezado
        tabla.getTableHeader().setBackground(new Color(255, 153, 0)); // Color de fondo encabezado (naranja)
        tabla.getTableHeader().setForeground(Color.WHITE); // Color de texto encabezado (blanco)
        tabla.setEnabled(false); // Deshabilitar edición de tabla

        DefaultTableCellRenderer valueRenderer = new DefaultTableCellRenderer() { // Renderizador personalizado
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, // Sobrescribir método
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Obtener componente

                if (row == 3 && column == 1) { // Si es fila de demanda
                    setBackground(new Color(146, 208, 80)); // Fondo verde claro
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente en negrita
                } else if (row == 4 && column == 1) { // Si es fila de cantidad comprada
                    setBackground(new Color(0, 176, 240)); // Fondo azul claro
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente en negrita
                } else if (row == 6 && column == 1) { // Si es fila de ganancia
                    setBackground(new Color(0, 176, 240)); // Fondo azul claro
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente en negrita
                } else { // Para otras filas
                    setBackground(Color.WHITE); // Fondo blanco
                    setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Fuente normal
                }

                if (column == 0) { // Si es columna de parámetros
                    setFont(new Font("Segoe UI", Font.BOLD, 13)); // Fuente en negrita
                }

                return c; // Retornar componente
            }
        };

        tabla.getColumnModel().getColumn(1).setCellRenderer(valueRenderer); // Aplicar renderizador a columna de valores
        tabla.getColumnModel().getColumn(0).setPreferredWidth(250); // Ancho columna parámetros
        tabla.getColumnModel().getColumn(1).setPreferredWidth(150); // Ancho columna valores
    }

    private void llenarTabla() { // Método para llenar tabla con datos
        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00"); // Formato monetario con decimales
        DecimalFormat dfInt = new DecimalFormat("$ #,##0"); // Formato monetario sin decimales

        double gananciaEjemplo = calcularGanancia(demandaEjemplo, cantidadComprada); // Calcular ganancia de ejemplo

        modeloTabla.addRow(new Object[]{"Precio de venta", dfMoney.format(precioVenta)}); // Agregar fila precio de venta
        modeloTabla.addRow(new Object[]{"Costo", dfMoney.format(costo)}); // Agregar fila costo
        modeloTabla.addRow(new Object[]{"Precio con descuento", dfMoney.format(precioDescuento)}); // Agregar fila precio con descuento
        modeloTabla.addRow(new Object[]{"Demanda", dfInt.format(demandaEjemplo)}); // Agregar fila demanda
        modeloTabla.addRow(new Object[]{"Cantidad comprada", dfInt.format(cantidadComprada)}); // Agregar fila cantidad comprada
        modeloTabla.addRow(new Object[]{"-", "-"}); // Agregar fila separadora
        modeloTabla.addRow(new Object[]{"Ganancia", dfMoney.format(gananciaEjemplo)}); // Agregar fila ganancia
    }

    private JPanel crearPanelDistribucion() { // Método para crear panel de distribución
        JPanel panel = new JPanel(new BorderLayout()); // Crear panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder("Distribución Personalizada - Demanda")); // Agregar borde con título

        txtDistribucion = new JTextArea(); // Crear área de texto
        txtDistribucion.setEditable(false); // Hacer área de texto no editable
        txtDistribucion.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establecer fuente
        txtDistribucion.setBackground(new Color(255, 255, 224)); // Color de fondo amarillo claro

        actualizarDistribucion(); // Actualizar texto de distribución

        JScrollPane scroll = new JScrollPane(txtDistribucion); // Crear scroll para área de texto
        panel.add(scroll, BorderLayout.CENTER); // Agregar scroll al centro

        return panel; // Retornar panel completo
    }

    private void actualizarDistribucion() { // Método para actualizar texto de distribución
        StringBuilder sb = new StringBuilder(); // Crear StringBuilder para construir texto
        sb.append("Distribución Discreta Uniforme:\n\n"); // Agregar título
        sb.append(String.format("Rango: $%d - $%d\n", demandaMin, demandaMax)); // Agregar rango
        sb.append(String.format("Paso: $%d\n", paso)); // Agregar paso
        sb.append(String.format("Probabilidad por valor: %.4f (%.2f%%)\n\n", probabilidad, probabilidad * 100)); // Agregar probabilidad
        sb.append("Valores posibles:\n"); // Agregar encabezado de valores

        for (int valor = demandaMin; valor <= demandaMax; valor += paso) { // Iterar sobre valores posibles
            sb.append(String.format("  $%d - Prob: %.2f%%\n", valor, probabilidad * 100)); // Agregar cada valor con su probabilidad
        }

        txtDistribucion.setText(sb.toString()); // Establecer texto en área de texto
    }

    private JPanel crearPanelSimulacion() { // Método para crear panel de simulación
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 30)); // Crear panel con FlowLayout
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Agregar margen vacío

        JLabel lblSimulaciones = new JLabel("Número de Simulaciones:"); // Crear etiqueta
        lblSimulaciones.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establecer fuente

        JSpinner spinnerSimulaciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 100000, 1000)); // Crear spinner para número de iteraciones
        spinnerSimulaciones.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Establecer fuente

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo"); // Crear botón para simular
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Establecer fuente
        btnSimular.setBackground(new Color(30, 144, 255)); // Color de fondo azul
        btnSimular.setForeground(Color.WHITE); // Color de texto blanco
        btnSimular.setFocusPainted(false); // Quitar borde de foco
        btnSimular.setPreferredSize(new Dimension(350, 50)); // Establecer tamaño preferido

        panel.add(lblSimulaciones); // Agregar etiqueta al panel
        panel.add(spinnerSimulaciones); // Agregar spinner al panel
        panel.add(btnSimular); // Agregar botón al panel

        btnSimular.addActionListener(e -> { // Agregar listener al botón
            int numSimulaciones = (int) spinnerSimulaciones.getValue(); // Obtener número de simulaciones
            ejecutarSimulacion(numSimulaciones); // Ejecutar simulación
        });

        return panel; // Retornar panel completo
    }

    private JPanel crearPanelEstadisticas() { // Método para crear panel de estadísticas
        JPanel panel = new JPanel(new GridLayout(2, 4, 15, 10)); // Crear panel con GridLayout
        panel.setBorder(BorderFactory.createCompoundBorder( // Agregar borde compuesto
            BorderFactory.createTitledBorder("Resultados de la Simulación"), // Borde con título
            BorderFactory.createEmptyBorder(10, 10, 10, 10) // Margen interno
        ));
        panel.setBackground(Color.WHITE); // Fondo blanco

        Font fuenteLabel = new Font("Segoe UI", Font.BOLD, 13); // Fuente para etiquetas
        Font fuenteValor = new Font("Segoe UI", Font.PLAIN, 13); // Fuente para valores

        lblEstado = new JLabel("Pendiente", SwingConstants.CENTER); // Etiqueta de estado
        lblMedia = new JLabel("--", SwingConstants.CENTER); // Etiqueta de media
        lblMediana = new JLabel("--", SwingConstants.CENTER); // Etiqueta de mediana
        lblModo = new JLabel("--", SwingConstants.CENTER); // Etiqueta de moda
        lblDesviacion = new JLabel("--", SwingConstants.CENTER); // Etiqueta de desviación
        lblVarianza = new JLabel("--", SwingConstants.CENTER); // Etiqueta de varianza
        lblMin = new JLabel("--", SwingConstants.CENTER); // Etiqueta de mínimo
        lblMax = new JLabel("--", SwingConstants.CENTER); // Etiqueta de máximo

        lblEstado.setFont(fuenteValor); // Aplicar fuente
        lblMedia.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Aplicar fuente en negrita
        lblMediana.setFont(fuenteValor); // Aplicar fuente
        lblModo.setFont(fuenteValor); // Aplicar fuente
        lblDesviacion.setFont(fuenteValor); // Aplicar fuente
        lblVarianza.setFont(fuenteValor); // Aplicar fuente
        lblMin.setFont(fuenteValor); // Aplicar fuente
        lblMax.setFont(fuenteValor); // Aplicar fuente

        panel.add(crearEtiqueta("Estado:", fuenteLabel)); // Agregar etiqueta "Estado:"
        panel.add(crearEtiqueta("Media:", fuenteLabel)); // Agregar etiqueta "Media:"
        panel.add(crearEtiqueta("Mediana:", fuenteLabel)); // Agregar etiqueta "Mediana:"
        panel.add(crearEtiqueta("Modo:", fuenteLabel)); // Agregar etiqueta "Modo:"

        panel.add(lblEstado); // Agregar etiqueta de estado
        panel.add(lblMedia); // Agregar etiqueta de media
        panel.add(lblMediana); // Agregar etiqueta de mediana
        panel.add(lblModo); // Agregar etiqueta de moda

        panel.add(crearEtiqueta("Desv. Est.:", fuenteLabel)); // Agregar etiqueta "Desv. Est.:"
        panel.add(crearEtiqueta("Varianza:", fuenteLabel)); // Agregar etiqueta "Varianza:"
        panel.add(crearEtiqueta("Mínimo:", fuenteLabel)); // Agregar etiqueta "Mínimo:"
        panel.add(crearEtiqueta("Máximo:", fuenteLabel)); // Agregar etiqueta "Máximo:"

        panel.add(lblDesviacion); // Agregar etiqueta de desviación
        panel.add(lblVarianza); // Agregar etiqueta de varianza
        panel.add(lblMin); // Agregar etiqueta de mínimo
        panel.add(lblMax); // Agregar etiqueta de máximo

        return panel; // Retornar panel completo
    }

    private JLabel crearEtiqueta(String texto, Font fuente) { // Método auxiliar para crear etiquetas
        JLabel lbl = new JLabel(texto, SwingConstants.CENTER); // Crear etiqueta centrada
        lbl.setFont(fuente); // Aplicar fuente
        return lbl; // Retornar etiqueta
    }

    private int generarDemanda(Random random) { // Método para generar demanda aleatoria
        int[] valoresPosibles = generarValoresPosibles(); // Obtener valores posibles
        return valoresPosibles[random.nextInt(valoresPosibles.length)]; // Retornar valor aleatorio del array
    }

    private double calcularGanancia(int demanda, double cantidad) { // Método para calcular ganancia
        double margenVenta = precioVenta - precioDescuento; // Calcular margen de venta
        double margenDescuento = precioDescuento - costo; // Calcular margen con descuento

        if (demanda <= cantidad) { // Si la demanda es menor o igual a la cantidad
            return margenVenta * demanda - margenDescuento * cantidad; // Calcular ganancia con inventario sobrante
        } else { // Si la demanda es mayor que la cantidad
            return margenVenta * cantidad; // Calcular ganancia vendiendo todo el inventario
        }
    }

    private void ejecutarSimulacion(int iteraciones) { // Método para ejecutar simulación Monte Carlo
        lblEstado.setText("Simulando..."); // Cambiar estado a "Simulando..."
        lblEstado.setForeground(Color.ORANGE); // Color naranja

        SwingWorker<double[], Void> worker = new SwingWorker<>() { // Worker para ejecutar en segundo plano
            @Override
            protected double[] doInBackground() { // Método ejecutado en hilo separado
                resultadosGanancia = new double[iteraciones]; // Inicializar array de resultados
                Random random = new Random(); // Crear generador aleatorio

                for (int i = 0; i < iteraciones; i++) { // Ejecutar cada iteración
                    int demanda = generarDemanda(random); // Generar demanda aleatoria
                    double ganancia = calcularGanancia(demanda, cantidadComprada); // Calcular ganancia
                    resultadosGanancia[i] = ganancia; // Guardar resultado
                }

                return resultadosGanancia; // Retornar resultados
            }

            @Override
            protected void done() { // Método ejecutado al terminar (en hilo de UI)
                try {
                    double[] resultados = get(); // Obtener resultados
                    actualizarEstadisticas(resultados); // Actualizar estadísticas
                    actualizarTabla(); // Actualizar tabla
                    mostrarHistograma(resultados, iteraciones); // Mostrar histograma
                } catch (Exception ex) { // Capturar excepciones
                    ex.printStackTrace(); // Imprimir stack trace
                    JOptionPane.showMessageDialog(SimuladorInventarioAleatorio.this, // Mostrar diálogo de error
                        "Error en la simulación: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };

        worker.execute(); // Iniciar ejecución del worker
    }

    private void actualizarEstadisticas(double[] resultados) { // Método para actualizar estadísticas
        DecimalFormat df = new DecimalFormat("$#,##0.00"); // Formato monetario

        double suma = Arrays.stream(resultados).sum(); // Sumar todos los resultados
        double media = suma / resultados.length; // Calcular media

        double varianza = Arrays.stream(resultados) // Stream de resultados
            .map(x -> Math.pow(x - media, 2)) // Elevar al cuadrado diferencia con media
            .sum() / resultados.length; // Sumar y dividir entre cantidad
        double desviacion = Math.sqrt(varianza); // Calcular desviación estándar

        double[] ordenados = resultados.clone(); // Clonar array de resultados
        Arrays.sort(ordenados); // Ordenar array
        double mediana; // Variable para mediana
        if (ordenados.length % 2 == 0) { // Si la cantidad es par
            mediana = (ordenados[ordenados.length / 2 - 1] + ordenados[ordenados.length / 2]) / 2; // Promedio de valores centrales
        } else { // Si la cantidad es impar
            mediana = ordenados[ordenados.length / 2]; // Valor central
        }

        double min = Arrays.stream(resultados).min().orElse(0); // Obtener valor mínimo
        double max = Arrays.stream(resultados).max().orElse(0); // Obtener valor máximo
        double modo = calcularModo(resultados); // Calcular moda

        lblEstado.setText("Completado (" + resultados.length + " pruebas)"); // Actualizar estado
        lblEstado.setForeground(new Color(0, 150, 0)); // Color verde
        lblMedia.setText(df.format(media)); // Mostrar media formateada
        lblMedia.setForeground(new Color(0, 100, 200)); // Color azul
        lblMediana.setText(df.format(mediana)); // Mostrar mediana formateada
        lblModo.setText(df.format(modo)); // Mostrar moda formateada
        lblDesviacion.setText(df.format(desviacion)); // Mostrar desviación formateada
        lblVarianza.setText(df.format(varianza)); // Mostrar varianza formateada
        lblMin.setText(df.format(min)); // Mostrar mínimo formateado
        lblMax.setText(df.format(max)); // Mostrar máximo formateado
    }

    private double calcularModo(double[] datos) { // Método para calcular la moda
        Arrays.sort(datos); // Ordenar datos
        double moda = datos[0]; // Inicializar moda con primer valor
        int maxFrecuencia = 1; // Inicializar frecuencia máxima
        int frecuenciaActual = 1; // Inicializar frecuencia actual
        double valorActual = datos[0]; // Inicializar valor actual

        for (int i = 1; i < datos.length; i++) { // Iterar sobre datos
            if (Math.abs(datos[i] - valorActual) < 1.0) { // Si el valor es similar al actual
                frecuenciaActual++; // Incrementar frecuencia actual
            } else { // Si el valor es diferente
                if (frecuenciaActual > maxFrecuencia) { // Si frecuencia actual es mayor que máxima
                    maxFrecuencia = frecuenciaActual; // Actualizar frecuencia máxima
                    moda = valorActual; // Actualizar moda
                }
                valorActual = datos[i]; // Actualizar valor actual
                frecuenciaActual = 1; // Reiniciar frecuencia actual
            }
        }

        if (frecuenciaActual > maxFrecuencia) { // Verificar última secuencia
            moda = valorActual; // Actualizar moda si es necesario
        }

        return moda; // Retornar moda
    }

    private void actualizarTabla() { // Método para actualizar tabla
        DecimalFormat df = new DecimalFormat("$#,##0.00"); // Formato monetario

        if (resultadosGanancia != null && resultadosGanancia.length > 0) { // Si hay resultados disponibles
            double media = Arrays.stream(resultadosGanancia).average().orElse(0); // Calcular media
            modeloTabla.setValueAt(df.format(media), 6, 1); // Actualizar celda de ganancia con media
        }
    }

    private void mostrarHistograma(double[] datos, int numSimulaciones) { // Método para mostrar histograma
        HistogramDataset dataset = new HistogramDataset(); // Crear dataset de histograma
        dataset.addSeries("Ganancia", datos, 50); // Agregar datos con 50 bins

        JFreeChart chart = ChartFactory.createHistogram( // Crear gráfico de histograma
            "Distribución de Ganancia", // Título
            "Ganancia ($)", // Etiqueta eje X
            "Frecuencia", // Etiqueta eje Y
            dataset, // Dataset
            PlotOrientation.VERTICAL, // Orientación vertical
            false, // Sin leyenda
            true, // Con tooltips
            false // Sin URLs
        );

        XYPlot plot = chart.getXYPlot(); // Obtener plot del gráfico
        plot.setBackgroundPaint(Color.WHITE); // Color de fondo blanco
        plot.setDomainGridlinePaint(new Color(200, 200, 200)); // Color de líneas de cuadrícula eje X (gris claro)
        plot.setRangeGridlinePaint(new Color(200, 200, 200)); // Color de líneas de cuadrícula eje Y (gris claro)

        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer(); // Obtener renderizador de barras
        renderer.setSeriesPaint(0, new Color(0, 112, 192)); // Color de barras (azul)
        renderer.setBarPainter(new org.jfree.chart.renderer.xy.StandardXYBarPainter()); // Pintor estándar
        renderer.setShadowVisible(false); // Desactivar sombras

        chart.setBackgroundPaint(Color.WHITE); // Color de fondo del gráfico (blanco)

        DecimalFormat df = new DecimalFormat("$#,##0.00"); // Formato monetario
        double media = Arrays.stream(datos).average().orElse(0); // Calcular media
        chart.addSubtitle(new org.jfree.chart.title.TextTitle( // Agregar subtítulo
            String.format("%d pruebas | Media: %s | Certeza: 100.00%%", // Texto formateado
                numSimulaciones, df.format(media)),
            new Font("Segoe UI", Font.PLAIN, 12) // Fuente del subtítulo
        ));

        JFrame frameHistograma = new JFrame("Vista de Frecuencia - Ganancia"); // Crear ventana para histograma
        ChartPanel chartPanel = new ChartPanel(chart); // Crear panel del gráfico
        chartPanel.setPreferredSize(new Dimension(900, 600)); // Establecer tamaño preferido

        frameHistograma.setContentPane(chartPanel); // Establecer panel como contenido
        frameHistograma.pack(); // Ajustar tamaño al contenido
        frameHistograma.setLocationRelativeTo(this); // Centrar respecto a ventana principal
        frameHistograma.setVisible(true); // Hacer visible la ventana
    }

    public static void main(String[] args) { // Método main - punto de entrada
        try {
            UIManager.setLookAndFeel(new FlatLightLaf()); // Establecer Look and Feel FlatLaf
        } catch (Exception e) { // Capturar excepciones
            e.printStackTrace(); // Imprimir error
        }

        SwingUtilities.invokeLater(() -> { // Ejecutar en hilo de eventos de Swing
            SimuladorInventarioAleatorio simulador = new SimuladorInventarioAleatorio(); // Crear instancia del simulador
            simulador.setVisible(true); // Hacer visible la ventana
        });
    }
}
