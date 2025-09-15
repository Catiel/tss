package actividad_5.urnas;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Utilidades de formato y parseo decimal con coma o punto. */
public class UtilFormatoUrnas {
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES"));
    private static final DecimalFormat df = new DecimalFormat("0.00", sym);

    public static String fmt(double v){ return df.format(v); }

    public static Double parse(String texto) throws NumberFormatException {
        if(texto==null) throw new NumberFormatException("nulo");
        String t = texto.trim().replace(',', '.');
        if(t.isEmpty()) throw new NumberFormatException("vacío");
        return Double.parseDouble(t);
    }
}

