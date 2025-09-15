package actividad_5.urnas; // Paquete donde se encuentra la clase de utilidades de formato

import java.text.DecimalFormat; // Importa clase para formatear números decimales
import java.text.DecimalFormatSymbols; // Importa clase para símbolos de formato (coma, punto)
import java.util.Locale; // Importa clase para definir la configuración regional

/** Utilidades de formato y parseo decimal con coma o punto. */ // Comentario descriptivo de la clase
public class UtilFormatoUrnas { // Inicio de la clase UtilFormatoUrnas
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES")); // Define símbolos (coma decimal) para España
    private static final DecimalFormat df = new DecimalFormat("0.00", sym); // Define formateador a 2 decimales usando esos símbolos

    public static String fmt(double v){ return df.format(v); } // Método que formatea un double (ej 0.1 -> "0,10")

    public static Double parse(String texto) throws NumberFormatException { // Método que convierte texto a Double aceptando coma o punto
        if(texto==null) throw new NumberFormatException("nulo"); // Valida nulo
        String t = texto.trim().replace(',', '.'); // Recorta espacios y reemplaza coma por punto
        if(t.isEmpty()) throw new NumberFormatException("vacío"); // Valida cadena vacía
        return Double.parseDouble(t); // Convierte a double estándar y retorna
    } // Fin método parse
} // Fin clase UtilFormatoUrnas
