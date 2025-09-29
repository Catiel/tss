package actividad_7.manual;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

/**
 * Diálogo para permitir la entrada manual de valores Rn
 */
public class EntradaManualDialog extends JDialog {

    private double[] valoresRn;
    private boolean confirmado = false;
    private JTable tablaEntrada;
    private DefaultTableModel modeloEntrada;

    public EntradaManualDialog(JFrame parent, int cantidadDias) {
        super(parent, "Entrada Manual de Valores Rn", true);
        this.valoresRn = new double[cantidadDias];

        // Inicializar con valores aleatorios por defecto
        for (int i = 0; i < cantidadDias; i++) {
            this.valoresRn[i] = Math.random();
        }

        initComponents(cantidadDias);
        setupLayout();
        setupEventHandlers();
    }

    private void initComponents(int cantidadDias) {
        // Crear modelo de tabla para entrada de datos
        String[] columnas = {"Día", "Valor Rn"};
        modeloEntrada = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Solo la columna de Rn es editable
            }

            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        // Llenar tabla con datos iniciales
        for (int i = 0; i < cantidadDias; i++) {
            modeloEntrada.addRow(new Object[]{
                i + 1,
                String.format("%.4f", valoresRn[i])
            });
        }

        tablaEntrada = new JTable(modeloEntrada);
        configurarTablaEntrada();
    }

    private void configurarTablaEntrada() {
        // Configuración básica
        tablaEntrada.setFont(Constantes.FUENTE_GENERAL);
        tablaEntrada.setRowHeight(25);
        tablaEntrada.setGridColor(Color.LIGHT_GRAY);
        tablaEntrada.setShowGrid(true);
        tablaEntrada.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Configurar anchos de columnas
        tablaEntrada.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaEntrada.getColumnModel().getColumn(0).setMaxWidth(80);
        tablaEntrada.getColumnModel().getColumn(0).setMinWidth(80);
        tablaEntrada.getColumnModel().getColumn(1).setPreferredWidth(120);

        // Configurar header
        tablaEntrada.getTableHeader().setBackground(Constantes.COLOR_PRIMARIO);
        tablaEntrada.getTableHeader().setForeground(Color.WHITE);
        tablaEntrada.getTableHeader().setFont(Constantes.FUENTE_HEADER);

        // Renderizador para centrar la primera columna
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        tablaEntrada.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);

        // Renderizador para la segunda columna (valores Rn)
        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        tablaEntrada.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        // Editor personalizado para validación
        tablaEntrada.getColumnModel().getColumn(1).setCellEditor(new RnCellEditor());
    }

    private void setupLayout() {
        setLayout(new BorderLayout(10, 10));

        // Panel superior con instrucciones
        JPanel panelInstrucciones = new JPanel(new BorderLayout());
        panelInstrucciones.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));
        panelInstrucciones.setBackground(Color.WHITE);

        JLabel lblTitulo = new JLabel("Entrada Manual de Valores Rn", SwingConstants.CENTER);
        lblTitulo.setFont(Constantes.FUENTE_TITULO);
        lblTitulo.setForeground(Constantes.COLOR_PRIMARIO);

        JTextArea txtInstrucciones = new JTextArea(
            "Ingrese los valores Rn para cada día (valores entre 0.0000 y 0.9999).\n" +
            "Los valores por defecto son aleatorios. Puede editarlos haciendo clic en la celda correspondiente."
        );
        txtInstrucciones.setFont(Constantes.FUENTE_GENERAL);
        txtInstrucciones.setEditable(false);
        txtInstrucciones.setOpaque(false);
        txtInstrucciones.setWrapStyleWord(true);
        txtInstrucciones.setLineWrap(true);

        panelInstrucciones.add(lblTitulo, BorderLayout.NORTH);
        panelInstrucciones.add(Box.createVerticalStrut(10), BorderLayout.CENTER);
        panelInstrucciones.add(txtInstrucciones, BorderLayout.SOUTH);

        // Panel central con la tabla
        JScrollPane scrollPane = new JScrollPane(tablaEntrada);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 15, 0, 15));
        scrollPane.setPreferredSize(new Dimension(300, 400));

        // Panel inferior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelBotones.setBackground(Color.WHITE);

        JButton btnGenearAleatorios = new JButton("Generar Aleatorios");
        btnGenearAleatorios.setFont(Constantes.FUENTE_GENERAL);
        btnGenearAleatorios.addActionListener(e -> generarValoresAleatorios());

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(Constantes.FUENTE_GENERAL);
        btnCancelar.addActionListener(e -> {
            confirmado = false;
            dispose();
        });

        JButton btnAceptar = new JButton("Aceptar");
        btnAceptar.setFont(Constantes.FUENTE_GENERAL);
        btnAceptar.setBackground(Constantes.COLOR_PRIMARIO);
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.addActionListener(e -> {
            if (validarYguardarDatos()) {
                confirmado = true;
                dispose();
            }
        });

        panelBotones.add(btnGenearAleatorios);
        panelBotones.add(btnCancelar);
        panelBotones.add(btnAceptar);

        // Agregar componentes al diálogo
        add(panelInstrucciones, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        // Configuración del diálogo
        setSize(400, 600);
        setLocationRelativeTo(getParent());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
    }

    private void setupEventHandlers() {
        // Manejar Enter y Escape
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
            .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESCAPE");
        getRootPane().getActionMap().put("ESCAPE", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                confirmado = false;
                dispose();
            }
        });
    }

    private void generarValoresAleatorios() {
        for (int i = 0; i < valoresRn.length; i++) {
            double nuevoValor = Math.random();
            valoresRn[i] = nuevoValor;
            modeloEntrada.setValueAt(String.format("%.4f", nuevoValor), i, 1);
        }
        tablaEntrada.repaint();
    }

    private boolean validarYguardarDatos() {
        try {
            for (int i = 0; i < modeloEntrada.getRowCount(); i++) {
                String valorStr = (String) modeloEntrada.getValueAt(i, 1);
                double valor = Double.parseDouble(valorStr.replace(",", "."));

                if (valor < 0.0 || valor >= 1.0) {
                    JOptionPane.showMessageDialog(this,
                        "El valor en el día " + (i + 1) + " debe estar entre 0.0000 y 0.9999",
                        "Error de Validación",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }

                valoresRn[i] = valor;
            }
            return true;
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                "Todos los valores deben ser números válidos",
                "Error de Formato",
                JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public double[] getValoresRn() {
        return valoresRn.clone();
    }

    public boolean isConfirmado() {
        return confirmado;
    }

    /**
     * Editor de celda personalizado para validación en tiempo real
     */
    private class RnCellEditor extends DefaultCellEditor {
        private JTextField textField;
        private NumberFormat formatter;

        public RnCellEditor() {
            super(new JTextField());
            textField = (JTextField) getComponent();
            formatter = DecimalFormat.getNumberInstance(Locale.US);
            formatter.setMinimumFractionDigits(4);
            formatter.setMaximumFractionDigits(4);

            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyTyped(KeyEvent e) {
                    char c = e.getKeyChar();
                    String currentText = textField.getText();

                    // Permitir dígitos, punto decimal y teclas de control
                    if (!Character.isDigit(c) && c != '.' && c != ',' &&
                        !Character.isISOControl(c)) {
                        e.consume();
                    }

                    // Permitir solo un punto decimal
                    if ((c == '.' || c == ',') && (currentText.contains(".") || currentText.contains(","))) {
                        e.consume();
                    }
                }
            });
        }

        @Override
        public boolean stopCellEditing() {
            String value = textField.getText().replace(",", ".");
            try {
                double doubleValue = Double.parseDouble(value);
                if (doubleValue >= 0.0 && doubleValue < 1.0) {
                    textField.setText(String.format("%.4f", doubleValue));
                    return super.stopCellEditing();
                } else {
                    JOptionPane.showMessageDialog(textField,
                        "El valor debe estar entre 0.0000 y 0.9999",
                        "Valor Inválido",
                        JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(textField,
                    "Ingrese un número válido",
                    "Formato Inválido",
                    JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
}
