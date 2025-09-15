package actividad_5.calentadores;

/** Modelo de ventas semanales de calentadores (datos de 50 semanas).
 *  Valores de venta: 4..10 con probabilidades derivadas de frecuencias: 6,5,9,12,8,7,3.
 *  Probabilidades: 0.12,0.10,0.18,0.24,0.16,0.14,0.06 (suman 1.00)
 */
public class CalentadoresModelo {
    public static final int INVENTARIO_FIJO = 8; // inventario semanal para el inciso a)
    public static final int[] VENTAS = {4,5,6,7,8,9,10};
    public static final double[] PROBS = {0.12,0.10,0.18,0.24,0.16,0.14,0.06};

    private static final double[] ACUMULADA;
    static {
        ACUMULADA = new double[PROBS.length];
        double acum=0; for(int i=0;i<PROBS.length;i++){ acum+=PROBS[i]; ACUMULADA[i] = (i==PROBS.length-1)?1.0:acum; }
    }

    /** Devuelve ventas simuladas para un número aleatorio r en [0,1). */
    public static int ventasPara(double r){
        if(r<0 || r>=1) return -1;
        for(int i=0;i<ACUMULADA.length;i++) if(r < ACUMULADA[i]) return VENTAS[i];
        return VENTAS[VENTAS.length-1];
    }

    /** Valor esperado analítico. */
    public static double esperado(){ double s=0; for(int i=0;i<VENTAS.length;i++) s += VENTAS[i]*PROBS[i]; return s; }
}

