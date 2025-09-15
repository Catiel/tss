package actividad_5.colas;

/** Modelo de distribuciones para la cola del banco (un solo cajero).
 *  Tiempos de servicio (min): 0:0.00, 1:0.25, 2:0.20, 3:0.40, 4:0.15
 *  Tiempos entre llegadas (min): 0:0.10, 1:0.35, 2:0.25, 3:0.15, 4:0.10, 5:0.05
 */
public class ColaBancoModelo {
    public static final int[] SERVICIO_VALORES = {0,1,2,3,4};
    public static final double[] SERVICIO_PROBS = {0.00,0.25,0.20,0.40,0.15};

    public static final int[] LLEGADA_VALORES = {0,1,2,3,4,5};
    public static final double[] LLEGADA_PROBS = {0.10,0.35,0.25,0.15,0.10,0.05};

    private static int valor(double r, int[] valores, double[] probs){
        double acum=0; for(int i=0;i<probs.length;i++){ acum+=probs[i]; if(r < acum + 1e-12) return valores[i]; }
        return valores[valores.length-1];
    }
    public static int tiempoServicio(double r){ return valor(r, SERVICIO_VALORES, SERVICIO_PROBS); }
    public static int tiempoInterLlegada(double r){ return valor(r, LLEGADA_VALORES, LLEGADA_PROBS); }
}

