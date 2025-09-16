package actividad_5.dulce;

/** Modelo de la simulación "Dulce Ada" corregido para coincidir exactamente con Excel.
 * Demanda histórica uniforme en {40,50,60,70,80,90} (probabilidades iguales 1/6).
 * Costos y precios:
 *  - Costo mayorista: 7.50
 *  - Precio normal venta: 12.00
 *  - Precio descuento remate: 6.00 (si queda inventario después del 23 de Julio)
 */
public class DulceModelo {
    public static final double COSTO = 7.50;
    public static final double PRECIO_NORMAL = 12.00;
    public static final double PRECIO_DESCUENTO = 6.00;

    public static final int[] DEMANDAS = {40, 50, 60, 70, 80, 90};
    public static final double[] PROB = {1/6.0, 1/6.0, 1/6.0, 1/6.0, 1/6.0, 1/6.0};

    // Distribución acumulada corregida
    private static final double[] ACUM = {1/6.0, 2/6.0, 3/6.0, 4/6.0, 5/6.0, 1.0};

    /** 100 números aleatorios fijos del Excel */
    public static final double[] RAND_FIJOS = {
        0.5962, 0.0683, 0.2991, 0.4092, 0.1936, 0.4560, 0.2890, 0.3403, 0.2124, 0.7571,
        0.2492, 0.5512, 0.3948, 0.1730, 0.6656, 0.8816, 0.0696, 0.0128, 0.1294, 0.2292,
        0.0320, 0.2818, 0.0359, 0.1132, 0.5231, 0.5289, 0.3017, 0.2096, 0.8850, 0.2611,
        0.5299, 0.4195, 0.0965, 0.6486, 0.7210, 0.7043, 0.1553, 0.6662, 0.9286, 0.2495,
        0.3603, 0.2322, 0.0394, 0.2182, 0.3722, 0.5225, 0.7731, 0.8572, 0.5716, 0.3634,
        0.9652, 0.2213, 0.0442, 0.7381, 0.7344, 0.2853, 0.1946, 0.0509, 0.6486, 0.3673,
        0.1917, 0.2325, 0.7494, 0.4237, 0.0566, 0.5090, 0.2919, 0.4120, 0.0245, 0.8966,
        0.0626, 0.4428, 0.6920, 0.7149, 0.1573, 0.9393, 0.5318, 0.6861, 0.2052, 0.8888,
        0.2706, 0.5004, 0.8316, 0.9806, 0.3443, 0.2515, 0.2393, 0.4586, 0.7869, 0.8751,
        0.3627, 0.2229, 0.3455, 0.1001, 0.1016, 0.8382, 0.1141, 0.9972, 0.3679, 0.4236
    };

    /** Determina demanda según número aleatorio r */
    public static int demandaPara(double r) {
        for (int i = 0; i < ACUM.length; i++) {
            if (r < ACUM[i]) return DEMANDAS[i];
        }
        return DEMANDAS[DEMANDAS.length - 1];
    }

    /** Calcula ganancia según fórmula del Excel */
    public static double ganancia(int Q, int demanda) {
        if (demanda >= Q) {
            // Vende todo lo que compró al precio normal
            return (PRECIO_NORMAL - COSTO) * Q;
        } else {
            // Vende lo demandado al precio normal y el resto al precio de descuento
            return (PRECIO_NORMAL - COSTO) * demanda + (PRECIO_DESCUENTO - COSTO) * (Q - demanda);
        }
    }

    /** Simula ganancias usando arreglo de randoms y decisión Q */
    public static double[] simularGanancias(int Q, double[] randoms) {
        double[] ganancias = new double[randoms.length];
        for (int i = 0; i < randoms.length; i++) {
            int d = demandaPara(randoms[i]);
            ganancias[i] = ganancia(Q, d);
        }
        return ganancias;
    }

    /** Calcula promedio de un arreglo */
    public static double promedio(double[] arr) {
        if (arr.length == 0) return 0;
        double suma = 0;
        for (double v : arr) suma += v;
        return suma / arr.length;
    }

    /** Obtiene los rangos para la distribución */
    public static double[][] getRangos() {
        double[][] rangos = new double[DEMANDAS.length][2];
        double inicio = 0.0;
        for (int i = 0; i < DEMANDAS.length; i++) {
            rangos[i][0] = inicio;
            rangos[i][1] = ACUM[i];
            inicio = ACUM[i];
        }
        return rangos;
    }
}