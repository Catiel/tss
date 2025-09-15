package actividad_5.sangre;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class UtilFormatoSangre {
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES"));
    private static final DecimalFormat df2 = new DecimalFormat("0.00", sym);

    public static String fmt2(double v){ return df2.format(v); }
}

