package actividad_3.ejercicio_1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelTablaAuto extends TablaEstilizadaPanel {
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
