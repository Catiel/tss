package actividad_3.ejercicio_1;

import javax.swing.*;
import java.awt.*;

public class PanelConfiguracion extends JPanel {
    private final JTextField txtCosteUnitario;
    private final JTextField txtTipoCambio;
    private final JTextField txtConstanteDemanda;
    private final JTextField txtElasticidad;
    private final JLabel lblCosteUnitarioLibras;

    public PanelConfiguracion() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 18, 14, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        JLabel titulo = new JLabel("Configuración de parámetros del modelo");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblCoste = new JLabel("Costo unitario ($):");
        EstilosUI.aplicarEstiloLabel(lblCoste);
        add(lblCoste, gbc);
        gbc.gridx = 1;
        txtCosteUnitario = new JTextField("50", 8);
        txtCosteUnitario.setToolTipText("Costo unitario de fabricación en dólares");
        txtCosteUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCosteUnitario, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCambio = new JLabel("Tipo de cambio ($/£):");
        EstilosUI.aplicarEstiloLabel(lblCambio);
        add(lblCambio, gbc);
        gbc.gridx = 1;
        txtTipoCambio = new JTextField("1.22", 8);
        txtTipoCambio.setToolTipText("Tipo de cambio actual entre dólar y libra");
        txtTipoCambio.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtTipoCambio, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCosteLibras = new JLabel("Costo unitario equivalente en libras (calculado):");
        EstilosUI.aplicarEstiloLabel(lblCosteLibras);
        add(lblCosteLibras, gbc);
        gbc.gridx = 1;
        lblCosteUnitarioLibras = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblCosteUnitarioLibras);
        add(lblCosteUnitarioLibras, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblConstante = new JLabel("Constante de demanda:");
        EstilosUI.aplicarEstiloLabel(lblConstante);
        add(lblConstante, gbc);
        gbc.gridx = 1;
        txtConstanteDemanda = new JTextField("27556759", 8);
        txtConstanteDemanda.setToolTipText("Constante de la función de demanda");
        txtConstanteDemanda.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtConstanteDemanda, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblElasticidad = new JLabel("Elasticidad:");
        EstilosUI.aplicarEstiloLabel(lblElasticidad);
        add(lblElasticidad, gbc);
        gbc.gridx = 1;
        txtElasticidad = new JTextField("-2.4", 8);
        txtElasticidad.setToolTipText("Elasticidad de la función de demanda");
        txtElasticidad.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtElasticidad, gbc);

        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnGuardar = new JButton("Guardar cambios");
        EstilosUI.aplicarEstiloBoton(btnGuardar);
        btnGuardar.setToolTipText("Aplica los cambios a los parámetros del modelo");
        add(btnGuardar, gbc);

        txtCosteUnitario.getDocument().addDocumentListener(new SimpleDocumentListener(this::actualizarCosteLibras));
        txtTipoCambio.getDocument().addDocumentListener(new SimpleDocumentListener(this::actualizarCosteLibras));

        btnGuardar.addActionListener(e -> guardarCambios());
        actualizarCosteLibras();
    }

    private void actualizarCosteLibras() {
        try {
            double costeUnitario = Double.parseDouble(txtCosteUnitario.getText());
            double tipoCambio = Double.parseDouble(txtTipoCambio.getText());
            double libras = costeUnitario / tipoCambio;
            lblCosteUnitarioLibras.setText(String.format("%.3f", libras));
        } catch (Exception ex) {
            lblCosteUnitarioLibras.setText("-");
        }
    }

    private void guardarCambios() {
        try {
            double costeUnitario = Double.parseDouble(txtCosteUnitario.getText());
            double tipoCambio = Double.parseDouble(txtTipoCambio.getText());
            double constanteDemanda = Double.parseDouble(txtConstanteDemanda.getText());
            double elasticidad = Double.parseDouble(txtElasticidad.getText());
            if (txtCosteUnitario.getText().isEmpty() || txtTipoCambio.getText().isEmpty() ||
                txtConstanteDemanda.getText().isEmpty() || txtElasticidad.getText().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ningún campo puede quedar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            ControladorParametros params = ControladorParametros.getInstancia();
            params.setCosteUnitario(costeUnitario);
            params.setTipoCambio(tipoCambio);
            params.setConstanteDemanda(constanteDemanda);
            params.setElasticidad(elasticidad);
            actualizarCosteLibras();
            JOptionPane.showMessageDialog(this, "Parámetros actualizados correctamente.", "Configuración", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Todos los campos deben ser numéricos y válidos.", "Error", JOptionPane.ERROR_MESSAGE);
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
