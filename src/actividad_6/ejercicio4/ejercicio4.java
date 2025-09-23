package actividad_6.ejercicio4;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class ejercicio4 extends JFrame {

    private final JTextField txtDias;
    private final JTextField txtCapacidadBodega;
    private final JTextField txtCostoOrdenar;
    private final JTextField txtCostoFaltante;
    private final JTextField txtCostoMantenimiento;
    private final JTextField txtMediaDemanda;
    private final DefaultTableModel model;

    public ejercicio4() {
        setTitle("Simulación Inventario Azúcar");
        setSize(1200, 500);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelInput = new JPanel(new FlowLayout());

        panelInput.add(new JLabel("Número de Días:"));
        txtDias = new JTextField("14", 5);
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

        panelInput.add(new JLabel("Media Demanda (Kg/día):"));
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

        Random random = new Random();
        double inventarioInicial = 0;

        for (int dia = 1; dia <= dias; dia++) {
            double entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0;
            double inventarioTotal = inventarioInicial + entregaProveedor;

            double rnDemanda = random.nextDouble();
            // Demanda exponencial
            double demanda = -mediaDemanda * Math.log(1 - rnDemanda);

            double venta = Math.min(demanda, inventarioTotal);
            double inventarioFinal = inventarioTotal - venta;
            double ventasPerdidas = Math.max(0, demanda - inventarioTotal);

            double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0;
            double costoFalt = ventasPerdidas * costoFaltante;
            double costoMant = inventarioFinal * costoMantenimiento;
            double costoTotal = costoOrden + costoFalt + costoMant;

            model.addRow(new Object[]{
                    dia,
                    String.format("%.2f", inventarioInicial),
                    String.format("%.2f", entregaProveedor),
                    String.format("%.2f", inventarioTotal),
                    String.format("%.4f", rnDemanda),
                    String.format("%.2f", demanda),
                    String.format("%.2f", venta),
                    String.format("%.2f", inventarioFinal),
                    String.format("%.2f", ventasPerdidas),
                    String.format("$%.2f", costoOrden),
                    String.format("$%.2f", costoFalt),
                    String.format("$%.2f", costoMant),
                    String.format("$%.2f", costoTotal)
            });

            inventarioInicial = inventarioFinal;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ejercicio4().setVisible(true);
        });
    }
}
