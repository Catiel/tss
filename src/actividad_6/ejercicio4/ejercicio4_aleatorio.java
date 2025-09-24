package actividad_6.ejercicio4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class ejercicio4_aleatorio extends JFrame {

    private final JTextField txtDias;
    private final JTextField txtCapacidadBodega;
    private final JTextField txtCostoOrdenar;
    private final JTextField txtCostoFaltante;
    private final JTextField txtCostoMantenimiento;
    private final JTextField txtMediaDemanda;
    private final DefaultTableModel model;

    public ejercicio4_aleatorio() {
        setTitle("Simulacion Inventario Azucar - Version Aleatoria");
        setSize(1200, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelInput = new JPanel(new FlowLayout());

        panelInput.add(new JLabel("Número de Días:"));
        txtDias = new JTextField("14", 5);
        // Ahora es editable para poder cambiar la cantidad de días
        panelInput.add(txtDias);

        panelInput.add(new JLabel("Capacidad Bodega (Kg):"));
        txtCapacidadBodega = new JTextField("700", 5);
        panelInput.add(txtCapacidadBodega);

        panelInput.add(new JLabel("Costo de ordenar ($):"));
        txtCostoOrdenar = new JTextField("1000", 5);
        panelInput.add(txtCostoOrdenar);

        panelInput.add(new JLabel("Costo de faltante ($ por Kg):"));
        txtCostoFaltante = new JTextField("6", 5);
        panelInput.add(txtCostoFaltante);

        panelInput.add(new JLabel("Costo de mantenimiento ($ por Kg):"));
        txtCostoMantenimiento = new JTextField("1", 5);
        panelInput.add(txtCostoMantenimiento);

        panelInput.add(new JLabel("Media Demanda (Kg/dia):"));
        txtMediaDemanda = new JTextField("100", 5);
        panelInput.add(txtMediaDemanda);

        JButton btnSimular = new JButton("Simular");
        panelInput.add(btnSimular);

        String[] columnas = {
                "Día", "Inventario Inicial (Kg)", "Entrega del Proveedor (Kg)", "Inventario Total (Kg)",
                "Rn Demanda", "Demanda (Kg)", "Venta (Kg)", "Inventario Final (Kg)",
                "Ventas Perdidas (Kg)", "Costo de ordenar ($)", "Costo de faltante ($)",
                "Costo de mantenimiento ($)", "Costo total ($)"
        };
        model = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        add(panelInput, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSimular.addActionListener(this::simular);
    }

    private void simular(ActionEvent e) {
        model.setRowCount(0);

        int dias = Integer.parseInt(txtDias.getText());
        double capacidadBodega = Double.parseDouble(txtCapacidadBodega.getText());
        double costoOrdenar = Double.parseDouble(txtCostoOrdenar.getText());
        double costoFaltante = Double.parseDouble(txtCostoFaltante.getText());
        double costoMantenimiento = Double.parseDouble(txtCostoMantenimiento.getText());
        double mediaDemanda = Double.parseDouble(txtMediaDemanda.getText());

        // Generador de números aleatorios para Rn
        Random random = new Random();

        double inventarioInicial = 0;
        double inventarioFinalAnterior = 0; // Para el cálculo de entregas del proveedor

        for (int dia = 1; dia <= dias; dia++) {
            // Fórmula de entregas del proveedor según las especificaciones de Excel
            double entregaProveedor;
            if (dia == 1) {
                // Primer día: =SI(RESIDUO(A7;7)=1;700;0)
                entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0;
            } else {
                // Días siguientes: =SI(RESIDUO(A8;7)=0;700-H7;0) donde H7 es inventario final anterior
                entregaProveedor = (dia % 7 == 0) ? Math.max(0, capacidadBodega - inventarioFinalAnterior) : 0;
            }

            double inventarioTotal = inventarioInicial + entregaProveedor;

            // Generar valor aleatorio de Rn entre 0 y 1
            double rnDemanda = random.nextDouble();

            // Demanda usando la fórmula de Excel: =-100*LN(1-E7)
            double demanda = -mediaDemanda * Math.log(1 - rnDemanda);

            // Venta usando la fórmula de Excel: =MIN(F7;D7) - mínimo entre demanda e inventario total
            double venta = Math.min(demanda, inventarioTotal);

            // Inventario final: inventario total - venta
            double inventarioFinal = inventarioTotal - venta;

            // Ventas perdidas usando la fórmula de Excel: =MAX(0;F7-D7) - demanda menos inventario total
            double ventasPerdidas = Math.max(0, demanda - inventarioTotal);

            // Costo de ordenar usando la fórmula de Excel: =SI(C7>0;1000;0) - si hay entrega del proveedor
            double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0;
            double costoFalt = ventasPerdidas * costoFaltante;
            double costoMant = inventarioFinal * costoMantenimiento;
            double costoTotal = costoOrden + costoFalt + costoMant;

            model.addRow(new Object[]{
                    dia,
                    String.format("%.0f", inventarioInicial),
                    String.format("%.0f", entregaProveedor),
                    String.format("%.0f", inventarioTotal),
                    String.format("%.4f", rnDemanda),
                    String.format("%.0f", demanda),
                    String.format("%.0f", venta),
                    String.format("%.0f", inventarioFinal),
                    String.format("%.0f", ventasPerdidas),
                    String.format("$%.0f", costoOrden),
                    String.format("$%.0f", costoFalt),
                    String.format("$%.0f", costoMant),
                    String.format("$%.0f", costoTotal)
            });

            // Actualizar valores para la siguiente iteración
            inventarioInicial = inventarioFinal;
            inventarioFinalAnterior = inventarioFinal;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio4_aleatorio().setVisible(true));
    }
}
