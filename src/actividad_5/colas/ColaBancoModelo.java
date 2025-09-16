package actividad_5.colas; // Define el paquete donde se encuentra esta clase

/** Modelo de distribuciones para la cola del banco (ventanilla auto).
 *  Tiempos de servicio (min): 0:0.00, 1:0.25, 2:0.20, 3:0.40, 4:0.15
 *  Tiempos entre llegadas (min): 0:0.10, 1:0.35, 2:0.25, 3:0.15, 4:0.10, 5:0.05
 */
public class ColaBancoModelo { // Clase que modela las distribuciones de probabilidad para tiempos en una cola bancaria
    public static final int[] SERVICIO_VALORES = {0,1,2,3,4}; // Array con los posibles tiempos de servicio en minutos (0 a 4 minutos)
    public static final double[] SERVICIO_PROBS = {0.00,0.25,0.20,0.40,0.15}; // Probabilidades correspondientes para cada tiempo de servicio

    public static final int[] LLEGADA_VALORES = {0,1,2,3,4,5}; // Array con los posibles tiempos entre llegadas en minutos (0 a 5 minutos)
    public static final double[] LLEGADA_PROBS = {0.10,0.35,0.25,0.15,0.10,0.05}; // Probabilidades correspondientes para cada tiempo entre llegadas

    // Distribuciones acumuladas precalculadas
    private static final double[] SERVICIO_ACUM = {0.00, 0.25, 0.45, 0.85, 1.00}; // Probabilidades acumuladas para tiempos de servicio, precalculadas para eficiencia
    private static final double[] LLEGADA_ACUM = {0.10, 0.45, 0.70, 0.85, 0.95, 1.00}; // Probabilidades acumuladas para tiempos entre llegadas, precalculadas para eficiencia

    /** Determina el tiempo de servicio basado en un número aleatorio r [0,1) */
    public static int tiempoServicio(double r){ // Método que convierte un número aleatorio en tiempo de servicio según la distribución
        if(r < 0 || r >= 1) return -1; // Valida que el número aleatorio esté en el rango válido [0,1), retorna -1 si no es válido
        for(int i = 0; i < SERVICIO_ACUM.length; i++){ // Itera sobre las probabilidades acumuladas de servicio
            if(r < SERVICIO_ACUM[i]) return SERVICIO_VALORES[i]; // Si el número aleatorio cae en este rango, retorna el tiempo de servicio correspondiente
        }
        return SERVICIO_VALORES[SERVICIO_VALORES.length - 1]; // Retorna el último valor de servicio como fallback (aunque no debería llegar aquí con r válido)
    }

    /** Determina el tiempo entre llegadas basado en un número aleatorio r [0,1) */
    public static int tiempoInterLlegada(double r){ // Método que convierte un número aleatorio en tiempo entre llegadas según la distribución
        if(r < 0 || r >= 1) return -1; // Valida que el número aleatorio esté en el rango válido [0,1), retorna -1 si no es válido
        for(int i = 0; i < LLEGADA_ACUM.length; i++){ // Itera sobre las probabilidades acumuladas de llegadas
            if(r < LLEGADA_ACUM[i]) return LLEGADA_VALORES[i]; // Si el número aleatorio cae en este rango, retorna el tiempo entre llegadas correspondiente
        }
        return LLEGADA_VALORES[LLEGADA_VALORES.length - 1]; // Retorna el último valor de llegadas como fallback (aunque no debería llegar aquí con r válido)
    }

    /** Obtiene los rangos de números aleatorios para tiempo de servicio */
    public static double[][] getRangosServicio(){ // Método que genera una matriz con los rangos de números aleatorios para tiempos de servicio
        double[][] rangos = new double[SERVICIO_VALORES.length][2]; // Crea matriz bidimensional: filas=valores de servicio, columnas=[inicio,fin]
        double inicio = 0.0; // Inicializa el punto de inicio del primer rango
        for(int i = 0; i < SERVICIO_VALORES.length; i++){ // Itera sobre todos los valores de tiempo de servicio
            rangos[i][0] = inicio; // inicio del rango - Establece el inicio del rango para este valor de servicio
            rangos[i][1] = SERVICIO_ACUM[i]; // fin del rango - Establece el fin del rango usando la probabilidad acumulada
            inicio = SERVICIO_ACUM[i]; // El inicio del siguiente rango es el fin del rango actual
        }
        return rangos; // Retorna la matriz completa con todos los rangos de servicio
    }

    /** Obtiene los rangos de números aleatorios para tiempo entre llegadas */
    public static double[][] getRangosLlegada(){ // Método que genera una matriz con los rangos de números aleatorios para tiempos entre llegadas
        double[][] rangos = new double[LLEGADA_VALORES.length][2]; // Crea matriz bidimensional: filas=valores de llegada, columnas=[inicio,fin]
        double inicio = 0.0; // Inicializa el punto de inicio del primer rango
        for(int i = 0; i < LLEGADA_VALORES.length; i++){ // Itera sobre todos los valores de tiempo entre llegadas
            rangos[i][0] = inicio; // inicio del rango - Establece el inicio del rango para este valor de llegada
            rangos[i][1] = LLEGADA_ACUM[i]; // fin del rango - Establece el fin del rango usando la probabilidad acumulada
            inicio = LLEGADA_ACUM[i]; // El inicio del siguiente rango es el fin del rango actual
        }
        return rangos; // Retorna la matriz completa con todos los rangos de llegada
    }
}