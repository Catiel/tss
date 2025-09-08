package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Layouts y utilidades AWT
import java.awt.event.ActionEvent; // Eventos de acción para botones
import java.util.Random; // Generador de números aleatorios

/**
 * Panel que genera tasas de descuento aleatorias y muestra cómo afectan a la diferencia de VAN.
 */
public class PanelTablaRandom extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios de parámetros
    private final DefaultTableModel modeloTabla; // Modelo de datos (no editable)
    private final JTable tablaSensibilidad;      // Tabla de visualización
    private final JTextField txtMinimo;          // Campo tasa mínima (%)
    private final JTextField txtMaximo;          // Campo tasa máxima (%)
    private final JTextField txtCantidad;        // Campo cantidad de tasas a generar
    private final JButton btnGenerar;            // Botón generar valores aleatorios
    private final JButton btnLimpiar;            // Botón limpiar la tabla

    /**
     * Constructor del panel de tabla con valores aleatorios para el análisis de sensibilidad.
     */
    public PanelTablaRandom() { // Inicio constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como oyente de cambios

        EstilosUI.aplicarEstiloPanel(this);        // Aplica estilo base
        setLayout(new BorderLayout(10, 10));       // Layout principal con márgenes

        JLabel titulo = new JLabel("Tabla con tasas de descuento aleatorias"); // Título del panel
        EstilosUI.aplicarEstiloTitulo(titulo);     // Estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Márgenes del título
        add(titulo, BorderLayout.NORTH);           // Añade título arriba

        JPanel panelSuperior = new PanelSuperiorRandom(); // Panel de controles superiores

        String[] columnas = {"Tasa de descuento", "Diferencia VAN"}; // Encabezados de tabla

        modeloTabla = new DefaultTableModel(columnas, 0) { // Modelo sin filas iniciales
            @Override public boolean isCellEditable(int row, int col) { return false; } // No editable
        };

        tablaSensibilidad = new JTable(modeloTabla); // Crea tabla
        EstilosUI.aplicarEstiloTabla(tablaSensibilidad); // Aplica estilo

        tablaSensibilidad.getColumnModel().getColumn(0).setPreferredWidth(150); // Ajusta ancho col 0
        tablaSensibilidad.getColumnModel().getColumn(1).setPreferredWidth(250); // Ajusta ancho col 1

        JScrollPane scrollPane = new JScrollPane(tablaSensibilidad); // Scroll para tabla
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Márgenes

        JPanel panelCentral = new JPanel(new BorderLayout()); // Panel central contenedor
        panelCentral.add(panelSuperior, BorderLayout.NORTH);  // Controles arriba
        panelCentral.add(scrollPane, BorderLayout.CENTER);    // Tabla al centro
        add(panelCentral, BorderLayout.CENTER);               // Añade al panel principal

        JPanel panelDescripcion = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel inferior descripción
        JLabel lblDescripcion = new JLabel("Esta tabla muestra cómo varía la diferencia de VAN entre los escenarios con y sin versión francesa para tasas de descuento generadas aleatoriamente."); // Texto descriptivo
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12)); // Fuente itálica
        panelDescripcion.add(lblDescripcion); // Añade descripción
        add(panelDescripcion, BorderLayout.SOUTH); // Añade panel inferior

        // Referencias a los componentes internos del panel superior
        txtMinimo = ((PanelSuperiorRandom)panelSuperior).txtMinimo;   // Campo mínimo
        txtMaximo = ((PanelSuperiorRandom)panelSuperior).txtMaximo;   // Campo máximo
        txtCantidad = ((PanelSuperiorRandom)panelSuperior).txtCantidad; // Campo cantidad
        btnGenerar = ((PanelSuperiorRandom)panelSuperior).btnGenerar; // Botón generar
        btnLimpiar = ((PanelSuperiorRandom)panelSuperior).btnLimpiar; // Botón limpiar

        configurarListeners(); // Configura acciones de botones
    }

    /**
     * Panel superior con controles para generar valores aleatorios
     */
    private class PanelSuperiorRandom extends JPanel { // Panel interno con inputs
        final JTextField txtMinimo;   // Campo tasa mínima (%)
        final JTextField txtMaximo;   // Campo tasa máxima (%)
        final JTextField txtCantidad; // Campo cantidad de valores a generar
        final JButton btnGenerar;     // Botón generar
        final JButton btnLimpiar;     // Botón limpiar

        PanelSuperiorRandom() { // Constructor panel superior
            setLayout(new FlowLayout(FlowLayout.LEFT)); // Distribución horizontal
            setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // Márgenes

            JLabel lblMinimo = new JLabel("Tasa mínima (%): "); // Etiqueta mínimo
            txtMinimo = new JTextField("5", 5);                 // Valor por defecto 5%

            JLabel lblMaximo = new JLabel("Tasa máxima (%): "); // Etiqueta máximo
            txtMaximo = new JTextField("30", 5);                // Valor por defecto 30%

            JLabel lblCantidad = new JLabel("Cantidad: ");      // Etiqueta cantidad
            txtCantidad = new JTextField("10", 5);              // Valor por defecto 10

            btnGenerar = new JButton("Generar");                // Botón generar
            EstilosUI.aplicarEstiloBoton(btnGenerar);            // Estilo
            btnLimpiar = new JButton("Limpiar");                // Botón limpiar
            EstilosUI.aplicarEstiloBoton(btnLimpiar);            // Estilo

            add(lblMinimo); add(txtMinimo);        // Agrega mínimo
            add(lblMaximo); add(txtMaximo);        // Agrega máximo
            add(lblCantidad); add(txtCantidad);    // Agrega cantidad
            add(btnGenerar); add(btnLimpiar);      // Agrega botones
        }
    }

    /**
     * Configura los listeners para los botones de la interfaz
     */
    private void configurarListeners() { // Define acciones
        btnGenerar.addActionListener((ActionEvent e) -> { // Acción generar
            try {
                double minimo = Double.parseDouble(txtMinimo.getText().trim());   // Lee mínimo
                double maximo = Double.parseDouble(txtMaximo.getText().trim());   // Lee máximo
                int cantidad = Integer.parseInt(txtCantidad.getText().trim());    // Lee cantidad

                if (minimo < 0 || minimo >= 100) { // Validación mínimo
                    JOptionPane.showMessageDialog(this, "La tasa mínima debe estar entre 0 y 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE); return; }
                if (maximo <= minimo || maximo > 100) { // Validación máximo
                    JOptionPane.showMessageDialog(this, "La tasa máxima debe ser mayor que la mínima y menor o igual a 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE); return; }
                if (cantidad <= 0 || cantidad > 100) { // Validación cantidad
                    JOptionPane.showMessageDialog(this, "La cantidad debe estar entre 1 y 100", "Valor fuera de rango", JOptionPane.WARNING_MESSAGE); return; }

                modeloTabla.setRowCount(0); // Limpia tabla anterior

                Random random = new Random(); // Generador aleatorio
                for (int i = 0; i < cantidad; i++) { // Genera cada tasa
                    double tasa = minimo + (maximo - minimo) * random.nextDouble(); // Tasa en rango
                    tasa = tasa / 100.0; // Convierte a decimal
                    calcularYAgregarFila(tasa); // Calcula y agrega
                }
            } catch (NumberFormatException ex) { // Error de formato
                JOptionPane.showMessageDialog(this, "Por favor ingrese números válidos en todos los campos", "Error de formato", JOptionPane.ERROR_MESSAGE); }
        });

        btnLimpiar.addActionListener((ActionEvent e) -> { // Acción limpiar
            modeloTabla.setRowCount(0); // Borra filas
        });
    }

    /**
     * Calcula la diferencia de VAN para una tasa específica y la agrega a la tabla
     */
    private void calcularYAgregarFila(double tasa) { // Añade fila calculada
        try {
            ControladorParametros params = ControladorParametros.getInstancia(); // Parámetros actuales
            ModeloSoftwareCalculo.ResultadoComparativo resultado = // Calcula comparativo
                ModeloSoftwareCalculo.calcularComparativo(params, tasa);
            String tasaFormateada = String.format("%.2f%%", tasa * 100); // Formatea tasa
            String diferenciaFormateada = formatearMoneda(resultado.diferenciaVAN); // Formatea VAN
            modeloTabla.addRow(new Object[]{tasaFormateada, diferenciaFormateada}); // Inserta fila
            aplicarFormatoCondicional(); // Actualiza colores
        } catch (Exception ex) { // Cualquier error
            ex.printStackTrace(); // Traza
            JOptionPane.showMessageDialog(this, "Error al calcular el resultado", "Error", JOptionPane.ERROR_MESSAGE); // Mensaje error
        }
    }

    /**
     * Aplica formato visual condicional (verde positivo, rojo negativo)
     */
    private void aplicarFormatoCondicional() { // Renderer condicional
        tablaSensibilidad.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column); // Base
                if (column == 1) { // Sólo columna diferencia
                    String valorTexto = (String) value; // Texto
                    if (valorTexto.contains("-")) c.setForeground(new Color(192, 0, 0)); // Negativo => rojo
                    else c.setForeground(new Color(0, 128, 0)); // Positivo => verde
                } else c.setForeground(Color.BLACK); // Otras columnas
                return c; // Retorna componente estilizado
            }
        });
    }

    /** Formatea valor como moneda */
    private String formatearMoneda(double valor) { return UtilidadesFormato.formatearMoneda(valor); }

    /** Actualiza tabla tras cambios en parámetros si ya había datos */
    private void actualizarTabla() { // Recalcula filas existentes
        if (modeloTabla.getRowCount() == 0) return; // Nada que recalcular
        java.util.List<Double> tasas = new java.util.ArrayList<>(); // Lista de tasas
        for (int i = 0; i < modeloTabla.getRowCount(); i++) { // Recorre filas
            String tasaStr = (String) modeloTabla.getValueAt(i, 0); // Lee texto
            tasaStr = tasaStr.replace("%", "").trim(); // Quita %
            tasas.add(Double.parseDouble(tasaStr) / 100.0); // Convierte a decimal
        }
        modeloTabla.setRowCount(0); // Limpia
        for (Double t : tasas) calcularYAgregarFila(t); // Recalcula cada una
    }

    /** Notificación de cambio de parámetros */
    @Override public void onParametrosChanged() { SwingUtilities.invokeLater(this::actualizarTabla); }

    /** Limpieza de listener al remover panel */
    @Override public void removeNotify() { // Eliminación del panel
        ControladorParametros.getInstancia().removeChangeListener(this); // Se des-registra
        super.removeNotify(); // Llama a super
    }
}
