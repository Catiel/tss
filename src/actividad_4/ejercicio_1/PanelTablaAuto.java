package actividad_4.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel que muestra una tabla de sensibilidad de la diferencia de VAN (NPV)
 * en función de diferentes tasas de descuento predefinidas.
 */
public class PanelTablaAuto extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final DefaultTableModel modeloTabla;
    private final JTable tablaSensibilidad;

    // Tasas de descuento predefinidas según el Excel
    private final double[] tasasDescuento = {0.10, 0.12, 0.14, 0.16, 0.18, 0.20, 0.22, 0.24, 0.26, 0.28};

    /**
     * Constructor del panel de tabla automática para análisis de sensibilidad
     * de la diferencia de VAN respecto a la tasa de descuento.
     */
    public PanelTablaAuto() {
        // Registramos este panel como oyente de cambios en los parámetros
        ControladorParametros.getInstancia().addChangeListener(this);

        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10, 10));

        // Título del panel
        JLabel titulo = new JLabel("Análisis de sensibilidad - Diferencia de VAN por tasa de descuento");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(titulo, BorderLayout.NORTH);

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
        add(scrollPane, BorderLayout.CENTER);

        // Panel con descripción de la tabla
        JPanel panelDescripcion = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblDescripcion = new JLabel("Esta tabla muestra cómo varía la diferencia de VAN entre los escenarios con y sin versión francesa para diferentes tasas de descuento.");
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        panelDescripcion.add(lblDescripcion);
        add(panelDescripcion, BorderLayout.SOUTH);

        // Actualizamos la tabla con los datos iniciales
        actualizarTabla();
    }

    /**
     * Actualiza la tabla con los valores de diferencia de VAN para cada tasa de descuento.
     */
    private void actualizarTabla() {
        try {
            ControladorParametros params = ControladorParametros.getInstancia();

            // Limpiamos la tabla
            modeloTabla.setRowCount(0);

            // Para cada tasa de descuento predefinida, calculamos la diferencia de VAN
            for (double tasa : tasasDescuento) {
                // Calculamos el resultado comparativo usando la tasa de descuento actual
                ModeloSoftwareCalculo.ResultadoComparativo resultado =
                    ModeloSoftwareCalculo.calcularComparativo(params, tasa);

                // Formateamos la tasa como porcentaje y la diferencia como moneda
                String tasaFormateada = UtilidadesFormato.formatearPorcentaje(tasa);
                String diferenciaFormateada = UtilidadesFormato.formatearMoneda(resultado.diferenciaVAN);

                // Añadimos la fila a la tabla
                modeloTabla.addRow(new Object[]{tasaFormateada, diferenciaFormateada});

                // Destacamos visualmente cuando la diferencia se vuelve negativa
                int ultimaFila = modeloTabla.getRowCount() - 1;
                if (resultado.diferenciaVAN < 0) {
                    tablaSensibilidad.setValueAt(diferenciaFormateada, ultimaFila, 1);
                }
            }

            // Aplicamos formato condicional (color) después de que la tabla esté llena
            aplicarFormatoCondicional();

        } catch (Exception ex) {
            ex.printStackTrace();
            modeloTabla.setRowCount(0); // Limpiamos la tabla en caso de error
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
