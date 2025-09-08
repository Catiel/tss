package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import java.math.BigDecimal;          // Clase para operaciones decimales de alta precisión y control de escala
import java.math.RoundingMode;         // Enumeración para definir estrategias de redondeo
import java.text.DecimalFormat;        // Clase para formatear números con patrones personalizados
import java.text.DecimalFormatSymbols; // Permite definir símbolos (separador decimal, miles, moneda) específicos
import java.util.Locale;               // Locale para asegurar consistencia en símbolos (punto decimal, etc.)

/**
 * Clase utilitaria para manejar operaciones con decimales y formateo de números
 * con precisión específica (redondeo a dos decimales, formato moneda y porcentaje).
 */
public class UtilidadesFormato { // Clase de utilidades (solo métodos estáticos, no instanciable por diseño)

    /**
     * Redondea un valor double a dos decimales usando HALF_UP (redondeo comercial estándar).
     * @param valor Valor a redondear (double original)
     * @return double redondeado a dos decimales
     */
    public static double redondearDosDecimales(double valor) { // Método de redondeo principal
        BigDecimal bd = new BigDecimal(Double.toString(valor)); // Crea BigDecimal evitando errores binarios de representación
        bd = bd.setScale(2, RoundingMode.HALF_UP);              // Ajusta escala a 2 decimales con redondeo HALF_UP
        return bd.doubleValue();                                // Devuelve resultado como double primitivo
    }

    /**
     * Formatea un número como moneda en formato US: símbolo $, separador de miles y dos decimales.
     * @param valor Valor numérico bruto (double)
     * @return Cadena formateada: ej. $1,234.56
     */
    public static String formatearMoneda(double valor) { // Método para formato monetario
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.US); // Usa Locale US (coma miles, punto decimal)
        DecimalFormat formato = new DecimalFormat("$#,##0.00", simbolos);   // Patrón: $ + agrupación miles + 2 decimales
        return formato.format(redondearDosDecimales(valor));                // Aplica redondeo y formatea
    }

    /**
     * Formatea un número decimal como porcentaje con dos decimales.
     * El valor de entrada debe estar en forma decimal (0.10 => 10%).
     * @param valor Valor decimal a convertir a porcentaje
     * @return Cadena con formato porcentaje (ej. 10.00%)
     */
    public static String formatearPorcentaje(double valor) { // Método para formato de porcentaje
        DecimalFormat formato = new DecimalFormat("0.00%");               // Patrón de porcentaje con 2 decimales
        return formato.format(redondearDosDecimales(valor));               // Redondea y formatea (multiplica internamente por 100)
    }
}
