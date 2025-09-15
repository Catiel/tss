package actividad_5.urnas;

/** Modelo de la urna: 100 pelotas (10% verdes, 40% rojas, 50% amarillas). */
public class UrnaModelo {
    public static final double P_VERDE = 0.10;    // Prob verde
    public static final double P_ROJA = 0.40;     // Prob roja
    public static final double P_AMARILLA = 0.50; // Prob amarilla

    private static final double AC_VERDE = P_VERDE;                 // 0.10
    private static final double AC_ROJA = P_VERDE + P_ROJA;         // 0.50
    private static final double AC_AMARILLA = P_VERDE + P_ROJA + P_AMARILLA; // 1.00

    /** Devuelve el color asociado a un número aleatorio en [0,1). */
    public static String colorPara(double r){
        if(r < 0 || r > 1) return "FUERA_RANGO"; // Validación simple
        if(r < AC_VERDE) return "verdes";        // [0,0.10)
        if(r < AC_ROJA) return "rojas";          // [0.10,0.50)
        return "amarillas";                      // [0.50,1.00]
    }
}

