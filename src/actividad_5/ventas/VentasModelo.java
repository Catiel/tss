package actividad_5.ventas; // Define el paquete donde se encuentra esta clase modelo para el módulo de simulación de ventas

/** Modelo para distribución de demanda diaria y ganancias asociadas según las imágenes proporcionadas. */ // Comentario de documentación que explica el propósito del modelo de datos para simulación de ventas
public class VentasModelo { // Declara la clase modelo que contiene las distribuciones de probabilidad y datos para la simulación de ventas de programas
    // Valores de demanda (programas vendidos) y probabilidades según la tabla
    public static final int[] DEMANDA = {2300, 2400, 2500, 2600, 2700}; // Array estático final que contiene los valores posibles de demanda diaria en unidades de programas vendidos
    public static final double[] PROBS = {0.15, 0.22, 0.24, 0.21, 0.18}; // Array estático final que contiene las probabilidades correspondientes para cada nivel de demanda

    // Constantes para el cálculo de ganancias
    public static final double PRECIO_VENTA = 2.0; // Precio de venta por programa en Bs
    public static final double COSTO_PRODUCCION = 0.8; // Costo de producción por programa en Bs

    // Ganancias precalculadas para compatibilidad (ya no se usan, pero se mantienen por compatibilidad)
    @Deprecated
    public static final int[] GANANCIA_2500 = {2600, 2800, 3000, 3000, 3000}; // Array estático final que contiene las ganancias en dólares para producción de 2500 programas
    @Deprecated
    public static final int[] GANANCIA_2600 = {2300, 2500, 2700, 3200, 3200}; // Array estático final que contiene las ganancias en dólares para producción de 2600 programas

    // Distribución acumulada precalculada
    private static final double[] ACUM = {0.15, 0.37, 0.61, 0.82, 1.00}; // Array estático final privado con las probabilidades acumuladas precalculadas para optimizar la búsqueda

    /** Determina la demanda basada en un número aleatorio r [0,1) */ // Comentario de documentación que explica el propósito del método
    public static int demandaPara(double r) { // Método público estático que convierte un número aleatorio en un valor de demanda usando el método de transformación inversa
        if (r < 0 || r >= 1) return -1; // Valida que el número aleatorio esté en el rango válido [0,1), si no retorna -1 como valor de error
        for (int i = 0; i < ACUM.length; i++) { // Itera sobre cada valor de probabilidad acumulada
            if (r < ACUM[i]) return DEMANDA[i]; // Si el número aleatorio es menor que la probabilidad acumulada actual, retorna la demanda correspondiente
        }
        return DEMANDA[DEMANDA.length - 1]; // Si no se encuentra coincidencia (caso extremo), retorna el último valor de demanda
    }

    /** Calcula la ganancia para una demanda y producción específicas usando la fórmula correcta */
    public static int calcularGanancia(int demanda, int produccion) {
        if (demanda < produccion) {
            // Si la demanda es menor que la producción: ganancia = (precio_venta × demanda) - (costo × producción)
            return (int) Math.round(PRECIO_VENTA * demanda - COSTO_PRODUCCION * produccion);
        } else {
            // Si la demanda es mayor o igual que la producción: ganancia = (precio_venta × producción) - (costo × producción)
            return (int) Math.round(PRECIO_VENTA * produccion - COSTO_PRODUCCION * produccion);
        }
    }

    /** Obtiene la ganancia para una demanda específica con producción de 2500 */ // Comentario de documentación que explica el propósito del método
    public static int gananciaParaDemanda(int demanda) { // Método público estático que calcula la ganancia para producción de 2500 usando la fórmula dinámica
        return calcularGanancia(demanda, 2500);
    }

    /** Obtiene la ganancia para una demanda específica con producción de 2600 */ // Comentario de documentación que explica el propósito del método
    public static int gananciaParaDemanda2600(int demanda) { // Método público estático que calcula la ganancia para producción de 2600 usando la fórmula dinámica
        return calcularGanancia(demanda, 2600);
    }

    /** Obtiene los rangos de números aleatorios para la simulación */ // Comentario de documentación que explica el propósito del método
    public static double[][] getRangos() { // Método público estático que calcula los rangos de números aleatorios para cada nivel de demanda
        double[][] rangos = new double[DEMANDA.length][2]; // Crea una matriz bidimensional para almacenar inicio y fin de cada rango
        double inicio = 0.0; // Inicializa el valor de inicio del primer rango en 0.0
        for (int i = 0; i < DEMANDA.length; i++) { // Itera sobre cada nivel de demanda
            rangos[i][0] = inicio; // Establece el inicio del rango actual (primera columna de la fila)
            rangos[i][1] = ACUM[i]; // Establece el fin del rango actual usando la probabilidad acumulada (segunda columna de la fila)
            inicio = ACUM[i]; // Actualiza el inicio para el siguiente rango con el fin del rango actual
        }
        return rangos; // Retorna la matriz completa con todos los rangos calculados
    }
}