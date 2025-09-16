package actividad_5.calentadores; // Define el paquete donde se encuentra esta clase

import java.text.DecimalFormat; // Importa la clase para formatear números decimales con patrones específicos
import java.text.DecimalFormatSymbols; // Importa la clase para personalizar símbolos de formato decimal (comas, puntos, etc.)
import java.util.Locale; // Importa la clase para especificar configuraciones regionales (idioma y país)

public class UtilFormatoCalent { // Clase utilitaria que proporciona métodos estáticos para formatear números en la aplicación de calentadores
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES")); // Crea símbolos de formato decimal configurados para España (usa coma como separador decimal)
    private static final DecimalFormat df2 = new DecimalFormat("0.00", sym); // Crea formateador que muestra números con exactamente 2 decimales, usando los símbolos españoles
    public static String f2(double v){ return df2.format(v); } // Método público que recibe un double y retorna su representación como string formateado a 2 decimales
}
