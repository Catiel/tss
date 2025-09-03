package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelTablaManual extends TablaEstilizadaPanel {

    public PanelTablaManual() {
        super(
            "Tabla manual de beneficio total versus unidades de capacidad",
            new DefaultTableModel(new String[]{"Capacidad", "Ganancia ($)", "VAN ($)"}, 0) {
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
                modeloTabla.addRow(new Object[]{"", "-", "-"});
            }
            filaOptima = -1;
            lblOptimo.setText("Mejor capacidad: - | Ganancia máxima: -");
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
        JButton btnCrearTabla = new JButton("Crear tabla");
        EstilosUI.aplicarEstiloBoton(btnCrearTabla);
        panelSuperior.add(lblFilas);
        panelSuperior.add(spinnerFilas);
        panelSuperior.add(btnCrearTabla);
        return panelSuperior;
    }

    private void calcularGanancias() {
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        int mejorCapacidad = 0;
        filaOptima = -1;
        double mejorVan = Double.NEGATIVE_INFINITY;
        int mejorCapacidadVan = 0;
        int filaVan = -1;
        double tasaDescuento = params.getTasaDescuento();
        for (int i = 0; i < modeloTabla.getRowCount(); i++) {
            try {
                int capacidad = Integer.parseInt(modeloTabla.getValueAt(i, 0).toString());
                double ganancia = ModeloWozacCalculo.calcularGananciaTotal(
                    capacidad,
                    params.getDemandaInicial(),
                    params.getCrecimientoAnual(),
                    params.getCostoCapacidadUnitaria(),
                    params.getPrecioVentaUnitario(),
                    params.getCostoVariableUnitario(),
                    params.getCostoOperativoUnitario()
                );
                double van = calcularVAN(
                    capacidad,
                    params.getDemandaInicial(),
                    params.getCrecimientoAnual(),
                    params.getCostoCapacidadUnitaria(),
                    params.getPrecioVentaUnitario(),
                    params.getCostoVariableUnitario(),
                    params.getCostoOperativoUnitario(),
                    tasaDescuento
                );
                modeloTabla.setValueAt(String.format("$%,.0f", ganancia), i, 1);
                modeloTabla.setValueAt(String.format("$%,.0f", van), i, 2);
                if (ganancia > mejorGanancia) {
                    mejorGanancia = ganancia;
                    mejorCapacidad = capacidad;
                    filaOptima = i;
                }
                if (van > mejorVan) {
                    mejorVan = van;
                    mejorCapacidadVan = capacidad;
                    filaVan = i;
                }
            } catch (Exception ex) {
                modeloTabla.setValueAt("-", i, 1);
                modeloTabla.setValueAt("-", i, 2);
            }
        }
        actualizarOptimo(mejorCapacidad, mejorGanancia, filaOptima, mejorCapacidadVan, mejorVan, filaVan);
    }

    private double calcularVAN(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario, double tasaDescuento) {
        double van = 0;
        ModeloWozacCalculo.ResultadoAnual[] resultados = ModeloWozacCalculo.calcularModelo(capacidad, demandaInicial, crecimientoAnual, costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        for (int i = 0; i < resultados.length; i++) {
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
        }
        return van;
    }
}
