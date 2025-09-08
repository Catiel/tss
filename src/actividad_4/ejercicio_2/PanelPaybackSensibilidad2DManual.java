package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de sensibilidad 2D manual editable EN LA MISMA TABLA.
 * Estructura de la tabla:
 *  - Fila 0: encabezado de flujos (columna 0 = etiqueta "Tasa/Flujo", columnas 1..N = valores de Flujo Año 1 editables)
 *  - Filas 1..M: cada fila representa una tasa de crecimiento (columna 0 = tasa % editable). El resto de celdas son resultados calculados (payback) no editables.
 * Flujo y tasa se introducen en bruto (flujo en millones, tasa en %). Botón "Calcular" genera los paybacks.
 */
public class PanelPaybackSensibilidad2DManual extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JSpinner spCols; // número de flujos
    private JSpinner spFilas; // número de tasas
    private JButton btnGenerar;
    private JButton btnCalcular;
    private JLabel lblInfo;

    public PanelPaybackSensibilidad2DManual(){
        ControladorParametros.getInstancia().addChangeListener(this);
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        // Barra superior de configuración
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        EstilosUI.aplicarEstiloPanel(top);
        top.add(new JLabel("Columnas (Flujos):"));
        spCols = new JSpinner(new SpinnerNumberModel(5,1,30,1));
        top.add(spCols);
        top.add(new JLabel("Filas (Tasas):"));
        spFilas = new JSpinner(new SpinnerNumberModel(5,1,30,1));
        top.add(spFilas);
        btnGenerar = new JButton("Generar tabla");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        btnGenerar.addActionListener(e->generarTabla());
        top.add(btnGenerar);
        btnCalcular = new JButton("Calcular");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        btnCalcular.addActionListener(e->calcularResultados());
        top.add(btnCalcular);
        add(top, BorderLayout.NORTH);

        // Modelo inicial vacío
        crearTablaBase(5,5);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scroll, BorderLayout.CENTER);

        lblInfo = new JLabel("Edite flujos (fila superior) y tasas % (columna izquierda). Luego pulse Calcular.");
        EstilosUI.aplicarEstiloLabel(lblInfo);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(south);
        south.add(lblInfo);
        add(south, BorderLayout.SOUTH);
    }

    private void crearTablaBase(int numFlujos, int numTasas){
        // Column identifiers: 1 + numFlujos
        String[] cols = new String[numFlujos + 1];
        cols[0] = "Tasa/Flujo";
        for(int i=1;i<cols.length;i++) cols[i] = "F"+i; // temporales hasta que usuario llene

        modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int row,int col){
                // Editable sólo flujos (fila 0 excepto col0) y tasas (col0 excepto fila0)
                if(row==0 && col>0) return true; // flujos
                if(col==0 && row>0) return true; // tasas
                return false; // resultados
            }
        };
        // Añadir fila 0 (flujos) inicial vacía
        Object[] filaFlujos = new Object[numFlujos + 1];
        filaFlujos[0] = "Flujos";
        for(int c=1;c<filaFlujos.length;c++) filaFlujos[c] = ""; // vacío
        modelo.addRow(filaFlujos);
        // Añadir filas de tasas
        for(int r=0;r<numTasas;r++){
            Object[] fila = new Object[numFlujos + 1];
            fila[0] = ""; // tasa % editable
            for(int c=1;c<fila.length;c++) fila[c] = ""; // resultados en blanco
            modelo.addRow(fila);
        }
        if(tabla==null){
            tabla = new JTable(modelo){
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                    Component comp = super.prepareRenderer(renderer,row,column);
                    if(row==0 || column==0) comp.setBackground(new Color(240,240,255)); else comp.setBackground(Color.WHITE);
                    return comp;
                }
            };
            EstilosUI.aplicarEstiloTabla(tabla);
            tabla.setFillsViewportHeight(true);
        } else {
            tabla.setModel(modelo);
        }
    }

    private void generarTabla(){
        int cols = (Integer) spCols.getValue();
        int filas = (Integer) spFilas.getValue();
        crearTablaBase(cols, filas);
    }

    private void calcularResultados(){
        ControladorParametros p = ControladorParametros.getInstancia();
        double inversion = p.getInversionOriginal();
        int horizonte = p.getHorizonteAnios();

        int totalCols = modelo.getColumnCount();
        int totalRows = modelo.getRowCount();

        // Leer flujos
        double[] flujos = new double[totalCols-1];
        for(int c=1;c<totalCols;c++){
            Object val = modelo.getValueAt(0,c);
            flujos[c-1] = parseDoubleOrNaN(val);
        }
        // Leer tasas
        double[] tasas = new double[totalRows-1];
        for(int r=1;r<totalRows;r++){
            Object val = modelo.getValueAt(r,0);
            tasas[r-1] = parseDoubleOrNaN(val)/100.0; // convertir % a decimal
        }

        // Validar
        boolean anyError=false;
        StringBuilder sbErrores = new StringBuilder();
        for(int i=0;i<flujos.length;i++) if(Double.isNaN(flujos[i])||flujos[i]<=0){ anyError=true; sbErrores.append("Flujo col ").append(i+1).append(" inválido. "); }
        for(int i=0;i<tasas.length;i++) if(Double.isNaN(tasas[i])||tasas[i]<0){ anyError=true; sbErrores.append("Tasa fila ").append(i+1).append(" inválida. "); }
        if(anyError){
            lblInfo.setText("Errores: "+sbErrores);
            return;
        }

        // Calcular
        for(int r=1;r<totalRows;r++){
            double tasa = tasas[r-1];
            for(int c=1;c<totalCols;c++){
                double flujo = flujos[c-1];
                int payback = PaybackCalculo.calcularPeriodo(inversion, flujo, tasa, horizonte);
                modelo.setValueAt(payback==-1?"NR":payback, r, c);
            }
            // mostrar tasa formateada de nuevo (por si ingresó decimal)
            modelo.setValueAt(String.format("%.2f", tasas[r-1]*100), r,0);
        }
        // Mostrar flujos formateados
        for(int c=1;c<totalCols;c++) modelo.setValueAt(formatoNumero(flujos[c-1]),0,c);
        lblInfo.setText("Cálculo completado. 'NR' = No recupera en horizonte="+horizonte+" años");
    }

    private double parseDoubleOrNaN(Object v){
        if(v==null) return Double.NaN;
        try { return Double.parseDouble(v.toString().trim()); } catch(Exception e){ return Double.NaN; }
    }
    private String formatoNumero(double d){
        if(Math.abs(d-Math.rint(d))<1e-6) return String.format("%.0f", d);
        return String.format("%.2f", d);
    }

    @Override public void onParametrosChanged(){ /* Recalcular sólo si ya hay datos manuales */ }

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); }
}
