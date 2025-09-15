package actividad_5.colas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidades de formato para la simulación de colas. */
public class UtilFormatoColas {
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES"));
    private static final DecimalFormat df2 = new DecimalFormat("0.00", sym);

    public static String f2(double v){ return df2.format(v); }

    /** Convierte minutos (offset desde 0) a hora HH:MM partiendo de 09:00 */
    public static String horaDesdeBase(int minutosOffset){
        int total = 9*60 + minutosOffset; // base 09:00
        int h = total/60; int m = total%60;
        return String.format("%02d:%02d", h, m);
    }
}

