package actividad_4.ejercicio_1;

public class ModeloSoftwareCalculo {
    public static class ResultadoAnual {
        public int anio;
        public double demanda; // tamaño de mercado anual (cambiado a double para precisión)
        public double inversionInicial; // inversión ocurrida en este año (sólo año 1)
        public double costoFijoOperacion; // costo operativo anual asociado a la capacidad
        public double unidadesProducidas; // unidades vendidas (limitadas por capacidad, cambiado a double)
        public double ingresosVentas; // cambiado a double para mantener precisión de dos decimales
        public double costoVariableProduccion; // cambiado a double para mantener precisión de dos decimales
        public double utilidad; // cambiado a double para mantener precisión de dos decimales
    }

    /**
     * Firma antigua compatible: calcula resultados usando crecimiento constante.
     * @param capacidad capacidad instalada (unidades)
     * @param demandaInicial tamaño de mercado inicial
     * @param crecimientoAnual crecimiento anual (por ejemplo 0.10 para 10%)
     * @param costoCapacidadUnitaria costo de inversión por unidad de capacidad
     * @param precioVentaUnitario precio de venta por unidad
     * @param costoVariableUnitario coste variable por unidad
     * @param costoOperativoUnitario coste operativo anual por unidad de capacidad
     */
    public static ResultadoAnual[] calcularModelo(int capacidad, int demandaInicial, double crecimientoAnual,
                                                  double costoCapacidadUnitaria, double precioVentaUnitario,
                                                  double costoVariableUnitario, double costoOperativoUnitario) {
        int H = 10;
        ResultadoAnual[] res = new ResultadoAnual[H];
        double market = demandaInicial;

        for (int i = 0; i < H; i++) {
            ResultadoAnual r = new ResultadoAnual();
            r.anio = i + 1;
            r.demanda = UtilidadesFormato.redondearDosDecimales(market); // Cambiado a double

            // inversión inicial sólo en el año 1
            r.inversionInicial = (i == 0) ? UtilidadesFormato.redondearDosDecimales(capacidad * costoCapacidadUnitaria) : 0.0;
            // costo operativo anual (se aplica cada año)
            r.costoFijoOperacion = UtilidadesFormato.redondearDosDecimales(capacidad * costoOperativoUnitario);

            // unidades producidas limitadas por capacidad y demanda
            r.unidadesProducidas = Math.min(capacidad, r.demanda); // Mantener como double

            r.ingresosVentas = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * precioVentaUnitario);
            r.costoVariableProduccion = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * costoVariableUnitario);
            // utilidad = ingresos - coste variable - costo fijo operativo
            r.utilidad = UtilidadesFormato.redondearDosDecimales(r.ingresosVentas - r.costoVariableProduccion - r.costoFijoOperacion);

            res[i] = r;

