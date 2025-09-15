package actividad_5.sangre;

/** Modelo de distribuciones para el problema de plasma/sangre.
 *  Cantidades suministradas (pintas/entrega) -> Probabilidades:
 *  4:0.15, 5:0.20, 6:0.25, 7:0.15, 8:0.15, 9:0.10
 *  Pacientes por semana -> 0:0.25, 1:0.25, 2:0.30, 3:0.15, 4:0.05
 *  Demanda por paciente (pintas) -> 1:0.40, 2:0.30, 3:0.20, 4:0.10
 */
public class SangreModelo {
    // Valores y probabilidades (ordenados)
    public static final int[] SUPPLY_VALUES = {4,5,6,7,8,9};
    public static final double[] SUPPLY_PROBS = {0.15,0.20,0.25,0.15,0.15,0.10};

    public static final int[] PACIENTES_VALUES = {0,1,2,3,4};
    public static final double[] PACIENTES_PROBS = {0.25,0.25,0.30,0.15,0.05};

    public static final int[] DEMANDA_VALUES = {1,2,3,4};
    public static final double[] DEMANDA_PROBS = {0.40,0.30,0.20,0.10};

    private static int valorDesdeDistribucion(double r, int[] valores, double[] probs){
        double acum = 0;
        for(int i=0;i<probs.length;i++){
            acum += probs[i];
            if(r < acum + 1e-12) return valores[i];
        }
        return valores[valores.length-1];
    }

    public static int suministro(double r){ return valorDesdeDistribucion(r, SUPPLY_VALUES, SUPPLY_PROBS); }
    public static int pacientes(double r){ return valorDesdeDistribucion(r, PACIENTES_VALUES, PACIENTES_PROBS); }
    public static int demandaPaciente(double r){ return valorDesdeDistribucion(r, DEMANDA_VALUES, DEMANDA_PROBS); }
}

