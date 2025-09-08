package actividad_4.ejercicio_2;

import javax.swing.table.DefaultTableModel;
import java.util.Locale;

/** Utilidades compartidas para paneles de sensibilidad Payback 2D (fila0=flujos, col0=tasas). */
public final class Payback2DUtil {
    private Payback2DUtil(){}

    /** Crea un modelo base con fila 0 (flujos) + N filas de tasas. */
    public static DefaultTableModel crearModeloBase(int numFlujos, int numTasas, boolean editableFlujos, boolean editableTasas){
        String[] cols = new String[numFlujos + 1];
        cols[0] = "Tasa/Flujo";
        for(int i=1;i<cols.length;i++) cols[i] = "F"+i;
        return new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int row,int col){
                if(row==0 && col>0) return editableFlujos; // flujos año 1
                if(col==0 && row>0) return editableTasas;   // tasas
                return false; // resultados + esquina
            }
        };
    }

    /** Añade filas vacías (llamar tras crear modelo). */
    public static void inicializarFilas(DefaultTableModel modelo, int numFlujos, int numTasas){
        Object[] fila0 = new Object[numFlujos + 1];
        for(int c=0;c<fila0.length;c++) fila0[c]="";
        modelo.addRow(fila0);
        for(int r=0;r<numTasas;r++){
            Object[] fila = new Object[numFlujos + 1];
            for(int c=0;c<fila.length;c++) fila[c]="";
            modelo.addRow(fila);
        }
    }

    /** Parsea número permitiendo coma decimal y opcional '%'. Devuelve NaN si falla. */
    public static double parseNumero(Object v){
        if(v==null) return Double.NaN;
        String s = v.toString().trim();
        if(s.isEmpty()) return Double.NaN;
        if(s.endsWith("%")) s = s.substring(0,s.length()-1).trim();
        s = s.replace(',','.');
        try { return Double.parseDouble(s); } catch(Exception e){ return Double.NaN; }
    }

    public static String formatear(double d){
        if(Double.isNaN(d)) return "";
        if(Math.abs(d - Math.rint(d)) < 1e-6) return String.format(Locale.US, "%.0f", d);
        return String.format(Locale.US, "%.2f", d);
    }

    /** Calcula paybacks y llena celdas (r>=1,c>=1). Retorna true si todo válido. */
    public static boolean calcularYVolcar(DefaultTableModel modelo, double inversion, int horizonte){
        int totalCols = modelo.getColumnCount();
        int totalRows = modelo.getRowCount();
        if(totalCols<=1 || totalRows<=1) return false;
        double[] flujos = new double[totalCols-1];
        for(int c=1;c<totalCols;c++){ flujos[c-1] = parseNumero(modelo.getValueAt(0,c)); if(Double.isNaN(flujos[c-1])||flujos[c-1]<=0) return false; }
        double[] tasas = new double[totalRows-1];
        for(int r=1;r<totalRows;r++){ tasas[r-1] = parseNumero(modelo.getValueAt(r,0))/100.0; if(Double.isNaN(tasas[r-1])||tasas[r-1]<0) return false; }
        for(int r=1;r<totalRows;r++){
            double tasa = tasas[r-1];
            for(int c=1;c<totalCols;c++){
                int pay = PaybackCalculo.calcularPeriodo(inversion, flujos[c-1], tasa, horizonte);
                modelo.setValueAt(pay==-1? horizonte+1: pay, r,c);
            }
            modelo.setValueAt(String.format(Locale.US, "%.2f", tasas[r-1]*100), r,0);
        }
        for(int c=1;c<totalCols;c++) modelo.setValueAt(formatear(flujos[c-1]),0,c);
        return true;
    }
}

