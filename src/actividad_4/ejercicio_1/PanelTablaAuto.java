package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla
import java.awt.*; // Layouts y utilidades AWT

/**
 * Panel que muestra una tabla de sensibilidad de la diferencia de VAN (NPV)
 * en función de diferentes tasas de descuento predefinidas.
 */
public class PanelTablaAuto extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios
    private final DefaultTableModel modeloTabla; // Modelo de la tabla (no editable)
    private final JTable tablaSensibilidad;      // Tabla que visualiza los datos

    // Tasas de descuento predefinidas según el Excel (decimales, no %)
    private final double[] tasasDescuento = {0.10, 0.12, 0.14, 0.16, 0.18, 0.20, 0.22, 0.24, 0.26, 0.28};

    /**
     * Constructor del panel de tabla automática para análisis de sensibilidad
     * de la diferencia de VAN respecto a la tasa de descuento.
     */
    public PanelTablaAuto() { // Inicio constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como oyente de cambios globales

        EstilosUI.aplicarEstiloPanel(this);       // Aplica estilo de fondo
        setLayout(new BorderLayout(10, 10));      // Usa BorderLayout con separación

        JLabel titulo = new JLabel("Análisis de sensibilidad - Diferencia de VAN por tasa de descuento"); // Título
        EstilosUI.aplicarEstiloTitulo(titulo);    // Estilo tipográfico de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interno
        add(titulo, BorderLayout.NORTH);          // Coloca título arriba

        String[] columnas = {"Tasa de descuento", "Diferencia VAN"}; // Encabezados de tabla

        modeloTabla = new DefaultTableModel(columnas, 0) { // Crea modelo sin filas
            @Override public boolean isCellEditable(int row, int col) { return false; } // Desactiva edición
        };

        tablaSensibilidad = new JTable(modeloTabla); // Instancia tabla
        EstilosUI.aplicarEstiloTabla(tablaSensibilidad); // Aplica estilo común

        tablaSensibilidad.getColumnModel().getColumn(0).setPreferredWidth(150); // Ajuste ancho col tasa
        tablaSensibilidad.getColumnModel().getColumn(1).setPreferredWidth(250); // Ajuste ancho col diferencia

        JScrollPane scrollPane = new JScrollPane(tablaSensibilidad); // Scroll para tabla
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen scroll
        add(scrollPane, BorderLayout.CENTER); // Coloca tabla en el centro

        JPanel panelDescripcion = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel inferior descripción
        JLabel lblDescripcion = new JLabel("Esta tabla muestra cómo varía la diferencia de VAN entre los escenarios con y sin versión francesa para diferentes tasas de descuento."); // Texto descriptivo
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente descriptiva
        panelDescripcion.add(lblDescripcion); // Añade descripción
        add(panelDescripcion, BorderLayout.SOUTH); // Añade panel abajo

        actualizarTabla(); // Primer llenado de datos
    }

    /**
     * Actualiza la tabla con los valores de diferencia de VAN para cada tasa de descuento.
     */
    private void actualizarTabla() { // Recalcula filas
        try { // Bloque seguro
            ControladorParametros params = ControladorParametros.getInstancia(); // Parámetros globales
            modeloTabla.setRowCount(0); // Limpia filas existentes

            for (double tasa : tasasDescuento) { // Itera cada tasa predefinida
                ModeloSoftwareCalculo.ResultadoComparativo resultado = // Calcula comparativo con esta tasa
                        ModeloSoftwareCalculo.calcularComparativo(params, tasa);

                String tasaFormateada = UtilidadesFormato.formatearPorcentaje(tasa); // Convierte tasa a % texto
                String diferenciaFormateada = UtilidadesFormato.formatearMoneda(resultado.diferenciaVAN); // Formatea diferencia

                modeloTabla.addRow(new Object[]{tasaFormateada, diferenciaFormateada}); // Inserta fila

                int ultimaFila = modeloTabla.getRowCount() - 1; // Índice fila recién añadida
                if (resultado.diferenciaVAN < 0) { // Si la diferencia es negativa
                    tablaSensibilidad.setValueAt(diferenciaFormateada, ultimaFila, 1); // (Ya está, redundante pero seguro)
                }
            }

            aplicarFormatoCondicional(); // Colorea según signo
        } catch (Exception ex) { // Ante error
            ex.printStackTrace(); // Traza
            modeloTabla.setRowCount(0); // Limpia tabla
        }
    }

    /**
     * Aplica formato visual condicional a las celdas de la tabla:
     * - Valores positivos en verde
     * - Valores negativos en rojo
     */
    private void aplicarFormatoCondicional() { // Añade renderer condicional
        tablaSensibilidad.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() { // Renderer personalizado
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Base
                if (column == 1) { // Sólo columna diferencia VAN
                    String valorTexto = (String) value; // Texto de la celda
                    if (valorTexto.contains("-")) { // Valor negativo (formato monetario ya con signo)
                        c.setForeground(new Color(192, 0, 0)); // Rojo oscuro
                    } else {
                        c.setForeground(new Color(0, 128, 0)); // Verde
                    }
                } else {
                    c.setForeground(Color.BLACK); // Color por defecto
                }
                return c; // Devuelve componente coloreado
            }
        });
    }

    /**
     * Notificación de cambio en parámetros globales.
     */
    @Override public void onParametrosChanged() { SwingUtilities.invokeLater(this::actualizarTabla); } // Recalcula en EDT

    /**
     * Limpieza de listener al remover el panel para evitar fugas.
     */
    @Override public void removeNotify() { // Al quitar de la jerarquía
        ControladorParametros.getInstancia().removeChangeListener(this); // Se des-registra
        super.removeNotify(); // Llama a super
    }
}
