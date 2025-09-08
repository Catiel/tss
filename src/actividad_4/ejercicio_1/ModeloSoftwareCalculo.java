package actividad_4.ejercicio_1;

public class ModeloSoftwareCalculo {
    public static class ResultadoAnual {
        public int anio;
        public double demanda; // tamaño de mercado anual
        public double inversionInicial; // inversión ocurrida en este año (ej. año 1 escenario con)
        public double unidadesProducidas; // unidades vendidas
        public double ingresosVentas;
        public double costoVariableProduccion;
        public double utilidad;
    }

    public static class ResultadoComparativo {
        public ResultadoAnual[] resultadosCon;
        public ResultadoAnual[] resultadosSin;
        public double vanCon;
        public double vanSin;
        public double diferenciaVAN;
    }

    public static ResultadoComparativo calcularComparativo(ControladorParametros params, double tasaDescuento) {
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
            int n = i + 1;
            int tramo1 = Math.min(n, 5);
            int tramo2 = Math.max(0, n - 5);
            double marketYear = demandaInicial;
            marketYear = marketYear * Math.pow(1.0 + g1, tramo1);
            marketYear = marketYear * Math.pow(1.0 + g2, tramo2);

            resCon[i] = crearResultadoAnual(n, marketYear, cuotaCon, precio, costeVar, (i == 0) ? costeFijo : 0.0);
            resSin[i] = crearResultadoAnual(n, marketYear, cuotaSin, precio, costeVar, 0.0);
        }

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
        double units = demanda * cuota;
        r.unidadesProducidas = UtilidadesFormato.redondearDosDecimales(units);
        r.ingresosVentas = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * precio);
        r.costoVariableProduccion = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * costeVar);
        r.utilidad = UtilidadesFormato.redondearDosDecimales(r.ingresosVentas - r.costoVariableProduccion);
        return r;
    }

    private static double calcularVAN(ResultadoAnual[] resultados, double tasaDescuento) {
        double van = 0.0;
        for (int i = 0; i < resultados.length; i++) {
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
        }
        if (resultados.length > 0) {
            van -= resultados[0].inversionInicial;
        }
        return UtilidadesFormato.redondearDosDecimales(van);
    }
}