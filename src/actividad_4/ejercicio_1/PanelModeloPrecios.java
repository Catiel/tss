package actividad_4.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelModeloPrecios extends JPanel {
    private final JLabel lblVANCon;
    private final JLabel lblVANSin;
    private final JLabel lblDiferenciaVAN;
    private final DefaultTableModel modeloTabla;

    /**
     * Constructor. Inicializa la interfaz del panel para el modelo de decisión de capacidad y utilidades esperadas.
     * Configura los componentes visuales y los listeners para actualizar los resultados automáticamente.
     */
    public PanelModeloPrecios() {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 18, 12, 18);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridy = 0;

        // Título
        JLabel titulo = new JLabel("Comparación de beneficios y VAN - Versión francesa del software");
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

        // Tabla de resultados
        gbc.gridy++;
        gbc.gridx = 0; gbc.gridwidth = 8;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        String[] columnas = {"Año", "Tamaño mercado", "Unidades vendidas SIN", "Utilidad SIN", "Unidades vendidas CON", "Utilidad CON", "Diferencia utilidad"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        JTable tablaResultados = new JTable(modeloTabla);
        EstilosUI.aplicarEstiloTabla(tablaResultados);
        int[] anchos = {60, 120, 120, 120, 120, 120, 120};
        for (int i = 0; i < anchos.length; i++) {
            tablaResultados.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }
        JScrollPane scroll = new JScrollPane(tablaResultados, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        tablaResultados.setFillsViewportHeight(true);
        add(scroll, gbc);

        actualizarResultados();
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
            modeloTabla.setRowCount(0);
            for (int i = 0; i < resultados.resultadosCon.length; i++) {
                ModeloSoftwareCalculo.ResultadoAnual rCon = resultados.resultadosCon[i];
                ModeloSoftwareCalculo.ResultadoAnual rSin = resultados.resultadosSin[i];
                modeloTabla.addRow(new Object[] {
                    rCon.anio,
                    rCon.demanda,
                    (int)rSin.unidadesProducidas,
                    String.format("$%,.0f", rSin.utilidad),
                    (int)rCon.unidadesProducidas,
                    String.format("$%,.0f", rCon.utilidad),
                    String.format("$%,.0f", rCon.utilidad - rSin.utilidad)
                });
            }
            lblVANCon.setText(String.format("$%,.0f", resultados.vanCon));
            lblVANSin.setText(String.format("$%,.0f", resultados.vanSin));
            lblDiferenciaVAN.setText(String.format("$%,.0f", resultados.diferenciaVAN));
        } catch (Exception ex) {
            lblVANCon.setText("-");
            lblVANSin.setText("-");
            lblDiferenciaVAN.setText("-");
            modeloTabla.setRowCount(0);
        }
    }

}
