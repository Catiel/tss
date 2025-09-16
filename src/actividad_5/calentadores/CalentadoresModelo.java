package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

/** Modelo de ventas semanales de calentadores (datos de 50 semanas).
 *  Valores de venta: 4..10 con probabilidades derivadas de frecuencias: 6,5,9,12,8,7,3.
 *  Probabilidades: 0.12,0.10,0.18,0.24,0.16,0.14,0.06 (suman 1.00)
 */
public class CalentadoresModelo { // Clase que modela la distribución de probabilidad de ventas de calentadores
    public static final int INVENTARIO_FIJO = 8; // inventario semanal para el inciso a) - Inventario constante de 8 unidades por semana
    public static final int[] VENTAS = {4,5,6,7,8,9,10}; // Array con los posibles valores de venta semanal (4 a 10 unidades)
    public static final double[] PROBS = {0.12,0.10,0.18,0.24,0.16,0.14,0.06}; // Probabilidades correspondientes a cada valor de venta

    private static final double[] ACUMULADA; // Array para almacenar probabilidades acumuladas
    static { // Bloque de inicialización estático que se ejecuta al cargar la clase
        ACUMULADA = new double[PROBS.length]; // Inicializa el array de probabilidades acumuladas con el mismo tamaño que PROBS
        double acum=0; for(int i=0;i<PROBS.length;i++){ acum+=PROBS[i]; ACUMULADA[i] = (i==PROBS.length-1)?1.0:acum; } // Calcula probabilidades acumuladas sumando cada probabilidad a la anterior, asegurando que la última sea exactamente 1.0
    }

    /** Devuelve ventas simuladas para un número aleatorio r en [0,1). */
    public static int ventasPara(double r){ // Método que convierte un número aleatorio en un valor de ventas según la distribución
        if(r<0 || r>=1) return -1; // Valida que el número aleatorio esté en el rango válido [0,1), retorna -1 si no es válido
        for(int i=0;i<ACUMULADA.length;i++) if(r < ACUMULADA[i]) return VENTAS[i]; // Busca en qué intervalo de probabilidad acumulada cae r y retorna el valor de venta correspondiente
        return VENTAS[VENTAS.length-1]; // Retorna el último valor de ventas como fallback (aunque no debería llegar aquí con r válido)
    }

    /** Valor esperado analítico. */
    public static double esperado(){ double s=0; for(int i=0;i<VENTAS.length;i++) s += VENTAS[i]*PROBS[i]; return s; } // Calcula el valor esperado de ventas multiplicando cada valor por su probabilidad y sumando todos los productos
}
