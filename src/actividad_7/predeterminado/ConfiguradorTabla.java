package actividad_7.predeterminado;

// Importa componentes de Swing
import javax.swing.*;
// Importa renderizadores y encabezados de tabla
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
// Importa utilidades de diseño
import java.awt.*;

/**
 * Clase responsable de configurar el aspecto visual de las tablas
 */
public class ConfiguradorTabla {

    /**
     * Aplica todos los estilos visuales a una tabla dada
     */
    public static void configurarEstilosTabla(JTable tabla) {
        // Configuración básica de la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Fuente general
        tabla.setRowHeight(28); // Altura de fila
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // No autoajustar columnas
        tabla.setFillsViewportHeight(true); // Llenar el viewport
        tabla.getTableHeader().setReorderingAllowed(false); // No permitir reordenar columnas

        configurarEncabezadoTabla(tabla); // Configura el encabezado
        configurarRenderizadoresYAnchos(tabla); // Configura renderizadores y anchos
        configurarColoresAlternados(tabla); // Configura colores alternados
    }

    /**
     * Configura los estilos del encabezado de la tabla
     */
    private static void configurarEncabezadoTabla(JTable tabla) {
        JTableHeader header = tabla.getTableHeader(); // Obtiene el encabezado
        header.setBackground(Constantes.COLOR_PRIMARIO); // Fondo primario
        header.setForeground(Color.WHITE); // Texto blanco
        header.setFont(Constantes.FUENTE_HEADER); // Fuente del header
        header.setPreferredSize(new Dimension(header.getWidth(), 32)); // Altura preferida
    }

    /**
     * Configura anchos de columnas y renderizadores específicos
     */
    private static void configurarRenderizadoresYAnchos(JTable tabla) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Renderizador centrado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda(); // Renderizador de moneda

        for (int i = 0; i < Constantes.COLUMNAS.length; i++) { // Para cada columna
            TableColumn col = tabla.getColumnModel().getColumn(i); // Obtiene la columna
            col.setPreferredWidth(Constantes.ANCHOS_COLUMNAS[i]); // Asigna ancho

            if (i >= 9) { // Si es columna de dinero
                col.setCellRenderer(moneyRenderer); // Usa renderizador de moneda
            } else {
                col.setCellRenderer(centerRenderer); // Usa renderizador centrado
            }
        }
    }

    /**
     * Aplica colores alternados a las filas de la tabla
     */
    private static void configurarColoresAlternados(JTable tabla) {
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (!isSelected) { // Si la fila no está seleccionada
                    comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR); // Color alternado
                }
                return comp;
            }
        });
    }

    /**
     * Crea un renderizador especializado para valores monetarios
     */
    public static DefaultTableCellRenderer crearRenderizadorMoneda() {
        return new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) { // Si es número
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText(String.format("$%,.2f", value)); // Formato moneda
                } else if (value instanceof String) { // Si es string
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText(value.toString()); // Muestra el texto
                } else {
                    super.setValue(value); // Valor por defecto
                }
            }
        };
    }

    /**
     * Configura tabla específica para réplicas
     */
    public static void configurarTablaReplicas(JTable tablaReplicas, String[] columnasReplicas) {
        tablaReplicas.setFont(Constantes.FUENTE_GENERAL); // Fuente general
        tablaReplicas.setRowHeight(28); // Altura de fila
        tablaReplicas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Autoajustar columnas
        tablaReplicas.setFillsViewportHeight(true); // Llenar viewport
        tablaReplicas.getTableHeader().setReorderingAllowed(false); // No permitir reordenar

        JTableHeader header = tablaReplicas.getTableHeader(); // Encabezado
        header.setBackground(Constantes.COLOR_PRIMARIO); // Fondo primario
        header.setForeground(Color.WHITE); // Texto blanco
        header.setFont(Constantes.FUENTE_HEADER); // Fuente del header

        configurarRenderizadoresReplicas(tablaReplicas, columnasReplicas.length); // Renderizadores para réplicas
    }

    /**
     * Configura renderizadores específicos para la tabla de réplicas
     */
    private static void configurarRenderizadoresReplicas(JTable tablaReplicas, int numColumnas) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Renderizador centrado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centrar texto
        tablaReplicas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Primera columna centrada

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda(); // Renderizador de moneda
        for (int i = 1; i < numColumnas; i++) { // Para columnas de datos
            tablaReplicas.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer); // Renderizador de moneda
        }

        tablaReplicas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (!isSelected) { // Si la fila no está seleccionada
                    if (column > 0) {
                        comp.setBackground(Constantes.COLOR_FONDO_REPLICA); // Fondo especial para datos
                    } else {
                        comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR); // Color alternado
                    }
                }

                aplicarFormatoColumna(column, value); // Aplica formato según columna
                return comp;
            }

            private void aplicarFormatoColumna(int column, Object value) {
                if (column == 0) { // Primera columna
                    setHorizontalAlignment(SwingConstants.CENTER); // Centrar
                } else if (value instanceof Number) { // Si es número
                    setHorizontalAlignment(SwingConstants.RIGHT); // Derecha
                    setText(String.format("$%,.2f", value)); // Formato moneda
                } else if (value instanceof String && column > 0) { // Si es string y no es la primera columna
                    setHorizontalAlignment(SwingConstants.RIGHT); // Derecha
                }
            }
        });
    }
}
