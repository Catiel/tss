package actividad_4.ejercicio_2; // Paquete del ejercicio 2

import javax.swing.table.DefaultTableModel; // Modelo de tabla Swing
import java.util.Locale; // Locale para formateo numérico consistente

/** Utilidades compartidas para paneles de sensibilidad Payback 2D (fila0=flujos, col0=tasas). */
public final class Payback2DUtil { // Clase final (no heredable)
    private Payback2DUtil(){} // Constructor privado (clase de utilidades)

    /** Crea un modelo base con fila 0 (flujos) + N filas de tasas. */
    public static DefaultTableModel crearModeloBase(int numFlujos, int numTasas, boolean editableFlujos, boolean editableTasas){ // Factory de modelo
        String[] cols = new String[numFlujos + 1]; // Arreglo nombres columnas (incluye col 0 tasas)
        cols[0] = "Tasa/Flujo"; // Título cabecera combinada
        for(int i=1;i<cols.length;i++) cols[i] = "F"+i; // Etiquetas genéricas F1..Fn
        return new DefaultTableModel(cols,0){ // Modelo sin filas iniciales
            @Override public boolean isCellEditable(int row,int col){ // Control granular de edición
                if(row==0 && col>0) return editableFlujos; // Flujos año 1 (fila 0, cols >=1)
                if(col==0 && row>0) return editableTasas;   // Tasas (col 0, filas >=1)
                return false; // Celdas de resultados + esquina (0,0) no editables
            }
        }; // Retorna modelo configurado
    }

    /** Añade filas vacías (llamar tras crear modelo). */
    public static void inicializarFilas(DefaultTableModel modelo, int numFlujos, int numTasas){ // Inicializa estructura
        Object[] fila0 = new Object[numFlujos + 1]; // Fila de flujos (fila 0)
        for(int c=0;c<fila0.length;c++) fila0[c]=""; // Vacía cada celda
        modelo.addRow(fila0); // Añade fila 0
        for(int r=0;r<numTasas;r++){ // Itera filas de tasas
            Object[] fila = new Object[numFlujos + 1]; // Fila resultados + tasa
            for(int c=0;c<fila.length;c++) fila[c]=""; // Inicializa vacía
            modelo.addRow(fila); // Añade fila
        }
    }

    /** Parsea número permitiendo coma decimal y opcional '%'. Devuelve NaN si falla. */
    public static double parseNumero(Object v){ // Parse seguro
        if(v==null) return Double.NaN; // Null -> NaN
        String s = v.toString().trim(); // Convierte a texto y recorta
        if(s.isEmpty()) return Double.NaN; // Vacío -> NaN
        if(s.endsWith("%")) s = s.substring(0,s.length()-1).trim(); // Remueve símbolo % final
        s = s.replace(',','.'); // Normaliza coma a punto
        try { return Double.parseDouble(s); } catch(Exception e){ return Double.NaN; } // Intenta parsear
    }

    /** Formatea número (entero sin decimales o con 2 decimales). Vacío si NaN. */
    public static String formatear(double d){ // Formateo estándar
        if(Double.isNaN(d)) return ""; // NaN -> cadena vacía
        if(Math.abs(d - Math.rint(d)) < 1e-6) return String.format(Locale.US, "%.0f", d); // Casi entero -> sin decimales
        return String.format(Locale.US, "%.2f", d); // Otro caso -> 2 decimales
    }

    /** Calcula paybacks y llena celdas (r>=1,c>=1). Retorna true si todo válido. */
    public static boolean calcularYVolcar(DefaultTableModel modelo, double inversion, int horizonte){ // Motor de cálculo
        int totalCols = modelo.getColumnCount(); // Cantidad columnas
        int totalRows = modelo.getRowCount();    // Cantidad filas
        if(totalCols<=1 || totalRows<=1) return false; // Modelo insuficiente
        double[] flujos = new double[totalCols-1]; // Arreglo flujos (col 1..n)
        for(int c=1;c<totalCols;c++){ // Recorre columnas de flujos
            flujos[c-1] = parseNumero(modelo.getValueAt(0,c)); // Parse flujo
            if(Double.isNaN(flujos[c-1])||flujos[c-1]<=0) return false; // Valida positivo
        }
        double[] tasas = new double[totalRows-1]; // Arreglo tasas (fila 1..m)
        for(int r=1;r<totalRows;r++){ // Recorre filas de tasas
            tasas[r-1] = parseNumero(modelo.getValueAt(r,0))/100.0; // Parse y pasa a decimal
            if(Double.isNaN(tasas[r-1])||tasas[r-1]<0) return false; // Valida no negativa
        }
        for(int r=1;r<totalRows;r++){ // Recorre filas de resultados
            double tasa = tasas[r-1]; // Tasa fila
            for(int c=1;c<totalCols;c++){ // Recorre columnas de flujos
                int pay = PaybackCalculo.calcularPeriodo(inversion, flujos[c-1], tasa, horizonte); // Calcula payback
                modelo.setValueAt(pay==-1? horizonte+1: pay, r,c); // Inserta valor normalizado
            }
            modelo.setValueAt(String.format(Locale.US, "%.2f", tasas[r-1]*100), r,0); // Re-formatea tasa (%)
        }
        for(int c=1;c<totalCols;c++) modelo.setValueAt(formatear(flujos[c-1]),0,c); // Formatea fila de flujos
        return true; // Éxito
    }
}
