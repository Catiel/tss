package actividad_6.ejercicio4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio4_manual extends JFrame {

    private JTextField txtDias;
    private JTextField txtCapacidadBodega;
    private JTextField txtCostoOrdenar;
    private JTextField txtCostoFaltante;
    private JTextField txtCostoMantenimiento;
    private JTextField txtMediaDemanda;
    private DefaultTableModel model;
    private JTable tabla;
    private JButton btnCrearFilas;
    private JButton btnSimular;

    public ejercicio4_manual() {
        setTitle("Simulacion Inventario Azucar - Ingreso Manual");
        setSize(1400, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout());

        panelSuperior.add(new JLabel("Número de Dias:"));
        txtDias = new JTextField("14", 5);
        panelSuperior.add(txtDias);

        panelSuperior.add(new JLabel("Capacidad Bodega (Kg):"));
        txtCapacidadBodega = new JTextField("700", 5);
        panelSuperior.add(txtCapacidadBodega);

        panelSuperior.add(new JLabel("Costo de ordenar ($):"));
        txtCostoOrdenar = new JTextField("1000", 5);
        panelSuperior.add(txtCostoOrdenar);

        panelSuperior.add(new JLabel("Costo de faltante ($ por Kg):"));
        txtCostoFaltante = new JTextField("6", 5);
        panelSuperior.add(txtCostoFaltante);

        panelSuperior.add(new JLabel("Costo de mantenimiento ($ por Kg):"));
        txtCostoMantenimiento = new JTextField("1", 5);
        panelSuperior.add(txtCostoMantenimiento);

        panelSuperior.add(new JLabel("Media Demanda (Kg/dia):"));
        txtMediaDemanda = new JTextField("100", 5);
        panelSuperior.add(txtMediaDemanda);

        // Botones
        btnCrearFilas = new JButton("Crear Filas");
        btnSimular = new JButton("Simular");
        btnSimular.setEnabled(false); // Deshabilitar hasta crear filas

        panelSuperior.add(btnCrearFilas);
        panelSuperior.add(btnSimular);

        // Configurar tabla
        String[] columnas = {
                "Dia", "Inventario Inicial (Kg)", "Entrega del Proveedor (Kg)", "Inventario Total (Kg)",
                "Rn Demanda", "Demanda (Kg)", "Venta (Kg)", "Inventario Final (Kg)",
                "Ventas Perdidas (Kg)", "Costo de ordenar ($)", "Costo de faltante ($)",
                "Costo de mantenimiento ($)", "Costo total ($)"
        };

        model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar la columna Rn Demanda (columna 4)
                return column == 4;
            }
        };

        tabla = new JTable(model);

        // Ajustar ancho de columnas para mejor visualización
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // Día
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);  // Inventario Inicial
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);  // Entrega Proveedor
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);  // Inventario Total
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Rn Demanda - EDITABLE
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Demanda
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // Venta
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120);  // Inventario Final
        tabla.getColumnModel().getColumn(8).setPreferredWidth(120);  // Ventas Perdidas
        tabla.getColumnModel().getColumn(9).setPreferredWidth(120);  // Costo ordenar
        tabla.getColumnModel().getColumn(10).setPreferredWidth(120); // Costo faltante
        tabla.getColumnModel().getColumn(11).setPreferredWidth(120); // Costo mantenimiento
        tabla.getColumnModel().getColumn(12).setPreferredWidth(100); // Costo total

        JScrollPane scrollPane = new JScrollPane(tabla);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        // Layout principal
        add(panelSuperior, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas);
        btnSimular.addActionListener(this::simular);
    }

    private void crearFilas(ActionEvent e) {
        try {
            int dias = Integer.parseInt(txtDias.getText());

            if (dias <= 0) {
                JOptionPane.showMessageDialog(this, "El numero de dias debe ser mayor a 0",
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Limpiar tabla
            model.setRowCount(0);

            // Crear filas vacías
            for (int dia = 1; dia <= dias; dia++) {
                model.addRow(new Object[]{
                    dia,           // Día
                    "",           // Inventario Inicial (se calculará)
                    "",           // Entrega del Proveedor (se calculará)
                    "",           // Inventario Total (se calculará)
                    "",           // Rn Demanda - CAMPO EDITABLE
                    "",           // Demanda (se calculará)
                    "",           // Venta (se calculará)
                    "",           // Inventario Final (se calculará)
                    "",           // Ventas Perdidas (se calculará)
                    "",           // Costo de ordenar (se calculará)
                    "",           // Costo de faltante (se calculará)
                    "",           // Costo de mantenimiento (se calculará)
                    ""            // Costo total (se calculará)
                });
            }

            btnSimular.setEnabled(true);
            JOptionPane.showMessageDialog(this,
                "Se han creado " + dias + " filas.\n" +
                "Ahora ingrese manualmente los valores Rn en la columna 'Rn Demanda'.\n" +
                "Los valores deben estar entre 0 y 1 (ejemplo: 0.9350, 0.1307, etc.)",
                "Filas creadas", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido para los dias",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void simular(ActionEvent e) {
        try {
            // Validar que todos los valores Rn estén ingresados
            int filas = model.getRowCount();
            for (int i = 0; i < filas; i++) {
                Object rnValue = model.getValueAt(i, 4); // Columna Rn Demanda
                if (rnValue == null || rnValue.toString().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this,
                        "Falta ingresar el valor Rn para el dia " + (i + 1),
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                try {
                    double rn = Double.parseDouble(rnValue.toString().trim());
                    if (rn < 0 || rn >= 1) {
                        JOptionPane.showMessageDialog(this,
                            "El valor Rn del dia " + (i + 1) + " debe estar entre 0 y 1 (exclusive)",
                            "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "El valor Rn del dia " + (i + 1) + " no es un número válido",
                        "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Obtener parámetros
            double capacidadBodega = Double.parseDouble(txtCapacidadBodega.getText());
            double costoOrdenar = Double.parseDouble(txtCostoOrdenar.getText());
            double costoFaltante = Double.parseDouble(txtCostoFaltante.getText());
            double costoMantenimiento = Double.parseDouble(txtCostoMantenimiento.getText());
            double mediaDemanda = Double.parseDouble(txtMediaDemanda.getText());

            // Realizar simulación
            double inventarioInicial = 0;
            double inventarioFinalAnterior = 0;

            for (int i = 0; i < filas; i++) {
                int dia = i + 1;

                // Fórmula de entregas del proveedor
                double entregaProveedor;
                if (dia == 1) {
                    entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0;
                } else {
                    entregaProveedor = (dia % 7 == 0) ? Math.max(0, capacidadBodega - inventarioFinalAnterior) : 0;
                }

                double inventarioTotal = inventarioInicial + entregaProveedor;

                // Obtener el valor Rn ingresado manualmente
                double rnDemanda = Double.parseDouble(model.getValueAt(i, 4).toString());

                // Calcular demanda usando la fórmula: =-100*LN(1-E7)
                double demanda = -mediaDemanda * Math.log(1 - rnDemanda);

                // Venta: MIN(demanda, inventarioTotal)
                double venta = Math.min(demanda, inventarioTotal);

                // Inventario final
                double inventarioFinal = inventarioTotal - venta;

                // Ventas perdidas: MAX(0, demanda - inventarioTotal)
                double ventasPerdidas = Math.max(0, demanda - inventarioTotal);

                // Costos
                double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0;
                double costoFalt = ventasPerdidas * costoFaltante;
                double costoMant = inventarioFinal * costoMantenimiento;
                double costoTotal = costoOrden + costoFalt + costoMant;

                // Actualizar fila con resultados (formato sin decimales excepto Rn)
                model.setValueAt(String.format("%.0f", inventarioInicial), i, 1);
                model.setValueAt(String.format("%.0f", entregaProveedor), i, 2);
                model.setValueAt(String.format("%.0f", inventarioTotal), i, 3);
                // La columna 4 (Rn) ya tiene el valor ingresado manualmente
                model.setValueAt(String.format("%.0f", demanda), i, 5);
                model.setValueAt(String.format("%.0f", venta), i, 6);
                model.setValueAt(String.format("%.0f", inventarioFinal), i, 7);
                model.setValueAt(String.format("%.0f", ventasPerdidas), i, 8);
                model.setValueAt(String.format("$%.0f", costoOrden), i, 9);
                model.setValueAt(String.format("$%.0f", costoFalt), i, 10);
                model.setValueAt(String.format("$%.0f", costoMant), i, 11);
                model.setValueAt(String.format("$%.0f", costoTotal), i, 12);

                // Actualizar para siguiente iteración
                inventarioInicial = inventarioFinal;
                inventarioFinalAnterior = inventarioFinal;
            }

            JOptionPane.showMessageDialog(this, "Simulacion completada exitosamente",
                "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Por favor verifique que todos los parámetros sean números válidos",
                "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error durante la simulacion: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio4_manual().setVisible(true));
    }
}
