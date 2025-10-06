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
 * Simulador de Optimización de Inventario (EDITABLE)
 * Permite modificar todos los parámetros
 */
public class SimuladorInventarioEditable extends JFrame { // Clase principal que extiende JFrame

    private double precioVenta = 12.00; // Precio de venta del producto (valor inicial)
    private double costo = 7.50; // Costo del producto (valor inicial)
    private double precioDescuento = 6.00; // Precio con descuento aplicado (valor inicial)
    private int demandaMin = 40; // Demanda mínima posible (valor inicial)
    private int demandaMax = 90; // Demanda máxima posible (valor inicial)
    private int paso = 10; // Incremento entre valores de demanda (valor inicial)
    private double cantidadComprada = 90; // Cantidad de productos comprados (valor inicial)
    private int demandaEjemplo = 40; // Demanda de ejemplo para mostrar en la tabla (valor inicial)

    private double[] resultadosGanancia; // Array con resultados de ganancia de todas las iteraciones
    private JTable tabla; // Tabla para mostrar parámetros
    private DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    private JLabel lblEstado; // Etiqueta para mostrar estado de simulación
    private JLabel lblMedia; // Etiqueta para mostrar media
    private JLabel lblMediana; // Etiqueta para mostrar mediana
    private JLabel lblModo; // Etiqueta para mostrar moda
    private JLabel lblDesviacion; // Etiqueta para mostrar desviación estándar
    private JLabel lblVarianza; // Etiqueta para mostrar varianza
    private JLabel lblMin; // Etiqueta para mostrar valor mínimo
    private JLabel lblMax; // Etiqueta para mostrar valor máximo
    private JSpinner spinnerCantidad; // Spinner para seleccionar cantidad a comprar
    private JTextArea txtDistribucion; // Área de texto para mostrar distribución

