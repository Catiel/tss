package actividad_4.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelModeloPrecios extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final JLabel lblVANCon;
    private final JLabel lblVANSin;
    private final JLabel lblDiferenciaVAN;
    private final JLabel lblTasaDescuentoValor; // Para mostrar la tasa de descuento actual
    private final DefaultTableModel modeloTabla;
    private final JTable tablaResultados;

    /**
     * Constructor. Inicializa la interfaz del panel para el modelo de decisión de capacidad y utilidades esperadas.
     * Configura los componentes visuales y los listeners para actualizar los resultados automáticamente.
     */
    public PanelModeloPrecios() {
        // Registramos este panel como oyente de cambios en los parámetros
        ControladorParametros.getInstancia().addChangeListener(this);

        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 18, 12, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Título
        JLabel titulo = new JLabel("Modelo de beneficios");
        EstilosUI.aplicarEstiloTitulo(titulo);
        gbc.gridx = 0; gbc.gridwidth = 8;
        add(titulo, gbc);

        // VAN con versión francesa
        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.gridx = 0;
        JLabel lblVANConTitulo = new JLabel("VAN con versión francesa ($):");
        EstilosUI.aplicarEstiloLabel(lblVANConTitulo);
        add(lblVANConTitulo, gbc);
        gbc.gridx = 1;
        lblVANCon = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblVANCon);
        add(lblVANCon, gbc);

        // VAN sin versión francesa
        gbc.gridx = 2;
        JLabel lblVANSinTitulo = new JLabel("VAN sin versión francesa ($):");
        EstilosUI.aplicarEstiloLabel(lblVANSinTitulo);
        add(lblVANSinTitulo, gbc);
        gbc.gridx = 3;
        lblVANSin = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblVANSin);
        add(lblVANSin, gbc);

        // Diferencia VAN
        gbc.gridx = 4;
        JLabel lblDiferenciaVANTitulo = new JLabel("Diferencia VAN ($):");
        EstilosUI.aplicarEstiloLabel(lblDiferenciaVANTitulo);
        add(lblDiferenciaVANTitulo, gbc);
        gbc.gridx = 5;
        lblDiferenciaVAN = new JLabel("-");
        EstilosUI.aplicarEstiloLabel(lblDiferenciaVAN);
        add(lblDiferenciaVAN, gbc);

        // Creamos las columnas según el Excel
        String[] columnas = {
            "Año",
            "Tamaño de mercado",
            "Unidades vendidas",
            "Costo variable",
            "Ingresos",
            "Unidades vendidas",
            "Costo variable",
            "Ingresos"
        };

        // Creamos el modelo de tabla y configuramos que las celdas no sean editables
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        // Creamos la tabla con el modelo
        tablaResultados = new JTable(modeloTabla);
        EstilosUI.aplicarEstiloTabla(tablaResultados);

        // Ajustamos los anchos de las columnas para que se vean bien
        int[] anchos = {60, 120, 120, 120, 120, 120, 120, 120};
        for (int i = 0; i < anchos.length; i++) {
            tablaResultados.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        // Creamos un panel para los encabezados de grupo
        JPanel headerPanel = new JPanel(new BorderLayout());

        // Panel para los encabezados "Sin versión francesa" y "Con versión francesa"
        JPanel groupHeaderPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcHeader = new GridBagConstraints();
        gbcHeader.fill = GridBagConstraints.HORIZONTAL;
        gbcHeader.insets = new Insets(2, 0, 2, 0);

        // Encabezado para "Sin la versión francesa"
        JLabel sinVersionLabel = new JLabel("Sin la versión francesa", SwingConstants.CENTER);
        sinVersionLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        sinVersionLabel.setOpaque(true);
        sinVersionLabel.setBackground(new Color(230, 230, 255));
        EstilosUI.aplicarEstiloLabel(sinVersionLabel);

        // Encabezado para "Con versión francesa"
        JLabel conVersionLabel = new JLabel("Con versión francesa", SwingConstants.CENTER);
        conVersionLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        conVersionLabel.setOpaque(true);
        conVersionLabel.setBackground(new Color(230, 255, 230));
        EstilosUI.aplicarEstiloLabel(conVersionLabel);

        // Espacio para las primeras dos columnas (Año, Tamaño de mercado)
        JLabel espacioLabel = new JLabel("");
        espacioLabel.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, Color.LIGHT_GRAY));
        espacioLabel.setOpaque(true);

        // Añadimos los encabezados al panel
        gbcHeader.gridx = 0;
        gbcHeader.gridy = 0;
        gbcHeader.gridwidth = 2;
        gbcHeader.weightx = 0.25;
        groupHeaderPanel.add(espacioLabel, gbcHeader);

        gbcHeader.gridx = 2;
        gbcHeader.gridwidth = 3;
        gbcHeader.weightx = 0.375;
        groupHeaderPanel.add(sinVersionLabel, gbcHeader);

        gbcHeader.gridx = 5;
        gbcHeader.gridwidth = 3;
        gbcHeader.weightx = 0.375;
        groupHeaderPanel.add(conVersionLabel, gbcHeader);

        // Creamos el panel de la tabla con los encabezados y la tabla
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.add(groupHeaderPanel, BorderLayout.NORTH);

        JScrollPane scrollPane = new JScrollPane(tablaResultados);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        // Agregamos el panel de la tabla al panel principal
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        add(panelTabla, gbc);

        // Añadimos información sobre la tasa de descuento
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 8;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weighty = 0.0;

        JPanel panelTasaDescuento = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTasaDescuentoTitulo = new JLabel("Tasa de descuento: ");
        EstilosUI.aplicarEstiloLabel(lblTasaDescuentoTitulo);
        lblTasaDescuentoValor = new JLabel("10%");
        EstilosUI.aplicarEstiloLabel(lblTasaDescuentoValor);
        panelTasaDescuento.add(lblTasaDescuentoTitulo);
        panelTasaDescuento.add(lblTasaDescuentoValor);

        add(panelTasaDescuento, gbc);

        // Actualizamos los resultados
        actualizarResultados();
    }

    /**
     * Formatea un valor numérico como moneda (entero con separador de miles y símbolo $).
     * @param valor El valor a formatear
     * @return Cadena formateada como moneda
     */
    private String formatearMoneda(double valor) {
        return String.format("$%,.0f", valor);
    }

    /**
     * Actualiza las etiquetas de VAN con los valores proporcionados.
     * @param vanCon Valor del VAN con versión francesa
     * @param vanSin Valor del VAN sin versión francesa
     * @param diferenciaVAN Diferencia entre ambos valores
     */
    private void actualizarEtiquetasVAN(double vanCon, double vanSin, double diferenciaVAN) {
        lblVANCon.setText(formatearMoneda(vanCon));
        lblVANSin.setText(formatearMoneda(vanSin));
        lblDiferenciaVAN.setText(formatearMoneda(diferenciaVAN));
    }

    /**
     * Limpia los resultados en caso de error.
     */
    private void limpiarResultados() {
        lblVANCon.setText("-");
        lblVANSin.setText("-");
        lblDiferenciaVAN.setText("-");
        modeloTabla.setRowCount(0);
    }

    /**
     * Actualiza la tabla de resultados y la ganancia total en función de la capacidad ingresada.
     * Obtiene los parámetros del modelo, ejecuta el cálculo y muestra los resultados en la tabla.
     * Si hay un error en los datos, limpia la tabla y muestra '-'.
     */
    private void actualizarResultados() {
        try {
            ControladorParametros params = ControladorParametros.getInstancia();
            double tasaDescuento = params.getTasaDescuento();
            // La capacidad ya no se usa, se pasa 0 solo por compatibilidad de firma
            ModeloSoftwareCalculo.ResultadoComparativo resultados = ModeloSoftwareCalculo.calcularComparativo(params, 0, tasaDescuento);

            // Limpiamos la tabla y llenamos con nuevos datos
            modeloTabla.setRowCount(0);
            for (int i = 0; i < resultados.resultadosCon.length; i++) {
                ModeloSoftwareCalculo.ResultadoAnual rCon = resultados.resultadosCon[i];
                ModeloSoftwareCalculo.ResultadoAnual rSin = resultados.resultadosSin[i];

                // Agregamos fila con los datos formateados usando el método formatearMoneda
                modeloTabla.addRow(new Object[] {
                    rCon.anio,
                    rCon.demanda,
                    rSin.unidadesProducidas,
                    formatearMoneda(rSin.costoVariableProduccion),
                    formatearMoneda(rSin.ingresosVentas),
                    rCon.unidadesProducidas,
                    formatearMoneda(rCon.costoVariableProduccion),
                    formatearMoneda(rCon.ingresosVentas)
                });
            }

            // Actualizamos las etiquetas de VAN
            actualizarEtiquetasVAN(resultados.vanCon, resultados.vanSin, resultados.diferenciaVAN);

            // Actualizamos el label de la tasa de descuento
            lblTasaDescuentoValor.setText(String.format("%.0f%%", tasaDescuento * 100));
        } catch (Exception ex) {
            ex.printStackTrace(); // Para depuración
            limpiarResultados();
        }
    }

    /**
     * Implementación del método requerido por la interfaz ControladorParametros.ParametrosChangeListener.
     * Este método se llama automáticamente cuando hay cambios en los parámetros.
     */
    @Override
    public void onParametrosChanged() {
        // Cuando cambian los parámetros, actualizamos los resultados
        SwingUtilities.invokeLater(this::actualizarResultados);
    }

    /**
     * Método que se llama cuando este panel se elimina del contenedor padre.
     * Nos desregistramos como oyente para evitar memory leaks.
     */
    @Override
    public void removeNotify() {
        // Nos desregistramos como oyente de cambios
        ControladorParametros.getInstancia().removeChangeListener(this);
        super.removeNotify();
    }
}
