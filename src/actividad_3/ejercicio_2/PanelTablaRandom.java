package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Random;

public class PanelTablaRandom extends TablaEstilizadaPanel {
    private final JTextField txtMin;
    private final JTextField txtMax;
    private final JTextField txtFilas;

    /**
     * Constructor del panel aleatorio.
     * Inicializa la interfaz y los controles para generar una tabla de capacidades aleatorias.
     * Permite al usuario definir el rango y la cantidad de capacidades a analizar.
     */
    public PanelTablaRandom() {
        super(
            "Tabla aleatoria de beneficio total versus unidades de capacidad",
            new DefaultTableModel(new String[]{"Capacidad", "Ganancia ($)", "VAN ($)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            },
            null,
            null
        );
        PanelSuperiorRandom panelSup = crearPanelSuperior();
        add(panelSup.panel, BorderLayout.BEFORE_FIRST_LINE);
        txtMin = panelSup.txtMin;
        txtMax = panelSup.txtMax;
        txtFilas = panelSup.txtFilas;
        JButton btnGenerar = panelSup.btnGenerar;
        btnGenerar.addActionListener(e -> generarTabla());
    }

    /**
     * Clase interna que agrupa los controles del panel superior para ingresar parámetros aleatorios.
     */
    private static class PanelSuperiorRandom {
        JPanel panel;
        JTextField txtMin, txtMax, txtFilas;
        JButton btnGenerar;
        PanelSuperiorRandom(JPanel panel, JTextField txtMin, JTextField txtMax, JTextField txtFilas, JButton btnGenerar) {
            this.panel = panel;
            this.txtMin = txtMin;
            this.txtMax = txtMax;
            this.txtFilas = txtFilas;
            this.btnGenerar = btnGenerar;
        }
    }

    /**
     * Crea el panel superior con controles para definir el rango y la cantidad de capacidades aleatorias.
     * @return PanelSuperiorRandom con los campos y botón para generar la tabla.
     */
    private static PanelSuperiorRandom crearPanelSuperior() {
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 18, 12));
        EstilosUI.aplicarEstiloPanel(panelTop);
        JLabel lblTitulo = new JLabel("Tabla aleatoria de beneficio total versus unidades de capacidad");
        EstilosUI.aplicarEstiloTitulo(lblTitulo);
        panelTop.add(lblTitulo);
        JLabel lblMin = new JLabel("Capacidad mínima:");
        EstilosUI.aplicarEstiloLabel(lblMin);
        JTextField txtMin = new JTextField("40000", 6);
        txtMin.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblMin);
        panelTop.add(txtMin);
        JLabel lblMax = new JLabel("Capacidad máxima:");
        EstilosUI.aplicarEstiloLabel(lblMax);
        JTextField txtMax = new JTextField("80000", 6);
        txtMax.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblMax);
        panelTop.add(txtMax);
        JLabel lblFilas = new JLabel("Cantidad de filas:");
        EstilosUI.aplicarEstiloLabel(lblFilas);
        JTextField txtFilas = new JTextField("8", 4);
        txtFilas.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        panelTop.add(lblFilas);
        panelTop.add(txtFilas);
        JButton btnGenerar = new JButton("Generar tabla");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        panelTop.add(btnGenerar);
        return new PanelSuperiorRandom(panelTop, txtMin, txtMax, txtFilas, btnGenerar);
    }

    /**
     * Genera la tabla de resultados con capacidades aleatorias dentro del rango definido.
     * Calcula la ganancia total y el VAN para cada capacidad, y determina las filas óptimas.
     * Actualiza la tabla y los indicadores óptimos en la interfaz.
     * Si los datos ingresados son inválidos, muestra un mensaje de error y limpia la tabla.
     */
    private void generarTabla() {
        try {
            int min = Integer.parseInt(txtMin.getText());
            int max = Integer.parseInt(txtMax.getText());
            int filas = Integer.parseInt(txtFilas.getText());
            if (min > max || filas < 1) throw new Exception();
            modeloTabla.setRowCount(0);
            filaOptima = -1;
            filaOptimaVan = -1;
            ControladorParametros params = ControladorParametros.getInstancia();
            double mejorGanancia = Double.NEGATIVE_INFINITY;
            int mejorCapacidad = 0;
            double mejorVan = Double.NEGATIVE_INFINITY;
            int mejorCapacidadVan = 0;
            int filaVan = -1;
            double tasaDescuento = params.getTasaDescuento();
            Random rand = new Random();
            for (int i = 0; i < filas; i++) {
                int capacidad = min + rand.nextInt(max - min + 1);
                double ganancia = ModeloWozacCalculo.calcularGananciaTotal(
                    capacidad,
                    params.getDemandaInicial(),
                    params.getCrecimientoAnual(),
                    params.getCostoCapacidadUnitaria(),
                    params.getPrecioVentaUnitario(),
                    params.getCostoVariableUnitario(),
                    params.getCostoOperativoUnitario()
                );
                double van = calcularVAN(
                    capacidad,
                    params.getDemandaInicial(),
                    params.getCrecimientoAnual(),
                    params.getCostoCapacidadUnitaria(),
                    params.getPrecioVentaUnitario(),
                    params.getCostoVariableUnitario(),
                    params.getCostoOperativoUnitario(),
                    tasaDescuento
                );
                modeloTabla.addRow(new Object[]{capacidad, String.format("$%,.0f", ganancia), String.format("$%,.0f", van)});
                if (ganancia > mejorGanancia) {
                    mejorGanancia = ganancia;
                    mejorCapacidad = capacidad;
                    filaOptima = i;
                }
                if (van > mejorVan) {
                    mejorVan = van;
                    mejorCapacidadVan = capacidad;
                    filaVan = i;
                }
            }
            actualizarOptimo(mejorCapacidad, mejorGanancia, filaOptima, mejorCapacidadVan, mejorVan, filaVan);
        } catch (Exception ex) {
            modeloTabla.setRowCount(0);
            lblOptimo.setText("Datos inválidos");
        }
    }

    /**
     * Calcula el Valor Actual Neto (VAN) para una capacidad dada usando los resultados anuales.
     * @param capacidad Capacidad instalada de producción.
     * @param demandaInicial Demanda inicial.
     * @param crecimientoAnual Crecimiento anual de la demanda.
     * @param costoCapacidadUnitaria Costo de inversión por unidad de capacidad.
     * @param precioVentaUnitario Precio de venta por unidad producida.
     * @param costoVariableUnitario Costo variable por unidad producida.
     * @param costoOperativoUnitario Costo fijo anual por unidad de capacidad instalada.
     * @param tasaDescuento Tasa de descuento anual para el cálculo de VAN.
     * @return VAN calculado para los 10 años.
     */
    private double calcularVAN(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario, double tasaDescuento) {
        double van = 0;
        ModeloWozacCalculo.ResultadoAnual[] resultados = ModeloWozacCalculo.calcularModelo(capacidad, demandaInicial, crecimientoAnual, costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        for (int i = 0; i < resultados.length; i++) {
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
        }
        return van;
    }
}
