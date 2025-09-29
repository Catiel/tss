package actividad_7.manual;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
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
        tabla.setFont(Constantes.FUENTE_GENERAL);
        tabla.setRowHeight(28);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        configurarEncabezadoTabla(tabla);
        configurarRenderizadoresYAnchos(tabla);
        configurarColoresAlternados(tabla);
    }

    /**
     * Configura los estilos del encabezado de la tabla
     */
    private static void configurarEncabezadoTabla(JTable tabla) {
        JTableHeader header = tabla.getTableHeader();
        header.setBackground(Constantes.COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setFont(Constantes.FUENTE_HEADER);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
    }

    /**
     * Configura anchos de columnas y renderizadores específicos
     */
    private static void configurarRenderizadoresYAnchos(JTable tabla) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();

        for (int i = 0; i < Constantes.COLUMNAS.length; i++) {
            TableColumn col = tabla.getColumnModel().getColumn(i);
            col.setPreferredWidth(Constantes.ANCHOS_COLUMNAS[i]);

            if (i >= 9) {
                col.setCellRenderer(moneyRenderer);
            } else {
                col.setCellRenderer(centerRenderer);
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

                if (!isSelected) {
                    comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR);
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
                if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(value.toString());
                } else {
                    super.setValue(value);
                }
            }
        };
    }

    /**
     * Configura tabla específica para réplicas
     */
    public static void configurarTablaReplicas(JTable tablaReplicas, String[] columnasReplicas) {
        tablaReplicas.setFont(Constantes.FUENTE_GENERAL);
        tablaReplicas.setRowHeight(28);
        tablaReplicas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        tablaReplicas.setFillsViewportHeight(true);
        tablaReplicas.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tablaReplicas.getTableHeader();
        header.setBackground(Constantes.COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setFont(Constantes.FUENTE_HEADER);

        configurarRenderizadoresReplicas(tablaReplicas, columnasReplicas.length);
    }

    /**
     * Configura renderizadores específicos para la tabla de réplicas
     */
    private static void configurarRenderizadoresReplicas(JTable tablaReplicas, int numColumnas) {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaReplicas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();
        for (int i = 1; i < numColumnas; i++) {
            tablaReplicas.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer);
        }

        tablaReplicas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    if (column > 0) {
                        comp.setBackground(Constantes.COLOR_FONDO_REPLICA);
                    } else {
                        comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR);
                    }
                }

                aplicarFormatoColumna(column, value);
                return comp;
            }

            private void aplicarFormatoColumna(int column, Object value) {
                if (column == 0) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String && column > 0) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
        });
    }
}
