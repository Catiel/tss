package actividad_4.ejercicio_1;

/**
 * Adaptador / implementación del "modelo software" que expone una API compatible
 * con las llamadas antiguas del proyecto pero calcula los flujos usando la
 * lógica del Excel (crecimientos por tramos, cuotas, precio y coste variable).
 * <p>
 * Notas / asunciones razonables tomadas al implementar:
 * - Horizonte por defecto 10 años salvo que se indique en los parámetros.
 * - Cuando se usa la firma antigua con un único crecimiento, ese crecimiento
 *   se aplica todos los años; cuando se usa la versión con ControladorParametros
 *   se aplican crecimientoPrimeros5 y crecimientoProximos5 (años 1-5 y 6-10).
 * - "Inversión inicial" se interpreta como el coste de instalar la capacidad
 *   (capacidad * costoCapacidadUnitaria) y se registra en el año 1.
 * - "Costo fijo operación" se interpreta como un costo anual ligado a la
 *   capacidad (capacidad * costoOperativoUnitario) y se aplica cada año.
 * - Unidades producidas = min(capacidad, demanda anual) — demanda es tamaño de
 *   mercado (no se aplica cuota en la firma antigua). Cuando se usa
 *   ControladorParametros se podrá calcular unidades sin/con versión usando
 *   las cuotas disponibles.
 */

public class ModeloSoftwareCalculo {
	public static class ResultadoAnual {
		public int anio;
		public int demanda; // tamaño de mercado anual
		public double inversionInicial; // inversión ocurrida en este año (sólo año 1)
		public double costoFijoOperacion; // costo operativo anual asociado a la capacidad
		public int unidadesProducidas; // unidades vendidas (limitadas por capacidad)
		public int ingresosVentas;
		public int costoVariableProduccion;
		public int utilidad;
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
			r.demanda = (int)Math.round(market);

			// inversión inicial sólo en el año 1
			r.inversionInicial = (i == 0) ? capacidad * costoCapacidadUnitaria : 0.0;
			// costo operativo anual (se aplica cada año)
			r.costoFijoOperacion = capacidad * costoOperativoUnitario;

			// unidades producidas limitadas por capacidad y demanda
			r.unidadesProducidas = Math.min(capacidad, r.demanda);

			r.ingresosVentas = (int)Math.round(r.unidadesProducidas * precioVentaUnitario);
			r.costoVariableProduccion = (int)Math.round(r.unidadesProducidas * costoVariableUnitario);
			// utilidad = ingresos - coste variable - costo fijo operativo
			r.utilidad = r.ingresosVentas - r.costoVariableProduccion - (int)Math.round(r.costoFijoOperacion);

			res[i] = r;

			market = market * (1.0 + crecimientoAnual);
		}

