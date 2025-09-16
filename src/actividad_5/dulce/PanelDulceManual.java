package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel manual: permite editar los números aleatorios (hasta 100 filas) y evaluar distintas decisiones Q con la misma muestra. */
public class PanelDulceManual extends JPanel {
    private final JTextField txtDecisiones; // lista de Q
    private final JSpinner spQDetalle;      // Q para tabla detalle
    private final JButton btnCargarFijos;
    private final JButton btnGenerarAleatorios;
    private final JButton btnCalcular;
    private final JButton btnComparar;

    private final DefaultTableModel modeloRandoms; // r editable
    private final DefaultTableModel modeloDetalle; // replica, r, demanda, ganancia (Q detalle)
    private final DefaultTableModel modeloComparativa; // Q, promedio

    private final JLabel lblPromedioDetalle;

    public PanelDulceManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Dulce Ada - Simulación manual (editar números aleatorios)");
        EstilosUI.aplicarEstiloTitulo(titulo); add(titulo, BorderLayout.NORTH);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT)); EstilosUI.aplicarEstiloPanel(barra);
        barra.add(new JLabel("Decisiones Q (coma):"));
        txtDecisiones = new JTextField("40,50,60,70,80,90",16); barra.add(txtDecisiones);
        barra.add(new JLabel("Q detalle:"));
        spQDetalle = new JSpinner(new SpinnerNumberModel(60,1,500,1)); barra.add(spQDetalle);
        btnCargarFijos = new JButton("Cargar 100 fijos"); EstilosUI.aplicarEstiloBoton(btnCargarFijos); barra.add(btnCargarFijos);
        btnGenerarAleatorios = new JButton("Aleatorios"); EstilosUI.aplicarEstiloBoton(btnGenerarAleatorios); barra.add(btnGenerarAleatorios);
        btnCalcular = new JButton("Calcular Q detalle"); EstilosUI.aplicarEstiloBoton(btnCalcular); barra.add(btnCalcular);
        btnComparar = new JButton("Comparar Qs"); EstilosUI.aplicarEstiloBoton(btnComparar); barra.add(btnComparar);
        add(barra, BorderLayout.BEFORE_FIRST_LINE);

        // Tabla de randoms editables
        modeloRandoms = new DefaultTableModel(new Object[]{"Replica","r"},0){
            @Override public boolean isCellEditable(int r,int c){ return c==1; }
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tablaR = new JTable(modeloRandoms); EstilosUI.aplicarEstiloTabla(tablaR); tablaR.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaR.getColumnModel().getColumn(0).setPreferredWidth(60); tablaR.getColumnModel().getColumn(1).setPreferredWidth(70);
        JScrollPane spR = new JScrollPane(tablaR); spR.setBorder(BorderFactory.createTitledBorder("Números aleatorios r (0-1)"));

        // Tabla detalle
        modeloDetalle = new DefaultTableModel(new Object[]{"Replica","r","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaDet = new JTable(modeloDetalle); EstilosUI.aplicarEstiloTabla(tablaDet); tablaDet.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchosD={60,70,70,85}; for(int i=0;i<anchosD.length;i++) tablaDet.getColumnModel().getColumn(i).setPreferredWidth(anchosD[i]);
        JScrollPane spDet = new JScrollPane(tablaDet); spDet.setBorder(BorderFactory.createTitledBorder("Detalle para Q"));

        // Comparativa
        modeloComparativa = new DefaultTableModel(new Object[]{"Q","Promedio"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaComp = new JTable(modeloComparativa); EstilosUI.aplicarEstiloTabla(tablaComp); tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(60); tablaComp.getColumnModel().getColumn(1).setPreferredWidth(110);
        JScrollPane spComp = new JScrollPane(tablaComp); spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones"));

        JSplitPane splitLeft = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spR, spComp); splitLeft.setResizeWeight(0.55); splitLeft.setOneTouchExpandable(true);
        JSplitPane splitMain = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, splitLeft, spDet); splitMain.setResizeWeight(0.40); splitMain.setOneTouchExpandable(true);
        add(splitMain, BorderLayout.CENTER);

        lblPromedioDetalle = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblPromedioDetalle); lblPromedioDetalle.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblPromedioDetalle, BorderLayout.SOUTH);

        // Listeners
        btnCargarFijos.addActionListener(this::cargarFijos);
        btnGenerarAleatorios.addActionListener(this::generarAleatorios);
        btnCalcular.addActionListener(this::calcularDetalle);
        btnComparar.addActionListener(this::comparar);

        // Inicial
        cargarFijos(null);
        calcularDetalle(null);
        comparar(null);
    }

    private void cargarFijos(ActionEvent e){
        modeloRandoms.setRowCount(0);
        for(int i=0;i<DulceModelo.RAND_FIJOS.length;i++){
            modeloRandoms.addRow(new Object[]{i+1,String.format("%.4f",DulceModelo.RAND_FIJOS[i])});
        }
    }

    private void generarAleatorios(ActionEvent e){
        int n = DulceModelo.RAND_FIJOS.length; modeloRandoms.setRowCount(0);
        for(int i=0;i<n;i++) modeloRandoms.addRow(new Object[]{i+1,String.format("%.4f",Math.random())});
    }

    private Double parseR(Object v){ if(v==null) return null; String t=v.toString().trim().replace(',','.'); if(t.isEmpty()) return null; try{ double d=Double.parseDouble(t); if(d<0||d>=1) return null; return d; }catch(Exception ex){ return null; } }

    private double[] obtenerRandoms(){
        int n = modeloRandoms.getRowCount(); double[] arr = new double[n];
        for(int i=0;i<n;i++){
            Double r = parseR(modeloRandoms.getValueAt(i,1)); if(r==null){ throw new IllegalArgumentException("Fila "+(i+1)+" r inválido"); }
            arr[i] = r;
        }
        return arr;
    }

    private void calcularDetalle(ActionEvent e){
        try{
            int Q = (int) spQDetalle.getValue();
            double[] rs = obtenerRandoms();
            modeloDetalle.setRowCount(0); double suma=0;
            for(int i=0;i<rs.length;i++){
                int d = DulceModelo.demandaPara(rs[i]); double g = DulceModelo.ganancia(Q,d); suma+=g;
                modeloDetalle.addRow(new Object[]{i+1,String.format("%.4f",rs[i]), d, UtilFormatoDulce.m2(g)});
            }
            lblPromedioDetalle.setText("Promedio Q="+Q+": "+UtilFormatoDulce.m2(suma/rs.length));
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void comparar(ActionEvent e){
        try{
            double[] rs = obtenerRandoms();
            modeloComparativa.setRowCount(0); double mejor=-Double.MAX_VALUE; int mejorQ=-1; double mejorProm=0;
            String[] partes = txtDecisiones.getText().split(",");
            for(String p: partes){ p=p.trim(); if(p.isEmpty()) continue; try{ int Q=Integer.parseInt(p); double[] g = DulceModelo.simularGanancias(Q, rs); double prom = DulceModelo.promedio(g); if(prom>mejor){mejor=prom; mejorQ=Q; mejorProm=prom;} modeloComparativa.addRow(new Object[]{Q, UtilFormatoDulce.m2(prom)});}catch(Exception ignored){} }
            if(mejorQ!=-1) modeloComparativa.addRow(new Object[]{"Mejor", mejorQ+" ("+UtilFormatoDulce.m2(mejorProm)+")"});
        }catch(Exception ex){ JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }
}

