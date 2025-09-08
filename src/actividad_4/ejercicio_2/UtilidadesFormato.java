package actividad_4.ejercicio_2; // Paquete del ejercicio 2

import java.math.BigDecimal;          // Clase para operaciones decimales de alta precisión
import java.math.RoundingMode;         // Modos de redondeo
import java.text.DecimalFormat;        // Formato numérico personalizado
import java.text.DecimalFormatSymbols; // Símbolos (separadores, moneda)
import java.util.Locale;               // Locale para consistencia de símbolos

/**
 * Clase utilitaria para manejar operaciones con decimales y formateo de números
 * con precisión específica.
 */
public class UtilidadesFormato { // Clase de utilidades (solo métodos estáticos)

    /**
     * Redondea un valor double a dos decimales.
     * @param valor Valor a redondear
     * @return Valor redondeado con precisión de dos decimales
     */
    public static double redondearDosDecimales(double valor) { // Método de redondeo estándar
        BigDecimal bd = new BigDecimal(Double.toString(valor)); // Envuelve el double evitando problemas binarios
        bd = bd.setScale(2, RoundingMode.HALF_UP);              // Ajusta a 2 decimales con HALF_UP (comercial)
        return bd.doubleValue();                                // Devuelve como double
    }

    /**
     * Formatea un valor numérico como moneda (con separador de miles, dos decimales y símbolo $).
     * @param valor El valor a formatear
     * @return Cadena formateada como moneda
     */
    public static String formatearMoneda(double valor) { // Formato monetario estándar
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US); // Usa Locale US (punto decimal, coma miles)
        DecimalFormat formato = new DecimalFormat("$#,##0.00", simbolos);   // Patrón: $ + miles + 2 decimales
        return formato.format(redondearDosDecimales(valor));                // Redondea y formatea
    }

    /**
     * Formatea un valor numérico como porcentaje con dos decimales.
     * @param valor El valor a formatear (en formato decimal, ej: 0.10 para 10%)
     * @return Cadena formateada como porcentaje con dos decimales
     */
    public static String formatearPorcentaje(double valor) { // Formato porcentaje
        DecimalFormat formato = new DecimalFormat("0.00%");               // Patrón porcentaje con 2 decimales
        return formato.format(redondearDosDecimales(valor));               // Redondea y aplica formato (multiplica internamente por 100)
    }
}
