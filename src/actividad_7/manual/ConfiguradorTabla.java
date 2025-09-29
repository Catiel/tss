package actividad_7.manual; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.table.DefaultTableCellRenderer; // Importa el renderizador de celdas por defecto
import javax.swing.table.JTableHeader; // Importa la clase para el encabezado de la tabla
import javax.swing.table.TableColumn; // Importa la clase para columnas de la tabla
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales

/**
 * Clase responsable de configurar el aspecto visual de las tablas
 */
public class ConfiguradorTabla { // Declaración de la clase ConfiguradorTabla

    /**
     * Aplica todos los estilos visuales a una tabla dada
     */
    public static void configurarEstilosTabla(JTable tabla) { // Método para aplicar estilos a una tabla
        // Configuración básica de la tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Establece la fuente general de la tabla
        tabla.setRowHeight(28); // Establece la altura de las filas
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el ajuste automático de columnas
        tabla.setFillsViewportHeight(true); // Permite que la tabla llene el viewport
        tabla.getTableHeader().setReorderingAllowed(false); // Impide que el usuario reordene las columnas del encabezado

        configurarEncabezadoTabla(tabla); // Aplica estilos al encabezado de la tabla
        configurarRenderizadoresYAnchos(tabla); // Configura renderizadores y anchos de columnas
        configurarColoresAlternados(tabla); // Aplica colores alternados a las filas
    }

    /**
     * Configura los estilos del encabezado de la tabla
     */
    private static void configurarEncabezadoTabla(JTable tabla) { // Método para configurar el encabezado de la tabla
        JTableHeader header = tabla.getTableHeader(); // Obtiene el encabezado de la tabla
        header.setBackground(Constantes.COLOR_PRIMARIO); // Establece el color de fondo del encabezado
        header.setForeground(Color.WHITE); // Establece el color del texto del encabezado
        header.setFont(Constantes.FUENTE_HEADER); // Establece la fuente del encabezado
        header.setPreferredSize(new Dimension(header.getWidth(), 32)); // Establece la altura preferida del encabezado
    }

    /**
     * Configura anchos de columnas y renderizadores específicos
     */
    private static void configurarRenderizadoresYAnchos(JTable tabla) { // Método para configurar renderizadores y anchos
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Crea un renderizador centrado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centra el contenido de las celdas

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda(); // Crea un renderizador para valores monetarios

        for (int i = 0; i < Constantes.COLUMNAS.length; i++) { // Itera sobre todas las columnas
            TableColumn col = tabla.getColumnModel().getColumn(i); // Obtiene la columna correspondiente
            col.setPreferredWidth(Constantes.ANCHOS_COLUMNAS[i]); // Establece el ancho preferido de la columna

            if (i >= 9) { // Si la columna es de tipo monetario (a partir de la columna 9)
                col.setCellRenderer(moneyRenderer); // Aplica el renderizador de moneda
            } else { // Si no es de tipo monetario
                col.setCellRenderer(centerRenderer); // Aplica el renderizador centrado
            }
        }
    }

    /**
     * Aplica colores alternados a las filas de la tabla
     */
    private static void configurarColoresAlternados(JTable tabla) { // Método para alternar colores de filas
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { // Establece un renderizador por defecto
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column); // Obtiene el componente de celda predeterminado

                if (!isSelected) { // Si la fila no está seleccionada
                    comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR); // Alterna el color de fondo
                }
                return comp; // Devuelve el componente
            }
        });
    }

    /**
     * Crea un renderizador especializado para valores monetarios
     */
    public static DefaultTableCellRenderer crearRenderizadorMoneda() { // Método para crear renderizador de moneda
        return new DefaultTableCellRenderer() { // Devuelve un renderizador personalizado
            @Override
            public void setValue(Object value) { // Sobrescribe el método para establecer el valor
                if (value instanceof Number) { // Si el valor es numérico
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText(String.format("$%,.2f", value)); // Formatea como moneda
                } else if (value instanceof String) { // Si es texto
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText(value.toString()); // Muestra el texto
                } else { // Si no es ninguno de los anteriores
                    super.setValue(value); // Usa el comportamiento por defecto
                }
            }
        };
    }

    /**
     * Configura tabla específica para réplicas
     */
    public static void configurarTablaReplicas(JTable tablaReplicas, String[] columnasReplicas) { // Método para configurar tabla de réplicas
        tablaReplicas.setFont(Constantes.FUENTE_GENERAL); // Establece la fuente general
        tablaReplicas.setRowHeight(28); // Establece la altura de las filas
        tablaReplicas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS); // Ajusta automáticamente el ancho de las columnas
        tablaReplicas.setFillsViewportHeight(true); // Permite que la tabla llene el viewport
        tablaReplicas.getTableHeader().setReorderingAllowed(false); // Impide reordenar columnas

        JTableHeader header = tablaReplicas.getTableHeader(); // Obtiene el encabezado
        header.setBackground(Constantes.COLOR_PRIMARIO); // Color de fondo del encabezado
        header.setForeground(Color.WHITE); // Color del texto del encabezado
        header.setFont(Constantes.FUENTE_HEADER); // Fuente del encabezado

        configurarRenderizadoresReplicas(tablaReplicas, columnasReplicas.length); // Configura renderizadores para la tabla de réplicas
    }

    /**
     * Configura renderizadores específicos para la tabla de réplicas
     */
    private static void configurarRenderizadoresReplicas(JTable tablaReplicas, int numColumnas) { // Método para renderizadores de réplicas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Renderizador centrado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centra el contenido
        tablaReplicas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Aplica a la primera columna

        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda(); // Renderizador de moneda
        for (int i = 1; i < numColumnas; i++) { // Itera sobre las columnas de datos
            tablaReplicas.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer); // Aplica renderizador de moneda
        }

        tablaReplicas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() { // Renderizador por defecto para la tabla de réplicas
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column); // Obtiene el componente de celda predeterminado

                if (!isSelected) { // Si la fila no está seleccionada
                    if (column > 0) { // Si es una columna de datos
                        comp.setBackground(Constantes.COLOR_FONDO_REPLICA); // Aplica color de fondo especial
                    } else { // Si es la primera columna
                        comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR); // Alterna el color de fondo
                    }
                }

                aplicarFormatoColumna(column, value); // Aplica formato especial según la columna
                return comp; // Devuelve el componente
            }

            private void aplicarFormatoColumna(int column, Object value) { // Método auxiliar para formato de columna
                if (column == 0) { // Si es la primera columna
                    setHorizontalAlignment(SwingConstants.CENTER); // Centra el contenido
                } else if (value instanceof Number) { // Si es un número
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                    setText(String.format("$%,.2f", value)); // Formatea como moneda
                } else if (value instanceof String && column > 0) { // Si es texto en columna de datos
                    setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
                }
            }
        });
    }
}
