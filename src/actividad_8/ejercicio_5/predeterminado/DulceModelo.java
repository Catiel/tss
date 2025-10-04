package actividad_8.ejercicio_5.predeterminado; // Define el paquete donde se encuentra esta clase

/** Modelo de la simulación "Dulce Ada" corregido para coincidir exactamente con Excel.
 * Demanda histórica uniforme en {40,50,60,70,80,90} (probabilidades iguales 1/6).
 * Costos y precios:
 *  - Costo mayorista: 7.50
 *  - Precio normal venta: 12.00
 *  - Precio descuento remate: 6.00 (si queda inventario después del 23 de Julio)
 */
public class DulceModelo { // Clase que modela el problema de optimización de ganancias para el negocio Dulce Ada
    public static final double COSTO = 7.50; // Costo por unidad que Dulce Ada paga al proveedor mayorista
    public static final double PRECIO_NORMAL = 12.00; // Precio de venta regular por unidad durante la temporada normal
    public static final double PRECIO_DESCUENTO = 6.00; // Precio reducido de liquidación para inventario no vendido después del 23 de julio

    public static final int[] DEMANDAS = {40, 50, 60, 70, 80, 90}; // Array con los posibles valores de demanda histórica observados (unidades)
    public static final double[] PROB = {1/6.0, 1/6.0, 1/6.0, 1/6.0, 1/6.0, 1/6.0}; // Probabilidades uniformes para cada nivel de demanda (distribución equiprobable)

    // Distribución acumulada corregida
    private static final double[] ACUM = {1/6.0, 2/6.0, 3/6.0, 4/6.0, 5/6.0, 1.0}; // Probabilidades acumuladas precalculadas: {0.1667, 0.3333, 0.5, 0.6667, 0.8333, 1.0}

    /** 100 números aleatorios fijos del Excel */
    public static final double[] RAND_FIJOS = { // Array con exactamente 100 números aleatorios predefinidos extraídos de Excel para reproducir resultados exactos
        0.5962, 0.0683, 0.2991, 0.4092, 0.1936, 0.4560, 0.2890, 0.3403, 0.2124, 0.7571, // Primeros 10 números aleatorios de la simulación
        0.2492, 0.5512, 0.3948, 0.1730, 0.6656, 0.8816, 0.0696, 0.0128, 0.1294, 0.2292, // Números aleatorios 11-20
        0.0320, 0.2818, 0.0359, 0.1132, 0.5231, 0.5289, 0.3017, 0.2096, 0.8850, 0.2611, // Números aleatorios 21-30
        0.5299, 0.4195, 0.0965, 0.6486, 0.7210, 0.7043, 0.1553, 0.6662, 0.9286, 0.2495, // Números aleatorios 31-40
        0.3603, 0.2322, 0.0394, 0.2182, 0.3722, 0.5225, 0.7731, 0.8572, 0.5716, 0.3634, // Números aleatorios 41-50
        0.9652, 0.2213, 0.0442, 0.7381, 0.7344, 0.2853, 0.1946, 0.0509, 0.6486, 0.3673, // Números aleatorios 51-60
        0.1917, 0.2325, 0.7494, 0.4237, 0.0566, 0.5090, 0.2919, 0.4120, 0.0245, 0.8966, // Números aleatorios 61-70
        0.0626, 0.4428, 0.6920, 0.7149, 0.1573, 0.9393, 0.5318, 0.6861, 0.2052, 0.8888, // Números aleatorios 71-80
        0.2706, 0.5004, 0.8316, 0.9806, 0.3443, 0.2515, 0.2393, 0.4586, 0.7869, 0.8751, // Números aleatorios 81-90
        0.3627, 0.2229, 0.3455, 0.1001, 0.1016, 0.8382, 0.1141, 0.9972, 0.3679, 0.4236  // Números aleatorios 91-100 (últimos 10)
    };

    /** Determina demanda según número aleatorio r */
    public static int demandaPara(double r) { // Método que convierte un número aleatorio [0,1) en un valor de demanda según la distribución uniforme
        for (int i = 0; i < ACUM.length; i++) { // Itera sobre las probabilidades acumuladas para encontrar el rango apropiado
            if (r < ACUM[i]) return DEMANDAS[i]; // Si el número aleatorio cae en este rango acumulado, retorna la demanda correspondiente
        }
        return DEMANDAS[DEMANDAS.length - 1]; // Retorna la demanda más alta como fallback (90 unidades) si no se encontró rango
    }

    /** Calcula ganancia según fórmula del Excel */
    public static double ganancia(int Q, int demanda) { // Método que calcula la ganancia neta basada en la cantidad pedida Q y la demanda real
        if (demanda >= Q) { // Si la demanda es mayor o igual a lo que se pidió
            // Vende todo lo que compró al precio normal
            return (PRECIO_NORMAL - COSTO) * Q; // Ganancia = (12.00 - 7.50) × Q = 4.50 × Q (vende todo al precio normal)
        } else { // Si la demanda es menor a lo que se pidió (sobra inventario)
            // Vende lo demandado al precio normal y el resto al precio de descuento
            return (PRECIO_NORMAL - COSTO) * demanda + (PRECIO_DESCUENTO - COSTO) * (Q - demanda); // Ganancia mixta: parte al precio normal (4.50 × demanda) + parte al precio descuento (-1.50 × sobrante)
        }
    }

    /** Simula ganancias usando arreglo de randoms y decisión Q */
    public static double[] simularGanancias(int Q, double[] randoms) { // Método que simula las ganancias para una decisión de pedido Q usando un array de números aleatorios
        double[] ganancias = new double[randoms.length]; // Crea array para almacenar las ganancias de cada simulación
        for (int i = 0; i < randoms.length; i++) { // Itera sobre cada número aleatorio proporcionado
            int d = demandaPara(randoms[i]); // Convierte el número aleatorio en un valor de demanda
            ganancias[i] = ganancia(Q, d); // Calcula la ganancia para esta combinación de Q y demanda, y la almacena
        }
        return ganancias; // Retorna el array completo de ganancias simuladas
    }

    /** Calcula promedio de un arreglo */
    public static double promedio(double[] arr) { // Método utilitario que calcula el promedio (media aritmética) de un array de números
        if (arr.length == 0) return 0; // Maneja el caso edge de array vacío retornando 0
        double suma = 0; // Inicializa acumulador para la suma
        for (double v : arr) suma += v; // Suma todos los valores del array
        return suma / arr.length; // Retorna el promedio dividiendo la suma entre el número de elementos
    }

    /** Obtiene los rangos para la distribución */
    public static double[][] getRangos() { // Método que genera una matriz con los rangos de números aleatorios para cada valor de demanda
        double[][] rangos = new double[DEMANDAS.length][2]; // Crea matriz bidimensional: filas=valores de demanda, columnas=[inicio,fin]
        double inicio = 0.0; // Inicializa el punto de inicio del primer rango en 0.0
        for (int i = 0; i < DEMANDAS.length; i++) { // Itera sobre todos los valores de demanda
            rangos[i][0] = inicio; // Establece el inicio del rango para este valor de demanda
            rangos[i][1] = ACUM[i]; // Establece el fin del rango usando la probabilidad acumulada
            inicio = ACUM[i]; // El inicio del siguiente rango es el fin del rango actual
        }
        return rangos; // Retorna la matriz completa con todos los rangos de números aleatorios
    }
}