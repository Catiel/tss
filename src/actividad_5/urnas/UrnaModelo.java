package actividad_5.urnas; // Paquete del modelo de urna

/** Modelo de la urna: 100 pelotas (10% verdes, 40% rojas, 50% amarillas). */ // Descripción de la clase
public class UrnaModelo { // Inicio de la clase UrnaModelo
    public static final double P_VERDE = 0.10;    // Probabilidad individual de pelota verde
    public static final double P_ROJA = 0.40;     // Probabilidad individual de pelota roja
    public static final double P_AMARILLA = 0.50; // Probabilidad individual de pelota amarilla

    private static final double AC_VERDE = P_VERDE;                 // Acumulada hasta verdes = 0.10
    private static final double AC_ROJA = P_VERDE + P_ROJA;         // Acumulada hasta rojas = 0.50
    private static final double AC_AMARILLA = P_VERDE + P_ROJA + P_AMARILLA; // Acumulada total = 1.00

    /** Devuelve el color asociado a un número aleatorio en [0,1). */ // Comentario del método
    public static String colorPara(double r){ // Firma del método colorPara
        if(r < 0 || r > 1) return "FUERA_RANGO"; // Si está fuera de rango devuelve indicador de error
        if(r < AC_VERDE) return "verdes";        // Menor que 0.10 -> verde
        if(r < AC_ROJA) return "rojas";          // Menor que 0.50 (y >=0.10) -> roja
        return "amarillas";                      // Resto (>=0.50 y <=1) -> amarilla
    } // Fin método colorPara
} // Fin clase UrnaModelo