		return res;
	}

	/**
	 * Nueva firma que usa directamente el ControladorParametros (lógica del Excel):
	 * calcula demanda por tramos (primeros 5 años con g1, siguientes 5 con g2)
	 * y aplica cuotas para calcular unidades sin y con la nueva versión. Para
	 * compatibilidad con la tabla antigua, este método devuelve un arreglo de
	 * ResultadoAnual donde las métricas reflejan el escenario "con" (utilidadCon).
	 */
	public static ResultadoAnual[] calcularModelo(ControladorParametros params, int capacidad) {
		int H = params.getHorizonteAnios();
		ResultadoAnual[] res = new ResultadoAnual[H];
		double market = params.getTamanoActualMercado();
		double g1 = params.getCrecimientoPrimeros5();
		double g2 = params.getCrecimientoProximos5();

		for (int i = 0; i < H; i++) {
			ResultadoAnual r = new ResultadoAnual();
			r.anio = i + 1;
			r.demanda = (int)Math.round(market);

			r.inversionInicial = 0.0; // inversión gestionada externamente
			r.costoFijoOperacion = 0.0; // asumimos 0 para evitar doble contabilidad

			double demandaCon = r.demanda * params.getCuotaMercadoConNuevaVersion();
			// double demandaSin = r.demanda * params.getCuotaMercadoVersionIngles(); // no usada

			r.unidadesProducidas = Math.min(capacidad, (int)Math.round(demandaCon));
			r.ingresosVentas = (int)Math.round(r.unidadesProducidas * params.getPrecioVentaUnitario());
			r.costoVariableProduccion = (int)Math.round(r.unidadesProducidas * params.getCosteVariableUnitario());
			r.utilidad = r.ingresosVentas - r.costoVariableProduccion;

			res[i] = r;

			if (i < 5) market = market * (1.0 + g1); else market = market * (1.0 + g2);
		}

		return res;
	}

	/**
	 * Suma simple de utilidades (no descontadas) sobre el horizonte para una capacidad dada
	 */
	public static double calcularGananciaTotal(int capacidad, int demandaInicial, double crecimientoAnual,
											  double costoCapacidadUnitaria, double precioVentaUnitario,
											  double costoVariableUnitario, double costoOperativoUnitario) {
		ResultadoAnual[] res = calcularModelo(capacidad, demandaInicial, crecimientoAnual,
			costoCapacidadUnitaria, precioVentaUnitario, costoVariableUnitario, costoOperativoUnitario);
		double suma = 0;
		for (ResultadoAnual r : res) suma += r.utilidad;
		return suma;
	}

	/**
	 * Calcula VAN (usando tasa), restando la inversión inicial en t=0 (si la hubiera).
	 */
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
			van += res[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
		}
		return van;
	}

	/**
	 * Calcula los resultados año a año para ambos escenarios (con y sin versión francesa),
	 * el VAN de cada uno y la diferencia, usando la tasa de descuento indicada.
	 */
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
        double market = params.getTamanoActualMercado();
        double g2 = params.getCrecimientoProximos5();
        double demandaInicial = params.getTamanoActualMercado();
        double cuotaCon = params.getCuotaMercadoConNuevaVersion();
        double cuotaSin = params.getCuotaMercadoVersionIngles();
        double precio = params.getPrecioVentaUnitario();
        double costeVar = params.getCosteVariableUnitario();
        double costeFijo = params.getCosteFijoCrearVersion();

        for (int i = 0; i < H; i++) {
            ResultadoAnual rCon = new ResultadoAnual();
            ResultadoAnual rSin = new ResultadoAnual();
            rCon.anio = rSin.anio = i + 1;
            // Calcular tamaño de mercado compuesto por tramos
            int n = i + 1;
            int tramo1 = Math.min(n, 5);
            int tramo2 = Math.max(0, n - 5);
            double g1 = params.getCrecimientoPrimeros5();
            double marketYear = demandaInicial * Math.pow(1.0 + g1, tramo1) * Math.pow(1.0 + g2, tramo2);
            rCon.demanda = rSin.demanda = (int)Math.round(marketYear);

            // Solo la versión francesa tiene inversión inicial en t=0
            rCon.inversionInicial = (i == 0) ? costeFijo : 0.0;
            rSin.inversionInicial = 0.0;
            rCon.costoFijoOperacion = rSin.costoFijoOperacion = 0.0;

            rCon.unidadesProducidas = (int)Math.round(rCon.demanda * cuotaCon);
            rSin.unidadesProducidas = (int)Math.round(rSin.demanda * cuotaSin);

            rCon.ingresosVentas = (int)Math.round(rCon.unidadesProducidas * precio);
            rSin.ingresosVentas = (int)Math.round(rSin.unidadesProducidas * precio);
            rCon.costoVariableProduccion = (int)Math.round(rCon.unidadesProducidas * costeVar);
            rSin.costoVariableProduccion = (int)Math.round(rSin.unidadesProducidas * costeVar);
            rCon.utilidad = rCon.ingresosVentas - rCon.costoVariableProduccion;
            rSin.utilidad = rSin.ingresosVentas - rSin.costoVariableProduccion;

            resCon[i] = rCon;
            resSin[i] = rSin;
        }

        // Calcular VAN para ambos escenarios
        double vanCon = 0.0, vanSin = 0.0;
        for (int i = 0; i < H; i++) {
            vanCon += resCon[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
            vanSin += resSin[i].utilidad / Math.pow(1.0 + tasaDescuento, i + 1);
        }
        // Restar inversión inicial de la versión francesa
        if (H > 0) vanCon -= resCon[0].inversionInicial;

        ResultadoComparativo resultado = new ResultadoComparativo();
        resultado.resultadosCon = resCon;
        resultado.resultadosSin = resSin;
        resultado.vanCon = vanCon;
        resultado.vanSin = vanSin;
        resultado.diferenciaVAN = vanCon - vanSin;
        return resultado;
    }
}
