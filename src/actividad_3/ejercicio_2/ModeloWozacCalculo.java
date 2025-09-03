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

    /**
     * Calcula los resultados anuales del modelo financiero de Wozac para 10 años.
     * @param capacidad Capacidad instalada de producción.
     * @param demandaInicial Demanda estimada para el primer año.
     * @param crecimientoAnual Crecimiento porcentual anual de la demanda (ejemplo: 0.05 para 5%).
     * @param costoCapacidadUnitaria Costo de inversión por unidad de capacidad.
     * @param precioVentaUnitario Precio de venta por unidad producida.
     * @param costoVariableUnitario Costo variable por unidad producida.
     * @param costoOperativoUnitario Costo fijo anual por unidad de capacidad instalada.
     * @return Array con los resultados de cada año (demanda, inversión, costos, utilidad, etc.).
     *
     * La inversión inicial solo se realiza el primer año. La demanda crece cada año según el parámetro de crecimiento.
     * La producción está limitada por la capacidad instalada. La utilidad anual considera todos los costos e ingresos.
     */
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

    /**
     * Calcula la ganancia total sumando las utilidades de los 10 años del modelo.
     * @param capacidad Capacidad instalada de producción.
     * @param demandaInicial Demanda estimada para el primer año.
     * @param crecimientoAnual Crecimiento porcentual anual de la demanda.
     * @param costoCapacidadUnitaria Costo de inversión por unidad de capacidad.
     * @param precioVentaUnitario Precio de venta por unidad producida.
     * @param costoVariableUnitario Costo variable por unidad producida.
     * @param costoOperativoUnitario Costo fijo anual por unidad de capacidad instalada.
     * @return Ganancia total acumulada en los 10 años.
     *
     * Utiliza el método calcularModelo para obtener los resultados anuales y suma las utilidades.
     */
    public static double calcularGananciaTotal(int capacidad, int demandaInicial, double crecimientoAnual, double costoCapacidadUnitaria, double precioVentaUnitario, double costoVariableUnitario, double costoOperativoUnitario) {
        ResultadoAnual[] resultados = calcularModelo(capacidad, demandaInicial, crecimientoAnual, costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        double total = 0;
        for (ResultadoAnual r : resultados) {
            total += r.utilidad;
        }
        return total;
    }
}
