package actividad_4.ejercicio_2; // Paquete donde reside la clase

/**
 * Cálculos del EJERCICIO 2: periodo de recuperación (payback) ignorando valor temporal.
 * Valores monetarios en millones según enunciado.
 */
public class PaybackCalculo { // Clase utilitaria para cálculo de payback

    public static class ResultadoAnual { // Estructura para almacenar datos de cada año
        public int anio;                 // Año (1..horizonte)
        public double flujo;             // Flujo de caja de ese año (millones, redondeado)
        public double acumulado;         // Suma acumulada de los flujos hasta este año
        public int menorQueInversion;    // Indicador (1 si acumulado < inversión, 0 si >=)
    }

    public static class ResultadoPayback { // Contenedor del resultado completo
        public ResultadoAnual[] resultados;    // Arreglo con el detalle año a año
        public int periodoRecuperacion;        // Año en que se recupera la inversión; -1 si no se recupera
    }

    /**
     * Calcula el detalle anual y el periodo de recuperación usando los parámetros globales.
     */
    public static ResultadoPayback calcular(ControladorParametros params) { // Método de conveniencia usando singleton
        return calcular(params.getInversionOriginal(),    // Inversión inicial
                params.getFlujoAnio1(),                   // Flujo año 1
                params.getTasaCrecimientoAnual(),         // Crecimiento anual decimal
                params.getHorizonteAnios());              // Horizonte en años
    }

    /**
     * Calcula payback para valores explícitos.
     * @param inversion Inversión inicial (millones)
     * @param flujoAnio1 Flujo de caja del año 1 (millones)
     * @param crecimiento Crecimiento anual constante (decimal)
     * @param horizonte Años a simular
     */
    public static ResultadoPayback calcular(double inversion, double flujoAnio1, double crecimiento, int horizonte) {
        ResultadoPayback total = new ResultadoPayback();               // Crea objeto resultado global
        total.resultados = new ResultadoAnual[horizonte];              // Reserva arreglo para cada año
        double flujo = flujoAnio1;                                     // Variable temporal para flujo del año actual
        double acumulado = 0.0;                                        // Acumulado de flujos
        int periodo = -1;                                              // Periodo de recuperación (-1 aún no recupera)
        for (int i = 0; i < horizonte; i++) {                          // Itera cada año 0..horizonte-1
            ResultadoAnual r = new ResultadoAnual();                   // Crea registro anual
            r.anio = i + 1;                                            // Año humano (1-based)
            if (i == 0) {                                              // Primer año usa flujo base
                flujo = flujoAnio1;
            } else {                                                   // Años siguientes aplican crecimiento compuesto
                flujo = flujo * (1.0 + crecimiento);
            }
            r.flujo = UtilidadesFormato.redondearDosDecimales(flujo);  // Redondea flujo a 2 decimales
            acumulado += flujo;                                        // Suma al acumulado
            r.acumulado = UtilidadesFormato.redondearDosDecimales(acumulado); // Redondea acumulado
            r.menorQueInversion = acumulado < inversion ? 1 : 0;       // Marca si aún no se recupera
            if (periodo == -1 && acumulado >= inversion) {             // Si es la primera vez que se supera la inversión
                periodo = r.anio;                                      // Registra año de recuperación
            }
            total.resultados[i] = r;                                   // Guarda registro anual
        }
        total.periodoRecuperacion = periodo;                           // Establece periodo (o -1 si no recuperó)
        return total;                                                  // Devuelve estructura
    }

    /**
     * Sólo devuelve el periodo de recuperación (para tabla de sensibilidad 2D).
     */
    public static int calcularPeriodo(double inversion, double flujoAnio1, double crecimiento, int horizonte) {
        double flujo = flujoAnio1;            // Flujo del año actual
        double acumulado = 0.0;               // Suma acumulada
        for (int i = 1; i <= horizonte; i++) { // Recorre años 1..horizonte
            if (i == 1) {                     // Año 1 usa flujo base
                flujo = flujoAnio1;
            } else {                          // Años siguientes crecen
                flujo = flujo * (1.0 + crecimiento);
            }
            acumulado += flujo;               // Actualiza acumulado
            if (acumulado >= inversion) return i; // Recuperado en este año
        }
        return -1;                            // No recupera dentro del horizonte
    }
}
