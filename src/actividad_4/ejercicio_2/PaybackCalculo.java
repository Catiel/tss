package actividad_4.ejercicio_2;

/**
 * Cálculos del EJERCICIO 2: periodo de recuperación (payback) ignorando valor temporal.
 * Valores monetarios en millones según enunciado.
 */
public class PaybackCalculo {

    public static class ResultadoAnual {
        public int anio;
        public double flujo;
        public double acumulado;
        public int menorQueInversion; // 1 si acumulado < inversion, 0 en caso contrario
    }

    public static class ResultadoPayback {
        public ResultadoAnual[] resultados;
        public int periodoRecuperacion; // año en el que se recupera (primera vez acumulado >= inversion). -1 si no recupera
    }

    /**
     * Calcula el detalle anual y el periodo de recuperación usando los parámetros globales.
     */
    public static ResultadoPayback calcular(ControladorParametros params) {
        return calcular(params.getInversionOriginal(), params.getFlujoAnio1(), params.getTasaCrecimientoAnual(), params.getHorizonteAnios());
    }

    /**
     * Calcula payback para valores explícitos.
     * @param inversion Inversión inicial (millones)
     * @param flujoAnio1 Flujo de caja del año 1 (millones)
     * @param crecimiento Crecimiento anual constante (decimal)
     * @param horizonte Años a simular
     */
    public static ResultadoPayback calcular(double inversion, double flujoAnio1, double crecimiento, int horizonte) {
        ResultadoPayback total = new ResultadoPayback();
        total.resultados = new ResultadoAnual[horizonte];
        double flujo = flujoAnio1;
        double acumulado = 0.0;
        int periodo = -1;
        for (int i = 0; i < horizonte; i++) {
            ResultadoAnual r = new ResultadoAnual();
            r.anio = i + 1;
            if (i == 0) {
                flujo = flujoAnio1;
            } else {
                flujo = flujo * (1.0 + crecimiento);
            }
            r.flujo = UtilidadesFormato.redondearDosDecimales(flujo);
            acumulado += flujo;
            r.acumulado = UtilidadesFormato.redondearDosDecimales(acumulado);
            r.menorQueInversion = acumulado < inversion ? 1 : 0;
            if (periodo == -1 && acumulado >= inversion) {
                periodo = r.anio; // primer año en que se recupera
            }
            total.resultados[i] = r;
        }
        total.periodoRecuperacion = periodo; // puede quedar -1 si no se recupera
        return total;
    }

    /**
     * Sólo devuelve el periodo de recuperación (para tabla de sensibilidad 2D).
     */
    public static int calcularPeriodo(double inversion, double flujoAnio1, double crecimiento, int horizonte) {
        double flujo = flujoAnio1;
        double acumulado = 0.0;
        for (int i = 1; i <= horizonte; i++) {
            if (i == 1) {
                flujo = flujoAnio1;
            } else {
                flujo = flujo * (1.0 + crecimiento);
            }
            acumulado += flujo;
            if (acumulado >= inversion) return i;
        }
        return -1; // no recupera
    }
}

