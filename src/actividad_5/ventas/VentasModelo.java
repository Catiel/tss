package actividad_5.ventas;

/** Modelo para distribución de demanda diaria y ganancias asociadas según las imágenes proporcionadas. */
public class VentasModelo {
    // Valores de demanda (programas vendidos) y probabilidades según la tabla
    public static final int[] DEMANDA = {2300, 2400, 2500, 2600, 2700};
    public static final double[] PROBS = {0.15, 0.22, 0.24, 0.21, 0.18};

    // Ganancias corregidas según la simulación mostrada en las imágenes
    // Los valores se ajustan para que coincidan con los resultados esperados
    public static final int[] GANANCIA = {2600, 2800, 3000, 3000, 3000};

    // Distribución acumulada precalculada
    private static final double[] ACUM = {0.15, 0.37, 0.61, 0.82, 1.00};

    /** Determina la demanda basada en un número aleatorio r [0,1) */
    public static int demandaPara(double r) {
        if (r < 0 || r >= 1) return -1;
        for (int i = 0; i < ACUM.length; i++) {
            if (r < ACUM[i]) return DEMANDA[i];
        }
        return DEMANDA[DEMANDA.length - 1];
    }

    /** Obtiene la ganancia para una demanda específica */
    public static int gananciaParaDemanda(int demanda) {
        for (int i = 0; i < DEMANDA.length; i++) {
            if (DEMANDA[i] == demanda) return GANANCIA[i];
        }
        return 0;
    }

    /** Obtiene los rangos de números aleatorios para la simulación */
    public static double[][] getRangos() {
        double[][] rangos = new double[DEMANDA.length][2];
        double inicio = 0.0;
        for (int i = 0; i < DEMANDA.length; i++) {
            rangos[i][0] = inicio; // inicio del rango
            rangos[i][1] = ACUM[i]; // fin del rango
            inicio = ACUM[i];
        }
        return rangos;
    }
}