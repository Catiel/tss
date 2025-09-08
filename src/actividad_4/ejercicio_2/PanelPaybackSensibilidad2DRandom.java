package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Panel de sensibilidad 2D (payback) con generación aleatoria de flujos (fila 0) y tasas (col 0).
 * (0,0) muestra el payback actual global. Valor horizonte+1 = no recupera.
 */
public class PanelPaybackSensibilidad2DRandom extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JSpinner spCols, spFilas, spFlujoMin, spFlujoMax, spTasaMin, spTasaMax;
    private JButton btnGenerar;
    private JLabel lblInfo;
    private boolean datosGenerados = false;

    public PanelPaybackSensibilidad2DRandom(){
        ControladorParametros.getInstancia().addChangeListener(this);
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));
        construirBarraSuperior();
        crearTablaBase(5,5);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scroll, BorderLayout.CENTER);
        lblInfo = new JLabel("Configure y pulse Generar Aleatorio.");
        EstilosUI.aplicarEstiloLabel(lblInfo);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(south);
        south.add(lblInfo);
        add(south, BorderLayout.SOUTH);
        onParametrosChanged();
    }

    private void construirBarraSuperior(){
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5));
        EstilosUI.aplicarEstiloPanel(top);
        top.add(new JLabel("Cols:")); spCols = new JSpinner(new SpinnerNumberModel(5,1,50,1)); top.add(spCols);
        top.add(new JLabel("Filas:")); spFilas = new JSpinner(new SpinnerNumberModel(5,1,50,1)); top.add(spFilas);
        top.add(new JLabel("Flujo Min:")); spFlujoMin = new JSpinner(new SpinnerNumberModel(30.0,0.01,1e6,1.0)); top.add(spFlujoMin);
        top.add(new JLabel("Flujo Max:")); spFlujoMax = new JSpinner(new SpinnerNumberModel(100.0,0.01,1e6,1.0)); top.add(spFlujoMax);
        top.add(new JLabel("Tasa% Min:")); spTasaMin = new JSpinner(new SpinnerNumberModel(5.0,0.0,1000.0,0.5)); top.add(spTasaMin);
        top.add(new JLabel("Tasa% Max:")); spTasaMax = new JSpinner(new SpinnerNumberModel(25.0,0.0,1000.0,0.5)); top.add(spTasaMax);
        btnGenerar = new JButton("Generar Aleatorio");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        btnGenerar.addActionListener(e->generarAleatorio());
        top.add(btnGenerar);
        add(top, BorderLayout.NORTH);
    }

    private void crearTablaBase(int numFlujos, int numTasas){
        String[] cols = new String[numFlujos+1]; cols[0]="Tasa/Flujo"; for(int i=1;i<cols.length;i++) cols[i]="F"+i;
        modelo = new DefaultTableModel(cols,0){ @Override public boolean isCellEditable(int r,int c){return false;} };
        Object[] fila0 = new Object[numFlujos+1]; for(int c=0;c<fila0.length;c++) fila0[c]=""; modelo.addRow(fila0);
        for(int r=0;r<numTasas;r++){ Object[] f = new Object[numFlujos+1]; for(int c=0;c<f.length;c++) f[c]=""; modelo.addRow(f);}
        if(tabla==null){
            tabla = new JTable(modelo){
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                    Component comp = super.prepareRenderer(renderer,row,column);
                    if(row==0||column==0) comp.setBackground(new Color(240,240,255)); else comp.setBackground(Color.WHITE);
                    return comp; }
            }; EstilosUI.aplicarEstiloTabla(tabla); tabla.setFillsViewportHeight(true);
        } else tabla.setModel(modelo);
        datosGenerados=false;
    }

    private void generarAleatorio(){
        int cols=(Integer)spCols.getValue(); int filas=(Integer)spFilas.getValue();
        double fMin=((Number)spFlujoMin.getValue()).doubleValue(); double fMax=((Number)spFlujoMax.getValue()).doubleValue();
        double tMin=((Number)spTasaMin.getValue()).doubleValue(); double tMax=((Number)spTasaMax.getValue()).doubleValue();
        if(fMax<=fMin || tMax<tMin || fMin<=0){ lblInfo.setText("Rangos inválidos"); return; }
        crearTablaBase(cols,filas);
        ThreadLocalRandom rnd=ThreadLocalRandom.current();
        for(int c=1;c<=cols;c++){ double v=fMin+rnd.nextDouble()*(fMax-fMin); modelo.setValueAt(formato(v),0,c);}
        for(int r=1;r<=filas;r++){ double t=tMin+rnd.nextDouble()*(tMax-tMin); modelo.setValueAt(String.format(Locale.US,"%.2f",t),r,0);}
        calcularResultadosDesdeModelo();
        actualizarCeldaEsquina();
        lblInfo.setText("Generado (h+1 = no recupera)");
    }

    private void calcularResultadosDesdeModelo(){
        if(modelo==null) return; int totalCols=modelo.getColumnCount(); int totalRows=modelo.getRowCount();
        if(totalCols<=1||totalRows<=1) return;
        double[] flujos=new double[totalCols-1]; boolean flujosOk=true;
        for(int c=1;c<totalCols;c++){ flujos[c-1]=parse(modelo.getValueAt(0,c)); if(Double.isNaN(flujos[c-1])||flujos[c-1]<=0){ flujosOk=false; break; } }
        double[] tasas=new double[totalRows-1]; boolean tasasOk=true;
        for(int r=1;r<totalRows;r++){ tasas[r-1]=parse(modelo.getValueAt(r,0))/100.0; if(Double.isNaN(tasas[r-1])||tasas[r-1]<0){ tasasOk=false; break; } }
        if(!flujosOk || !tasasOk){ datosGenerados=false; limpiarResultados(); return; }
        datosGenerados=true;
        ControladorParametros p=ControladorParametros.getInstancia();
        double inversion=p.getInversionOriginal(); int horizonte=p.getHorizonteAnios();
        for(int r=1;r<totalRows;r++) for(int c=1;c<totalCols;c++){
            int pay=PaybackCalculo.calcularPeriodo(inversion, flujos[c-1], tasas[r-1], horizonte);
            modelo.setValueAt(pay==-1? horizonte+1: pay, r,c);
        }
    }

    private void limpiarResultados(){ int tc=modelo.getColumnCount(); int tr=modelo.getRowCount(); for(int r=1;r<tr;r++) for(int c=1;c<tc;c++) modelo.setValueAt("",r,c); }

    private void actualizarCeldaEsquina(){
        ControladorParametros p=ControladorParametros.getInstancia();
        int pay=PaybackCalculo.calcularPeriodo(p.getInversionOriginal(), p.getFlujoAnio1(), p.getTasaCrecimientoAnual(), p.getHorizonteAnios());
        if(modelo!=null && modelo.getRowCount()>0) modelo.setValueAt(pay==-1? p.getHorizonteAnios()+1: pay,0,0);
    }

    private double parse(Object v){ if(v==null) return Double.NaN; String s=v.toString().trim(); if(s.isEmpty()) return Double.NaN; if(s.endsWith("%")) s=s.substring(0,s.length()-1).trim(); s=s.replace(',','.'); try{return Double.parseDouble(s);}catch(Exception e){return Double.NaN;} }
    private String formato(double d){ return Math.abs(d-Math.rint(d))<1e-6? String.format(Locale.US,"%.0f",d): String.format(Locale.US,"%.2f",d);}

    @Override public void onParametrosChanged(){ SwingUtilities.invokeLater(() -> { actualizarCeldaEsquina(); if(datosGenerados) calcularResultadosDesdeModelo(); }); }

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); }
}
