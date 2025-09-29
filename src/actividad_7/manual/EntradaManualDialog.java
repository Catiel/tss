package actividad_7.manual; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de javax.swing para interfaces gráficas
import javax.swing.table.DefaultTableCellRenderer; // Importa el renderizador de celdas por defecto
import javax.swing.table.DefaultTableModel; // Importa DefaultTableModel para manipular datos de tablas
import java.awt.*; // Importa clases para manejo de gráficos y componentes visuales
import java.awt.event.KeyAdapter; // Importa KeyAdapter para eventos de teclado
import java.awt.event.KeyEvent; // Importa KeyEvent para eventos de teclado
import java.text.DecimalFormat; // Importa DecimalFormat para formateo de números
import java.text.NumberFormat; // Importa NumberFormat para formateo de números
import java.text.ParseException; // Importa ParseException para manejo de errores de parseo
import java.util.Locale; // Importa Locale para internacionalización

/**
 * Diálogo para permitir la entrada manual de valores Rn
 */
public class EntradaManualDialog extends JDialog { // Declaración de la clase EntradaManualDialog que extiende JDialog

    private double[] valoresRn; // Arreglo para almacenar los valores Rn
    private boolean confirmado = false; // Bandera para saber si se confirmó la entrada
    private JTable tablaEntrada; // Tabla para la entrada de valores
    private DefaultTableModel modeloEntrada; // Modelo de la tabla de entrada

    public EntradaManualDialog(JFrame parent, int cantidadDias) { // Constructor del diálogo
        super(parent, "Entrada Manual de Valores Rn", true); // Llama al constructor de JDialog con título y modal
        this.valoresRn = new double[cantidadDias]; // Inicializa el arreglo de valores Rn

        // Inicializar con valores aleatorios por defecto
        for (int i = 0; i < cantidadDias; i++) { // Recorre los días
            this.valoresRn[i] = Math.random(); // Asigna un valor aleatorio entre 0 y 1
        }

        initComponents(cantidadDias); // Inicializa los componentes
        setupLayout(); // Configura el layout del diálogo
        setupEventHandlers(); // Configura los eventos
    }

    private void initComponents(int cantidadDias) { // Método para inicializar componentes
        // Crear modelo de tabla para entrada de datos
        String[] columnas = {"Día", "Valor Rn"}; // Nombres de las columnas
        modeloEntrada = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { // Solo la columna de Rn es editable
                return column == 1;
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) { // Define el tipo de dato de cada columna
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        // Llenar tabla con datos iniciales
        for (int i = 0; i < cantidadDias; i++) { // Recorre los días
            modeloEntrada.addRow(new Object[]{
                i + 1, // Día
                String.format("%.4f", valoresRn[i]) // Valor Rn formateado
            });
        }

        tablaEntrada = new JTable(modeloEntrada); // Crea la tabla de entrada
        configurarTablaEntrada(); // Configura la tabla
    }

    private void configurarTablaEntrada() { // Método para configurar la tabla de entrada
        // Configuración básica
        tablaEntrada.setFont(Constantes.FUENTE_GENERAL); // Fuente general
        tablaEntrada.setRowHeight(25); // Altura de las filas
        tablaEntrada.setGridColor(Color.LIGHT_GRAY); // Color de la grilla
        tablaEntrada.setShowGrid(true); // Muestra la grilla
        tablaEntrada.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Selección de una sola fila

        // Configurar anchos de columnas
        tablaEntrada.getColumnModel().getColumn(0).setPreferredWidth(80); // Ancho preferido columna Día
        tablaEntrada.getColumnModel().getColumn(0).setMaxWidth(80); // Ancho máximo columna Día
        tablaEntrada.getColumnModel().getColumn(0).setMinWidth(80); // Ancho mínimo columna Día
        tablaEntrada.getColumnModel().getColumn(1).setPreferredWidth(120); // Ancho preferido columna Valor Rn

        // Configurar header
        tablaEntrada.getTableHeader().setBackground(Constantes.COLOR_PRIMARIO); // Fondo del header
        tablaEntrada.getTableHeader().setForeground(Color.WHITE); // Color del texto del header
        tablaEntrada.getTableHeader().setFont(Constantes.FUENTE_HEADER); // Fuente del header

        // Renderizador para centrar la primera columna
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer(); // Renderizador centrado
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER); // Centra el contenido
        tablaEntrada.getColumnModel().getColumn(0).setCellRenderer(centerRenderer); // Aplica a la columna Día

        // Renderizador para la segunda columna (valores Rn)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer(); // Renderizador a la derecha
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT); // Alinea a la derecha
        tablaEntrada.getColumnModel().getColumn(1).setCellRenderer(rightRenderer); // Aplica a la columna Valor Rn

