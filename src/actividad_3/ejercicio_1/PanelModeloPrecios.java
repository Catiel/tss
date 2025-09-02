package actividad_3.ejercicio_1;

import javax.swing.*;
import java.awt.*;

public class PanelModeloPrecios extends JPanel {
    private final JTextField txtPrecioPrueba;
    private final JLabel lblDemanda;
    private final JLabel lblGanancia;

    public PanelModeloPrecios() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 18, 12, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        JLabel titulo = new JLabel("Modelo de precios (encontrar el precio correcto en £ para maximizar las ganancias en $)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 6;
        add(titulo, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblPrecio = new JLabel("Precio £ (valor de prueba):");
        EstilosUI.aplicarEstiloLabel(lblPrecio);
        add(lblPrecio, gbc);
        gbc.gridx = 1;
        txtPrecioPrueba = new JTextField("43", 8);
        txtPrecioPrueba.setToolTipText("Ingrese el precio de prueba en libras");
        txtPrecioPrueba.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtPrecioPrueba, gbc);

        gbc.gridx = 2;
        JLabel lblDemandaTitulo = new JLabel("Demanda (en el Reino Unido):");
        EstilosUI.aplicarEstiloLabel(lblDemandaTitulo);
        add(lblDemandaTitulo, gbc);
        gbc.gridx = 3;
        lblDemanda = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblDemanda);
        add(lblDemanda, gbc);

        gbc.gridx = 4;
        JLabel lblGananciaTitulo = new JLabel("Ganancia ($):");
        EstilosUI.aplicarEstiloLabel(lblGananciaTitulo);
        add(lblGananciaTitulo, gbc);
        gbc.gridx = 5;
        lblGanancia = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblGanancia);
        add(lblGanancia, gbc);

        txtPrecioPrueba.getDocument().addDocumentListener(new SimpleDocumentListener(this::actualizarResultados));
        actualizarResultados();
    }

    private void actualizarResultados() {
        try {
            double precio = Double.parseDouble(txtPrecioPrueba.getText());
            ControladorParametros params = ControladorParametros.getInstancia();
            double demanda = params.getConstanteDemanda() * Math.pow(precio, params.getElasticidad());
            double ingresos = demanda * precio * params.getTipoCambio();
            double costeTotal = demanda * params.getCosteUnitario();
            double ganancia = ingresos - costeTotal;
            lblDemanda.setText(String.format("%.2f", demanda));
            lblGanancia.setText(String.format("%.2f", ganancia));
        } catch (Exception ex) {
            lblDemanda.setText("-");
            lblGanancia.setText("-");
        }
    }

    // DocumentListener simple para cambios en JTextField
    private static class SimpleDocumentListener implements javax.swing.event.DocumentListener {
        private final Runnable onChange;
        public SimpleDocumentListener(Runnable onChange) { this.onChange = onChange; }
        public void insertUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        public void removeUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
        public void changedUpdate(javax.swing.event.DocumentEvent e) { onChange.run(); }
    }
}
