package actividad_4.ejercicio_1; // Paquete del ejercicio 1

public class ModeloSoftwareCalculo { // Clase que encapsula la lógica de proyección y VAN comparativo
    public static class ResultadoAnual { // Estructura para almacenar métricas por año
        public int anio;                       // Año (1..H)
        public double demanda;                // Tamaño de mercado total proyectado ese año
        public double inversionInicial;       // Inversión imputada ese año (sólo año 1 en escenario "con")
        public double unidadesProducidas;     // Unidades (ventas) = demanda * cuota de mercado
        public double ingresosVentas;         // Ingresos = unidades * precio unitario
        public double costoVariableProduccion;// Coste variable = unidades * coste variable unitario
        public double utilidad;               // Utilidad (contribución) = ingresos - coste variable
    }

    public static class ResultadoComparativo { // Contenedor del resultado completo para ambos escenarios
        public ResultadoAnual[] resultadosCon; // Vector anual con NUEVA versión
        public ResultadoAnual[] resultadosSin; // Vector anual SIN nueva versión
        public double vanCon;                 // VAN del escenario con nueva versión
        public double vanSin;                 // VAN del escenario sin nueva versión
        public double diferenciaVAN;          // Diferencia: vanCon - vanSin
    }

    public static ResultadoComparativo calcularComparativo(ControladorParametros params, double tasaDescuento) { // Método principal de comparación
        int H = params.getHorizonteAnios();                       // Horizonte de años
        ResultadoAnual[] resCon = new ResultadoAnual[H];          // Array de resultados escenario "con"
        ResultadoAnual[] resSin = new ResultadoAnual[H];          // Array de resultados escenario "sin"
        double demandaInicial = params.getTamanoActualMercado();  // Demanda (mercado) base año 0
        double cuotaCon = params.getCuotaMercadoConNuevaVersion();// Cuota esperada con versión francesa
        double cuotaSin = params.getCuotaMercadoVersionIngles();  // Cuota mantenida sin nueva versión
        double precio = params.getPrecioVentaUnitario();          // Precio de venta unitario
        double costeVar = params.getCosteVariableUnitario();      // Coste variable unitario
        double costeFijo = params.getCosteFijoCrearVersion();     // Inversión (coste fijo) solo año 1 (escenario con)
        double g1 = params.getCrecimientoPrimeros5();             // Crecimiento anual para primeros 5 años
        double g2 = params.getCrecimientoProximos5();             // Crecimiento anual para años 6..H

        for (int i = 0; i < H; i++) {                            // Bucle por cada año
            int n = i + 1;                                       // Año calendario (1-based)
            int tramo1 = Math.min(n, 5);                         // Años que aplican crecimiento g1
            int tramo2 = Math.max(0, n - 5);                     // Años (más allá de 5) que aplican crecimiento g2
            double marketYear = demandaInicial;                  // Parte de demanda base
            marketYear = marketYear * Math.pow(1.0 + g1, tramo1);// Aplica crecimiento g1 tramo1 veces
            marketYear = marketYear * Math.pow(1.0 + g2, tramo2);// Aplica crecimiento g2 tramo2 veces

            resCon[i] = crearResultadoAnual(                     // Construye registro año escenario con
                n,                                               // Año
                marketYear,                                      // Demanda total proyectada
                cuotaCon,                                        // Cuota con nueva versión
                precio,                                          // Precio unitario
                costeVar,                                        // Coste variable unitario
                (i == 0) ? costeFijo : 0.0                       // Inversión sólo en el primer año
            );
            resSin[i] = crearResultadoAnual(                     // Construye registro año escenario sin
                n,                                               // Año
                marketYear,                                      // Demanda idéntica (mercado común)
                cuotaSin,                                        // Cuota sin nueva versión
                precio,                                          // Precio unitario
                costeVar,                                        // Coste variable unitario
                0.0                                              // Sin inversión adicional
            );
        }

        double vanCon = calcularVAN(resCon, tasaDescuento);      // VAN escenario con
        double vanSin = calcularVAN(resSin, tasaDescuento);      // VAN escenario sin

        ResultadoComparativo resultado = new ResultadoComparativo(); // Estructura agregada
        resultado.resultadosCon = resCon;                        // Asigna vector con
        resultado.resultadosSin = resSin;                        // Asigna vector sin
        resultado.vanCon = UtilidadesFormato.redondearDosDecimales(vanCon); // Redondea VAN con
        resultado.vanSin = UtilidadesFormato.redondearDosDecimales(vanSin); // Redondea VAN sin
        resultado.diferenciaVAN = UtilidadesFormato.redondearDosDecimales(vanCon - vanSin); // Diferencia redondeada
        return resultado;                                        // Devuelve resultado completo
    }

    private static ResultadoAnual crearResultadoAnual(int anio, double demanda, double cuota, // Factory para registro anual
                                                      double precio, double costeVar, double inversionInicial) {
        ResultadoAnual r = new ResultadoAnual();                // Instancia contenedor anual
        r.anio = anio;                                          // Asigna año
        r.demanda = UtilidadesFormato.redondearDosDecimales(demanda);            // Demanda redondeada
        r.inversionInicial = UtilidadesFormato.redondearDosDecimales(inversionInicial); // Inversión redondeada
        double units = demanda * cuota;                         // Unidades = demanda * cuota
        r.unidadesProducidas = UtilidadesFormato.redondearDosDecimales(units);   // Unidades redondeadas
        r.ingresosVentas = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * precio); // Ingresos redondeados
        r.costoVariableProduccion = UtilidadesFormato.redondearDosDecimales(r.unidadesProducidas * costeVar); // Coste variable
        r.utilidad = UtilidadesFormato.redondearDosDecimales(r.ingresosVentas - r.costoVariableProduccion);   // Margen contribución
        return r;                                               // Devuelve registro armado
    }

    private static double calcularVAN(ResultadoAnual[] resultados, double tasaDescuento) { // Calcula VAN sencillo
        double van = 0.0;                                       // Acumulador VAN
        for (int i = 0; i < resultados.length; i++) {           // Recorre años
            van += resultados[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1); // Descuenta utilidad del año i
        }
        if (resultados.length > 0) {                            // Si hay al menos un año
            van -= resultados[0].inversionInicial;              // Resta inversión inicial (flujo de salida año 1)
        }
        return UtilidadesFormato.redondearDosDecimales(van);    // Devuelve VAN redondeado
    }
}