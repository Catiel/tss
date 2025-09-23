package actividad_6.ejercicio3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio3Manual extends JFrame {

    private JTextField txtNumEnsambles;
    private JTextField txtMinBarraA;
    private JTextField txtMaxBarraA;
    private JTextField txtValorEsperadoErlang;
    private JTextField txtFormaErlang;
    private JTextField txtEspecInf;
    private JTextField txtEspecSup;
    private DefaultTableModel model;
    private JTable tabla;
    private JButton btnCrearFilas;
    private JButton btnSimular;

    public ejercicio3Manual() {
        setTitle("Simulación Barras Defectuosas - Ingreso Manual");
        setSize(1400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout());

        panelSuperior.add(new JLabel("Número de Ensambles:"));
        txtNumEnsambles = new JTextField("15", 5);
        panelSuperior.add(txtNumEnsambles);

        panelSuperior.add(new JLabel("Mín Barra A (cm):"));
        txtMinBarraA = new JTextField("45", 5);
        txtMinBarraA.setEnabled(false);
        panelSuperior.add(txtMinBarraA);

        panelSuperior.add(new JLabel("Máx Barra A (cm):"));
        txtMaxBarraA = new JTextField("55", 5);
        txtMaxBarraA.setEnabled(false);
        panelSuperior.add(txtMaxBarraA);

        panelSuperior.add(new JLabel("Valor Esperado Erlang:"));
        txtValorEsperadoErlang = new JTextField("30", 5);
        txtValorEsperadoErlang.setEnabled(false);
        panelSuperior.add(txtValorEsperadoErlang);

        panelSuperior.add(new JLabel("Forma Erlang k:"));
        txtFormaErlang = new JTextField("4", 5);
        txtFormaErlang.setEnabled(false);
        panelSuperior.add(txtFormaErlang);

        panelSuperior.add(new JLabel("Espec Inf:"));
        txtEspecInf = new JTextField("70", 5);
        txtEspecInf.setEnabled(false);
        panelSuperior.add(txtEspecInf);

        panelSuperior.add(new JLabel("Espec Sup:"));
        txtEspecSup = new JTextField("90", 5);
        txtEspecSup.setEnabled(false);
        panelSuperior.add(txtEspecSup);

        // Botones
        btnCrearFilas = new JButton("Crear Filas");
        btnSimular = new JButton("Simular");
        btnSimular.setEnabled(false);

        panelSuperior.add(btnCrearFilas);
        panelSuperior.add(btnSimular);

        // Configurar tabla
        String[] columnas = {
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1",
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)",
            "Longitud total (cm)", "Especificación inferior (cm)",
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No",
            "Piezas defectuosas acumuladas", "% piezas defectuosas"
        };

        model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar las columnas Rn (1), Rn1 (3), Rn2 (4), Rn3 (5), Rn4 (6)
                return column == 1 || column == 3 || column == 4 || column == 5 || column == 6;
            }
        };

        tabla = new JTable(model);

        // Ajustar ancho de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);  // Ensambles
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);  // Rn
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Dimensión Barra A
        tabla.getColumnModel().getColumn(3).setPreferredWidth(80);  // Rn1
        tabla.getColumnModel().getColumn(4).setPreferredWidth(80);  // Rn2
        tabla.getColumnModel().getColumn(5).setPreferredWidth(80);  // Rn3
        tabla.getColumnModel().getColumn(6).setPreferredWidth(80);  // Rn4
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120); // Dimensión Barra B
        tabla.getColumnModel().getColumn(8).setPreferredWidth(120); // Longitud total

        JScrollPane scrollPane = new JScrollPane(tabla);

        // Layout
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas);
        btnSimular.addActionListener(this::simular);

        // Listener para el campo de número de ensambles
        txtNumEnsambles.addActionListener(e -> habilitarCrearFilas());
        txtNumEnsambles.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); }
        });
    }

    private void habilitarCrearFilas() {
        btnCrearFilas.setEnabled(true);
        btnSimular.setEnabled(false);
    }

    private void crearFilas(ActionEvent e) {
        try {
            int numEnsambles = Integer.parseInt(txtNumEnsambles.getText());
            if (numEnsambles <= 0) {
                JOptionPane.showMessageDialog(this, "El número de ensambles debe ser mayor a 0");
                return;
            }

            // Limpiar tabla
            model.setRowCount(0);

            // Crear filas con valores por defecto
            for (int i = 0; i < numEnsambles; i++) {
                Object[] fila = new Object[14]; // 14 columnas en total
                fila[0] = i + 1;    // Ensambles
                fila[1] = "";       // Rn - editable
                fila[2] = "";       // Dimensión Barra A - calculado
                fila[3] = "";       // Rn1 - editable
                fila[4] = "";       // Rn2 - editable
                fila[5] = "";       // Rn3 - editable
                fila[6] = "";       // Rn4 - editable
                fila[7] = "";       // Dimensión Barra B - calculado
                fila[8] = "";       // Longitud total - calculado
                fila[9] = "";       // Especificación inferior - calculado
                fila[10] = "";      // Especificación superior - calculado
                fila[11] = "";      // ¿Defectuosa? - calculado
                fila[12] = "";      // Piezas defectuosas acumuladas - calculado
                fila[13] = "";      // % piezas defectuosas - calculado

                model.addRow(fila);
            }

            // Habilitar el botón simular y deshabilitar crear filas
            btnSimular.setEnabled(true);
            btnCrearFilas.setEnabled(false);

            JOptionPane.showMessageDialog(this, "Tabla generada. Ahora ingrese los valores Rn manualmente en las columnas editables (Rn, Rn1, Rn2, Rn3, Rn4).");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido de ensambles", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simular(ActionEvent e) {
        try {
            // Validar que todos los campos estén llenos
            if (!validarDatos()) {
                return;
            }

            double minA = Double.parseDouble(txtMinBarraA.getText());
            double maxA = Double.parseDouble(txtMaxBarraA.getText());
            double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText());
            int kErlang = Integer.parseInt(txtFormaErlang.getText());
            double especInf = Double.parseDouble(txtEspecInf.getText());
            double especSup = Double.parseDouble(txtEspecSup.getText());

            int acumDefectuosas = 0;
            int numFilas = model.getRowCount();

            for (int i = 0; i < numFilas; i++) {
                // Obtener valores ingresados manualmente
                double rnA = Double.parseDouble(model.getValueAt(i, 1).toString());
                double rn1 = Double.parseDouble(model.getValueAt(i, 3).toString());
                double rn2 = Double.parseDouble(model.getValueAt(i, 4).toString());
                double rn3 = Double.parseDouble(model.getValueAt(i, 5).toString());
                double rn4 = Double.parseDouble(model.getValueAt(i, 6).toString());

                // Validar que los valores estén entre 0 y 1
                if (!validarRango(rnA, rn1, rn2, rn3, rn4, i + 1)) {
                    return;
                }

                // Calcular dimensiones
                double dimBarraA = minA + (maxA - minA) * rnA;

                // Calcular Erlang
                double lnProduct = Math.log(1 - rn1) + Math.log(1 - rn2) + Math.log(1 - rn3) + Math.log(1 - rn4);
                double dimBarraB = -(valorEsperado / kErlang) * lnProduct;

                double longitudTotal = dimBarraA + dimBarraB;

                boolean defectuosa = (longitudTotal < especInf) || (longitudTotal > especSup);
                int defectInt = defectuosa ? 1 : 0;
                acumDefectuosas += defectInt;
                double porcentaje = (double) acumDefectuosas / (i + 1);

                // Actualizar fila con resultados calculados
                model.setValueAt(String.format("%.2f", dimBarraA), i, 2);
                model.setValueAt(String.format("%.2f", dimBarraB), i, 7);
                model.setValueAt(String.format("%.2f", longitudTotal), i, 8);
                model.setValueAt(String.format("%.2f", especInf), i, 9);
                model.setValueAt(String.format("%.2f", especSup), i, 10);
                model.setValueAt(defectInt, i, 11);
                model.setValueAt(acumDefectuosas, i, 12);
                model.setValueAt(String.format("%.2f%%", porcentaje * 100), i, 13);
            }

            JOptionPane.showMessageDialog(this, "Simulación completada exitosamente.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante la simulación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean validarDatos() {
        int numFilas = model.getRowCount();
        if (numFilas == 0) {
            JOptionPane.showMessageDialog(this, "Primero genere la tabla con 'Crear Filas'");
            return false;
        }

        // Validar columnas editables: Rn (1), Rn1 (3), Rn2 (4), Rn3 (5), Rn4 (6)
        int[] columnasEditables = {1, 3, 4, 5, 6};
        String[] nombresColumnas = {"Rn", "Rn1", "Rn2", "Rn3", "Rn4"};

        for (int i = 0; i < numFilas; i++) {
            for (int j = 0; j < columnasEditables.length; j++) {
                int colIndex = columnasEditables[j];
                Object valor = model.getValueAt(i, colIndex);
                if (valor == null || valor.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Falta ingresar valor en fila " + (i + 1) + ", columna " + nombresColumnas[j]);
                    return false;
                }
            }
        }
        return true;
    }

    private boolean validarRango(double... valores) {
        for (double valor : valores) {
            if (valor < 0 || valor > 1) {
                return false;
            }
        }
        return true;
    }

    private boolean validarRango(double rnA, double rn1, double rn2, double rn3, double rn4, int fila) {
        if (!validarRango(rnA, rn1, rn2, rn3, rn4)) {
            JOptionPane.showMessageDialog(this,
                "Los valores Rn deben estar entre 0 y 1 (fila " + fila + ")");
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio3Manual().setVisible(true));
    }
}