    public SimuladorInventarioEditable() { // Constructor de la clase principal
        super("Simulador de Inventario - Editable"); // Título de la ventana
        configurarUI(); // Configurar interfaz de usuario
        setSize(1400, 900); // Establecer tamaño de ventana
        setLocationRelativeTo(null); // Centrar ventana en pantalla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Cerrar aplicación al cerrar ventana
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

    private boolean actualizandoTabla = false; // Bandera para evitar recursión al actualizar tabla

    private JPanel crearPanelParametros() { // Método para crear panel de parámetros
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crear panel con BorderLayout
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Agregar margen vacío

        String[] columnas = {"Parámetros", "Valores"}; // Nombres de columnas
        modeloTabla = new DefaultTableModel(columnas, 0) { // Crear modelo de tabla
            @Override
            public boolean isCellEditable(int row, int col) { // Sobrescribir método de edición
                return col == 1 && row != 5 && row != 6; // Editable solo columna valores excepto separador y ganancia
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) { // Sobrescribir método para obtener tipo de columna
                if (columnIndex == 1) return Object.class; // Columna de valores es Object
                return String.class; // Columna de parámetros es String
            }
        };

        tabla = new JTable(modeloTabla); // Crear tabla con el modelo
        configurarTabla(); // Configurar formato de tabla
        llenarTabla(); // Llenar tabla con datos

        modeloTabla.addTableModelListener(e -> { // Agregar listener para detectar cambios en tabla
            if (actualizandoTabla) return; // Evitar recursión si ya está actualizando

            int row = e.getFirstRow(); // Obtener fila modificada
            int col = e.getColumn(); // Obtener columna modificada

            if (col == 1 && row >= 0 && row != 5 && row != 6) { // Si es columna de valores y no es separador ni ganancia
                try {
                    actualizarParametroDesdeTabla(row); // Actualizar parámetro desde tabla
                } catch (Exception ex) { // Capturar excepciones de conversión
                    JOptionPane.showMessageDialog(this, // Mostrar diálogo de error
                        "Valor inválido. Por favor ingrese un número válido.",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    llenarTabla(); // Restaurar valores anteriores
                }
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tabla); // Crear panel con scroll para tabla
        scrollTabla.setBorder(BorderFactory.createTitledBorder("Parámetros del Problema (Editable)")); // Agregar borde con título
        scrollTabla.setPreferredSize(new Dimension(600, 250)); // Establecer tamaño preferido

        panel.add(scrollTabla, BorderLayout.WEST); // Agregar scroll de tabla al oeste

        JPanel panelDistribucion = crearPanelDistribucion(); // Crear panel de distribución
        panel.add(panelDistribucion, BorderLayout.CENTER); // Agregar panel de distribución al centro

        return panel; // Retornar panel completo
    }

    private void configurarTabla() { // Método para configurar apariencia de tabla
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Fuente de la tabla
        tabla.setRowHeight(28); // Altura de filas
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14)); // Fuente del encabezado
        tabla.getTableHeader().setBackground(new Color(255, 153, 0)); // Color de fondo encabezado (naranja)
        tabla.getTableHeader().setForeground(Color.WHITE); // Color de texto encabezado (blanco)

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
                } else if (row == 5) { // Si es fila separadora
                    setBackground(new Color(220, 220, 220)); // Fondo gris claro
                    setFont(new Font("Segoe UI", Font.PLAIN, 13)); // Fuente normal
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
        actualizandoTabla = true; // Activar bandera para evitar listener

        boolean wasUpdating = modeloTabla.getRowCount() > 0; // Verificar si la tabla ya tiene filas

        if (!wasUpdating) { // Si la tabla está vacía
            modeloTabla.setRowCount(0); // Limpiar filas (redundante pero seguro)
        }

        DecimalFormat dfMoney = new DecimalFormat("$#,##0.00"); // Formato monetario con decimales
        DecimalFormat dfInt = new DecimalFormat("$ #,##0"); // Formato monetario sin decimales

        double gananciaCalculada = calcularGanancia(demandaEjemplo, cantidadComprada); // Calcular ganancia con valores actuales

        if (wasUpdating) { // Si la tabla ya tenía filas (actualización)
            modeloTabla.setValueAt(dfMoney.format(precioVenta), 0, 1); // Actualizar precio de venta
            modeloTabla.setValueAt(dfMoney.format(costo), 1, 1); // Actualizar costo
            modeloTabla.setValueAt(dfMoney.format(precioDescuento), 2, 1); // Actualizar precio con descuento
            modeloTabla.setValueAt(dfInt.format(demandaEjemplo), 3, 1); // Actualizar demanda
            modeloTabla.setValueAt(dfInt.format((int)cantidadComprada), 4, 1); // Actualizar cantidad comprada
            modeloTabla.setValueAt(dfMoney.format(gananciaCalculada), 6, 1); // Actualizar ganancia
        } else { // Si es primera vez (inicialización)
            modeloTabla.addRow(new Object[]{"Precio de venta", dfMoney.format(precioVenta)}); // Agregar fila precio de venta
            modeloTabla.addRow(new Object[]{"Costo", dfMoney.format(costo)}); // Agregar fila costo
            modeloTabla.addRow(new Object[]{"Precio con descuento", dfMoney.format(precioDescuento)}); // Agregar fila precio con descuento
            modeloTabla.addRow(new Object[]{"Demanda", dfInt.format(demandaEjemplo)}); // Agregar fila demanda
            modeloTabla.addRow(new Object[]{"Cantidad comprada", dfInt.format((int)cantidadComprada)}); // Agregar fila cantidad comprada
            modeloTabla.addRow(new Object[]{"-", "-"}); // Agregar fila separadora
            modeloTabla.addRow(new Object[]{"Ganancia", dfMoney.format(gananciaCalculada)}); // Agregar fila ganancia
        }

        actualizandoTabla = false; // Desactivar bandera para reactivar listener
    }

    private void actualizarParametroDesdeTabla(int row) { // Método para actualizar parámetro desde tabla editada
        String valor = modeloTabla.getValueAt(row, 1).toString().replace("$", "").replace(",", "").trim(); // Obtener y limpiar valor

        switch (row) { // Evaluar qué fila fue editada
            case 0: // Precio de venta
                precioVenta = Double.parseDouble(valor); // Parsear y actualizar precio de venta
                break;
            case 1: // Costo
                costo = Double.parseDouble(valor); // Parsear y actualizar costo
                break;
            case 2: // Precio con descuento
                precioDescuento = Double.parseDouble(valor); // Parsear y actualizar precio con descuento
                break;
            case 3: // Demanda
                demandaEjemplo = Integer.parseInt(valor); // Parsear y actualizar demanda de ejemplo
                break;
            case 4: // Cantidad comprada
                cantidadComprada = Double.parseDouble(valor); // Parsear y actualizar cantidad comprada
                if (spinnerCantidad != null) { // Si el spinner existe
                    spinnerCantidad.setValue((int)cantidadComprada); // Sincronizar valor del spinner
                }
                break;
        }

        llenarTabla(); // Actualizar tabla completa con nuevos valores
    }

    private JPanel crearPanelDistribucion() { // Método para crear panel de distribución
        JPanel panel = new JPanel(new BorderLayout(10, 10)); // Crear panel con BorderLayout
        panel.setBorder(BorderFactory.createTitledBorder("Distribución Personalizada - Demanda (Editable)")); // Agregar borde con título

        JPanel panelControles = new JPanel(new GridLayout(3, 2, 10, 10)); // Panel de controles con GridLayout
        panelControles.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Agregar margen vacío

        JLabel lblMin = new JLabel("Mínimo:"); // Etiqueta para mínimo
        JSpinner spinnerMin = new JSpinner(new SpinnerNumberModel(demandaMin, 10, 200, 10)); // Spinner para valor mínimo

        JLabel lblMax = new JLabel("Máximo:"); // Etiqueta para máximo
        JSpinner spinnerMax = new JSpinner(new SpinnerNumberModel(demandaMax, 10, 200, 10)); // Spinner para valor máximo

        JLabel lblPaso = new JLabel("Paso:"); // Etiqueta para paso
        JSpinner spinnerPaso = new JSpinner(new SpinnerNumberModel(paso, 5, 50, 5)); // Spinner para paso

        panelControles.add(lblMin); // Agregar etiqueta mínimo
        panelControles.add(spinnerMin); // Agregar spinner mínimo
        panelControles.add(lblMax); // Agregar etiqueta máximo
        panelControles.add(spinnerMax); // Agregar spinner máximo
        panelControles.add(lblPaso); // Agregar etiqueta paso
        panelControles.add(spinnerPaso); // Agregar spinner paso

        JButton btnActualizar = new JButton("Actualizar Distribución"); // Botón para actualizar distribución
        btnActualizar.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Establecer fuente
        btnActualizar.setBackground(new Color(46, 204, 113)); // Color de fondo verde
        btnActualizar.setForeground(Color.WHITE); // Color de texto blanco
        btnActualizar.setFocusPainted(false); // Quitar borde de foco

        btnActualizar.addActionListener(e -> { // Agregar listener al botón
            demandaMin = (int) spinnerMin.getValue(); // Obtener nuevo valor mínimo
            demandaMax = (int) spinnerMax.getValue(); // Obtener nuevo valor máximo
            paso = (int) spinnerPaso.getValue(); // Obtener nuevo paso

            if (demandaMin >= demandaMax) { // Validar que mínimo sea menor que máximo
                JOptionPane.showMessageDialog(this, // Mostrar diálogo de error
                    "El mínimo debe ser menor que el máximo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return; // Salir sin actualizar
            }

            actualizarDistribucionTexto(); // Actualizar texto de distribución
        });

        txtDistribucion = new JTextArea(); // Crear área de texto
        txtDistribucion.setEditable(false); // Hacer área de texto no editable
        txtDistribucion.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establecer fuente
        txtDistribucion.setBackground(new Color(255, 255, 224)); // Color de fondo amarillo claro
        actualizarDistribucionTexto(); // Actualizar texto inicial

        JScrollPane scroll = new JScrollPane(txtDistribucion); // Crear scroll para área de texto

        JPanel panelTop = new JPanel(new BorderLayout()); // Panel combinado superior
        panelTop.add(panelControles, BorderLayout.CENTER); // Agregar controles al centro
        panelTop.add(btnActualizar, BorderLayout.SOUTH); // Agregar botón al sur

        panel.add(panelTop, BorderLayout.NORTH); // Agregar panel top al norte
        panel.add(scroll, BorderLayout.CENTER); // Agregar scroll al centro

        return panel; // Retornar panel completo
    }

    private void actualizarDistribucionTexto() { // Método para actualizar texto de distribución
        int numValores = (demandaMax - demandaMin) / paso + 1; // Calcular número de valores posibles
        double probabilidad = 1.0 / numValores; // Calcular probabilidad uniforme

        StringBuilder sb = new StringBuilder(); // Crear StringBuilder para construir texto
        sb.append("Distribución Discreta Uniforme:\n\n"); // Agregar título
        sb.append(String.format("Rango: $%d - $%d\n", demandaMin, demandaMax)); // Agregar rango
        sb.append(String.format("Paso: $%d\n", paso)); // Agregar paso
        sb.append(String.format("Número de valores: %d\n", numValores)); // Agregar número de valores
        sb.append(String.format("Probabilidad por valor: %.4f (%.2f%%)\n\n", probabilidad, probabilidad * 100)); // Agregar probabilidad
        sb.append("Valores posibles:\n"); // Agregar encabezado de valores

        for (int valor = demandaMin; valor <= demandaMax; valor += paso) { // Iterar sobre valores posibles
            sb.append(String.format("  $%d - Prob: %.2f%%\n", valor, probabilidad * 100)); // Agregar cada valor con su probabilidad
        }

        txtDistribucion.setText(sb.toString()); // Establecer texto en área de texto
    }

    private JPanel crearPanelSimulacion() { // Método para crear panel de simulación
        JPanel panel = new JPanel(new GridBagLayout()); // Crear panel con GridBagLayout
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Agregar margen vacío

        GridBagConstraints gbc = new GridBagConstraints(); // Crear restricciones para layout
        gbc.insets = new Insets(10, 10, 10, 10); // Establecer espaciado entre componentes
        gbc.fill = GridBagConstraints.HORIZONTAL; // Rellenar horizontalmente

        JLabel lblCantidad = new JLabel("Cantidad a Comprar:"); // Etiqueta para cantidad
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establecer fuente

        spinnerCantidad = new JSpinner(new SpinnerNumberModel((int)cantidadComprada, 10, 200, 5)); // Spinner para cantidad
        spinnerCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Establecer fuente
        ((JSpinner.DefaultEditor) spinnerCantidad.getEditor()).getTextField().setColumns(8); // Establecer ancho del campo

        spinnerCantidad.addChangeListener(e -> { // Agregar listener al spinner
            cantidadComprada = (int) spinnerCantidad.getValue(); // Actualizar cantidad comprada
            llenarTabla(); // Actualizar tabla con nueva cantidad y ganancia
        });

        gbc.gridx = 0; // Posición columna 0
        gbc.gridy = 0; // Posición fila 0
        panel.add(lblCantidad, gbc); // Agregar etiqueta

        gbc.gridx = 1; // Posición columna 1
        panel.add(spinnerCantidad, gbc); // Agregar spinner

        JLabel lblSimulaciones = new JLabel("Número de Simulaciones:"); // Etiqueta para simulaciones
        lblSimulaciones.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Establecer fuente

        JSpinner spinnerSimulaciones = new JSpinner(new SpinnerNumberModel(5000, 1000, 100000, 1000)); // Spinner para número de iteraciones
        spinnerSimulaciones.setFont(new Font("Segoe UI", Font.PLAIN, 14)); // Establecer fuente

        gbc.gridx = 0; // Posición columna 0
        gbc.gridy = 1; // Posición fila 1
        panel.add(lblSimulaciones, gbc); // Agregar etiqueta

        gbc.gridx = 1; // Posición columna 1
        panel.add(spinnerSimulaciones, gbc); // Agregar spinner

        JButton btnSimular = new JButton("Ejecutar Simulación Monte Carlo"); // Botón para simular
        btnSimular.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Establecer fuente
        btnSimular.setBackground(new Color(30, 144, 255)); // Color de fondo azul
        btnSimular.setForeground(Color.WHITE); // Color de texto blanco
        btnSimular.setFocusPainted(false); // Quitar borde de foco
        btnSimular.setPreferredSize(new Dimension(350, 50)); // Establecer tamaño preferido

        gbc.gridx = 0; // Posición columna 0
        gbc.gridy = 2; // Posición fila 2
        gbc.gridwidth = 2; // Ocupar 2 columnas
        gbc.insets = new Insets(30, 10, 10, 10); // Ajustar espaciado
        panel.add(btnSimular, gbc); // Agregar botón

        JButton btnReset = new JButton("Resetear a Valores Originales"); // Botón para resetear
        btnReset.setFont(new Font("Segoe UI", Font.PLAIN, 12)); // Establecer fuente
        btnReset.setBackground(new Color(231, 76, 60)); // Color de fondo rojo
        btnReset.setForeground(Color.WHITE); // Color de texto blanco
        btnReset.setFocusPainted(false); // Quitar borde de foco

        btnReset.addActionListener(e -> { // Agregar listener al botón
            precioVenta = 12.00; // Restaurar precio de venta original
            costo = 7.50; // Restaurar costo original
            precioDescuento = 6.00; // Restaurar precio con descuento original
            demandaMin = 40; // Restaurar demanda mínima original
            demandaMax = 90; // Restaurar demanda máxima original
            paso = 10; // Restaurar paso original
            cantidadComprada = 90; // Restaurar cantidad comprada original
            demandaEjemplo = 40; // Restaurar demanda de ejemplo original

            llenarTabla(); // Actualizar tabla con valores originales
            spinnerCantidad.setValue(90); // Restaurar valor del spinner
            actualizarDistribucionTexto(); // Actualizar texto de distribución

            JOptionPane.showMessageDialog(this, // Mostrar diálogo de confirmación
                "Parámetros restablecidos a valores originales.",
                "Reset Exitoso", JOptionPane.INFORMATION_MESSAGE);
        });

        gbc.gridy = 3; // Posición fila 3
        gbc.insets = new Insets(10, 10, 10, 10); // Restaurar espaciado normal
        panel.add(btnReset, gbc); // Agregar botón reset

        btnSimular.addActionListener(e -> { // Agregar listener al botón simular
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
        int[] valoresPosibles = new int[(demandaMax - demandaMin) / paso + 1]; // Crear array de valores posibles
        int idx = 0; // Inicializar índice
        for (int valor = demandaMin; valor <= demandaMax; valor += paso) { // Iterar sobre rango con paso
            valoresPosibles[idx++] = valor; // Agregar valor al array
        }
        return valoresPosibles[random.nextInt(valoresPosibles.length)]; // Retornar valor aleatorio del array
    }

    private double calcularGanancia(int demanda, double cantidad) { // Método para calcular ganancia
        if (demanda <= cantidad) { // Si la demanda es menor o igual a la cantidad
            return 6.0 * demanda - 1.5 * cantidad; // Calcular ganancia con inventario sobrante (fórmula específica)
        } else { // Si la demanda es mayor que la cantidad
            return 4.5 * cantidad; // Calcular ganancia vendiendo todo el inventario (fórmula específica)
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
                    actualizarTablaConMedia(); // Actualizar tabla con media
                    mostrarHistograma(resultados, iteraciones); // Mostrar histograma
                } catch (Exception ex) { // Capturar excepciones
                    ex.printStackTrace(); // Imprimir stack trace
                    JOptionPane.showMessageDialog(SimuladorInventarioEditable.this, // Mostrar diálogo de error
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

    private void actualizarTablaConMedia() { // Método para actualizar tabla con media
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
            SimuladorInventarioEditable simulador = new SimuladorInventarioEditable(); // Crear instancia del simulador
            simulador.setVisible(true); // Hacer visible la ventana
        });
    }
}