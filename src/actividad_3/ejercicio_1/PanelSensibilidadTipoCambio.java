package actividad_3.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelSensibilidadTipoCambio extends JPanel {
    private final JTextField txtMinCambio;
    private final JTextField txtMaxCambio;
    private final JTextField txtPaso;
    private final JButton btnAnalizar;
    private final DefaultTableModel modeloTabla;
    private final JTable tabla;
    private int filaOptima = -1;

    public PanelSensibilidadTipoCambio() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10, 10));
        JLabel lblTitulo = new JLabel("Análisis de sensibilidad: tipo de cambio");
        EstilosUI.aplicarEstiloTitulo(lblTitulo);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);

        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        EstilosUI.aplicarEstiloPanel(panelSuperior);
        panelSuperior.add(new JLabel("Tipo de cambio mínimo:"));
        txtMinCambio = new JTextField("0.90", 6);
        txtMinCambio.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelSuperior.add(txtMinCambio);
        panelSuperior.add(new JLabel("máximo:"));
        txtMaxCambio = new JTextField("1.50", 6);
        txtMaxCambio.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelSuperior.add(txtMaxCambio);
        panelSuperior.add(new JLabel("paso:"));
        txtPaso = new JTextField("0.05", 4);
        txtPaso.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelSuperior.add(txtPaso);
        btnAnalizar = new JButton("Analizar sensibilidad");
        EstilosUI.aplicarEstiloBoton(btnAnalizar);
        panelSuperior.add(btnAnalizar);
        add(panelSuperior, BorderLayout.BEFORE_FIRST_LINE);

        modeloTabla = new DefaultTableModel(new String[]{"Tipo de cambio", "Precio óptimo", "Ganancia máxima"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tabla = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row == filaOptima) {
                    c.setBackground(new Color(180, 255, 180));
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        EstilosUI.aplicarEstiloTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        btnAnalizar.addActionListener(e -> analizarSensibilidad());
    }

    private void analizarSensibilidad() {
        modeloTabla.setRowCount(0);
        filaOptima = -1;
        double min, max, paso;
        try {
            min = Double.parseDouble(txtMinCambio.getText());
            max = Double.parseDouble(txtMaxCambio.getText());
            paso = Double.parseDouble(txtPaso.getText());
            if (min >= max || paso <= 0) throw new Exception();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos. Verifica el rango y el paso.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ControladorParametros params = ControladorParametros.getInstancia();
        double tipoCambioOriginal = params.getTipoCambio();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        int mejorFila = -1;
        int filaActual = 0;
        for (double tc = min; tc <= max + 1e-8; tc += paso) {
            params.setTipoCambio(tc);
            double mejorGananciaFila = Double.NEGATIVE_INFINITY;
            double mejorPrecio = 0;
            for (int precio = 44; precio <= 100; precio++) {
                double demanda = params.getConstanteDemanda() * Math.pow(precio, params.getElasticidad());
                double ingresos = demanda * precio * params.getTipoCambio();
                double costeTotal = demanda * params.getCosteUnitario();
                double ganancia = ingresos - costeTotal;
                if (ganancia > mejorGananciaFila) {
                    mejorGananciaFila = ganancia;
                    mejorPrecio = precio;
                }
            }
            modeloTabla.addRow(new Object[]{String.format("%.2f", tc), String.format("%.2f", mejorPrecio), String.format("%.2f", mejorGananciaFila)});
            if (mejorGananciaFila > mejorGanancia) {
                mejorGanancia = mejorGananciaFila;
                mejorFila = filaActual;
            }
            filaActual++;
        }
        params.setTipoCambio(tipoCambioOriginal); // restaurar
        filaOptima = mejorFila;
        tabla.repaint();
    }
}
