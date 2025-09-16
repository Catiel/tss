package actividad_5.dulce;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class UtilFormatoDulce {
    private static final DecimalFormatSymbols sym = new DecimalFormatSymbols(new Locale("es","ES"));
    private static final DecimalFormat df2 = new DecimalFormat("Bs#,##0.00", sym);
    private static final DecimalFormat df3 = new DecimalFormat("0.0000", sym);
    public static String m2(double v){ return df2.format(v); }
    public static String p4(double v){ return df3.format(v); }
}

