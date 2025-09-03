package actividad_3.ejercicio_2;

import javax.swing.table.DefaultTableModel;

public class PanelTablaAuto extends TablaEstilizadaPanel {
    public PanelTablaAuto() {
        super(
            "Tabla automática de beneficio total versus unidades de capacidad",
            new DefaultTableModel(new String[]{"Capacidad", "Ganancia ($)"}, 0) {
                @Override
                public boolean isCellEditable(int row, int col) { return false; }
            },
            null,
            null
        );
        generarTabla();
    }

    private void generarTabla() {
        modeloTabla.setRowCount(0);
        filaOptima = -1;
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        int mejorCapacidad = 0;
        // Capacidades típicas: 40000 a 80000 en pasos de 5000
        for (int capacidad = 40000; capacidad <= 80000; capacidad += 5000) {
            double ganancia = ModeloWozacCalculo.calcularGananciaTotal(
                capacidad,
                params.getDemandaInicial(),
                params.getCrecimientoAnual(),
                params.getCostoCapacidadUnitaria(),
                params.getPrecioVentaUnitario(),
                params.getCostoVariableUnitario(),
                params.getCostoOperativoUnitario()
            );
            modeloTabla.addRow(new Object[]{capacidad, String.format("$%,.0f", ganancia)});
            if (ganancia > mejorGanancia) {
                mejorGanancia = ganancia;
                mejorCapacidad = capacidad;
                filaOptima = modeloTabla.getRowCount() - 1;
            }
        }
        actualizarOptimo(mejorCapacidad, mejorGanancia, filaOptima);
    }
}
