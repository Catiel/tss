package actividad_5.ventas;

/** Modelo para distribución de demanda diaria y ganancias asociadas. */
public class VentasModelo {
    // Valores de demanda (programas vendidos) y probabilidades
    public static final int[] DEMANDA = {2300,2400,2500,2600,2700};
    public static final double[] PROBS   = {0.15,0.22,0.24,0.21,0.18};
    // Ganancias asociadas segun la tabla del ejemplo
    public static final int[] GANANCIA = {2600,2800,3000,3000,3000};

    private static final double[] ACUM;
    static { ACUM = new double[PROBS.length]; double a=0; for(int i=0;i<PROBS.length;i++){ a+=PROBS[i]; ACUM[i]=(i==PROBS.length-1)?1.0:a; } }

    public static int demandaPara(double r){
        for(int i=0;i<ACUM.length;i++) if(r < ACUM[i]) return DEMANDA[i];
        return DEMANDA[DEMANDA.length-1];
    }
    public static int gananciaParaDemanda(int demanda){
        for(int i=0;i<DEMANDA.length;i++) if(DEMANDA[i]==demanda) return GANANCIA[i];
        return 0;
    }
    public static double esperadoDemanda(){ double s=0; for(int i=0;i<DEMANDA.length;i++) s+= DEMANDA[i]*PROBS[i]; return s; }
    public static double esperadoGanancia(){ double s=0; for(int i=0;i<GANANCIA.length;i++) s+= GANANCIA[i]*PROBS[i]; return s; }
}

