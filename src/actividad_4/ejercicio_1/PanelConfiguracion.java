package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Importa componentes Swing
import java.awt.*;    // Importa clases AWT para layouts, colores y fuentes

public class PanelConfiguracion extends JPanel { // Clase del panel de configuración
    private final JTextField txtTamanoMercado;           // Campo: tamaño actual del mercado
    private final JTextField txtCrecimientoPrimeros5;    // Campo: crecimiento años 1-5 (%)
    private final JTextField txtCrecimientoProximos5;    // Campo: crecimiento años 6-10 (%)
    private final JTextField txtPrecioVentaUnitario;     // Campo: precio de venta unitario
    private final JTextField txtCosteVariableUnitario;   // Campo: coste variable unitario
    private final JTextField txtCuotaVersionIngles;      // Campo: cuota mercado versión inglés (%)
    private final JTextField txtCuotaConNuevaVersion;    // Campo: cuota mercado nueva versión (%)
    private final JTextField txtCosteFijoCrearVersion;   // Campo: coste fijo desarrollo nueva versión
    private final JTextField txtHorizonteAnios;          // Campo: horizonte (años)
    private final JTextField txtTasaDescuento;           // Campo: tasa de descuento (%)

    /**
     * Constructor: arma la UI, precarga valores y define acción de guardado.
     */
    public PanelConfiguracion() { // Inicio constructor
        EstilosUI.aplicarEstiloPanel(this);              // Aplica estilo visual al panel
        setLayout(new GridBagLayout());                  // Usa GridBagLayout para disposición flexible
        GridBagConstraints gbc = new GridBagConstraints(); // Restricciones para cada componente
        gbc.insets = new Insets(14, 18, 14, 18);         // Márgenes internos (padding)
        gbc.anchor = GridBagConstraints.WEST;            // Alinear a la izquierda
        gbc.gridy = 0;                                   // Comienza en fila 0

        JLabel titulo = new JLabel("Configuración de parámetros - Ejercicio 1"); // Título principal
        EstilosUI.aplicarEstiloTitulo(titulo);           // Aplica estilo de título
        gbc.gridx = 0; gbc.gridwidth = 2;                // Ocupa dos columnas
        add(titulo, gbc);                                // Añade título al panel

        ControladorParametros params = ControladorParametros.getInstancia(); // Obtiene singleton de parámetros

        gbc.gridy++;                                     // Siguiente fila
        gbc.gridwidth = 1;                               // Restaura ancho a 1 columna
        JLabel lblTamano = new JLabel("Tamaño actual del mercado (unidades):"); // Etiqueta tamaño mercado
        EstilosUI.aplicarEstiloLabel(lblTamano);         // Estilo etiqueta
        add(lblTamano, gbc);                             // Añade etiqueta
        gbc.gridx = 1;                                   // Columna campo
        txtTamanoMercado = new JTextField(String.valueOf(params.getTamanoActualMercado()), 10); // Campo tamaño
        txtTamanoMercado.setToolTipText("Tamaño actual del mercado (unidades)"); // Tooltip
        txtTamanoMercado.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtTamanoMercado, gbc);                      // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila crecimiento 1-5
        JLabel lblCrecimiento = new JLabel("Crecimiento anual (años 1-5) (%):"); // Etiqueta crecimiento 1-5
        EstilosUI.aplicarEstiloLabel(lblCrecimiento);    // Estilo
        add(lblCrecimiento, gbc);                        // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCrecimientoPrimeros5 = new JTextField(String.valueOf(params.getCrecimientoPrimeros5() * 100), 8); // Campo crecimiento 1-5
        txtCrecimientoPrimeros5.setToolTipText("Crecimiento anual para los primeros 5 años"); // Tooltip
        txtCrecimientoPrimeros5.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCrecimientoPrimeros5, gbc);               // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila crecimiento 6-10
        JLabel lblCrecimiento2 = new JLabel("Crecimiento anual (años 6-10) (%):"); // Etiqueta crecimiento 6-10
        EstilosUI.aplicarEstiloLabel(lblCrecimiento2);   // Estilo
        add(lblCrecimiento2, gbc);                       // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCrecimientoProximos5 = new JTextField(String.valueOf(params.getCrecimientoProximos5() * 100), 8); // Campo crecimiento 6-10
        txtCrecimientoProximos5.setToolTipText("Crecimiento anual para los años 6-10"); // Tooltip
        txtCrecimientoProximos5.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCrecimientoProximos5, gbc);               // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // (Fila separadora vacía opcional)
        // (No se añade componente; mantiene espaciamiento vertical)

        gbc.gridy++; gbc.gridx = 0;                      // Fila precio de venta
        JLabel lblPrecioVenta = new JLabel("Precio de venta unitario ($):"); // Etiqueta precio
        EstilosUI.aplicarEstiloLabel(lblPrecioVenta);    // Estilo
        add(lblPrecioVenta, gbc);                        // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtPrecioVentaUnitario = new JTextField(String.valueOf(params.getPrecioVentaUnitario()), 8); // Campo precio
        txtPrecioVentaUnitario.setToolTipText("Precio de venta por unidad"); // Tooltip
        txtPrecioVentaUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtPrecioVentaUnitario, gbc);                // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila coste variable
        JLabel lblCosteVariable = new JLabel("Coste variable unitario ($):"); // Etiqueta coste variable
        EstilosUI.aplicarEstiloLabel(lblCosteVariable);  // Estilo
        add(lblCosteVariable, gbc);                      // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCosteVariableUnitario = new JTextField(String.valueOf(params.getCosteVariableUnitario()), 8); // Campo coste variable
        txtCosteVariableUnitario.setToolTipText("Coste variable por unidad"); // Tooltip
        txtCosteVariableUnitario.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCosteVariableUnitario, gbc);              // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila cuota inglés
        JLabel lblCuotaIngles = new JLabel("Cuota de mercado (versión inglés) (%):"); // Etiqueta cuota actual
        EstilosUI.aplicarEstiloLabel(lblCuotaIngles);    // Estilo
        add(lblCuotaIngles, gbc);                        // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCuotaVersionIngles = new JTextField(String.valueOf(params.getCuotaMercadoVersionIngles() * 100), 8); // Campo cuota inglés
        txtCuotaVersionIngles.setToolTipText("Cuota de mercado de la versión en inglés (%)"); // Tooltip
        txtCuotaVersionIngles.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCuotaVersionIngles, gbc);                 // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila cuota nueva versión
        JLabel lblCuotaNueva = new JLabel("Cuota de mercado con nueva versión (%):"); // Etiqueta cuota nueva
        EstilosUI.aplicarEstiloLabel(lblCuotaNueva);     // Estilo
        add(lblCuotaNueva, gbc);                         // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCuotaConNuevaVersion = new JTextField(String.valueOf(params.getCuotaMercadoConNuevaVersion() * 100), 8); // Campo cuota nueva
        txtCuotaConNuevaVersion.setToolTipText("Cuota de mercado con la nueva versión (%)"); // Tooltip
        txtCuotaConNuevaVersion.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCuotaConNuevaVersion, gbc);               // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila coste fijo
        JLabel lblCosteFijo = new JLabel("Coste fijo crear versión ($):"); // Etiqueta coste fijo
        EstilosUI.aplicarEstiloLabel(lblCosteFijo);      // Estilo
        add(lblCosteFijo, gbc);                          // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtCosteFijoCrearVersion = new JTextField(String.valueOf(params.getCosteFijoCrearVersion()), 12); // Campo coste fijo
        txtCosteFijoCrearVersion.setToolTipText("Coste fijo de crear la nueva versión ($)"); // Tooltip
        txtCosteFijoCrearVersion.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtCosteFijoCrearVersion, gbc);              // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila horizonte
        JLabel lblHorizonte = new JLabel("Horizonte (años):"); // Etiqueta horizonte
        EstilosUI.aplicarEstiloLabel(lblHorizonte);      // Estilo
        add(lblHorizonte, gbc);                          // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtHorizonteAnios = new JTextField(String.valueOf(params.getHorizonteAnios()), 6); // Campo horizonte
        txtHorizonteAnios.setToolTipText("Horizonte de planificación (años)"); // Tooltip
        txtHorizonteAnios.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtHorizonteAnios, gbc);                     // Añade campo

        gbc.gridy++; gbc.gridx = 0;                      // Fila tasa descuento
        JLabel lblTasaDescuento = new JLabel("Tasa de descuento para VAN (%):"); // Etiqueta tasa descuento
        EstilosUI.aplicarEstiloLabel(lblTasaDescuento);  // Estilo
        add(lblTasaDescuento, gbc);                      // Añade etiqueta
        gbc.gridx = 1;                                   // Campo
        txtTasaDescuento = new JTextField(String.valueOf(params.getTasaDescuento() * 100), 8); // Campo tasa descuento
        txtTasaDescuento.setToolTipText("Tasa de descuento anual para el cálculo de VAN"); // Tooltip
        txtTasaDescuento.setFont(new Font("Segoe UI", Font.PLAIN, 15)); // Fuente
        add(txtTasaDescuento, gbc);                     // Añade campo

        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2;   // Fila botón guardar
        JButton btnGuardar = new JButton("Guardar cambios"); // Botón guardar
        EstilosUI.aplicarEstiloBoton(btnGuardar);        // Estilo botón
        btnGuardar.setToolTipText("Aplica los cambios a los parámetros del modelo"); // Tooltip
        add(btnGuardar, gbc);                            // Añade botón

        btnGuardar.addActionListener(e -> guardarCambios()); // Acción guardar -> invoca método
    }

    /**
     * Lee los campos, valida formato numérico, actualiza el controlador y muestra mensajes.
     */
    private void guardarCambios() { // Inicia guardado
        ControladorParametros params = ControladorParametros.getInstancia(); // Obtiene controlador
        try { // Intento de parseo y asignación
            int tamano = Integer.parseInt(txtTamanoMercado.getText()); // Parse tamaño mercado
            double crec1 = Double.parseDouble(txtCrecimientoPrimeros5.getText()) / 100.0; // Parse crecimiento 1-5
            double crec2 = Double.parseDouble(txtCrecimientoProximos5.getText()) / 100.0; // Parse crecimiento 6-10
            double precioVenta = Double.parseDouble(txtPrecioVentaUnitario.getText()); // Parse precio
            double costeVar = Double.parseDouble(txtCosteVariableUnitario.getText()); // Parse coste variable
            double cuotaIng = Double.parseDouble(txtCuotaVersionIngles.getText()) / 100.0; // Parse cuota inglés
            double cuotaNew = Double.parseDouble(txtCuotaConNuevaVersion.getText()) / 100.0; // Parse cuota nueva versión
            double costeFijo = Double.parseDouble(txtCosteFijoCrearVersion.getText()); // Parse coste fijo
            int horizonte = Integer.parseInt(txtHorizonteAnios.getText()); // Parse horizonte años
            double tasaDesc = Double.parseDouble(txtTasaDescuento.getText()) / 100.0; // Parse tasa descuento

            params.setTamanoActualMercado(tamano);                 // Actualiza tamaño
            params.setCrecimientoPrimeros5(crec1);                  // Actualiza crecimiento 1-5
            params.setCrecimientoProximos5(crec2);                  // Actualiza crecimiento 6-10
            params.setPrecioVentaUnitario(precioVenta);             // Actualiza precio
            params.setCosteVariableUnitario(costeVar);              // Actualiza coste variable
            params.setCuotaMercadoVersionIngles(cuotaIng);          // Actualiza cuota inglés
            params.setCuotaMercadoConNuevaVersion(cuotaNew);        // Actualiza cuota nueva versión
            params.setCosteFijoCrearVersion(costeFijo);             // Actualiza coste fijo
            params.setHorizonteAnios(horizonte);                    // Actualiza horizonte
            params.setTasaDescuento(tasaDesc);                     // Actualiza tasa descuento

            JOptionPane.showMessageDialog(this, "Parámetros guardados correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE); // Mensaje éxito
        } catch (Exception ex) { // Captura cualquier error de parseo
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados. Verifique los valores.", "Error", JOptionPane.ERROR_MESSAGE); // Mensaje error
        }
    }
}
