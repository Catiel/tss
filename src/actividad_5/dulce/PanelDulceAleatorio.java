package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Arrays;

/** Panel que genera una nueva muestra de números aleatorios para evaluar decisiones Q usando la MISMA muestra. */
public class PanelDulceAleatorio extends JPanel {
    private final JSpinner spReplicas; // número de réplicas
    private final JTextField txtDecisiones; // lista de decisiones Q separadas por coma
    private final JSpinner spQDetalle; // Q para tabla detallada
    private final JButton btnSimular;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComp;
    private final JLabel lblPromedio;

    private double[] randoms; // muestra actual usada por todas las decisiones

    public PanelDulceAleatorio(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Dulce Ada - Simulación aleatoria (misma muestra para comparar Q)");
        EstilosUI.aplicarEstiloTitulo(titulo); add(titulo, BorderLayout.NORTH);

        JPanel barra = new JPanel(new FlowLayout(FlowLayout.LEFT)); EstilosUI.aplicarEstiloPanel(barra);
        barra.add(new JLabel("Réplicas:")); spReplicas = new JSpinner(new SpinnerNumberModel(100,10,1000,10)); barra.add(spReplicas);
        barra.add(new JLabel("Decisiones Q (coma):")); txtDecisiones = new JTextField("40,50,60,70,80,90",18); barra.add(txtDecisiones);
        barra.add(new JLabel("Q detallado:")); spQDetalle = new JSpinner(new SpinnerNumberModel(60,1,500,1)); barra.add(spQDetalle);
        btnSimular = new JButton("Generar y evaluar"); EstilosUI.aplicarEstiloBoton(btnSimular); barra.add(btnSimular);
        add(barra, BorderLayout.BEFORE_FIRST_LINE);

        modeloSim = new DefaultTableModel(new Object[]{"Replica","r","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchosS={60,70,70,85}; for(int i=0;i<anchosS.length;i++) tablaSim.getColumnModel().getColumn(i).setPreferredWidth(anchosS[i]);

        modeloComp = new DefaultTableModel(new Object[]{"Q","Promedio"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaComp = new JTable(modeloComp); EstilosUI.aplicarEstiloTabla(tablaComp); tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(60); tablaComp.getColumnModel().getColumn(1).setPreferredWidth(110);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tablaComp), new JScrollPane(tablaSim));
        split.setResizeWeight(0.30); split.setOneTouchExpandable(true); add(split, BorderLayout.CENTER);

        lblPromedio = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblPromedio); lblPromedio.setBorder(BorderFactory.createEmptyBorder(4,8,4,8)); add(lblPromedio, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);
        simular(null); // primera corrida
    }

    private void simular(ActionEvent e){
        int n = (int) spReplicas.getValue();
        randoms = new double[n];
        for(int i=0;i<n;i++) randoms[i] = Math.random();
        Arrays.sort(randoms); // ordenamos sólo para estabilidad visual (opcional)
        int Qdet = (int) spQDetalle.getValue();
        llenarTablaDetalle(Qdet);
        llenarComparativa();
    }

    private void llenarTablaDetalle(int Q){
        modeloSim.setRowCount(0);
        double suma=0;
        for(int i=0;i<randoms.length;i++){
            double r = randoms[i]; int d = DulceModelo.demandaPara(r); double g = DulceModelo.ganancia(Q,d); suma+=g;
            modeloSim.addRow(new Object[]{i+1,String.format("%.4f",r),d,UtilFormatoDulce.m2(g)});
        }
        double prom = suma/randoms.length;
        lblPromedio.setText("Promedio para Q="+Q+": "+UtilFormatoDulce.m2(prom));
    }

    private void llenarComparativa(){
        modeloComp.setRowCount(0);
        String[] partes = txtDecisiones.getText().split(",");
        double mejor=-Double.MAX_VALUE; int mejorQ=-1; double mejorProm=0;
        for(String p: partes){
            p=p.trim(); if(p.isEmpty()) continue; try{ int Q=Integer.parseInt(p); double[] g = DulceModelo.simularGanancias(Q, randoms); double prom = DulceModelo.promedio(g); if(prom>mejor){mejor=prom; mejorQ=Q; mejorProm=prom;} modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(prom)});}catch(Exception ignored){}
        }
        if(mejorQ!=-1) modeloComp.addRow(new Object[]{"Mejor", mejorQ+" ("+UtilFormatoDulce.m2(mejorProm)+")"});
    }
}