        // Editor personalizado para validación
        tablaEntrada.getColumnModel().getColumn(1).setCellEditor(new RnCellEditor()); // Editor personalizado
    }

    private void setupLayout() { // Método para configurar el layout del diálogo
        setLayout(new BorderLayout(10, 10)); // Layout principal

        // Panel superior con instrucciones
        JPanel panelInstrucciones = new JPanel(new BorderLayout()); // Panel de instrucciones
        panelInstrucciones.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15)); // Borde del panel
        panelInstrucciones.setBackground(Color.WHITE); // Fondo blanco

        JLabel lblTitulo = new JLabel("Entrada Manual de Valores Rn", SwingConstants.CENTER); // Título
        lblTitulo.setFont(Constantes.FUENTE_TITULO); // Fuente del título
        lblTitulo.setForeground(Constantes.COLOR_PRIMARIO); // Color del título

        JTextArea txtInstrucciones = new JTextArea(
            "Ingrese los valores Rn para cada día (valores entre 0.0000 y 0.9999).\n" +
            "Los valores por defecto son aleatorios. Puede editarlos haciendo clic en la celda correspondiente."
        ); // Instrucciones
        txtInstrucciones.setFont(Constantes.FUENTE_GENERAL); // Fuente de las instrucciones
        txtInstrucciones.setEditable(false); // No editable
        txtInstrucciones.setOpaque(false); // Fondo transparente
        txtInstrucciones.setWrapStyleWord(true); // Ajuste de palabras
        txtInstrucciones.setLineWrap(true); // Salto de línea

        panelInstrucciones.add(lblTitulo, BorderLayout.NORTH); // Añade el título
        panelInstrucciones.add(Box.createVerticalStrut(10), BorderLayout.CENTER); // Espacio vertical
        panelInstrucciones.add(txtInstrucciones, BorderLayout.SOUTH); // Añade las instrucciones

        // Panel central con la tabla
        JScrollPane scrollPane = new JScrollPane(tablaEntrada); // Scroll para la tabla
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15)); // Borde del scroll
        scrollPane.setPreferredSize(new Dimension(300, 400)); // Tamaño preferido

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15)); // Panel de botones
        panelBotones.setBackground(Color.WHITE); // Fondo blanco

        JButton btnGenearAleatorios = new JButton("Generar Aleatorios"); // Botón para generar aleatorios
        btnGenearAleatorios.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        btnGenearAleatorios.addActionListener(e -> generarValoresAleatorios()); // Acción del botón

        JButton btnCancelar = new JButton("Cancelar"); // Botón cancelar
        btnCancelar.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        btnCancelar.addActionListener(e -> {
            confirmado = false; // Marca como no confirmado
            dispose(); // Cierra el diálogo
        });

        JButton btnAceptar = new JButton("Aceptar"); // Botón aceptar
        btnAceptar.setFont(Constantes.FUENTE_GENERAL); // Fuente del botón
        btnAceptar.setBackground(Constantes.COLOR_PRIMARIO); // Fondo del botón
        btnAceptar.setForeground(Color.WHITE); // Color del texto
        btnAceptar.addActionListener(e -> {
            if (validarYguardarDatos()) { // Valida y guarda los datos
                confirmado = true; // Marca como confirmado
                dispose(); // Cierra el diálogo
            }
        });

        panelBotones.add(btnGenearAleatorios); // Añade el botón de aleatorios
        panelBotones.add(btnCancelar); // Añade el botón cancelar
        panelBotones.add(btnAceptar); // Añade el botón aceptar

        // Agregar componentes al diálogo
        add(panelInstrucciones, BorderLayout.NORTH); // Añade el panel de instrucciones
        add(scrollPane, BorderLayout.CENTER); // Añade el scroll de la tabla
        add(panelBotones, BorderLayout.SOUTH); // Añade el panel de botones

        // Configuración del diálogo
        setSize(400, 600); // Tamaño del diálogo
        setLocationRelativeTo(getParent()); // Centra el diálogo respecto al padre
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Cierra el diálogo al cerrar
        getContentPane().setBackground(Color.WHITE); // Fondo blanco
    }

    private void setupEventHandlers() { // Método para configurar eventos
        // Manejar Enter y Escape
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE"); // Asocia ESCAPE
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmado = false; // Marca como no confirmado
                dispose(); // Cierra el diálogo
            }
        });
    }

    private void generarValoresAleatorios() { // Método para generar valores aleatorios
        for (int i = 0; i < valoresRn.length; i++) { // Recorre los días
            double nuevoValor = Math.random(); // Genera un valor aleatorio
            valoresRn[i] = nuevoValor; // Asigna el valor
            modeloEntrada.setValueAt(String.format("%.4f", nuevoValor), i, 1); // Actualiza la tabla
        }
        tablaEntrada.repaint(); // Repinta la tabla
    }

    private boolean validarYguardarDatos() { // Método para validar y guardar los datos
        try {
            for (int i = 0; i < modeloEntrada.getRowCount(); i++) { // Recorre las filas
                String valorStr = (String) modeloEntrada.getValueAt(i, 1); // Obtiene el valor como String
                double valor = Double.parseDouble(valorStr.replace(",", ".")); // Convierte a double

                if (valor < 0.0 || valor >= 1.0) { // Valida el rango
                    JOptionPane.showMessageDialog(this,
                        "El valor en el día " + (i + 1) + " debe estar entre 0.0000 y 0.9999",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                valoresRn[i] = valor; // Asigna el valor al arreglo
            }
            return true; // Si todo es válido
        } catch (NumberFormatException e) { // Si hay error de formato
            JOptionPane.showMessageDialog(this,
                "Todos los valores deben ser números válidos",
                "Error de Formato",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public double[] getValoresRn() { // Getter para los valores Rn
        return valoresRn.clone(); // Devuelve una copia del arreglo
    }

    public boolean isConfirmado() { // Getter para saber si se confirmó
        return confirmado;
    }

    /**
     * Editor de celda personalizado para validación en tiempo real
     */
    private class RnCellEditor extends DefaultCellEditor { // Clase interna para el editor de celdas
        private JTextField textField; // Campo de texto para la celda
        private NumberFormat formatter; // Formateador de números

        public RnCellEditor() { // Constructor del editor
            super(new JTextField()); // Llama al constructor de DefaultCellEditor
            textField = (JTextField) getComponent(); // Obtiene el campo de texto
            formatter = DecimalFormat.getNumberInstance(Locale.US); // Formateador para US
            formatter.setMinimumFractionDigits(4); // Mínimo 4 decimales
            formatter.setMaximumFractionDigits(4); // Máximo 4 decimales

            textField.addKeyListener(new KeyAdapter() { // Listener para teclas
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar(); // Carácter presionado
                    String currentText = textField.getText(); // Texto actual

                    // Permitir dígitos, punto decimal y teclas de control
                    if (!Character.isDigit(c) && c != '.' && c != ',' &&
                        !Character.isISOControl(c)) {
                        e.consume(); // Ignora el carácter
                    }

                    // Permitir solo un punto decimal
                    if ((c == '.' || c == ',') && (currentText.contains(".") || currentText.contains(","))) {
                        e.consume(); // Ignora si ya hay un punto o coma
                    }
                }
            });
        }

        @Override
        public boolean stopCellEditing() { // Método para validar al terminar la edición
            String value = textField.getText().replace(",", "."); // Reemplaza coma por punto
            try {
                double doubleValue = Double.parseDouble(value); // Convierte a double
                if (doubleValue >= 0.0 && doubleValue < 1.0) { // Valida el rango
                    textField.setText(String.format("%.4f", doubleValue)); // Formatea el texto
                    return super.stopCellEditing(); // Termina la edición
                } else {
                    JOptionPane.showMessageDialog(textField,
                        "El valor debe estar entre 0.0000 y 0.9999",
                        "Valor Inválido",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) { // Si hay error de formato
                JOptionPane.showMessageDialog(textField,
                    "Ingrese un número válido",
                    "Formato Inválido",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
}
