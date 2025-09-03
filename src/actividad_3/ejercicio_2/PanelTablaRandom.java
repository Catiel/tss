package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;

public class PanelTablaRandom extends TablaEstilizadaPanel {
    private final JTextField txtMin;
    private final JTextField txtMax;
    private final JTextField txtFilas;

    public PanelTablaRandom() {
        super(
            "Tabla aleatoria de beneficio total versus unidades de capacidad",
            new DefaultTableModel(new String[]{"Capacidad", "Ganancia ($)", "VAN ($)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            },
            null,
            null
        );
        PanelSuperiorRandom panelSup = crearPanelSuperior();
        add(panelSup.panel, BorderLayout.BEFORE_FIRST_LINE);
        txtMin = panelSup.txtMin;
        txtMax = panelSup.txtMax;
        txtFilas = panelSup.txtFilas;
        JButton btnGenerar = panelSup.btnGenerar;
        btnGenerar.addActionListener(e -> generarTabla());
    }

    private static class PanelSuperiorRandom {
        JPanel panel;
        JTextField txtMin, txtMax, txtFilas;
        JButton btnGenerar;
        PanelSuperiorRandom(JPanel panel, JTextField txtMin, JTextField txtMax, JTextField txtFilas, JButton btnGenerar) {
            this.panel = panel;
            this.txtMin = txtMin;
            this.txtMax = txtMax;
            this.txtFilas = txtFilas;
            this.btnGenerar = btnGenerar;
        }
    }

    private static PanelSuperiorRandom crearPanelSuperior() {
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        EstilosUI.aplicarEstiloPanel(panelTop);
        JLabel lblTitulo = new JLabel("Tabla aleatoria de beneficio total versus unidades de capacidad");
        EstilosUI.aplicarEstiloTitulo(lblTitulo);
        panelTop.add(lblTitulo);
        JLabel lblMin = new JLabel("Capacidad mínima:");
        EstilosUI.aplicarEstiloLabel(lblMin);
        JTextField txtMin = new JTextField("40000", 6);
        txtMin.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblMin);
        panelTop.add(txtMin);
        JLabel lblMax = new JLabel("Capacidad máxima:");
        EstilosUI.aplicarEstiloLabel(lblMax);
        JTextField txtMax = new JTextField("80000", 6);
        txtMax.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblMax);
        panelTop.add(txtMax);
        JLabel lblFilas = new JLabel("Cantidad de filas:");
        EstilosUI.aplicarEstiloLabel(lblFilas);
        JTextField txtFilas = new JTextField("8", 4);
        txtFilas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblFilas);
        panelTop.add(txtFilas);
        JButton btnGenerar = new JButton("Generar tabla");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        panelTop.add(btnGenerar);
        return new PanelSuperiorRandom(panelTop, txtMin, txtMax, txtFilas, btnGenerar);
    }

    private void generarTabla() {
        try {
            int min = Integer.parseInt(txtMin.getText());
            int max = Integer.parseInt(txtMax.getText());
            int filas = Integer.parseInt(txtFilas.getText());
            if (min > max || filas < 1) throw new Exception();
            modeloTabla.setRowCount(0);
            filaOptima = -1;
            ControladorParametros params = ControladorParametros.getInstancia();
            double mejorGanancia = Double.NEGATIVE_INFINITY;
            int mejorCapacidad = 0;
            double tasaDescuento = params.getTasaDescuento();
            Random rand = new Random();
            for (int i = 0; i < filas; i++) {
                int capacidad = min + rand.nextInt(max - min + 1);
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
                modeloTabla.addRow(new Object[]{capacidad, String.format("$%,.0f", ganancia), String.format("$%,.0f", van)});
                if (ganancia > mejorGanancia) {
                    mejorGanancia = ganancia;
                    mejorCapacidad = capacidad;
                    filaOptima = i;
                }
            }
            actualizarOptimo(mejorCapacidad, mejorGanancia, filaOptima);
        } catch (Exception ex) {
            modeloTabla.setRowCount(0);
            lblOptimo.setText("Datos inválidos");
        }
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
