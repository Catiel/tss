package actividad_4.ejercicio_2;

import javax.swing.*;
import java.awt.*;

public class PanelConfiguracion extends JPanel {
    private final JTextField txtInversionOriginal;
    private final JTextField txtFlujoAnio1;
    private final JTextField txtTasaCrecimientoAnual;

    /**
     * Constructor del panel de configuración de parámetros del modelo Wozac.
     * Inicializa la interfaz gráfica y carga los valores actuales de los parámetros.
     * Permite al usuario modificar los parámetros y guardarlos.
     */
    public PanelConfiguracion() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 16, 8, 16);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        JLabel titulo = new JLabel("Configuración de parámetros");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        ControladorParametros params = ControladorParametros.getInstancia();
        gbc.gridwidth = 1;

        // ---------------- Ejercicio 2 ----------------
        gbc.gridy++; gbc.gridx = 0;
        JLabel seccion2 = new JLabel("Parámetros Ejercicio 2 (Payback)");
        seccion2.setFont(new Font("Segoe UI", Font.BOLD, 15));
        add(seccion2, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel lblInv = new JLabel("Inversión original (millones):"); EstilosUI.aplicarEstiloLabel(lblInv); add(lblInv, gbc);
        gbc.gridx = 1;
        txtInversionOriginal = new JTextField(String.valueOf(params.getInversionOriginal()),10); add(txtInversionOriginal, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel lblFlujo1 = new JLabel("Flujo caja año 1 (millones):"); EstilosUI.aplicarEstiloLabel(lblFlujo1); add(lblFlujo1, gbc);
        gbc.gridx = 1;
        txtFlujoAnio1 = new JTextField(String.valueOf(params.getFlujoAnio1()),10); add(txtFlujoAnio1, gbc);

        gbc.gridy++; gbc.gridx = 0;
        JLabel lblCrecConst = new JLabel("Crecimiento anual flujo (%):"); EstilosUI.aplicarEstiloLabel(lblCrecConst); add(lblCrecConst, gbc);
        gbc.gridx = 1;
        txtTasaCrecimientoAnual = new JTextField(String.valueOf(params.getTasaCrecimientoAnual()*100),10); add(txtTasaCrecimientoAnual, gbc);

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnGuardar = new JButton("Guardar cambios");
        EstilosUI.aplicarEstiloBoton(btnGuardar);
        add(btnGuardar, gbc);
        btnGuardar.addActionListener(e -> guardarCambios());
    }

    /**
     * Guarda los cambios realizados en los campos de parámetros.
     * Valida y actualiza los valores en el controlador de parámetros.
     * Si los datos son válidos, muestra un mensaje de éxito; si hay error, muestra un mensaje de advertencia.
     */
    private void guardarCambios() {
        ControladorParametros params = ControladorParametros.getInstancia();
        try {
            double inversion = Double.parseDouble(txtInversionOriginal.getText());
            double flujo1 = Double.parseDouble(txtFlujoAnio1.getText());
            double crecFlujo = Double.parseDouble(txtTasaCrecimientoAnual.getText())/100.0;

            params.setInversionOriginal(inversion);
            params.setFlujoAnio1(flujo1);
            params.setTasaCrecimientoAnual(crecFlujo);

            JOptionPane.showMessageDialog(this, "Parámetros guardados.", "OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en datos ingresados.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
