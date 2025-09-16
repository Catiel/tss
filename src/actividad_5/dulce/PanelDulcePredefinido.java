package actividad_5.dulce;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con la simulación usando los 100 números aleatorios fijos y permitiendo evaluar distintas decisiones (cantidades compradas). */
public class PanelDulcePredefinido extends JPanel {
    private final JSpinner spDecision; // cantidad Q seleccionada para la tabla detallada
    private final DefaultTableModel modeloDistribucion;
    private final DefaultTableModel modeloSim;
    private final DefaultTableModel modeloComparacion;
    private final JLabel lblPromedio;

    private static final int[] DECISIONES_DEFECTO = {40,50,60,70,80,90};

    public PanelDulcePredefinido(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Dulce Ada - Simulación (100 réplicas fijas)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(top);
        top.add(new JLabel("Cantidad comprada (Q):"));
        spDecision = new JSpinner(new SpinnerNumberModel(60,40,120,10));
        top.add(spDecision);
        JButton btnSimular = new JButton("Actualizar Q"); EstilosUI.aplicarEstiloBoton(btnSimular); top.add(btnSimular);
        JButton btnRecalcularComparacion = new JButton("Recalcular tabla comparativa"); EstilosUI.aplicarEstiloBoton(btnRecalcularComparacion); top.add(btnRecalcularComparacion);
        add(top, BorderLayout.BEFORE_FIRST_LINE);

        // Distribución de demanda
        modeloDistribucion = new DefaultTableModel(new Object[]{"Prob","Acum","RangoIni","RangoFin","Demanda"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribucion();
        JTable tablaDist = new JTable(modeloDistribucion); EstilosUI.aplicarEstiloTabla(tablaDist); tablaDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchosD = {60,60,70,70,65}; for(int i=0;i<anchosD.length;i++) tablaDist.getColumnModel().getColumn(i).setPreferredWidth(anchosD[i]);
        JScrollPane spDist = new JScrollPane(tablaDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de la demanda (uniforme)"));

        // Tabla simulación detallada
        modeloSim = new DefaultTableModel(new Object[]{"Replica","r","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchosS = {60,70,70,80}; for(int i=0;i<anchosS.length;i++) tablaSim.getColumnModel().getColumn(i).setPreferredWidth(anchosS[i]);
        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Tabla de simulación (100 réplicas fijas)"));

        // Panel lateral izquierdo (distribución + comparativa)
        JPanel panelIzq = new JPanel(); panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS)); EstilosUI.aplicarEstiloPanel(panelIzq);
        panelIzq.add(spDist); panelIzq.add(Box.createVerticalStrut(8));

        // Tabla comparativa de decisiones
        modeloComparacion = new DefaultTableModel(new Object[]{"Compra Q","Ganancia promedio"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaComp = new JTable(modeloComparacion); EstilosUI.aplicarEstiloTabla(tablaComp); tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130);
        JScrollPane spComp = new JScrollPane(tablaComp); spComp.setBorder(BorderFactory.createTitledBorder("Ganancias promedio por decisión (mismos r)"));
        panelIzq.add(spComp);

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, panelIzq, spSim);
        split.setResizeWeight(0.35); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblPromedio = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblPromedio); lblPromedio.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblPromedio, BorderLayout.SOUTH);

        btnSimular.addActionListener(e -> refrescarSimulacion());
        btnRecalcularComparacion.addActionListener(e -> llenarComparativa());

        // Carga inicial
        refrescarSimulacion();
        llenarComparativa();
    }

    private void llenarDistribucion(){
        modeloDistribucion.setRowCount(0);
        double prob = 1.0/ DulceModelo.DEMANDAS.length;
        double ini=0; double acum=0;
        for(int d: DulceModelo.DEMANDAS){
            acum += prob; double fin = (d==DulceModelo.DEMANDAS[DulceModelo.DEMANDAS.length-1])?1.0:acum;
            modeloDistribucion.addRow(new Object[]{String.format("%.4f",prob), String.format("%.4f",fin), String.format("%.4f",ini), String.format("%.4f",fin), d});
            ini = fin;
        }
    }

    private void refrescarSimulacion(){
        int Q = (int) spDecision.getValue();
        modeloSim.setRowCount(0);
        double suma=0;
        for(int i=0;i< DulceModelo.RAND_FIJOS.length;i++){
            double r = DulceModelo.RAND_FIJOS[i];
            int demanda = DulceModelo.demandaPara(r);
            double g = DulceModelo.ganancia(Q, demanda);
            suma += g;
            modeloSim.addRow(new Object[]{i+1, String.format("%.4f", r), demanda, UtilFormatoDulce.m2(g)});
        }
        double promedio = suma / DulceModelo.RAND_FIJOS.length;
        lblPromedio.setText("Ganancia promedio para Q="+Q+" = "+ UtilFormatoDulce.m2(promedio));
    }

    private void llenarComparativa(){
        modeloComparacion.setRowCount(0);
        double mejor=-Double.MAX_VALUE; int mejorQ=-1;
        for(int Q: DECISIONES_DEFECTO){
            double[] g = DulceModelo.simularGanancias(Q, DulceModelo.RAND_FIJOS);
            double prom = DulceModelo.promedio(g);
            if(prom>mejor){ mejor=prom; mejorQ=Q; }
            modeloComparacion.addRow(new Object[]{Q, UtilFormatoDulce.m2(prom)});
        }
        // Añadir fila resumen
        modeloComparacion.addRow(new Object[]{"Mejor Q", mejorQ+" ("+UtilFormatoDulce.m2(mejor)+")"});
    }
}

