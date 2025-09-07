package actividad_4.ejercicio_1;

import javax.swing.*;
import java.awt.*;

public class PanelConfiguracion extends JPanel {
    private final JTextField txtTamanoMercado;
    private final JTextField txtCrecimientoPrimeros5;
    private final JTextField txtCrecimientoProximos5;
    private final JTextField txtPrecioVentaUnitario;
    private final JTextField txtCosteVariableUnitario;
    private final JTextField txtCuotaVersionIngles;
    private final JTextField txtCuotaConNuevaVersion;
    private final JTextField txtCosteFijoCrearVersion;
    private final JTextField txtHorizonteAnios;
    private final JTextField txtTasaDescuento;

    /**
     * Constructor del panel de configuración de parámetros del modelo Wozac.
     * Inicializa la interfaz gráfica y carga los valores actuales de los parámetros.
     * Permite al usuario modificar los parámetros y guardarlos.
     */
    public PanelConfiguracion() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(14, 18, 14, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

    JLabel titulo = new JLabel("Configuración de parámetros - Ejercicio 1");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 2;
        add(titulo, gbc);

        ControladorParametros params = ControladorParametros.getInstancia();

        gbc.gridy++;
        gbc.gridwidth = 1;
    JLabel lblTamano = new JLabel("Tamaño actual del mercado (unidades):");
    EstilosUI.aplicarEstiloLabel(lblTamano);
    add(lblTamano, gbc);
    gbc.gridx = 1;
    txtTamanoMercado = new JTextField(String.valueOf(params.getTamanoActualMercado()), 10);
    txtTamanoMercado.setToolTipText("Tamaño actual del mercado (unidades)");
    txtTamanoMercado.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtTamanoMercado, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
    JLabel lblCrecimiento = new JLabel("Crecimiento anual (años 1-5) (%):");
    EstilosUI.aplicarEstiloLabel(lblCrecimiento);
    add(lblCrecimiento, gbc);
    gbc.gridx = 1;
    txtCrecimientoPrimeros5 = new JTextField(String.valueOf(params.getCrecimientoPrimeros5() * 100), 8);
    txtCrecimientoPrimeros5.setToolTipText("Crecimiento anual para los primeros 5 años");
    txtCrecimientoPrimeros5.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCrecimientoPrimeros5, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    JLabel lblCrecimiento2 = new JLabel("Crecimiento anual (años 6-10) (%):");
    EstilosUI.aplicarEstiloLabel(lblCrecimiento2);
    add(lblCrecimiento2, gbc);
    gbc.gridx = 1;
    txtCrecimientoProximos5 = new JTextField(String.valueOf(params.getCrecimientoProximos5() * 100), 8);
    txtCrecimientoProximos5.setToolTipText("Crecimiento anual para los años 6-10");
    txtCrecimientoProximos5.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCrecimientoProximos5, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
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
    JLabel lblCosteVariable = new JLabel("Coste variable unitario ($):");
    EstilosUI.aplicarEstiloLabel(lblCosteVariable);
    add(lblCosteVariable, gbc);
    gbc.gridx = 1;
    txtCosteVariableUnitario = new JTextField(String.valueOf(params.getCosteVariableUnitario()), 8);
    txtCosteVariableUnitario.setToolTipText("Coste variable por unidad");
    txtCosteVariableUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCosteVariableUnitario, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    JLabel lblCuotaIngles = new JLabel("Cuota de mercado (versión inglés) (%):");
    EstilosUI.aplicarEstiloLabel(lblCuotaIngles);
    add(lblCuotaIngles, gbc);
    gbc.gridx = 1;
    txtCuotaVersionIngles = new JTextField(String.valueOf(params.getCuotaMercadoVersionIngles() * 100), 8);
    txtCuotaVersionIngles.setToolTipText("Cuota de mercado de la versión en inglés (%)");
    txtCuotaVersionIngles.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCuotaVersionIngles, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    JLabel lblCuotaNueva = new JLabel("Cuota de mercado con nueva versión (%):");
    EstilosUI.aplicarEstiloLabel(lblCuotaNueva);
    add(lblCuotaNueva, gbc);
    gbc.gridx = 1;
    txtCuotaConNuevaVersion = new JTextField(String.valueOf(params.getCuotaMercadoConNuevaVersion() * 100), 8);
    txtCuotaConNuevaVersion.setToolTipText("Cuota de mercado con la nueva versión (%)");
    txtCuotaConNuevaVersion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCuotaConNuevaVersion, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    JLabel lblCosteFijo = new JLabel("Coste fijo crear versión ($):");
    EstilosUI.aplicarEstiloLabel(lblCosteFijo);
    add(lblCosteFijo, gbc);
    gbc.gridx = 1;
    txtCosteFijoCrearVersion = new JTextField(String.valueOf(params.getCosteFijoCrearVersion()), 12);
    txtCosteFijoCrearVersion.setToolTipText("Coste fijo de crear la nueva versión ($)");
    txtCosteFijoCrearVersion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtCosteFijoCrearVersion, gbc);

    gbc.gridy++;
    gbc.gridx = 0;
    JLabel lblHorizonte = new JLabel("Horizonte (años):");
    EstilosUI.aplicarEstiloLabel(lblHorizonte);
    add(lblHorizonte, gbc);
    gbc.gridx = 1;
    txtHorizonteAnios = new JTextField(String.valueOf(params.getHorizonteAnios()), 6);
    txtHorizonteAnios.setToolTipText("Horizonte de planificación (años)");
    txtHorizonteAnios.setFont(new Font("Segoe UI", Font.PLAIN, 15));
    add(txtHorizonteAnios, gbc);

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

    /**
     * Guarda los cambios realizados en los campos de parámetros.
     * Valida y actualiza los valores en el controlador de parámetros.
     * Si los datos son válidos, muestra un mensaje de éxito; si hay error, muestra un mensaje de advertencia.
     */
    private void guardarCambios() {
        ControladorParametros params = ControladorParametros.getInstancia();
        try {
            int tamano = Integer.parseInt(txtTamanoMercado.getText());
            double crec1 = Double.parseDouble(txtCrecimientoPrimeros5.getText()) / 100.0;
            double crec2 = Double.parseDouble(txtCrecimientoProximos5.getText()) / 100.0;
            double precioVenta = Double.parseDouble(txtPrecioVentaUnitario.getText());
            double costeVar = Double.parseDouble(txtCosteVariableUnitario.getText());
            double cuotaIng = Double.parseDouble(txtCuotaVersionIngles.getText()) / 100.0;
            double cuotaNew = Double.parseDouble(txtCuotaConNuevaVersion.getText()) / 100.0;
            double costeFijo = Double.parseDouble(txtCosteFijoCrearVersion.getText());
            int horizonte = Integer.parseInt(txtHorizonteAnios.getText());
            double tasaDesc = Double.parseDouble(txtTasaDescuento.getText()) / 100.0;

            params.setTamanoActualMercado(tamano);
            params.setCrecimientoPrimeros5(crec1);
            params.setCrecimientoProximos5(crec2);
            params.setPrecioVentaUnitario(precioVenta);
            params.setCosteVariableUnitario(costeVar);
            params.setCuotaMercadoVersionIngles(cuotaIng);
            params.setCuotaMercadoConNuevaVersion(cuotaNew);
            params.setCosteFijoCrearVersion(costeFijo);
            params.setHorizonteAnios(horizonte);
            params.setTasaDescuento(tasaDesc);

            JOptionPane.showMessageDialog(this, "Parámetros guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados. Verifique los valores.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
