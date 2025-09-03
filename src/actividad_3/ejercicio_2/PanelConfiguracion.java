package actividad_3.ejercicio_2;

import javax.swing.*;
import java.awt.*;

public class PanelConfiguracion extends JPanel {
    private final JTextField txtDemandaInicial;
    private final JTextField txtCrecimientoAnual;
    private final JTextField txtCostoCapacidadUnitaria;
    private final JTextField txtPrecioVentaUnitario;
    private final JTextField txtCostoVariableUnitario;
    private final JTextField txtCostoOperativoUnitario;
    private final JTextField txtTasaDescuento;

    public PanelConfiguracion() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 18, 14, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        JLabel titulo = new JLabel("Configuración de parámetros del modelo Wozac");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        ControladorParametros params = ControladorParametros.getInstancia();

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel lblDemanda = new JLabel("Demanda del año actual:");
        EstilosUI.aplicarEstiloLabel(lblDemanda);
        add(lblDemanda, gbc);
        gbc.gridx = 1;
        txtDemandaInicial = new JTextField(String.valueOf(params.getDemandaInicial()), 8);
        txtDemandaInicial.setToolTipText("Demanda inicial del medicamento Wozac");
        txtDemandaInicial.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtDemandaInicial, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCrecimiento = new JLabel("Crecimiento anual de la demanda (%):");
        EstilosUI.aplicarEstiloLabel(lblCrecimiento);
        add(lblCrecimiento, gbc);
        gbc.gridx = 1;
        txtCrecimientoAnual = new JTextField(String.valueOf(params.getCrecimientoAnual() * 100), 8);
        txtCrecimientoAnual.setToolTipText("Porcentaje de crecimiento anual de la demanda");
        txtCrecimientoAnual.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCrecimientoAnual, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCostoCapacidad = new JLabel("Costo de capacidad unitaria ($):");
        EstilosUI.aplicarEstiloLabel(lblCostoCapacidad);
        add(lblCostoCapacidad, gbc);
        gbc.gridx = 1;
        txtCostoCapacidadUnitaria = new JTextField(String.valueOf(params.getCostoCapacidadUnitaria()), 8);
        txtCostoCapacidadUnitaria.setToolTipText("Costo único por unidad de capacidad");
        txtCostoCapacidadUnitaria.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCostoCapacidadUnitaria, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblPrecioVenta = new JLabel("Precio de venta unitario ($):");
        EstilosUI.aplicarEstiloLabel(lblPrecioVenta);
        add(lblPrecioVenta, gbc);
        gbc.gridx = 1;
        txtPrecioVentaUnitario = new JTextField(String.valueOf(params.getPrecioVentaUnitario()), 8);
        txtPrecioVentaUnitario.setToolTipText("Precio de venta por unidad");
        txtPrecioVentaUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtPrecioVentaUnitario, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCostoVariable = new JLabel("Costo variable unitario ($):");
        EstilosUI.aplicarEstiloLabel(lblCostoVariable);
        add(lblCostoVariable, gbc);
        gbc.gridx = 1;
        txtCostoVariableUnitario = new JTextField(String.valueOf(params.getCostoVariableUnitario()), 8);
        txtCostoVariableUnitario.setToolTipText("Costo variable de producción por unidad");
        txtCostoVariableUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCostoVariableUnitario, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblCostoOperativo = new JLabel("Costo operativo unitario anual ($):");
        EstilosUI.aplicarEstiloLabel(lblCostoOperativo);
        add(lblCostoOperativo, gbc);
        gbc.gridx = 1;
        txtCostoOperativoUnitario = new JTextField(String.valueOf(params.getCostoOperativoUnitario()), 8);
        txtCostoOperativoUnitario.setToolTipText("Costo operativo anual por unidad de capacidad");
        txtCostoOperativoUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtCostoOperativoUnitario, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        JLabel lblTasaDescuento = new JLabel("Tasa de descuento para VAN (%):");
        EstilosUI.aplicarEstiloLabel(lblTasaDescuento);
        add(lblTasaDescuento, gbc);
        gbc.gridx = 1;
        txtTasaDescuento = new JTextField(String.valueOf(params.getTasaDescuento() * 100), 8);
        txtTasaDescuento.setToolTipText("Tasa de descuento anual para el cálculo de VAN");
        txtTasaDescuento.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        add(txtTasaDescuento, gbc);

        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnGuardar = new JButton("Guardar cambios");
        EstilosUI.aplicarEstiloBoton(btnGuardar);
        btnGuardar.setToolTipText("Aplica los cambios a los parámetros del modelo");
        add(btnGuardar, gbc);

        btnGuardar.addActionListener(e -> guardarCambios());
    }

    private void guardarCambios() {
        ControladorParametros params = ControladorParametros.getInstancia();
        try {
            int demanda = Integer.parseInt(txtDemandaInicial.getText());
            double crecimiento = Double.parseDouble(txtCrecimientoAnual.getText()) / 100.0;
            double costoCapacidad = Double.parseDouble(txtCostoCapacidadUnitaria.getText());
            double precioVenta = Double.parseDouble(txtPrecioVentaUnitario.getText());
            double costoVariable = Double.parseDouble(txtCostoVariableUnitario.getText());
            double costoOperativo = Double.parseDouble(txtCostoOperativoUnitario.getText());
            double tasaDescuento = Double.parseDouble(txtTasaDescuento.getText()) / 100.0;
            params.setDemandaInicial(demanda);
            params.setCrecimientoAnual(crecimiento);
            params.setCostoCapacidadUnitaria(costoCapacidad);
            params.setPrecioVentaUnitario(precioVenta);
            params.setCostoVariableUnitario(costoVariable);
            params.setCostoOperativoUnitario(costoOperativo);
            params.setTasaDescuento(tasaDescuento);
            JOptionPane.showMessageDialog(this, "Parámetros guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados. Verifique los valores.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
