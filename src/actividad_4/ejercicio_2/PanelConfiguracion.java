package actividad_4.ejercicio_2; // Paquete donde reside la clase

import javax.swing.*; // Importa clases Swing
import java.awt.*;    // Importa clases AWT (layout, color, fuentes)

public class PanelConfiguracion extends JPanel { // Panel de configuración de parámetros
    private final JTextField txtInversionOriginal;      // Campo texto inversión inicial
    private final JTextField txtFlujoAnio1;             // Campo texto flujo año 1
    private final JTextField txtTasaCrecimientoAnual;   // Campo texto tasa crecimiento (%)

    /**
     * Constructor: arma la UI y carga valores iniciales desde ControladorParametros.
     */
    public PanelConfiguracion() { // Inicio constructor
        EstilosUI.aplicarEstiloPanel(this);          // Aplica estilo base al panel
        setLayout(new GridBagLayout());              // Usa GridBagLayout para disposición flexible
        GridBagConstraints gbc = new GridBagConstraints(); // Restricciones de posicionamiento
        gbc.insets = new Insets(8, 16, 8, 16);       // Márgenes internos (padding entre componentes)
        gbc.anchor = GridBagConstraints.WEST;        // Alinear componentes a la izquierda
        gbc.gridy = 0;                               // Fila inicial 0

        JLabel titulo = new JLabel("Configuración de parámetros"); // Etiqueta título
        EstilosUI.aplicarEstiloTitulo(titulo);       // Aplica estilo de título
        gbc.gridx = 0; gbc.gridwidth = 2;            // Columna 0, ocupa 2 columnas
        add(titulo, gbc);                            // Añade título al panel

        ControladorParametros params = ControladorParametros.getInstancia(); // Obtiene instancia singleton
        gbc.gridwidth = 1;                           // Restaura gridwidth a 1

        // ---------------- Ejercicio 2 ----------------
        gbc.gridy++; gbc.gridx = 0;                  // Siguiente fila, columna 0
        JLabel seccion2 = new JLabel("Parámetros Ejercicio 2 (Payback)"); // Subtítulo sección
        seccion2.setFont(new Font("Segoe UI", Font.BOLD, 15)); // Fuente negrita
        add(seccion2, gbc);                          // Añade subtítulo

        // Línea inversión
        gbc.gridy++; gbc.gridx = 0;                  // Nueva fila, columna 0
        JLabel lblInv = new JLabel("Inversión original (millones):"); // Etiqueta inversión
        EstilosUI.aplicarEstiloLabel(lblInv);        // Aplica estilo label
        add(lblInv, gbc);                            // Añade etiqueta
        gbc.gridx = 1;                               // Columna 1 (campo)
        txtInversionOriginal = new JTextField(String.valueOf(params.getInversionOriginal()),10); // Campo con valor actual
        add(txtInversionOriginal, gbc);              // Añade campo

        // Línea flujo año 1
        gbc.gridy++; gbc.gridx = 0;                  // Nueva fila, columna 0
        JLabel lblFlujo1 = new JLabel("Flujo caja año 1 (millones):"); // Etiqueta flujo 1
        EstilosUI.aplicarEstiloLabel(lblFlujo1);     // Estilo label
        add(lblFlujo1, gbc);                         // Añade etiqueta
        gbc.gridx = 1;                               // Columna campo
        txtFlujoAnio1 = new JTextField(String.valueOf(params.getFlujoAnio1()),10); // Campo valor flujo
        add(txtFlujoAnio1, gbc);                     // Añade campo

        // Línea tasa crecimiento
        gbc.gridy++; gbc.gridx = 0;                  // Nueva fila
        JLabel lblCrecConst = new JLabel("Crecimiento anual flujo (%):"); // Etiqueta tasa
        EstilosUI.aplicarEstiloLabel(lblCrecConst);  // Estilo
        add(lblCrecConst, gbc);                      // Añade etiqueta
        gbc.gridx = 1;                               // Columna campo
        txtTasaCrecimientoAnual = new JTextField(String.valueOf(params.getTasaCrecimientoAnual()*100),10); // Tasa en %
        add(txtTasaCrecimientoAnual, gbc);           // Añade campo

        // Botón guardar
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 2; // Nueva fila, ocupa 2 columnas
        JButton btnGuardar = new JButton("Guardar cambios"); // Botón guardar
        EstilosUI.aplicarEstiloBoton(btnGuardar);    // Estilo botón
        add(btnGuardar, gbc);                        // Añade botón
        btnGuardar.addActionListener(e -> guardarCambios()); // Listener click -> guardar
    }

    /**
     * Lee y valida campos; actualiza singleton; muestra mensaje según resultado.
     */
    private void guardarCambios() { // Método para persistir cambios
        ControladorParametros params = ControladorParametros.getInstancia(); // Instancia parámetros
        try { // Bloque de validación / parseo
            double inversion = Double.parseDouble(txtInversionOriginal.getText()); // Parse inversión
            double flujo1 = Double.parseDouble(txtFlujoAnio1.getText());            // Parse flujo año 1
            double crecFlujo = Double.parseDouble(txtTasaCrecimientoAnual.getText())/100.0; // Parse y pasa a decimal

            params.setInversionOriginal(inversion); // Actualiza inversión
            params.setFlujoAnio1(flujo1);           // Actualiza flujo 1
            params.setTasaCrecimientoAnual(crecFlujo); // Actualiza tasa crecimiento

            JOptionPane.showMessageDialog(this, "Parámetros guardados.", "OK", JOptionPane.INFORMATION_MESSAGE); // Éxito
        } catch (Exception ex) { // Cualquier error de parseo
            JOptionPane.showMessageDialog(this, "Error en datos ingresados.", "Error", JOptionPane.ERROR_MESSAGE); // Mensaje error
        }
    }
}
