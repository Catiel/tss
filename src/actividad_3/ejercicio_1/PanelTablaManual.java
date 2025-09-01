package actividad_3.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelTablaManual extends TablaEstilizadaPanel {
    private boolean actualizandoTabla = false;

    public PanelTablaManual() {
        super(
            "Tabla manual de precios y ganancias",
            new DefaultTableModel(new String[]{"Precio (libras)", "Ganancia ($)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return col == 0; }
            },
            crearPanelSuperior(),
            null
        );
        // Acción para crear tabla con la cantidad de filas indicada
        JPanel panelSuperior = (JPanel) getComponent(1);
        JButton btnCrearTabla = (JButton) panelSuperior.getComponent(2);
        JSpinner spinnerFilas = (JSpinner) panelSuperior.getComponent(1);
        btnCrearTabla.addActionListener(e -> {
            int cantidad = (Integer) spinnerFilas.getValue();
            modeloTabla.setRowCount(0);
            for (int i = 0; i < cantidad; i++) {
                modeloTabla.addRow(new Object[]{"", "-"});
            }
            filaOptima = -1;
            lblOptimo.setText("Mejor precio: - | Ganancia máxima: -");
            tabla.repaint();
        });
        // Botón calcular ganancias
        JButton btnCalcular = new JButton("Calcular ganancias");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        JPanel panelInferior = (JPanel) getComponent(getComponentCount() - 1);
        panelInferior.add(btnCalcular, BorderLayout.WEST);
        btnCalcular.addActionListener(e -> calcularGanancias());
    }

    private static JPanel crearPanelSuperior() {
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(panelSuperior);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 220, 240)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        JLabel lblFilas = new JLabel("Cantidad de filas:");
        EstilosUI.aplicarEstiloLabel(lblFilas);
        JSpinner spinnerFilas = new JSpinner(new SpinnerNumberModel(5, 1, 100, 1));
        spinnerFilas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        spinnerFilas.setPreferredSize(new Dimension(60, 28));
        JButton btnCrearTabla = new JButton("Crear tabla");
        EstilosUI.aplicarEstiloBoton(btnCrearTabla);
        panelSuperior.add(lblFilas);
        panelSuperior.add(spinnerFilas);
        panelSuperior.add(btnCrearTabla);
        return panelSuperior;
    }

    private void calcularGanancias() {
        actualizandoTabla = true;
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        double mejorPrecio = 0;
        filaOptima = -1;
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            String val = modeloTabla.getValueAt(i, 0).toString();
            if (val.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debes ingresar todos los precios antes de calcular.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                actualizandoTabla = false;
                return;
            }
            try {
                Double.parseDouble(val);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "El valor en la fila " + (i+1) + " no es un número válido.", "Error de entrada", JOptionPane.ERROR_MESSAGE);
                actualizandoTabla = false;
                return;
            }
        }
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            double precio = Double.parseDouble(modeloTabla.getValueAt(i, 0).toString());
            double demanda = params.getConstanteDemanda() * Math.pow(precio, params.getElasticidad());
            double ingresos = demanda * precio * params.getTipoCambio();
            double costeTotal = demanda * params.getCosteUnitario();
            double ganancia = ingresos - costeTotal;
            modeloTabla.setValueAt(String.format("%.2f", ganancia), i, 1);
            if (ganancia > mejorGanancia) {
                mejorGanancia = ganancia;
                mejorPrecio = precio;
                filaOptima = i;
            }
        }
        actualizarOptimo(mejorPrecio, mejorGanancia, filaOptima);
        actualizandoTabla = false;
    }
}
