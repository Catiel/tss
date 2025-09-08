package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Layouts y utilidades gráficas
import java.awt.event.ActionEvent; // Evento de acción para listeners de botones

/**
 * Panel que permite al usuario crear filas, ingresar manualmente tasas de descuento
 * y ver cómo afectan a la diferencia de VAN entre escenarios.
 */
public class PanelTablaManual extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios globales
    private final DefaultTableModel modeloTabla;    // Modelo de datos de la tabla (col 0 editable, col 1 resultados)
    private final JTable tablaSensibilidad;         // Tabla que muestra tasas y diferencia de VAN
    private final JTextField txtFilas;              // Campo para introducir número de filas a crear
    private final JButton btnCrearFilas;            // Botón para generar filas vacías
    private final JButton btnGenerar;               // Botón para calcular resultados
    private final JButton btnLimpiar;               // Botón para limpiar la tabla

    /**
     * Constructor del panel de tabla manual para análisis de tasas de descuento personalizadas.
     */
    public PanelTablaManual() { // Inicio constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como oyente de parámetros

        EstilosUI.aplicarEstiloPanel(this);           // Aplica estilo base al panel
        setLayout(new BorderLayout(10, 10));          // Usa BorderLayout con margenes

        JLabel titulo = new JLabel("Tabla con tasas de descuento ingresadas manualmente"); // Título
        EstilosUI.aplicarEstiloTitulo(titulo);        // Estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interno
        add(titulo, BorderLayout.NORTH);              // Añade título arriba

        JPanel panelEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel superior (inputs y botones)
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Margen panel entrada

        JLabel lblFilas = new JLabel("Número de filas: "); // Etiqueta campo filas
        txtFilas = new JTextField(5);                       // Campo texto número de filas
        btnCrearFilas = new JButton("Crear filas");        // Botón crear filas
        EstilosUI.aplicarEstiloBoton(btnCrearFilas);        // Estilo botón
        btnGenerar = new JButton("Generar resultados");    // Botón generar resultados
        EstilosUI.aplicarEstiloBoton(btnGenerar);           // Estilo botón
        btnLimpiar = new JButton("Limpiar tabla");         // Botón limpiar
        EstilosUI.aplicarEstiloBoton(btnLimpiar);           // Estilo botón

        panelEntrada.add(lblFilas);                // Añade etiqueta filas
        panelEntrada.add(txtFilas);                // Añade campo filas
        panelEntrada.add(btnCrearFilas);           // Añade botón crear
        panelEntrada.add(btnGenerar);              // Añade botón generar
        panelEntrada.add(btnLimpiar);              // Añade botón limpiar

        String[] columnas = {"Tasa de descuento (%)", "Diferencia VAN"}; // Definición de columnas

        modeloTabla = new DefaultTableModel(columnas, 0) { // Modelo con 0 filas iniciales
            @Override public boolean isCellEditable(int row, int col) { // Control edición
                return col == 0; // Solo primera columna editable (tasa)
            }
            @Override public Class<?> getColumnClass(int columnIndex) { // Tipo de cada columna
                if (columnIndex == 0) return Double.class; // Tasa => Double (para spinner/edición numérica)
                return String.class; // Resultado formateado => String
            }
        };

        tablaSensibilidad = new JTable(modeloTabla); // Crea tabla con modelo
        EstilosUI.aplicarEstiloTabla(tablaSensibilidad); // Aplica estilo

        tablaSensibilidad.getColumnModel().getColumn(0).setPreferredWidth(150); // Ancho col tasa
        tablaSensibilidad.getColumnModel().getColumn(1).setPreferredWidth(250); // Ancho col resultado

        JScrollPane scrollPane = new JScrollPane(tablaSensibilidad); // Scroll para tabla
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen scroll

        JPanel panelCentral = new JPanel(new BorderLayout()); // Panel central contenedor
        panelCentral.add(panelEntrada, BorderLayout.NORTH);  // Añade panel de entrada arriba
        panelCentral.add(scrollPane, BorderLayout.CENTER);   // Añade tabla al centro
        add(panelCentral, BorderLayout.CENTER);              // Inserta panel central

        JPanel panelDescripcion = new JPanel();              // Panel inferior instrucciones
        panelDescripcion.setLayout(new BoxLayout(panelDescripcion, BoxLayout.Y_AXIS)); // Layout vertical
        panelDescripcion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen

        JLabel lblInstrucciones1 = new JLabel("1. Ingresa el número de filas y haz clic en \"Crear filas\""); // Paso 1
        JLabel lblInstrucciones2 = new JLabel("2. Completa las tasas de descuento en la tabla (valores entre 0 y 100)"); // Paso 2
        JLabel lblInstrucciones3 = new JLabel("3. Haz clic en \"Generar resultados\" para calcular la diferencia de VAN"); // Paso 3
        lblInstrucciones1.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente itálica
        lblInstrucciones2.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente itálica
        lblInstrucciones3.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente itálica
        panelDescripcion.add(lblInstrucciones1); // Añade instrucción 1
        panelDescripcion.add(lblInstrucciones2); // Añade instrucción 2
        panelDescripcion.add(lblInstrucciones3); // Añade instrucción 3
        add(panelDescripcion, BorderLayout.SOUTH); // Añade panel instrucciones

        configurarListeners();        // Configura listeners de botones
        btnGenerar.setEnabled(false); // Deshabilita generar hasta crear filas
    }

    /** Configura los listeners para los botones de la interfaz */
    private void configurarListeners() { // Inicio configuración
        btnCrearFilas.addActionListener((ActionEvent e) -> { // Listener crear filas
            try {
                String filasStr = txtFilas.getText().trim(); // Lee texto
                if (filasStr.isEmpty()) { // Validación vacío
                    JOptionPane.showMessageDialog(this, "Ingrese un número de filas", "Campo vacío", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int numFilas = Integer.parseInt(filasStr); // Parse entero
                if (numFilas <= 0 || numFilas > 50) { // Rango permitido
                    JOptionPane.showMessageDialog(this, "El número de filas debe estar entre 1 y 50", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                modeloTabla.setRowCount(0); // Limpia tabla
                for (int i = 0; i < numFilas; i++) { // Crea filas vacías
                    modeloTabla.addRow(new Object[]{null, ""}); // Tasa null, resultado vacío
                }
                btnGenerar.setEnabled(true); // Habilita generar
            } catch (NumberFormatException ex) { // Error parseo
                JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnGenerar.addActionListener((ActionEvent e) -> { // Listener generar resultados
            boolean hayCeldasVacias = false; // Flag incompletos
            for (int i = 0; i < modeloTabla.getRowCount(); i++) { // Recorre filas
                if (modeloTabla.getValueAt(i, 0) == null) { hayCeldasVacias = true; break; } // Falta tasa
            }
            if (hayCeldasVacias) { // Aviso si incompleto
                JOptionPane.showMessageDialog(this,
                        "Por favor completa todas las tasas de descuento antes de generar los resultados",
                        "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }
            for (int i = 0; i < modeloTabla.getRowCount(); i++) { // Procesa cada fila
                Object valor = modeloTabla.getValueAt(i, 0); // Lee tasa
                double tasa; // Variable numérica
                try {
                    if (valor instanceof Double) { // Caso Double
                        tasa = (Double) valor;
                    } else if (valor instanceof String) { // Caso String
                        tasa = Double.parseDouble(valor.toString());
                    } else {
                        tasa = 0; // Fallback
                    }
                    if (tasa <= 0 || tasa >= 100) { // Validación rango (0,100)
                        JOptionPane.showMessageDialog(this,
                                "La tasa en la fila " + (i+1) + " debe estar entre 0 y 100",
                                "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    tasa = tasa / 100.0; // Convierte a decimal
                    ControladorParametros params = ControladorParametros.getInstancia(); // Parámetros globales
                    ModeloSoftwareCalculo.ResultadoComparativo resultado =
                            ModeloSoftwareCalculo.calcularComparativo(params, tasa); // Cálculo VAN
                    String diferenciaFormateada = formatearMoneda(resultado.diferenciaVAN); // Formato moneda
                    modeloTabla.setValueAt(diferenciaFormateada, i, 1); // Actualiza resultado
                } catch (NumberFormatException ex) { // Error conversión
                    JOptionPane.showMessageDialog(this,
                            "Formato inválido en la fila " + (i+1) + ": " + ex.getMessage(),
                            "Error de formato", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Exception ex) { // Otros errores
                    JOptionPane.showMessageDialog(this,
                            "Error al calcular el resultado para la fila " + (i+1) + ": " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            aplicarFormatoCondicional(); // Aplica colores según signo
        });

        btnLimpiar.addActionListener((ActionEvent e) -> { // Listener limpiar
            modeloTabla.setRowCount(0);      // Vacía tabla
            btnGenerar.setEnabled(false);    // Deshabilita generar
        });
    }

    /** Aplica formato condicional a la columna de resultados */
    private void aplicarFormatoCondicional() { // Renderer condicional
        tablaSensibilidad.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Base
                if (column == 1 && value != null) { // Solo columna resultados
                    String valorTexto = value.toString(); // Texto celda
                    if (valorTexto.contains("-")) {      // Negativo
                        c.setForeground(new Color(192, 0, 0)); // Rojo
                    } else {
                        c.setForeground(new Color(0, 128, 0)); // Verde
                    }
                } else {
                    c.setForeground(Color.BLACK); // Color por defecto
                }
                return c; // Devuelve componente
            }
        });
    }

    /** Formatea un número como moneda usando utilidad central */
    private String formatearMoneda(double valor) { return UtilidadesFormato.formatearMoneda(valor); }

    /** Responde a cambios globales recalculando (si había resultados) */
    @Override public void onParametrosChanged() { // Cambio de parámetros
        if (modeloTabla.getRowCount() > 0 && modeloTabla.getValueAt(0, 1) != null
                && !modeloTabla.getValueAt(0, 1).toString().isEmpty()) { // Hay resultados previos
            SwingUtilities.invokeLater(() -> btnGenerar.doClick()); // Recalcula automáticamente
        }
    }

    /** Limpieza: elimina listener al ser removido */
    @Override public void removeNotify() { // Removiendo panel
        ControladorParametros.getInstancia().removeChangeListener(this); // Se des-registra
        super.removeNotify(); // Llama a super
    }
}
