package actividad_4.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Panel que permite al usuario crear filas, ingresar manualmente tasas de descuento
 * y ver cómo afectan a la diferencia de VAN entre escenarios.
 */
public class PanelTablaManual extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final DefaultTableModel modeloTabla;
    private final JTable tablaSensibilidad;
    private final JTextField txtFilas;
    private final JButton btnCrearFilas;
    private final JButton btnGenerar;
    private final JButton btnLimpiar;

    /**
     * Constructor del panel de tabla manual para análisis de tasas de descuento personalizadas.
     */
    public PanelTablaManual() {
        // Registramos este panel como oyente de cambios en los parámetros
        ControladorParametros.getInstancia().addChangeListener(this);

        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10, 10));

        // Título del panel
        JLabel titulo = new JLabel("Tabla con tasas de descuento ingresadas manualmente");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titulo, BorderLayout.NORTH);

        // Panel para crear filas y botones de acción
        JPanel panelEntrada = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelEntrada.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Campo para ingresar cantidad de filas
        JLabel lblFilas = new JLabel("Número de filas: ");
        txtFilas = new JTextField(5);
        btnCrearFilas = new JButton("Crear filas");
        EstilosUI.aplicarEstiloBoton(btnCrearFilas);
        btnGenerar = new JButton("Generar resultados");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        btnLimpiar = new JButton("Limpiar tabla");
        EstilosUI.aplicarEstiloBoton(btnLimpiar);

        panelEntrada.add(lblFilas);
        panelEntrada.add(txtFilas);
        panelEntrada.add(btnCrearFilas);
        panelEntrada.add(btnGenerar);
        panelEntrada.add(btnLimpiar);

        // Creamos las columnas de la tabla
        String[] columnas = {"Tasa de descuento (%)", "Diferencia VAN"};

        // Creamos el modelo de tabla con columna de tasa editable
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                // Solo la primera columna (tasa) es editable
                return col == 0;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) {
                    return Double.class; // La columna de tasa es de tipo Double
                }
                return String.class; // La columna de diferencia es de tipo String
            }
        };

        // Creamos la tabla con el modelo
        tablaSensibilidad = new JTable(modeloTabla);
        EstilosUI.aplicarEstiloTabla(tablaSensibilidad);

        // Ajustamos los anchos de las columnas
        tablaSensibilidad.getColumnModel().getColumn(0).setPreferredWidth(150);
        tablaSensibilidad.getColumnModel().getColumn(1).setPreferredWidth(250);

        // Creamos un panel con scroll para la tabla
        JScrollPane scrollPane = new JScrollPane(tablaSensibilidad);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Panel central que contiene el panel de entrada y la tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(panelEntrada, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // Panel con instrucciones
        JPanel panelDescripcion = new JPanel();
        panelDescripcion.setLayout(new BoxLayout(panelDescripcion, BoxLayout.Y_AXIS));
        panelDescripcion.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel lblInstrucciones1 = new JLabel("1. Ingresa el número de filas y haz clic en \"Crear filas\"");
        JLabel lblInstrucciones2 = new JLabel("2. Completa las tasas de descuento en la tabla (valores entre 0 y 100)");
        JLabel lblInstrucciones3 = new JLabel("3. Haz clic en \"Generar resultados\" para calcular la diferencia de VAN");

        lblInstrucciones1.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInstrucciones2.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblInstrucciones3.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        panelDescripcion.add(lblInstrucciones1);
        panelDescripcion.add(lblInstrucciones2);
        panelDescripcion.add(lblInstrucciones3);

        add(panelDescripcion, BorderLayout.SOUTH);

        // Configuramos los listeners de los botones
        configurarListeners();

        // Deshabilitamos el botón de generar inicialmente
        btnGenerar.setEnabled(false);
    }

    /**
     * Configura los listeners para los botones de la interfaz
     */
    private void configurarListeners() {
        // Botón para crear filas vacías
        btnCrearFilas.addActionListener((ActionEvent e) -> {
            try {
                String filasStr = txtFilas.getText().trim();
                if (filasStr.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Ingrese un número de filas", "Campo vacío", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int numFilas = Integer.parseInt(filasStr);
                if (numFilas <= 0 || numFilas > 50) {
                    JOptionPane.showMessageDialog(this, "El número de filas debe estar entre 1 y 50", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Limpiamos la tabla
                modeloTabla.setRowCount(0);

                // Creamos las filas vacías
                for (int i = 0; i < numFilas; i++) {
                    modeloTabla.addRow(new Object[]{null, ""});
                }

                // Habilitamos el botón de generar
                btnGenerar.setEnabled(true);

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Botón para generar resultados basados en las tasas ingresadas
        btnGenerar.addActionListener((ActionEvent e) -> {
            // Verificamos que todas las celdas tengan valores
            boolean hayCeldasVacias = false;
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                if (modeloTabla.getValueAt(i, 0) == null) {
                    hayCeldasVacias = true;
                    break;
                }
            }

            if (hayCeldasVacias) {
                JOptionPane.showMessageDialog(this,
                    "Por favor completa todas las tasas de descuento antes de generar los resultados",
                    "Datos incompletos", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Recorremos cada fila y calculamos el resultado
            for (int i = 0; i < modeloTabla.getRowCount(); i++) {
                Object valor = modeloTabla.getValueAt(i, 0);
                double tasa;

                try {
                    // Intentamos convertir el valor a double
                    if (valor instanceof Double) {
                        tasa = (Double) valor;
                    } else if (valor instanceof String) {
                        tasa = Double.parseDouble(valor.toString());
                    } else {
                        tasa = 0;
                    }

                    // Validamos el rango
                    if (tasa <= 0 || tasa >= 100) {
                        JOptionPane.showMessageDialog(this,
                            "La tasa en la fila " + (i+1) + " debe estar entre 0 y 100",
                            "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                        return;
                    }

                    // Convertimos de porcentaje a decimal
                    tasa = tasa / 100.0;

                    // Calculamos el resultado para esta tasa
                    ControladorParametros params = ControladorParametros.getInstancia();
                    ModeloSoftwareCalculo.ResultadoComparativo resultado =
                        ModeloSoftwareCalculo.calcularComparativo(params, 0, tasa);

                    // Formateamos la diferencia como moneda
                    String diferenciaFormateada = formatearMoneda(resultado.diferenciaVAN);

                    // Actualizamos la celda de resultado
                    modeloTabla.setValueAt(diferenciaFormateada, i, 1);

                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Formato inválido en la fila " + (i+1) + ": " + ex.getMessage(),
                        "Error de formato", JOptionPane.ERROR_MESSAGE);
                    return;
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error al calcular el resultado para la fila " + (i+1) + ": " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Aplicamos formato condicional después de generar todos los resultados
            aplicarFormatoCondicional();

        });

        // Botón para limpiar la tabla
        btnLimpiar.addActionListener((ActionEvent e) -> {
            modeloTabla.setRowCount(0);
            btnGenerar.setEnabled(false);
        });
    }

    /**
     * Aplica formato visual condicional a las celdas de la tabla:
     * - Valores positivos en verde
     * - Valores negativos en rojo
     */
    private void aplicarFormatoCondicional() {
        tablaSensibilidad.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {

                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                if (column == 1 && value != null) { // Solo la columna de diferencia VAN
                    String valorTexto = value.toString();
                    if (valorTexto.contains("-")) {
                        c.setForeground(new Color(192, 0, 0)); // Rojo para valores negativos
                    } else {
                        c.setForeground(new Color(0, 128, 0)); // Verde para valores positivos
                    }
                } else {
                    c.setForeground(Color.BLACK); // Color normal para otras columnas
                }

                return c;
            }
        });
    }

    /**
     * Formatea un valor numérico como moneda (con separador de miles y símbolo $).
     * @param valor El valor a formatear
     * @return Cadena formateada como moneda
     */
    private String formatearMoneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    /**
     * Implementación del método requerido por la interfaz ControladorParametros.ParametrosChangeListener.
     * Este método se llama automáticamente cuando hay cambios en los parámetros.
     */
    @Override
    public void onParametrosChanged() {
        // Cuando cambian los parámetros y hay resultados en la tabla, los actualizamos
        if (modeloTabla.getRowCount() > 0 && modeloTabla.getValueAt(0, 1) != null
                && !modeloTabla.getValueAt(0, 1).toString().isEmpty()) {
            SwingUtilities.invokeLater(() -> btnGenerar.doClick());
        }
    }

    /**
     * Método que se llama cuando este panel se elimina del contenedor padre.
     * Nos desregistramos como oyente para evitar memory leaks.
     */
    @Override
    public void removeNotify() {
        // Nos desregistramos como oyente de cambios
        ControladorParametros.getInstancia().removeChangeListener(this);
        super.removeNotify();
    }
}