            market = UtilidadesFormato.redondearDosDecimales(market * (1.0 + crecimientoAnual));
        }

        return res;
    }
    public static ResultadoAnual[] calcularModelo(ControladorParametros params, int capacidad) {
        int H = params.getHorizonteAnios();
        ResultadoAnual[] res = new ResultadoAnual[H];
        double market = params.getTamanoActualMercado();
        double g1 = params.getCrecimientoPrimeros5();
        double g2 = params.getCrecimientoProximos5();

        for (int i = 0; i < H; i++) {
            ResultadoAnual r = new ResultadoAnual();
            r.anio = i + 1;
            r.demanda = UtilidadesFormato.redondearDosDecimales(market); // Cambiado a double

            r.inversionInicial = 0.0; // inversión gestionada externamente
            r.costoFijoOperacion = 0.0; // asumimos 0 para evitar doble contabilidad

            double demandaCon = UtilidadesFormato.redondearDosDecimales(r.demanda * params.getCuotaMercadoConNuevaVersion());
            // double demandaSin = r.demanda * params.getCuotaMercadoVersionIngles(); // no usada

            r.unidadesProducidas = Math.min(capacidad, demandaCon); // Mantener como double
            r.ingresosVentas = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * params.getPrecioVentaUnitario());
            r.costoVariableProduccion = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * params.getCosteVariableUnitario());
            r.utilidad = UtilidadesFormato.redondearDosDecimales(r.ingresosVentas - r.costoVariableProduccion);

            res[i] = r;

            if (i < 5)
                market = UtilidadesFormato.redondearDosDecimales(market * (1.0 + g1));
            else
                market = UtilidadesFormato.redondearDosDecimales(market * (1.0 + g2));
        }

        return res;
    }
    public static double calcularGananciaTotal(int capacidad, int demandaInicial, double crecimientoAnual,
                                               double costoCapacidadUnitaria, double precioVentaUnitario,
                                               double costoVariableUnitario, double costoOperativoUnitario) {
        ResultadoAnual[] res = calcularModelo(capacidad, demandaInicial, crecimientoAnual,
                costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        double suma = 0;
        for (ResultadoAnual r : res) suma += r.utilidad;
        return UtilidadesFormato.redondearDosDecimales(suma);
    }
    public static double calcularVAN(int capacidad, int demandaInicial, double crecimientoAnual,
                                     double costoCapacidadUnitaria, double precioVentaUnitario,
                                     double costoVariableUnitario, double costoOperativoUnitario,
                                     double tasaDescuento) {
        ResultadoAnual[] res = calcularModelo(capacidad, demandaInicial, crecimientoAnual,
                costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
        double van = 0.0;
        // inversión inicial (t=0)
        if (res.length > 0) van -= res[0].inversionInicial;
        for (int i = 0; i < res.length; i++) {
            van += res[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1); // Sin redondeo intermedio
        }
        return UtilidadesFormato.redondearDosDecimales(van); // Redondear al final
    }

    public static class ResultadoComparativo {
        public ResultadoAnual[] resultadosCon;
        public ResultadoAnual[] resultadosSin;
        public double vanCon;
        public double vanSin;
        public double diferenciaVAN;
    }

    public static ResultadoComparativo calcularComparativo(ControladorParametros params, int capacidad, double tasaDescuento) {
        int H = params.getHorizonteAnios();
        ResultadoAnual[] resCon = new ResultadoAnual[H];
        ResultadoAnual[] resSin = new ResultadoAnual[H];
        double demandaInicial = params.getTamanoActualMercado();
        double cuotaCon = params.getCuotaMercadoConNuevaVersion();
        double cuotaSin = params.getCuotaMercadoVersionIngles();
        double precio = params.getPrecioVentaUnitario();
        double costeVar = params.getCosteVariableUnitario();
        double costeFijo = params.getCosteFijoCrearVersion();
        double g1 = params.getCrecimientoPrimeros5();
        double g2 = params.getCrecimientoProximos5();

        for (int i = 0; i < H; i++) {
            // Calcular tamaño de mercado compuesto por tramos
            int n = i + 1;
            int tramo1 = Math.min(n, 5);
            int tramo2 = Math.max(0, n - 5);
            double marketYear = demandaInicial;
            marketYear = marketYear * Math.pow(1.0 + g1, tramo1); // Evitar redondeo intermedio innecesario
            marketYear = marketYear * Math.pow(1.0 + g2, tramo2);
            // No redondear a int: mantener como double

            // Crear y configurar resultados anuales
            resCon[i] = crearResultadoAnual(n, marketYear, cuotaCon, precio, costeVar, (i == 0) ? costeFijo : 0.0);
            resSin[i] = crearResultadoAnual(n, marketYear, cuotaSin, precio, costeVar, 0.0);
        }

        // Calcular VAN para ambos escenarios
        double vanCon = calcularVAN(resCon, tasaDescuento);
        double vanSin = calcularVAN(resSin, tasaDescuento);

        ResultadoComparativo resultado = new ResultadoComparativo();
        resultado.resultadosCon = resCon;
        resultado.resultadosSin = resSin;
        resultado.vanCon = UtilidadesFormato.redondearDosDecimales(vanCon);
        resultado.vanSin = UtilidadesFormato.redondearDosDecimales(vanSin);
        resultado.diferenciaVAN = UtilidadesFormato.redondearDosDecimales(vanCon - vanSin);
        return resultado;
    }

    private static ResultadoAnual crearResultadoAnual(int anio, double demanda, double cuota,
                                                      double precio, double costeVar, double inversionInicial) {
        ResultadoAnual r = new ResultadoAnual();
        r.anio = anio;
        r.demanda = UtilidadesFormato.redondearDosDecimales(demanda);
        r.inversionInicial = UtilidadesFormato.redondearDosDecimales(inversionInicial);
        r.costoFijoOperacion = 0.0;

        double units = demanda * cuota; // Mantener como double, sin redondeo a int
        r.unidadesProducidas = UtilidadesFormato.redondearDosDecimales(units);
        r.ingresosVentas = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * precio);
        r.costoVariableProduccion = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * costeVar);
        r.utilidad = UtilidadesFormato.redondearDosDecimales(r.ingresosVentas - r.costoVariableProduccion);

        return r;
    }

    private static double calcularVAN(ResultadoAnual[] resultados, double tasaDescuento) {
        double van = 0.0;
        for (int i = 0; i < resultados.length; i++) {
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1); // Sin redondeo intermedio
        }
        if (resultados.length > 0) {
            van -= resultados[0].inversionInicial;
        }
        return UtilidadesFormato.redondearDosDecimales(van); // Redondear solo al final
    }
}