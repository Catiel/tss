package actividad_5.dulce; // Define el paquete donde se encuentra esta clase utilitaria

import java.text.DecimalFormat; // Importa la clase para formatear números decimales con patrones específicos
import java.text.DecimalFormatSymbols; // Importa la clase para definir símbolos personalizados de formato decimal
import java.util.Locale; // Importa la clase para manejar configuraciones regionales específicas

public class UtilFormatoDulce { // Declara la clase utilitaria para formatear números en la simulación de dulces
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES")); // Crea símbolos de formato para configuración regional de España (coma como separador decimal)
    private static final DecimalFormat df2 = new DecimalFormat("Bs#,##0.00", sym); // Crea formateador para moneda boliviana con 2 decimales, separadores de miles y prefijo "Bs"
    private static final DecimalFormat df3 = new DecimalFormat("0.0000", sym); // Crea formateador para números decimales con exactamente 4 posiciones decimales
    public static String m2(double v){ return df2.format(v); } // Método público estático que formatea un valor como moneda boliviana con 2 decimales
    public static String p4(double v){ return df3.format(v); } // Método público estático que formatea un valor como decimal con 4 posiciones decimales
}
