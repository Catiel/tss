package actividad_7.aleatorio; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.table.*; // Importa clases para manipulación de tablas en Swing
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales

/**
 * Clase responsable de configurar el aspecto visual de las tablas
 */
public class ConfiguradorTabla { // Declaración de la clase ConfiguradorTabla

    /**
     * Aplica todos los estilos visuales a una tabla dada
     */
    public static void configurarEstilosTabla(JTable tabla) { // Método para aplicar estilos a una tabla
        tabla.setFont(Constantes.FUENTE_GENERAL); // Establece la fuente general de la tabla
        tabla.setRowHeight(28); // Establece la altura de las filas
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el ajuste automático de columnas
        tabla.setFillsViewportHeight(true); // Permite que la tabla llene el viewport
        tabla.getTableHeader().setReorderingAllowed(false); // Impide que el usuario reordene las columnas del encabezado

        // Aplica estilos al encabezado de la tabla
        configurarEncabezadoTabla(tabla);
        // Configura renderizadores y anchos de columnas
        configurarRenderizadoresYAnchos(tabla);
        // Aplica colores alternados a las filas
        configurarColoresAlternados(tabla);
    }

    /**
     * Configura los estilos del encabezado de la tabla
     */
    private static void configurarEncabezadoTabla(JTable tabla) { // Método para configurar el encabezado de la tabla
        // Obtiene el encabezado de la tabla
        JTableHeader header = tabla.getTableHeader();
        // Establece el color de fondo del encabezado
        header.setBackground(Constantes.COLOR_PRIMARIO);
        // Establece el color del texto del encabezado
        header.setForeground(Color.WHITE);
        // Establece la fuente del encabezado
        header.setFont(Constantes.FUENTE_HEADER);
        // Establece la altura preferida del encabezado
        header.setPreferredSize(new Dimension(header.getWidth(), 32));
    }

    /**
     * Configura anchos de columnas y renderizadores específicos
     */
    private static void configurarRenderizadoresYAnchos(JTable tabla) { // Método para configurar renderizadores y anchos
        // Renderizador para centrar el contenido de las celdas
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        // Renderizador para valores monetarios
        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();

        // Itera sobre todas las columnas definidas en Constantes.COLUMNAS
        for (int i = 0; i < Constantes.COLUMNAS.length; i++) {
            // Obtiene la columna correspondiente
            TableColumn col = tabla.getColumnModel().getColumn(i);
            // Establece el ancho preferido de la columna
            col.setPreferredWidth(Constantes.ANCHOS_COLUMNAS[i]);

            // Si la columna es de tipo monetario (a partir de la columna 9)
            if (i >= 9) {
                // Aplica el renderizador de moneda
                col.setCellRenderer(moneyRenderer);
            } else {
                // Aplica el renderizador centrado
                col.setCellRenderer(centerRenderer);
            }
        }
    }

    /**
     * Aplica colores alternados a las filas de la tabla
     */
    private static void configurarColoresAlternados(JTable tabla) { // Método para alternar colores de filas
        // Establece un renderizador por defecto para alternar colores de fila
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, // Sobrescribe el método de renderizado
                    boolean isSelected, boolean hasFocus, int row, int column) {
                // Obtiene el componente de celda predeterminado
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Si la fila no está seleccionada, alterna el color de fondo
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
    public static DefaultTableCellRenderer crearRenderizadorMoneda() { // Método para crear renderizador de moneda
        // Devuelve un renderizador personalizado para mostrar valores monetarios
        return new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) { // Sobrescribe el método para establecer el valor
                // Si el valor es numérico, lo formatea como moneda
                if (value instanceof Number) {
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
        // Establece la fuente general de la tabla de réplicas
        tablaReplicas.setFont(Constantes.FUENTE_GENERAL);
        // Establece la altura de las filas
        tablaReplicas.setRowHeight(28);
        // Ajusta automáticamente el ancho de las columnas
        tablaReplicas.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        // Permite que la tabla llene el viewport
        tablaReplicas.setFillsViewportHeight(true);
        // Impide que el usuario reordene las columnas del encabezado
        tablaReplicas.getTableHeader().setReorderingAllowed(false);

        // Configura el encabezado de la tabla de réplicas
        JTableHeader header = tablaReplicas.getTableHeader();
        header.setBackground(Constantes.COLOR_PRIMARIO);
        header.setForeground(Color.WHITE);
        header.setFont(Constantes.FUENTE_HEADER);

        // Configura renderizadores específicos para la tabla de réplicas
        configurarRenderizadoresReplicas(tablaReplicas, columnasReplicas.length);
    }

    /**
     * Configura renderizadores específicos para la tabla de réplicas
     */
    private static void configurarRenderizadoresReplicas(JTable tablaReplicas, int numColumnas) { // Método para renderizadores de réplicas
        // Renderizador para centrar la primera columna
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaReplicas.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Renderizador para valores monetarios en las demás columnas
        DefaultTableCellRenderer moneyRenderer = crearRenderizadorMoneda();
        for (int i = 1; i < numColumnas; i++) { // Itera sobre las columnas de datos
            tablaReplicas.getColumnModel().getColumn(i).setCellRenderer(moneyRenderer); // Aplica renderizador de moneda
        }

        // Renderizador por defecto para alternar colores y formato en la tabla de réplicas
        tablaReplicas.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, // Sobrescribe el método de renderizado
                    boolean isSelected, boolean hasFocus, int row, int column) {
                // Obtiene el componente de celda predeterminado
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);

                // Si la fila no está seleccionada
                if (!isSelected) {
                    if (column > 0) {
                        // Aplica color de fondo especial para columnas de datos
                        comp.setBackground(Constantes.COLOR_FONDO_REPLICA);
                    } else {
                        // Alterna el color de fondo para la primera columna
                        comp.setBackground(row % 2 == 0 ? Constantes.COLOR_FILA_PAR : Constantes.COLOR_FILA_IMPAR);
                    }
                }

                // Aplica formato especial según la columna
                aplicarFormatoColumna(column, value);
                return comp;
            }

            // Método auxiliar para aplicar formato según el tipo de columna
            private void aplicarFormatoColumna(int column, Object value) {
                if (column == 0) {
                    // Centra el contenido de la primera columna
                    setHorizontalAlignment(SwingConstants.CENTER);
                } else if (value instanceof Number) {
                    // Formatea como moneda y alinea a la derecha
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String && column > 0) {
                    // Si es texto en columna de datos, alinea a la derecha
                    setHorizontalAlignment(SwingConstants.RIGHT);
                }
            }
        });
    }
}
