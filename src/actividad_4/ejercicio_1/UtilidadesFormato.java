package actividad_4.ejercicio_1;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Clase utilitaria para manejar operaciones con decimales y formateo de números
 * con precisión específica.
 */
public class UtilidadesFormato {

    /**
     * Redondea un valor double a dos decimales.
     * @param valor Valor a redondear
     * @return Valor redondeado con precisión de dos decimales
     */
    public static double redondearDosDecimales(double valor) {
        BigDecimal bd = new BigDecimal(Double.toString(valor));
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Formatea un valor numérico como moneda (con separador de miles, dos decimales y símbolo $).
     * @param valor El valor a formatear
     * @return Cadena formateada como moneda
     */
    public static String formatearMoneda(double valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US);
        DecimalFormat formato = new DecimalFormat("$#,##0.00", simbolos);
        return formato.format(redondearDosDecimales(valor));
    }

    /**
     * Formatea un valor numérico con dos decimales.
     * @param valor El valor a formatear
     * @return Cadena formateada con dos decimales
     */
    public static String formatearDosDecimales(double valor) {
        DecimalFormat formato = new DecimalFormat("0.00");
        return formato.format(redondearDosDecimales(valor));
    }

    /**
     * Formatea un valor numérico como porcentaje con dos decimales.
     * @param valor El valor a formatear (en formato decimal, ej: 0.10 para 10%)
     * @return Cadena formateada como porcentaje con dos decimales
     */
    public static String formatearPorcentaje(double valor) {
        DecimalFormat formato = new DecimalFormat("0.00%");
        return formato.format(redondearDosDecimales(valor));
    }
}
