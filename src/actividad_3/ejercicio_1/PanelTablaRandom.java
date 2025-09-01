package actividad_3.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;

public class PanelTablaRandom extends TablaEstilizadaPanel {
    private JTextField txtMin, txtMax, txtFilas;
    private JButton btnGenerar;

    public PanelTablaRandom() {
        super(
            "Tabla aleatoria de precios y ganancias",
            new DefaultTableModel(new String[]{"Precio (libras)", "Ganancia ($)"}, 0) {
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
        btnGenerar = panelSup.btnGenerar;
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
        JLabel lblTitulo = new JLabel("Tabla aleatoria de precios y ganancias");
        EstilosUI.aplicarEstiloTitulo(lblTitulo);
        panelTop.add(lblTitulo);
        panelTop.add(Box.createHorizontalStrut(30));
        panelTop.add(new JLabel("Precio mínimo:"));
        JTextField txtMin = new JTextField("44", 5);
        txtMin.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(txtMin);
        panelTop.add(new JLabel("Precio máximo:"));
        JTextField txtMax = new JTextField("100", 5);
        txtMax.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(txtMax);
        panelTop.add(new JLabel("Cantidad de filas:"));
        JTextField txtFilas = new JTextField("10", 5);
        txtFilas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(txtFilas);
        JButton btnGenerar = new JButton("Generar aleatorio");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        btnGenerar.setToolTipText("Genera precios aleatorios y calcula la ganancia para cada uno");
        panelTop.add(btnGenerar);
        return new PanelSuperiorRandom(panelTop, txtMin, txtMax, txtFilas, btnGenerar);
    }

    private void generarTabla() {
        modeloTabla.setRowCount(0);
        filaOptima = -1;
        int filas;
        double min, max;
        try {
            min = Double.parseDouble(txtMin.getText());
            max = Double.parseDouble(txtMax.getText());
            filas = Integer.parseInt(txtFilas.getText());
            if (filas < 1 || min >= max) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos. Verifica el rango y la cantidad de filas.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        double mejorPrecio = 0;
        Random rand = new Random();
        for (int i = 0; i < filas; i++) {
            double precio = min + rand.nextDouble() * (max - min);
            double demanda = params.getConstanteDemanda() * Math.pow(precio, params.getElasticidad());
            double ingresos = demanda * precio * params.getTipoCambio();
            double costeTotal = demanda * params.getCosteUnitario();
            double ganancia = ingresos - costeTotal;
            modeloTabla.addRow(new Object[]{String.format("%.2f", precio), String.format("%.2f", ganancia)});
            if (ganancia > mejorGanancia) {
                mejorGanancia = ganancia;
                mejorPrecio = precio;
                filaOptima = i;
            }
        }
        actualizarOptimo(mejorPrecio, mejorGanancia, filaOptima);
    }
}
