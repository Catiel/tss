package actividad_5.colas; // Define el paquete donde se encuentra esta clase

import java.text.DecimalFormat; // Importa la clase para formatear números decimales con patrones específicos
import java.text.DecimalFormatSymbols; // Importa la clase para personalizar símbolos de formato decimal (comas, puntos, etc.)
import java.util.Locale; // Importa la clase para especificar configuraciones regionales (idioma y país)

/** Utilidades de formato para la simulación de colas. */
public class UtilFormatoColas { // Clase utilitaria que proporciona métodos estáticos para formatear números y tiempos en simulaciones de colas bancarias
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES")); // Crea símbolos de formato decimal configurados para España (usa coma como separador decimal)
    private static final DecimalFormat df2 = new DecimalFormat("0.00", sym); // Crea formateador que muestra números con exactamente 2 decimales, usando los símbolos españoles

    public static String f2(double v){ return df2.format(v); } // Método público que recibe un double y retorna su representación como string formateado a 2 decimales

    /** Convierte minutos (offset desde 0) a hora HH:MM partiendo de 09:00 */
    public static String horaDesdeBase(int minutosOffset){ // Método que convierte un offset en minutos a formato de hora real, asumiendo que el banco abre a las 09:00
        int total = 9*60 + minutosOffset; // base 09:00 - Calcula el total de minutos desde medianoche: 540 minutos (09:00) + el offset proporcionado
        int h = total/60; int m = total%60; // Convierte el total de minutos a horas (división entera) y minutos restantes (módulo 60)
        return String.format("%02d:%02d", h, m); // Retorna la hora formateada como HH:MM con ceros a la izquierda si es necesario
    }
}
