package actividad_3.ejercicio_2;

public class ModeloWozacCalculo {
    public static class ResultadoAnual {
        public int anio;

        public int demanda;
        public double inversionInicial;
        public double costoFijoOperacion;
        public int unidadesProducidas;
        public double ingresosVentas;
        public double costoVariableProduccion;
        public double utilidad;
    }

    public static ResultadoAnual[] calcularModelo(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario) {
        ResultadoAnual[] resultados = new ResultadoAnual[10];
        int demanda = demandaInicial;
        boolean invRealizada = false;
        for (int i = 0; i < 10; i++) {
            ResultadoAnual r = new ResultadoAnual();
            r.anio = i + 1;
            r.demanda = demanda;
            r.inversionInicial = (!invRealizada ? capacidad * costoCapacidadUnitaria : 0);
            r.costoFijoOperacion = capacidad * costoOperativoUnitario;
            r.unidadesProducidas = Math.min(demanda, capacidad);
            r.ingresosVentas = r.unidadesProducidas * precioVentaUnitario;
            r.costoVariableProduccion = r.unidadesProducidas * costoVariableUnitario;
            r.utilidad = r.ingresosVentas - r.costoVariableProduccion - r.costoFijoOperacion - r.inversionInicial;
            resultados[i] = r;
            demanda = (int)Math.round(demanda * (1.0 + crecimientoAnual));
            invRealizada = true;
        }
        return resultados;
    }

    public static double calcularGananciaTotal(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario) {
        ResultadoAnual[] resultados = calcularModelo(capacidad, demandaInicial, crecimientoAnual, costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        double total = 0;
        for (ResultadoAnual r : resultados) {
            total += r.utilidad;
        }
        return total;
    }
}

