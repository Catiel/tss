package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelModeloPrecios extends JPanel {
    private final JTextField txtCapacidad;
    private final JLabel lblGananciaTotal;
    private final DefaultTableModel modeloTabla;

    public PanelModeloPrecios() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 18, 12, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Título
        JLabel titulo = new JLabel("Modelo de decisión de capacidad y utilidades esperadas");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 6;
        add(titulo, gbc);

        // Parámetro de capacidad
        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblCapacidad = new JLabel("Unidades de capacidad a incorporar en este período:");
        EstilosUI.aplicarEstiloLabel(lblCapacidad);
        add(lblCapacidad, gbc);
        gbc.gridx = 1;
        txtCapacidad = new JTextField("55000", 8);
        txtCapacidad.setToolTipText("Capacidad de la planta a construir");
        txtCapacidad.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCapacidad, gbc);

        // Ganancia total
        gbc.gridx = 2;
        JLabel lblGananciaTitulo = new JLabel("Ganancia total en 10 años ($):");
        EstilosUI.aplicarEstiloLabel(lblGananciaTitulo);
        add(lblGananciaTitulo, gbc);
        gbc.gridx = 3;
        lblGananciaTotal = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblGananciaTotal);
        add(lblGananciaTotal, gbc);

        // Tabla de resultados
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 6;
        String[] columnas = {"Año", "Demanda", "Inversión inicial", "Costo fijo operación", "Unidades producidas", "Ingresos ventas", "Costo variable producción", "Utilidad"};
        modeloTabla = new DefaultTableModel(columnas, 0);
        JTable tablaResultados = new JTable(modeloTabla);
        EstilosUI.aplicarEstiloTabla(tablaResultados);
        JScrollPane scroll = new JScrollPane(tablaResultados, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scroll.setPreferredSize(new Dimension(1100, 280));
        tablaResultados.setFillsViewportHeight(true);
        // Ajuste de ancho de columnas
        int[] anchos = {60, 90, 120, 120, 120, 140, 140, 120};
        for (int i = 0; i < anchos.length; i++) {
            tablaResultados.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        add(scroll, gbc);

        // Listener para recalcular al cambiar la capacidad
        txtCapacidad.getDocument().addDocumentListener(new SimpleDocumentListener(this::actualizarResultados));
        actualizarResultados();
    }

    private void actualizarResultados() {
        try {
            int capacidad = Integer.parseInt(txtCapacidad.getText());
            ControladorParametros params = ControladorParametros.getInstancia();
            ModeloWozacCalculo.ResultadoAnual[] resultados = ModeloWozacCalculo.calcularModelo(
                capacidad,
                params.getDemandaInicial(),
                params.getCrecimientoAnual(),
                params.getCostoCapacidadUnitaria(),
                params.getPrecioVentaUnitario(),
                params.getCostoVariableUnitario(),
                params.getCostoOperativoUnitario()
            );
            modeloTabla.setRowCount(0);
            double total = 0;
            for (ModeloWozacCalculo.ResultadoAnual r : resultados) {
                modeloTabla.addRow(new Object[] {
                    r.anio,
                    r.demanda,
                    String.format("$%,.0f", r.inversionInicial),
                    String.format("$%,.0f", r.costoFijoOperacion),
                    r.unidadesProducidas,
                    String.format("$%,.0f", r.ingresosVentas),
                    String.format("$%,.0f", r.costoVariableProduccion),
                    String.format("$%,.0f", r.utilidad)
                });
                total += r.utilidad;
            }
            lblGananciaTotal.setText(String.format("$%,.0f", total));
        } catch (Exception ex) {
            lblGananciaTotal.setText("-");
            modeloTabla.setRowCount(0);
        }
    }

    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable onChange;
        public SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }
}
