package actividad_3.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelTablaAuto extends TablaEstilizadaPanel {
    /**
     * Constructor del panel automático de precios y ganancias.
     * Inicializa la tabla y genera los resultados automáticamente para precios entre 44 y 100 (paso 1).
     */
    public PanelTablaAuto() {
        super(
            "Tabla automática de precios y ganancias (44 a 100, paso 1)",
            new DefaultTableModel(new String[]{"Precio (libras)", "Ganancia ($)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            },
            null,
            null
        );
        generarTabla();
    }

    /**
     * Genera la tabla de resultados para precios entre 44 y 100 (paso 1).
     * Calcula la ganancia para cada precio y determina la fila óptima.
     * Actualiza la tabla y los indicadores óptimos en la interfaz.
     */
    public void generarTabla() {
        modeloTabla.setRowCount(0);
        filaOptima = -1;
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        double mejorPrecio = 0;
        for (int precio = 44; precio <= 100; precio++) {
            double demanda = params.getConstanteDemanda() * Math.pow(precio, params.getElasticidad());
            double ingresos = demanda * precio * params.getTipoCambio();
            double costeTotal = demanda * params.getCosteUnitario();
            double ganancia = ingresos - costeTotal;
            modeloTabla.addRow(new Object[]{String.format("%.2f", (double)precio), String.format("%.2f", ganancia)});
            if (ganancia > mejorGanancia) {
                mejorGanancia = ganancia;
                mejorPrecio = precio;
                filaOptima = modeloTabla.getRowCount() - 1;
            }
        }
        actualizarOptimo(mejorPrecio, mejorGanancia, filaOptima);
    }
}
