package actividad_3.ejercicio_2;

import javax.swing.table.DefaultTableModel;

public class PanelTablaAuto extends TablaEstilizadaPanel {
    public PanelTablaAuto() {
        super(
            "Tabla automática de beneficio total versus unidades de capacidad",
            new DefaultTableModel(new String[]{"Capacidad", "Ganancia ($)", "VAN ($)"}, 0) {
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
        filaOptimaVan = -1;
        ControladorParametros params = ControladorParametros.getInstancia();
        double mejorGanancia = Double.NEGATIVE_INFINITY;
        int mejorCapacidad = 0;
        int mejorCapacidadVan = 0;
        double mejorVan = Double.NEGATIVE_INFINITY;
        int filaVan = -1;
        double tasaDescuento = ControladorParametros.getInstancia().getTasaDescuento();
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
            int filaActual = modeloTabla.getRowCount() - 1;
            if (ganancia > mejorGanancia) {
                mejorGanancia = ganancia;
                mejorCapacidad = capacidad;
                filaOptima = filaActual;
            }
            if (van > mejorVan) {
                mejorVan = van;
                mejorCapacidadVan = capacidad;
                filaVan = filaActual;
            }
        }
        actualizarOptimo(mejorCapacidad, mejorGanancia, filaOptima, mejorCapacidadVan, mejorVan, filaVan);
    }

    private double calcularVAN(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario, double tasaDescuento) {
        double van = 0;
        ModeloWozacCalculo.ResultadoAnual[] resultados = ModeloWozacCalculo.calcularModelo(capacidad, demandaInicial, crecimientoAnual, costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        for (int i = 0; i < resultados.length; i++) {
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
        }
        return van;
    }
}
