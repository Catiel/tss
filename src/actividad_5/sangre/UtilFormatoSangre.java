package actividad_5.sangre; // Define el paquete donde se encuentra esta clase utilitaria para el módulo de simulación de sangre

import java.text.DecimalFormat; // Importa la clase para formatear números decimales con patrones específicos
import java.text.DecimalFormatSymbols; // Importa la clase para definir símbolos personalizados de formato decimal
import java.util.Locale; // Importa la clase para manejar configuraciones regionales específicas

public class UtilFormatoSangre { // Declara la clase utilitaria para formatear números en la simulación de sangre/plasma
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES")); // Crea símbolos de formato para configuración regional de España (coma como separador decimal)
    private static final DecimalFormat df2 = new DecimalFormat("0.00", sym); // Crea formateador para números decimales con exactamente 2 posiciones decimales usando símbolos españoles

    public static String fmt2(double v){ return df2.format(v); } // Método público estático que formatea un valor como decimal con exactamente 2 posiciones decimales
}
