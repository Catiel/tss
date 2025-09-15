package actividad_5.colas;

/** Modelo de distribuciones para la cola del banco (ventanilla auto).
 *  Tiempos de servicio (min): 0:0.00, 1:0.25, 2:0.20, 3:0.40, 4:0.15
 *  Tiempos entre llegadas (min): 0:0.10, 1:0.35, 2:0.25, 3:0.15, 4:0.10, 5:0.05
 */
public class ColaBancoModelo {
    public static final int[] SERVICIO_VALORES = {0,1,2,3,4};
    public static final double[] SERVICIO_PROBS = {0.00,0.25,0.20,0.40,0.15};

    public static final int[] LLEGADA_VALORES = {0,1,2,3,4,5};
    public static final double[] LLEGADA_PROBS = {0.10,0.35,0.25,0.15,0.10,0.05};

    // Distribuciones acumuladas precalculadas
    private static final double[] SERVICIO_ACUM = {0.00, 0.25, 0.45, 0.85, 1.00};
    private static final double[] LLEGADA_ACUM = {0.10, 0.45, 0.70, 0.85, 0.95, 1.00};

    /** Determina el tiempo de servicio basado en un número aleatorio r [0,1) */
    public static int tiempoServicio(double r){
        if(r < 0 || r >= 1) return -1;
        for(int i = 0; i < SERVICIO_ACUM.length; i++){
            if(r < SERVICIO_ACUM[i]) return SERVICIO_VALORES[i];
        }
        return SERVICIO_VALORES[SERVICIO_VALORES.length - 1];
    }

    /** Determina el tiempo entre llegadas basado en un número aleatorio r [0,1) */
    public static int tiempoInterLlegada(double r){
        if(r < 0 || r >= 1) return -1;
        for(int i = 0; i < LLEGADA_ACUM.length; i++){
            if(r < LLEGADA_ACUM[i]) return LLEGADA_VALORES[i];
        }
        return LLEGADA_VALORES[LLEGADA_VALORES.length - 1];
    }

    /** Obtiene los rangos de números aleatorios para tiempo de servicio */
    public static double[][] getRangosServicio(){
        double[][] rangos = new double[SERVICIO_VALORES.length][2];
        double inicio = 0.0;
        for(int i = 0; i < SERVICIO_VALORES.length; i++){
            rangos[i][0] = inicio; // inicio del rango
            rangos[i][1] = SERVICIO_ACUM[i]; // fin del rango
            inicio = SERVICIO_ACUM[i];
        }
        return rangos;
    }

    /** Obtiene los rangos de números aleatorios para tiempo entre llegadas */
    public static double[][] getRangosLlegada(){
        double[][] rangos = new double[LLEGADA_VALORES.length][2];
        double inicio = 0.0;
        for(int i = 0; i < LLEGADA_VALORES.length; i++){
            rangos[i][0] = inicio; // inicio del rango
            rangos[i][1] = LLEGADA_ACUM[i]; // fin del rango
            inicio = LLEGADA_ACUM[i];
        }
        return rangos;
    }
}