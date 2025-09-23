package actividad_6.ejercicio3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio3Manual extends JFrame {

    private JTextField txtNumMuestras;
    private JTextField txtMinBarraA;
    private JTextField txtMaxBarraA;
    private JTextField txtValorEsperadoErlang;
    private JTextField txtFormaErlang;
    private JTextField txtEspecInf;
    private JTextField txtEspecSup;
    private DefaultTableModel modelInput;
    private DefaultTableModel modelResults;
    private JTable tablaInput;
    private JTable tablaResults;
    private JButton btnGenerarTabla;
    private JButton btnSimular;

    public ejercicio3Manual() {
        setTitle("Simulación Barras Defectuosas - Ingreso Manual");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Panel superior para parámetros
        JPanel panelParametros = createParametrosPanel();
        add(panelParametros, BorderLayout.NORTH);

        // Panel central con pestañas
        JTabbedPane tabbedPane = new JTabbedPane();

        // Pestaña para ingreso de datos
        JPanel panelInput = createInputPanel();
        tabbedPane.addTab("Ingreso de Datos", panelInput);

        // Pestaña para resultados
        JPanel panelResults = createResultsPanel();
        tabbedPane.addTab("Resultados", panelResults);

        add(tabbedPane, BorderLayout.CENTER);
    }

    private JPanel createParametrosPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        panel.add(new JLabel("Número de Ensambles:"));
        txtNumMuestras = new JTextField("15", 5);
        panel.add(txtNumMuestras);

        panel.add(new JLabel("Mín Barra A (cm):"));
        txtMinBarraA = new JTextField("45", 5);
        txtMinBarraA.setEditable(false);
        panel.add(txtMinBarraA);

        panel.add(new JLabel("Máx Barra A (cm):"));
        txtMaxBarraA = new JTextField("55", 5);
        txtMaxBarraA.setEditable(false);
        panel.add(txtMaxBarraA);

        panel.add(new JLabel("Valor Esperado Erlang:"));
        txtValorEsperadoErlang = new JTextField("30", 5);
        txtValorEsperadoErlang.setEditable(false);
        panel.add(txtValorEsperadoErlang);

        panel.add(new JLabel("Forma Erlang k:"));
        txtFormaErlang = new JTextField("4", 5);
        txtFormaErlang.setEditable(false);
        panel.add(txtFormaErlang);

        panel.add(new JLabel("Espec Inf:"));
        txtEspecInf = new JTextField("70", 5);
        txtEspecInf.setEditable(false);
        panel.add(txtEspecInf);

        panel.add(new JLabel("Espec Sup:"));
        txtEspecSup = new JTextField("90", 5);
        txtEspecSup.setEditable(false);
        panel.add(txtEspecSup);

        btnGenerarTabla = new JButton("Generar Tabla");
        btnGenerarTabla.addActionListener(this::generarTabla);
        panel.add(btnGenerarTabla);

        return panel;
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnasInput = {"Ensamble", "Rn", "Rn1", "Rn2", "Rn3", "Rn4"};
        modelInput = new DefaultTableModel(columnasInput, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Solo la primera columna (Ensamble) no es editable
            }
        };

        tablaInput = new JTable(modelInput);
        tablaInput.getColumnModel().getColumn(0).setPreferredWidth(80);
        for (int i = 1; i < tablaInput.getColumnCount(); i++) {
            tablaInput.getColumnModel().getColumn(i).setPreferredWidth(100);
        }

        JScrollPane scrollInput = new JScrollPane(tablaInput);
        panel.add(scrollInput, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout());
        btnSimular = new JButton("Simular");
        btnSimular.addActionListener(this::simular);
        btnSimular.setEnabled(false);
        panelBotones.add(btnSimular);

        panel.add(panelBotones, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createResultsPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        String[] columnasResults = {
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1",
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)",
            "Longitud total (cm)", "Especificación inferior (cm)",
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No",
            "Piezas defectuosas acumuladas", "% piezas defectuosas"
        };

        modelResults = new DefaultTableModel(columnasResults, 0);
        tablaResults = new JTable(modelResults);

        // Ajustar ancho de columnas
        tablaResults.getColumnModel().getColumn(0).setPreferredWidth(80);
        for (int i = 1; i < tablaResults.getColumnCount(); i++) {
            tablaResults.getColumnModel().getColumn(i).setPreferredWidth(120);
        }

        JScrollPane scrollResults = new JScrollPane(tablaResults);
        panel.add(scrollResults, BorderLayout.CENTER);

        return panel;
    }

    private void generarTabla(ActionEvent e) {
        try {
            int n = Integer.parseInt(txtNumMuestras.getText());
            if (n <= 0) {
                JOptionPane.showMessageDialog(this, "El número de ensambles debe ser mayor a 0");
                return;
            }

            modelInput.setRowCount(0);

            for (int i = 1; i <= n; i++) {
                modelInput.addRow(new Object[]{i, "", "", "", "", ""});
            }

            btnSimular.setEnabled(true);
            JOptionPane.showMessageDialog(this, "Tabla generada. Ahora ingrese los valores Rn manualmente.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido para los ensambles");
        }
    }

    private void simular(ActionEvent e) {
        try {
            // Validar que todos los campos estén llenos
            if (!validarDatos()) {
                return;
            }

            modelResults.setRowCount(0);

            double minA = Double.parseDouble(txtMinBarraA.getText());
            double maxA = Double.parseDouble(txtMaxBarraA.getText());
            double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText());
            int kErlang = Integer.parseInt(txtFormaErlang.getText());
            double especInf = Double.parseDouble(txtEspecInf.getText());
            double especSup = Double.parseDouble(txtEspecSup.getText());

            int acumDefectuosas = 0;
            int numFilas = modelInput.getRowCount();

            for (int i = 0; i < numFilas; i++) {
                // Obtener valores ingresados manualmente
                double rnA = Double.parseDouble(modelInput.getValueAt(i, 1).toString());
                double rn1 = Double.parseDouble(modelInput.getValueAt(i, 2).toString());
                double rn2 = Double.parseDouble(modelInput.getValueAt(i, 3).toString());
                double rn3 = Double.parseDouble(modelInput.getValueAt(i, 4).toString());
                double rn4 = Double.parseDouble(modelInput.getValueAt(i, 5).toString());

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

                modelResults.addRow(new Object[]{
                    i + 1,
                    String.format("%.4f", rnA),
                    String.format("%.2f", dimBarraA),
                    String.format("%.4f", rn1),
                    String.format("%.4f", rn2),
                    String.format("%.4f", rn3),
                    String.format("%.4f", rn4),
                    String.format("%.2f", dimBarraB),
                    String.format("%.2f", longitudTotal),
                    String.format("%.2f", especInf),
                    String.format("%.2f", especSup),
                    defectInt,
                    acumDefectuosas,
                    String.format("%.2f%%", porcentaje * 100)
                });
            }

            JOptionPane.showMessageDialog(this, "Simulación completada. Ver pestaña 'Resultados'.");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados: " + ex.getMessage());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante la simulación: " + ex.getMessage());
        }
    }

    private boolean validarDatos() {
        int numFilas = modelInput.getRowCount();
        if (numFilas == 0) {
            JOptionPane.showMessageDialog(this, "Primero genere la tabla con 'Generar Tabla'");
            return false;
        }

        for (int i = 0; i < numFilas; i++) {
            for (int j = 1; j < 6; j++) { // Columnas Rn, Rn1, Rn2, Rn3, Rn4
                Object valor = modelInput.getValueAt(i, j);
                if (valor == null || valor.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Falta ingresar valor en fila " + (i + 1) + ", columna " +
                        modelInput.getColumnName(j));
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
