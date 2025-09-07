package actividad_4.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

/**
 * Panel que genera tasas de descuento aleatorias y muestra cómo afectan a la diferencia de VAN.
 */
public class PanelTablaRandom extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final DefaultTableModel modeloTabla;
    private final JTable tablaSensibilidad;
    private final JTextField txtMinimo;
    private final JTextField txtMaximo;
    private final JTextField txtCantidad;
    private final JButton btnGenerar;
    private final JButton btnLimpiar;

    /**
     * Constructor del panel de tabla con valores aleatorios para el análisis de sensibilidad.
     */
    public PanelTablaRandom() {
        // Registramos este panel como oyente de cambios en los parámetros
        ControladorParametros.getInstancia().addChangeListener(this);

        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10, 10));

        // Título del panel
        JLabel titulo = new JLabel("Tabla con tasas de descuento aleatorias");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titulo, BorderLayout.NORTH);

        // Panel superior para controles
        JPanel panelSuperior = new PanelSuperiorRandom();

        // Creamos las columnas de la tabla
        String[] columnas = {"Tasa de descuento", "Diferencia VAN"};

        // Creamos el modelo de tabla (no editable)
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
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

        // Panel central que contiene el panel superior y la tabla
        JPanel panelCentral = new JPanel(new BorderLayout());
        panelCentral.add(panelSuperior, BorderLayout.NORTH);
        panelCentral.add(scrollPane, BorderLayout.CENTER);

        add(panelCentral, BorderLayout.CENTER);

        // Panel con descripción de la tabla
        JPanel panelDescripcion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblDescripcion = new JLabel("Esta tabla muestra cómo varía la diferencia de VAN entre los escenarios con y sin versión francesa para tasas de descuento generadas aleatoriamente.");
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        panelDescripcion.add(lblDescripcion);
        add(panelDescripcion, BorderLayout.SOUTH);

        // Referencias a los componentes del panel superior
        txtMinimo = ((PanelSuperiorRandom)panelSuperior).txtMinimo;
        txtMaximo = ((PanelSuperiorRandom)panelSuperior).txtMaximo;
        txtCantidad = ((PanelSuperiorRandom)panelSuperior).txtCantidad;
        btnGenerar = ((PanelSuperiorRandom)panelSuperior).btnGenerar;
        btnLimpiar = ((PanelSuperiorRandom)panelSuperior).btnLimpiar;

        // Configuramos los listeners para los botones
        configurarListeners();
    }

    /**
     * Panel superior con controles para generar valores aleatorios
     */
    private class PanelSuperiorRandom extends JPanel {
        final JTextField txtMinimo;
        final JTextField txtMaximo;
        final JTextField txtCantidad;
        final JButton btnGenerar;
        final JButton btnLimpiar;

        PanelSuperiorRandom() {
            setLayout(new FlowLayout(FlowLayout.LEFT));
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

            // Componentes para el rango mínimo
            JLabel lblMinimo = new JLabel("Tasa mínima (%): ");
            txtMinimo = new JTextField("5", 5);

            // Componentes para el rango máximo
            JLabel lblMaximo = new JLabel("Tasa máxima (%): ");
            txtMaximo = new JTextField("30", 5);

            // Componentes para la cantidad
            JLabel lblCantidad = new JLabel("Cantidad: ");
            txtCantidad = new JTextField("10", 5);

            // Botones
            btnGenerar = new JButton("Generar");
            EstilosUI.aplicarEstiloBoton(btnGenerar);
            btnLimpiar = new JButton("Limpiar");
            EstilosUI.aplicarEstiloBoton(btnLimpiar);

            // Agregamos los componentes al panel
            add(lblMinimo);
            add(txtMinimo);
            add(lblMaximo);
            add(txtMaximo);
            add(lblCantidad);
            add(txtCantidad);
            add(btnGenerar);
            add(btnLimpiar);
        }
    }

    /**
     * Configura los listeners para los botones de la interfaz
     */
    private void configurarListeners() {
        // Botón para generar valores aleatorios
        btnGenerar.addActionListener((ActionEvent e) -> {
            try {
                // Obtenemos los valores de los campos
                double minimo = Double.parseDouble(txtMinimo.getText().trim());
                double maximo = Double.parseDouble(txtMaximo.getText().trim());
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());

                // Validaciones
                if (minimo < 0 || minimo >= 100) {
                    JOptionPane.showMessageDialog(this, "La tasa mínima debe estar entre 0 y 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (maximo <= minimo || maximo > 100) {
                    JOptionPane.showMessageDialog(this, "La tasa máxima debe ser mayor que la mínima y menor o igual a 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                if (cantidad <= 0 || cantidad > 100) {
                    JOptionPane.showMessageDialog(this, "La cantidad debe estar entre 1 y 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Limpiamos la tabla
                modeloTabla.setRowCount(0);

                // Generamos los valores aleatorios
                Random random = new Random();
                for (int i = 0; i < cantidad; i++) {
                    // Generamos una tasa aleatoria entre mínimo y máximo
                    double tasa = minimo + (maximo - minimo) * random.nextDouble();
                    tasa = tasa / 100.0; // Convertimos a decimal
                    calcularYAgregarFila(tasa);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Por favor ingrese números válidos en todos los campos", "Error de formato", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Botón para limpiar la tabla
        btnLimpiar.addActionListener((ActionEvent e) -> {
            modeloTabla.setRowCount(0);
        });
    }

    /**
     * Calcula la diferencia de VAN para una tasa específica y la agrega a la tabla
     */
    private void calcularYAgregarFila(double tasa) {
        try {
            ControladorParametros params = ControladorParametros.getInstancia();

            // Calculamos el resultado comparativo usando la tasa de descuento proporcionada
            ModeloSoftwareCalculo.ResultadoComparativo resultado =
                ModeloSoftwareCalculo.calcularComparativo(params, 0, tasa);

            // Formateamos la tasa como porcentaje y la diferencia como moneda
            String tasaFormateada = String.format("%.2f%%", tasa * 100);
            String diferenciaFormateada = formatearMoneda(resultado.diferenciaVAN);

            // Añadimos la fila a la tabla
            modeloTabla.addRow(new Object[]{tasaFormateada, diferenciaFormateada});

            // Aplicamos formato condicional
            aplicarFormatoCondicional();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al calcular el resultado", "Error", JOptionPane.ERROR_MESSAGE);
        }
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

                if (column == 1) { // Solo la columna de diferencia VAN
                    String valorTexto = (String)value;
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
     * Actualiza los valores de la tabla cuando cambian los parámetros
     */
    private void actualizarTabla() {
        // Si la tabla está vacía, no hay nada que actualizar
        if (modeloTabla.getRowCount() == 0) {
            return;
        }

        // Guardamos las tasas actuales
        java.util.List<Double> tasas = new java.util.ArrayList<>();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String tasaStr = (String) modeloTabla.getValueAt(i, 0);
            tasaStr = tasaStr.replace("%", "").trim();
            tasas.add(Double.parseDouble(tasaStr) / 100.0);
        }

        // Limpiamos la tabla
        modeloTabla.setRowCount(0);

        // Recalculamos para cada tasa
        for (Double tasa : tasas) {
            calcularYAgregarFila(tasa);
        }
    }

    /**
     * Implementación del método requerido por la interfaz ControladorParametros.ParametrosChangeListener.
     * Este método se llama automáticamente cuando hay cambios en los parámetros.
     */
    @Override
    public void onParametrosChanged() {
        // Cuando cambian los parámetros, actualizamos la tabla
        SwingUtilities.invokeLater(this::actualizarTabla);
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
